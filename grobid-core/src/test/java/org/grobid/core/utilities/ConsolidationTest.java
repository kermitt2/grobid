package org.grobid.core.utilities;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ConsolidationTest {


    @Test
    public void testCleanDoiPrefix1_shouldRemovePrefix() throws Exception {

        String doi = "doi:10.1063/1.1905789";
        String cleanDoi = Consolidation.cleanDoi(doi);

        assertThat(cleanDoi, is("10.1063/1.1905789"));
    }

    @Test
    public void testCleanDoiPrefix2_shouldRemovePrefix() throws Exception {

        String doi = "doi/10.1063/1.1905789";
        String cleanDoi = Consolidation.cleanDoi(doi);

        assertThat(cleanDoi, is("10.1063/1.1905789"));
    }

    @Test
    public void testCleanDoi_diactric() throws Exception {
        String doi = "10.1063/1.1905789͔";

        String cleanDoi = Consolidation.cleanDoi(doi);

        assertThat(cleanDoi, is("10.1063/1.1905789"));
    }

}