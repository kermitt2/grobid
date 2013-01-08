package org.grobid.core.utilities;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.exceptions.GrobidException;

/**
 * Utility class to write some XML.
 * 
 * @author Damien
 * 
 */
public class XMLWriter {

	/**
	 * The output string buffer.
	 */
	private StringWriter out;

	/**
	 * The XML writer.
	 */
	private XMLEventWriter writer;

	/**
	 * The XMLEventFactory used to create needed tags.
	 */
	private XMLEventFactory eventFactory;

	/**
	 * The constructor.
	 */
	public XMLWriter() {
		init();
		eventFactory = XMLEventFactory.newInstance();
	}

	public void addStartElement(final StartElement pStartElem) throws XMLStreamException {
		writer.add(pStartElem);
		writer.flush();
	}

	public void addStartElement(final StartElement pStartElem, final QName qName, final String pValue) throws XMLStreamException {
		writer.add(pStartElem);
		writer.add(eventFactory.createAttribute(qName, pValue));
		writer.flush();
	}

	/**
	 * Add a start tag to the current buffer.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @throws XMLStreamException
	 */
	public void addStartElement(final String pTagName) throws XMLStreamException {
		final XMLEvent tmpEvent = eventFactory.createStartElement(StringUtils.EMPTY, StringUtils.EMPTY, pTagName);
		writer.add(tmpEvent);
		writer.flush();
	}

	/**
	 * Add a start tag to the current buffer.
	 * 
	 * @param pTagName
	 *            The name of the tag.
	 * @param pAttributes
	 *            The attributes of the tag.
	 * @throws XMLStreamException
	 */
	public void addStartElement(final String pTagName, final Iterator<Attribute> pAttributes) throws XMLStreamException {
		final XMLEvent tmpEvent = eventFactory.createStartElement(new QName(pTagName), pAttributes, null);
		writer.add(tmpEvent);
		writer.flush();
	}

	/**
	 * Add a start tag to the current buffer.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @param pAttributes
	 *            map containing the attributes of the tag.
	 * @throws XMLStreamException
	 */
	public void addStartElement(final String pTagName, final Map<String, String> pAttributes) throws XMLStreamException {
		XMLEvent tmpEvent = eventFactory.createStartElement(StringUtils.EMPTY, StringUtils.EMPTY, pTagName);
		writer.add(tmpEvent);

		if (pAttributes != null) {
			Set<Entry<String, String>> attributes = pAttributes.entrySet();
			for (final Entry<String, String> currAttr : attributes) {
				tmpEvent = eventFactory.createAttribute(currAttr.getKey(), currAttr.getValue());
				writer.add(tmpEvent);
			}
		}
		writer.flush();
	}

	/**
	 * Add a start tag to the current buffer. This method can receive any number
	 * of pAttributes. The syntax to use is:<br>
	 * 
	 * addStartElement("TagName", "firstAttributeName", "firstAttributeValue",
	 * "secondAttributeName", "secondAttributeValue", ...)
	 * 
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @param pAttributes
	 *            the attributes to add.
	 * @throws XMLStreamException
	 */
	public void addStartElement(final String pTagName, final String... pAttributes) throws XMLStreamException {
		XMLEvent tmpEvent = eventFactory.createStartElement(StringUtils.EMPTY, StringUtils.EMPTY, pTagName);
		writer.add(tmpEvent);

		if (pAttributes != null) {
			String name = null;
			String value = null;
			int cpt = 0;
			for (String currAttr : pAttributes) {
				if (cpt % 2 == 0) {
					name = currAttr;
				} else {
					value = currAttr;
					tmpEvent = eventFactory.createAttribute(name, value);
					writer.add(tmpEvent);
				}
				cpt++;
			}
		}
		writer.flush();
	}

	/**
	 * Add characters to the current buffer.
	 * 
	 * @param pContent
	 *            the content to add.
	 * @throws XMLStreamException
	 */
	public void addCharacters(final String pContent) throws XMLStreamException {
		final XMLEvent tmpEvent = eventFactory.createCharacters(pContent);
		writer.add(tmpEvent);
	}

	/**
	 * Add a end tag to the current buffer.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @throws XMLStreamException
	 */
	public void addEndElement(final String pTagName) throws XMLStreamException {
		final XMLEvent tmpEvent = eventFactory.createEndElement(StringUtils.EMPTY, StringUtils.EMPTY, pTagName);
		writer.add(tmpEvent);
		writer.flush();
	}

	/**
	 * Add any kind of data to the buffer (even XML).
	 * 
	 * @param pContent
	 *            the content to append.
	 */
	public void addRaw(final String pContent) {
		out.write(pContent);
		out.flush();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return out.toString();
	}

	/**
	 * Return the xml built formatted. Return {@link StringUtils#EMPTY} if XML
	 * formatting failed.
	 * 
	 * @return formatted XML.
	 */
	public String toStringIndented() {
		return formatXML(out.toString());
	}

	/**
	 * Return the pToFormat formatted. Return {@link StringUtils#EMPTY} if XML
	 * formatting failed.
	 * 
	 * @param pToFormat
	 *            The String to format.
	 * @return the indented XML.
	 */
	public static String formatXML(final String pToFormat) {
		final StringWriter indented = new StringWriter();
		final TransformerFactory factory = TransformerFactory.newInstance();
		try {
			final Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new StreamSource(new StringReader(pToFormat)), new StreamResult(indented));
		} catch (TransformerConfigurationException tcExp) {
			throwRuntimeFormatException(tcExp);
		} catch (TransformerException tExp) {
			throwRuntimeFormatException(tExp);
		}
		return indented.toString();
	}

	/**
	 * Flush data.
	 */
	public void flush() {
		try {
			writer.flush();
		} catch (XMLStreamException exmlStrExp) {
			throwRuntimeFormatException(exmlStrExp);
		}
	}

	/**
	 * Re initialize the writer.
	 */
	public void resetWriter() {
		init();
	}

	/**
	 * @return true if the writer is empty, false else.
	 */
	public boolean isEmpty() {
		return out.toString().isEmpty();
	}

	/**
	 * Init the buffer and the XML writer.
	 */
	protected void init() {
		out = new StringWriter();
		try {
			writer = XMLOutputFactory.newInstance().createXMLEventWriter(out);
		} catch (XMLStreamException xmlStrExp) {
			throw new GrobidException("An error occured while creating the Stax event readers: " + xmlStrExp.toString());
		} catch (FactoryConfigurationError factCongExp) {
			throw new GrobidException("An error occured while creating the Stax event readers: " + factCongExp.toString());
		}
	}

	/**
	 * Throws RuntimeException with the content of the exception in parameter.
	 * 
	 * @param pExp
	 *            The exception.
	 */
	protected static void throwRuntimeFormatException(Exception pExp) {
		throw new RuntimeException(pExp);
	}
}
