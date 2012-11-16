package org.grobid.core.utilities;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilitiesTest {

	@Test
	public void testStringToBooleanTrue() {
		assertEquals(
				"stringToBoolean value does not match expected result", true,
				Utilities.stringToBoolean("true"));
	}

	@Test
	public void testStringToBooleanTrue2() {
		assertEquals(
				"stringToBoolean value does not match expected result", true,
				Utilities.stringToBoolean(" TruE "));
	}

	@Test
	public void testStringToBooleanFalse() {
		assertEquals(
				"stringToBoolean value does not match expected result", false,
				Utilities.stringToBoolean("false"));
	}

	@Test
	public void testStringToBooleanFalse2() {
		assertEquals(
				"stringToBoolean value does not match expected result", false,
				Utilities.stringToBoolean(" fAlSe "));
	}

	@Test
	public void testStringToBooleanFalse3() {
		assertEquals(
				"stringToBoolean value does not match expected result", false,
				Utilities.stringToBoolean(" non boolean value"));
	}

	@Test
	public void testStringToBooleanBlank() {
		assertEquals(
				"stringToBoolean value does not match expected result", false,
				Utilities.stringToBoolean(""));
	}

	@Test
	public void testStringToBooleanBlank2() {
		assertEquals(
				"stringToBoolean value does not match expected result", false,
				Utilities.stringToBoolean(null));
	}

	@Test
	public void testwriteInFileANDreadFile() throws IOException {
		File file = File.createTempFile("temp", "test");
		Utilities.writeInFile(file.getAbsolutePath(), getString());
		assertEquals("Not expected value", getString(), Utilities.readFile(file.getAbsolutePath()));
	}

	private static String getString() {
		return "1 \" ' A \n \t \r test\n\\n \n M";
	}
}
