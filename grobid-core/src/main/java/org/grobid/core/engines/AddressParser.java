package org.grobid.core.engines;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.MutablePair;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Affiliation;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.engines.tagging.TaggerFactory;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorAddress;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.PDFAnnotation;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.core.lang.Language;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressParser extends AbstractParser {
	private static Logger LOGGER = LoggerFactory.getLogger(AddressParser.class);

    public Lexicon lexicon = Lexicon.getInstance();

    public AddressParser() {
        super(GrobidModels.ADDRESS);
    }

    public List<Affiliation> processingText(String input) throws Exception {
        if ((input == null) || (input.length() == 0)) {
            return null;
        }
    	List<Affiliation> results = null;
        try {
            input = UnicodeUtil.normaliseText(input);
            input = input.trim();

            //input = TextUtilities.dehyphenize(input);

            // TBD: pass the language object to the tokenizer 
            List<LayoutToken> tokens = analyzer.tokenizeWithLayoutToken(input);
            
            results = processingLayoutTokens(tokens);
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
        return results;
    }

    public List<List<Affiliation>> processingTextBatch(List<String> inputs) throws Exception {
        if ((inputs == null) || (inputs.size() == 0)) {
            return null;
        }
        List<List<Affiliation>> results = null;
        List<List<LayoutToken>> allTokens = new ArrayList<>();
        try {
            for (String input : inputs) {
                input = UnicodeUtil.normaliseText(input);
                input = input.trim();

                //input = TextUtilities.dehyphenize(input);
                // TBD: pass the language object to the tokenizer 

                List<LayoutToken> tokens = analyzer.tokenizeWithLayoutToken(input);
                allTokens.add(tokens);
            }
            
            results = processingBatch(allTokens);
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
        return results;
    }

    public List<Affiliation>  processingLayoutTokens(List<LayoutToken> tokens) throws Exception {
        if (CollectionUtils.isEmpty(tokens)) {
            return null;
        }
        List<List<LayoutToken>> inputsTokens = new ArrayList<>();
        inputsTokens.add(tokens);
        List<List<Affiliation>> resultList = processingBatch(inputsTokens);

        if (resultList == null || resultList.size() == 0) {
            // return empty list
            return new ArrayList<Affiliation>();
        }
        return resultList.get(0);
    }

    public List<List<Affiliation>> processingBatch(List<List<LayoutToken>> inputsTokens) throws Exception {
        List<List<Affiliation>> results = null;
        try {
            List<String> allSequencesWithFeatures = new ArrayList<>();
            for(List<LayoutToken> tokens : inputsTokens) {
                List<OffsetPosition> placesPositions = lexicon.tokenPositionsLocationNames(tokens);
                List<OffsetPosition> countriesPositions = lexicon.tokenPositionsCountryNames(tokens);

                String sequenceWithFeatures = 
                    FeaturesVectorAddress.addFeaturesAddress(tokens, 
                                                            null,
                                                            placesPositions, 
                                                            countriesPositions);
                allSequencesWithFeatures.add(sequenceWithFeatures);
            }
            
            String allRes = label(allSequencesWithFeatures);
System.out.println(allRes);
            results = resultExtractionLayoutTokens(allRes, inputsTokens);

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
        return results;
    }

    private List<List<Affiliation>> resultExtractionLayoutTokens(String allRes, 
                                                        List<List<LayoutToken>> inputsTokens) {
        if (CollectionUtils.isEmpty(inputsTokens)) {
            return null;
        }

        List<List<Affiliation>> results = new ArrayList<>();
        if (allRes == null || allRes.length() == 0)
            return null;
        String[] resBlocks = allRes.split("\n\n");
        int i = 0;
        for (List<LayoutToken> tokens : inputsTokens) {
            if (CollectionUtils.isEmpty(tokens)) {
                results.add(null);
                continue;
            }
            String res = resBlocks[i];       
            i++;
            List<Affiliation> localResults = new ArrayList<>();
            try {
                TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.ADDRESS, res, tokens);
                Affiliation aff = new Affiliation();
                
                List<TaggingTokenCluster> clusters = clusteror.cluster();
                for (TaggingTokenCluster cluster : clusters) {
                    if (cluster == null) {
                        continue;
                    }

                    TaggingLabel clusterLabel = cluster.getTaggingLabel();
                    Engine.getCntManager().i(clusterLabel);
                    //String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
                    String clusterContent = StringUtils.normalizeSpace(LayoutTokensUtil.toText(cluster.concatTokens()));
                    if (clusterContent.trim().length() == 0)
                        continue;
                    if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_COUNTRY)) {
                        if (aff.getCountry() != null) {
                            aff.setCountry(aff.getCountry() + " " + clusterContent);
                        } else {
                            aff.setCountry(clusterContent);
                        }
                        aff.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_POSTCODE)) {
                        if (aff.getPostCode() != null) {
                            aff.setPostCode(aff.getPostCode() + " " + clusterContent);
                        } else {
                            aff.setPostCode(clusterContent);
                        }
                        aff.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_POSTBOX)) {
                        if (aff.getPostBox() != null) {
                            aff.setPostBox(aff.getPostBox() + " " + clusterContent);
                        } else {
                            aff.setPostBox(clusterContent);
                        }
                        aff.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_REGION)) {
                        if (aff.getRegion() != null) {
                            aff.setRegion(aff.getRegion() + " " + clusterContent);
                        } else {
                            aff.setRegion(clusterContent);
                        }
                        aff.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_SETTLEMENT)) {
                        if (aff.getSettlement() != null) {
                            aff.setSettlement(aff.getSettlement() + " " + clusterContent);
                        } else {
                            aff.setSettlement(clusterContent);
                        }
                        aff.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_ADDRESSLINE)) {
                        if (aff.getAddrLine() != null) {
                            aff.setAddrLine(aff.getAddrLine() + " " + clusterContent);
                        } else {
                            aff.setAddrLine(clusterContent);
                        }
                        aff.appendLayoutTokens(cluster.concatTokens());
                    }
                }

                // add last built affiliation
                if (aff.hasAddress()) {
                    localResults.add(aff);
                } 
            } catch (Exception e) {
                throw new GrobidException("An exception occurred while running Grobid.", e);
            }

            results.add(localResults);
        }

        return results;
    }


    private String writeField(String s1,
                              String lastTag0,
                              String s2,
                              String field,
                              String outField,
                              boolean addSpace,
                              int nbIndent) {
        String result = null;
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            if ((s1.equals("<other>") || s1.equals("I-<other>"))) {
                if (addSpace)
                    result = " " + s2;
                else
                    result = s2;
            } else if ((s1.equals(lastTag0) || s1.equals("I-" + lastTag0))) {
                if (addSpace)
                    result = " " + s2;
                else
                    result = s2;
            } else {
                result = "";
	               for (int i = 0; i < nbIndent; i++) {
	                result += "\t";
	            }
				
				if (addSpace)
					result += " " + outField + s2;
				else		
 					result += outField + s2;
            }
        }
        return result;
    }

    private boolean testClosingTag(StringBuilder buffer,
                                   String currentTag0,
                                   String lastTag0) {
        boolean res = false;
        if (!currentTag0.equals(lastTag0)) {
            res = true;
            // we close the current tag
            if (lastTag0.equals("<other>")) {
				buffer.append("\n");
            } else if (lastTag0.equals("<forename>")) {
                buffer.append("</forename>");
				buffer.append("\n");
            } else if (lastTag0.equals("<middlename>")) {
                buffer.append("</middlename>");
				buffer.append("\n");
            } else if (lastTag0.equals("<surname>")) {
                buffer.append("</surname>");
				buffer.append("\n");
            } else if (lastTag0.equals("<title>")) {
                buffer.append("</roleName>");
				buffer.append("\n");
            } else if (lastTag0.equals("<suffix>")) {
                buffer.append("</suffix>");
				buffer.append("\n");
            } else if (lastTag0.equals("<marker>")) {
                buffer.append("</marker>");
				buffer.append("\n");
            } else {
                res = false;
            }

        }
        return res;
    }

    public void close() throws IOException {
    }
}