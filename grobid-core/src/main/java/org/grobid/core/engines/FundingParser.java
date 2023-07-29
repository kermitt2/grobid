package org.grobid.core.engines;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.Funding;
import org.grobid.core.data.Funder;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabels;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorFunding;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.OffsetPosition;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.apache.commons.lang3.tuple.Pair;

import static org.grobid.core.engines.label.TaggingLabels.*;

public class FundingParser extends AbstractParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundingParser.class);

    protected FundingParser() {
        super(GrobidModels.FUNDING);
    }

    /**
     * The processing here is called from the header and/or full text parser in cascade
     * when one of these higher-level model detect a "funding" section, or in case
     * no funding section is found, when a acknolwedgements section is detected.
     * 
     * Independently from the place this parser is called, it process the input sequence 
     * of layout tokens in a context free manner. 
     */
    public List<Funding> processing(List<LayoutToken> tokenizationFunding) {
        if (tokenizationFunding == null || tokenizationFunding.size() == 0)
            return null;
        String res;
        try {
            String featureVector = FeaturesVectorFunding.addFeatures(tokenizationFunding, null);
            res = label(featureVector);
        } catch (Exception e) {
            throw new GrobidException("CRF labeling with table model fails.", e);
        }

        if (res == null) {
            return null;
        }
        return getExtractionResult(tokenizationFunding, res);
    }

    private List<Funding> getExtractionResult(List<LayoutToken> tokenizations, String result) {
        List<Funding> fundings = new ArrayList<>();
      
        // first funding
        Funding funding = new Funding();
        
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FUNDING, result, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();
        TaggingLabel previousLabel = null;

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i(clusterLabel);

            List<LayoutToken> tokens = cluster.concatTokens();
            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(tokens));
            if (clusterLabel.equals(FUNDING_FUNDER_FULL_NAME)) {
                Funder localFunder = funding.getFunder();
                if (localFunder == null) {
                    localFunder = new Funder();
                    funding.setFunder(localFunder);
                }

                if (StringUtils.isNotBlank(localFunder.getFullName())) {
                    fundings.add(funding);
                    // next funding object
                    funding = new Funding();
                    localFunder = new Funder();
                    funding.setFunder(localFunder);
                }

                localFunder.setFullName(clusterContent);
                localFunder.appendFullNameLayoutTokens(tokens);
                localFunder.addLayoutTokens(tokens);
                funding.addLayoutTokens(tokens);
            } else {
                LOGGER.warn("Unexpected funding model label - " + clusterLabel.getLabel() + " for " + clusterContent);
            }

            // last funding
            if (funding.isValid())
                fundings.add(funding);

            previousLabel = clusterLabel;
        }     

        return fundings;
    }

    /**
     * The training data creation is called from the full text training creation in cascade.
     */
    public Pair<String, String> createTrainingData(List<LayoutToken> tokenizations,
                                                   String id) {
        String res = null;
        String featureVector = null;
        try {
            featureVector = FeaturesVectorFunding.addFeatures(tokenizations, null);
            res = label(featureVector);
        } catch (Exception e) {
            LOGGER.error("Sequence labeling in FundingParser fails.", e);
        }
        if (res == null) {
            return Pair.of(null, featureVector);
        }

        List<Pair<String, String>> labeled = GenericTaggerUtils.getTokensAndLabels(res);
        StringBuilder sb = new StringBuilder();

        int tokPtr = 0;
        boolean addSpace = false;
        String lastTag = null;
        boolean fundOpen = false;
        for (Pair<String, String> l : labeled) {
            String tok = l.getLeft();
            String label = l.getRight();

            int tokPtr2 = tokPtr;
            for (; tokPtr2 < tokenizations.size(); tokPtr2++) {
                if (tokenizations.get(tokPtr2).getText().equals(" ")) {
                    addSpace = true;
                } else if (tokenizations.get(tokPtr2).getText().equals("\n") ||
                        tokenizations.get(tokPtr).getText().equals("\r")) {
                    addSpace = true;
                } else {
                    break;
                }
            }
            tokPtr = tokPtr2;

            if (tokPtr >= tokenizations.size()) {
                LOGGER.error("Implementation error: Reached the end of tokenizations, but current token is " + tok);
                // we add a space to avoid concatenated text
                addSpace = true;
            } else {
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
                                    tokPtr = tokPtr - 3;
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
                testClosingTag(sb, plainLabel, lastTag, addSpace);
            }

            output = writeField(label, lastTag, tok, "<funderFull>", "<fundingAgency>", addSpace, 3);
            String fundingOpening = "\t\t<funding>\n";
            if (output != null) {
                if (!fundOpen) {
                    sb.append(fundingOpening);
                    fundOpen = true;
                }
                sb.append(output);
            }
            output = writeField(label, lastTag, tok, "<funderAbbrv>", "<fundingAgency>", addSpace, 3);
            if (output != null) {
                if (!fundOpen) {
                    sb.append(fundingOpening);
                    fundOpen = true;
                }
                sb.append(output);
            }
            output = writeField(label, lastTag, tok, "<grantNumber>", "<grantNumber>", addSpace, 3);
            if (output != null) {
                if (!fundOpen) {
                    sb.append(fundingOpening);
                    fundOpen = true;
                }
                sb.append(output);
            }
            output = writeField(label, lastTag, tok, "<projectFull>", "<projectName>", addSpace, 3);
            if (output != null) {
                if (!fundOpen) {
                    sb.append(fundingOpening);
                    fundOpen = true;
                }
                sb.append(output);
            }
            output = writeField(label, lastTag, tok, "<projectAbbrv>", "<projectName>", addSpace, 3);
            if (output != null) {
                if (!fundOpen) {
                    sb.append(fundingOpening);
                    fundOpen = true;
                }
                sb.append(output);
            }
            output = writeField(label, lastTag, tok, "<url>", "<url>", addSpace, 3);
            if (output != null) {
                if (!fundOpen) {
                    sb.append(fundingOpening);
                    fundOpen = true;
                }
                sb.append(output);
            }

            lastTag = plainLabel;
            addSpace = false;
            tokPtr++;
        }

        if (fundOpen) {
            testClosingTag(sb, "", lastTag, addSpace);
            sb.append("\t\t</funding>\n");
        }

        return Pair.of(sb.toString(), featureVector);
    }

    public String getTEIHeader(String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tei>\n" +
                "    <teiHeader>\n" +
                "        <fileDesc xml:id=\"_" + id + "\"/>\n" +
                "    </teiHeader>\n" +
                "    <text xml:lang=\"en\">\n");
        return sb.toString();
    }

    private boolean testClosingTag(StringBuilder buffer,
                                   String currentTag,
                                   String lastTag,
                                   boolean addSpace) {
        boolean res = false;
        if (!currentTag.equals(lastTag)) {
            res = true;
            // we close the current tag
            if (lastTag.equals("<funderFull>")) {
                if (addSpace)
                    buffer.append(" ");
                buffer.append("<funderFull>\n");
            } else if (lastTag.equals("<funderAbbrv>")) {
                if (addSpace)
                    buffer.append(" ");
                buffer.append("</funderAbbrv>\n");
            } else if (lastTag.equals("<grantNumber>")) {
                if (addSpace)
                    buffer.append(" ");
                buffer.append("</grantNumber>\n");
            } else if (lastTag.equals("<projectFull>")) {
                if (addSpace)
                    buffer.append(" ");
                buffer.append("</projectFull>\n");
            } else if (lastTag.equals("<projectAbbrv>")) {
                if (addSpace)
                    buffer.append(" ");
                buffer.append("</projectAbbrv>\n");
            } else if (lastTag.equals("<url>")) {
                if (addSpace)
                    buffer.append(" ");
                buffer.append("</url>\n");
            }else {
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
                              int nbIndent) {
        String result = null;
        if (currentTag.endsWith(field)) {
            /*if (currentTag.endsWith("<other>") || currentTag.endsWith("<content>")) {
                result = "";
                if (currentTag.startsWith("I-") || (lastTag == null)) {
                    result += "\n";
                    for (int i = 0; i < nbIndent; i++) {
                        result += "    ";
                    }
                }
                if (addSpace)
                    result += " ";
                result += TextUtilities.HTMLEncode(token);
            }
            else*/
            if ((lastTag != null) && currentTag.endsWith(lastTag)) {
                result = "";
                if (addSpace)
                    result += " ";
                if (currentTag.startsWith("I-"))
                    result += outField;
                result += TextUtilities.HTMLEncode(token);
            } else {
                result = "";
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