package org.grobid.core.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.main.GrobidConstants;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 *  @author Patrice Lopez
 */
public class TestHeaderParser extends EngineTest{
	
	private String testPath = null;
	private String newTrainingPath = null;

	private void getTestResourcePath() {
		testPath = GrobidConstants.TEST_RESOURCES_PATH;
		GrobidProperties.getInstance();
		newTrainingPath = GrobidProperties.getTempPath().getAbsolutePath();
	}
	
	@Test
	public void testHeaderHeader() throws Exception {
		getTestResourcePath();
		
		String pdfPath = testPath + "/Wang-paperAVE2008.pdf";
		BiblioItem resHeader = new BiblioItem();
		
		String tei = engine.processHeader(pdfPath, false, resHeader);
	
		assertNotNull(resHeader);
        assertThat(resHeader.getTitle(), is("Information Synthesis for Answer Validation"));
        assertThat(resHeader.getKeyword(),
            is("Answer Validation, Recognizing Textual Entailment, Information Synthesis"));
        assertNotNull(resHeader.getFullAuthors());

        //System.out.println(tei);

        pdfPath = testPath + "/1060._fulltext3.pdf";
		resHeader = new BiblioItem();
		tei = engine.processHeader(pdfPath, false, resHeader);
		
		assertNotNull(resHeader);
        //System.out.println(tei);

        pdfPath = testPath + "/ZFN-A-054-0304-0272.pdf";
		resHeader = new BiblioItem();
		tei = engine.processHeader(pdfPath, false, resHeader);
		
		assertNotNull(resHeader);
        //System.out.println(tei);

        pdfPath = testPath + "/ZNC-1988-43c-0034.pdf";
		resHeader = new BiblioItem();
		tei = engine.processHeader(pdfPath, false, resHeader);
        //System.out.println(tei);

        //assertNotNull(resHeader);

        pdfPath = testPath + "/ZNC-1988-43c-0065.pdf";
		resHeader = new BiblioItem();
		tei = engine.processHeader(pdfPath, false, resHeader);

		assertNotNull(resHeader);
        //System.out.println(tei);

    }

	@Test
	public void testSegmentationHeader() throws Exception {
		getTestResourcePath();
		
		File pdfPath = new File(testPath + "/Wang-paperAVE2008.pdf");
		BiblioItem resHeader = new BiblioItem();
		
		String tei = engine.segmentAndProcessHeader(pdfPath, false, resHeader);
	
		assertNotNull(resHeader);
        assertThat(resHeader.getTitle(), is("Information Synthesis for Answer Validation"));
        assertThat(resHeader.getKeyword(),
            is("Answer Validation, Recognizing Textual Entailment, Information Synthesis"));
        assertNotNull(resHeader.getFullAuthors());
    }
	
	@Test
	public void testTrainingHeader() throws Exception {
		getTestResourcePath();
		
		String pdfPath = testPath + "/Wang-paperAVE2008.pdf";
		engine.createTrainingHeader(pdfPath, newTrainingPath, newTrainingPath, 0);
		
		pdfPath = testPath + "/1060._fulltext3.pdf";
		engine.createTrainingHeader(pdfPath, newTrainingPath, newTrainingPath, 0);
		
		pdfPath = testPath + "/ZFN-A-054-0304-0272.pdf";
		engine.createTrainingHeader(pdfPath, newTrainingPath, newTrainingPath, 0);
		
		pdfPath = testPath + "/1001._0908.0054.pdf";
		engine.createTrainingHeader(pdfPath, newTrainingPath, newTrainingPath, 0);

		/*engine.batchCreateTrainingHeader("/Users/lopez/repository/ZFN/regensburg3", 
						   	      "/Users/lopez/repository/ZFN/regensburg3/training", 
								  0);*/

	}
	
	@Test
	public void testBatchHeader() throws Exception {
		
		getTestResourcePath();
				
		/*engine.batchCreateTrainingHeader("/Users/lopez/repository/CiteNPL/set4", 
							   	     	 "/Users/lopez/repository/CiteNPL/set4", 
									 	 0);*/
		/*engine.batchProcessHeader("/Users/lopez/repository/CiteNPL/set1", 
						   	      "/Users/lopez/repository/CiteNPL/set1", 
								  false);	*/	
								
		/*engine.batchProcessHeader(testPath, 
						   	      testPath, 
								  false);	*/					
								
	}




}