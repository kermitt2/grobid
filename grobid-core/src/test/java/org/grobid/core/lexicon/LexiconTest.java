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
    public void testCharacterPositionsUrlPattern_URLStartingWithWWW_shouldReturnCorrectInterval() throws Exception {
        final String input = "This work was distributed on www. github.com/myUsername/MyProject";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPattern(tokenisedInput);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition FirstURL = offsetPositions.get(0);
        assertThat(input.substring(FirstURL.start, FirstURL.end), is("www. github.com/myUsername/MyProject"));
    }

    @Test
    public void testCharacterPositionsUrlPattern_URLStartingWithHTTPS_shouldReturnCorrectInterval() throws Exception {
        final String input = "This work was distributed on https:// www.github.com/myUsername/MyProject";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPattern(tokenisedInput);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition FirstURL = offsetPositions.get(0);
        assertThat(input.substring(FirstURL.start, FirstURL.end), is("https:// www.github.com/myUsername/MyProject"));
    }

    /**
     * This test is to confirm the limitation of this method using the regex, where we prefer failing on some cases
     * rather than have a lot of false positive. This method will be anyway complemented with the annotated links in
     * the PDF (if available).
     */
    @Test
    public void testCharacterPositionsUrlPattern_URLTruncated_shouldReturnCorrectIntervalWithmissingPartOfURL() throws Exception {
        final String input = "This work was distributed on https://www. github.com/myUsername/MyProject";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPattern(tokenisedInput);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition FirstURL = offsetPositions.get(0);
        assertThat(input.substring(FirstURL.start, FirstURL.end), is("https://www"));
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
    public void testCharacterPositionsUrlPatternWithPDFAnnotations_URL_NoPDFAnnotationAvailable_shouldReturnCorrectInterval() throws Exception {
        final String input = "Data was analyzed using SPM8 software (http://www.fil.ion.ucl.ac.uk/spm). Images were \n" +
            "\n" +
            "spatially aligned to the first volume to correct for small movements; no run showed more than \n" +
            "\n" +
            "4mm displacement along the x, y or z dimension. Sinc interpolation minimized timing-errors \n" +
            "\n" +
            "between slices; functional images were coregistered to the anatomical image, normalized to the \n" +
            "\n" +
            "standard T1 Montreal Neurological Institute (MNI) template, and resliced at 4mm 3 resolution. \n" +
            "\n";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        List<PDFAnnotation> pdfAnnotations = new ArrayList<>();
        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url = offsetPositions.get(0);
        assertThat(StringUtils.substring(input, url.start, url.end), is("http://www.fil.ion.ucl.ac.uk/spm"));
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
        // LF: we need a + 1 because the convention for the tokenPositionUrlPattern is inclusive, inclusive
        assertThat(LayoutTokensUtil.toText(tokenisedInput.subList(url.start, url.end + 1)), is("https://github.com/lfoppiano/ \nsupercon2"));
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

    @Test
    public void testCharacterPositionsUrlPatternWithPDFAnnotations2_URL_shouldReturnCorrectIntervalBasedOnText() throws Exception {
        final String input = "Table S1: Gene annotations from which exon-exon junctions were extracted and unioned to obtain \n" +
            "a list of annotated junctions. All tracks were taken from the UCSC Genome Browser [10] except for \n" +
            "GENCODE [2], which was downloaded from the GENCODE website http://www.gencodegenes. \n" +
            "org/releases/. Junction coordinates from hg38 annotations were lifted over to hg19 before the \n" +
            "union was performed. Of all gene annotations listed here, the Swedish Bioinformatics Institute \n" +
            "(SIB) genes has the most, with over 400,000 junctions for each of hg19 and hg38. \n";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        LayoutToken lastTokenOfTheURL = tokenisedInput.get(97);
        lastTokenOfTheURL.setPage(19);
        lastTokenOfTheURL.setX(465.54675000000003);
        lastTokenOfTheURL.setY(404.908);
        lastTokenOfTheURL.setWidth(68.727);
        lastTokenOfTheURL.setHeight(9.0873);

        PDFAnnotation annotation = new PDFAnnotation();
        annotation.setPageNumber(19);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(19, 401.551, 402.396, 139.445, 12.901999999999987));
        annotation.setBoundingBoxes(boundingBoxes);
        annotation.setDestination("http://www.gencodegenes.org/releases/");
        annotation.setType(PDFAnnotation.Type.URI);
        List<PDFAnnotation> pdfAnnotations = List.of(annotation);

        //This is the actual text that is passed and is different from the layoutToken text.
        final String inputReal = "Table S1: Gene annotations from which exon-exon junctions were extracted and unioned to obtain a list of annotated junctions. All tracks were taken from the UCSC Genome Browser [10] except for GENCODE [2], which was downloaded from the GENCODE website http://www.gencodegenes. org/releases/. Junction coordinates from hg38 annotations were lifted over to hg19 before the union was performed. Of all gene annotations listed here, the Swedish Bioinformatics Institute (SIB) genes has the most, with over 400,000 junctions for each of hg19 and hg38.  ";

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations, inputReal);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url = offsetPositions.get(0);
        assertThat(inputReal.substring(url.start, url.end), is("http://www.gencodegenes. org/releases/"));
    }


    @Test
    public void testCharacterPositionsUrlPatternWithPDFAnnotations_URL_shouldReturnCorrectIntervalBasedOnText2() throws Exception {
        final String input = "Opportunities (International Rice Research Institute) \n" +
            "(available at: http://lad.nafri.org.la/fulltext/231-0.pdf) \n" +
            "Salinas-Melgoza M A, Skutsch M, Lovett J C and Borrego A \n" +
            "2017 Carbon emissions from dryland shifting cultivation: a \n" +
            "case study of Mexican tropical dry forest Silva Fenn. \n" +
            "51 1553 \n";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        LayoutToken lastTokenOfTheURL = tokenisedInput.get(37);
        lastTokenOfTheURL.setPage(11);
        lastTokenOfTheURL.setX(519.4089069767441);
        lastTokenOfTheURL.setY(733.461);
        lastTokenOfTheURL.setWidth(9.03006976744186);
        lastTokenOfTheURL.setHeight(7.5199);

        PDFAnnotation annotation = new PDFAnnotation();
        annotation.setPageNumber(11);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(11, 402.018, 732.661, 126.47100000000006, 8.480000000000018));
        annotation.setBoundingBoxes(boundingBoxes);
        annotation.setDestination("http://lad.nafri.org.la/fulltext/231-0.pdf");
        annotation.setType(PDFAnnotation.Type.URI);
        List<PDFAnnotation> pdfAnnotations = List.of(annotation);

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url = offsetPositions.get(0);
        assertThat(input.substring(url.start, url.end), is("http://lad.nafri.org.la/fulltext/231-0.pdf"));
    }


    @Test
    public void testCharacterPositionsUrlPatternWithPDFAnnotations_URL_shouldReturnCorrectIntervalBasedOnText3() throws Exception {
        final String input = "). The Laos official forest change \n" +
            "maps (https://nfms.maf.gov.la/) are created from the \n" +
            "land cover classification maps from the start year and \n" +
            "end year for each period (see the periods in table ";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        LayoutToken lastTokenOfTheURL = tokenisedInput.get(28);
        lastTokenOfTheURL.setPage(9);
        lastTokenOfTheURL.setX(461.1686153846154);
        lastTokenOfTheURL.setY(653.148);
        lastTokenOfTheURL.setWidth(3.9706923076923077);
        lastTokenOfTheURL.setHeight(9.3999);

        LayoutToken beforeLastTokenOfTheURL = tokenisedInput.get(27);
        beforeLastTokenOfTheURL.setPage(9);
        beforeLastTokenOfTheURL.setX(453.2272307692308);
        beforeLastTokenOfTheURL.setY(653.148);
        beforeLastTokenOfTheURL.setWidth(7.9413846153846155);
        beforeLastTokenOfTheURL.setHeight(9.3999);

        PDFAnnotation annotation = new PDFAnnotation();
        annotation.setPageNumber(9);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(9, 369.582, 652.149, 95.82900000000001, 10.600000000000023));
        annotation.setBoundingBoxes(boundingBoxes);
        annotation.setDestination("https://nfms.maf.gov.la/");
        annotation.setType(PDFAnnotation.Type.URI);
        List<PDFAnnotation> pdfAnnotations = List.of(annotation);

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url = offsetPositions.get(0);
        assertThat(input.substring(url.start, url.end), is("https://nfms.maf.gov.la/"));
    }


    @Test
    public void testCharacterPositionsUrlPatternWithPDFAnnotations_URL_shouldReturnCorrectIntervalBasedOnText4() throws Exception {
        final String input = "Google Earth Engine applications to visualize the \n" +
            "datasets: https://github.com/shijuanchen/shift_cult \n" +
            "Map products visualization: https://sites.google. \n" +
            "com/view/shijuanchen/research/shift_cult \n";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        LayoutToken lastTokenOfTheURL1 = tokenisedInput.get(28);
        lastTokenOfTheURL1.setPage(10);
        lastTokenOfTheURL1.setX(504.75295121951217);
        lastTokenOfTheURL1.setY(626.353);
        lastTokenOfTheURL1.setWidth(40.858048780487806);
        lastTokenOfTheURL1.setHeight(9.3999);

        LayoutToken lastTokenOfTheURL2 = tokenisedInput.get(44);
        lastTokenOfTheURL2.setPage(10);
        lastTokenOfTheURL2.setX(526.9964666666667);
        lastTokenOfTheURL2.setY(638.853);
        lastTokenOfTheURL2.setWidth(22.0712);
        lastTokenOfTheURL2.setHeight(9.3999);

        PDFAnnotation annotation1 = new PDFAnnotation();
        annotation1.setPageNumber(10);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(10, 378.093,  625.354,  167.51799999999997,  10.599999999999909));
        annotation1.setBoundingBoxes(boundingBoxes);
        annotation1.setDestination("https://github.com/shijuanchen/shift_cult");
        annotation1.setType(PDFAnnotation.Type.URI);

        PDFAnnotation annotation2 = new PDFAnnotation();
        annotation2.setPageNumber(10);
        List<BoundingBox> boundingBoxes2 = new ArrayList<>();
        boundingBoxes2.add(BoundingBox.fromPointAndDimensions(10, 475.497, 637.854,  77.26,10.60));
        annotation2.setBoundingBoxes(boundingBoxes2);
        annotation2.setDestination("https://sites.google.com/view/shijuanchen/research/shift_cult");
        annotation2.setType(PDFAnnotation.Type.URI);
        List<PDFAnnotation> pdfAnnotations = List.of(annotation1, annotation2);

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations);

        assertThat(offsetPositions, hasSize(2));
        OffsetPosition url0 = offsetPositions.get(0);
        assertThat(input.substring(url0.start, url0.end), is("https://github.com/shijuanchen/shift_cult"));
        OffsetPosition url1 = offsetPositions.get(1);
        assertThat(input.substring(url1.start, url1.end), is("https://sites.google. \ncom/view/shijuanchen/research/shift_cult"));
    }

    @Test
    public void testCharacterPositionsUrlPatternWithPDFAnnotations_URL_shouldReturnCorrectIntervalBasedOnText5() throws Exception {
        final String input = ", accessible through the University of Hawaii Sea Level Center with station ID of UHSLC ID 57 \n" +
            "(https://uhslc.soest.hawaii.edu/stations/?stn=057#levels). You can access a processed dataset of nearshore wave \n";

        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        LayoutToken lastTokenOfTheURL1 = tokenisedInput.get(53);
        lastTokenOfTheURL1.setPage(6);
        lastTokenOfTheURL1.setX(334.9306551724138);
        lastTokenOfTheURL1.setY(719.076);
        lastTokenOfTheURL1.setWidth(21.514758620689655);
        lastTokenOfTheURL1.setHeight(9.2001);

        LayoutToken lastTokenOfTheURL2 = tokenisedInput.get(54);
        lastTokenOfTheURL2.setPage(6);
        lastTokenOfTheURL2.setX(356.4454137931035);
        lastTokenOfTheURL2.setY(719.076);
        lastTokenOfTheURL2.setWidth(3.585793103448276);
        lastTokenOfTheURL2.setHeight(9.2001);

        LayoutToken lastTokenOfTheURL3 = tokenisedInput.get(55);
        lastTokenOfTheURL3.setPage(54);
        lastTokenOfTheURL3.setX(360.0312068965518);
        lastTokenOfTheURL3.setY(719.076);
        lastTokenOfTheURL3.setWidth(3.585793103448276);
        lastTokenOfTheURL3.setHeight(9.2001);

        PDFAnnotation annotation1 = new PDFAnnotation();
        annotation1.setPageNumber(6);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(6, 158.784, 716.0, 199.524, 12.275999999999954));
        annotation1.setBoundingBoxes(boundingBoxes);
        annotation1.setDestination("https://uhslc.soest.hawaii.edu/stations/?stn=057#levels");
        annotation1.setType(PDFAnnotation.Type.URI);

        List<PDFAnnotation> pdfAnnotations = List.of(annotation1);

        List<OffsetPosition> offsetPositions = Lexicon.characterPositionsUrlPatternWithPdfAnnotations(tokenisedInput, pdfAnnotations);

        assertThat(offsetPositions, hasSize(1));
        OffsetPosition url0 = offsetPositions.get(0);
        assertThat(input.substring(url0.start, url0.end), is("https://uhslc.soest.hawaii.edu/stations/?stn=057#levels"));
    }

    @Test
    public void testGetTokenPosition() throws Exception {

        //NOTE LF: The current behaviour will return -1 if the tokens are not matching with the positions
        // of the characters
        //Here the url is https://paperpile.com/c/QlNkzH/Hj7c+4D5e but because `Lameness` is attached the last token
        // is `Hj7c+4D5eLameness` which will cause troubles.

        String input = "https://paperpile.com/c/QlNkzH/Hj7c+4D5eLameness";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        OffsetPosition tokenPositions = Lexicon.getTokenPositions(40, 48, tokens);

        assertThat(tokenPositions.start, is(-1));
        assertThat(tokenPositions.end, is(-1));

    }

}
