package org.grobid.core.engines.patent;

import static org.grobid.core.utilities.TextUtilities.delimiters;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.grobid.core.factory.AbstractEngineFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidTimer;
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
		String res = extractor
				.extractAllReferencesString(
						"Economic Development Quarterly November 2011 25: 353-365, first published on August 25, 2011.",
						false, false, null, null);
	}

	@Test
	public void extractAllReferencesStringArticles() {
		ReferenceExtractor extractor = new ReferenceExtractor();
		List<PatentItem> patents = new ArrayList<PatentItem>();
		List<BibDataSet> articles = new ArrayList<BibDataSet>();
		
		//First description.This desciption was published by ABBS Editorial Office in association with Oxford University  Press on behalf of the Institute of Biochemistry and Cell Biology, Shanghai Institutes for Biological Sciences,  Chinese Academy of Sciences.Some other description. ref. US 2011/0155847 A1 aerodynamic and applied physics.  This patent, ref. US 7930197 says data mining of personal data is patented.  That article refers to Economic Development Quarterly November 2011 25: 353-365, first published on August 25, 2011.
		
		String toExtract = "That article It refers to Economic Development Quarterly November 2011 25: 353-365, first published on August 25, 2011.";
		toExtract = "First description.This desciption was published by ABBS Editorial Office in association with Oxford University " +
                "Press on behalf of the Institute of Biochemistry and Cell Biology, Shanghai Institutes for Biological Sciences, " +
                "Chinese Academy of Sciences.Some other description. ref. US 2011/0155847 A1 aerodynamic and applied physics. " +
                "This patent, ref. US 7930197 says data mining of personal data is patented. " +
                "That article refers to Economic Development Quarterly November 2011 25: 353-365, first published on August 25, 2011." +
                "Third description.First description.This desciption was published by ABBS Editorial Office in association with Oxford University " +
                "Press on behalf of the Institute of Biochemistry and Cell Biology, Shanghai Institutes for Biological Sciences, Chinese Academy of Sciences.Some other description.";
		GrobidTimer timer = new GrobidTimer(true);
		extractor.extractAllReferencesString(toExtract, false, false, patents, articles);
		timer.stop("STOP");
		System.out.println(timer.getElapsedTimeFromStartFormated("STOP"));
		LOGGER.info("BibDataSet: " + articles.toString());
		assertEquals(2, patents.size());
		assertEquals(2, articles.size());
		LOGGER.info(articles.get(0).getOffsets().toString());
	}

	@Test
	public void extractAllReferencesStringArticles2() {
		ReferenceExtractor extractor = new ReferenceExtractor();
		List<PatentItem> patents = new ArrayList<PatentItem>();
		List<BibDataSet> articles = new ArrayList<BibDataSet>();
		extractor
				.extractAllReferencesString(
						"This article refers to Economic Development Quarterly November 2011 25: 353-365, first published on August 25, 2011.",
						false, false, patents, articles);
		LOGGER.info("BibDataSet: " + articles.toString());
		assertEquals(0, patents.size());
		assertEquals(1, articles.size());

		List<Integer> offsets = articles.get(0).getOffsets();
		int startOffset = -1;
		int endOffset = -1;
		String rawBib = articles.get(0).getRawBib();
		if (!offsets.isEmpty() && offsets.get(0) != null) {
			startOffset = offsets.get(0).intValue();
			StringTokenizer stt = new StringTokenizer(rawBib, delimiters, true);
			int count2 = 0;
			int charCpt = 0;
			String token2;
			while (stt.hasMoreTokens()) {
				token2 = stt.nextToken();

				if (token2.trim().length() != 0) {
					count2++;
				}

				charCpt += token2.length();
				System.err.println(token2 + "   count=" + count2
						+ "   charCpt=" + charCpt);
			}
			endOffset = startOffset + count2;

			System.out.println("RawBib=" + rawBib);
			System.out.println("Start=" + startOffset + "   offset=" + count2
					+ "   end=" + endOffset);
		}

		LOGGER.info(articles.get(0).getOffsets().toString());
	}

	@Test
	public void extractAllReferencesStringPatents() {
		ReferenceExtractor extractor = new ReferenceExtractor();
		List<PatentItem> patents = new ArrayList<PatentItem>();
		List<BibDataSet> articles = new ArrayList<BibDataSet>();
		String toExtract = "US-8303618, Intravascular filter and method A filter disposed at the distal end of an elongate guidewire. Catheters are provided for delivering the filter to, and retrieving the filter from, a treatment...";
		//toExtract = "this patent refers US-8303618, bla bla";
		extractor.extractAllReferencesString(toExtract, false, false, patents,
				articles);
		LOGGER.info("PatentItem: " + patents.toString());
		assertEquals(1, patents.size());
		assertEquals(0, articles.size());
		PatentItem patent = patents.get(0);
		assertEquals("8303618", patent.getNumber());
		System.out.println("context=" + patent.getContext());
		System.out.println("offset start/end/raw=" + patent.getOffsetBegin()
				+ "/" + patent.getOffsetEnd() + "/" + patent.getOffsetRaw());
	}

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
