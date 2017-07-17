package org.grobid.core.document;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 *  @author Patrice Lopez
 */
public class TEIFormatterTest {

	@Test
	public void testBracketReference_noBracket() throws Exception {
		String inputText = "I'm thinking of something.";
		String processedText = TEIFormatter.bracketReferenceSegment(inputText);

		assertThat(processedText, is(nullValue()));
	}

	@Test
	public void testBracketReference_openBracket() throws Exception {
		String inputText = "I'm thinking of (something.";
		String processedText = TEIFormatter.bracketReferenceSegment(inputText);

		assertThat(processedText, is(nullValue()));
	}

	@Test
	public void testBracketReference_closingBracket() throws Exception {
		String inputText = "I'm thinking of something).";
		String processedText = TEIFormatter.bracketReferenceSegment(inputText);

		assertThat(processedText, is(nullValue()));
	}

	@Test
	public void testBracketReference_openAndClosedBracket() throws Exception {
		String inputText = "I'm thinking of (something).";
		String processedText = TEIFormatter.bracketReferenceSegment(inputText);

		assertThat(processedText, is(notNullValue()));
	}

    @Test
    public void testRegex() {
		Pattern localNumberRefCompact =
		    		//Pattern.compile("(\\[|\\()((\\d+\\w?)(\\-(\\d+\\w?))?(,|;)?\\s?)+(\\)|\\])?");
			Pattern.compile("(\\[|\\()((\\d)+(\\w)?(\\-\\d+\\w?)?,\\s?)+(\\d+\\w?)(\\-\\d+\\w?)?(\\)|\\])");
		System.out.println(localNumberRefCompact.pattern());
		String text = "All selected (2000, 2100, 2900, 2902, 2908, 2909, 4300, 8200, 8900, 8902, 9720, 9730, 9780, 9790, 9900,17100,17202,17230,17231,17232, 17239,17290,17292,17293,17320,17330, 17360,17390,17901, 80000";
		text = TEIFormatter.bracketReferenceSegment(text);
		if (text != null) {
			Matcher m = localNumberRefCompact.matcher(text);
			m.find();
		}
		System.out.println("Done");
    }

}