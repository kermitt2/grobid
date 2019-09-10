package org.grobid.core.document;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

public class DocumentTest {

    @BeforeClass
    public static void setInitialContext() throws Exception {
        GrobidProperties.getInstance();
    }

    @Before
    public void setUp() throws Exception {
        GrobidProperties.getInstance();
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_3RD_PARTY_PDFTOXML_VALIDATION_ENABLED,
            "false"
        );
    }

    private static byte[] getXmlBytesWithTextByteSequence(byte[] textBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("<xml>".getBytes());
        out.write(textBytes);
        out.write("</xml>".getBytes());
        return out.toByteArray();
    }

    private static byte[] getXmlBytesWithInvalidUtf8Sequence() throws IOException {
        return getXmlBytesWithTextByteSequence(new byte[] {
            (byte) 0xe0, (byte) 0xd8, 0x35
        });
    }

    private static String parseXmlBytesAndReturnText(byte[] xmlBytes)
            throws SAXException, IOException, ParserConfigurationException {
        StringBuilder buffer = new StringBuilder();
        Document.parseInputStream(
            new ByteArrayInputStream(xmlBytes),
            SAXParserFactory.newInstance(),
            new DefaultHandler() {
                public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
                    buffer.append(ch, start, length);
                }
            }
        );
        return buffer.toString();
    }

    @Test
    public void shouldParseTextOfValidXml() throws Exception {
        String text = parseXmlBytesAndReturnText(
            getXmlBytesWithTextByteSequence("test".getBytes())
        );
        assertEquals("characters", "test", text);
    }

    @Test
    public void shouldNotFailToParseInvalidUtf8ByteSequenceXmlByDefault() throws Exception {
        parseXmlBytesAndReturnText(
            getXmlBytesWithInvalidUtf8Sequence()
        );
    }

    @Test
    public void shouldReplaceInvalidUtf8ByteSequence() throws Exception {
        String text = parseXmlBytesAndReturnText(
            getXmlBytesWithInvalidUtf8Sequence()
        );
        // our invalid utf-8 sequence is three bytes long
        // once the first byte is ignore, it will think it is two bytes long
        // leaving us with two question marks and the last byte, which happens to be "5"
        assertEquals("characters", "??5", text);
    }

    @Test(expected = SAXParseException.class)
    public void shouldFailToParseInvalidUtf8ByteSequenceXmlWithValidationEnabled() throws Exception {
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_3RD_PARTY_PDFTOXML_VALIDATION_ENABLED,
            "true"
        );
        parseXmlBytesAndReturnText(
            getXmlBytesWithInvalidUtf8Sequence()
        );
    }
}