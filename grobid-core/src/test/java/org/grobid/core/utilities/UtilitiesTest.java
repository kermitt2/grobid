package org.grobid.core.utilities;

import org.junit.Test;
import org.junit.Assert;

public class UtilitiesTest {
	
	@Test
	public void testStringToBooleanTrue(){
		Assert.assertEquals("stringToBoolean value does not match expected result", true,Utilities.stringToBoolean("true"));
	}
	
	@Test
	public void testStringToBooleanTrue2(){
		Assert.assertEquals("stringToBoolean value does not match expected result", true,Utilities.stringToBoolean(" TruE "));
	}

	@Test
	public void testStringToBooleanFalse(){
		Assert.assertEquals("stringToBoolean value does not match expected result", false,Utilities.stringToBoolean("false"));
	}
	
	@Test
	public void testStringToBooleanFalse2(){
		Assert.assertEquals("stringToBoolean value does not match expected result", false,Utilities.stringToBoolean(" fAlSe "));
	}
	
	@Test
	public void testStringToBooleanFalse3(){
		Assert.assertEquals("stringToBoolean value does not match expected result", false,Utilities.stringToBoolean(" non boolean value"));
	}
	
	@Test
	public void testStringToBooleanBlank(){
		Assert.assertEquals("stringToBoolean value does not match expected result", false,Utilities.stringToBoolean(""));
	}
	
	@Test
	public void testStringToBooleanBlank2(){
		Assert.assertEquals("stringToBoolean value does not match expected result", false,Utilities.stringToBoolean(null));
	}
}
