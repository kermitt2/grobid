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
     * Lookup by journal title, volume and first page
     */
    private static final String JOURNAL_BASE_QUERY =
            "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";

    /**
     * Lookup first author surname and  article title
     */
    private static final String TITLE_BASE_QUERY =
            "query?usr=%s&pwd=%s&type=a&format=unixref&qdata=%s|%s||key|";

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web service based on
     * title and first author
     */
    public boolean consolidateCrossrefGet(BiblioItem bib, List<BiblioItem> bib2) throws Exception {
        boolean result = false;
        String doi = bib.getDOI();
        String aut = bib.getFirstAuthorSurname();
        String title = bib.getTitle();
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

        if (doi != null) {
            //System.out.println(doi);
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
                    System.err.println("EXCEPTION HANDLING CROSSREF CACHE");
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
                String subpath = String.format(DOI_BASE_QUERY, GrobidProperties.getInstance().getCrossrefId(), GrobidProperties.getInstance().getCrossrefPw(), doi);
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
        } else if ((title != null) & (aut != null)) {
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
                String subpath = String.format(TITLE_BASE_QUERY, GrobidProperties.getInstance().getCrossrefId(), GrobidProperties.getInstance().getCrossrefPw(),
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
                            pstmt2.setString(2, bib.getTitle());
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
        } else if ((firstPage != null) & (bib.getJournal() != null) & (bib.getVolume() != null)) {
            /*String urlmsg = "http://doi.crossref.org/servlet/query?usr=" + crossref_id +"&pwd="+crossref_pw +
                               "&type=a&format=unixref";
               urlmsg += "&qdata=" + "|";
               if (bib.getJournal() != null)
                   urlmsg += URLEncoder.encode(bib.getJournal());
               if (aut != null)
                   urlmsg += "|"+URLEncoder.encode(aut)+"|";
               else
                   urlmsg += "||";
               if (bib.getVolume() != null)
                   urlmsg += URLEncoder.encode(bib.getVolume());

               if (firstPage!=null)
                   urlmsg += "||"+firstPage+"|";
               else
                   urlmsg += "|||";
               if (bib.getPublicationDate() != null)
                   urlmsg += URLEncoder.encode(bib.getPublicationDate());
               urlmsg += "||KEY|";*/

            String subpath = String.format(JOURNAL_BASE_QUERY, GrobidProperties.getInstance().getCrossrefId(), GrobidProperties.getInstance().getCrossrefPw(),
                    URLEncoder.encode(bib.getJournal(), "UTF-8"),
                    URLEncoder.encode(bib.getVolume(), "UTF-8"), firstPage);
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

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web service - post version
     */
    public boolean consolidateCrossrefPostBatch(List<BiblioItem> bib, List<BiblioItem> bib2)
            throws Exception {

        int p = 0;
        String pipedQuery = null;
        for (int n = 0; n < bib.size(); n++) {

            if (p == 0) {
                //urlmsg = "http://doi.crossref.org/servlet/query?usr=" + crossref_id +"&pwd="+crossref_pw +
                //"&type=a&format=unixref&qdata=";
                pipedQuery = "";
            }

            BiblioItem bibo = bib.get(n);
            String aut = bibo.getFirstAuthorSurname();
            if ((bibo.getTitle() != null) & (aut != null)) {
                if (p != 0)
                    pipedQuery += "\n";
                pipedQuery += bibo.getTitle() + "|" + aut + "||key" + n + "|";
            }

            if (p == 9) {

                //pipedQuery += bib.getTitle() + "|" + bib.getFirstAuthorSurname() + "||key|";

                /*	  		System.out.println("Sending: " + pipedQuery);
                    HTTPClient.NVPair[] uploadOpts = new HTTPClient.NVPair[3];

                    uploadOpts[0] = new HTTPClient.NVPair ("usr", crossref_id);
                    uploadOpts[1] = new HTTPClient.NVPair ("pwd", crossref_pw);
                    uploadOpts[2] = new HTTPClient.NVPair ("qdata", pipedQuery);
                    //uploadOpts[3] = new HTTPClient.NVPair ("operation", "doQueryUpload");
                    //uploadOpts[4] = new HTTPClient.NVPair ("format", "unixref");

                    HTTPClient.HTTPConnection httpConn = new HTTPClient.HTTPConnection(crossref_host, crossref_port);

                    HTTPClient.CookieModule.setCookiePolicyHandler(null);
                    HTTPClient.HTTPResponse httpResp = null;

                    httpResp = httpConn.Post("/servlet/query?usr=" + crossref_id + "&pwd=" + crossref_pw +
                                        "&type=a&format=unixref",
                                        uploadOpts);
                    System.out.println("httpResp status is "+ httpResp.getStatusCode());
                    String responseString = httpResp.getText();
                    System.out.println(responseString);

                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(responseString));

                    //BiblioItem bibo2 = new BiblioItem();
                       //DefaultHandler crossref = new CrossrefUnixrefSaxParser(bib2);

                       // get a factory
                       SAXParserFactory spf = SAXParserFactory.newInstance();
                       //get a new instance of parser
                    SAXParser parser = spf.newSAXParser();
                    parser.parse(is, crossref);

                    //is.close();

                    httpConn.stop();*/
                p = 0;
            } else
                p++;
        }

        // we need to finish the remaining entries
        if (p != 0) {
            System.out.println("Sending: " + pipedQuery);
            /*HTTPClient.NVPair[] uploadOpts = new HTTPClient.NVPair[3];

               uploadOpts[0] = new HTTPClient.NVPair ("usr", crossref_id);
               uploadOpts[1] = new HTTPClient.NVPair ("pwd", crossref_pw);
               uploadOpts[2] = new HTTPClient.NVPair ("qdata", pipedQuery);
       //doQueryUpload
               HTTPClient.HTTPConnection httpConn = new HTTPClient.HTTPConnection(crossref_host, crossref_port);

               HTTPClient.CookieModule.setCookiePolicyHandler(null);
               HTTPClient.HTTPResponse httpResp = null;

               httpResp = httpConn.Post("/servlet/query?usr=" + crossref_id +
                                       "&pwd=" + crossref_pw + "&type=a&format=unixref",
                                       uploadOpts);
               System.out.println("httpResp status is "+ httpResp.getStatusCode());
               String responseString = httpResp.getText();
               System.out.println(responseString);

               InputSource is = new InputSource();
               is.setCharacterStream(new StringReader(responseString));

               //BiblioItem bibo2 = new BiblioItem();
                  //DefaultHandler crossref = new CrossrefUnixrefSaxParser(bib2);

               // get a factory
                  SAXParserFactory spf = SAXParserFactory.newInstance();
               //get a new instance of parser
               SAXParser parser = spf.newSAXParser();
               parser.parse(is, crossref);

               //is.close();

               httpConn.stop();*/
        }

        return true;
    }


}
