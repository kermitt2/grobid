package org.grobid.core.lexicon;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.PDFAnnotation;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.layout.LayoutToken;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;

public class LexiconIntegrationTest {
    private Lexicon target = null;

    @Before
    public void setUp() {
        target = Lexicon.getInstance();
    }

    // journals
    @Test
    public void testInAbbrevJournalNames_case1() throws Exception {
        String input = "Nature";
        List<OffsetPosition> journalsPositions = target.tokenPositionsAbbrevJournalNames(input);

        assertNotNull(journalsPositions);
        assertThat(journalsPositions, hasSize(1));
        assertThat(journalsPositions.get(0).start, is(0));
    }


    @Test
    public void testInAbbrevJournalNames_case2() throws Exception {
        String input = "in Nature, volume";
        List<OffsetPosition> journalsPositions = target.tokenPositionsAbbrevJournalNames(input);

        assertNotNull(journalsPositions);
        assertThat(journalsPositions, hasSize(1));
        assertThat(journalsPositions.get(0).start, is(1));
    }

    @Test
    public void testJournalNames_case1() throws Exception {
        String input = "Taylor, et al., Nature 297:(1982)";
        List<OffsetPosition> journalsPositions = target.tokenPositionsJournalNames(input);

        assertNotNull(journalsPositions);
        assertThat(journalsPositions, hasSize(1));
        assertThat(journalsPositions.get(0).start, is(6));
        assertThat(journalsPositions.get(0).end, is(6));
    }

    @Test
    public void testJournalNames_case2() throws Exception {
        String input = "to be published in the official publication of the National Venereology Council " +
                "of Australia, volume 10, 2010.";
        List<OffsetPosition> journalsPositions = target.tokenPositionsJournalNames(input);

        assertNotNull(journalsPositions);
        assertThat(journalsPositions, hasSize(2));
    }

    @Test
    public void testCity() throws Exception {
        String input = "University of New-York, USA, bla bla City, bla";
        List<OffsetPosition> citiesPositions = target.tokenPositionsCityNames(input);

        assertNotNull(citiesPositions);
        assertThat(citiesPositions, hasSize(2));
    }

    @Test
    public void testInJournalNames() throws Exception {
        List<OffsetPosition> inJournalNames = target.tokenPositionsJournalNames("abc <p> Economics </p>");

        assertNotNull(inJournalNames);
        assertThat(inJournalNames, hasSize(1));
        assertThat(inJournalNames.get(0).start, is(2));
        assertThat(inJournalNames.get(0).end, is(2));
    }

    /**
     * Locations
     **/

    @Test
    public void testGetPositionInLocation_case1() throws Exception {
        final String input = "In retrospect, the National Archives of Belgium were established by the French law of October 26th 1796 (5 Brumair V), which, amongst others, foresaw in the organisation of departmental depots (amongst others, in Brussels), in which the archives of the disbanded institutions of the Ancien Régime would be stored.";
        final List<OffsetPosition> positions = target.charPositionsLocationNames(input);

        assertThat(positions, hasSize(15));
        assertThat(positions.get(0).start, is(0));
        assertThat(positions.get(0).end, is(2));
    }

    @Test
    public void testGetPositionInLocation_case1_tokenised() throws Exception {
        String input = "In retrospect, the National Archives of Belgium were established by the French law of October 26th 1796 (5 Brumair V), which, amongst others, foresaw in the organisation of departmental depots (amongst others, in Brussels), in which the archives of the disbanded institutions of the Ancien Régime would be stored.";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        final List<OffsetPosition> positions = target.charPositionsLocationNames(tokenisedInput);

        assertThat(positions, hasSize(15));
        assertThat(positions.get(0).start, is(0));
        assertThat(positions.get(0).end, is(0));
    }

    @Test
    public void testGetPositionsInLocation_case2() throws Exception {
        final String input = "I'm walking in The Bronx";
        final List<OffsetPosition> positions = target.charPositionsLocationNames(input);

        assertThat(positions, hasSize(4));
        assertThat(positions.get(3).start, is(19));
        assertThat(positions.get(3).end, is(24));
    }

    @Test
    public void testGetPositionsInLocation_case2_tokenised() throws Exception {
        final String input = "I'm walking in The Bronx";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        final List<OffsetPosition> positions = target.charPositionsLocationNames(tokenisedInput);

        assertThat(positions, hasSize(4));
        assertThat(positions.get(3).start, is(10));
        assertThat(positions.get(3).end, is(10));

        assertThat(positions.get(2).start, is(8));
        assertThat(positions.get(2).end, is(10));
    }


    /**
     * ORG Form
     **/
    @Test
    public void testGetPositionInOrgForm() throws Exception {
        final String input = "Matusa Inc. was bought by Bayer";
        final List<OffsetPosition> positions = target.charPositionsOrgForm(input);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(7));
        assertThat(positions.get(0).end, is(10));
    }

    @Test
    public void testGetPositionInOrgForm_tokenised() throws Exception {
        final String input = "Matusa Inc. was bought by Bayer";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        final List<OffsetPosition> positions = target.charPositionsOrgForm(tokenisedInput);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(2));
        assertThat(positions.get(0).end, is(2));
    }

    /**
     * Organisation names
     */
    @Test
    public void testGetPositionInOrganisationNames() throws Exception {
        final String input = "Matusa Inc. was bought by Bayer";
        final List<OffsetPosition> positions = target.charPositionsOrganisationNames(input);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(26));
        assertThat(positions.get(0).end, is(31));
    }

    @Test
    public void testGetPositionInOrganisationNames_tokenised() throws Exception {
        final String input = "Matusa Inc. was bought by Bayer";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);

        final List<OffsetPosition> positions = target.charPositionsOrganisationNames(tokenisedInput);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(11));
        assertThat(positions.get(0).end, is(11));
    }

    /**
     * Person title
     **/
    @Test
    public void testGetPositionInPersonTitleNames() throws Exception {
        final String input = "The president had a meeting with the vice president, duke and cto of the company.";
        final List<OffsetPosition> positions = target.charPositionsPersonTitle(input);

        assertThat(positions, hasSize(4));
        assertThat(positions.get(0).start, is(4));
        assertThat(positions.get(0).end, is(13));
        assertThat(positions.get(1).start, is(37));
        assertThat(positions.get(1).end, is(51));
        assertThat(positions.get(2).start, is(42));
        assertThat(positions.get(2).end, is(51));
        assertThat(positions.get(3).start, is(53));
        assertThat(positions.get(3).end, is(57));
    }

    @Test
    public void testGetPositionInPersonTitleNames_tokenised() throws Exception {
        final String input = "The president had a meeting with the vice president, duke and cto of the company.";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        final List<OffsetPosition> positions = target.charPositionsPersonTitle(tokenisedInput);

        assertThat(positions, hasSize(4));
        assertThat(positions.get(0).start, is(2));
        assertThat(positions.get(0).end, is(2));
        assertThat(positions.get(1).start, is(14));
        assertThat(positions.get(1).end, is(16));
        assertThat(positions.get(2).start, is(16));
        assertThat(positions.get(2).end, is(16));
        assertThat(positions.get(3).start, is(19));
        assertThat(positions.get(3).end, is(19));
    }

    @Test
    public void testInJournalNamesLayoutToken() {
        String piece = "Greaves M, Lawlor F. Angioedema:  manifestations and management. J Am Acad Dermatol. 1991;25(1 Pt 2):155-161;";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        List<OffsetPosition> positions = target.tokenPositionsJournalNames(tokens);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(18));
        assertThat(positions.get(0).end, is(18));
    }

    @Test
    public void testInAbbrevJournalNamesLayoutToken() {
        String piece = "Greaves M, Lawlor F. Angioedema:  manifestations and management. J Am Acad Dermatol. 1991;25(1 Pt 2):155-161;";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        List<OffsetPosition> positions = target.tokenPositionsAbbrevJournalNames(tokens);

        assertThat(positions, hasSize(2));
        /*for(OffsetPosition position :  positions) {
            System.out.print(position.start + " / " + position.end + ": ");
            for(int j = position.start; j <= position.end; j++)
                System.out.print(tokens.get(j));
            System.out.println("");
        }*/
        assertThat(positions.get(0).start, is(18));
        assertThat(positions.get(0).end, is(18));
        assertThat(positions.get(1).start, is(21));
        assertThat(positions.get(1).end, is(27));
    }

    @Test
    public void testInLocationNamesLayoutToken() {
        String piece = "Academic Press, New York. 1987. Near Porosły-Kolonia.";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        List<OffsetPosition> positions = target.tokenPositionsLocationNames(tokens);

        assertThat(positions, hasSize(4));
        assertThat(positions.get(0).start, is(5));
        assertThat(positions.get(0).end, is(7));
        assertThat(positions.get(1).end, is(7));
        assertThat(positions.get(1).end, is(7));
        assertThat(positions.get(2).end, is(15));
        assertThat(positions.get(2).end, is(15));
        assertThat(positions.get(3).end, is(17));
        assertThat(positions.get(3).end, is(17));
    }

    @Test
    public void testInPublisherNamesLayoutToken() {
        String piece = "Academic Press, New York. 1987. Near Porosły-Kolonia.";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        List<OffsetPosition> positions = target.tokenPositionsPublisherNames(tokens);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(0));
        assertThat(positions.get(0).end, is(2));
    }

    @Test
    public void testInDOIPatternLayoutToken1() {
        String piece = "Garza, K., Goble, C., Brooke, J., & Jay, C. (2015). Framing the community data system interface. "+
        "In Proceedings of the 2015 British HCI Conference on - British HCI ’15. ACM Press. 10.1145/2783446.2783605";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = target.tokenPositionsDOIPattern(tokens, text);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(80));
        assertThat(positions.get(0).end, is(86));
    }

    @Test
    public void testInDOIPatternLayoutToken2() {
        String piece = "Morey, C. C., Cong, Y., Zheng, Y., Price, M., & Morey, R. D. (2015). The color-sharing bonus: Roles of "+
        "perceptual organization and attentive processes in visual working memory. Archives of Scientific Psychology, 3, 18–29. https://doi.org/10.1037/arc0000014";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = target.tokenPositionsDOIPattern(tokens, text);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(104));
        assertThat(positions.get(0).end, is(108));
    }

    @Test
    public void testInArXivPatternLayoutToken1() {
        String piece = "ATLAS collaboration, Measurements of the Nuclear Modification Factor for Jets in Pb+Pb Collisionsat √ "+
        "sNN = 2 . 76TeVwith the ATLAS Detector, Phys. Rev. Lett. 114(2015) 072302 [ arXiv: 1411.2357][INSPIRE] .";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = target.tokenPositionsArXivPattern(tokens, text);

        /*for(OffsetPosition position :  positions) {
            System.out.print(position.start + " / " + position.end + ": ");
            for(int j = position.start; j <= position.end; j++)
                System.out.print(tokens.get(j));
            System.out.println("");
        }*/

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(64));
        assertThat(positions.get(0).end, is(69));
    }

    @Test
    public void testInArXivPatternLayoutToken2() {
        String piece = "O .Suvorova arXiv .org:hep -ph/9911415( 1999).";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = target.tokenPositionsArXivPattern(tokens, text);

        /*for(OffsetPosition position :  positions) {
            System.out.print(position.start + " / " + position.end + ": ");
            for(int j = position.start; j <= position.end; j++)
                System.out.print(tokens.get(j));
            System.out.println("");
        }*/

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(5));
        assertThat(positions.get(0).end, is(15));
    }

    @Test
    public void testInIdentifierPatternLayoutToken() {
        String piece = "ATLAS collaboration, Measurements of the Nuclear Modification Factor for Jets in Pb+Pb Collisionsat √ "+
        "sNN = 2 . 76TeVwith the ATLAS Detector, Phys. Rev. Lett. 114(2015) 072302 [ arXiv: 1411.2357][INSPIRE] .";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        List<OffsetPosition> positions = target.tokenPositionsIdentifierPattern(tokens);

        /*for(OffsetPosition position :  positions) {
            System.out.print(position.start + " / " + position.end + ": ");
            for(int j = position.start; j <= position.end; j++)
                System.out.print(tokens.get(j));
            System.out.println("");
        }*/

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(64));
        assertThat(positions.get(0).end, is(69));
    }

    @Test
    public void testInUrlPatternLayoutToken() {
        String piece = "ATLAS collaboration, . https://doi.org/10.1145/2783446.2783605, https://inria.fr/index.html, http://inria.fr/index.html. " +
            "wikipedia: httpS://en.wikipedia.org/wiki/Reich_(disambiguation), Ftp://pubmed.truc.edu";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        List<OffsetPosition> positions = target.tokenPositionsUrlPattern(tokens);

        assertThat(positions, hasSize(5));
        assertThat(positions.get(0).start, is(7));
        assertThat(positions.get(0).end, is(21));
        assertThat(positions.get(1).start, is(24));
        assertThat(positions.get(1).end, is(34));
        assertThat(positions.get(2).start, is(37));
        assertThat(positions.get(2).end, is(47));
        assertThat(positions.get(3).start, is(53));
        assertThat(positions.get(3).end, is(68));
        assertThat(positions.get(4).start, is(71));
        assertThat(positions.get(4).end, is(79));
    }

    @Test
    public void testInEmailPatternLayoutToken() {
        String piece = "20000 NW Walker Rd, Beaverton, Oregon 97006 \nericwan @ece.ogi.edu, rvdmerwe@ece.ogi.edu \nAbstract \n";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        List<OffsetPosition> positions = target.tokenPositionsEmailPattern(tokens);

        assertThat(positions, hasSize(2));
        assertThat(positions.get(0).start, is(17));
        assertThat(positions.get(0).end, is(24));
        assertThat(positions.get(1).start, is(27));
        assertThat(positions.get(1).end, is(33));
    }

    @Test
    public void testinFunders1Match() throws Exception {
        final String input = "Thank you Deutsche Forschungsgemeinschaft for the money.";
        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
        final List<OffsetPosition> positions = target.tokenPositionsFunderNames(tokenisedInput);
        
        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(4));
        assertThat(positions.get(0).end, is(6));
    }

//    @Test
//    public void testCharacterPositionsUrlPattern_URL_shouldReturnCorrectInterval() throws Exception {
//        final String input = "This work was distributed on http:// github.com/ myUsername/ MyProject";
//        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
//
//        List<OffsetPosition> offsetPositions = target.characterPositionsUrlPattern(tokenisedInput);
//
//        assertThat(offsetPositions, hasSize(1));
//        OffsetPosition FirstURL = offsetPositions.get(0);
//        assertThat(input.substring(FirstURL.start, FirstURL.end), is("http:// github.com/ myUsername/ MyProject"));
//    }
//
//    @Test
//    public void testCharacterPositionsUrlPattern_two_URL_shouldReturnCorrectInterval() throws Exception {
//        final String input = "This work was distributed on http:// github.com/ myUsername/ MyProject. The data is available at https :// github.com/ superconductors/ hola.";
//        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
//
//        List<OffsetPosition> offsetPositions = target.characterPositionsUrlPattern(tokenisedInput);
//
//        assertThat(offsetPositions, hasSize(2));
//        OffsetPosition url = offsetPositions.get(1);
//        assertThat(input.substring(url.start, url.end), is("https :// github.com/ superconductors/ hola"));
//    }
//
//    @Test
//    public void testCharacterPositionsUrlPattern_URL_shouldReturnCorrectInterval_2() throws Exception {
//        final String input = "720 137409 The Government of Lao PDR 2005 Forestry Strategy to the year 2020 of the Lao PDR (available at: https://faolex.fao.org/ docs/pdf/lao144178.pdf)";
//        List<LayoutToken> tokenisedInput = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(input);
//
//        List<OffsetPosition> offsetPositions = target.characterPositionsUrlPattern(tokenisedInput);
//
//        assertThat(offsetPositions, hasSize(1));
//        OffsetPosition url = offsetPositions.get(0);
//        assertThat(input.substring(url.start, url.end), is("https://faolex.fao.org/ docs/pdf/lao144178.pdf"));
//    }

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
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(10, 84.30, 706.68,177.39,9.52));
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

        //This is the actual text that is passed and is different from the layoutToken text.
        final String inputReal = "This work is available at https://github.com/lfoppiano/ supercon2. The repository contains the code of the SuperCon 2 interface, the curation workflow, and the Table 2. Data support, the number of entities for each label in each of the datasets used for evaluating the ML models. The base dataset is the original dataset described in [18], and the curation dataset is automatically collected based on the database corrections by the interface and manually corrected. ";

        PDFAnnotation annotation = new PDFAnnotation();
        annotation.setPageNumber(9);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(9,408.76,537.11,126.54,10.49));
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
        boundingBoxes.add(BoundingBox.fromPointAndDimensions(9,408.76,537.11,126.54,10.49));
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