package org.grobid.core.engines.patent;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.grobid.core.factory.AbstractEngineFactory;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceExtractorTest {

	public static final Logger LOGGER = LoggerFactory
			.getLogger(ReferenceExtractorTest.class);

	@BeforeClass
	public static void setInitialContext() throws Exception {
		MockContext.setInitialContext();
		AbstractEngineFactory.init();
	}

	@AfterClass
	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}

	// extractor.extractAllReferencesXMLFile(new
	// File("src/test/resources/org/grobid/core/engines/patent/ReferenceExtractor/sample-24514352.tei.xml").getAbsolutePath(),
	// false, false, patents, articles);

	@Test
	public void extractAllReferencesStringNull() {
		ReferenceExtractor extractor = new ReferenceExtractor();
		int nbRes = extractor
				.extractAllReferencesString(
						"Economic Development Quarterly November 2011 25: 353-365, first published on August 25, 2011.",
						false, false, null, null);
		assertEquals(0, nbRes);
	}

	@Test
	public void extractAllReferencesStringArticles() {
		ReferenceExtractor extractor = new ReferenceExtractor();
		List<PatentItem> patents = new ArrayList<PatentItem>();
		List<BibDataSet> articles = new ArrayList<BibDataSet>();
		extractor
				.extractAllReferencesString(
						"Economic Development Quarterly November 2011 25: 353-365, first published on August 25, 2011.",
						false, false, patents, articles);
		LOGGER.info("BibDataSet: " + articles.toString());
		assertEquals(0, patents.size());
		assertEquals(1, articles.size());
	}

	@Test
	public void extractAllReferencesStringPatents() {
		ReferenceExtractor extractor = new ReferenceExtractor();
		List<PatentItem> patents = new ArrayList<PatentItem>();
		List<BibDataSet> articles = new ArrayList<BibDataSet>();
		extractor
				.extractAllReferencesString(
						"US-8303618, Intravascular filter and method A filter disposed at the distal end of an elongate guidewire. Catheters are provided for delivering the filter to, and retrieving the filter from, a treatment...",
						false, false, patents, articles);
		LOGGER.info("PatentItem: " + patents.toString());
		assertEquals(1, patents.size());
		assertEquals(0, articles.size());
		assertEquals("8303618", patents.get(0).getNumber());
	}
	
	@Ignore
	@Test
	public void extractAllReferencesXmlST36() {
		ReferenceExtractor extractor = new ReferenceExtractor();
		List<PatentItem> patents = new ArrayList<PatentItem>();
		List<BibDataSet> articles = new ArrayList<BibDataSet>();
		extractor
				.extractAllReferencesXMLFile(
						new File(
								"src/test/resources/org/grobid/core/engines/patent/ReferenceExtractor/st36-sample-1.xml")
								.getAbsolutePath(), false, false, patents,
						articles);
		LOGGER.info("PatentItem: " + patents.toString());
		assertEquals(2, patents.size());
		assertEquals(0, articles.size());
		assertEquals("9937368", patents.get(0).getNumber());
		assertEquals("6083121", patents.get(1).getNumber());
	}

	@Test
	@Ignore
	public void extractAllReferencesPdf() {
		ReferenceExtractor extractor = new ReferenceExtractor();
		List<PatentItem> patents = new ArrayList<PatentItem>();
		List<BibDataSet> articles = new ArrayList<BibDataSet>();
		extractor
				.extractAllReferencesPDFFile(
						new File(
								"src/test/resources/org/grobid/core/engines/patent/ReferenceExtractor/sample-1.pdf")
								.getAbsolutePath(), false, false, patents,
						articles);
	}
}
