package org.grobid.core.utilities;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;

public class XMLWriterTest {

	private XMLWriter writerInstance;

	@Before
	public void setUp() {
		writerInstance = new XMLWriter();
	}

	@Test
	public void testFullTagCreation() throws XMLStreamException {
		writerInstance.addStartElement("someTag", "att1", "val1", "att2",
				"val2", "att3", "val3");

		writerInstance.addStartElement("second");
		writerInstance.addCharacters("Some Content");
		writerInstance.addEndElement("second");

		writerInstance.addStartElement("second", "attr", "val");
		writerInstance.addCharacters("bla bla");
		writerInstance.addEndElement("second");

		writerInstance.addEndElement("someTag");
		assertEquals(
				"Returned XML does not match the expected one",
				"<someTag att1=\"val1\" att2=\"val2\" att3=\"val3\"><second>Some Content</second><second attr=\"val\">bla bla</second></someTag>",
				writerInstance.toString());
		System.out.println(XMLWriter.formatXML(writerInstance.toString()));
	}

	@Test
	public void temp() throws TransformerException {

		StringWriter sw = new StringWriter();
		sw.write("<test>some test</test>");
		TransformerFactory factory = TransformerFactory.newInstance();

		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");

		StringWriter formattedStringWriter = new StringWriter();
		transformer.transform(
				new StreamSource(new StringReader(sw.toString())),
				new StreamResult(formattedStringWriter));
		System.out.println(formattedStringWriter);
		System.out.println(XMLWriter.formatXML("<test>some test</test>"));
	}
}
