package org.grobid.core.engines;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.AbstractEngineFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class HeaderParserTest {

    private HeaderParser target;

    @BeforeClass
    public static void setInitialContext() throws Exception {
//        MockContext.setInitialContext();
        AbstractEngineFactory.init();
    }

    @AfterClass
    public static void destroyInitialContext() throws Exception {
//        MockContext.destroyInitialContext();
    }

    @Before
    public void setUp() throws Exception {
        EngineParsers parsers = new EngineParsers();
        target = new HeaderParser(parsers);
    }

    // @Test
    // public void testShouldNotUseHeuristics() throws Exception {
    //     File input = new File(this.getClass().getResource("samplePdf.segmentation.pdf").toURI());
    //     DocumentSource doc = DocumentSource.fromPdf(input);

    //     final Document document = new Document(doc);
    //     document.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());
    //     BiblioItem resHeader = new BiblioItem();
    //     String output = target.processingHeaderBlock(0, document, resHeader);

    //     String[] splittedOutput = output.split("\n");

    //     assertThat(splittedOutput.length, is(25));
    //     assertThat(splittedOutput[0], startsWith("Title"));
    //     assertThat(splittedOutput[0], is("Title Title title T Ti Tit Titl BLOCKSTART PAGESTART NEWFONT HIGHERFONT 1 0 INITCAP NODIGIT 0 0 1 0 0 0 0 0 12 12 no 0 10 0 0 0 0 1"));

    //     doc.close(true, true, true);
    // }

    // @Test
    // public void testGetAllLinesFeatures_SimpleDocument_shouldWork() throws Exception {
    //     File input = new File(this.getClass().getResource("samplePdf.segmentation.pdf").toURI());
    //     DocumentSource doc = DocumentSource.fromPdf(input);

    //     final Document document = new Document(doc);
    //     document.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());
    //     BiblioItem resHeader = new BiblioItem();
    //     String output = target.processingHeaderBlock(0, document, resHeader);

    //     String[] splittedOutput = output.split("\n");

    //     assertThat(splittedOutput.length, is(25));
    //     assertThat(splittedOutput[0], startsWith("Title"));
    //     assertThat(splittedOutput[0], is("Title Title title T Ti Tit Titl BLOCKSTART PAGESTART NEWFONT HIGHERFONT 1 0 INITCAP NODIGIT 0 0 1 0 0 0 0 0 12 12 no 0 10 0 0 0 0 1"));

    //     doc.close(true, true, true);
    // }
}