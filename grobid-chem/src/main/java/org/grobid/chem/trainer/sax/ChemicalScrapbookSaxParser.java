package org.grobid.trainer.sax;

import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.grobid.core.exceptions.GrobidException; 

import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.List;

/**
 * SAX parser for the chemical patent corpus in scrapbook format, i.e. with xml inline annotations.
 *
 * @author Patrice Lopez
 */
public class ChemicalScrapbookSaxParser extends DefaultHandler {

    //private Stack<StringBuffer> accumulators = null; // accumulated parsed piece of texts
    private StringBuffer accumulator = null; // current accumulated text

    private String currentTags = null;
    private List<String> labeled = null; // store line by line the labeled data

    public ChemicalScrapbookSaxParser() {
        labeled = new ArrayList<String>();
        accumulator = new StringBuffer();
    }

    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    public String getText() {
        if (accumulator != null) {
            //System.out.println(accumulator.toString().trim());
            return accumulator.toString().trim();
        } 
		else {
            return null;
        }
    }

    public List<String> getLabeledResult() {
        return labeled;
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {
//System.out.println(qName); 
//System.out.println(currentTags); 
		writeData();
		currentTags = "<other>";
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {
//System.out.println(qName);      
        // we have to write first what has been accumulated yet with the upper-level tag
        String text = getText();
        if (text != null) {
            if (text.length() > 0) {
				currentTags = "<other>";
                writeData();
            }
        }
        accumulator.setLength(0);
		currentTags = "<other>";
		
		if (qName.equals("ne")) {
			int length = atts.getLength();			
            
			// Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);
				
                if (name != null) {
                    if (name.equals("type")) {
                        if (value.equals("CLASS") || value.equals("class")) {
                            currentTags = "<chemical>";
                        }
						else if (value.equals("CHEMICAL") || value.equals("chemical")) {
                            currentTags = "<chemical>";
                        }
						else if (value.equals("ONT") || value.equals("ont")) {
                            currentTags = "<chemical>";
                        }
						else if (value.equals("LIGAND") || value.equals("ligand")) {
                            currentTags = "<chemical>";
                        }
						else if (value.equals("FORMULA") || value.equals("formula")) {
                            currentTags = "<chemical>";
                        }
                    }
                }
            }
        }		
    }

    private void writeData() {
        String text = getText();
        // we segment the text
        StringTokenizer st = new StringTokenizer(text, " \n\t" + TextUtilities.fullPunctuations, true);
        boolean begin = true;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken().trim();
            if (tok.length() == 0) continue;

            String content = tok;
            int i = 0;
            if (content.length() > 0) {
                if (begin && !currentTags.equals("<other>")) {
                    labeled.add(content + " I-" + currentTags + "\n");
                    begin = false;
                } else {
                    labeled.add(content + " " + currentTags + "\n");
                }
            }

            begin = false;
        }
        accumulator.setLength(0);
    }

}