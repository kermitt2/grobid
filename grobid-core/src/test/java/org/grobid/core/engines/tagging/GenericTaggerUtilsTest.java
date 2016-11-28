package org.grobid.core.engines.tagging;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by lfoppiano on 28/11/16.
 */
public class GenericTaggerUtilsTest {

    @Test
    public void testGetPlainLabel_normalValue() throws Exception {
        assertThat(GenericTaggerUtils.getPlainLabel("<status>"), is("<status>"));
    }

    @Test
    public void testGetPlainLabel_startingValue() throws Exception {
        assertThat(GenericTaggerUtils.getPlainLabel("I-<status>"), is("<status>"));
    }

    @Test
    public void testGetPlainLabel_nullValue() throws Exception {
        assertNull(GenericTaggerUtils.getPlainLabel(null));
    }

    @Test
    public void testIsBeginningOfEntity_true() throws Exception {
        assertTrue(GenericTaggerUtils.isBeginningOfEntity("I-<status>"));
    }

    @Test
    public void testIsBeginningOfEntity_false() throws Exception {
        assertFalse(GenericTaggerUtils.isBeginningOfEntity("<status>"));
    }

    @Test
    public void testIsBeginningOfEntity_false2() throws Exception {
        assertFalse(GenericTaggerUtils.isBeginningOfEntity("<I-status>"));
    }
}