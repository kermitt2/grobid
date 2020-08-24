package org.grobid.core.utilities;

import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;

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
}