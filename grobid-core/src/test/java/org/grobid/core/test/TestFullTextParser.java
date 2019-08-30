package org.grobid.core.test;

import org.apache.commons.io.FileUtils;
import org.easymock.Mock;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.label.SegmentationLabels;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import nu.xom.Element;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Patrice Lopez
 */
public class TestFullTextParser extends EngineTest {

    @BeforeClass
    public static void init() {
        GrobidProperties.getInstance();
    }

    @AfterClass
    public static void tearDown() {
        GrobidFactory.reset();
    }

    @Test
    public void testFullTextParser_1() throws Exception {
        File inputTmpFile = getInputDocument("/test/Wang-paperAVE2008.pdf");

        Document tei = engine.fullTextToTEIDoc(inputTmpFile, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
    }

    private File getInputDocument(String inputPath) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(inputPath);
        File inputTmpFile = File.createTempFile("tmpFileTest", "testFullTextParser");
        inputTmpFile.deleteOnExit();

        FileUtils.copyToFile(is, inputTmpFile);

        return inputTmpFile;
    }

    @Test
    public void testFullTextParser_2() throws Exception {
        File inputTmpFile = getInputDocument("/test/two_pages.pdf");

        Document tei = engine.fullTextToTEIDoc(inputTmpFile, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
    }

    @Test
    public void testFullTextParser_3() throws Exception {
        File inputTmpFile = getInputDocument("/test/MullenJSSv18i03.pdf");
        Document tei = engine.fullTextToTEIDoc(inputTmpFile, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
    }

    @Test
    public void testFullTextParser_4() throws Exception {
        File inputTmpFile = getInputDocument("/test/1001._0908.0054.pdf");
        Document tei = engine.fullTextToTEIDoc(inputTmpFile, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
    }

    @Test
    public void testFullTextParser_5() throws Exception {
        File inputTmpFile = getInputDocument("/test/submission_161.pdf");
        Document tei = engine.fullTextToTEIDoc(inputTmpFile, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
    }

    @Test
    public void testFullTextParser_6() throws Exception {
        File inputTmpFile = getInputDocument("/test/submission_363.pdf");
        Document tei = engine.fullTextToTEIDoc(inputTmpFile, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
    }

    @Test
    public void testFullTextParser_7() throws Exception {
        File inputTmpFile = getInputDocument("/test/ApplPhysLett_98_082505.pdf");
        Document tei = engine.fullTextToTEIDoc(inputTmpFile, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
    }

    @Test
    public void testFullTextParser_8() throws Exception {
        File inputTmpFile = getInputDocument("/test/1996PRBAConfProc00507417Vos.pdf");
        Document tei = engine.fullTextToTEIDoc(inputTmpFile, GrobidAnalysisConfig.defaultInstance());
        assertTei(tei);
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
                DocumentPointer startPtr = p.getLeft();
                DocumentPointer endPtr = p.getRight();

                Block endBlock = doc.getBlocks().get(endPtr.getBlockPtr());
                assertTrue(endPtr.getTokenBlockPos() < endBlock.getTokens().size());
            }
        }

    }

    @Test
    public void testGetDocumentPieces1() throws Exception {
//        Document documentMock = createMock(Document.class);
//
//        List<LayoutToken> sentence1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is a sentence");
//
//        List<LayoutToken> sentence2 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is another sentence, somewhere else.");
//
//        // Faking block pointers
//        sentence1.get(0).setBlockPtr(0);
//        sentence1.get(6).setBlockPtr(1);
//
//        sentence2.get(0).setBlockPtr(2);
//        sentence2.get(12).setBlockPtr(3);
//
//        // First sentence blocks
//        Block fakeBlock1_1 = new Block();
//        fakeBlock1_1.setStartToken(12345);
//        Block fakeBlock1_2 = new Block();
//        fakeBlock1_2.setStartToken(12345);
//
//        // Second sentence blocks
//        Block fakeBlock2_1 = new Block();
//        fakeBlock2_1.setStartToken(25000);
//        Block fakeBlock2_2 = new Block();
//        fakeBlock2_2.setStartToken(25000);
//
//        List<Block> blocks = new ArrayList<>();
//        blocks.add(fakeBlock1_1);
//        blocks.add(fakeBlock1_2);
//        blocks.add(fakeBlock2_1);
//        blocks.add(fakeBlock2_2);
//
//
//        //Moving this sentence somewhere else
//        sentence1.stream().peek(l -> l.setOffset(l.getOffset() + 12345));
//        sentence2.stream().peek(l -> l.setOffset(l.getOffset() + 25000));
//
//        List<LayoutToken> layoutTokens = new ArrayList<>();
//        layoutTokens.addAll(sentence1Far);
//        layoutTokens.addAll(sentence2Far);
//
//        expect(documentMock.getBlocks()).andReturn(blocks);
//        expect(documentMock.getTokenizations()).andReturn(layoutTokens);
//        expect(documentMock.getBlocks()).andReturn(blocks);
//        expect(documentMock.getTokenizations()).andReturn(layoutTokens);
//        expect(documentMock.getBlocks()).andReturn(blocks);
//
//        expect(documentMock.getBlocks()).andReturn(blocks);
//        expect(documentMock.getBlocks()).andReturn(blocks);
//
//        replay(documentMock);
//        engine.getParsers().getFullTextParser().getDocumentPieces1(layoutTokens, documentMock);
//        verify(documentMock);


    }

}
