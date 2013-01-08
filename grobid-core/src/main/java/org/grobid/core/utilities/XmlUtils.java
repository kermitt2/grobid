package org.grobid.core.utilities;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.XMLEvent;

import org.xml.sax.Attributes;

public class XmlUtils {

	public static final String TAG_START_LOW = "<";
	public static final String TAG_START_SUP = ">";
	public static final String TAG_START_SLASH_SUP = "/>";
	public static final String TAG_END_LOW_SLASH = "</";
	public static final String TAG_END_SUP = ">";
	public static final String SPACE = " ";
	public static final String EQUALS = "=";
	public static final String QUOTE = "\"";

	/**
	 * Build a start tag.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @return requested tag.
	 */
	public static String startTag(String pTagName) {
		StringBuilder tag = new StringBuilder();
		tag.append(TAG_START_LOW);
		tag.append(pTagName);
		tag.append(TAG_START_SUP);
		return tag.toString();
	}

	/**
	 * Build a start tag.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * 
	 * @param pAtts
	 *            the attributes of the tag
	 * @return requested tag.
	 */
	public static String startTag(String pTagName, Attributes pAtts) {
		StringBuilder tag = new StringBuilder();
		tag.append(TAG_START_LOW);
		tag.append(pTagName);
		String name;
		String value;
		for (int i = 0; i < pAtts.getLength(); i++) {
			name = pAtts.getQName(i);
			value = pAtts.getValue(i);
			tag.append(SPACE);
			tag.append(name);
			tag.append(EQUALS);
			tag.append(QUOTE);
			tag.append(value);
			tag.append(QUOTE);
		}
		tag.append(TAG_START_SUP);
		return tag.toString();
	}

	/**
	 * Build a tag <code><tagName/></code>.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @return requested tag.
	 */
	public static String startEndTag(String pTagName) {
		StringBuilder tag = new StringBuilder();
		tag.append(TAG_START_LOW);
		tag.append(pTagName);
		tag.append(TAG_START_SLASH_SUP);
		return tag.toString();
	}

	/**
	 * Build a end tag.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @return requested tag.
	 */
	public static String endTag(String pTagName) {
		StringBuilder tag = new StringBuilder();
		tag.append(TAG_END_LOW_SLASH);
		tag.append(pTagName);
		tag.append(TAG_END_SUP);
		return tag.toString();
	}

	/**
	 * Build a full tag.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @param pValue
	 *            value inside the tag.
	 * @return requested tag.
	 */
	public static String fullTag(String pTagName, String pValue) {
		StringBuilder tag = new StringBuilder();
		tag.append(startTag(pTagName));
		tag.append(pValue);
		tag.append(endTag(pTagName));
		return tag.toString();
	}

	/**
	 * Compares two {@link XMLEvent} instances. This method delegates actual
	 * matching to the appropriate overloaded method.
	 * 
	 * @param a
	 *            The first event.
	 * @param b
	 *            The second event.
	 * @return <code>true</code> if the events match, <code>false</code>
	 *         otherwise.
	 */
	public static boolean eventsMatch(XMLEvent a, XMLEvent b) {

		if (a == b) {

			return true;

		} else if (a == null || b == null) {

			return false;

		} else if (a.getEventType() == b.getEventType()) {

			switch (a.getEventType()) {

			case XMLEvent.START_ELEMENT:
				return eventsMatch(a.asStartElement(), b.asStartElement());

			case XMLEvent.END_ELEMENT:
				return eventsMatch(a.asEndElement(), b.asEndElement());

			case XMLEvent.CDATA:
			case XMLEvent.SPACE:
			case XMLEvent.CHARACTERS:
				return eventsMatch(a.asCharacters(), b.asCharacters());

			case XMLEvent.COMMENT:
				return eventsMatch((Comment) a, (Comment) b);

			case XMLEvent.ENTITY_REFERENCE:
				return eventsMatch((EntityReference) a, (EntityReference) b);

			case XMLEvent.ATTRIBUTE:
				return eventsMatch((Attribute) a, (Attribute) b);

			case XMLEvent.NAMESPACE:
				return eventsMatch((Namespace) a, (Namespace) b);

			case XMLEvent.START_DOCUMENT:
				return eventsMatch((StartDocument) a, (StartDocument) b);

			case XMLEvent.END_DOCUMENT:
				return eventsMatch((EndDocument) a, (EndDocument) b);

			case XMLEvent.PROCESSING_INSTRUCTION:
				return eventsMatch((ProcessingInstruction) a, (ProcessingInstruction) b);

			case XMLEvent.DTD:
				return eventsMatch((DTD) a, (DTD) b);

			case XMLEvent.ENTITY_DECLARATION:
				return eventsMatch((EntityDeclaration) a, (EntityDeclaration) b);

			case XMLEvent.NOTATION_DECLARATION:
				return eventsMatch((NotationDeclaration) a, (NotationDeclaration) b);

			}

		}

		return false;

	}

	/**
	 * Give the String representation of an Attribute name.
	 * 
	 * @param pAttribute
	 *            the attribute
	 * @return String representation of the attribute name.
	 */
	public static String attributeNameToString(Attribute pAttribute) {
		StringBuffer fullAttribute = new StringBuffer();
		if (pAttribute.getName().getPrefix() != null) {
			fullAttribute.append(pAttribute.getName().getPrefix());
			fullAttribute.append(":");
		}
		fullAttribute.append(pAttribute.getName().getLocalPart());
		return fullAttribute.toString();
	}

}
