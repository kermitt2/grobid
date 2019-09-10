package org.grobid.core.utilities;

import com.google.common.collect.Iterables;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.layout.LayoutToken;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class LayoutTokensUtilTest {


    @Test
    public void testSubList() throws Exception {

        String text = "This is a simple text that I'm making up just for fun... or well for the sake of the test!";

        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        layoutTokens.stream().forEach(layoutToken -> layoutToken.setOffset(layoutToken.getOffset() + 1000));

        List<LayoutToken> result = LayoutTokensUtil.subListByOffset(layoutTokens, 1005, 1008);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getText(), is("is"));
        assertThat(result.get(1).getText(), is(" "));

    }

    @Test
    public void testSubList_noEnd() throws Exception {

        String text = "This is a simple text that I'm making up just for fun... or well for the sake of the test!";

        List<LayoutToken> layoutTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        layoutTokens.stream().forEach(layoutToken -> layoutToken.setOffset(layoutToken.getOffset() + 1000));

        List<LayoutToken> result = LayoutTokensUtil.subListByOffset(layoutTokens, 1005);

        assertThat(result, hasSize(43));
        assertThat(result.get(0).getText(), is("is"));
        assertThat(Iterables.getLast(result).getText(), is("!"));

    }
}