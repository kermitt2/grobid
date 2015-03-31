package org.grobid.trainer.sax;

import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * SAX parser for the TEI format for the training data for the segmentation model.
 * Normally all training data should be in this unique format.
 * The segmentation of tokens must be identical as the one from pdf2xml files so that
 * training and online input tokens are aligned.
 *
 * @author Patrice Lopez
 */
public class TEISegmentationSaxParser extends DefaultHandler {

  	/* TEI -> label mapping (9 labels for this model) 
 		cover page (<cover>): titlePage (optionally under front), 
		document header (<header>): front, 
		page footer (<footnote>): note type footnote, 
		page header (<headnote>): note type headnote, 
		document body (<body>): body, 
		bibliographical section (<references>): listbibl, 
		page number (<page>): page,
		? each bibliographical references in the biblio section (<ref>): bibl 
		annexes (<annex>): div type="annex" (optionally under back)
		acknowledgement (<acknowledgement>): div type="acknowledgement" (optionally under back)
 	*/

    private StringBuffer accumulator = null; // current accumulated text

    private String output = null;
    //private Stack<String> currentTags = null;
	private String currentTag = null;
	private String upperQname = null;
	private String upperTag = null;
    private List<String> labeled = null; // store line by line the labeled data

    public TEISegmentationSaxParser() {
        labeled = new ArrayList<String>();
        //currentTags = new Stack<String>();
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
        if ((!qName.equals("lb")) && (!qName.equals("pb") )) {
            writeData(qName, currentTag);
        }
		if (qName.equals("body") || 
			qName.equals("cover") || 
			qName.equals("header") || 
			qName.equals("div") || 
			qName.equals("listBibl")) {
			currentTag = null;
			upperTag = null;
		}
		else if (qName.equals("note") || 
				 qName.equals("page") || 
				 qName.equals("pages") || 
				 qName.equals("titlePage") ) {
			currentTag = upperTag;
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
        } else {
            // we have to write first what has been accumulated yet with the upper-level tag
            String text = getText();
            if (text != null) {
                if (text.length() > 0) {
                    writeData(upperQname, upperTag);
                }
            }
            accumulator.setLength(0);

            if (qName.equals("front")) {
                //currentTags.push("<header>");
				currentTag = "<header>";
				upperTag = currentTag;
				upperQname = "front";
            } else if (qName.equals("body")) {
	                //currentTags.push("<other>");
				currentTag = "<body>";
				upperTag = currentTag;
				upperQname = "body";
	        }
			else if (qName.equals("titlePage")) {
                //currentTags.push("<other>");
				currentTag = "<cover>";
				//upperTag = currentTag;
				//upperQname = "titlePage";
            }
			/*else if (qName.equals("other")) {
                //currentTags.push("<other>");
				currentTag = "<other>";
            } */
			/*else if (qName.equals("ref")) {
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
            } */
			else if (qName.equals("note")) {
                int length = atts.getLength();

                // Process each attribute
                for (int i = 0; i < length; i++) {
                    // Get names and values for each attribute
                    String name = atts.getQName(i);
                    String value = atts.getValue(i);

                    if (name != null) {
                        if (name.equals("place")) {
                            if (value.equals("footnote")) {
								currentTag = "<footnote>";
                            }
                            if (value.equals("headnote")) {
								currentTag = "<headnote>";
                            }
                        }
                    }
                }
            } 
			else if (qName.equals("div")) {
                int length = atts.getLength();

                // Process each attribute
                for (int i = 0; i < length; i++) {
                    // Get names and values for each attribute
                    String name = atts.getQName(i);
                    String value = atts.getValue(i);

                    if (name != null) {
                        if (name.equals("type")) {
                            if (value.equals("annex")) {
								currentTag = "<annex>";
								upperTag = currentTag;
								upperQname = "div";
                            }
                            else if (value.equals("acknowledgement")) {
								currentTag = "<acknowledgement>";
								upperTag = currentTag;
								upperQname = "div";
                            }
                        }
                    }
                }
            }
			else if (qName.equals("page") || qName.equals("pages")) {
				currentTag = "<page>";
            }
			else if (qName.equals("listBibl")) {
				currentTag = "<references>";
				upperTag = currentTag;
				upperQname = "listBibl";
            }
        }
    }

    private void writeData(String qName, String surfaceTag) {
        if ((qName.equals("front")) || (qName.equals("titlePage")) || (qName.equals("note")) ||
                (qName.equals("page")) || (qName.equals("pages")) || (qName.equals("body")) ||
                (qName.equals("listBibl")) || 
                (qName.equals("div")) 
                ) {
            String text = getText();
			boolean begin = true;
//System.out.println(text);			
            // we segment the text line by line first
            //StringTokenizer st = new StringTokenizer(text, "\n", true);
			String[] tokens = text.split("\\+L\\+");
			//while (st.hasMoreTokens()) {
			boolean page = false;
			for(int p=0; p<tokens.length; p++) {	
				//String line = st.nextToken().trim();
				String line = tokens[p].trim();
				if (line.equals("\n"))
					continue;
				if (line.length() == 0) 
					continue;
				if (line.indexOf("+PAGE+") != -1) {
                    // page break should be a distinct feature
                    //labeled.add("@newpage\n");
					line = line.replace("+PAGE+", "");
					page = true;
                } 
				
				StringTokenizer st = new StringTokenizer(line, " \t");
				if (!st.hasMoreTokens()) 
					continue;
				String tok = st.nextToken();

                //String tok = line.replace(" ", "").replace("\t", "");
				//if (tok.length() > 10)
				//	tok = tok.substring(0,10);
				
	            //StringTokenizer st2 = new StringTokenizer(text, " \t" + TextUtilities.fullPunctuations, true);
	            
	            //if (st2.hasMoreTokens()) {
	                //String tok = st2.nextToken().trim();
	            if (tok.length() == 0) continue;

	                //if (tok.equals("+L+")) {
	                //    labeled.add("@newline\n");
	                //} else 
					
	                    //if (tok.length() > 0) {
	        	if (begin) {
	        		labeled.add(tok + " I-" + surfaceTag + "\n");
	  			  	begin = false;
	         	} else {
	           	 	labeled.add(tok + " " + surfaceTag + "\n");
	            }
				if (page) {
					labeled.add("@newpage\n");
					page = false;
				}
							//}
			}
            accumulator.setLength(0);
        }
    }
	
    /*private void writeData2(String qName, String surfaceTag) {
        if ((qName.equals("front")) || (qName.equals("titlePage")) || (qName.equals("note")) ||
                (qName.equals("page")) || (qName.equals("pages")) || (qName.equals("body")) ||
                (qName.equals("listBibl")) || 
                (qName.equals("div")) 
                ) {				
            String text = getText();
			if (surfaceTag == null) {
				System.err.println("Warning label is null for text: " + text);
			}	
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
                            labeled.add(content + " I-" + surfaceTag + "\n");
                            begin = false;
                        } else {
                            labeled.add(content + " " + surfaceTag + "\n");
                        }
                    }
                }
                //begin = false;
            }
            accumulator.setLength(0);
        }
    }*/

}