package org.grobid.core.annotations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import static org.grobid.core.utilities.TeiValues.ATTR_ID;
import static org.grobid.core.utilities.TeiValues.W3C_NAMESPACE;
import static org.grobid.core.utilities.TeiValues.XML;
import static org.junit.Assert.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.grobid.core.utilities.TeiValues;
import org.junit.Test;

public class ParagraphTest {

	@Test
	public void testParagraph() {
		final StartElement start = createStartElement(new QName(""), null, null);
		final Paragraph par = new Paragraph("ID", "GORN", 0, 2, start, true);
		assertEquals("ID", par.id);
		assertEquals("GORN", par.gornIdx);
		assertEquals(0, par.positionStart);
		assertEquals(2, par.positionEnd);
		assertEquals(true, par.isAnnotated);
		assertNotNull(par.startTag);
	}

	@Test
	public void testParagraphPtr() {
		final Paragraph par = new Paragraph(2);
		assertEquals(2, par.descriptionPointer);
	}

	@Test
	public void testClone() {
		final StartElement start = createStartElement(new QName(""), null, null);
		final Paragraph par = new Paragraph("ID", "GORN", 0, 2, start, true).clone();

		assertEquals("ID", par.id);
		assertEquals("GORN", par.gornIdx);
		assertEquals(0, par.positionStart);
		assertEquals(2, par.positionEnd);
		assertEquals(true, par.isAnnotated);
		assertNotNull(par.startTag);
	}

	@Test
	public void testId() {
		final Paragraph par = new Paragraph();
		par.setId("ID");
		assertEquals("ID", par.getId());
	}

	@Test
	public void testGorn() {
		final Paragraph par = new Paragraph();
		par.setGornIdx("GORN");
		assertEquals("GORN", par.getGornIdx());
	}

	@Test
	public void testPosStart() {
		final Paragraph par = new Paragraph();
		par.setPositionStart(4);
		assertEquals(4, par.getPositionStart());
	}

	@Test
	public void testPosEnd() {
		final Paragraph par = new Paragraph();
		par.setPositionEnd(4);
		assertEquals(4, par.getPositionEnd());
	}

	@Test
	public void testIsAnnotated() {
		final Paragraph par = new Paragraph();
		par.setAnnotated(true);
		assertEquals(true, par.isAnnotated());
	}

	@Test
	public void testStartTag() {
		final Paragraph par = new Paragraph();
		final StartElement start = createStartElement(new QName("TAG"), null, null);
		par.setStartTag(start);
		assertEquals("<TAG>", par.getStartTag().asStartElement().toString());
	}

	@Test
	public void testGetIdFromAttribute() {
		final QName qName = getQName("", TeiValues.TAG_P, "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute(new QName(W3C_NAMESPACE, ATTR_ID, XML), "ID"));
		final StartElement start = createStartElement(qName, attributes, null);
		final Paragraph par = new Paragraph();
		par.setStartTag(start);
		assertEquals("ID", par.getIdFromAttributes());
	}

	@Test
	public void testGetIdFromAttributeNull() {
		final StartElement start = createStartElement(new QName(""), null, null);
		final Paragraph par = new Paragraph();
		par.setStartTag(start);
		assertNull(par.getIdFromAttributes());
	}

	@Test
	public void testEquals() {
		List<Paragraph> pars = new ArrayList<Paragraph>();
		final Paragraph par = new Paragraph();
		par.setPositionStart(0);
		par.setPositionEnd(8);
		pars.add(par.clone());

		par.setPositionStart(10);
		par.setPositionEnd(33);
		pars.add(par.clone());

		assertEquals(1, pars.indexOf(new Paragraph(22)));
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

	private static Iterator<Attribute> getAttributes(final Attribute... pAttr) {
		Vector<Attribute> attributes = new Vector<Attribute>();
		for (final Attribute attr : pAttr) {
			attributes.add(attr);
		}
		return attributes.iterator();
	}
}
