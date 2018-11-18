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

    private static String xmlInputPath = null;
    private Engine engine = null;
    
    public static final int BIBLIO_GLUTTON = 0;
    public static final int CROSSREF_API = 1;
    
    public double fileRatio = 1.0;
        
    // the type of evaluation XML data - NLM or TEI (obtained via Pub2TEI)
    private String inputType = null;

    public EvaluationDOIMatching(String path, String inType) {
        this.xmlInputPath = path;   
        this.inputType = inType;
    
        File xmlInputFile = new File(path);
        if (!xmlInputFile.exists()) {
            System.out.println("Path to evaluation (gold) XML data is not valid !");
            xmlInputPath = null;
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
        if (xmlInputPath == null) {
            throw new GrobidResourceException("Path to evaluation (gold) XML data is not correctly set");
        }
        StringBuilder report = new StringBuilder();

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
        
        // we run Grobid reference extraction on the PubMedCentral data
        File input = new File(xmlInputPath);
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
            return report.toString();
        }
        
        List<List<String>> predictedCrossrefDOI = new ArrayList<List<String>>();
        List<List<String>> allReferenceDOI = new ArrayList<List<String>>();

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

            List<String> referenceDOI = new ArrayList<String>();
            // get the (gold) reference file corresponding to this pdf 
            if (this.inputType.equals("nlm")) {
                File[] refFiles3 = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".nxml");
                    }
                });
                nlmFile = refFiles3[0];
            } else if (this.inputType.equals("tei")) {
                File[] refFiles3 = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".tei");
                    }
                });
                teiFile = refFiles3[0];
            } else {
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

                // get the DOI of the bibliographical references ia xpath
                String path = null;
                if (teiFile == null) {
                    // gold DOI are in the nlm file 
                    path = "/article/back/ref-list/ref/mixed-citation/pub-id[@pub-id-type=\"doi\"]/text()"; 
                } else {
                    path = "//back/div/listBibl/biblStruct/idno[@type=\"DOI\"]/text()";
                }
                NodeList doiNodeList = (NodeList) xp.compile(path).
                            evaluate(gold.getDocumentElement(), XPathConstants.NODESET);
                for (int i = 0; i < doiNodeList.getLength(); i++) {
                    Node node = doiNodeList.item(i);
                    String doi = node.getNodeValue();
                    referenceDOI.add(doi);
                }

                allReferenceDOI.add(referenceDOI);
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
                    BiblioItem biblio = bib.getResBib();
                    if (biblio.getDOI() != null)
                        predictedDOI.add(biblio.getDOI());
                } 

                // we get from this the reference strings and matched DOI
                System.out.println("total of " + bibrefs.size() + " ref. bib. found by GROBID");
                System.out.println("with " + predictedDOI.size() + " DOI matched");
                System.out.println(referenceDOI.size() + " DOI identified in gold");

                predictedCrossrefDOI.add(predictedDOI);
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
        
        // evaluation of the run
        start = System.currentTimeMillis();

        report.append("\n======= CROSSREF API ======= \n");
        report.append(evaluationRun(this.CROSSREF_API, allReferenceDOI, predictedCrossrefDOI));

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
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        if ( (args.length >3) || (args.length == 0) ) {
            System.err.println("usage: command [path to the (gold) evaluation XML dataset] fileRatio[0.0-1.0]");
            return;
        }

        String inputType = args[0];
        if ( (inputType == null) || (inputType.length() == 0) || (!inputType.equals("nlm") && !inputType.equals("tei")) ) {
            System.err.println("Input type is not correctly set, should be [tei|nlm]");
            return;
        }

        boolean runGrobidVal = true;
        String xmlInputPath = args[1];
        if ( (xmlInputPath == null) || (xmlInputPath.length() == 0) ) {
            System.err.println("Path to evaluation (gold) XML data is not correctly set");
            return;
        }
        
        // optional file ratio for applying the evaluation
        double fileRatio = 1.0;
        if (args.length > 1) {
            String fileRatioString = args[2];
            if ((fileRatioString != null) && (fileRatioString.length() > 0)) {
                try {
                    fileRatio = Double.parseDouble(fileRatioString);
                }
                catch(Exception e) {
                    System.err.println("Invalid argument fileRatio, must be a double, e.g. 0.1");
                    return;
                }
            }
        }
        
        try {
            File xmlPath = new File(xmlInputPath);
            if (!xmlPath.exists()) {
                System.err.println("Path to evaluation (gold) XML data does not exist");
                return;
            }
            if (!xmlPath.isDirectory()) {
                System.err.println("Path to evaluation (gold) XML data is not a directory");
                return;
            }  
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            EvaluationDOIMatching eval = new EvaluationDOIMatching(xmlInputPath, inputType);
            eval.fileRatio = fileRatio;
            String report = eval.evaluation();
            System.out.println(report);
            System.out.println(Engine.getCntManager());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // to be sure jvm stops
        System.exit(0);
    }
}