package org.grobid.core.document;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.data.Figure;
import org.grobid.core.data.Keyword;
import org.grobid.core.data.Person;
import org.grobid.core.data.Table;
import org.grobid.core.document.xml.NodeChildrenIterator;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.engines.TaggingLabel;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.BoundingBoxCalculator;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.counters.CntManager;
import org.grobid.core.utilities.matching.EntityMatcherException;
import org.grobid.core.utilities.matching.ReferenceMarkerMatcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.grobid.core.document.xml.XmlBuilderUtils.fromString;
import static org.grobid.core.document.xml.XmlBuilderUtils.teiElement;
/**
 * Class for generating a TEI representation of a document.
 *
 * @author Patrice Lopez
 */
@SuppressWarnings("StringConcatenationInsideStringBuilderAppend")
public class TEIFormater {
    private Document doc = null;
    public static final Set<TaggingLabel> MARKER_LABELS = Sets.newHashSet(TaggingLabel.CITATION_MARKER, TaggingLabel.FIGURE_MARKER, TaggingLabel.TABLE_MARKER);

    // possible association to Grobid customised TEI schemas: DTD, XML schema, RelaxNG or compact RelaxNG
    // DEFAULT means no schema association in the generated XML documents
    public enum SchemaDeclaration {
        DEFAULT, DTD, XSD, RNG, RNC
    }

    private Boolean inParagraph = false;

    private ArrayList<String> elements = null;

    // static variable for the position of italic and bold features in the CRF model
    private static final int ITALIC_POS = 16;
    private static final int BOLD_POS = 15;

    private static Pattern numberRef = Pattern.compile("(\\[|\\()\\d+\\w?(\\)|\\])");
    private static Pattern numberRefCompact =
            Pattern.compile("(\\[|\\()((\\d)+(\\w)?(\\-\\d+\\w?)?,\\s?)+(\\d+\\w?)(\\-\\d+\\w?)?(\\)|\\])");
    private static Pattern numberRefCompact2 = Pattern.compile("(\\[|\\()(\\d+)(-|‒|–|—|―|\u2013)(\\d+)(\\)|\\])");

    private static Pattern startNum = Pattern.compile("^(\\d+)(.*)");

    public TEIFormater(Document document) {
        doc = document;
    }

    public StringBuilder toTEIHeader(BiblioItem biblio,
                                     String defaultPublicationStatement,
                                     GrobidAnalysisConfig config) {
        return toTEIHeader(biblio, SchemaDeclaration.XSD,
                defaultPublicationStatement, config);
    }

    public StringBuilder toTEIHeader(BiblioItem biblio,
                                     SchemaDeclaration schemaDeclaration,
                                     String defaultPublicationStatement,
                                     GrobidAnalysisConfig config) {
        StringBuilder tei = new StringBuilder();
        tei.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        if (config.isWithXslStylesheet()) {
            tei.append("<?xml-stylesheet type=\"text/xsl\" href=\"../jsp/xmlverbatimwrapper.xsl\"?> \n");
        }
        if (schemaDeclaration == SchemaDeclaration.DTD) {
            tei.append("<!DOCTYPE TEI SYSTEM \"" + GrobidProperties.get_GROBID_HOME_PATH()
                    + "/schemas/dtd/Grobid.dtd" + "\">\n");
        } else if (schemaDeclaration == SchemaDeclaration.XSD) {
            // XML schema
            tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" \n" +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                    //"\n xsi:noNamespaceSchemaLocation=\"" +
                    //GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\""	+
                    "xsi:schemaLocation=\"http://www.tei-c.org/ns/1.0 " +
                    GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\"" +
                    "\n xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
//				"\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
        } else if (schemaDeclaration == SchemaDeclaration.RNG) {
            // standard RelaxNG
            tei.append("<?xml-model href=\"file://" +
                    GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rng" +
                    "\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\n");
        } else if (schemaDeclaration == SchemaDeclaration.RNC) {
            // compact RelaxNG
            tei.append("<?xml-model href=\"file://" +
                    GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rnc" +
                    "\" type=\"application/relax-ng-compact-syntax\"?>\n");
        }
        // by default there is no schema association

        if (schemaDeclaration != SchemaDeclaration.XSD) {
            tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
        }

        if (doc.getLanguage() != null) {
            tei.append("\t<teiHeader xml:lang=\"" + doc.getLanguage() + "\">");
        } else {
            tei.append("\t<teiHeader>");
        }

        // encodingDesc gives info about the producer of the file
        tei.append("\n\t\t<encodingDesc>\n");
        tei.append("\t\t\t<appInfo>\n");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        tei.append("\t\t\t\t<application version=\"" + GrobidProperties.getVersion() +
                "\" ident=\"GROBID\" when=\"" + dateISOString + "\">\n");
        tei.append("\t\t\t\t\t<ref target=\"https://github.com/kermitt2/grobid\">GROBID - A machine learning software for extracting information from scholarly documents</ref>\n");
        tei.append("\t\t\t\t</application>\n");
        tei.append("\t\t\t</appInfo>\n");
        tei.append("\t\t</encodingDesc>");

        tei.append("\n\t\t<fileDesc>\n\t\t\t<titleStmt>\n\t\t\t\t<title level=\"a\" type=\"main\"");
        if (config.isGenerateTeiIds()) {
            String divID = KeyGen.getKey().substring(0, 7);
            tei.append(" xml:id=\"_" + divID + "\"");
        }
        tei.append(">");

        if (biblio == null) {
            // if the biblio object is null, we simply create an empty one
            biblio = new BiblioItem();
        }

        if (biblio.getTitle() != null) {
            tei.append(TextUtilities.HTMLEncode(biblio.getTitle()));
        }

        tei.append("</title>\n\t\t\t</titleStmt>\n");
        if ((biblio.getPublisher() != null) ||
                (biblio.getPublicationDate() != null) ||
                (biblio.getNormalizedPublicationDate() != null)) {
            tei.append("\t\t\t<publicationStmt>\n");
            if (biblio.getPublisher() != null) {
                // publisher and date under <publicationStmt> for better TEI conformance
                tei.append("\t\t\t\t<publisher>" + TextUtilities.HTMLEncode(biblio.getPublisher()) +
                        "</publisher>\n");

                tei.append("\t\t\t\t<availability status=\"unknown\">");
                tei.append("<p>Copyright ");
                //if (biblio.getPublicationDate() != null)
                tei.append(TextUtilities.HTMLEncode(biblio.getPublisher()) + "</p>\n");
                tei.append("\t\t\t\t</availability>\n");
            } else {
                // a dummy publicationStmt is still necessary according to TEI
                tei.append("\t\t\t\t<publisher/>\n");
                if (defaultPublicationStatement == null) {
                    tei.append("\t\t\t\t<availability status=\"unknown\"><licence/></availability>");
                } else {
                    tei.append("\t\t\t\t<availability status=\"unknown\"><p>" +
                            defaultPublicationStatement + "</p></availability>");
                }
                tei.append("\n");
            }

            if (biblio.getNormalizedPublicationDate() != null) {
                Date date = biblio.getNormalizedPublicationDate();
                int year = date.getYear();
                int month = date.getMonth();
                int day = date.getDay();

                String when = "";
                if (year != -1) {
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
                    tei.append("\t\t\t\t<date type=\"published\" when=\"");
                    tei.append(when + "\">");
                } else
                    tei.append("\t\t\t\t<date>");
                if (biblio.getPublicationDate() != null) {
                    tei.append(TextUtilities.HTMLEncode(biblio.getPublicationDate()));
                } else {
                    tei.append(when);
                }
                tei.append("</date>\n");
            } else if ((biblio.getYear() != null) && (biblio.getYear().length() > 0)) {
                String when = "";
                if (biblio.getYear().length() == 1)
                    when += "000" + biblio.getYear();
                else if (biblio.getYear().length() == 2)
                    when += "00" + biblio.getYear();
                else if (biblio.getYear().length() == 3)
                    when += "0" + biblio.getYear();
                else if (biblio.getYear().length() == 4)
                    when += biblio.getYear();

                if ((biblio.getMonth() != null) && (biblio.getMonth().length() > 0)) {
                    if (biblio.getMonth().length() == 1)
                        when += "-0" + biblio.getMonth();
                    else
                        when += "-" + biblio.getMonth();
                    if ((biblio.getDay() != null) && (biblio.getDay().length() > 0)) {
                        if (biblio.getDay().length() == 1)
                            when += "-0" + biblio.getDay();
                        else
                            when += "-" + biblio.getDay();
                    }
                }
                tei.append("\t\t\t\t<date type=\"published\" when=\"");
                tei.append(when + "\">");
                if (biblio.getPublicationDate() != null) {
                    tei.append(TextUtilities.HTMLEncode(biblio.getPublicationDate()));
                } else {
                    tei.append(when);
                }
                tei.append("</date>\n");
            } else if (biblio.getE_Year() != null) {
                String when = "";
                if (biblio.getE_Year().length() == 1)
                    when += "000" + biblio.getE_Year();
                else if (biblio.getE_Year().length() == 2)
                    when += "00" + biblio.getE_Year();
                else if (biblio.getE_Year().length() == 3)
                    when += "0" + biblio.getE_Year();
                else if (biblio.getE_Year().length() == 4)
                    when += biblio.getE_Year();

                if (biblio.getE_Month() != null) {
                    if (biblio.getE_Month().length() == 1)
                        when += "-0" + biblio.getE_Month();
                    else
                        when += "-" + biblio.getE_Month();

                    if (biblio.getE_Day() != null) {
                        if (biblio.getE_Day().length() == 1)
                            when += "-0" + biblio.getE_Day();
                        else
                            when += "-" + biblio.getE_Day();
                    }
                }
                tei.append("\t\t\t\t<date type=\"ePublished\" when=\"");
                tei.append(when + "\">");
                if (biblio.getPublicationDate() != null) {
                    tei.append(TextUtilities.HTMLEncode(biblio.getPublicationDate()));
                } else {
                    tei.append(when);
                }
                tei.append("</date>\n");
            } else if (biblio.getPublicationDate() != null) {
                tei.append("\t\t\t\t<date type=\"published\">");
                tei.append(TextUtilities.HTMLEncode(biblio.getPublicationDate())
                        + "</date>");
            }
            tei.append("\t\t\t</publicationStmt>\n");
        } else {
            tei.append("\t\t\t<publicationStmt>\n");
            tei.append("\t\t\t\t<publisher/>\n");
            tei.append("\t\t\t\t<availability status=\"unknown\"><licence/></availability>\n");
            tei.append("\t\t\t</publicationStmt>\n");
        }
        tei.append("\t\t\t<sourceDesc>\n\t\t\t\t<biblStruct>\n\t\t\t\t\t<analytic>\n");

        // authors + affiliation
        //biblio.createAuthorSet();
        //biblio.attachEmails();
        //biblio.attachAffiliations();
        tei.append(biblio.toTEIAuthorBlock(6));

        // title
        String title = biblio.getTitle();
        String language = biblio.getLanguage();
        String english_title = biblio.getEnglishTitle();
        if (title != null) {
            tei.append("\t\t\t\t\t\t<title");
            /*if ( (bookTitle == null) & (journal == null) )
                    tei.append(" level=\"m\"");
		    	else */
            tei.append(" level=\"a\" type=\"main\"");

            if (config.isGenerateTeiIds()) {
                String divID = KeyGen.getKey().substring(0, 7);
                tei.append(" xml:id=\"_" + divID + "\"");
            }

            // here check the language ?
            if (english_title == null)
                tei.append(">" + TextUtilities.HTMLEncode(title) + "</title>\n");
            else
                tei.append(" xml:lang=\"" + language + "\">" + TextUtilities.HTMLEncode(title) + "</title>\n");
        }

        boolean hasEnglishTitle = false;
        boolean generateIDs = config.isGenerateTeiIds();
        if (english_title != null) {
            // here do check the language!
            LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
            Language resLang = languageUtilities.runLanguageId(english_title);

            if (resLang != null) {
                String resL = resLang.getLangId();
                if (resL.equals(Language.EN)) {
                    hasEnglishTitle = true;
                    tei.append("\t\t\t\t\t\t<title");
                    //if ( (bookTitle == null) & (journal == null) )
                    //	tei.append(" level=\"m\"");
                    //else 
                    tei.append(" level=\"a\"");
                    if (generateIDs) {
                        String divID = KeyGen.getKey().substring(0, 7);
                        tei.append(" xml:id=\"_" + divID + "\"");
                    }
                    tei.append(" xml:lang=\"en\">")
                            .append(TextUtilities.HTMLEncode(english_title)).append("</title>\n");
                }
            }
            // if it's not something in English, we will write it anyway as note without type at the end
        }

        tei.append("\t\t\t\t\t</analytic>\n");

        if ((biblio.getJournal() != null) ||
                (biblio.getJournalAbbrev() != null) ||
                (biblio.getISSN() != null) ||
                (biblio.getISSNe() != null) ||
                (biblio.getPublisher() != null) ||
                (biblio.getPublicationDate() != null) ||
                (biblio.getVolumeBlock() != null) ||
                (biblio.getItem() == BiblioItem.Periodical) ||
                (biblio.getItem() == BiblioItem.InProceedings) ||
                (biblio.getItem() == BiblioItem.Proceedings) ||
                (biblio.getItem() == BiblioItem.InBook) ||
                (biblio.getItem() == BiblioItem.Book) ||
                (biblio.getItem() == BiblioItem.Serie) ||
                (biblio.getItem() == BiblioItem.InCollection)) {
            tei.append("\t\t\t\t\t<monogr");
            tei.append(">\n");

            if (biblio.getJournal() != null) {
                tei.append("\t\t\t\t\t\t<title level=\"j\" type=\"main\"");
                if (generateIDs) {
                    String divID = KeyGen.getKey().substring(0, 7);
                    tei.append(" xml:id=\"_" + divID + "\"");
                }
                tei.append(">" + TextUtilities.HTMLEncode(biblio.getJournal()) + "</title>\n");
            } else if (biblio.getBookTitle() != null) {
                tei.append("\t\t\t\t\t\t<title level=\"m\"");
                if (generateIDs) {
                    String divID = KeyGen.getKey().substring(0, 7);
                    tei.append(" xml:id=\"_" + divID + "\"");
                }
                tei.append(">" + TextUtilities.HTMLEncode(biblio.getBookTitle()) + "</title>\n");
            }

            if (biblio.getJournalAbbrev() != null) {
                tei.append("\t\t\t\t\t\t<title level=\"j\" type=\"abbrev\">" +
                        TextUtilities.HTMLEncode(biblio.getJournalAbbrev()) + "</title>\n");
            }

            if (biblio.getISSN() != null) {
                tei.append("\t\t\t\t\t\t<idno type=\"ISSN\">" +
                        TextUtilities.HTMLEncode(biblio.getISSN()) + "</idno>\n");
            }

            if (biblio.getISSNe() != null) {
                if (!biblio.getISSNe().equals(biblio.getISSN()))
                    tei.append("\t\t\t\t\t\t<idno type=\"eISSN\">" +
                            TextUtilities.HTMLEncode(biblio.getISSNe()) + "</idno>\n");
            }

//            if (biblio.getEvent() != null) {
//                // TODO:
//            }

            // in case the booktitle corresponds to a proceedings, we can try to indicate the meeting title
            String meeting = biblio.getBookTitle();
            boolean meetLoc = false;
            if (biblio.getEvent() != null)
                meeting = biblio.getEvent();
            else if (meeting != null) {
                meeting = meeting.trim();
                for (String prefix : BiblioItem.confPrefixes) {
                    if (meeting.startsWith(prefix)) {
                        meeting = meeting.replace(prefix, "");
                        meeting = meeting.trim();
                        tei.append("\t\t\t\t\t\t<meeting>" + TextUtilities.HTMLEncode(meeting));
                        if ((biblio.getLocation() != null) || (biblio.getTown() != null) ||
                                (biblio.getCountry() != null)) {
                            tei.append(" <address>");
                            if (biblio.getTown() != null) {
                                tei.append("<settlement>" + biblio.getTown() + "</settlement>");
                            }
                            if (biblio.getCountry() != null) {
                                tei.append("<country>" + biblio.getCountry() + "</country>");
                            }
                            if ((biblio.getLocation() != null) && (biblio.getTown() == null) &&
                                    (biblio.getCountry() == null)) {
                                tei.append("<addrLine>" + TextUtilities.HTMLEncode(biblio.getLocation()) + "</addrLine>");
                            }
                            tei.append("</address>\n");
                            meetLoc = true;
                        }
                        tei.append("\t\t\t\t\t\t</meeting>\n");
                        break;
                    }
                }
            }

            if (((biblio.getLocation() != null) || (biblio.getTown() != null) ||
                    (biblio.getCountry() != null))
                    && (!meetLoc)) {
                tei.append("\t\t\t\t\t\t<meeting>");
                tei.append(" <address>");
                if (biblio.getTown() != null) {
                    tei.append(" <settlement>" + biblio.getTown() + "</settlement>");
                }
                if (biblio.getCountry() != null) {
                    tei.append(" <country>" + biblio.getCountry() + "</country>");
                }
                if ((biblio.getLocation() != null) && (biblio.getTown() == null)
                        && (biblio.getCountry() == null)) {
                    tei.append("<addrLine>" + TextUtilities.HTMLEncode(biblio.getLocation()) + "</addrLine>");
                }
                tei.append("</address>\n");
                tei.append("\t\t\t\t\t\t</meeting>\n");
            }

            String pageRange = biblio.getPageRange();

            if ((biblio.getVolumeBlock() != null) | (biblio.getPublicationDate() != null) |
                    (biblio.getNormalizedPublicationDate() != null) |
                    (pageRange != null) | (biblio.getIssue() != null) |
                    (biblio.getBeginPage() != -1) |
                    (biblio.getPublisher() != null)) {
                tei.append("\t\t\t\t\t\t<imprint>\n");

                if (biblio.getPublisher() != null) {
                    tei.append("\t\t\t\t\t\t\t<publisher>" + TextUtilities.HTMLEncode(biblio.getPublisher())
                            + "</publisher>\n");
                }

                if (biblio.getVolumeBlock() != null) {
                    String vol = biblio.getVolumeBlock();
                    vol = vol.replace(" ", "").trim();
                    tei.append("\t\t\t\t\t\t\t<biblScope unit=\"volume\">" +
                            TextUtilities.HTMLEncode(vol) + "</biblScope>\n");
                }

                if (biblio.getIssue() != null) {
                    tei.append("\t\t\t\t\t\t\t<biblScope unit=\"issue\">"
                            + TextUtilities.HTMLEncode(biblio.getIssue()) + "</biblScope>\n");
                }

                if (pageRange != null) {
                    StringTokenizer st = new StringTokenizer(pageRange, "--");
                    if (st.countTokens() == 2) {
                        tei.append("\t\t\t\t\t\t\t<biblScope unit=\"page\"");
                        tei.append(" from=\"" + TextUtilities.HTMLEncode(st.nextToken()) + "\"");
                        tei.append(" to=\"" + TextUtilities.HTMLEncode(st.nextToken()) + "\"/>\n");
                        //tei.append(">" + TextUtilities.HTMLEncode(pageRange) + "</biblScope>\n");
                    } else {
                        tei.append("\t\t\t\t\t\t\t<biblScope unit=\"page\">" + TextUtilities.HTMLEncode(pageRange)
                                + "</biblScope>\n");
                    }
                } else if (biblio.getBeginPage() != -1) {
                    if (biblio.getEndPage() != -1) {
                        tei.append("\t\t\t\t\t\t\t<biblScope unit=\"page\"");
                        tei.append(" from=\"" + biblio.getBeginPage() + "\"");
                        tei.append(" to=\"" + biblio.getEndPage() + "\"/>\n");
                    } else {
                        tei.append("\t\t\t\t\t\t\t<biblScope unit=\"page\"");
                        tei.append(" from=\"" + biblio.getBeginPage() + "\"/>\n");
                    }
                }

                if (biblio.getNormalizedPublicationDate() != null) {
                    Date date = biblio.getNormalizedPublicationDate();
                    int year = date.getYear();
                    int month = date.getMonth();
                    int day = date.getDay();

                    String when = "";
                    if (year != -1) {
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
                        if (biblio.getPublicationDate() != null) {
                            tei.append("\t\t\t\t\t\t\t<date type=\"published\" when=\"");
                            tei.append(when + "\">");
                            tei.append(TextUtilities.HTMLEncode(biblio.getPublicationDate())
                                    + "</date>\n");
                        } else {
                            tei.append("\t\t\t\t\t\t\t<date type=\"published\" when=\"");
                            tei.append(when + "\" />\n");
                        }
                    } else {
                        if (biblio.getPublicationDate() != null) {
                            tei.append("\t\t\t\t\t\t\t<date type=\"published\">");
                            tei.append(TextUtilities.HTMLEncode(biblio.getPublicationDate())
                                    + "</date>\n");
                        }
                    }
                } else if (biblio.getYear() != null) {
                    String when = "";
                    if (biblio.getYear().length() == 1)
                        when += "000" + biblio.getYear();
                    else if (biblio.getYear().length() == 2)
                        when += "00" + biblio.getYear();
                    else if (biblio.getYear().length() == 3)
                        when += "0" + biblio.getYear();
                    else if (biblio.getYear().length() == 4)
                        when += biblio.getYear();

                    if (biblio.getMonth() != null) {
                        if (biblio.getMonth().length() == 1)
                            when += "-0" + biblio.getMonth();
                        else
                            when += "-" + biblio.getMonth();
                        if (biblio.getDay() != null) {
                            if (biblio.getDay().length() == 1)
                                when += "-0" + biblio.getDay();
                            else
                                when += "-" + biblio.getDay();
                        }
                    }
                    if (biblio.getPublicationDate() != null) {
                        tei.append("\t\t\t\t\t\t\t<date type=\"published\" when=\"");
                        tei.append(when + "\">");
                        tei.append(TextUtilities.HTMLEncode(biblio.getPublicationDate())
                                + "</date>\n");
                    } else {
                        tei.append("\t\t\t\t\t\t\t<date type=\"published\" when=\"");
                        tei.append(when + "\" />\n");
                    }
                } else if (biblio.getE_Year() != null) {
                    String when = "";
                    if (biblio.getE_Year().length() == 1)
                        when += "000" + biblio.getE_Year();
                    else if (biblio.getE_Year().length() == 2)
                        when += "00" + biblio.getE_Year();
                    else if (biblio.getE_Year().length() == 3)
                        when += "0" + biblio.getE_Year();
                    else if (biblio.getE_Year().length() == 4)
                        when += biblio.getE_Year();

                    if (biblio.getE_Month() != null) {
                        if (biblio.getE_Month().length() == 1)
                            when += "-0" + biblio.getE_Month();
                        else
                            when += "-" + biblio.getE_Month();

                        if (biblio.getE_Day() != null) {
                            if (biblio.getE_Day().length() == 1)
                                when += "-0" + biblio.getE_Day();
                            else
                                when += "-" + biblio.getE_Day();
                        }
                    }
                    tei.append("\t\t\t\t\t\t\t<date type=\"ePublished\" when=\"");
                    tei.append(when + "\" />\n");
                } else if (biblio.getPublicationDate() != null) {
                    tei.append("\t\t\t\t\t\t\t<date type=\"published\">");
                    tei.append(TextUtilities.HTMLEncode(biblio.getPublicationDate())
                            + "</date>\n");
                }

                // Fix for issue #31
                tei.append("\t\t\t\t\t\t</imprint>\n");
            }
            tei.append("\t\t\t\t\t</monogr>\n");
        } else {
            tei.append("\t\t\t\t\t<monogr>\n");
            tei.append("\t\t\t\t\t\t<imprint>\n");
            tei.append("\t\t\t\t\t\t\t<date/>\n");
            tei.append("\t\t\t\t\t\t</imprint>\n");
            tei.append("\t\t\t\t\t</monogr>\n");
        }

        if (biblio.getDOI() != null) {
            String theDOI = TextUtilities.HTMLEncode(biblio.getDOI());
            if (theDOI.endsWith(".xml")) {
                theDOI = theDOI.replace(".xml", "");
            }

            tei.append("\t\t\t\t\t<idno type=\"DOI\">" + theDOI + "</idno>\n");
        }

        if (biblio.getSubmission() != null) {
            tei.append("\t\t\t\t\t<note type=\"submission\">" +
                    TextUtilities.HTMLEncode(biblio.getSubmission()) + "</note>\n");
        }

        if (biblio.getDedication() != null) {
            tei.append("\t\t\t\t\t<note type=\"dedication\">" + TextUtilities.HTMLEncode(biblio.getDedication())
                    + "</note>\n");
        }

        if ((english_title != null) & (!hasEnglishTitle)) {
            tei.append("\t\t\t\t\t<note type=\"title\"");
            if (generateIDs) {
                String divID = KeyGen.getKey().substring(0, 7);
                tei.append(" xml:id=\"_" + divID + "\"");
            }
            tei.append(">" + TextUtilities.HTMLEncode(english_title) + "</note>\n");
        }

        if (biblio.getNote() != null) {
            tei.append("\t\t\t\t\t<note");
            if (generateIDs) {
                String divID = KeyGen.getKey().substring(0, 7);
                tei.append(" xml:id=\"_" + divID + "\"");
            }
            tei.append(">" + TextUtilities.HTMLEncode(biblio.getNote()) + "</note>\n");
        }

        tei.append("\t\t\t\t</biblStruct>\n");

        if (biblio.getURL() != null) {
            tei.append("\t\t\t\t<ref target=\"" + biblio.getURL() + "\" />\n");
        }

        tei.append("\t\t\t</sourceDesc>\n");
        tei.append("\t\t</fileDesc>\n");

        boolean textClassWritten = false;

        tei.append("\t\t<profileDesc>\n");

        // keywords here !! Normally the keyword field has been preprocessed
        // if the segmentation into individual keywords worked, the first conditional
        // statement will be used - otherwise the whole keyword field is outputed
        if ((biblio.getKeywords() != null) && (biblio.getKeywords().size() > 0)) {
            textClassWritten = true;
            tei.append("\t\t\t<textClass>\n");
            tei.append("\t\t\t\t<keywords>\n");

            List<Keyword> keywords = biblio.getKeywords();
            int pos = 0;
            for (Keyword keyw : keywords) {
                if ((keyw.getKeyword() == null) || (keyw.getKeyword().length() == 0))
                    continue;
                String res = keyw.getKeyword().trim();
                if (res.startsWith(":")) {
                    res = res.substring(1);
                }
                if (pos == (keywords.size() - 1)) {
                    if (res.endsWith(".")) {
                        res = res.substring(0, res.length() - 1);
                    }
                }
                tei.append("\t\t\t\t\t<term");
                if (generateIDs) {
                    String divID = KeyGen.getKey().substring(0, 7);
                    tei.append(" xml:id=\"_" + divID + "\"");
                }
                tei.append(">" + TextUtilities.HTMLEncode(res) + "</term>\n");
                pos++;
            }
            tei.append("\t\t\t\t</keywords>\n");
        } else if (biblio.getKeyword() != null) {
            String keywords = biblio.getKeyword();
            textClassWritten = true;
            tei.append("\t\t\t<textClass>\n");
            tei.append("\t\t\t\t<keywords");

            if (generateIDs) {
                String divID = KeyGen.getKey().substring(0, 7);
                tei.append(" xml:id=\"_" + divID + "\"");
            }
            tei.append(">");
            tei.append(TextUtilities.HTMLEncode(biblio.getKeyword())).append("</keywords>\n");
        }

        if (biblio.getCategories() != null) {
            if (!textClassWritten) {
                textClassWritten = true;
                tei.append("\t\t\t<textClass>\n");
            }
            List<String> categories = biblio.getCategories();
            tei.append("\t\t\t\t<keywords>");
            for (String category : categories) {
                tei.append("\t\t\t\t\t<term");
                if (generateIDs) {
                    String divID = KeyGen.getKey().substring(0, 7);
                    tei.append(" xml:id=\"_" + divID + "\"");
                }
                tei.append(">" + TextUtilities.HTMLEncode(category.trim()) + "</term>\n");
            }
            tei.append("\t\t\t\t</keywords>\n");
        }

        if (textClassWritten)
            tei.append("\t\t\t</textClass>\n");

        String abstractText = biblio.getAbstract();

        Language resLang = null;
        if (abstractText != null) {
            LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
            resLang = languageUtilities.runLanguageId(abstractText);
        }
        if (resLang != null) {
            String resL = resLang.getLangId();
            if (!resL.equals(doc.getLanguage())) {
                tei.append("\t\t\t<abstract xml:lang=\"").append(resL).append("\">\n");
            } else {
                tei.append("\t\t\t<abstract>\n");
            }
        } else if ((abstractText == null) || (abstractText.length() == 0)) {
            tei.append("\t\t\t<abstract/>\n");
        } else {
            tei.append("\t\t\t<abstract>\n");
        }

        if ((abstractText != null) && (abstractText.length() != 0)) {
        	/*String abstractHeader = biblio.getAbstractHeader();
            if (abstractHeader == null)
                abstractHeader = "Abstract";
            tei.append("\t\t\t\t<head");
			if (generateIDs) {
				String divID = KeyGen.getKey().substring(0,7);
				tei.append(" xml:id=\"_" + divID + "\"");
			}
			tei.append(">").append(TextUtilities.HTMLEncode(abstractHeader)).append("</head>\n");*/

            tei.append("\t\t\t\t<p");
            if (generateIDs) {
                String divID = KeyGen.getKey().substring(0, 7);
                tei.append(" xml:id=\"_" + divID + "\"");
            }
            tei.append(">").append(TextUtilities.HTMLEncode(abstractText)).append("</p>\n");

            tei.append("\t\t\t</abstract>\n");
        }

        tei.append("\t\t</profileDesc>\n");

        if ((biblio.getA_Year() != null) |
                (biblio.getS_Year() != null) |
                (biblio.getSubmissionDate() != null) |
                (biblio.getNormalizedSubmissionDate() != null)
                ) {
            tei.append("\t\t<revisionDesc>\n");
        }

        // submission and other review dates here !
        if (biblio.getA_Year() != null) {
            String when = biblio.getA_Year();
            if (biblio.getA_Month() != null) {
                when += "-" + biblio.getA_Month();
                if (biblio.getA_Day() != null) {
                    when += "-" + biblio.getA_Day();
                }
            }
            tei.append("\t\t\t\t<date type=\"accepted\" when=\"");
            tei.append(when).append("\" />\n");
        }
        if (biblio.getNormalizedSubmissionDate() != null) {
            Date date = biblio.getNormalizedSubmissionDate();
            int year = date.getYear();
            int month = date.getMonth();
            int day = date.getDay();

            String when = "" + year;
            if (month != -1) {
                when += "-" + month;
                if (day != -1) {
                    when += "-" + day;
                }
            }
            tei.append("\t\t\t\t<date type=\"submission\" when=\"");
            tei.append(when).append("\" />\n");
        } else if (biblio.getS_Year() != null) {
            String when = biblio.getS_Year();
            if (biblio.getS_Month() != null) {
                when += "-" + biblio.getS_Month();
                if (biblio.getS_Day() != null) {
                    when += "-" + biblio.getS_Day();
                }
            }
            tei.append("\t\t\t\t<date type=\"submission\" when=\"");
            tei.append(when).append("\" />\n");
        } else if (biblio.getSubmissionDate() != null) {
            tei.append("\t\t\t<date type=\"submission\">")
                    .append(TextUtilities.HTMLEncode(biblio.getSubmissionDate())).append("</date>\n");

            /*tei.append("\t\t\t<change when=\"");
			tei.append(TextUtilities.HTMLEncode(biblio.getSubmissionDate()));
			tei.append("\">Submitted</change>\n");
			*/
        }
        if ((biblio.getA_Year() != null) |
                (biblio.getS_Year() != null) |
                (biblio.getSubmissionDate() != null)
                ) {
            tei.append("\t\t</revisionDesc>\n");
        }

        tei.append("\t</teiHeader>\n");

        if (doc.getLanguage() != null) {
            tei.append("\t<text xml:lang=\"").append(doc.getLanguage()).append("\">\n");
        } else {
            tei.append("\t<text>\n");
        }

        return tei;
    }


    /**
     * TEI formatting of the body where only basic logical document structures are present.
     * This TEI format avoids most of the risks of ill-formed TEI due to structure recognition
     * errors and frequent PDF noises.
     * It is adapted to fully automatic process and simple exploitation of the document structures
     * like structured indexing and search.
     */
    public StringBuilder toTEIBody(StringBuilder buffer,
                                   String result,
                                   BiblioItem biblio,
                                   List<BibDataSet> bds,
                                   LayoutTokenization layoutTokenization,
                                   List<Figure> figures,
                                   List<Table> tables,
                                   Document doc,
                                   GrobidAnalysisConfig config) throws Exception {
        if ((result == null) || (layoutTokenization == null) || (layoutTokenization.getTokenization() == null)) {
            buffer.append("\t\t<body/>\n");
            return buffer;
        }
        buffer.append("\t\t<body>\n");
        buffer = toTEITextPiece(buffer, result, biblio, bds,
                layoutTokenization, figures, tables, doc, config);

        // footnotes are still in the body
        buffer = toTEIFootNote(buffer, doc, config);

        buffer.append("\t\t</body>\n");

        return buffer;
    }

    public StringBuilder toTEIFootNote(StringBuilder tei,
                                       Document doc,
                                       GrobidAnalysisConfig config) throws Exception {
        // write the footnotes
        SortedSet<DocumentPiece> documentFootnoteParts = doc.getDocumentPart(SegmentationLabel.FOOTNOTE);
        String footnotes = doc.getDocumentPartText(SegmentationLabel.FOOTNOTE);
        if (documentFootnoteParts != null) {
            List<String> allNotes = new ArrayList<String>();
            for (DocumentPiece docPiece : documentFootnoteParts) {
                String footText = doc.getDocumentPieceText(docPiece);
                footText = TextUtilities.dehyphenize(footText);
                footText = footText.replace("\n", " ");
                footText = footText.replace("  ", " ").trim();
                if (footText.length() < 6)
                    continue;
                if (allNotes.contains(footText)) {
                    // basically we have here the "recurrent" headnote/footnote for each page,
                    // no need to add them several times (in the future we could even use them
                    // differently combined with the header)
                    continue;
                }
                // pattern is <note n="1" place="foot" xml:id="no1">
                tei.append("\n\t\t\t<note place=\"foot\"");
                Matcher ma = startNum.matcher(footText);
                int currentNumber = -1;
                if (ma.find()) {
                    String groupStr = ma.group(1);
                    footText = ma.group(2);
                    try {
                        currentNumber = Integer.parseInt(groupStr);
                    } catch (NumberFormatException e) {
                        currentNumber = -1;
                    }
                }
                if (currentNumber != -1) {
                    tei.append(" n=\"" + currentNumber + "\"");
                }
                if (config.isGenerateTeiIds()) {
                    String divID = KeyGen.getKey().substring(0, 7);
                    tei.append(" xml:id=\"_" + divID + "\"");
                }
                tei.append(">");
                tei.append(TextUtilities.HTMLEncode(footText));
                allNotes.add(footText);
                tei.append("</note>\n");
            }
        }

        return tei;
    }

    public StringBuilder toTEIAcknowledgement(StringBuilder buffer,
                                              String reseAcknowledgement,
                                              List<LayoutToken> tokenizationsAcknowledgement,
                                              List<BibDataSet> bds,
                                              GrobidAnalysisConfig config) throws Exception {
        if ((reseAcknowledgement == null) || (tokenizationsAcknowledgement == null)) {
            return buffer;
        }

        buffer.append("\n\t\t\t<div type=\"acknowledgement\">\n");
        StringBuilder buffer2 = new StringBuilder();

        buffer2 = toTEITextPiece(buffer2, reseAcknowledgement, null, bds,
                new LayoutTokenization(tokenizationsAcknowledgement), null, null, doc, config);
        String acknowResult = buffer2.toString();
        String[] acknowResultLines = acknowResult.split("\n");
        boolean extraDiv = false;
        if (acknowResultLines.length != 0) {
            for (int i = 0; i < acknowResultLines.length; i++) {
                if (acknowResultLines[i].trim().length() == 0)
                    continue;
				/*if ( (i==0) && acknowResultLines[i].trim().startsWith("<div>") ) {
					extraDiv = true;
					// we skip the first div (there is already a <div> just opened)
				}
				else if ( (i==acknowResultLines.length-1) && extraDiv) {
					extraDiv = false;
					// we skip the last div
				}
				else {*/
                buffer.append(acknowResultLines[i] + "\n");
                //}
            }
        }
        buffer.append("\t\t\t</div>\n\n");

        return buffer;
    }


    public StringBuilder toTEIAnnex(StringBuilder buffer,
                                    String result,
                                    BiblioItem biblio,
                                    List<BibDataSet> bds,
                                    List<LayoutToken> tokenizations,
                                    Document doc,
                                    GrobidAnalysisConfig config) throws Exception {
        if ((result == null) || (tokenizations == null)) {
            return buffer;
        }

        buffer.append("\t\t\t<div type=\"annex\">\n");
        buffer = toTEITextPiece(buffer, result, biblio, bds,
                new LayoutTokenization(tokenizations), null, null, doc, config);
        buffer.append("\t\t\t</div>\n");

        return buffer;
    }

    private StringBuilder toTEITextPiece(StringBuilder buffer,
                                         String result,
                                         BiblioItem biblio,
                                         List<BibDataSet> bds,
                                         LayoutTokenization layoutTokenization,
                                         List<Figure> figures,
                                         List<Table> tables,
                                         Document doc,
                                         GrobidAnalysisConfig config) throws Exception {


        TaggingLabel lastClusterLabel = null;

        int startPosition = buffer.length();

        boolean figureBlock = false; // indicate that a figure or table sequence was met
        // used for reconnecting a paragraph that was cut by a figure/table

        List<LayoutToken> tokenizations = layoutTokenization.getTokenization();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FULLTEXT, result, tokenizations);

        String tokenLabel = null;
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        List<Element> divResults = new ArrayList<>();

        Element curDiv = teiElement("div");
        Element curParagraph = null;
        //divResults.add(curDiv);

//        System.out.println(new TaggingTokenClusteror(GrobidModels.FULLTEXT, result, tokenizations).cluster());

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i(clusterLabel);

            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
            if (clusterLabel  == TaggingLabel.SECTION) {
                curDiv = teiElement("div");
                Element head = teiElement("head");
                head.appendChild(clusterContent);
                curDiv.appendChild(head);
                divResults.add(curDiv);
            } else if (clusterLabel == TaggingLabel.EQUATION) {
                curDiv.appendChild(teiElement("formula", clusterContent));
            } else if (clusterLabel == TaggingLabel.ITEM) {
                curDiv.appendChild(teiElement("item", clusterContent));
            } else if (clusterLabel == TaggingLabel.OTHER) {
                Element note = teiElement("note", clusterContent);
                note.addAttribute(new Attribute("type", "other"));
                curDiv.appendChild(note);
            }
            else if (clusterLabel == TaggingLabel.PARAGRAPH) {
                if (isNewParagraph(lastClusterLabel, curParagraph)) {
                    curParagraph = teiElement("p");
                    curDiv.appendChild(curParagraph);
                }
                curParagraph.appendChild(clusterContent);
            } else if (MARKER_LABELS.contains(clusterLabel)) {
                String replacement = null;
                List<LayoutToken> refTokens = cluster.concatTokens();
                String chunkRefString = LayoutTokensUtil.toText(refTokens);
                List<Node> refNodes = null;
                switch (clusterLabel) {
                    case CITATION_MARKER:
                        refNodes = markReferencesTEILuceneBased(chunkRefString,
                                refTokens,
                                doc.getReferenceMarkerMatcher(),
                                config.isGenerateTeiCoordinates());
                        break;
                    case FIGURE_MARKER:
                        replacement = markReferencesFigureTEI(chunkRefString, refTokens, figures,
                                config.isGenerateTeiCoordinates());
                        break;
                    case TABLE_MARKER:
                        replacement = markReferencesTableTEI(chunkRefString, refTokens, tables,
                                config.isGenerateTeiCoordinates());
                        break;
                    default:
                        throw new IllegalStateException("Unsupported marker type: " + clusterLabel);
                }

                //TODO: get rid of dummy stuff and the 'replacement' var - it complicates things
                if (replacement != null) {
                    //TODO: hack for now to be able to parse - should return a list of nodes already
                    Element dummyRepl = fromString("<dummy xmlns=\"http://www.tei-c.org/ns/1.0\">" + replacement + "</dummy>");
                    for (Node n : NodeChildrenIterator.get(dummyRepl)) {
                        Element parent = curParagraph != null ? curParagraph : curDiv;
                        n.detach();
                        parent.appendChild(n);
                    }
                } else if (refNodes != null) {
                    for (Node n : refNodes) {
                        Element parent = curParagraph != null ? curParagraph : curDiv;
                        parent.appendChild(n);
                    }
                }
            }

            lastClusterLabel = cluster.getTaggingLabel();
        }

        buffer.append(XmlBuilderUtils.toXml(divResults));

        // we apply some overall cleaning and simplification
        buffer = TextUtilities.replaceAll(buffer, "</head><head",
                "</head>\n\t\t\t</div>\n\t\t\t<div>\n\t\t\t\t<head");
        buffer = TextUtilities.replaceAll(buffer, "</p>\t\t\t\t<p>", " ");


//        if (figureBlock) {
//            if (lastTag != null) {
//                testClosingTag(buffer, "", lastTag, tokenLabel, bds, generateIDs, false);
//            }
//        }
//
//        if (divOpen) {
//            buffer.append("\t\t\t</div>\n");
//            divOpen = false;
//        }

        //TODO: work on reconnection
        // we evaluate the need to reconnect paragraphs cut by a figure or a table
        int indP1 = buffer.indexOf("</p0>", startPosition - 1);
        while (indP1 != -1) {
            int indP2 = buffer.indexOf("<p>", indP1 + 1);
            if ((indP2 != 1) && (buffer.length() > indP2 + 5)) {
                if (Character.isUpperCase(buffer.charAt(indP2 + 4)) &&
                        Character.isLowerCase(buffer.charAt(indP2 + 5))) {
                    // a marker for reconnecting the two paragraphs
                    buffer.setCharAt(indP2 + 1, 'q');
                }
            }
            indP1 = buffer.indexOf("</p0>", indP1 + 1);
        }
        buffer = TextUtilities.replaceAll(buffer, "</p0>(\\n\\t)*<q>", " ");
        buffer = TextUtilities.replaceAll(buffer, "</p0>", "</p>");
        buffer = TextUtilities.replaceAll(buffer, "<q>", "<p>");

        if (figures != null) {
            for (Figure figure : figures) {
                String figSeg = figure.toTEI(3, config);
                if (figSeg != null) {
                    buffer.append(figSeg).append("\n");
                }
            }
        }
        if (tables != null) {
            for (Table table : tables) {
                String tabSeg = table.toTEI(3, config);
                if (tabSeg != null) {
                    buffer.append(tabSeg).append("\n");
                }
            }
        }

        // additional pass for inserting reference markers for citations, figures and table
//        buffer = injectMarkers(buffer, result, bds, figures, tables, doc, config, startPosition, tokenizations);

        return buffer;
    }

    private boolean isNewParagraph(TaggingLabel lastClusterLabel, Element curParagraph) {
        return (!MARKER_LABELS.contains(lastClusterLabel) && lastClusterLabel != TaggingLabel.FIGURE
                && lastClusterLabel != TaggingLabel.TABLE)|| curParagraph == null;
    }

//    private StringBuilder toTEITextPieceOld(StringBuilder buffer,
//                                         String result,
//                                         BiblioItem biblio,
//                                         List<BibDataSet> bds,
//                                         LayoutTokenization layoutTokenization,
//                                         List<Figure> figures,
//                                         List<Table> tables,
//                                         Document doc,
//                                         GrobidAnalysisConfig config) throws Exception {
//
//        boolean generateIDs = config.isGenerateTeiIds();
//
////        StringTokenizer st = new StringTokenizer(result, "\n");
//        String tokenLabel = null;
//        String encodedToken = null;
//        String lastTag = null;
//        String lastOriginalTag = "";
//        //System.out.println(result);
//
//        // current token position
//        int p = 0;
//
//        boolean start = true;
//        boolean divOpen = false;
//        int startPosition = buffer.length();
//
//        boolean figureBlock = false; // indicate that a figure or table sequence was met
//        // used for reconnecting a paragraph that was cut by a figure/table
//
//        // this List will identify in particular the figure objects
//        List<GraphicObject> graphicObjects = new ArrayList<GraphicObject>();
//
//
////        List<Integer> markerPositions = new ArrayList<>();
//
//        List<LayoutToken> tokenizations = layoutTokenization.getTokenization();
//        List<Pair<String, String>> tokensAndLabels = GenericTaggerUtils.getTokensAndLabels(result);
//
////        while (st.hasMoreTokens()) {
//        int cnt = 0;
//        for (Pair<String, String> tokenAndLabel : tokensAndLabels) {
//            cnt++;
//
//            boolean addSpace = false;
//
////            String tok = st.nextToken().trim();
////            if (tok.length() == 0) {
////                continue;
////            }
//
//            if (tokenAndLabel == null) {
//                continue;
//            }
////            StringTokenizer stt = new StringTokenizer(tok, " \t");
////            int i = 0;
//            boolean newLine = false;
////            int ll = stt.countTokens();
////            while (stt.hasMoreTokens()) {
////                String s = stt.nextToken().trim();
//            String plainToken = tokenAndLabel.a;
//
////            if (i == 0) {
//            int p0 = p;
//            boolean strop = false;
//            while ((!strop) && (p < tokenizations.size())) {
//                String tokOriginal = tokenizations.get(p).t();
//                if (tokOriginal.equals(" ")
//                        || tokOriginal.equals("\u00A0")) {
//                    addSpace = true;
//                } else if (tokOriginal.equals("\n") || tokOriginal.equals("\r")) {
//                    newLine = true;
//                } else if (tokOriginal.equals(plainToken)) {
//                    strop = true;
//                }
//                p++;
//            }
//            if (p >= tokenizations.size()) {
//                // either we are at the end of the text body, or we might have
//                // a problematic token in tokenization for some reasons
//                if ((p - p0) > 1) {
//                    // we loose the synchronicity, so we reinit p for the next token
//                    p = p0;
//                    // and we add a space to avoid concatenated words
//                    addSpace = true;
//                }
//            }
//            if (plainToken.equals("@BULLET")) {
//                plainToken = "•";
//            }
//
//            if (plainToken.equals("LINESTART")) {
//                newLine = true;
//            }
//
//
//            encodedToken = TextUtilities.HTMLEncode(plainToken); // lexical token
////                }
//
////        else if (i == ll - 1) {
////                    s1 = s; // current tag
//            tokenLabel = tokenAndLabel.b;
//
////                } else {
//            //localFeatures.add(s);
////                }
////                i++;
////            }
//
//            if (newLine && !start) {
//                buffer.append("\n");
//            }
//
//            if (tokenLabel.endsWith("<figure>") || tokenLabel.endsWith("<table>")) {
//                figureBlock = true;
//                continue;
//            }
//
//            String lastTag0 = null;
//            if (lastTag != null) {
//                if (lastTag.startsWith("I-")) {
//                    lastTag0 = lastTag.substring(2, lastTag.length());
//                } else {
//                    lastTag0 = lastTag;
//                }
//            }
//            String currentTag0 = null;
//            if (tokenLabel != null) {
//                if (tokenLabel.startsWith("I-")) {
//                    currentTag0 = tokenLabel.substring(2, tokenLabel.length());
//                } else {
//                    currentTag0 = tokenLabel;
//                }
//            }
//            // we avoid citation_marker and figure_marker tags because they introduce too much mess,
//            // they will be injected later
//
//            String currentOriginalTag = tokenLabel;
//
////            if (GenericTaggerUtils.isBeginningOfEntity(currentOriginalTag) && (currentTag0.equals("<citation_marker>") ||
////                    currentTag0.equals("<figure_marker>") ||
////                    currentTag0.equals("<table_marker>"))) {
////                markerPositions.add(buffer.length());
////            }
//
//            if (currentTag0.equals("<citation_marker>") ||
//                    currentTag0.equals("<figure_marker>") ||
//                    currentTag0.equals("<table_marker>") ||
//                    currentTag0.equals("<item>")) {
//                currentTag0 = lastTag0;
//                tokenLabel = lastTag0;
//            }
//            if ((tokenLabel != null) && tokenLabel.equals("I-<paragraph>") &&
//                    (lastOriginalTag.endsWith("<citation_marker>") ||
//                            lastOriginalTag.endsWith("<figure_marker>") ||
//                            lastOriginalTag.endsWith("<table_marker>") ||
//                            lastOriginalTag.endsWith("<item>"))) {
//                currentTag0 = "<paragraph>";
//                tokenLabel = "<paragraph>";
//            }
//            lastOriginalTag = currentOriginalTag;
//            boolean closeParagraph = false;
//            if (lastTag != null) {
//                closeParagraph =
//                        testClosingTag(buffer, currentTag0, lastTag0, tokenLabel, bds, config.isGenerateTeiIds(), figureBlock);
//            }
//
//            boolean output = FullTextParser.writeField(buffer, tokenLabel, lastTag0, encodedToken, "<other>",
//                    "<note type=\"other\">", addSpace, 3, generateIDs);
//
//            // for paragraph we must distinguish starting and closing tags
//            if (!output) {
//                if (closeParagraph) {
//                    output = FullTextParser.writeFieldBeginEnd(buffer, tokenLabel, "", encodedToken,
//                            "<paragraph>", "<p>", addSpace, 4, generateIDs);
//                } else {
//                    output = FullTextParser.writeFieldBeginEnd(buffer, tokenLabel, lastTag, encodedToken,
//                            "<paragraph>", "<p>", addSpace, 4, generateIDs);
//                }
//            }
//
//            if (!output) {
//                if (divOpen) {
//                    output = FullTextParser.writeField(buffer, tokenLabel, lastTag0, encodedToken, "<section>",
//                            "</div>\n\t\t\t<div>\n\t\t\t\t<head>", addSpace, 3, generateIDs);
//                    //output = FullTextParser.writeFieldBeginEnd(buffer, s1, lastTag, s2, "<section>",
//                    //		"</div>\n\t\t\t<div>\n\t\t\t\t<head>", addSpace, 3, generateIDs);
//
//                    //if (!s1.equals(lastTag0))
//                    //divOpen = false;
//                } else {
//                    output = FullTextParser.writeField(buffer, tokenLabel, lastTag0, encodedToken, "<section>",
//                            "<div>\n\t\t\t\t<head>", addSpace, 3, generateIDs);
//                    //output = FullTextParser.writeFieldBeginEnd(buffer, s1, lastTag, s2, "<section>",
//                    //		"<div>\n\t\t\t\t<head>", addSpace, 3, generateIDs);
//                }
//                if (output) {
//                    if (!tokenLabel.equals(lastTag0)) {
//                        divOpen = true;
//                    }
//                }
//            }
//
//            if (!output) {
//                output = FullTextParser.writeField(buffer, tokenLabel, lastTag0, encodedToken, "<equation>",
//                        "<formula>", addSpace, 4, generateIDs);
//            }
//            /*if (!output) {
//                output = FullTextParser.writeField(buffer, s1, lastTag0, s2, "<label>",
//					"<label>", addSpace, 4, generateIDs);
//            }*/
//
//            // for item we must distinguish starting and closing tags
//            if (!output) {
//                output = FullTextParser.writeFieldBeginEnd(buffer, tokenLabel, lastTag, encodedToken, "<item>",
//                        "<item>", addSpace, 4, generateIDs);
//            }
//
//            lastTag = tokenLabel;
//            lastTag0 = currentTag0;
//
////            if (!st.hasMoreTokens()) {
//            if (cnt == tokensAndLabels.size()) {
//                if (lastTag != null) {
//                    testClosingTag(buffer, "", currentTag0, tokenLabel, bds, generateIDs, false);
//                }
//            }
//            if (start) {
//                start = false;
//            }
//
//            if (figureBlock) {
//                figureBlock = false;
//            }
//        }
//
//        // we apply some overall cleaning and simplification
//        buffer = TextUtilities.replaceAll(buffer, "</head><head",
//                "</head>\n\t\t\t</div>\n\t\t\t<div>\n\t\t\t\t<head");
//        buffer = TextUtilities.replaceAll(buffer, "</p>\t\t\t\t<p>", " ");
//
//		/*String str1 = "</ref></p>\n\n\t\t\t\t<p>";
//        String str2 = "</ref> ";
//		int startPos = 0;
//		while(startPos != -1) {
//			startPos = buffer.indexOf(str1, startPos);
//			if (startPos != -1) {
//				int endPos = startPos + str1.length();
//				buffer.replace(startPos, endPos, str2);
//				startPos = endPos;
//			}
//		}*/
//
//        if (figureBlock) {
//            if (lastTag != null) {
//                testClosingTag(buffer, "", lastTag, tokenLabel, bds, generateIDs, false);
//            }
//        }
//
//        if (divOpen) {
//            buffer.append("\t\t\t</div>\n");
//            divOpen = false;
//        }
//
//        // we evaluate the need to reconnect paragraphs cut by a figure or a table
//        int indP1 = buffer.indexOf("</p0>", startPosition - 1);
//        while (indP1 != -1) {
//            int indP2 = buffer.indexOf("<p>", indP1 + 1);
//            if ((indP2 != 1) && (buffer.length() > indP2 + 5)) {
//                if (Character.isUpperCase(buffer.charAt(indP2 + 4)) &&
//                        Character.isLowerCase(buffer.charAt(indP2 + 5))) {
//                    // a marker for reconnecting the two paragraphs
//                    buffer.setCharAt(indP2 + 1, 'q');
//                }
//            }
//            indP1 = buffer.indexOf("</p0>", indP1 + 1);
//        }
//        buffer = TextUtilities.replaceAll(buffer, "</p0>(\\n\\t)*<q>", " ");
//        buffer = TextUtilities.replaceAll(buffer, "</p0>", "</p>");
//        buffer = TextUtilities.replaceAll(buffer, "<q>", "<p>");
//
//        // additional pass for inserting reference markers for citations, figures and table
//        buffer = injectMarkers(buffer, result, bds, figures, tables, doc, config, startPosition, tokenizations);
//
//        return buffer;
//    }

//    private StringBuilder injectMarkers(StringBuilder buffer, String result, List<BibDataSet> bds,
//                                        List<Figure> figures, List<Table> tables, Document doc,
//                                        GrobidAnalysisConfig config,
//                                        int startPosition, List<LayoutToken> tokenizations) throws EntityMatcherException {
//        String lastTag = null;
//        String fullLabel;
//        String s2;
//        int teiPosition = startPosition - 1;
//        int startRefPosition = 0;
//        int endRefPosition = 0;
//
//        StringBuilder refString = new StringBuilder();
//        List<LayoutToken> refTokens = new ArrayList<>();
//        TaggingTokenSynchronizer taggingTokenSynchronizer =
//                new TaggingTokenSynchronizer(GrobidModels.FULLTEXT, result, tokenizations);
//
////        System.out.println(new TaggingTokenClusteror(GrobidModels.FULLTEXT, result, tokenizations).cluster());
////        System.out.println("-----------------------");
////        System.out.println("-----------------------");
////        System.out.println("-----------------------");
//
//        for (LabeledTokensContainer cont : taggingTokenSynchronizer) {
//            if (cont == null) {
//                continue;
//            }
//            fullLabel = cont.getFullLabel();
//            if (fullLabel.endsWith("<figure>") || fullLabel.endsWith("<table>")) {
//                continue;
//            }
//
//            String resultToken = cont.getToken();
//            List<LayoutToken> layoutTokenBuffer = cont.getLayoutTokens();
//
//            if (resultToken.equals("@BULLET")) {
//                resultToken = "•";
//            }
//            s2 = TextUtilities.HTMLEncode(resultToken); // lexical token
//            if (s2.endsWith("-")) {
//                s2 = s2.substring(0, s2.length() - 1);
//            }
//
//            // we synchronize with the TEI
//            //TODO: nasty
//            int teiPosition0 = teiPosition;
//            teiPosition = buffer.indexOf(s2, teiPosition);
//            if (teiPosition - teiPosition0 > s2.length() + 50) {
//                // suspecious large shift... could be due to dehyphenation for instance
//                teiPosition = teiPosition0;
//                Engine.getCntManager().i(TEICounters.TEI_POSITION_REF_MARKERS_OFFSET_TOO_LARGE);
//            }
//            if (teiPosition == -1) {
//                Engine.getCntManager().i(TEICounters.TEI_POSITION_REF_MARKERS_TOK_NOT_FOUND);
//                // token not found, this could be also due to dehyphenation for instance
//                if (s2.length() > 10) {
//                    teiPosition = teiPosition0 - 100;
//                } else {
//                    teiPosition = teiPosition0;
//                }
//            }
//
//            String lastTag0 = GenericTaggerUtils.getPlainLabel(lastTag);
//            String currentTag0 = cont.getPlainLabel();
//
//            if (currentTag0.equals("<citation_marker>") || currentTag0.equals("<figure_marker>") || currentTag0.equals("<table_marker>")) {
//                if (!currentTag0.equals(lastTag0) || cont.isBeginning()) {
//                    startRefPosition = teiPosition;
//                    appendRefStrDataClean(refString, refTokens, s2, layoutTokenBuffer, false);
//                } else {
//                    appendRefStrData(refString, refTokens, s2, layoutTokenBuffer, cont.isSpacePreceding());
//                }
//                endRefPosition = teiPosition + s2.length();
//                teiPosition = teiPosition + s2.length();
////            }
//            } else if ((currentTag0 != null) && (lastTag0 != null) && !currentTag0.equals(lastTag0) && lastTag0.endsWith("_marker>")) {
//                // proceeding with replacement
//                String chunkRefString = refString.toString();
//                if (chunkRefString.trim().length() == 0) {
//                    continue;
//                }
//
//                if (chunkRefString.contains("<") && chunkRefString.contains(">")) {
//                    // normally never appear - inserting tags around this chunk could harm the
//                    // XML hierarchical structure, so we skip this chunk
//                    clearRefStrData(refString, refTokens);
//                    continue;
//                }
//
//                // GENERATING REPLACEMENT
//                String replacement = null;
//                switch (lastTag0) {
//                    case "<citation_marker>":
//                        if (config.getMatchingMode() == GrobidAnalysisConfig.LuceneBased) {
//                            replacement = markReferencesTEILuceneBased(chunkRefString,
//                                    refTokens,
//                                    doc.getReferenceMarkerMatcher(),
//                                    config.isGenerateTeiCoordinates());
//                        } else {
//                            // default
//                            replacement = markReferencesTEI(chunkRefString,
//                                    refTokens,
//                                    bds,
//                                    config.isGenerateTeiCoordinates());
//                        }
//                        break;
//                    case "<figure_marker>":
//                        replacement = markReferencesFigureTEI(chunkRefString, refTokens, figures,
//                                config.isGenerateTeiCoordinates());
//                        break;
//                    case "<table_marker>":
//                        replacement = markReferencesTableTEI(chunkRefString, refTokens, tables,
//                                config.isGenerateTeiCoordinates());
//                        break;
//                }
//
//                // END - GENERATING REPLACEMENT
//
//                if ((startRefPosition == -1) || (endRefPosition == -1)
//                        || (startRefPosition > endRefPosition) || (startRefPosition >= buffer.length())
//                        || (endRefPosition >= buffer.length())) {
//                    teiPosition0 = teiPosition;
//                    int nbMatch = 0;
//                    List<Integer> matches = new ArrayList<>();
//                    while (teiPosition != -1) {
//                        teiPosition = buffer.indexOf(chunkRefString, teiPosition + 1);
//                        if (teiPosition != -1) {
//                            nbMatch++;
//                            matches.add(teiPosition);
//                        }
//                    }
//                    if (nbMatch > 0) {
//                        teiPosition = matches.get(0);
//                        String replacedChunk = buffer.substring(teiPosition, teiPosition + chunkRefString.length());
//                        if ((replacedChunk.contains("<") && replacedChunk.contains(">")) ||
//                                (replacedChunk.contains("&lt;") && replacedChunk.contains("&gt;"))) {
//                            // normally never appear - inserting tags around this chunk could harm the
//                            // XML hierarchical structure, so we skip this chunk
//                            clearRefStrData(refString, refTokens);
//                            teiPosition = teiPosition0;
//                            continue;
//                        }
//
//                        buffer = buffer.replace(teiPosition, teiPosition + chunkRefString.length(), replacement);
//                        Engine.getCntManager().i(TEICounters.CITATION_FIGURE_REF_MARKER_SUBSTITUTED);
////                        lastMatchPos = teiPosition;
//                        teiPosition = teiPosition + replacement.length();
//                    } else {
//                        // select the first position after the current teiPosition
//                        teiPosition = teiPosition0;
//                    }
//                    clearRefStrData(refString, refTokens);
//                } else if ((startRefPosition < buffer.length()) &&
//                        (startRefPosition >= 0) &&
//                        (endRefPosition < buffer.length()) &&
//                        (endRefPosition >= 0) &&
//                        (startRefPosition < endRefPosition) &&
//                        (buffer.substring(startRefPosition, endRefPosition).equals(chunkRefString))) {
//                    String replacedChunk = buffer.substring(startRefPosition, endRefPosition);
//                    if ((replacedChunk.contains("<") && replacedChunk.contains(">")) ||
//                            (replacedChunk.contains("&lt;") && replacedChunk.contains("&gt;"))) {
//                        // normally never appear - inserting tags around this chunk could harm the
//                        // XML hierarchical structure, so we skip this chunk
//                        clearRefStrData(refString, refTokens);
//                        continue;
//                    }
//                    Engine.getCntManager().i(TEICounters.CITATION_FIGURE_REF_MARKER_SUBSTITUTED);
//                    buffer = buffer.replace(startRefPosition, endRefPosition, replacement);
//                    teiPosition = startRefPosition + replacement.length();
////                    lastMatchPos = teiPosition;
//                    clearRefStrData(refString, refTokens);
//                } else {
//                    Engine.getCntManager().i(TEICounters.CITATION_FIGURE_REF_MARKER_MISSED_SUBSTITUTION);
//                }
//            }
//            lastTag = fullLabel;
//        }
//
//        // we output figures and tables
//        if (figures != null) {
//            for (Figure figure : figures) {
//                String figSeg = figure.toTEI(3, config);
//                if (figSeg != null) {
//                    buffer.append(figSeg);
//                }
//            }
//        }
//        if (tables != null) {
//            for (Table table : tables) {
//                String tabSeg = table.toTEI(3, config);
//                if (tabSeg != null) {
//                    buffer.append(tabSeg);
//                }
//            }
//        }
//        return buffer;
//    }

//    private static void clearRefStrData(StringBuilder refStr, List<LayoutToken> toks) {
//        refStr.setLength(0);
//        if (toks != null) {
//            toks.clear();
//        }
//    }
//
//    private static void appendRefStrDataClean(StringBuilder refStr, List<LayoutToken> toks, String data, List<LayoutToken> toAdd, boolean addSpace) {
//        clearRefStrData(refStr, toks);
//
//        if (toAdd == null || toAdd.isEmpty()) {
//            return;
//        }
//
//        refStr.append(addSpace ? " " : "").append(data);
//        if (toks != null) {
//            toks.addAll(toAdd);
//        }
//    }
//
//    private static void appendRefStrData(StringBuilder refStr, List<LayoutToken> toks, String data, List<LayoutToken> toAdd, boolean addSpace) {
//        if (toAdd == null || toAdd.isEmpty()) {
//            return;
//        }
//
//        refStr.append(addSpace ? " " : "").append(data);
//        if (toks != null) {
//            toks.addAll(toAdd);
//        }
//    }

    /**
     * Return the graphic objects in a given interval position in the document.
     */
    private List<GraphicObject> getGraphicObject(List<GraphicObject> graphicObjects, int startPos, int endPos) {
        List<GraphicObject> result = new ArrayList<GraphicObject>();
        for (GraphicObject nto : graphicObjects) {
            if ((nto.getStartPosition() >= startPos) && (nto.getStartPosition() <= endPos)) {
                result.add(nto);
            }
            if (nto.getStartPosition() > endPos) {
                break;
            }
        }
        return result;
    }

    /**
     * TODO some documentation
     */
//    private boolean testClosingTag(StringBuilder buffer,
//                                   String currentTag0,
//                                   String lastTag0,
//                                   String currentTag,
//                                   List<BibDataSet> bds,
//                                   boolean generateIDs,
//                                   boolean figureBlock) {
//        boolean res = false;
//        if (!currentTag0.equals(lastTag0) ||
//                currentTag.equals("I-<paragraph>") ||
//                currentTag.equals("I-<item>") ||
//                currentTag.equals("I-<section>")) {
//			/*if (currentTag0.equals("<citation_marker>") || currentTag0.equals("<figure_marker>")
//			|| currentTag0.equals("<table_marker>")) {
//				return false;
//			}*/
////System.out.println(buffer.toString());
////System.out.println("lastTag0 : " + lastTag0);
////System.out.println("currentTag0 : " + currentTag0);
//            // we get the enclosed text
//            int ind = buffer.lastIndexOf(">");
//            String text;
//            boolean refEnd = false;
//            if (ind != -1) {
//                text = buffer.substring(ind + 1, buffer.length()).trim();
////System.out.println("text : " + text);
//                if (text.length() == 0) {
//
//                }
//                // cleaning
//                int ind2 = buffer.lastIndexOf("<");
//                String tag = buffer.substring(ind2 + 1, ind);
//                if (tag.equals("/ref")) {
//                    buffer.delete(ind + 1, buffer.length());
//                    refEnd = true;
//                } else if (tag.startsWith("/")) {
//                    buffer.delete(ind + 1, buffer.length());
//                } else
//                    buffer.delete(ind2, buffer.length());
//            } else {
//                // this should actually never happen
//                text = buffer.toString().trim();
//            }
//
//            text = TextUtilities.dehyphenize(text);
//            text = text.replace("\n", " ");
//            text = text.replace("  ", " ");
//            text = TextUtilities.trimEncodedCharaters(text).trim();
//
//            String divID = null;
//            if (generateIDs) {
//                divID = KeyGen.getKey().substring(0, 7);
//            }
//            if (lastTag0.equals("<section>")) {
//                // let's have a look at the numbering
//                // we try to recognize the numbering of the section titles
//                Matcher m1 = BasicStructureBuilder.headerNumbering1.matcher(text);
//                Matcher m2 = BasicStructureBuilder.headerNumbering2.matcher(text);
//                Matcher m3 = BasicStructureBuilder.headerNumbering3.matcher(text);
//                Matcher m = null;
//                String numb = null;
//                if (m1.find()) {
//                    numb = m1.group(0);
//                    m = m1;
//                } else if (m2.find()) {
//                    numb = m2.group(0);
//                    m = m2;
//                } else if (m3.find()) {
//                    numb = m3.group(0);
//                    m = m3;
//                }
//                if (numb != null) {
//                    text = text.replace(numb, "").trim();
//                    numb = numb.replace(" ", "");
//                    if (numb.endsWith("."))
//                        numb = numb.substring(0, numb.length() - 1);
//                    if (generateIDs)
//                        text = "<head n=\"" + numb + "\" xml:id=\"_" + divID + "\">" + text;
//                    else
//                        text = "<head n=\"" + numb + "\">" + text;
//                } else {
//                    if (generateIDs)
//                        text = "<head xml:id=\"_" + divID + "\">" + text;
//                    else
//                        text = "<head>" + text;
//                }
//            }
//
//            res = false;
//
//            // we close the current tag
//            if (lastTag0.equals("<other>")) {
//                buffer.append("<note");
//                if (generateIDs)
//                    buffer.append(" xml:id=\"_" + divID + "\"");
//                buffer.append(">" + text + "</note>\n\n");
//            } else if (lastTag0.equals("<paragraph>")) {
//                if (refEnd) {
//                    if (figureBlock)
//                        buffer.append(text + "</p0>\n\n");
//                    else
//                        buffer.append(text + "</p>\n\n");
//                    res = true;
//                } else {
//                    buffer.append("<p");
//                    if (generateIDs)
//                        buffer.append(" xml:id=\"_" + divID + "\"");
//                    buffer.append(">" + text);
//                    if (!currentTag0.endsWith("<figure_marker>") && !currentTag0.endsWith("<table_marker>") &&
//                            !currentTag0.endsWith("<citation_marker>")) {
//                        if (figureBlock) {
//                            // we add a "marker" for later evaluating if we need or not to reconnect the
//                            // cut paragraph
//                            buffer.append("</p0>\n\n");
//                        } else
//                            buffer.append("</p>\n\n");
//                    }
//                    res = true;
//                    // return true only when the paragraph is closed
//                }
//            } else if (lastTag0.equals("<section>")) {
//                buffer.append(text + "</head>\n\n");
//            } /*else if (lastTag0.equals("<subsection>")) {
//                buffer.append(text + "</head>\n\n");
//
//            } */ else if (lastTag0.equals("<equation>")) {
//                buffer.append("<p><formula>" + text + "</formula></p>\n\n");
//
//            } else if (lastTag0.equals("<item>")) {
//                buffer.append("<item");
//                if (generateIDs)
//                    buffer.append(" xml:id=\"_" + divID + "\"");
//                buffer.append(">" + text + "</item>\n\n");
//            } else {
//                res = false;
//            }
//
//        }
//
//        return res;
//    }

    public StringBuilder toTEIReferences(StringBuilder tei,
                                         List<BibDataSet> bds,
                                         GrobidAnalysisConfig config) throws Exception {
        tei.append("\t\t\t<div type=\"references\">\n\n");

        if ((bds == null) || (bds.size() == 0))
            tei.append("\t\t\t\t<listBibl/>\n");
        else {
            tei.append("\t\t\t\t<listBibl>\n");

            int p = 0;
            if (bds.size() > 0) {
                for (BibDataSet bib : bds) {
                    BiblioItem bit = bib.getResBib();
                    if (bit != null) {
                        tei.append("\n" + bit.toTEI(p, 0, config));
                    } else {
                        tei.append("\n");
                    }
                    p++;
                }
            }
            tei.append("\n\t\t\t\t</listBibl>\n");
        }
        tei.append("\t\t\t</div>\n");

        return tei;
    }


    //bounding boxes should have already been calculated when calling this method
    public static String getCoordsAttribute(List<BoundingBox> boundingBoxes, boolean generateCoordinates) {
        if (!generateCoordinates || boundingBoxes == null || boundingBoxes.isEmpty()) {
            return "";
        }
        String coords = Joiner.on(";").join(boundingBoxes);
        return "coords=\"" + coords + "\"";
    }

    /**
     * Mark using TEI annotations the identified references in the text body build with the machine learning model.
     */
    public String markReferencesTEI(String text, List<LayoutToken> refTokens,
                                    List<BibDataSet> bds, boolean generateCoordinates) {
        // safety tests
        if (text == null)
            return null;
        if (text.trim().length() == 0)
            return text;
        if (text.endsWith("</ref>") || text.startsWith("<ref"))
            return text;

        CntManager cntManager = Engine.getCntManager();

        text = TextUtilities.HTMLEncode(text);
        boolean numerical = false;

        String coords = null;
        if (generateCoordinates)
            coords = LayoutTokensUtil.getCoordsString(refTokens);
        if (coords == null) {
            coords = "";
        } else {
            coords = "coords=\"" + coords + "\"";
        }
        // we check if we have numerical references

        // we re-write compact references, i.e [1,2] -> [1] [2]
        //
        String relevantText = bracketReferenceSegment(text);
        if (relevantText != null) {
            Matcher m2 = numberRefCompact.matcher(text);
            StringBuffer sb = new StringBuffer();
            boolean result = m2.find();
            // Loop through and create a new String
            // with the replacements
            while (result) {
                String toto = m2.group(0);
                if (toto.contains("]")) {
                    toto = toto.replace(",", "] [");
                    toto = toto.replace("[ ", "[");
                    toto = toto.replace(" ]", "]");
                } else {
                    toto = toto.replace(",", ") (");
                    toto = toto.replace("( ", "(");
                    toto = toto.replace(" )", ")");
                }
                m2.appendReplacement(sb, toto);
                result = m2.find();
            }
            // Add the last segment of input to
            // the new String
            m2.appendTail(sb);
            text = sb.toString();

            // we expend the references [1-3] -> [1] [2] [3]
            Matcher m3 = numberRefCompact2.matcher(text);
            StringBuffer sb2 = new StringBuffer();
            boolean result2 = m3.find();
            // Loop through and create a new String
            // with the replacements
            while (result2) {
                String toto = m3.group(0);
                if (toto.contains("]")) {
                    toto = toto.replace("]", "");
                    toto = toto.replace("[", "");
                    int ind = toto.indexOf('-');
                    if (ind == -1)
                        ind = toto.indexOf('\u2013');
                    if (ind != -1) {
                        try {
                            int firstIndex = Integer.parseInt(toto.substring(0, ind));
                            int secondIndex = Integer.parseInt(toto.substring(ind + 1, toto.length()));
                            // how much values can we expend? We use a ratio of the total number of references
                            // with a minimal value
                            int maxExpend = 10 + (bds.size() / 10);
                            if (secondIndex - firstIndex > maxExpend) {
                                break;
                            }
                            toto = "";
                            boolean first = true;
                            for (int j = firstIndex; j <= secondIndex; j++) {
                                if (first) {
                                    toto += "[" + j + "]";
                                    first = false;
                                } else
                                    toto += " [" + j + "]";
                            }
                        } catch (Exception e) {
                            throw new GrobidException("An exception occurs.", e);
                        }
                    }
                } else {
                    toto = toto.replace(")", "");
                    toto = toto.replace("(", "");
                    int ind = toto.indexOf('-');
                    if (ind == -1)
                        ind = toto.indexOf('\u2013');
                    if (ind != -1) {
                        try {
                            int firstIndex = Integer.parseInt(toto.substring(0, ind));
                            int secondIndex = Integer.parseInt(toto.substring(ind + 1, toto.length()));
                            if (secondIndex - firstIndex > 9) {
                                break;
                            }
                            toto = "";
                            boolean first = true;
                            for (int j = firstIndex; j <= secondIndex; j++) {
                                if (first) {
                                    toto += "(" + j + ")";
                                    first = false;
                                } else
                                    toto += " (" + j + ")";
                            }
                        } catch (Exception e) {
                            throw new GrobidException("An exception occurs.", e);
                        }
                    }
                }
                m3.appendReplacement(sb2, toto);
                result2 = m3.find();
            }
            // Add the last segment of input to
            // the new String
            m3.appendTail(sb2);
            text = sb2.toString();
        }
        int p = 0;
        if ((bds != null) && (bds.size() > 0)) {
            for (BibDataSet bib : bds) {
                List<String> contexts = bib.getSourceBib();
                String marker = TextUtilities.HTMLEncode(bib.getRefSymbol());
                BiblioItem resBib = bib.getResBib();

                if (resBib != null) {
                    // try first to match the reference marker string with marker (label) present in the
                    // bibliographical section
                    if (marker != null) {
                        Matcher m = numberRef.matcher(marker);
                        int ind = -1;
                        if (m.find()) {
                            ind = text.indexOf(marker);
                        } else {
                            // possibly the marker in the biblio section is simply a number, and used
                            // in the ref. with brackets - so we also try this case
                            m = numberRef.matcher("[" + marker + "]");
                            if (m.find()) {
                                ind = text.indexOf("[" + marker + "]");
                                if (ind != -1) {
                                    marker = "[" + marker + "]";
                                }
                            }

                        }
                        if (ind != -1) {
                            text = text.substring(0, ind) +
                                    "<ref type=\"bibr\" target=\"#b" + p + "\" " + coords + ">" + marker
                                    + "</ref>" + text.substring(ind + marker.length(), text.length());
                            cntManager.i(ReferenceMarkerMatcher.Counters.MATCHED_REF_MARKERS);
                        }
                    }

                    // search for first author, date and possibly second author
                    String author1 = resBib.getFirstAuthorSurname();
                    String author2 = null;
                    if (author1 != null) {
                        author1 = author1.toLowerCase();
                    }
                    String year = null;
                    Date datt = resBib.getNormalizedPublicationDate();
                    if (datt != null) {
                        if (datt.getYear() != -1) {
                            year = "" + datt.getYear();
                        }
                    }
                    char extend1 = 0;
                    // we check if we have an identifier with the year (e.g. 2010b)
                    if (resBib.getPublicationDate() != null) {
                        String dat = resBib.getPublicationDate();
                        if (year != null) {
                            int ind = dat.indexOf(year);
                            if (ind != -1) {
                                if (ind + year.length() < dat.length()) {
                                    extend1 = dat.charAt(ind + year.length());
                                }
                            }
                        }
                    }

                    List<Person> fullAuthors = resBib.getFullAuthors();
                    if (fullAuthors != null) {
                        int nbAuthors = fullAuthors.size();
                        if (nbAuthors == 2) {
                            // we get the last name of the second author
                            author2 = fullAuthors.get(1).getLastName();
                        }
                    }
                    if (author2 != null) {
                        author2 = author2.toLowerCase();
                    }

                    // try to match based on the author and year strings
                    if ((author1 != null) && (year != null)) {
                        int indi1; // first author
                        int indi2; // year
                        int indi3 = -1; // second author if only two authors in total
                        int i = 0;
                        boolean end = false;

                        while (!end) {
                            indi1 = text.toLowerCase().indexOf(author1, i); // first author matching
                            indi2 = text.indexOf(year, i); // year matching
                            int added = 1;
                            if (author2 != null) {
                                indi3 = text.toLowerCase().indexOf(author2, i); // second author matching
                            }
                            char extend2 = 0;
                            if (indi2 != -1) {
                                if (text.length() > indi2 + year.length()) {
                                    extend2 = text.charAt(indi2 + year.length()); // (e.g. 2010b)
                                }
                            }

                            if ((indi1 == -1) || (indi2 == -1)) {
                                end = true;
                                // no author has been found, we go on with the next biblio item
                            } else if ((indi1 != -1) && (indi2 != -1) && (indi3 != -1) && (indi1 < indi2) &&
                                    (indi1 < indi3) && (indi2 - indi1 > author1.length())) {
                                // this is the case with 2 authors in the marker

                                if ((extend1 != 0) && (extend2 != 0) && (extend1 != extend2)) {
                                    end = true;
                                    // we have identifiers with the year, but they don't match
                                    // e.g. 2010a != 2010b
                                } else {
                                    // we check if we don't have another instance of the author between the two indices
                                    int indi1bis = text.toLowerCase().indexOf(author1, indi1 + author1.length());
                                    if (indi1bis == -1) {
                                        String reference = text.substring(indi1, indi2 + 4);
                                        boolean extended = false;
                                        if (text.length() > indi2 + 4) {
                                            if ((text.charAt(indi2 + 4) == ')') ||
                                                    (text.charAt(indi2 + 4) == ']') ||
                                                    ((extend1 != 0) && (extend2 != 0) && (extend1 == extend2))) {
                                                reference += text.charAt(indi2 + 4);
                                                extended = true;
                                            }
                                        }
                                        String previousText = text.substring(0, indi1);
                                        String followingText = "";
                                        if (extended) {
                                            followingText = text.substring(indi2 + 5, text.length());
                                            // 5 digits for the year + identifier character
                                            text = "<ref type=\"bibr\" target=\"#b" + p + "\" " + coords + ">" + reference + "</ref>";
                                            cntManager.i(ReferenceMarkerMatcher.Counters.MATCHED_REF_MARKERS);
                                            added = 8;

                                        } else {
                                            followingText = text.substring(indi2 + 4, text.length());
                                            // 4 digits for the year
                                            text = "<ref type=\"bibr\" target=\"#b" + p + "\" " + coords + ">" + reference + "</ref>";
                                            cntManager.i(ReferenceMarkerMatcher.Counters.MATCHED_REF_MARKERS);
                                            added = 7;
                                        }
                                        if (previousText.length() > 2) {
                                            previousText =
                                                    markReferencesTEI(previousText, refTokens, bds,
                                                            generateCoordinates);
                                        }
                                        if (followingText.length() > 2) {
                                            followingText =
                                                    markReferencesTEI(followingText, refTokens, bds,
                                                            generateCoordinates);
                                        }

                                        return previousText + text + followingText;
                                    }
                                    end = true;
                                }
                            } else if ((indi1 != -1) && (indi2 != -1) && (indi1 < indi2) &&
                                    (indi2 - indi1 > author1.length())) {
                                // this is the case with 1 author in the marker

                                if ((extend1 != 0) && (extend2 != 0) && (extend1 != extend2)) {
                                    end = true;
                                } else {
                                    // we check if we don't have another instance of the author between the two indices
                                    int indi1bis = text.toLowerCase().indexOf(author1, indi1 + author1.length());
                                    if (indi1bis == -1) {
                                        String reference = text.substring(indi1, indi2 + 4);
                                        boolean extended = false;
                                        if (text.length() > indi2 + 4) {
                                            if ((text.charAt(indi2 + 4) == ')') ||
                                                    (text.charAt(indi2 + 4) == ']') ||
                                                    ((extend1 != 0) && (extend2 != 0) & (extend1 == extend2))) {
                                                reference += text.charAt(indi2 + 4);
                                                extended = true;
                                            }
                                        }
                                        String previousText = text.substring(0, indi1);
                                        String followingText = "";
                                        if (extended) {
                                            followingText = text.substring(indi2 + 5, text.length());
                                            // 5 digits for the year + identifier character
                                            text = "<ref type=\"bibr\" target=\"#b" + p + "\" " + coords + ">" + reference + "</ref>";
                                            cntManager.i(ReferenceMarkerMatcher.Counters.MATCHED_REF_MARKERS);
                                            added = 8;
                                        } else {
                                            followingText = text.substring(indi2 + 4, text.length());
                                            // 4 digits for the year
                                            text = "<ref type=\"bibr\" target=\"#b" + p + "\" " + coords + ">" + reference + "</ref>";
                                            cntManager.i(ReferenceMarkerMatcher.Counters.MATCHED_REF_MARKERS);
                                            added = 7;
                                        }
                                        if (previousText.length() > 2) {
                                            previousText =
                                                    markReferencesTEI(previousText, refTokens, bds,
                                                            generateCoordinates);
                                        }
                                        if (followingText.length() > 2) {
                                            followingText =
                                                    markReferencesTEI(followingText, refTokens, bds,
                                                            generateCoordinates);
                                        }

                                        return previousText + text + followingText;
                                    }
                                    end = true;
                                }
                            }
                            i = indi2 + year.length() + added;
                            if (i >= text.length()) {
                                end = true;
                            }
                        }
                    }
                }
                p++;
            }
        }

        // we have not been able to solve the bibliographical marker, but we still annotate it globally
        // without pointer - just ignoring possible punctuation at the beginning and end of the string
        if (!text.endsWith("</ref>") && !text.startsWith("<ref"))
            text = "<ref type=\"bibr\">" + text + "</ref>";
        cntManager.i(ReferenceMarkerMatcher.Counters.UNMATCHED_REF_MARKERS);
        return text;
    }

    /**
     * Mark using TEI annotations the identified references in the text body build with the machine learning model.
     */
    public List<Node> markReferencesTEILuceneBased(String text, List<LayoutToken> refTokens,
                                                   ReferenceMarkerMatcher markerMatcher, boolean generateCoordinates) throws EntityMatcherException {
        // safety tests
        if (text == null)
            return null;
        if (text.trim().length() == 0)
            return Collections.<Node>singletonList(new Text(text));
        if (text.endsWith("</ref>") || text.startsWith("<ref")) {
            return Collections.<Node>singletonList(new Text(text));
        }

        List<Node> nodes = new ArrayList<>();

        for (ReferenceMarkerMatcher.MatchResult matchResult : markerMatcher.match(refTokens)) {
            String markerText = LayoutTokensUtil.normalizeText(matchResult.getText());
            String coords = null;
            if (generateCoordinates && matchResult.getTokens() != null) {
                coords = LayoutTokensUtil.getCoordsString(matchResult.getTokens());
            }

            Element ref = teiElement("ref");
            ref.addAttribute(new Attribute("type", "bibr"));

            if (coords != null) {
                ref.addAttribute(new Attribute("coords", coords));
            }
            ref.appendChild(markerText);

            if (matchResult.getBibDataSet() != null) {
                ref.addAttribute(new Attribute("target", "#b" + matchResult.getBibDataSet().getResBib().getOrdinal()));
            }
            nodes.add(ref);
        }
        return nodes;
    }

    /**
     * Identify in a reference string the part in bracket. Return null if no opening and closing bracket
     * can be found.
     */
    public static String bracketReferenceSegment(String text) {
        int ind1 = text.indexOf("(");
        if (ind1 == -1)
            ind1 = text.indexOf("[");
        if (ind1 != -1) {
            int ind2 = text.lastIndexOf(")");
            if (ind2 == -1)
                ind2 = text.lastIndexOf("]");
            if ((ind2 != -1) && (ind1 < ind2)) {
                return text.substring(ind1, ind2 + 1);
            }
        }
        return null;
    }

    public String markReferencesFigureTEI(String text, List<LayoutToken> refTokens,
                                          List<Figure> figures,
                                          boolean generateCoordinates) {
        if (text == null)
            return null;
        if (text.trim().length() == 0)
            return text;
        if (figures == null)
            return text;
        String coords = null;
        if (generateCoordinates)
            coords = LayoutTokensUtil.getCoordsString(refTokens);
        if (coords == null) {
            coords = "";
        } else {
            coords = "coords=\"" + coords + "\"";
        }
        String textLow = text.toLowerCase();
        String bestFigure = null;
        for (Figure figure : figures) {
            if ((figure.getLabel() != null) && (figure.getLabel().length() > 0)) {
                String label = TextUtilities.cleanField(figure.getLabel().toString(), false);
                if ((label.length() > 0) &&
                        (textLow.indexOf(label.toLowerCase()) != -1)) {
                    bestFigure = figure.getId();
                    break;
                }
            }
        }

        text = TextUtilities.HTMLEncode(text).replace("\n", " ").trim();
        if (bestFigure != null) {
            text = "<ref type=\"figure\" target=\"#fig_" + bestFigure + "\" " + coords + ">" + text + "</ref>";
        } else {
            text = "<ref type=\"figure\">" + text + "</ref>";
        }
        return text;
    }

    public String markReferencesTableTEI(String text, List<LayoutToken> refTokens,
                                         List<Table> tables,
                                         boolean generateCoordinates) {
        if (text == null)
            return null;
        if (text.trim().length() == 0)
            return text;
        if (tables == null)
            return text;
        String coords = null;
        if (generateCoordinates)
            coords = LayoutTokensUtil.getCoordsString(refTokens);
        if (coords == null) {
            coords = "";
        } else {
            coords = "coords=\"" + coords + "\"";
        }
        String textLow = text.toLowerCase();
        String bestTable = null;
        for (Table table : tables) {
            if ((table.getId() != null) &&
                    (table.getId().length() > 0) &&
                    (textLow.indexOf(table.getId().toLowerCase()) != -1)) {
                bestTable = table.getId();
                break;
            }
        }

        text = TextUtilities.HTMLEncode(text).replace("\n", " ").trim();
        if (bestTable != null) {
            text = "<ref type=\"table\" target=\"#tab_" + bestTable + "\" " + coords + ">" + text + "</ref>";
        } else {
            text = "<ref type=\"table\">" + text + "</ref>";
        }
        return text;
    }

    private String normalizeText(String localText) {
        localText = localText.trim();
        localText = TextUtilities.dehyphenize(localText);
        localText = localText.replace("\n", " ");
        localText = localText.replace("  ", " ");

        return localText.trim();
    }
}