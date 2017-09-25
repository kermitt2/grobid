package org.grobid.core.test;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.label.SegmentationLabels;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.Block;
import org.grobid.core.main.GrobidConstants;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Patrice Lopez
 */
public class TestFullTextParser extends EngineTest {

    private String testPath = null;
    private String newTrainingPath = null;

    @BeforeClass
    public static void init() {
        GrobidProperties.getInstance();
    }

    @Before
    public void setUp() {
        newTrainingPath = GrobidProperties.getTempPath().getAbsolutePath();
    }

    private void getTestResourcePath() {
        testPath = GrobidConstants.TEST_RESOURCES_PATH;
    }

    @Test
    public void testFullTextParser() throws Exception {
        getTestResourcePath();

        File pdfPath = new File(testPath, "/Wang-paperAVE2008.pdf");

        Engine engine = GrobidFactory.getInstance().getEngine();
        Document tei = engine.fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
        //System.out.println(tei);

        //TODO: fix the test

        pdfPath = new File(testPath + File.separator + "two_pages.pdf");
        tei = engine.fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);


        pdfPath = new File(testPath + File.separator + "MullenJSSv18i03.pdf");
        tei = engine.fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);



        pdfPath = new File(testPath + File.separator + "1001._0908.0054.pdf");

        tei = engine.fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
        //System.out.println(tei);

        pdfPath = new File(testPath + File.separator + "submission_161.pdf");
        tei = engine.fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
        //System.out.println(tei);

        pdfPath = new File(testPath + File.separator + "submission_363.pdf");
        tei = engine.fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
        //System.out.println(tei);

        pdfPath = new File(testPath + File.separator + "ApplPhysLett_98_082505.pdf");
        tei = engine.fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);

        pdfPath = new File(testPath + File.separator + "1996PRBAConfProc00507417Vos.pdf");
        tei = engine.fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);

        engine.close();

    }

    private void assertTei(Document doc) {
        assertDocAndBlockTokenizationSync(doc);
        assertNotNull(doc.getTei());

        //check that XML is valid
        XmlBuilderUtils.fromString(doc.getTei());
    }

    private void assertDocAndBlockTokenizationSync(Document doc) {
        List<Block> blocks = doc.getBlocks();

        for (Block block : blocks) {
            int start = block.getStartToken();
            int end = block.getEndToken();

            if (start == -1) {
                continue;
            }

            for (int i = start; i <= end; i++) {
                assertEquals(doc.getTokenizations().get(i), block.getTokens().get(i - start));
            }
//            assertTrue(endPtr.getTokenBlockPos() < endBlock.getTokens().size());
        }

        for (TaggingLabel l : Arrays.asList(SegmentationLabels.BODY, SegmentationLabels.REFERENCES, SegmentationLabels.HEADER, SegmentationLabels.ACKNOWLEDGEMENT, SegmentationLabels.ANNEX,
                SegmentationLabels.FOOTNOTE, SegmentationLabels.HEADNOTE, SegmentationLabels.TOC)) {
            SortedSet<DocumentPiece> parts = doc.getDocumentPart(l);
            if (parts == null) {
                continue;
            }
            for (DocumentPiece p : parts) {
                DocumentPointer startPtr = p.a;
                DocumentPointer endPtr = p.b;

                Block endBlock = doc.getBlocks().get(endPtr.getBlockPtr());
                assertTrue(endPtr.getTokenBlockPos() < endBlock.getTokens().size());
            }
        }

    }

}
