package org.grobid.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.grobid.core.data.Affiliation;
import org.junit.Test;

/**
 *  @author Patrice Lopez
 */
public class TestAffiliationAddressParser extends EngineTest{
	
	@Test
	public void testParser() throws Exception {


		String affiliationSequence1 = "Atomic Physics Division, Department of Atomic Physics and Luminescence, " + 
									  "Faculty of Applied Physics and Mathematics, Gdansk University of " + 
									  "Technology, Narutowicza 11/12, 80-233 Gdansk, Poland";			
		List<Affiliation> res = engine.processAffiliation(affiliationSequence1);	
		assertEquals(1, res.size());
		if (res.size() > 0) {
			assertNotNull(res.get(0).getInstitutions());
			assertEquals(1, res.get(0).getInstitutions().size());
			assertEquals(res.get(0).getInstitutions().get(0), "Gdansk University of Technology");
			assertEquals(res.get(0).getCountry(), "Poland");
			assertEquals(res.get(0).getAddrLine(), "Narutowicza 11/12");
		}
		
		String affiliationSequence2 = "Faculty of Health, School of Biomedical Sciences, " + 
			"University of Newcastle, New South Wales, Australia.";
		List<Affiliation> res2 = engine.processAffiliation(affiliationSequence2);	
		if (res2.size() > 0) {
			assertNotNull(res.get(0).getInstitutions());
		}
	}
}