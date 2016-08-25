package org.grobid.core;

import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by lfoppiano on 19/08/16.
 */
public class GrobidModelsTest {

    @BeforeClass
    public static void setInitialContext() throws Exception{
        MockContext.setInitialContext();
        GrobidProperties.getInstance();
    }


    @Test
    public void testGrobidModelsEnum_StandardModel_affiliation() throws Exception {

        GrobidModel model = GrobidModels.AFFIILIATON_ADDRESS;

        assertThat(model.getFolderName(), is("affiliation-address"));
        assertThat(model.getModelName(), is("affiliation-address"));
        assertThat(model.getTemplateName(), is("affiliation-address.template"));
        assertThat(model.getModelPath(), endsWith("/grobid/grobid-home/models/affiliation-address/model.wapiti"));
    }

    @Test
    public void testGrobidModelsEnum_StandardModel_name() throws Exception {

        GrobidModel model = GrobidModels.HEADER;

        assertThat(model.getFolderName(), is("header"));
        assertThat(model.getModelName(), is("header"));
        assertThat(model.getTemplateName(), is("header.template"));
        assertThat(model.getModelPath(), endsWith("/grobid/grobid-home/models/header/model.wapiti"));
    }

    @Test
    public void testGrobidModelsEnum_CustomModel() throws Exception {
        GrobidModel model = GrobidModels.modelFor("dictionaries-senses");

        assertThat(model.getFolderName(), is("dictionaries-senses"));
        assertThat(model.getModelName(), is("dictionaries-senses"));
        assertThat(model.getTemplateName(), is("dictionaries-senses.template"));
        assertThat(model.getModelPath(), endsWith("/grobid/grobid-home/models/dictionaries-senses/model.wapiti"));

        GrobidModel model2 = GrobidModels.modelFor("dictionaries-lemma");
        assertFalse(model2.equals(model));

        GrobidModel model3 = GrobidModels.modelFor("dictionaries-senses");
        assertTrue(model3.equals(model));
    }


}