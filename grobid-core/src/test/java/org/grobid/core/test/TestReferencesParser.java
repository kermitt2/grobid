package org.grobid.core.test;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.main.GrobidConstants;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Patrice Lopez
 */
public class TestReferencesParser extends EngineTest {

    private String getTestResourcePath() {
        return GrobidConstants.TEST_RESOURCES_PATH;
    }

    //@Test
    public void testReferences() throws Exception {
        String testPath = getTestResourcePath();

        String pdfPath = testPath + File.separator + "Wang-paperAVE2008.pdf";
        List<BibDataSet> resRefs = engine.processReferences(new File(pdfPath), true);

        assertNotNull(resRefs);
        assertThat(resRefs.size(), is(12));
    }

}