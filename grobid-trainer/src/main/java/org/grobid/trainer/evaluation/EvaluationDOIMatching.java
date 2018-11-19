package org.grobid.trainer.evaluation;

import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.exceptions.*;
import org.grobid.core.engines.Engine;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.trainer.Stats;
import org.grobid.trainer.sax.NLMHeaderSaxHandler;
import org.grobid.trainer.sax.FieldExtractSaxHandler;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.trainer.evaluation.utilities.NamespaceContextMap;
import org.grobid.trainer.evaluation.utilities.FieldSpecification;
    
import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;

import org.w3c.dom.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.xpath.XPathConstants;

import com.rockymadden.stringmetric.similarity.RatcliffObershelpMetric;
import scala.Option;

/**
 * Evaluation of the DOI matching for the extracted bibliographical references,
 * using PDF+native publisher XML where the DOI or PMID are provided. A typical 
 * example of the evaluation data is PubMed Central fulltext resources. A second 
 * type of evaluation is PDF+TEI files produced via Pub2TEI fir mainstream 
 * publishers.   
 *
 * @author Patrice Lopez
 */
public class EvaluationDOIMatching {

    private static String evaluationFilePath = null;
    private Engine engine = null;
    
    public static final int BIBLIO_GLUTTON = 0;
    public static final int CROSSREF_API = 1;

    public EvaluationDOIMatching(String path) {
        this.evaluationFilePath = path;   
    
        File evaluationFile = new File(path);
        if (!evaluationFile.exists()) {
            System.out.println("Path to evaluation (gold) XML data is not valid !");
            this.evaluationFilePath = null;
        }

        try {
            GrobidProperties.getInstance();
            System.out.println(">>>>>>>> GROBID_HOME="+GrobidProperties.get_GROBID_HOME_PATH());

            engine = GrobidFactory.getInstance().createEngine();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String evaluation() throws Exception {
        StringBuilder report = new StringBuilder();
        
        // we run Grobid reference extraction on the PubMedCentral data
        File input = new File(this.evaluationFilePath);
        // we process all tsv files in the input evaluation directory
        File[] refFiles = input.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".tsv");
            }
        });

        if (refFiles == null) {
            report.append("No file in dataset");
            return report.toString();
        }
        
        List<List<String>> predictedCrossrefDOI = new ArrayList<List<String>>();
        List<List<String>> goldReferenceDOI = new ArrayList<List<String>>();

        int n = 0;
        long start = System.currentTimeMillis();
        int fails = 0;
        for (File dir : refFiles) {
            // get the PDF file in the directory

            final File tsvFile = refFiles[0];

            List<String> referenceDOI = new ArrayList<String>();
            // get the (gold) reference file corresponding to this pdf 

            String doi = null;
            String rawRef = null;
            try {
                doi = null;
                rawRef = null;
                referenceDOI.add(doi);
                n++;
            } catch(Exception e) {
                e.printStackTrace();
            }

            // run Grobid reference extraction
            try {
                System.out.println(n + " - " + tsvFile.getPath());
                /*GrobidAnalysisConfig config =
                    GrobidAnalysisConfig.builder()
                            .consolidateHeader(1)
                            .consolidateCitations(0)
                            .withPreprocessImages(true)
                            .build();*/
                BiblioItem biblio = engine.processRawReference(rawRef, 2);

                if (biblio.getDOI() != null)
                    n++;
                //predictedCrossrefDOI.add(biblio.getDOI());

                // we get from this the reference strings and matched DOI
                System.out.println("total of " + goldReferenceDOI.size() + " ref. bib.");
                System.out.println("with " + n + " DOI matched by GROBID");
            } 
            catch (Exception e) {
                System.out.println("Error when processing: " + tsvFile.getPath());
                e.printStackTrace();
            }
        }

        double processTime = ((double)System.currentTimeMillis() - start) / 1000.0;

        System.out.println(goldReferenceDOI.size() + " bibliographical references processed in " + 
             processTime + " seconds, " + ((double)processTime)/goldReferenceDOI.size() + " seconds per bibliographical reference.");
        
        // evaluation of the run
        start = System.currentTimeMillis();

        report.append("\n======= CROSSREF API ======= \n");
        report.append(evaluationRun(this.CROSSREF_API, goldReferenceDOI, predictedCrossrefDOI));

        //report.append("\n======= BIBLIO GLUTTON ======= \n");
        //report.append(evaluationRun(this.BIBLIO_GLUTTON, allReferenceDOI, predictedGluttonDOI));
        
        System.out.println("Evaluation metrics produced in " + 
                (System.currentTimeMillis() - start) / (1000.00) + " seconds");

        return report.toString();
    }

    private String evaluationRun(int runType, List<List<String>> referenceDOI, List<List<String>> predictedDOI) {
        if ( (runType != this.CROSSREF_API) && (runType != this.BIBLIO_GLUTTON) ) {
            throw new GrobidException("The run type is not valid for evaluation: " + runType);
        }

        StringBuffer buffer = new StringBuffer();



        return buffer.toString();
    }


    /**
     * From PDF and publisher XML (nlm or TEI), we create a dataset of raw bibliographical 
     * references extracted from the PDF by GROBID associated with their DOI obtained from 
     * the XML. This set will be used for evaluating the DOI matching. 
     */
    public void buildEvaluationDataset() throws Exception {
        if (this.evaluationFilePath == null) {
            throw new GrobidResourceException("Path to evaluation (gold) XML data is not correctly set");
        }
        StringBuffer report = new StringBuffer();

        // get a factory for SAX parsers
        SAXParserFactory spf = SAXParserFactory.newInstance();
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        HashMap map = new HashMap();
        // explicit indication of the default namespace
        map.put("tei", "http://www.tei-c.org/ns/1.0");

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("tei", "http://www.tei-c.org/ns/1.0");
        xp.setNamespaceContext(new NamespaceContextMap(mappings));
        
        File input = new File(this.evaluationFilePath);
        // we process all tei files in the output directory
        File[] refFiles = input.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (dir.isDirectory())
                    return true;
                else
                    return false;
            }
        });

        if (refFiles == null) {
            report.append("No file in dataset");
            return;
        }
        
        List<BibRefAggregated> allGoldReferences = new ArrayList<BibRefAggregated>();

        int n = 0;
        long start = System.currentTimeMillis();
        int fails = 0;
        for (File dir : refFiles) {
            // get the PDF file in the directory
            File[] refFiles2 = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf") || name.endsWith(".PDF");
                }
            });

            if (refFiles2 == null || refFiles2.length == 0) {
                System.out.println("warning: no PDF found under " + dir.getPath());
                continue;
            }
            if (refFiles2.length != 1) {
                System.out.println("warning: more than one PDF found under " + dir.getPath());
                System.out.println("processing only the first one...");
            }

            final File pdfFile = refFiles2[0];
            File nlmFile = null;
            File teiFile = null;

            List<BibRefAggregated> goldReference = new ArrayList<BibRefAggregated>();
            // get the (gold) reference file corresponding to this pdf 
            File[] refFiles3 = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".nxml");
                }
            });
            if ( (refFiles3 != null) && (refFiles3.length != 0) )
                nlmFile = refFiles3[0];
            refFiles3 = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tei");
                }
            });
            if ( (refFiles3 != null) && (refFiles3.length != 0) )
                teiFile = refFiles3[0];

            if ( (nlmFile == null) && (teiFile == null) ) {
                System.out.println("warning: no reference NLM or TEI file found under " + dir.getPath());
                continue;
            }

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setValidating(false);

            System.out.println("\n\nFile: " + pdfFile.getPath());

            try {
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                docBuilder.setEntityResolver(new EntityResolver() {
                    public InputSource resolveEntity(String publicId, String systemId) {
                        return new InputSource(
                            new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes()));
                    }
                }); // swap in a dummy resolver to neutralise the online DTD
                File goldFile = null;
                if (teiFile != null)
                    goldFile = teiFile;
                else 
                    goldFile = nlmFile;
                Document gold = docBuilder.parse(goldFile);

                System.out.println("Reference DOIs in : " + goldFile.getPath());

                // get the DOI or PMID of the bibliographical references ia xpath
                String path_doi = null;
                String path_pmid = null;
                String path_ref = null;
                if (teiFile == null) {
                    // gold DOI are in the nlm file 
                    path_ref = "/article/back/ref-list/ref/mixed-citation";
                    path_doi = "pub-id[@pub-id-type=\"doi\"]/text()"; 
                    path_pmid = "pub-id[@pub-id-type=\"pmid\"]/text()"; 
                    
                } else {
                    path_ref = "//back/div/listBibl/biblStruct";
                    path_doi = "idno[@type=\"doi\"]/text()";
                }
                NodeList nodeList = (NodeList) xp.compile(path_ref).
                            evaluate(gold.getDocumentElement(), XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    // for each node we have a ref bib
                    BibRefAggregated refBib = new BibRefAggregated();
                    Node ref = nodeList.item(i);
                    refBib.setXML(ref);

                    // get DOI and PMID - if any
                    NodeList nodeListDOI = (NodeList) xp.compile(path_doi).
                            evaluate(ref, XPathConstants.NODESET);
                    if (nodeListDOI.getLength() > 0) {
                        Node nodeDOI = nodeListDOI.item(0);
                        String doi = nodeDOI.getNodeValue();
                        refBib.setDOI(doi);
                    }
                    NodeList nodeListPMID = (NodeList) xp.compile(path_pmid).
                            evaluate(ref, XPathConstants.NODESET);
                    if (nodeListPMID.getLength() > 0) {
                        Node nodePMID = nodeListPMID.item(0);
                        String pmid = nodePMID.getNodeValue();
                        refBib.setPMID(pmid);
                    }
                    allGoldReferences.add(refBib);
                }

            } catch(Exception e) {
                e.printStackTrace();
            }

            // run Grobid reference extraction
            try {
                System.out.println(n + " - " + pdfFile.getPath());
                /*GrobidAnalysisConfig config =
                    GrobidAnalysisConfig.builder()
                            .consolidateHeader(1)
                            .consolidateCitations(0)
                            .withPreprocessImages(true)
                            .build();*/
                List<BibDataSet> bibrefs = engine.processReferences(pdfFile, 2);
                List<String> predictedDOI = new ArrayList<String>();

                for(BibDataSet bib : bibrefs) {
                    String rawRef = bib.getRawBib();
                    if (rawRef != null) {
                        // we need to align this raw ref bib string with a gold ref bib


                    }
                } 

                // we get from this the reference strings and matched DOI
                System.out.println("total of " + bibrefs.size() + " ref. bib. found by GROBID");
                System.out.println("with " + predictedDOI.size() + " DOI matched");
                System.out.println(allGoldReferences.size() + " DOI identified in gold");

                //predictedCrossrefDOI.add(predictedDOI);
            } 
            catch (Exception e) {
                System.out.println("Error when processing: " + pdfFile.getPath());
                e.printStackTrace();
                fails++;
            }
            n++;
        }

        System.out.println("GROBID failed on " + fails + " PDF");
        double processTime = ((double)System.currentTimeMillis() - start) / 1000.0;

        System.out.println(n + " PDF files processed in " + 
             processTime + " seconds, " + ((double)processTime)/n + " seconds per PDF file.");
    }


    /**
     * This class represents a bibliographical reference by aggregating information
     * from the publisher XML and the PDF extracted information from GROBID.
     */
    public class BibRefAggregated {
        // raw string of the bib ref
        private String rawRef = null;

        // doi if any
        private String doi = null;

        // pmid if present
        private String pmid = null;

        // xml segment corresponding to the bibliographical reference
        private Node xml = null;

        public String getRawRef() {
            return this.rawRef;
        }

        public void setRawRef(String raw) {
            this.rawRef = raw;
        }

        public String getDOI() {
            return this.doi;
        }

        public void setDOI(String doi) {
            this.doi = doi;
        }
        
        public String getPMID() {
            return this.pmid;
        }

        public void setPMID(String pmid) {
            this.pmid = pmid;
        }

        public Node getXML() {
            return this.xml;
        }

        public void setXML(Node xml) {
            this.xml = xml;
        }
    }


    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        if ( (args.length > 2) || (args.length == 0) ) {
            System.err.println("command parameters: action[build|eval] [path to the (gold) evaluation dataset]");
            return;
        }

        String action = args[0];
        if ( (action == null) || (action.length() == 0) || (!action.equals("build")) || (!action.equals("eval")) ) {
            System.err.println("Action to be performed not correctly set, should be [build|eval]");
            return;
        }

        String inputPath = args[1];
        if ( (inputPath == null) || (inputPath.length() == 0) ) {
            System.err.println("Path to evaluation (gold) XML data is not correctly set");
            return;
        }
        
        try {
            File thePath = new File(inputPath);
            if (!thePath.exists()) {
                System.err.println("Path to evaluation (gold) XML data does not exist");
                return;
            }
            if (!thePath.isDirectory()) {
                System.err.println("Path to evaluation (gold) XML data is not a directory");
                return;
            }  
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (action.equals("build")) {
                EvaluationDOIMatching build = new EvaluationDOIMatching(inputPath);
                build.buildEvaluationDataset();
            } else if (action.equals("eval")) {
                EvaluationDOIMatching eval = new EvaluationDOIMatching(inputPath);
                String report = eval.evaluation();
                System.out.println(report);
            }

            System.out.println(Engine.getCntManager());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // to be sure jvm stops
        System.exit(0);
    }
}