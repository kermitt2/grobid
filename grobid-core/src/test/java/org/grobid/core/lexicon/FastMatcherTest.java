package org.grobid.core.lexicon;

import java.io.File;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.mock.MockContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FastMatcherTest {
	
	@BeforeClass
	public static void setInitialContext() throws Exception{
		MockContext.setInitialContext();
		GrobidProperties.getInstance();
	}
	
	@AfterClass
	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}

	@Test
	public void testFastMatcher() {
		FastMatcher fmtch = new FastMatcher(new File(
				GrobidProperties.getGrobidHomePath()
						+ "/lexicon/journals/abbrev_journals.txt"));
	}

}
