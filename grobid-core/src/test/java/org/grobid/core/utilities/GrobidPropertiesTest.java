package org.grobid.core.utilities;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
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

    @Test
    public void testGetVersion_shouldWork() {
        assertNotNull(GrobidProperties.getVersion());
        assertNotEquals(GrobidProperties.UNKNOWN_VERSION_STR, GrobidProperties.getVersion());
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
    public void testNativeLibraryPath() throws IOException {
//        File expectedFile = new File(MockContext.GROBID_HOME_PATH
//                + File.separator + "/lib");
        assertNotNull(GrobidProperties
                .getNativeLibraryPath().getCanonicalFile());
    }

    @Test(expected = GrobidPropertyException.class)
    public void testCheckPropertiesException_shouldThrowException() {
        GrobidProperties.getProps().put(
                GrobidPropertyKeys.PROP_3RD_PARTY_PDF2XML, "");
        GrobidProperties.checkProperties();
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
    public void testgetsetCrossrefId() {
        String value = "1";
        GrobidProperties.setCrossrefId(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getCrossrefId());
    }

    @Test
    public void testsetgetCrossrefPw() {
        String value = "pass";
        GrobidProperties.setCrossrefPw(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getCrossrefPw());
    }

    @Test
    public void testsetgetCrossrefHost() {
        String value = "host";
        GrobidProperties.setCrossrefHost(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getCrossrefHost());
    }

    @Test
    public void testsetgetCrossrefPort() {
        String value = "1";
        GrobidProperties.setCrossrefPort(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getCrossrefPort().toString());
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

    /*@Test
    public void testsetgetMySQLDBName() {
        String value = "dbName";
        GrobidProperties.setMySQLDBName(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getMySQLDBName());
    }

    @Test
    public void testsetgetMySQLUsername() {
        String value = "userName";
        GrobidProperties.setMySQLUsername(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getMySQLUsername());
    }

    @Test
    public void testsetgetMySQLPw() {
        String value = "pass";
        GrobidProperties.setMySQLPw(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getMySQLPw());
    }

    @Test
    public void testsetgetMySQLHost() {
        String value = "1";
        GrobidProperties.setMySQLHost(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getMySQLHost());
    }

    @Test
    public void testsetgetMySQLPort() {
        String value = "1";
        GrobidProperties.setMySQLPort(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getMySQLPort().toString());
    }*/

    @Test
    public void testsetgetNBThreads() {
        String value = "1";
        GrobidProperties.setNBThreads(value);
        assertEquals("The property has not the value expected", value,
                GrobidProperties.getNBThreads().toString());
    }

    @Test
    public void testsetisUseLanguageId() {
        String value = "true";
        GrobidProperties.setUseLanguageId(value);
        assertTrue("The property has not the value expected",
                GrobidProperties.isUseLanguageId());
    }

    @Test
    public void testsetisResourcesInHome() {
        String value = "true";
        GrobidProperties.setResourcesInHome(value);
        assertTrue("The property has not the value expected",
                GrobidProperties.isResourcesInHome());
    }

    //@Test
    public void testGetModelPath() {
        GrobidModels value = GrobidModels.DATE;
        assertEquals("The property has not the value expected",
                new File(GrobidProperties.get_GROBID_HOME_PATH(),
                        GrobidProperties.FOLDER_NAME_MODELS + File.separator
                                + value.getFolderName() + File.separator
                                + GrobidProperties.FILE_NAME_MODEL),
                GrobidProperties.getModelPath(value));
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
        GrobidProperties.setUseLanguageId("true");
        GrobidProperties.getProps().put(
                GrobidPropertyKeys.PROP_LANG_DETECTOR_FACTORY, "");
        GrobidProperties.getLanguageDetectorFactory();
    }

    @Test(expected = GrobidPropertyException.class)
    public void testgetLanguageDetectorFactory2_shouldThrowException() {
        GrobidProperties.getProps().put(
                GrobidPropertyKeys.PROP_LANG_DETECTOR_FACTORY, "");
        GrobidProperties.setUseLanguageId("true");
        GrobidProperties.getLanguageDetectorFactory();
    }

    @Test
    public void testgetLanguageDetectorFactory() {
        GrobidProperties.setUseLanguageId("false");
        assertEquals("The property has not the value expected",
                "org.grobid.core.lang.impl.CybozuLanguageDetectorFactory",
                GrobidProperties.getLanguageDetectorFactory());
    }

    @Test
    public void testgetPdf2XMLPath() throws Exception {
        assertNotNull("The property has not the value expected", GrobidProperties
                .getPdf2XMLPath().getAbsolutePath());
    }


    @Test(expected = GrobidPropertyException.class)
    public void testloadPdf2XMLPath_shouldThrowException() {
        GrobidProperties.getProps().put(
                GrobidPropertyKeys.PROP_3RD_PARTY_PDF2XML, "/notExistingPath");
        GrobidProperties
                .getPropertyValue(GrobidPropertyKeys.PROP_3RD_PARTY_PDF2XML);
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
