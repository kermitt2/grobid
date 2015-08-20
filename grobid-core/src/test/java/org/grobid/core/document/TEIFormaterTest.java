package org.grobid.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.grobid.core.document.TEIFormater;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *  @author Patrice Lopez
 */
public class TEIFormaterTest {

    @Test
    public void testRegex() {
		Pattern localNumberRefCompact =
		    		//Pattern.compile("(\\[|\\()((\\d+\\w?)(\\-(\\d+\\w?))?(,|;)?\\s?)+(\\)|\\])?");
			Pattern.compile("(\\[|\\()((\\d)+(\\w)?(\\-\\d+\\w?)?,\\s?)+(\\d+\\w?)(\\-\\d+\\w?)?(\\)|\\])");
		System.out.println(localNumberRefCompact.pattern());
		String text = "All selected (2000, 2100, 2900, 2902, 2908, 2909, 4300, 8200, 8900, 8902, 9720, 9730, 9780, 9790, 9900,17100,17202,17230,17231,17232, 17239,17290,17292,17293,17320,17330, 17360,17390,17901, 80000";
		text = TEIFormater.bracketReferenceSegment(text);
		if (text != null) {
			Matcher m = localNumberRefCompact.matcher(text);
			m.find();
		}
		System.out.println("Done");
    }

}