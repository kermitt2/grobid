package org.grobid.core.lang.impl;

import org.grobid.core.utilities.OffsetPosition;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class PragmaticSentenceDetectorTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testGetSentenceSpans() {
        String original_text = "This is the original text.   Some spaces are going to be removed.";
        List<String> sentences = Arrays.asList("This is the original text.", "Some spaces are going to be removed.");
        List<OffsetPosition> sentence_spans = PragmaticSentenceDetector.getSentenceOffsets(original_text, sentences);

        assertThat(sentence_spans, hasSize(2));
        assertThat(sentence_spans.get(0).start, is(0));
        assertThat(sentence_spans.get(0).end, is(26));
        assertThat(sentence_spans.get(1).start, is(29));
        assertThat(sentence_spans.get(1).end, is(65));
    }

    @Test
    public void testGetSentenceSpanMismatchFirstSentence() {
        String original_text = "This is the   original text.   Some spaces are going to be removed.";
        List<String> sentences = Arrays.asList("This is the original text.", "Some spaces are going to be removed.");
        List<OffsetPosition> sentence_spans = PragmaticSentenceDetector.getSentenceOffsets(original_text, sentences);

        assertThat(sentence_spans, hasSize(2));
        assertThat(sentence_spans.get(0).start, is(0));
        assertThat(sentence_spans.get(0).end, is(28));
        assertThat(original_text.substring(sentence_spans.get(0).start, sentence_spans.get(0).end), is("This is the   original text."));
        assertThat(sentence_spans.get(1).start, is(31));
        assertThat(sentence_spans.get(1).end, is(67));
        assertThat(original_text.substring(sentence_spans.get(1).start, sentence_spans.get(1).end), is("Some spaces are going to be removed."));
    }


    @Test
    public void testGetSentenceSpanMismatchSecondSentence() {
        String original_text = "This is the original text.   Some spaces are    going to be removed.";
        List<String> sentences = Arrays.asList("This is the original text.", "Some spaces are going to be removed.");
        List<OffsetPosition> sentence_spans = PragmaticSentenceDetector.getSentenceOffsets(original_text, sentences);

        assertThat(sentence_spans, hasSize(2));
        assertThat(sentence_spans.get(0).start, is(0));
        assertThat(sentence_spans.get(0).end, is(26));
        assertThat(original_text.substring(sentence_spans.get(0).start, sentence_spans.get(0).end), is("This is the original text."));
        assertThat(sentence_spans.get(1).start, is(29));
        assertThat(sentence_spans.get(1).end, is(68));
        assertThat(original_text.substring(sentence_spans.get(1).start, sentence_spans.get(1).end), is("Some spaces are    going to be removed."));
    }

    @Test
    public void testGetSentenceSpanMismatchSecondSentence_sameSentence() {
        String original_text = "This is the original text.   This is the    original text.";
        List<String> sentences = Arrays.asList("This is the original text.", "This is the    original text.");
        List<OffsetPosition> sentence_spans = PragmaticSentenceDetector.getSentenceOffsets(original_text, sentences);

        assertThat(sentence_spans, hasSize(2));
        assertThat(sentence_spans.get(0).start, is(0));
        assertThat(sentence_spans.get(0).end, is(26));
        assertThat(original_text.substring(sentence_spans.get(0).start, sentence_spans.get(0).end), is("This is the original text."));
        assertThat(sentence_spans.get(1).start, is(29));
        assertThat(sentence_spans.get(1).end, is(58));
        assertThat(original_text.substring(sentence_spans.get(1).start, sentence_spans.get(1).end), is("This is the    original text."));
    }

    @Test
    public void testGetSentenceSpanMismatchAllSentences() {
        String original_text = "This is the    original text.   Some spaces are    going to be removed.";
        List<String> sentences = Arrays.asList("This is the original text.", "Some spaces are going to be removed.");
        List<OffsetPosition> sentence_spans = PragmaticSentenceDetector.getSentenceOffsets(original_text, sentences);

        assertThat(sentence_spans, hasSize(2));
        assertThat(sentence_spans.get(0).start, is(0));
        assertThat(sentence_spans.get(0).end, is(29));
        assertThat(original_text.substring(sentence_spans.get(0).start, sentence_spans.get(0).end), is("This is the    original text."));
        assertThat(sentence_spans.get(1).start, is(32));
        assertThat(sentence_spans.get(1).end, is(71));
        assertThat(original_text.substring(sentence_spans.get(1).start, sentence_spans.get(1).end), is("Some spaces are going to be removed."));
    }

    @Test
    public void testGetSentenceSpanMismatch_realCase() {
        String original_text = "Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean  density) in our thermal balance runs. Most of these runs show two stages of evolution -the first being a turbulent steady state and the second reflecting thermal instability that leads to multiphase condensation. The first stage occurs after an eddy turnover time scale for most of our runs. It depends on the amplitude of forcing, and thus on the parameter f turb (the fraction of turbulent heating). The second stage of evolution has much higher density fluctuations ( δρ rms / ρ ≥ 1). In this stage, the gas separates into hot and cold phases due to thermal instability. The multiphase gas formation time scale (t mp ) is very different for different parameter choices.";
        List<String> sentences = Arrays.asList("Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean density) in our thermal balance runs.");
        List<OffsetPosition> sentence_spans = PragmaticSentenceDetector.getSentenceOffsets(original_text, sentences);

        assertThat(sentence_spans, hasSize(1));
        assertThat(sentence_spans.get(0).start, is(0));
        assertThat(sentence_spans.get(0).end, is(143));
        assertThat(original_text.substring(sentence_spans.get(0).start, sentence_spans.get(0).end), is("Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean  density) in our thermal balance runs."));
    }

//    def test_find_in_text_mismatch_real_case(self):
//    text = "Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean  density) in our thermal balance runs. Most of these runs show two stages of evolution -the first being a turbulent steady state and the second reflecting thermal instability that leads to multiphase condensation. The first stage occurs after an eddy turnover time scale for most of our runs. It depends on the amplitude of forcing, and thus on the parameter f turb (the fraction of turbulent heating). The second stage of evolution has much higher density fluctuations ( δρ rms / ρ ≥ 1). In this stage, the gas separates into hot and cold phases due to thermal instability. The multiphase gas formation time scale (t mp ) is very different for different parameter choices."
//    sentence = "Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean density) in our thermal balance runs."
//
//    in_text, start = find_in_text(sentence, text)
//        assert start == 0
//        assert in_text == 'Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean  density) in our thermal balance runs.'
//
//    def test_find_in_text(self):
//    sentence = 'This is the original text.'
//    text = 'This is the   original text.   Some spaces are going to be removed.'
//
//    in_text, start = find_in_text(sentence, text)
//
//        assert start == 0
//        assert in_text == 'This is the   original text.'


    }