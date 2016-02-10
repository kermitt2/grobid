package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Figure;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Patrice
 */
public class FigureParser extends AbstractParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(FigureParser.class);

    protected FigureParser() {
        super(GrobidModels.FIGURE);
    }

	/**
	 * The processing here is called from the full text parser in cascade. 
	 * Start and end position in the higher level tokenization are indicated in 
	 * the resulting Figure object. 
	 */
	public Figure processing(List<LayoutToken> tokenizationFigure, String featureVector) {

		String res;
		try {
			res = label(featureVector);
		} catch (Exception e) {
			throw new GrobidException("CRF labeling in ReferenceSegmenter fails.", e);
		}
		if (res == null) {
			return null;
		}
//        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);

//		System.out.println(Joiner.on("\n").join(labeled));
//		System.out.println("----------------------");
//		System.out.println("----------------------");

//		return getExtractionResult(tokenizationFigure, labeled);
		return getExtractionResult(tokenizationFigure, res);
	}

    private Figure getExtractionResult(List<LayoutToken> tokenizations, String result) {
		TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FIGURE, result, tokenizations);
		List<TaggingTokenCluster> clusters = clusteror.cluster();

		Figure figure = new Figure();
		for (TaggingTokenCluster cluster : clusters) {
			if (cluster == null) {
				continue;
			}

			TaggingLabel clusterLabel = cluster.getTaggingLabel();
			Engine.getCntManager().i(clusterLabel);

			String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
			switch (clusterLabel) {
				case FIG_DESC:
					figure.appendCaption(clusterContent);
					break;
				case FIG_HEAD:
					figure.appendHeader(clusterContent);
					break;
				case FIG_LABEL:
					figure.appendLabel(clusterContent);
					break;
				case FIG_OTHER:
					break;
				case FIG_TRASH:
					figure.appendContent(clusterContent);
					break;
				default:
					LOGGER.error("Warning: unexpected figure model label - " + clusterLabel + " for " + clusterContent);
			}
		}
		return figure;
	}

    private Figure getExtractionResult(List<LayoutToken> tokenizations,
		List<Pair<String, String>> labeled) {
		Figure figure = new Figure();
        int tokPtr = 0;
        boolean addSpace = false;
		boolean addLine = false;
        for (Pair<String, String> l : labeled) {
            String tok = l.a;
            String label = l.b;       
			String currToken = null;
            while(tokPtr < tokenizations.size()) {
            	currToken = tokenizations.get(tokPtr).getText();
				if (currToken.equals(" ") ||
					   currToken.equals("\n") ||
					   currToken.equals("\r") ) {
					if (currToken.equals(" "))
                		addSpace = true;
					else
						addLine = true;
				}
				else
					break;
				tokPtr++;
            } 
            if (tokPtr >= tokenizations.size()) {
                //throw new IllegalStateException("Implementation error: Reached the end of tokenizations, but current token is " + tok);
				LOGGER.error("Implementation error: Reached the end of tokenizations, but current token is " + tok);
				// we add a space to avoid concatenated text
				addSpace = true;
            }
            else {
				String tokenizationToken = tokenizations.get(tokPtr).getText();

				if ((tokPtr != tokenizations.size()) && !tokenizationToken.equals(tok)) {
					// and we add a space by default to avoid concatenated text
					addSpace = true;
					if (!tok.startsWith(tokenizationToken)) {
						// this is a very exceptional case due to a sequence of accent/diacresis, in this case we skip
						// a shift in the tokenizations list and continue on the basis of the labeled token
						// we check one ahead
						tokPtr++;
						tokenizationToken = tokenizations.get(tokPtr).getText();
						if (!tok.equals(tokenizationToken)) {
							// we try another position forward (second hope!)
							tokPtr++;
							tokenizationToken = tokenizations.get(tokPtr).getText();
							if (!tok.equals(tokenizationToken)) {
								// we try another position forward (last hope!)
								tokPtr++;
								tokenizationToken = tokenizations.get(tokPtr).getText();
								if (!tok.equals(tokenizationToken)) {
									// we return to the initial position
									tokPtr = tokPtr-3;
									tokenizationToken = tokenizations.get(tokPtr).getText();
									LOGGER.error("Implementation error, tokens out of sync: " +
										tokenizationToken + " != " + tok + ", at position " + tokPtr);
								}
							}
						}
					}
					// note: if the above condition is true, this is an exceptional case due to a
					// sequence of accent/diacresis and we can go on as a full string match
	            }
			}

            String plainLabel = GenericTaggerUtils.getPlainLabel(label);            
            if (plainLabel.equals("<figDesc>")) {
                if (addSpace || addLine) {
                    figure.appendCaption(" ");
                    addSpace = false;
                    addLine = false;
                }

                figure.appendCaption(tok);
            } else if (plainLabel.equals("<figure_head>")) {    	
                if (addSpace) {
                    figure.appendHeader(" ");
                    addSpace = false;
                }
                if (addLine) {
                    figure.appendHeader("\n");
                    addLine = false;
                }

                figure.appendHeader(tok);               
            } else if (plainLabel.equals("<trash>")) {
                if (addSpace) {
                    figure.appendContent(" ");
                    addSpace = false;
                }
                if (addLine) {
                    figure.appendContent("\n");
                    addLine = false;
                }

                figure.appendContent(tok);
            } else if (plainLabel.equals("<label>")) {
                if (addSpace) {
                    figure.appendLabel(" ");
                    figure.appendHeader(" ");
                    addSpace = false;
                }
                if (addLine) {
                    figure.appendLabel("\n");
                    figure.appendHeader("\n");
                    addLine = false;
                }

                figure.appendLabel(tok);
                figure.appendHeader(tok); 
            } else if (plainLabel.equals("<other>")) {
				//features.append(theFeatures);
				//features.append("\n");
			}
			else {
				LOGGER.info("Warning: unexpected figure model label - " + plainLabel + " for "
								 + tok + ", at position " + tokPtr);
			}
            tokPtr++;
        }
        //figure.setId();
		return figure;
    }

	/**
	 * The training data creation is called from the full text training creation in cascade.
	 */
	public org.grobid.core.utilities.Pair<String,String> createTrainingData(List<LayoutToken> tokenizations, 
			String featureVector, String id) {
//System.out.println(tokenizations.toString() + "\n" );
		String res = null;
		try {
			res = label(featureVector);
		}
		catch(Exception e) {
			LOGGER.error("CRF labeling in FigureParser fails.", e);
		}	
		if (res == null) {
			return new Pair(null, featureVector);
		}
//System.out.println(res + "\n" );
        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);		
        StringBuilder sb = new StringBuilder();
		
		int tokPtr = 0;
		boolean addSpace = false;
		boolean addEOL = false;
		String lastTag = null;
		boolean figOpen = false;
		for (Pair<String, String> l : labeled) {
            String tok = l.a;
            String label = l.b;

			int tokPtr2 = tokPtr;
            for(; tokPtr2 < tokenizations.size(); tokPtr2++) {
                if (tokenizations.get(tokPtr2).getText().equals(" ")) {
					addSpace = true;
				}
				else if (tokenizations.get(tokPtr2).getText().equals("\n") ||
					     tokenizations.get(tokPtr).getText().equals("\r") ) {
					addEOL = true;	
				}
                else {
					break;
				}
            }
			tokPtr = tokPtr2;

            if (tokPtr >= tokenizations.size()) {
				LOGGER.error("Implementation error: Reached the end of tokenizations, but current token is " + tok);
				// we add a space to avoid concatenated text
				addSpace = true;
            }
            else {
				String tokenizationToken = tokenizations.get(tokPtr).getText();

				if ((tokPtr != tokenizations.size()) && !tokenizationToken.equals(tok)) {
					// and we add a space by default to avoid concatenated text
					addSpace = true;
					if (!tok.startsWith(tokenizationToken)) {
						// this is a very exceptional case due to a sequence of accent/diacresis, in this case we skip
						// a shift in the tokenizations list and continue on the basis of the labeled token
						// we check one ahead
						tokPtr++;
						tokenizationToken = tokenizations.get(tokPtr).getText();
						if (!tok.equals(tokenizationToken)) {
							// we try another position forward (second hope!)
							tokPtr++;
							tokenizationToken = tokenizations.get(tokPtr).getText();
							if (!tok.equals(tokenizationToken)) {
								// we try another position forward (last hope!)
								tokPtr++;
								tokenizationToken = tokenizations.get(tokPtr).getText();
								if (!tok.equals(tokenizationToken)) {
									// we return to the initial position
									tokPtr = tokPtr-3;
									tokenizationToken = tokenizations.get(tokPtr).getText();
									LOGGER.error("Implementation error, tokens out of sync: " +
										tokenizationToken + " != " + tok + ", at position " + tokPtr);
								}
							}
						}
					}
					// note: if the above condition is true, this is an exceptional case due to a
					// sequence of accent/diacresis and we can go on as a full string match
	            }
			}
			
			String plainLabel = GenericTaggerUtils.getPlainLabel(label);

			String output = null;
			if (lastTag != null) {
                testClosingTag(sb, plainLabel, lastTag, addSpace, addEOL);
            }

			output = writeField(label, lastTag, tok, "<figure_head>", "<head>", addSpace, addEOL, 3);
			String figureOpening = "        <figure>\n";
			if (output != null) {
				if (!figOpen) {
					sb.append(figureOpening);
					figOpen= true;
				}
				sb.append(output);
			}
			output = writeField(label, lastTag, tok, "<figDesc>", "<figDesc>", addSpace, addEOL, 3);
			if (output != null) {
				if (!figOpen) {
					sb.append(figureOpening);
					figOpen= true;
				}
				sb.append(output);
			}
			output = writeField(label, lastTag, tok, "<label>", "<label>", addSpace, addEOL, 3);
			if (output != null) {
				if (!figOpen) {
					sb.append(figureOpening);
					figOpen= true;
				}
				sb.append(output);
			}
			output = writeField(label, lastTag, tok, "<trash>", "", addSpace, addEOL, 3);
			if (output != null) {
				if (!figOpen) {
					sb.append(figureOpening);
					figOpen= true;
				}
				sb.append(output);
				//continue;
			}
			output = writeField(label, lastTag, tok, "<other>", "", addSpace, addEOL, 2);
			if (output != null) {
				sb.append(output);
			}

			lastTag = plainLabel;
			addSpace = false;
			addEOL = false;
            tokPtr++;
        }

		if (figOpen) {
			testClosingTag(sb, "", lastTag, addSpace, addEOL);
			sb.append("        </figure>\n");
		}
		
		return new Pair(sb.toString(), featureVector);
    }

    public String getTEIHeader(String id) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<tei>\n" +
                "    <teiHeader>\n" +
                "        <fileDesc xml:id=\"_"+ id + "\"/>\n" +
                "    </teiHeader>\n" +
                "    <text xml:lang=\"en\">\n"); 
    	return sb.toString();
    }

	private boolean testClosingTag(StringBuilder buffer,
                                   String currentTag,
                                   String lastTag,
								   boolean addSpace,
								   boolean addEOL) {	
        boolean res = false;
        if (!currentTag.equals(lastTag)) {
            res = true;
            // we close the current tag
            if (lastTag.equals("<other>")) {
				if (addEOL)
                    buffer.append("<lb/>");
				if (addSpace)
                    buffer.append(" ");
                buffer.append("\n");
            } else if (lastTag.equals("<figure_head>")) {
				if (addEOL)
                    buffer.append("<lb/>");
				if (addSpace)
                    buffer.append(" ");
				buffer.append("</head>\n");
            } else if (lastTag.equals("<figDesc>")) {
				if (addEOL)
                    buffer.append("<lb/>");
				if (addSpace)
                    buffer.append(" ");
                buffer.append("</figDesc>\n");
            } else if (lastTag.equals("<label>")) {
				if (addEOL)
                    buffer.append("<lb/>");
				if (addSpace)
                    buffer.append(" ");
                buffer.append("</label>\n");
            } else if (lastTag.equals("<trash>")) {
				if (addEOL)
                    buffer.append("<lb/>");
				if (addSpace)
                    buffer.append(" ");
                buffer.append("</trash>\n");
            } else {
                res = false;
            }
        }
        return res;
    }

    private String writeField(String currentTag,
                              String lastTag,
                              String token,
                              String field,
                              String outField,
                              boolean addSpace,
							  boolean addEOL,
							  int nbIndent) {
        String result = null;       
        if (currentTag.endsWith(field)) {
            if (currentTag.endsWith("<other>") || currentTag.endsWith("<trash>")) {
                result = "";
				if (currentTag.startsWith("I-") || (lastTag == null)) {
					result += "\n";
					for (int i = 0; i < nbIndent; i++) {
	                    result += "    ";
	                }
				}
				if (addEOL)
                    result += "<lb/>";
				if (addSpace)
                    result += " ";
                result += TextUtilities.HTMLEncode(token);
            }
			else if ((lastTag != null) && currentTag.endsWith(lastTag)) {
                result = "";
				if (addEOL)
                    result += "<lb/>";
				if (addSpace)
                    result += " ";
				if (currentTag.startsWith("I-"))
					result += outField;
                result += TextUtilities.HTMLEncode(token);
            }
			else {
                result = "";
                if (addEOL)
                    result += "<lb/>";
                if (addSpace)
                    result += " ";
                result += "\n";
				if (outField.length() > 0) {
					for (int i = 0; i < nbIndent; i++) {
                    	result += "    ";
                	}
				}
				
            	result += outField + TextUtilities.HTMLEncode(token);
            }
        }
        return result;
    }

    /*static public String getFigureFeatured(Document doc, List<LayoutToken> figureTokens) {	
		FeatureFactory featureFactory = FeatureFactory.getInstance();
        StringBuilder figure = new StringBuilder();
        String currentFont = null;
        int currentFontSize = -1;

		List<Block> blocks = doc.getBlocks();
		if ( (blocks == null) || blocks.size() == 0) {
			return null;
		}

        // vector for features
        FeaturesVectorFulltext features;
        FeaturesVectorFulltext previousFeatures = null;
        LayoutToken layoutToken = null;
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
				boolean graphicVector = false;
	    		boolean graphicBitmap = false;
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
	             
                // check if we have a graphical object connected to the current block
                List<GraphicObject> localImages = getConnectedGraphics(block, doc);
                if (localImages != null) {
                	for(GraphicObject localImage : localImages) {
                		if (localImage.getType() == GraphicObject.BITMAP) 
                			graphicVector = true;
                		if (localImage.getType() == GraphicObject.VECTOR) 
                			graphicBitmap = true;
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
	                if (text.startsWith("@IMAGE") || text.contains(".pbm") || text.contains(".vec") || text.contains(".jpg")) {
	                    n++;
	                    mm++;
	                    nn++;
	                    continue;
	                }

	                features.string = text;

	                if (graphicBitmap) {
	                	features.bitmapAround = true;
	                }
	                if (graphicVector) {
	                	features.vectorAround = true;
	                }

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
	}*/
}
