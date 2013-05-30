package org.grobid.core.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.grobid.core.utilities.TeiValues;
import org.junit.Test;

/**
 * Test the class TeiStAXParsedInfo.
 * 
 * @author Damien
 * 
 */
public class TeiStAXParsedInfoTest {

	@Test
	public void testConstructor() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		assertNotNull("should not be null", infos.gorn);
		assertNotNull("should not be null", infos.description);
	}

	@Test
	public void testResetDexcription() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		infos.description.rawDescription.append("raw description");
		infos.resetDescription();
		assertEquals("rawDescription of description should be empty", "", infos.description.rawDescription.toString());
	}

	@Test
	public void testCheckIfDescriptionTrue() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		final QName qName = getQName("", TeiValues.TAG_DIV, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute("type", "description"));
		final StartElement start = createStartElement(qName, attributes, null);
		assertTrue("checkIfDescription should return true", infos.checkIfDescription(start));
	}

	@Test
	public void testCheckIfDescriptionFalse() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		final QName qName = getQName("", TeiValues.TAG_DIV, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute("noType", "description"));
		final StartElement start = createStartElement(qName, attributes, null);
		assertFalse("checkIfDescription should return false", infos.checkIfDescription(start));
	}

	@Test
	public void testCheckIfDescriptionFalse2() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		final QName qName = getQName("", "SOME_TAG", "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute("type", "description"));
		final StartElement start = createStartElement(qName, attributes, null);
		assertFalse("checkIfDescription should return false", infos.checkIfDescription(start));
	}

	@Test
	public void testProcessParagraphStartTagTrue() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		final QName qName = getQName("", TeiValues.TAG_P, "");
		final StartElement start = createStartElement(qName, null, null);
		assertTrue("processParagraphStartTag should return true", infos.processParagraphStartTag(start));
	}

	@Test
	public void testProcessParagraphStartTagFalse() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		final QName qName = getQName("", "SOME_TAG", "");
		final StartElement start = createStartElement(qName, null, null);
		assertFalse("processParagraphStartTag should return false", infos.processParagraphStartTag(start));
	}

	@Test
	public void testProcessParagraphEndTagTrue() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		final QName qName = getQName("", TeiValues.TAG_P, "");
		final StartElement start = createStartElement(qName, null, null);
		// Add paragraph to avoid NullPointerException.
		infos.description.appendParagraphStartTag(start);
		assertTrue("processParagraphEndTag should return true", infos.processParagraphEndTag(TeiValues.TAG_P));
	}

	@Test
	public void testProcessParagraphEndTagFalse() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		assertFalse("processParagraphEndTag should return flase", infos.processParagraphEndTag("SOME_TAG"));
	}

	@Test
	public void testAppendDescriptionContent() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		infos.appendDescriptionContent("some description");
		assertEquals("rawDescription value should be \"some description\"", "some description",
				infos.getDescription().rawDescription.toString());
	}

	@Test
	public void testGornIndex() {
		final TeiStAXParsedInfo infos = new TeiStAXParsedInfo(false);
		infos.incrementGornIndex();
		infos.incrementGornIndex();
		infos.incrementGornIndex();
		infos.decrementGornIndex();
		infos.decrementGornIndex();
		infos.incrementGornIndex();
		infos.incrementGornIndex();
		infos.decrementGornIndex();
		infos.incrementGornIndex();
		infos.decrementGornIndex();
		infos.incrementGornIndex();
		assertEquals("The gorn index value should be 1.2.3.", "1.2.3.", infos.getCurrentGornIndex());
	}

	private static QName getQName(final String namespaceURI, final String localPart, final String prefix) {
		return new QName(namespaceURI, localPart, prefix);
	}

	private static StartElement createStartElement(final QName name, final Iterator<?> attributes, final Iterator<?> namespaces) {
		final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		return eventFactory.createStartElement(name, attributes, namespaces);
	}

	private static Attribute createAttribute(final String localName, final String value) {
		final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		return eventFactory.createAttribute(localName, value);
	}

	private static Iterator<Attribute> getAttributes(final Attribute... pAttr) {
		Vector<Attribute> attributes = new Vector<Attribute>();
		for (final Attribute attr : pAttr) {
			attributes.add(attr);
		}
		return attributes.iterator();
	}
}
