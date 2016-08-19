package org.grobid.core;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by lfoppiano on 19/08/16.
 */
public class GrobidModelsTest {

    @Test
    public void testGrobidModelsEnum_StandardModel_affiliation() throws Exception {

        GrobidModel model = GrobidModels.AFFIILIATON_ADDRESS;

        assertThat(model.getFolderName(), is("affiliation-address"));
        assertThat(model.getModelName(), is("affiliation-address"));
        assertThat(model.getTemplateName(), is("affiliation-address.template"));
        assertThat(model.getModelPath(), is("/Users/lfoppiano/development/inria/grobid/grobid-core/models/affiliation-address/model.wapiti"));
    }

    @Test
    public void testGrobidModelsEnum_StandardModel_name() throws Exception {

        GrobidModel model = GrobidModels.HEADER;

        assertThat(model.getFolderName(), is("header"));
        assertThat(model.getModelName(), is("header"));
        assertThat(model.getTemplateName(), is("header.template"));
        assertThat(model.getModelPath(), is("/Users/lfoppiano/development/inria/grobid/grobid-core/models/header/model.wapiti"));
    }

    @Test
    public void testGrobidModelsEnum_CustomModel() throws Exception {
        GrobidModel model = GrobidModels.modelFor("dictionaries-senses");

        assertThat(model.getFolderName(), is("dictionaries-senses"));
        assertThat(model.getModelName(), is("dictionaries-senses"));
        assertThat(model.getTemplateName(), is("dictionaries-senses.template"));
        assertThat(model.getModelPath(), is("/Users/lfoppiano/development/inria/grobid/grobid-core/models/dictionaries-senses/model.wapiti"));

        GrobidModel model2 = GrobidModels.modelFor("dictionaries-lemma");
        assertFalse(model2.equals(model));

        GrobidModel model3 = GrobidModels.modelFor("dictionaries-senses");
        assertTrue(model3.equals(model));
    }


}