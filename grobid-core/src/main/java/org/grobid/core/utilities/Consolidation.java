package org.grobid.core.utilities;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.collections4.CollectionUtils;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.sax.CrossrefUnixrefSaxParser;
import org.grobid.core.utilities.crossref.*;
import org.grobid.core.utilities.counters.CntManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

import com.rockymadden.stringmetric.similarity.RatcliffObershelpMetric;
import scala.Option;

/**
 * Class for managing the extraction of bibliographical information from pdf documents.
 * When consolidation operations are realized, be sure to call the close() method
 * to ensure that all Executors are terminated.
 *
 * @author Patrice Lopez
 */
public class Consolidation {
    private static final Logger LOGGER = LoggerFactory.getLogger(Consolidation.class);

    private CrossrefClient client = null;
    private WorkDeserializer workDeserializer = null;
    private CntManager cntManager = null;

    public Consolidation(CntManager cntManager) {
        this.cntManager = cntManager;
        //client = new CrossrefClient();
        client = CrossrefClient.getInstance();
        workDeserializer = new WorkDeserializer();
    }

    /**
     * After consolidation operations, this need to be called to ensure that all
     * involved Executors are shut down immediatly, otherwise non terminated thread
     * could prevent the JVM from exiting
     */
    public void close() {
        //client.close();
    }

    /**
     * Lookup by DOI - 3 parameters are id, password, doi.
     */
    private static final String DOI_BASE_QUERY =
            "openurl?url_ver=Z39.88-2004&pid=%s:%s&rft_id=info:doi/%s&noredirect=true&format=unixref";

    /**
     * Lookup by journal title, volume and first page - 6 parameters are id, password, journal title, author, volume, firstPage.
     */
    private static final String JOURNAL_AUTHOR_BASE_QUERY =
            //"query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";
            "servlet/query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s|%s|%s||%s|||KEY|";

    // ISSN|TITLE/ABBREV|FIRST AUTHOR|VOLUME|ISSUE|START PAGE|YEAR|RESOURCE TYPE|KEY|DOI

    /**
     * Lookup by journal title, volume and first page - 6 parameters are id, password, journal title, volume, firstPage.
     */
    private static final String JOURNAL_BASE_QUERY =
            //"query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";
            "servlet/query?usr=%s&pwd=%s&type=a&format=unixref&qdata=|%s||%s||%s|||KEY|";

    /**
     * Lookup first author surname and  article title - 4 parameters are id, password, title, author.
     */
    private static final String TITLE_BASE_QUERY =
            "servlet/query?usr=%s&pwd=%s&type=a&format=unixref&qdata=%s|%s||key|";


    /**
     * Try to consolidate one bibliographical object with crossref web services based on
     * core metadata
     */
    public boolean consolidate(BiblioItem bib, List<BiblioItem> additionalBiblioInformation) throws Exception {
        boolean result = false;
        boolean valid = false;

        String doi = bib.getDOI();
        String aut = bib.getFirstAuthorSurname();
        String title = bib.getTitle();
        String journalTitle = bib.getJournal();
        String volume = bib.getVolume();
        if (StringUtils.isBlank(volume))
            volume = bib.getVolumeBlock();

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
        if (journalTitle != null) {
            journalTitle = TextUtilities.removeAccents(journalTitle);
        }
        if (cntManager != null)
            cntManager.i(ConsolidationCounters.CONSOLIDATION);

        try {
            if (StringUtils.isNotBlank(doi)) {
                // retrieval per DOI
                //System.out.println("test retrieval per DOI");
                valid = consolidateCrossrefGetByDOI(bib, additionalBiblioInformation);
            }  
            if (!valid && StringUtils.isNotBlank(title)
                    && StringUtils.isNotBlank(aut)) {
                // retrieval per first author and article title
                //additionalBiblioInformation.clear();
                //System.out.println("test retrieval per title, author");
                valid = consolidateCrossrefGetByAuthorTitle(aut, title, bib, additionalBiblioInformation);
                /*if (!valid) {
                    valid = consolidateCrossrefGetByAuthorTitleLibrary(aut, title, bib, additionalBiblioInformation);
                }*/
            }



            /*if (!valid && StringUtils.isNotBlank(journalTitle)
                    && StringUtils.isNotBlank(volume)
                    && StringUtils.isNotBlank(aut)
                    && StringUtils.isNotBlank(firstPage)) {
                // retrieval per journal title, author, volume, first page
                //System.out.println("test retrieval per journal title, author, volume, first page");
                additionalBiblioInformation.clear();
                valid = consolidateCrossrefGetByJournalVolumeFirstPage(aut, firstPage, journalTitle,
                        volume, bib, additionalBiblioInformation);
            }*/
            /*if (!valid && StringUtils.isNotBlank(journalTitle) 
                        && StringUtils.isNotBlank(volume)
                        && StringUtils.isNotBlank(firstPage)) {
                // retrieval per journal title, volume, first page
                additionalBiblioInformation.clear();
                //System.out.println("test retrieval per journal title, volume, first page");
                valid = consolidateCrossrefGetByJournalVolumeFirstPage(null, firstPage, journalTitle, 
                    volume, bib, additionalBiblioInformation);
            }*/
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid consolidation.", e);
        }

        if (valid && (cntManager != null))
            cntManager.i(ConsolidationCounters.CONSOLIDATION_SUCCESS);
        return valid;
    }


    /**
     * Try tp consolidate a list of bibliographical objects in one operation with CrossRef web services
     */
    public Map<Integer,BiblioItem> consolidate(List<BibDataSet> biblios) {    
        if (CollectionUtils.isEmpty(biblios))
            return null;

        final Map<Integer,BiblioItem> results = new HashMap<Integer,BiblioItem>();
        // init the results
        int n = 0;
        for(n=0; n<biblios.size(); n++) {
            results.put(new Integer(n), null);
        }
        n = 0;
        long threadId = Thread.currentThread().getId();
        for(BibDataSet bibDataSet : biblios) {
            final BiblioItem theBiblio = bibDataSet.getResBib();

            // first we get the exploitable metadata
            String doi = theBiblio.getDOI();
            String aut = theBiblio.getFirstAuthorSurname();
            String title = theBiblio.getTitle();
            String journalTitle = theBiblio.getJournal();
           
            if (aut != null) {
                aut = TextUtilities.removeAccents(aut);
            }
            if (title != null) {
                title = TextUtilities.removeAccents(title);
            }
            if (journalTitle != null) {
                journalTitle = TextUtilities.removeAccents(journalTitle);
            }

            Map<String, String> arguments = null;

            // request id
            //final int id = n;

            if (StringUtils.isNotBlank(doi)) {
                // call based on the identified DOI
                doi = cleanDoi(doi);
                arguments = null;
            } else if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(aut)) {
                // call based on partial metadata
                doi = null;
                arguments = new HashMap<String,String>();
                arguments.put("query.title", title);
                arguments.put("query.author", aut);
                if (StringUtils.isNotBlank(journalTitle))
                     arguments.put("query.container-title", journalTitle);

                arguments.put("rows", "1"); // we just request the top-one result
            } 

            if ((doi == null) && (arguments == null)) {
                //results.put(new Integer(n), null);
                n++;
                continue;
            }

            try {
                //CrossrefRequestListener<BiblioItem> requestListener = new CrossrefRequestListener<BiblioItem>();
                client.<BiblioItem>pushRequest("works", doi, arguments, workDeserializer, threadId, new CrossrefRequestListener<BiblioItem>(n) {
                    
                    @Override
                    public void onSuccess(List<BiblioItem> res) {
                        //System.out.println("Success request "+id);
                        //System.out.println("size of results: " + res.size());
                        if ((res != null) && (res.size() > 0) ) {
                            // we need here to post-check that the found item corresponds
                            // correctly to the one requested in order to avoid false positive
                            for(BiblioItem oneRes : res) {
                                if (postValidation(theBiblio, oneRes)) {
                                    results.put(new Integer(getRank()), oneRes);
                                    break;
                                }
                            }
                        } 
                    }

                    @Override
                    public void onError(int status, String message, Exception exception) {
                        LOGGER.info("ERROR ("+status+") : "+message);
                        System.out.println("ERROR ("+status+") : "+message);
                        //exception.printStackTrace();
                    }
                });
            } catch(Exception e) {
                LOGGER.error("Consolidation error - " + ExceptionUtils.getStackTrace(e));
                //results.put(new Integer(id), null);
            } 
            n++;
        }
//System.out.println("before finish, result size is " + results.size());
        client.finish(threadId);
//System.out.println("after finish, result size is " + results.size());
/*int consolidated = 0;
for (Entry<Integer, BiblioItem> cursor : results.entrySet()) {
System.out.println("item: " + cursor.getKey());
if (cursor.getValue() != null) {
System.out.println(cursor.getValue().toTEI(1));
consolidated++;
} 

}
System.out.println("total (CrossRef JSON search API): " + consolidated + " / " + results.size());*/

// fallback with CrossRef metadata look-up web service

/*if (cursor.getValue() != null) {
    valid = consolidateCrossrefGetByAuthorTitleLibrary(aut, title, bib, additionalBiblioInformation);
}*/

        return results;
    }

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web service based on
     * the DOI if it is around
     *
     * @param biblio the Biblio item to be consolidated
     * @param bib2   the list of biblio items found as consolidations
     * @return Returns a boolean indicating if at least one bibliographical object
     * has been retrieved.
     */
    public boolean consolidateCrossrefGetByDOI(BiblioItem biblio, List<BiblioItem> bib2) throws Exception {
        boolean result = false;
        String doi = biblio.getDOI();

        if (bib2 == null)
            return false;

        if (StringUtils.isNotBlank(doi)) {
            // some cleaning of the doi
            doi = cleanDoi(doi);

            long threadId = Thread.currentThread().getId();
            CrossrefRequestListener<BiblioItem> requestListener = new CrossrefRequestListener<BiblioItem>();
            client.<BiblioItem>pushRequest("works", doi, null, workDeserializer, threadId, requestListener);
            if (cntManager != null)
                cntManager.i(ConsolidationCounters.CONSOLIDATION_PER_DOI);

            synchronized (requestListener) {
                try {
                    requestListener.wait(5000); // timeout after 5 seconds
                } catch (InterruptedException e) {
                    LOGGER.error("Timeout error - " + ExceptionUtils.getStackTrace(e));
                }
            }
            
            CrossrefRequestListener.Response<BiblioItem> response = requestListener.getResponse();
            
            if (response == null)
                LOGGER.error("No response ! Maybe timeout.");
            
            else if (response.hasError() || !response.hasResults())
                LOGGER.error("error: ("+response.status+") : "+response.errorMessage);
            
            else { // success
                LOGGER.info("Success request "+ doi);
                if (cntManager != null)
                    cntManager.i(ConsolidationCounters.CONSOLIDATION_PER_DOI_SUCCESS);
                for (BiblioItem bib : response.results) {
                    bib2.add(bib);
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web service based on
     * title and first author.
     *
     * @param biblio     the biblio item to be consolidated
     * @param biblioList the list of biblio items found as consolidations
     * @return Returns a boolean indicating whether at least one bibliographical object has been retrieved.
     */
    public boolean consolidateCrossrefGetByAuthorTitle(String aut, String title,
                                                       final BiblioItem biblio, final List<BiblioItem> bib2) throws Exception {
        boolean result = false;

        if (bib2 == null)
            return false;
        int originalSize = bib2.size();

        // conservative check
        if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(aut)) {
            
            Map<String, String> arguments = new HashMap<String,String>();
            arguments.put("query.title", title);
            arguments.put("query.author", aut);
            arguments.put("rows", "5"); // we just request the top-one result
            
            long threadId = Thread.currentThread().getId();
            CrossrefRequestListener<BiblioItem> requestListener = new CrossrefRequestListener<BiblioItem>();
            client.<BiblioItem>pushRequest("works", null, arguments, workDeserializer, threadId, new CrossrefRequestListener<BiblioItem>() {
                
                @Override
                public void onSuccess(List<BiblioItem> res) {
                    //System.out.println("size of results: " + res.size());
                    if ((res != null) && (res.size() > 0) ) {
                        // we need here to post-check that the found item corresponds
                        // correctly to the one requested in order to avoid false positive
                        for(BiblioItem oneRes : res) {
                            if (postValidation(biblio, oneRes)) {
                                bib2.add(oneRes);
                            }
                        }
                    }
                }

                @Override
                public void onError(int status, String message, Exception exception) {
                    LOGGER.info("ERROR ("+status+") : "+message);
                }
            });

            /*client.<BiblioItem>pushRequest("works", null, arguments, workDeserializer, requestListener);
            if (cntManager != null)
                cntManager.i(ConsolidationCounters.CONSOLIDATION_PER_DOI);

            synchronized (requestListener) {
                try {
                    requestListener.wait(5000); // timeout after 5 seconds
                } catch (InterruptedException e) {
                    LOGGER.error("Timeout error - " + ExceptionUtils.getStackTrace(e));
                }
            }
            
            CrossrefRequestListener.Response<BiblioItem> response = requestListener.getResponse();
            
            if (response == null)
                LOGGER.error("No response ! Maybe timeout.");
            
            else if (response.hasError() || !response.hasResults())
                LOGGER.error("error: ("+response.status+") : "+response.errorMessage);
            
            else { // success
                LOGGER.info("Success request "+ arguments.toString());
                if (cntManager != null)
                    cntManager.i(ConsolidationCounters.CONSOLIDATION_PER_DOI_SUCCESS);
                for (BiblioItem bib : response.results) {
                    if (postValidation(biblio, bib)) 
                        bib2.add(bib);
                    result = true;
                }
            }*/

            for(BiblioItem bib : bib2) {
                //System.out.println(bib.toTEI(0));
            }

            /*String subpath = String.format(TITLE_BASE_QUERY,
                    GrobidProperties.getInstance().getCrossrefId(),
                    GrobidProperties.getInstance().getCrossrefPw(),
                    URLEncoder.encode(title, "UTF-8"),
                    URLEncoder.encode(aut, "UTF-8"));
            URL url = new URL("http://" + GrobidProperties.getInstance().getCrossrefHost() + "/" + subpath);

            LOGGER.info("Sending: " + url.toString());
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
                    String xml = TextUtilities.convertStreamToString(in);

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
                } catch (Exception e) {
                    LOGGER.error("Warning: Consolidation set true, " +
                            "but the online connection to Crossref fails.");
                } finally {
                    urlConn.disconnect();
                }
            }*/
        }
        if (bib2.size() > originalSize)
            return true;
        else
            return false;
    }

    public boolean consolidateCrossrefGetByAuthorTitleLibrary(String aut, String title,
                                                       BiblioItem biblio, List<BiblioItem> bib2) throws Exception {
        boolean result = false;

        if (bib2 == null)
            return false;

        // conservative check
        if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(aut)) {
            
            String subpath = String.format(TITLE_BASE_QUERY,
                    GrobidProperties.getInstance().getCrossrefId(),
                    GrobidProperties.getInstance().getCrossrefPw(),
                    URLEncoder.encode(title, "UTF-8"),
                    URLEncoder.encode(aut, "UTF-8"));
            URL url = new URL("http://" + GrobidProperties.getInstance().getCrossrefHost() + "/" + subpath);

            LOGGER.info("Sending: " + url.toString());
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
                    String xml = TextUtilities.convertStreamToString(in);

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
                } catch (Exception e) {
                    LOGGER.error("Warning: Consolidation set true, " +
                            "but the online connection to Crossref fails.");
                } finally {
                    urlConn.disconnect();
                }
            }
        }
        return result;
    }

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web service based on
     * the following core information: journal title, volume and first page.
     * We use also the first author if it is there, it can help...
     *
     * @param biblio     the biblio item to be consolidated
     * @param biblioList the list of biblio items found as consolidations
     * @return Returns a boolean indicating if at least one bibliographical object
     * has been retrieve.
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
                StringUtils.isNotBlank(journal) && StringUtils.isNotBlank(volume)
                ) {
            String subpath = null;
            if (StringUtils.isNotBlank(aut))
                subpath = String.format(JOURNAL_AUTHOR_BASE_QUERY,
                        GrobidProperties.getInstance().getCrossrefId(),
                        GrobidProperties.getInstance().getCrossrefPw(),
                        URLEncoder.encode(journal, "UTF-8"),
                        URLEncoder.encode(aut, "UTF-8"),
                        URLEncoder.encode(volume, "UTF-8"),
                        firstPage);
            else
                subpath = String.format(JOURNAL_BASE_QUERY,
                        GrobidProperties.getInstance().getCrossrefId(),
                        GrobidProperties.getInstance().getCrossrefPw(),
                        URLEncoder.encode(journal, "UTF-8"),
                        URLEncoder.encode(volume, "UTF-8"),
                        firstPage);
            URL url = new URL("http://" + GrobidProperties.getInstance().getCrossrefHost() + "/" + subpath);
            String urlmsg = url.toString();
            LOGGER.info(urlmsg);
           
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

                urlConn.setDoOutput(true);
                urlConn.setDoInput(true);
                urlConn.setRequestMethod("GET");

                urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                InputStream in = null;
                try {
                    in = urlConn.getInputStream();

                    String xml = TextUtilities.convertStreamToString(in);

                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(xml));

                    DefaultHandler crossref = new CrossrefUnixrefSaxParser(bib2);

                    // get a factory
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    //get a new instance of parser
                    SAXParser p = spf.newSAXParser();
                    p.parse(is, crossref);
                    if (bib2.size() > 0 && !bib2.get(0).getError()) {
                        result = true;
                    }
                } catch (Exception e) {
                    LOGGER.error("Warning: Consolidation set true, " +
                            "but the online connection to Crossref fails.");
                } finally {
                    IOUtils.closeQuietly(in);
                    urlConn.disconnect();
                }
            }
        }
        return result;
    }

    /**
     * This is a DOI cleaning specifically adapted to CrossRef call
     */
    private static String cleanDoi(String doi) {
        doi = doi.replace("\"", "");
        doi = doi.replace("\n", "");
        if (doi.startsWith("doi:") || doi.startsWith("DOI:") || 
            doi.startsWith("doi/") || doi.startsWith("DOI/") ) {
            doi.substring(4, doi.length());
            doi = doi.trim();
        }

        doi = doi.replace(" ", "");
        return doi;
    }


    /**
     * The new public CrossRef API is a search API, and thus returns 
     * many false positives. It is necessary to validate return results 
     * against the (incomplete) source bibliographic item to block 
     * inconsistent results.  
     */
    private boolean postValidation(BiblioItem source, BiblioItem result) {
        boolean valid = true;

        // check main metadata available in source with fuzzy matching
        if (!StringUtils.isBlank(source.getTitle()) && !StringUtils.isBlank(source.getTitle())) {
//System.out.println(source.getTitle() + " / " + result.getTitle() + " = " + ratcliffObershelpDistance(source.getTitle(), result.getTitle(), false));      
            if (ratcliffObershelpDistance(source.getTitle(), result.getTitle(), false) < 0.8)
                return false;
        }

        if (!StringUtils.isBlank(source.getFirstAuthorSurname()) && 
            !StringUtils.isBlank(result.getFirstAuthorSurname())) {
//System.out.println(source.getFirstAuthorSurname() + " / " + result.getFirstAuthorSurname() + " = " + 
//    ratcliffObershelpDistance(source.getFirstAuthorSurname(), result.getFirstAuthorSurname(), false)); 
            if (ratcliffObershelpDistance(source.getFirstAuthorSurname(),result.getFirstAuthorSurname(), false) < 0.8)
                return false;
        }

        /*if (!StringUtils.isBlank(source.getPublicationDate()) && 
            !StringUtils.isBlank(result.getPublicationDate())) {
            if (!source.getPublicationDate().equals(result.getPublicationDate()))
                valid = false;
        }*/

        return valid;
    }

    private double ratcliffObershelpDistance(String string1, String string2, boolean caseDependent) {
        if ( StringUtils.isBlank(string1) || StringUtils.isBlank(string2) )
            return 0.0;
        Double similarity = 0.0;
        if (!caseDependent) {
            string1 = string1.toLowerCase();
            string2 = string2.toLowerCase();
        }
        if (string1.equals(string2))
            similarity = 1.0;
        if ( (string1.length() > 0) && (string2.length() > 0) ) {
            Option<Object> similarityObject = 
                RatcliffObershelpMetric.compare(string1, string2);
            if ( (similarityObject != null) && (similarityObject.get() != null) )
                 similarity = (Double)similarityObject.get();
        }
    
        return similarity.doubleValue();
    }

}
