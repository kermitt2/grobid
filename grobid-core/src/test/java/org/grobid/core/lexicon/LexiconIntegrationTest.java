package org.grobid.core.lexicon;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.layout.LayoutToken;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;

/**
 * @author Patrice Lopez
 */
public class LexiconIntegrationTest {
    private Lexicon target = null;

    @BeforeClass
    public static void setInitialContext() throws Exception {
        MockContext.setInitialContext();
    }

    @AfterClass
    public static void destroyInitialContext() throws Exception {
        MockContext.destroyInitialContext();
    }

    @Before
    public void setUp() {
        target = Lexicon.getInstance();
    }

    // journals
    @Test
    public void testInAbbrevJournalNames_case1() throws Exception {
        String input = "Nature";
        List<OffsetPosition> journalsPositions = target.inAbbrevJournalNames(input);

        assertNotNull(journalsPositions);
        assertThat(journalsPositions, hasSize(1));
        assertThat(journalsPositions.get(0).start, is(0));
    }


    @Test
    public void testInAbbrevJournalNames_case2() throws Exception {
        String input = "in Nature, volume";
        List<OffsetPosition> journalsPositions = target.inAbbrevJournalNames(input);

        assertNotNull(journalsPositions);
        assertThat(journalsPositions, hasSize(1));
        assertThat(journalsPositions.get(0).start, is(1));
    }

    @Test
    public void testJournalNames_case1() throws Exception {
        String input = "Taylor, et al., Nature 297:(1982)";
        List<OffsetPosition> journalsPositions = target.inJournalNames(input);

        assertNotNull(journalsPositions);
        assertThat(journalsPositions, hasSize(1));
        assertThat(journalsPositions.get(0).start, is(6));
        assertThat(journalsPositions.get(0).end, is(6));
    }

    @Test
    public void testJournalNames_case2() throws Exception {
        String input = "to be published in the official publication of the National Venereology Council " +
                "of Australia, volume 10, 2010.";
        List<OffsetPosition> journalsPositions = target.inJournalNames(input);

        assertNotNull(journalsPositions);
        assertThat(journalsPositions, hasSize(2));
    }

    @Test
    public void testCity() throws Exception {
        String input = "University of New-York, USA, bla bla City, bla";
        List<OffsetPosition> citiesPositions = target.inCityNames(input);

        assertNotNull(citiesPositions);
        assertThat(citiesPositions, hasSize(2));
    }

    @Test
    public void testInJournalNames() throws Exception {
        List<OffsetPosition> inJournalNames = target.inJournalNames("abc <p> Economics </p>");

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
        final List<OffsetPosition> positions = target.getPositionsInLocationNames(input);

        assertThat(positions, hasSize(15));
        assertThat(positions.get(0).start, is(0));
        assertThat(positions.get(0).end, is(2));
    }

    @Test
    public void testGetPositionInLocation_case1_tokenised() throws Exception {
        String input = "In retrospect, the National Archives of Belgium were established by the French law of October 26th 1796 (5 Brumair V), which, amongst others, foresaw in the organisation of departmental depots (amongst others, in Brussels), in which the archives of the disbanded institutions of the Ancien Régime would be stored.";
        List<String> tokenisedInput = GrobidAnalyzer.getInstance().tokenize(input);

        final List<OffsetPosition> positions = target.getPositionsInLocationNames(tokenisedInput);

        assertThat(positions, hasSize(15));
        assertThat(positions.get(0).start, is(0));
        assertThat(positions.get(0).end, is(0));
    }

    @Test
    public void testGetPositionsInLocation_case2() throws Exception {
        final String input = "I'm walking in The Bronx";
        final List<OffsetPosition> positions = target.getPositionsInLocationNames(input);

        assertThat(positions, hasSize(4));
        assertThat(positions.get(3).start, is(19));
        assertThat(positions.get(3).end, is(24));
    }

    @Test
    public void testGetPositionsInLocation_case2_tokenised() throws Exception {
        final String input = "I'm walking in The Bronx";
        List<String> tokenisedInput = GrobidAnalyzer.getInstance().tokenize(input);
        final List<OffsetPosition> positions = target.getPositionsInLocationNames(tokenisedInput);

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
        final List<OffsetPosition> positions = target.getPositionsInOrgFormNames(input);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(7));
        assertThat(positions.get(0).end, is(10));
    }

    @Test
    public void testGetPositionInOrgForm_tokenised() throws Exception {
        final String input = "Matusa Inc. was bought by Bayer";
        List<String> tokenisedInput = GrobidAnalyzer.getInstance().tokenize(input);

        final List<OffsetPosition> positions = target.getPositionsInOrgFormNames(tokenisedInput);

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
        final List<OffsetPosition> positions = target.getPositionsInOrganisationNames(input);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(26));
        assertThat(positions.get(0).end, is(31));
    }

    @Test
    public void testGetPositionInOrganisationNames_tokenised() throws Exception {
        final String input = "Matusa Inc. was bought by Bayer";
        List<String> tokenisedInput = GrobidAnalyzer.getInstance().tokenize(input);

        final List<OffsetPosition> positions = target.getPositionsInOrganisationNames(tokenisedInput);

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
        final List<OffsetPosition> positions = target.getPositionsInPersonTitleNames(input);

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
        List<String> tokenisedInput = GrobidAnalyzer.getInstance().tokenize(input);
        final List<OffsetPosition> positions = target.getPositionsInPersonTitleNames(tokenisedInput);

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
        List<OffsetPosition> positions = target.inJournalNamesLayoutToken(tokens);
        
        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(18));
        assertThat(positions.get(0).end, is(18));
    }

    @Test
    public void testInAbbrevJournalNamesLayoutToken() {
        String piece = "Greaves M, Lawlor F. Angioedema:  manifestations and management. J Am Acad Dermatol. 1991;25(1 Pt 2):155-161;";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        List<OffsetPosition> positions = target.inAbbrevJournalNamesLayoutToken(tokens);
        
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
        List<OffsetPosition> positions = target.inLocationNamesLayoutToken(tokens);
        
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
        List<OffsetPosition> positions = target.inPublisherNamesLayoutToken(tokens);

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
        List<OffsetPosition> positions = target.inDOIPatternLayoutToken(tokens, text);

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
        List<OffsetPosition> positions = target.inDOIPatternLayoutToken(tokens, text);

        assertThat(positions, hasSize(1));
        assertThat(positions.get(0).start, is(104));
        assertThat(positions.get(0).end, is(108));
    }   

    //@Test
    public void testInArXivPatternLayoutToken() {
        String piece = "ATLAS collaboration, Measurements of the Nuclear Modification Factor for Jets in Pb+Pb Collisionsat √ "+
        "sNN = 2 . 76TeVwith the ATLAS Detector, Phys. Rev. Lett. 114(2015) 072302 [ arXiv: 1411.2357][INSPIRE] .";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> positions = target.inArXivPatternLayoutToken(tokens, text);

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
    public void testInIdentifierPatternLayoutToken() {
        String piece = "ATLAS collaboration, Measurements of the Nuclear Modification Factor for Jets in Pb+Pb Collisionsat √ "+
        "sNN = 2 . 76TeVwith the ATLAS Detector, Phys. Rev. Lett. 114(2015) 072302 [ arXiv: 1411.2357][INSPIRE] .";
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(piece);
        List<OffsetPosition> positions = target.inIdentifierPatternLayoutToken(tokens);

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
}