package org.grobid.core.analyzers;

import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lang.Language;
import org.grobid.core.utilities.UnicodeUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class GrobidAnalyzerTest {
    GrobidAnalyzer target;

    @Before
    public void setUp() throws Exception {
        target = GrobidAnalyzer.getInstance();
    }

    @Test
    public void testTokenizeWithLayoutToken() {
        final List<LayoutToken> layoutTokens = target.tokenizeWithLayoutToken("This is a normal \ntext,\n\n\n on several lines.\n");

        assertThat(layoutTokens, hasSize(22));
        assertThat(layoutTokens.get(0).getText(), is("This"));
        assertThat(layoutTokens.get(1).getText(), is(" "));
        assertThat(layoutTokens.get(6).getText(), is("normal"));
        assertThat(layoutTokens.get(7).getText(), is(" "));
        assertThat(layoutTokens.get(7).isNewLineAfter(), is(true));
        assertThat(layoutTokens.get(8).getText(), is("\n"));
        assertThat(layoutTokens.get(8).isNewLineAfter(), is(false));
        assertThat(layoutTokens.get(10).getText(), is(","));
        assertThat(layoutTokens.get(10).isNewLineAfter(), is(true));
        assertThat(layoutTokens.get(11).getText(), is("\n"));
        assertThat(layoutTokens.get(11).isNewLineAfter(), is(true));
        assertThat(layoutTokens.get(12).getText(), is("\n"));
        assertThat(layoutTokens.get(12).isNewLineAfter(), is(true));
        assertThat(layoutTokens.get(13).getText(), is("\n"));
        assertThat(layoutTokens.get(13).isNewLineAfter(), is(false));
    }

    @Test
    public void testTokenizeWithLayoutToken_emptyText() {
        assertThat(target.tokenizeWithLayoutToken(""), hasSize(0));
    }

    @Test
    public void testTokenize_Korean() {
        String input = "최지수. 윤석민 (2019), 가짜뉴스 거버넌스: 정부규제, 자율규제, 공동규제 모형에 대한 비교를 중심으로, 사이버커뮤니케이션학보, 제36권 제1호, 127-180쪽.";
        input = UnicodeUtil.normaliseText(input);
        System.out.println("--> testTokenize_Korean: " + input);
        List<String> tokens = target.tokenize(input, new Language("kr"));
        System.out.println(tokens.toString());
        assertThat(tokens, hasSize(32));
    }

    @Test
    public void testTokenizeWithLayoutToken_Korean() {
        String input = "최지수. 윤석민 (2019), 가짜뉴스 거버넌스: 정부규제, 자율규제, 공동규제 모형에 대한 비교를 중심으로, 사이버커뮤니케이션학보, 제36권 제1호, 127-180쪽.";
        List<LayoutToken> tokens = target.tokenizeWithLayoutToken(input, new Language("kr"));
        System.out.println("testTokenizeWithLayoutToken_Korean:" + tokens.size());
        tokens = target.tokenizeWithLayoutToken(input, new Language("kr"));
        System.out.println("testTokenizeWithLayoutToken_Korean:" + tokens.size());
        tokens = target.tokenizeWithLayoutToken(input, new Language("kr"));
        System.out.println("testTokenizeWithLayoutToken_Korean:" + tokens.size());
        assertThat(tokens, hasSize(32));
    }

    @Test
    public void testRetokenizeSubdigits_Korean() {
        String input = "최지수. 윤석민 (2019), 가짜뉴스 거버넌스: 정부규제, 자율규제, 공동규제 모형에 대한 비교를 중심으로, 사이버커뮤니케이션학보, 제36권 제1호, 127-180쪽.";
        List<String> tokens = target.tokenize(input, new Language("kr"));
        assertThat(target.retokenizeSubdigits(tokens), hasSize(35));
    }

    @Test
    public void testRetokenizeSubdigitsWithLayoutToken_Korean() {
        String input = "최지수. 윤석민 (2019), 가짜뉴스 거버넌스: 정부규제, 자율규제, 공동규제 모형에 대한 비교를 중심으로, 사이버커뮤니케이션학보, 제36권 제1호, 127-180쪽.";
        List<String> tokens = target.tokenize(input, new Language("kr"));
        assertThat(target.retokenizeSubdigitsWithLayoutToken(tokens), hasSize(35));
    }

    @Test
    public void testRetokenizeSubdigitsFromLayoutToken_Korean() {
        String input = "최지수. 윤석민 (2019), 가짜뉴스 거버넌스: 정부규제, 자율규제, 공동규제 모형에 대한 비교를 중심으로, 사이버커뮤니케이션학보, 제36권 제1호, 127-180쪽.";
        List<LayoutToken> tokens = target.tokenizeWithLayoutToken(input, new Language("kr"));
        System.out.println("testRetokenizeSubdigitsFromLayoutToken_Korean:" + tokens.size());

        assertThat(target.retokenizeSubdigitsFromLayoutToken(tokens), hasSize(35));
    }

}