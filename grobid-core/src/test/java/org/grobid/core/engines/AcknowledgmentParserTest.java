package org.grobid.core.engines;

import org.grobid.core.data.AcknowledgmentItem;
import org.grobid.core.factory.AbstractEngineFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


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
    public void processing_akwnowledgmentNew1_shouldWork() throws Exception {
        List<AcknowledgmentItem> output = target.processing("This research was supported by the Deutsche Forschungsgemeinschaft through the SFB 649 \"Economic Risk\". http://sfb649.wiwi.hu-berlin.de ISSN 1860-5664");


        assertThat(output.get(0).getText(), is("Deutsche Forschungsgemeinschaft"));
        assertThat(output.get(0).getLabel(), is("fundingAgency"));

        assertThat(output.get(1).getText(), is("SFB 649 \"Economic Risk\""));
        assertThat(output.get(1).getLabel(), is("projectName"));
    }

    @Test
    public void processing_akwnowledgmentNew2_shouldWork() throws Exception {
        List<AcknowledgmentItem> output = target.processing("This research was funded by Computational Science grant #635.000.014 from " +
            "the Netherlands Organization for Scientific Research (NWO). " +
            "Mikas Vengris, Denitsa Grancharova and Rienk van Grondelle provided the data modeled in Section 5.6. Rob Koehorst, " +
            "Bart van Oort, Sergey Laptenok, Ton Visser and Herbert van Amerongen provided the data modeled in Section 6.3. " +
            "Joris Snellenburg is thanked for constructive comments on the text. Uwe Ligges and Martin Mächler collaborated " +
            "in the implementation of the nls options described in Section B. Achim Zeileis contributed helpful suggestions regarding the figures.");

        assertThat(output.get(1).getText(), is("#635.000.014"));
        assertThat(output.get(1).getLabel(), is("grantNumber"));

        assertThat(output.get(2).getText(), is("Netherlands Organization for Scientific Research (NWO)"));
        assertThat(output.get(2).getLabel(), is("fundingAgency"));

        assertThat(output.get(3).getText(), is("Mikas Vengris"));
        assertThat(output.get(3).getLabel(), is("individual"));

        assertThat(output.get(4).getText(), is("Denitsa Grancharova"));
        assertThat(output.get(4).getLabel(), is("individual"));
    }
}