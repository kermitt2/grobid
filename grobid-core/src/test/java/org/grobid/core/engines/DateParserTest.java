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
        Date outputDate = target.normalize(inputDate);

        assertThat(outputDate.getYearString(), is(nullValue()));
    }

    @Test
    public void testPostValidation_invalidYear2_onlyString_shouldSetNull() {
        Date inputDate = new Date();
        inputDate.setYearString("10360 10370 10380 10390 10400");
        Date outputDate = target.normalize(inputDate);

        assertThat(outputDate.getYearString(), is(nullValue()));
    }
}