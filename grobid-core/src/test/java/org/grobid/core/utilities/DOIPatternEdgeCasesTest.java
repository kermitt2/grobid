package org.grobid.core.utilities;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.Lexicon;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class DOIPatternEdgeCasesTest {

    private Lexicon lexicon;

    @Before
    public void setUp() {
        lexicon = Lexicon.getInstance();
    }

    private String extractByTokenOffsets(List<LayoutToken> tokens, OffsetPosition pos) {
        // positions from tokenPositionsDOIPattern are token-index based and inclusive for end
        List<LayoutToken> sub = tokens.subList(pos.start, pos.end + 1);
        return LayoutTokensUtil.toText(sub).trim();
    }

    @Test
    public void testDOIQuotedDoubleStraight() {
        String piece = "\"10.1000/xyz123\" and text";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = lexicon.tokenPositionsDOIPattern(tokens, text);
        assertThat(positions, hasSize(1));
        String doi = extractByTokenOffsets(tokens, positions.get(0));
        assertThat(doi, is("10.1000/xyz123"));
    }

    @Test
    public void testDOIQuotedSmart() {
        String piece = "“10.1000/xyz-ABC” more";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = lexicon.tokenPositionsDOIPattern(tokens, text);
        assertThat(positions, hasSize(1));
        String doi = extractByTokenOffsets(tokens, positions.get(0));
        assertThat(doi, is("10.1000/xyz-ABC"));
    }

    @Test
    public void testDOIWithTrailingPeriod() {
        String piece = "See 10.5555/abc.def.";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = lexicon.tokenPositionsDOIPattern(tokens, text);
        assertThat(positions, hasSize(1));
        String doi = extractByTokenOffsets(tokens, positions.get(0));
        assertThat(doi, is("10.5555/abc.def"));
    }

    @Test
    public void testDOIWithinParenthesesAndPeriod() {
        String piece = "(10.12345/ABC_123).";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = lexicon.tokenPositionsDOIPattern(tokens, text);
        assertThat(positions, hasSize(1));
        String doi = extractByTokenOffsets(tokens, positions.get(0));
        assertThat(doi, is("10.12345/ABC_123"));
    }

    @Test
    public void testDOIPrefixWithColon() {
        String piece = "doi:10.1000/test-1";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = lexicon.tokenPositionsDOIPattern(tokens, text);
        assertThat(positions, hasSize(1));
        String doi = extractByTokenOffsets(tokens, positions.get(0));
        assertThat(doi, is("10.1000/test-1"));
    }

    @Test
    public void testDOIFromDoiOrgURL() {
        String piece = "https://doi.org/10.1109/TSE.2019.1234567, referenced";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = lexicon.tokenPositionsDOIPattern(tokens, text);
        assertThat(positions, hasSize(1));
        String doi = extractByTokenOffsets(tokens, positions.get(0));
        assertThat(doi, is("10.1109/TSE.2019.1234567"));
    }

    @Test
    public void testDOIWithTrailingCommaAndQuote() {
        String piece = "10.1000/xyz-1’, another"; // right single quote
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = lexicon.tokenPositionsDOIPattern(tokens, text);
        assertThat(positions, hasSize(1));
        String doi = extractByTokenOffsets(tokens, positions.get(0));
        assertThat(doi, is("10.1000/xyz-1"));
    }
}

