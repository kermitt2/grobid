package org.grobid.trainer.sax;

import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.analyzers.*;
import org.grobid.core.lang.Language;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * SAX parser handler for address sequences encoded in the TEI format data.
 *
 * @author Patrice Lopez
 */
public class TEIAddressSaxHandler extends DefaultHandler {

    private StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text
    private StringBuffer allContent = new StringBuffer();

    private String output = null;
    private String currentTag = null;

    private List<String> labeled = null; // store token by token the labels
    private List<List<String>> allLabeled = null; // list of labels
    private List<LayoutToken> tokens = null;
    private List<List<LayoutToken>> allTokens = null; // list of LayoutToken segmentation

    public int n = 0;
    public Lexicon lexicon = Lexicon.getInstance();

    public TEIAddressSaxHandler() {
        allTokens = new ArrayList<List<LayoutToken>>();
        allLabeled = new ArrayList<List<String>>();
    }

    public void characters(char[] buffer, int start, int length) {
        StringBuffer localBuffer = new StringBuffer();
        localBuffer.append(buffer, start, length);
        String localText = localBuffer.toString();
        localText = localText.replace("\n\t", " ");
        localText = localText.replaceAll("( )+", " ");
        accumulator.append(localText);
    }

    public String getText() {
        return accumulator.toString().trim();
    }

    public List<List<String>> getLabeledResult() {
        return allLabeled;
    }

    public List<List<LayoutToken>> getAllTokens() {
        return allTokens;
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {
        if (
                qName.equals("addrLine") ||
                qName.equals("settlement") ||
                qName.equals("region") ||
                qName.equals("postCode") || qName.equals("postcode") ||
                qName.equals("postBox") ||  qName.equals("postbox") ||
                qName.equals("country") 
        ) {
            String text = getText();
            if (text.length() > 0) {
                writeField(text);
            }
            accumulator.setLength(0);
        } else if (qName.equals("lb") || qName.equals("pb")) {
            // we note a line break
            //accumulator.append(" @newline ");
            accumulator.append("\n");
        } else if (qName.equals("address")) {
            String text = getText();
            if (text.length() > 0) {
                currentTag = "<other>";
                writeField(text);
            }

            allLabeled.add(labeled);
            allTokens.add(tokens);

            accumulator.setLength(0);
        } else {
            accumulator.setLength(0);
        }
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {
        if (!qName.equals("lb") && !qName.equals("pb")) {
            String text = getText();
            if (text.length() > 0) {
                currentTag = "<other>";
                writeField(text);
            }
            accumulator.setLength(0);
        }

        if (qName.equals("address")) {
            accumulator = new StringBuffer();
            labeled = new ArrayList<>();
            tokens = new ArrayList<>();
            currentTag = null;
            accumulator.setLength(0);
            n++;
        } else if (qName.equals("addrLine") || qName.equals("addrline")) {
            currentTag = "<addrLine>";
        } else if (qName.equals("settlement")) {
            currentTag = "<settlement>";
        } else if (qName.equals("region")) {
            currentTag = "<region>";
        } else if (qName.equals("postCode") || qName.equals("postcode")) {
            currentTag = "<postCode>";
        } else if (qName.equals("postBox") || qName.equals("postbox")) {
            currentTag = "<postBox>";
        } else if (qName.equals("country")) {
            currentTag = "<country>";
        } else if (!qName.equals("analytic") && !qName.equals("biblStruct") && 
            !qName.equals("sourceDesc") && !qName.equals("fileDesc") && !qName.equals("address") && 
            !qName.equals("teiHeader") && !qName.equals("TEI") && !qName.equals("teiCorpus") && 
            !qName.equals("persName") && !qName.equals("tei") && !qName.equals("lb") &&
            !qName.equals("affilliation") && !qName.equals("orgname") && !qName.equals("orgName") &&
            !qName.equals("forename") && !qName.equals("surname") && !qName.equals("author") && 
            !qName.equals("middlename") && !qName.equals("affiliation") && !qName.equals("title")&& 
            !qName.equals("roleName") && !qName.equals("suffix") && !qName.equals("firstname") && 
            !qName.equals("rolename")) {
            System.out.println("Warning, invalid tag: <" + qName + ">");
        }
    }

    private void writeField(String text) {
        // we segment the text
        List<LayoutToken> localTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        if ( (localTokens == null) || (localTokens.size() == 0) )
            localTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text, new Language("en", 1.0));
        if  ( (localTokens == null) || (localTokens.size() == 0) )
            return;

        boolean begin = true;
        for (LayoutToken token : localTokens) {
            if (tokens == null) {
                // should not be the case, it can indicate a structure problem in the training XML file
                tokens = new ArrayList<LayoutToken>();
                System.out.println("Warning: list of LayoutToken not initialized properly, parsing continue... ");
            }
            if (labeled == null) {
                // should not be the case, it can indicate a structure problem in the training XML file
                labeled = new ArrayList<String>();
                System.out.println("Warning: list of labels not initialized properly, parsing continue... ");
            }
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

            begin = false;
        }
    }

}
