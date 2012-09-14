package org.grobid.core.utilities;

import java.io.File;

import junit.framework.TestCase;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GrobidPropertiesStaticTest extends TestCase{
	
	private static final String DIR_GROBID_HOME = "GROBID_HOME";
	
	@After
	public void tearDown()
	{
		System.out.println("before tear down: "+ GrobidProperties.get_GROBID_HOME_PATH());
		GrobidProperties.reset();
		// System.getProperties().remove(GrobidProperties.PROP_GROBID_HOME);
		System.out.println("after tear down: "+ GrobidProperties.get_GROBID_HOME_PATH());
	}
	
	@Test
	public void testGet_GROBID_HOME_PATH()
	{
		File expectedFile= null;
		
		//check without setting anything
		expectedFile= new File(System.getProperty("user.dir")+ "/" + DIR_GROBID_HOME);
		assertEquals(expectedFile, GrobidProperties.get_GROBID_HOME_PATH());
		
		//check with setting system property
		GrobidProperties.reset();
		expectedFile = new File(System.getProperty("java.io.tmpdir")+ "/1/" + DIR_GROBID_HOME);
		expectedFile.mkdirs();
		System.setProperty(GrobidPropertyKeys.PROP_GROBID_HOME, expectedFile.getAbsolutePath());
		assertEquals(expectedFile, GrobidProperties.get_GROBID_HOME_PATH());
		
		//check with setting static field
		expectedFile = new File(System.getProperty("java.io.tmpdir")+ "/2/" + DIR_GROBID_HOME);
		expectedFile.mkdirs();
		GrobidProperties.set_GROBID_HOME_PATH(expectedFile.getAbsolutePath());
		assertEquals(expectedFile, GrobidProperties.get_GROBID_HOME_PATH());
	}
}
