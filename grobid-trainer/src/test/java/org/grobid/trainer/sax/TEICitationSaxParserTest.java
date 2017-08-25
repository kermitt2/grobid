package org.grobid.trainer.sax;

import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class TEICitationSaxParserTest {

    TEICitationSaxParser target;
    SAXParserFactory spf;


    @Before
    public void setUp() throws Exception {
        spf = SAXParserFactory.newInstance();
        target = new TEICitationSaxParser();
    }

    @Test
    public void testCitation() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/31-1708.04410.training.references.tei.xml");

        final SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        assertThat(target.getLabeledResult(), hasSize(25));
    }

}