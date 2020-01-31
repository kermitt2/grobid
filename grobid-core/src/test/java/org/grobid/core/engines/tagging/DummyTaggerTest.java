package org.grobid.core.engines.tagging;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class DummyTaggerTest {
    GenericTagger target;

    @Before
    public void setUp() throws Exception {
        target = TaggerFactory.getTagger(GrobidModels.DUMMY);
    }

    @Test
    public void testDummyTagger_shouldReturnDummyLabel() {
        assertThat(target.label("bao"), is("<dummy>"));
    }

    @Test
    public void testDummyTagger() {
        assertThat(target.label(Arrays.asList("bao", "miao", "ciao")),
            is(equalTo("bao\t<dummy>\nmiao\t<dummy>\nciao\t<dummy>")));
    }

    @Test(expected = GrobidException.class)
    public void testWrongModelInitialisation_shouldThrowException() {
        target = new DummyTagger(GrobidModels.HEADER);
    }
}
