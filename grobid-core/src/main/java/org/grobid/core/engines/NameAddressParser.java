package org.grobid.core.engines;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.MutablePair;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Person;
import org.grobid.core.data.Affiliation;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.engines.tagging.TaggerFactory;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorNameAddress;
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

public class NameAddressParser extends AbstractParser {
	private static Logger LOGGER = LoggerFactory.getLogger(NameAddressParser.class);

    public Lexicon lexicon = Lexicon.getInstance();

    public NameAddressParser() {
        super(GrobidModels.NAMES_ADDRESS);
    }

    public List<Pair<Person,Affiliation>> processingText(String input) throws Exception {
        if ((input == null) || (input.length() == 0)) {
            return null;
        }
    	List<Pair<Person,Affiliation>> results = null;
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

    public List<List<Pair<Person,Affiliation>>> processingTextBatch(List<String> inputs) throws Exception {
        if ((inputs == null) || (inputs.size() == 0)) {
            return null;
        }
        List<List<Pair<Person,Affiliation>>> results = null;
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

    public List<Pair<Person,Affiliation>>  processingLayoutTokens(List<LayoutToken> tokens) throws Exception {
        if (CollectionUtils.isEmpty(tokens)) {
            return null;
        }
        List<List<LayoutToken>> inputsTokens = new ArrayList<>();
        inputsTokens.add(tokens);
        List<List<Pair<Person,Affiliation>>> resultList = processingBatch(inputsTokens);

        if (resultList == null || resultList.size() == 0) {
            // return empty list
            return new ArrayList<Pair<Person,Affiliation>>();
        }
        return resultList.get(0);
    }

    public List<List<Pair<Person,Affiliation>>> processingBatch(List<List<LayoutToken>> inputsTokens) throws Exception {
        List<List<Pair<Person,Affiliation>>> results = null;
        try {
            StringBuilder allSequencesWithFeatures = new StringBuilder();
            for(List<LayoutToken> tokens : inputsTokens) {
                List<OffsetPosition> placesPositions = lexicon.tokenPositionsLocationNames(tokens);
                List<OffsetPosition> countriesPositions = lexicon.tokenPositionsCountryNames(tokens);
                List<OffsetPosition> titlePositions = lexicon.tokenPositionsPersonTitle(tokens);
                List<OffsetPosition> suffixPositions = lexicon.tokenPositionsPersonSuffix(tokens);

                String sequenceWithFeatures = 
                    FeaturesVectorNameAddress.addFeaturesNameAddress(tokens, 
                                                                    null,
                                                                    placesPositions, 
                                                                    countriesPositions, 
                                                                    titlePositions, 
                                                                    suffixPositions);
                allSequencesWithFeatures.append(sequenceWithFeatures);
                allSequencesWithFeatures.append("\n\n");
            }
            
            String allRes = label(allSequencesWithFeatures.toString());
System.out.println(allRes);
            results = resultExtractionLayoutTokens(allRes, inputsTokens);

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
        return results;
    }

    private boolean nameLabel(String label) {
        return label.endsWith("<surname>") || label.endsWith("<forename>") || label.endsWith("<middlename>");
    }

    private List<List<Pair<Person,Affiliation>>> resultExtractionLayoutTokens(String allRes, 
                                                        List<List<LayoutToken>> inputsTokens) {
        if (CollectionUtils.isEmpty(inputsTokens)) {
            return null;
        }

        List<List<Pair<Person,Affiliation>>> results = new ArrayList<>();
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
            List<Pair<Person,Affiliation>> localResults = new ArrayList<>();
            try {
                TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.NAMES_ADDRESS, res, tokens);
                Person aut = new Person();
                Person lastAut = null;
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
                    if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_TITLE)) {
                        if (aut.getTitle() != null) {
                            if (aut.notNull()) {
                                aut.normalizeName();
                                if (aff.isNotEmptyAffiliation() || aff.hasAddress()) {
                                    aut.addAffiliation(aff);
                                    aff = new Affiliation();
                                }
                                localResults.add(Pair.of(aut, null));
                                lastAut = aut;
                            }
                            aut = new Person();
                            aut.setTitle(clusterContent);
                        } else {
                            aut.setTitle(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_FORENAME)) {
                        if (aut.getFirstName() != null) {
                            // new author
                            if (aut.notNull()) {
                                aut.normalizeName();
                                if (aff.isNotEmptyAffiliation() || aff.hasAddress()) {
                                    aut.addAffiliation(aff);
                                    aff = new Affiliation();
                                }
                                localResults.add(Pair.of(aut, null));
                                lastAut = aut;
                            }
                            aut = new Person();
                            aut.setFirstName(clusterContent);
                        } else {
                            aut.setFirstName(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_MIDDLENAME)) {
                        if (aut.getMiddleName() != null) {
                            aut.setMiddleName(aut.getMiddleName() + " " + clusterContent);
                        } else {
                            aut.setMiddleName(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_SURNAME)) {
                        if (aut.getLastName() != null) {
                            // new author
                            if (aut.notNull()) {
                                aut.normalizeName();
                                if (aff.isNotEmptyAffiliation() || aff.hasAddress()) {
                                    aut.addAffiliation(aff);
                                    aff = new Affiliation();
                                }
                                localResults.add(Pair.of(aut, null));
                                lastAut = aut;
                            }
                            aut = new Person();
                            aut.setLastName(clusterContent);
                        } else {
                            aut.setLastName(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_SUFFIX)) {
                        if (aut.getSuffix() != null) {
                            aut.setSuffix(aut.getSuffix() + " " + clusterContent);
                        } else {
                            aut.setSuffix(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_INSTITUTION)) {
                        if (aff.getInstitutions() != null && aff.getInstitutions().size()>0) {
                            // new affiliation
                            if (aff.isNotEmptyAffiliation() || aff.hasAddress()) {
                                if (aut != null && aut.notNull()) {
                                    aut.addAffiliation(aff);
                                } else if (lastAut != null && lastAut.notNull()) {
                                    lastAut.addAffiliation(aff);
                                } else {
                                    localResults.add(Pair.of(null, aff));
                                }
                            } 
                            aff = new Affiliation();
                            aff.addInstitution(clusterContent);
                        } else {
                            aff.addInstitution(clusterContent);
                        }
                        aff.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_DEPARTMENT)) {
                        if (aff.getDepartments() != null && aff.getDepartments().size()>0) {
                            aff.addDepartment(clusterContent);
                        } else {
                            aff.addDepartment(clusterContent);
                        }
                        aff.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_ADDRESS_COUNTRY)) {
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

                // add last built author
                if (aut.notNull()) {
                    aut.normalizeName();
                    localResults.add(Pair.of(aut, null));
                }

                // add last built affiliation
                if (aff.isNotEmptyAffiliation() || aff.hasAddress()) {
                    if (aut.notNull()) {
                        aut.addAffiliation(aff);
                    } else {
                        localResults.add(Pair.of(null, aff));
                    }
                } 
            } catch (Exception e) {
                throw new GrobidException("An exception occurred while running Grobid.", e);
            }

            results.add(localResults);
            i++;
        }

        return results;
    }

    /**
     * Extract results from a list of name strings in the training format without any string modification.
	 *
	 * @param input - the sequence of author names to be processed as a string.
	 * @return the pseudo-TEI training data
	 */
    public StringBuilder trainingExtraction(String input) {
        if (StringUtils.isEmpty(input))
            return null;
        // force analyser with English, to avoid bad surprise
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input, new Language("en", 1.0));
        StringBuilder buffer = new StringBuilder();
        try {
            if (CollectionUtils.isEmpty(tokens)) {
                return null;
            }

            List<OffsetPosition> placesPositions = lexicon.tokenPositionsLocationNames(tokens);
                List<OffsetPosition> countriesPositions = lexicon.tokenPositionsCountryNames(tokens);
            List<OffsetPosition> titlePositions = Lexicon.getInstance().tokenPositionsPersonTitle(tokens);
            List<OffsetPosition> suffixPositions = Lexicon.getInstance().tokenPositionsPersonSuffix(tokens);

            String sequence = FeaturesVectorNameAddress.addFeaturesNameAddress(
                    tokens, null, placesPositions, countriesPositions, titlePositions, suffixPositions);
            if (StringUtils.isEmpty(sequence))
                return null;
            String res = label(sequence);

            // extract results from the processed file
            StringTokenizer st2 = new StringTokenizer(res, "\n");
            String lastTag = null;
            boolean start = true;
            boolean hasMarker = false;
            boolean hasSurname = false;
            boolean hasForename = false;
            boolean tagClosed;
            int q = 0;
            boolean addSpace;
            String lastTag0;
            String currentTag0;
            while (st2.hasMoreTokens()) {
                String line = st2.nextToken();
                addSpace = false;
                if ((line.trim().length() == 0)) {
                    // new author	
                	buffer.append("/t<author>\n");
                    continue;
                } else {
                    String theTok = tokens.get(q).getText();
                    while (theTok.equals(" ") || theTok.equals("\n")) {
                        addSpace = true;
                        q++;
                        theTok = tokens.get(q).getText();
                    }
                    q++;
                }

                StringTokenizer st3 = new StringTokenizer(line, "\t");
                int ll = st3.countTokens();
                int i = 0;
                String s1 = null;
                String s2 = null;
                boolean newLine = false;
                List<String> localFeatures = new ArrayList<String>();
                while (st3.hasMoreTokens()) {
                    String s = st3.nextToken().trim();
                    if (i == 0) {
                        s2 = TextUtilities.HTMLEncode(s); // string
                    } else if (i == ll - 2) {
                    } else if (i == ll - 1) {
                        s1 = s; // label
                    } else {
                        localFeatures.add(s);
                        if (s.equals("LINESTART") && !start) {
                            newLine = true;
                            start = false;
                        } else if (s.equals("LINESTART")) {
                            start = false;
                        }
                    }
                    i++;
                }

                lastTag0 = null;
                if (lastTag != null) {
                    if (lastTag.startsWith("I-")) {
                        lastTag0 = lastTag.substring(2, lastTag.length());
                    } else {
                        lastTag0 = lastTag;
                    }
                }
                currentTag0 = null;
                if (s1 != null) {
                    if (s1.startsWith("I-")) {
                        currentTag0 = s1.substring(2, s1.length());
                    } else {
                        currentTag0 = s1;
                    }
                }

                tagClosed = lastTag0 != null && testClosingTag(buffer, currentTag0, lastTag0);

                if (newLine) {
                    if (tagClosed) {
                        buffer.append("\t\t\t\t\t\t\t<lb/>\n");
                    } else {
                        buffer.append("<lb/>");
                    }

                }

                String output = writeField(s1, lastTag0, s2, "<marker>", "<marker>", addSpace, 8);
                if (output != null) {
                    if (hasMarker) {
                        buffer.append("\t\t\t\t\t\t\t</persName>\n");
                        hasForename = false;
                        hasSurname = false;
                        buffer.append("\t\t\t\t\t\t\t<persName>\n");
                        hasMarker = true;
                    }
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                } else {
                    output = writeField(s1, lastTag0, s2, "<other>", "<other>", addSpace, 8);
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<forename>", "<forename>", addSpace, 8);
                } else {
                    if (buffer.length() > 0) {
                        if (buffer.charAt(buffer.length() - 1) == '\n') {
                            buffer.deleteCharAt(buffer.length() - 1);
                        }
                    }
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<middlename>", "<middlename>", addSpace, 8);
                } else {
                    if (hasForename && !currentTag0.equals(lastTag0)) {
                        buffer.append("\t\t\t\t\t\t\t</persName>\n");
                        hasMarker = false;
                        hasSurname = false;
                        buffer.append("\t\t\t\t\t\t\t<persName>\n");
                    }
                    hasForename = true;
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<surname>", "<surname>", addSpace, 8);
                } else {
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<title>", "<roleName>", addSpace, 8);
                } else {
                    if (hasSurname && !currentTag0.equals(lastTag0)) {
                        buffer.append("\t\t\t\t\t\t\t</persName>\n");
                        hasMarker = false;
                        hasForename = false;
                        buffer.append("\t\t\t\t\t\t\t<persName>\n");
                    }
                    hasSurname = true;
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<suffix>", "<suffix>", addSpace, 8);
                } else {
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output != null) {
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }

                lastTag = s1;
            }

            if (lastTag != null) {
                if (lastTag.startsWith("I-")) {
                    lastTag0 = lastTag.substring(2, lastTag.length());
                } else {
                    lastTag0 = lastTag;
                }
                currentTag0 = "";
                testClosingTag(buffer, currentTag0, lastTag0);
            }
        } catch (Exception e) {
//			e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return buffer;
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