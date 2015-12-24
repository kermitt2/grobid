package org.grobid.trainer.sax;

import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * SAX parser for the XML format for citation data. Normally all training data should be in this unique format which
 * replaces the ugly CORA format. Segmentation of tokens must be identical as the one from pdf2xml files to that
 * training and online input tokens are identical.
 * <p/>
 * This is unfortunately not yet TEI...
 *
 * @author Patrice Lopez
 */
public class TEICitationSaxParser extends DefaultHandler {

    private StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text
    private StringBuffer allContent = new StringBuffer();

    private String output = null;
    private String currentTag = null;

    private ArrayList<String> labeled = null; // store line by line the labeled data
    public int nbCitations = 0;
    public Lexicon lexicon = Lexicon.getInstance();

    public List<List<OffsetPosition>> journalsPositions = null;
    public List<List<OffsetPosition>> abbrevJournalsPositions = null;
    public List<List<OffsetPosition>> conferencesPositions = null;
    public List<List<OffsetPosition>> publishersPositions = null;

    public TEICitationSaxParser() {
        labeled = new ArrayList<String>();
        journalsPositions = new ArrayList<List<OffsetPosition>>();
        abbrevJournalsPositions = new ArrayList<List<OffsetPosition>>();
        conferencesPositions = new ArrayList<List<OffsetPosition>>();
        publishersPositions = new ArrayList<List<OffsetPosition>>();
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

    public ArrayList<String> getLabeledResult() {
        return labeled;
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {

        if ((qName.equals("author")) || (qName.equals("authors")) || (qName.equals("orgName")) ||
                (qName.equals("title")) || (qName.equals("editor")) || (qName.equals("editors")) ||
                (qName.equals("booktitle")) || (qName.equals("date")) || (qName.equals("journal")) ||
                (qName.equals("institution")) || (qName.equals("tech")) || (qName.equals("volume")) ||
                (qName.equals("pages")) || (qName.equals("page")) || (qName.equals("pubPlace")) ||
                (qName.equals("note")) || (qName.equals("web")) || (qName.equals("pages")) ||
                (qName.equals("publisher")) || (qName.equals("idno") || qName.equals("issue")) ||
                (qName.equals("pubnum")) || (qName.equals("biblScope")) || (qName.equals("ptr")) ||
                (qName.equals("keyword")) || (qName.equals("keywords"))
                ) {
            String text = getText();
            writeField(text);
        } else if (qName.equals("lb")) {
            // we note a line break
            accumulator.append(" +L+ ");
        } else if (qName.equals("pb")) {
            accumulator.append(" +PAGE+ ");
        } else if (qName.equals("bibl")) {
            String text = getText();
            if (text.length() > 0) {
                currentTag = "<other>";
                writeField(text);
            }
            labeled.add("\n \n");
            nbCitations++;

            String allString = allContent.toString();
            journalsPositions.add(lexicon.inJournalNames(allString));
            abbrevJournalsPositions.add(lexicon.inAbbrevJournalNames(allString));
            conferencesPositions.add(lexicon.inConferenceNames(allString));
            publishersPositions.add(lexicon.inPublisherNames(allString));
            allContent = null;
            allString = null;
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

        if (qName.equals("title")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if ((name != null) && (value != null)) {
                    if (name.equals("level")) {
                        if (value.equals("a")) {
                            currentTag = "<title>";
                        } else if (value.equals("j")) {
                            currentTag = "<journal>";
                        } else if (value.equals("m")) {
                            currentTag = "<booktitle>";
                        }
                    }
                }
            }
        } else if ((qName.equals("author")) || (qName.equals("authors"))) {
            currentTag = "<author>";
        } else if (qName.equals("editor")) {
            currentTag = "<editor>";
        } else if (qName.equals("date")) {
            currentTag = "<date>";
        } else if ((qName.equals("keywords")) || (qName.equals("keyword"))) {
            currentTag = "<keyword>";
        } else if (qName.equals("orgName")) {
            currentTag = "<institution>";
        } else if (qName.equals("note")) {
            int length = atts.getLength();

            if (length == 0) {
                currentTag = "<note>";
            } else {
                // Process each attribute
                for (int i = 0; i < length; i++) {
                    // Get names and values for each attribute
                    String name = atts.getQName(i);
                    String value = atts.getValue(i);

                    if ((name != null) && (value != null)) {
                        if (name.equals("type")) {
                            if (value.equals("report")) {
                                currentTag = "<tech>";
                            }
                        }
                    }
                }
            }
        } else if (qName.equals("biblScope")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if ((name != null) && (value != null)) {
                    if (name.equals("type") || name.equals("unit")) {
                        if ((value.equals("vol")) || (value.equals("volume"))) {
                            currentTag = "<volume>";
                        } else if ((value.equals("issue")) || (value.equals("number"))) {
                            currentTag = "<issue>";
                        }
                        if (value.equals("pp") || value.equals("page")) {
                            currentTag = "<pages>";
                        }
                    }
                }
            }
        } else if (qName.equals("pubPlace")) {
            currentTag = "<location>";
        } else if (qName.equals("publisher")) {
            currentTag = "<publisher>";
        } else if (qName.equals("ptr")) {
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if ((name != null) && (value != null)) {
                    if (name.equals("type")) {
                        if (value.equals("web")) {
                            currentTag = "<web>";
                        }
                    }
                }
            }
        } else if (qName.equals("idno") || qName.equals("pubnum")) {
            currentTag = "<pubnum>";
        } else if (qName.equals("bibl")) {
            accumulator = new StringBuffer();
            allContent = new StringBuffer();
        }
        accumulator.setLength(0);
    }

    private void writeField(String text) {
        // we segment the text
        List<String> tokens = TextUtilities.segment(text, "[(" + TextUtilities.punctuations);

        boolean begin = true;
        //while(st.hasMoreTokens()) {
        for (String tok : tokens) {
            //String tok = st.nextToken().trim();
            tok = tok.trim();
            if (tok.length() == 0) continue;
            boolean punct1 = false;

            if (tok.equals("+L+")) {
                labeled.add("@newline\n");
            } else if (tok.equals("+PAGE+")) {
                // page break not relevant for authors
                labeled.add("@newline\n");
            } else {
                String content = tok;
                int i = 0;
                for (; i < TextUtilities.punctuations.length(); i++) {
                    if (tok.length() > 0) {
                        if (tok.charAt(tok.length() - 1) == TextUtilities.punctuations.charAt(i)) {
                            punct1 = true;
                            content = tok.substring(0, tok.length() - 1);
                            break;
                        }
                    }
                }
                if (tok.length() > 0) {
                    if ((tok.startsWith("(")) & (tok.length() > 1)) {
                        if (punct1)
                            content = tok.substring(1, tok.length() - 1);
                        else
                            content = tok.substring(1, tok.length());
                        if (begin) {
                            labeled.add("(" + " I-" + currentTag + "\n");
                            begin = false;
                        } else {
                            labeled.add("(" + " " + currentTag + "\n");
                        }
                    } else if ((tok.startsWith("[")) & (tok.length() > 1)) {
                        if (punct1)
                            content = tok.substring(1, tok.length() - 1);
                        else
                            content = tok.substring(1, tok.length());
                        if (begin) {
                            labeled.add("[" + " I-" + currentTag + "\n");
                            begin = false;
                        } else {
                            labeled.add("[" + " " + currentTag + "\n");
                        }
                    } else if ((tok.startsWith("\"")) & (tok.length() > 1)) {
                        if (punct1)
                            content = tok.substring(1, tok.length() - 1);
                        else
                            content = tok.substring(1, tok.length());
                        if (begin) {
                            labeled.add("\"" + " I-" + currentTag + "\n");
                            begin = false;
                        } else {
                            labeled.add("\"" + " " + currentTag + "\n");
                        }
                    }
                }

                if (content.length() > 0) {
                    if (begin) {
                        labeled.add(content + " I-" + currentTag + "\n");
                        begin = false;
                    } else {
                        labeled.add(content + " " + currentTag + "\n");
                    }
                }

                if (punct1) {
                    if (begin) {
                        labeled.add(tok.charAt(tok.length() - 1) + " I-" + currentTag + "\n");
                        begin = false;
                    } else {
                        labeled.add(tok.charAt(tok.length() - 1) + " " + currentTag + "\n");
                    }
                }
            }

            begin = false;
        }
    }


}