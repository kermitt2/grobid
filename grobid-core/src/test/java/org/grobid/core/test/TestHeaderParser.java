package org.grobid.core.test;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.main.GrobidConstants;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Patrice Lopez
 */
public class TestHeaderParser extends EngineTest {

    private String testPath = null;
    private String newTrainingPath = null;

    private void getTestResourcePath() {
        testPath = GrobidConstants.TEST_RESOURCES_PATH;
        GrobidProperties.getInstance();
        newTrainingPath = GrobidProperties.getTempPath().getAbsolutePath();
    }

    @Test
    public void testHeaderHeader() throws Exception {
        getTestResourcePath();

        String pdfPath = testPath + File.separator + "Wang-paperAVE2008.pdf";
        BiblioItem resHeader = new BiblioItem();

        String tei = engine.processHeader(pdfPath, false, resHeader);

        assertNotNull(resHeader);
        assertThat(resHeader.getTitle(), is("Information Synthesis for Answer Validation"));
        assertThat(resHeader.getKeyword(),
                is("Answer Validation, Recognizing Textual Entailment, Information Synthesis"));
        assertNotNull(resHeader.getFullAuthors());

        //System.out.println(tei);

        pdfPath = testPath + File.separator + "1060._fulltext3.pdf";
        resHeader = new BiblioItem();
        tei = engine.processHeader(pdfPath, false, resHeader);

        assertNotNull(resHeader);
        //System.out.println(tei);

        pdfPath = testPath + File.separator + "ZFN-A-054-0304-0272.pdf";
        resHeader = new BiblioItem();
        tei = engine.processHeader(pdfPath, false, resHeader);

        assertNotNull(resHeader);
        //System.out.println(tei);

        pdfPath = testPath + File.separator + "ZNC-1988-43c-0034.pdf";
        resHeader = new BiblioItem();
        tei = engine.processHeader(pdfPath, false, resHeader);
        //System.out.println(tei);

        //assertNotNull(resHeader);

        pdfPath = testPath + File.separator + "ZNC-1988-43c-0065.pdf";
        resHeader = new BiblioItem();
        tei = engine.processHeader(pdfPath, false, resHeader);

        assertNotNull(resHeader);
        //System.out.println(tei);

    }

    @Test
    public void testSegmentationHeader() throws Exception {
        getTestResourcePath();

        File pdfPath = new File(testPath + File.separator + "Wang-paperAVE2008.pdf");
        BiblioItem resHeader = new BiblioItem();

        String tei = engine.segmentAndProcessHeader(pdfPath, false, resHeader);

        assertNotNull(resHeader);
        assertThat(resHeader.getTitle(), is("Information Synthesis for Answer Validation"));
        assertThat(resHeader.getKeyword(),
                is("Answer Validation, Recognizing Textual Entailment, Information Synthesis"));
        assertNotNull(resHeader.getFullAuthors());
    }
}