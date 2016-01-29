package org.grobid.core.test;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.Block;
import org.grobid.core.main.GrobidConstants;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
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
    public void testFullTextTrainingParser() throws Exception {
        getTestResourcePath();

        String pdfPath = testPath + "/Wang-paperAVE2008.pdf";
        //engine.createTrainingFullText(pdfPath, newTrainingPath, newTrainingPath, 0);

        pdfPath = testPath + "/1001._0908.0054.pdf";
        //engine.createTrainingFullText(pdfPath, newTrainingPath, newTrainingPath, 1);

        pdfPath = testPath + "/submission_161.pdf";
        //engine.createTrainingFullText(pdfPath, newTrainingPath, newTrainingPath, 2);

        pdfPath = testPath + "/submission_363.pdf";
        //engine.createTrainingFullText(pdfPath, newTrainingPath, newTrainingPath, 3);

        pdfPath = testPath + "/ApplPhysLett_98_082505.pdf";
        engine.createTrainingFullText(new File(pdfPath), newTrainingPath, newTrainingPath, 4);

		/*engine.batchCreateTrainingFulltext("/Users/lopez/repository/abstracts/", 
                                         "/Users/lopez/repository/abstracts/training/",
							 			4);*/
    }

    @Test
    public void testFullTextParser() throws Exception {
        getTestResourcePath();

        File pdfPath = new File(testPath, "/Wang-paperAVE2008.pdf");

        Document tei = GrobidFactory.getInstance().createEngine().fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
        //System.out.println(tei);

        //TODO: fix the test

        pdfPath = new File(testPath + "/two_pages.pdf");
        tei = GrobidFactory.getInstance().createEngine().fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);


        pdfPath = new File(testPath + "/MullenJSSv18i03.pdf");
        tei = GrobidFactory.getInstance().createEngine().fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);



        pdfPath = new File(testPath + "/1001._0908.0054.pdf");

        tei = GrobidFactory.getInstance().createEngine().fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
        //System.out.println(tei);

        pdfPath = new File(testPath + "/submission_161.pdf");
        tei = GrobidFactory.getInstance().createEngine().fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
        //System.out.println(tei);

        pdfPath = new File(testPath + "/submission_363.pdf");
        tei = GrobidFactory.getInstance().createEngine().fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
        //System.out.println(tei);

        pdfPath = new File(testPath + "/ApplPhysLett_98_082505.pdf");
        tei = GrobidFactory.getInstance().createEngine().fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);

        pdfPath = new File(testPath + "/1996PRBAConfProc00507417Vos.pdf");
        tei = GrobidFactory.getInstance().createEngine().fullTextToTEIDoc(pdfPath, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);



        //System.out.println(tei);
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



        for (SegmentationLabel l : SegmentationLabel.values()) {
            SortedSet<DocumentPiece> parts = doc.getDocumentPart(l);
            if (parts == null) {
                continue;
            }
            for (DocumentPiece p : parts) {
                DocumentPointer startPtr = p.a;
                DocumentPointer endPtr = p.b;
//                assertEquals(doc.getTokenizations().get(startPtr.getTokenDocPos()),
//                        doc.getBlocks().get(startPtr.getBlockPtr()).getTokens().get(startPtr.getTokenBlockPos()));
//                assertEquals(doc.getTokenizations().get(endPtr.getTokenDocPos()),
//                        doc.getBlocks().get(endPtr.getBlockPtr()).getTokens().get(endPtr.getTokenBlockPos()));

                Block endBlock = doc.getBlocks().get(endPtr.getBlockPtr());
                assertTrue(endPtr.getTokenBlockPos() < endBlock.getTokens().size());
            }
        }

    }

}
