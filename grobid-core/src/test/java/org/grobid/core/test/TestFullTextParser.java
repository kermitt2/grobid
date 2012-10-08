package org.grobid.core.test;

import static org.junit.Assert.assertNotNull;

import org.grobid.core.impl.GrobidFactory;
import org.grobid.core.main.GrobidConstants;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *  @author Patrice Lopez
 */
public class TestFullTextParser extends EngineTest {
	
	private String testPath = null;
	private String newTrainingPath = null;
	
	@BeforeClass
	public static void init(){
		GrobidProperties.getInstance();
	}
	
	@Before
	public void setUp() {
		newTrainingPath = GrobidProperties.getTempPath().getAbsolutePath();
	}
	
	private void getTestResourcePath() {
		testPath = GrobidConstants.TEST_RESOURCES_PATH;
	}
	
	@Test
	public void testFullTextTrainingParser() throws Exception {
		getTestResourcePath();

		String pdfPath = testPath + "/Wang-paperAVE2008.pdf";		
		//engine.createTrainingFullText(pdfPath, newTrainingPath, newTrainingPath, 0);
		
		pdfPath = testPath + "/1001._0908.0054.pdf";
		//engine.createTrainingFullText(pdfPath, newTrainingPath, newTrainingPath, 0);
		
		pdfPath = testPath + "/submission_161.pdf";
		//engine.createTrainingFullText(pdfPath, newTrainingPath, newTrainingPath, 0);
		
		pdfPath = testPath + "/submission_363.pdf";
		//engine.createTrainingFullText(pdfPath, newTrainingPath, newTrainingPath, 0);
		
		pdfPath = testPath + "/ApplPhysLett_98_082505.pdf";
		engine.createTrainingFullText(pdfPath, newTrainingPath, newTrainingPath, 0);
				
		/*engine.batchCreateTrainingFulltext("/Users/lopez/repository/abstracts/", 
							 			"/Users/lopez/repository/abstracts/training/",
							 			4);*/
	}
	
	@Test
	public void testFullTextParser() throws Exception {
		getTestResourcePath();

		String pdfPath = testPath + "/Wang-paperAVE2008.pdf";
		
		String tei = GrobidFactory.getInstance().createEngine().fullTextToTEI(pdfPath, false, false, 1);
		assertNotNull(tei);
 		//System.out.println(tei);
		
		pdfPath = testPath + "/1001._0908.0054.pdf";
		
		tei = GrobidFactory.getInstance().createEngine().fullTextToTEI(pdfPath, false, false, 1);
		assertNotNull(tei);
 		//System.out.println(tei);

		pdfPath = testPath + "/submission_161.pdf";
		tei = GrobidFactory.getInstance().createEngine().fullTextToTEI(pdfPath, false, false, 1);
		assertNotNull(tei);
 		//System.out.println(tei);

		pdfPath = testPath + "/submission_363.pdf";
		tei = GrobidFactory.getInstance().createEngine().fullTextToTEI(pdfPath, false, false, 1);
		assertNotNull(tei);
 		//System.out.println(tei);

		pdfPath = testPath + "/ApplPhysLett_98_082505.pdf";
		tei = GrobidFactory.getInstance().createEngine().fullTextToTEI(pdfPath, false, false, 1);
		assertNotNull(tei);
		//System.out.println(tei);
	}
	
}
