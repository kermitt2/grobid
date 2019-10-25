package org.grobid.trainer.sax;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.UnicodeUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * created by Tanti, 2019
 * SAX parser for the TEI format for the training data for the acknowledgment model.
 * <p>
 * TEI -> label mapping (9 labels for this model)
 * affiliation (<affiliation>): affiliation of individual,
 * educational institution (<educationalInstitution>): educational institution,
 * funding agency (<fundingAgency>): funding agency,
 * grant name (<grantName>): grant name,
 * grant number (<grantNumber>): grant number,
 * individual (<individual>): gratitude for individuals,
 * other institution (<otherInstitution>): name of institution other than a research institution or university,
 * project name (<projectName>): project name,
 * research institution (<researchInstitution>): name of research institution
 */

public class TEIAcknowledgmentSaxParser extends DefaultHandler {

    private StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text
    private StringBuffer allContent = new StringBuffer();

    private String currentTag = null;

    private List<String> labeled = null; // store token by token the labels
    private List<List<String>> allLabeled = null; // list of labels
    private List<LayoutToken> tokens = null;
    private List<List<LayoutToken>> allTokens = null; // list of layoutToken

    public int nbAcknowledgments = 0;

    public TEIAcknowledgmentSaxParser() {
        labeled = new ArrayList<>();
        tokens = new ArrayList<>();
        allTokens = new ArrayList<>();
        allLabeled = new ArrayList<>();
    }

    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
        if (allContent != null) {
            allContent.append(buffer, start, length);
        }
    }

    public String getText() {
        return accumulator.toString().trim();
    }

    public List<String> getLabeledResult() {
        return labeled;
    }

    public List<List<String>> getLabeledResults() {

        return allLabeled;
    }

    public List<List<LayoutToken>> getTokenResults() {
        return allTokens;
    }

    public List<LayoutToken> getTokenResult() {
        return tokens;
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {

        if (((qName.equals("affiliation")) || (qName.equals("educationalInstitution")) || (qName.equals("fundingAgency"))
            || (qName.equals("grantName")) || (qName.equals("grantNumber")) || (qName.equals("individual"))
            || (qName.equals("otherInstitution")) || (qName.equals("projectName")) || (qName.equals("researchInstitution") && (currentTag != null))))
        {
            String text = getText();
            writeField(text);
        } else if (qName.equals("lb")) {
            accumulator.append(" +L+ ");
        } else if (qName.equals("pb")) {
            accumulator.append(" +PAGE+ ");
        } else if (qName.equals("acknowledgment")) {
            String text = getText();
            if (text.length() > 0) {
                currentTag = "<other>";
                writeField(text);
            }

            nbAcknowledgments++;
            labeled.add("\n \n");
            allLabeled.add(labeled);
            allTokens.add(tokens);
            allContent = null;
        }
        accumulator.setLength(0);
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
        throws SAXException {

        String text = getText();
        if (text.length() > 0) {
            currentTag = "<other>";
            writeField(text);
        }
        accumulator.setLength(0);

        if (qName.equals("affiliation")) {
            currentTag = "<affiliation>";
        } else if (qName.equals("educationalInstitution")) {
            currentTag = "<educationalInstitution>";
        } else if (qName.equals("fundingAgency")) {
            currentTag = "<fundingAgency>";
        } else if (qName.equals("grantName")) {
            currentTag = "<grantName>";
        } else if (qName.equals("grantNumber")) {
            currentTag = "<grantNumber>";
        } else if (qName.equals("individual")) {
            currentTag = "<individual>";
        } else if (qName.equals("otherInstitution")) {
            currentTag = "<otherInstitution>";
        } else if (qName.equals("projectName")) {
            currentTag = "<projectName>";
        } else if (qName.equals("researchInstitution")) {
            currentTag = "<researchInstitution>";
        } else if (qName.equals("acknowledgment")) {
            accumulator = new StringBuffer();
            allContent = new StringBuffer();
            labeled = new ArrayList<>();
            tokens = new ArrayList<>();
        }
        accumulator.setLength(0);
    }

    private void writeField(String text) {
        if (tokens == null){
            return;
        }

        // segment the text into tokens
        List<LayoutToken> localTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        // if the tokens are empty
        if (isEmpty(localTokens)){
            localTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text, new Language("en", 1.0)); // force to tokenize with English tokenizer
        }

        // if the tokens are still empty, then ignore the process
        if (isEmpty(localTokens)){
            return;
        }

        boolean begin = true;

        for (LayoutToken token : localTokens) {
            tokens.add(token);
            String content = token.getText();
            if (content.equals(" ") || content.equals("\n")) {
                labeled.add(null);
                continue;
            }

            content = UnicodeUtil.normaliseTextAndRemoveSpaces(content);
            if (content.trim().length() == 0) {
                labeled.add(null);
                continue;
            }

            if (content.length() > 0) {
                if (begin) {
                    labeled.add("I-" + currentTag);
                    begin = false;
                } else {
                    labeled.add(currentTag);
                }
            }
        }
    }
}
