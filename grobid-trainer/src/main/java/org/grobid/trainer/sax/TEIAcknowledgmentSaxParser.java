package org.grobid.trainer.sax;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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

    private String output = null;
    private String currentTag = null;

    private List<String> labeled = null; // store line by line the labeled data
    private List<List<String>> allLabeled = null;
    private List<LayoutToken> tokens = null;
    private List<List<LayoutToken>> allTokens = null;

    public int nbAcknowledgments = 0;

    public TEIAcknowledgmentSaxParser() {
        allTokens = new ArrayList<>();
        allLabeled = new ArrayList<>();
        tokens = new ArrayList<>();
        labeled = new ArrayList<String>();
    }

    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
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
        if (((qName.equals("affiliation")) | (qName.equals("educationalInstitution")) | (qName.equals("fundingAgency"))
            | (qName.equals("grantName")) | (qName.equals("grantNumber")) | (qName.equals("individual"))
            | (qName.equals("otherInstitution")) | (qName.equals("projectName")) | (qName.equals("researchInstitution"))
        )) {
            String text = getText();
            writeField(text);
            if (allContent != null) {
                if (allContent.length() != 0) {
                    allContent.append(" ");
                }
                allContent.append(text);
            }
            accumulator.setLength(0);
        } else if (qName.equals("lb")) {
            accumulator.append(" +L+ ");
        } else if (qName.equals("pb")) {
            accumulator.append(" +PAGE+ ");
        } else if (qName.equals("acknowledgment")) {
            String text = getText();
            if (text.length() > 0) {
                currentTag = "<other>";
                writeField(text);
                if (allContent != null) {
                    allContent.append(" ");
                }
                allContent.append(text);
            }
            labeled.add("\n \n");

            String allString = allContent.toString().trim();
            allString = allString.replace("@newline", "\n");
            List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(allString);
            allString = null;

            nbAcknowledgments++;

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
            if (allContent != null) {
                if (allContent.length() != 0) {
                    allContent.append(" ");
                }
                allContent.append(text);
            }
        }
        accumulator.setLength(0);
        qName = qName.toLowerCase();

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
        currentTag = null;
        accumulator.setLength(0);
    }

    private void writeField(String text) {
        // we segment the text
        //List<String> tokens = TextUtilities.segment(text, TextUtilities.punctuations);
        //StringTokenizer st = new StringTokenizer(text, " \n\t" + TextUtilities.fullPunctuations, true);
        List<String> tokens = GrobidAnalyzer.getInstance().tokenize(text); // utilize Grobid analyzer for segmenting the text
        boolean begin = true;

       /* while (st.hasMoreTokens()) {
            String tok = st.nextToken().trim();*/

        for (String tok : tokens) {
            tok = tok.trim();

            // if the token is empty just continue
            if (tok.length() == 0) {
                continue;
            }

            // if the token is the newline break
            if (tok.equals("+L+") || tok.equals("@newline")) {
                labeled.add("@newline");
            } else if (tok.equals("+PAGE+")) {
                // page break not relevant for authors
                labeled.add("@newline");
            } else {
                String content = tok;
                int i = 0;
                if (content.length() > 0) {
                    if (begin) {
                        labeled.add(content + " I-" + currentTag);
                        begin = false;
                    } else {
                        labeled.add(content + " " + currentTag);
                    }
                }
            }

            begin = false;
        }
    }
}
