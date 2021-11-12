package org.grobid.core.data;

import org.grobid.core.main.LibraryLoader;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;

public class BiblioItemTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(BiblioItemTest.class);


    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
    }

    private GrobidAnalysisConfig.GrobidAnalysisConfigBuilder configBuilder = (
        new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
    );

    @BeforeClass
    public static void init() {
        GrobidProperties.getInstance();
    }

    private static Document parseXml(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private static List<String> getXpathStrings(
        Document doc, String xpath_expr
    ) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(xpath_expr);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        ArrayList<String> matchingStrings = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            matchingStrings.add(nodes.item(i).getNodeValue()); 
        }
        return matchingStrings;
    }

    @Test
    public void shouldGenerateRawAffiliationTextIfEnabled() throws Exception {
        GrobidAnalysisConfig config = configBuilder.includeRawAffiliations(true).build();
        Affiliation aff = new Affiliation();
        aff.setRawAffiliationString("raw affiliation 1");
        aff.setFailAffiliation(false);
        Person author = new Person();
        author.setLastName("Smith");
        author.setAffiliations(Arrays.asList(aff));
        BiblioItem biblioItem = new BiblioItem();
        biblioItem.setFullAuthors(Arrays.asList(author));
        biblioItem.setFullAffiliations(Arrays.asList(aff));
        String tei = biblioItem.toTEI(0, 2, config);
        LOGGER.debug("tei: {}", tei);
        Document doc = parseXml(tei);
        assertThat(
            "raw_affiliation",
            getXpathStrings(doc, "//note[@type=\"raw_affiliation\"]/text()"),
            is(Arrays.asList("raw affiliation 1"))
        );
    }

    @Test
    public void shouldIncludeMarkerInRawAffiliationText() throws Exception {
        GrobidAnalysisConfig config = configBuilder.includeRawAffiliations(true).build();
        Affiliation aff = new Affiliation();
        aff.setMarker("A");
        aff.setRawAffiliationString("raw affiliation 1");
        aff.setFailAffiliation(false);
        Person author = new Person();
        author.setLastName("Smith");
        author.setAffiliations(Arrays.asList(aff));
        BiblioItem biblioItem = new BiblioItem();
        biblioItem.setFullAuthors(Arrays.asList(author));
        biblioItem.setFullAffiliations(Arrays.asList(aff));
        String tei = biblioItem.toTEI(0, 2, config);
        LOGGER.debug("tei: {}", tei);
        Document doc = parseXml(tei);
        assertThat(
            "raw_affiliation label",
            getXpathStrings(doc, "//note[@type=\"raw_affiliation\"]/label/text()"),
            is(Arrays.asList("A"))
        );
        assertThat(
            "raw_affiliation",
            getXpathStrings(doc, "//note[@type=\"raw_affiliation\"]/text()"),
            is(Arrays.asList(" raw affiliation 1"))
        );
    }

    @Test
    public void shouldIncludeEscapedMarkerInRawAffiliationText() throws Exception {
        GrobidAnalysisConfig config = configBuilder.includeRawAffiliations(true).build();
        Affiliation aff = new Affiliation();
        aff.setMarker("&");
        aff.setRawAffiliationString("raw affiliation 1");
        aff.setFailAffiliation(false);
        Person author = new Person();
        author.setLastName("Smith");
        author.setAffiliations(Arrays.asList(aff));
        BiblioItem biblioItem = new BiblioItem();
        biblioItem.setFullAuthors(Arrays.asList(author));
        biblioItem.setFullAffiliations(Arrays.asList(aff));
        String tei = biblioItem.toTEI(0, 2, config);
        LOGGER.debug("tei: {}", tei);
        Document doc = parseXml(tei);
        assertThat(
            "raw_affiliation label",
            getXpathStrings(doc, "//note[@type=\"raw_affiliation\"]/label/text()"),
            is(Arrays.asList("&"))
        );
        assertThat(
            "raw_affiliation",
            getXpathStrings(doc, "//note[@type=\"raw_affiliation\"]/text()"),
            is(Arrays.asList(" raw affiliation 1"))
        );
    }

    @Test
    public void shouldGenerateRawAffiliationTextForFailAffiliationsIfEnabled() throws Exception {
        GrobidAnalysisConfig config = configBuilder.includeRawAffiliations(true).build();
        Affiliation aff = new Affiliation();
        aff.setRawAffiliationString("raw affiliation 1");
        aff.setFailAffiliation(true);
        BiblioItem biblioItem = new BiblioItem();
        biblioItem.setFullAffiliations(Arrays.asList(aff));
        String tei = biblioItem.toTEI(0, 2, config);
        LOGGER.debug("tei: {}", tei);
        Document doc = parseXml(tei);
        assertThat(
            "raw_affiliation",
            getXpathStrings(doc, "//note[@type=\"raw_affiliation\"]/text()"),
            is(Arrays.asList("raw affiliation 1"))
        );
    }

    @Test
    public void shouldNotGenerateRawAffiliationTextIfNotEnabled() throws Exception {
        GrobidAnalysisConfig config = configBuilder.includeRawAffiliations(false).build();
        Affiliation aff = new Affiliation();
        aff.setRawAffiliationString("raw affiliation 1");
        Person author = new Person();
        author.setLastName("Smith");
        author.setAffiliations(Arrays.asList(aff));
        BiblioItem biblioItem = new BiblioItem();
        biblioItem.setFullAuthors(Arrays.asList(author));
        biblioItem.setFullAffiliations(Arrays.asList(aff));
        String tei = biblioItem.toTEI(0, 2, config);
        LOGGER.debug("tei: {}", tei);
        Document doc = parseXml(tei);
        assertThat(
            "raw_affiliation",
            getXpathStrings(doc, "//note[@type=\"raw_affiliation\"]/text()"),
            is(empty())
        );
    }

    @Test
    public void injectIdentifiers() {
        BiblioItem item1 = new BiblioItem();
        item1.setDOI("10.1233/23232/3232");
        item1.setPMID("pmid");
        item1.setPMCID("bao");
        item1.setPII("miao");
        item1.setIstexId("zao");
        item1.setArk("Noah!");

        BiblioItem item2 = new BiblioItem();
        BiblioItem.injectIdentifiers(item2, item1);

        assertThat(item2.getDOI(), is("10.1233/23232/3232"));
        assertThat(item2.getPMID(), is("pmid"));
        assertThat(item2.getPMCID(), is("bao"));
        assertThat(item2.getPII(), is("miao"));
        assertThat(item2.getIstexId(), is("zao"));
        assertThat(item2.getArk(), is("Noah!"));
    }

    @Test
    public void shouldEscapeIdentifiers() throws Exception {
        BiblioItem item1 = new BiblioItem();
        item1.setJournal("Dummy Journal Title");
        item1.setDOI("10.1233/23232&3232");
        item1.setPMID("pmid & 123");
        item1.setArk("Noah & !");
        item1.setISSN("0974&9756");

        GrobidAnalysisConfig config = configBuilder.build();
        String tei = item1.toTEI(0, 2, config);
        LOGGER.debug("tei: {}", tei);
        Document doc = parseXml(tei);
        assertThat(
            "DOI",
            getXpathStrings(doc, "//idno[@type=\"DOI\"]/text()"),
            is(Arrays.asList("10.1233/23232&3232"))
        );
        assertThat(
            "ISSN",
            getXpathStrings(doc, "//idno[@type=\"ISSN\"]/text()"),
            is(Arrays.asList("0974&9756"))
        );
        assertThat(
            "PMID",
            getXpathStrings(doc, "//idno[@type=\"PMID\"]/text()"),
            is(Arrays.asList("pmid&123"))
        );
        assertThat(
            "Ark",
            getXpathStrings(doc, "//idno[@type=\"ark\"]/text()"),
            is(Arrays.asList("Noah & !"))
        );

    }

    @Test
    public void correct_empty_shouldNotFail() {
        BiblioItem.correct(new BiblioItem(), new BiblioItem());
    }


    @Test
    public void correct_1author_shouldWork() {
        BiblioItem biblio1 = new BiblioItem();
        List<Person> authors = new ArrayList<>();
        authors.add(createPerson("John", "Doe"));
        biblio1.setFullAuthors(authors);

        BiblioItem biblio2 = new BiblioItem();
        authors = new ArrayList<>();
        authors.add(createPerson("John1", "Doe"));
        biblio2.setFullAuthors(authors);

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
    }

    @Test
    public void correct_2authors_shouldMatchFullName_shouldUpdateAffiliation() {
        BiblioItem biblio1 = new BiblioItem();
        List<Person> authors = new ArrayList<>();
        authors.add(createPerson("John", "Doe"));
        authors.add(createPerson("Jane", "Will"));
        biblio1.setFullAuthors(authors);

        BiblioItem biblio2 = new BiblioItem();
        authors = new ArrayList<>();
        authors.add(createPerson("John", "Doe", "UCLA"));
        authors.add(createPerson("Jane", "Will","Harward"));
        biblio2.setFullAuthors(authors);

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors(), hasSize(2));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
        // biblio1 affiliations empty we update them with ones from biblio2
        assertThat(biblio1.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString(), is(biblio2.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString()));
        assertThat(biblio1.getFullAuthors().get(1).getFirstName(), is(biblio2.getFullAuthors().get(1).getFirstName()));
        assertThat(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString(), is(biblio2.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString()));
    }

    @Test
    public void correct_2authors_shouldMatchFullName_shouldKeepAffiliation() {
        BiblioItem biblio1 = new BiblioItem();
        List<Person> authors = new ArrayList<>();
        authors.add(createPerson("John", "Doe", "Stanford"));
        authors.add(createPerson("Jane", "Will", "Cambridge"));
        biblio1.setFullAuthors(authors);

        BiblioItem biblio2 = new BiblioItem();
        authors = new ArrayList<>();
        authors.add(createPerson("John", "Doe" ));
        authors.add(createPerson("Jane", "Will", "UCLA"));
        biblio2.setFullAuthors(authors);

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors(), hasSize(2));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
        // biblio1 affiliations not empty, we keep biblio1 as is
        assertThat(biblio1.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString(), is(biblio1.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString()));
        assertThat(biblio1.getFullAuthors().get(1).getFirstName(), is(biblio2.getFullAuthors().get(1).getFirstName()));
        assertThat(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString(), is(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString()));
        assertThat(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString(), is(biblio2.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString()));
    }

    @Test
    public void correct_2authors_initial_2_shouldUpdateAuthor() {
        BiblioItem biblio1 = new BiblioItem();
        List<Person> authors = new ArrayList<>();
        authors.add(createPerson("John", "Doe", "ULCA"));
        authors.add(createPerson("J", "Will", "Harward"));
        biblio1.setFullAuthors(authors);

        BiblioItem biblio2 = new BiblioItem();
        authors = new ArrayList<>();
        authors.add(createPerson("John1", "Doe", "Stanford"));
        authors.add(createPerson("Jane", "Will", "Berkeley"));
        biblio2.setFullAuthors(authors);

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors(), hasSize(2));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
        // affiliation should be kept though since not empty
        assertThat(biblio1.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString(), is(biblio1.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString()));
        assertThat(biblio1.getFullAuthors().get(1).getFirstName(), is(biblio2.getFullAuthors().get(1).getFirstName()));
        assertThat(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString(), is(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString()));
    }

    @Test
    public void correct_2authors_initial_shouldUpdateAuthor() {
        BiblioItem biblio1 = new BiblioItem();
        List<Person> authors = new ArrayList<>();
        authors.add(createPerson("John", "Doe", "ULCA"));
        authors.add(createPerson("Jane", "Will", "Harward"));
        biblio1.setFullAuthors(authors);

        BiblioItem biblio2 = new BiblioItem();
        authors = new ArrayList<>();
        authors.add(createPerson("John1", "Doe", "Stanford"));
        authors.add(createPerson("J", "Will", "Berkeley"));
        biblio2.setFullAuthors(authors);

        BiblioItem.correct(biblio1, biblio2);

        assertThat(biblio1.getFirstAuthorSurname(), is(biblio2.getFirstAuthorSurname()));
        assertThat(biblio1.getFullAuthors(), hasSize(2));
        assertThat(biblio1.getFullAuthors().get(0).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
        // affiliation should be kept though
        assertThat(biblio1.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString(), is(biblio1.getFullAuthors().get(0).getAffiliations().get(0).getAffiliationString()));
        //assertThat(biblio1.getFullAuthors().get(1).getFirstName(), is(biblio2.getFullAuthors().get(0).getFirstName()));
        assertThat(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString(), is(biblio1.getFullAuthors().get(1).getAffiliations().get(0).getAffiliationString()));
    }

    @Test
    public void testCleanDOIxPrefix1_shouldRemovePrefix() throws Exception {

        String doi = "doi:10.1063/1.1905789";
        String cleanDoi = BiblioItem.cleanDOI(doi);

        assertThat(cleanDoi, Matchers.is("10.1063/1.1905789"));
    }

    @Test
    public void testCleanDOIPrefix2_shouldRemovePrefix() throws Exception {

        String doi = "doi/10.1063/1.1905789";
        String cleanDoi = BiblioItem.cleanDOI(doi);

        assertThat(cleanDoi, Matchers.is("10.1063/1.1905789"));
    }

    @Test
    public void testCleanDOI_cleanCommonExtractionPatterns() throws Exception {
        String doi = "43-61.DOI:10.1093/jpepsy/14.1.436/7";
        String cleanedDoi = BiblioItem.cleanDOI(doi);

        assertThat(cleanedDoi, is("10.1093/jpepsy/14.1.436/7"));
    }

    @Test
    public void testCleanDOI_removeURL_http() throws Exception {

        String doi = "http://doi.org/10.1063/1.1905789";
        String cleanDoi = BiblioItem.cleanDOI(doi);

        assertThat(cleanDoi, Matchers.is("10.1063/1.1905789"));
    }

    @Test
    public void testCleanDOI_removeURL_https() throws Exception {

        String doi = "https://doi.org/10.1063/1.1905789";
        String cleanDoi = BiblioItem.cleanDOI(doi);

        assertThat(cleanDoi, Matchers.is("10.1063/1.1905789"));
    }

    @Test
    public void testCleanDOI_removeURL_file() throws Exception {

        String doi = "file://doi.org/10.1063/1.1905789";
        String cleanDoi = BiblioItem.cleanDOI(doi);

        assertThat(cleanDoi, Matchers.is("10.1063/1.1905789"));
    }

    @Test
    public void testCleanDOI_diactric() throws Exception {
        String doi = "10.1063/1.1905789͔";

        String cleanDoi = BiblioItem.cleanDOI(doi);

        assertThat(cleanDoi, Matchers.is("10.1063/1.1905789"));
    }

    private Person createPerson(String firstName, String secondName) {
        final Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(secondName);
        return person;
    }

    private Person createPerson(String firstName, String secondName, String affiliation) {
        final Person person = createPerson(firstName, secondName);
        final Affiliation affiliation1 = new Affiliation();
        affiliation1.setAffiliationString(affiliation);
        List<Affiliation> affiliations = new ArrayList<>();
        affiliations.add(affiliation1);
        person.setAffiliations(affiliations);
        return person;
    }
}
