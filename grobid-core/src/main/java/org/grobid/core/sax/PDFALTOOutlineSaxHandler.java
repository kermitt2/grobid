package org.grobid.core.sax;

import org.grobid.core.document.DocumentNode;
import org.grobid.core.document.Document;
import org.grobid.core.layout.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;

/**
 *  SAX parser for ALTO XML representation of the outline/bookmark present in PDF files 
 *  obtained via pdfalto. 
 * 
 */
public class PDFALTOOutlineSaxHandler extends DefaultHandler {
	public static final Logger LOGGER = LoggerFactory.getLogger(PDFALTOOutlineSaxHandler.class);
	
	private StringBuilder accumulator = new StringBuilder(); // Accumulate parsed text
	private Document doc = null;
	private DocumentNode root = null;
	private DocumentNode currentNode = null;
	
	private String label = null;
	private BoundingBox box = null;

	private int currentLevel = -1;
	private int currentId = -1;

	private Deque<Integer> currentParentIdStack = new ArrayDeque<>();

	private Map<Integer,DocumentNode> nodes = null;

	public PDFALTOOutlineSaxHandler(Document doc) {
		this.doc = doc;
	}
	
	public void characters(char[] ch, int start, int length) {
		accumulator.append(ch, start, length);
	}

	public String getText() {
		return accumulator.toString().trim();
	}
	
	public DocumentNode getRootNode() {
		return root;
	}
	
	public void endElement(java.lang.String uri, java.lang.String localName,
			java.lang.String qName) throws SAXException {

		if (qName.equals("STRING")) {
		    currentNode.setLabel(getText());
		} else if (qName.equals("ITEM")) {
		    //The box could come from a nested element
		    if (box != null) {
                currentNode.setBoundingBox(box);
		    }

            box = null;
            label = null;
		} else if (qName.equals("TOCITEMLIST")) {
            if (!currentParentIdStack.isEmpty()) {
                currentParentIdStack.pop();
            } else {
                LOGGER.warn("TOCITEMLIST end encountered with empty parent stack. Possible malformed outline structure.");
            }
        } else if (qName.equals("LINK")) {
		    // in case of nested item, we need to assign the box right away or we will lose it.
            if (box != null) {
                currentNode.setBoundingBox(box);
            }
            box = null;
        }
	}
	
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (qName.equals("TOCITEMS")) {
			// this is the document root
			root = new DocumentNode();
			nodes = new HashMap<>();
		} else if (qName.equals("ITEM")) {
			currentNode = new DocumentNode();
			// get the node id 
			int length = atts.getLength();

			// Process attributes
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equalsIgnoreCase("id")) {
						try {
							currentId = Integer.parseInt(value);
						} catch(Exception e) {
							LOGGER.warn("Invalid id string (should be an integer): " + value);
							currentId = -1;
						}
					}
				}
			}
			currentNode.setId(currentId);
			nodes.put(currentId,currentNode);
			if (!currentParentIdStack.isEmpty()) {
				int currentParentId = currentParentIdStack.peek();
				DocumentNode father = nodes.get(currentParentId);
                if (father == null)
					LOGGER.warn("Father not yet encountered! id is " + currentParentId);
                else {
				    currentNode.setFather(father);
				    father.addChild(currentNode);
                }
			} else {
				// parent is the root node
				currentNode.setFather(root);
				root.addChild(currentNode);
			}

		} else if (qName.equals("TOCITEMLIST")) {
			// we only consider annotation with attribute @subtype of value "Link" 
			int length = atts.getLength();

			// Process attributes
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("level")) {
						try {
							currentLevel = Integer.parseInt(value);
						} catch(Exception e) {
							LOGGER.warn("Invalid level string (should be an integer): " + value);
							currentLevel = -1;
						}
					} else if (name.equals("idItemParent")) {
						try {
							int parentId = Integer.parseInt(value);
							currentParentIdStack.push(parentId);
						} catch(Exception e) {
							LOGGER.warn("Invalid parent id string (should be an integer): " + value);
							currentParentIdStack.push(-1);
						}
					}
				}
			}
            System.out.println("TOCITEMLIST: level=" + currentLevel + ", parentId=" + currentParentIdStack.peek());
		} else if (qName.equals("LINK")) {
			int length = atts.getLength();

			int page = -1;
			double top = -1.0;
			double bottom = -1.0;
			double left = -1.0;
			double right = -1.0;

			// Process attributes
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("page")) {
						try {
							page = Integer.parseInt(value);
						} catch(Exception e) {
							LOGGER.error("The value for page coordinate attribute is not a valid int: " + value);
						}
					} else if (name.equals("top")) {
						double val = -1.0;
						try {
							val = Double.parseDouble(value);
						} catch(Exception e) {
							LOGGER.error("The value for top coordinate attribute is not a valid double: " + value);
						}
						if (val != -1.0) {
							top = val;
						}
					} else if (name.equals("bottom")) {
						double val = -1.0;
						try {
							val = Double.parseDouble(value);
						} catch(Exception e) {
							LOGGER.error("The value for bottom coordinate attribute is not a valid double: " + value);
						}
						if (val != -1.0) {
							bottom = val;
						}
					} else if (name.equals("left")) {
						double val = -1.0;
						try {
							val = Double.parseDouble(value);
						} catch(Exception e) {
							LOGGER.error("The value for left coordinate attribute is not a valid double: " + value);
						}
						if (val != -1.0) {
							left = val;
						}
					} else if (name.equals("right")) {
						double val = -1.0;
						try {
							val = Double.parseDouble(value);
						} catch(Exception e) {
							LOGGER.error("The value for right coordinate attribute is not a valid double: " + value);
						}
						if (val != -1.0) {
							right = val;
						}
					}
				}
			}

			// create the bounding box
            double x = left;
			double y = right;
			double width = -1.0;
			double height = -1.0;
			if (right >= left)
				width = right - left;
			if (bottom >= top)
				height = bottom - top;
			box = BoundingBox
				.fromPointAndDimensions(page, x, y, width, height);

            System.out.println("LINK ("+currentParentIdStack.peek()+"): "+ accumulator.toString());
		}
		accumulator.setLength(0);
	}
	
}
