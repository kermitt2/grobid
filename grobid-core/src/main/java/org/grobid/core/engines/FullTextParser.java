package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Figure;
import org.grobid.core.data.Table;
import org.grobid.core.data.Equation;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.document.TEIFormatter;
import org.grobid.core.engines.citations.LabeledReferenceResult;
import org.grobid.core.engines.citations.ReferenceSegmenter;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.counters.CitationParserCounters;
import org.grobid.core.engines.label.SegmentationLabels;
import org.grobid.core.engines.label.TaggingLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorFulltext;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.GraphicObjectType;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Consolidation;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.TextUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;

import static org.apache.commons.lang3.StringUtils.*;

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
	private static final int NBBINS_POSITION = 12;

	// default bins for inter-block spacing
	private static final int NBBINS_SPACE = 5;

	// default bins for block character density
	private static final int NBBINS_DENSITY = 5;

	// projection scale for line length
	private static final int LINESCALE = 10;

    private EngineParsers parsers;

    /**
     * TODO some documentation...
     */
    public FullTextParser(EngineParsers parsers) {
        super(GrobidModels.FULLTEXT);
        this.parsers = parsers;
        tmpPath = GrobidProperties.getTempPath();
    }

	public Document processing(File inputPdf,
							   GrobidAnalysisConfig config) throws Exception {
		DocumentSource documentSource = 
			DocumentSource.fromPdf(inputPdf, config.getStartPage(), config.getEndPage(), 
				config.getPdfAssetPath() != null, true);
		return processing(documentSource, config);
	}

	/**
     * Machine-learning recognition of the complete full text structures.
     *
     * @param documentSource input
     * @param config config
     * @return the document object with built TEI
     */
    public Document processing(DocumentSource documentSource,
                               GrobidAnalysisConfig config) throws Exception {
        if (tmpPath == null) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        }
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                    tmpPath.getAbsolutePath() + "' does not exists.");
        }
        try {
			// general segmentation
			Document doc = parsers.getSegmentationParser().processing(documentSource, config);
			SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabels.BODY);

			// full text processing
			Pair<String, LayoutTokenization> featSeg = getBodyTextFeatured(doc, documentBodyParts);
			String rese = null;
			LayoutTokenization layoutTokenization = null;
			List<Figure> figures = null;
			List<Table> tables = null;
			List<Equation> equations = null;
			if (featSeg != null) {
				// if featSeg is null, it usually means that no body segment is found in the
				// document segmentation
				String bodytext = featSeg.getA();
//System.out.println(bodytext);
				layoutTokenization = featSeg.getB();
				//tokenizationsBody = featSeg.getB().getTokenization();
                //layoutTokensBody = featSeg.getB().getLayoutTokens();
				if ( (bodytext != null) && (bodytext.trim().length() > 0) ) {				
					rese = label(bodytext);
				} else {
					LOGGER.debug("Fulltext model: The input to the CRF processing is empty");
				}
				//LOGGER.info(rese);
				// we apply now the figure and table models based on the fulltext labeled output
				figures = processFigures(rese, layoutTokenization.getTokenization(), doc);
				tables = processTables(rese, layoutTokenization.getTokenization(), doc);
				equations = processEquations(rese, layoutTokenization.getTokenization(), doc);
			} else {
				LOGGER.debug("Fulltext model: The featured body is empty");
			}

            // header processing
			BiblioItem resHeader = new BiblioItem();
           	parsers.getHeaderParser().processingHeaderBlock(config.isConsolidateHeader(), doc, resHeader);
           	// above the old version of the header block identification, because more robust
           	if ( (resHeader == null) ||
           		 (resHeader.getTitle() == null) || (resHeader.getTitle().trim().length() == 0) ||
           		 (resHeader.getAuthors() == null) || (resHeader.getFullAuthors() == null) ||
				 (resHeader.getFullAuthors().size() == 0) ) {
           		resHeader = new BiblioItem();
				parsers.getHeaderParser().processingHeaderSection(config.isConsolidateHeader(), doc, resHeader);
				// above, use the segmentation model result
			}

            // citation processing
            // consolidation, if selected, is not done individually for each citation but 
            // in a second stage for all citations
            List<BibDataSet> resCitations = parsers.getCitationParser().
				processingReferenceSection(doc, parsers.getReferenceSegmenterParser(), false);

			// consolidate the set
			if (config.isConsolidateCitations()) {
				Consolidation consolidator = new Consolidation(cntManager);
				try {
					Map<Integer,BiblioItem> resConsolidation = consolidator.consolidate(resCitations);
					for(int i=0; i<resCitations.size(); i++) {
						BiblioItem resCitation = resCitations.get(i).getResBib();
						BiblioItem bibo = resConsolidation.get(new Integer(i));
						if (bibo != null) {
			                BiblioItem.correct(resCitation, bibo);
						}
					}
				} catch(Exception e) {
					throw new GrobidException(
                    "An exception occured while running consolidation on bibliographical references.", e);
				} finally {
					//consolidator.close();
				}
			}
            doc.setBibDataSets(resCitations);

            if (resCitations != null) {
                for (BibDataSet bds : resCitations) {
                    String marker = bds.getRefSymbol();
                    if (marker != null) {
                        marker = marker.replace(".", "");
                        marker = marker.replace(" ", "");
                        bds.setRefSymbol(marker);
                    }
                }
            }

			// possible annexes (view as a piece of full text similar to the body)
			documentBodyParts = doc.getDocumentPart(SegmentationLabels.ANNEX);
            featSeg = getBodyTextFeatured(doc, documentBodyParts);
			String rese2 = null;
			List<LayoutToken> tokenizationsBody2 = null;
			if (featSeg != null) {
				// if featSeg is null, it usually means that no body segment is found in the
				// document segmentation
				String bodytext = featSeg.getA();
				tokenizationsBody2 = featSeg.getB().getTokenization();
				if (isNotEmpty(trim(bodytext))) 
	            	rese2 = label(bodytext);
				//System.out.println(rese);
			}

            // final combination
            toTEI(doc, // document
				rese, rese2, // labeled data for body and annex
				layoutTokenization, tokenizationsBody2, // tokenization for body and annex
				resHeader, resCitations, // header and bibliographical citations
				figures, tables, equations, 
				config);
            return doc;
        } catch (GrobidException e) {
			throw e;
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

		boolean endblock;
        boolean endPage = true;
        boolean newPage = true;
        //boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        double lineStartX = Double.NaN;
		boolean indented = false;
        int fulltextLength = 0;
        int pageLength = 0; // length of the current page
		double lowestPos = 0.0;
		double spacingPreviousBlock = 0.0;
		int currentPage = 0;

		List<LayoutToken> layoutTokens = new ArrayList<LayoutToken>();
		fulltextLength = getFulltextLength(doc, documentBodyParts, fulltextLength);

		// System.out.println("fulltextLength: " + fulltextLength);

		for(DocumentPiece docPiece : documentBodyParts) {
			DocumentPointer dp1 = docPiece.a;
			DocumentPointer dp2 = docPiece.b;

			//int blockPos = dp1.getBlockPtr();
			for(int blockIndex = dp1.getBlockPtr(); blockIndex <= dp2.getBlockPtr(); blockIndex++) {
				boolean graphicVector = false;
	    		boolean graphicBitmap = false;
            	Block block = blocks.get(blockIndex);
            	// length of the page where the current block is
            	double pageHeight = block.getPage().getHeight();
				int localPage = block.getPage().getNumber();
				if (localPage != currentPage) {
					newPage = true;
					currentPage = localPage;
	                mm = 0;
					lowestPos = 0.0;
					spacingPreviousBlock = 0.0;
				} 

	            /*if (start) {
	                newPage = true;
	                start = false;
	            }*/

	            boolean newline;
	            boolean previousNewline = false;
	            endblock = false;

	            /*if (endPage) {
	                newPage = true;
	                mm = 0;
					lowestPos = 0.0;
	            }*/

                if (lowestPos >  block.getY()) {
                    // we have a vertical shift, which can be due to a change of column or other particular layout formatting 
                    spacingPreviousBlock = doc.getMaxBlockSpacing() / 5.0; // default
                }
                else
                    spacingPreviousBlock = block.getY() - lowestPos;

	            String localText = block.getText();
                if (TextUtilities.filterLine(localText)) {
                    continue;
                }
	            /*if (localText != null) {
	                if (localText.contains("@PAGE")) {
	                    mm = 0;
	                    // pageLength = 0;
	                    endPage = true;
	                    newPage = false;
	                } else {
	                    endPage = false;
	                }
	            }*/

                // character density of the block
                double density = 0.0;
                if ( (block.getHeight() != 0.0) && (block.getWidth() != 0.0) &&
                     (localText != null) && (!localText.contains("@PAGE")) &&
                     (!localText.contains("@IMAGE")) )
                    density = (double)localText.length() / (block.getHeight() * block.getWidth());

                // check if we have a graphical object connected to the current block
                List<GraphicObject> localImages = Document.getConnectedGraphics(block, doc);
                if (localImages != null) {
                	for(GraphicObject localImage : localImages) {
                		if (localImage.getType() == GraphicObjectType.BITMAP)
                			graphicVector = true;
                		if (localImage.getType() == GraphicObjectType.VECTOR)
                			graphicBitmap = true;
                	}
                }

	            List<LayoutToken> tokens = block.getTokens();
	            if (tokens == null) {
	                continue;
	            }

				int n = 0;// token position in current block
				if (blockIndex == dp1.getBlockPtr()) {
//					n = dp1.getTokenDocPos() - block.getStartToken();
					n = dp1.getTokenBlockPos();
				}
				int lastPos = tokens.size();
				// if it's a last block from a document piece, it may end earlier
				if (blockIndex == dp2.getBlockPtr()) {
					lastPos = dp2.getTokenBlockPos();
					if (lastPos >= tokens.size()) {
						LOGGER.error("DocumentPointer for block " + blockIndex + " points to " +
							dp2.getTokenBlockPos() + " token, but block token size is " +
							tokens.size());
						lastPos = tokens.size();
					}
				}

	            while (n < lastPos) {
					if (blockIndex == dp2.getBlockPtr()) {
						//if (n > block.getEndToken()) {
						if (n > dp2.getTokenDocPos() - block.getStartToken()) {
							break;
						}
					}

					LayoutToken token = tokens.get(n);
					layoutTokens.add(token);

					features = new FeaturesVectorFulltext();
	                features.token = token;

	                double coordinateLineY = token.getY();

	                String text = token.getText();
	                if ( (text == null) || (text.length() == 0)) {
	                    n++;
	                    //mm++;
	                    //nn++;
	                    continue;
	                }
	                //text = text.replaceAll("\\s+", "");
	                text = text.replace(" ", "");
	                if (text.length() == 0) {
	                    n++;
	                    mm++;
	                    nn++;
	                    continue;
	                }

	                //if (text.equals("\n") || text.equals("\r") ) {
	                if (text.equals("\n")) {
	                    newline = true;
	                    previousNewline = true;
	                    n++;
	                    mm++;
	                    nn++;
	                    continue;
	                } else
	                    newline = false;

	                // final sanitisation and filtering
	                text = text.replaceAll("[ \n]", "");
	                if (TextUtilities.filterLine(text)) {
						n++;
	                    continue;
	                }

	                if (previousNewline) {
	                    newline = true;
	                    previousNewline = false;
						if ((token != null) && (previousFeatures != null)) {
							double previousLineStartX = lineStartX;
	                        lineStartX = token.getX();
	                        double characterWidth = token.width / text.length();
							if (!Double.isNaN(previousLineStartX)) {
								if (previousLineStartX - lineStartX > characterWidth)
	                                indented = false;
	                            else if (lineStartX - previousLineStartX > characterWidth)
	        					    indented = true;
	        					// Indentation ends if line start is > 1 character width to the left of previous line start
	        					// Indentation starts if line start is > 1 character width to the right of previous line start
	                            // Otherwise indentation is unchanged
							}
						}
	                }
//System.out.println(text + "\t" + token.getX() + "\t" + lineStartX + "\t" + indented);
	                features.string = text;

	                if (graphicBitmap) {
	                	features.bitmapAround = true;
	                }
	                if (graphicVector) {
	                	features.vectorAround = true;
	                }

	                if (newline) {
	                    features.lineStatus = "LINESTART";
	                    if (token != null)
		                    lineStartX = token.getX();
		                // be sure that previous token is closing a line, except if it's a starting line
	                    if (previousFeatures != null) {
	                    	if (!previousFeatures.lineStatus.equals("LINESTART"))
		                    	previousFeatures.lineStatus = "LINEEND";
	                    }
	                }
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

                    if (indented) {
	                	features.alignmentStatus = "LINEINDENT";
	                }
	                else {
	                	features.alignmentStatus = "ALIGNEDLEFT";
	                }

	                if (n == 0) {
	                    features.lineStatus = "LINESTART";
	                    // be sure that previous token is closing a line, except if it's a starting line
	                    if (previousFeatures != null) {
	                    	if (!previousFeatures.lineStatus.equals("LINESTART"))
		                    	previousFeatures.lineStatus = "LINEEND";
	                    }
	                    if (token != null)
		                    lineStartX = token.getX();
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
												&& (!(toto.startsWith("@PAGE")))
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
	                        //endblock = true;
	                    }
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

	                Matcher m = featureFactory.isDigit.matcher(text);
	                if (m.find()) {
	                    features.digit = "ALLDIGIT";
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

	                if (features.capitalisation == null)
	                    features.capitalisation = "NOCAPS";

	                if (features.digit == null)
	                    features.digit = "NODIGIT";

	                if (features.punctType == null)
	                    features.punctType = "NOPUNCT";

	                features.relativeDocumentPosition = featureFactory
	                        .linearScaling(nn, fulltextLength, NBBINS_POSITION);
	                // System.out.println(mm + " / " + pageLength);
	                features.relativePagePositionChar = featureFactory
	                        .linearScaling(mm, pageLength, NBBINS_POSITION);

	                int pagePos = featureFactory
                        .linearScaling(coordinateLineY, pageHeight, NBBINS_POSITION);
					if (pagePos > NBBINS_POSITION)
						pagePos = NBBINS_POSITION;
	                features.relativePagePosition = pagePos;
//System.out.println((coordinateLineY) + " " + (pageHeight) + " " + NBBINS_POSITION + " " + pagePos); 

                    if (spacingPreviousBlock != 0.0) {
                        features.spacingWithPreviousBlock = featureFactory
                            .linearScaling(spacingPreviousBlock - doc.getMinBlockSpacing(),
                                    doc.getMaxBlockSpacing() - doc.getMinBlockSpacing(), NBBINS_SPACE);
                    }

                    if (density != -1.0) {
                        features.characterDensity = featureFactory
                            .linearScaling(density - doc.getMinCharacterDensity(), doc.getMaxCharacterDensity() - doc.getMinCharacterDensity(), NBBINS_DENSITY);
//System.out.println((density-doc.getMinCharacterDensity()) + " " + (doc.getMaxCharacterDensity()-doc.getMinCharacterDensity()) + " " + NBBINS_DENSITY + " " + features.characterDensity);
                    }

	                // fulltext.append(features.printVector());
	                if (previousFeatures != null) {
						if (features.blockStatus.equals("BLOCKSTART") &&
							previousFeatures.blockStatus.equals("BLOCKIN")) {
							// this is a post-correction due to the fact that the last character of a block
							// can be a space or EOL character
							previousFeatures.blockStatus = "BLOCKEND";
							previousFeatures.lineStatus = "LINEEND";
						}
                        fulltext.append(previousFeatures.printVector());
                    }
	                n++;
	                mm += text.length();
	                nn += text.length();
	                previousFeatures = features;
            	}
                // lowest position of the block
                lowestPos = block.getY() + block.getHeight();

            	//blockPos++;
			}
        }
        if (previousFeatures != null) {
            fulltext.append(previousFeatures.printVector());

        }

        return new Pair<>(fulltext.toString(),
            new LayoutTokenization(layoutTokens));
	}

	/**
	 * Evaluate the length of the fulltext
	 */
	private static int getFulltextLength(Document doc, SortedSet<DocumentPiece> documentBodyParts, int fulltextLength) {
		for(DocumentPiece docPiece : documentBodyParts) {
			DocumentPointer dp1 = docPiece.a;
			DocumentPointer dp2 = docPiece.b;

            int tokenStart = dp1.getTokenDocPos();
            int tokenEnd = dp2.getTokenDocPos();
            for (int i = tokenStart; i <= tokenEnd; i++) {
                //tokenizationsBody.add(tokenizations.get(i));
				fulltextLength += doc.getTokenizations().get(i).getText().length();
            }
		}
		return fulltextLength;
	}

    /**
     * Process the specified pdf and format the result as training data for all the models.
     *
     * @param inputFile input file
     * @param pathFullText path to fulltext
     * @param pathTEI path to TEI
     * @param id id
     */
    public Document createTraining(File inputFile,
                                   String pathFullText,
                                   String pathTEI,
                                   int id) {
        if (tmpPath == null)
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                    tmpPath.getAbsolutePath() + "' does not exists.");
        }
        DocumentSource documentSource = null;
        try {
            if (!inputFile.exists()) {
               	throw new GrobidResourceException("Cannot train for fulltext, becuase file '" +
                       inputFile.getAbsolutePath() + "' does not exists.");
           	}
           	String pdfFileName = inputFile.getName();

           	// SEGMENTATION MODEL
            documentSource = DocumentSource.fromPdf(inputFile, -1, -1, true, true);
            Document doc = new Document(documentSource);
            doc.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());

            if (doc.getBlocks() == null) {
                throw new Exception("PDF parsing resulted in empty content");
            }
            doc.produceStatistics();

            String fulltext = //getAllTextFeatured(doc, false);
                    parsers.getSegmentationParser().getAllLinesFeatured(doc);
            List<LayoutToken> tokenizations = doc.getTokenizationsFulltext();

            // we write first the full text untagged (but featurized with segmentation features)
            String outPathFulltext = pathFullText + File.separator + 
				pdfFileName.replace(".pdf", ".training.segmentation");
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");
            writer.write(fulltext + "\n");
            writer.close();

			// also write the raw text as seen before segmentation
			StringBuffer rawtxt = new StringBuffer();
			for(LayoutToken txtline : tokenizations) {
				rawtxt.append(txtline.getText());
			}
			String outPathRawtext = pathFullText + File.separator +
				pdfFileName.replace(".pdf", ".training.segmentation.rawtxt");
			FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");

            if (isNotBlank(fulltext)) {
                String rese = parsers.getSegmentationParser().label(fulltext);
                StringBuffer bufferFulltext = parsers.getSegmentationParser().trainingExtraction(rese, tokenizations, doc);

                // write the TEI file to reflect the extact layout of the text as extracted from the pdf
                writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
                        File.separator + 
						pdfFileName.replace(".pdf", ".training.segmentation.tei.xml")), false), "UTF-8");
                writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
                        "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">\n");

                writer.write(bufferFulltext.toString());
                writer.write("\n\t</text>\n</tei>\n");
                writer.close();
            }

            // FULLTEXT MODEL (body)
            doc = parsers.getSegmentationParser().processing(documentSource, 
				GrobidAnalysisConfig.defaultInstance());

			SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabels.BODY);
			if (documentBodyParts != null) {
				Pair<String, LayoutTokenization> featSeg = getBodyTextFeatured(doc, documentBodyParts);
				if (featSeg == null) {
					// no textual body part found, nothing to generate
					return doc;
				}

				String bodytext = featSeg.getA();
				List<LayoutToken> tokenizationsBody = featSeg.getB().getTokenization();

	            // we write the full text untagged
	            outPathFulltext = pathFullText + File.separator
					+ pdfFileName.replace(".pdf", ".training.fulltext");
	            writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");
	            writer.write(bodytext + "\n");
	            writer.close();

//              StringTokenizer st = new StringTokenizer(fulltext, "\n");
	            String rese = label(bodytext);
				//System.out.println(rese);
	            StringBuilder bufferFulltext = trainingExtraction(rese, tokenizationsBody);

	            // write the TEI file to reflect the extract layout of the text as extracted from the pdf
	            writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                    File.separator +
						pdfFileName.replace(".pdf", ".training.fulltext.tei.xml")), false), "UTF-8");
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

	            // training data for FIGURES
	            Pair<String,String> trainingFigure = processTrainingDataFigures(rese, tokenizationsBody, inputFile.getName());
	            if (trainingFigure.getA().trim().length() > 0) {
		            String outPathFigures = pathFullText + File.separator
						+ pdfFileName.replace(".pdf", ".training.figure");
					writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFigures), false), "UTF-8");
		            writer.write(trainingFigure.getB() + "\n\n");
		            writer.close();

					String outPathFiguresTEI = pathTEI + File.separator
						+ pdfFileName.replace(".pdf", ".training.figure.tei.xml");
					writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFiguresTEI), false), "UTF-8");
		            writer.write(trainingFigure.getA() + "\n");
		            writer.close();
		        }

	            // training data for TABLES
		        Pair<String,String> trainingTable = processTrainingDataTables(rese, tokenizationsBody, inputFile.getName());
	            if (trainingTable.getA().trim().length() > 0) {
		            String outPathTables = pathFullText + File.separator
						+ pdfFileName.replace(".pdf", ".training.table");
					writer = new OutputStreamWriter(new FileOutputStream(new File(outPathTables), false), "UTF-8");
		            writer.write(trainingTable.getB() + "\n\n");
		            writer.close();

					String outPathTablesTEI = pathTEI + File.separator
						+ pdfFileName.replace(".pdf", ".training.table.tei.xml");
					writer = new OutputStreamWriter(new FileOutputStream(new File(outPathTablesTEI), false), "UTF-8");
		            writer.write(trainingTable.getA() + "\n");
		            writer.close();
		        }

				// HEADER MODEL
		        SortedSet<DocumentPiece> documentHeaderParts = doc.getDocumentPart(SegmentationLabels.HEADER);
	            List<LayoutToken> tokenizationsFull = doc.getTokenizations();
	            if (documentHeaderParts != null) {
	                List<LayoutToken> headerTokenizations = new ArrayList<LayoutToken>();

	                for (DocumentPiece docPiece : documentHeaderParts) {
	                    DocumentPointer dp1 = docPiece.a;
	                    DocumentPointer dp2 = docPiece.b;

	                    int tokens = dp1.getTokenDocPos();
	                    int tokene = dp2.getTokenDocPos();
	                    for (int i = tokens; i < tokene; i++) {
	                        headerTokenizations.add(tokenizationsFull.get(i));
	                    }
	                }
	                String header = parsers.getHeaderParser().getSectionHeaderFeatured(doc, documentHeaderParts, true);
	                if ((header != null) && (header.trim().length() > 0)) {
	                    rese = parsers.getHeaderParser().label(header);
	                    //String header = doc.getHeaderFeatured(true, true);
	                    //List<LayoutToken> tokenizations = doc.getTokenizationsHeader();

	                    // we write the header untagged
	                    String outPathHeader = pathTEI + File.separator + pdfFileName.replace(".pdf", ".training.header");
	                    writer = new OutputStreamWriter(new FileOutputStream(new File(outPathHeader), false), "UTF-8");
	                    writer.write(header + "\n");
	                    writer.close();

	                    // buffer for the header block
	                    StringBuilder bufferHeader = parsers.getHeaderParser().trainingExtraction(rese, true, headerTokenizations);
	                    Language lang = LanguageUtilities.getInstance().runLanguageId(bufferHeader.toString());
	                    if (lang != null) {
	                        doc.setLanguage(lang.getLang());
	                    }

	                    // buffer for the affiliation+address block
	                    StringBuilder bufferAffiliation =
	                            parsers.getAffiliationAddressParser().trainingExtraction(rese, headerTokenizations);
	                    
	                    // buffer for the date block
	                    StringBuilder bufferDate = null;
	                    // we need to rebuild the found date string as it appears
	                    String input = "";
	                    int q = 0;
	                    StringTokenizer st = new StringTokenizer(rese, "\n");
	                    while (st.hasMoreTokens() && (q < headerTokenizations.size())) {
	                        String line = st.nextToken();
	                        String theTotalTok = headerTokenizations.get(q).getText();
	                        String theTok = headerTokenizations.get(q).getText();
	                        while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n") || theTok.equals("\r")) {
	                            q++;
	                            if ((q > 0) && (q < headerTokenizations.size())) {
	                                theTok = headerTokenizations.get(q).getText();
	                                theTotalTok += theTok;
	                            }
	                        }
	                        if (line.endsWith("<date>")) {
	                            input += theTotalTok;
	                        }
	                        q++;
	                    }
	                    if (input.trim().length() > 1) {
	                        List<String> inputs = new ArrayList<String>();
	                        inputs.add(input.trim());
	                        bufferDate = parsers.getDateParser().trainingExtraction(inputs);
	                    }

	                    // buffer for the name block
	                    StringBuilder bufferName = null;
	                    // we need to rebuild the found author string as it appears
	                    input = "";
	                    q = 0;
	                    st = new StringTokenizer(rese, "\n");
	                    while (st.hasMoreTokens() && (q < headerTokenizations.size())) {
	                        String line = st.nextToken();
	                        String theTotalTok = headerTokenizations.get(q).getText();
	                        String theTok = headerTokenizations.get(q).getText();
	                        while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n") || theTok.equals("\r")) {
	                            q++;
	                            if ((q > 0) && (q < headerTokenizations.size())) {
	                                theTok = headerTokenizations.get(q).getText();
	                                theTotalTok += theTok;
	                            }
	                        }
	                        if (line.endsWith("<author>")) {
	                            input += theTotalTok;
	                        }
	                        q++;
	                    }
	                    if (input.length() > 1) {
	                        /*List<String> inputs = new ArrayList<String>();
	                        inputs.add(input.trim());*/
	                        bufferName = parsers.getAuthorParser().trainingExtraction(input, true);
	                    }

	                    // buffer for the reference block
	                    StringBuilder bufferReference = null;
	                    // we need to rebuild the found citation string as it appears
	                    input = "";
	                    q = 0;
	                    st = new StringTokenizer(rese, "\n");
	                    while (st.hasMoreTokens() && (q < headerTokenizations.size())) {
	                        String line = st.nextToken();
	                        String theTotalTok = headerTokenizations.get(q).getText();
	                        String theTok = headerTokenizations.get(q).getText();
	                        while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n") || theTok.equals("\r")) {
	                            q++;
	                            if ((q > 0) && (q < headerTokenizations.size())) {
	                                theTok = headerTokenizations.get(q).getText();
	                                theTotalTok += theTok;
	                            }
	                        }
	                        if (line.endsWith("<reference>")) {
	                            input += theTotalTok;
	                        }
	                        q++;
	                    }
	                    if (input.length() > 1) {
	                        List<String> inputs = new ArrayList<String>();
	                        inputs.add(input.trim());
	                        bufferReference = parsers.getCitationParser().trainingExtraction(inputs);
	                    }

	                    // write the training TEI file for header which reflects the extract layout of the text as
	                    // extracted from the pdf
	                    writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + File.separator
	                            + pdfFileName.replace(".pdf", ".training.header.tei.xml")), false), "UTF-8");
	                    writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\""
	                            + pdfFileName.replace(".pdf", "")
	                            + "\"/>\n\t</teiHeader>\n\t<text");

	                    if (lang != null) {
	                        writer.write(" xml:lang=\"" + lang.getLang() + "\"");
	                    }
	                    writer.write(">\n\t\t<front>\n");

	                    writer.write(bufferHeader.toString());
	                    writer.write("\n\t\t</front>\n\t</text>\n</tei>\n");
	                    writer.close();

	                    // AFFILIATION-ADDRESS model
	                    if (bufferAffiliation != null) {
	                        if (bufferAffiliation.length() > 0) {
	                            Writer writerAffiliation = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                                    File.separator
	                                    + pdfFileName.replace(".pdf", ".training.header.affiliation.tei.xml")), false), "UTF-8");
	                            writerAffiliation.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	                            writerAffiliation.write("\n<tei xmlns=\"http://www.tei-c.org/ns/1.0\""
	                                    + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" " + "xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">");
	                            writerAffiliation.write("\n\t<teiHeader>\n\t\t<fileDesc>\n\t\t\t<sourceDesc>");
	                            writerAffiliation.write("\n\t\t\t\t<biblStruct>\n\t\t\t\t\t<analytic>\n\t\t\t\t\t\t<author>\n\n");

	                            writerAffiliation.write(bufferAffiliation.toString());

	                            writerAffiliation.write("\n\t\t\t\t\t\t</author>\n\t\t\t\t\t</analytic>");
	                            writerAffiliation.write("\n\t\t\t\t</biblStruct>\n\t\t\t</sourceDesc>\n\t\t</fileDesc>");
	                            writerAffiliation.write("\n\t</teiHeader>\n</tei>\n");
	                            writerAffiliation.close();
	                        }
	                    }

	                    // DATE MODEL (for dates in header)
	                    if (bufferDate != null) {
	                        if (bufferDate.length() > 0) {
	                            Writer writerDate = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                                    File.separator
	                                    + pdfFileName.replace(".pdf", ".training.header.date.xml")), false), "UTF-8");
	                            writerDate.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	                            writerDate.write("<dates>\n");

	                            writerDate.write(bufferDate.toString());

	                            writerDate.write("</dates>\n");
	                            writerDate.close();
	                        }
	                    }

	                    // HEADER AUTHOR NAME model
	                    if (bufferName != null) {
	                        if (bufferName.length() > 0) {
	                            Writer writerName = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                                    File.separator
	                                    + pdfFileName.replace(".pdf", ".training.header.authors.tei.xml")), false), "UTF-8");
	                            writerName.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	                            writerName.write("\n<tei xmlns=\"http://www.tei-c.org/ns/1.0\"" + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
	                                    + "xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">");
	                            writerName.write("\n\t<teiHeader>\n\t\t<fileDesc>\n\t\t\t<sourceDesc>");
	                            writerName.write("\n\t\t\t\t<biblStruct>\n\t\t\t\t\t<analytic>\n\n\t\t\t\t\t\t<author>");
	                            writerName.write("\n\t\t\t\t\t\t\t<persName>\n");

	                            writerName.write(bufferName.toString());

	                            writerName.write("\t\t\t\t\t\t\t</persName>\n");
	                            writerName.write("\t\t\t\t\t\t</author>\n\n\t\t\t\t\t</analytic>");
	                            writerName.write("\n\t\t\t\t</biblStruct>\n\t\t\t</sourceDesc>\n\t\t</fileDesc>");
	                            writerName.write("\n\t</teiHeader>\n</tei>\n");
	                            writerName.close();
	                        }
	                    }

	                    // CITATION MODEL (for bibliographical reference in header)
	                    if (bufferReference != null) {
	                        if (bufferReference.length() > 0) {
	                            Writer writerReference = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                                    File.separator
	                                    + pdfFileName.replace(".pdf", ".training.header.reference.xml")), false), "UTF-8");
	                            writerReference.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	                            writerReference.write("<citations>\n");

	                            writerReference.write(bufferReference.toString());

	                            writerReference.write("</citations>\n");
	                            writerReference.close();
	                        }
	                    }
	                }	
	            }

	            // REFERENCE SEGEMENTER MODEL
	            String referencesStr = doc.getDocumentPartText(SegmentationLabels.REFERENCES);
	            if (!referencesStr.isEmpty()) {
					//String tei = parsers.getReferenceSegmenterParser().createTrainingData2(referencesStr, id);
					org.grobid.core.utilities.Pair<String,String> result =
						parsers.getReferenceSegmenterParser().createTrainingData(doc, id);
					String tei = result.getA();
					String raw = result.getB();
					if (tei != null) {
	                    String outPath = pathTEI + "/" +
							pdfFileName.replace(".pdf", ".training.references.referenceSegmenter.tei.xml");
	                    writer = new OutputStreamWriter(new FileOutputStream(new File(outPath), false), "UTF-8");
	                    writer.write(tei + "\n");
	                    writer.close();

						// generate also the raw vector file with the features
						outPath = pathTEI + "/" + pdfFileName.replace(".pdf", ".training.references.referenceSegmenter");
	                    writer = new OutputStreamWriter(new FileOutputStream(new File(outPath), false), "UTF-8");
	                    writer.write(raw + "\n");
	                    writer.close();

						// also write the raw text as it is before reference segmentation
						outPathRawtext = pathTEI + "/" + pdfFileName
							.replace(".pdf", ".training.references.referenceSegmenter.rawtxt");
						Writer strWriter = new OutputStreamWriter(
							new FileOutputStream(new File(outPathRawtext), false), "UTF-8");
						strWriter.write(referencesStr + "\n");
						strWriter.close();
	                }
				}

	            // BIBLIO REFERENCE MODEL
				StringBuilder allBufferReference = new StringBuilder();
	            if (!referencesStr.isEmpty()) {
	                cntManager.i(CitationParserCounters.NOT_EMPTY_REFERENCES_BLOCKS);
	            }
				ReferenceSegmenter referenceSegmenter = parsers.getReferenceSegmenterParser();
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
							pdfFileName.replace(".pdf", ".training.references.tei.xml")), false), "UTF-8");

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

					// BIBLIO REFERENCE AUTHOR NAMES
	                Writer writerName = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                        File.separator +
							pdfFileName.replace(".pdf", ".training.references.authors.tei.xml")), false), "UTF-8");

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
								/*List<String> inputs = new ArrayList<String>();
								inputs.add(authorSequence);*/
								StringBuilder bufferName = parsers.getAuthorParser().trainingExtraction(authorSequence, false);
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
			e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid training" +
                    " data generation for full text.", e);
        } finally {
            DocumentSource.close(documentSource, true, true);
        }
    }

    /**
     * Extract results from a labelled full text in the training format without any string modification.
     *
     * @param result reult
     * @param tokenizations toks
     * @return extraction
     */
    private StringBuilder trainingExtraction(String result,
                                            List<LayoutToken> tokenizations) {
        // this is the main buffer for the whole full text
        StringBuilder buffer = new StringBuilder();
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
                    output = writeField(buffer, s1, lastTag0, s2, "<equation_marker>", "<ref type=\"formula\">",
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
                    output = writeField(buffer, s1, lastTag0, s2, "<equation>",
						"<formula>", addSpace, 4, false);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<equation_label>", 
						"<label>", addSpace, 4, false);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<figure_marker>",
						"<ref type=\"figure\">", addSpace, 3, false);
                }
				if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<figure>",
						"<figure>", addSpace, 3, false);
                }
				if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<table>",
						"<figure type=\"table\">", addSpace, 3, false);
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
    public static boolean writeField(StringBuilder buffer,
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
            } else if (field.equals("<equation_marker>")) {
                if (addSpace)
                    buffer.append(" ").append(outField).append(s2);
                else
                    buffer.append(outField).append(s2);
            } /*else if (field.equals("<label>")) {
                if (addSpace)
                    buffer.append(" ").append(outField).append(s2);
                else
                    buffer.append(outField).append(s2);
            } */ /*else if (field.equals("<reference_marker>")) {
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
            } else if (!lastTag0.equals("<citation_marker>") 
            	&& !lastTag0.equals("<figure_marker>")
            	&& !lastTag0.equals("<equation_marker>")
                    //&& !lastTag0.equals("<figure>")
                    ) {
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
    public static boolean writeFieldBeginEnd(StringBuilder buffer,
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
                    && !lastTag0.equals("<table_marker>") && !lastTag0.equals("<equation_marker>")) {
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
    private static boolean testClosingTag(StringBuilder buffer,
                                   String currentTag0,
                                   String lastTag0,
                                   String currentTag) {
        boolean res = false;
        // reference_marker and citation_marker are two exceptions because they can be embedded

        if (!currentTag0.equals(lastTag0) || currentTag.equals("I-<paragraph>") || currentTag.equals("I-<item>")) {
            if (currentTag0.equals("<citation_marker>") || currentTag0.equals("<equation_marker>") ||
				currentTag0.equals("<figure_marker>") || currentTag0.equals("<table_marker>")) {
                return res;
            }

            res = false;
            // we close the current tag
            if (lastTag0.equals("<other>")) {
                buffer.append("</note>\n\n");

            } else if (lastTag0.equals("<paragraph>") &&
						!currentTag0.equals("<citation_marker>") &&
						!currentTag0.equals("<table_marker>") &&
						!currentTag0.equals("<equation_marker>") &&
						!currentTag0.equals("<figure_marker>")
				) {
                buffer.append("</p>\n\n");
                res = true;

            } else if (lastTag0.equals("<section>")) {
                buffer.append("</head>\n\n");
            } else if (lastTag0.equals("<subsection>")) {
                buffer.append("</head>\n\n");
            } else if (lastTag0.equals("<equation>")) {
                buffer.append("</formula>\n\n");
            } else if (lastTag0.equals("<equation_label>")) {
                buffer.append("</label>\n\n");
            } else if (lastTag0.equals("<table>")) {
                buffer.append("</table>\n\n");
            } else if (lastTag0.equals("<figure>")) {
                buffer.append("</figure>\n\n");
            } else if (lastTag0.equals("<item>")) {
                buffer.append("</item>\n\n");
            } /*else if (lastTag0.equals("<label>")) {
                buffer.append("</label>\n\n");
            } 
			else if (lastTag0.equals("<trash>")) {
                buffer.append("</trash>\n\n");
            } */
			else if (lastTag0.equals("<citation_marker>")) {
                buffer.append("</ref>");

            } else if (lastTag0.equals("<figure_marker>")) {
                buffer.append("</ref>");
            } else if (lastTag0.equals("<table_marker>")) {
                buffer.append("</ref>");
            } else if (lastTag0.equals("<equation_marker>")) {
                buffer.append("</ref>");
            } else {
                res = false;

            }

        }
        return res;
    }

    /**
     * Process figures identified by the full text model
     */
    private List<Figure> processFigures(String rese, List<LayoutToken> layoutTokens, Document doc) {

        List<Figure> results = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FULLTEXT, rese, layoutTokens, true);

        for (TaggingTokenCluster cluster : Iterables.filter(clusteror.cluster(),
				new TaggingTokenClusteror.LabelTypePredicate(TaggingLabels.FIGURE))) {
            List<LayoutToken> tokenizationFigure = cluster.concatTokens();
            Figure result = parsers.getFigureParser().processing(
                    tokenizationFigure,
                    cluster.getFeatureBlock()
            );
			SortedSet<Integer> blockPtrs = new TreeSet<>();
			for (LayoutToken lt : tokenizationFigure) {
				if (!LayoutTokensUtil.spaceyToken(lt.t()) && !LayoutTokensUtil.newLineToken(lt.t())) {
					blockPtrs.add(lt.getBlockPtr());
				}
			}
			result.setBlockPtrs(blockPtrs);

            result.setLayoutTokens(tokenizationFigure);


			// the first token could be a space from previous page
			for (LayoutToken lt : tokenizationFigure) {
				if (!LayoutTokensUtil.spaceyToken(lt.t()) && !LayoutTokensUtil.newLineToken(lt.t())) {
					result.setPage(lt.getPage());
					break;
				}
			}

            results.add(result);
            result.setId("" + (results.size() - 1));
        }

        doc.setFigures(results);
		doc.assignGraphicObjectsToFigures();
        return results;
    }


    /**
     * Create training data for the figures as identified by the full text model.
     * Return the pair (TEI fragment, CRF raw data).
     */
    private Pair<String,String> processTrainingDataFigures(String rese,
    		List<LayoutToken> tokenizations, String id) {
    	StringBuilder tei = new StringBuilder();
    	StringBuilder featureVector = new StringBuilder();
    	int nb = 0;
    	StringTokenizer st1 = new StringTokenizer(rese, "\n");
    	boolean openFigure = false;
    	StringBuilder figureBlock = new StringBuilder();
    	List<LayoutToken> tokenizationsFigure = new ArrayList<LayoutToken>();
    	List<LayoutToken> tokenizationsBuffer = null;
    	int p = 0; // position in tokenizations
    	int i = 0;
    	while(st1.hasMoreTokens()) {
    		String row = st1.nextToken();
    		String[] s = row.split("\t");
    		String token = s[0].trim();
			int p0 = p;
            boolean strop = false;
            tokenizationsBuffer = new ArrayList<LayoutToken>();
            while ((!strop) && (p < tokenizations.size())) {
                String tokOriginal = tokenizations.get(p).getText().trim();
                if (openFigure)
                	tokenizationsFigure.add(tokenizations.get(p));
                tokenizationsBuffer.add(tokenizations.get(p));
                if (tokOriginal.equals(token)) {
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
					continue;
				}
			}

    		int ll = s.length;
    		String label = s[ll-1];
    		String plainLabel = GenericTaggerUtils.getPlainLabel(label);
    		if (label.equals("<figure>") || ((label.equals("I-<figure>") && !openFigure))) {
    			if (!openFigure) {
    				for(LayoutToken lTok : tokenizationsBuffer) {
    					tokenizationsFigure.add(lTok);
    				}
    				openFigure = true;
    			}
    			// we remove the label in the CRF row
    			int ind = row.lastIndexOf("\t");
    			figureBlock.append(row.substring(0, ind)).append("\n");
    		}
    		else if (label.equals("I-<figure>") || openFigure) {
    			// remove last token
    			if (tokenizationsFigure.size() > 0) {
    				int nbToRemove = tokenizationsBuffer.size();
    				for(int q=0; q<nbToRemove; q++)
		    			tokenizationsFigure.remove(tokenizationsFigure.size()-1);
	    		}
    			// parse the recognized figure area
//System.out.println(tokenizationsFigure.toString());
//System.out.println(figureBlock.toString()); 
	    		//adjustment
	    		if ((p != tokenizations.size()) && (tokenizations.get(p).getText().equals("\n") ||
	    											tokenizations.get(p).getText().equals("\r") ||
	    											tokenizations.get(p).getText().equals(" ")) ) {
	    			tokenizationsFigure.add(tokenizations.get(p));
	    			p++;
	    		}
	    		while((tokenizationsFigure.size() > 0) &&
	    				(tokenizationsFigure.get(0).getText().equals("\n") ||
	    				tokenizationsFigure.get(0).getText().equals(" ")) )
	    			tokenizationsFigure.remove(0);

    			// process the "accumulated" figure
    			Pair<String,String> trainingData = parsers.getFigureParser()
    				.createTrainingData(tokenizationsFigure, figureBlock.toString(), "Fig" + nb);
    			tokenizationsFigure = new ArrayList<LayoutToken>();
				figureBlock = new StringBuilder();
    			if (trainingData!= null) {
	    			if (tei.length() == 0) {
	    				tei.append(parsers.getFigureParser().getTEIHeader(id)).append("\n\n");
	    			}
	    			if (trainingData.getA() != null)
		    			tei.append(trainingData.getA()).append("\n\n");
		    		if (trainingData.getB() != null)
	    				featureVector.append(trainingData.getB()).append("\n\n");
	    		}

    			if (label.equals("I-<figure>")) {
    				for(LayoutToken lTok : tokenizationsBuffer) {
    					tokenizationsFigure.add(lTok);
    				}
    				int ind = row.lastIndexOf("\t");
	    			figureBlock.append(row.substring(0, ind)).append("\n");
	    		}
    			else {
	    			openFigure = false;
	    		}
    			nb++;
    		}
    		else
    			openFigure = false;
    	}

    	if (tei.length() != 0) {
    		tei.append("\n    </text>\n" +
                "</tei>\n");
    	}
    	return new Pair<>(tei.toString(), featureVector.toString());
    }

    /**
     * Process tables identified by the full text model
     */
    private List<Table> processTables(String rese,
									List<LayoutToken> tokenizations,
									Document doc) {
		List<Table> results = new ArrayList<>();
		TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FULLTEXT, rese, tokenizations, true);

		for (TaggingTokenCluster cluster : Iterables.filter(clusteror.cluster(),
				new TaggingTokenClusteror.LabelTypePredicate(TaggingLabels.TABLE))) {
			List<LayoutToken> tokenizationTable = cluster.concatTokens();
			Table result = parsers.getTableParser().processing(
					tokenizationTable,
					cluster.getFeatureBlock()
			);

			SortedSet<Integer> blockPtrs = new TreeSet<>();
			for (LayoutToken lt : tokenizationTable) {
				if (!LayoutTokensUtil.spaceyToken(lt.t()) && !LayoutTokensUtil.newLineToken(lt.t())) {
					blockPtrs.add(lt.getBlockPtr());
				}
			}
			result.setBlockPtrs(blockPtrs);
			result.setLayoutTokens(tokenizationTable);

			// the first token could be a space from previous page
			for (LayoutToken lt : tokenizationTable) {
				if (!LayoutTokensUtil.spaceyToken(lt.t()) && !LayoutTokensUtil.newLineToken(lt.t())) {
					result.setPage(lt.getPage());
					break;
				}
			}
			results.add(result);
			result.setId("" + (results.size() - 1));
		}

		doc.setTables(results);
		doc.postProcessTables();

		return results;
	}


 	/**
     * Create training data for the table as identified by the full text model.
     * Return the pair (TEI fragment, CRF raw data).
     */
    private Pair<String,String> processTrainingDataTables(String rese,
    	List<LayoutToken> tokenizations, String id) {
    	StringBuilder tei = new StringBuilder();
    	StringBuilder featureVector = new StringBuilder();
    	int nb = 0;
    	StringTokenizer st1 = new StringTokenizer(rese, "\n");
    	boolean openTable = false;
    	StringBuilder tableBlock = new StringBuilder();
    	List<LayoutToken> tokenizationsTable = new ArrayList<LayoutToken>();
    	List<LayoutToken> tokenizationsBuffer = null;
    	int p = 0; // position in tokenizations
    	int i = 0;
    	while(st1.hasMoreTokens()) {
    		String row = st1.nextToken();
    		String[] s = row.split("\t");
    		String token = s[0].trim();
//System.out.println(s0 + "\t" + tokenizations.get(p).getText().trim());
			int p0 = p;
            boolean strop = false;
            tokenizationsBuffer = new ArrayList<LayoutToken>();
            while ((!strop) && (p < tokenizations.size())) {
                String tokOriginal = tokenizations.get(p).getText().trim();
                if (openTable)
                	tokenizationsTable.add(tokenizations.get(p));
                tokenizationsBuffer.add(tokenizations.get(p));
                if (tokOriginal.equals(token)) {
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
					continue;
				}
			}

    		int ll = s.length;
    		String label = s[ll-1];
    		String plainLabel = GenericTaggerUtils.getPlainLabel(label);
    		if (label.equals("<table>") || (label.equals("I-<table>") && !openTable) ) {
    			if (!openTable) {
    				for(LayoutToken lTok : tokenizationsBuffer) {
    					tokenizationsTable.add(lTok);
    				}
    				openTable = true;
    			}
    			// we remove the label in the CRF row
    			int ind = row.lastIndexOf("\t");
    			tableBlock.append(row.substring(0, ind)).append("\n");
    		}
    		else if (label.equals("I-<table>") || openTable) {
    			// remove last token
    			if (tokenizationsTable.size() > 0) {
    				int nbToRemove = tokenizationsBuffer.size();
    				for(int q=0; q<nbToRemove; q++)
		    			tokenizationsTable.remove(tokenizationsTable.size()-1);
	    		}
    			// parse the recognized table area
//System.out.println(tokenizationsTable.toString());
//System.out.println(tableBlock.toString()); 
	    		//adjustment
	    		if ((p != tokenizations.size()) && (tokenizations.get(p).getText().equals("\n") ||
	    											tokenizations.get(p).getText().equals("\r") ||
	    											tokenizations.get(p).getText().equals(" ")) ) {
	    			tokenizationsTable.add(tokenizations.get(p));
	    			p++;
	    		}
	    		while( (tokenizationsTable.size() > 0) &&
	    				(tokenizationsTable.get(0).getText().equals("\n") ||
	    				tokenizationsTable.get(0).getText().equals(" ")) )
	    			tokenizationsTable.remove(0);

    			// process the "accumulated" table
    			Pair<String,String> trainingData = parsers.getTableParser().createTrainingData(tokenizationsTable, tableBlock.toString(), "Fig"+nb);
    			tokenizationsTable = new ArrayList<LayoutToken>();
				tableBlock = new StringBuilder();
    			if (trainingData!= null) {
	    			if (tei.length() == 0) {
	    				tei.append(parsers.getTableParser().getTEIHeader(id)).append("\n\n");
	    			}
	    			if (trainingData.getA() != null)
	    				tei.append(trainingData.getA()).append("\n\n");
	    			if (trainingData.getB() != null)
	    				featureVector.append(trainingData.getB()).append("\n\n");
	    		}
    			if (label.equals("I-<table>")) {
    				for(LayoutToken lTok : tokenizationsBuffer) {
    					tokenizationsTable.add(lTok);
    				}
    				int ind = row.lastIndexOf("\t");
	    			tableBlock.append(row.substring(0, ind)).append("\n");
	    		}
    			else {
	    			openTable = false;
	    		}
    			nb++;
    		}
    		else
    			openTable = false;
    	}

    	if (tei.length() != 0) {
    		tei.append("\n    </text>\n" +
                "</tei>\n");
    	}
    	return new Pair<>(tei.toString(), featureVector.toString());
    }

    /**
     * Process equations identified by the full text model
     */
    private List<Equation> processEquations(String rese,
									List<LayoutToken> tokenizations,
									Document doc) {
		List<Equation> results = new ArrayList<>();
		TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FULLTEXT, rese, tokenizations, true);
		List<TaggingTokenCluster> clusters = clusteror.cluster();

		Equation currentResult = null;
		TaggingLabel lastLabel = null;		
		for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i(clusterLabel);
			if ( (clusterLabel != TaggingLabels.EQUATION) && (clusterLabel != TaggingLabels.EQUATION_LABEL) ) {
				lastLabel = clusterLabel;
				if (currentResult != null) {
					results.add(currentResult);
					currentResult.setId("" + (results.size() - 1));
					currentResult = null;
				}
				continue;
			}

			List<LayoutToken> tokenizationEquation = cluster.concatTokens();
			String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
			
			if (currentResult == null) 
				currentResult = new Equation();
			if ( (!currentResult.getContent().isEmpty()) && (!currentResult.getLabel().isEmpty()) ) {
				results.add(currentResult);
				currentResult.setId("" + (results.size() - 1));
				currentResult = new Equation();
			}
			if (clusterLabel.equals(TaggingLabels.EQUATION)) {
				if (!currentResult.getContent().isEmpty()) {
					results.add(currentResult);
					currentResult.setId("" + (results.size() - 1));
					currentResult = new Equation();
				}
	            currentResult.appendContent(clusterContent);
            	currentResult.addLayoutTokens(cluster.concatTokens());
            } else if (clusterLabel.equals(TaggingLabels.EQUATION_LABEL)) {
                currentResult.appendLabel(clusterContent);
	            currentResult.addLayoutTokens(cluster.concatTokens());
            }

			lastLabel = clusterLabel;
		}

		// add last open result
		if (currentResult != null) {
			results.add(currentResult);
			currentResult.setId("" + (results.size() - 1));
		}

		doc.setEquations(results);

		return results;
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
                       List<Figure> figures,
                       List<Table> tables,
                       List<Equation> equations,
                       GrobidAnalysisConfig config) {
        if (doc.getBlocks() == null) {
            return;
        }
        TEIFormatter teiFormatter = new TEIFormatter(doc);
        StringBuilder tei;
        try {
            tei = teiFormatter.toTEIHeader(resHeader, null, config);

			//System.out.println(rese);
            //int mode = config.getFulltextProcessingMode();
			tei = teiFormatter.toTEIBody(tei, reseBody, resHeader, resCitations,
					layoutTokenization, figures, tables, equations, doc, config);

			tei.append("\t\t<back>\n");

			// acknowledgement is in the back
			SortedSet<DocumentPiece> documentAcknowledgementParts =
				doc.getDocumentPart(SegmentationLabels.ACKNOWLEDGEMENT);
			Pair<String, LayoutTokenization> featSeg =
				getBodyTextFeatured(doc, documentAcknowledgementParts);
			List<LayoutToken> tokenizationsAcknowledgement;
			if (featSeg != null) {
				// if featSeg is null, it usually means that no body segment is found in the
				// document segmentation
				String acknowledgementText = featSeg.getA();
				tokenizationsAcknowledgement = featSeg.getB().getTokenization();
				String reseAcknowledgement = null;
				if ( (acknowledgementText != null) && (acknowledgementText.length() >0) )
					reseAcknowledgement = label(acknowledgementText);
				tei = teiFormatter.toTEIAcknowledgement(tei, reseAcknowledgement,
					tokenizationsAcknowledgement, resCitations, config);
			}

			tei = teiFormatter.toTEIAnnex(tei, reseAnnex, resHeader, resCitations,
				tokenizationsAnnex, doc, config);

			tei = teiFormatter.toTEIReferences(tei, resCitations, config);
            doc.calculateTeiIdToBibDataSets();

            tei.append("\t\t</back>\n");

            tei.append("\t</text>\n");
            tei.append("</TEI>\n");
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
		doc.setTei(tei.toString());

		//TODO: reevaluate
//		doc.setTei(
//				XmlBuilderUtils.toPrettyXml(
//						XmlBuilderUtils.fromString(tei.toString())
//				)
//		);
	}

	private static List<TaggingLabel> inlineFullTextLabels = Arrays.asList(TaggingLabels.CITATION_MARKER, TaggingLabels.TABLE_MARKER, 
                                TaggingLabels.FIGURE_MARKER, TaggingLabels.EQUATION_LABEL);

    public static List<LayoutTokenization> getDocumentFullTextTokens(List<TaggingLabel> labels, String labeledResult, List<LayoutToken> tokenizations) {
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FULLTEXT, labeledResult, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();
        List<LayoutTokenization> labeledTokenSequences = new ArrayList<LayoutTokenization>();
        LayoutTokenization currentTokenization = null;
        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            List<LayoutToken> clusterTokens = cluster.concatTokens();

            if (inlineFullTextLabels.contains(clusterLabel)) {
                // sequence is not interrupted
                if (currentTokenization == null)
	                currentTokenization = new LayoutTokenization();

            } else {
                // we have an independent sequence
                if ( (currentTokenization != null) && (currentTokenization.size() > 0) ) {
	                labeledTokenSequences.add(currentTokenization);
					currentTokenization = new LayoutTokenization(); 
				}
            }
			if (labels.contains(clusterLabel)) {
				if (currentTokenization == null)
	                currentTokenization = new LayoutTokenization();
				currentTokenization.addTokens(clusterTokens);
            }
        }
        
        if ( (currentTokenization != null) && (currentTokenization.size() > 0) )
			labeledTokenSequences.add(currentTokenization);

        return labeledTokenSequences;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}