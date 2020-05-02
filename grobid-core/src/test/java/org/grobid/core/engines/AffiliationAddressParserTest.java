package org.grobid.core.engines;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Affiliation;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.features.FeaturesVectorAffiliationAddress;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.LayoutTokensUtil;

public class AffiliationAddressParserTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(AffiliationAddressParserTest.class);

    private static boolean NO_USE_PRELABEL = false;
    private static List<List<OffsetPosition>> NO_PLACES_POSITIONS = Arrays.asList(
        Collections.emptyList()
    );

    private AffiliationAddressParser target;
    private GrobidAnalyzer analyzer;

    @Before
    public void setUp() throws Exception {
        this.target = new AffiliationAddressParser();
        this.analyzer = GrobidAnalyzer.getInstance();
    }

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
        GrobidProperties.getInstance();
    }

    @AfterClass
    public static void tearDown() {
        GrobidFactory.reset();
    }

    @Test
    public void shouldNotFailOnEmptyLabelResult() throws Exception {
        String labelResult = "";
        List<LayoutToken> tokenizations = Collections.emptyList();
        List<Affiliation> result = this.target.resultBuilder(
            labelResult,
            tokenizations,
            NO_USE_PRELABEL
        );
        assertThat("affiliations should be null", result, is(nullValue()));
    }
    private static List<String> getAffiliationBlocksWithLineFeed(List<LayoutToken> tokenizations) {
        ArrayList<String> affiliationBlocks = new ArrayList<String>();
        for(LayoutToken tok : tokenizations) {
            if (tok.getText().length() == 0) continue;
            if (!tok.getText().equals(" ")) {
                if (tok.getText().equals("\n")) {
                    affiliationBlocks.add("@newline");
                } else
                    affiliationBlocks.add(tok + " <affiliation>");
            }
        }
        return affiliationBlocks;
    }

    private static String addLabelsToFeatures(String header, List<String> labels) {
        String[] headerLines = header.split("\n");
        if (headerLines.length != labels.size()) {
            throw new IllegalArgumentException(String.format(
                "number of header lines and labels must match, %d != %d",
                headerLines.length, labels.size()
            ));
        }
        ArrayList<String> resultLines = new ArrayList<>(headerLines.length);
        for (int i = 0; i < headerLines.length; i++) {
            resultLines.add(headerLines[i] + " " + labels.get(i));
        }
        return Joiner.on("\n").join(resultLines);
    }

    private List<Affiliation> processLabelResults(
        List<String> tokens,
        List<String> labels
    ) throws Exception {
        List<LayoutToken> tokenizations = LayoutTokensUtil.getLayoutTokensForTokenizedText(tokens);
        LOGGER.debug("tokenizations: {}", tokenizations);
        List<String> affiliationBlocks = getAffiliationBlocksWithLineFeed(tokenizations);
        String header = FeaturesVectorAffiliationAddress.addFeaturesAffiliationAddress(
            affiliationBlocks, Arrays.asList(tokenizations), NO_PLACES_POSITIONS
        );
        LOGGER.debug("header: {}", header);
        String labelResult = addLabelsToFeatures(header, labels);
        LOGGER.debug("labelResult: {}", labelResult);
        return this.target.resultBuilder(
            labelResult,
            tokenizations,
            NO_USE_PRELABEL
        );
    }

    private List<Affiliation> processLabelResults(String[][] tokenLabelPairs) throws Exception {
        ArrayList<String> tokens = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        boolean prevWhitespace = false;
        for (String[] pair: tokenLabelPairs) {
            if (!tokens.isEmpty() && (!prevWhitespace)) {
                tokens.add(" ");
            }
            prevWhitespace = pair[0].trim().isEmpty();
            tokens.add(pair[0]);
            if (pair.length > 1) {
                labels.add(pair[1]);
            }
        }
        return this.processLabelResults(tokens, labels);
    }

    @Test
    public void shouldExtractSimpleAffiliation() throws Exception {
        List<Affiliation> affiliations = this.processLabelResults(new String[][] {
            {"1", "I-<marker>"},
            {"University", "I-<institution>"},
            {"of", "<institution>"},
            {"Science", "<institution>"}
        });
        assertThat("should have one affiliation", affiliations, is(hasSize(1)));
        Affiliation affiliation = affiliations.get(0);
        assertThat("institution.marker", affiliation.getMarker(), is("1"));
        assertThat(
            "institution.institutions",
            affiliation.getInstitutions(),
            is(Arrays.asList("University of Science"))
        );
        assertThat(
            "institution.rawAffiliationString",
            affiliation.getRawAffiliationString(),
            is("University of Science")
        );
    }

    @Test
    public void shouldExtractMultipleInstitutions() throws Exception {
        List<Affiliation> affiliations = this.processLabelResults(new String[][] {
            {"1", "I-<marker>"},
            {"University", "I-<institution>"},
            {"of", "<institution>"},
            {"Science", "<institution>"},
            {"University", "I-<institution>"},
            {"of", "<institution>"},
            {"Madness", "<institution>"}
        });
        assertThat("should have one affiliation", affiliations, is(hasSize(1)));
        Affiliation affiliation = affiliations.get(0);
        assertThat("institution.marker", affiliation.getMarker(), is("1"));
        assertThat(
            "institution.institutions",
            affiliation.getInstitutions(),
            is(Arrays.asList("University of Science", "University of Madness"))
        );
        assertThat(
            "institution.rawAffiliationString",
            affiliation.getRawAffiliationString(),
            is("University of Science University of Madness")
        );
    }

    @Test
    public void shouldExtractSecondInstitutionAsSeparateAffiliationIfNewLine() throws Exception {
        List<Affiliation> affiliations = this.processLabelResults(new String[][] {
            {"1", "I-<marker>"},
            {"University", "I-<institution>"},
            {"of", "<institution>"},
            {"Science", "<institution>"},
            {"\n"},
            {"University", "I-<institution>"},
            {"of", "<institution>"},
            {"Madness", "<institution>"}
        });
        assertThat("should have one affiliation", affiliations, is(hasSize(2)));
        assertThat("(0).institution.marker", affiliations.get(0).getMarker(), is("1"));
        assertThat(
            "(0).institution.institutions",
            affiliations.get(0).getInstitutions(),
            is(Arrays.asList("University of Science"))
        );
        assertThat(
            "(0).institution.rawAffiliationString",
            affiliations.get(0).getRawAffiliationString(),
            is("University of Science")
        );
        assertThat("(1).institution.marker", affiliations.get(1).getMarker(), is("1"));
        assertThat(
            "(1).institution.institutions",
            affiliations.get(1).getInstitutions(),
            is(Arrays.asList("University of Madness"))
        );
        assertThat(
            "(1).institution.rawAffiliationString",
            affiliations.get(1).getRawAffiliationString(),
            is("University of Madness")
        );
    }

    @Test
    public void shouldExtractMultipleAffiliations() throws Exception {
        List<Affiliation> affiliations = this.processLabelResults(new String[][] {
            {"1", "I-<marker>"},
            {"University", "I-<institution>"},
            {"of", "<institution>"},
            {"Science", "<institution>"},
            {"2", "I-<marker>"},
            {"University", "I-<institution>"},
            {"of", "<institution>"},
            {"Madness", "<institution>"}
        });
        assertThat("should have one affiliation", affiliations, is(hasSize(2)));
        assertThat("institution.marker", affiliations.get(0).getMarker(), is("1"));
        assertThat(
            "institution.institutions",
            affiliations.get(0).getInstitutions(),
            is(Arrays.asList("University of Science"))
        );
        assertThat(
            "institution.rawAffiliationString",
            affiliations.get(0).getRawAffiliationString(),
            is("University of Science")
        );
        assertThat("institution.marker", affiliations.get(1).getMarker(), is("2"));
        assertThat(
            "institution.institutions",
            affiliations.get(1).getInstitutions(),
            is(Arrays.asList("University of Madness"))
        );
        assertThat(
            "institution.rawAffiliationString",
            affiliations.get(1).getRawAffiliationString(),
            is("University of Madness")
        );
    }
}
