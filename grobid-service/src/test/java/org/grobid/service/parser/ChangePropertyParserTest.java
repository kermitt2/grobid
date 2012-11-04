package org.grobid.service.parser;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * 
 * @author Damien
 * 
 */
public class ChangePropertyParserTest {

	@Test
	public void testGetPassword() throws ParserConfigurationException,
			SAXException, IOException {
		ChangePropertyParser parser = new ChangePropertyParser(generateXml(
				"pass", "key", "val", "type"));
		assertEquals("Returned value not the one expected.", "pass",
				parser.getPassword());
	}

	@Test
	public void testGetKey() throws ParserConfigurationException, SAXException,
			IOException {
		ChangePropertyParser parser = new ChangePropertyParser(generateXml(
				"pass", "key", "val", "type"));
		assertEquals("Returned value not the one expected.", "key",
				parser.getKey());
	}

	@Test
	public void testGetValue() throws ParserConfigurationException,
			SAXException, IOException {
		ChangePropertyParser parser = new ChangePropertyParser(generateXml(
				"pass", "key", "val", "type"));
		assertEquals("Returned value not the one expected.", "val",
				parser.getValue());
	}

	@Test
	public void testGetType() throws ParserConfigurationException,
			SAXException, IOException {
		ChangePropertyParser parser = new ChangePropertyParser(generateXml(
				"pass", "key", "val", "type"));
		assertEquals("Returned value not the one expected.", "type",
				parser.getType());
	}

	private String generateXml(String pwd, String key, String value, String type) {
		StringBuffer xml = new StringBuffer("<changeProperty><password>");
		xml.append(pwd);
		xml.append("</password><property><key>");
		xml.append(key);
		xml.append("</key><value>");
		xml.append(value);
		xml.append("</value><type>");
		xml.append(type);
		xml.append("</type></property></changeProperty>");
		return xml.toString();
	}
}
