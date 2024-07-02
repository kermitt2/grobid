package org.grobid.core.engines;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

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
            List<String> allSequencesWithFeatures = new ArrayList<>();
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
                allSequencesWithFeatures.add(sequenceWithFeatures);
            }
            
            String allRes = label(allSequencesWithFeatures);
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

        List<List<Pair<Person,Affiliation>>> results = null;
        List<Person> authors = null;
        List<Affiliation> affiliations = null;

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
            try {
                TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.NAMES_ADDRESS, res, tokens);
                org.grobid.core.data.Person aut = new Person();
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
                    if (clusterLabel.equals(TaggingLabels.NAMES_HEADER_TITLE) || 
                                clusterLabel.equals(TaggingLabels.NAMES_CITATION_TITLE)) {
                        if (aut.getTitle() != null) {
                            if (aut.notNull()) {
                                if (fullAuthors == null)
                                    fullAuthors = new ArrayList<Person>();
                                fullAuthors.add(aut);
                            }
                            aut = new Person();
                            aut.setTitle(clusterContent);
                        } else {
                            aut.setTitle(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_HEADER_FORENAME) || 
                                clusterLabel.equals(TaggingLabels.NAMES_CITATION_FORENAME)) {
                        if (aut.getFirstName() != null) {
                            // new author
                            if (aut.notNull()) {
                                if (fullAuthors == null)
                                    fullAuthors = new ArrayList<Person>();
                                fullAuthors.add(aut);
                            }
                            aut = new Person();
                            aut.setFirstName(clusterContent);
                        } else {
                            aut.setFirstName(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_HEADER_MIDDLENAME) || 
                                clusterLabel.equals(TaggingLabels.NAMES_CITATION_MIDDLENAME)) {
                        if (aut.getMiddleName() != null) {
                            aut.setMiddleName(aut.getMiddleName() + " " + clusterContent);
                        } else {
                            aut.setMiddleName(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_HEADER_SURNAME) || 
                                clusterLabel.equals(TaggingLabels.NAMES_CITATION_SURNAME)) {
                        if (aut.getLastName() != null) {
                            // new author
                            if (aut.notNull()) {
                                if (fullAuthors == null)
                                    fullAuthors = new ArrayList<Person>();
                                fullAuthors.add(aut);
                            }
                            aut = new Person();
                            aut.setLastName(clusterContent);
                        } else {
                            aut.setLastName(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    } else if (clusterLabel.equals(TaggingLabels.NAMES_HEADER_SUFFIX) || 
                                clusterLabel.equals(TaggingLabels.NAMES_CITATION_SUFFIX)) {
                        if (aut.getSuffix() != null) {
                            aut.setSuffix(aut.getSuffix() + " " + clusterContent);
                        } else {
                            aut.setSuffix(clusterContent);
                        }
                        aut.appendLayoutTokens(cluster.concatTokens());
                    }
                }

                // add last built author
                if (aut.notNull()) {
                    if (fullAuthors == null) {
                        fullAuthors = new ArrayList<Person>();
                    }
                    fullAuthors.add(aut);
                }

                // some more person name normalisation
                if (fullAuthors != null) {
                    for(Person author : fullAuthors) {
                        author.normalizeName();
                    }
                } 
            } catch (Exception e) {
                throw new GrobidException("An exception occurred while running Grobid.", e);
            }

        }

        return results;
    }

    /**
     * Extract results from a list of name strings in the training format without any string modification.
	 *
	 * @param input - the sequence of author names to be processed as a string.
	 * @param head - if true use the model for header's name, otherwise the model for names in citation
	 * @return the pseudo-TEI training data
	 */
    public StringBuilder trainingExtraction(String input,
                                            boolean head) {
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
					if (head)
                    	buffer.append("/t<author>\n");
					else {
						//buffer.append("<author>");
					}
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

                tagClosed = lastTag0 != null && testClosingTag(buffer, currentTag0, lastTag0, head);

                if (newLine) {
                    if (tagClosed) {
                        buffer.append("\t\t\t\t\t\t\t<lb/>\n");
                    } else {
                        buffer.append("<lb/>");
                    }

                }

                String output = writeField(s1, lastTag0, s2, "<marker>", "<marker>", addSpace, 8, head);
                if (output != null) {
                    if (hasMarker) {
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t</persName>\n");
                        } else {
                            //buffer.append("</author>\n");
                        }
                        hasForename = false;
                        hasSurname = false;
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t<persName>\n");
                        } else {
                            //buffer.append("<author>\n");
                        }
                        hasMarker = true;
                    }
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                } else {
                    output = writeField(s1, lastTag0, s2, "<other>", "<other>", addSpace, 8, head);
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<forename>", "<forename>", addSpace, 8, head);
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
                    output = writeField(s1, lastTag0, s2, "<middlename>", "<middlename>", addSpace, 8, head);
                } else {
                    if (hasForename && !currentTag0.equals(lastTag0)) {
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t</persName>\n");
                        } else {
                            //buffer.append("</author>\n");
                        }
                        hasMarker = false;
                        hasSurname = false;
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t<persName>\n");
                        } else {
                            //buffer.append("<author>\n");
                        }
                    }
                    hasForename = true;
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<surname>", "<surname>", addSpace, 8, head);
                } else {
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<title>", "<roleName>", addSpace, 8, head);
                } else {
                    if (hasSurname && !currentTag0.equals(lastTag0)) {
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t</persName>\n");
                        } else {
                            //buffer.append("</author>\n");
                        }
                        hasMarker = false;
                        hasForename = false;
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t<persName>\n");
                        } else {
                            //buffer.append("<author>\n");
                        }
                    }
                    hasSurname = true;
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<suffix>", "<suffix>", addSpace, 8, head);
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
                testClosingTag(buffer, currentTag0, lastTag0, head);
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
                              int nbIndent, 
							  boolean head) {
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
				if (head) {
	                for (int i = 0; i < nbIndent; i++) {
	                    result += "\t";
	                }
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
                                   String lastTag0,
								   boolean head) {
        boolean res = false;
        if (!currentTag0.equals(lastTag0)) {
            res = true;
            // we close the current tag
            if (lastTag0.equals("<other>")) {
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<forename>")) {
                buffer.append("</forename>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<middlename>")) {
                buffer.append("</middlename>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<surname>")) {
                buffer.append("</surname>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<title>")) {
                buffer.append("</roleName>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<suffix>")) {
                buffer.append("</suffix>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<marker>")) {
                buffer.append("</marker>");
				if (head)
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