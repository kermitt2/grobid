package org.grobid.trainer;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class StatsTest {
    Stats target;

    @Before
    public void setUp() throws Exception {
        target = new Stats();
    }

    @Test
    public void testPrecision_fullMatch() throws Exception {
        target.getLabelStat("BAO").setExpected(4);
        target.getLabelStat("BAO").setObserved(4);
        assertThat(target.getLabelStat("BAO").getPrecision(), is(1.0));
    }

    @Test
    public void testPrecision_noMatch() throws Exception {
        target.getLabelStat("MIAO").setExpected(4);
        target.getLabelStat("MIAO").setObserved(0);
        target.getLabelStat("MIAO").setFalsePositive(3);
        target.getLabelStat("MIAO").setFalseNegative(1);
        assertThat(target.getLabelStat("MIAO").getPrecision(), is(0.0));
    }

    @Test
    public void testPrecision_missingMatch() throws Exception {
        // The precision stays at 1.0 because none of the observed
        // is wrong (no false positives)
        target.getLabelStat("CIAO").setExpected(4);
        target.getLabelStat("CIAO").setObserved(1);
        target.getLabelStat("CIAO").setFalseNegative(3);
        assertThat(target.getLabelStat("CIAO").getPrecision(), is(1.0));
    }

    @Test
    public void testPrecision_2wronglyRecognised() throws Exception {
        target.getLabelStat("ZIAO").setExpected(4);
        target.getLabelStat("ZIAO").setObserved(1);
        target.getLabelStat("ZIAO").setFalsePositive(2);

        assertThat(target.getLabelStat("ZIAO").getPrecision(), is(0.3333333333333333));
    }

    @Test
    public void testRecall_fullMatch() throws Exception {
        target.getLabelStat("BAO").setExpected(4);
        target.getLabelStat("BAO").setObserved(4);
        assertThat(target.getLabelStat("BAO").getRecall(), is(1.0));
    }

    @Test
    public void testRecall_noMatch() throws Exception {
        target.getLabelStat("MIAO").setExpected(4);
        target.getLabelStat("MIAO").setObserved(0);
        target.getLabelStat("MIAO").setFalsePositive(3);
        target.getLabelStat("MIAO").setFalseNegative(1);
        assertThat(target.getLabelStat("MIAO").getRecall(), is(0.0));
    }

    @Test
    public void testRecall_oneOverFour() throws Exception {
        target.getLabelStat("CIAO").setExpected(4);
        target.getLabelStat("CIAO").setObserved(1);
        target.getLabelStat("CIAO").setFalseNegative(3);
        assertThat(target.getLabelStat("CIAO").getRecall(), is(0.25));

    }

    @Test
    public void testRecall_partialMatch() throws Exception {
        target.getLabelStat("ZIAO").setExpected(4);
        target.getLabelStat("ZIAO").setObserved(3);
        target.getLabelStat("ZIAO").setFalsePositive(1);

        assertThat(target.getLabelStat("ZIAO").getPrecision(), is(0.75));
    }


    // Average measures

    @Test
    public void testMicroAvgPrecision_shouldWork() throws Exception {
        target.getLabelStat("BAO").setExpected(4);
        target.getLabelStat("BAO").setObserved(4);

        target.getLabelStat("MIAO").setExpected(4);
        target.getLabelStat("MIAO").setObserved(0);
        target.getLabelStat("MIAO").setFalsePositive(3);
        target.getLabelStat("MIAO").setFalseNegative(1);

        target.getLabelStat("CIAO").setExpected(4);
        target.getLabelStat("CIAO").setObserved(1);
        target.getLabelStat("CIAO").setFalseNegative(3);

        target.getLabelStat("ZIAO").setExpected(4);
        target.getLabelStat("ZIAO").setObserved(0);
        target.getLabelStat("ZIAO").setFalsePositive(2);

        assertThat(target.getMicroAveragePrecision(), is(((double) 4 + 0 + 1 + 0) / (4 + 0 + 3 + 1 + 0 + 2)));
    }


    @Test
    public void testMacroAvgPrecision_shouldWork() throws Exception {

        target.getLabelStat("BAO").setExpected(4);
        target.getLabelStat("BAO").setObserved(4);

        target.getLabelStat("MIAO").setExpected(4);
        target.getLabelStat("MIAO").setObserved(0);
        target.getLabelStat("MIAO").setFalsePositive(3);
        target.getLabelStat("MIAO").setFalseNegative(1);

        target.getLabelStat("CIAO").setExpected(4);
        target.getLabelStat("CIAO").setObserved(1);
        target.getLabelStat("CIAO").setFalseNegative(3);

        target.getLabelStat("ZIAO").setExpected(4);
        target.getLabelStat("ZIAO").setObserved(0);
        target.getLabelStat("ZIAO").setFalsePositive(2);

        final double precisionBao = target.getLabelStat("BAO").getPrecision();
        final double precisionMiao = target.getLabelStat("MIAO").getPrecision();
        final double precisionCiao = target.getLabelStat("CIAO").getPrecision();
        final double precisionZiao = target.getLabelStat("ZIAO").getPrecision();
        assertThat(target.getMacroAveragePrecision(),
            is((precisionBao + precisionMiao + precisionCiao + precisionZiao) / (4)));
    }


    @Test
    public void testMicroAvgRecall_shouldWork() throws Exception {

        target.getLabelStat("BAO").setExpected(4);
        target.getLabelStat("BAO").setObserved(4);     //TP

        target.getLabelStat("MIAO").setExpected(4);
        target.getLabelStat("MIAO").setObserved(0);
        target.getLabelStat("MIAO").setFalsePositive(3);
        target.getLabelStat("MIAO").setFalseNegative(1);

        target.getLabelStat("CIAO").setExpected(4);
        target.getLabelStat("CIAO").setObserved(1);
        target.getLabelStat("CIAO").setFalseNegative(3);

        target.getLabelStat("ZIAO").setExpected(4);
        target.getLabelStat("ZIAO").setObserved(0);
        target.getLabelStat("ZIAO").setFalsePositive(2);

        assertThat(target.getMicroAverageRecall(), is(((double) 4 + 0 + 1 + 0) / (4 + 4 + 4 + 4)));
    }

    @Test
    public void testMacroAvgRecall_shouldWork() throws Exception {

        target.getLabelStat("BAO").setExpected(4);
        target.getLabelStat("BAO").setObserved(4);

        target.getLabelStat("MIAO").setExpected(4);
        target.getLabelStat("MIAO").setObserved(0);
        target.getLabelStat("MIAO").setFalsePositive(3);
        target.getLabelStat("MIAO").setFalseNegative(1);

        target.getLabelStat("CIAO").setExpected(4);
        target.getLabelStat("CIAO").setObserved(1);
        target.getLabelStat("CIAO").setFalseNegative(3);

        target.getLabelStat("ZIAO").setExpected(4);
        target.getLabelStat("ZIAO").setObserved(0);
        target.getLabelStat("ZIAO").setFalsePositive(2);

        final double recallBao = target.getLabelStat("BAO").getRecall();
        final double recallMiao = target.getLabelStat("MIAO").getRecall();
        final double recallCiao = target.getLabelStat("CIAO").getRecall();
        final double recallZiao = target.getLabelStat("ZIAO").getRecall();
        assertThat(target.getMacroAverageRecall(),
            is((recallBao + recallMiao + recallCiao + recallZiao) / (4)));
    }

    @Test
    public void testMicroAvgF0_shouldWork() throws Exception {

        target.getLabelStat("BAO").setExpected(4);
        target.getLabelStat("BAO").setObserved(4);     //TP

        target.getLabelStat("MIAO").setExpected(4);
        target.getLabelStat("MIAO").setObserved(0);
        target.getLabelStat("MIAO").setFalsePositive(3);
        target.getLabelStat("MIAO").setFalseNegative(1);

        target.getLabelStat("CIAO").setExpected(4);
        target.getLabelStat("CIAO").setObserved(1);
        target.getLabelStat("CIAO").setFalseNegative(3);

        target.getLabelStat("ZIAO").setExpected(4);
        target.getLabelStat("ZIAO").setObserved(0);
        target.getLabelStat("ZIAO").setFalsePositive(2);

        assertThat(target.getMicroAverageF1(),
            is(((double) 2 * target.getMicroAveragePrecision()
                * target.getMicroAverageRecall())
                / (target.getMicroAveragePrecision() + target.getMicroAverageRecall())));
    }

    @Test
    public void testMacroAvgF0_shouldWork() throws Exception {

        target.getLabelStat("BAO").setExpected(4);
        target.getLabelStat("BAO").setObserved(4);

        target.getLabelStat("MIAO").setExpected(4);
        target.getLabelStat("MIAO").setObserved(0);
        target.getLabelStat("MIAO").setFalsePositive(3);
        target.getLabelStat("MIAO").setFalseNegative(1);

        target.getLabelStat("CIAO").setExpected(4);
        target.getLabelStat("CIAO").setObserved(1);
        target.getLabelStat("CIAO").setFalseNegative(3);

        target.getLabelStat("ZIAO").setExpected(4);
        target.getLabelStat("ZIAO").setObserved(0);
        target.getLabelStat("ZIAO").setFalsePositive(2);

        final double f1Bao = target.getLabelStat("BAO").getRecall();
        final double f1Miao = target.getLabelStat("MIAO").getRecall();
        final double f1Ciao = target.getLabelStat("CIAO").getRecall();
        final double f1Ziao = target.getLabelStat("ZIAO").getRecall();

        assertThat(target.getMacroAverageF1(),
            is((f1Bao + f1Miao + f1Ciao + f1Ziao) / (4)));
    }


    @Test
    public void testMicroMacroAveragePrecision() throws Exception {
        final LabelStat conceptual = target.getLabelStat("CONCEPTUAL");
        conceptual.setFalsePositive(1);
        conceptual.setFalseNegative(1);
        conceptual.setObserved(2);
        conceptual.setExpected(3);

        final LabelStat location = target.getLabelStat("LOCATION");
        location.setFalsePositive(0);
        location.setFalseNegative(0);
        location.setObserved(2);
        location.setExpected(2);

        final LabelStat media = target.getLabelStat("MEDIA");
        media.setFalsePositive(0);
        media.setFalseNegative(0);
        media.setObserved(7);
        media.setExpected(7);

        final LabelStat national = target.getLabelStat("NATIONAL");
        national.setFalsePositive(1);
        national.setFalseNegative(0);
        national.setObserved(0);
        national.setExpected(0);

        final LabelStat other = target.getLabelStat("O");
        other.setFalsePositive(0);
        other.setFalseNegative(1);
        other.setObserved(33);
        other.setExpected(34);

        final LabelStat organisation = target.getLabelStat("ORGANISATION");
        organisation.setFalsePositive(0);
        organisation.setFalseNegative(0);
        organisation.setObserved(2);
        organisation.setExpected(2);

        final LabelStat period = target.getLabelStat("PERIOD");
        period.setFalsePositive(0);
        period.setFalseNegative(0);
        period.setObserved(8);
        period.setExpected(8);

        final LabelStat person = target.getLabelStat("PERSON");
        person.setFalsePositive(1);
        person.setFalseNegative(0);
        person.setObserved(0);
        person.setExpected(0);

        final LabelStat personType = target.getLabelStat("PERSON_TYPE");
        personType.setFalsePositive(0);
        personType.setFalseNegative(1);
        personType.setObserved(0);
        personType.setExpected(1);

        for (String label : target.getLabels()) {
            System.out.println(label + " precision --> " + target.getLabelStat(label).getPrecision());
            System.out.println(label + " recall --> " + target.getLabelStat(label).getRecall());
        }

        System.out.println(target.getMacroAveragePrecision());
        System.out.println(target.getMicroAveragePrecision());
    }

}