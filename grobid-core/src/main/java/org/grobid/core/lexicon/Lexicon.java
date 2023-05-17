package org.grobid.core.lexicon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.PDFAnnotation;
import org.grobid.core.sax.CountryCodeSaxParser;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Utilities;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing all the lexical resources.
 *
 */
public class Lexicon {
	private static final Logger LOGGER = LoggerFactory.getLogger(Lexicon.class);
    // private static volatile Boolean instanceController = false;
    private static volatile Lexicon instance;

    private Set<String> dictionary_en = null;
    private Set<String> dictionary_de = null;
    private Set<String> lastNames = null;
    private Set<String> firstNames = null;
    private Map<String, String> countryCodes = null;
    private Set<String> countries = null;

    private FastMatcher abbrevJournalPattern = null;
    private FastMatcher conferencePattern = null;
    private FastMatcher publisherPattern = null;
    private FastMatcher journalPattern = null;
    private FastMatcher cityPattern = null;
	private FastMatcher organisationPattern = null;
	private FastMatcher locationPattern = null;
	
	private FastMatcher orgFormPattern = null;
    private FastMatcher collaborationPattern = null;

    private FastMatcher personTitlePattern = null;
	private FastMatcher personSuffixPattern = null;

    public static Lexicon getInstance() {
        if (instance == null) {
            synchronized (Lexicon.class) {
                if (instance == null) {
					getNewInstance();
                }
            }
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
	private static synchronized void getNewInstance() {
		LOGGER.debug("Get new instance of Lexicon");
		GrobidProperties.getInstance();
		instance = new Lexicon();
	}

    /**
     * Hidden constructor
     */
    private Lexicon() {
        initDictionary();
        initNames();
		// the loading of the journal and conference names is lazy
        addDictionary(GrobidProperties.getGrobidHomePath() + File.separator + 
			"lexicon"+File.separator+"wordforms"+File.separator+"english.wf", Language.EN);
        addDictionary(GrobidProperties.getGrobidHomePath() + File.separator + 
			"lexicon"+File.separator+"wordforms"+File.separator+"german.wf", Language.EN);
        addLastNames(GrobidProperties.getGrobidHomePath() + File.separator +
			"lexicon"+File.separator+"names"+File.separator+"names.family");
		addLastNames(GrobidProperties.getGrobidHomePath() + File.separator +
			"lexicon"+File.separator+"names"+File.separator+"lastname.5k");
        addFirstNames(GrobidProperties.getGrobidHomePath() + File.separator + 
			"lexicon"+File.separator+"names"+File.separator+"names.female");
        addFirstNames(GrobidProperties.getGrobidHomePath() + File.separator + 
			"lexicon"+File.separator+"names"+File.separator+"names.male");
		addFirstNames(GrobidProperties.getGrobidHomePath() + File.separator + 
			"lexicon"+File.separator+"names"+File.separator+"firstname.5k");
        initCountryCodes();
        addCountryCodes(GrobidProperties.getGrobidHomePath() + File.separator +
            "lexicon"+File.separator+"countries"+File.separator+"CountryCodes.xml");
    }

    private void initDictionary() {
    	LOGGER.info("Initiating dictionary");
        dictionary_en = new HashSet<>();
        dictionary_de = new HashSet<>();
        LOGGER.info("End of Initialization of dictionary");
    }

    public final void addDictionary(String path, String lang) {
        File file = new File(path);
        if (!file.exists()) {
            throw new GrobidResourceException("Cannot add entries to dictionary (language '" + lang +
                    "'), because file '" + file.getAbsolutePath() + "' does not exists.");
        }
        if (!file.canRead()) {
            throw new GrobidResourceException("Cannot add entries to dictionary (language '" + lang +
                    "'), because cannot read file '" + file.getAbsolutePath() + "'.");
        }
        InputStream ist = null;
        InputStreamReader isr = null;
        BufferedReader dis = null;
        try {
            ist = new FileInputStream(file);
            isr = new InputStreamReader(ist, "UTF8");
            dis = new BufferedReader(isr);

            String l = null;
            while ((l = dis.readLine()) != null) {
                if (l.length() == 0) continue;
                // the first token, separated by a tabulation, gives the word form
                if (lang.equals(Language.EN)) {
                    // multext format
                    StringTokenizer st = new StringTokenizer(l, "\t");
                    if (st.hasMoreTokens()) {
                        String word = st.nextToken();
                        if (!dictionary_en.contains(word))
                            dictionary_en.add(word);
                    }
                } else if (lang.equals(Language.DE)) {
                    // celex format
                    StringTokenizer st = new StringTokenizer(l, "\\");
                    if (st.hasMoreTokens()) {
                        st.nextToken(); // id
                        String word = st.nextToken();
                        word = word.replace("\"a", "ä");
                        word = word.replace("\"u", "ü");
                        word = word.replace("\"o", "ö");
                        word = word.replace("$", "ß");
                        if (!dictionary_de.contains(word))
                            dictionary_de.add(word);
                    }
                }
            }
        } catch (IOException e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        } finally {
            IOUtils.closeQuietly(ist, isr, dis);
        }
    }

    public boolean isCountry(String tok) {
        return countries.contains(tok.toLowerCase());
    }

    private void initNames() {
    	LOGGER.info("Initiating names");
        firstNames = new HashSet<String>();
        lastNames = new HashSet<String>();
        LOGGER.info("End of initialization of names");
    }

    private void initCountryCodes() {
    	LOGGER.info("Initiating country codes");
        countryCodes = new HashMap<String, String>();
        countries = new HashSet<String>();
        LOGGER.info("End of initialization of country codes");
    }

    private void addCountryCodes(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new GrobidResourceException("Cannot add country codes to dictionary, because file '" +
                    file.getAbsolutePath() + "' does not exists.");
        }
        if (!file.canRead()) {
            throw new GrobidResourceException("Cannot add country codes to dictionary, because cannot read file '" +
                    file.getAbsolutePath() + "'.");
        }
        InputStream ist = null;
        //InputStreamReader isr = null;
        //BufferedReader dis = null;
        try {
            ist = new FileInputStream(file);
            CountryCodeSaxParser parser = new CountryCodeSaxParser(countryCodes, countries);
            SAXParserFactory spf = SAXParserFactory.newInstance();
            //get a new instance of parser
            SAXParser p = spf.newSAXParser();
            p.parse(ist, parser);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {

            try {
                if (ist != null)
                    ist.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close all streams.", e);
            }
        }
    }

    public String getCountryCode(String country) {
        String code = (String) countryCodes.get(country.toLowerCase());
        return code;
    }

    public final void addFirstNames(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new GrobidResourceException("Cannot add first names to dictionary, because file '" +
                    file.getAbsolutePath() + "' does not exists.");
        }
        if (!file.canRead()) {
            throw new GrobidResourceException("Cannot add first names to dictionary, because cannot read file '" +
                    file.getAbsolutePath() + "'.");
        }
        InputStream ist = null;
        BufferedReader dis = null;
        try {
            ist = new FileInputStream(file);
            dis = new BufferedReader(new InputStreamReader(ist, "UTF8"));

            String l = null;
            while ((l = dis.readLine()) != null) {
                // read the line
                // the first token, separated by a tabulation, gives the word form
                StringTokenizer st = new StringTokenizer(l, "\t\n-");
                if (st.hasMoreTokens()) {
                    String word = st.nextToken().toLowerCase().trim();
                    if (!firstNames.contains(word)) {
                        firstNames.add(word);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } catch (IOException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            try {
                if (ist != null)
                    ist.close();
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close all streams.", e);
            }
        }
    }

    public final void addLastNames(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new GrobidResourceException("Cannot add last names to dictionary, because file '" +
                    file.getAbsolutePath() + "' does not exists.");
        }
        if (!file.canRead()) {
            throw new GrobidResourceException("Cannot add last names to dictionary, because cannot read file '" +
                    file.getAbsolutePath() + "'.");
        }
        InputStream ist = null;
        BufferedReader dis = null;
        try {
            ist = new FileInputStream(file);
            dis = new BufferedReader(new InputStreamReader(ist, "UTF8"));

            String l = null;
            while ((l = dis.readLine()) != null) {
                // read the line
                // the first token, separated by a tabulation, gives the word form
                StringTokenizer st = new StringTokenizer(l, "\t\n-");
                if (st.hasMoreTokens()) {
                    String word = st.nextToken().toLowerCase().trim();
                    if (!lastNames.contains(word)) {
                        lastNames.add(word);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } catch (IOException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            try {
                if (ist != null)
                    ist.close();
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close all streams.", e);
            }
        }
    }

    /**
     * Lexical look-up, default is English
     * @param s a string to test
     * @return true if in the dictionary
     */
    public boolean inDictionary(String s) {
        return inDictionary(s, Language.EN);
    }

    public boolean inDictionary(String s, String lang) {
        if (s == null)
            return false;
        if ((s.endsWith(".")) | (s.endsWith(",")) | (s.endsWith(":")) | (s.endsWith(";")) | (s.endsWith(".")))
            s = s.substring(0, s.length() - 1);
        int i1 = s.indexOf('-');
        int i2 = s.indexOf(' ');
        if (i1 != -1) {
            String s1 = s.substring(0, i1);
            String s2 = s.substring(i1 + 1, s.length());
            if (lang.equals(Language.DE)) {
                if ((dictionary_de.contains(s1)) & (dictionary_de.contains(s2)))
                    return true;
                else
                    return false;
            } else {
                if ((dictionary_en.contains(s1)) & (dictionary_en.contains(s2)))
                    return true;
                else
                    return false;
            }
        }
        if (i2 != -1) {
            String s1 = s.substring(0, i2);
            String s2 = s.substring(i2 + 1, s.length());
            if (lang.equals(Language.DE)) {
                if ((dictionary_de.contains(s1)) & (dictionary_de.contains(s2)))
                    return true;
                else
                    return false;
            } else {
                if ((dictionary_en.contains(s1)) & (dictionary_en.contains(s2)))
                    return true;
                else
                    return false;
            }
        } else {
            if (lang.equals(Language.DE)) {
                return dictionary_de.contains(s);
            } else {
                return dictionary_en.contains(s);
            }
        }
    }

    public void initJournals() {
        try {
            abbrevJournalPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/journals/abbrev_journals.txt"));

            journalPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/journals/journals.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException(
                    "Error when compiling lexicon matcher for abbreviated journal names.", e);
        }
    }

    public void initConferences() {
        // ArrayList<String> conferences = new ArrayList<String>();
        try {
            conferencePattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/journals/proceedings.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon matcher for conference names.", e);
        }
    }

    public void initPublishers() {
        try {
            publisherPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/publishers/publishers.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon matcher for conference names.", e);
        }
    }

    public void initCities() {
        try {
            cityPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/places/cities15000.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon matcher for cities.", e);
        }
    }

    public void initCollaborations() {
        try {
            //collaborationPattern = new FastMatcher(new
            //        File(GrobidProperties.getGrobidHomePath() + "/lexicon/organisations/collaborations.txt"));
            collaborationPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/organisations/inspire_collaborations.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon matcher for collaborations.", e);
        }
    }

	public void initOrganisations() {
        try {
            organisationPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/organisations/WikiOrganizations.lst"));
			organisationPattern.loadTerms(new File(GrobidProperties.getGrobidHomePath() + 
				"/lexicon/organisations/government.government_agency"));
			organisationPattern.loadTerms(new File(GrobidProperties.getGrobidHomePath() + 
				"/lexicon/organisations/known_corporations.lst"));
			organisationPattern.loadTerms(new File(GrobidProperties.getGrobidHomePath() + 
				"/lexicon/organisations/venture_capital.venture_funded_company"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon matcher for organisations.", e);
        } catch (IOException e) {
            throw new GrobidResourceException("Cannot add term to matcher, because the lexicon resource file " + 
				"does not exist or cannot be read.", e);
        } catch (Exception e) {
			throw new GrobidException("An exception occured while running Grobid Lexicon init.", e);
		}
    }
	
	public void initOrgForms() {
        try {
			orgFormPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/organisations/orgClosings.txt"));	
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon matcher for organisations.", e);
        } catch (Exception e) {
			throw new GrobidException("An exception occured while running Grobid Lexicon init.", e);
		}
    }
	
	public void initLocations() {
        try {
            locationPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/places/location.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon matcher for locations.", e);
        }
    }

	public void initPersonTitles() {
        try {
            personTitlePattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/names/VincentNgPeopleTitles.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon matcher for person titles.", e);
        }
    }

    public void initPersonSuffix() {
        try {
            personSuffixPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/names/suffix.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon matcher for person name suffix.", e);
        }
    }

    /**
     * Look-up in first name gazetteer
     */
    public boolean inFirstNames(String s) {
        return firstNames.contains(s);
    }

    /**
     * Look-up in last name gazetteer
     */
    public boolean inLastNames(String s) {
        return lastNames.contains(s);
    }

    /**
     * Indicate if we have a punctuation
     */
    public boolean isPunctuation(String s) {
        if (s.length() != 1)
            return false;
        else {
            char c = s.charAt(0);
            if ((!Character.isLetterOrDigit(c)) & !(c == '-'))
                return true;
        }
        return false;
    }

    /**
     * Map the language codes used by the language identifier component to the normal
     * language name.
     *
     * @param code the language to be mapped
     */
    public String mapLanguageCode(String code) {
        if (code == null)
            return "";
        else if (code.length() == 0)
            return "";
        else if (code.equals(Language.EN))
            return "English";
        else if (code.equals(Language.FR))
            return "French";
        else if (code.equals(Language.DE))
            return "German";
        else if (code.equals("cat"))
            return "Catalan";
        else if (code.equals("dk"))
            return "Danish";
        else if (code.equals("ee"))
            return "Estonian";
        else if (code.equals("fi"))
            return "Finish";
        else if (code.equals("it"))
            return "Italian";
        else if (code.equals("jp"))
            return "Japanese";
        else if (code.equals("kr"))
            return "Korean";
        else if (code.equals("nl"))
            return "Deutch";
        else if (code.equals("no"))
            return "Norvegian";
        else if (code.equals("se"))
            return "Swedish";
        else if (code.equals("sorb"))
            return "Sorbian";
        else if (code.equals("tr"))
            return "Turkish";
        else
            return "";
    }

    /**
     * Soft look-up in journal name gazetteer with token positions
     */
    public List<OffsetPosition> tokenPositionsJournalNames(String s) {
        if (journalPattern == null) {
            initJournals();
        }
        List<OffsetPosition> results = journalPattern.matchToken(s);
        return results;
    }

    /**
     * Soft look-up in journal name gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsJournalNames(List<LayoutToken> s) {
        if (journalPattern == null) {
            initJournals();
        }
        List<OffsetPosition> results = journalPattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in journal abbreviated name gazetteer with token positions
     */
    public List<OffsetPosition> tokenPositionsAbbrevJournalNames(String s) {
        if (abbrevJournalPattern == null) {
            initJournals();
        }
        List<OffsetPosition> results = abbrevJournalPattern.matchToken(s);
        return results;
    }

    /**
     * Soft look-up in journal abbreviated name gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsAbbrevJournalNames(List<LayoutToken> s) {
        if (abbrevJournalPattern == null) {
            initJournals();
        }
        List<OffsetPosition> results = abbrevJournalPattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in conference/proceedings name gazetteer with token positions
     */
    public List<OffsetPosition> tokenPositionsConferenceNames(String s) {
        if (conferencePattern == null) {
            initConferences();
        }
        List<OffsetPosition> results = conferencePattern.matchToken(s);
        return results;
    }

    /**
     * Soft look-up in conference/proceedings name gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsConferenceNames(List<LayoutToken> s) {
        if (conferencePattern == null) {
            initConferences();
        }
        List<OffsetPosition> results = conferencePattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in conference/proceedings name gazetteer with token positions
     */
    public List<OffsetPosition> tokenPositionsPublisherNames(String s) {
        if (publisherPattern == null) {
            initPublishers();
        }
        List<OffsetPosition> results = publisherPattern.matchToken(s);
        return results;
    }

    /**
     * Soft look-up in publisher name gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsPublisherNames(List<LayoutToken> s) {
        if (publisherPattern == null) {
            initPublishers();
        }
        List<OffsetPosition> results = publisherPattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in collaboration name gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsCollaborationNames(List<LayoutToken> s) {
        if (collaborationPattern == null) {
            initCollaborations();
        }
        List<OffsetPosition> results = collaborationPattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in city name gazetteer for a given string with token positions
     */
    public List<OffsetPosition> tokenPositionsCityNames(String s) {
        if (cityPattern == null) {
            initCities();
        }
        List<OffsetPosition> results = cityPattern.matchToken(s);
        return results;
    }

    /**
     * Soft look-up in city name gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsCityNames(List<LayoutToken> s) {
        if (cityPattern == null) {
            initCities();
        }
        List<OffsetPosition> results = cityPattern.matchLayoutToken(s);
        return results;
    }

    /** Organisation names **/

	/**
     * Soft look-up in organisation name gazetteer for a given string with token positions
     */
    public List<OffsetPosition> tokenPositionsOrganisationNames(String s) {
        if (organisationPattern == null) {
            initOrganisations();
        }
        List<OffsetPosition> results = organisationPattern.matchToken(s);
        return results;
    }

    /**
     * Soft look-up in organisation name gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsOrganisationNames(List<LayoutToken> s) {
        if (organisationPattern == null) {
            initOrganisations();
        }
        List<OffsetPosition> results = organisationPattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in organisation names gazetteer for a string.
     * It return a list of positions referring to the character positions within the string.
     *
     * @param s the input string
     * @return a list of positions referring to the character position in the input string
     */
    public List<OffsetPosition> charPositionsOrganisationNames(String s) {
        if (organisationPattern == null) {
            initOrganisations();
        }
        List<OffsetPosition> results = organisationPattern.matchCharacter(s);
        return results;
    }

    /**
     * Soft look-up in organisation names gazetteer for a tokenize sequence.
     * It return a list of positions referring to the character positions within the input 
     * sequence.
     *
     * @param s the input list of LayoutToken
     * @return a list of positions referring to the character position in the input sequence
     */
    public List<OffsetPosition> charPositionsOrganisationNames(List<LayoutToken> s) {
        if (organisationPattern == null) {
            initOrganisations();
        }
        List<OffsetPosition> results = organisationPattern.matchCharacterLayoutToken(s);
        return results;
    }

	/**
     * Soft look-up in organisation form name gazetteer for a given string with token positions
     */
    public List<OffsetPosition> tokenPositionsOrgForm(String s) {
        if (orgFormPattern == null) {
            initOrgForms();
        }
        List<OffsetPosition> results = orgFormPattern.matchToken(s);
        return results;
    }

    /**
     * Soft look-up in organisation form name gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsOrgForm(List<LayoutToken> s) {
        if (orgFormPattern == null) {
            initOrgForms();
        }
        List<OffsetPosition> results = orgFormPattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in org form names gazetteer for a string.
     * It return a list of positions referring to the character positions within the string.
     *
     * @param s the input string
     * @return a list of positions referring to the character position in the input string
     */
    public List<OffsetPosition> charPositionsOrgForm(String s) {
        if (orgFormPattern == null) {
            initOrgForms();
        }
        List<OffsetPosition> results = orgFormPattern.matchCharacter(s);
        return results;
    }

    /**
     * Soft look-up in org form names gazetteer for a tokenized string.
     * It return a list of positions referring to the character positions within the sequence.
     *
     * @param s the input list of LayoutToken
     * @return a list of positions referring to the character position in the input sequence
     */
    public List<OffsetPosition> charPositionsOrgForm(List<LayoutToken> s) {
        if (orgFormPattern == null) {
            initOrgForms();
        }
        List<OffsetPosition> results = orgFormPattern.matchCharacterLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in location name gazetteer for a given string with token positions
     */
    public List<OffsetPosition> tokenPositionsLocationNames(String s) {
        if (locationPattern == null) {
            initLocations();
        }
        List<OffsetPosition> results = locationPattern.matchToken(s);
        return results;
    }

    /**
     * Soft look-up in location name gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsLocationNames(List<LayoutToken> s) {
        if (locationPattern == null) {
            initLocations();
        }
        List<OffsetPosition> results = locationPattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in location name gazetteer for a string, return a list of positions referring 
     * to the character positions within the string.
     *
     * For example "The car is in Milan" as Milan is a location, would return OffsetPosition(14,19)
     *
     * @param s the input string
     * @return a list of positions referring to the character position in the input string
     */
    public List<OffsetPosition> charPositionsLocationNames(String s) {
        if (locationPattern == null) {
            initLocations();
        }
        List<OffsetPosition> results = locationPattern.matchCharacter(s);
        return results;
    }

    /**
     * Soft look-up in location name gazetteer for a list of LayoutToken, return a list of 
     * positions referring to the character positions in the input sequence.
     *
     * For example "The car is in Milan" as Milan is a location, would return OffsetPosition(14,19)
     *
     * @param s the input list of LayoutToken
     * @return a list of positions referring to the character position in the input sequence
     */
    public List<OffsetPosition> charPositionsLocationNames(List<LayoutToken> s) {
        if (locationPattern == null) {
            initLocations();
        }
        List<OffsetPosition> results = locationPattern.matchCharacterLayoutToken(s);
        return results;
    }

	/**
     * Soft look-up in person title gazetteer for a given string with token positions
     */
    public List<OffsetPosition> tokenPositionsPersonTitle(String s) {
        if (personTitlePattern == null) {
            initPersonTitles();
        }
        List<OffsetPosition> results = personTitlePattern.matchToken(s);
        return results;
    }

    /**
     * Soft look-up in person title gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsPersonTitle(List<LayoutToken> s) {
        if (personTitlePattern == null) {
            initPersonTitles();
        }
        List<OffsetPosition> results = personTitlePattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in person name suffix gazetteer for a given list of LayoutToken objects
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsPersonSuffix(List<LayoutToken> s) {
        if (personSuffixPattern == null) {
            initPersonSuffix();
        }
        List<OffsetPosition> results = personSuffixPattern.matchLayoutToken(s);
        return results;
    }

    /**
     * Soft look-up in person title name gazetteer for a string.
     * It return a list of positions referring to the character positions within the string.
     *
     * @param s the input string
     * @return a list of positions referring to the character position in the input string
     */
    public List<OffsetPosition> charPositionsPersonTitle(String s) {
        if (personTitlePattern == null) {
            initPersonTitles();
        }
        List<OffsetPosition> results = personTitlePattern.matchCharacter(s);
        return results;
    }

    /**
     * Soft look-up in person title name gazetteer for a list of LayoutToken.
     * It return a list of positions referring to the character positions in the input
     * sequence.
     *
     * @param s the input list of LayoutToken
     * @return a list of positions referring to the character position in the input sequence
     */
    public List<OffsetPosition> charPositionsPersonTitle(List<LayoutToken> s) {
        if (personTitlePattern == null) {
            initPersonTitles();
        }
        List<OffsetPosition> results = personTitlePattern.matchCharacterLayoutToken(s);
        return results;
    }

    /**
     * Identify in tokenized input the positions of identifier patterns with token positions
     */
    public List<OffsetPosition> tokenPositionsIdentifierPattern(List<LayoutToken> tokens) {
        List<OffsetPosition> result = new ArrayList<OffsetPosition>();
        String text = LayoutTokensUtil.toText(tokens);
        
        // DOI positions
        result = tokenPositionsDOIPattern(tokens, text);

        // arXiv 
        List<OffsetPosition> positions = tokenPositionsArXivPattern(tokens, text);
        result = Utilities.mergePositions(result, positions);

        // ISSN and ISBN
        /*positions = tokenPositionsISSNPattern(tokens);
        result = Utilities.mergePositions(result, positions);
        positions = tokenPositionsISBNPattern(tokens);
        result = Utilities.mergePositions(result, positions);*/

        return result;
    }

    /**
     * Identify in tokenized input the positions of the DOI patterns with token positons
     */
    public List<OffsetPosition> tokenPositionsDOIPattern(List<LayoutToken> tokens, String text) {
        List<OffsetPosition> textResult = new ArrayList<OffsetPosition>();
        Matcher doiMatcher = TextUtilities.DOIPattern.matcher(text);
        while (doiMatcher.find()) {            
            textResult.add(new OffsetPosition(doiMatcher.start(), doiMatcher.end()));
        }
        return Utilities.convertStringOffsetToTokenOffset(textResult, tokens);
    }

    /**
     * Identify in tokenized input the positions of the arXiv identifier patterns
     * with token positions
     */
    public List<OffsetPosition> tokenPositionsArXivPattern(List<LayoutToken> tokens, String text) {
        List<OffsetPosition> textResult = new ArrayList<OffsetPosition>();
        Matcher arXivMatcher = TextUtilities.arXivPattern.matcher(text);
        while (arXivMatcher.find()) {  
            //System.out.println(arXivMatcher.start() + " / " + arXivMatcher.end() + " / " + text.substring(arXivMatcher.start(), arXivMatcher.end()));                 
            textResult.add(new OffsetPosition(arXivMatcher.start(), arXivMatcher.end()));
        }
        return Utilities.convertStringOffsetToTokenOffset(textResult, tokens);
    }


    /**
     * Identify in tokenized input the positions of ISSN patterns with token positions
     */
    public List<OffsetPosition> tokenPositionsISSNPattern(List<LayoutToken> tokens) {
        List<OffsetPosition> result = new ArrayList<OffsetPosition>();
        
        // TBD !

        return result;
    }

    /**
     * Identify in tokenized input the positions of ISBN patterns with token positions
     */
    public List<OffsetPosition> tokenPositionsISBNPattern(List<LayoutToken> tokens) {
        List<OffsetPosition> result = new ArrayList<OffsetPosition>();

        // TBD !!

        return result;
    }

    /**
     * Identify in tokenized input the positions of an URL pattern with token positions
     */
    public List<OffsetPosition> tokenPositionsUrlPattern(List<LayoutToken> tokens) {
        //List<OffsetPosition> result = new ArrayList<OffsetPosition>();
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> textResult = new ArrayList<OffsetPosition>();
        Matcher urlMatcher = TextUtilities.urlPattern.matcher(text);
        while (urlMatcher.find()) {  
            //System.out.println(urlMatcher.start() + " / " + urlMatcher.end() + " / " + text.substring(urlMatcher.start(), urlMatcher.end()));                 
            textResult.add(new OffsetPosition(urlMatcher.start(), urlMatcher.end()));
        }
        return Utilities.convertStringOffsetToTokenOffset(textResult, tokens);
    }

    /**
     * Identify in tokenized input the positions of an URL pattern with character positions
     */
    public List<OffsetPosition> characterPositionsUrlPattern(List<LayoutToken> tokens) {
        //List<OffsetPosition> result = new ArrayList<OffsetPosition>();
        String text = LayoutTokensUtil.toText(tokens);
        List<OffsetPosition> textResult = new ArrayList<OffsetPosition>();
        Matcher urlMatcher = TextUtilities.urlPattern.matcher(text);
        while (urlMatcher.find()) {  
            textResult.add(new OffsetPosition(urlMatcher.start(), urlMatcher.end()));
        }
        return textResult;
    }

    /**
     * Identify in tokenized input the positions of an URL pattern with character positions, 
     * and refine positions based on possible PDF URI annotations.
     * 
     * This will produce better quality recognized URL, avoiding missing suffixes and problems
     * with break lines and spaces.
     **/
    public static List<OffsetPosition> characterPositionsUrlPatternWithPdfAnnotations(
                                    List<LayoutToken> layoutTokens, 
                                    List<PDFAnnotation> pdfAnnotations, 
                                    String text) {
        List<OffsetPosition> urlPositions = Lexicon.getInstance().characterPositionsUrlPattern(layoutTokens);
        List<OffsetPosition> resultPositions = new ArrayList<>();

        // do we need to extend the url position based on additional position of the corresponding 
        // PDF annotation?
        for(OffsetPosition urlPosition : urlPositions) {

            int startPos = urlPosition.start;
            int endPos = urlPosition.end;

            int startTokenIndex = -1;
            int endTokensIndex = -1;

            // token sublist 
            List<LayoutToken> urlTokens = new ArrayList<>();
            int tokenPos = 0;
            int tokenIndex = 0;
            for(LayoutToken localToken : layoutTokens) {
                if (startPos <= tokenPos && (tokenPos+localToken.getText().length() <= endPos) ) {
                    urlTokens.add(localToken);
                    if (startTokenIndex == -1)
                        startTokenIndex = tokenIndex;
                    if (tokenIndex > endTokensIndex)
                        endTokensIndex = tokenIndex;
                }
                if (tokenPos > endPos) {
                    break;
                }
                tokenPos += localToken.getText().length();
                tokenIndex++;
            }

            //String urlString = LayoutTokensUtil.toText(urlTokens);
            String urlString = text.substring(startPos, endPos);

            PDFAnnotation targetAnnotation = null;
            if (urlTokens.size()>0) {
                LayoutToken lastToken = urlTokens.get(urlTokens.size()-1);
                if (pdfAnnotations != null) {
                    for (PDFAnnotation pdfAnnotation : pdfAnnotations) {
                        if (pdfAnnotation.getType() != null && pdfAnnotation.getType() == PDFAnnotation.Type.URI) {
                            if (pdfAnnotation.cover(lastToken)) {
    //System.out.println("found overlapping PDF annotation for URL: " + pdfAnnotation.getDestination());
                                targetAnnotation = pdfAnnotation;
                                break;
                            }
                        }
                    }
                }
            }

            if (targetAnnotation != null) {
                String destination = targetAnnotation.getDestination();

                int destinationPos = 0;
                if (destination.indexOf(urlString) != -1) {
                    destinationPos = destination.indexOf(urlString)+urlString.length();
                }

                if (endTokensIndex < layoutTokens.size()-1) {
                    for(int j=endTokensIndex+1; j<layoutTokens.size(); j++) {
                        LayoutToken nextToken = layoutTokens.get(j);

                        if ("\n".equals(nextToken.getText()) || 
                            " ".equals(nextToken.getText()) ||
                            nextToken.getText().length() == 0) {
                            endPos += nextToken.getText().length();
                            urlTokens.add(nextToken);
                            continue;
                        }

                        int pos = destination.indexOf(nextToken.getText(), destinationPos);
                        if (pos != -1) {
                            endPos += nextToken.getText().length();
                            destinationPos = pos + nextToken.getText().length();
                            urlTokens.add(nextToken);
                        } else 
                            break;
                    }
                }
            }

            // finally avoid ending a URL by a dot, because it can harm the sentence segmentation
            if (text.charAt(endPos-1) == '.') 
                endPos = endPos-1;

            OffsetPosition position = new OffsetPosition();
            position.start = startPos;
            position.end = endPos;
            resultPositions.add(position);
        }
        return resultPositions;
    }


    /**
     * Identify in tokenized input the positions of an email address pattern with token positions
     */
    public List<OffsetPosition> tokenPositionsEmailPattern(List<LayoutToken> tokens) {
        //List<OffsetPosition> result = new ArrayList<OffsetPosition>();
        String text = LayoutTokensUtil.toText(tokens);
        if (text.indexOf("@") == -1)
            return new ArrayList<OffsetPosition>();
        List<OffsetPosition> textResult = new ArrayList<OffsetPosition>();
        Matcher emailMatcher = TextUtilities.emailPattern.matcher(text);
        while (emailMatcher.find()) {  
            //System.out.println(urlMatcher.start() + " / " + urlMatcher.end() + " / " + text.substring(urlMatcher.start(), urlMatcher.end()));                 
            textResult.add(new OffsetPosition(emailMatcher.start(), emailMatcher.end()));
        }
        return Utilities.convertStringOffsetToTokenOffset(textResult, tokens);
    }

}
