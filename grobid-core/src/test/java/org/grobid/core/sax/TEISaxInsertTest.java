package org.grobid.core.sax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.grobid.core.utilities.Utilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;

public class TEISaxInsertTest {
	
	private File file;
	
	@Before
	public void before() throws IOException{
		file = File.createTempFile("TEISaxInsertTestSample", ".tei.xml");
	}
	
	@After
	public void after(){
		file.delete();
	}
	
	@Test
	public void testTEISaxInsertTest() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException{
		Utilities.writeInFile(file.getAbsolutePath(), "<teiCorpus/>");
		//TEISaxInsert parser = new TEISaxInsert(file);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		//saxParser.parse(file, parser);
	}

}
