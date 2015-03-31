package org.grobid.trainer.evaluation;

import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.exceptions.*;
import org.grobid.core.engines.Engine;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
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
 * Evaluation against PubMedCentral native XML documents.
 *
 * @author Patrice Lopez
 */
public class PubMedCentralEvaluation {
    private static String pubMedCentralPath = null;
	private Engine engine = null;
	
	public static final int GROBID = 0;
	public static final int PDFX = 1;
	public static final int CERMINE = 2;
	
	public static final int HEADER = 0;
	public static final int CITATION = 1;
	public static final int FULLTEXT = 2;
	
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
		
	public PubMedCentralEvaluation(String path) {
		pubMedCentralPath = path;	
	
		File pubMedCentralFile = new File(path);
		if (!pubMedCentralFile.exists()) {
			System.out.println("Path to PubMedCentral is not valid !");
			pubMedCentralPath = null;
		}
		
		String pGrobidHome = "../grobid-home";
		String pGrobidProperties = "../grobid-home/config/grobid.properties";

		try {
			MockContext.setInitialContext(pGrobidHome, pGrobidProperties);		
			GrobidProperties.getInstance();
			System.out.println(">>>>>>>> GROBID_HOME="+GrobidProperties.get_GROBID_HOME_PATH());

			engine = GrobidFactory.getInstance().createEngine();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// initialize the field specifications
		setUpFields();
	}
	
	private void setUpFields() {
		// header fields
		headerFields = new ArrayList<FieldSpecification>();	
		fulltextFields = new ArrayList<FieldSpecification>();	
		citationsFields = new ArrayList<FieldSpecification>();	
		headerLabels = new ArrayList<String>();
		fulltextLabels = new ArrayList<String>();
		citationsLabels = new ArrayList<String>();
		
		// header
		
		// title
		FieldSpecification titleField = new FieldSpecification();
		titleField.fieldName = "title";
		titleField.isTextual = true;
		titleField.grobidPath.add("//titleStmt/title/text()");
		titleField.nlmPath.add("/article/front/article-meta/title-group/article-title//text()");
		titleField.pdfxPath.add("/pdfx/article/front/title-group/article-title/text()");
		headerFields.add(titleField);
		headerLabels.add("title");
		
		// authors
		FieldSpecification authorField = new FieldSpecification();
		authorField.fieldName = "authors";
		authorField.isTextual = true;
		authorField.hasMultipleValue = true;
		/*authorField.grobidPath.
			add("//sourceDesc/biblStruct/analytic/author/persName/forename[@type=\"first\"]");
		authorField.grobidPath.
			add("//sourceDesc/biblStruct/analytic/author/persName/forename[@type=\"middle\"]");*/
		authorField.grobidPath.
			add("//sourceDesc/biblStruct/analytic/author/persName/surname/text()");
		//authorField.nlmPath.
		//	add("/article/front/article-meta/contrib-group/contrib[@contrib-type=\"author\"]/name/given-names");
		authorField.nlmPath.
			add("/article/front/article-meta/contrib-group/contrib[@contrib-type=\"author\"]/name/surname/text()");	
		authorField.pdfxPath.add("/pdfx/article/front/contrib-group/contrib[@contrib-type=\"author\"]/name/text()");
		headerFields.add(authorField);
		headerLabels.add("authors");

		// affiliation
		FieldSpecification affiliationField = new FieldSpecification();
		affiliationField.fieldName = "affiliations";
		affiliationField.isTextual = true;
		affiliationField.hasMultipleValue = true;
		affiliationField.grobidPath.
			add("//sourceDesc/biblStruct/analytic/author/affiliation/orgName/text()");
		affiliationField.nlmPath.
			add("/article/front/article-meta/contrib-group/aff/text()");
		affiliationField.pdfxPath.add("/pdfx/article/front/contrib-group");
		headerFields.add(affiliationField);
		headerLabels.add("affiliations");
		
		// date
		FieldSpecification dateField = new FieldSpecification();
		dateField.fieldName = "date";
		dateField.grobidPath.
			add("//publicationStmt/date/@when");
		dateField.nlmPath.
			add("/article/front/article-meta/pub-date[@pub-type=\"pmc-release\"]//text()");
		headerFields.add(dateField);
		headerLabels.add("date");

		// abstract
		FieldSpecification abstractField = new FieldSpecification();
		abstractField.fieldName = "abstract";
		abstractField.isTextual = true;
		abstractField.grobidPath.
			add("//profileDesc/abstract//text()");
		abstractField.nlmPath.
			add("/article/front/article-meta/abstract//text()");
		headerFields.add(abstractField);
		headerLabels.add("abstract");
		
		// keywords
		FieldSpecification keywordsField = new FieldSpecification();
		keywordsField.fieldName = "keywords";
		keywordsField.isTextual = true;
		keywordsField.grobidPath.
			add("//profileDesc/textClass/keywords//text()");
		keywordsField.nlmPath.
			add("/article/front/article-meta/kwd-group/kwd/text()");
		headerFields.add(keywordsField);
		headerLabels.add("keywords");
		
		// citations
		
		// the first field gives the base path for each citation structure
		FieldSpecification baseCitation = new FieldSpecification();
		baseCitation.fieldName = "base";
		baseCitation.grobidPath.
			add("//back/div/listBibl/biblStruct");
		baseCitation.nlmPath.
			add("//ref-list/ref");
		baseCitation.pdfxPath.
			add("//ref-list/ref"); // note: there is nothing beyond that in pdfx xml results!
		citationsFields.add(baseCitation);
		// the rest of the citation fields are relative to the base path 
		
		// title
		FieldSpecification titleField2 = new FieldSpecification();
		titleField2.fieldName = "title";
		titleField2.isTextual = true;
		titleField2.grobidPath.
			add("analytic/title/text()");
		titleField2.nlmPath.
			add("*/article-title//text()");
		citationsFields.add(titleField2);
		citationsLabels.add("title");
		
		// authors
		FieldSpecification authorField2 = new FieldSpecification();
		authorField2.fieldName = "authors";
		authorField2.isTextual = true;
		authorField2.hasMultipleValue = true;
		authorField2.grobidPath.
			add("analytic/author/persName/surname/text()");
		authorField2.nlmPath.
			add("*/person-group/name/surname/text()");
		citationsFields.add(authorField2);
		citationsLabels.add("author");
		
		// date
		FieldSpecification dateField2 = new FieldSpecification();
		dateField2.fieldName = "date";
		dateField2.grobidPath.
			add("monogr/imprint/date/@when");
		dateField2.nlmPath.
			add("*/year/text()");
		citationsFields.add(dateField2);
		citationsLabels.add("date");
		
		// date
		FieldSpecification inTitleField2 = new FieldSpecification();
		inTitleField2.fieldName = "inTitle";
		inTitleField2.grobidPath.
			add("monogr/title/text()");
		inTitleField2.nlmPath.
			add("*/source/text()");
		citationsFields.add(inTitleField2);
		citationsLabels.add("inTitle");
		
		// full text structures
		//labels.add("section_title");
		//labels.add("paragraph");
		//labels.add("citation_marker");
		//labels.add("figure_marker");
		//labels.add("table_marker");
	}
	
	public String evaluationGrobid(boolean forceRun) throws Exception {
		if (pubMedCentralPath == null) {
			throw new GrobidResourceException("Path to PubMedCentral is not correctly set");
		}
		StringBuilder report = new StringBuilder();
		
		if (forceRun) {
			// we run Grobid full text extraction on the PubMedCentral data
            File input = new File(pubMedCentralPath);
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
            for (File dir : refFiles) {
				// get the PDF file in the directory
	            File[] refFiles2 = dir.listFiles(new FilenameFilter() {
	                public boolean accept(File dir, String name) {
	                    return name.endsWith(".pdf") || name.endsWith(".PDF");
	                }
	            });

	            if (refFiles2 == null) {
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
					String tei = engine.fullTextToTEI(pdfFile.getPath(), false, false);
					// write the result in the same directory
					File resultTEI = new File(dir.getPath()+"/"+pdfFile.getName().replace(".pdf", ".tei.xml"));
					FileUtils.writeStringToFile(resultTEI, tei, "UTF-8");
				} 
				catch (Exception e) {
					e.printStackTrace();
				} 
				n++;
			}
		}
		
		// evaluation of the run
		
		report.append("\n======= Header metadata ======= \n");
		report.append(evaluationRun(this.GROBID, this.HEADER));
		
		report.append("\n======= Citation metadata ======= \n");
		//report.append(evaluationRun(this.GROBID, this.CITATION));
		
		report.append("\n======= Fulltext structures ======= \n");
		//report.append(evaluationRun(this.GROBID, this.FULLTEXT));
		
		return report.toString();
	}
	
	public String evaluationPDFX() throws Exception {
		if (pubMedCentralPath == null) {
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
		if (pubMedCentralPath == null) {
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
		
		// true positive
		final List<Integer> counterObservedStrict = new ArrayList<Integer>();
		// all expected
		final List<Integer> counterExpectedStrict = new ArrayList<Integer>();
		// false positive
		final List<Integer> counterFalsePositiveStrict = new ArrayList<Integer>();
		// false negative
		final List<Integer> counterFalseNegativeStrict = new ArrayList<Integer>();
		
		// true positive
		final List<Integer> counterObservedSoft = new ArrayList<Integer>();
		// all expected
		final List<Integer> counterExpectedSoft = new ArrayList<Integer>();
		// false positive
		final List<Integer> counterFalsePositiveSoft = new ArrayList<Integer>();
		// false negative
		final List<Integer> counterFalseNegativeSoft = new ArrayList<Integer>();
		
		// true positive
		final List<Integer> counterObservedLevenshtein = new ArrayList<Integer>();
		// all expected
		final List<Integer> counterExpectedLevenshtein = new ArrayList<Integer>();
		// false positive
		final List<Integer> counterFalsePositiveLevenshtein = new ArrayList<Integer>();
		// false negative
		final List<Integer> counterFalseNegativeLevenshtein = new ArrayList<Integer>();
		
		// true positive
		final List<Integer> counterObservedRatcliffObershelp = new ArrayList<Integer>();
		// all expected
		final List<Integer> counterExpectedRatcliffObershelp = new ArrayList<Integer>();
		// false positive
		final List<Integer> counterFalsePositiveRatcliffObershelp = new ArrayList<Integer>();
		// false negative
		final List<Integer> counterFalseNegativeRatcliffObershelp = new ArrayList<Integer>();
		
		List<String> labels = null;
		List<FieldSpecification> fields = null;
		
		int totalExpectedInstances = 0;
		int totalObservedInstances = 0;
		int totalCorrectInstances = 0;
		
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
		
		// intialize all the counters
		initFields(fields.size(),
			counterExpectedStrict, counterObservedStrict, 
			counterFalsePositiveStrict, counterFalseNegativeStrict);
		initFields(fields.size(),
			counterExpectedSoft, counterObservedSoft, 
			counterFalsePositiveSoft, counterFalseNegativeSoft);
		initFields(fields.size(),
			counterExpectedLevenshtein, counterObservedLevenshtein, 
			counterFalsePositiveLevenshtein, counterFalseNegativeLevenshtein);
		initFields(fields.size(),
			counterExpectedRatcliffObershelp, counterObservedRatcliffObershelp, 
			counterFalsePositiveRatcliffObershelp, counterFalseNegativeRatcliffObershelp);
		
        File input = new File(pubMedCentralPath);
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
		
		int nbFile = 0;
        for (File dir : refFiles) {
			if (nbFile > 100) {
				break;
			}
			
			// get the NLM file in the directory
            File[] refFiles2 = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".nxml");
                }
            });

            if (refFiles2 == null) {
            	System.out.println("warning: no PubMed NLM file found under " + dir.getPath());
			    continue;
			}

			if (refFiles2.length != 1) {
            	System.out.println("warning: more than one PubMed NLM files found under " + dir.getPath());
			    System.out.println("processing only the first one...");
			}
			
			File nlmFile = refFiles2[0];
			
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
				Document nlm = docBuilder.parse(nlmFile);

				// get the results of the evaluated tool for this file
				if (runType == this.GROBID) {
					// results are produced in a TEI file
		            File[] refFiles3 = dir.listFiles(new FilenameFilter() {
		                public boolean accept(File dir, String name) {
		                    return name.endsWith(".tei.xml");
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
		                    return name.endsWith(".nxml");
		                }
		            });

		            if (refFiles3 == null) {
		            	System.out.println("warning: no NLM file found under " + dir.getPath());
					    continue;
					}

					if (refFiles3.length != 1) {
		            	System.out.println("warning: more than one NLM files found under " + dir.getPath());
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
						String path = base.nlmPath.get(0);
						NodeList nodeList = (NodeList) xp.compile(path).
							evaluate(nlm.getDocumentElement(), XPathConstants.NODESET);
						int nbCitationsNLM = nodeList.getLength();
						totalExpectedInstances += nbCitationsNLM;
						System.out.println("found " + nbCitationsNLM + " citations in reference NLM file.");
						List<Map<String,List<String>>> nlmCitations = 
							new ArrayList<Map<String,List<String>>>();
						// "signature" of the citations for this file
						List<String> nlmCitationSignatures = new ArrayList<String>();
						for (int i = 0; i < nodeList.getLength(); i++) {
							Map<String,List<String>> fieldsValues = new HashMap<String,List<String>>();
							Node node = nodeList.item(i);
							int p = 0;
							for(FieldSpecification field : fields) {
								String fieldName = field.fieldName;
								for(String subpath : field.nlmPath) {
									NodeList nodeList2 = (NodeList) xp.compile(subpath).
										evaluate(node, XPathConstants.NODESET);
									
									List<String> nlmResults = new ArrayList<String>();
									for (int j = 0; j < nodeList2.getLength(); j++) {
										String content = nodeList2.item(j).getNodeValue();
										if ((content != null) && (content.trim().length() > 0))
											nlmResults.add(content);
									}
									
									if (nlmResults.size() > 0) {
										fieldsValues.put(fieldName, nlmResults);
										
										Integer count = counterExpectedStrict.get(p);
										counterExpectedStrict.set(p, count+1);

										count = counterExpectedSoft.get(p);
										counterExpectedSoft.set(p, count+1);

										count = counterExpectedLevenshtein.get(p);
										counterExpectedLevenshtein.set(p, count+1);

										count = counterExpectedRatcliffObershelp.get(p);
										counterExpectedRatcliffObershelp.set(p, count+1);
									}
								}
								
								p++;
							}
							nlmCitations.add(fieldsValues);
							
							// signature for this citation
							String nlmTitle = "";
							String nlmInTitle = "";
							List<String> nlmResults = fieldsValues.get("title");
							if (nlmResults != null) {
								for(String res : nlmResults) {
									nlmTitle += " " + res;
								}
							}
							nlmTitle = basicNormalization(nlmTitle);
							String nlmTitleSoft = removeFullPunct(nlmTitle);
							String nlmInTitleSoft = "";
							
							if (nlmTitleSoft.length() == 0) {
								// title is void, we look at the inTitle information
								List<String> inTitleResults = fieldsValues.get("inTitle");
								if (inTitleResults != null) {
									for(String res : inTitleResults) {
										nlmInTitle += " " + res;
									}
								}
								nlmInTitle = basicNormalization(nlmInTitle);
								nlmInTitleSoft = removeFullPunct(nlmInTitle);
							}
							
							List<String> authorResults = fieldsValues.get("authors");
							// first author last name only
							String nlmAuthor = "";
							if ((authorResults != null) && (authorResults.size() > 0))
								nlmAuthor = authorResults.get(0);
							nlmAuthor = basicNormalization(nlmAuthor);
							String nlmAuthorSoft = removeFullPunct(nlmAuthor);
							
							String signature = nlmAuthor;
							if (nlmTitleSoft.length()>0)
								signature += nlmTitleSoft;
							else
								signature += nlmInTitleSoft;
							nlmCitationSignatures.add(signature);
						}
						
						// get the Grobid citations
						path = base.grobidPath.get(0);
						nodeList = (NodeList) xp.compile(path).
							evaluate(tei.getDocumentElement(), XPathConstants.NODESET);
						int nbCitationsGrobid = nodeList.getLength();
						System.out.println("found " + nbCitationsGrobid + " citations in Grobid TEI file.");
						List<Map<String,List<String>>> grobidCitations = 
							new ArrayList<Map<String,List<String>>>();
						for (int i = 0; i < nodeList.getLength(); i++) {
							Map<String,List<String>> fieldsValues = new HashMap<String,List<String>>();
							Node node = nodeList.item(i);
							int p = 0;
							for(FieldSpecification field : fields) {
								String fieldName = field.fieldName;
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
							String grobidInTitle = "";
							List<String> titleResults = grobidCitation.get("title");
							if (titleResults != null) {
								for(String res : titleResults) {
									grobidTitle += " " + res;
								}
							}
							grobidTitle = basicNormalization(grobidTitle);
							String grobidTitleSoft = removeFullPunct(grobidTitle);
							String grobidInTitleSoft = "";
							
							if (grobidTitleSoft.length() == 0) {
								// title is void, we look at the inTitle information
								List<String> inTitleResults = grobidCitation.get("inTitle");
								if (inTitleResults != null) {
									for(String res : inTitleResults) {
										grobidInTitle += " " + res;
									}
								}
								grobidInTitle = basicNormalization(grobidInTitle);
								grobidInTitleSoft = removeFullPunct(grobidInTitle);
							}
							
							List<String> authorResults = grobidCitation.get("authors");
							// first author last name only
							String grobidAuthor = "";
							if ((authorResults != null) && (authorResults.size() > 0))
								grobidAuthor = authorResults.get(0);
							grobidAuthor = basicNormalization(grobidAuthor);
							String grobidAuthorSoft = removeFullPunct(grobidAuthor);
							
							String grobidSignature = grobidAuthorSoft;
							if (grobidTitleSoft.length() > 0)
								grobidSignature += grobidTitleSoft;
							else
								grobidSignature += grobidInTitleSoft;

							// try to match an expected citation with the signature
							if (nlmCitationSignatures.contains(grobidSignature)) {
								// we have a citation-level match and we can evaluate the fields
								int indexNLM = nlmCitationSignatures.indexOf(grobidSignature);
								if (indexNLM == -1)
									continue;
								Map<String,List<String>> nlmCitation = nlmCitations.get(indexNLM);
								int p = 0;
								for(FieldSpecification field : fields) {
									String label = field.fieldName;
									if (label.equals("base")) {
										p++;
										continue;
									}
									
									List<String> grobidResults = grobidCitation.get(label);
									if (grobidResults == null) {
										p++;
										continue;
									}
									
									String grobidResult = "";
									for(String res : grobidResults) {
										grobidResult += " " + res;
									}
									grobidResult = basicNormalization(grobidResult);
									
									List<String> nlmResults = nlmCitation.get(label);
									String nlmResult = "";
									if (nlmResults != null) {
										for(String res : nlmResults) {
											nlmResult += " " + res;
										}
									}
									nlmResult = basicNormalization(nlmResult);									

									// strict
									if ((nlmResult.length()>0) && (nlmResult.equals(grobidResult))) {
										Integer count = counterObservedStrict.get(p);
										counterObservedStrict.set(p, count+1);
									}
									else {
										if (grobidResult.length() > 0) {
											Integer count = counterFalsePositiveStrict.get(p);
											counterFalsePositiveStrict.set(p, count+1);
										}
										else if (nlmResult.length()>0) {
											Integer count = counterFalseNegativeStrict.get(p);
											counterFalseNegativeStrict.set(p, count+1);
										}
									}
									
									// soft
									String nlmResultSoft = nlmResult;
									String grobidResultSoft = grobidResult;
									if (field.isTextual) {
										nlmResultSoft = removeFullPunct(nlmResult);
										grobidResultSoft = removeFullPunct(grobidResult);
									}
									if (nlmResultSoft.trim().equals(grobidResultSoft.trim())) {
										Integer count = counterObservedSoft.get(p);
										counterObservedSoft.set(p, count+1);
									}
									else {
										if (grobidResultSoft.trim().length() > 0) {
											Integer count = counterFalsePositiveSoft.get(p);
											counterFalsePositiveSoft.set(p, count+1);
										}
										else {
											Integer count = counterFalseNegativeSoft.get(p);
											counterFalseNegativeSoft.set(p, count+1);
										}
									}
									
									// Levenshtein
									double pct = 0.0;
									if (nlmResult.equals(grobidResult))
										pct = 1.0;
									if (field.isTextual) {
										int distance = TextUtilities.getLevenshteinDistance(nlmResult, grobidResult);
										// Levenshtein distance is an integer value, not a percentage... however
										// articles usually introduced it as a percentage... so we report it
										// following the straightforward formula:
										int bigger = Math.max(nlmResult.length(), grobidResult.length());
										pct = (double)(bigger - distance) / bigger;
									}
									if (pct >= minLevenshteinDistance) {
										Integer count = counterObservedLevenshtein.get(p);
										counterObservedLevenshtein.set(p, count+1);
									}
									else {
										if (grobidResultSoft.length() > 0) {
											Integer count = counterFalsePositiveLevenshtein.get(p);
											counterFalsePositiveLevenshtein.set(p, count+1);
										}
										else {
											Integer count = counterFalseNegativeLevenshtein.get(p);
											counterFalseNegativeLevenshtein.set(p, count+1);
										}
									}
						
									// RatcliffObershelp
									Double similarity = 0.0;
									if (nlmResult.equals(grobidResult))
										similarity = 1.0;
									if (field.isTextual) {
										if ( (nlmResult.length() > 0) && (grobidResult.length() > 0) ) {
											Option<Object> similarityObject = 
												RatcliffObershelpMetric.compare(nlmResult, grobidResult);
											if ( (similarityObject != null) && (similarityObject.get() != null) )
												 similarity = (Double)similarityObject.get();
										}
									}
									if (similarity >= minRatcliffObershelpSimilarity) {
										Integer count = counterObservedRatcliffObershelp.get(p);
										counterObservedRatcliffObershelp.set(p, count+1);
									}
									else {
										if (grobidResultSoft.length() > 0) {
											Integer count = counterFalsePositiveRatcliffObershelp.get(p);
											counterFalsePositiveRatcliffObershelp.set(p, count+1);
										}
										else {
											Integer count = counterFalseNegativeRatcliffObershelp.get(p);
											counterFalseNegativeRatcliffObershelp.set(p, count+1);
										}
									}
									
									p++;
								}
							}
							else {
								// false positive
								
							}
						}
					}	
					else {
						// for non-citation structures, i.e. HEADER and FULTEXT
						int p = 0;
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
							if (!field.hasMultipleValue) {
								String grobidResult = "";
								for(String res : grobidResults)
									grobidResult += " " + res;
								// basic normalisation
								grobidResult = basicNormalization(grobidResult);
								System.out.println("Grobid: " + fieldName + ":\t" + grobidResult);
								grobidResults = new ArrayList<String>();
								grobidResults.add(grobidResult);
								nbGrobidResults = 1;
							}
						
							List<String> nlmResults = new ArrayList<String>();
							int nbNlmResults = 0;
							for(String path : field.nlmPath) {
								NodeList nodeList = (NodeList) xp.compile(path).
									evaluate(nlm.getDocumentElement(), XPathConstants.NODESET);
								//System.out.println(path + ": " + nodeList.getLength() + " nodes");
								nbNlmResults = nodeList.getLength();
								for (int i = 0; i < nodeList.getLength(); i++) {
									nlmResults.add(nodeList.item(i).getNodeValue());
								}
							}
							if (!field.hasMultipleValue) {
								String nlmResult = "";
								for(String res : nlmResults)
									nlmResult += " " + res;
								// basic normalisation
								nlmResult = basicNormalization(nlmResult);								
								System.out.println("nlm:  " + fieldName + ":\t" + nlmResult);
								nlmResults = new ArrayList<String>();
								nlmResults.add(nlmResult);
								nbNlmResults = 1;
							}

							int g = 0; 
							for (String nlmResult : nlmResults) {
								String grobidResult = "";
								if (g < grobidResults.size())
									grobidResult = grobidResults.get(g);
								// nb expected results
								if (nlmResult.trim().length() > 0) {
									Integer count = counterExpectedStrict.get(p);
									counterExpectedStrict.set(p, count+1);

									count = counterExpectedSoft.get(p);
									counterExpectedSoft.set(p, count+1);

									count = counterExpectedLevenshtein.get(p);
									counterExpectedLevenshtein.set(p, count+1);

									count = counterExpectedRatcliffObershelp.get(p);
									counterExpectedRatcliffObershelp.set(p, count+1);
								}
							
								// strict
								if (nlmResult.trim().equals(grobidResult.trim())) {
									Integer count = counterObservedStrict.get(p);
									counterObservedStrict.set(p, count+1);
								}
								else {
									if (grobidResult.trim().length() > 0) {
										Integer count = counterFalsePositiveStrict.get(p);
										counterFalsePositiveStrict.set(p, count+1);
									}
									else {
										Integer count = counterFalseNegativeStrict.get(p);
										counterFalseNegativeStrict.set(p, count+1);
									}
								}
						
								// soft
								String nlmResultSoft = nlmResult;
								String grobidResultSoft = grobidResult;
								if (field.isTextual) {
									nlmResultSoft = removeFullPunct(nlmResult);
									grobidResultSoft = removeFullPunct(grobidResult);
								}
								if (nlmResultSoft.trim().equals(grobidResultSoft.trim())) {
									Integer count = counterObservedSoft.get(p);
									counterObservedSoft.set(p, count+1);
								}
								else {
									if (grobidResultSoft.trim().length() > 0) {
										Integer count = counterFalsePositiveSoft.get(p);
										counterFalsePositiveSoft.set(p, count+1);
									}
									else {
										Integer count = counterFalseNegativeSoft.get(p);
										counterFalseNegativeSoft.set(p, count+1);
									}
								}
						
								// Levenshtein
								double pct = 0.0;
								if (nlmResult.trim().equals(grobidResult.trim()))
									pct = 1.0;
								if (field.isTextual) {
									int distance = TextUtilities.getLevenshteinDistance(nlmResult, grobidResult);
									// Levenshtein distance is an integer value, not a percentage... however
									// articles usually introduced it as a percentage... so we report it
									// following the straightforward formula:
									int bigger = Math.max(nlmResult.length(), grobidResult.length());
									pct = (double)(bigger - distance) / bigger;
								}
								if (pct >= minLevenshteinDistance) {
									Integer count = counterObservedLevenshtein.get(p);
									counterObservedLevenshtein.set(p, count+1);
								}
								else {
									if (grobidResultSoft.trim().length() > 0) {
										Integer count = counterFalsePositiveLevenshtein.get(p);
										counterFalsePositiveLevenshtein.set(p, count+1);
									}
									else {
										Integer count = counterFalseNegativeLevenshtein.get(p);
										counterFalseNegativeLevenshtein.set(p, count+1);
									}
								}
						
								// RatcliffObershelp
								Double similarity = 0.0;
								if (nlmResult.trim().equals(grobidResult.trim()))
									similarity = 1.0;
								if (field.isTextual) {
									if ( (nlmResult.length() > 0) && (grobidResult.length() > 0) ) {
										Option<Object> similarityObject = 
											RatcliffObershelpMetric.compare(nlmResult, grobidResult);
										if ( (similarityObject != null) && (similarityObject.get() != null) )
											 similarity = (Double)similarityObject.get();
									}
								}
								if (similarity >= minRatcliffObershelpSimilarity) {
									Integer count = counterObservedRatcliffObershelp.get(p);
									counterObservedRatcliffObershelp.set(p, count+1);
								}
								else {
									if (grobidResultSoft.trim().length() > 0) {
										Integer count = counterFalsePositiveRatcliffObershelp.get(p);
										counterFalsePositiveRatcliffObershelp.set(p, count+1);
									}
									else {
										Integer count = counterFalseNegativeRatcliffObershelp.get(p);
										counterFalseNegativeRatcliffObershelp.set(p, count+1);
									}
								}
								g++;
							}
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
			System.out.println("\n");
		}
		
		report.append("\n======= Strict Matching ======= (exact matches)\n");
		report.append("\n===== Field-level results =====\n");
		report.append(EvaluationUtilities.computeMetrics(labels, counterObservedStrict, 
			counterExpectedStrict, counterFalsePositiveStrict, counterFalseNegativeStrict));

		report.append("\n\n======== Soft Matching ======== (ignoring punctuation, " + 
			"case and space characters mismatches)\n");
		report.append("\n===== Field-level results =====\n");
		report.append(EvaluationUtilities.computeMetrics(labels, counterObservedSoft, 
			counterExpectedSoft, counterFalsePositiveSoft, counterFalseNegativeSoft));

		report.append("\n\n==== Levenshtein Matching ===== (Minimum Levenshtein distance at " + 
			this.minLevenshteinDistance + ")\n");
		report.append("\n===== Field-level results =====\n");
		report.append(EvaluationUtilities.computeMetrics(labels, counterObservedLevenshtein, 
			counterExpectedLevenshtein, counterFalsePositiveLevenshtein, counterFalseNegativeLevenshtein));

		report.append("\n\n= Ratcliff/Obershelp Matching = (Minimum Ratcliff/Obershelp similarity at " +
			minRatcliffObershelpSimilarity + ")\n");
		report.append("\n===== Field-level results =====\n");
		report.append(EvaluationUtilities.computeMetrics(labels, counterObservedRatcliffObershelp, 
			counterExpectedRatcliffObershelp, counterFalsePositiveRatcliffObershelp, 
			counterFalseNegativeRatcliffObershelp));

		return report.toString();
	}
	
	public void close() {
		try {
			MockContext.destroyInitialContext();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initFields(int nbFields, final List<Integer> counterExpected, 
										  final List<Integer> counterObserved, 
										  final List<Integer> counterFalsePositive, 
										  final List<Integer> counterFalseNegative) {
		for(int p=0; p<nbFields; p++) {
			counterExpected.add(0);
			counterObserved.add(0);
			counterFalsePositive.add(0);
			counterFalseNegative.add(0);	
		}							 
	}
	
	private static String basicNormalization(String string) {
		string = string.trim();
		string = string.replace("\n", " ");
		string = string.replaceAll("\t", " ");
		string = string.replaceAll(" ( )*", " ");
		return string.trim();
	}
	
	private static String removeFullPunct(String string) {
		StringBuilder result = new StringBuilder();
		string = string.toLowerCase();
		String allMismatchToIgnore = TextUtilities.fullPunctuations+" \t\n\r\u00A0";
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
		if (args.length != 1) {
			System.err.println("usage: command [path to the PubMedCentral dataset]");
		}
		String pubMedCentralPath = args[0];
        try {
            PubMedCentralEvaluation eval = new PubMedCentralEvaluation(pubMedCentralPath);
			String report = eval.evaluationGrobid(false);
			System.out.println(report);
			eval.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}