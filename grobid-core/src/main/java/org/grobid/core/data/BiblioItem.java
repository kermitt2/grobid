package org.grobid.core.data;

import org.grobid.core.data.util.AuthorEmailAssigner;
import org.grobid.core.data.util.ClassicAuthorEmailAssigner;
import org.grobid.core.data.util.EmailSanitizer;
import org.grobid.core.document.TEIFormater;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.KeyGen;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for representing and exchanging a bibliographical item.
 *
 * @author Patrice Lopez
 */
public class BiblioItem {
    LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
    private AuthorEmailAssigner authorEmailAssigner = new ClassicAuthorEmailAssigner();
    private EmailSanitizer emailSanitizer = new EmailSanitizer();
    private String teiId;
    //TODO: keep in sync with teiId - now teiId is generated in many different places
    private Integer ordinal;
    private List<BoundingBox> coordinates = null;

    @Override
    public String toString() {
        return "BiblioItem{" +
                "submission_date='" + submission_date + '\'' +
                ", languageUtilities=" + languageUtilities +
                ", item=" + item +
                ", parentItem=" + parentItem +
                ", ISBN13='" + ISBN13 + '\'' +
                ", ISBN10='" + ISBN10 + '\'' +
                ", title='" + title + '\'' +
                ", publisher='" + publisher + '\'' +
                ", nbPages=" + nbPages +
                ", edition='" + edition + '\'' +
                ", language='" + language + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", publication_date='" + publication_date + '\'' +
                ", normalized_publication_date=" + normalized_publication_date +
                ", editors='" + editors + '\'' +
                ", publisher_website='" + publisher_website + '\'' +
                ", serie='" + serie + '\'' +
                ", ISSN='" + ISSN + '\'' +
                ", ISSNe='" + ISSNe + '\'' +
                ", volume='" + volume + '\'' +
                ", number='" + number + '\'' +
                ", month='" + month + '\'' +
                ", support_type='" + support_type + '\'' +
                ", version='" + version + '\'' +
                ", smallImageURL='" + smallImageURL + '\'' +
                ", largeImageURL='" + largeImageURL + '\'' +
                ", publisherPlace='" + publisherPlace + '\'' +
                ", review='" + review + '\'' +
                ", keywords=" + keywords +
                ", subjects=" + subjects +
                ", categories='" + categories + '\'' +
                ", type='" + type + '\'' +
                ", typeDescription='" + typeDescription + '\'' +
                ", book_type='" + book_type + '\'' +
                ", DOI='" + DOI + '\'' +
                ", inDOI='" + inDOI + '\'' +
                ", abstract_='" + abstract_ + '\'' +
                ", authors='" + authors + '\'' +
                ", location='" + location + '\'' +
                ", bookTitle='" + bookTitle + '\'' +
                ", serieTitle='" + serieTitle + '\'' +
                ", pageRange='" + pageRange + '\'' +
                ", journal='" + journal + '\'' +
                ", volumeBlock='" + volumeBlock + '\'' +
                ", institution='" + institution + '\'' +
                ", note='" + note + '\'' +
                ", affiliation='" + affiliation + '\'' +
                ", address='" + address + '\'' +
                ", country='" + country + '\'' +
                ", town='" + town + '\'' +
                ", email='" + email + '\'' +
                ", pubnum='" + pubnum + '\'' +
                ", keyword='" + keyword + '\'' +
                ", phone='" + phone + '\'' +
                ", degree='" + degree + '\'' +
                ", web='" + web + '\'' +
                ", issue='" + issue + '\'' +
                ", journal_abbrev='" + journal_abbrev + '\'' +
                ", event='" + event + '\'' +
                ", abstractHeader='" + abstractHeader + '\'' +
                ", day='" + day + '\'' +
                ", locationPublisher='" + locationPublisher + '\'' +
                ", dedication='" + dedication + '\'' +
                ", submission='" + submission + '\'' +
                ", english_title='" + english_title + '\'' +
                ", url='" + url + '\'' +
                ", uri='" + uri + '\'' +
                ", confidence='" + confidence + '\'' +
                ", conf=" + conf +
                ", e_year='" + e_year + '\'' +
                ", e_month='" + e_month + '\'' +
                ", e_day='" + e_day + '\'' +
                ", s_year='" + s_year + '\'' +
                ", s_month='" + s_month + '\'' +
                ", s_day='" + s_day + '\'' +
                ", a_year='" + a_year + '\'' +
                ", a_month='" + a_month + '\'' +
                ", a_day='" + a_day + '\'' +
                ", authorList=" + authorList +
                ", editorList=" + editorList +
                ", affiliationList=" + affiliationList +
                ", addressList=" + addressList +
                ", emailList=" + emailList +
                ", webList=" + webList +
                ", phoneList=" + phoneList +
                ", markers=" + markers +
                ", fullAuthors=" + fullAuthors +
                ", fullEditors=" + fullEditors +
                ", fullAffiliations=" + fullAffiliations +
                ", reference='" + reference + '\'' +
                ", copyright='" + copyright + '\'' +
                ", grant='" + grant + '\'' +
                ", affiliationAddressBlock='" + affiliationAddressBlock + '\'' +
                ", articleTitle='" + articleTitle + '\'' +
                ", beginPage=" + beginPage +
                ", endPage=" + endPage +
                ", year='" + year + '\'' +
                ", authorString='" + authorString + '\'' +
                ", path='" + path + '\'' +
                ", postProcessEditors=" + postProcessEditors +
                ", crossrefError=" + crossrefError +
                ", normalized_submission_date=" + normalized_submission_date +
                ", originalAffiliation='" + originalAffiliation + '\'' +
                ", originalAbstract='" + originalAbstract + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", originalAuthors='" + originalAuthors + '\'' +
                ", originalAddress='" + originalAddress + '\'' +
                ", originalNote='" + originalNote + '\'' +
                ", originalKeyword='" + originalKeyword + '\'' +
                ", originalVolumeBlock='" + originalVolumeBlock + '\'' +
                ", originalJournal='" + originalJournal + '\'' +
                '}';
    }

    public int item = -1;

    public static final int Book = 0; // the whole book
    public static final int Periodical = 1; // the journal or magazine item
    public static final int Digital_support = 2;
    public static final int Article = 3; // of a journal or magazine
    public static final int Unknown = 4;
    public static final int InBook = 5;
    public static final int InProceedings = 6;
    public static final int InCollection = 7;
    public static final int Manual = 8;
    public static final int TechReport = 9;
    public static final int MasterThesis = 10;
    public static final int PhdThesis = 11;
    public static final int Unpublished = 12;
    public static final int Proceedings = 13;
    public static final int Serie = 14;

    private BiblioItem parentItem = null; // the bibliographic item "container", i.e. 
    // the book for a chapter
    // the journal for a journal article, etc. 

    private String ISBN13 = null;
    private String ISBN10 = null;
    private String title = null;
    private String publisher = null;
    private int nbPages = -1;
    private String edition = null;
    private String language = null;
    private String subtitle = null;
    private String publication_date = null;
    private Date normalized_publication_date = null;
    private String editors = null;
    private String publisher_website = null;
    private String serie = null;
    private String ISSN = null; // print/default
    private String ISSNe = null; // electronic
    private String volume = null;
    private String number = null;
    private String month = null;
    private String support_type = null;
    private String version = null;
    private String smallImageURL = null;
    private String largeImageURL = null;
    private String publisherPlace = null;
    private String review = null;
    private List<Keyword> keywords;
    private List<String> subjects;
    private List<String> categories;
    private String type = null; // book, journal, proceedings, in book, etc
    private String typeDescription = null;
    private String book_type = null;
    private String DOI = null;
    private String inDOI = null;
    private String abstract_ = null;

    // for convenience GROBIDesque
    private String authors = null;
    private String location = null;
    private String bookTitle = null;
    private String serieTitle = null;
    private String pageRange = null;
    private String journal = null;
    private String volumeBlock = null;
    private String institution = null;
    private String note = null;
    private String affiliation = null;
    private String address = null;
    private String country = null;
    private String town = null;
    private String email = null;
    private String pubnum = null;
    private String keyword = null;
    private String phone = null;
    private String degree = null;
    private String web = null;
    private String issue = null;
    private String journal_abbrev = null;
    private String event = null;
    private String abstractHeader = null;
    private String day = null;
    private String locationPublisher = null;
    private String dedication = null;
    private String submission = null;
    private String english_title = null;
    private String url = null;
    private String uri = null;
    private String confidence = null;
    private double conf = 0.0;

    // date for electronic publishing
    private String e_year = null;
    private String e_month = null;
    private String e_day = null;

    // date of submission 
    private String s_year = null;
    private String s_month = null;
    private String s_day = null;

    // date of acceptance 
    private String a_year = null;
    private String a_month = null;
    private String a_day = null;

    // advanced grobid recognitions
    private List<String> authorList;
    private List<String> editorList;
    private List<String> affiliationList;
    private List<String> addressList;
    private List<String> emailList;
    private List<String> webList;
    private List<String> phoneList;
    private List<String> markers;

    private List<Person> fullAuthors = null;
    private List<Person> fullEditors = null;
    private List<Affiliation> fullAffiliations = null;

    private String reference = null;
    private String copyright = null;
    private String grant = null;

    //public List<String> affiliationAddressBlock = null; 
    public String affiliationAddressBlock = null;

    // just for articles
    private String articleTitle = null;
    private int beginPage = -1;
    private int endPage = -1;
    private String year = null; // default is publication date on print media 
    private String authorString = null;
    private String path = "";
    private boolean postProcessEditors = false;
    private boolean crossrefError = true;
    private String submission_date = null;
    private Date normalized_submission_date = null;

    // for OCR post-corrections
    private String originalAffiliation = null;
    private String originalAbstract = null;
    private String originalTitle = null;
    private String originalAuthors = null;
    private String originalAddress = null;
    private String originalNote = null;
    private String originalKeyword = null;
    private String originalVolumeBlock = null;
    private String originalJournal = null;

    public static final List<String> confPrefixes = Arrays.asList("Proceedings of", "proceedings of",
            "In Proceedings of the", "In: Proceeding of", "In Proceedings, ", "In Proceedings of",
            "In Proceeding of", "in Proceeding of", "in Proceeding", "In Proceeding", "Proceedings",
            "proceedings", "In Proc", "in Proc", "In Proc.", "in Proc.", "In proc.", "in proc", "in proc.",
            "In proc", "Proc", "proc", "Proc.", "proc.", "Acte de la", "Acte de", "Acte", "acte de la",
            "acte de", "acte");

    public BiblioItem() {
    }

    public void setParentItem(BiblioItem bi) {
        parentItem = bi;
    }

    public BiblioItem getParentItem() {
        return parentItem;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int type) {
        item = type;
    }

    public String getISBN13() {
        return this.ISBN13;
    }

    public String getISBN10() {
        return this.ISBN10;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public String getEdition() {
        return this.edition;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getSubtitle() {
        if (subtitle != null)
            if (subtitle.length() != 0)
                if (!subtitle.equals("null"))
                    return this.subtitle;
        return null;
    }

    public String getPublicationDate() {
        return this.publication_date;
    }

    public Date getNormalizedPublicationDate() {
        return normalized_publication_date;
    }

    public String getEditors() {
        return this.editors;
    }

    public String getPublisherWebsite() {
        return this.publisher_website;
    }

    public String getSerie() {
        return this.serie;
    }

    public String getISSN() {
        return this.ISSN;
    }

    public String getISSNe() {
        return this.ISSNe;
    }

    public String getVolume() {
        return this.volume;
    }

    public String getNumber() {
        return this.number;
    }

    public String getMonth() {
        return this.month;
    }

    public String getSupportType() {
        return this.support_type;
    }

    public String getVersion() {
        return this.version;
    }

    public String getSmallImageURL() {
        return this.smallImageURL;
    }

    public String getLargeImageURL() {
        return this.largeImageURL;
    }

    public String getPublisherPlace() {
        return publisherPlace;
    }

    public String getReview() {
        return this.review;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public int getNbPages() {
        return nbPages;
    }

    public String getType() {
        return type;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public String getBookType() {
        return book_type;
    }

    public String getDOI() {
        return DOI;
    }

    public String getInDOI() {
        return inDOI;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public int getBeginPage() {
        return beginPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public String getYear() {
        return year;
    }

    public String getAbstract() {
        return abstract_;
    }

    public String getEmail() {
        return email;
    }

    public String getPubnum() {
        return pubnum;
    }

    public String getSerieTitle() {
        return serieTitle;
    }

    public String getURL() {
        return url;
    }

    public String getURI() {
        return uri;
    }

    public String getConfidence() {
        return confidence;
    }

    // temp
    public String getAuthors() {
        return authors;
    }

    public String getLocation() {
        return location;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getPageRange() {
        if (pageRange != null)
            return pageRange;
        else if ((beginPage != -1) && (endPage != -1))
            return "" + beginPage + "--" + endPage;
        else
            return null;
    }

    public String getJournal() {
        return journal;
    }

    public String getVolumeBlock() {
        return volumeBlock;
    }

    public String getInstitution() {
        return institution;
    }

    public String getNote() {
        return note;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public String getAddress() {
        return address;
    }

    public String getCountry() {
        return country;
    }

    public String getTown() {
        return town;
    }

    public String getKeyword() {
        return keyword;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public String getPhone() {
        return phone;
    }

    public String getDegree() {
        return degree;
    }

    public String getWeb() {
        return web;
    }

    public String getIssue() {
        return issue;
    }

    public String getJournalAbbrev() {
        return journal_abbrev;
    }

    public String getEvent() {
        return event;
    }

    public boolean getError() {
        return crossrefError;
    }

    public String getAbstractHeader() {
        return abstractHeader;
    }

    public String getDay() {
        return day;
    }

    public String getLocationPublisher() {
        return locationPublisher;
    }

    public String getAuthorString() {
        return authorString;
    }

    public String getE_Year() {
        return e_year;
    }

    public String getE_Month() {
        return e_month;
    }

    public String getE_Day() {
        return e_day;
    }

    public String getS_Year() {
        return s_year;
    }

    public String getS_Month() {
        return s_month;
    }

    public String getS_Day() {
        return s_day;
    }

    public String getA_Year() {
        return a_year;
    }

    public String getA_Month() {
        return a_month;
    }

    public String getA_Day() {
        return a_day;
    }

    public String getDedication() {
        return dedication;
    }

    public String getSubmission() {
        return submission;
    }

    public String getEnglishTitle() {
        return english_title;
    }

    public String getSubmissionDate() {
        return submission_date;
    }

    public Date getNormalizedSubmissionDate() {
        return normalized_submission_date;
    }

    public String getOriginalAffiliation() {
        return originalAffiliation;
    }

    public String getOriginalAbstract() {
        return originalAbstract;
    }

    public String getOriginalAuthors() {
        return originalAuthors;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOriginalAddress() {
        return originalAddress;
    }

    public String getOriginalNote() {
        return originalNote;
    }

    public String getOriginalKeyword() {
        return originalKeyword;
    }

    public String getOriginalVolumeBlock() {
        return originalVolumeBlock;
    }

    public String getOriginalJournal() {
        return originalJournal;
    }

    public List<org.grobid.core.data.Person> getFullAuthors() {
        return fullAuthors;
    }

    public List<org.grobid.core.data.Person> getFullEditors() {
        return fullEditors;
    }

    public List<org.grobid.core.data.Affiliation> getFullAffiliations() {
        return fullAffiliations;
    }

    public String getReference() {
        return reference;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getGrant() {
        return grant;
    }

    public void setISBN13(String isbn) {
        /* some cleaning... */
        this.ISBN13 = cleanSQLString(cleanISBNString(isbn));
    }

    public void setISBN10(String isbn) {
        /* some cleaning... */
        this.ISBN10 = cleanISBNString(isbn);
    }

    public void setTitle(String theTitle) {
        this.title = theTitle;
    }

    public void setPublisher(String thePublisher) {
        this.publisher = thePublisher;
    }

    public void setEdition(String theEdition) {
        if (theEdition != null) {
            if (theEdition.length() > 10) {
                theEdition = theEdition.substring(0, 9);
            }
        }
        this.edition = cleanSQLString(theEdition);
    }

    public void setLanguage(String theLanguage) {
        this.language = cleanSQLString(theLanguage);
    }

    public void setSubtitle(String theSubtitle) {
        this.subtitle = theSubtitle;
    }

    public void setPublicationDate(String theDate) {
        this.publication_date = cleanSQLString(theDate);
    }

    public void setNormalizedPublicationDate(Date theDate) {
        this.normalized_publication_date = theDate;
    }

    public void setEditors(String theEditors) {
        this.editors = theEditors;
    }

    public void setPublisherWebsite(String theWebsite) {
        this.publisher_website = theWebsite;
    }

    public void setSerie(String theSerie) {
        this.serie = cleanSQLString(theSerie);
    }

    public void setISSN(String theISSN) {
        this.ISSN = cleanSQLString(theISSN);
    }

    public void setISSNe(String theISSN) {
        this.ISSNe = cleanSQLString(theISSN);
    }

    public void setVolume(String theVolume) {
        this.volume = theVolume;
    }

    public void setNumber(String theNumber) {
        this.number = theNumber;
    }

    public void setMonth(String theMonth) {
        this.month = cleanSQLString(theMonth);
    }

    public void setSupportType(String theType) {
        this.support_type = cleanSQLString(theType);
    }

    public void setVersion(String theVersion) {
        this.version = cleanSQLString(theVersion);
    }

    public void setSmallImageURL(String url) {
        this.smallImageURL = url;
    }

    public void setLargeImageURL(String url) {
        this.largeImageURL = url;
    }

    public void setPublisherPlace(String p) {
        this.publisherPlace = p;
    }

    public void setCategories(List<String> cat) {
        this.categories = cat;
    }

    public void addCategory(String cat) {
        if (categories == null) {
            categories = new ArrayList<String>();
        }
        categories.add(cat);
    }

    public void setNbPages(int nb) {
        this.nbPages = nb;
    }

    public void setReview(String rev) {
        this.review = rev;
    }

    public void setType(String t) {
        this.type = t;
    }

    public void setTypeDescription(String t) {
        typeDescription = t;
    }

    public void setBookType(String bt) {
        this.book_type = bt;
    }

    public void setDOI(String id) {
        DOI = id;
    } //{ DOI = cleanDOI(id); } 

    public void setInDOI(String id) {
        inDOI = id;
    }

    public void setArticleTitle(String ti) {
        articleTitle = ti;
    }

    public void setBeginPage(int p) {
        beginPage = p;
    }

    public void setEndPage(int p) {
        endPage = p;
    }

    public void setYear(String y) {
        year = y;
    }

    public void setAbstract(String a) {
        abstract_ = cleanAbstract(a);
    }

    public void setLocationPublisher(String s) {
        locationPublisher = s;
    }

    public void setSerieTitle(String s) {
        serieTitle = s;
    }

    public void setAuthorString(String s) {
        authorString = s;
    }

    public void setURL(String s) {
        url = s;
    }

    public void setURI(String s) {
        uri = s;
    }

    public void setConfidence(String s) {
        confidence = s;
    }

    public void setConf(double b) {
        conf = b;
    }

    public void setFullAuthors(List<org.grobid.core.data.Person> full) {
        fullAuthors = full;
    }

    public void setFullEditors(List<org.grobid.core.data.Person> full) {
        fullEditors = full;
    }

    public void setFullAffiliations(List<org.grobid.core.data.Affiliation> full) {
        fullAffiliations = full;
    }

    // temp
    public void setAuthors(String aut) {
        authors = aut;
    }

    public void addAuthor(String aut) {
        if (authors == null)
            authors = aut;
        else
            authors += " ; " + aut;

        if (authorList == null)
            authorList = new ArrayList<String>();
        if (!authorList.contains(aut))
            authorList.add(aut);
    }

    public void addFullAuthor(Person aut) {
        if (fullAuthors == null)
            fullAuthors = new ArrayList<Person>();
        if (!fullAuthors.contains(aut))
            fullAuthors.add(aut);
    }

    public void addFullEditor(Person aut) {
        if (fullEditors == null)
            fullEditors = new ArrayList<Person>();
        if (!fullEditors.contains(aut))
            fullEditors.add(aut);
    }

    public void addEditor(String aut) {
        if (editors == null)
            editors = aut;
        else
            editors += " ; " + aut;

        if (editorList == null)
            editorList = new ArrayList<String>();
        if (!editorList.contains(aut))
            editorList.add(aut);
    }

    public void setLocation(String loc) {
        location = loc;
    }

    public void setBookTitle(String book) {
        bookTitle = book;
    }

    public void setPageRange(String pages) {
        pageRange = pages;
    }

    public void setJournal(String jour) {
        journal = jour;
    }

    public void setVolumeBlock(String vol, boolean postProcess) {
        volumeBlock = vol;
        if (postProcess)
            volumeBlock = postProcessVolumeBlock();
    }

    public void setInstitution(String inst) {
        institution = inst;
    }

    public void setNote(String not) {
        note = not;
    }

    public void setAffiliation(String a) {
        affiliation = a;
    }

    public void setAddress(String a) {
        address = a;
    }

    public void setCountry(String a) {
        country = a;
    }

    public void setTown(String a) {
        town = a;
    }

    public void setEmail(String e) {
        email = e;
    }

    public void setPubnum(String p) {
        pubnum = p;
    }

    public void setKeyword(String k) {
        keyword = cleanKeywords(k);
    }

    public void addKeyword(String k) {
        if (keywords == null)
            keywords = new ArrayList<Keyword>();
		String theKey = cleanKeywords(k);
		if (theKey.toLowerCase().contains("introduction")) {
			// if the keyword contains introduction, this is normally a segmentation error
			theKey = null;
		}
		if (theKey != null) {
        	keywords.add(new Keyword(theKey));
		}
    }

    public void setKeywords(List<Keyword> k) {
        keywords = k;
    }

    public void addSubject(String k) {
        if (subjects == null)
            subjects = new ArrayList<String>();
        subjects.add(k);
    }

    public void setSubjects(List<String> k) {
        subjects = k;
    }

    public void setPhone(String p) {
        phone = p;
    }

    public void setDegree(String d) {
        degree = d;
    }

    public void setWeb(String w) {
        web = w;
    }

    public void setIssue(String i) {
        issue = i;
    }

    public void setJournalAbbrev(String j) {
        journal_abbrev = j;
    }

    public void setEvent(String e) {
        event = e;
    }

    public void setError(boolean e) {
        crossrefError = e;
    }

    public void setAbstractHeader(String a) {
        abstractHeader = a;
    }

    public void setPath(String p) {
        path = p;
    }

    public void setDay(String d) {
        day = d;
    }

    public void setE_Year(String d) {
        e_year = d;
    }

    public void setE_Month(String d) {
        e_month = d;
    }

    public void setE_Day(String d) {
        e_day = d;
    }

    public void setA_Year(String d) {
        a_year = d;
    }

    public void setA_Month(String d) {
        a_month = d;
    }

    public void setA_Day(String d) {
        a_day = d;
    }

    public void setS_Year(String d) {
        s_year = d;
    }

    public void setS_Month(String d) {
        s_month = d;
    }

    public void setS_Day(String d) {
        s_day = d;
    }

    public void setDedication(String d) {
        dedication = d;
    }

    public void setSubmission(String s) {
        submission = s;
    }

    public void setEnglishTitle(String d) {
        english_title = d;
    }

    public void setSubmissionDate(String d) {
        submission_date = d;
    }

    public void setNormalizedSubmissionDate(Date d) {
        normalized_submission_date = d;
    }

    public void setOriginalAffiliation(String original) {
        originalAffiliation = original;
    }

    public void setOriginalAbstract(String original) {
        originalAbstract = original;
    }

    public void setOriginalAuthors(String original) {
        originalAuthors = original;
    }

    public void setOriginalTitle(String original) {
        originalTitle = original;
    }

    public void setOriginalAddress(String original) {
        originalAddress = original;
    }

    public void setOriginalNote(String original) {
        originalNote = original;
    }

    public void setOriginalKeyword(String original) {
        originalKeyword = original;
    }

    public void setOriginalVolumeBlock(String original) {
        originalVolumeBlock = original;
    }

    public void setOriginalJournal(String original) {
        originalJournal = original;
    }

    public void setReference(String ref) {
        reference = ref;
    }

    public void setCopyright(String cop) {
        copyright = cop;
    }

    public void setGrant(String gra) {
        grant = gra;
    }

    /**
     * General string cleaining for SQL strings. This method might depend on the chosen
     * relation database.
     */
    public static String cleanSQLString(String str) {
        if (str == null)
            return null;
        if (str.length() == 0)
            return null;
        String cleanedString = "";
        boolean special = false;
        for (int index = 0; (index < str.length()); index++) {
            char currentCharacter = str.charAt(index);
            if ((currentCharacter == '\'') || (currentCharacter == '%') || (currentCharacter == '_')) {
                special = true;
                cleanedString += '\\';
            }
            cleanedString += currentCharacter;
        }

        return cleanedString;
    }

    /**
     * Special string cleaining of ISBN and ISSN numbers.
     */
    public static String cleanISBNString(String str) {
        String cleanedString = "";
        for (int index = 0; (index < str.length()); index++) {
            char currentCharacter = str.charAt(index);
            if ((currentCharacter != '-') && (currentCharacter != ' ') && (currentCharacter != '\''))
                cleanedString += currentCharacter;
        }

        return cleanedString;
    }

    /**
     * Reinit all the values of the current bibliographical item
     */
    public void reset() {
        ISBN13 = null;
        ISBN10 = null;
        title = null;
        publisher = null;
        edition = null;
        language = null;
        subtitle = null;
        publication_date = null;
        normalized_publication_date = null;
        editors = null;
        publisher_website = null;
        serie = null;
        ISSN = null;
        ISSNe = null;
        volume = null;
        number = null;
        month = null;
        support_type = null;
        version = null;
        smallImageURL = null;
        largeImageURL = null;
        publisherPlace = null;
        review = null;
        categories = null;
        nbPages = -1;
        type = null;
        book_type = null;
        DOI = null;
        abstract_ = null;

        authors = null;
        location = null;
        bookTitle = null;
        pageRange = null;
        journal = null;
        volumeBlock = null;
        institution = null;
        note = null;
        affiliation = null;
        address = null;
        email = null;
        pubnum = null;
        keyword = null;
        phone = null;
        degree = null;
        web = null;
        issue = null;
        journal_abbrev = null;
        event = null;
        day = null;
        submission_date = null;
        normalized_submission_date = null;

        beginPage = -1;
        endPage = -1;
        articleTitle = null;
        dedication = null;
        submission = null;
        english_title = null;

        fullAuthors = null;
        fullAffiliations = null;
        reference = null;
        copyright = null;
        grant = null;
    }

    /**
     * Post process the volume block in order to distinguish when
     * possible and when appropriate volume and issue
     */
    public String postProcessVolumeBlock() {
        if (volumeBlock == null) {
            return null;
        }
        if (volumeBlock.length() == 0) {
            return volumeBlock;
        }
        volumeBlock = volumeBlock.trim();

        // the volume is always the first full number sequence of the block
        // first we remove the possible non digit character prefix
        boolean stop = false;
        int p = 0;
        while (!stop && (p < volumeBlock.length())) {
            if (Character.isDigit(volumeBlock.charAt(p)))
                stop = true;
            else
                p++;
        }

        if (!stop)
            return volumeBlock; // we just have letters... we can't do anything

        int i = p;
        stop = false;
        while (!stop && (i < volumeBlock.length())) {
            if (!Character.isDigit(volumeBlock.charAt(i)))
                stop = true;
            else
                i++;
        }

        String resVolume = null;
        if (stop)
            resVolume = volumeBlock.substring(p, i);
        else
            return volumeBlock.substring(p);

        // we then have at least one non numerical character
        stop = false;
        while (!stop && (i < volumeBlock.length())) {
            if (Character.isDigit(volumeBlock.charAt(i)))
                stop = true;
            else
                i++;
        }

        if (!stop)
            return resVolume;

        // if present, the second number sequence is the issue 
        stop = false;
        int j = i + 1;
        while (!stop && (j < volumeBlock.length())) {
            if (!Character.isDigit(volumeBlock.charAt(j)))
                stop = true;
            else
                j++;
        }

        if (!stop)
            j = volumeBlock.length();

        issue = volumeBlock.substring(i, j);
        return resVolume;
    }

    /**
     * Some little cleaning of the abstract field.
     */
    final String[] ABSTRACT_PREFIXES = {"abstract", "summary", "résumé", "abrégé", "a b s t r a c t"};

    public String cleanAbstract(String string) {

        if (string == null)
            return null;
        if (string.length() == 0)
            return string;
        String res = string.trim();
        String res0 = res.toLowerCase();

        for (String abstractPrefix : ABSTRACT_PREFIXES) {
            if (res0.startsWith(abstractPrefix)) {
                if (abstractPrefix.length() < res.length()) {
                    res = res.substring(abstractPrefix.length(), res.length());
                    res.trim();
                } else {
                    res = "";
                }
                abstractHeader = abstractPrefix;
                break;
            }
        }

        if ((res.startsWith(".")) || (res.startsWith(":")) || (res.startsWith(")"))) {
            res = res.substring(1, res.length());
            res = res.trim();
        }

        res = res.replace("@BULLET", " • ");

        res = res.replace("( ", "(");
        res = res.replace(" )", ")");
        res = res.replace("  ", " ");

        return res;
    }

    static public void cleanTitles(BiblioItem bibl) {
        if (bibl.getTitle() != null) {
            String localTitle = TextUtilities.cleanField(bibl.getTitle(), false);
            if (localTitle.endsWith(" y")) {
                // some markers at the end of the title are extracted from the pdf as " y" at the end of the title
                // e.g. <title level="a" type="main">Computations in finite-dimensional Lie algebras y</title>
                localTitle = localTitle.substring(0, localTitle.length() - 2);
            }
            bibl.setTitle(localTitle);
        }
        if (bibl.getBookTitle() != null) {
            bibl.setBookTitle(TextUtilities.cleanField(bibl.getBookTitle(), false));
        }
    }

    private String cleanDOI(String bibl) {
        if (bibl != null) {
            bibl = bibl.replace(" ", "");
            if (bibl.startsWith("DOI:") || bibl.startsWith("DOI/")) {
                bibl = bibl.substring(0, 4);
            } else if (bibl.startsWith("DOI")) {
                bibl = bibl.substring(0, 3);
            }
        }
        return bibl;
    }

    /**
     * Some little cleaning of the keyword field.
     */
    public static String cleanKeywords(String string) {
        if (string == null)
            return null;
        if (string.length() == 0)
            return string;
        String res = string.trim();
        String resLow = res.toLowerCase();
        if (resLow.startsWith("keywords")) {
            res = res.substring(8);
        } else if (resLow.startsWith("key words") || resLow.startsWith("mots clés") || resLow.startsWith("mots cles")) {
            res = res.substring(9);
        } else if (resLow.startsWith("mots clefs")) {
            res = res.substring(10);
        }
		
        res = res.trim();
        if (res.startsWith(":") || res.startsWith("—") || res.startsWith("-")) {
            res = res.substring(1);
        }
        if (res.endsWith(".")) {
            res = res.substring(0, res.length() - 1);
        }

        return res.trim();
    }

    /**
     * Keyword field segmentation.
     */
    public static List<Keyword> segmentKeywords(String string) {
        if (string == null)
            return null;
        if (string.length() == 0)
            return null;
		String type = null;
        if (string.startsWith("Categories and Subject Descriptors")) {
            type = "subject-headers";
			 string = string.replace("Categories and Subject Descriptors", "").trim();
        } 
		else if (string.startsWith("PACS Numbers") || 
				   string.startsWith("PACS") ) {
            type = "pacs";
            string = string.replace("PACS Numbers", "").replace("PACS", "").trim();
			if (string.startsWith(":")) {
	            string = string.substring(1);
			}
        }
		else {
			type = "author";
		}
		
		List<Keyword> result = new ArrayList<Keyword>();
		
		// the list of possible keyword separators
		List<String> separators = Arrays.asList(";","•", "Á", "\n", ",");
		
		for(String separator : separators) {
	        StringTokenizer st = new StringTokenizer(string, separator);
	        if (st.countTokens() > 2) {
	            while (st.hasMoreTokens()) {
					String res = st.nextToken().trim();
					if (res.startsWith(":")) {
			            res = res.substring(1);
			        }
					res = res.replace("\n", " ").replace("  ", " ");
					Keyword keyw = new Keyword(res, type);
					result.add(keyw);
	            }
				break;
	        }
		}
		
		return result;
	}	

    /**
     * Export to BibTeX format
     */
    public String toBibTeX() {
		return toBibTeX("id");
	}

    /**
     * Export to BibTeX format
     */
    public String toBibTeX(String id) {
        String bibtex = "";
        try {

            if (journal != null) {
                bibtex += "@article{" + id + ",\n";
            } else if (book_type != null) {
                bibtex += "@techreport{" + id + ",\n";
            } else if (bookTitle != null) {
                if ((bookTitle.startsWith("proc")) || (bookTitle.startsWith("Proc")) ||
                        (bookTitle.startsWith("In Proc")) || (bookTitle.startsWith("In proc"))) {
                    bibtex += "@inproceedings{" + id + ",\n";
                } else {
                    bibtex += "@article{" + id + ",\n"; // ???
                }
            } else {
                bibtex += "@misc{" + id + ",\n"; // ???
            }

            // author 
            // fullAuthors has to be used instead
            if (fullAuthors != null) {
                if (fullAuthors.size() > 0) {
                    boolean begin = true;
                    for (Person person : fullAuthors) {
                        if (begin) {
                            bibtex += "author\t=\t\"" + person.getFirstName() + " " + person.getLastName();
                            begin = false;
                        } else
                            bibtex += " and " + person.getFirstName() + " " + person.getLastName();
                    }
                    bibtex += "\"";
                }
            } else if (authors != null) {
                StringTokenizer st = new StringTokenizer(authors, ";");
                if (st.countTokens() > 1) {
                    boolean begin = true;
                    while (st.hasMoreTokens()) {
                        String author = st.nextToken();
                        if (author != null)
                            author = author.trim();
                        if (begin) {
                            bibtex += "author\t=\t\"" + author;
                            begin = false;
                        } else
                            bibtex += " and " + author;

                    }
                    bibtex += "\"";
                } else {
                    if (authors != null)
                        bibtex += "author\t=\t\"" + authors + "\"";
                }
            }

            // title
            if (title != null) {
                bibtex += ",\ntitle\t=\t\"" + title + "\"";
            }

            // journal
            if (journal != null) {
                bibtex += ",\njournal\t=\t\"" + journal + "\"";
            }

            // booktitle
            if ((journal == null) && (book_type == null) && (bookTitle != null)) {
                bibtex += ",\nbooktitle\t=\t\"" + bookTitle + "\"";
            }

            // publisher
            if (publisher != null) {
                bibtex += ",\npublisher\t=\t\"" + publisher + "\"";
            }

            // editors
            if (editors != null) {
                String locEditors = editors.replace(" ; ", " and ");
                bibtex += ",\neditor\t=\t\"" + locEditors + "\"";
            }
            // fullEditors has to be used instead

            // year
            if (publication_date != null) {
                bibtex += ",\nyear\t=\t\"" + publication_date + "\"";
            }

            // location
            if (location != null) {
                bibtex += ",\naddress\t=\t\"" + location + "\"";
            }

            // pages
            if (pageRange != null) {
                bibtex += ",\npages\t=\t\"" + pageRange + "\"";
            }

			// volume
			if (volumeBlock != null) {
				bibtex += ",\nvolume\t=\t\"" + volumeBlock + "\"";
			}

			// issue (named number in BibTeX)
			if (issue != null) {
				bibtex += ",\nnumber\t=\t\"" + issue + "\"";
			}
			
            // abstract
            if (abstract_ != null) {
                if (abstract_.length() > 0) {
                    bibtex += ",\nabstract\t=\t\"" + abstract_ + "\"";
                }
            }

            // keywords
            if (keywords != null) {
                bibtex += ",\nkeywords\t=\t\"";
                boolean begin = true;
                for (Keyword keyw : keywords) {
					if ( (keyw.getKeyword() == null) || (keyw.getKeyword().length() == 0) )
						continue;
                    if (begin) {
                        begin = false;
                        bibtex += keyw.getKeyword();
                    } else
                        bibtex += ", " + keyw.getKeyword();
                }
                bibtex += "\"";
            }

            bibtex += "\n}\n";
        } catch (Exception e) {
            throw new GrobidException("Cannot export BibTex format, because of nested exception.", e);
        }
        return bibtex;
    }

    /**
     * Export the bibliographical item into a TEI BiblStruct string
     *
     * @param n - the index of the bibliographical record, the corresponding id will be b+n
     */

    public String toTEI(int n) {
        return toTEI(n, 0, GrobidAnalysisConfig.defaultInstance());
    }

    public String toTEI(int n, int indent) {
        return toTEI(n, indent, GrobidAnalysisConfig.defaultInstance());
    }


    /**
     * Export the bibliographical item into a TEI BiblStruct string
     *
     * @param n      - the index of the bibliographical record, the corresponding id will be b+n
     * @param indent - the tabulation indentation for the output of the xml elements
     */
    public String toTEI(int n, int indent, GrobidAnalysisConfig config) {
        StringBuilder tei = new StringBuilder();
        boolean generateIDs = config.isGenerateTeiIds();

        try {
            // we just produce here xml strings
            for (int i = 0; i < indent; i++) {
                tei.append("\t");
            }
            tei.append("<biblStruct");
            tei.append(" ").append(TEIFormater.getCoordsAttribute(coordinates, config.isGenerateTeiCoordinates())).append(" ");
            if (language != null) {
                if (n == -1) {
                    if (pubnum != null) {
                        teiId = TextUtilities.HTMLEncode(pubnum);
                        tei.append(" xml:lang=\"" + language + "\" xml:id=\"" + teiId + "\">\n");
                    } else
                        tei.append(" xml:lang=\"" + language + ">\n");
                } else {
                    teiId = "b" + n;
                    tei.append(" xml:lang=\"" + language + "\" xml:id=\"" + teiId + "\">\n");
                }
                // TBD: the language should be normalized following xml lang attributes !
            } else {
                if (n == -1) {
                    if (pubnum != null) {
                        teiId = TextUtilities.HTMLEncode(pubnum);
                        tei.append(" xml:id=\"" + teiId + "\">\n");
                    } else
                        tei.append(">\n");
                } else {
                    teiId = "b" + n;
                    tei.append(" xml:id=\"" + teiId + "\">\n");
                }
            }

            if (teiId != null) {

            }

            if ((bookTitle == null) && (journal == null)) {
                for (int i = 0; i < indent + 1; i++) {
                    tei.append("\t");
                }
                tei.append("<monogr>\n");
            } else {
                for (int i = 0; i < indent + 1; i++) {
                    tei.append("\t");
                }
                tei.append("<analytic>\n");
            }

            // title
            for (int i = 0; i < indent + 2; i++) {
                tei.append("\t");
            }
            if (title != null) {
                tei.append("<title");
                if ((bookTitle == null) && (journal == null)) {
                    tei.append(" level=\"m\" type=\"main\"");
                } else
                    tei.append(" level=\"a\" type=\"main\"");
				if (generateIDs) {
					String divID = KeyGen.getKey().substring(0,7);
					tei.append(" xml:id=\"_" + divID + "\"");
				}
                // here check the language ?
                if (english_title == null) {
                    tei.append(">").append(TextUtilities.HTMLEncode(title)).append("</title>\n");
                } else {
                    tei.append(" xml:lang=\"").append(language)
						.append("\">").append(TextUtilities.HTMLEncode(title)).append("</title>\n");
                }
            }
			else {
                tei.append("<title/>\n");
			}
            boolean hasEnglishTitle = false;
            if (english_title != null) {
                // here do check the language !
                Language resLang = languageUtilities.runLanguageId(english_title);

                if (resLang != null) {
                    String resL = resLang.getLangId();
                    if (resL.equals(Language.EN)) {
                        hasEnglishTitle = true;
                        for (int i = 0; i < indent + 2; i++) {
                            tei.append("\t");
                        }
                        tei.append("<title");
                        if ((bookTitle == null) && (journal == null)) {
                            tei.append(" level=\"m\"");
                        } else {
                            tei.append(" level=\"a\"");
                        }
						if (generateIDs) {
							String divID = KeyGen.getKey().substring(0,7);
							tei.append(" xml:id=\"_" + divID + "\"");
						}
                        tei.append(" xml:lang=\"en\">")
							.append(TextUtilities.HTMLEncode(english_title)).append("</title>\n");
                    }
                }
                // if it's not something in English, we will write it anyway as note without type at the end
            }

            tei.append(toTEIAuthorBlock(2));

            if ((bookTitle != null) || (journal != null)) {
                for (int i = 0; i < indent + 1; i++) {
                    tei.append("\t");
                }
                tei.append("</analytic>\n");
                for (int i = 0; i < indent + 1; i++) {
                    tei.append("\t");
                }
                tei.append("<monogr>\n");
            }

            if (bookTitle != null) {
                for (int i = 0; i < indent + 2; i++) {
                    tei.append("\t");
                }
                tei.append("<title level=\"m\"");
				if (generateIDs) {
					String divID = KeyGen.getKey().substring(0,7);
					tei.append(" xml:id=\"_" + divID + "\"");
				}
				tei.append(">" + TextUtilities.HTMLEncode(bookTitle) + "</title>\n");

                if (editors != null) {
                    //postProcessingEditors();

                    StringTokenizer st = new StringTokenizer(editors, ";");
                    if (st.countTokens() > 0) {
                        while (st.hasMoreTokens()) {
                            String editor = st.nextToken();
                            if (editor != null)
                                editor = editor.trim();
                            for (int i = 0; i < indent + 2; i++) {
                                tei.append("\t");
                            }
                            tei.append("<editor>" + TextUtilities.HTMLEncode(editor) + "</editor>\n");
                        }
                    } else {
                        if (editors != null)
                            for (int i = 0; i < indent + 2; i++) {
                                tei.append("\t");
                            }
                        tei.append("<editor>" + TextUtilities.HTMLEncode(editors) + "</editor>\n");
                    }
                }

                // in case the booktitle corresponds to a proceedings, we can try to indidate the meeting title
                String meeting = bookTitle;
                boolean meetLoc = false;
                if (event != null)
                    meeting = event;
                else {
                    meeting = meeting.trim();
                    for (String prefix : confPrefixes) {
                        if (meeting.startsWith(prefix)) {
                            meeting = meeting.replace(prefix, "");
                            meeting = meeting.trim();
                            for (int i = 0; i < indent + 2; i++) {
                                tei.append("\t");
                            }
                            tei.append("<meeting>" + TextUtilities.HTMLEncode(meeting));
                            if ((location != null) || (town != null) || (country != null)) {
                                tei.append("<address>");
                                if (town != null) {
                                    tei.append("<settlement>" + town + "</settlement>");
                                }
                                if (country != null) {
                                    tei.append("<country>" + country + "</country>");
                                }
                                if ((location != null) && (town == null) && (country == null)) {
                                    tei.append("<addrLine>" + TextUtilities.HTMLEncode(location) + "</addrLine>");
                                }
                                tei.append("</address>");
                                meetLoc = true;
                            }
                            tei.append("</meeting>\n");
                            break;
                        }
                        //break;
                    }
                }

                if (((location != null) || (town != null) || (country != null)) && (!meetLoc)) {
                    for (int i = 0; i < indent + 2; i++) {
                        tei.append("\t");
                    }
                    tei.append("<meeting>");
                    tei.append("<address>");
                    if (town != null) {
                        tei.append("<settlement>" + town + "</settlement>");
                    }
                    if (country != null) {
                        tei.append("<country>" + country + "</country>");
                    }
                    if ((location != null) && (town == null) && (country == null)) {
                        tei.append("<addrLine>" + TextUtilities.HTMLEncode(location) + "</addrLine>");
                    }
                    tei.append("</address>");
                    tei.append("</meeting>\n");
                }

                for (int i = 0; i < indent + 2; i++) {
                    tei.append("\t");
                }
                if ((publication_date != null) || (pageRange != null) || (publisher != null)) {   
                    tei.append("<imprint>\n");
                }
				else 
					tei.append("<imprint/>\n");
				
                if (publisher != null) {
                    for (int i = 0; i < indent + 3; i++) {
                        tei.append("\t");
                    }
                    tei.append("<publisher>" + TextUtilities.HTMLEncode(publisher) + "</publisher>\n");
                }

                if (normalized_publication_date != null) {
                    if ((normalized_publication_date.getDay() != -1) ||
                            (normalized_publication_date.getMonth() != -1) ||
                            (normalized_publication_date.getYear() != -1)) {
                        int year = normalized_publication_date.getYear();
                        int month = normalized_publication_date.getMonth();
                        int day = normalized_publication_date.getDay();
						
						if (year != -1) {
			                String when = "";
							if (year <= 9) 
								when += "000" + year;
							else if (year <= 99) 
								when += "00" + year;
							else if (year <= 999)
								when += "0" + year;
							else
								when += year;
			                if (month != -1) {
								if (month <= 9) 
									when += "-0" + month;
								else 
			 					   	when += "-" + month;
			                    if (day != -1) {
									if (day <= 9)
										when += "-0" + day;
									else
										when += "-" + day;
			                    }
			                }
	                        for (int i = 0; i < indent + 3; i++) {
	                            tei.append("\t");
	                        }
	                        tei.append("<date type=\"published\" when=\"");
	                        tei.append(when + "\" />\n");
						}
                    } else if (this.getYear() != null) {
						String when = "";
						if (this.getYear().length() == 1)
							when += "000" + this.getYear();
						else if (this.getYear().length() == 2)
							when += "00" + this.getYear();
						else if (this.getYear().length() == 3)
							when += "0" + this.getYear();
						else if (this.getYear().length() == 4)
							when += this.getYear();
				
		                if (this.getMonth() != null) {
							if (this.getMonth().length() == 1)
								when += "-0" + this.getMonth();
							else
								when += "-" + this.getMonth();
		                    if (this.getDay() != null) {
								if (this.getDay().length() == 1)
									when += "-0" + this.getDay();
								else
									when += "-" + this.getDay();
		                    }
		                }
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<date type=\"published\" when=\"");
                        tei.append(when + "\" />\n");
                    } else {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n");
                    }
                } else if (publication_date != null) {
                    for (int i = 0; i < indent + 3; i++) {
                        tei.append("\t");
                    }
                    tei.append("<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n");
                }

                if (pageRange != null) {
                    StringTokenizer st = new StringTokenizer(pageRange, "--");
                    if (st.countTokens() == 2) {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
						tei.append("<biblScope unit=\"page\" from=\"" + TextUtilities.HTMLEncode(st.nextToken()) + "\" to=\"" 
								+ TextUtilities.HTMLEncode(st.nextToken()) + "\" />\n");  
                    } else {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<biblScope unit=\"page\">" + TextUtilities.HTMLEncode(pageRange) + "</biblScope>\n");
                    }
                }
                if ((publication_date != null) || (pageRange != null) || (publisher != null)) {
	                for (int i = 0; i < indent + 2; i++) {
	                    tei.append("\t");
	                }
                    tei.append("</imprint>\n");
                }
            } else if (journal != null) {
                for (int i = 0; i < indent + 2; i++) {
                    tei.append("\t");
                }
                tei.append("<title level=\"j\"");
				if (generateIDs) {
					String divID = KeyGen.getKey().substring(0,7);
					tei.append(" xml:id=\"_" + divID + "\"");
				}	
				tei.append(">" + TextUtilities.HTMLEncode(journal) + "</title>\n");

                if (getJournalAbbrev() != null) {
                    for (int i = 0; i < indent + 2; i++) {
                        tei.append("\t");
                    }
                    tei.append("<title level=\"j\" type=\"abbrev\">"
                            + TextUtilities.HTMLEncode(getJournalAbbrev()) + "</title>\n");
                }

                if (editors != null) {
                    //postProcessingEditors();

                    StringTokenizer st = new StringTokenizer(editors, ";");
                    if (st.countTokens() > 0) {
                        while (st.hasMoreTokens()) {
                            String editor = st.nextToken();
                            if (editor != null) {
                                for (int i = 0; i < indent + 2; i++) {
                                    tei.append("\t");
                                }
                                editor = editor.trim();
                                tei.append("<editor>" + TextUtilities.HTMLEncode(editor) + "</editor>\n");
                            }
                        }
                    } else {
                        if (editors != null) {
                            for (int i = 0; i < indent + 2; i++) {
                                tei.append("\t");
                            }
                            tei.append("<editor>" + TextUtilities.HTMLEncode(editors) + "</editor>\n");
                        }
                    }
                }

                if (getISSN() != null) {
                    for (int i = 0; i < indent + 2; i++) {
                        tei.append("\t");
                    }
                    tei.append("<idno type=\"ISSN\">" + getISSN() + "</idno>\n");
                }

                if (getISSNe() != null) {
                    if (!getISSNe().equals(getISSN())) {
                        for (int i = 0; i < indent + 2; i++) {
                            tei.append("\t");
                        }
                        tei.append("<idno type=\"ISSNe\">" + getISSNe() + "</idno>\n");
                    }
                }

                for (int i = 0; i < indent + 2; i++) {
                    tei.append("\t");
                }
                if ((volumeBlock != null) | (issue != null) || (pageRange != null) || (publication_date != null)
                        || (publisher != null)) {
					tei.append("<imprint>\n");
                    if (publisher != null) {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<publisher>" + TextUtilities.HTMLEncode(publisher) + "</publisher>\n");
                    }
                    if (volumeBlock != null) {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<biblScope unit=\"volume\">" + TextUtilities.HTMLEncode(volumeBlock) + "</biblScope>\n");
                    }
                    if (issue != null) {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<biblScope unit=\"issue\">" + TextUtilities.HTMLEncode(issue) + "</biblScope>\n");
                    }
                    if (pageRange != null) {
                        StringTokenizer st = new StringTokenizer(pageRange, "--");
                        if (st.countTokens() == 2) {
                            for (int i = 0; i < indent + 3; i++) {
                                tei.append("\t");
                            }
							tei.append("<biblScope unit=\"page\" from=\"" + 
								TextUtilities.HTMLEncode(st.nextToken()) + "\" to=\"" + 
								TextUtilities.HTMLEncode(st.nextToken()) + "\" />\n");
                        } else {
                            for (int i = 0; i < indent + 3; i++) {
                                tei.append("\t");
                            }
                            tei.append("<biblScope unit=\"page\">" + TextUtilities.HTMLEncode(pageRange) + "</biblScope>\n");
                        }
                    }

                    // date
                    if (normalized_publication_date != null) {
                        if ((normalized_publication_date.getDay() != -1) ||
                                (normalized_publication_date.getMonth() != -1) ||
                                (normalized_publication_date.getYear() != -1)) {
                            int year = normalized_publication_date.getYear();
                            int month = normalized_publication_date.getMonth();
                            int day = normalized_publication_date.getDay();
							
							if (year != -1) {
				                String when = "";
								if (year <= 9) 
									when += "000" + year;
								else if (year <= 99) 
									when += "00" + year;
								else if (year <= 999)
									when += "0" + year;
								else
									when += year;
				                if (month != -1) {
									if (month <= 9) 
										when += "-0" + month;
									else 
				 					   	when += "-" + month;
				                    if (day != -1) {
										if (day <= 9)
											when += "-0" + day;
										else
											when += "-" + day;
				                    }
				                }
	                            for (int i = 0; i < indent + 3; i++) {
	                                tei.append("\t");
	                            }
	                            tei.append("<date type=\"published\" when=\"");
	                            tei.append(when + "\" />\n");
							}
                        } else if (this.getYear() != null) {
							String when = "";
							if (this.getYear().length() == 1)
								when += "000" + this.getYear();
							else if (this.getYear().length() == 2)
								when += "00" + this.getYear();
							else if (this.getYear().length() == 3)
								when += "0" + this.getYear();
							else if (this.getYear().length() == 4)
								when += this.getYear();
				
			                if (this.getMonth() != null) {
								if (this.getMonth().length() == 1)
									when += "-0" + this.getMonth();
								else
									when += "-" + this.getMonth();
			                    if (this.getDay() != null) {
									if (this.getDay().length() == 1)
										when += "-0" + this.getDay();
									else
										when += "-" + this.getDay();
			                    }
			                }
                            for (int i = 0; i < indent + 3; i++) {
                                tei.append("\t");
                            }
                            tei.append("<date type=\"published\" when=\"");
                            tei.append(when + "\" />\n");
                        } else {
                            for (int i = 0; i < indent + 3; i++) {
                                tei.append("\t");
                            }
                            tei.append("<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n");
                        }
                    } else if (publication_date != null) {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n");
                    }

                    if (getPublisher() != null) {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<publisher>" + TextUtilities.HTMLEncode(getPublisher()) + "</publisher>\n");
                    }
                    for (int i = 0; i < indent + 2; i++) {
                        tei.append("\t");
                    }
                    tei.append("</imprint>\n");
                }
				else {
					tei.append("<imprint/>\n");
				}
            } else {
                // not a journal and not something in a book...
                if (editors != null) {
                    //postProcessingEditors();

                    StringTokenizer st = new StringTokenizer(editors, ";");
                    if (st.countTokens() > 0) {
                        while (st.hasMoreTokens()) {
                            String editor = st.nextToken();
                            if (editor != null) {
                                editor = editor.trim();
                                for (int i = 0; i < indent + 2; i++) {
                                    tei.append("\t");
                                }
                                tei.append("<editor>" + TextUtilities.HTMLEncode(editor) + "</editor>\n");
                            }
                        }
                    } else {
                        if (editors != null) {
                            for (int i = 0; i < indent + 2; i++) {
                                tei.append("\t");
                            }
                            tei.append("<editor>" + TextUtilities.HTMLEncode(editors) + "</editor>\n");
                        }
                    }
                }

                for (int i = 0; i < indent + 2; i++) {
                    tei.append("\t");
                }
                if ((publication_date != null) || (pageRange != null) || (location != null) || (publisher != null)) {
                    tei.append("<imprint>\n");
                }
				else {
					tei.append("<imprint/>\n");
				}
                // date
                if (normalized_publication_date != null) {
                    if ((normalized_publication_date.getDay() != -1) |
                            (normalized_publication_date.getMonth() != -1) |
                            (normalized_publication_date.getYear() != -1)) {
                        int year = normalized_publication_date.getYear();
                        int month = normalized_publication_date.getMonth();
                        int day = normalized_publication_date.getDay();

						if (year != -1) {
			                String when = "";
							if (year <= 9) 
								when += "000" + year;
							else if (year <= 99) 
								when += "00" + year;
							else if (year <= 999)
								when += "0" + year;
							else
								when += year;
			                if (month != -1) {
								if (month <= 9) 
									when += "-0" + month;
								else 
			 					   	when += "-" + month;
			                    if (day != -1) {
									if (day <= 9)
										when += "-0" + day;
									else
										when += "-" + day;
			                    }
			                }
	                        for (int i = 0; i < indent + 3; i++) {
	                            tei.append("\t");
	                        }
	                        tei.append("<date type=\"published\" when=\"");
	                        tei.append(when + "\" />\n");
						}
                    } else if (this.getYear() != null) {
						String when = "";
						if (this.getYear().length() == 1)
							when += "000" + this.getYear();
						else if (this.getYear().length() == 2)
							when += "00" + this.getYear();
						else if (this.getYear().length() == 3)
							when += "0" + this.getYear();
						else if (this.getYear().length() == 4)
							when += this.getYear();
				
		                if (this.getMonth() != null) {
							if (this.getMonth().length() == 1)
								when += "-0" + this.getMonth();
							else
								when += "-" + this.getMonth();
		                    if (this.getDay() != null) {
								if (this.getDay().length() == 1)
									when += "-0" + this.getDay();
								else
									when += "-" + this.getDay();
		                    }
		                }
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<date type=\"published\" when=\"");
                        tei.append(when + "\" />\n");
                    } else {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n");
                    }
                } else if (publication_date != null) {
                    for (int i = 0; i < indent + 3; i++) {
                        tei.append("\t");
                    }
                    tei.append("<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n");
                }

                if (publisher != null) {
                    for (int i = 0; i < indent + 3; i++) {
                        tei.append("\t");
                    }
                    tei.append("<publisher>" + TextUtilities.HTMLEncode(publisher) + "</publisher>\n");
                }
                if (pageRange != null) {
                    StringTokenizer st = new StringTokenizer(pageRange, "--");
                    if (st.countTokens() == 2) {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
						tei.append("<biblScope unit=\"page\" from=\"" + 
							TextUtilities.HTMLEncode(st.nextToken()) + 
								"\" to=\"" + TextUtilities.HTMLEncode(st.nextToken()) + "\" />\n");   	
                    } else {
                        for (int i = 0; i < indent + 3; i++) {
                            tei.append("\t");
                        }
                        tei.append("<biblScope unit=\"page\">" + TextUtilities.HTMLEncode(pageRange) + "</biblScope>\n");
                    }
                }
                if (location != null) {
                    for (int i = 0; i < indent + 3; i++) {
                        tei.append("\t");
                    }
                    tei.append("<pubPlace>" + TextUtilities.HTMLEncode(location) + "</pubPlace>\n");
                }

                if ((publication_date != null) || (pageRange != null) || (location != null) || (publisher != null)) {
                    for (int i = 0; i < indent + 2; i++) {
                        tei.append("\t");
                    }
                    tei.append("</imprint>\n");
                }
            }
            for (int i = 0; i < indent + 1; i++) {
                tei.append("\t");
            }
            tei.append("</monogr>\n");

            if (submission != null) {
                for (int i = 0; i < indent + 1; i++) {
                    tei.append("\t");
                }
                tei.append("<note type=\"submission\">" + TextUtilities.HTMLEncode(submission) + "</note>\n");
            }
            if (getSubmissionDate() != null) {
                for (int i = 0; i < indent + 1; i++) {
                    tei.append("\t");
                }
                tei.append("<date type=\"submission\">" + TextUtilities.HTMLEncode(getSubmissionDate()) + "</date>\n");
            }

            if (dedication != null) {
                tei.append("<note type=\"dedication\">" + TextUtilities.HTMLEncode(dedication) + "</note>\n");
            }

            if (note != null) {
                for (int i = 0; i < indent + 1; i++) {
                    tei.append("\t");
                }
                tei.append("<note>" + TextUtilities.HTMLEncode(note) + "</note>\n");
            }

            if ((english_title != null) && (!hasEnglishTitle)) {
                for (int i = 0; i < indent + 1; i++) {
                    tei.append("\t");
                }
                tei.append("<note>" + TextUtilities.HTMLEncode(english_title) + "</note>\n");
            }

            if (subjects != null) {
                if (subjects.size() > 0) {
                    for (int i = 0; i < indent + 1; i++) {
                        tei.append("\t");
                    }
                    tei.append("<keywords scheme=\"hal\"><list>\n");
                    for (String subject : subjects) {
                        for (int i = 0; i < indent + 2; i++) {
                            tei.append("\t");
                        }
                        tei.append("<item>" + subject + "</item>\n");
                    }
                    tei.append("</list></keywords>\n");
                }
            }

            // keywords here !!
            if (getKeyword() != null) {
                String keywords = getKeyword();
                if (keywords.startsWith("Categories and Subject Descriptors")) {
                    int start = keywords.indexOf("Keywords");
                    if (start != -1) {
                        String keywords1 = keywords.substring(0, start - 1);
                        String keywords2 = keywords.substring(start + 9, keywords.length());
                        for (int i = 0; i < indent + 1; i++) {
                            tei.append("\t");
                        }
                        tei.append("<keywords type=\"subject-headers\">" + keywords1 + "</keywords>\n");
                        for (int i = 0; i < indent + 1; i++) {
                            tei.append("\t");
                        }
                        tei.append("<keywords>" + TextUtilities.HTMLEncode(keywords2) + "</keywords>\n");
                    } else {
                        for (int i = 0; i < indent + 1; i++) {
                            tei.append("\t");
                        }
                        tei.append("<keywords>" + TextUtilities.HTMLEncode(getKeyword()) + "</keywords>\n");
                    }
                } else
                    for (int i = 0; i < indent + 1; i++) {
                        tei.append("\t");
                    }
                tei.append("<keywords>" + TextUtilities.HTMLEncode(getKeyword()) + "</keywords>\n");
            }

            if (DOI != null) {
                for (int i = 0; i < indent + 1; i++) {
                    tei.append("\t");
                }
                tei.append("<idno type=\"doi\">" + DOI + "</idno>\n");
            }

            if (uri != null) {
                if (uri.startsWith("http://hal.")) {
                    for (int i = 0; i < indent + 1; i++) {
                        tei.append("\t");
                    }
                    tei.append("<idno type=\"HALid\">" + uri + "</idno>\n");
                } else {
                    for (int i = 0; i < indent + 1; i++) {
                        tei.append("\t");
                    }
                    tei.append("<idno>" + uri + "</idno>\n");
                }
            }

            if (url != null) {
                if (url.startsWith("http://hal.")) {
                    for (int i = 0; i < indent + 1; i++) {
                        tei.append("\t");
                    }
                    tei.append("<idno type=\"HALFile\">" + url + "</idno>\n");
                }
            }

            if (abstract_ != null) {
                if (abstract_.length() > 0) {
                    for (int i = 0; i < indent + 1; i++) {
                        tei.append("\t");
                    }
                    tei.append("<div type=\"abstract\">" + abstract_ + "</div>\n");
                }
            }
            for (int i = 0; i < indent; i++) {
                tei.append("\t");
            }
            tei.append("</biblStruct>\n");
        } catch (Exception e) {
            throw new GrobidException("Cannot convert  bibliographical item into a TEI, " +
                    "because of nested exception.", e);
        }

        return tei.toString();
    }

    public void buildBiblioSet(BiblioSet bs, String path0) {
        path = path0;
        try {
            // authors
            if (authors != null) {
                StringTokenizer st = new StringTokenizer(authors, ";");
                if (st.countTokens() > 0) {
                    while (st.hasMoreTokens()) {
                        String author = st.nextToken();
                        if (author != null)
                            author = author.trim();
                        //bs.addAuthor(TextUtilities.HTMLEncode(author));
                        bs.addAuthor(author);
                    }
                }
            }

            // editors
            if (editors != null) {
                //postProcessingEditors();

                StringTokenizer st = new StringTokenizer(editors, ";");
                if (st.countTokens() > 0) {
                    while (st.hasMoreTokens()) {
                        String editor = st.nextToken();
                        if (editor != null)
                            editor = editor.trim();
                        //bs.addEditor(TextUtilities.HTMLEncode(editor));
                        bs.addEditor(editor);
                    }
                }
            }

            // publishers
            if (publisher != null) {
                //bs.addPublisher(TextUtilities.HTMLEncode(publisher));
                bs.addPublisher(publisher);
            }

            // meetings
            if (bookTitle != null) {
                // in case the booktitle corresponds to a proceedings, we can try to indidate the meeting title
                String meeting = bookTitle;

                for (String prefix : confPrefixes) {
                    if (meeting.startsWith(prefix)) {
                        meeting = meeting.replace(prefix, "");
                        meeting = meeting.trim();
                        //String meetStr = TextUtilities.HTMLEncode(meeting);
                        String meetStr = meeting;
                        if (location != null) {
                            meetStr += "<address>" + TextUtilities.HTMLEncode(location) + "</address>";
                        }
                        bs.addMeeting(meetStr);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new GrobidException("Cannot build a biblioSet, because of nested exception.", e);
        }
    }


    /**
     * Export the bibliographical item into a TEI BiblStruct string with pointers and list sharing
     */
    public String toTEI2(BiblioSet bs) {
        String tei = "";
        try {
            // we just produce here xml strings, DOM XML objects should be used for JDK 1.4, J2E compliance thingy
            tei = "<biblStruct";
            if (language != null) {
                tei += " xml:lang=\"" + language + "\">\n";
                // TBD: the language should be normalized following xml lang attributes !
            } else {
                tei += " xml:lang=\"en\">\n";
            }

            if ((bookTitle == null) && (journal == null)) {
                tei += "\t<monogr>\n";
            } else {
                tei += "\t<analytic>\n";
            }

            // title
            if (title != null) {
                tei += "\t\t<title";
                if ((bookTitle == null) && (journal == null))
                    tei += " level=\"m\"";
                else
                    tei += " level=\"a\"";
                tei += ">" + TextUtilities.HTMLEncode(title) + "</title>\n";
            }
			else {
				tei += "\t\t<title/>\n";
			}

            // authors
            if (authors != null) {
                StringTokenizer st = new StringTokenizer(authors, ";");
                if (st.countTokens() > 0) {
                    while (st.hasMoreTokens()) {
                        String author = st.nextToken();
                        if (author != null)
                            author = author.trim();
                        int ind = -1;
                        if (bs.getAuthors() != null)
                            ind = bs.getAuthors().indexOf(author);
                        if (ind != -1) {
                            tei += "\t\t<contributor role=\"author\">\n";
                            tei += "\t\t\t<ptr target=\"#author" + ind + "\" />\n";
                            tei += "\t\t</contributor>\n";
                        } else {
                            tei += "\t\t<contributor role=\"author\">" + TextUtilities.HTMLEncode(author) +
                                    "</contributor>\n";
                        }
                    }
                } else {
                    if (authors != null)
                        tei += "\t\t<author>" + TextUtilities.HTMLEncode(authors) + "</author>\n";
                }
            }

            if (editors != null) {
                //postProcessingEditors();

                StringTokenizer st = new StringTokenizer(editors, ";");
                if (st.countTokens() > 0) {
                    while (st.hasMoreTokens()) {
                        String editor = st.nextToken();
                        if (editor != null)
                            editor = editor.trim();
                        int ind = -1;
                        if (bs.getEditors() != null)
                            ind = bs.getEditors().indexOf(editor);
                        if (ind != -1) {
                            tei += "\t\t<contributor role=\"editor\">\n";
                            tei += "\t\t\t<ptr target=\"#editor" + ind + "\" />\n";
                            tei += "\t\t</contributor>\n";
                        } else {
                            tei += "\t\t<contributor role=\"editor\">" + TextUtilities.HTMLEncode(editor) +
                                    "</contributor>\n";
                        }
                    }
                } else {
                    if (editors != null)
                        tei += "\t\t<editor>" + TextUtilities.HTMLEncode(editors) + "</editor>\n";
                }
            }

            if (note != null) {
                tei += "\t\t<note>" + TextUtilities.HTMLEncode(note) + "</note>\n";
            }

            if ((bookTitle != null) || (journal != null)) {
                tei += "\t</analytic>\n";
                tei += "\t<monogr>\n";
            }

            if (bookTitle != null) {
                tei += "\t\t<title level=\"m\">" + TextUtilities.HTMLEncode(bookTitle) + "</title>\n";

                // in case the booktitle corresponds to a proceedings, we can try to indidate the meeting title
                String meeting = bookTitle;
                boolean meetLoc = false;

                for (String prefix : confPrefixes) {
                    if (meeting.startsWith(prefix)) {
                        meeting = meeting.replace(prefix, "");
                        meeting = meeting.trim();
                        String meetStr = meeting;
                        if (location != null) {
                            meetStr += "<address>" + TextUtilities.HTMLEncode(location) + "</address>";
                            meetLoc = true;
                        }
                        int ind = -1;
                        if (bs.getMeetings() != null)
                            ind = bs.getMeetings().indexOf(meetStr);
                        if (ind != -1) {
                            tei += "\t\t<meeting>\n";
                            tei += "\t\t\t<ptr target=\"#meeting" + ind + "\" />\n";
                            tei += "\t\t</meeting>\n";
                        } else {
                            tei += "\t\t<meeting>" + TextUtilities.HTMLEncode(meeting);
                            if (location != null) {
                                tei += "<address>" + TextUtilities.HTMLEncode(location) + "</address>";
                                meetLoc = true;
                            }
                            tei += "</meeting>\n";
                        }
                        break;
                    }
                }

                if ((location != null) && (!meetLoc)) {
                    tei += "\t\t\t<meeting><address>" + TextUtilities.HTMLEncode(location)
                            + "</address></meeting>\n";
                }
                if ((publication_date != null) || (pageRange != null) || (publisher != null))
                    tei += "\t\t<imprint>\n";
                if (publisher != null) {
                    int ind = -1;
                    if (bs.getPublishers() != null)
                        ind = bs.getPublishers().indexOf(publisher);
                    if (ind != -1) {
                        tei += "\t\t\t<publisher>\n";
                        tei += "\t\t\t\t<ptr target=\"#publisher" + ind + "\" />\n";
                        tei += "\t\t\t</publisher>\n";
                    } else
                        tei += "\t\t\t<publisher>" + TextUtilities.HTMLEncode(publisher) + "</publisher>\n";
                }
                if (normalized_publication_date != null) {
                    if ((normalized_publication_date.getDay() != -1) ||
                            (normalized_publication_date.getMonth() != -1) ||
                            (normalized_publication_date.getYear() != -1)) {
                        tei += "\t\t\t<date>\n";
                        if (normalized_publication_date.getDay() != -1) {
                            tei += "\t\t\t\t<day>" + normalized_publication_date.getDay() + "</day>\n";
                        }
                        if (normalized_publication_date.getMonth() != -1) {
                            tei += "\t\t\t\t<month>" + normalized_publication_date.getMonth() + "</month>\n";
                        }
                        if (normalized_publication_date.getYear() != -1) {
                            tei += "\t\t\t\t<year>" + normalized_publication_date.getYear() + "</year>\n";
                        }
                        tei += "\t\t\t</date>\n";
                    } else {
                        tei += "\t\t\t<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n";
                    }
                } else if (publication_date != null) {
                    tei += "\t\t\t<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n";
                }
                if (pageRange != null) {
                    StringTokenizer st = new StringTokenizer(pageRange, "--");
                    if (st.countTokens() == 2) {
						tei += "\t\t\t<biblScope unit=\"page\" from=\"" + 
							TextUtilities.HTMLEncode(st.nextToken()) + 
								"\" to=\"" + TextUtilities.HTMLEncode(st.nextToken()) + "\" />\n";   	
                    } else {
                        tei += "\t\t\t<biblScope unit=\"page\">" + TextUtilities.HTMLEncode(pageRange)
                                + "</biblScope>\n";
                    }
                }
                if ((publication_date != null) || (pageRange != null) || (publisher != null))
                    tei += "\t\t</imprint>\n";
            } else if (journal != null) {
                tei += "\t\t<title level=\"j\">" + TextUtilities.HTMLEncode(journal) + "</title>\n";

                if ((volumeBlock != null) || (issue != null) || (pageRange != null) ||
                        (publication_date != null) || (publisher != null)) {
                    tei += "\t\t<imprint>\n";
                }
                if (publisher != null) {
                    int ind = -1;
                    if (bs.getPublishers() != null)
                        ind = bs.getPublishers().indexOf(publisher);
                    if (ind != -1) {
                        tei += "\t\t\t<publisher>\n";
                        tei += "\t\t\t\t<ptr target=\"#publisher" + ind + "\" />\n";
                        tei += "\t\t\t</publisher>\n";
                    } else
                        tei += "\t\t\t<publisher>" + TextUtilities.HTMLEncode(publisher) + "</publisher>\n";
                }
                if (volumeBlock != null) {
                    tei += "\t\t\t<biblScope unit=\"volume\">" + TextUtilities.HTMLEncode(volumeBlock)
                            + "</biblScope>\n";
                }
                if (issue != null) {
                    tei += "\t\t\t<biblScope unit=\"issue\">" + TextUtilities.HTMLEncode(issue)
                            + "</biblScope>\n";
                }
                if (pageRange != null) {
                    StringTokenizer st = new StringTokenizer(pageRange, "--");
                    if (st.countTokens() == 2) {
						tei += "\t\t\t<biblScope unit=\"page\" from=\"" + 
							TextUtilities.HTMLEncode(st.nextToken()) + 
								"\" to=\"" + TextUtilities.HTMLEncode(st.nextToken()) + "\" />\n";
                    } else {
                        tei += "\t\t\t<biblScope unit=\"page\">" + TextUtilities.HTMLEncode(pageRange)
                                + "</biblScope>\n";
                    }
                }
                // date
                if (normalized_publication_date != null) {
                    if ((normalized_publication_date.getDay() != -1) ||
                            (normalized_publication_date.getMonth() != -1) ||
                            (normalized_publication_date.getYear() != -1)) {
                        tei += "\t\t\t<date>\n";
                        if (normalized_publication_date.getDay() != -1) {
                            tei += "\t\t\t\t<day>" + normalized_publication_date.getDay() + "</day>\n";
                        }
                        if (normalized_publication_date.getMonth() != -1) {
                            tei += "\t\t\t\t<month>" + normalized_publication_date.getMonth() + "</month>\n";
                        }
                        if (normalized_publication_date.getYear() != -1) {
                            tei += "\t\t\t\t<year>" + normalized_publication_date.getYear() + "</year>\n";
                        }
                        tei += "\t\t\t</date>\n";
                    } else {
                        tei += "\t\t\t<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n";
                    }
                } else if (publication_date != null) {
                    tei += "\t\t\t<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n";
                }

                if ((volumeBlock != null) || (issue != null) || (pageRange != null) || (publication_date != null)
                        || (publisher != null)) {
                    tei += "\t\t</imprint>\n";
                }
            } else {
                // not a journal and not something in a book...
                if ((publication_date != null) || (pageRange != null) || (location != null)
                        || (publisher != null)) {
                    tei += "\t\t<imprint>\n";
                }
                // date
                if (publication_date != null) {
                    tei += "\t\t\t<date>" + TextUtilities.HTMLEncode(publication_date) + "</date>\n";
                }
                if (publisher != null) {
                    int ind = -1;
                    if (bs.getPublishers() != null)
                        ind = bs.getPublishers().indexOf(publisher);
                    if (ind != -1) {
                        tei += "\t\t\t<publisher>\n";
                        tei += "\t\t\t\t<ptr target=\"#publisher" + ind + "\" />\n";
                        tei += "\t\t\t</publisher>\n";
                    } else
                        tei += "\t\t\t<publisher>" + TextUtilities.HTMLEncode(publisher) + "</publisher>\n";
                }
                if (pageRange != null) {
                    StringTokenizer st = new StringTokenizer(pageRange, "--");
                    if (st.countTokens() == 2) {						
						tei += "\t\t\t<biblScope unit=\"page\" from=\"" + 
							TextUtilities.HTMLEncode(st.nextToken()) + 
								"\" to=\"" + TextUtilities.HTMLEncode(st.nextToken()) + "\" />\n";
                    } else {
                        tei += "\t\t\t<biblScope unit=\"page\">" + TextUtilities.HTMLEncode(pageRange)
                                + "</biblScope>\n";
                    }
                }
                if (location != null)
                    tei += "\t\t\t<pubPlace>" + TextUtilities.HTMLEncode(location) + "</pubPlace>\n";

                if ((publication_date != null) || (pageRange != null) || (location != null)
                        || (publisher != null)) {
                    tei += "\t\t</imprint>\n";
                }
            }

            tei += "\t</monogr>\n";

            tei += "</biblStruct>\n";
        } catch (Exception e) {
            throw new GrobidException("Cannot convert bibliographical item into a TEI, " +
                    "because of nested exception.", e);
        }

        return tei;
    }

    /**
     * Export the bibliographical item into OpenURL 1.0.
     */
    public String toOpenURL(String authors) {
        String openurl = "";

        try {
            // general - independent from the type of bibliographical object
            //openurl += "url_ver=Z39.88-2004";
            openurl += "ctx_ver=Z39.88-2004";

            if (DOI != null) {
                //openurl += "&rft.doi=" + HTMLEncode(DOI);
                openurl += "&rft_id=info:doi/" + URLEncoder.encode(DOI, "UTF-8");
                //openurl += "&rft.doi=" + URLEncoder.encode(DOI,"UTF-8");
                // we can finish here
                openurl += "&url_ctx_fmt=info:ofi/fmt:kev:mtx:ctx&rft.genre=article ";
                return openurl;
            }

            // journal
            if ((bookTitle != null) || (journal != null)) {
                if (journal != null)
                    openurl += "&rft_val_fmt=info:ofi/fmt:kev:mtx:journal";
                openurl += "&rft.genre=article"; // ? always to be written ?
                if (ISSN != null)
                    openurl += "&rft.issn=" + URLEncoder.encode(ISSN, "UTF-8");
                if (title != null)
                    openurl += "&rft.atitle=" + URLEncoder.encode(title, "UTF-8");
                if (journal != null)
                    openurl += "&rft.jtitle=" + URLEncoder.encode(journal, "UTF-8");
                else if (bookTitle != null)
                    openurl += "&rft.btitle=" + URLEncoder.encode(bookTitle, "UTF-8");
                if (volumeBlock != null)
                    openurl += "&rft.volume=" + URLEncoder.encode(volumeBlock, "UTF-8");
                if (issue != null)
                    openurl += "&rft.issue=" + URLEncoder.encode(issue, "UTF-8");

                if (pageRange != null) {
                    StringTokenizer st = new StringTokenizer(pageRange, "--");
                    if (st.countTokens() > 0) {
                        if (st.hasMoreTokens()) {
                            String spage = st.nextToken();
                            openurl += "&rft.spage=" + URLEncoder.encode(spage, "UTF-8");
                        }
                        if (st.hasMoreTokens()) {
                            String epage = st.nextToken();
                            openurl += "&rft.epage=" + URLEncoder.encode(epage, "UTF-8");
                        }
                    }
                }
            } else {
                // book 	    
                openurl += "&rft_val_fmt=info:ofi/fmt:kev:mtx:book";
                if (ISBN13 != null)
                    openurl += "&rft.isbn=" + URLEncoder.encode(ISBN13, "UTF-8");
                if (title != null)
                    openurl += "&rft.genre=book&rft.btitle=" + URLEncoder.encode(title, "UTF-8");
            }

            if (publication_date != null)
                openurl += "&rft.date=" + URLEncoder.encode(publication_date, "UTF-8"); // year is enough!

            // authors
            if (authors != null) {
                StringTokenizer st = new StringTokenizer(authors, ";");
                if (st.countTokens() > 0) {
                    if (st.hasMoreTokens()) { // we take just the first author
                        String author = st.nextToken();
                        if (author != null)
                            author = author.trim();
                        int ind = author.lastIndexOf(" ");
                        if (ind != -1) {
                            openurl += "&rft.aulast=" + URLEncoder.encode(author.substring(ind + 1), "UTF-8")
                                    + "&rft.auinit="
                                    + URLEncoder.encode(author.substring(0, ind), "UTF-8");
                        } else
                            openurl += "&rft.au=" + URLEncoder.encode(author, "UTF-8");
                    }
                }
            }

            openurl += "&url_ctx_fmt=info:ofi/fmt:kev:mtx:ctx";
        } catch (Exception e) {
            throw new GrobidException("Cannot open url to DOI, because of nested exception.", e);
        }
        return openurl;
    }


    /**
     * Export the bibliographical item into a COinS (OpenURL ContextObject in SPAN).
     */
    public String toCOinS() {
        String res = "<span class=\"Z3988\" title=\"" + toOpenURL(authors) + "\"></span>";
        return res;
    }


    /**
     * Export the bibliographical item into an OpenURL with given link resolver address.
     */
    public String toFullOpenURL(String linkResolver, String imageLinkResolver) {
        String res = "<a href=\"" + linkResolver + toOpenURL(authors)
                + "\"  target=\"_blank\"><img src=\"" + imageLinkResolver + "\"/></a>";
        return res;
    }

    /**
     * Return the surname of the first author.
     */
    public String getFirstAuthorSurname() {
        if (fullAuthors != null) {
            if (fullAuthors.size() > 0) {
                Person aut = fullAuthors.get(0);
                String sur = aut.getLastName();
                if (sur != null) {
                    if (sur.length() > 0) {
                        return TextUtilities.HTMLEncode(sur);
                    }
                }
            }
        }

        if (authors != null) {
            StringTokenizer st = new StringTokenizer(authors, ";");
            if (st.countTokens() > 0) {
                if (st.hasMoreTokens()) { // we take just the first author
                    String author = st.nextToken();
                    if (author != null)
                        author = author.trim();
                    int ind = author.lastIndexOf(" ");
                    if (ind != -1) {
                        return TextUtilities.HTMLEncode(author.substring(ind + 1));
                    } else
                        return TextUtilities.HTMLEncode(author);
                }
            }

        }
        return null;
    }

    public static String punctuationsAll = "([,:;?.!)-\"']";
    public static String punctuationsSub = "([,;])";

    /**
     * Create author set
     */
    /*public void createAuthorSet() {
      	  if (authors == null)
      	  	  return;
      	  StringTokenizer st = new StringTokenizer(authors, ";");
      	  while(st.hasMoreTokens()) {
      	  	  String token = st.nextToken().trim();
      	  	  Person aut = new Person();
      	  	  aut.setDisplayName(token);
			
      	  	  int ind = token.lastIndexOf(" ");
      	  	  if (ind != -1) {
      	  	  	  aut.setLastName(token.substring(ind+1).trim());
      	  	  	  String first = token.substring(0, ind).trim();
      	  	  	  
      	  	  	  int ind2 = first.lastIndexOf(" ");
      	  	  	  if (ind2 != -1) {
      	  	  	  	  aut.setMiddleName(first.substring(ind2+1).trim());
      	  	  	  	  aut.setFirstName(first.substring(0, ind2).trim());
      	  	  	  }
      	  	  	  else {
      	  	  	  	  aut.setFirstName(first);
      	  	  	  }
      	  	  	  
      	  	  }
      	  	  
      	  	  if (fullAuthors == null)
      	  	  	  fullAuthors = new ArrayList<Person>();
      	  	  fullAuthors.add(aut); 			
      	  }
      }*/

    /**
     * Attach existing recognized emails to authors
     */
    public void attachEmails() {
        // do we have an email field recognized? 
        if (email == null)
            return;
        // we check if we have several emails in the field
        email = email.trim();
        email = email.replace(" and ", ";");
        ArrayList<String> emailles = new ArrayList<String>();
        StringTokenizer st0 = new StringTokenizer(email, ";");
        while (st0.hasMoreTokens()) {
            emailles.add(st0.nextToken().trim());
        }


        List<String> sanitizedEmails = emailSanitizer.splitAndClean(emailles);

        if (sanitizedEmails != null) {
            authorEmailAssigner.assign(fullAuthors, emailles);
        }
    }

    /**
     * Attach existing recognized affiliations to authors
     */
    public void attachAffiliations() {
        if (fullAffiliations == null) {
            return;
        }

        if (fullAuthors == null) {
            return;
        }
        int nbAffiliations = fullAffiliations.size();
        int nbAuthors = fullAuthors.size();

        boolean hasMarker = false;

        // do we have markers in the affiliations?
        for (Affiliation aff : fullAffiliations) {
            if (aff.getMarker() != null) {
                hasMarker = true;
                break;
            }
        }

        if (nbAffiliations == 1) {
            // we distribute this affiliation to each author
            Affiliation aff = fullAffiliations.get(0);
            for (Person aut : fullAuthors) {
                aut.addAffiliation(aff);
            }
            aff.setFailAffiliation(false);
        } else if ((nbAuthors == 1) && (nbAffiliations > 1)) {
            // we put all the affiliations to the single author
            Person auth = fullAuthors.get(0);
            for (Affiliation aff : fullAffiliations) {
                auth.addAffiliation(aff);
                aff.setFailAffiliation(false);
            }
        } else if (hasMarker) {
            // we get the marker for each affiliation and try  to find the related author in the
            // original author field
            for (Affiliation aff : fullAffiliations) {
                if (aff.getMarker() != null) {
                    String marker = aff.getMarker();
                    int from = 0;
                    int ind = 0;
                    ArrayList<Integer> winners = new ArrayList<Integer>();
                    while (ind != -1) {
                        ind = originalAuthors.indexOf(marker, from);
                        boolean bad = false;
                        if (ind != -1) {
                            // we check if we have a digit/letter (1) matching incorrectly
                            //  a double digit/letter (11), or a special non-digit (*) matching incorrectly
                            //  a double special non-digit (**)
                            if (marker.length() == 1) {
                                if (Character.isDigit(marker.charAt(0))) {
                                    if (ind - 1 > 0) {
                                        if (Character.isDigit(originalAuthors.charAt(ind - 1))) {
                                            bad = true;
                                        }
                                    }
                                    if (ind + 1 < originalAuthors.length()) {
                                        if (Character.isDigit(originalAuthors.charAt(ind + 1))) {
                                            bad = true;
                                        }
                                    }
                                } else if (Character.isLetter(marker.charAt(0))) {
                                    if (ind - 1 > 0) {
                                        if (Character.isLetter(originalAuthors.charAt(ind - 1))) {
                                            bad = true;
                                        }
                                    }
                                    if (ind + 1 < originalAuthors.length()) {
                                        if (Character.isLetter(originalAuthors.charAt(ind + 1))) {
                                            bad = true;
                                        }
                                    }
                                } else if (marker.charAt(0) == '*') {
                                    if (ind - 1 > 0) {
                                        if (originalAuthors.charAt(ind - 1) == '*') {
                                            bad = true;
                                        }
                                    }
                                    if (ind + 1 < originalAuthors.length()) {
                                        if (originalAuthors.charAt(ind + 1) == '*') {
                                            bad = true;
                                        }
                                    }
                                }
                            }
                            if (marker.length() == 2) {
                                // case with ** as marker
                                if ((marker.charAt(0) == '*') && (marker.charAt(1) == '*')) {
                                    if (ind - 2 > 0) {
                                        if ((originalAuthors.charAt(ind - 1) == '*') &&
                                                (originalAuthors.charAt(ind - 2) == '*')) {
                                            bad = true;
                                        }
                                    }
                                    if (ind + 2 < originalAuthors.length()) {
                                        if ((originalAuthors.charAt(ind + 1) == '*') &&
                                                (originalAuthors.charAt(ind + 2) == '*')) {
                                            bad = true;
                                        }
                                    }
                                    if ((ind - 1 > 0) && (ind + 1 < originalAuthors.length())) {
                                        if ((originalAuthors.charAt(ind - 1) == '*') &&
                                                (originalAuthors.charAt(ind + 1) == '*')) {
                                            bad = true;
                                        }
                                    }
                                }
                            }
                        }

                        if ((ind != -1) && !bad) {
                            // we find the associated author name 
                            String original = originalAuthors.toLowerCase();
                            int p = 0;
                            int best = -1;
                            int ind2 = -1;
                            int bestDistance = 1000;
                            for (Person aut : fullAuthors) {
                                if (!winners.contains(new Integer(p))) {
                                    String lastname = aut.getLastName();

                                    if (lastname != null) {
                                        lastname = lastname.toLowerCase();
                                        ind2 = original.indexOf(lastname, ind2 + 1);
                                        int dist = Math.abs(ind - (ind2 + lastname.length()));
                                        if (dist < bestDistance) {
                                            best = p;
                                            bestDistance = dist;
                                        }
                                    }
                                }
                                p++;
                            }

                            // and we associate this affiliation to this author
                            if (best != -1) {
                                fullAuthors.get(best).addAffiliation(aff);
                                aff.setFailAffiliation(false);
                                winners.add(new Integer(best));
                            }

                            from = ind + 1;
                        }
                        if (bad) {
                            from = ind + 1;
                            bad = false;
                        }
                    }
                }
            }
        } else if (nbAuthors == nbAffiliations) {
            // risky heuristics, we distribute in this case one affiliation per author
            // preserving author 
            // sometimes 2 affiliations belong both to 2 authors, for these case, the layout
            // positioning should be studied
            for (int p = 0; p < nbAuthors; p++) {
                fullAuthors.get(p).addAffiliation(fullAffiliations.get(p));
                fullAffiliations.get(p).setFailAffiliation(false);
            }
        }
    }


    /**
     * Create the TEI encoding for the author+affiliation block for the current biblio object.
     */
    public String toTEIAuthorBlock(int nbTag) {
        StringBuffer tei = new StringBuffer();
        int nbAuthors = 0;
        int nbAffiliations = 0;
        int nbAddresses = 0;
        // int nbEmails = 0;
        // int nbPhones = 0;
        //int nbWebs = 0;

        List<Person> auts = fullAuthors;

        Lexicon lexicon = Lexicon.getInstance();

        List<Affiliation> affs = fullAffiliations;
        if (affs == null)
            nbAffiliations = 0;
        else
            nbAffiliations = affs.size();

        if (auts == null)
            nbAuthors = 0;
        else
            nbAuthors = auts.size();
        boolean failAffiliation = true;

        //if (getAuthors() != null) {
        if (auts != null) {
            failAffiliation = false;
            if (nbAuthors > 0) {
                int autRank = 0;
                int contactAut = -1;
                //check if we have a single author of contact
                for (Person author : auts) {
                    if (author.getEmail() != null) {
                        if (contactAut == -1)
                            contactAut = autRank;
                        else {
                            contactAut = -1;
                            break;
                        }
                    }
                    autRank++;
                }
                autRank = 0;
                for (Person author : auts) {
                    if (author.getLastName() != null) {
                        if (author.getLastName().length() < 2)
                            continue;
                    }

                    TextUtilities.appendN(tei, '\t', nbTag);
                    tei.append("<author");

                    if (autRank == contactAut) {
                        tei.append(" role=\"corresp\">\n");
                    } else
                        tei.append(">\n");

                    TextUtilities.appendN(tei, '\t', nbTag + 1);
                    tei.append("<persName>\n");
                    if (author.getFirstName() != null) {
                        TextUtilities.appendN(tei, '\t', nbTag + 2);
                        tei.append("<forename type=\"first\">" +
                                TextUtilities.HTMLEncode(author.getFirstName()) + "</forename>\n");
                    }
                    if (author.getMiddleName() != null) {
                        TextUtilities.appendN(tei, '\t', nbTag + 2);
                        tei.append("<forename type=\"middle\">" +
                                TextUtilities.HTMLEncode(author.getMiddleName()) + "</forename>\n");
                    }
                    if (author.getLastName() != null) {
                        TextUtilities.appendN(tei, '\t', nbTag + 2);
                        tei.append("<surname>" +
                                TextUtilities.HTMLEncode(author.getLastName()) + "</surname>\n");
                        //author.getLastName() + "</surname>\n");
                    }
                    if (author.getTitle() != null) {
                        TextUtilities.appendN(tei, '\t', nbTag + 2);
                        tei.append("<roleName>" +
                                TextUtilities.HTMLEncode(author.getTitle()) + "</roleName>\n");
                    }
                    if (author.getSuffix() != null) {
                        TextUtilities.appendN(tei, '\t', nbTag + 2);
                        tei.append("<genName>" +
                                TextUtilities.HTMLEncode(author.getSuffix()) + "</genName>\n");
                    }

                    TextUtilities.appendN(tei, '\t', nbTag + 1);
                    tei.append("</persName>\n");

                    if (author.getEmail() != null) {
                        TextUtilities.appendN(tei, '\t', nbTag + 1);
                        tei.append("<email>" + TextUtilities.HTMLEncode(author.getEmail()) + "</email>\n");
                    }

                    if (author.getAffiliations() != null) {

                        for (Affiliation aff : author.getAffiliations()) {
                            TextUtilities.appendN(tei, '\t', nbTag + 1);
                            tei.append("<affiliation>\n");

                            if (aff.getDepartments() != null) {
                                if (aff.getDepartments().size() == 1) {
                                    TextUtilities.appendN(tei, '\t', nbTag + 2);
                                    tei.append("<orgName type=\"department\">" +
                                            TextUtilities.HTMLEncode(aff.getDepartments().get(0)) + "</orgName>\n");
                                } else {
                                    int q = 1;
                                    for (String depa : aff.getDepartments()) {
                                        TextUtilities.appendN(tei, '\t', nbTag + 2);
                                        tei.append("<orgName type=\"department\" key=\"dep" + q + "\">" +
                                                TextUtilities.HTMLEncode(depa) + "</orgName>\n");
                                        q++;
                                    }
                                }
                            }

                            if (aff.getLaboratories() != null) {
                                if (aff.getLaboratories().size() == 1) {
                                    TextUtilities.appendN(tei, '\t', nbTag + 2);
                                    tei.append("<orgName type=\"laboratory\">" +
                                            TextUtilities.HTMLEncode(aff.getLaboratories().get(0)) + "</orgName>\n");
                                } else {
                                    int q = 1;
                                    for (String labo : aff.getLaboratories()) {
                                        TextUtilities.appendN(tei, '\t', nbTag + 2);
                                        tei.append("<orgName type=\"laboratory\" key=\"lab" + q + "\">" +
                                                TextUtilities.HTMLEncode(labo) + "</orgName>\n");
                                        q++;
                                    }
                                }
                            }

                            if (aff.getInstitutions() != null) {
                                if (aff.getInstitutions().size() == 1) {
                                    TextUtilities.appendN(tei, '\t', nbTag + 2);
                                    tei.append("<orgName type=\"institution\">" +
                                            TextUtilities.HTMLEncode(aff.getInstitutions().get(0)) + "</orgName>\n");
                                } else {
                                    int q = 1;
                                    for (String inst : aff.getInstitutions()) {
                                        TextUtilities.appendN(tei, '\t', nbTag + 2);
                                        tei.append("<orgName type=\"institution\" key=\"instit" + q + "\">" +
                                                TextUtilities.HTMLEncode(inst) + "</orgName>\n");
                                        q++;
                                    }
                                }
                            }

                            if ((aff.getAddressString() != null) ||
                                    (aff.getAddrLine() != null) ||
                                    (aff.getPostBox() != null) ||
                                    (aff.getPostCode() != null) ||
                                    (aff.getSettlement() != null) ||
                                    (aff.getRegion() != null) ||
                                    (aff.getCountry() != null)) {
                                TextUtilities.appendN(tei, '\t', nbTag + 2);
								
                                tei.append("<address>\n");
                                if (aff.getAddressString() != null) {
                                    TextUtilities.appendN(tei, '\t', nbTag + 3);
                                    tei.append("<addrLine>" + TextUtilities.HTMLEncode(aff.getAddressString()) +
                                            "</addrLine>\n");
                                }
                                if (aff.getAddrLine() != null) {
                                    TextUtilities.appendN(tei, '\t', nbTag + 3);
                                    tei.append("<addrLine>" + TextUtilities.HTMLEncode(aff.getAddrLine()) +
                                            "</addrLine>\n");
                                }
                                if (aff.getPostBox() != null) {
                                    TextUtilities.appendN(tei, '\t', nbTag + 3);
                                    tei.append("<postBox>" + TextUtilities.HTMLEncode(aff.getPostBox()) +
                                            "</postBox>\n");
                                }
                                if (aff.getPostCode() != null) {
                                    TextUtilities.appendN(tei, '\t', nbTag + 3);
                                    tei.append("<postCode>" + TextUtilities.HTMLEncode(aff.getPostCode()) +
                                            "</postCode>\n");
                                }
                                if (aff.getSettlement() != null) {
                                    TextUtilities.appendN(tei, '\t', nbTag + 3);
                                    tei.append("<settlement>" + TextUtilities.HTMLEncode(aff.getSettlement()) +
                                            "</settlement>\n");
                                }
                                if (aff.getRegion() != null) {
                                    TextUtilities.appendN(tei, '\t', nbTag + 3);
                                    tei.append("<region>" + TextUtilities.HTMLEncode(aff.getRegion()) +
                                            "</region>\n");
                                }
                                if (aff.getCountry() != null) {
                                    String code = lexicon.getcountryCode(aff.getCountry());
                                    TextUtilities.appendN(tei, '\t', nbTag + 3);
                                    tei.append("<country");
                                    if (code != null)
                                        tei.append(" key=\"" + code + "\"");
                                    tei.append(">" + TextUtilities.HTMLEncode(aff.getCountry()) +
                                            "</country>\n");
                                }

                                TextUtilities.appendN(tei, '\t', nbTag + 2);
                                tei.append("</address>\n");
                            }

                            TextUtilities.appendN(tei, '\t', nbTag + 1);
                            tei.append("</affiliation>\n");
                        }
                    }

                    TextUtilities.appendN(tei, '\t', nbTag);
                    tei.append("</author>\n");
                    autRank++;
                }
            }
        }

        // if the affiliations were not outputted with the authors, we add them here 
        // (better than nothing!)		
        if (affs != null) {
            for (Affiliation aff : affs) {
                if (aff.getFailAffiliation()) {
					// dummy <author> for TEI conformance
                    TextUtilities.appendN(tei, '\t', nbTag);
                    tei.append("<author>\n");
                    TextUtilities.appendN(tei, '\t', nbTag+1);
                    tei.append("<affiliation>\n");

                    if (aff.getDepartments() != null) {
                        if (aff.getDepartments().size() == 1) {
                            TextUtilities.appendN(tei, '\t', nbTag + 2);
                            tei.append("<orgName type=\"department\">" +
                                    TextUtilities.HTMLEncode(aff.getDepartments().get(0)) + "</orgName>\n");
                        } else {
                            int q = 1;
                            for (String depa : aff.getDepartments()) {
                                TextUtilities.appendN(tei, '\t', nbTag + 2);
                                tei.append("<orgName type=\"department\" key=\"dep" + q + "\">" +
                                        TextUtilities.HTMLEncode(depa) + "</orgName>\n");
                                q++;
                            }
                        }
                    }

                    if (aff.getLaboratories() != null) {
                        if (aff.getLaboratories().size() == 1) {
                            TextUtilities.appendN(tei, '\t', nbTag + 2);
                            tei.append("<orgName type=\"laboratory\">" +
                                    TextUtilities.HTMLEncode(aff.getLaboratories().get(0)) + "</orgName>\n");
                        } else {
                            int q = 1;
                            for (String labo : aff.getLaboratories()) {
                                TextUtilities.appendN(tei, '\t', nbTag + 2);
                                tei.append("<orgName type=\"laboratory\" key=\"lab" + q + "\">" +
                                        TextUtilities.HTMLEncode(labo) + "</orgName>\n");
                                q++;
                            }
                        }
                    }

                    if (aff.getInstitutions() != null) {
                        if (aff.getInstitutions().size() == 1) {
                            TextUtilities.appendN(tei, '\t', nbTag + 2);
                            tei.append("<orgName type=\"institution\">" +
                                    TextUtilities.HTMLEncode(aff.getInstitutions().get(0)) + "</orgName>\n");
                        } else {
                            int q = 1;
                            for (String inst : aff.getInstitutions()) {
                                TextUtilities.appendN(tei, '\t', nbTag + 2);
                                tei.append("<orgName type=\"institution\" key=\"instit" + q + "\">" +
                                        TextUtilities.HTMLEncode(inst) + "</orgName>\n");
                                q++;
                            }
                        }
                    }

                    if ((aff.getAddressString() != null) ||
                            (aff.getAddrLine() != null) ||
                            (aff.getPostBox() != null) ||
                            (aff.getPostCode() != null) ||
                            (aff.getSettlement() != null) ||
                            (aff.getRegion() != null) ||
                            (aff.getCountry() != null)) {

	                    TextUtilities.appendN(tei, '\t', nbTag + 2);
	                    tei.append("<address>\n");
	                    if (aff.getAddressString() != null) {
	                        TextUtilities.appendN(tei, '\t', nbTag + 3);
	                        tei.append("<addrLine>" + TextUtilities.HTMLEncode(aff.getAddressString()) +
	                                "</addrLine>\n");
	                    }
	                    if (aff.getAddrLine() != null) {
	                        TextUtilities.appendN(tei, '\t', nbTag + 3);
	                        tei.append("<addrLine>" + TextUtilities.HTMLEncode(aff.getAddrLine()) +
	                                "</addrLine>\n");
	                    }
	                    if (aff.getPostBox() != null) {
	                        TextUtilities.appendN(tei, '\t', nbTag + 3);
	                        tei.append("<postBox>" + TextUtilities.HTMLEncode(aff.getPostBox()) +
	                                "</postBox>\n");
	                    }
	                    if (aff.getPostCode() != null) {
	                        TextUtilities.appendN(tei, '\t', nbTag + 3);
	                        tei.append("<postCode>" + TextUtilities.HTMLEncode(aff.getPostCode()) +
	                                "</postCode>\n");
	                    }
	                    if (aff.getSettlement() != null) {
	                        TextUtilities.appendN(tei, '\t', nbTag + 3);
	                        tei.append("<settlement>" + TextUtilities.HTMLEncode(aff.getSettlement()) +
	                                "</settlement>\n");
	                    }
	                    if (aff.getRegion() != null) {
	                        TextUtilities.appendN(tei, '\t', nbTag + 3);
	                        tei.append("<region>" + TextUtilities.HTMLEncode(aff.getRegion()) +
	                                "</region>\n");
	                    }
	                    if (aff.getCountry() != null) {
	                        String code = lexicon.getcountryCode(aff.getCountry());
	                        TextUtilities.appendN(tei, '\t', nbTag + 3);
	                        tei.append("<country");
	                        if (code != null)
	                            tei.append(" key=\"" + code + "\"");
	                        tei.append(">" + TextUtilities.HTMLEncode(aff.getCountry()) + "</country>\n");
	                    }

	                    TextUtilities.appendN(tei, '\t', nbTag + 2);
	                    tei.append("</address>\n");
					}

                    TextUtilities.appendN(tei, '\t', nbTag+1);
                    tei.append("</affiliation>\n");
					
                    TextUtilities.appendN(tei, '\t', nbTag);
                    tei.append("</author>\n");
                }
            }
        } else if (affiliation != null) {
            StringTokenizer st2 = new StringTokenizer(affiliation, ";");
            int affiliationRank = 0;
            while (st2.hasMoreTokens()) {
                String aff = st2.nextToken();
                TextUtilities.appendN(tei, '\t', nbTag);
                tei.append("<author>\n");
                TextUtilities.appendN(tei, '\t', nbTag+1);
                tei.append("<affiliation>\n");
                TextUtilities.appendN(tei, '\t', nbTag+2);
                tei.append("<orgName>" + TextUtilities.HTMLEncode(aff) + "</orgName>\n");
                if (nbAddresses == nbAffiliations) {
                    int addressRank = 0;
                    if (address != null) {
                        StringTokenizer st3 = new StringTokenizer(address, ";");
                        while (st3.hasMoreTokens()) {
                            String add = st3.nextToken();
                            if (addressRank == affiliationRank) {
                                TextUtilities.appendN(tei, '\t', nbTag + 2);
                                tei.append("<address><addrLine>" + TextUtilities.HTMLEncode(add)
                                        + "</addrLine></address>\n");
                                break;
                            }
                            addressRank++;
                        }
                    }
                }
                TextUtilities.appendN(tei, '\t', nbTag+1);
                tei.append("</affiliation>\n");

                TextUtilities.appendN(tei, '\t', nbTag);
                tei.append("</author>\n");

                affiliationRank++;
            }
        }
        return tei.toString();

    }

    private static volatile Pattern page = Pattern.compile("(\\d+)");

    /**
     * Correct fields of the first biblio item based on the second one and he reference string.
     */
    public void postProcessPages() {
        if (pageRange != null) {
            Matcher matcher = page.matcher(pageRange);
            if (matcher.find()) {
                String firstPage = null;
                String lastPage = null;
                if (matcher.groupCount() > 0) {
                    firstPage = matcher.group(0);
                }
                if (firstPage != null) {
                    try {
                        beginPage = Integer.parseInt(firstPage);
                    } catch (Exception e) {
                        beginPage = -1;
                    }
					if (beginPage != -1)
						pageRange = "" + beginPage;
					else
						pageRange = firstPage;

                    if (matcher.find()) {
                        if (matcher.groupCount() > 0) {
                            lastPage = matcher.group(0);
                        }
                        if (lastPage != null) {
                            try {
                                endPage = Integer.parseInt(lastPage);
                            } catch (Exception e) {
                                endPage = -1;
                            }
							
							if ( (endPage != -1) && (endPage < beginPage) && (endPage < 50) ) {
								endPage = beginPage + endPage;
								pageRange += "--" + endPage;
							}
							else 
								pageRange += "--" + lastPage;
                        }
                    }
                }
            }
        }

    }


    /**
     * Correct fields of the first biblio item based on the second one and he reference string.
     */
    public static void correct(BiblioItem bib, BiblioItem bibo) {
        if (bibo.getDOI() != null)
            bib.setDOI(bibo.getDOI());
        if (bibo.getJournal() != null)
            bib.setJournal(bibo.getJournal());
        if (bibo.getAuthors() != null)
            bib.setAuthors(bibo.getAuthors());
        if (bibo.getEditors() != null)
            bib.setEditors(bibo.getEditors());
        if (bibo.getBookTitle() != null)
            bib.setBookTitle(bibo.getBookTitle());
        if (bibo.getVolume() != null)
            bib.setVolume(bibo.getVolume());
        if (bibo.getVolumeBlock() != null)
            bib.setVolumeBlock(bibo.getVolumeBlock(), false);
        if (bibo.getIssue() != null)
            bib.setIssue(bibo.getIssue());
        if (bibo.getBeginPage() != -1)
            bib.setBeginPage(bibo.getBeginPage());
        if (bibo.getEndPage() != -1)
            bib.setEndPage(bibo.getEndPage());
        if (bibo.getPublicationDate() != null)
            bib.setPublicationDate(bibo.getPublicationDate());
        if (bibo.getSubmissionDate() != null)
            bib.setSubmissionDate(bibo.getSubmissionDate());
        if (bibo.getYear() != null)
            bib.setYear(bibo.getYear());
        if (bibo.getMonth() != null)
            bib.setMonth(bibo.getMonth());
        if (bibo.getDay() != null)
            bib.setDay(bibo.getDay());
        if (bibo.getE_Year() != null)
            bib.setE_Year(bibo.getE_Year());
        if (bibo.getE_Month() != null)
            bib.setE_Month(bibo.getE_Month());
        if (bibo.getE_Day() != null)
            bib.setE_Day(bibo.getE_Day());
        if (bibo.getA_Year() != null)
            bib.setA_Year(bibo.getA_Year());
        if (bibo.getA_Month() != null)
            bib.setA_Month(bibo.getA_Month());
        if (bibo.getA_Day() != null)
            bib.setA_Day(bibo.getA_Day());
        if (bibo.getS_Year() != null)
            bib.setS_Year(bibo.getS_Year());
        if (bibo.getS_Month() != null)
            bib.setS_Month(bibo.getS_Month());
        if (bibo.getS_Day() != null)
            bib.setS_Day(bibo.getS_Day());
        if (bibo.getLocation() != null)
            bib.setLocation(bibo.getLocation());
        if (bibo.getPublisher() != null)
            bib.setPublisher(bibo.getPublisher());
        if (bibo.getArticleTitle() != null) {
            bib.setArticleTitle(bibo.getArticleTitle());
            bib.setTitle(bibo.getArticleTitle());
        }
        if (bibo.getJournalAbbrev() != null) {
            bib.setJournalAbbrev(bibo.getJournalAbbrev());
        }
        if (bibo.getISSN() != null)
            bib.setISSN(bibo.getISSN());
        if (bibo.getISSNe() != null)
            bib.setISSNe(bibo.getISSNe());
        if (bibo.getISBN10() != null)
            bib.setISBN10(bibo.getISBN10());
        if (bibo.getISBN13() != null)
            bib.setISBN13(bibo.getISBN13());

        if (bibo.getItem() != -1) {
            bib.setItem(bibo.getItem());
        }

        // authors present in fullAuthors list should be in the existing resources 
        // at least the corresponding author
        if (bibo.getFullAuthors() != null) {
            if (bib.getFullAuthors() == null)
                bib.setFullAuthors(bibo.getFullAuthors());
            else if (bib.getFullAuthors().size() == 0)
                bib.setFullAuthors(bibo.getFullAuthors());
            else if (bibo.getFullAuthors().size() == 1) {
                // we have the corresponding author	
                // check if the author exists in the obtained list
                Person auto = (Person) bibo.getFullAuthors().get(0);
                List<Person> auts = bib.getFullAuthors();
                if (auts != null) {
                    for (Person aut : auts) {
                        if (aut.getLastName() != null) {
                            if (aut.getLastName().equals(auto.getLastName())) {
                                aut.setCorresp(true);
                                aut.setEmail(auto.getEmail());
                                // we check the country ?
                            }
                        }
                    }
                }
            } else if (bibo.getFullAuthors().size() > 1) {
                // we have the complete list of authors
                // TBD
            }
        }

    }

	/**
     *  Check is the biblio item can be considered as a minimally valid bibliographical reference. 
	 *  A certain minimal number of core metadata have to be instanciated. Otherwise, the biblio
	 *  item can be considered as "garbage" extracted incorrectly.  
	 */
	public boolean rejectAsReference() {
		boolean titleSet = true;
		if ( (title == null) && (bookTitle == null) && (journal == null) && 
				(ISSN == null) && (ISBN13 == null)  && (ISBN10 == null))
			titleSet = false;
		boolean authorSet = true;
		if (fullAuthors == null) 
			authorSet = false;
		// normally properties authors and authorList are null in the current Grobid version
		if (!titleSet && !authorSet && (url == null) && (DOI == null))
			return true;
		else 
			return false;
	}

    public String getTeiId() {
        return teiId;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public void setCoordinates(List<BoundingBox> coordinates) {
        this.coordinates = coordinates;
    }

    public List<BoundingBox> getCoordinates() {
        return coordinates;
    }
}