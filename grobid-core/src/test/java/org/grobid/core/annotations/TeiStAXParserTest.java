package org.grobid.core.annotations;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.custommonkey.xmlunit.XMLTestCase;
import org.grobid.core.engines.patent.ReferenceExtractor;
import org.grobid.core.factory.AbstractEngineFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.XMLWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TeiStAXParserTest extends XMLTestCase {

	public static void setInitialContext() throws Exception {
		MockContext.setInitialContext();
		AbstractEngineFactory.init();
	}

	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}

	@Before
	public void setUp() throws Exception {
		setInitialContext();
	}

	@After
	public void tearDown() throws Exception {
		destroyInitialContext();
	}

	@Test
	public void testTeiStAXParser3Args() throws IOException {
		final TeiStAXParser parser = new TeiStAXParser(createInputStream("input"), createOutputStream("output"), false);

		assertEquals("The inputStream value of the parser should be 'input'", "input", fromInputStreamToString(parser.inputStream));
		assertEquals("The outputStream value of the parser should be 'output'", "output", fromOutputStreamToString(parser.outputStream));
	}

	@Test
	public void testTeiStAXParser4Args() throws IOException {
		final TeiStAXParser parser = new TeiStAXParser(createInputStream("input"), createOutputStream("output"), true, false);

		assertEquals("The inputStream value of the parser should be 'input'", "input", fromInputStreamToString(parser.inputStream));
		assertEquals("The outputStream value of the parser should be 'output'", "output", fromOutputStreamToString(parser.outputStream));
		assertEquals("The isIndented value of the parser should be true", true, parser.isIndented);
		assertEquals("The isSelfInstanceRefExtractor value of the parser should be true", true, parser.isSelfInstanceRefExtractor);
	}

	@Test
	public void testTeiStAXParser5Args() throws IOException {
		final ReferenceExtractor refExtr = new ReferenceExtractor();
		refExtr.currentPatentNumber = "patNb";
		final String input = "<tag id=\"tId\">input</tag>";
		TeiStAXParser parser = new TeiStAXParser(createInputStream(input), createOutputStream("output"), false, refExtr, false);

		assertEquals("The inputStream value of the parser should be '" + input + "'", input, fromInputStreamToString(parser.inputStream));
		assertEquals("The outputStream value of the parser should be 'output'", "output", fromOutputStreamToString(parser.outputStream));
		assertEquals("The isIndented value of the parser should be false", false, parser.isIndented);
		assertEquals("The isSelfInstanceRefExtractor value of the parser should be false", false, parser.isSelfInstanceRefExtractor);
		assertEquals("The extractor.currentPatentNumber value of the parser should be 'patNb'", "patNb",
				parser.extractor.currentPatentNumber);

		assertNotNull("currTEIParsedInfo should not be null", parser.currTEIParsedInfo);
		assertNotNull("teiBuffer should not be null", parser.teiBuffer);
		assertNotNull("headerAnnotation should n ot be null", parser.headerAnnotation);
		// escape the START_DOCUMENT event
		parser.reader.next();
		final XMLEvent event = (XMLEvent) parser.reader.next();
		assertEquals("the tag value should be 'tag'", "tag", event.asStartElement().getName().getLocalPart());
		final Attribute attr = event.asStartElement().getAttributeByName(getQName("", "id", ""));
		assertEquals("the id should be 'tId'", "tId", attr.getValue());
	}

	@Test
	public void testParse() throws IOException, XMLStreamException, SAXException {
		final String input = "<tag id=\"tId\"> <div type=\"abstract\" xml:id=\"_cc53f64\" xml:lang=\"en\" subtype=\"docdba\">some text</div> </tag>";
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(input), createOutputStream(""), false);
		parser.parse();
		assertXMLEqual("The 2 xml should be identical", input, fromOutputStreamToString(parser.outputStream));
	}

	@Test
	public void testWriteInTeiBufferStart() throws IOException, XMLStreamException, SAXException {
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(""), createOutputStream(""), false);
		final QName qName = getQName("", "tag", "");
		final Iterator<Attribute> attributes = getAttributes(createAttribute("attr", "val"));
		final StartElement start = createStartElement(qName, attributes, null);
		parser.writeInTeiBufferStart(start);

		assertEquals("The teiBuffer value should be <tag attr=\"val\">", "<tag attr=\"val\">", parser.teiBuffer.toString());
	}

	@Test
	public void testWriteInTeiBufferCharacters() throws IOException, XMLStreamException, SAXException {
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(""), createOutputStream(""), false);
		final String content = "some content";
		parser.writeInTeiBufferCharacters(createCharacters(content));

		assertEquals("The teiBuffer value should be " + content, content, parser.teiBuffer.toString());
	}

	@Test
	public void testWriteInTeiBufferEnd() throws IOException, XMLStreamException, SAXException {
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(""), createOutputStream(""), false);
		final QName qName = getQName("", "endTag", "");
		parser.writeInTeiBufferEnd(createEndElement(qName, null));

		assertEquals("The teiBuffer value should be </endTag>", "</endTag>", parser.teiBuffer.toString());
	}

	@Test
	public void testWriteInTeiBufferRaw() throws IOException, XMLStreamException, SAXException {
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(""), createOutputStream(""), false);
		final String content = "some content";
		parser.writeInTeiBufferRaw(content);

		assertEquals("The teiBuffer value should be " + content, content, parser.teiBuffer.toString());
	}

	@Test
	public void testParseNotSelfRefExtractor() throws UnsupportedEncodingException, IOException, XMLStreamException, SAXException {
		final String input = "<teiCorpus id=\"tId\"> <div type=\"claims\"><p>some paragraph</p></div> </teiCorpus>";
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(input), createOutputStream(""), false);
		parser.isSelfInstanceRefExtractor = false;
		parser.parse();
		assertXMLEqual("The 2 xml should be identical", input, fromOutputStreamToString(parser.outputStream));
	}

	@Test
	public void testParseDescription() throws UnsupportedEncodingException, IOException, XMLStreamException, SAXException {
		final String input = "<teiCorpus id=\"tId\"> <div type=\"description\"><p>some paragraph</p></div> </teiCorpus>";
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(input), createOutputStream(""), false, false);
		parser.parse();
		assertXMLEqual("The 2 xml should be identical", input, fromOutputStreamToString(parser.outputStream));
	}

	@Test
	public void testParseDescription2() throws UnsupportedEncodingException, IOException, XMLStreamException, SAXException {
		final String input = "<teiCorpus id=\"tId\"> <div type=\"description\"><p>some paragraph</p><p>some text &lt;sep&gt; &quot; <br clear=\"none\" /> </p></div> </teiCorpus>";
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(input), createOutputStream(""), false, false);
		parser.parse();
		assertXMLEqual("The 2 xml should be identical", XMLWriter.formatXML(input),
				XMLWriter.formatXML(fromOutputStreamToString(parser.outputStream)));
	}

	@Test
	public void testParseDescriptionTagInsideP() throws UnsupportedEncodingException, IOException, XMLStreamException, SAXException {
		final String input = "<teiCorpus id=\"tId\"> <div type=\"description\"><p>some paragraph</p><p>some text &lt;sep&gt; &quot; <someTag>text inside tag</someTag> </p></div> </teiCorpus>";
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(input), createOutputStream(""), false, false);
		parser.parse();
		assertXMLEqual("The 2 xml should be identical", XMLWriter.formatXML(input),
				XMLWriter.formatXML(fromOutputStreamToString(parser.outputStream)));
	}

	@Test
	public void testParseNoDescription() throws UnsupportedEncodingException, IOException, XMLStreamException, SAXException {
		final String input = "<teiCorpus id=\"tId\"> <div type=\"noDescription\"><p>some paragraph</p></div> </teiCorpus>";
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(input), createOutputStream(""), false, false);
		parser.parse();
		assertXMLEqual("The 2 xml should be identical", input, fromOutputStreamToString(parser.outputStream));
	}

	@Test
	public void testParseStartTEI() throws UnsupportedEncodingException, IOException, XMLStreamException, SAXException {
		final String input = "<teiCorpus id=\"tId\"> <TEI><teiHeader><notesStmt><notes>some element</notes></notesStmt></teiHeader><div type=\"description\"><p>some paragraph</p></div></TEI> </teiCorpus>";
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(input), createOutputStream(""), false, false);
		parser.parse();
		assertXMLEqual("The 2 xml should be identical", input, fromOutputStreamToString(parser.outputStream));
	}

	@Test
	public void testParseNotesStmt() throws UnsupportedEncodingException, IOException, XMLStreamException, SAXException {
		final String input = "<teiCorpus id=\"tId\"><TEI><teiHeader><notesStmt><notes>some element</notes></notesStmt></teiHeader></TEI></teiCorpus>";
		final TeiStAXParser parser = new TeiStAXParser(createInputStream(input), createOutputStream(""), false, false);
		parser.parse();
		assertXMLEqual("The 2 xml should be identical", input, fromOutputStreamToString(parser.outputStream));
	}

	@Test
	public void testParserOnFullTEI() throws XMLStreamException, IOException {
		ReferenceExtractor extractor = new ReferenceExtractor();
		OutputStream out;
		TeiStAXParser stax;
		//out = getOutputStreamFromFile("src/test/resources/org/grobid/core/annotations/resTeiStAXParser/out.tei.xml");
		out = System.out;
		// ByteArrayOutputStream baos = new ByteArrayOutputStream();

		stax = new TeiStAXParser(
				getInputStreamFromFile("src/test/resources/org/grobid/core/annotations/resTeiStAXParser/sample-2.tei.xml"),
				out, true,
				extractor, false);

		stax.parse();
	}

	private static FileInputStream getInputStreamFromFile(final String pFileName) throws FileNotFoundException {
		return new FileInputStream(pFileName);
	}

	private static FileOutputStream getOutputStreamFromFile(final String pFileName) throws FileNotFoundException {
		return new FileOutputStream(pFileName);
	}

	private InputStream createInputStream(final String str) throws java.io.UnsupportedEncodingException {
		return new ByteArrayInputStream(str.getBytes("UTF-8"));
	}

	private OutputStream createOutputStream(final String str) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(str.getBytes());
		return out;
	}

	private String fromInputStreamToString(final InputStream input) throws IOException {
		input.reset();
		byte[] bytes = new byte[input.available()];
		input.read(bytes);
		return new String(bytes);
	}

	private String fromOutputStreamToString(final OutputStream output) throws IOException {
		output.toString();
		return output.toString();
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

	private static Characters createCharacters(final String content) {
		final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		return eventFactory.createCharacters(content);
	}

	private static EndElement createEndElement(final QName name, final Iterator<?> namespaces) {
		final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		return eventFactory.createEndElement(name, namespaces);
	}

}
