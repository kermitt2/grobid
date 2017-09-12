package org.grobid.core.utilities;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ConsolidationTest {

	private Consolidation target = null;

	public static String[] DOIs = {
		"10.1086/107043",
		"10.1086/102351",
		"10.1086/100853",
		"10.1086/105172"
	};

    @Before
    public void setUp() {
    	LibraryLoader.load();
        GrobidProperties.getInstance();

        target = new Consolidation(null);
    }

	@Test
	public void testConsolidationDOISimple() {
		BiblioItem biblio = new BiblioItem();
		biblio.setDOI(DOIs[0]);
		List<BiblioItem> bib2 = new ArrayList<BiblioItem>();
		try {
			boolean found = target.consolidateCrossrefGetByDOI(biblio, bib2);
			assertEquals("The consolidation has not the expected outcome", true, found);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			//target.close();
		}
	}

	//@Test
	public void testConsolidationDOIMultiple() {
		List<BiblioItem> biblios = new ArrayList<BiblioItem>();
		for(int i=0; i<DOIs.length; i++) {
			BiblioItem biblio = new BiblioItem();
			biblio.setDOI(DOIs[i]);
			biblios.add(biblio);
		}
		for(BiblioItem biblio : biblios) {
			List<BiblioItem> bib2 = null;
			try {
				boolean found = target.consolidateCrossrefGetByDOI(biblio, bib2);
				assertEquals("The consolidation has not the expected outcome", true, found);
				assertNotNull(bib2);
				assertThat(bib2.size(), is(1));
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				//target.close();
			}
		}
	}


}