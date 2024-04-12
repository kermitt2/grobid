package org.grobid.core.lexicon;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.PDFAnnotation;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class LexiconTest {
    @Test
    public void testCharacterPositionsUrlPattern_URL_shouldReturnCorrectInterval() throws Exception {
        final String input = "This work was distributed on http:// github.com/myUsername/MyProject";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPattern(tokenisedInput);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition FirstURL = offsetPositions.get(0);
        assertThat(input.substring(FirstURL.start, FirstURL.end), is("http:// github.com/myUsername/MyProject"));
    }

    @Test
    public void testTokenPositionsUrlPattern_URL_shouldReturnCorrectInterval() throws Exception {
        final String input = "This work was distributed on http:// github.com/myUsername/MyProject";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        List<OffsetPosition> offsetPositions = Lexicon.tokenPositionsUrlPattern(tokenisedInput);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition FirstURL = offsetPositions.get(0);
        //Note: The intervals returned by the method Utilities.convertStringOffsetToTokenOffset
        // consider the upper index to be included, while java consider the upper index to be excluded
        assertThat(LayoutTokensUtil.toText(tokenisedInput.subList(FirstURL.start, FirstURL.end + 1)), is("http:// github.com/myUsername/MyProject"));
    }

    @Test
    @Ignore("This test will fail, it can be used to test a real case when updating the regular exception")
    public void testCharacterPositionsUrlPattern_URL_shouldReturnCorrectInterval_2() throws Exception {
        final String input = "720 137409 The Government of Lao PDR 2005 Forestry Strategy to the year 2020 of the Lao PDR (available at: https://faolex.fao.org/ docs/pdf/lao144178.pdf)";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPattern(tokenisedInput);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url = offsetPositions.get(0);
        assertThat(input.substring(url.start, url.end), is("https://faolex.fao.org/ docs/pdf/lao144178.pdf"));
    }

    @Test
    public void testCharacterPositionsUrlPatternWithPDFAnnotations_URL_shouldReturnCorrectInterval() throws Exception {
        final String input = "1. 'internal status' indicates that their records should be \n" +
            "hidden in the interface. \n" +
            "2. In our previous work [1] we reported 77.03% F1-\n" +
            "score. There is a slight decrease in absolute scores \n" +
            "between DeLFT 0.2.8 and DeLFT 0.3.0. One cause \n" +
            "may be the use of different hyperparameters in \n" +
            "version 0.3.0 such as batch size and learning rate. \n" +
            "However, the most probable cause could be the \n" +
            "impact of using the Huggingface tokenizers \n" +
            "library which is suffering from quality issues \n" +
            "https://github.com/kermitt2/delft/issues/150. \n" +
            "\n" +
            "\n";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        //This is the actual text that is passed and is different from the layoutToken text.
        final String inputReal = "1. 'internal status' indicates that their records should be hidden in the interface. 2. In our previous work [1] we reported 77.03% F1score. There is a slight decrease in absolute scores between DeLFT 0.2.8 and DeLFT 0.3.0. One cause may be the use of different hyperparameters in version 0.3.0 such as batch size and learning rate. However, the most probable cause could be the impact of using the Huggingface tokenizers library which is suffering from quality issues https://github.com/kermitt2/delft/issues/150. ";

        PDFAnnotation annotation = new PDFAnnotation();
        annotation.setPageNumber(10);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(10, 84.30, 706.68, 177.39, 9.52));
        annotation.setBoundingBoxes(boundingBoxes);
        annotation.setDestination("https://github.com/kermitt2/delft/issues/150");
        annotation.setType(PDFAnnotation.Type.URI);

        List<PDFAnnotation> pdfAnnotations = List.of(annotation);
        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url = offsetPositions.get(0);
        assertThat(StringUtils.substring(input, url.start, url.end), is("https://github.com/kermitt2/delft/issues/150"));
    }

    @Test
    public void testCharacterPositionsUrlPatternWithPDFAnnotations_URL_shouldReturnCorrectInterval2() throws Exception {
        final String input = "This work is available at https://github.com/lfoppiano/ \n" +
            "supercon2. The repository contains the code of the \n" +
            "SuperCon 2 interface, the curation workflow, and the \n" +
            "\n" +
            "Table 2. Data support, the number of entities for each label in \n" +
            "each of the datasets used for evaluating the ML models. The \n" +
            "base dataset is the original dataset described in [18], and the \n" +
            "curation dataset is automatically collected based on the data-\n" +
            "base corrections by the interface and manually corrected. \n" +
            "\n";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        LayoutToken lastTokenOfTheURL = tokenisedInput.get(19);
        lastTokenOfTheURL.setPage(9);
        lastTokenOfTheURL.setX(530.9363448275863);
        lastTokenOfTheURL.setY(538.153);
        lastTokenOfTheURL.setWidth(4.363655172413793);
        lastTokenOfTheURL.setHeight(9.702);

        PDFAnnotation annotation = new PDFAnnotation();
        annotation.setPageNumber(9);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(9, 408.76, 537.11, 126.54, 10.49));
        annotation.setBoundingBoxes(boundingBoxes);
        annotation.setDestination("https://github.com/lfoppiano/supercon2");
        annotation.setType(PDFAnnotation.Type.URI);

        List<PDFAnnotation> pdfAnnotations = List.of(annotation);
        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url = offsetPositions.get(0);
        assertThat(input.substring(url.start, url.end), is("https://github.com/lfoppiano/ \nsupercon2"));
    }

    @Test
    public void testTokensPositionsUrlPatternWithPDFAnnotations_URL_shouldReturnCorrectInterval2() throws Exception {
        final String input = "This work is available at https://github.com/lfoppiano/ \n" +
            "supercon2. The repository contains the code of the \n" +
            "SuperCon 2 interface, the curation workflow, and the \n" +
            "\n" +
            "Table 2. Data support, the number of entities for each label in \n" +
            "each of the datasets used for evaluating the ML models. The \n" +
            "base dataset is the original dataset described in [18], and the \n" +
            "curation dataset is automatically collected based on the data-\n" +
            "base corrections by the interface and manually corrected. \n" +
            "\n";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        LayoutToken lastTokenOfTheURL = tokenisedInput.get(19);
        lastTokenOfTheURL.setPage(9);
        lastTokenOfTheURL.setX(530.9363448275863);
        lastTokenOfTheURL.setY(538.153);
        lastTokenOfTheURL.setWidth(4.363655172413793);
        lastTokenOfTheURL.setHeight(9.702);

        PDFAnnotation annotation = new PDFAnnotation();
        annotation.setPageNumber(9);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(9, 408.76, 537.11, 126.54, 10.49));
        annotation.setBoundingBoxes(boundingBoxes);
        annotation.setDestination("https://github.com/lfoppiano/supercon2");
        annotation.setType(PDFAnnotation.Type.URI);

        List<PDFAnnotation> pdfAnnotations = List.of(annotation);
        List<OffsetPosition> offsetPositions = Lexicon.tokenPositionUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url = offsetPositions.get(0);
        assertThat(LayoutTokensUtil.toText(tokenisedInput.subList(url.start, url.end)), is("https://github.com/lfoppiano/ \nsupercon2"));
    }

    @Test
    public void testCharacterPositionsUrlPatternWithPDFAnnotations_URL_shouldReturnCorrectIntervalBasedOnText() throws Exception {
        final String input = "This work is available at https://github.com/lfoppiano/ \n" +
            "supercon2. The repository contains the code of the \n" +
            "SuperCon 2 interface, the curation workflow, and the \n" +
            "\n" +
            "Table 2. Data support, the number of entities for each label in \n" +
            "each of the datasets used for evaluating the ML models. The \n" +
            "base dataset is the original dataset described in [18], and the \n" +
            "curation dataset is automatically collected based on the data-\n" +
            "base corrections by the interface and manually corrected. \n" +
            "\n";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        LayoutToken lastTokenOfTheURL = tokenisedInput.get(19);
        lastTokenOfTheURL.setPage(9);
        lastTokenOfTheURL.setX(530.9363448275863);
        lastTokenOfTheURL.setY(538.153);
        lastTokenOfTheURL.setWidth(4.363655172413793);
        lastTokenOfTheURL.setHeight(9.702);

        PDFAnnotation annotation = new PDFAnnotation();
        annotation.setPageNumber(9);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(9, 408.76, 537.11, 126.54, 10.49));
        annotation.setBoundingBoxes(boundingBoxes);
        annotation.setDestination("https://github.com/lfoppiano/supercon2");
        annotation.setType(PDFAnnotation.Type.URI);
        List<PDFAnnotation> pdfAnnotations = List.of(annotation);

        //This is the actual text that is passed and is different from the layoutToken text.
        final String inputReal = "This work is available at https://github.com/lfoppiano/ supercon2. The repository contains the code of the SuperCon 2 interface, the curation workflow, and the Table 2. Data support, the number of entities for each label in each of the datasets used for evaluating the ML models. The base dataset is the original dataset described in [18], and the curation dataset is automatically collected based on the database corrections by the interface and manually corrected. ";

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations, inputReal);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url = offsetPositions.get(0);
        assertThat(inputReal.substring(url.start, url.end), is("https://github.com/lfoppiano/ supercon2"));
    }
}
