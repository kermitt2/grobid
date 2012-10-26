package org.grobid.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.OffsetPosition;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *  @author Patrice Lopez
 */
public class TestFastMatcher {
	private Lexicon lexicon = null;
	
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
		lexicon = Lexicon.getInstance();
	}
	
	@Test
	public void testLexiconMatcher() {
		
		// journals
		String input = "Nature";
		List<OffsetPosition> journalsPositions = lexicon.inAbbrevJournalNames(input);
		assertNotNull(journalsPositions);
		if (journalsPositions != null) {
			assertEquals("Problem with journal matcher", journalsPositions.size(), 1);
			if (journalsPositions.size() > 0) {
				assertEquals("Problem with matching position", journalsPositions.get(0).start, 0);
			}
		}
		
		input = "in Nature, volume";
		journalsPositions = lexicon.inAbbrevJournalNames(input);
		assertNotNull(journalsPositions);
		if (journalsPositions != null) {
			assertEquals("Problem with journal matcher", journalsPositions.size(), 1);
			if (journalsPositions.size() > 0) {
				assertEquals("Problem with matching position", journalsPositions.get(0).start, 1);
			}
		}
		
		input = "Taylor, et al., Nature 297:(1982)";
		journalsPositions = lexicon.inJournalNames(input);
		assertNotNull(journalsPositions);
		if (journalsPositions != null) {
			assertEquals("Problem with journal matcher", journalsPositions.size(), 1);
			if (journalsPositions.size() > 0) {
				assertEquals("Problem with matching position", journalsPositions.get(0).start, 6);
				assertEquals("Problem with matching position", journalsPositions.get(0).end, 6);
			}
		}
		
		input = "to be published in the official publication of the National Venereology Council " + 
			"of Australia, volume 10, 2010.";
		journalsPositions = lexicon.inJournalNames(input);
		assertNotNull(journalsPositions);
		if (journalsPositions != null) {
			assertEquals("Problem with journal matcher", journalsPositions.size(), 2);
		}
		
		// cities 
		input = "University of New-York, USA, bla bla City, bla";
		List<OffsetPosition> citiesPositions = lexicon.inCityNames(input);
		assertNotNull(citiesPositions);
		if (journalsPositions != null) {
			assertEquals("Problem with city matcher", journalsPositions.size(), 2);
			//System.out.println(citiesPositions);
		}
	}

}