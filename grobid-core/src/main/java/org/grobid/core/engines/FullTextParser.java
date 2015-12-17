package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.document.TEIFormater;
import org.grobid.core.engines.citations.LabeledReferenceResult;
import org.grobid.core.engines.citations.ReferenceSegmenter;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.counters.CitationParserCounters;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorFulltext;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * @author Patrice Lopez
 */
public class FullTextParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FullTextParser.class);

    //private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();

    //	private String tmpPathName = null;
//    private Document doc = null;
    private File tmpPath = null;
//    private String pathXML = null;
//	private BiblioItem resHeader = null;

	// default bins for relative position
    private static final int NBBINS = 12;
    private EngineParsers parsers;

    /**
     * TODO some documentation...
     */
    public FullTextParser(EngineParsers parsers) {
        super(GrobidModels.FULLTEXT);
        this.parsers = parsers;
        tmpPath = GrobidProperties.getTempPath();
    }

    /**
     * Machine-learning recognition of the complete full text structures.
     *
     * @param inputPdf filename of pdf file
     * @param config config
     * @return the document object with built TEI
     */
    public Document processing(File inputPdf,
                               GrobidAnalysisConfig config) throws Exception {
        if (inputPdf == null) {
            throw new GrobidResourceException("Cannot process pdf file, because input file was null.");
        }
        if (!inputPdf.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because input file '" +
                    inputPdf.getAbsolutePath() + "' does not exists.");
        }
        if (tmpPath == null) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        }
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                    tmpPath.getAbsolutePath() + "' does not exists.");
        }
        try {
            // general segmentation
            Document doc = parsers.getSegmentationParser().processing(inputPdf, config);
			SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);
			Pair<String, LayoutTokenization> featSeg = getBodyTextFeatured(doc, documentBodyParts);
			String rese = null;
			LayoutTokenization layoutTokenization = null;
			//List<String> tokenizationsBody = null;
			//List<LayoutToken> layoutTokensBody = null;
			if (featSeg != null) {
				// if featSeg is null, it usually means that no body segment is found in the
				// document segmentation
				String bodytext = featSeg.getA();
				layoutTokenization = featSeg.getB();
				//tokenizationsBody = featSeg.getB().getTokenization();
                //layoutTokensBody = featSeg.getB().getLayoutTokens();
				if ( (bodytext != null) && (bodytext.trim().length() > 0) ) {
					rese = label(bodytext);
				}
				//System.out.println(rese);
			}

            // header processing
			BiblioItem resHeader = new BiblioItem();
			//if (mode == 0)
			{
            	parsers.getHeaderParser().processingHeaderBlock(config.isConsolidateHeader(), doc, resHeader);
			}
			/*else {
				parsers.getHeaderParser().processingHeaderSection(doc, consolidateHeader, resHeader);
			}*/
            // citation processing
            List<BibDataSet> resCitations = parsers.getCitationParser().
				processingReferenceSection(doc, parsers.getReferenceSegmenterParser(), config.isConsolidateCitations());

            doc.setBibDataSets(resCitations);

            if (resCitations != null) {
                for (BibDataSet bds : resCitations) {
                    String marker = bds.getRefSymbol();
                    if (marker != null) {
                        marker = marker.replace(".", "");
                        marker = marker.replace(" ", "");
                        bds.setRefSymbol(marker);
                    }
                    //BiblioItem bib = citationParser.processing(bds.getRawBib(), consolidateCitations);
                    //bds.setResBib(bib);
                }
            }

			// possible annexes (view as a piece of full text similar to the body)
			documentBodyParts = doc.getDocumentPart(SegmentationLabel.ANNEX);
            featSeg = getBodyTextFeatured(doc, documentBodyParts);
			String rese2 = null;
			List<LayoutToken> tokenizationsBody2 = null;
			if (featSeg != null) {
				// if featSeg is null, it usually means that no body segment is found in the
				// document segmentation
				String bodytext = featSeg.getA();
				tokenizationsBody2 = featSeg.getB().getTokenization();
	            rese2 = label(bodytext);
				//System.out.println(rese);
			}

            // final combination
            toTEI(doc, // document
				rese, rese2, // labeled data for body and annex
				layoutTokenization, tokenizationsBody2, // tokenization for body and annex
				resHeader, resCitations, // header and bibliographical citations
				config);
            return doc;
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
    }

	static public Pair<String, LayoutTokenization> getBodyTextFeatured(Document doc,
                                                                       SortedSet<DocumentPiece> documentBodyParts) {
		if ((documentBodyParts == null) || (documentBodyParts.size() == 0)) {
			return null;
		}
		FeatureFactory featureFactory = FeatureFactory.getInstance();
        StringBuilder fulltext = new StringBuilder();
        String currentFont = null;
        int currentFontSize = -1;

		List<Block> blocks = doc.getBlocks();
		if ( (blocks == null) || blocks.size() == 0) {
			return null;
		}

        // vector for features
        FeaturesVectorFulltext features;
        FeaturesVectorFulltext previousFeatures = null;
//        LayoutToken layoutToken = null;
        boolean endblock;
        boolean endPage = true;
        boolean newPage = true;
        boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        int documentLength = 0;
        int pageLength = 0; // length of the current page

		List<LayoutToken> tokenizationsBody = new ArrayList<LayoutToken>();
		List<LayoutToken> layoutTokens = new ArrayList<LayoutToken>();
		List<LayoutToken> tokenizations = doc.getTokenizations();

        // we calculate current document length and intialize the body tokenization structure
		for(DocumentPiece docPiece : documentBodyParts) {
			DocumentPointer dp1 = docPiece.a;
			DocumentPointer dp2 = docPiece.b;

            int tokens = dp1.getTokenDocPos();
            int tokene = dp2.getTokenDocPos();
            for (int i = tokens; i <= tokene; i++) {
                tokenizationsBody.add(tokenizations.get(i));
				documentLength++;
            }
		}

        // System.out.println("documentLength: " + documentLength);
		for(DocumentPiece docPiece : documentBodyParts) {
			DocumentPointer dp1 = docPiece.a;
			DocumentPointer dp2 = docPiece.b;

			//int blockPos = dp1.getBlockPtr();
			for(int blockIndex = dp1.getBlockPtr(); blockIndex <= dp2.getBlockPtr(); blockIndex++) {
            	Block block = blocks.get(blockIndex);

           	 	// we estimate the length of the page where the current block is
	            if (start || endPage) {
	                boolean stop = false;
	                pageLength = 0;
	                for (int z = blockIndex; (z < blocks.size()) && !stop; z++) {
	                    String localText2 = blocks.get(z).getText();
	                    if (localText2 != null) {
	                        if (localText2.contains("@PAGE")) {
	                            if (pageLength > 0) {
	                                if (blocks.get(z).getTokens() != null) {
	                                    pageLength += blocks.get(z).getTokens()
	                                            .size();
	                                }
	                                stop = true;
	                                break;
	                            }
	                        } else {
	                            if (blocks.get(z).getTokens() != null) {
	                                pageLength += blocks.get(z).getTokens().size();
	                            }
	                        }
	                    }
	                }
	                // System.out.println("pageLength: " + pageLength);
	            }
	            if (start) {
	                newPage = true;
	                start = false;
	            }
	            boolean newline;
	            boolean previousNewline = false;
	            endblock = false;

	            if (endPage) {
	                newPage = true;
	                mm = 0;
	            }

	            String localText = block.getText();
	            if (localText != null) {
	                if (localText.contains("@PAGE")) {
	                    mm = 0;
	                    // pageLength = 0;
	                    endPage = true;
	                    newPage = false;
	                } else {
	                    endPage = false;
	                }
	            }

	            List<LayoutToken> tokens = block.getTokens();
	            if (tokens == null) {
	                //blockPos++;
	                continue;
	            }

				int n = 0;// token position in current block
				if (blockIndex == dp1.getBlockPtr()) {
					n = dp1.getTokenDocPos() - block.getStartToken();
					/*if (n != 0) {
						n = n - 1;
					}*/
				}
	            while (n < tokens.size()) {
					if (blockIndex == dp2.getBlockPtr()) {
						//if (n > block.getEndToken()) {
						if (n > dp2.getTokenDocPos() - block.getStartToken()) {
							break;
						}
					}

	                LayoutToken token = tokens.get(n);
	                features = new FeaturesVectorFulltext();
	                features.token = token;

	                String text = token.getText();
	                if (text == null) {
	                    n++;
	                    mm++;
	                    nn++;
	                    continue;
	                }
	                text = text.replace(" ", "");
	                if (text.length() == 0) {
	                    n++;
	                    mm++;
	                    nn++;
	                    continue;
	                }

	                if (text.equals("\n")) {
	                    newline = true;
	                    previousNewline = true;
	                    n++;
	                    mm++;
	                    nn++;
	                    continue;
	                } else
	                    newline = false;

	                if (previousNewline) {
	                    newline = true;
	                    previousNewline = false;
	                }

	                boolean filter = false;
	                if (text.startsWith("@IMAGE")) {
	                    filter = true;
	                } else if (text.contains(".pbm")) {
	                    filter = true;
	                } else if (text.contains(".vec")) {
	                    filter = true;
	                } else if (text.contains(".jpg")) {
	                    filter = true;
	                }

	                if (filter) {
	                    n++;
	                    mm++;
	                    nn++;
	                    continue;
	                }

	                features.string = text;


	                if (newline)
	                    features.lineStatus = "LINESTART";
	                Matcher m0 = featureFactory.isPunct.matcher(text);
	                if (m0.find()) {
	                    features.punctType = "PUNCT";
	                }
                    if (text.equals("(") || text.equals("[")) {
                        features.punctType = "OPENBRACKET";

                    } else if (text.equals(")") || text.equals("]")) {
                        features.punctType = "ENDBRACKET";

                    } else if (text.equals(".")) {
                        features.punctType = "DOT";

                    } else if (text.equals(",")) {
                        features.punctType = "COMMA";

                    } else if (text.equals("-")) {
                        features.punctType = "HYPHEN";

                    } else if (text.equals("\"") || text.equals("\'") || text.equals("`")) {
                        features.punctType = "QUOTE";

                    }

	                if (n == 0) {
	                    features.lineStatus = "LINESTART";
	                    features.blockStatus = "BLOCKSTART";
	                } else if (n == tokens.size() - 1) {
	                    features.lineStatus = "LINEEND";
	                    previousNewline = true;
	                    features.blockStatus = "BLOCKEND";
	                    endblock = true;
	                } else {
	                    // look ahead...
	                    boolean endline = false;

	                    int ii = 1;
	                    boolean endloop = false;
	                    while ((n + ii < tokens.size()) && (!endloop)) {
	                        LayoutToken tok = tokens.get(n + ii);
	                        if (tok != null) {
	                            String toto = tok.getText();
	                            if (toto != null) {
	                                if (toto.equals("\n")) {
	                                    endline = true;
	                                    endloop = true;
	                                } else {
	                                    if ((toto.length() != 0)
	                                            && (!(toto.startsWith("@IMAGE")))
	                                            && (!text.contains(".pbm"))
	                                            && (!text.contains(".vec"))
	                                            && (!text.contains(".jpg"))) {
	                                        endloop = true;
	                                    }
	                                }
	                            }
	                        }

	                        if (n + ii == tokens.size() - 1) {
	                            endblock = true;
	                            endline = true;
	                        }

	                        ii++;
	                    }

	                    if ((!endline) && !(newline)) {
	                        features.lineStatus = "LINEIN";
	                    }
						else if (!newline) {
	                        features.lineStatus = "LINEEND";
	                        previousNewline = true;
	                    }

	                    if ((!endblock) && (features.blockStatus == null))
	                        features.blockStatus = "BLOCKIN";
	                    else if (features.blockStatus == null) {
	                        features.blockStatus = "BLOCKEND";
	                        endblock = true;
	                    }
	                }

	                if (newPage) {
	                    features.pageStatus = "PAGESTART";
	                    newPage = false;
	                    endPage = false;
	                    if (previousFeatures != null)
	                        previousFeatures.pageStatus = "PAGEEND";
	                } else {
	                    features.pageStatus = "PAGEIN";
	                    newPage = false;
	                    endPage = false;
	                }

	                if (text.length() == 1) {
	                    features.singleChar = true;
	                }

	                if (Character.isUpperCase(text.charAt(0))) {
	                    features.capitalisation = "INITCAP";
	                }

	                if (featureFactory.test_all_capital(text)) {
	                    features.capitalisation = "ALLCAP";
	                }

	                if (featureFactory.test_digit(text)) {
	                    features.digit = "CONTAINSDIGITS";
	                }

	                if (featureFactory.test_common(text)) {
	                    features.commonName = true;
	                }

	                if (featureFactory.test_names(text)) {
	                    features.properName = true;
	                }

	                if (featureFactory.test_month(text)) {
	                    features.month = true;
	                }

	                Matcher m = featureFactory.isDigit.matcher(text);
	                if (m.find()) {
	                    features.digit = "ALLDIGIT";
	                }

	                Matcher m2 = featureFactory.YEAR.matcher(text);
	                if (m2.find()) {
	                    features.year = true;
	                }

	                Matcher m3 = featureFactory.EMAIL.matcher(text);
	                if (m3.find()) {
	                    features.email = true;
	                }

	                Matcher m4 = featureFactory.HTTP.matcher(text);
	                if (m4.find()) {
	                    features.http = true;
	                }

	                if (currentFont == null) {
	                    currentFont = token.getFont();
	                    features.fontStatus = "NEWFONT";
	                } else if (!currentFont.equals(token.getFont())) {
	                    currentFont = token.getFont();
	                    features.fontStatus = "NEWFONT";
	                } else
	                    features.fontStatus = "SAMEFONT";

	                int newFontSize = (int) token.getFontSize();
	                if (currentFontSize == -1) {
	                    currentFontSize = newFontSize;
	                    features.fontSize = "HIGHERFONT";
	                } else if (currentFontSize == newFontSize) {
	                    features.fontSize = "SAMEFONTSIZE";
	                } else if (currentFontSize < newFontSize) {
	                    features.fontSize = "HIGHERFONT";
	                    currentFontSize = newFontSize;
	                } else if (currentFontSize > newFontSize) {
	                    features.fontSize = "LOWERFONT";
	                    currentFontSize = newFontSize;
	                }

	                if (token.getBold())
	                    features.bold = true;

	                if (token.getItalic())
	                    features.italic = true;

	                // HERE horizontal information
	                // CENTERED
	                // LEFTAJUSTED
	                // CENTERED

	                if (features.capitalisation == null)
	                    features.capitalisation = "NOCAPS";

	                if (features.digit == null)
	                    features.digit = "NODIGIT";

	                if (features.punctType == null)
	                    features.punctType = "NOPUNCT";

	                features.relativeDocumentPosition = featureFactory
	                        .relativeLocation(nn, documentLength, NBBINS);
	                // System.out.println(mm + " / " + pageLength);
	                features.relativePagePosition = featureFactory
	                        .relativeLocation(mm, pageLength, NBBINS);

	                // fulltext.append(features.printVector());
	                if (previousFeatures != null) {
                        fulltext.append(previousFeatures.printVector());
                    }
	                n++;
	                mm++;
	                nn++;
	                previousFeatures = features;
                    layoutTokens.add(token);
            	}
            	//blockPos++;
			}
        }
        if (previousFeatures != null) {
            fulltext.append(previousFeatures.printVector());

        }

        return new Pair<String,LayoutTokenization>(fulltext.toString(),
			new LayoutTokenization(tokenizationsBody));
	}

    /**
     * Process the full text of the specified pdf and format the result as training data.
     *
     * @param inputFile input file
     * @param pathFullText path to fulltext
     * @param pathTEI path to TEI
     * @param id id
     */
    public Document createTrainingFullText(File inputFile,
                                       String pathFullText,
                                       String pathTEI,
                                       int id) {
        if (tmpPath == null)
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                    tmpPath.getAbsolutePath() + "' does not exists.");
        }
        Document doc;
        try {
            if (!inputFile.exists()) {
               	throw new GrobidResourceException("Cannot train for fulltext, becuase file '" +
                       inputFile.getAbsolutePath() + "' does not exists.");
           	}
           	String PDFFileName = inputFile.getName();

            doc = parsers.getSegmentationParser().processing(inputFile, GrobidAnalysisConfig.defaultInstance());

            //String fulltext = doc.getFulltextFeatured(true, true);
			SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);
			if (documentBodyParts != null) {
				Pair<String, LayoutTokenization> featSeg = getBodyTextFeatured(doc, documentBodyParts);
				if (featSeg == null) {
					// no textual body part found, nothing to generate
					return doc;
				}

				String bodytext = featSeg.getA();
				List<LayoutToken> tokenizationsBody = featSeg.getB().getTokenization();
				
				/*List<String> tokenizationsBody = new ArrayList<String>();
				List<String> tokenizations = doc.getTokenizations();
				
				for(DocumentPiece docPiece : documentBodyParts) {
					DocumentPointer dp1 = docPiece.a;
					DocumentPointer dp2 = docPiece.b;
					
		            int tokens = dp1.getTokenDocPos();
		            int tokene = dp2.getTokenDocPos();
		            for (int i = tokens; i < tokene; i++) {
		                tokenizationsBody.add(tokenizations.get(i)); 
		            }
				}
				*/

	            // we write the full text untagged
	            String outPathFulltext = pathFullText + File.separator
					+ PDFFileName.replace(".pdf", ".training.fulltext");
	            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");
	            writer.write(bodytext + "\n");
	            writer.close();

	//            StringTokenizer st = new StringTokenizer(fulltext, "\n");
	            String rese = label(bodytext);
	            StringBuffer bufferFulltext = trainingExtraction(rese, tokenizationsBody);

	            // write the TEI file to reflect the extract layout of the text as extracted from the pdf
	            writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                    File.separator +
						PDFFileName.replace(".pdf", ".training.fulltext.tei.xml")), false), "UTF-8");
				if (id == -1) {
					writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader/>\n\t<text xml:lang=\"en\">\n");
				}
				else {
					writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
	                    "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">\n");
				}
	            writer.write(bufferFulltext.toString());
	            writer.write("\n\t</text>\n</tei>\n");
	            writer.close();

				// output of header as training data
				// ...

	            // output of the identified citations as traning data
				StringBuilder allBufferReference = new StringBuilder();
	            String referencesStr = doc.getDocumentPartText(SegmentationLabel.REFERENCES);
	            if (!referencesStr.isEmpty()) {
	                cntManager.i(CitationParserCounters.NOT_EMPTY_REFERENCES_BLOCKS);
	            }
	//			List<String> tokenizations = doc.getTokenizationsReferences();
				ReferenceSegmenter referenceSegmenter = parsers.getReferenceSegmenterParser();
	            //List<LabeledReferenceResult> references = referenceSegmenter.extract(referencesStr);
				List<LabeledReferenceResult> references = referenceSegmenter.extract(doc);

	            if (references == null) {
	                cntManager.i(CitationParserCounters.NULL_SEGMENTED_REFERENCES_LIST);
	                return doc;
	            } else {
	                cntManager.i(CitationParserCounters.SEGMENTED_REFERENCES, references.size());
	            }
				List<String> allInput = new ArrayList<String>();
	            for (LabeledReferenceResult ref : references) {
					allInput.add(ref.getReferenceText());
				}
				StringBuilder bufferReference = parsers.getCitationParser().trainingExtraction(allInput);
                if (bufferReference != null) {
                    bufferReference.append("\n");

	                Writer writerReference = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                        File.separator +
							PDFFileName.replace(".pdf", ".training.references.tei.xml")), false), "UTF-8");

					writerReference.write("<?xml version=\"1.0\" ?>\n<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" " +
											"xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
					                		"\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
					if (id == -1) {
						writerReference.write("\t<teiHeader/>\n\t<text>\n\t\t<front/>\n\t\t<body/>\n\t\t<back>\n");
					}
					else {
						writerReference.write("\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
		                    "\"/>\n\t</teiHeader>\n\t<text>\n\t\t<front/>\n\t\t<body/>\n\t\t<back>\n");
					}
	                writerReference.write("<listBibl>\n");

	                writerReference.write(bufferReference.toString());

					writerReference.write("\t\t</listBibl>\n\t</back>\n\t</text>\n</TEI>\n");
	                writerReference.close();

					// output of citation author names
	                Writer writerName = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                        File.separator +
							PDFFileName.replace(".pdf", ".training.citations.authors.tei.xml")), false), "UTF-8");

					writerName.write("<?xml version=\"1.0\" ?>\n<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" " +
											"xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
					                		"\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
					writerName.write("\t<teiHeader>\n\t\t<fileDesc>\n\t\t\t<sourceDesc>\n" +
									 "\t\t\t\t<biblStruct>\n\t\t\t\t\t<analytic>\n\n");

		            for (LabeledReferenceResult ref : references) {
						if ( (ref.getReferenceText() != null) && (ref.getReferenceText().trim().length() > 0) ) {
			                BiblioItem bib = parsers.getCitationParser().processing(ref.getReferenceText(), false);
			                String authorSequence = bib.getAuthors();
							if ((authorSequence != null) && (authorSequence.trim().length() > 0) ) {
								List<String> inputs = new ArrayList<String>();
								inputs.add(authorSequence);
								StringBuffer bufferName = parsers.getAuthorParser().trainingExtraction(inputs, false);

								if ( (bufferName != null) && (bufferName.length()>0) ) {
									writerName.write("\n\t\t\t\t\t\t<author>");
									writerName.write(bufferName.toString());
									writerName.write("</author>\n");
								}
							}
						}
					}

					writerName.write("\n\t\t\t\t\t</analytic>");
					writerName.write("\n\t\t\t\t</biblStruct>\n\t\t\t</sourceDesc>\n\t\t</fileDesc>");
					writerName.write("\n\t</teiHeader>\n</TEI>\n");
					writerName.close();
	            }
			}

			return doc;

        } catch (Exception e) {
			//e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid training" +
                    " data generation for full text.", e);
        }
    }

//    /**
//     * Return the Document object of the last processed pdf file.
//     */
//    public Document getDoc() {
//        return doc;
//    }

	/**
     * Return the Biblio object corresponding to the last processed pdf file.
     */
//    public BiblioItem getResHeader() {
//        return resHeader;
//    }

    /**
     * Extract results from a labelled full text in the training format without any string modification.
     *
     * @param result reult
     * @param tokenizations toks
     * @return extraction
     */
    private StringBuffer trainingExtraction(String result,
                                            List<LayoutToken> tokenizations) {
        // this is the main buffer for the whole full text
        StringBuffer buffer = new StringBuffer();
        try {
            StringTokenizer st = new StringTokenizer(result, "\n");
            String s1 = null;
            String s2 = null;
            String lastTag = null;
			//System.out.println(tokenizations.toString());
			//System.out.println(result);
            // current token position
            int p = 0;
            boolean start = true;
            boolean openFigure = false;
            boolean headFigure = false;
            boolean descFigure = false;
            boolean tableBlock = false;

            while (st.hasMoreTokens()) {
                boolean addSpace = false;
                String tok = st.nextToken().trim();

                if (tok.length() == 0) {
                    continue;
                }
                StringTokenizer stt = new StringTokenizer(tok, " \t");
                List<String> localFeatures = new ArrayList<String>();
                int i = 0;

                boolean newLine = false;
                int ll = stt.countTokens();
                while (stt.hasMoreTokens()) {
                    String s = stt.nextToken().trim();
                    if (i == 0) {
                        s2 = TextUtilities.HTMLEncode(s); // lexical token
						int p0 = p;
                        boolean strop = false;
                        while ((!strop) && (p < tokenizations.size())) {
                            String tokOriginal = tokenizations.get(p).t();
                            if (tokOriginal.equals(" ")
							 || tokOriginal.equals("\u00A0")) {
                                addSpace = true;
                            }
							else if (tokOriginal.equals("\n")) {
								newLine = true;
							}
							else if (tokOriginal.equals(s)) {
                                strop = true;
                            }
                            p++;
                        }
						if (p == tokenizations.size()) {
							// either we are at the end of the header, or we might have
							// a problematic token in tokenization for some reasons
							if ((p - p0) > 2) {
								// we loose the synchronicity, so we reinit p for the next token
								p = p0;
							}
						}
                    } else if (i == ll - 1) {
                        s1 = s; // current tag
                    } else {
                        if (s.equals("LINESTART"))
                            newLine = true;
                        localFeatures.add(s);
                    }
                    i++;
                }

                if (newLine && !start) {
                    buffer.append("<lb/>");
                }

                String lastTag0 = null;
                if (lastTag != null) {
                    if (lastTag.startsWith("I-")) {
                        lastTag0 = lastTag.substring(2, lastTag.length());
                    } else {
                        lastTag0 = lastTag;
                    }
                }
                String currentTag0 = null;
                if (s1 != null) {
                    if (s1.startsWith("I-")) {
                        currentTag0 = s1.substring(2, s1.length());
                    } else {
                        currentTag0 = s1;
                    }
                }

                boolean closeParagraph = false;
                if (lastTag != null) {
                    closeParagraph = testClosingTag(buffer, currentTag0, lastTag0, s1);
                }

                boolean output;

                if (!currentTag0.equals("<table>") &&
                        !currentTag0.equals("<trash>") &&
                        !currentTag0.equals("<figure_head>") &&
                        !currentTag0.equals("<figDesc>")) {
                    if (openFigure) {
                        buffer.append("\n\t\t\t</figure>\n\n");
                    }
                    openFigure = false;
                    headFigure = false;
                    descFigure = false;
                    tableBlock = false;
                }

                //output = writeField(buffer, s1, lastTag0, s2, "<header>", "<front>", addSpace, 3);
                //if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<other>",
						"<note type=\"other\">", addSpace, 3, false);
                //}
                // for paragraph we must distinguish starting and closing tags
                if (!output) {
                    if (closeParagraph) {
                        output = writeFieldBeginEnd(buffer, s1, "", s2, "<paragraph>",
							"<p>", addSpace, 3, false);
                    } else {
                        output = writeFieldBeginEnd(buffer, s1, lastTag, s2, "<paragraph>",
							"<p>", addSpace, 3, false);
                    }
                }
                /*if (!output) {
                    if (closeParagraph) {
                        output = writeField(buffer, s1, "", s2, "<reference_marker>", "<label>", addSpace, 3);
                    } else
                        output = writeField(buffer, s1, lastTag0, s2, "<reference_marker>", "<label>", addSpace, 3);
                }*/
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<citation_marker>", "<ref type=\"biblio\">",
                            addSpace, 3, false);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<table_marker>", "<ref type=\"table\">",
                            addSpace, 3, false);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<section>",
						"<head>", addSpace, 3, false);
                }
                /*if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<subsection>", 
						"<head>", addSpace, 3, false);
                }*/
                if (!output) {
                    if (openFigure) {
                        output = writeField(buffer, s1, lastTag0, s2, "<trash>", "<trash>", addSpace, 4, false);
                    } else {
                        //output = writeField(buffer, s1, lastTag0, s2, "<trash>", "<figure>\n\t\t\t\t<trash>",
                        output = writeField(buffer, s1, lastTag0, s2, "<trash>", "<trash>",
                                addSpace, 3, false);
                        if (output) {
                            openFigure = true;
                        }
                    }
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<equation>",
						"<formula>", addSpace, 3, false);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<figure_marker>",
						"<ref type=\"figure\">", addSpace, 3, false);
                }
                /*if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<label>", 
						"<label>", addSpace, 4, false);
                }*/
                if (!output) {
                    if (openFigure) {
                        if (tableBlock && (!lastTag0.equals("<table>")) && (currentTag0.equals("<table>"))) {
                            buffer.append("\n\t\t\t</figure>\n\n");
                            output = writeField(buffer, s1, lastTag0, s2, "<figure>\n\t\t\t\t<table>", "<figure>",
                                    addSpace, 3, false);
                            if (output) {
                                tableBlock = true;
                                descFigure = false;
                                headFigure = false;
                            }
                        } else {
                            output = writeField(buffer, s1, lastTag0, s2, "<table>",
								"<table>", addSpace, 4, false);
                            if (output) {
                                tableBlock = true;
                            }
                        }
                    } else {
                        output = writeField(buffer, s1, lastTag0, s2, "<table>",
							"<figure>\n\t\t\t\t<table>", addSpace, 3, false);
                        if (output) {
                            openFigure = true;
                            tableBlock = true;
                        }
                    }
                }
                if (!output) {
                    if (openFigure) {
                        if (descFigure && (!lastTag0.equals("<figDesc>")) && (currentTag0.equals("<figDesc>"))) {
                            buffer.append("\n\t\t\t</figure>\n\n");
                            output = writeField(buffer, s1, lastTag0, s2, "<figDesc>", "<figure>\n\t\t\t\t<figDesc>",
                                    addSpace, 3, false);
                            if (output) {
                                descFigure = true;
                                tableBlock = false;
                                headFigure = false;
                            }
                        } else {
                            output = writeField(buffer, s1, lastTag0, s2, "<figDesc>",
								"<figDesc>", addSpace, 4, false);
                            if (output) {
                                descFigure = true;
                            }
                        }
                    } else {
                        output = writeField(buffer, s1, lastTag0, s2, "<figDesc>",
							"<figure>\n\t\t\t\t<figDesc>", addSpace, 3, false);
                        if (output) {
                            openFigure = true;
                            descFigure = true;
                        }
                    }
                }
                if (!output) {
                    if (openFigure) {
                        if (headFigure && (!lastTag0.equals("<figure_head>")) &&
                                (currentTag0.equals("<figure_head>"))) {
                            buffer.append("\n\t\t\t</figure>\n\n");
                            output = writeField(buffer, s1, lastTag0, s2, "<figure_head>",
								"<figure>\n\t\t\t\t<head>", addSpace, 3, false);
                            if (output) {
                                descFigure = false;
                                tableBlock = false;
                                headFigure = true;
                            }
                        } else {
                            output = writeField(buffer, s1, lastTag0, s2, "<figure_head>",
								"<head>", addSpace, 4, false);
                            if (output) {
                                headFigure = true;
                            }
                        }
                    } else {
                        output = writeField(buffer, s1, lastTag0, s2, "<figure_head>", "<figure>\n\t\t\t\t<head>",
                                addSpace, 3, false);
                        if (output) {
                            openFigure = true;
                            headFigure = true;
                        }
                    }
                }
                // for item we must distinguish starting and closing tags
                if (!output) {
                    output = writeFieldBeginEnd(buffer, s1, lastTag, s2, "<item>",
						"<item>", addSpace, 3, false);
                }

                lastTag = s1;

                if (!st.hasMoreTokens()) {
                    if (lastTag != null) {
                        testClosingTag(buffer, "", currentTag0, s1);
                    }
                    if (openFigure) {
                        buffer.append("\n\t\t\t</figure>\n\n");
                    }
                }
                if (start) {
                    start = false;
                }
            }

            return buffer;
        } catch (Exception e) {
			e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    /**
     * TODO some documentation...
     *
     * @param buffer buffer
     * @param s1
     * @param lastTag0
     * @param s2
     * @param field
     * @param outField
     * @param addSpace
     * @param nbIndent
     * @return
     */
    public static boolean writeField(StringBuffer buffer,
                               String s1,
                               String lastTag0,
                               String s2,
                               String field,
                               String outField,
                               boolean addSpace,
                               int nbIndent,
					 	  	   boolean generateIDs) {
        boolean result = false;
		if (s1 == null) {
			return result;
		}
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
			String divID = null;
			if (generateIDs) {
				divID = KeyGen.getKey().substring(0,7);
				if (outField.charAt(outField.length()-2) == '>')
					outField = outField.substring(0, outField.length()-2) + " xml:id=\"_"+ divID + "\">";
			}
            if (s1.equals(lastTag0) || s1.equals("I-" + lastTag0)) {
                if (addSpace)
                    buffer.append(" ").append(s2);
                else
                    buffer.append(s2);
            }
            /*else if (lastTag0 == null) {
                   for(int i=0; i<nbIndent; i++) {
                       buffer.append("\t");
                   }
                     buffer.append(outField+s2);
               }*/
            else if (field.equals("<citation_marker>")) {
                if (addSpace)
                    buffer.append(" ").append(outField).append(s2);
                else
                    buffer.append(outField).append(s2);
            } else if (field.equals("<figure_marker>")) {
                if (addSpace)
                    buffer.append(" ").append(outField).append(s2);
                else
                    buffer.append(outField).append(s2);
            } else if (field.equals("<table_marker>")) {
                if (addSpace)
                    buffer.append(" ").append(outField).append(s2);
                else
                    buffer.append(outField).append(s2);
            } /*else if (field.equals("<label>")) {
                if (addSpace)
                    buffer.append(" ").append(outField).append(s2);
                else
                    buffer.append(outField).append(s2);
            } else if (field.equals("<reference_marker>")) {
                if (!lastTag0.equals("<reference>") && !lastTag0.equals("<reference_marker>")) {
                    for (int i = 0; i < nbIndent; i++) {
                        buffer.append("\t");
                    }
                    buffer.append("<bibl>");
                }
                if (addSpace)
                    buffer.append(" ").append(outField).append(s2);
                else
                    buffer.append(outField).append(s2);
            }*/ else if (lastTag0 == null) {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField).append(s2);
            } else if (!lastTag0.equals("<citation_marker>") && !lastTag0.equals("<figure_marker>")
                    && !lastTag0.equals("<figure>")) {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField).append(s2);
            } else {
                if (addSpace)
                    buffer.append(" ").append(s2);
                else
                    buffer.append(s2);
            }
        }
        return result;
    }

    /**
     * This is for writing fields for fields where begin and end of field matter, like paragraph or item
     *
     * @param buffer
     * @param s1
     * @param lastTag0
     * @param s2
     * @param field
     * @param outField
     * @param addSpace
     * @param nbIndent
     * @return
     */
    public static boolean writeFieldBeginEnd(StringBuffer buffer,
                                       String s1,
                                       String lastTag0,
                                       String s2,
                                       String field,
                                       String outField,
                                       boolean addSpace,
                                       int nbIndent,
									   boolean generateIDs) {
        boolean result = false;
		if (s1 == null) {
			return false;
		}
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
			if (lastTag0 == null) {
				lastTag0 = "";
			}
			String divID;
			if (generateIDs) {
				divID = KeyGen.getKey().substring(0,7);
				if (outField.charAt(outField.length()-2) == '>')
					outField = outField.substring(0, outField.length()-2) + " xml:id=\"_"+ divID + "\">";
			}
            if (lastTag0.equals("I-" + field)) {
                if (addSpace)
                    buffer.append(" ").append(s2);
                else
                    buffer.append(s2);
            } else if (lastTag0.equals(field) && s1.equals(field)) {
                if (addSpace)
                    buffer.append(" ").append(s2);
                else
                    buffer.append(s2);
            } else if (!lastTag0.equals("<citation_marker>") && !lastTag0.equals("<figure_marker>")
                    && !lastTag0.equals("<figure>") && !lastTag0.equals("<reference_marker>")) {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField).append(s2);
            } else {
                if (addSpace)
                    buffer.append(" ").append(s2);
                else
                    buffer.append(s2);
            }
        }
        return result;
    }

    /**
     * TODO some documentation
     *
     * @param buffer
     * @param currentTag0
     * @param lastTag0
     * @param currentTag
     * @return
     */
    private static boolean testClosingTag(StringBuffer buffer,
                                   String currentTag0,
                                   String lastTag0,
                                   String currentTag) {
        boolean res = false;
        // reference_marker and citation_marker are two exceptions because they can be embedded

        if (!currentTag0.equals(lastTag0) || currentTag.equals("I-<paragraph>") || currentTag.equals("I-<item>")) {
            if (currentTag0.equals("<citation_marker>") || currentTag0.equals("<figure_marker>")) {
                return res;
            }

            res = false;
            // we close the current tag
            if (lastTag0.equals("<other>")) {
                buffer.append("</note>\n\n");

            } else if (lastTag0.equals("<paragraph>")) {
                buffer.append("</p>\n\n");
                res = true;

            } else if (lastTag0.equals("<section>")) {
                buffer.append("</head>\n\n");

            } else if (lastTag0.equals("<subsection>")) {
                buffer.append("</head>\n\n");

            } else if (lastTag0.equals("<equation>")) {
                buffer.append("</formula>\n\n");

            } else if (lastTag0.equals("<table>")) {
                buffer.append("</table>\n");

            } else if (lastTag0.equals("<figDesc>")) {
                buffer.append("</figDesc>\n");

            } else if (lastTag0.equals("<figure_head>")) {
                buffer.append("</head>\n\n");

            } else if (lastTag0.equals("<item>")) {
                buffer.append("</item>\n\n");

            } /*else if (lastTag0.equals("<label>")) {
                buffer.append("</label>\n\n");

            } */
			else if (lastTag0.equals("<trash>")) {
                buffer.append("</trash>\n\n");

                /*case "<reference_marker>":
                    buffer.append("</label>");
                    break;*/
            } else if (lastTag0.equals("<citation_marker>")) {
                buffer.append("</ref>");

            } else if (lastTag0.equals("<figure_marker>")) {
                buffer.append("</ref>");

                /*case "<page>":
                    buffer.append("</page>\n\n");
                    break;*/
            } else {
                res = false;

            }

        }
        return res;
    }

    /**
     * Create the TEI representation for a document based on the parsed header, references
     * and body sections.
     */
    private void toTEI(Document doc,
                       String reseBody,
                       String reseAnnex,
					   LayoutTokenization layoutTokenization,
                       List<LayoutToken> tokenizationsAnnex,
                       BiblioItem resHeader,
                       List<BibDataSet> resCitations,
                       GrobidAnalysisConfig config) {
        if (doc.getBlocks() == null) {
            return;
        }
        TEIFormater teiFormater = new TEIFormater(doc);
        StringBuffer tei;
        try {
            tei = teiFormater.toTEIHeader(resHeader, null, config);

			//System.out.println(rese);
            int mode = config.getFulltextProcessingMode();
            if (config.getFulltextProcessingMode() == 0) {
				tei = teiFormater.toTEIBodyLight(tei, reseBody, resHeader, resCitations,
					layoutTokenization, doc, config);
			}
			else if (mode == 1) {
           		tei = teiFormater.toTEIBodyML(tei, reseBody, resHeader, resCitations,
					layoutTokenization.getTokenization(), doc);
			}

			tei.append("\t\t<back>\n");
			if (mode == 0) {
				// acknowledgement is in the back
				SortedSet<DocumentPiece> documentAcknowledgementParts =
					doc.getDocumentPart(SegmentationLabel.ACKNOWLEDGEMENT);
				Pair<String, LayoutTokenization> featSeg =
					getBodyTextFeatured(doc, documentAcknowledgementParts);
				List<LayoutToken> tokenizationsAcknowledgement = null;
				if (featSeg != null) {
					// if featSeg is null, it usually means that no body segment is found in the
					// document segmentation
					String acknowledgementText = featSeg.getA();
					tokenizationsAcknowledgement = featSeg.getB().getTokenization();
					String reseAcknowledgement = null;
					if ( (acknowledgementText != null) && (acknowledgementText.length() >0) )
						reseAcknowledgement = label(acknowledgementText);
					tei = teiFormater.toTEIAcknowledgementLight(tei, reseAcknowledgement,
						tokenizationsAcknowledgement, resCitations, config);
				}

				tei = teiFormater.toTEIAnnexLight(tei, reseAnnex, resHeader, resCitations,
					tokenizationsAnnex, doc, config);
			}
			else if (mode == 1) {
				tei = teiFormater.toTEIAnnexML(tei, reseAnnex, resHeader, resCitations,
					tokenizationsAnnex, doc);
			}
			tei = teiFormater.toTEIReferences(tei, resCitations, config);
            doc.calculateTeiIdToBibDataSets();

            tei.append("\t\t</back>\n");

            tei.append("\t</text>\n");
            tei.append("</TEI>\n");
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
//System.out.println(tei.toString());		
        doc.setTei(tei.toString());
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}