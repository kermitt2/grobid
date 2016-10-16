package org.grobid.core.engines;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 29/09/16.
 */
public class TaggingLabelsTest {

//    @Test
//    public void testTaggingLabel_StandardLabelSection() throws Exception {
//        TaggingLabel label = TaggingLabels.SECTION;
//
//        assertNotNull(label);
//
//        assertThat(label.getLabel(), is("<section>"));
//        assertThat(label.getGrobidModel(), is((GrobidModel) GrobidModels.FULLTEXT));
//    }
//
//    @Test
//    public void testModelFor_StandardLabelStartingSection() throws Exception {
//
//        TaggingLabel label = TaggingLabels.labelFor(GrobidModels.FULLTEXT, "I-<section>");
//
//        assertNotNull(label);
//
//        assertThat(label.getLabel(), is("<section>"));
//        assertThat(label.getGrobidModel(), is((GrobidModel) GrobidModels.FULLTEXT));
//
//    }
//
//    @Test
//    public void testModelFor_StandardLabelMiddleSection() throws Exception {
//
//        TaggingLabel label = TaggingLabels.labelFor(GrobidModels.FULLTEXT, "<section>");
//
//        assertNotNull(label);
//
//        assertThat(label.getLabel(), is("<section>"));
//        assertThat(label.getGrobidModel(), is((GrobidModel) GrobidModels.FULLTEXT));
//    }
//
//    @Test
//    public void testTaggingLabel_CustomLabel() throws Exception {
//
//        TaggingLabel label = TaggingLabels.labelFor(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, "<lemma>");
//
//        assertNotNull(label);
//
//        assertThat(label.getLabel(), is("<lemma>"));
//        assertThat(label.getGrobidModel(), is((GrobidModel) GrobidModels.DICTIONARIES_LEXICAL_ENTRIES));
//
//    }
}