package org.grobid.core.document;

import nu.xom.Element;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class TEIFormatterTest {

    @BeforeClass
    public static void setInitialContext() throws Exception {
        GrobidProperties.getInstance();
    }

    @Test
    public void testSegmentIntoSentences_simpleText_ShouldSplitIntoSentencesAndAddSTag() throws Exception {
        String text = "One sentence. Second sentence.";

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List< LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        Element currentParagraph = XmlBuilderUtils.teiElement("p");
        currentParagraph.appendChild(text);

        new TEIFormatter(null, null)
            .segmentIntoSentences(currentParagraph, currentParagraphTokens, config, "en");

        assertThat(currentParagraph.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\"><s>One sentence.</s><s>Second sentence.</s></p>"));
        assertThat(currentParagraph.getChildElements().size(), is(2));
    }

    @Test
    public void testSegmentIntoSentences_Bold_ShouldSplitIntoSentencesAndAddSTag() throws Exception {
        String text = "One sentence. Second sentence.";

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List< LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        currentParagraphTokens.get(0).setBold(true);
        currentParagraphTokens.get(2).setBold(true);
        currentParagraphTokens.get(2).setItalic(true);
        Element currentParagraph = XmlBuilderUtils.teiElement("p");
        currentParagraph.appendChild(text);

        new TEIFormatter(null, null)
            .segmentIntoSentences(currentParagraph, currentParagraphTokens, config, "en");

        assertThat(currentParagraph.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\"><s>One sentence.</s><s>Second sentence.</s></p>"));
        assertThat(currentParagraph.getChildElements().size(), is(2));
    }

    @Test
    public void testExtractStylesList_1_shouldWork() throws Exception {
        String text = "The room temperature magnetic hysteresis loop for melt-spun ribbons of pure Nd 2 Fe 14 B is shown in Figure ";
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List< LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        currentParagraphTokens.get(26).setSubscript(true);
        currentParagraphTokens.get(30).setSubscript(true);

        List<Triple<String, String, OffsetPosition>> pairs = new TEIFormatter(null, null).extractStylesList(currentParagraphTokens);

        assertThat(pairs, hasSize(2));
        assertThat(pairs.get(0).getLeft(), is("subscript"));
        assertThat(pairs.get(0).getMiddle(), is("2"));
        assertThat(pairs.get(0).getRight().start, is(79));
        assertThat(pairs.get(0).getRight().end, is(80));

        assertThat(pairs.get(1).getLeft(), is("subscript"));
        assertThat(pairs.get(1).getMiddle(), is("14"));
        assertThat(pairs.get(1).getRight().start, is(84));
        assertThat(pairs.get(1).getRight().end, is(86));


    }

    @Test
    public void testGetSectionNumber_simple_ShouldWork() throws Exception {
        String text = "3 Supercon 2";
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List< LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        currentParagraphTokens.get(4).setSubscript(true);
        Pair<List<LayoutToken>, String> sectionNumber = new TEIFormatter(null, null)
            .getSectionNumber(currentParagraphTokens);

        String output = LayoutTokensUtil.toText(sectionNumber.getLeft());
        assertThat(output, is("Supercon 2"));
        assertThat(sectionNumber.getRight(), is("3"));
    }

    @Test
    public void testGetSectionNumber_doubleSpace_ShouldWork() throws Exception {
        String text = "3   Supercon 2";
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List< LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        currentParagraphTokens.get(6).setSubscript(true);
        Pair<List<LayoutToken>, String> sectionNumber = new TEIFormatter(null, null)
            .getSectionNumber(currentParagraphTokens);

        String output = LayoutTokensUtil.toText(sectionNumber.getLeft());
        assertThat(output, is("Supercon 2"));
        assertThat(sectionNumber.getRight(), is("3"));
    }

}