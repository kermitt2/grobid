package org.grobid.core.features;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.lexicon.Lexicon;

import static org.junit.Assert.*;

public class TestFeatures {

	@BeforeClass
	public static void setInitialContext() throws Exception{
		MockContext.setInitialContext();
		GrobidProperties.getInstance();
		Lexicon.getInstance();
	}
	
	@AfterClass
	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}

	@Test
	public void testDiscretizeLinearScale() {
		FeatureFactory featureFactory = FeatureFactory.getInstance();
		int val = 30;
		int total = 100;
		int nbBins = 10;
System.out.println("linear " + nbBins);
System.out.println(FeatureFactory.linearScaling(0, total, nbBins));
System.out.println(FeatureFactory.linearScaling(10, total, nbBins));
System.out.println(FeatureFactory.linearScaling(20, total, nbBins));
System.out.println(FeatureFactory.linearScaling(50, total, nbBins));
System.out.println(FeatureFactory.linearScaling(70, total, nbBins));
System.out.println(FeatureFactory.linearScaling(100, total, nbBins));

		assertEquals("Discretized value is not the expected one",
				3,
				FeatureFactory.linearScaling(val, total, nbBins));

		nbBins = 7;

System.out.println("linear " + nbBins);
System.out.println(FeatureFactory.linearScaling(10, total, nbBins));
System.out.println(FeatureFactory.linearScaling(20, total, nbBins));
System.out.println(FeatureFactory.linearScaling(50, total, nbBins));
System.out.println(FeatureFactory.linearScaling(70, total, nbBins));

		assertEquals("Discretized value is not the expected one",
				2,
				FeatureFactory.linearScaling(val, total, nbBins));


		double valD = 0.3;
		double totalD = 1.0;
		nbBins = 10;

System.out.println("linear (double) " + nbBins);
System.out.println(FeatureFactory.linearScaling(0.1, totalD, nbBins));
System.out.println(FeatureFactory.linearScaling(0.2, totalD, nbBins));
System.out.println(FeatureFactory.linearScaling(0.5, totalD, nbBins));
System.out.println(FeatureFactory.linearScaling(0.7, totalD, nbBins));

		assertEquals("Discretized value is not the expected one",
				3,
				FeatureFactory.linearScaling(valD, totalD, nbBins));
		nbBins = 8;
		assertEquals("Discretized value is not the expected one",
				2,
				FeatureFactory.linearScaling(valD, totalD, nbBins));
	}
	
	@Test
	public void testDiscretizeLogScale() {
		FeatureFactory featureFactory = FeatureFactory.getInstance();
		double valD = 0.3;
		double totalD = 1.0;
		int nbBins = 12;

System.out.println("log (double) " + nbBins);
System.out.println(FeatureFactory.logScaling(0.0, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(0.1, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(0.2, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(0.5, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(0.7, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(1.0, totalD, nbBins));

		assertEquals("Discretized value is not the expected one",
				4,
				FeatureFactory.logScaling(valD, totalD, nbBins));
		nbBins = 8;

System.out.println("log (double) " + nbBins);
System.out.println(FeatureFactory.logScaling(0.0, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(0.1, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(0.2, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(0.5, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(0.7, totalD, nbBins));
System.out.println(FeatureFactory.logScaling(1.0, totalD, nbBins));

		assertEquals("Discretized value is not the expected one",
				3,
				FeatureFactory.logScaling(valD, totalD, nbBins));
	}
	

}
