package org.grobid.trainer.sax;

import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * SAX parser for the TEI format for fulltext data encoded for training. Normally all training data should 
 * be in this unique format for the fulltext model.
 * The segmentation of tokens must be identical as the one from pdf2xml files so that
 * training and online input tokens are aligned.
 *
 * @author Patrice Lopez
 */
public class TEIFulltextSaxParser extends DefaultHandler {

    private StringBuffer accumulator = null; // current accumulated text

    private String output = null;
    private Stack<String> currentTags = null;
	private String currentTag = null;
	
    private boolean figureBlock = false;
	private boolean tableBlock = false;

    private ArrayList<String> labeled = null; // store line by line the labeled data

    public TEIFulltextSaxParser() {
        labeled = new ArrayList<String>();
        currentTags = new Stack<String>();
        accumulator = new StringBuffer();
    }

    public void characters(char[] buffer, int start, int length) {
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

    public List<String> getLabeledResult() {
        return labeled;
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {
		if ( (!qName.equals("lb")) && (!qName.equals("pb")) ) {
            writeData(qName, true);
			if (!currentTags.empty()) {
				currentTag = currentTags.peek();
			}
        }

        if (qName.equals("figure")) {
            figureBlock = false;
			tableBlock = false;
        }
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {
        if (qName.equals("lb")) {
            accumulator.append(" +L+ ");
        } 
		else if (qName.equals("pb")) {
            accumulator.append(" +PAGE+ ");
        } 
		else if (qName.equals("space")) {
            accumulator.append(" ");
        } 
		else {
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
								currentTag = "<paragraph>";
                            }
                        }
                    }
                }
            } 
			else if (qName.equals("p") ) {
                currentTags.push("<paragraph>");
				currentTag = "<paragraph>";
            } 
			else if (qName.equals("other")) {
                currentTags.push("<other>");
				currentTag = "<other>";
            } 
			else if (qName.equals("ref")) {
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
								currentTag = "<citation_marker>";
                            } else if (value.equals("figure")) {
                                currentTags.push("<figure_marker>");
								currentTag = "<figure_marker>";
                            }
							else if (value.equals("table")) {
								currentTags.push("<table_marker>");
								currentTag = "<table_marker>";
							}
                        }
                    }
                }
            } 
			else if (qName.equals("formula") || qName.equals("label")) {
                currentTags.push("<equation>");
				currentTag = "<equation>";
            } 
			else if (qName.equals("head")) {
                /*if (figureBlock) {
                    currentTags.push("<figure_head>");
					currentTag = "<figure_head>";
                }
				else*/ 
				{
                    currentTags.push("<section>");
					currentTag = "<section>";
                }
            } 
			/*else if (qName.equals("figDesc")) {
                currentTags.push("<figDesc>");
				currentTag = "<figDesc>";
            }*/
            else if (qName.equals("table")) {
                currentTags.push("<table>");
				currentTag = "<table>";
            } 
			else if (qName.equals("item")) {
                currentTags.push("<paragraph>");
				currentTag = "<paragraph>";
            } 
			/*else if (qName.equals("label")) {
                currentTags.push("<label>");
				currentTag = "<label>";
            } */
			/*else if (qName.equals("trash")) {
                currentTags.push("<trash>");
				currentTag = "<trash>";
            }*/
			else if (qName.equals("figure")) {
	            figureBlock = true;
	            int length = atts.getLength();

	            // Process each attribute
	            for (int i = 0; i < length; i++) {
	                // Get names and values for each attribute
	                String name = atts.getQName(i);
	                String value = atts.getValue(i);

	                if (name != null) {
	                    if (name.equals("type")) {
	                        if (value.equals("table")) {
	                            tableBlock = true;
	                        }
	                    }
	                }
	            }
				if (tableBlock) {
					figureBlock = false;
	                currentTags.push("<table>");
					currentTag = "<table>";
				}
				else {
	                currentTags.push("<figure>");
					currentTag = "<figure>";
				}
	        } 
			else {
                currentTags.push("<other>");
				currentTag = "<other>";
			}
        }
		
    }

    private void writeData(String qName, boolean pop) {
        if ( (qName.equals("other")) || 
                (qName.equals("ref")) || (qName.equals("head")) || (qName.equals("figure")) || 
                (qName.equals("paragraph")) ||
                (qName.equals("div")) || //(qName.equals("figDesc")) ||
                (qName.equals("table")) || //(qName.equals("trash")) ||
                (qName.equals("formula")) || (qName.equals("item")) || (qName.equals("label"))
                ) {
			if (currentTag == null) {
				return;
			}
	
            if (pop) {
				if (!currentTags.empty()) {
					currentTags.pop();
				}
            }

			// adjust tag (conservative)
			if (tableBlock) {
				currentTag = "<table>";
			}
			else if (figureBlock) {
				currentTag = "<figure>";
			}

            String text = getText();
            // we segment the text
            StringTokenizer st = new StringTokenizer(text, " \n\t" + TextUtilities.fullPunctuations, true);
            boolean begin = true;
            while (st.hasMoreTokens()) {
                String tok = st.nextToken().trim();
                if (tok.length() == 0) 
					continue;

                if (tok.equals("+L+")) {
                    labeled.add("@newline\n");
                } 
				else if (tok.equals("+PAGE+")) {
                    // page break should be a distinct feature
                    labeled.add("@newpage\n");
                }
				else {
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