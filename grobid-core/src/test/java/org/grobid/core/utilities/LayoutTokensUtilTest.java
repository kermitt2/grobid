package org.grobid.core.utilities;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.layout.LayoutToken;
import org.junit.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LayoutTokensUtilTest {

    /**
     * We fake the new line in the layout token coordinates
     */
    @Test
    public void testDoesRequireDehyphenization_shouldReturnTrue() throws Exception {
        String input = "The study of iron-based supercondu- \n" +
            "ctors superconductivity in the iron-pnictide LaFeAsO 1-x F x has been expanding and has \n";

        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        assertThat(LayoutTokensUtil.doesRequireDehypenisation(layoutTokens, 11), is(true));
    }

    @Test
    public void testDoesRequireDehyphenization2_shouldReturnTrue() throws Exception {
        String input = "The study of iron-based supercondu - \n" +
            "ctors superconductivity in the iron-pnictide LaFeAsO 1-x F x has been expanding and has \n";

        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        assertThat(LayoutTokensUtil.doesRequireDehypenisation(layoutTokens, 12), is(true));
    }

    @Test
    public void testDoesRequireDehyphenization_composedWords_shouldReturnFalse() throws Exception {
        String input = "The study of iron-based supercondu - \n" +
            "ctors superconductivity in the iron-pnictide LaFeAsO 1-x F x has been expanding and has \n";

        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        assertThat(LayoutTokensUtil.doesRequireDehypenisation(layoutTokens, 7), is(false));
        assertThat(LayoutTokensUtil.doesRequireDehypenisation(layoutTokens, 24), is(false));
    }

    @Test
    public void testDoesRequireDehyphenization2_composedWords_shouldReturnFalse() throws Exception {
        String input = "The study of iron- based supercondu - \n" +
            "ctors superconductivity in the iron-pnictide LaFeAsO 1-x F x has been expanding and has \n";

        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        assertThat(LayoutTokensUtil.doesRequireDehypenisation(layoutTokens, 7), is(false));
    }

    @Test
    public void testDoesRequireDehyphenization3_composedWords_shouldReturnFalse() throws Exception {
        String input = "The study of iron - based supercondu - \n" +
            "ctors superconductivity in the iron-pnictide LaFeAsO 1-x F x has been expanding and has \n";

        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        assertThat(LayoutTokensUtil.doesRequireDehypenisation(layoutTokens, 8), is(false));
    }

    @Test
    public void testDoesRequireDehyphenization_usingCoordinates_shouldReturnTrue() throws Exception {
        String input = "The study of iron-based supercondu -  " +
            "ctors superconductivity in the iron-pnictide LaFeAsO 1-x F x has been expanding and has \n";

        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        IntStream.range(0, 15).forEach(i -> layoutTokens.get(i).setY(10));
        IntStream.range(15, layoutTokens.size()).forEach(i -> layoutTokens.get(i).setY(30));

        assertThat(LayoutTokensUtil.doesRequireDehypenisation(layoutTokens, 12), is(true));
    }

//    @Test
//    public void testDoesRequireDehyphenization_withoutNewLine() throws Exception {
//        String input = "The study of iron-based supercondu -  " +
//            "ctors superconductivity in the iron-pnictide LaFeAsO 1-x F x has been expanding and has \n";
//
//        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
//
//        IntStream.range(0, 15).forEach(i -> layoutTokens.get(i).setY(10));
//        IntStream.range(15, layoutTokens.size()).forEach(i -> layoutTokens.get(i).setY(30));
//
//        assertThat(LayoutTokensUtil.doesRequireDehypenisation(layoutTokens, 12), is(true));
//    }


    @Test
    public void testDoesRequireDehyphenization_hypenAtEndOfString_shouldReturnFalse() throws Exception {
        String input = "The study of iron-based supercondu-";

        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        assertThat(LayoutTokensUtil.doesRequireDehypenisation(layoutTokens, 11), is(false));
    }

}