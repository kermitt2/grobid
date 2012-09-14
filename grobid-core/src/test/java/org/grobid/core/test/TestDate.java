package org.grobid.core.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.grobid.core.data.Date;
import org.junit.Before;
import org.junit.Test;

/**
 *  @author Patrice Lopez
 */
public class TestDate extends EngineTest {

    @Before
	public void setUp() {
	}
	
	@Test
	public void testDateCompare() {
		Date date1 = new Date();
		date1.setYear(2000);
		date1.setMonth(10);
		date1.setDay(2);
		
		Date date2 = new Date();
		date2.setYear(2000);
		date2.setMonth(10);
		
		int actual = date1.compareTo(date2);
		
		assertEquals("Problem with date comparison", actual, -1);
		
		date1 = new Date();
		date1.setYear(2007);
		
		date2 = new Date();
		date2.setYear(2007);
		date2.setMonth(9);
		
		actual = date1.compareTo(date2);
		
		assertEquals("Problem with date comparison", actual, 1);
	}

	@Test
	public void testDateParser() throws Exception {
		String dateSequence1 = "10 January 2001";
		List<Date> res = engine.processDate(dateSequence1);	
		assertNotNull(res);
		assertEquals(1, res.size());	 
		if (res.size() > 0) {
			Date date = res.get(0);
			System.out.println(date.toTEI());
			
			assertThat(date.getDayString(), is("10"));
			assertThat(date.getMonthString(), is("January"));
			assertThat(date.getYearString(), is("2001"));
			
			assertThat(date.getDay(), is(10));
			assertThat(date.getMonth(), is(1));
			assertThat(date.getYear(), is(2001));
		}
		
		String dateSequence2 = "19 November 1 999";		
		res = engine.processDate(dateSequence2);	
		assertNotNull(res);
		assertEquals(1, res.size());	 
		if (res.size() > 0) {
			Date date = res.get(0);
			System.out.println(date.toTEI());
			
			assertThat(date.getDayString(), is("19"));
			assertThat(date.getMonthString(), is("November"));
			assertThat(date.getYearString(), is("1 999"));
			
			assertThat(date.getDay(), is(19));
			assertThat(date.getMonth(), is(11));
			assertThat(date.getYear(), is(1999));
		}
		
		String dateSequence3 = "15-08-2007";		
		res = engine.processDate(dateSequence3);	
		assertNotNull(res);
		assertEquals(1, res.size());	 
		if (res.size() > 0) {
			Date date = res.get(0);
			System.out.println(date.toTEI());
			
			assertThat(date.getDayString(), is("15"));
			assertThat(date.getMonthString(), is("08"));
			assertThat(date.getYearString(), is("2007"));
			
			assertThat(date.getDay(), is(15));
			assertThat(date.getMonth(), is(8));
			assertThat(date.getYear(), is(2007));
		}
		
		String dateSequence4 = "November 14 1999";
		res = engine.processDate(dateSequence4);	
		assertNotNull(res);
		//assertEquals(1, res.size());
		if (res.size() > 0) {
			Date date = res.get(0);
			System.out.println(date.toTEI());
			
			assertThat(date.getDayString(), is("14"));
			assertThat(date.getMonthString(), is("November"));
			assertThat(date.getYearString(), is("1999"));
			
			assertThat(date.getDay(), is(14));
			assertThat(date.getMonth(), is(11));
			assertThat(date.getYear(), is(1999));
		}
	}

}