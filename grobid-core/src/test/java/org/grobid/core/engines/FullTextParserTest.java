package org.grobid.core.engines;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class FullTextParserTest {

    private FullTextParser target;

    @Before
    public void setUp() throws Exception {
        target = new FullTextParser(new EngineParsers());
    }

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
        GrobidProperties.getInstance();
    }

    @AfterClass
    public static void tearDown() {
        GrobidFactory.reset();
    }

//    @Test
//    public void testProcess2() throws Exception {
//        String text = "(a) shows the temperature variation of the 31 P-\n" +
//            "NMR spectrum for x ¼ 0:25, which was obtained by \n" +
//            "sweeping magnetic fields. A single sharp spectrum was \n" +
//            "observed above T N , but no anomaly was detected in the NMR spectrum at the structural transition T S determined by \n" +
//            "xx . Below T N , a broad NMR spectrum with a Gaussian \n" +
//            "shape develops gradually and coexists with a sharp peak at \n" +
//            "around T on \n" +
//            "c $ 30 K. We measured 1=T 1 at the sharp and \n" +
//            "broad peaks shown by the solid black and dashed red arrows, \n" +
//            "respectively. ";
//
//        Document documentMock = createMock(Document.class);
//        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
//
//        target.processShort2(layoutTokens, documentMock);
//
//    }

    /*@Test
    public void testGetDocumentPieces1() throws Exception {
        Document documentMock = createMock(Document.class);

        List<LayoutToken> sentence1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is a sentence");

        List<LayoutToken> sentence2 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is another sentence, somewhere else.");

        // Faking block pointers
        sentence1.get(0).setBlockPtr(0);
        sentence1.get(6).setBlockPtr(1);

        sentence2.get(0).setBlockPtr(2);
        sentence2.get(12).setBlockPtr(3);

        // First sentence blocks
        Block fakeBlock1_1 = new Block();
        fakeBlock1_1.setStartToken(12345);
        Block fakeBlock1_2 = new Block();
        fakeBlock1_2.setStartToken(12347);

        // Second sentence blocks
        Block fakeBlock2_1 = new Block();
        fakeBlock2_1.setStartToken(25000);
        Block fakeBlock2_2 = new Block();
        fakeBlock2_2.setStartToken(25088);

        List<Block> blocks = new ArrayList<>();
        blocks.add(fakeBlock1_1);
        blocks.add(fakeBlock1_2);
        blocks.add(fakeBlock2_1);
        blocks.add(fakeBlock2_2);


        //Moving this sentence somewhere else
//        sentence1.stream().peek(l -> l.setOffset(l.getOffset() + 12345));
//        sentence2.stream().peek(l -> l.setOffset(l.getOffset() + 25000));

        List<LayoutToken> sentence1Far = sentence1.stream().peek(l -> l.setOffset(l.getOffset() + 12345)).collect(Collectors.toList());
        List<LayoutToken> sentence2Far = sentence2.stream().peek(l -> l.setOffset(l.getOffset() + 25000)).collect(Collectors.toList());

        List<LayoutToken> layoutTokens = new ArrayList<>();
        layoutTokens.addAll(sentence1Far);
        layoutTokens.addAll(sentence2Far);

        expect(documentMock.getBlocks()).andReturn(blocks).anyTimes();
        expect(documentMock.getTokenizations()).andReturn(layoutTokens).anyTimes();


        replay(documentMock);
        SortedSet<DocumentPiece> documentPieces = target.collectPiecesFromLayoutTokens(layoutTokens, documentMock);
        verify(documentMock);

        List<DocumentPiece> documentPieces1 = new ArrayList<>(documentPieces);

        assertThat(documentPieces1, hasSize(2));

        assertThat(documentPieces1.get(0).getLeft().getBlockPtr(), is(0));
        assertThat(documentPieces1.get(0).getLeft().getTokenDocPos(), is(12345));
        assertThat(documentPieces1.get(0).getRight().getBlockPtr(), is(1));
        assertThat(documentPieces1.get(0).getRight().getTokenDocPos(), is(12347));

        assertThat(documentPieces1.get(1).getLeft().getBlockPtr(), is(2));
        assertThat(documentPieces1.get(1).getLeft().getTokenDocPos(), is(25000));
        assertThat(documentPieces1.get(1).getRight().getBlockPtr(), is(3));
        assertThat(documentPieces1.get(1).getRight().getTokenDocPos(), is(25088));
    }*/

}