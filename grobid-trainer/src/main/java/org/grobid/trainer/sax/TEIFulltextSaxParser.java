package org.grobid.trainer.sax;

import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * SAX parser for the TEI format for fulltext data. Normally all training data should be in this unique format.
 * The segmentation of tokens must be identical as the one from pdf2xml files so that
 * training and online input tokens are aligned.
 *
 * @author Patrice Lopez
 */
public class TEIFulltextSaxParser extends DefaultHandler {

    //private Stack<StringBuffer> accumulators = null; // accumulated parsed piece of texts
    private StringBuffer accumulator = null; // current accumulated text

    private String output = null;
    private Stack<String> currentTags = null;

    //private String fileName = null;
    //private String pdfName = null;

    private boolean figureBlock = false;

    private ArrayList<String> labeled = null; // store line by line the labeled data

    public TEIFulltextSaxParser() {
        labeled = new ArrayList<String>();
        currentTags = new Stack<String>();
        //accumulators = new Stack<StringBuffer>();
        accumulator = new StringBuffer();
    }

    public void characters(char[] buffer, int start, int length) {
        //if (accumulator != null)
        accumulator.append(buffer, start, length);
    }

    public String getText() {
        if (accumulator != null) {
            //System.out.println(accumulator.toString().trim());
            return accumulator.toString().trim();
        } else {
            return null;
        }
    }

    public ArrayList<String> getLabeledResult() {
        return labeled;
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {
        if ((!qName.equals("lb")) & (!qName.equals("pb") & (!qName.equals("figure")))) {
            writeData(qName, true);
        }

        if (qName.equals("figure")) {
            figureBlock = false;
        }
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {
        if (qName.equals("lb")) {
            accumulator.append(" +L+ ");
        } else if (qName.equals("pb")) {
            accumulator.append(" +PAGE+ ");
        } else if (qName.equals("space")) {
            accumulator.append(" ");
        } else if (qName.equals("figure")) {
            figureBlock = true;
        } else {
            // we have to write first what has been accumulated yet with the upper-level tag
            String text = getText();
            if (text != null) {
                if (text.length() > 0) {
                    writeData(qName, false);
                }
            }
            accumulator.setLength(0);

            if (qName.equals("div")) {
                int length = atts.getLength();

                // Process each attribute
                for (int i = 0; i < length; i++) {
                    // Get names and values for each attribute
                    String name = atts.getQName(i);
                    String value = atts.getValue(i);

                    if (name != null) {
                        if (name.equals("type")) {
                            if (value.equals("paragraph")) {
                                currentTags.push("<paragraph>");
                            }
                        }
                    }
                }
            } else if (qName.equals("p")) {
                currentTags.push("<paragraph>");
            } else if (qName.equals("front")) {
                currentTags.push("<header>");
            } else if (qName.equals("other")) {
                currentTags.push("<other>");
            } else if (qName.equals("ref")) {
                int length = atts.getLength();

                // Process each attribute
                for (int i = 0; i < length; i++) {
                    // Get names and values for each attribute
                    String name = atts.getQName(i);
                    String value = atts.getValue(i);

                    if (name != null) {
                        if (name.equals("type")) {
                            if (value.equals("biblio")) {
                                currentTags.push("<citation_marker>");
                            } else if (value.equals("figure")) {
                                currentTags.push("<figure_marker>");
                            }
                        }
                    }
                }
            } else if (qName.equals("note")) {
                int length = atts.getLength();

                // Process each attribute
                for (int i = 0; i < length; i++) {
                    // Get names and values for each attribute
                    String name = atts.getQName(i);
                    String value = atts.getValue(i);

                    if (name != null) {
                        if (name.equals("place")) {
                            if (value.equals("footnote")) {
                                currentTags.push("<page_footnote>");
                            }
                            if (value.equals("headnote")) {
                                currentTags.push("<page_header>");
                            }
                        }
                    }
                }
            } else if (qName.equals("formula")) {
                currentTags.push("<equation>");
            } else if (qName.equals("page") | qName.equals("pages")) {
                currentTags.push("<page>");
            } else if (qName.equals("head")) {
                if (figureBlock) {
                    currentTags.push("<figure_head>");
                } else {
                    currentTags.push("<section>");
                }
            } else if (qName.equals("figDesc")) {
                currentTags.push("<label>");
            } else if (qName.equals("label")) {
                currentTags.push("<reference_marker>");
            } else if (qName.equals("bibl")) {
                currentTags.push("<reference>");
            }
            /*else if (qName.equals("subsection")) {
                   currentTags.push("<subsection>");
               }
               else if (qName.equals("subsubsection")) {
                   currentTags.push("<subsubsection>");
               }*/
            else if (qName.equals("table")) {
                currentTags.push("<table>");
            } else if (qName.equals("item")) {
                currentTags.push("<item>");
            } else if (qName.equals("trash")) {
                currentTags.push("<trash>");
            }
            /*else if (qName.equals("fileDesc")) {
                   int length = atts.getLength();

                   // Process each attribute
                   for (int i=0; i<length; i++) {
                       // Get names and values for each attribute
                       String name = atts.getQName(i);
                       String value = atts.getValue(i);

                       if (name != null) {
                           if (name.equals("xml:id")) {
                               //pdfName = value;
                           }
                       }
                   }
               }*/
        }
    }

    private void writeData(String qName, boolean pop) {
        if ((qName.equals("front")) || (qName.equals("other")) || (qName.equals("note")) ||
                (qName.equals("ref")) || (qName.equals("page")) || (qName.equals("pages")) ||
                (qName.equals("bibl")) || (qName.equals("figure_head")) ||
                (qName.equals("label")) || (qName.equals("p")) || (qName.equals("paragraph")) ||
                (qName.equals("head")) || (qName.equals("div")) || (qName.equals("figDesc")) ||
                (qName.equals("table")) || (qName.equals("trash")) ||
                (qName.equals("formula")) || (qName.equals("item"))
                ) {
            String currentTag = null;
            if (pop) {
                currentTag = currentTags.pop();
            } else {
                currentTag = currentTags.peek();
            }
            String text = getText();
            // we segment the text
            StringTokenizer st = new StringTokenizer(text, " \n\t" + TextUtilities.fullPunctuations, true);
            boolean begin = true;
            while (st.hasMoreTokens()) {
                String tok = st.nextToken().trim();
                if (tok.length() == 0) continue;

                if (tok.equals("+L+")) {
                    labeled.add("@newline\n");
                } else if (tok.equals("+PAGE+")) {
                    // page break should be a distinct feature
                    labeled.add("@newpage\n");
                } else {
                    String content = tok;
                    int i = 0;
                    if (content.length() > 0) {
                        if (begin) {
                            labeled.add(content + " I-" + currentTag + "\n");
                            begin = false;
                        } else {
                            labeled.add(content + " " + currentTag + "\n");
                        }
                    }
                }
                begin = false;
            }
            accumulator.setLength(0);
        }
    }

}