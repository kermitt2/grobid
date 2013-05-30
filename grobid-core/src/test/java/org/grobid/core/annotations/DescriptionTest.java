package org.grobid.core.annotations;

import static org.grobid.core.utilities.TeiValues.ATTR_ID;
import static org.grobid.core.utilities.TeiValues.W3C_NAMESPACE;
import static org.grobid.core.utilities.TeiValues.XML;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.custommonkey.xmlunit.XMLTestCase;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.grobid.core.utilities.TeiValues;
import org.junit.Test;
import org.xml.sax.SAXException;

public class DescriptionTest extends XMLTestCase {

	@Test
	public void testDescription() {
		final Description desc = new Description(true);
		assertTrue("isIndented should be true", desc.isIndented);
		assertTrue("paragraphs should be empty", desc.paragraphs.isEmpty());
		assertTrue("rawDescription should be empty", desc.rawDescription.toString().isEmpty());
	}

	@Test
	public void testAppendStartDescription() {
		final Description desc = new Description(true);
		final QName qName = getQName("", TeiValues.TAG_DIV, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute("type", "description"));
		final StartElement start = createStartElement(qName, attributes, null);
		desc.appendStartDescription(start, "Gorn index");
		assertEquals(start, desc.descriptionTag);
		assertEquals("gornIdxStart value should be 'Gorn index'", "Gorn index", desc.gornIdxStart);
	}

	@Test
	public void testAppendParagraphStartTag() {
		final Description desc = new Description(true);
		final QName qName = getQName("", TeiValues.TAG_P, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute("type", "description"));
		final StartElement start = createStartElement(qName, attributes, null);
		desc.appendStartDescription(start, "Gorn index");
		assertEquals(start, desc.descriptionTag);
		assertEquals("gornIdxStart value should be 'Gorn index'", "Gorn index", desc.gornIdxStart);
	}

	@Test
	public void testAppendParagraphContentAnd() {
		final Description desc = new Description(true);
		desc.appendParagraphContent("&");
		assertEquals("&amp;", desc.rawDescription.toString());
	}

	@Test
	public void testAppendParagraphContentGreaterThan() {
		final Description desc = new Description(true);
		desc.appendParagraphContent(">");
		assertEquals("&gt;", desc.rawDescription.toString());
	}

	@Test
	public void testAppendParagraphContentLessThan() {
		final Description desc = new Description(true);
		desc.appendParagraphContent("<");
		assertEquals("&lt;", desc.rawDescription.toString());
	}

	@Test
	public void testAppendParagraphContentDoubleQuote() {
		final Description desc = new Description(true);
		desc.appendParagraphContent("\"");
		assertEquals("&quot;", desc.rawDescription.toString());
	}

	@Test
	public void testAppendParagraphContentExtratSpace() {
		final Description desc = new Description(true);
		desc.appendParagraphContent("  text");
		assertEquals(" text", desc.rawDescription.toString());
	}

	@Test
	public void testAppendRawContent() {
		final Description desc = new Description(true);
		desc.appendRawContent("raw content <br>");
		assertEquals("raw content <br>", desc.rawDescription.toString());
	}

	@Test
	public void testAppendParagraphEndTag() {
		final Description desc = new Description(true);
		final QName qName = getQName("", TeiValues.TAG_P, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute("id", "someId"));
		final StartElement start = createStartElement(qName, attributes, null);
		desc.appendParagraphStartTag(start);
		desc.appendParagraphContent("content");
		desc.appendParagraphEndTag();
		final Paragraph currP = desc.paragraphs.get(0);
		assertEquals(start, currP.getStartTag());
		assertEquals(0, currP.getPositionStart());
		assertEquals(7, currP.getPositionEnd());
		assertEquals("content ", desc.rawDescription.toString());
	}

	@Test
	public void testToRawString() {
		final Description desc = new Description(true);
		desc.appendRawContent("raw content <br>");
		assertEquals("raw content <br>", desc.toRawString());
	}

	@Test
	public void testToTeiNotIndented() throws SAXException, IOException {
		final Description desc = new Description(false);

		QName qName = getQName("", TeiValues.TAG_DIV, "");
		Iterator<Attribute> attributes = getAttributes(createAttribute("type", "description"));
		StartElement start = createStartElement(qName, attributes, null);
		desc.appendStartDescription(start, "Gorn index");

		qName = getQName("", TeiValues.TAG_P, "");
		attributes = getAttributes(createAttribute("id", "someId"));
		start = createStartElement(qName, attributes, null);
		desc.appendParagraphStartTag(start);
		desc.appendParagraphContent("content");
		desc.appendParagraphEndTag();

		assertXMLEqual("The 2 xml should be identical", "<div type=\"description\"><p id=\"someId\">content</p></div>", desc.toTei());
	}

	@Test
	public void testToTeiIndented() throws SAXException, IOException {
		final Description desc = new Description(true);

		QName qName = getQName("", TeiValues.TAG_DIV, "");
		Iterator<Attribute> attributes = getAttributes(createAttribute("type", "description"));
		StartElement start = createStartElement(qName, attributes, null);
		desc.appendStartDescription(start, "Gorn index");

		qName = getQName("", TeiValues.TAG_P, "");
		attributes = getAttributes(createAttribute("id", "someId"));
		start = createStartElement(qName, attributes, null);
		desc.appendParagraphStartTag(start);
		desc.appendParagraphContent("content");
		desc.appendParagraphEndTag();

		assertEquals("The 2 xml should be identical", "<div type=\"description\">\n  <p id=\"someId\">content</p>\n</div>", desc.toTei());
	}

	@Test
	public void testGetPointerPatentItemReturnNull() {
		final Description desc = new Description(true);
		final QName qName = getQName("", TeiValues.TAG_P, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute("type", "description"));
		final StartElement start = createStartElement(qName, attributes, null);
		Paragraph paragraph = new Paragraph();
		paragraph.setStartTag(start);
		paragraph.setPositionEnd(1);
		desc.paragraphs.add(paragraph.clone());

		paragraph = new Paragraph();
		paragraph.setStartTag(start);
		paragraph.setPositionEnd(1);
		desc.paragraphs.add(paragraph.clone());

		final PatentItem item = new PatentItem();
		item.setOffsetBegin(10);
		assertNull(desc.getPointer(item));
	}

	@Test
	public void testGetPointerBibDataSet() {
		final Description desc = new Description(true);
		desc.gornIdxStart="1.2.2.";
		final QName qName = getQName("", TeiValues.TAG_P, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute("type", "description"));
		final StartElement start = createStartElement(qName, attributes, null);
		Paragraph paragraph = new Paragraph();
		paragraph.setStartTag(start);
		paragraph.setPositionEnd(2);
		desc.paragraphs.add(paragraph.clone());

		paragraph = new Paragraph();
		paragraph.setStartTag(start);
		paragraph.setPositionStart(3);
		paragraph.setPositionEnd(10);
		desc.paragraphs.add(paragraph.clone());

		final BibDataSet item = new BibDataSet();
		item.addOffset(3);
		item.setRawBib(" ");
		String ptr = desc.getPointer(item);
		assertEquals("#string-range('1.2.2.2',0,1)", ptr);
	}

	@Test
	public void testGetPointerBibDataSet2() {
		final Description desc = new Description(true);
		final QName qName = getQName("", TeiValues.TAG_P, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute(new QName(W3C_NAMESPACE, ATTR_ID, XML), "ID"));
		final StartElement start = createStartElement(qName, attributes, null);
		Paragraph paragraph = new Paragraph();
		paragraph.setStartTag(start);
		paragraph.setPositionEnd(2);
		desc.paragraphs.add(paragraph.clone());

		paragraph = new Paragraph();
		paragraph.setStartTag(start);
		paragraph.setPositionStart(3);
		paragraph.setPositionEnd(10);
		desc.paragraphs.add(paragraph.clone());

		final BibDataSet item = new BibDataSet();
		item.addOffset(3);
		item.setRawBib(" ");
		String ptr = desc.getPointer(item);
		assertEquals("#string-range('ID',0,1)", ptr);
	}

	@Test
	public void testGetPointerPatentBibDataSetNull() {
		final Description desc = new Description(true);
		final BibDataSet item = new BibDataSet();
		assertNull(desc.getPointer(item));
	}

	@Test
	public void testGetPointerPatentBibDataSetNull2() {
		final Description desc = new Description(true);
		final BibDataSet item = new BibDataSet();
		item.addOffset(null);
		assertNull(desc.getPointer(item));
	}

	@Test
	public void testGetPointerPatentBibDataSetNull3() {
		final Description desc = new Description(true);
		final BibDataSet item = new BibDataSet();
		item.addOffset(null);
		item.getOffsets().clear();
		assertNull(desc.getPointer(item));
	}

	@Test
	public void testGetPointerPatentBibDataSetNull4() {
		final Description desc = new Description(true);
		assertNull(desc.getPointer(new String()));
	}

	@Test
	public void testGetParagraphFromPointer() {
		final Description desc = new Description(true);
		desc.gornIdxStart = "1.";
		Paragraph paragraph = new Paragraph();
		paragraph.setPositionStart(1);
		paragraph.setPositionEnd(10);
		final StartElement start = createStartElement(new QName(""), null, null);
		paragraph.setStartTag(start);
		paragraph.setGornIdx("Gorn idx");
		desc.paragraphs.add(paragraph.clone());

		paragraph.setPositionStart(11);
		paragraph.setPositionEnd(20);
		desc.paragraphs.add(paragraph.clone());

		paragraph = desc.getParagraphFromPointer(3);
		assertEquals(1, paragraph.getPositionStart());
		assertEquals(10, paragraph.getPositionEnd());
		assertEquals(null, paragraph.getId());
		assertEquals("1.1", paragraph.getGornIdx());
	}

	@Test
	public void testGetParagraphFromPointer2() {
		final Description desc = new Description(true);
		Paragraph paragraph = new Paragraph();
		paragraph.setPositionStart(1);
		paragraph.setPositionEnd(10);
		final QName qName = getQName("", TeiValues.TAG_P, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute(new QName(W3C_NAMESPACE, ATTR_ID, XML), "ID"));
		final StartElement start = createStartElement(qName, attributes, null);
		paragraph.setStartTag(start);
		paragraph.setGornIdx("Gorn idx");
		desc.paragraphs.add(paragraph.clone());

		paragraph.setPositionStart(11);
		paragraph.setPositionEnd(20);
		desc.paragraphs.add(paragraph.clone());

		paragraph = desc.getParagraphFromPointer(3);
		assertEquals(1, paragraph.getPositionStart());
		assertEquals(10, paragraph.getPositionEnd());
		assertEquals("ID", paragraph.getId());
	}

	@Test
	public void testGetParagraphFromPointer3() {
		final Description desc = new Description(true);
		Paragraph paragraph = new Paragraph();
		paragraph.setPositionStart(1);
		paragraph.setPositionEnd(10);
		final StartElement start = createStartElement(new QName(""), null, null);
		paragraph.setStartTag(start);
		paragraph.setGornIdx("Gorn idx");
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(11);
		paragraph.setPositionEnd(21);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(22);
		paragraph.setPositionEnd(33);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(34);
		paragraph.setPositionEnd(45);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(46);
		paragraph.setPositionEnd(55);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(56);
		paragraph.setPositionEnd(88);
		desc.paragraphs.add(paragraph.clone());

		paragraph = desc.getParagraphFromPointer(21);
		assertNotNull(paragraph);
		assertEquals(11, paragraph.getPositionStart());
		assertEquals(21, paragraph.getPositionEnd());
	}

	@Test
	public void testGetParagraphFromPointerNull() {
		final Description desc = new Description(true);
		Paragraph paragraph = new Paragraph();
		paragraph.setPositionStart(1);
		paragraph.setPositionEnd(10);
		final StartElement start = createStartElement(new QName(""), null, null);
		paragraph.setStartTag(start);
		paragraph.setGornIdx("Gorn idx");
		desc.paragraphs.add(paragraph.clone());

		paragraph.setPositionStart(11);
		paragraph.setPositionEnd(20);
		desc.paragraphs.add(paragraph.clone());

		paragraph = desc.getParagraphFromPointer(21);
		assertNull(paragraph);
	}

	@Test
	public void testGetParagraphFromPointerNull2() {
		final Description desc = new Description(true);
		Paragraph paragraph = new Paragraph();
		paragraph.setPositionStart(1);
		paragraph.setPositionEnd(10);
		final StartElement start = createStartElement(new QName(""), null, null);
		paragraph.setStartTag(start);
		paragraph.setGornIdx("Gorn idx");
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(11);
		paragraph.setPositionEnd(20);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(22);
		paragraph.setPositionEnd(33);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(34);
		paragraph.setPositionEnd(45);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(46);
		paragraph.setPositionEnd(55);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(56);
		paragraph.setPositionEnd(88);
		desc.paragraphs.add(paragraph.clone());

		paragraph = desc.getParagraphFromPointer(21);
		assertNull(paragraph);
	}

	@Test
	public void testGetParagraphFromPointerNull3() {
		final Description desc = new Description(true);
		Paragraph paragraph = new Paragraph();
		paragraph.setPositionStart(1);
		paragraph.setPositionEnd(10);
		final StartElement start = createStartElement(new QName(""), null, null);
		paragraph.setStartTag(start);
		paragraph.setGornIdx("Gorn idx");
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(11);
		paragraph.setPositionEnd(20);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(22);
		paragraph.setPositionEnd(33);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(34);
		paragraph.setPositionEnd(45);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(46);
		paragraph.setPositionEnd(55);
		desc.paragraphs.add(paragraph.clone());
		paragraph.setPositionStart(56);
		paragraph.setPositionEnd(88);
		desc.paragraphs.add(paragraph.clone());

		paragraph = desc.getParagraphFromPointer(-1);
		assertNull(paragraph);
	}

	private static QName getQName(final String namespaceURI, final String localPart, final String prefix) {
		return new QName(namespaceURI, localPart, prefix);
	}

	private static StartElement createStartElement(final QName name, final Iterator<?> attributes, final Iterator<?> namespaces) {
		final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		return eventFactory.createStartElement(name, attributes, namespaces);
	}

	private static Attribute createAttribute(final QName qName, final String value) {
		final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		return eventFactory.createAttribute(qName, value);
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
