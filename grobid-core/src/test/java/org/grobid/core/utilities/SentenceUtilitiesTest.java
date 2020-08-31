package org.grobid.core.utilities;

import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class SentenceUtilitiesTest {

    @Before
    public void setUp() {
        LibraryLoader.load();
        GrobidProperties.getInstance();
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
        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text);
        assertThat(theSentences.size(), is(0));
    }

    @Test
    public void testOneSentenceText() throws Exception {
        String text = "Bla bla bla.";
        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text);
        assertThat(theSentences.size(), is(1));
    }

    @Test
    public void testTwoSentencesText() throws Exception {
        String text = "Bla bla bla. Bli bli bli.";
        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text);
        assertThat(theSentences.size(), is(2));
    }

    @Test
    public void testTwoSentencesTextWithUselessForbidden() throws Exception {
        String text = "Bla bla bla. Bli bli bli.";
        List<OffsetPosition> forbidden = new ArrayList<>();
        forbidden.add(new OffsetPosition(2, 8));
        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text, forbidden);
        assertThat(theSentences.size(), is(2));
    }

    @Test
    public void testTwoSentencesTextWithUsefullForbidden() throws Exception {
        String text = "Bla bla bla. Bli bli bli.";
        List<OffsetPosition> forbidden = new ArrayList<>();
        forbidden.add(new OffsetPosition(2, 8));
        forbidden.add(new OffsetPosition(9, 15));
        List<OffsetPosition> theSentences = SentenceUtilities.getInstance().runSentenceDetection(text, forbidden);
        assertThat(theSentences.size(), is(1));
    }

    @Test
    public void testGetText() throws Exception {
        String text = "Bla bla bla. Bli bli bli.";

        List<OffsetPosition> offsetPositions = Arrays.asList(
            new OffsetPosition(0, 12),
            new OffsetPosition(14, 21)
        );

        String outputText = SentenceUtilities.getInstance().getXml(text, offsetPositions);

        assertThat(outputText, is("<sents><s>Bla bla bla.</s> B<s>li bli </s>bli.</sents>"));
    }

    @Test
    public void testParagraphWithMarkersOutsideTheSentence() throws Exception {
        String text = "Precisely controlling surface chemistry using self-assembled monolayers (SAMs) and bilayers " +
            "has been a central focus of research in both synthetic and biological interfaces. " +
            "1−4 Much synthetic monolayer chemistry has its basis in the formation of SAMs of alkanethiols " +
            "on gold and the coinage metals, pioneered by groups including those of Whitesides, " +
            "Nuzzo, and Allara in the 1980s. 5−8 ";

        List<OffsetPosition> forbidden = Arrays.asList(
            new OffsetPosition(174, 177),
            new OffsetPosition(383, 386)
        );

        List<OffsetPosition> offsetPositions = SentenceUtilities.getInstance().runSentenceDetection(text, forbidden);
        assertThat(SentenceUtilities.getInstance().getXml(text, offsetPositions), is("<sents><s>Precisely controlling " +
            "surface chemistry using self-assembled monolayers (SAMs) and bilayers " +
            "has been a central focus of research in both synthetic and biological interfaces. " +
            "1−4</s> <s>Much synthetic monolayer chemistry has its basis in the formation of SAMs of alkanethiols " +
            "on gold and the coinage metals, pioneered by groups including those of Whitesides, " +
            "Nuzzo, and Allara in the 1980s. 5−8</s> </sents>"));

    }
}