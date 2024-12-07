package org.grobid.core.utilities;

import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.engines.DateParser;
import org.grobid.core.lang.SentenceDetector;
import org.grobid.core.lang.SentenceDetectorFactory;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;
import static org.powermock.api.easymock.PowerMock.*;
import org.junit.Ignore;

// Patrice @Luca this class is failing to run with JDK 1.17 and maybe lower versions (not tried), possibly security reasons,
// and I am not able to understand why with the complexity introduced by powermock in initialization.
// Could we move back to something simpler and readable maybe? 

@Ignore
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.grobid.core.lang.SentenceDetectorFactory")
@PrepareForTest({SentenceUtilities.class})
public class SentenceUtilitiesTest {

    SentenceDetectorFactory sentenceDetectorFactoryMock;
    SentenceDetector sentenceDetectorMock;
    SentenceUtilities target;

    @Before
    public void setUp() {
        GrobidProperties.getInstance();
        GrobidConfig.ModelParameters modelParameters = new GrobidConfig.ModelParameters();
        modelParameters.name = "bao";
        GrobidProperties.addModel(modelParameters);

        sentenceDetectorFactoryMock = createMock(SentenceDetectorFactory.class);
        sentenceDetectorMock = createMock(SentenceDetector.class);
        target = SentenceUtilities.getInstance();
        Whitebox.setInternalState(target, sentenceDetectorFactoryMock);
    }

    @Test
    public void testNullText() throws Exception {
        String text = null;
        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text);
        assertNull(theSentences);
    }

    @Test
    public void testEmptyText() throws Exception {
        String text = "";
        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(text)).andReturn(new ArrayList<>());
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);
        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text);

        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        assertThat(theSentences.size(), is(0));
    }

    @Test
    public void testOneSentenceText() throws Exception {
        String text = "Bla bla bla.";
        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(text)).andReturn(Arrays.asList(new OffsetPosition(0, 12)));
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);

        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text);
        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        assertThat(theSentences.size(), is(1));
    }

    @Test
    public void testTwoSentencesText() throws Exception {
        String text = "Bla bla bla. Bli bli bli.";
        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(text)).andReturn(Arrays.asList(new OffsetPosition(0, 12), new OffsetPosition(13, 24)));
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);

        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text);
        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        assertThat(theSentences.size(), is(2));
    }

    @Test
    public void testTwoSentencesTextWithUselessForbidden() throws Exception {
        String text = "Bla bla bla. Bli bli bli.";
        List<OffsetPosition> forbidden = new ArrayList<>();
        forbidden.add(new OffsetPosition(2, 8));

        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(text, null)).andReturn(Arrays.asList(new OffsetPosition(0, 12), new OffsetPosition(13, 24)));
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);

        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text, forbidden);
        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        assertThat(theSentences.size(), is(2));
    }

    @Test
    public void testTwoSentencesTextWithUsefullForbidden() throws Exception {
        String text = "Bla bla bla. Bli bli bli.";
        List<OffsetPosition> forbidden = new ArrayList<>();
        forbidden.add(new OffsetPosition(2, 8));
        forbidden.add(new OffsetPosition(9, 15));

        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(text, null)).andReturn(Arrays.asList(new OffsetPosition(0, 12), new OffsetPosition(13, 24)));
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);

        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text, forbidden);

        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        assertThat(theSentences.size(), is(1));
    }

    @Test
    public void testCorrectSegmentation_shouldNotCancelSegmentation() throws Exception {
        String paragraph = "This is a sentence. [3] Another sentence.";

        List<String> refs = Arrays.asList("[3]");
        List<String> sentences = Arrays.asList("This is a sentence.", "Another sentence.");

        List<OffsetPosition> refSpans = getPositions(paragraph, refs);
        List<OffsetPosition> sentenceSpans = getPositions(paragraph, sentences);

        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(paragraph, null)).andReturn(sentenceSpans);
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);

        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(paragraph, refSpans);
        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        assertThat(theSentences.size(), is(2));
    }

    @Test
    public void testCorrectSegmentation_shouldNotCancelSegmentation2() throws Exception {
        String paragraph = "This is a sentence [3] and the continuing sentence.";

        List<String> refs = Arrays.asList("[3]");
        List<String> sentences = Arrays.asList("This is a sentence", "and the continuing sentence.");

        List<OffsetPosition> refSpans = getPositions(paragraph, refs);
        List<OffsetPosition> sentenceSpans = getPositions(paragraph, sentences);

        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(paragraph, null)).andReturn(sentenceSpans);
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);

        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(paragraph, refSpans);
        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        assertThat(theSentences.size(), is(2));
    }

    @Test
    public void testCorrectSegmentation_shouldCancelWrongSegmentation() throws Exception {
        String paragraph = "(Foppiano and al. 2021) explains what he's thinking.";

        List<String> refs = Arrays.asList("(Foppiano and al. 2021)");
        List<String> sentences = Arrays.asList("(Foppiano and al.", "2021) explains what he's thinking.");

        List<OffsetPosition> refSpans = getPositions(paragraph, refs);
        List<OffsetPosition> sentenceSpans = getPositions(paragraph, sentences);

        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(paragraph, null)).andReturn(sentenceSpans);
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);

        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(paragraph, refSpans);
        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        assertThat(theSentences.size(), is(1));
    }

    @Test
    public void testCorrectSegmentation_shouldCancelWrongSegmentation2() throws Exception {
        String paragraph = "What we claim corresponds with what (Foppiano and al. 2021) explains what he's thinking.";

        List<String> refs = Arrays.asList("(Foppiano and al. 2021)");
        List<String> sentences = Arrays.asList("What we claim corresponds with what (Foppiano and al.", "2021) explains what he's thinking.");

        List<OffsetPosition> refSpans = getPositions(paragraph, refs);
        List<OffsetPosition> sentenceSpans = getPositions(paragraph, sentences);

        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(paragraph, null)).andReturn(sentenceSpans);
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);

        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(paragraph, refSpans);
        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        assertThat(theSentences.size(), is(1));
    }

    @Test
    public void testRealCaseDesynchronisation_shouldReturnCorrectSentences() throws Exception {
        String text = "CCAligned ) is a 119language 1 parallel dataset built off 68 snapshots of Common Crawl. Documents are aligned if they are in the same language according to FastText LangID (Joulin et al., 2016(Joulin et al., , 2017, and have the same URL but for a differing language code. These alignments are refined with cross-lingual LASER embeddings (Artetxe and Schwenk, 2019). For sentence-level data, they split on newlines and align with LASER, but perform no further filtering. Human annotators evaluated the quality of document alignments for six languages (de, zh, ar, ro, et, my) selected for their different scripts and amount of retrieved documents, reporting precision of over 90%. The quality of the extracted parallel sentences is evaluated in a machine translation (MT) task on six European (da, cr, sl, sk, lt, et) languages of the TED corpus (Qi et al., 2018), where it compares favorably to systems built on crawled sentences from WikiMatrix and ParaCrawl   (Qi et al., 2018); WMT-5: cs, de, fi, lv, ro. POS/DEP-5: part-of-speech labeling and dependency parsing for bg, ca, da, fi, id.";

        String textLayoutToken = "CCAligned (El-Kishky et al., 2020) is a 119-\n" +
            "language 1 parallel dataset built off 68 snapshots \n" +
            "of Common Crawl. Documents are aligned if they \n" +
            "are in the same language according to FastText \n" +
            "LangID (Joulin et al., 2016, 2017), and have the \n" +
            "same URL but for a differing language code. These \n" +
            "alignments are refined with cross-lingual LASER \n" +
            "embeddings (Artetxe and Schwenk, 2019). For \n" +
            "sentence-level data, they split on newlines and \n" +
            "align with LASER, but perform no further filtering. \n" +
            "Human annotators evaluated the quality of docu-\n" +
            "ment alignments for six languages (de, zh, ar,  ro, et, my) selected for their different scripts and \n" +
            "amount of retrieved documents, reporting precision \n" +
            "of over 90%. The quality of the extracted paral-\n" +
            "lel sentences is evaluated in a machine translation \n" +
            "(MT) task on six European (da, cr, sl, sk, lt,  et) languages of the TED corpus(Qi et al., 2018), \n" +
            "where it compares favorably to systems built on \n" +
            "crawled sentences from WikiMatrix and ParaCrawl \n" +
            "(Qi et al., 2018); WMT-5: cs, \n" +
            "de, fi, lv, ro. POS/DEP-5: part-of-speech labeling and dependency parsing for bg, ca, da, fi, id. \n" +
            "\n";

        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(textLayoutToken);
        
        tokens.get(25).setSuperscript(true);

        List<OffsetPosition> referencesSpans = Arrays.asList(
            new OffsetPosition(172, 192),
            new OffsetPosition(192, 214),
            new OffsetPosition(338, 365),
            new OffsetPosition(551, 575),
            new OffsetPosition(793, 817),
            new OffsetPosition(846, 863),
            new OffsetPosition(963, 980)
        );
        
        List<OffsetPosition> sentencesPositions = Arrays.asList(
            new OffsetPosition(0, 87),
            new OffsetPosition(88, 272),
            new OffsetPosition(273, 366),
            new OffsetPosition(367, 470),
            new OffsetPosition(471, 680),
            new OffsetPosition(681, 1008),
            new OffsetPosition(1009, 1090)
        );

        expect(sentenceDetectorFactoryMock.getInstance()).andReturn(sentenceDetectorMock);
        expect(sentenceDetectorMock.detect(text, null)).andReturn(sentencesPositions);
        replay(sentenceDetectorFactoryMock, sentenceDetectorMock);

        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text, referencesSpans, tokens, null);
        verify(sentenceDetectorFactoryMock, sentenceDetectorMock);
        
        assertThat(theSentences.size(), is(7));
        
        assertThat(theSentences.get(0).start, is(0));
        assertThat(theSentences.get(0).end, is(87));

        assertThat(theSentences.get(1).start, is(88));
        assertThat(theSentences.get(1).end, is(272));

        assertThat(theSentences.get(2).start, is(273));
        assertThat(theSentences.get(2).end, is(366));

        assertThat(theSentences.get(3).start, is(367));
        assertThat(theSentences.get(3).end, is(470));

        assertThat(theSentences.get(4).start, is(471));
        assertThat(theSentences.get(4).end, is(680));

        assertThat(theSentences.get(5).start, is(681));
        assertThat(theSentences.get(5).end, is(1008));

        assertThat(theSentences.get(6).start, is(1009));
        assertThat(theSentences.get(6).end, is(1090));
    }

    private List<OffsetPosition> getPositions(String paragraph, List<String> refs) {
        List<OffsetPosition> positions = new ArrayList<>();
        int previousRefEnd = 0;
        for (String ref : refs) {
            int startRef = paragraph.indexOf(ref, previousRefEnd);
            int endRef = startRef + ref.length();

            positions.add(new OffsetPosition(startRef, endRef));
            previousRefEnd = endRef;
        }

        return positions;
    }
}