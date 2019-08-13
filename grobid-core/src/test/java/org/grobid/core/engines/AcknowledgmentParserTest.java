package org.grobid.core.engines;

import org.grobid.core.data.Acknowledgment;
import org.grobid.core.data.Date;
import org.grobid.core.factory.AbstractEngineFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * Created by Tanti 12/08/2019
 */

public class AcknowledgmentParserTest {
       AcknowledgmentParser target;

    @BeforeClass
    public static void setInitialContext() throws Exception {
        AbstractEngineFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        target = new AcknowledgmentParser();
    }

    @Test
    public void processing_akwnowledgment_shouldWork() throws Exception {
        List<Acknowledgment> output = target.processing("This research was supported by the Deutsche Forschungsgemeinschaft through the SFB 649 \"Economic Risk\". http://sfb649.wiwi.hu-berlin.de ISSN 1860-5664");
        //assertThat(output, hasSize(1));
        final Acknowledgment acknowledgment = output.get(0);
        //assertThat(acknowledgment.getFundingAgency(), is("the Deutsche Forschungsgemeinschaft"));
        //assertThat(acknowledgment.getProjectName(), is("the SFB 649 \"Economic Risk\""));

        System.out.println(output.get(0));
    }
}