package org.grobid.core.utilities.counters.impl;

import org.grobid.core.data.Figure;
import org.grobid.core.engines.TaggingLabel;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 25/11/16.
 */
public class CntManagerImplTest {

    CntManagerImpl target;

    @Before
    public void setUp() throws Exception {
        target = new CntManagerImpl();
    }

    @Test
    public void testCountSingleGroup() throws Exception {
        target.i(TaggingLabel.ITEM);

        assertThat(target.getCounter(TaggingLabel.ITEM).cnt(), is(1l));
    }

    @Test
    public void testGetAllCounters() throws Exception {
        target.i(TaggingLabel.ITEM);
        target.i(TaggingLabel.OTHER);

        final Map<String, Map<String, Long>> allCounters = target.getAllCounters();
        assertThat(allCounters.size(), is(1));
        final Map<String, Long> stringLongMap = allCounters.get("org.grobid.core.engines.TaggingLabel");
        assertThat(stringLongMap.size(), is(45));

        assertThat(stringLongMap.get("OTHER"), is(1l));
        assertThat(stringLongMap.get("ITEM"), is(1l));
    }

    @Test
    public void testGetAllCounters2() throws Exception {
        target.i(TaggingLabel.ITEM);
        target.i(Figure.Counters.SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS);

        final Map<String, Map<String, Long>> allCounters = target.getAllCounters();
        assertThat(allCounters.size(), is(2));

        final Map<String, Long> taggingLabelMap = allCounters.get("org.grobid.core.engines.TaggingLabel");
        assertThat(taggingLabelMap.size(), is(45));

        final Map<String, Long> countersMap = allCounters.get("org.grobid.core.data.Figure$Counters");
        assertThat(countersMap.size(), is(2));
    }

    @Test
    public void testCnt_withEnum() throws Exception {
        assertThat(target.cnt(TaggingLabel.ITEM), is(0l));
        target.i(TaggingLabel.ITEM);
        assertThat(target.cnt(TaggingLabel.ITEM), is(1l));
        assertThat(target.cnt(Figure.Counters.SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS), is(0l));
        target.i(Figure.Counters.SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS);
        assertThat(target.cnt(Figure.Counters.SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS), is(1l));

    }

    @Test
    public void testCnt_withExplicitValues() throws Exception {
        assertThat(target.cnt("figures", "element"), is(0l));
        target.i("figures", "element");
        assertThat(target.cnt("figures", "element"), is(1l));

        assertThat(target.cnt("figures", "item"), is(0l));
        target.i("figures", "item");
        assertThat(target.cnt("figures", "item"), is(1l));

        assertThat(target.cnt("tables", "item"), is(0l));
        target.i("tables", "item");
        assertThat(target.cnt("tables", "item"), is(1l));

        target.i("figures", "item");
        assertThat(target.cnt("figures", "item"), is(2l));
    }

    @Test
    public void getCounter_shouldWork() throws Exception {
        target.i("figures", "element", 2);
        assertThat(target.getCounter("figures", "element").cnt(), is(2l));

        target.i(TaggingLabel.ASTRO_OBJECT, 20);
        assertThat(target.getCounter(TaggingLabel.ASTRO_OBJECT).cnt(), is(20l));
    }

    @Test
    public void getCounters_shouldWork() throws Exception {
        target.i("figures", "element", 2);
        target.i("table", "john", 2);
        target.i("table", "miao", 2);
        assertThat(target.getCounters("figures").size(), is(1));

        target.i(TaggingLabel.ASTRO_OBJECT, 20);
        assertThat(target.getCounters("table").size(), is(2));

        final String[] tables = target.getCounters("table").keySet().toArray(new String[0]);
        Arrays.sort(tables);
        assertThat(tables, is(new String[]{"john", "miao"}));
    }

}