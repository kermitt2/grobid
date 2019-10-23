package org.grobid.core.engines;

import org.grobid.core.data.AcknowledgmentItem;
import org.grobid.core.data.Acknowledgment;
import org.grobid.core.factory.AbstractEngineFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Created by Tanti 12/08/2019
 */

public class AcknowledgmentParserTest {
       AcknowledgmentParserOld targetOld;
       AcknowledgmentParser targetNew;

    @BeforeClass
    public static void setInitialContext() throws Exception {
        AbstractEngineFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        targetOld = new AcknowledgmentParserOld();
        targetNew = new AcknowledgmentParser();
    }

    @Ignore ("old method")
    @Test
    public void processing_akwnowledgmentOld_shouldWork() throws Exception {
        List<Acknowledgment> output = targetOld.processing("This research was supported by the Deutsche Forschungsgemeinschaft through the SFB 649 \"Economic Risk\". http://sfb649.wiwi.hu-berlin.de ISSN 1860-5664");

        final Acknowledgment acknowledgment = output.get(0);
        assertThat(acknowledgment.getFundingAgency(), is("the Deutsche Forschungsgemeinschaft"));
        assertThat(acknowledgment.getProjectName(), is("the SFB 649 \" Economic Risk \""));
    }

    @Test
    public void processing_akwnowledgmentNew1_shouldWork() throws Exception {
        List<AcknowledgmentItem> output = targetNew.processing("This research was supported by the Deutsche Forschungsgemeinschaft through the SFB 649 \"Economic Risk\". http://sfb649.wiwi.hu-berlin.de ISSN 1860-5664");
        AcknowledgmentItem acknowledgment = new AcknowledgmentItem();

        acknowledgment = output.get(0);

        assertThat(acknowledgment.getText(), is("the Deutsche Forschungsgemeinschaft"));
        assertThat(acknowledgment.getLabel(), is("fundingAgency"));
    }

    @Test
    public void processing_akwnowledgmentNew2_shouldWork() throws Exception {
        List<AcknowledgmentItem> output = targetNew.processing("This research was funded by Computational Science grant #635.000.014 from " +
            "the Netherlands Organization for Scientific Research (NWO). " +
            "Mikas Vengris, Denitsa Grancharova and Rienk van Grondelle provided the data modeled in Section 5.6. Rob Koehorst, " +
            "Bart van Oort, Sergey Laptenok, Ton Visser and Herbert van Amerongen provided the data modeled in Section 6.3. " +
            "Joris Snellenburg is thanked for constructive comments on the text. Uwe Ligges and Martin Mächler collaborated " +
            "in the implementation of the nls options described in Section B. Achim Zeileis contributed helpful suggestions regarding the figures.");

        assertThat(output.get(1).getText(), is("the Netherlands Organization for Scientific Research (NWO)"));
        assertThat(output.get(1).getLabel(), is("fundingAgency"));

        assertThat(output.get(2).getText(), is("Mikas Vengris"));
        assertThat(output.get(2).getLabel(), is("individual"));

        assertThat(output.get(3).getText(), is("Denitsa Grancharova"));
        assertThat(output.get(3).getLabel(), is("individual"));
    }
}