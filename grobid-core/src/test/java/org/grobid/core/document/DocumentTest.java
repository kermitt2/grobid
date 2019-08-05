package org.grobid.core.document;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

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

    private static byte[] getValidXmlBytes() {
        return "<xml>test</xml>".getBytes();
    }

    private static byte[] getXmlBytesWithInvalidUtf8Sequence() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("<xml>".getBytes());
        out.write(0xe0);
        out.write(0xd8);
        out.write(0x35);
        out.write("</xml>".getBytes());
        return out.toByteArray();
    }

    @Test
    public void shouldNotFailToParseValidXml() throws Exception {
        Document.parseInputStream(
            new ByteArrayInputStream(getValidXmlBytes()),
            SAXParserFactory.newInstance(),
            new DefaultHandler()
        );
    }

    @Test
    public void shouldNotFailToParseInvalidUtf8ByteSequenceXmlByDefault() throws Exception {
        Document.parseInputStream(
            new ByteArrayInputStream(getXmlBytesWithInvalidUtf8Sequence()),
            SAXParserFactory.newInstance(),
            new DefaultHandler()
        );
    }

    @Test(expected = SAXParseException.class)
    public void shouldFailToParseInvalidUtf8ByteSequenceXmlWithValidationEnabled() throws Exception {
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_3RD_PARTY_PDFTOXML_VALIDATION_ENABLED,
            "true"
        );
        Document.parseInputStream(
            new ByteArrayInputStream(getXmlBytesWithInvalidUtf8Sequence()),
            SAXParserFactory.newInstance(),
            new DefaultHandler()
        );
    }
}
