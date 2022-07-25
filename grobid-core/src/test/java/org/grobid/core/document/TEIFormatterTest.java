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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.grobid.core.document.TEIFormatter.TEI_STYLE_BOLD_NAME;
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
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
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
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
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
    public void testExtractStylesList_single_shouldWork() throws Exception {
        String text = "The room temperature magnetic hysteresis loop for melt-spun ribbons of pure Nd 2 Fe 14 B is shown in Figure ";
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        currentParagraphTokens.get(26).setSubscript(true);
        currentParagraphTokens.get(30).setSubscript(true);

        List<Triple<String, String, OffsetPosition>> pairs = TEIFormatter.extractStylesList(currentParagraphTokens);

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
    public void applyStyleList_simpleStyles_shouldWork() throws Exception {
        String text = "This is bold and italic.";
        List<Triple<String, String, OffsetPosition>> styles = new ArrayList<>();
        styles.add(Triple.of("bold", "bold", new OffsetPosition(8, 12)));
        styles.add(Triple.of("italic", "italic", new OffsetPosition(17, 23)));
        Element rootElement = XmlBuilderUtils.teiElement("p");
        TEIFormatter.applyStyleList(rootElement, text, styles);

        assertThat(rootElement.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">This is " +
            "<hi rend=\"bold\">bold</hi> and <hi rend=\"italic\">italic</hi>.</p>"));
    }

    @Test
    public void applyStyleList_complexStyles_shouldWork() throws Exception {
        String text = "This is bold and italic.";
        List<Triple<String, String, OffsetPosition>> styles = new ArrayList<>();
        styles.add(Triple.of("subscript", "is", new OffsetPosition(5, 7)));
        styles.add(Triple.of("bold subscript", "bold", new OffsetPosition(8, 12)));
        styles.add(Triple.of("italic superscript", "and", new OffsetPosition(13, 16)));
        styles.add(Triple.of("italic", "italic", new OffsetPosition(17, 23)));
        Element rootElement = XmlBuilderUtils.teiElement("p");
        TEIFormatter.applyStyleList(rootElement, text, styles);

        assertThat(rootElement.toXML(), is("<p xmlns=\"http://www.tei-c.org/ns/1.0\">This " +
            "<hi rend=\"subscript\">is</hi> " +
            "<hi rend=\"bold subscript\">bold</hi> " +
            "<hi rend=\"italic superscript\">and</hi> " +
            "<hi rend=\"italic\">italic</hi>.</p>"));
    }

    @Test
    public void testExtractStylesList_combined_shouldWork() throws Exception {
        String text = "The room temperature magnetic hysteresis loop for melt-spun ribbons of pure Nd 2 Fe 14 B is shown in Figure ";
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        currentParagraphTokens.get(26).setSubscript(true);
        currentParagraphTokens.get(26).setBold(true);
        currentParagraphTokens.get(26).setItalic(true);
        currentParagraphTokens.get(30).setSubscript(true);

        List<Triple<String, String, OffsetPosition>> pairs = TEIFormatter.extractStylesList(currentParagraphTokens);

        assertThat(pairs, hasSize(2));
        assertThat(pairs.get(0).getLeft(), is("bold italic subscript"));
        assertThat(pairs.get(0).getMiddle(), is("2"));
        assertThat(pairs.get(0).getRight().start, is(79));
        assertThat(pairs.get(0).getRight().end, is(80));

        assertThat(pairs.get(1).getLeft(), is("subscript"));
        assertThat(pairs.get(1).getMiddle(), is("14"));
        assertThat(pairs.get(1).getRight().start, is(84));
        assertThat(pairs.get(1).getRight().end, is(86));
    }

    @Test
    public void testExtractStylesList_ignoreBold_shouldWork() throws Exception {
        String text = "The room temperature magnetic hysteresis loop for melt-spun ribbons of pure Nd 2 Fe 14 B is shown in Figure ";
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        currentParagraphTokens.get(26).setSubscript(true);
        currentParagraphTokens.get(26).setBold(true);
        currentParagraphTokens.get(26).setItalic(true);
        currentParagraphTokens.get(30).setSubscript(true);

        List<Triple<String, String, OffsetPosition>> pairs = TEIFormatter.extractStylesList(currentParagraphTokens, Arrays.asList(TEI_STYLE_BOLD_NAME));

        assertThat(pairs, hasSize(2));
        assertThat(pairs.get(0).getLeft(), is("italic subscript"));
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
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

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
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        currentParagraphTokens.get(6).setSubscript(true);
        Pair<List<LayoutToken>, String> sectionNumber = new TEIFormatter(null, null)
            .getSectionNumber(currentParagraphTokens);

        String output = LayoutTokensUtil.toText(sectionNumber.getLeft());
        assertThat(output, is("Supercon 2"));
        assertThat(sectionNumber.getRight(), is("3"));
    }

}