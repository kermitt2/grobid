package org.grobid.core.sax;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.LayoutToken;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by lfoppiano on 21/07/16.
 */
public class PDF2XMLSaxParserTest {
    SAXParserFactory spf = SAXParserFactory.newInstance();

    PDF2XMLSaxHandler target;
    DocumentSource mockDocumentSource;
    Document document;
    private List<GraphicObject> images;

    @Before
    public void setUp() throws Exception {

        mockDocumentSource = createMock(DocumentSource.class);

        document = Document.createFromText("");
        images = new ArrayList<>();
        target = new PDF2XMLSaxHandler(document, images);
    }

    @Test
    public void testParsing_pdf2XMLwithNoIMages_ShouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("pdf2xml_noImages.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<LayoutToken> tokenList = target.getTokenization();

        assertTrue(tokenList.size() > 0);
        assertTrue(document.getImages().size() == 0);
        assertTrue(images.size() == 0);
        assertTrue(document.getPages().size() == 4);
        assertTrue(document.getBlocks().size() == 26);
    }

    @Test
    public void testParsing_pdf2XMLwithIMages_ShouldWork() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("pdf2xml_Images.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(inputStream, target);

        List<LayoutToken> tokenList = target.getTokenization();

        assertTrue(tokenList.size() > 0);
        assertThat(images.size(), is(17));
        assertThat(document.getImages().size(), is(17));
        assertTrue(document.getPages().size() == 4);
        assertTrue(document.getBlocks().size() == 26);
    }

}