package org.grobid.core.utilities;

import java.io.File;
import java.io.IOException;

import java.util.List;

import org.junit.Test;
import org.grobid.core.data.Person;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;

public class TextUtilitiesTest {

	@Test
	public void testHTMLEncode() throws IOException {
		String test1 = "Dé&amps, C & Bidule, D.;";
		String test2 = "Dé&amps, C &";
		String result1 = TextUtilities.HTMLEncode(test1);
		String result2 = TextUtilities.HTMLEncode(test2);
		assertEquals("Not expected value", "Dé&amp;amps, C &amp; Bidule, D.;", result1);
		assertEquals("Not expected value", "Dé&amp;amps, C &amp;", result2);
	}

	@Test
	public void testDephynization() {
//		da("Test", "Test");
//		da("Testing", "Compa- \nrison");
		//da(" Test", " Test");
	}

	public void da(String e, String s) {
		String dehyphenize = TextUtilities.dehyphenize(s);
		assertEquals("'" + e + "' vs. '" + dehyphenize + "'", e, dehyphenize);
	}



}
