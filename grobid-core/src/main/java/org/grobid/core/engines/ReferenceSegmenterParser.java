package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.citations.LabeledReferenceResult;
import org.grobid.core.engines.citations.ReferenceSegmenter;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.features.FeaturesVectorReferenceSegmenter;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Slava
 * Date: 4/14/14
 */
public class ReferenceSegmenterParser extends AbstractParser implements ReferenceSegmenter{
	private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceSegmenterParser.class);
	
    protected ReferenceSegmenterParser() {
        super(GrobidModels.REFERENCE_SEGMENTER);
    }

    /**
     *
     * @param referenceBlock text containing citation block
     * @return <reference_label, reference_string>  Note, that label is null when no label was detected
     *              example: <"[1]", "Hu W., Barkana, R., &amp; Gruzinov A. Phys. Rev. Lett. 85, 1158">
     */
    public List<LabeledReferenceResult> extract(String referenceBlock) {
        List<String> blocks = new ArrayList<String>();


        //String input = referenceBlock.replace("\n", " @newline ");
        String input = referenceBlock;
		if ( (input == null) || (input.trim().length()== 0) )
			return null;
		input = input.replace("\t", " ");
		StringTokenizer st = new StringTokenizer(input, TextUtilities.delimiters, true);
		
        if (st.countTokens() == 0) {
            return null;
        }

        List<String> tokenizations = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            final String tok = st.nextToken();
            
            if (tok.equals("\n")) {
				blocks.add("@newline");
				//tokenizations.add(" ");
			}
			else if (!tok.equals(" ")) {
                blocks.add(tok + " <reference-block>");
				tokenizations.add(tok);
            }
			else {
				tokenizations.add(" ");
			}
        }
        blocks.add("\n");
        String featureVector = FeaturesVectorReferenceSegmenter.addFeaturesReferenceSegmenter(blocks);
        String res = label(featureVector);

        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);

        return getExtractionResult(tokenizations, labeled);
    }

    private List<LabeledReferenceResult> getExtractionResult(List<String> tokenizations, List<Pair<String, String>> labeled) {
        List<LabeledReferenceResult> resultList = new ArrayList<LabeledReferenceResult>();
        StringBuilder reference = new StringBuilder();
        StringBuilder referenceLabel = new StringBuilder();

        int tokPtr = 0;
        boolean addSpace = false;
        for (Pair<String, String> l : labeled) {
            String tok = l.a;
            String label = l.b;
            while(tokPtr < tokenizations.size()) {
				while (tokenizations.get(tokPtr).equals(" ")) {
                	addSpace = true;
                	tokPtr++;
				}
				break;
            }

            if (tokPtr >= tokenizations.size()) {
                //throw new IllegalStateException("Implementation error: Reached the end of tokenizations, but current token is " + tok);
				LOGGER.error("Implementation error: Reached the end of tokenizations, but current token is " + tok);
				// we add a space to avoid concatenated text
				addSpace = true;
            }
            else {
				String tokenizationToken = tokenizations.get(tokPtr);
			
				if ((tokPtr == tokenizations.size()) && !tokenizationToken.equals(tok)) {
					// and we add a space by default to avoid concatenated text
					addSpace = true;
					if (!tok.startsWith(tokenizationToken)) {
						// this is a very exceptional case due to a sequence of accent/diacresis, in this case we skip
						// a shift in the tokenizations list and continue on the basis of the labeled token
						// we check one ahead
						tokPtr++;
						tokenizationToken = tokenizations.get(tokPtr);
						if (!tok.equals(tokenizationToken)) {
							// we try another position forward (second hope!)
							tokPtr++;
							tokenizationToken = tokenizations.get(tokPtr);
							if (!tok.equals(tokenizationToken)) {
								// we try another position forward (last hope!)
								tokPtr++;
								tokenizationToken = tokenizations.get(tokPtr);
								if (!tok.equals(tokenizationToken)) {
									// we return to the initial position
									tokPtr = tokPtr-3;
									tokenizationToken = tokenizations.get(tokPtr);
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
            if (plainLabel.equals("<label>")) {
                if (GenericTaggerUtils.isBeginningOfEntity(label)) {
                    if (reference.length() != 0) {
                        resultList.add(new LabeledReferenceResult(referenceLabel.length() == 0 ? null :
                                referenceLabel.toString().trim(), reference.toString().trim()));
                        reference.setLength(0);
                        referenceLabel.setLength(0);
                    }
                }
                if (addSpace) {
                    referenceLabel.append(' ');
                    addSpace = false;
                }
                referenceLabel.append(tok);

            } else if (plainLabel.equals("<reference>")) {
                if (GenericTaggerUtils.isBeginningOfEntity(label)) {
                    if (reference.length() != 0) {
                        resultList.add(new LabeledReferenceResult(referenceLabel.length() == 0 ?
                                null : referenceLabel.toString().trim(), reference.toString().trim()));
                        reference.setLength(0);
                        referenceLabel.setLength(0);
                    }
                }
                if (addSpace) {
                    reference.append(' ');
                    addSpace = false;
                }
                reference.append(tok);

            }
            tokPtr++;
        }

        if (reference.length() != 0) {
            resultList.add(new LabeledReferenceResult(referenceLabel.length() == 0 ? null :
                    referenceLabel.toString().trim(), reference.toString().trim()));
            reference.setLength(0);
            referenceLabel.setLength(0);
        }

//        for (LabeledReferenceResult r : resultList) {
//            System.out.println(r);
//        }
        return resultList;
    }


    public String createTrainingData(String input) {
        List<LabeledReferenceResult> res = extract(input);
        StringBuilder sb = new StringBuilder();

        sb.append("<tei>\n" +
                "    <teiHeader>\n" +
                "        <fileDesc xml:id=\"0\"/>\n" +
                "    </teiHeader>\n" +
                "    <text xml:lang=\"en\">\n" +
                "        <listBibl>");

        for (LabeledReferenceResult p : res) {
            if (p.getLabel() != null) {
                sb.append(String.format("<bibl> <label>%s</label>%s</bibl>", p.getLabel(), p.getReferenceText()));
            } else {
                sb.append(String.format("<bibl>%s</bibl>", p.getReferenceText()));
            }
            sb.append("\n");
        }

        sb.append("        </listBibl>\n" +
                "    </text>\n" +
                "</tei>\n");
        return sb.toString();
    }

	public String createTrainingData2(String input, int id) {
		List<String> tokenizations = new ArrayList<String>();
		input = input.replace("\t", " ");
		StringTokenizer st = new StringTokenizer(input, TextUtilities.delimiters, true);

		if (id == -1) {
			id = 0;
		}
		
        if (st.countTokens() == 0)
            return null;
		List<String> blocks = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            final String tok = st.nextToken();
            
            if (tok.equals("\n")) {
				blocks.add("@newline");
				tokenizations.add("\n");
			}
			else if (!tok.equals(" ")) {
                blocks.add(tok + " <reference-block>");
				tokenizations.add(tok);
            }
			else {
				tokenizations.add(" ");
			}
        }
		blocks.add("\n");
		
		String featureVector = FeaturesVectorReferenceSegmenter.addFeaturesReferenceSegmenter(blocks);
        String res = label(featureVector);
		List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);
        StringBuilder sb = new StringBuilder();

        sb.append("<tei>\n" +
                "    <teiHeader>\n" +
                "        <fileDesc xml:id=\""+ id + "\"/>\n" +
                "    </teiHeader>\n" +
                "    <text xml:lang=\"en\">\n" +
				"        <listBibl>\n"); 
		
		int tokPtr = 0;
		boolean addSpace = false;
		boolean addEOL = false;
		String lastTag = null;
		boolean refOpen = false;
		for (Pair<String, String> l : labeled) {
            String tok = l.a;
            String label = l.b;

			int tokPtr2 = tokPtr;
            for(; tokPtr2 < tokenizations.size(); tokPtr2++) {
                if (tokenizations.get(tokPtr2).equals(" ")) {
					addSpace = true;
				}
				else if (tokenizations.get(tokPtr2).equals("\n")) {
					addEOL = true;	
				}
                else {
					break;
				}
            }
			tokPtr = tokPtr2;

            if (tokPtr == tokenizations.size()) {
                throw new IllegalStateException("Implementation error: Reached the end of tokenizations, but current token is " + tok);
            }

			String tokenizationToken = tokenizations.get(tokPtr);

            if (!tokenizationToken.equals(tok)) {
                throw new IllegalStateException("Implementation error: " + tokenizationToken + " != " + tok);
            }
	
			String plainLabel = GenericTaggerUtils.getPlainLabel(label);
			
			boolean tagClosed = (lastTag != null) && testClosingTag(sb, label, lastTag, addSpace, addEOL);
			
			if (tagClosed) {
				addSpace = false;
				addEOL = false;
			}
			if (tagClosed && lastTag.equals("<reference>")) {
				refOpen = false;
			}
			String output = null;
			String field = null;
			if (refOpen) {
				field = "<label>";
			}
			else {
				field = "<bibl><label>";
			}
			output = writeField(label, lastTag, tok, "<label>", field, addSpace, addEOL, 2);
			if (output != null) {
				sb.append(output);
				refOpen = true;
			}
			else {
				if (refOpen) {
					field = "";
				}
				else {
					field = "<bibl>";
				}
				output = writeField(label, lastTag, tok, "<reference>", field, addSpace, addEOL, 2);
				if (output != null) {
					sb.append(output);
					refOpen= true;
				}
				else {
					output = writeField(label, lastTag, tok, "<other>", "", addSpace, addEOL, 2);
					if (output != null) {
						sb.append(output);
						refOpen = false;
					}
				}
			}
			
			lastTag = plainLabel;
			addSpace = false;
			addEOL = false;
            tokPtr++;
        }

		if (refOpen) {
			sb.append("</bibl>");
		}

        sb.append("\n        </listBibl>\n" +
                "    </text>\n" +
                "</tei>\n");
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
            } else if (lastTag.equals("<label>")) {
				if (addEOL)
                    buffer.append("<lb/>");
				if (addSpace)
                    buffer.append(" ");
                buffer.append("</label>");
            } else if (lastTag.equals("<reference>")) {
				if (addEOL)
                    buffer.append("<lb/>");
				if (addSpace)
                    buffer.append(" ");
                buffer.append("</bibl>\n");
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
            if (currentTag.endsWith("<other>")) {
                result = "";
				if (currentTag.equals("I-<other>")) {
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
				if (outField.length() > 0) {
					for (int i = 0; i < nbIndent; i++) {
                    	result += "    ";
                	}
				}
				if (addEOL)
                    result += "<lb/>";
                if (addSpace)
                    result += " ";
                result += outField + TextUtilities.HTMLEncode(token);
            }
        }
        return result;
    }

}
