package org.grobid.service.util;

import static org.junit.Assert.assertEquals;

import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class GrobidPropertiesUtilTest {
	
	@BeforeClass
	public static void setInitialContext() throws Exception {
		MockContext.setInitialContext();
		GrobidServiceProperties.getInstance();
		GrobidProperties.getInstance();
	}

	@AfterClass
	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}

	@Test
	public void testgetAllPropertiesList() {
		assertEquals("Wrong expected number of properties.",
				GrobidServiceProperties.getProps().size()
						+ GrobidProperties.getProps().size(),
				GrobidPropertiesUtil.getAllPropertiesList().size());
	}
}
