package org.grobid.core.document;

import nu.xom.Element;
import nu.xom.Node;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Note;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.SentenceUtilities;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.grobid.core.document.TEIFormatter.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class TEIFormatterTest {

    @BeforeClass
    public static void setInitialContext() throws Exception {
        GrobidProperties.getInstance();
    }

    @Test
    public void testMakeFootNote() throws Exception {

        String text = "1 This is a footnote";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        List<Note> footnotes = new TEIFormatter(null, null).makeNotes(tokens, text, Note.NoteType.FOOT, 0);
        assertThat(footnotes.size(), is(1));

        Note footnote = footnotes.get(0);

        assertThat(footnote.getText(), is("This is a footnote"));
        assertThat(LayoutTokensUtil.toText(footnote.getTokens()), is("This is a footnote"));
        assertThat(footnote.getLabel(), is("1"));
    }


    @Test
    public void testMakeNotes() throws Exception {
        String text = "198 U.S. Const. art. I,  § §9 & 10. \n199 To be sure, there are revisionist arguments that the Ex Post Facto clause itself extends to retroactive civil laws too. See Eastern Enterprises v. Apfel, 524 U.S. 498, 538-39 (1998) (Thomas, J., concurring). And as with bills of attainder, in the wake of the Civil War the Supreme Court held that Ironclad  Oath requirements were ex post facto laws as well. Cummings, 71 U.S. at 326-332; Garland, 71 U.S.  at 377-368. But as discussed in the text, even these principles do not ensnare Section Three going  forward, on a non-ex-post-facto basis \n200 3 U.S. at 378-80 (arguments of counsel). \n201 Id. \n202 Id. at 382. See Baude & Sachs, Eleventh Amendment, supra note 9, at 626-627.   Electronic copy available at: https://ssrn.com/abstract=4532751";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        text = text.replace("\n", " ");
        tokens.stream().forEach(t-> t.setOffset(t.getOffset() + 403));
        List<Note> footnotes = new TEIFormatter(null, null)
            .makeNotes(tokens, text, Note.NoteType.FOOT, 37);

        assertThat(footnotes, hasSize(5));
        assertThat(footnotes.get(0).getLabel(), is("198"));
        assertThat(footnotes.get(0).getTokens(), hasSize(greaterThan(0)));
        assertThat(footnotes.get(1).getLabel(), is("199"));
        assertThat(footnotes.get(1).getTokens(), hasSize(greaterThan(0)));
        assertThat(footnotes.get(2).getLabel(), is("200"));
        assertThat(footnotes.get(2).getTokens(), hasSize(greaterThan(0)));
        assertThat(footnotes.get(3).getLabel(), is("201"));
        assertThat(footnotes.get(3).getText(), is("Id. "));
        assertThat(footnotes.get(3).getTokens(), hasSize(greaterThan(0)));
        assertThat(footnotes.get(4).getLabel(), is("202"));
        assertThat(footnotes.get(4).getTokens(), hasSize(greaterThan(0)));
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
    public void testSegmentIntoSentences_NoStyle_ShouldWork() throws Exception {
        String text = "One sentence (Foppiano et al.). Second sentence (Lopez et al.). ";

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        currentParagraphTokens.get(0).setBold(true);
        currentParagraphTokens.get(2).setBold(true);
        currentParagraphTokens.get(2).setItalic(true);
        Element currentParagraph = XmlBuilderUtils.teiElement("p");
        currentParagraph.appendChild("One sentence");
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Foppiano et al.)"));
        currentParagraph.appendChild(". ");
        currentParagraph.appendChild("Second sentence");
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Lopez et al.)"));
        currentParagraph.appendChild(".");

        new TEIFormatter(null, null)
            .segmentIntoSentences(currentParagraph, currentParagraphTokens, config, "en");

        assertThat(currentParagraph.toXML(),
            is("<p xmlns=\"http://www.tei-c.org/ns/1.0\"><s>One sentence <ref>(Foppiano et al.)</ref>.</s><s>Second sentence <ref>(Lopez et al.)</ref>.</s></p>"));
    }


    @Test
    public void testSegmentIntoSentences_Style_ShouldWork() throws Exception {
        String text1_0 = "One sentence ";
        String text1_1 = ". ";
        String text2_0 = "Second sentence ";
        String text2_1 = ".";

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder()
            .withSentenceSegmentation(true)
            .build();

        List<LayoutToken> tokens = new ArrayList<>();
        List<LayoutToken> currentParagraphTokens1_0 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_0);
        tokens.addAll(currentParagraphTokens1_0);
        List<LayoutToken> currentParagraphTokens1_1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_1);
        tokens.addAll(currentParagraphTokens1_1);
        List<LayoutToken> currentParagraphTokens2_0 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text2_0);
        tokens.addAll(currentParagraphTokens2_0);
        List<LayoutToken> currentParagraphTokens2_1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text2_1);
        tokens.addAll(currentParagraphTokens2_1);

        currentParagraphTokens1_0.get(0).setBold(true);
        currentParagraphTokens1_0.get(2).setBold(true);
        currentParagraphTokens1_0.get(2).setItalic(true);

        List<Triple<String, String, OffsetPosition>> styles1_0 = extractStylesList(currentParagraphTokens1_0);
        List<Triple<String, String, OffsetPosition>> styles1_1 = extractStylesList(currentParagraphTokens1_1);
        List<Triple<String, String, OffsetPosition>> styles2_0 = extractStylesList(currentParagraphTokens2_0);
        List<Triple<String, String, OffsetPosition>> styles2_1 = extractStylesList(currentParagraphTokens2_1);

        Element currentParagraph = XmlBuilderUtils.teiElement("p");

        applyStyleList(currentParagraph, text1_0, styles1_0);
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Foppiano et al.)"));
        applyStyleList(currentParagraph, text1_1, styles1_1);
        currentParagraph.appendChild(" ");
        applyStyleList(currentParagraph, text2_0, styles2_0);
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Lopez et al.)"));
        applyStyleList(currentParagraph, text2_1, styles2_1);

        //Assuming these are injected correctly

        new TEIFormatter(null, null).segmentIntoSentences(currentParagraph, tokens, config, "en");

        assertThat(currentParagraph.toXML(),
            is("<p xmlns=\"http://www.tei-c.org/ns/1.0\"><s><hi rend=\"bold\">One</hi> <hi rend=\"bold italic\">sentence</hi> <ref>(Foppiano et al.)</ref>.</s><s>Second sentence <ref>(Lopez et al.)</ref>.</s></p>"));
    }

    @Test
    public void testSegmentIntoSentences_StyleBetweenTwoSentences_ShouldWork() throws Exception {
        String text1_0 = "One sentence";
        String text1_1 = ". ";
        String text2_0 = "Second sentence";
        String text2_1 = ".";

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder()
            .withSentenceSegmentation(true)
            .build();

        List<LayoutToken> tokens = new ArrayList<>();
        List<LayoutToken> currentParagraphTokens1_0 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_0);
        tokens.addAll(currentParagraphTokens1_0);
        List<LayoutToken> currentParagraphTokens1_1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_1);
        tokens.addAll(currentParagraphTokens1_1);
        List<LayoutToken> currentParagraphTokens2_0 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text2_0);
        tokens.addAll(currentParagraphTokens2_0);
        List<LayoutToken> currentParagraphTokens2_1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text2_1);
        tokens.addAll(currentParagraphTokens2_1);

        currentParagraphTokens1_0.get(0).setBold(true); //One
        currentParagraphTokens1_0.get(2).setItalic(true); //sentence
        currentParagraphTokens1_1.get(0).setItalic(true); //.
        currentParagraphTokens2_0.get(0).setItalic(true); //Second
        currentParagraphTokens2_0.get(2).setItalic(true); //sentence

        List<Triple<String, String, OffsetPosition>> styles1_0 = extractStylesList(currentParagraphTokens1_0);
        List<Triple<String, String, OffsetPosition>> styles1_1 = extractStylesList(currentParagraphTokens1_1);
        List<Triple<String, String, OffsetPosition>> styles2_0 = extractStylesList(currentParagraphTokens2_0);
        List<Triple<String, String, OffsetPosition>> styles2_1 = extractStylesList(currentParagraphTokens2_1);

        Element currentParagraph = XmlBuilderUtils.teiElement("p");

        applyStyleList(currentParagraph, text1_0, styles1_0);
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Foppiano et al.)"));
        applyStyleList(currentParagraph, text1_1, styles1_1);
        currentParagraph.appendChild(" ");
        applyStyleList(currentParagraph, text2_0, styles2_0);
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Lopez et al.)"));
        applyStyleList(currentParagraph, text2_1, styles2_1);

        //Assuming these are injected correctly

        new TEIFormatter(null, null).segmentIntoSentences(currentParagraph, tokens, config, "en");

        assertThat(currentParagraph.toXML(),
            is("<p xmlns=\"http://www.tei-c.org/ns/1.0\"><s><hi rend=\"bold\">One</hi> <hi rend=\"italic\">sentence</hi> <ref>(Foppiano et al.)</ref><hi rend=\"italic\">.</hi></s><s><hi rend=\"italic\">Second sentence</hi> <ref>(Lopez et al.)</ref>.</s></p>"));
    }

    @Test
    public void testSegmentIntoSentences_StyleBetweenTwoSentences_oneRef_ShouldWork() throws Exception {
        String text1_0 = "One sentence. Second sentence";
        String text1_1 = ".";

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder()
            .withSentenceSegmentation(true)
            .build();

        List<LayoutToken> tokens = new ArrayList<>();
        List<LayoutToken> currentParagraphTokens1_0 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_0);
        tokens.addAll(currentParagraphTokens1_0);
        List<LayoutToken> currentParagraphTokens1_1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_1);
        tokens.addAll(currentParagraphTokens1_1);

        currentParagraphTokens1_0.get(0).setBold(true); //One
        currentParagraphTokens1_0.get(2).setItalic(true); //sentence
        currentParagraphTokens1_0.get(3).setItalic(true); //.
        currentParagraphTokens1_0.get(5).setItalic(true); //Second

        List<Triple<String, String, OffsetPosition>> styles1_0 = extractStylesList(currentParagraphTokens1_0);
        List<Triple<String, String, OffsetPosition>> styles1_1 = extractStylesList(currentParagraphTokens1_1);

        Element currentParagraph = XmlBuilderUtils.teiElement("p");

        applyStyleList(currentParagraph, text1_0, styles1_0);
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Lopez et al.)"));
        applyStyleList(currentParagraph, text1_1, styles1_1);

        new TEIFormatter(null, null).segmentIntoSentences(currentParagraph, tokens, config, "en");

        assertThat(currentParagraph.toXML(),
            is("<p xmlns=\"http://www.tei-c.org/ns/1.0\"><s><hi rend=\"bold\">One</hi> <hi rend=\"italic\">sentence.</hi></s><s><hi rend=\"italic\">Second</hi> sentence <ref>(Lopez et al.)</ref>.</s></p>"));
    }

    @Test
    public void testSegmentIntoSentences_StyleBetweenTwoSentencesWithoutRefs_ShouldWork() throws Exception {
        String text = "One sentence. Second sentence.";

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder()
            .withSentenceSegmentation(true)
            .build();

        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        tokens.get(0).setBold(true); //One
        tokens.get(2).setItalic(true); //sentence
        tokens.get(3).setItalic(true); //.
        tokens.get(5).setItalic(true); //Second
//        currentParagraphTokens.get(7).setItalic(true); //sentence

        List<Triple<String, String, OffsetPosition>> styles = extractStylesList(tokens);

        Element currentParagraph = XmlBuilderUtils.teiElement("p");

        applyStyleList(currentParagraph, text, styles);

        //Assuming these are injected correctly
        new TEIFormatter(null, null).segmentIntoSentences(currentParagraph, tokens, config, "en");

        assertThat(currentParagraph.toXML(),
            is("<p xmlns=\"http://www.tei-c.org/ns/1.0\"><s><hi rend=\"bold\">One</hi> <hi rend=\"italic\">sentence.</hi></s><s><hi rend=\"italic\">Second</hi> sentence.</s></p>"));
    }

    @Test
    public void testSplitMapNodesOverSentenceSplits_shouldAdjustNodes() {
        TEIFormatter teiFormatter = new TEIFormatter(null, null);

        String text1_0 = "One sentence. Second sentence";
        String text1_1 = ".";

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder()
            .withSentenceSegmentation(true)
            .build();

        List<LayoutToken> tokens = new ArrayList<>();
        List<LayoutToken> currentParagraphTokens1_0 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_0);
        tokens.addAll(currentParagraphTokens1_0);
        List<LayoutToken> currentParagraphTokens1_1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_1);
        tokens.addAll(currentParagraphTokens1_1);

        currentParagraphTokens1_0.get(0).setBold(true); //One
        currentParagraphTokens1_0.get(2).setItalic(true); //sentence
        currentParagraphTokens1_0.get(3).setItalic(true); //.
        currentParagraphTokens1_0.get(5).setItalic(true); //Second

        List<Triple<String, String, OffsetPosition>> styles1_0 = extractStylesList(currentParagraphTokens1_0);
        List<Triple<String, String, OffsetPosition>> styles1_1 = extractStylesList(currentParagraphTokens1_1);

        Element currentParagraph = XmlBuilderUtils.teiElement("p");

        applyStyleList(currentParagraph, text1_0, styles1_0);
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Lopez et al.)"));
        applyStyleList(currentParagraph, text1_1, styles1_1);

        String text = currentParagraph.getValue();

        Map<Integer, Pair<Node, String>> nestedNodes = teiFormatter.identifyNestedNodes(currentParagraph);
        List<OffsetPosition> forbiddenPositions = nestedNodes.entrySet()
            .stream()
            .filter(entry -> ((Element) entry.getValue().getLeft()).getLocalName().equals("ref"))
            .map(entry -> new OffsetPosition(entry.getKey(), entry.getValue().getRight().length() + entry.getKey()))
            .collect(Collectors.toList());

        List<OffsetPosition> sentencesOffsetPosition =
            SentenceUtilities.getInstance().runSentenceDetection(text, forbiddenPositions, tokens, new Language("en"));

        Map<Integer, Pair<Node, String>> adjustedNestedNodes = teiFormatter.splitMapNodesOverSentenceSplits(nestedNodes, text, sentencesOffsetPosition);

        assertThat(adjustedNestedNodes.size(), is(4));

        assertThat(new ArrayList<>(adjustedNestedNodes.keySet()), is(Arrays.asList(0, 4, 14, 30)));

        assertThat(adjustedNestedNodes.get(0).getRight(), is("One"));
        assertThat(adjustedNestedNodes.get(4).getRight(), is("sentence."));
        assertThat(adjustedNestedNodes.get(14).getRight(), is("Second"));
        assertThat(adjustedNestedNodes.get(30).getRight(), is("(Lopez et al.)"));
    }

    @Test
    public void testSplitMapNodesOverThreeSentenceSplits_shouldAdjustNodes() {
        TEIFormatter teiFormatter = new TEIFormatter(null, null);

        String text1_0 = "One sentence. Second sentence. Third sentence";
        String text1_1 = ".";

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder()
            .withSentenceSegmentation(true)
            .build();

        List<LayoutToken> tokens = new ArrayList<>();
        List<LayoutToken> currentParagraphTokens1_0 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_0);
        tokens.addAll(currentParagraphTokens1_0);
        List<LayoutToken> currentParagraphTokens1_1 = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text1_1);
        tokens.addAll(currentParagraphTokens1_1);

        currentParagraphTokens1_0.get(0).setBold(true); //One
        currentParagraphTokens1_0.get(2).setItalic(true); //sentence
        currentParagraphTokens1_0.get(3).setItalic(true); //.
        currentParagraphTokens1_0.get(5).setItalic(true); //Second
        currentParagraphTokens1_0.get(7).setItalic(true); //sentence
        currentParagraphTokens1_0.get(8).setItalic(true); //.
        currentParagraphTokens1_0.get(10).setItalic(true); //Third
//        currentParagraphTokens1_0.get(12).setItalic(true); //sentence

        List<Triple<String, String, OffsetPosition>> styles1_0 = extractStylesList(currentParagraphTokens1_0);
        List<Triple<String, String, OffsetPosition>> styles1_1 = extractStylesList(currentParagraphTokens1_1);

        Element currentParagraph = XmlBuilderUtils.teiElement("p");

        applyStyleList(currentParagraph, text1_0, styles1_0);
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Lopez et al.)"));
        applyStyleList(currentParagraph, text1_1, styles1_1);

        String text = currentParagraph.getValue();

        Map<Integer, Pair<Node, String>> nestedNodes = teiFormatter.identifyNestedNodes(currentParagraph);
        List<OffsetPosition> forbiddenPositions = nestedNodes.entrySet()
            .stream()
            .filter(entry -> ((Element) entry.getValue().getLeft()).getLocalName().equals("ref"))
            .map(entry -> new OffsetPosition(entry.getKey(), entry.getValue().getRight().length() + entry.getKey()))
            .collect(Collectors.toList());

        List<OffsetPosition> sentencesOffsetPosition =
            SentenceUtilities.getInstance().runSentenceDetection(text, forbiddenPositions, tokens, new Language("en"));

        Map<Integer, Pair<Node, String>> adjustedNestedNodes = teiFormatter.splitMapNodesOverSentenceSplits(nestedNodes, text, sentencesOffsetPosition);

        assertThat(adjustedNestedNodes.size(), is(5));

        assertThat(new ArrayList<>(adjustedNestedNodes.keySet()), is(Arrays.asList(0, 4, 14, 31, 46)));

        assertThat(adjustedNestedNodes.get(0).getRight(), is("One"));
        assertThat(adjustedNestedNodes.get(4).getRight(), is("sentence."));
        assertThat(adjustedNestedNodes.get(14).getRight(), is("Second sentence."));
        assertThat(adjustedNestedNodes.get(31).getRight(), is("Third"));
        assertThat(adjustedNestedNodes.get(46).getRight(), is("(Lopez et al.)"));
    }

    @Test
    public void testIdentifyRefNotes() throws Exception {
        Element currentParagraph = XmlBuilderUtils.teiElement("p");
        currentParagraph.appendChild("One sentence");
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Foppiano et al.)"));
        currentParagraph.appendChild(". ");
        currentParagraph.appendChild("Second sentence");
        currentParagraph.appendChild(" ");
        currentParagraph.appendChild(XmlBuilderUtils.teiElement("ref", "(Lopez et al.)"));
        currentParagraph.appendChild(".");

        Map<Integer, Pair<Node, String>> integerPairMap = new TEIFormatter(null, null).identifyNestedNodes(currentParagraph);

        assertThat(integerPairMap.keySet(), hasSize(2));
        assertThat(integerPairMap.keySet().stream().toArray()[1], is(13));
        assertThat(integerPairMap.get(13).getRight(), is("(Foppiano et al.)"));

        assertThat(integerPairMap.keySet().stream().toArray()[0], is(48));
        assertThat(integerPairMap.get(48).getRight(), is("(Lopez et al.)"));
    }

    @Test
    public void testExtractStylesList_single_shouldWork() throws Exception {
        String text = "The room temperature magnetic hysteresis loop for melt-spun ribbons of pure Nd 2 Fe 14 B is shown in Figure ";
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        currentParagraphTokens.get(26).setSubscript(true);
        currentParagraphTokens.get(30).setSubscript(true);

        List<Triple<String, String, OffsetPosition>> pairs = extractStylesList(currentParagraphTokens);

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

        List<Triple<String, String, OffsetPosition>> pairs = extractStylesList(currentParagraphTokens);

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
    public void testExtractStylesList_continuousTokens_shouldWork() throws Exception {
        String text = "The room temperature magnetic hysteresis loop for melt-spun ribbons of pure Nd 2 Fe 14 B is shown in Figure ";
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().build();
        List<LayoutToken> currentParagraphTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        currentParagraphTokens.get(24).setBold(true);
        currentParagraphTokens.get(26).setBold(true);
        currentParagraphTokens.get(28).setBold(true);
        currentParagraphTokens.get(30).setBold(true);

        List<Triple<String, String, OffsetPosition>> pairs = extractStylesList(currentParagraphTokens);

        assertThat(pairs, hasSize(1));
        assertThat(pairs.get(0).getLeft(), is("bold"));
        assertThat(pairs.get(0).getMiddle(), is("Nd 2 Fe 14"));
        assertThat(pairs.get(0).getRight().start, is(76));
        assertThat(pairs.get(0).getRight().end, is(86));
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

        List<Triple<String, String, OffsetPosition>> pairs = extractStylesList(currentParagraphTokens, Arrays.asList(TEI_STYLE_BOLD_NAME));

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

    @Ignore("The middle is actually not used")
    public void testExtractStylesList_checkProducedText_ShouldWork() throws Exception {
        String text = "I. Introduction  1.1. Généralités et rappels  ";
        List<LayoutToken> textTokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        textTokens.get(0).setBold(true);
        textTokens.get(1).setBold(true);
        textTokens.get(3).setBold(true);

        textTokens.get(6).setItalic(true);
        textTokens.get(7).setItalic(true);
        textTokens.get(8).setItalic(true);
        textTokens.get(9).setItalic(true);
        textTokens.get(11).setItalic(true);
        textTokens.get(13).setItalic(true);
        textTokens.get(15).setItalic(true);

        List<Triple<String, String, OffsetPosition>> pairs = extractStylesList(textTokens);

        assertThat(pairs, hasSize(2));
        assertThat(pairs.get(0).getLeft(), is("bold"));
        assertThat(pairs.get(0).getMiddle(), is("I. Introduction"));
        assertThat(pairs.get(1).getLeft(), is("italic"));
        assertThat(pairs.get(1).getMiddle(), is("1.1. Généralités et rappels"));
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