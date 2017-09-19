package org.grobid.core.utilities;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class ConsolidationIntegrationTest {

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
    @Ignore("Crossref API not realiable for the moment")
    public void testConsolidationDOISimple() throws Exception {
        BiblioItem biblio = new BiblioItem();
        biblio.setDOI(DOIs[0]);
        List<BiblioItem> bib2 = new ArrayList<BiblioItem>();

        boolean found = target.consolidateCrossrefGetByDOI(biblio, bib2);
        assertEquals("The consolidation has not the expected outcome", true, found);

    }

    @Test
    @Ignore("Crossref API not realiable for the moment")
    public void testConsolidationDOIMultiple() throws Exception {
        List<BiblioItem> biblios = new ArrayList<BiblioItem>();
        for (int i = 0; i < DOIs.length; i++) {
            BiblioItem biblio = new BiblioItem();
            biblio.setDOI(DOIs[i]);
            biblios.add(biblio);
        }
        for (BiblioItem biblio : biblios) {
            List<BiblioItem> bib2 = null;
            boolean found = target.consolidateCrossrefGetByDOI(biblio, bib2);
            assertEquals("The consolidation has not the expected outcome", true, found);
            assertNotNull(bib2);
            assertThat(bib2, hasSize(1));
        }
    }


}