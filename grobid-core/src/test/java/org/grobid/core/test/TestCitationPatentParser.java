package org.grobid.core.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.Ignore;

/**
 *  @author Patrice Lopez
 */
@Ignore
public class TestCitationPatentParser extends EngineTest {
	private String newTrainingPath = null;
	

	public File getResourceDir(String resourceDir) {
		File file = new File(resourceDir);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
			}
		}
		return(file);
	}
	
	private void getTestResourcePath() {
		newTrainingPath = GrobidProperties.getInstance().getTempPath().getAbsolutePath();
	}
	
	//@Test
	public void testCitationPatentParser() throws Exception {
		File textFile = new File(this.getResourceDir("./src/test/resources/").getAbsoluteFile()+"/patents/sample1.txt");
		if (!textFile.exists()) {
			throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
		}

		String text = FileUtils.readFileToString(textFile);	
		
		List<BibDataSet> nplResults = new ArrayList<BibDataSet>();
		List<PatentItem> patentResults = new ArrayList<PatentItem>();
		boolean consolidateCitations = false;
		/*engine.processAllCitationsInPatent(text, nplResults, patentResults, consolidateCitations);
		assertThat(patentResults.size(), is(26));
		assertThat(nplResults.size(), is(0));*/
		
		textFile = new File(this.getResourceDir("./src/test/resources/").getAbsoluteFile()+"/patents/sample2.txt");
		if (!textFile.exists()) {
			throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
		}
		text = FileUtils.readFileToString(textFile);	
		
		nplResults = new ArrayList<BibDataSet>();
		patentResults = new ArrayList<PatentItem>();
		/*engine.processAllCitationsInPatent(text, nplResults, patentResults, consolidateCitations);
		assertThat(patentResults.size(), is(420));
		assertThat(nplResults.size(), is(80));*/
			
		File xmlFile = new File(this.getResourceDir("./src/test/resources/").getAbsoluteFile()
			+ "/patents/EP1059354A2.xml");
		if (!xmlFile.exists()) {
			throw new GrobidException("Cannot start test, because test resource folder is not correctly set.");
		}
		/*engine.processAllCitationsInXMLPatent(xmlFile.getPath(), nplResults, patentResults, consolidateCitations);
		System.out.println("Patent references: " + patentResults.size());
		System.out.println("Non patent references: " + nplResults.size());*/
	}
	
	//@Test
	public void testTrainingPatent() throws Exception {
				getTestResourcePath();
		
		String xmlPath = this.getResourceDir("./src/test/resources/").getAbsoluteFile()+"/patents/sample1.xml";
		engine.createTrainingPatentCitations(xmlPath, newTrainingPath);
			
		xmlPath = this.getResourceDir("./src/test/resources/").getAbsoluteFile()+"/patents/sample2.xml";
		engine.createTrainingPatentCitations(xmlPath, newTrainingPath);
	}
}