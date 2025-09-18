package org.grobid.core.lang.impl;

import org.apache.commons.lang3.tuple.Pair;
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
    public void testGetSentenceOffsets() {
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
    public void testGetSentenceOffsetsMismatchFirstSentence() {
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
    public void testGetSentenceOffsetsMismatchSecondSentence() {
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
    public void testGetSentenceOffsetsMismatchSecondSentence_sameSentence() {
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
    public void testGetSentenceOffsetsMismatchAllSentences() {
        String original_text = "This is the    original text.   Some spaces are    going to be removed.";
        List<String> sentences = Arrays.asList("This is the original text.", "Some spaces are going to be removed.");
        List<OffsetPosition> sentence_spans = PragmaticSentenceDetector.getSentenceOffsets(original_text, sentences);

        assertThat(sentence_spans, hasSize(2));
        assertThat(sentence_spans.get(0).start, is(0));
        assertThat(sentence_spans.get(0).end, is(29));
        assertThat(original_text.substring(sentence_spans.get(0).start, sentence_spans.get(0).end), is("This is the    original text."));
        assertThat(sentence_spans.get(1).start, is(32));
        assertThat(sentence_spans.get(1).end, is(71));
        assertThat(original_text.substring(sentence_spans.get(1).start, sentence_spans.get(1).end), is("Some spaces are    going to be removed."));
    }

    @Test
    public void testGetSentenceOffsetsMismatch_realCase() {
        String original_text = "Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean  density) in our thermal balance runs. Most of these runs show two stages of evolution -the first being a turbulent steady state and the second reflecting thermal instability that leads to multiphase condensation. The first stage occurs after an eddy turnover time scale for most of our runs. It depends on the amplitude of forcing, and thus on the parameter f turb (the fraction of turbulent heating). The second stage of evolution has much higher density fluctuations ( δρ rms / ρ ≥ 1). In this stage, the gas separates into hot and cold phases due to thermal instability. The multiphase gas formation time scale (t mp ) is very different for different parameter choices.";
        List<String> sentences = Arrays.asList("Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean density) in our thermal balance runs.");
        List<OffsetPosition> sentence_spans = PragmaticSentenceDetector.getSentenceOffsets(original_text, sentences);

        assertThat(sentence_spans, hasSize(1));
        assertThat(sentence_spans.get(0).start, is(0));
        assertThat(sentence_spans.get(0).end, is(143));
        assertThat(original_text.substring(sentence_spans.get(0).start, sentence_spans.get(0).end), is("Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean  density) in our thermal balance runs."));
    }

    @Test
    public void testFindInText() throws Exception {
        String originalText = "This is the   original text.   Some spaces are going to be removed.";
        List<String> sentences = Arrays.asList("This is the original text.", "Some spaces are going to be removed.");
        Pair<String, Integer> inText = PragmaticSentenceDetector.findInText(sentences.get(0), originalText);

        assertThat(inText.getRight(), is(0));
        assertThat(inText.getLeft(), is("This is the   original text."));
    }


    @Test
    public void testFindInText_mismatchRealCase() throws Exception {
        String originalText = "Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean  density) in our thermal balance runs. Most of these runs show two stages of evolution -the first being a turbulent steady state and the second reflecting thermal instability that leads to multiphase condensation. The first stage occurs after an eddy turnover time scale for most of our runs. It depends on the amplitude of forcing, and thus on the parameter f turb (the fraction of turbulent heating). The second stage of evolution has much higher density fluctuations ( δρ rms / ρ ≥ 1). In this stage, the gas separates into hot and cold phases due to thermal instability. The multiphase gas formation time scale (t mp ) is very different for different parameter choices.";
        String sentence = "Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean density) in our thermal balance runs.";

        Pair<String, Integer> inText = PragmaticSentenceDetector.findInText(sentence, originalText);

        assertThat(inText.getRight(), is(0));
        assertThat(inText.getLeft(), is("Figure 5 shows the time evolution of the volumeaveraged rms density fluctuations (normalized to the mean  density) in our thermal balance runs."));
    }

    @Test
    public void testFindInText_errorCase() throws Exception {
        String originalText = "In two species of toads and in Salamandra, which are among the most terrestrial of lissamphibians, SF were developed more prominently (i.e. the fibres were thicker and covered greater area of the section) than in species spending more time in water, such as the fire-bellied toads (compare Fig. 4b, c, g with a). Thus, one could suspect that this may be related to greater forces acting on the limbs during terrestrial locomotion. However, SF are very well developed in femora and humeri of the aquatic Chinese salamander Andrias davidianus (Canoville et al. 2018), as well as in the Triassic temnospondyl Metoposaurus krasiejowensis, which is interpreted as almost exclusively aquatic (nonetheless, this amphibian was probably able to burrow—this requires strong muscles, which would be consistent with the presence of well developed SF; Konietzko-Meier and Sander 2013). On the other hand, we did not observe well developed SF in P. fuscus, despite partially burrowing lifestyle of this amphibian. However, in closely related P. varaldii these fibres can readily be observed in at least some specimens (Guarino et al. 2011). Also, it should be noted that the presence of SF may be dependent on a number of physiological stimuli, at least in mammals. These include influence of hormones (such as estrogen), degree of physical activity, ageing or pathologies such as osteoporosis or osteoarthritis (Aaron 2012). Explaining the reasons of these differences in amphibians requires further studies.";
        String sentence = "In two species of toads and in Salamandra, which are among the most terrestrial of lissamphibians, SF were developed more prominently (i.e. the fibres were thicker and covered greater area of the section) than in species spending more time in water, such as the fire-bellied toads (compare Fig. 4b, c, g with a).";

        Pair<String, Integer> inText = PragmaticSentenceDetector.findInText(sentence, originalText);

        assertThat(inText.getRight(), is(0));
        assertThat(inText.getLeft(), is("In two species of toads and in Salamandra, which are among the most terrestrial of lissamphibians, SF were developed more prominently (i.e. the fibres were thicker and covered greater area of the section) than in species spending more time in water, such as the fire-bellied toads (compare Fig. 4b, c, g with a)."));
    }

    @Test
    public void testGetSentenceOffsets_realcase_2() throws Exception {

        String originalText = "With the success of large-scale pre-training and multilingual modeling in Natural Language Processing (NLP), recent years have seen a proliferation of large, web-mined text datasets covering hundreds of languages. However, to date there has been no systematic analysis of the quality of these publicly available datasets, or whether the datasets actually contain content in the languages they claim to represent. In this work, we manually audit the quality of 205 languagespecific corpora released with five major public datasets (CCAligned, ParaCrawl, WikiMatrix, OSCAR, mC4), and audit the correctness of language codes in a sixth (JW300). We find that lower-resource corpora have systematic issues: at least 15 corpora are completely erroneous, and a significant fraction contains less than 50% sentences of acceptable quality. Similarly, we find 82 corpora that are mislabeled or use nonstandard/ambiguous language codes. We demonstrate that these issues are easy to detect even for non-speakers of the languages in question, and supplement the human judgements with automatic analyses. Inspired by our analysis, we recommend techniques to evaluate and improve multilingual corpora and discuss the risks that come with low-quality data releases.";

        List<String> sentences = Arrays.asList(
            "With the success of large-scale pre-training and multilingual modeling in Natural Language Processing (NLP), recent years have seen a proliferation of large, web-mined text datasets covering hundreds of languages.",
            "However, to date there has been no systematic analysis of the quality of these publicly available datasets, or whether the datasets actually contain content in the languages they claim to represent.",
            "In this work, we manually audit the quality of 205 languagespecific corpora released with five major public datasets (CCAligned, ParaCrawl, WikiMatrix, OSCAR, mC4), and audit the correctness of language codes in a sixth (JW300).",
            "We find that lower-resource corpora have systematic issues: at least 15 corpora are completely erroneous, and a significant fraction contains less than 50% sentences of acceptable quality.",
            "Similarly, we find 82 corpora that are mislabeled or use nonstandard/ambiguous language codes.",
            "We demonstrate that these issues are easy to detect even for non-speakers of the languages in question, and supplement the human judgements with automatic analyses.",
            "Inspired by our analysis, we recommend techniques to evaluate and improve multilingual corpora and discuss the risks that come with low-quality data releases."
        );
        List<OffsetPosition> sentenceSpans = PragmaticSentenceDetector.getSentenceOffsets(originalText, sentences);

        assertThat(sentenceSpans, hasSize(7));
        for (int i = 0; i < sentenceSpans.size(); i++) {
            assertThat(originalText.substring(sentenceSpans.get(i).start, sentenceSpans.get(i).end), is(sentences.get(i)));
        }

    }

    @Test
    public void testGetSentenceOffsets_realcase_3() throws Exception {

        String originalText = "CCAligned ) is a 119language 1 parallel dataset built off 68 snapshots of Common Crawl. Documents are aligned if they are in the same language according to FastText LangID (Joulin et al., 2016(Joulin et al., , 2017, and have the same URL but for a differing language code. These alignments are refined with cross-lingual LASER embeddings (Artetxe and Schwenk, 2019). For sentence-level data, they split on newlines and align with LASER, but perform no further filtering. Human annotators evaluated the quality of document alignments for six languages (de, zh, ar, ro, et, my) selected for their different scripts and amount of retrieved documents, reporting precision of over 90%. The quality of the extracted parallel sentences is evaluated in a machine translation (MT) task on six European (da, cr, sl, sk, lt, et) languages of the TED corpus (Qi et al., 2018)   (Qi et al., 2018); WMT-5: cs, de, fi, lv, ro. POS/DEP-5: part-of-speech labeling and dependency parsing for bg, ca, da, fi, id.";

        List<String> sentences = Arrays.asList(
            "CCAligned ) is a 119language 1 parallel dataset built off 68 snapshots of Common Crawl.",
            "Documents are aligned if they are in the same language according to FastText LangID (Joulin et al., 2016(Joulin et al., , 2017, and have the same URL but for a differing language code.",
            "These alignments are refined with cross-lingual LASER embeddings (Artetxe and Schwenk, 2019).",
            "For sentence-level data, they split on newlines and align with LASER, but perform no further filtering.",
            "Human annotators evaluated the quality of document alignments for six languages (de, zh, ar, ro, et, my) selected for their different scripts and amount of retrieved documents, reporting precision of over 90%.",
            "The quality of the extracted parallel sentences is evaluated in a machine translation (MT) task on six European (da, cr, sl, sk, lt, et) languages of the TED corpus (Qi et al., 2018)   (Qi et al., 2018); WMT-5: cs, de, fi, lv, ro.",
            "POS/DEP-5: part-of-speech labeling and dependency parsing for bg, ca, da, fi, id."
        );
        List<OffsetPosition> sentenceSpans = PragmaticSentenceDetector.getSentenceOffsets(originalText, sentences);

        assertThat(sentenceSpans, hasSize(7));
        for (int i = 0; i < sentenceSpans.size(); i++) {
            assertThat(originalText.substring(sentenceSpans.get(i).start, sentenceSpans.get(i).end), is(sentences.get(i)));
        }

    }
}