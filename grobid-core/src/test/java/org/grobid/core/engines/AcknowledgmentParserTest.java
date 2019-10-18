package org.grobid.core.engines;

import org.grobid.core.data.Acknowledgment;
import org.grobid.core.data.Date;
import org.grobid.core.document.TEIFormatter;
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
    public void processing_akwnowledgment1_shouldWork() throws Exception {
        List<Acknowledgment> output = target.processing("This research was supported by the Deutsche Forschungsgemeinschaft through the SFB 649 \"Economic Risk\". http://sfb649.wiwi.hu-berlin.de ISSN 1860-5664");

        final Acknowledgment acknowledgment = output.get(0);
        assertThat(acknowledgment.getFundingAgency(), is("the Deutsche Forschungsgemeinschaft"));
        assertThat(acknowledgment.getProjectName(), is("the SFB 649 \" Economic Risk \""));

        //System.out.println(output.get(0).getFundingAgency());
    }

    @Test
    public void processing_akwnowledgment2_shouldWork() throws Exception {
        List<Acknowledgment> output = target.processing("This research was funded by Computational Science grant #635.000.014 from " +
            "the Netherlands Organization for Scientific Research (NWO). " +
            "Mikas Vengris, Denitsa Grancharova and Rienk van Grondelle provided the data modeled in Section 5.6. Rob Koehorst, " +
            "Bart van Oort, Sergey Laptenok, Ton Visser and Herbert van Amerongen provided the data modeled in Section 6.3. " +
            "Joris Snellenburg is thanked for constructive comments on the text. Uwe Ligges and Martin Mächler collaborated " +
            "in the implementation of the nls options described in Section B. Achim Zeileis contributed helpful suggestions regarding the figures.");

        for (Acknowledgment acknowledgment : output) {
            if (acknowledgment.getAffiliation() != null){
                System.out.println(acknowledgment.getAffiliation());
            }
            if (acknowledgment.getEducationalInstitution() != null){
                System.out.println(acknowledgment.getEducationalInstitution());
            }
            if (acknowledgment.getFundingAgency() != null){
                System.out.println(acknowledgment.getFundingAgency());
            }
            if (acknowledgment.getGrantName() != null){
                System.out.println(acknowledgment.getGrantName());
            }
            if (acknowledgment.getGrantNumber() != null){
                System.out.println(acknowledgment.getGrantNumber());
            }
            if (acknowledgment.getOtherInstitution() != null){
                System.out.println(acknowledgment.getOtherInstitution());
            }
            if (acknowledgment.getProjectName() != null){
                System.out.println(acknowledgment.getProjectName());
            }
            if (acknowledgment.getResearchInstitution() != null){
                System.out.println(acknowledgment.getResearchInstitution());
            }
            if (acknowledgment.getIndividual() != null){
                System.out.println(acknowledgment.getIndividual());
            }
        }
    }
}