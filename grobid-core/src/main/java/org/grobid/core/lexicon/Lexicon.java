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
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.lang.Language;
import org.grobid.core.sax.CountryCodeSaxParser;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing all the lexical resources.
 *
 * @author Patrice Lopez
 */
public class Lexicon {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Lexicon.class);
    // private static volatile Boolean instanceController = false;
    private static volatile Lexicon instance;

    private Set<String> dictionary_en = null;
    private Set<String> dictionary_de = null;
    private Set<String> lastNames = null;
    private Set<String> firstNames = null;
    private Map<String, String> countryCodes = null;
    private Set<String> countries = null;
    //private Set journals = null;
    private FastMatcher abbrevJournalPattern = null;
    private FastMatcher conferencePattern = null;
    private FastMatcher publisherPattern = null;
    private FastMatcher journalPattern = null;
    private FastMatcher cityPattern = null;
	private FastMatcher organisationPattern = null;
	private FastMatcher locationPattern = null;
	private FastMatcher personTitlePattern = null;
	private FastMatcher orgFormPattern = null;
	
    public static Lexicon getInstance() {
        if (instance == null) {
            //double check idiom
            // synchronized (instanceController) {
                if (instance == null)
					getNewInstance();
            // }
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
        dictionary_en = new HashSet<String>();
        dictionary_de = new HashSet<String>();
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
            if (GrobidProperties.isResourcesInHome())
                ist = new FileInputStream(file);
            else
                ist = getClass().getResourceAsStream(path);


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
        } catch (FileNotFoundException e) {
//	    	e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } catch (IOException e) {
//	    	e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            try {
                if (ist != null)
                    ist.close();
                if (isr != null)
                    isr.close();
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close all streams.", e);
            }
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
        InputStreamReader isr = null;
        BufferedReader dis = null;
        try {
            if (GrobidProperties.isResourcesInHome())
                ist = new FileInputStream(file);
            else
                ist = getClass().getResourceAsStream(path);

            isr = new InputStreamReader(ist, "UTF8");
            dis = new BufferedReader(isr);
            CountryCodeSaxParser parser = new CountryCodeSaxParser(countryCodes, countries);

            SAXParserFactory spf = SAXParserFactory.newInstance();
            //get a new instance of parser
            SAXParser p = spf.newSAXParser();
            p.parse(ist, parser);
        } catch (Exception e) {
//			e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            try {
                if (ist != null)
                    ist.close();
                if (isr != null)
                    isr.close();
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close all streams.", e);
            }
        }
    }

    public String getcountryCode(String country) {
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
            if (GrobidProperties.isResourcesInHome()) {
                ist = new FileInputStream(file);
            } else {
                ist = getClass().getResourceAsStream(path);
            }
            dis = new BufferedReader(new InputStreamReader(ist, "UTF8"));

            String l = null;
            while ((l = dis.readLine()) != null) {
                // read the line
                // the first token, separated by a tabulation, gives the word form
                StringTokenizer st = new StringTokenizer(l, "\t\n");
                if (st.hasMoreTokens()) {
                    String word = st.nextToken().toLowerCase();
                    if (!firstNames.contains(word)) {
                        firstNames.add(word);
                    }
                }
            }
        } catch (FileNotFoundException e) {
//    		e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } catch (IOException e) {
//    		e.printStackTrace();
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
            if (GrobidProperties.isResourcesInHome())
                ist = new FileInputStream(file);
            else
                ist = getClass().getResourceAsStream(path);

            dis = new BufferedReader(new InputStreamReader(ist, "UTF8"));

            String l = null;
            while ((l = dis.readLine()) != null) {
                // read the line
                // the first token, separated by a tabulation, gives the word form
                StringTokenizer st = new StringTokenizer(l, "\t\n");
                if (st.hasMoreTokens()) {
                    String word = st.nextToken().toLowerCase();
                    if (!lastNames.contains(word)) {
                        lastNames.add(word);
                    }
                }
            }
        } catch (FileNotFoundException e) {
//    		e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } catch (IOException e) {
//    		e.printStackTrace();
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
                    "Error when compiling lexicon regular expression for abbreviated journal names.", e);
        }
    }

    public void initConferences() {
        // ArrayList<String> conferences = new ArrayList<String>();
        try {
            conferencePattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/journals/proceedings.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon regular expression for conference names.", e);
        }
    }

    public void initPublishers() {
        try {
            publisherPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/publishers/publishers.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon regular expression for conference names.", e);
        }
    }

    public void initCities() {
        try {
            cityPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/places/cities15000.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon regular expression for cities.", e);
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
            throw new GrobidResourceException("Error when compiling lexicon regular expression for organisations.", e);
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
            throw new GrobidResourceException("Error when compiling lexicon regular expression for organisations.", e);
        } catch (Exception e) {
			throw new GrobidException("An exception occured while running Grobid Lexicon init.", e);
		}
    }
	
	public void initLocations() {
        try {
            locationPattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/places/location.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon regular expression for locations.", e);
        }
    }

	public void initPersonTitles() {
        try {
            personTitlePattern = new FastMatcher(new
                    File(GrobidProperties.getGrobidHomePath() + "/lexicon/names/VincentNgPeopleTitles.txt"));
        } catch (PatternSyntaxException e) {
            throw new GrobidResourceException("Error when compiling lexicon regular expression for locations.", e);
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
     * Soft look-up in journal name gazetteer
     */
    public List<OffsetPosition> inJournalNames(String s) {
        if (journalPattern == null) {
            initJournals();
        }
        List<OffsetPosition> results = journalPattern.matcher(s);
        return results;
    }

    /**
     * Soft look-up in journal abbreviated name gazetteer
     */
    public List<OffsetPosition> inAbbrevJournalNames(String s) {
        if (abbrevJournalPattern == null) {
            initJournals();
        }
        List<OffsetPosition> results = abbrevJournalPattern.matcher(s);
        return results;
    }

    /**
     * Soft look-up in conference/proceedings name gazetteer
     */
    public List<OffsetPosition> inConferenceNames(String s) {
        if (conferencePattern == null) {
            initConferences();
        }
        List<OffsetPosition> results = conferencePattern.matcher(s);
        return results;
    }

    /**
     * Soft look-up in conference/proceedings name gazetteer
     */
    public List<OffsetPosition> inPublisherNames(String s) {
        if (publisherPattern == null) {
            initPublishers();
        }
        List<OffsetPosition> results = publisherPattern.matcher(s);
        return results;
    }

    /**
     * Soft look-up in city name gazetteer for a given string
     */
    public List<OffsetPosition> inCityNames(String s) {
        if (cityPattern == null) {
            initCities();
        }
        List<OffsetPosition> results = cityPattern.matcher(s);
        return results;
    }

    /**
     * Soft look-up in city name gazetteer for a given string already tokenized
     */
    public List<OffsetPosition> inCityNames(List<String> s) {
        if (cityPattern == null) {
            initCities();
        }
        List<OffsetPosition> results = cityPattern.matcher(s);
        return results;
    }

	/**
     * Soft look-up in organisation name gazetteer for a given string
     */
    public List<OffsetPosition> inOrganisationNames(String s) {
        if (organisationPattern == null) {
            initOrganisations();
        }
        List<OffsetPosition> results = organisationPattern.matcher(s);
        return results;
    }

    /**
     * Soft look-up in organisation name gazetteer for a given string already tokenized
     */
    public List<OffsetPosition> inOrganisationNames(List<String> s) {
        if (organisationPattern == null) {
            initOrganisations();
        }
        List<OffsetPosition> results = organisationPattern.matcher(s);
        return results;
    }

	/**
     * Soft look-up in organisation form name gazetteer for a given string
     */
    public List<OffsetPosition> inOrgFormNames(String s) {
        if (orgFormPattern == null) {
            initOrgForms();
        }
        List<OffsetPosition> results = orgFormPattern.matcher(s);
        return results;
    }

    /**
     * Soft look-up in organisation form name gazetteer for a given string already tokenized
     */
    public List<OffsetPosition> inOrgFormNames(List<String> s) {
        if (orgFormPattern == null) {
            initOrgForms();
        }
        List<OffsetPosition> results = orgFormPattern.matcher(s);
        return results;
    }

	/**
     * Soft look-up in location name gazetteer for a given string
     */
    public List<OffsetPosition> inLocationNames(String s) {
        if (locationPattern == null) {
            initLocations();
        }
        List<OffsetPosition> results = locationPattern.matcher(s);
        return results;
    }

    /**
     * Soft look-up in location name gazetteer for a given string already tokenized
     */
    public List<OffsetPosition> inLocationNames(List<String> s) {
        if (locationPattern == null) {
            initLocations();
        }
        List<OffsetPosition> results = locationPattern.matcher(s);
        return results;
    }

	/**
     * Soft look-up in person title gazetteer for a given string
     */
    public List<OffsetPosition> inPersonTitleNames(String s) {
        if (personTitlePattern == null) {
            initPersonTitles();
        }
        List<OffsetPosition> results = personTitlePattern.matcher(s);
        return results;
    }

    /**
     * Soft look-up in person title gazetteer for a given string already tokenized
     */
    public List<OffsetPosition> inPersonTitleNames(List<String> s) {
        if (personTitlePattern == null) {
            initPersonTitles();
        }
        List<OffsetPosition> results = personTitlePattern.matcher(s);
        return results;
    }
}
