package org.grobid.service.parser;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ChangePropertyParser {

	Document doc;

	/**
	 * Constructor of ChangePropertyParser.
	 * @param pInput the xml to parse.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public ChangePropertyParser(String pInput)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		doc = dBuilder.parse(new InputSource(new StringReader(pInput)));
		doc.getDocumentElement().normalize();
	}

	/**
	 * @return the password.
	 */
	public String getPassword() {
		return getValue("password");
	}
	
	/**
	 * @return the key.
	 */
	public String getKey(){
		return getValue("key");
	}
	
	/**
	 * @return the value.
	 */
	public String getValue(){
		return getValue("value");
	}
	
	/**
	 * @return the type.
	 */
	public String getType(){
		return getValue("type");
	}

	/**
	 * Return the value of the pTag.
	 * 
	 * @param pTag
	 *            the tag name.
	 * @return the value contained in the tag.
	 */
	protected String getValue(String pTag) {
		NodeList nList = doc.getElementsByTagName("changeProperty");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				return getTagValue(pTag, eElement);
			}
		}
		return null;
	}

	/**
	 * Return the value of the tag pTag in element eElement
	 * 
	 * @param sTag
	 *            the tag name.
	 * @param eElement
	 *            the element
	 * @return the value contained in the tag.
	 */
	protected static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

}
