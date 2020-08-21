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
import org.grobid.core.utilities.glutton.*;
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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

import com.rockymadden.stringmetric.similarity.RatcliffObershelpMetric;
import scala.Option;

/**
 * Singleton class for managing the extraction of bibliographical information from pdf documents.
 * When consolidation operations are realized, be sure to call the close() method
 * to ensure that all Executors are terminated.
 *
 * @author Patrice Lopez
 */
public class Consolidation {
    private static final Logger LOGGER = LoggerFactory.getLogger(Consolidation.class);

    private static volatile Consolidation instance;

    private CrossrefClient client = null;
    private WorkDeserializer workDeserializer = null;
    private CntManager cntManager = null;

    public enum GrobidConsolidationService {
        CROSSREF("crossref"),
        GLUTTON("glutton");

        private final String ext;

        GrobidConsolidationService(String ext) {
            this.ext = ext;
        }

        public String getExt() {
            return ext;
        }

        public static GrobidConsolidationService get(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of consolidation service must not be null");
            }

            String n = name.toLowerCase();
            for (GrobidConsolidationService e : values()) {
                if (e.name().toLowerCase().equals(n)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("No consolidation service with name '" + name +
                    "', possible values are: " + Arrays.toString(values()));
        }
    }

    public static Consolidation getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
    private static synchronized void getNewInstance() {
        LOGGER.debug("Get new instance of Consolidation");
        instance = new Consolidation();
    }

    /**
     * Hidden constructor
     */
    private Consolidation() {
        if (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.GLUTTON)
            client = GluttonClient.getInstance();
        else 
            client = CrossrefClient.getInstance();
        workDeserializer = new WorkDeserializer();   
    }

    public void setCntManager(CntManager cntManager) {
        this.cntManager = cntManager;
    }

    public CntManager getCntManager() {
        return this.cntManager;
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
     * Try to consolidate one bibliographical object with crossref metadata lookup web services based on
     * core metadata
     */
    public BiblioItem consolidate(BiblioItem bib, String rawCitation) throws Exception {
        final List<BiblioItem> results = new ArrayList<BiblioItem>();

        String theDOI = bib.getDOI();
        if (StringUtils.isNotBlank(theDOI)) {
            theDOI = cleanDoi(theDOI);
        }
        final String doi = theDOI;
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

        /*if (aut != null) {
            aut = TextUtilities.removeAccents(aut);
        }
        if (title != null) {
            title = TextUtilities.removeAccents(title);
        }
        if (journalTitle != null) {
            journalTitle = TextUtilities.removeAccents(journalTitle);
        }*/
        if (cntManager != null) 
            cntManager.i(ConsolidationCounters.CONSOLIDATION);

        long threadId = Thread.currentThread().getId();
        Map<String, String> arguments = null;

        if (StringUtils.isNotBlank(doi)) {
            // call based on the identified DOI
            arguments = new HashMap<String,String>();
            arguments.put("doi", doi);
        } 
        if (StringUtils.isNotBlank(rawCitation)) {
            // call with full raw string
            if (arguments == null)
                arguments = new HashMap<String,String>();
            if ( (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) || 
                     StringUtils.isBlank(doi) )
                arguments.put("query.bibliographic", rawCitation);
            //arguments.put("query", rawCitation);
        }
        if (StringUtils.isNotBlank(aut)) {
            // call based on partial metadata
            if (arguments == null)
                arguments = new HashMap<String,String>();
            if ( (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) || 
                 (StringUtils.isBlank(rawCitation) && StringUtils.isBlank(doi)) )
                arguments.put("query.author", aut);
        }
        if (StringUtils.isNotBlank(title)) {
            // call based on partial metadata
            if (arguments == null)
                arguments = new HashMap<String,String>();
            if ( (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) || 
                (StringUtils.isBlank(rawCitation) && StringUtils.isBlank(doi)) )
                arguments.put("query.title", title);
        }
        if (StringUtils.isNotBlank(journalTitle)) {
            // call based on partial metadata
            if (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) {
                if (arguments == null)
                    arguments = new HashMap<String,String>();
                arguments.put("query.container-title", journalTitle);
            }
        }
        if (StringUtils.isNotBlank(volume)) {
            // call based on partial metadata
            if (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) {
                if (arguments == null)
                    arguments = new HashMap<String,String>();
                arguments.put("volume", volume);
            }
        }
        if (StringUtils.isNotBlank(firstPage)) {
            // call based on partial metadata
            if (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) {
                if (arguments == null)
                    arguments = new HashMap<String,String>();
                arguments.put("firstPage", firstPage);
            }
        }

        if (arguments == null || arguments.size() == 0) {
            return null;
        }

        if (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.CROSSREF) {
            if (StringUtils.isBlank(doi) && StringUtils.isBlank(rawCitation) && 
                 (StringUtils.isBlank(aut) || StringUtils.isBlank(title)) ) {
                // there's not enough information for a crossref request, which might always return a result
                return null;
            }
        }

        if (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.CROSSREF) {
            arguments.put("rows", "1"); // we just request the top-one result
        } else if (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.GLUTTON) {
            if (StringUtils.isNotBlank(doi))
                arguments.put("postValidate", "false");
            // GROBID has already parsed the reference, so no need to redo this in glutton
            arguments.put("parseReference", "false");
        }

        final boolean doiQuery;
        try {
            //CrossrefRequestListener<BiblioItem> requestListener = new CrossrefRequestListener<BiblioItem>();
            if (cntManager != null) {
                cntManager.i(ConsolidationCounters.CONSOLIDATION);
            }

            if ( StringUtils.isNotBlank(doi) && (cntManager != null) ) {
                cntManager.i(ConsolidationCounters.CONSOLIDATION_PER_DOI);
                doiQuery = true;
            } else {
                doiQuery = false;
            }

            client.<BiblioItem>pushRequest("works", arguments, workDeserializer, threadId, new CrossrefRequestListener<BiblioItem>(0) {
                
                @Override
                public void onSuccess(List<BiblioItem> res) {
                    if ((res != null) && (res.size() > 0) ) {
                        // we need here to post-check that the found item corresponds
                        // correctly to the one requested in order to avoid false positive
                        for(BiblioItem oneRes : res) {
                            /* 
                              Glutton integrates its own post-validation, so we can skip post-validation in GROBID when it is used as 
                              consolidation service - except in specific case where the DOI is failing and the consolidation is based on 
                              extracted title and author.  

                              In case of crossref REST API, for single bib. ref. consolidation (this case comes only for header extraction), 
                              having an extracted DOI matching is considered safe enough, and we don't require further post-validation.

                              For all the other case of matching with CrossRef, we require a post-validation. 
                            */
                            if ( 
                                ( (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.GLUTTON) && 
                                    !doiQuery
                                )
                                ||
                                ( (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.GLUTTON) && 
                                    StringUtils.isNotBlank(oneRes.getDOI()) &&
                                    doi.equals(oneRes.getDOI())
                                )
                                ||
                                ( (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.CROSSREF) && 
                                  (doiQuery) ) 
                                ||
                                postValidation(bib, oneRes)) {
                                results.add(oneRes);
                                if (cntManager != null) {
                                    cntManager.i(ConsolidationCounters.CONSOLIDATION_SUCCESS);
                                    if (doiQuery)
                                        cntManager.i(ConsolidationCounters.CONSOLIDATION_PER_DOI_SUCCESS);
                                }
                                break;
                            }
                        }
                    } 
                }

                @Override
                public void onError(int status, String message, Exception exception) {
                    LOGGER.info("Consolidation service returns error ("+status+") : "+message);
                }
            });
        } catch(Exception e) {
            LOGGER.info("Consolidation error - " + ExceptionUtils.getStackTrace(e));
        } 

        client.finish(threadId);
        if (results.size() == 0)
            return null;
        else
            return results.get(0);
    }


    /**
     * Try tp consolidate a list of bibliographical objects in one operation with consolidation services
     */
    public Map<Integer,BiblioItem> consolidate(List<BibDataSet> biblios) {   
        if (CollectionUtils.isEmpty(biblios))
            return null;
        final Map<Integer,BiblioItem> results = new HashMap<Integer,BiblioItem>();
        // init the results
        int n = 0;
        for(n=0; n<biblios.size(); n++) {
            results.put(Integer.valueOf(n), null);
        }
        n = 0;
        long threadId = Thread.currentThread().getId();
        for(BibDataSet bibDataSet : biblios) {
            final BiblioItem theBiblio = bibDataSet.getResBib();

            if (cntManager != null) 
                cntManager.i(ConsolidationCounters.TOTAL_BIB_REF);

            // first we get the exploitable metadata
            String doi = theBiblio.getDOI();
            if (StringUtils.isNotBlank(doi)) {
                doi = cleanDoi(doi);
            }
            String aut = theBiblio.getFirstAuthorSurname();
            String title = theBiblio.getTitle();
            String journalTitle = theBiblio.getJournal();
           
            // and the row string
            String rawCitation = bibDataSet.getRawBib();

            Map<String, String> arguments = null;

            String volume = theBiblio.getVolume();
            if (StringUtils.isBlank(volume))
                volume = theBiblio.getVolumeBlock();

            String firstPage = null;
            String pageRange = theBiblio.getPageRange();
            int beginPage = theBiblio.getBeginPage();
            if (beginPage != -1) {
                firstPage = "" + beginPage;
            } else if (pageRange != null) {
                StringTokenizer st = new StringTokenizer(pageRange, "--");
                if (st.countTokens() == 2) {
                    firstPage = st.nextToken();
                } else if (st.countTokens() == 1)
                    firstPage = pageRange;
            }

            /*if (aut != null) {
                aut = TextUtilities.removeAccents(aut);
            }
            if (title != null) {
                title = TextUtilities.removeAccents(title);
            }
            if (journalTitle != null) {
                journalTitle = TextUtilities.removeAccents(journalTitle);
            }*/

            if (StringUtils.isNotBlank(doi)) {
                // call based on the identified DOI
                arguments = new HashMap<String,String>();
                arguments.put("doi", doi);
            } 
            if (StringUtils.isNotBlank(rawCitation)) {
                // call with full raw string
                if (arguments == null)
                    arguments = new HashMap<String,String>();
                if ( (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) || 
                     StringUtils.isBlank(doi) )
                    arguments.put("query.bibliographic", rawCitation);
            }
            if (StringUtils.isNotBlank(title)) {
                // call based on partial metadata
                if (arguments == null)
                    arguments = new HashMap<String,String>();
                if ( (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) || 
                     (StringUtils.isBlank(rawCitation) && StringUtils.isBlank(doi)) )
                    arguments.put("query.title", title);
            }
            if (StringUtils.isNotBlank(aut)) {
                // call based on partial metadata
                if (arguments == null)
                    arguments = new HashMap<String,String>();
                if ( (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) || 
                     (StringUtils.isBlank(rawCitation) && StringUtils.isBlank(doi)) )
                    arguments.put("query.author", aut);
            }
            if (StringUtils.isNotBlank(journalTitle)) {
                // call based on partial metadata
                if (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) {
                    if (arguments == null)
                        arguments = new HashMap<String,String>();
                    arguments.put("query.container-title", journalTitle);
                }
            }
            if (StringUtils.isNotBlank(volume)) {
                // call based on partial metadata
                if (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) {
                    if (arguments == null)
                        arguments = new HashMap<String,String>();
                    arguments.put("volume", volume);
                }
            }
            if (StringUtils.isNotBlank(firstPage)) {
                // call based on partial metadata
                if (GrobidProperties.getInstance().getConsolidationService() != GrobidConsolidationService.CROSSREF) {
                    if (arguments == null)
                        arguments = new HashMap<String,String>();
                    arguments.put("firstPage", firstPage);
                }
            }
            
            if (arguments == null || arguments.size() == 0) {
                n++;
                continue;
            }

            if (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.CROSSREF) {
                if (StringUtils.isBlank(doi) && StringUtils.isBlank(rawCitation) && 
                     (StringUtils.isBlank(aut) || StringUtils.isBlank(title)) ) {
                    // there's not enough information for a crossref request, which might always return a result
                    n++;
                    continue;
                }
            }

            if (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.CROSSREF)
                arguments.put("rows", "1"); // we just request the top-one result
            else if (GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.GLUTTON) {
                // grobid is doing its own post-validation right now
                //arguments.put("postValidate", "false");
                // GROBID has already parsed the reference, so no need to redo this in glutton
                arguments.put("parseReference", "false");
            }

            final boolean doiQuery;
            try {
                //CrossrefRequestListener<BiblioItem> requestListener = new CrossrefRequestListener<BiblioItem>();
                if (cntManager != null) {
                    cntManager.i(ConsolidationCounters.CONSOLIDATION);
                }

                if ( StringUtils.isNotBlank(doi) && (cntManager != null) ) {
                    cntManager.i(ConsolidationCounters.CONSOLIDATION_PER_DOI);
                    doiQuery = true;
                } else {
                    doiQuery = false;
                }

                client.<BiblioItem>pushRequest("works", arguments, workDeserializer, threadId, new CrossrefRequestListener<BiblioItem>(n) {
                    
                    @Override
                    public void onSuccess(List<BiblioItem> res) {
                        if ((res != null) && (res.size() > 0) ) {
                            // we need here to post-check that the found item corresponds
                            // correctly to the one requested in order to avoid false positive
                            for(BiblioItem oneRes : res) {
                                if ((GrobidProperties.getInstance().getConsolidationService() == GrobidConsolidationService.GLUTTON) ||
                                    postValidation(theBiblio, oneRes)) {
                                    results.put(Integer.valueOf(getRank()), oneRes);
                                    if (cntManager != null) {
                                        cntManager.i(ConsolidationCounters.CONSOLIDATION_SUCCESS);
                                        if (doiQuery)
                                            cntManager.i(ConsolidationCounters.CONSOLIDATION_PER_DOI_SUCCESS);
                                    }
                                    break;
                                }
                            }
                        } 
                    }

                    @Override
                    public void onError(int status, String message, Exception exception) {
                        LOGGER.info("Consolidation service returns error ("+status+") : "+message);
                    }
                });
            } catch(Exception e) {
                LOGGER.info("Consolidation error - " + ExceptionUtils.getStackTrace(e));
            } 
            n++;
        }
        client.finish(threadId);

        return results;
    }

    /**
     * Try to consolidate some uncertain bibliographical data with crossref REST API service based on
     * the DOI if it is around
     *
     * @param biblio the Biblio item to be consolidated
     * @param bib2   the list of biblio items found as consolidations
     * @return Returns a boolean indicating if at least one bibliographical object
     * has been retrieved.
     */
    /*public boolean consolidateCrossrefGetByDOI(BiblioItem biblio, List<BiblioItem> bib2) throws Exception {
        boolean result = false;
        String doi = biblio.getDOI();

        if (bib2 == null)
            return false;

        if (StringUtils.isNotBlank(doi)) {
            // some cleaning of the doi
            doi = cleanDoi(doi);
            Map<String, String> arguments = new HashMap<String,String>();
            arguments.put("doi", doi);

            long threadId = Thread.currentThread().getId();
            CrossrefRequestListener<BiblioItem> requestListener = new CrossrefRequestListener<BiblioItem>();
            client.<BiblioItem>pushRequest("works", arguments, workDeserializer, threadId, requestListener);
            if (cntManager != null)
                cntManager.i(ConsolidationCounters.CONSOLIDATION_PER_DOI);

            synchronized (requestListener) {
                try {
                    requestListener.wait(5000); // timeout after 5 seconds
                } catch (InterruptedException e) {
                    LOGGER.warn("Timeout error - " + ExceptionUtils.getStackTrace(e));
                }
            }
            
            CrossrefRequestListener.Response<BiblioItem> response = requestListener.getResponse();
            
            if (response == null)
                LOGGER.warn("No response ! Maybe timeout.");
            
            else if (response.hasError() || !response.hasResults())
                LOGGER.warn("Consolidation service returns error ("+response.status+") : "+response.errorMessage);
            
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
    }*/

    /**
     * Try to consolidate some uncertain bibliographical data with crossref web service based on
     * title and first author.
     *
     * @param biblio     the biblio item to be consolidated
     * @param biblioList the list of biblio items found as consolidations
     * @return Returns a boolean indicating whether at least one bibliographical object has been retrieved.
     */
    /*public boolean consolidateCrossrefGetByAuthorTitle(String aut, String title,
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
            arguments.put("rows", "1"); // we just request the top-one result
            
            long threadId = Thread.currentThread().getId();
            CrossrefRequestListener<BiblioItem> requestListener = new CrossrefRequestListener<BiblioItem>();
            if (cntManager != null) 
                cntManager.i(ConsolidationCounters.CONSOLIDATION);
            client.<BiblioItem>pushRequest("works", arguments, workDeserializer, threadId, new CrossrefRequestListener<BiblioItem>() {
                
                @Override
                public void onSuccess(List<BiblioItem> res) {
                    //System.out.println("size of results: " + res.size());
                    if ((res != null) && (res.size() > 0) ) {
                        // we need here to post-check that the found item corresponds
                        // correctly to the one requested in order to avoid false positive
                        for(BiblioItem oneRes : res) {
                            if (postValidation(biblio, oneRes)) {
                                if (cntManager != null) 
                                    cntManager.i(ConsolidationCounters.CONSOLIDATION_SUCCESS);
                                bib2.add(oneRes);
                            }
                        }
                    }
                }

                @Override
                public void onError(int status, String message, Exception exception) {
                    LOGGER.warn("Consolidation service returns error ("+status+") : "+message);
                }
            });

        }
        if (bib2.size() > originalSize)
            return true;
        else
            return false;
    }*/

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
        /*if (!StringUtils.isBlank(source.getTitle()) && !StringUtils.isBlank(source.getTitle())) {
//System.out.println(source.getTitle() + " / " + result.getTitle() + " = " + ratcliffObershelpDistance(source.getTitle(), result.getTitle(), false));      
            if (ratcliffObershelpDistance(source.getTitle(), result.getTitle(), false) < 0.8)
                return false;
        }*/

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
