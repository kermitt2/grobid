package org.grobid.core.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.grobid.core.mock.MockContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GrobidPropertiesTest {

	@BeforeClass
	public static void setInitialContext() throws Exception {
		MockContext.setInitialContext();
	}

	@AfterClass
	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}

	@Before
	public void setUp() {
		GrobidProperties.getInstance();
	}

	@After
	public void tearDown() {
		GrobidProperties.reset();
	}

	@Test
	public void testMockedPropertiesLoad() throws IOException {
		assertEquals("Path to GROBID_HOME not properly set", new File(
				MockContext.GROBID_HOME_PATH).getCanonicalFile(),
				GrobidProperties.get_GROBID_HOME_PATH());
		GrobidProperties.getInstance(); // Used to test the reuse of the
												// GrobidProperties instance
												// when get the instance.
		assertEquals("Path to grobid.properties not properly set",
				new File(MockContext.GROBID_PROPERTY_PATH).getCanonicalFile(), GrobidProperties
						.getGrobidPropertiesPath());
	}

	@Test
	public void testSet_GROBID_HOME_PATHThrowException() {
		try {
			GrobidProperties.set_GROBID_HOME_PATH(null);
			assertFalse(
					"An exception should have been thrown during the set of GROBID_HOME_PATH",
					true);
		} catch (GrobidPropertyException exp) {
		}
	}

	@Test
	public void testSet_GROBID_HOME_PATHFileNotExisting() {
		try {
			GrobidProperties.set_GROBID_HOME_PATH("/NotExistingPath");
			assertFalse(
					"An exception should have been thrown during the set of GROBID_HOME_PATH",
					true);
		} catch (GrobidPropertyException exp) {
		}
	}

	@Test
	public void testSetGrobidPropertiesPathhrowException() {
		try {
			GrobidProperties.setGrobidPropertiesPath(null);
			assertFalse(
					"An exception should have been thrown during the set of GrobidPropertiesPath",
					true);
		} catch (GrobidPropertyException exp) {
		}
	}

	@Test
	public void testSetGrobidPropertiesPathFileNotExisting() {
		try {
			GrobidProperties.setGrobidPropertiesPath("/NotExistingPath");
			assertFalse(
					"An exception should have been thrown during the set of GrobidPropertiesPath",
					true);
		} catch (GrobidPropertyException exp) {
		}
	}

	@Test
	public void testLoadGrobidPropertiesPathNoContext() throws Exception {
		try {
			GrobidProperties.reset();
			removeInitialContext(GrobidPropertyKeys.PROP_GROBID_PROPERTY);
			GrobidProperties.loadGrobidPropertiesPath();
			assertFalse(
					"An exception should have been thrown during the load of GrobidPropertiesPath",
					true);
		} catch (GrobidPropertyException exp) {
		} finally {
			bindValueInInitialContext(GrobidPropertyKeys.PROP_GROBID_PROPERTY,
					MockContext.GROBID_PROPERTY_PATH);
		}
	}

	@Test
	public void testNativeLibraryPath() throws IOException {
		File expectedFile = new File(MockContext.GROBID_HOME_PATH
				+ File.separator + "/lib");
		assertEquals(expectedFile.getCanonicalFile(), GrobidProperties
				.getNativeLibraryPath().getCanonicalFile());
	}

	@Test
	public void testCheckPropertiesException() {
		GrobidProperties.getProps().put(
				GrobidPropertyKeys.PROP_3RD_PARTY_PDF2XML, "");
		try {
			GrobidProperties.checkProperties();
			assertFalse(
					"An exception should have been thrown during the load of GrobidPropertiesPath",
					true);
		} catch (GrobidPropertyException exp) {
		}
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
				GrobidProperties.getCrossrefId().toString());
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

	@Test
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
	}

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
		GrobidModels value = GrobidModels.DATE;
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

	//@Test
	public void testgetGrobidHomePath() throws IOException {
		assertEquals("The property has not the value expected", new File(
				MockContext.GROBID_HOME_PATH).getCanonicalFile(),
				GrobidProperties.getGrobidHomePath());
	}

	@Test
	public void testgetLanguageDetectorFactoryGrobidException() {
		try {
			GrobidProperties.setUseLanguageId("true");
			GrobidProperties.getProps().put(
					GrobidPropertyKeys.PROP_LANG_DETECTOR_FACTORY, "");
			GrobidProperties.getLanguageDetectorFactory();
			assertFalse(
					"The getLanguageDetectorFactory should have raised a GrobidPropertyException",
					true);
		} catch (GrobidPropertyException gpe) {
		}
	}

	@Test
	public void testgetLanguageDetectorFactoryGrobid() {
		try {
			GrobidProperties.getProps().put(
					GrobidPropertyKeys.PROP_LANG_DETECTOR_FACTORY, "");
			GrobidProperties.setUseLanguageId("true");
			GrobidProperties.getLanguageDetectorFactory();
			assertFalse(
					"The getLanguageDetectorFactory should have raised a GrobidPropertyException",
					true);
		} catch (GrobidPropertyException gpe) {
		}
	}

	@Test
	public void testgetLanguageDetectorFactory() {
		GrobidProperties.setUseLanguageId("false");
		assertEquals("The property has not the value expected",
				"org.grobid.core.lang.impl.CybozuLanguageDetectorFactory",
				GrobidProperties.getLanguageDetectorFactory());
	}

	@Test
	public void testgetPdf2XMLPath() throws IOException {
		assertEquals("The property has not the value expected", new File(
				MockContext.GROBID_HOME_PATH + File.separator + "pdf2xml"
						+ File.separator + Utilities.getOsNameAndArch())
				.getCanonicalPath().toString(), GrobidProperties
				.getPdf2XMLPath().getAbsolutePath());
	}

	@Test
	public void testloadPdf2XMLPathGrobidPropertyException() {
		GrobidProperties.getProps().put(
				GrobidPropertyKeys.PROP_3RD_PARTY_PDF2XML, "/notExistingPath");
		GrobidProperties
				.getPropertyValue(GrobidPropertyKeys.PROP_3RD_PARTY_PDF2XML);
		try {
			GrobidProperties.loadPdf2XMLPath();
			assertFalse(
					"loadPdf2XMLPath should have raised a GrobidPropertyException",
					true);
		} catch (GrobidPropertyException gpe) {
		}
	}

	@Test
	public void testsetPropertyValueGrobidPropertyException() {
		try {
			GrobidProperties.setPropertyValue(MockContext.GROBID_HOME_PATH,
					null);
			assertFalse(
					"setPropertyValue should have raised a GrobidPropertyException",
					true);
		} catch (GrobidPropertyException gpe) {
		}
	}

	@Test
	public void testinitException2() {
		try {
			GrobidProperties.GROBID_PROPERTY_PATH = new File(StringUtils.EMPTY);
			GrobidProperties.init();
			assertFalse("init should have raised a GrobidPropertyException",
					true);
		} catch (GrobidPropertyException gpe) {
		}
	}

	@Test
	public void testsetgetContext() throws NamingException {
		GrobidProperties.setContext(null);
		assertEquals("The property has not the value expected", null,
				GrobidProperties.getContext());
	}

	@Test
	public void testload_GROBID_HOME_PATH() throws Exception {
		Context ctx = new InitialContext();
		GrobidProperties.setContext(ctx);
		GrobidProperties.GROBID_HOME_PATH = null;
		GrobidProperties.load_GROBID_HOME_PATH();
		assertEquals("The property has not the value expected", new File(
				MockContext.GROBID_HOME_PATH).getCanonicalPath().toString(),
				GrobidProperties.get_GROBID_HOME_PATH().getAbsolutePath());
	}

	@Test
	public void testload_GROBID_HOME_PATHGrobidPropertyException()
			throws Exception {
		try {
			removeInitialContext(GrobidPropertyKeys.PROP_GROBID_HOME);
			Context ctx = new InitialContext();
			GrobidProperties.setContext(ctx);
			GrobidProperties.GROBID_HOME_PATH = null;
			GrobidProperties.load_GROBID_HOME_PATH();
			assertFalse(
					"load_GROBID_HOME_PATH should have raised a GrobidPropertyException",
					true);
		} catch (GrobidPropertyException gpe) {
		} finally {
			bindValueInInitialContext(GrobidPropertyKeys.PROP_GROBID_HOME,
					MockContext.GROBID_HOME_PATH);
		}
	}

	@Test
	public void testload_GROBID_HOME_PATHGrobidPropertyException2()
			throws Exception {
		try {
			reBindValueInInitialContext(GrobidPropertyKeys.PROP_GROBID_HOME,
					"/NotExistingPath");
			Context ctx = new InitialContext();
			GrobidProperties.setContext(ctx);
			GrobidProperties.GROBID_HOME_PATH = null;
			GrobidProperties.load_GROBID_HOME_PATH();
			assertFalse(
					"load_GROBID_HOME_PATH should have raised a GrobidPropertyException",
					true);
		} catch (GrobidPropertyException gpe) {
		} finally {
			reBindValueInInitialContext(GrobidPropertyKeys.PROP_GROBID_HOME,
					MockContext.GROBID_HOME_PATH);
		}
	}

	@Test
	public void testloadGrobidPropertiesPath() throws Exception {
		Context ctx = new InitialContext();
		GrobidProperties.setContext(ctx);
		GrobidProperties.GROBID_PROPERTY_PATH = null;
		GrobidProperties.loadGrobidPropertiesPath();
		assertEquals("The property has not the value expected",
				new File(MockContext.GROBID_PROPERTY_PATH).getCanonicalPath()
						.toString(), GrobidProperties.getGrobidPropertiesPath()
						.getAbsolutePath());
	}

	@Test
	public void testloadGrobidPropertiesPathGrobidPropertyException()
			throws Exception {
		try {
			removeInitialContext(GrobidPropertyKeys.PROP_GROBID_PROPERTY);
			Context ctx = new InitialContext();
			GrobidProperties.setContext(ctx);
			GrobidProperties.GROBID_PROPERTY_PATH = null;
			GrobidProperties.loadGrobidPropertiesPath();
			assertFalse(
					"loadGrobidPropertiesPath should have raised a GrobidPropertyException",
					true);
		} catch (GrobidPropertyException gpe) {
		} finally {
			bindValueInInitialContext(GrobidPropertyKeys.PROP_GROBID_PROPERTY,
					MockContext.GROBID_PROPERTY_PATH);
		}
	}

	@Test
	public void loadGrobidPropertiesPathGrobidPropertyException2()
			throws Exception {
		try {
			reBindValueInInitialContext(
					GrobidPropertyKeys.PROP_GROBID_PROPERTY, "/NotExistingPath");
			Context ctx = new InitialContext();
			GrobidProperties.setContext(ctx);
			GrobidProperties.GROBID_PROPERTY_PATH = null;
			GrobidProperties.loadGrobidPropertiesPath();
			assertFalse(
					"loadGrobidPropertiesPath should have raised a GrobidPropertyException",
					true);
		} catch (GrobidPropertyException gpe) {
		} finally {
			reBindValueInInitialContext(
					GrobidPropertyKeys.PROP_GROBID_PROPERTY,
					MockContext.GROBID_PROPERTY_PATH);
		}
	}

	@Test
	public void testGetInstance() throws Exception {
		GrobidProperties.reset();
		GrobidProperties.getInstance();
		// test the resue of the instance created previously
		GrobidProperties.reset();
		assertTrue(
				"GrobidProperties.getInstance() does not return an instance of GrobidProperties",
				GrobidProperties.getInstance() instanceof GrobidProperties);
	}

	/**
	 * Remove the initial context.
	 * 
	 * @throws Exception
	 */
	private static void removeInitialContext(String key) throws Exception {
		InitialContext ic = new InitialContext();
		ic.unbind("java:comp/env/" + key);
	}

	private static void bindValueInInitialContext(String key, String value)
			throws Exception {
		InitialContext ic = new InitialContext();
		ic.bind("java:comp/env/" + key, value);
	}

	private static void reBindValueInInitialContext(String key, String value)
			throws Exception {
		InitialContext ic = new InitialContext();
		ic.unbind("java:comp/env/" + key);
		ic.bind("java:comp/env/" + key, value);
	}

}
