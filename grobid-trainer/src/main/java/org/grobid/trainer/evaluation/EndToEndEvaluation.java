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
 * Evaluation against native XML documents. This is an end-to-end evaluation involving
 * complete document processing, and therefore a complete set of CRF models.
 *
 * @author Patrice Lopez
 */
public class EndToEndEvaluation {
    private static String xmlInputPath = null;
	private Engine engine = null;
	
	public static final int GROBID = 0;
	public static final int PDFX = 1;
	public static final int CERMINE = 2;

	public static final int HEADER = 0;
	public static final int CITATION = 1;
	public static final int FULLTEXT = 2;
	
	public double fileRatio = 1.0;
	
	public static final double minLevenshteinDistance = 0.8;
	public static final double minRatcliffObershelpSimilarity = 0.95;
	
	// the list of labels considered for the evaluation
	private List<String> headerLabels = null;
	private List<String> fulltextLabels = null;
	private List<String> citationsLabels = null;
	
	// the list of fields considered for the evaluation
	private List<FieldSpecification> headerFields = null;
	private List<FieldSpecification> fulltextFields = null;
	private List<FieldSpecification> citationsFields = null;
		
	// the type of evaluation XML data - NLM or TEI (obtained via Pub2TEI)
	private String inputType = null;

	public EndToEndEvaluation(String path, String inType) {
		this.xmlInputPath = path;	
		this.inputType = inType;
	
		File xmlInputFile = new File(path);
		if (!xmlInputFile.exists()) {
			System.out.println("Path to evaluation (gold) XML data is not valid !");
			xmlInputPath = null;
		}
		
		String pGrobidHome = "../grobid-home";
		String pGrobidProperties = "../grobid-home/config/grobid.properties";

		try {
			GrobidProperties.getInstance();
			System.out.println(">>>>>>>> GROBID_HOME="+GrobidProperties.get_GROBID_HOME_PATH());

			engine = GrobidFactory.getInstance().createEngine();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// initialize the field specifications and label list
		headerFields = new ArrayList<>();
		fulltextFields = new ArrayList<FieldSpecification>();	
		citationsFields = new ArrayList<FieldSpecification>();
		
		headerLabels = new ArrayList<String>();
		fulltextLabels = new ArrayList<String>();
		citationsLabels = new ArrayList<String>();
		
		FieldSpecification.setUpFields(headerFields, fulltextFields, citationsFields, 
			headerLabels, fulltextLabels, citationsLabels);
	}
	
	public String evaluationGrobid(boolean forceRun) throws Exception {
		if (xmlInputPath == null) {
			throw new GrobidResourceException("Path to evaluation (gold) XML data is not correctly set");
		}
		StringBuilder report = new StringBuilder();
		
		if (forceRun) {
			// we run Grobid full text extraction on the PubMedCentral data
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
				
				// run Grobid full text and write the TEI result in the directory
				try {
					System.out.println(n + " - " + pdfFile.getPath());
					GrobidAnalysisConfig config =
                        GrobidAnalysisConfig.builder()
                                .consolidateHeader(true)
                                .consolidateCitations(false)
                                .withPreprocessImages(true)
                                .build();
					String tei = engine.fullTextToTEI(pdfFile, config);
					// write the result in the same directory
					File resultTEI = new File(dir.getPath() + File.separator
						+ pdfFile.getName().replace(".pdf", ".fulltext.tei.xml"));
					FileUtils.writeStringToFile(resultTEI, tei, "UTF-8");
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
		
		// evaluation of the run
		long start = System.currentTimeMillis();

		report.append("\n======= Header metadata ======= \n");
		report.append(evaluationRun(this.GROBID, this.HEADER));
		
		report.append("\n======= Citation metadata ======= \n");
		report.append(evaluationRun(this.GROBID, this.CITATION));
		
		report.append("\n======= Fulltext structures ======= \n");
		report.append(evaluationRun(this.GROBID, this.FULLTEXT));
		
		System.out.println("Evaluation metrics produced in " + 
				(System.currentTimeMillis() - start) / (1000.00) + " seconds");

		return report.toString();
	}
	
	public String evaluationPDFX() throws Exception {
		if (xmlInputPath == null) {
			throw new GrobidResourceException("Path to PubMedCentral is not correctly set");
		}
		StringBuilder report = new StringBuilder();
		
		// evaluation of the run
		report.append("\n======= Header metadata ======= \n");
		report.append(evaluationRun(this.PDFX, this.HEADER));
		
		report.append("\n======= Citation metadata ======= \n");
		report.append(evaluationRun(this.PDFX, this.CITATION));
		
		report.append("\n======= Header structures ======= \n");
		report.append(evaluationRun(this.PDFX, this.FULLTEXT));
				
		return report.toString();
	}
	
	public String evaluationCermine(boolean forceRun) throws Exception {
		if (xmlInputPath == null) {
			throw new GrobidResourceException("Path to PubMedCentral is not correctly set");
		}
		StringBuilder report = new StringBuilder();
		
		if (forceRun) {
			// we run here CERMINE on the PDF files...
			// TBD
			// ...
		}
		
		// evaluation of the run
		report.append("\n======= Header metadata ======= \n");
		report.append(evaluationRun(this.CERMINE, this.HEADER));
		
		report.append("\n======= Citation metadata ======= \n");
		report.append(evaluationRun(this.CERMINE, this.CITATION));
		
		report.append("\n======= Header structures ======= \n");
		report.append(evaluationRun(this.CERMINE, this.FULLTEXT));
				
		return report.toString();
	}
	
	private String evaluationRun(int runType, int sectionType) {
		if (  (runType != this.GROBID) && (runType != this.PDFX) && (runType != this.CERMINE)) {
			throw new GrobidException("The run type is not valid for evaluation");
		}
		if (  (sectionType != this.HEADER) && (sectionType != this.CITATION) && (sectionType != this.FULLTEXT)) {
			throw new GrobidException("The section type is not valid for evaluation");
		}
		StringBuilder report = new StringBuilder();
		
		// we introduce four string-matching comparisons variants for different levels of 
		// fidelity between observed and expected strings, in line with other evaluations
		// in the litterature: 
		// - strict, i.e. exact match
		// - soft, matching ignoring punctuations, character case and extra space characters 
		// - Levenshtein distance (relative to the max length of fields)
		// - Ratcliff/Obershelp similarity
		// These variants only apply to textual fields, not numerical and dates fields 
		// (such as volume, issue, dates).

        Stats strictStats = new Stats();
        Stats softStats = new Stats();
        Stats levenshteinStats = new Stats();
        Stats ratcliffObershelpStats = new Stats();

		List<String> labels = null;
		List<FieldSpecification> fields = null;
		
		int totalExpectedInstances = 0;
		int totalObservedInstances = 0;
		int totalCorrectInstancesStrict = 0;
		int totalCorrectInstancesSoft = 0;
		int totalCorrectInstancesLevenshtein = 0;
		int totalCorrectInstancesRatcliffObershelp = 0;
		
		if (sectionType == this.HEADER) {
			fields = headerFields;
			labels = headerLabels;
		}
		else if (sectionType == this.CITATION) {
			fields = citationsFields;
			labels = citationsLabels;
		}
		else if (sectionType == this.FULLTEXT) {
			fields = fulltextFields;
			labels = fulltextLabels;
		}
		
		// statics about citation matching
		int match1 = 0;
		int match2 = 0;
		int match3 = 0;
		int match4 = 0;
		
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

        // get a factory for SAX parsers
        SAXParserFactory spf = SAXParserFactory.newInstance();
		Random rand = new Random();
		int nbFile = 0;
        for (File dir : refFiles) {
			// file ratio filtering
			double random = rand.nextDouble();
			if (random > fileRatio) {
				continue;
			}
			
			// get the gold file in the directory
            File[] refFiles2 = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".nxml") | name.endsWith(".pub2tei.tei.xml");
                }
            });

			if (refFiles2 == null || refFiles2.length == 0) {
            	System.out.println("warning: no evaluation (gold) XML data file found under " + dir.getPath());
			    continue;
			}

			if (refFiles2.length != 1) {
            	System.out.println("warning: more than one evaluation (gold) XML data files found under " + dir.getPath());
			    System.out.println("processing only the first one...");
			}
			
			File goldFile = refFiles2[0];

	        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	        docFactory.setValidating(false);

			try {
		        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				docBuilder.setEntityResolver(new EntityResolver() {
					public InputSource resolveEntity(String publicId, String systemId) {
						return new InputSource(
							new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes()));
					}
				}); // swap in a dummy resolver to neutralise the online DTD
				Document gold = docBuilder.parse(goldFile);

				// get the results of the evaluated tool for this file
				if (runType == this.GROBID) {
					// results are produced in a TEI file
		            File[] refFiles3 = dir.listFiles(new FilenameFilter() {
		                public boolean accept(File dir, String name) {
		                    return name.endsWith(".fulltext.tei.xml");
		                }
		            });

		            if ( (refFiles3 == null) || (refFiles3.length == 0) ) {
		            	System.out.println("warning: no Grobid TEI file found under " + dir.getPath());
					    continue;
					}

					if (refFiles3.length != 1) {
		            	System.out.println("warning: more than one Grobid TEI files found under " + dir.getPath());
					    System.out.println("processing only the first one...");
					}

					File teiFile = refFiles3[0];
				
					refFiles3 = dir.listFiles(new FilenameFilter() {
		                public boolean accept(File dir, String name) {
		                    return name.endsWith(".nxml") || name.endsWith(".pub2tei.tei.xml");
		                }
		            });

		            if (refFiles3 == null) {
		            	System.out.println("warning: no evaluation (gold) XML data file found under " + dir.getPath());
					    continue;
					}

					if (refFiles3.length != 1) {
		            	System.out.println("warning: more than one evaluation (gold) XML data file found under " + 
		            		dir.getPath());
					    System.out.println("processing only the first one...");
					}

			        Document tei = docBuilder.parse(teiFile);

					XPathFactory xpf = XPathFactory.newInstance();
					XPath xp = xpf.newXPath();
					HashMap map = new HashMap();
					// explicit indication of the default namespace
				  	map.put("tei", "http://www.tei-c.org/ns/1.0");

					Map<String, String> mappings = new HashMap<String, String>();
					mappings.put("tei", "http://www.tei-c.org/ns/1.0");
				  	xp.setNamespaceContext(new NamespaceContextMap(mappings));
					
					if (sectionType == this.CITATION) {
						// we start by identifying each expected citation
						// the first FieldSpecification object for the citation is the base path for
						// each citation structure in the corresponding XML
						FieldSpecification base = fields.get(0);

						String path = null;
						if (inputType.equals("nlm"))
							path = base.nlmPath.get(0);
						else 
							path = base.grobidPath.get(0);

						NodeList nodeList = (NodeList) xp.compile(path).
							evaluate(gold.getDocumentElement(), XPathConstants.NODESET);
						int nbCitationsGold = nodeList.getLength();
						totalExpectedInstances += nbCitationsGold;
//System.out.println("found " + nbCitationsGold + " citations in reference gold file.");
						List<Map<String,List<String>>> goldCitations = 
							new ArrayList<Map<String,List<String>>>();

						// "signature" of the citations for this file
						// level 1 signature: titre + date 
						List<String> goldCitationSignaturesLevel1 = new ArrayList<String>();
						
						// level 2 signature: all authors names + date
						List<String> goldCitationSignaturesLevel2 = new ArrayList<String>();
						
						// level 3 signature: journal + volume + page
						List<String> goldCitationSignaturesLevel3 = new ArrayList<String>();
						
						// level 4 signature:  "fuzzy titre" + date + at least one of auteurs or first page
						List<String> goldCitationSignaturesLevel4 = new ArrayList<String>();
						
						for (int i = 0; i < nodeList.getLength(); i++) {
							// sometimes we just have the raw citation bellow this, so we will have to further
							// test if we have something structured 							
							Map<String,List<String>> fieldsValues = new HashMap<String,List<String>>();
							Node node = nodeList.item(i);
							int p = 0;
							for(FieldSpecification field : fields) {
								String fieldName = field.fieldName;
								if (fieldName.equals("base")) {
									//p++;
									continue;
								}
								List<String> subpaths = null;
								if (inputType.equals("nlm")) {
									subpaths = field.nlmPath;
								} else if (inputType.equals("tei")) {
									subpaths = field.grobidPath;
								}

								if (subpaths == null)
									continue;

								for(String subpath : subpaths) {
									NodeList nodeList2 = (NodeList) xp.compile(subpath).
										evaluate(node, XPathConstants.NODESET);
									
									List<String> goldResults = new ArrayList<String>();
									for (int j = 0; j < nodeList2.getLength(); j++) {
										String content = nodeList2.item(j).getNodeValue();
										if ((content != null) && (content.trim().length() > 0))
											goldResults.add(content);
									}
									
									if (goldResults.size() > 0) {
										fieldsValues.put(fieldName, goldResults);
//System.out.println("gold / " + fieldName + " / " + goldResults.toString());
                                        strictStats.incrementExpected(fieldName);
                                        softStats.incrementExpected(fieldName);
                                        levenshteinStats.incrementExpected(fieldName);
                                        ratcliffObershelpStats.incrementExpected(fieldName);
									}
								}

								p++;
							}
							
							// signature for this citation
							String goldTitle = "";
							List<String> goldResults = fieldsValues.get("title");
							if (goldResults != null) {
								for(String res : goldResults) {
									goldTitle += " " + res;
								}
							}
							goldTitle = basicNormalization(goldTitle);
							String goldTitleSoft = removeFullPunct(goldTitle);
							
							// source title / inTitle information
							String goldInTitle = "";
							List<String> inTitleResults = fieldsValues.get("inTitle");
							if (inTitleResults != null) {
								for(String res : inTitleResults) {
									goldInTitle += " " + res;
								}
							}
							goldInTitle = basicNormalization(goldInTitle);
							String goldInTitleSoft = removeFullPunct(goldInTitle);
							
							// first author last name only
							List<String> authorResults = fieldsValues.get("first_author");
							String goldAuthor = "";
							if ((authorResults != null) && (authorResults.size() > 0))
								goldAuthor = authorResults.get(0);
							goldAuthor = basicNormalization(goldAuthor);
							String goldAuthorSoft = removeFullPunct(goldAuthor);
							
							// all authors last names
							String goldAuthors = "";
							List<String> authorsResults = fieldsValues.get("authors");
							if ((authorsResults != null) && (authorsResults.size() > 0)) {
								for(String aut : authorsResults)
									goldAuthors += aut;
							}
							goldAuthors = basicNormalization(goldAuthors);
							String goldAuthorsSoft = removeFullPunct(goldAuthors);
							
							// date of publication
							List<String> dateResults = fieldsValues.get("date");
							String goldDate = "";
							if ((dateResults != null) && (dateResults.size() > 0))
								goldDate = dateResults.get(0);
							goldDate = basicNormalization(goldDate);
							
							// volume
							List<String> volumeResults = fieldsValues.get("volume");
							String goldVolume = "";
							if ((volumeResults != null) && (volumeResults.size() > 0))
								goldVolume = volumeResults.get(0);
							goldVolume = basicNormalization(goldVolume);
							
							// first page
							List<String> pageResults = fieldsValues.get("page");
							String goldPage = "";
							if ((pageResults != null) && (pageResults.size() > 0))
								goldPage = pageResults.get(0);
							goldPage = basicNormalization(goldPage);
							
/*
 * We introduce 4 sequential alignment rules to match an extracted citation with an expected citation.
 * If the first rule is not working, we test the second one, and so on until the last one.
 * If all rules fail, the extracted citation is considered as false positive for its non-empty fields.
 * - first rule: matching of the "soft" title (title ignoring case, punctuation ans space mismatches) and year
 * - second rule: matching all of "soft" authors and year
 * - third rule: matching of "soft" inTitle (title of Journal or Conference), volume and first page
 * - forth rule: matching of first author last name and title, or inTitle if title is empty  
 */	
							
							String signature1 = null;								
							if ( (goldTitleSoft.length()>0) && (goldDate.length()>0) ) {
								signature1 = goldTitleSoft + goldDate;
								//signature1 = signature1.replaceAll("[^\\x00-\\x7F]", "");
							}
							
							String signature2 = null;	
							if ( (goldAuthorsSoft.length()>0) && (goldDate.length()>0) ) {
								signature2 = goldAuthorsSoft + goldDate;
								//signature2 = signature2.replaceAll("[^\\x00-\\x7F]", "");
							}
							
							String signature3 = null;	
							if ( (goldInTitleSoft.length()>0) && (goldVolume.length()>0) && (goldPage.length()>0)) {
								signature3 = goldInTitleSoft + goldVolume + goldPage;
								//signature3 = signature3.replaceAll("[^\\x00-\\x7F]", "");
							}
							
							String signature4 = null;
							if ( ((goldInTitleSoft.length()>0) || (goldTitleSoft.length()>0))
									&& (goldAuthorSoft.length()>0) ) {
								if (goldTitleSoft.length()>0)
									signature4 = goldAuthorSoft + goldTitleSoft;
								else
									signature4 = goldAuthorSoft + goldInTitleSoft;
								//signature4 = signature4.replaceAll("[^\\x00-\\x7F]", "");
							}		
							
							//signature = signature.replaceAll("[^\\x00-\\x7F]", "");
							
							//if (signature.trim().length() > 0) 
							{
								goldCitationSignaturesLevel1.add(signature1);
								goldCitationSignaturesLevel2.add(signature2);
								goldCitationSignaturesLevel3.add(signature3);
								goldCitationSignaturesLevel4.add(signature4);
								goldCitations.add(fieldsValues);
							}
						}
/*for(String sign : goldCitationSignaturesLevel1)
	System.out.println("gold 1:\t" + sign);
for(String sign : goldCitationSignaturesLevel2)
	System.out.println("gold 2:\t" + sign);
for(String sign : goldCitationSignaturesLevel3)
	System.out.println("gold 3:\t" + sign);
for(String sign : goldCitationSignaturesLevel4)
	System.out.println("gold 4:\t" + sign);*/
						
						// get the Grobid citations
						path = base.grobidPath.get(0);
						nodeList = (NodeList) xp.compile(path).
							evaluate(tei.getDocumentElement(), XPathConstants.NODESET);
						int nbCitationsGrobid = nodeList.getLength();
//System.out.println("found " + nbCitationsGrobid + " citations in Grobid TEI file.");
						totalObservedInstances += nbCitationsGrobid;
						List<Map<String,List<String>>> grobidCitations = 
							new ArrayList<Map<String,List<String>>>();
						for (int i = 0; i < nodeList.getLength(); i++) {
							Map<String,List<String>> fieldsValues = new HashMap<String,List<String>>();
							Node node = nodeList.item(i);
							int p = 0;
							for(FieldSpecification field : fields) {
								String fieldName = field.fieldName;
								if (fieldName.equals("base")) {
									//p++;
									continue;
								}
								for(String subpath : field.grobidPath) {
									NodeList nodeList2 = (NodeList) xp.compile(subpath).
										evaluate(node, XPathConstants.NODESET);
									List<String> grobidResults = new ArrayList<String>();
									for (int j = 0; j < nodeList2.getLength(); j++) {
										String content = nodeList2.item(j).getNodeValue();
										if ((content != null) && (content.trim().length() > 0))
											grobidResults.add(content);
									}
									if (grobidResults.size() > 0)
										fieldsValues.put(fieldName, grobidResults);
								}
								p++;
							}
							grobidCitations.add(fieldsValues);
						}
						
						for(Map<String,List<String>> grobidCitation: grobidCitations) {
							String grobidTitle = "";
							
							List<String> titleResults = grobidCitation.get("title");
							if (titleResults != null) {
								for(String res : titleResults) {
									grobidTitle += " " + res;
								}
							}
							grobidTitle = basicNormalization(grobidTitle);
							String grobidTitleSoft = removeFullPunct(grobidTitle);
						
							List<String> inTitleResults = grobidCitation.get("inTitle");
							String grobidInTitle = "";
							if (inTitleResults != null) {
								for(String res : inTitleResults) {
									grobidInTitle += " " + res;
								}
							}
							grobidInTitle = basicNormalization(grobidInTitle);
							String grobidInTitleSoft = removeFullPunct(grobidInTitle);
							
							// first author last name only
							List<String> authorResults = grobidCitation.get("first_author");
							String grobidAuthor = "";
							if ((authorResults != null) && (authorResults.size() > 0))
								grobidAuthor = authorResults.get(0);
							grobidAuthor = basicNormalization(grobidAuthor);
							String grobidAuthorSoft = removeFullPunct(grobidAuthor);
							
							// all authors last names
							String grobidAuthors = "";
							List<String> authorsResults = grobidCitation.get("authors");
							if ((authorsResults != null) && (authorsResults.size() > 0)) {
								for(String aut : authorsResults)
									grobidAuthors += aut;
							}
							grobidAuthors = basicNormalization(grobidAuthors);
							String grobidAuthorsSoft = removeFullPunct(grobidAuthors);
							
							// date of publication
							List<String> dateResults = grobidCitation.get("date");
							String grobidDate = "";
							if ((dateResults != null) && (dateResults.size() > 0))
								grobidDate = dateResults.get(0);
							grobidDate = basicNormalization(grobidDate);
							
							// volume
							List<String> volumeResults = grobidCitation.get("volume");
							String grobidVolume = "";
							if ((volumeResults != null) && (volumeResults.size() > 0))
								grobidVolume = volumeResults.get(0);
							grobidVolume = basicNormalization(grobidVolume);
							
							// first page
							List<String> pageResults = grobidCitation.get("page");
							String grobidPage = "";
							if ((pageResults != null) && (pageResults.size() > 0))
								grobidPage = pageResults.get(0);
							grobidPage = basicNormalization(grobidPage);
							
							String grobidSignature1 = null;								
							if ( (grobidTitleSoft.length()>0) && (grobidDate.length()>0) ) {
								grobidSignature1 = grobidTitleSoft + grobidDate;
								//grobidSignature1 = grobidSignature1.replaceAll("[^\\x00-\\x7F]", "");
							}
							
							String grobidSignature2 = null;	
							if ( (grobidAuthorsSoft.length()>0) && (grobidDate.length()>0) ) {
								grobidSignature2 = grobidAuthorsSoft + grobidDate;
								//grobidSignature2 = grobidSignature2.replaceAll("[^\\x00-\\x7F]", "");
							}
							
							String grobidSignature3 = null;	
							if ( (grobidInTitleSoft.length()>0) && (grobidVolume.length()>0) 
									&& (grobidPage.length()>0)) {
								grobidSignature3 = grobidInTitleSoft + grobidVolume + grobidPage;
								//grobidSignature3 = grobidSignature3.replaceAll("[^\\x00-\\x7F]", "");
							}
							
							String grobidSignature4 = null;	
							if ( ((grobidInTitleSoft.length()>0) || (grobidTitleSoft.length()>0))
									&& (grobidAuthorSoft.length()>0) ) {
								if (grobidTitleSoft.length()>0)
									grobidSignature4 = grobidAuthorSoft + grobidTitleSoft;
								else
									grobidSignature4 = grobidAuthorSoft + grobidInTitleSoft;
								//grobidSignature4 = grobidSignature4.replaceAll("[^\\x00-\\x7F]", "");
							}

/*System.out.println("grobid 1:\t" + grobidSignature1);
System.out.println("grobid 2:\t" + grobidSignature2);
System.out.println("grobid 3:\t" + grobidSignature3);
System.out.println("grobid 4:\t" + grobidSignature4);*/
							int indexGold = -1;
							// try to match an expected citation with the signature
							if ( ((grobidSignature1 != null) && (grobidSignature1.length() > 0)) || 
								 ((grobidSignature2 != null) && (grobidSignature2.length() > 0)) || 
							     ((grobidSignature3 != null) && (grobidSignature3.length() > 0)) || 
							     ((grobidSignature4 != null) && (grobidSignature4.length() > 0)))
							{		
								if ((grobidSignature1 != null) && 
									 goldCitationSignaturesLevel1.contains(grobidSignature1)) {
//System.out.println("match 1 !\t" + grobidSignature1);								
									// we have a citation-level match and we can evaluate the fields
									indexGold = goldCitationSignaturesLevel1.indexOf(grobidSignature1);
									match1++;
								}
								else if ((grobidSignature2 != null) && 
									goldCitationSignaturesLevel2.contains(grobidSignature2)) {
//System.out.println("match 2 !\t" + grobidSignature2);								
									// we have a citation-level match and we can evaluate the fields
									indexGold = goldCitationSignaturesLevel2.indexOf(grobidSignature2);
									match2++;
								}
								else if ((grobidSignature3 != null) && 
									goldCitationSignaturesLevel3.contains(grobidSignature3)) {
//System.out.println("match 3 !\t" + grobidSignature3);								
									// we have a citation-level match and we can evaluate the fields
									indexGold = goldCitationSignaturesLevel3.indexOf(grobidSignature3);
									match3++;
								}	
								else if ((grobidSignature4 != null) && 
									goldCitationSignaturesLevel4.contains(grobidSignature4)) {
//System.out.println("match 4 !\t" + grobidSignature4);								
									// we have a citation-level match and we can evaluate the fields
									indexGold = goldCitationSignaturesLevel4.indexOf(grobidSignature4);
									match4++;
								}
							
								if (indexGold != -1) {
									// we have aligned an extracted citation with an expected ones
									boolean allGoodStrict = true;
									boolean allGoodSoft = true;
									boolean allGoodLevenshtein = true;
									boolean allGoodRatcliffObershelp = true;
									Map<String,List<String>> goldCitation = goldCitations.get(indexGold);
									goldCitationSignaturesLevel1.remove(indexGold);
									goldCitationSignaturesLevel2.remove(indexGold);
									goldCitationSignaturesLevel3.remove(indexGold);
									goldCitationSignaturesLevel4.remove(indexGold);
									goldCitations.remove(indexGold);
									int p = 0;
									for(FieldSpecification field : fields) {
										String label = field.fieldName;
										if (label.equals("base")) {
											//p++;
											continue;
										}
									
										List<String> grobidResults = grobidCitation.get(label);
										//if (grobidResults == null) {
										//	p++;
										//	continue;
										//}

										String grobidResult = "";
										if (grobidResults != null) {
											for(String res : grobidResults) {
												grobidResult += " " + res;
											}
										}
										grobidResult = basicNormalization(grobidResult);
										
										List<String> goldResults = goldCitation.get(label);
										String goldResult = "";
										if (goldResults != null) {
											for(String res : goldResults) {
												goldResult += " " + res;
											}
										}
										goldResult = basicNormalization(goldResult);									
//System.out.println(label + ": strict grobid\t-> " + grobidResult);
//System.out.println(label + ": strict gold\t-> " + goldResult);
										// strict
										if ((goldResult.length()>0) && (goldResult.equals(grobidResult))) {
                                            strictStats.incrementObserved(label);
										}
										else {
											if ( (grobidResult.length() > 0) ) {
                                                strictStats.incrementFalsePositive(label);
												allGoodStrict = false;
											}
											else if (goldResult.length()>0) {
                                                strictStats.incrementFalseNegative(label);
												allGoodStrict = false;
											}
										}
								
										// soft
										String goldResultSoft = goldResult;
										String grobidResultSoft = grobidResult;
										if (field.isTextual) {
											goldResultSoft = removeFullPunct(goldResult);
											grobidResultSoft = removeFullPunct(grobidResult);
										}
										if ((goldResultSoft.length() > 0) && 
											(goldResultSoft.equals(grobidResultSoft)) ) {
                                            softStats.incrementObserved(label);
										}
										else {
											if (grobidResultSoft.length() > 0) {
                                                softStats.incrementFalsePositive(label);
												allGoodSoft = false;
											}
											else if (goldResultSoft.length() > 0) {
                                                softStats.incrementFalseNegative(label);
												allGoodSoft = false;
											}
										}
//System.out.println(label + ": soft grobid\t-> " + grobidResultSoft);
//System.out.println(label + ": soft gold\t-> " + goldResultSoft);										
										// Levenshtein
										double pct = 0.0;
										if ((goldResultSoft.length() > 0) && goldResult.equals(grobidResult))
											pct = 1.0;
										if (field.isTextual) {
											int distance = 
												TextUtilities.getLevenshteinDistance(goldResult, grobidResult);
											// Levenshtein distance is an integer value, not a percentage... however
											// articles usually introduced it as a percentage... so we report it
											// following the straightforward formula:
											int bigger = Math.max(goldResult.length(), grobidResult.length());
											pct = (double)(bigger - distance) / bigger;
										}
										if ((goldResultSoft.length() > 0) && (pct >= minLevenshteinDistance)) {
                                            levenshteinStats.incrementObserved(label);
										}
										else {
											if (grobidResultSoft.length() > 0) {
                                                levenshteinStats.incrementFalsePositive(label);
												allGoodLevenshtein = false;
											}
											else if (goldResultSoft.length() > 0) {
                                                levenshteinStats.incrementFalseNegative(label);
												allGoodLevenshtein = false;
											}
										}
						
										// RatcliffObershelp
										Double similarity = 0.0;
										if ((goldResultSoft.length() > 0) && goldResult.equals(grobidResult))
											similarity = 1.0;
										if (field.isTextual) {
											if ( (goldResult.length() > 0) && (grobidResult.length() > 0) ) {
												Option<Object> similarityObject = 
													RatcliffObershelpMetric.compare(goldResult, grobidResult);
												if ( (similarityObject != null) && (similarityObject.get() != null) )
													 similarity = (Double)similarityObject.get();
											}
										}
										if ((goldResultSoft.length() > 0) && 
											(similarity >= minRatcliffObershelpSimilarity)) {
                                            ratcliffObershelpStats.incrementObserved(label);
										}
										else {
											if (grobidResultSoft.length() > 0) {
                                                ratcliffObershelpStats.incrementFalsePositive(label);
												allGoodRatcliffObershelp = false;
											}
											else if (goldResultSoft.length() > 0) {
                                                ratcliffObershelpStats.incrementFalseNegative(label);
												allGoodRatcliffObershelp = false;
											}
										}
									
										p++;
									}
									if (allGoodStrict) {
										totalCorrectInstancesStrict++;
									}
									if (allGoodSoft) {
										totalCorrectInstancesSoft++;
									}
									if (allGoodLevenshtein) {
										totalCorrectInstancesLevenshtein++;
									}
									if (allGoodRatcliffObershelp) {
										totalCorrectInstancesRatcliffObershelp++;
									}
								}
								else {
									// we have a Grobid extracted citation, but no matching with 
									// expected ones -> false positive for all the present fields
									int p = 0;
									for(FieldSpecification field : fields) {
										String label = field.fieldName;
										if (label.equals("base")) {
											//p++;
											continue;
										}
									
										List<String> grobidResults = grobidCitation.get(label);
										if ( (grobidResults == null) || (grobidResults.size() == 0) ) {
											p++;
											continue;
										}
//for(String grobidResult : grobidResults)
//System.out.println(label + " / " + grobidResult);
										strictStats.incrementFalsePositive(label);
										softStats.incrementFalsePositive(label);
										levenshteinStats.incrementFalsePositive(label);
										ratcliffObershelpStats.incrementFalsePositive(label);

										p++;
									}
								}
							}
						}
					}
					else if (sectionType == this.HEADER) {
						// HEADER structures 
						int p = 0;
						boolean allGoodStrict = true;
						boolean allGoodSoft = true;
						boolean allGoodLevenshtein = true;
						boolean allGoodRatcliffObershelp = true;
						for(FieldSpecification field : fields) {
							String fieldName = field.fieldName;
						
							List<String> grobidResults = new ArrayList<String>();
							int nbGrobidResults = 0;
							for(String path : field.grobidPath) {
								NodeList nodeList = (NodeList) xp.compile(path).
									evaluate(tei.getDocumentElement(), XPathConstants.NODESET);
								nbGrobidResults = nodeList.getLength();
								for (int i = 0; i < nodeList.getLength(); i++) {
								    grobidResults.add(nodeList.item(i).getNodeValue());
								}
							}						
							//if (!field.hasMultipleValue) 
							{
								String grobidResult = "";
								for(String res : grobidResults)
									grobidResult += " " + res;
								// basic normalisation
								grobidResult = basicNormalization(grobidResult);
								//System.out.println("Grobid: " + fieldName + ":\t" + grobidResult);
								grobidResults = new ArrayList<String>();
								grobidResults.add(grobidResult);
								nbGrobidResults = 1;
							}
						
							List<String> goldResults = new ArrayList<String>();
							int nbGoldResults = 0;
							List<String> subpaths = null;
							if (inputType.equals("nlm")) {
								subpaths = field.nlmPath;
							} else if (inputType.equals("tei")) {
								subpaths = field.grobidPath;
							}

							if (subpaths == null)
								continue;

							for(String path : subpaths) {
								NodeList nodeList = (NodeList) xp.compile(path).
									evaluate(gold.getDocumentElement(), XPathConstants.NODESET);
								//System.out.println(path + ": " + nodeList.getLength() + " nodes");
								nbGoldResults = nodeList.getLength();
								for (int i = 0; i < nodeList.getLength(); i++) {
									goldResults.add(nodeList.item(i).getNodeValue());
								}
							}

							//if (!field.hasMultipleValue) 
							{
								String goldResult = "";
								for(String res : goldResults)
									goldResult += " " + res;
								// basic normalisation
								goldResult = basicNormalization(goldResult);								
								//System.out.println("gold:  " + fieldName + ":\t" + goldResult);
								goldResults = new ArrayList<String>();
								goldResults.add(goldResult);
								nbGoldResults = 1;
							}

							int g = 0; 
							for (String goldResult : goldResults) {
								String grobidResult = "";
								if (g < grobidResults.size())
									grobidResult = grobidResults.get(g);
								// nb expected results
								if (goldResult.length() > 0) {
                                    strictStats.incrementExpected(fieldName);
                                    softStats.incrementExpected(fieldName);
                                    levenshteinStats.incrementExpected(fieldName);
                                    ratcliffObershelpStats.incrementExpected(fieldName);
								}
//System.out.println("gold:   " + goldResult);
//System.out.println("grobid: " + grobidResult);		
								// strict
								if ((goldResult.length() > 0) && goldResult.equals(grobidResult)) {
                                    strictStats.incrementObserved(fieldName);
								}
								else {
									if (grobidResult.length() > 0) {
                                        strictStats.incrementFalsePositive(fieldName);
										allGoodStrict = false;
									}
									else if (goldResult.length() > 0) {
                                        strictStats.incrementFalseNegative(fieldName);
										allGoodStrict = false;
									}
								}
						
								// soft
								String goldResultSoft = goldResult;
								String grobidResultSoft = grobidResult;
								if (field.isTextual) {
									goldResultSoft = removeFullPunct(goldResult);
									grobidResultSoft = removeFullPunct(grobidResult);
								}
//System.out.println("gold:   " + goldResultSoft);
//System.out.println("grobid: " + grobidResultSoft);								
								if ((goldResult.length() > 0) && goldResultSoft.equals(grobidResultSoft)) {
                                    softStats.incrementObserved(fieldName);
								}
								else {
									if (grobidResultSoft.length() > 0) {
                                        softStats.incrementFalsePositive(fieldName);
										allGoodSoft = false;
									}
									else if (goldResultSoft.length() > 0){
                                        softStats.incrementFalseNegative(fieldName);
										allGoodSoft = false;
									}
								}
						
								// Levenshtein
								double pct = 0.0;
								if (goldResult.equals(grobidResult))
									pct = 1.0;
								if (field.isTextual) {
									int distance = TextUtilities.getLevenshteinDistance(goldResult, grobidResult);
									// Levenshtein distance is an integer value, not a percentage... however
									// articles usually introduced it as a percentage... so we report it
									// following the straightforward formula:
									int bigger = Math.max(goldResult.length(), grobidResult.length());
									pct = (double)(bigger - distance) / bigger;
								}
								if ((goldResult.length() > 0) && (pct >= minLevenshteinDistance)) {
                                    levenshteinStats.incrementObserved(fieldName);
								}
								else {
									if (grobidResultSoft.length() > 0) {
                                        levenshteinStats.incrementFalsePositive(fieldName);
										allGoodLevenshtein = false;
									}
									else if (goldResultSoft.length() > 0){
                                        levenshteinStats.incrementFalseNegative(fieldName);
										allGoodLevenshtein = false;
									}
								}
						
								// RatcliffObershelp
								Double similarity = 0.0;
								if (goldResult.trim().equals(grobidResult.trim()))
									similarity = 1.0;
								if (field.isTextual) {
									if ( (goldResult.length() > 0) && (grobidResult.length() > 0) ) {
										Option<Object> similarityObject = 
											RatcliffObershelpMetric.compare(goldResult, grobidResult);
										if ( (similarityObject != null) && (similarityObject.get() != null) )
											 similarity = (Double)similarityObject.get();
									}
								}
								if ((goldResult.length() > 0) && (similarity >= minRatcliffObershelpSimilarity)) {
                                    ratcliffObershelpStats.incrementObserved(fieldName);
								}
								else {
									if (grobidResultSoft.length() > 0) {
                                        ratcliffObershelpStats.incrementFalsePositive(fieldName);
										allGoodRatcliffObershelp = false;
									}
									else if (goldResultSoft.length() > 0){
                                        ratcliffObershelpStats.incrementFalseNegative(fieldName);
										allGoodRatcliffObershelp = false;
									}
								}
								g++;
							}
							p++;
						}
						totalExpectedInstances++;
						if (allGoodStrict) {
							totalCorrectInstancesStrict++;
						}
						if (allGoodSoft) {
							totalCorrectInstancesSoft++;
						}
						if (allGoodLevenshtein) {
							totalCorrectInstancesLevenshtein++;
						}
						if (allGoodRatcliffObershelp) {
							totalCorrectInstancesRatcliffObershelp++;
						}
					}
					else if (sectionType == this.FULLTEXT) {
						// full text structures 
						int p = 0;
						boolean allGoodStrict = true;
						boolean allGoodSoft = true;
						boolean allGoodLevenshtein = true;
						boolean allGoodRatcliffObershelp = true;
						for(FieldSpecification field : fields) {
							String fieldName = field.fieldName;
						
							List<String> grobidResults = new ArrayList<String>();
							int nbGrobidResults = 0;
							for(String path : field.grobidPath) {
								NodeList nodeList = (NodeList) xp.compile(path).
									evaluate(tei.getDocumentElement(), XPathConstants.NODESET);
								nbGrobidResults = nodeList.getLength();
								for (int i = 0; i < nodeList.getLength(); i++) {
								    grobidResults.add(basicNormalizationFullText(nodeList.item(i).getNodeValue(), fieldName));
								}
							}						

							/*boolean first = true;
							System.out.print("\n"+fieldName+" - ");
							System.out.print("\ngrobidResults:\t");
							for(String res : grobidResults) {
								if (!first)
									System.out.print(" | ");
								else 
									first = false;
								System.out.print(res);
							}
							System.out.println("");*/
							
							List<String> goldResults = new ArrayList<String>();
							int nbgoldResults = 0;
							List<String> subpaths = null;
							if (inputType.equals("nlm")) {
								subpaths = field.nlmPath;
							} else if (inputType.equals("tei")) {
								subpaths = field.grobidPath;
							}
							
							for(String path : subpaths) {
								NodeList nodeList = (NodeList) xp.compile(path).
									evaluate(gold.getDocumentElement(), XPathConstants.NODESET);
								//System.out.println(path + ": " + nodeList.getLength() + " nodes");
								nbgoldResults = nodeList.getLength();
								for (int i = 0; i < nodeList.getLength(); i++) {
									goldResults.add(basicNormalizationFullText(nodeList.item(i).getNodeValue(), fieldName));
								}
							}

							/*first = true;
							System.out.print("goldResults:\t");
							for(String res : goldResults) {
								if (!first)
									System.out.print(" | ");
								else 
									first = false;
								System.out.print(res);
							}
							System.out.println("");*/
							
							// we compare the two result sets
							
							// prepare first the grobidResult set for soft match
							List<String> grobidSoftResults = new ArrayList<String>();
							for(String res : grobidResults)
								grobidSoftResults.add(removeFullPunct(res));
							
							int g = 0; 
							int grobidResultsSize = grobidResults.size();
							int nbMatchStrict = 0; // number of matched grobid results, strict set
							int nbMatchSoft = 0; 
							int nbMatchLevenshtein = 0;
							int nbMatchRatcliffObershelp = 0;
							for (String goldResult : goldResults) {
								// nb expected results
								if (goldResult.length() > 0) {
                                    strictStats.incrementExpected(fieldName);
                                    softStats.incrementExpected(fieldName);
                                    levenshteinStats.incrementExpected(fieldName);
                                    ratcliffObershelpStats.incrementExpected(fieldName);
								}
								
								double pct = 0.0;
								// strict
								if ((goldResult.length() > 0) && grobidResults.contains(goldResult)) {
                                    strictStats.incrementObserved(fieldName);
									nbMatchStrict++;
									pct = 1.0;
									grobidResults.remove(goldResult);
								}
								else {
									if (goldResult.length() > 0) {
                                        strictStats.incrementFalseNegative(fieldName);
										allGoodStrict = false;
									}
								}
						
								// soft
								String goldResultSoft = goldResult;
								if (field.isTextual) {
									goldResultSoft = removeFullPunct(goldResult);
								}
								if ((goldResult.length() > 0) && grobidSoftResults.contains(goldResultSoft)) {
                                    softStats.incrementObserved(fieldName);
									nbMatchSoft++;
									grobidSoftResults.remove(goldResultSoft);
								}
								else {
									if (goldResultSoft.length() > 0){
                                        softStats.incrementFalseNegative(fieldName);
										allGoodSoft = false;
									}
								}
						
								/*StringBuilder goldResultBuilder = new StringBuilder();
								for (String goldResult : goldResults) {
									goldResultBuilder.append(goldResult).append(" ");
								}
								String goldResultString = goldResultBuilder.toString();
								StringBuilder grobidResultBuilder = new StringBuilder();
								for (String grobidResult : grobidResults) {
									grobidResultBuilder.append(grobidResult).append(" ");
								}
								String grobidResultString = grobidResultBuilder.toString();
								
								// Levenshtein
								if (field.isTextual) {
									int distance = TextUtilities.getLevenshteinDistance(goldResultString, grobidResultString);
									// Levenshtein distance is an integer value, not a percentage... however
									// articles usually introduced it as a percentage... so we report it
									// following the straightforward formula:
									int bigger = Math.max(goldResult.length(), grobidResult.length());
									pct = (double)(bigger - distance) / bigger;
								}
								if ((goldResult.length() > 0) && (pct >= minLevenshteinDistance)) {
									Integer count = counterObservedLevenshtein.get(p);
									counterObservedLevenshtein.set(p, count+1);
									nbMatchLevenshtein++;
								}
								else {
									if (goldResult.length() > 0){
										Integer count = counterFalseNegativeLevenshtein.get(p);
										counterFalseNegativeLevenshtein.set(p, count+1);
										allGoodLevenshtein = false;
									}
								}
						
								// RatcliffObershelp
								Double similarity = 0.0;
								if (goldResult.trim().equals(grobidResult.trim()))
									similarity = 1.0;
								if (field.isTextual) {
									if ( (goldResult.length() > 0) && (grobidResult.length() > 0) ) {
										Option<Object> similarityObject = 
											RatcliffObershelpMetric.compare(goldResultString, grobidResultString);
										if ( (similarityObject != null) && (similarityObject.get() != null) )
											 similarity = (Double)similarityObject.get();
									}
								}
								if ((goldResult.length() > 0) && (similarity >= minRatcliffObershelpSimilarity)) {
									Integer count = counterObservedRatcliffObershelp.get(p);
									counterObservedRatcliffObershelp.set(p, count+1);
									nbMatchRatcliffObershelp++;
								}
								else {
									if (grobidResultSoft.length() > 0) {
										Integer count = counterFalsePositiveRatcliffObershelp.get(p);
										counterFalsePositiveRatcliffObershelp.set(p, count+1);
										allGoodRatcliffObershelp = false;
									}
									else if (goldResultSoft.length() > 0){
										Integer count = counterFalseNegativeRatcliffObershelp.get(p);
										counterFalseNegativeRatcliffObershelp.set(p, count+1);
										allGoodRatcliffObershelp = false;
									}
								}*/
								g++;
							}
							
							if (nbMatchStrict < grobidResultsSize) {
                                strictStats.incrementFalsePositive(fieldName, grobidResultsSize-nbMatchStrict);
								allGoodStrict = false;
							}
							
							if (nbMatchSoft < grobidResultsSize) {
                                softStats.incrementFalsePositive(fieldName, grobidResultsSize-nbMatchSoft);
								allGoodSoft = false;
							}

							/*if (nbMatchLevenshtein < grobidResultsSize) {
                                levenshteinStats.incrementFalsePositive(fieldName, grobidResultsSize-nbMatchLevenshtein);
								allGoodLevenshtein= false;
							}

							if (nbMatchRatcliffObershelp < grobidResultsSize) {
                                ratcliffObershelpStats.incrementFalsePositive(fieldName, grobidResultsSize-nbMatchRatcliffObershelp);
								allGoodRatcliffObershelp = false;
							}*/

							p++;
						}
						
					}
				}
				else if (runType == this.PDFX) {

				}
				else if (runType == this.CERMINE) {

				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			nbFile++;
			//System.out.println("\n");
		}
		
		report.append("\nEvaluation on " + nbFile + " random PDF files out of " + 
			(refFiles.length-2) + " PDF (ratio " + fileRatio + ").\n");
		
		report.append("\n======= Strict Matching ======= (exact matches)\n");
		report.append("\n===== Field-level results =====\n");
		report.append(EvaluationUtilities.computeMetrics(strictStats));

		report.append("\n\n======== Soft Matching ======== (ignoring punctuation, " + 
			"case and space characters mismatches)\n");
		report.append("\n===== Field-level results =====\n");
		report.append(EvaluationUtilities.computeMetrics(softStats));

		if (sectionType != this.FULLTEXT) {
			report.append("\n\n==== Levenshtein Matching ===== (Minimum Levenshtein distance at " + 
				this.minLevenshteinDistance + ")\n");
			report.append("\n===== Field-level results =====\n");
			report.append(EvaluationUtilities.computeMetrics(levenshteinStats));

			report.append("\n\n= Ratcliff/Obershelp Matching = (Minimum Ratcliff/Obershelp similarity at " +
				minRatcliffObershelpSimilarity + ")\n");
			report.append("\n===== Field-level results =====\n");
			report.append(EvaluationUtilities.computeMetrics(ratcliffObershelpStats));
		}

		if (sectionType == this.CITATION) {
			report.append("\n===== Instance-level results =====\n\n");
			report.append("Total expected instances: \t\t").append(totalExpectedInstances).append("\n");
			report.append("Total extracted instances: \t\t").append(totalObservedInstances).append("\n");
			report.append("Total correct instances: \t\t").append(totalCorrectInstancesStrict)
				.append(" (strict) \n");
			report.append("Total correct instances: \t\t").append(totalCorrectInstancesSoft)
				.append(" (soft) \n");
			report.append("Total correct instances: \t\t").append(totalCorrectInstancesLevenshtein)
				.append(" (Levenshtein) \n");
			report.append("Total correct instances: \t\t").append(totalCorrectInstancesRatcliffObershelp)
				.append(" (RatcliffObershelp) \n");
			
			double precisionStrict = (double) totalCorrectInstancesStrict / (totalObservedInstances);
			double precisionSoft = (double) totalCorrectInstancesSoft / (totalObservedInstances);
			double precisionLevenshtein = (double) totalCorrectInstancesLevenshtein / (totalObservedInstances);
			double precisionRatcliffObershelp = (double) totalCorrectInstancesRatcliffObershelp / 
				(totalObservedInstances);
			report.append("\nInstance-level precision:\t")
				.append(TextUtilities.formatTwoDecimals(precisionStrict * 100)).append(" (strict) \n");
			report.append("Instance-level precision:\t")
				.append(TextUtilities.formatTwoDecimals(precisionSoft * 100)).append(" (soft) \n");
			report.append("Instance-level precision:\t")
				.append(TextUtilities.formatTwoDecimals(precisionLevenshtein * 100))
				.append(" (Levenshtein) \n");
			report.append("Instance-level precision:\t")
				.append(TextUtilities.formatTwoDecimals(precisionRatcliffObershelp * 100))
				.append(" (RatcliffObershelp) \n");
			
			double recallStrict = (double) totalCorrectInstancesStrict / (totalExpectedInstances);
			double recallSoft = (double) totalCorrectInstancesSoft / (totalExpectedInstances);
			double recallLevenshtein = (double) totalCorrectInstancesLevenshtein / (totalExpectedInstances);
			double recallRatcliffObershelp = (double) totalCorrectInstancesRatcliffObershelp / 
				(totalExpectedInstances);
			report.append("\nInstance-level recall:\t")
				.append(TextUtilities.formatTwoDecimals(recallStrict * 100)).append("\t(strict) \n");
			report.append("Instance-level recall:\t")
				.append(TextUtilities.formatTwoDecimals(recallSoft * 100)).append("\t(soft) \n");
			report.append("Instance-level recall:\t")
				.append(TextUtilities.formatTwoDecimals(recallLevenshtein * 100))
				.append("\t(Levenshtein) \n");
			report.append("Instance-level recall:\t")
				.append(TextUtilities.formatTwoDecimals(recallRatcliffObershelp* 100))
				.append("\t(RatcliffObershelp) \n");
			
			double f0Strict = (2 * precisionStrict * recallStrict) / (precisionStrict + recallStrict);
			double f0Soft = (2 * precisionSoft * recallSoft) / (precisionSoft + recallSoft);
			double f0Levenshtein = (2 * precisionLevenshtein * recallLevenshtein) / 
				(precisionLevenshtein + recallLevenshtein);
			double f0RatcliffObershelp = (2 * precisionRatcliffObershelp * recallRatcliffObershelp) / 
				(precisionRatcliffObershelp + recallRatcliffObershelp);
			report.append("\nInstance-level f-score:\t")
				.append(TextUtilities.formatTwoDecimals(f0Strict * 100)).append(" (strict) \n");
			report.append("Instance-level f-score:\t")
				.append(TextUtilities.formatTwoDecimals(f0Soft * 100)).append(" (soft) \n");
			report.append("Instance-level f-score:\t")
				.append(TextUtilities.formatTwoDecimals(f0Levenshtein * 100)).append(" (Levenshtein) \n");
			report.append("Instance-level f-score:\t")
				.append(TextUtilities.formatTwoDecimals(f0RatcliffObershelp * 100)).append(" (RatcliffObershelp) \n");
			
			report.append("\nMatching 1 :\t").append(match1 + "\n");
			report.append("\nMatching 2 :\t").append(match2 + "\n");
			report.append("\nMatching 3 :\t").append(match3 + "\n");
			report.append("\nMatching 4 :\t").append(match4 + "\n");
			report.append("\nTotal matches :\t").append((match1 + match2 + match3 + match4) + "\n");
		}
		else if (sectionType == this.HEADER) {
			report.append("\n===== Instance-level results =====\n\n");
			report.append("Total expected instances: \t").append(totalExpectedInstances).append("\n");
			report.append("Total correct instances: \t").append(totalCorrectInstancesStrict)
				.append(" (strict) \n");
			report.append("Total correct instances: \t").append(totalCorrectInstancesSoft)
				.append(" (soft) \n");
			report.append("Total correct instances: \t").append(totalCorrectInstancesLevenshtein)
				.append(" (Levenshtein) \n");
			report.append("Total correct instances: \t").append(totalCorrectInstancesRatcliffObershelp)
				.append(" (ObservedRatcliffObershelp) \n");
			double accuracyStrict = (double) totalCorrectInstancesStrict / (totalExpectedInstances);
			double accuracySoft = (double) totalCorrectInstancesSoft / (totalExpectedInstances);
			double accuracyLevenshtein = (double) totalCorrectInstancesLevenshtein / (totalExpectedInstances);
			double accuracyRatcliffObershelp = (double) totalCorrectInstancesRatcliffObershelp / 
				(totalExpectedInstances);
			report.append("\nInstance-level recall:\t")
				.append(TextUtilities.formatTwoDecimals(accuracyStrict * 100)).append("\t(strict) \n");
			report.append("Instance-level recall:\t")
				.append(TextUtilities.formatTwoDecimals(accuracySoft * 100)).append("\t(soft) \n");
			report.append("Instance-level recall:\t")
				.append(TextUtilities.formatTwoDecimals(accuracyLevenshtein * 100))
				.append("\t(Levenshtein) \n");
			report.append("Instance-level recall:\t")
				.append(TextUtilities.formatTwoDecimals(accuracyRatcliffObershelp * 100))
				.append("\t(RatcliffObershelp) \n");
		}

		return report.toString();
	}
	
	public void close() {
	}
	
	private static String basicNormalization(String string) {
		string = string.trim();
		string = string.replace("\n", " ");
		string = string.replaceAll("\t", " ");
		string = string.replaceAll(" ( )*", " ");
		return string.trim().toLowerCase();
	}
	
	private static String basicNormalizationFullText(String string, String fieldName) {
		string = string.trim();
		string = UnicodeUtil.normaliseText(string);
		string = string.replace("\n", " ");
		string = string.replace("\t", " ");
		string = string.replaceAll("_", " ");
		string = string.replace("\u00A0", " ");
		if (fieldName.equals("reference_figure")) {
			string = string.replace("figure", "").replace("Figure", "").replace("fig.", "").replace("Fig.", "").replace("fig", "").replace("Fig", "");
		}
		if (fieldName.equals("reference_table")) {
			string = string.replace("table", "").replace("Table", "");
		}
		string = string.replaceAll(" ( )*", " ");
		if (string.startsWith("[") || string.startsWith("("))
			string = string.substring(1,string.length());
		while (string.endsWith("]") || string.endsWith(")") || string.endsWith(","))
			string = string.substring(0,string.length()-1);
		return string.trim();
	}

	private static String removeFullPunct(String string) {
		StringBuilder result = new StringBuilder();
		string = string.toLowerCase();
		String allMismatchToIgnore = TextUtilities.fullPunctuations+"‐ \t\n\r\u00A0";
		for(int i=0; i<string.length(); i++) {
			if (allMismatchToIgnore.indexOf(string.charAt(i)) == -1) {
				result.append(string.charAt(i));
			}
		}
		return result.toString();
	}
	
    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
		if ( (args.length >4) || (args.length == 0) ) {
			System.err.println("usage: command [path to the (gold) evaluation XML dataset] Run[0|1] fileRatio[0.0-1.0]");
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
		
		String runGrobid = args[2];
		if (runGrobid.equals("0")) {
			runGrobidVal = false;
		}
		else if (runGrobid.equals("1")) {
			runGrobidVal = true;
		}
		else {
			System.err.println("Invalid value for last argument (run): [0|1]");
			return;
		}
		
		// optional file ratio for applying the evaluation
		double fileRatio = 1.0;
		if (args.length > 1) {
			String fileRatioString = args[3];
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
            EndToEndEvaluation eval = new EndToEndEvaluation(xmlInputPath, inputType);
			eval.fileRatio = fileRatio;
			String report = eval.evaluationGrobid(runGrobidVal);
			System.out.println(report);
			System.out.println(Engine.getCntManager());
			eval.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // to be sure jvm stops
        System.exit(0);
    }
	
}