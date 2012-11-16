package org.grobid.core.sax;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Stupid SAX parser which accumulate the textual content.
 * <p/>
 * As an option, it is possible to accumulate only the content under a given
 * element name, for instance "description" for getting the description of a
 * patent XML document.
 * 
 * @author Patrice Lopez
 */
public class TextSaxParser extends DefaultHandler {

	StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text

	private String filter = null; // the name of an element for getting only the
									// corresponding text

	private boolean accumule = true;

	public String currentPatentNumber = null;
	public String country = null;

	public TextSaxParser() {
	}

	public void characters(char[] buffer, int start, int length) {
		if (accumule) {
			accumulator.append(buffer, start, length);
		}
	}

	public void setFilter(String filt) {
		filter = filt;
		accumule = false;
	}

	public String getText() {
		String text = accumulator.toString().trim();
		text = text.replace("\n", " ");
		text = text.replace("\t", " ");
		text = text.replaceAll("\\p{Space}+", " ");
		return text;
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals(filter)) {
			accumule = false;
		}
		if (accumule) {
			if (qName.equals("row") || qName.equals("p")) {
				accumulator.append(" ");
			}
		}
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (qName.equals("patent-document")) {
			int length = atts.getLength();

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if (name != null) {
					if (name.equals("country")) {
						country = value;
					}
					if (name.equals("doc-number")) {
						currentPatentNumber = country + value;
					}
				}
			}
		}

		if (qName.equals(filter)) {
			accumule = true;
		}
	}

}
