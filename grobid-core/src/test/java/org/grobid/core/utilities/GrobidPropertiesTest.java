package org.grobid.core.utilities;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GrobidPropertiesTest {
    @Before
    public void setUp() {
        GrobidProperties.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        GrobidProperties.reset();
    }

    @Test(expected = GrobidPropertyException.class)
    public void testSet_GROBID_HOME_PATH_NullPath_shouldThrowException() {
        GrobidProperties.set_GROBID_HOME_PATH(null);
    }

    @Test(expected = GrobidPropertyException.class)
    public void testSet_GROBID_HOME_PATH_FileNotExisting_shouldThrowException() {
        GrobidProperties.set_GROBID_HOME_PATH("/NotExistingPath");
    }

    @Test(expected = GrobidPropertyException.class)
    public void testSetGrobidProperties_NullPath_shouldThrowException() {
        GrobidProperties.setGrobidPropertiesPath(null);
    }

    @Test(expected = GrobidPropertyException.class)
    public void testSetGrobidProperties_PathFileNotExisting() {
        GrobidProperties.setGrobidPropertiesPath("/NotExistingPath");
    }

    @Test(expected = GrobidPropertyException.class)
    public void testLoadGrobidProperties_PathNoContext_shouldThrowException() throws Exception {
        GrobidProperties.reset();
        GrobidProperties.loadGrobidPropertiesPath();
    }

    @Test
    public void shouldReturnAndConvertMatchingEnvironmentVariable() throws Exception {
        assertEquals(
            Collections.singletonMap("grobid.abc", "value1"),
            GrobidProperties.getEnvironmentVariableOverrides(
                Collections.singletonMap("GROBID__ABC", "value1")
            )
        );
    }

    @Test
    public void testNativeLibraryPath() throws IOException {
//        File expectedFile = new File(MockContext.GROBID_HOME_PATH
//                + File.separator + "/lib");
        assertNotNull(GrobidProperties
                .getNativeLibraryPath().getCanonicalFile());
    }

    @Test
    public void testGetTempPath() {
        assertEquals("The value of temp dir doesn't match the one expected",
                GrobidProperties
                        .getPropertyValue(GrobidPropertyKeys.PROP_TMP_PATH),
                GrobidProperties.getTempPath().getAbsolutePath());
    }

    @Test
    public void testgetsetNativeLibraryPath() {
        String value = "value";
        GrobidProperties.setNativeLibraryPath(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getNativeLibraryPath().getPath());
    }

    @Test
    public void testsetgetProxyHost() {
        String value = "host";
        GrobidProperties.setProxyHost(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getProxyHost());
    }

    @Test
    public void testsetgetProxyPort() {
        String value = "1";
        GrobidProperties.setProxyPort(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getProxyPort().toString());
    }

    @Test
    public void testsetgetNBThreads() {
        String value = "1";
        GrobidProperties.setNBThreads(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getNBThreads().toString());
    }

    @Test
    public void testgetNBThreadsShouldReturnAvailableProcessorsIfZero() {
        String value = "0";
        GrobidProperties.setNBThreads(value);
        assertEquals("The property has not the value expected",
                String.valueOf(Runtime.getRuntime().availableProcessors()),
                GrobidProperties.getNBThreads().toString());
        assertTrue("The property is not greater than zero",
            GrobidProperties.getNBThreads().intValue() > 0);
    }

    @Test
    public void testShouldReturnWapitiAsDefaultEngine() {
        GrobidProperties.getProps().remove(GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE);
        GrobidProperties.loadCrfEngine();
        assertEquals(
            "engine",
            GrobidCRFEngine.WAPITI,
            GrobidProperties.getGrobidCRFEngine("dummy")
        );
    }

    @Test
    public void testShouldReturnConfiguredEngineIfNotConfiguredForModel() {
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE,
            GrobidCRFEngine.DELFT.name()
        );
        GrobidProperties.loadCrfEngine();
        assertEquals(
            "engine",
            GrobidCRFEngine.DELFT,
            GrobidProperties.getGrobidCRFEngine("model1")
        );
    }

    @Test
    public void testShouldAllowModelSpecificEngineConfiguration() {
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE,
            GrobidCRFEngine.WAPITI.name()
        );
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE + "."
            + GrobidModels.SEGMENTATION.getModelName(),
            GrobidCRFEngine.DELFT.name()
        );
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE + "."
            + GrobidModels.FULLTEXT.getModelName(),
            GrobidCRFEngine.DELFT.name()
        );
        GrobidProperties.loadCrfEngine();
        assertEquals(
            "segmentation engine",
            GrobidCRFEngine.DELFT,
            GrobidProperties.getGrobidCRFEngine(GrobidModels.SEGMENTATION)
        );
        assertEquals(
            "fulltext engine",
            GrobidCRFEngine.DELFT,
            GrobidProperties.getGrobidCRFEngine(GrobidModels.FULLTEXT)
        );
    }


    @Test
    public void testShouldReplaceHyphenWithUnderscoreForModelSpecificEngineConfiguration() {
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE,
            GrobidCRFEngine.WAPITI.name()
        );
        GrobidProperties.getProps().put(
            GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE + "."
            + "model_name1",
            GrobidCRFEngine.DELFT.name()
        );
        GrobidProperties.loadCrfEngine();
        assertEquals(
            "segmentation engine",
            GrobidCRFEngine.DELFT,
            GrobidProperties.getGrobidCRFEngine("model-name1")
        );
    }

    @Test
    public void testShouldReturnModelPathWithExtension() {
        GrobidModels model = GrobidModels.DATE;
        String extension = GrobidProperties.getGrobidCRFEngine(model).getExt();
        assertEquals(
            "model path for " + model.name(),
            new File(GrobidProperties.get_GROBID_HOME_PATH(),
                GrobidProperties.FOLDER_NAME_MODELS
                + File.separator
                + model.getFolderName()
                + File.separator
                + GrobidProperties.FILE_NAME_MODEL
                + "."
                + extension
            ).getAbsoluteFile(),
            GrobidProperties.getModelPath(model).getAbsoluteFile()
        );
    }

    //@Test
    public void testgetTemplatePath() {
        GrobidModels value = GrobidModels.DATE;
        assertEquals(
                "The property has not the value expected",
                new File(GrobidProperties.get_GROBID_HOME_PATH(), "dataset/"
                        + value.getFolderName() + "/crfpp-templates/"
                        + value.getTemplateName()),
                GrobidProperties.getTemplatePath(
                        GrobidProperties.get_GROBID_HOME_PATH(), value));
    }

    //@Test
    public void testgetEvalCorpusPath() {
        GrobidModels value = GrobidModels.DATE;
        assertEquals(
                "The property has not the value expected",
                new File(GrobidProperties.get_GROBID_HOME_PATH(), "dataset/"
                        + value.getFolderName() + "/evaluation/"),
                GrobidProperties.getEvalCorpusPath(
                        GrobidProperties.get_GROBID_HOME_PATH(), value));
    }

    //@Test
    public void testgetCorpusPath() {
        GrobidModel value = GrobidModels.DATE;
        assertEquals(
                "The property has not the value expected",
                new File(GrobidProperties.get_GROBID_HOME_PATH(), "dataset/"
                        + value.getFolderName() + "/corpus"),
                GrobidProperties.getCorpusPath(
                        GrobidProperties.get_GROBID_HOME_PATH(), value));
    }

    //@Test
    public void testgetLexiconPath() {
        assertEquals("The property has not the value expected",
                new File(GrobidProperties.get_GROBID_HOME_PATH(), "lexicon")
                        .getAbsolutePath(), GrobidProperties.getLexiconPath());
    }


    @Test(expected = GrobidPropertyException.class)
    public void testgetLanguageDetectorFactory_shouldThrowException() {
        GrobidProperties.getProps().put(
                GrobidPropertyKeys.PROP_LANG_DETECTOR_FACTORY, "");
        GrobidProperties.getLanguageDetectorFactory();
    }

    @Test(expected = GrobidPropertyException.class)
    public void testgetLanguageDetectorFactory2_shouldThrowException() {
        GrobidProperties.getProps().put(
                GrobidPropertyKeys.PROP_LANG_DETECTOR_FACTORY, "");
        GrobidProperties.getLanguageDetectorFactory();
    }

    @Test
    public void testgetLanguageDetectorFactory() {
        assertEquals("The property has not the value expected",
                "org.grobid.core.lang.impl.CybozuLanguageDetectorFactory",
                GrobidProperties.getLanguageDetectorFactory());
    }

    @Test
    public void testgetPdf2XMLPath() throws Exception {
        assertNotNull("The property has not the value expected", GrobidProperties
                .getPdfToXMLPath().getAbsolutePath());
    }

    @Test(expected = GrobidPropertyException.class)
    public void testloadPdf2XMLPath_shouldThrowException() {
        GrobidProperties.getProps().put(
                GrobidPropertyKeys.PROP_3RD_PARTY_PDFTOXML, "/notExistingPath");
        GrobidProperties
                .getPropertyValue(GrobidPropertyKeys.PROP_3RD_PARTY_PDFTOXML);
        GrobidProperties.loadPdf2XMLPath();
    }

    @Test(expected = GrobidPropertyException.class)
    public void testinitException2_shouldThrowException() {
        GrobidProperties.GROBID_PROPERTY_PATH = new File(StringUtils.EMPTY);
        GrobidProperties.getNewInstance();
    }

    @Test
    public void testLoadGrobidPropertiesPath() throws Exception {
        GrobidProperties.GROBID_PROPERTY_PATH = null;
        GrobidProperties.loadGrobidPropertiesPath();
        assertNotNull("The property has not the value expected",
                GrobidProperties.getGrobidPropertiesPath().getAbsolutePath());
    }

    @Test
    public void testGetInstance() throws Exception {
        GrobidProperties.reset();
        GrobidProperties.getInstance();
        // test the resue of the instance created previously
        GrobidProperties.reset();
        assertTrue("GrobidProperties.getInstance() does not return an instance of GrobidProperties",
                GrobidProperties.getInstance() != null);
    }

}
