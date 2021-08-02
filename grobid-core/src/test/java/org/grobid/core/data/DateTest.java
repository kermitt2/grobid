package org.grobid.core.data;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;

public class DateTest {
    Date target;
    Date other;

    @Test
    public void testDateMerging1() {
        // "2010" "2010-10" -> "2010-10"

        target = new Date();
        target.setYear(2010);

        other = new Date();
        other.setYear(2010);
        other.setMonth(10);

        Date merged = Date.merge(target, other);

        assertThat(merged.getYear(), is(2010));
        assertThat(merged.getMonth(), is(10));
    }

    @Test
    public void testDateMerging2() {
        // "2010" "2010-10-27" -> "2010-10-27"

        target = new Date();
        target.setYear(2010);

        other = new Date();
        other.setYear(2010);
        other.setMonth(10);
        other.setDay(27);

        Date merged = Date.merge(target, other);

        assertThat(merged.getYear(), is(2010));
        assertThat(merged.getMonth(), is(10));
        assertThat(merged.getDay(), is(27));
    }

    @Test
    public void testDateMerging3() {
        // "2010-10" "2010-10-27" -> "2010-10-27"

        target = new Date();
        target.setYear(2010);
        target.setMonth(10);

        other = new Date();
        other.setYear(2010);
        other.setMonth(10);
        other.setDay(27);

        Date merged = Date.merge(target, other);

        assertThat(merged.getYear(), is(2010));
        assertThat(merged.getMonth(), is(10));
        assertThat(merged.getDay(), is(27));
    }

    @Test
    public void testDateMerging32() {
        // "2010-10-27" "2010-10" -> "2010-10-27"

        target = new Date();
        target.setYear(2010);
        target.setMonth(10);
        target.setDay(27);

        other = new Date();
        other.setYear(2010);
        other.setMonth(10);

        Date merged = Date.merge(target, other);

        assertThat(merged.getYear(), is(2010));
        assertThat(merged.getMonth(), is(10));
        assertThat(merged.getDay(), is(27));
    }

    @Test
    public void testDateMerging4() {
        // "2011-10" "2010-10-27" -> "2011-10"

        target = new Date();
        target.setYear(2011);
        target.setMonth(10);

        other = new Date();
        other.setYear(2010);
        other.setMonth(10);
        other.setDay(27);

        Date merged = Date.merge(target, other);

        assertThat(merged.getYear(), is(2011));
        assertThat(merged.getMonth(), is(10));
    }    

    @Test
    public void testDateMerging5() {
        // "2010" "2016-10-27" -> "2010"

        target = new Date();
        target.setYear(2010);

        other = new Date();
        other.setYear(2016);
        other.setMonth(10);
        other.setDay(27);

        Date merged = Date.merge(target, other);

        assertThat(merged.getYear(), is(2010));
    }

    @Test
    public void testDateMerging6() {
        // "2011" "2010" -> 2011

        target = new Date();
        target.setYear(2011);

        other = new Date();
        other.setYear(2010);

        Date merged = Date.merge(target, other);

        assertThat(merged.getYear(), is(2011));
    }

}
