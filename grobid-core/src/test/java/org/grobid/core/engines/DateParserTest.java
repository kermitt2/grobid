package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Date;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DateParserTest {

    private DateParser target;

    @Before
    public void setUp() throws Exception {
        target = new DateParser(GrobidModels.DUMMY);
    }

    @Test
    public void testPostValidation_invalidYear_onlyString_shouldSetNull() {
        Date inputDate = new Date();
        inputDate.setYearString("10360 10370");
        Date outputDate = DateParser.cleaning(inputDate);

        assertThat(outputDate.getYearString(), is(nullValue()));
    }

    @Test
    public void testPostValidation_invalidYear2_onlyString_shouldSetNull() {
        Date inputDate = new Date();
        inputDate.setYearString("10360 10370 10380 10390 10400");
        Date outputDate = DateParser.cleaning(inputDate);

        assertThat(outputDate.getYearString(), is(nullValue()));
    }

    @Test
    public void testPostValidation_invalidYear_bothStringAndInt_shouldSetNull() {
        Date inputDate = new Date();
        inputDate.setYear(1036010370);
        inputDate.setYearString("10360 10370");
        Date outputDate = DateParser.cleaning(inputDate);

        assertThat(outputDate.getYearString(), is(nullValue()));
        assertThat(outputDate.getYear(), is(-1));
    }

    @Test
    public void testPostValidation_invalidMonth_bothStringAndInt_shouldSetNull() {
        Date inputDate = new Date();
        inputDate.setMonth(1234);
        inputDate.setMonthString("1234");
        Date outputDate = DateParser.cleaning(inputDate);

        assertThat(outputDate.getMonthString(), is(nullValue()));
        assertThat(outputDate.getMonth(), is(-1));
    }

    @Test
    public void testPostValidation_invalidDay_bothStringAndInt_shouldSetNull() {
        Date inputDate = new Date();
        inputDate.setDay(12345);
        inputDate.setDayString("12345");
        Date outputDate = DateParser.cleaning(inputDate);

        assertThat(outputDate.getDayString(), is(nullValue()));
        assertThat(outputDate.getDay(), is(-1));
    }
    
    @Test
    public void testNormalize_yearContainsWholeDate_shouldReconstructCorrectly() {
        Date inputDate = new Date();
        inputDate.setYear(20021212);
        Date outputDate = target.normalizeAndClean(inputDate);
        
        assertThat(outputDate.getMonth(), is(12));
        assertThat(outputDate.getDay(), is(12));
        assertThat(outputDate.getYear(), is(2002));
    }

    @Test
    public void testNormalize_dayContainsWholeDate_shouldReturnEmptyDate() {
        Date inputDate = new Date();
        inputDate.setDay(20021212);
        Date outputDate = target.normalizeAndClean(inputDate);

        assertThat(outputDate.getMonth(), is(-1));
        assertThat(outputDate.getDay(), is(-1));
        assertThat(outputDate.getYear(), is(-1));
    }

    @Test
    public void testNormalize_monthContainsWholeDate_shouldReturnEmptyDate() {
        Date inputDate = new Date();
        inputDate.setMonth(20021212);
        Date outputDate = target.normalizeAndClean(inputDate);

        assertThat(outputDate.getMonth(), is(-1));
        assertThat(outputDate.getDay(), is(-1));
        assertThat(outputDate.getYear(), is(-1));
    }

    @Test
    public void testNormalize_yearOnly_validValue_shouldParseYearCorrectly() {
        Date inputDate = new Date();
        inputDate.setYearString("2002");
        Date outputDate = target.normalizeAndClean(inputDate);

        assertThat(outputDate.getMonth(), is(-1));
        assertThat(outputDate.getDay(), is(-1));
        assertThat(outputDate.getYear(), is(2002));
        assertThat(outputDate.getYearString(), is("2002"));
    }

    @Test
    public void testNormalize_wholeDate_invalidYearValue_shouldRemoveValue() {
        Date inputDate = new Date();
        inputDate.setDayString("12");
        inputDate.setMonthString("12");
        inputDate.setYearString("2222222012");
        Date outputDate = target.normalizeAndClean(inputDate);

        assertThat(outputDate.getDay(), is(12));
        assertThat(outputDate.getDayString(), is("12"));
        assertThat(outputDate.getMonth(), is(12));
        assertThat(outputDate.getMonthString(), is("12"));
        assertThat(outputDate.getYear(), is(-1));
        assertThat(outputDate.getYearString(), is(nullValue()));
    }

    @Test
    public void testNormalize_monthOnly_validValue_shouldParseMonthCorrectly() {
        Date inputDate = new Date();
        inputDate.setMonthString("12");
        Date outputDate = target.normalizeAndClean(inputDate);

        assertThat(outputDate.getMonth(), is(12));
        assertThat(outputDate.getDay(), is(-1));
        assertThat(outputDate.getYear(), is(-1));
        assertThat(outputDate.getMonthString(), is("12"));
    }

    @Test
    public void testNormalize_wholeDate_invalidMonthValue_shouldRemoveValue() {
        Date inputDate = new Date();
        inputDate.setDayString("12");
        inputDate.setMonthString("1222222222");
        inputDate.setYearString("2012");
        Date outputDate = target.normalizeAndClean(inputDate);

        assertThat(outputDate.getMonth(), is(-1));
        assertThat(outputDate.getMonthString(), is(nullValue()));
        assertThat(outputDate.getDay(), is(12));
        assertThat(outputDate.getDayString(), is("12"));
        assertThat(outputDate.getYear(), is(2012));
        assertThat(outputDate.getYearString(), is("2012"));
    }

    @Test
    public void testNormalize_dayOnly_validValue_shouldParseDayCorrectly() {
        Date inputDate = new Date();
        inputDate.setDayString("12");
        Date outputDate = target.normalizeAndClean(inputDate);

        assertThat(outputDate.getMonth(), is(-1));
        assertThat(outputDate.getDay(), is(12));
        assertThat(outputDate.getYear(), is(-1));
        assertThat(outputDate.getDayString(), is("12"));
    }

    @Test
    public void testNormalize_wholeDate_invalidDayValue_shouldRemoveValue() {
        Date inputDate = new Date();
        inputDate.setDayString("1221");
        inputDate.setMonthString("12");
        inputDate.setYearString("2012");
        Date outputDate = target.normalizeAndClean(inputDate);

        assertThat(outputDate.getDay(), is(-1));
        assertThat(outputDate.getDayString(), is(nullValue()));
        assertThat(outputDate.getMonth(), is(12));
        assertThat(outputDate.getMonthString(), is("12"));
        assertThat(outputDate.getYear(), is(2012));
        assertThat(outputDate.getYearString(), is("2012"));
    }
}