package org.grobid.core.sax;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.grobid.core.engines.patent.ReferenceExtractor;
import org.grobid.core.factory.AbstractEngineFactory;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TEISaxParserTest {

	@BeforeClass
	public static void setInitialContext() throws Exception {
		MockContext.setInitialContext();
		AbstractEngineFactory.init();
	}

	@AfterClass
	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}

	@Test
	public void testTEISaxPerser() throws ParserConfigurationException,
			SAXException, IOException {
		TEISaxParser parser = new TEISaxParser(new ReferenceExtractor());
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		saxParser
				.parse(new File(
						"src/test/resources/org/grobid/core/sax/resTEISaxParser/sample-1.tei.xml")
						.getAbsolutePath(), parser);
		System.out.println();
	}

}
