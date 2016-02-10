package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Figure;
import org.grobid.core.engines.citations.LabeledReferenceResult;
import org.grobid.core.engines.citations.ReferenceSegmenter;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.features.FeaturesVectorReferenceSegmenter;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.document.Document;
import org.grobid.core.engines.citations.LabeledReferenceResult;
import org.grobid.core.engines.citations.ReferenceSegmenter;
import org.grobid.core.engines.counters.CitationParserCounters;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.data.Table;
import org.grobid.core.engines.config.GrobidAnalysisConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Matcher;

/**
 * @author Patrice
 */
public class TableParser extends AbstractParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(TableParser.class);

    protected TableParser() {
        super(GrobidModels.TABLE);
    }

	/**
	 * The processing here is called from the full text parser in cascade.
	 */
    public Table processing(List<LayoutToken> tokenizationTable, String featureVector) {
		String res = null;
		try {
			res = label(featureVector);
		}
		catch(Exception e) {
			throw new GrobidException("CRF labeling in ReferenceSegmenter fails.", e);
		}
		if (res == null) {
			return null;
		}		
//        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);
		return getExtractionResult(tokenizationTable, res);
    }

	private Table getExtractionResult(List<LayoutToken> tokenizations, String result) {
		Table table = new Table();
		TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.TABLE, result, tokenizations);
		List<TaggingTokenCluster> clusters = clusteror.cluster();

		for (TaggingTokenCluster cluster : clusters) {
			if (cluster == null) {
				continue;
			}

			TaggingLabel clusterLabel = cluster.getTaggingLabel();
			Engine.getCntManager().i(clusterLabel);

			String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
			switch (clusterLabel) {
				case TBL_DESC:
					table.appendCaption(clusterContent);
					break;
				case TBL_HEAD:
					table.appendHeader(clusterContent);
					break;
				case TBL_LABEL:
					table.appendLabel(clusterContent);
					break;
				case TBL_OTHER:
					break;
				case TBL_TRASH:
					table.appendContent(clusterContent);
					break;
				default:
					LOGGER.error("Warning: unexpected table model label - " + clusterLabel + " for " + clusterContent);
			}
		}
		return table;
	}

	private Table getExtractionResult(List<LayoutToken> tokenizations,
									  List<Pair<String, String>> labeled) {
		Table table = new Table();
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
                    table.appendCaption(" ");
                    addSpace = false;
                }

                table.appendCaption(tok);
            } else if (plainLabel.equals("<figure_head>")) {
                if (addSpace) {
                    table.appendHeader(" ");
                    addSpace = false;
                }
                if (addLine) {
                    table.appendHeader("\n");
                    addLine = false;
                }

                table.appendHeader(tok);
            } else if (plainLabel.equals("<trash>")) {
                if (addSpace) {
                    table.appendContent(" ");
                    addSpace = false;
                }
                if (addLine) {
                    table.appendContent("\n");
                    addLine = false;
                }

                table.appendContent(tok);
            } else if (plainLabel.equals("<label>")) {
                if (addSpace) {
                    table.appendLabel(" ");
                    table.appendHeader(" ");
                    addSpace = false;
                }
                if (addLine) {
                    table.appendLabel("\n");
                    table.appendHeader("\n");
                    addLine = false;
                }

                table.appendLabel(tok);
                table.appendHeader(tok);
            } else if (plainLabel.equals("<other>")) {
				//features.append(theFeatures);
				//features.append("\n");
			}
            tokPtr++;
        }
        //table.setId();
		return table;
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
			LOGGER.error("CRF labeling in TableParser fails.", e);
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
			String tableOpening = "        <figure type=\"table\">\n";
			if (output != null) {
				if (!figOpen) {
					sb.append(tableOpening);
					figOpen= true;
				}
				sb.append(output);
			}
			output = writeField(label, lastTag, tok, "<figDesc>", "<figDesc>", addSpace, addEOL, 3);
			if (output != null) {
				if (!figOpen) {
					sb.append(tableOpening);
					figOpen= true;
				}
				sb.append(output);
			}
			output = writeField(label, lastTag, tok, "<label>", "<label>", addSpace, addEOL, 3);
			if (output != null) {
				if (!figOpen) {
					sb.append(tableOpening);
					figOpen= true;
				}
				sb.append(output);
			}
			output = writeField(label, lastTag, tok, "<trash>", "<table>", addSpace, addEOL, 3);
			if (output != null) {
				if (!figOpen) {
					sb.append(tableOpening);
					figOpen= true;
				}
				sb.append(output);
				//continue;
			}
			output = writeField(label, lastTag, tok, "<other>", "<other>", addSpace, addEOL, 2);
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
                buffer.append("<other>\n");
            } else if (lastTag.equals("<figure_head>")) {
				if (addEOL)
                    buffer.append("<lb/>");
				if (addSpace)
                    buffer.append(" ");
				buffer.append("</head>\n");
            } else if (lastTag.equals("<trash>")) {
				if (addEOL)
                    buffer.append("<lb/>");
				if (addSpace)
                    buffer.append(" ");
				buffer.append("</table>\n");
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
            /*if (currentTag.endsWith("<other>") || currentTag.endsWith("<trash>")) {
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
			else*/ if ((lastTag != null) && currentTag.endsWith(lastTag)) {
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

}
