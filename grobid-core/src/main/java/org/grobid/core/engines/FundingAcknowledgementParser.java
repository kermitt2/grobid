package org.grobid.core.engines;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Funding;
import org.grobid.core.data.Funder;
import org.grobid.core.data.Person;
import org.grobid.core.data.Affiliation;
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
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.core.engines.config.GrobidAnalysisConfig;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.Builder;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.MutableTriple;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.grobid.core.engines.label.TaggingLabels.*;
import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;
import static org.grobid.core.document.xml.XmlBuilderUtils.addXmlId;
import static org.grobid.core.document.xml.XmlBuilderUtils.textNode;

public class FundingAcknowledgementParser extends AbstractParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundingAcknowledgementParser.class);

    protected FundingAcknowledgementParser() {
        super(GrobidModels.FUNDING_ACKNOWLEDGEMENT);
    }

    private MutablePair<Element, MutableTriple<List<Funding>,List<Person>,List<Affiliation>>>
        processing(List<LayoutToken> tokenizationFunding, GrobidAnalysisConfig config) {
        if (tokenizationFunding == null || tokenizationFunding.size() == 0)
            return null;
        String res;
        try {
            String featureVector = FeaturesVectorFunding.addFeatures(tokenizationFunding, null);
            res = label(featureVector);
            //System.out.println(res);
        } catch (Exception e) {
            throw new GrobidException("CRF labeling with table model fails.", e);
        }

        if (res == null) {
            return null;
        }
        return getExtractionResult(tokenizationFunding, res);
    }

    /**
     * For convenience, a processing method taking a raw string as input. 
     * Tokenization is done with the default Grobid analyzer triggered by the identified language. 
     **/
    public MutablePair<Element, MutableTriple<List<Funding>,List<Person>,List<Affiliation>>> processing(String text,
                               GrobidAnalysisConfig config) {
        text = UnicodeUtil.normaliseText(text);
        List<LayoutToken> tokenizationFunding = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        return processing(tokenizationFunding, config);
    }

    /**
     * For convenience, a processing method taking an TEI XML segment as input - only paragraphs (Element p) 
     * will be processed in this segment and paragraph element will be replaced with the processed content.
     * Resulting entities are relative to the whole procssed XML segment.
     * 
     * Tokenization is done with the default Grobid analyzer triggered by the identified language. 
     **/
    public MutablePair<Element, MutableTriple<List<Funding>,List<Person>,List<Affiliation>>> processingXmlFragment(String tei,
                               GrobidAnalysisConfig config) {
        Builder parser = new Builder();
        MutablePair<Element, MutableTriple<List<Funding>,List<Person>,List<Affiliation>>> globalResult = null;
        try {
            tei = tei.replace(" xmlns=\"http://www.tei-c.org/ns/1.0\"", "");

            //System.out.println(tei);
            Document localDoc = parser.build(tei, null);

            // get the paragraphs
            Element root = localDoc.getRootElement();
            Nodes paragraphs = root.query("//p");

            for(Node paragraph : paragraphs) {
                String paragraphText = paragraph.getValue();
                List<LayoutToken> tokenizationFunding = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(paragraphText);

                MutablePair<Element, MutableTriple<List<Funding>,List<Person>,List<Affiliation>>> localResult = processing(tokenizationFunding, config);
                
                // replace paragraph content
                if (localResult.getLeft() != null && localResult.getLeft().getChildCount()>0) {
                    ((Element) paragraph).removeChildren();
                    for (int i = localResult.getLeft().getChildCount()-1; i >=0; i--) {
                        Node localNode = localResult.getLeft().getChild(i);
                        localNode.detach();
                        ((Element) paragraph).insertChild(localNode, 0);
                    }
                }
                // update extracted entities
                if (globalResult == null) {
                    globalResult = MutablePair.of(root, localResult.getRight());
                } else {
                    // concatenate members of the local results to the global ones
                    MutableTriple<List<Funding>,List<Person>,List<Affiliation>> localEntities = localResult.getRight();
                    MutableTriple<List<Funding>,List<Person>,List<Affiliation>> globalEntities = globalResult.getRight();

                    List<Funding> localFundings = localEntities.getLeft();
                    List<Funding> globalFundings = globalEntities.getLeft();
                    globalFundings.addAll(localFundings);
                    globalEntities.setLeft(globalFundings);

                    List<Person> localPersons = localEntities.getMiddle();
                    List<Person> globalPersons = globalEntities.getMiddle();
                    globalPersons.addAll(localPersons);
                    globalEntities.setMiddle(globalPersons);

                    List<Affiliation> localAffiliation = localEntities.getRight();
                    List<Affiliation> globalAffiliations = globalEntities.getRight();
                    globalAffiliations.addAll(localAffiliation);
                    globalEntities.setRight(globalAffiliations);

                    globalResult.setRight(globalEntities);
                }
            }

            //System.out.println(globalResult.getLeft().toXML());
        } catch(ValidityException exp) {
            LOGGER.warn("Invalid TEI fragment from funding/acknowledgement section", exp);
        } catch(ParsingException exp) {
            LOGGER.warn("Parsing error of the TEI fragment from funding/acknowledgement section", exp);
        } catch(IOException exp) {
            LOGGER.warn("Input TEI fragment invalid from funding/acknowledgement section", exp);
        } 
        
        return globalResult;
    }


    /**
     * The processing here is called from the header and/or full text parser in cascade
     * when one of these higher-level model detect a "funding" section, or in case
     * no funding section is found, when a acknolwedgements section is detected.
     * 
     * Independently from the place this parser is called, it process the input sequence 
     * of layout tokens in a context free manner. 
     * 
     * The expected input here is a paragraph.
     * 
     * Return an XML fragment with inline annotations of the input text, together with 
     * extracted normalized entities. These entities are referenced by the inline 
     * annotations with the usual @target attribute pointing to xml:id. 
     */
    private MutablePair<Element, MutableTriple<List<Funding>,List<Person>,List<Affiliation>>>
            getExtractionResult(List<LayoutToken> tokenizations, String result) {
        List<Funding> fundings = new ArrayList<>();
        List<Person> persons = new ArrayList<>();
        List<Affiliation> affiliations = new ArrayList<>();
        List<Affiliation> institutions = new ArrayList<>();

        // current funding
        Funding funding = new Funding();

        // current person
        Person person = new Person();
        
        // current organization
        Affiliation affiliation = new Affiliation();
        Affiliation institution = new Affiliation();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FUNDING_ACKNOWLEDGEMENT, result, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();
        TaggingLabel previousLabel = null;

        Element curParagraph = teiElement("p");
        List<Node> curParagraphNodes = new ArrayList<>();
        int posTokenization = 0;

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            boolean spaceBefore = false;
            if (posTokenization > 0 && tokenizations.size()>=posTokenization && tokenizations.get(posTokenization-1).getText().equals(" ")) {
                spaceBefore = true;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i(clusterLabel);

            List<LayoutToken> tokens = cluster.concatTokens();
            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(tokens));   

            if (clusterLabel.equals(FUNDING_FUNDER_NAME)) {
                Funder localFunder = funding.getFunder();
                if (localFunder == null) {
                    localFunder = new Funder();
                    funding.setFunder(localFunder);
                }

                if (StringUtils.isNotBlank(localFunder.getFullName())) {
                    if (funding.isValid()) {
                        fundings.add(funding);
                        // next funding object
                        funding = new Funding();
                        localFunder = new Funder();
                        funding.setFunder(localFunder);
                    }
                }

                localFunder.setFullName(clusterContent);
                localFunder.appendFullNameLayoutTokens(tokens);
                localFunder.addLayoutTokens(tokens);
                funding.addLayoutTokens(tokens);

                Element entity = teiElement("rs");
                entity.addAttribute(new Attribute("type", "funder"));
                entity.appendChild(clusterContent);

                if (spaceBefore)
                    curParagraphNodes.add(textNode(" "));
                curParagraphNodes.add(entity);

            } else if (clusterLabel.equals(FUNDING_GRANT_NAME)) {
                if (StringUtils.isNotBlank(funding.getGrantName())) {
                    if (funding.isValid()) {
                        fundings.add(funding);
                        // next funding object
                        funding = new Funding();
                    }
                }

                funding.setGrantName(clusterContent);
                funding.appendGrantNameLayoutTokens(tokens);
                funding.addLayoutTokens(tokens);

                Element entity = teiElement("rs");
                entity.addAttribute(new Attribute("type", "grantName"));
                entity.appendChild(clusterContent);

                if (spaceBefore)
                    curParagraphNodes.add(textNode(" "));
                curParagraphNodes.add(entity);

            } else if (clusterLabel.equals(FUNDING_PERSON)) {
                if (StringUtils.isNotBlank(person.getRawName())) {
                    if (person.isValid()) {
                        persons.add(person);
                        // next funding object
                        person = new Person();
                    }
                }

                person.setRawName(clusterContent);
                person.appendLayoutTokens(tokens);

                Element entity = teiElement("rs");
                entity.addAttribute(new Attribute("type", "person"));
                entity.appendChild(clusterContent);

                if (spaceBefore)
                    curParagraphNodes.add(textNode(" "));
                curParagraphNodes.add(entity);

            } else if (clusterLabel.equals(FUNDING_AFFILIATION)) {
                if (StringUtils.isNotBlank(affiliation.getAffiliationString())) {
                    if (affiliation.notNull()) {
                        affiliations.add(affiliation);
                        // next funding object
                        affiliation = new Affiliation();
                    }
                }

                affiliation.setRawAffiliationString(clusterContent);
                affiliation.appendLayoutTokens(tokens);

                Element entity = teiElement("rs");
                entity.addAttribute(new Attribute("type", "affiliation"));
                entity.appendChild(clusterContent);

                if (spaceBefore)
                    curParagraphNodes.add(textNode(" "));
                curParagraphNodes.add(entity);

            } else if (clusterLabel.equals(FUNDING_INSTITUTION)) {
                if (StringUtils.isNotBlank(institution.getAffiliationString())) {
                    if (institution.notNull()) {
                        institutions.add(institution);
                        // next funding object
                        institution = new Affiliation();
                    }
                }

                institution.setRawAffiliationString(clusterContent);
                institution.appendLayoutTokens(tokens);

                Element entity = teiElement("rs");
                entity.addAttribute(new Attribute("type", "institution"));
                entity.appendChild(clusterContent);

                if (spaceBefore)
                    curParagraphNodes.add(textNode(" "));
                curParagraphNodes.add(entity);

            } else if (clusterLabel.equals(FUNDING_GRANT_NUMBER)) {
                Funding previousFounding = null;
                if (StringUtils.isNotBlank(funding.getGrantNumber())) {
                    if (funding.isValid()) {
                        previousFounding = funding;
                        fundings.add(funding);
                        // next funding object
                        funding = new Funding();
                    }
                }

                funding.setGrantNumber(clusterContent);
                funding.appendGrantNumberLayoutTokens(tokens);
                funding.addLayoutTokens(tokens);

                // possibly copy funder from previous funding object (case of "factorization" of grant numbers)
                if (previousFounding != null && 
                    previousFounding.getGrantNumber() != null && 
                    clusterContent.length() == previousFounding.getGrantNumber().length()) {
                    funding.setFunder(previousFounding.getFunder());
                }

                Element entity = teiElement("rs");
                entity.addAttribute(new Attribute("type", "grantNumber"));
                entity.appendChild(clusterContent);

                if (spaceBefore)
                    curParagraphNodes.add(textNode(" "));
                curParagraphNodes.add(entity);

            } else if (clusterLabel.equals(FUNDING_PROGRAM_NAME)) {
                if (StringUtils.isNotBlank(funding.getProgramFullName())) {
                    if (funding.isValid()) {
                        fundings.add(funding);
                        // next funding object
                        funding = new Funding();
                    }
                }

                funding.setProgramFullName(clusterContent);
                funding.appendProgramFullNameLayoutTokens(tokens);
                funding.addLayoutTokens(tokens);

                Element entity = teiElement("rs");
                entity.addAttribute(new Attribute("type", "programName"));
                entity.appendChild(clusterContent);

                if (spaceBefore)
                    curParagraphNodes.add(textNode(" "));
                curParagraphNodes.add(entity);

            } else if (clusterLabel.equals(FUNDING_PROJECT_NAME)) {
                if (StringUtils.isNotBlank(funding.getProjectFullName())) {
                    if (funding.isValid()) {
                        fundings.add(funding);
                        // next funding object
                        funding = new Funding();
                    }
                }

                funding.setProjectFullName(clusterContent);
                funding.appendProjectFullNameLayoutTokens(tokens);
                funding.addLayoutTokens(tokens);

                Element entity = teiElement("rs");
                entity.addAttribute(new Attribute("type", "projectName"));
                entity.appendChild(clusterContent);

                if (spaceBefore)
                    curParagraphNodes.add(textNode(" "));
                curParagraphNodes.add(entity);

            } else if (clusterLabel.equals(FUNDING_OTHER)) {
                if (spaceBefore)
                    curParagraphNodes.add(textNode(" "));
                curParagraphNodes.add(textNode(clusterContent));
            } else {
                LOGGER.warn("Unexpected funding model label - " + clusterLabel.getLabel() + " for " + clusterContent);
            }

            previousLabel = clusterLabel;
            posTokenization += tokens.size(); 
        }

        for (Node n : curParagraphNodes) {
            curParagraph.appendChild(n);
        }

        // last funding, person, institution/affiliation
        if (funding.isValid())
            fundings.add(funding);

        if (institutions != null && institutions.size() > 0)
            affiliations.addAll(institutions);

        for(Funding localFunding : fundings) {
            localFunding.inferAcronyms();
        }

        MutableTriple<List<Funding>,List<Person>,List<Affiliation>> entities = MutableTriple.of(fundings, persons, affiliations);

        return MutablePair.of(curParagraph, entities);
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