package org.grobid.core.sax;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.document.DocumentNode;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;

import static org.easymock.EasyMock.createMock;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PDFALTOOutlineSaxParserTest {
    SAXParserFactory spf = SAXParserFactory.newInstance();

    PDFALTOOutlineSaxHandler target;
    DocumentSource mockDocumentSource;
    Document document;

    @Before
    public void setUp() throws Exception {

        mockDocumentSource = createMock(DocumentSource.class);

        document = Document.createFromText("");
        target = new PDFALTOOutlineSaxHandler(document);
    }

    @Test
    public void testParsing_pdf2XMLOutline_ShouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("pdfalto.xml_outline.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        DocumentNode root = target.getRootNode();
        assertTrue(root.getChildren().size() > 0);
        assertThat(root.getChildren(), hasSize(9));
    }

}