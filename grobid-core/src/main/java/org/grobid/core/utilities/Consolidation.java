package org.grobid.core.utilities;

import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.sax.CrossrefUnixrefSaxParser;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class for managing the extraction of bibliographical informations from pdf documents.
 *
 * @author Patrice Lopez
 */
public class Consolidation {

    // Cache crossref BD (allowed by the user crossref service agreement)
    private Connection cCon = null;
  
    public Consolidation() {
    }

    /**
     * Open database connection
     */
    public void openDb() throws ClassNotFoundException,
            InstantiationException,
            IllegalAccessException,
            SQLException {
        // compose database url: jdbc:mysql://<hostname>:<port>/<database>
        String dbUrl2 = "jdbc:mysql://"
                + GrobidProperties.getInstance().getMySQLHost()
                + ":"
                + GrobidProperties.getInstance().getMySQLPort()
                + "/" + GrobidProperties.getInstance().getMySQLDBName() + "?useUnicode=true&characterEncoding=utf8";

        // instantiate mysql driver manager, get database connection
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            cCon = DriverManager.getConnection(dbUrl2, GrobidProperties.getInstance().getMySQLUsername(), GrobidProperties.getInstance().getMySQLPw());
            if (cCon != null) {
                cCon.createStatement().execute("SET NAMES utf8");
            }
        } catch (Exception e) {
            System.err.println("The connection to the MySQL database could not be established. \n"
                    + "The call to Crossref service will not be cached.");
        }
    }

    /**
     * Close database connection
     */
    public void closeDb() {
        try {
            if (cCon != null) {
                cCon.close();
            }
        } catch (SQLException se) {
        }
    }

	/**
	 *  SQL queries for the DOI cache
	 */
    static final String INSERT_CROSSREF_SQL =
            "INSERT INTO AuthorTitle (Author, Title, Unixref) VALUES (?,?,?)";
    static final String INSERT_CROSSREF_SQL2 =
            "INSERT INTO AllSubFields (Request, Unixref) VALUES (?,?)";
    static final String INSERT_CROSSREF_SQL3 =
            "INSERT INTO DOIRequest (Request, Unixref) VALUES (?,?)";

    static final String QUERY_CROSSREF_SQL =
            "SELECT Unixref FROM AuthorTitle WHERE Author LIKE ? AND Title LIKE ?";
    static final String QUERY_CROSSREF_SQL2 =
            "SELECT Unixref FROM AllSubFields WHERE Request LIKE ?";
    static final String QUERY_CROSSREF_SQL3 =
            "SELECT Unixref FROM DOIRequest WHERE Request DOIRequest ?";

    /**
     * Lookup by DOI
     */
    private static final String DOI_BASE_QUERY =
            "openurl?url_ver=Z39.88-2004&pid=%s:%s&rft_id=info:doi/%s&noredirect=true&format=unixref";

    /**
     * Lookup by journal title, volume and first page - 6 possible parameters are id, password, title, author, volume, firstPage.
     */
    private static final String JOURNAL_BASE_QUERY =
            //"query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";
		    "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s|%s|%s||%s|||KEY|";

    /**
     * Lookup first author surname and  article title - 4 parameters are id, password, title, author.
     */
    private static final String TITLE_BASE_QUERY =
            "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=%s|%s||key|";


	/**
     * Try to consolidate some uncertain bibliographical data with crossref web service based on
     * core metadata
     */
	public boolean consolidate(BiblioItem bib, List<BiblioItem> additionalBiblioInformation) throws Exception {
	 	boolean result = false;
		//List<BiblioItem> additionalBiblioInformation = new ArrayList<Biblio>();
		boolean valid = false;

		String doi = bib.getDOI();
        String aut = bib.getFirstAuthorSurname();
        String title = bib.getTitle();
		String journalTitle = bib.getJournal();
		String volume = bib.getVolume();
		
        String firstPage = null;
        String pageRange = bib.getPageRange();
        int beginPage = bib.getBeginPage();
        if (beginPage != -1) {
            firstPage = "" + beginPage;
        } else if (pageRange != null) {
            StringTokenizer st = new StringTokenizer(pageRange, "--");
            if (st.countTokens() == 2) {
                firstPage = st.nextToken();
            } else if (st.countTokens() == 1)
                firstPage = pageRange;
        }

        if (aut != null) {
            aut = TextUtilities.removeAccents(aut);
        }
        if (title != null) {
            title = TextUtilities.removeAccents(title);
        }
		
		try {
			if (StringUtils.isNotBlank(doi)) {
				// retrieval per DOI
				valid = consolidateCrossrefGetByDOI(bib, additionalBiblioInformation);
			}
			else if (StringUtils.isNotBlank(journalTitle) 
						&& StringUtils.isNotBlank(volume)
						&& StringUtils.isNotBlank(firstPage)) {
				// retrieval per journal title, volume, first page
				valid = consolidateCrossrefGetByJournalVolumeFirstPage(aut, firstPage, journalTitle, 
					volume, bib, additionalBiblioInformation);
			}
			else if (StringUtils.isNotBlank(title)
						&& StringUtils.isNotBlank(aut)) {
				// retrieval per first author and article title
				valid = consolidateCrossrefGetByAuthorTitle(aut, title, bib, additionalBiblioInformation);
			}
		}
		catch(Exception e) {
			throw new GrobidException("An exception occured while running Grobid consolidation.", e);
		}
		return valid;
	}
	
	/**
	 *  Try to consolidate some uncertain bibliographical data with crossref web service based on 
	 *  the DOI if it is around
	 * 	@param biblio the Biblio item to be consolidated
	 * 	@param bib2 the list of biblio items found as consolidations
	 *  @return Returns a boolean indicating if at least one bibliographical object 
	 *  has been retrieved.
	 */
	public boolean consolidateCrossrefGetByDOI(BiblioItem biblio, List<BiblioItem> bib2) throws Exception {
		boolean result = false;
		String doi = biblio.getDOI();

		if (StringUtils.isNotBlank(doi)) {
            // some cleaning of the doi
            if (doi.startsWith("doi:") | doi.startsWith("DOI:")) {
                doi.substring(4, doi.length());
                doi = doi.trim();
            }

            doi = doi.replace(" ", "");
            String xml = null;

            // we check if the entry is not already in the DB
            if (cCon != null) {
                PreparedStatement pstmt = null;
                try {
                    pstmt = cCon.prepareStatement(QUERY_CROSSREF_SQL3);
                    pstmt.setString(1, doi);

                    ResultSet res = pstmt.executeQuery();
                    if (res.next()) {
                        xml = res.getString(1);
                    }
                    res.close();
                    pstmt.close();
                } catch (SQLException se) {
                    //System.err.println("EXCEPTION HANDLING CROSSREF CACHE");
                    throw new GrobidException("EXCEPTION HANDLING CROSSREF CACHE.", se);
//           			se.printStackTrace();
                } finally {
                    try {
                        if (pstmt != null)
                            pstmt.close();
                    } catch (SQLException se) {
                    }
                }

                if (xml != null) {
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(xml));

                    DefaultHandler crossref = new CrossrefUnixrefSaxParser(bib2);

                    // get a factory
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    //get a new instance of parser
                    SAXParser parser = spf.newSAXParser();
                    parser.parse(is, crossref);

                    if (bib2.size() > 0) {
                        if (!bib2.get(0).getError())
                            result = true;
                    }
                }
            }

            if (xml == null) {
                String subpath = String.format(DOI_BASE_QUERY, 
						GrobidProperties.getInstance().getCrossrefId(), 
						GrobidProperties.getInstance().getCrossrefPw(), 
						doi);
                URL url = new URL("http://" + GrobidProperties.getInstance().getCrossrefHost() + "/" + subpath);

                System.out.println("Sending: " + url.toString());
                HttpURLConnection urlConn = null;
                try {
                    urlConn = (HttpURLConnection) url.openConnection();
                } 
				catch (Exception e) {
                    try {
                        urlConn = (HttpURLConnection) url.openConnection();
                    } catch (Exception e2) {
//						e2.printStackTrace();
                        urlConn = null;
                        throw new GrobidException("An exception occured while running Grobid.", e2);
                    }
                }
                if (urlConn != null) {
                    try {
                        urlConn.setDoOutput(true);
                        urlConn.setDoInput(true);
                        urlConn.setRequestMethod("GET");

                        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                        InputStream in = urlConn.getInputStream();
                        xml = TextUtilities.convertStreamToString(in);

                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(xml));

                        DefaultHandler crossref = new CrossrefUnixrefSaxParser(bib2);

                        // get a factory
                        SAXParserFactory spf = SAXParserFactory.newInstance();
                        //get a new instance of parser
                        SAXParser parser = spf.newSAXParser();
                        parser.parse(is, crossref);

                        if (bib2.size() > 0) {
                            if (!bib2.get(0).getError())
                                result = true;
                        }

                        urlConn.disconnect();
                    } catch (Exception e) {
                        System.err.println("Warning: Consolidation set true, " +
                                "but the online connection to Crossref fails.");
                    }

                    if (cCon != null) {
                        // we put the answer (even in case of failure) un the DB
                        PreparedStatement pstmt2 = null;
                        try {
                            pstmt2 = cCon.prepareStatement(INSERT_CROSSREF_SQL3);
                            pstmt2.setString(1, doi);
                            pstmt2.setString(2, xml);
                            pstmt2.executeUpdate();
                            pstmt2.close();
                        } catch (SQLException se) {
                            System.err.println("EXCEPTION HANDLING CROSSREF UPDATE");
                        } finally {
                            try {
                                if (pstmt2 != null)
                                    pstmt2.close();
                            } catch (SQLException se) {
                            }
                        }
                    }
                }
            }
        }
		return result;
	}

	/**
	 * Try to consolidate some uncertain bibliographical data with crossref web service based on 
	 * title and first author.
	 *
	 * @param biblio the biblio item to be consolidated
	 * @param biblioList the list of biblio items found as consolidations
	 * @return Returns a boolean indicating whether at least one bibliographical object has been retrieved.
	 */
	public boolean consolidateCrossrefGetByAuthorTitle(String aut, String title, 
								BiblioItem biblio, List<BiblioItem> bib2) throws Exception {

		boolean result = false;
		// conservative check
		if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(aut)) {
			String xml = null;
            // we check if the entry is not already in the DB
            if (cCon != null) {
                PreparedStatement pstmt = null;

                try {
                    pstmt = cCon.prepareStatement(QUERY_CROSSREF_SQL);
                    pstmt.setString(1, aut);
                    pstmt.setString(2, title);

                    ResultSet res = pstmt.executeQuery();
                    if (res.next()) {
                        xml = res.getString(1);
                    }
                    res.close();
                    pstmt.close();
                } catch (SQLException se) {
//           			System.err.println("EXCEPTION HANDLING CROSSREF CACHE");
//           			se.printStackTrace();
                    throw new GrobidException("EXCEPTION HANDLING CROSSREF CACHE", se);
                } finally {
                    try {
                        if (pstmt != null)
                            pstmt.close();
                    } catch (SQLException se) {
                    }
                }

                if (xml != null) {
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(xml));

                    DefaultHandler crossref = new CrossrefUnixrefSaxParser(bib2);

                    // get a factory
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    //get a new instance of parser
                    SAXParser parser = spf.newSAXParser();
                    parser.parse(is, crossref);

                    if (bib2.size() > 0) {
                        if (!bib2.get(0).getError())
                            result = true;
                    }
                }
            }
            if (xml == null) {
                String subpath = String.format(TITLE_BASE_QUERY, 
						GrobidProperties.getInstance().getCrossrefId(), 
						GrobidProperties.getInstance().getCrossrefPw(),
                        URLEncoder.encode(title, "UTF-8"),
                        URLEncoder.encode(aut, "UTF-8"));
                URL url = new URL("http://" + GrobidProperties.getInstance().getCrossrefHost() + "/" + subpath);

                System.out.println("Sending: " + url.toString());
                HttpURLConnection urlConn = null;
                try {
                    urlConn = (HttpURLConnection) url.openConnection();
                } catch (Exception e) {
                    try {
                        urlConn = (HttpURLConnection) url.openConnection();
                    } catch (Exception e2) {
//						e2.printStackTrace();
                        urlConn = null;
                        throw new GrobidException("An exception occured while running Grobid.", e2);
                    }
                }
                if (urlConn != null) {
                    try {
                        urlConn.setDoOutput(true);
                        urlConn.setDoInput(true);
                        urlConn.setRequestMethod("GET");

                        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                        InputStream in = urlConn.getInputStream();
                        xml = TextUtilities.convertStreamToString(in);

                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(xml));

                        DefaultHandler crossref = new CrossrefUnixrefSaxParser(bib2);

                        // get a factory
                        SAXParserFactory spf = SAXParserFactory.newInstance();
                        //get a new instance of parser
                        SAXParser parser = spf.newSAXParser();
                        parser.parse(is, crossref);

                        if (bib2.size() > 0) {
                            if (!bib2.get(0).getError())
                                result = true;
                        }

                        urlConn.disconnect();
                    } catch (Exception e) {
                        System.err.println("Warning: Consolidation set true, " +
                                "but the online connection to Crossref fails.");
                    }

                    if (cCon != null) {
                        // we put the answer (even in case of failure) un the DB
                        PreparedStatement pstmt2 = null;
                        try {
                            pstmt2 = cCon.prepareStatement(INSERT_CROSSREF_SQL);
                            pstmt2.setString(1, aut);
                            pstmt2.setString(2, title);
                            pstmt2.setString(3, xml);
                            pstmt2.executeUpdate();
                            pstmt2.close();
                        } catch (SQLException se) {
                            System.err.println("EXCEPTION HANDLING CROSSREF UPDATE");
                        } finally {
                            try {
                                if (pstmt2 != null)
                                    pstmt2.close();
                            } catch (SQLException se) {
                            }
                        }
                    }
                }
            }
		}
		return result;
	}

	/**
	 *  Try to consolidate some uncertain bibliographical data with crossref web service based on 
	 *  the following core information: journal title, volume and first page. 
	 *  We use also the first author if it is there, it can help...
	 *
	 *  @param biblio the biblio item to be consolidated
	 * 	@param biblioList the list of biblio items found as consolidations
	 *  @return Returns a boolean indicating if at least one bibliographical object
	 *  has been retrieve.
	 */
	public boolean consolidateCrossrefGetByJournalVolumeFirstPage(String aut, 
				String firstPage, 
				String journal,
				String volume,
				BiblioItem biblio, 
				List<BiblioItem> bib2) throws Exception {

		boolean result = false;
		// conservative check
		if (StringUtils.isNotBlank(firstPage) &&
		 		(StringUtils.isNotBlank(aut) || (StringUtils.isNotBlank(journal) && StringUtils.isNotBlank(volume)))
		   ) {
			String subpath = String.format(JOURNAL_BASE_QUERY, 
					GrobidProperties.getInstance().getCrossrefId(), 
					GrobidProperties.getInstance().getCrossrefPw(),
                    URLEncoder.encode(journal, "UTF-8"),
					URLEncoder.encode(aut, "UTF-8"),
                    URLEncoder.encode(volume, "UTF-8"), 
					firstPage);
            URL url = new URL("http://" + GrobidProperties.getInstance().getCrossrefHost() + "/" + subpath);
            String urlmsg = url.toString();
            System.out.println(urlmsg);

            String xml = null;

            if (cCon != null) {
                // we check if the query/entry is not already in the DB
                PreparedStatement pstmt = null;

                try {
                    pstmt = cCon.prepareStatement(QUERY_CROSSREF_SQL2);
                    pstmt.setString(1, urlmsg);

                    ResultSet res = pstmt.executeQuery();
                    if (res.next()) {
                        xml = res.getString(1);
                    }
                    res.close();
                    pstmt.close();
                } catch (SQLException se) {
                    System.err.println("EXCEPTION HANDLING CROSSREF CACHE");
//           			se.printStackTrace();
                    throw new GrobidException("EXCEPTION HANDLING CROSSREF CACHE.", se);
                } finally {
                    try {
                        if (pstmt != null)
                            pstmt.close();
                    } catch (SQLException se) {
                    }
                }


                if (xml != null) {
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(xml));

                    DefaultHandler crossref = new CrossrefUnixrefSaxParser(bib2);

                    // get a factory
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    //get a new instance of parser
                    SAXParser parser = spf.newSAXParser();
                    parser.parse(is, crossref);

                    if (bib2.size() > 0) {
                        if (!bib2.get(0).getError())
                            result = true;
                    }
                }
            }

            if (xml == null) {
                System.out.println("Sending: " + urlmsg);
                HttpURLConnection urlConn = null;
                try {
                    urlConn = (HttpURLConnection) url.openConnection();
                } catch (Exception e) {
                    try {
                        urlConn = (HttpURLConnection) url.openConnection();
                    } catch (Exception e2) {
                        urlConn = null;
                        throw new GrobidException("An exception occured while running Grobid.", e2);
                    }
                }
                if (urlConn != null) {
                    try {
                        urlConn.setDoOutput(true);
                        urlConn.setDoInput(true);
                        urlConn.setRequestMethod("GET");

                        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                        InputStream in = urlConn.getInputStream();
                        xml = TextUtilities.convertStreamToString(in);

                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(xml));

                        DefaultHandler crossref = new CrossrefUnixrefSaxParser(bib2);

                        // get a factory
                        SAXParserFactory spf = SAXParserFactory.newInstance();
                        //get a new instance of parser
                        SAXParser p = spf.newSAXParser();
                        p.parse(is, crossref);
                        if (bib2.size() > 0) {
                            if (!bib2.get(0).getError())
                                result = true;
                        }

                        in.close();
                        urlConn.disconnect();
                    } catch (Exception e) {
                        System.err.println("Warning: Consolidation set true, " +
                                "but the online connection to Crossref fails.");
                    }

                    if (cCon != null) {
                        // we put the answer (even in case of failure) un the DB
                        PreparedStatement pstmt2 = null;
                        try {
                            pstmt2 = cCon.prepareStatement(INSERT_CROSSREF_SQL2);
                            pstmt2.setString(1, urlmsg);
                            pstmt2.setString(2, xml);
                            pstmt2.executeUpdate();
                            pstmt2.close();
                        } catch (SQLException se) {
                            System.err.println("EXCEPTION HANDLING CROSSREF UPDATE");
                        } finally {
                            try {
                                if (pstmt2 != null)
                                    pstmt2.close();
                            } catch (SQLException se) {
                            }
                        }
                    }
                }
            }
		}
		return result;
	}

}
