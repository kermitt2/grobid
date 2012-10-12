package org.grobid.core.engines;


import org.grobid.core.GrobidModels;
import org.grobid.core.main.LibraryLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelMapTest {
	
	@BeforeClass
	public static void init(){
		LibraryLoader.load();
	}

	@Test
	public void testGetNewModel(){
		// assertEquals("Wrong value of getModel", "-m "+GrobidModels.CITATION.getModelPath()+" ", GrobidModels.CITATION.getModelPath());
	}
}
