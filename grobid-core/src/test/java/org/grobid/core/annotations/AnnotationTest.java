package org.grobid.core.annotations;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_HIGH_LABEL;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_LOW_LABEL;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_MEDIUM_LABEL;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_VERY_HIGH_LABEL;
import static org.grobid.core.utilities.TeiValues.CERTAINTY_VERY_LOW_LABEL;
import static org.grobid.core.utilities.TeiValues.DEGREE_OF_CONFIDENCE;
import static org.grobid.core.utilities.TeiValues.NO_CERTAINTY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.PatentItem;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.factory.AbstractEngineFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.XMLWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class AnnotationTest extends XMLTestCase {

	public static void setInitialContext() throws Exception {
		MockContext.setInitialContext();
		AbstractEngineFactory.init();
	}

	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}

	@Before
	public void setUp() throws Exception {
		setInitialContext();
	}

	@After
	public void tearDown() throws Exception {
		destroyInitialContext();
	}

	@Test
	public void testAnnotation() {
		final Annotation atn = new Annotation(new ArrayList<PatentItem>(), new ArrayList<BibDataSet>(), new Description(false));
		assertNotNull(atn.patents);
		assertNotNull(atn.articles);
		assertNotNull(atn.description);
	}

	@Test
	public void testGetHeaderAnnotationEmpty() {
		final Annotation atn = new Annotation(new ArrayList<PatentItem>(), new ArrayList<BibDataSet>(), new Description(false));
		assertEquals(EMPTY, atn.getHeaderAnnotation(false));
	}

	@Test
	public void testGetHeaderAnnotationBibDataSet() throws SAXException, IOException {
		final List<BibDataSet> articles = new ArrayList<BibDataSet>();
		BibDataSet item = new BibDataSet();
		item.setResBib(new BiblioItem());
		articles.add(item);
		final Annotation atn = new Annotation(new ArrayList<PatentItem>(), articles, new Description(false));
		assertXMLEqual(
				"<note type=\"standoff-annotation\" subtype=\"automatic-annotation\"><list type=\"automatic-annotation\"></list></note>",
				atn.getHeaderAnnotation(false));
	}

	@Test
	public void testGetHeaderAnnotationPatentItem() throws SAXException, IOException {
		final List<PatentItem> patents = new ArrayList<PatentItem>();
		PatentItem item = new PatentItem();
		item.setAuthority("AUTH");
		patents.add(item);
		final Annotation atn = new Annotation(patents, new ArrayList<BibDataSet>(), new Description(false));
		assertXMLEqual(
				"<note type=\"standoff-annotation\" subtype=\"automatic-annotation\"><list type=\"automatic-annotation\"></list></note>",
				atn.getHeaderAnnotation(false));
	}

	/*@Test
	public void testGetConfidenceRateFromItem() {
		try {
			Annotation.getConfidenceRateFromItem(new String());
			assertFalse("getConfidenceRateFromItem should have thrown an exception.", true);
		} catch (GrobidException gbdExp) {
			// OK GrobidException has been thrown.
		}
	}*/

	@Test
	public void testConfidenceRateToString() {
		assertEquals(CERTAINTY_VERY_LOW_LABEL + DEGREE_OF_CONFIDENCE, Annotation.confidenceRateToString(10));
		assertEquals(CERTAINTY_LOW_LABEL + DEGREE_OF_CONFIDENCE, Annotation.confidenceRateToString(30));
		assertEquals(CERTAINTY_MEDIUM_LABEL + DEGREE_OF_CONFIDENCE, Annotation.confidenceRateToString(50));
		assertEquals(CERTAINTY_HIGH_LABEL + DEGREE_OF_CONFIDENCE, Annotation.confidenceRateToString(70));
		assertEquals(CERTAINTY_VERY_HIGH_LABEL + DEGREE_OF_CONFIDENCE, Annotation.confidenceRateToString(90));
		assertEquals(NO_CERTAINTY + DEGREE_OF_CONFIDENCE, Annotation.confidenceRateToString(200));
	}

	/*@Test
	public void testBuildItem() throws XMLStreamException {
		final Annotation atn = new Annotation(new ArrayList<PatentItem>(), new ArrayList<BibDataSet>(), new Description(false));
		atn.writer = new XMLWriter();
		atn.buildItem("<someTEI/>", "PTR", 0, "CONFIDENCE", "DATE");
		assertEquals(
				"<item><date when=\"DATE\"></date><author type=\"softwareApplication\"><appInfo><application version=\"1.0\" ident=\"GROBID\"><label>GROBID Annotator</label></application></appInfo><certainty cert=\"0.0\"><label>CONFIDENCE</label></certainty></author><someTEI/><ptr target=\"PTR\"></ptr></item>",
				atn.writer.toString());
	}*/

}
