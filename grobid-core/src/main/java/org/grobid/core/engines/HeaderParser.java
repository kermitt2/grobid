package org.grobid.core.engines;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.GrobidModels;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.data.Person;
import org.grobid.core.data.Keyword;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.document.TEIFormater;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorHeader;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.Consolidation;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * @author Patrice Lopez
 */
public class HeaderParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderParser.class);

    private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();

    private Consolidation consolidator = null;

    private EngineParsers parsers;

    public HeaderParser(EngineParsers parsers) {
        super(GrobidModels.HEADER);
        this.parsers = parsers;
        GrobidProperties.getInstance();
    }

    /**
     * Processing with application of the segmentation model
     */
    public Pair<String, Document> processing(File input, BiblioItem resHeader, GrobidAnalysisConfig config) {
        Document doc = parsers.getSegmentationParser().processing(input, config);

        String tei = processingHeaderSection(config.isConsolidateHeader(), doc, resHeader);
        return new ImmutablePair<String, Document>(tei, doc);
    }

    /**
     * Processing without application of the segmentation model, regex are used to identify the header
     * zone.
     */
    public Pair<String, Document> processing2(String pdfInput, boolean consolidate,
                                              BiblioItem resHeader, GrobidAnalysisConfig config) {
        DocumentSource documentSource = null;
        try {
            documentSource = DocumentSource.fromPdf(new File(pdfInput), config.getStartPage(), config.getEndPage());
            Document doc = new Document(documentSource);
            doc.addTokenizedDocument(config);

            if (doc.getBlocks() == null) {
                throw new GrobidException("PDF parsing resulted in empty content");
            }

            String tei = processingHeaderBlock(consolidate, doc, resHeader);
            return Pair.of(tei, doc);
        } finally {
            if (documentSource != null) {
                documentSource.close(true);
            }
        }
    }

    /**
     * Header processing after identification of the header blocks with heuristics (old approach)
     */
    public String processingHeaderBlock(boolean consolidate, Document doc, BiblioItem resHeader) {
        String header;
        //if (doc.getBlockDocumentHeaders() == null) {
            header = doc.getHeaderFeatured(true, true);
        /*} else {
            header = doc.getHeaderFeatured(false, true);
        }*/
        List<LayoutToken> tokenizations = doc.getTokenizationsHeader();
//System.out.println(tokenizations.toString());

        if ((header != null) && (header.trim().length() > 0)) {
            String res = label(header);
            resHeader = resultExtraction(res, true, tokenizations, resHeader);

            // language identification
            String contentSample = "";
            if (resHeader.getTitle() != null)
                contentSample += resHeader.getTitle();
            if (resHeader.getAbstract() != null)
                contentSample += "\n" + resHeader.getAbstract();
            if (resHeader.getKeywords() != null)
                contentSample += "\n" + resHeader.getKeywords();
            if (contentSample.length() < 200) {
                // we need more textual content to ensure that the language identification will be
                // correct
                contentSample += doc.getBody();
            }
            Language langu = languageUtilities.runLanguageId(contentSample);
            if (langu != null) {
                String lang = langu.getLangId();
                doc.setLanguage(lang);
                resHeader.setLanguage(lang);
            }

            if (resHeader != null) {
                if (resHeader.getAbstract() != null) {
                    resHeader.setAbstract(TextUtilities.dehyphenizeHard(resHeader.getAbstract()));
                    //resHeader.setAbstract(TextUtilities.dehyphenize(resHeader.getAbstract()));
                }
                BiblioItem.cleanTitles(resHeader);
                if (resHeader.getTitle() != null) {
                    // String temp =
                    // utilities.dehyphenizeHard(resHeader.getTitle());
                    String temp = TextUtilities.dehyphenize(resHeader.getTitle());
                    temp = temp.trim();
                    if (temp.length() > 1) {
                        if (temp.startsWith("1"))
                            temp = temp.substring(1, temp.length());
                        temp = temp.trim();
                    }
                    resHeader.setTitle(temp);
                }
                if (resHeader.getBookTitle() != null) {
                    resHeader.setBookTitle(TextUtilities.dehyphenize(resHeader.getBookTitle()));
                }

                resHeader.setOriginalAuthors(resHeader.getAuthors());
                boolean fragmentedAuthors = false;
                boolean hasMarker = false;
                List<Integer> authorsBlocks = new ArrayList<Integer>();
                String[] authorSegments;
                if (resHeader.getAuthors() != null) {
                    ArrayList<String> auts;
                    authorSegments = resHeader.getAuthors().split("\n");
                    if (authorSegments.length > 1) {
                        fragmentedAuthors = true;
                    }
                    for (int k = 0; k < authorSegments.length; k++) {
                        auts = new ArrayList<String>();
                        auts.add(authorSegments[k]);
                        List<Person> localAuthors = parsers.getAuthorParser().processingHeader(auts);
                        if (localAuthors != null) {
                            for (Person pers : localAuthors) {
                                resHeader.addFullAuthor(pers);
                                if (pers.getMarkers() != null) {
                                    hasMarker = true;
                                }
                                authorsBlocks.add(k);
                            }
                        }
                    }

                    resHeader.setFullAffiliations(
						parsers.getAffiliationAddressParser().processReflow(res, tokenizations));
                    resHeader.attachEmails();
                    boolean attached = false;
                    if (fragmentedAuthors && !hasMarker) {
                        if (resHeader.getFullAffiliations() != null) {
                            if (authorSegments != null) {
                                if (resHeader.getFullAffiliations().size() == authorSegments.length) {
                                    int k = 0;
                                    for (Person pers : resHeader.getFullAuthors()) {
                                        if (k < authorsBlocks.size()) {
                                            int indd = authorsBlocks.get(k);
                                            if (indd < resHeader.getFullAffiliations().size()) {
                                                pers.addAffiliation(resHeader.getFullAffiliations().get(indd));
                                            }
                                        }
                                        k++;
                                    }
                                    attached = true;
                                    resHeader.setFullAffiliations(null);
                                    resHeader.setAffiliation(null);
                                }
                            }
                        }
                    }
                    if (!attached) {
                        resHeader.attachAffiliations();
                    }

                    if (resHeader.getEditors() != null) {
                        ArrayList<String> edits = new ArrayList<String>();
                        edits.add(resHeader.getEditors());

                        resHeader.setFullEditors(parsers.getAuthorParser().processingHeader(edits));
                        // resHeader.setFullEditors(authorParser.processingCitation(edits));
                    }

                    if (resHeader.getReference() != null) {
                        BiblioItem refer = parsers.getCitationParser().processing(resHeader.getReference(), false);
                        if (refer != null)
                            BiblioItem.correct(resHeader, refer);
                    }
                }

				// keyword post-processing
				if (resHeader.getKeyword() != null) {
					String keywords = TextUtilities.dehyphenize(resHeader.getKeyword());
					keywords = BiblioItem.cleanKeywords(keywords);
					resHeader.setKeyword(keywords.replace("\n", " ").replace("  ", " "));
					List<Keyword> keywordsSegmented = BiblioItem.segmentKeywords(keywords);
					if ( (keywordsSegmented != null) && (keywordsSegmented.size() > 0) )
						resHeader.setKeywords(keywordsSegmented);
				}

                // DOI pass
                List<String> dois = doc.getDOIMatches();
                if (dois != null) {
                    if ((dois.size() == 1) && (resHeader != null)) {
                        resHeader.setDOI(dois.get(0));
                    }
                }

                if (consolidate) {
                    resHeader = consolidateHeader(resHeader);
                }

                // normalization of dates
                if (resHeader != null) {
                    if (resHeader.getPublicationDate() != null) {
                        List<Date> dates = parsers.getDateParser().processing(resHeader.getPublicationDate());
                        // most basic heuristic, we take the first date - to be
                        // revised...
                        if (dates != null) {
                            if (dates.size() > 0) {
                                resHeader.setNormalizedPublicationDate(dates.get(0));
                            }
                        }
                    }

                    if (resHeader.getSubmissionDate() != null) {
                        List<Date> dates = parsers.getDateParser().processing(resHeader.getSubmissionDate());
                        if (dates != null) {
                            if (dates.size() > 0) {
                                resHeader.setNormalizedSubmissionDate(dates.get(0));
                            }
                        }
                    }
                }
            }
        } else {
            LOGGER.debug("WARNING: header is empty.");
        }

        TEIFormater teiFormater = new TEIFormater(doc);
        StringBuilder tei = teiFormater.toTEIHeader(resHeader, null, GrobidAnalysisConfig.builder().consolidateHeader(consolidate).build());
        tei.append("\t</text>\n");
        tei.append("</TEI>\n");
        //LOGGER.debug(tei.toString());
        return tei.toString();
    }

    /**
     * Header processing after application of the segmentation model (new approach)
     */
    public String processingHeaderSection(boolean consolidate, Document doc, BiblioItem resHeader) {
        try {
            SortedSet<DocumentPiece> documentHeaderParts = doc.getDocumentPart(SegmentationLabel.HEADER);
            List<LayoutToken> tokenizations = doc.getTokenizations();

            if (documentHeaderParts != null) {
                List<LayoutToken> tokenizationsHeader = new ArrayList<LayoutToken>();

                for (DocumentPiece docPiece : documentHeaderParts) {
                    DocumentPointer dp1 = docPiece.a;
                    DocumentPointer dp2 = docPiece.b;

                    int tokens = dp1.getTokenDocPos();
                    int tokene = dp2.getTokenDocPos();
                    for (int i = tokens; i < tokene; i++) {
                        tokenizationsHeader.add(tokenizations.get(i));
                    }
                }

                String header = getSectionHeaderFeatured(doc, documentHeaderParts, true);
                String res = null;
                if ((header != null) && (header.trim().length() > 0)) {                 
                    res = label(header);
                    resHeader = resultExtraction(res, true, tokenizations, resHeader);                 
                }

                // language identification
                String contentSample = "";
                if (resHeader.getTitle() != null)
                    contentSample += resHeader.getTitle();
                if (resHeader.getAbstract() != null)
                    contentSample += "\n" + resHeader.getAbstract();
                if (contentSample.length() < 200) {
                    // we need more textual content to ensure that the language identification will be
                    // correct
                    SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);
                    StringBuilder contentBuffer = new StringBuilder();
                    for (DocumentPiece docPiece : documentBodyParts) {
                        DocumentPointer dp1 = docPiece.a;
                        DocumentPointer dp2 = docPiece.b;

                        int tokens = dp1.getTokenDocPos();
                        int tokene = dp2.getTokenDocPos();
                        for (int i = tokens; i < tokene; i++) {
                            contentBuffer.append(tokenizations.get(i));
                            contentBuffer.append(" ");
                        }
                    }
                    contentSample += " " + contentBuffer.toString();
                }
                Language langu = languageUtilities.runLanguageId(contentSample);
                if (langu != null) {
                    String lang = langu.getLangId();
                    doc.setLanguage(lang);
                    resHeader.setLanguage(lang);
                }

                if (resHeader != null) {
                    if (resHeader.getAbstract() != null) {
                        resHeader.setAbstract(TextUtilities.dehyphenizeHard(resHeader.getAbstract()));
                        //resHeader.setAbstract(TextUtilities.dehyphenize(resHeader.getAbstract()));
                    }
                    BiblioItem.cleanTitles(resHeader);
                    if (resHeader.getTitle() != null) {
                        // String temp =
                        // utilities.dehyphenizeHard(resHeader.getTitle());
                        String temp = TextUtilities.dehyphenize(resHeader.getTitle());
                        temp = temp.trim();
                        if (temp.length() > 1) {
                            if (temp.startsWith("1"))
                                temp = temp.substring(1, temp.length());
                            temp = temp.trim();
                        }
                        resHeader.setTitle(temp);
                    }
                    if (resHeader.getBookTitle() != null) {
                        resHeader.setBookTitle(TextUtilities.dehyphenize(resHeader.getBookTitle()));
                    }

                    resHeader.setOriginalAuthors(resHeader.getAuthors());
                    boolean fragmentedAuthors = false;
                    boolean hasMarker = false;
                    List<Integer> authorsBlocks = new ArrayList<Integer>();
                    String[] authorSegments = null;
                    if (resHeader.getAuthors() != null) {
                        List<String> auts;
                        authorSegments = resHeader.getAuthors().split("\n");
                        if (authorSegments.length > 1) {
                            fragmentedAuthors = true;
                        }
                        for (int k = 0; k < authorSegments.length; k++) {
                            auts = new ArrayList<String>();
                            auts.add(authorSegments[k]);
                            List<Person> localAuthors = parsers.getAuthorParser().processingHeader(auts);
                            if (localAuthors != null) {
                                for (Person pers : localAuthors) {
                                    resHeader.addFullAuthor(pers);
                                    if (pers.getMarkers() != null) {
                                        hasMarker = true;
                                    }
                                    authorsBlocks.add(k);
                                }
                            }
                        }
                    }

                    resHeader.setFullAffiliations(
						parsers.getAffiliationAddressParser().processReflow(res, tokenizations));
                    resHeader.attachEmails();
                    boolean attached = false;
                    if (fragmentedAuthors && !hasMarker) {
                        if (resHeader.getFullAffiliations() != null) {
                            if (authorSegments != null) {
                                if (resHeader.getFullAffiliations().size() == authorSegments.length) {
                                    int k = 0;
                                    for (Person pers : resHeader.getFullAuthors()) {
                                        if (k < authorsBlocks.size()) {
                                            int indd = authorsBlocks.get(k);
                                            if (indd < resHeader.getFullAffiliations().size()) {
                                                pers.addAffiliation(resHeader.getFullAffiliations().get(indd));
                                            }
                                        }
                                        k++;
                                    }
                                    attached = true;
                                    resHeader.setFullAffiliations(null);
                                    resHeader.setAffiliation(null);
                                }
                            }
                        }
                    }
                    if (!attached) {
                        resHeader.attachAffiliations();
                    }

                    if (resHeader.getEditors() != null) {
                        List<String> edits = new ArrayList<String>();
                        edits.add(resHeader.getEditors());

                        resHeader.setFullEditors(parsers.getAuthorParser().processingHeader(edits));
                        // resHeader.setFullEditors(authorParser.processingCitation(edits));
                    }

                    if (resHeader.getReference() != null) {
                        BiblioItem refer = parsers.getCitationParser().processing(resHeader.getReference(), false);
                        BiblioItem.correct(resHeader, refer);
                    }
                }

				// keyword post-processing
				if (resHeader.getKeyword() != null) {
					String keywords = TextUtilities.dehyphenize(resHeader.getKeyword());
					keywords = BiblioItem.cleanKeywords(keywords);
					resHeader.setKeyword(keywords.replace("\n", " ").replace("  ", " "));
					List<Keyword> keywordsSegmented = BiblioItem.segmentKeywords(keywords);
					if ( (keywordsSegmented != null) && (keywordsSegmented.size() > 0) )
						resHeader.setKeywords(keywordsSegmented);
				}

                // DOI pass
                List<String> dois = doc.getDOIMatches();
                if (dois != null) {
                    if ((dois.size() == 1) && (resHeader != null)) {
                        resHeader.setDOI(dois.get(0));
                    }
                }

                if (consolidate) {
                    resHeader = consolidateHeader(resHeader);
                }

                // normalization of dates
                if (resHeader != null) {
                    if (resHeader.getPublicationDate() != null) {
                        List<Date> dates = parsers.getDateParser().processing(resHeader.getPublicationDate());
                        // most basic heuristic, we take the first date - to be
                        // revised...
                        if (dates != null) {
                            if (dates.size() > 0) {
                                resHeader.setNormalizedPublicationDate(dates.get(0));
                            }
                        }
                    }

                    if (resHeader.getSubmissionDate() != null) {
                        List<Date> dates = parsers.getDateParser().processing(resHeader.getSubmissionDate());
                        if (dates != null) {
                            if (dates.size() > 0) {
                                resHeader.setNormalizedSubmissionDate(dates.get(0));
                            }
                        }
                    }
                }

                TEIFormater teiFormater = new TEIFormater(doc);
                StringBuilder tei = teiFormater.toTEIHeader(resHeader, null, GrobidAnalysisConfig.defaultInstance());
                tei.append("\t</text>\n");
                tei.append("</TEI>\n");
                //LOGGER.debug(tei);
                return tei.toString();
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
        return null;
    }


    /**
     * Return the header section with features to be processed by the CRF model
     */
    private String getSectionHeaderFeatured(Document doc,
                                            SortedSet<DocumentPiece> documentHeaderParts,
                                            boolean withRotation) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();
        StringBuilder header = new StringBuilder();
        String currentFont = null;
        int currentFontSize = -1;

        // vector for features
        FeaturesVectorHeader features;
        boolean endblock;
        //for (Integer blocknum : blockDocumentHeaders) {
        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }

        for (DocumentPiece docPiece : documentHeaderParts) {
            DocumentPointer dp1 = docPiece.a;
            DocumentPointer dp2 = docPiece.b;

            for (int blockIndex = dp1.getBlockPtr(); blockIndex <= dp2.getBlockPtr(); blockIndex++) {
                Block block = blocks.get(blockIndex);             
                boolean newline;
                boolean previousNewline = false;
                endblock = false;
                List<LayoutToken> tokens = block.getTokens();
                if (tokens == null)
                    continue;
                int n = 0;
                if (blockIndex == dp1.getBlockPtr()) {
                    //n = block.getStartToken();
                    n = dp1.getTokenDocPos() - block.getStartToken();
                }
                while (n < tokens.size()) {
                    if (blockIndex == dp2.getBlockPtr()) {
                        if (n > dp2.getTokenDocPos() - block.getStartToken()) { 
                            break;
                        }
                    }

                    LayoutToken token = tokens.get(n);
                    features = new FeaturesVectorHeader();
                    features.token = token;
                    String text = token.getText();
                    if (text == null) {
                        n++;
                        continue;
                    }
                    //text = text.trim();
					text = text.replace(" ", "").replace("\t", "").replace("\u00A0","");
                    if (text.length() == 0) {
                        n++;
                        continue;
                    }

                    if (text.equals("\n") || text.equals("\r")) {
                        newline = true;
                        previousNewline = true;
                        n++;
                        continue;
                    } else
                        newline = false;

                    if (previousNewline) {
                        newline = true;
                        previousNewline = false;
                    }

					if (TextUtilities.filterLine(text)) {
	                    n++;
	                    continue;
	                }

                    features.string = text;

                    if (newline)
                        features.lineStatus = "LINESTART";
                    Matcher m0 = featureFactory.isPunct.matcher(text);
                    if (m0.find()) {
                        features.punctType = "PUNCT";
                    }
                    if (text.equals("(") || text.equals("[")) {
                        features.punctType = "OPENBRACKET";

                    } else if (text.equals(")") || text.equals("]")) {
                        features.punctType = "ENDBRACKET";

                    } else if (text.equals(".")) {
                        features.punctType = "DOT";

                    } else if (text.equals(",")) {
                        features.punctType = "COMMA";

                    } else if (text.equals("-")) {
                        features.punctType = "HYPHEN";

                    } else if (text.equals("\"") || text.equals("\'") || text.equals("`")) {
                        features.punctType = "QUOTE";

                    }

                    if (n == 0) {
						// beginning of block
                        features.lineStatus = "LINESTART";
                        features.blockStatus = "BLOCKSTART";
                    } else if (n == tokens.size() - 1) {
						// end of block
                        features.lineStatus = "LINEEND";
                        previousNewline = true;
                        features.blockStatus = "BLOCKEND";
                        endblock = true;
                    } else {
                        // look ahead to see if we are at the end of a line within the block
                        boolean endline = false;

                        int ii = 1;
                        boolean endloop = false;
                        while ((n + ii < tokens.size()) && (!endloop)) {
                            LayoutToken tok = tokens.get(n + ii);
                            if (tok != null) {
                                String toto = tok.getText();
                                if (toto != null) {
                                    if (toto.equals("\n") || text.equals("\r")) {
                                        endline = true;
                                        endloop = true;
                                    } else {
                                        if ((toto.trim().length() != 0)
												&& (!text.equals("\u00A0"))
                                                && (!(toto.contains("@IMAGE")))
												&& (!(toto.contains("@PAGE")))
                                                && (!text.contains(".pbm"))
                                                && (!text.contains(".ppm"))
												&& (!text.contains(".png"))
                                                && (!text.contains(".vec"))
                                                && (!text.contains(".jpg"))) {
                                            endloop = true;
                                        }
                                    }
                                }
                            }

                            if (n + ii == tokens.size() - 1) {
                                endblock = true;
                                endline = true;
                            }

                            ii++;
                        }

                        if ((!endline) && !(newline)) {
                            features.lineStatus = "LINEIN";
                        } else if (!newline) {
                            features.lineStatus = "LINEEND";
                            previousNewline = true;
                        }

                        if ((!endblock) && (features.blockStatus == null))
                            features.blockStatus = "BLOCKIN";
                        else if (features.blockStatus == null)
                            features.blockStatus = "BLOCKEND";

                    }

                    if (text.length() == 1) {
                        features.singleChar = true;
                    }

                    if (Character.isUpperCase(text.charAt(0))) {
                        features.capitalisation = "INITCAP";
                    }

                    if (featureFactory.test_all_capital(text)) {
                        features.capitalisation = "ALLCAP";
                    }

                    if (featureFactory.test_digit(text)) {
                        features.digit = "CONTAINSDIGITS";
                    }

                    if (featureFactory.test_common(text)) {
                        features.commonName = true;
                    }

                    if (featureFactory.test_names(text)) {
                        features.properName = true;
                    }

                    if (featureFactory.test_month(text)) {
                        features.month = true;
                    }

                    if (text.contains("-")) {
                        features.containDash = true;
                    }

                    Matcher m = featureFactory.isDigit.matcher(text);
                    if (m.find()) {
                        features.digit = "ALLDIGIT";
                    }

                    Matcher m2 = featureFactory.YEAR.matcher(text);
                    if (m2.find()) {
                        features.year = true;
                    }

                    Matcher m3 = featureFactory.EMAIL.matcher(text);
                    if (m3.find()) {
                        features.email = true;
                    }

                    Matcher m4 = featureFactory.HTTP.matcher(text);
                    if (m4.find()) {
                        features.http = true;
                    }

                    if (currentFont == null) {
                        currentFont = token.getFont();
                        features.fontStatus = "NEWFONT";
                    } else if (!currentFont.equals(token.getFont())) {
                        currentFont = token.getFont();
                        features.fontStatus = "NEWFONT";
                    } else
                        features.fontStatus = "SAMEFONT";

                    int newFontSize = (int) token.getFontSize();
                    if (currentFontSize == -1) {
                        currentFontSize = newFontSize;
                        features.fontSize = "HIGHERFONT";
                    } else if (currentFontSize == newFontSize) {
                        features.fontSize = "SAMEFONTSIZE";
                    } else if (currentFontSize < newFontSize) {
                        features.fontSize = "HIGHERFONT";
                        currentFontSize = newFontSize;
                    } else if (currentFontSize > newFontSize) {
                        features.fontSize = "LOWERFONT";
                        currentFontSize = newFontSize;
                    }

                    if (token.getBold())
                        features.bold = true;

                    if (token.getItalic())
                        features.italic = true;

                    if (token.getRotation())
                        features.rotation = true;

                    // CENTERED
                    // LEFTAJUSTED

                    if (features.capitalisation == null)
                        features.capitalisation = "NOCAPS";

                    if (features.digit == null)
                        features.digit = "NODIGIT";

                    if (features.punctType == null)
                        features.punctType = "NOPUNCT";

                    header.append(features.printVector(withRotation));

                    n++;
                }
            }
        }

        return header.toString();
    }


    /**
     * Process the header of the specified pdf and format the result as training
     * data.
     *
     * @param inputFile  path to input file
     * @param pathHeader path to header
     * @param pathTEI    path to TEI
     */
    public Document createTrainingHeader(String inputFile, String pathHeader, String pathTEI) {
        DocumentSource documentSource = null;
        try {
            File file = new File(inputFile);
            String PDFFileName = file.getName();

            Document doc = parsers.getSegmentationParser().processing(file, GrobidAnalysisConfig.defaultInstance());

            //documentSource = DocumentSource.fromPdf(file);
            //Document doc = new Document(documentSource);

            //doc.addTokenizedDocument();
            /*if (doc.getBlocks() == null) {
                throw new GrobidException("PDF parsing resulted in empty content");
            }*/

            SortedSet<DocumentPiece> documentHeaderParts = doc.getDocumentPart(SegmentationLabel.HEADER);
            List<LayoutToken> tokenizationsFull = doc.getTokenizations();

            if (documentHeaderParts != null) {
                List<LayoutToken> tokenizations = new ArrayList<LayoutToken>();

                for (DocumentPiece docPiece : documentHeaderParts) {
                    DocumentPointer dp1 = docPiece.a;
                    DocumentPointer dp2 = docPiece.b;

                    int tokens = dp1.getTokenDocPos();
                    int tokene = dp2.getTokenDocPos();
                    for (int i = tokens; i < tokene; i++) {
                        tokenizations.add(tokenizationsFull.get(i));
                    }
                }
                String header = getSectionHeaderFeatured(doc, documentHeaderParts, true); 
                String rese = null;
                if ((header != null) && (header.trim().length() > 0)) {                 
                    rese = label(header);
                    //String header = doc.getHeaderFeatured(true, true);
                    //List<LayoutToken> tokenizations = doc.getTokenizationsHeader();

                    // we write the header untagged
                    String outPathHeader = pathHeader + File.separator + PDFFileName.replace(".pdf", ".header");
                    Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathHeader), false), "UTF-8");
                    writer.write(header + "\n");
                    writer.close();

                    // buffer for the header block
                    StringBuilder bufferHeader = trainingExtraction(rese, true, tokenizations);
                    Language lang = languageUtilities.runLanguageId(bufferHeader.toString());
                    if (lang != null) {
                        doc.setLanguage(lang.getLangId());
                    }

                    // buffer for the affiliation+address block
                    StringBuilder bufferAffiliation = 
        				parsers.getAffiliationAddressParser().trainingExtraction(rese, tokenizations);
                    // buffer for the date block
                    StringBuilder bufferDate = null;
                    // we need to rebuild the found date string as it appears
                    String input = "";
                    int q = 0;
                    StringTokenizer st = new StringTokenizer(rese, "\n");
                    while (st.hasMoreTokens() && (q < tokenizations.size())) {
                        String line = st.nextToken();
                        String theTotalTok = tokenizations.get(q).getText();
                        String theTok = tokenizations.get(q).getText();
                        while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n") || theTok.equals("\r")) {
                            q++;
                            if ((q>0) && (q < tokenizations.size())) {
                                theTok = tokenizations.get(q).getText();
                                theTotalTok += theTok;
                            }
                        }
                        if (line.endsWith("<date>")) {
                            input += theTotalTok;
                        }
                        q++;
                    }
                    if (input.trim().length() > 1) {
                        List<String> inputs = new ArrayList<String>();
                        inputs.add(input.trim());
                        bufferDate = parsers.getDateParser().trainingExtraction(inputs);
                    }

                    // buffer for the name block
                    StringBuilder bufferName = null;
                    // we need to rebuild the found author string as it appears
                    input = "";
                    q = 0;
                    st = new StringTokenizer(rese, "\n");
                    while (st.hasMoreTokens() && (q < tokenizations.size())) {
                        String line = st.nextToken();
                        String theTotalTok = tokenizations.get(q).getText();
                        String theTok = tokenizations.get(q).getText();
                        while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n") || theTok.equals("\r")) {
                            q++;
                            if ((q>0) && (q < tokenizations.size())) {
                                theTok = tokenizations.get(q).getText();
                                theTotalTok += theTok;
                            }
                        }
                        if (line.endsWith("<author>")) {
                            input += theTotalTok;
                        }
                        q++;
                    }
                    if (input.length() > 1) {                 
                        List<String> inputs = new ArrayList<String>();
                        inputs.add(input.trim());
                        bufferName = parsers.getAuthorParser().trainingExtraction(inputs, true);
                    }

                    // buffer for the reference block
                    StringBuilder bufferReference = null;
                    // we need to rebuild the found citation string as it appears
                    input = "";
                    q = 0;
                    st = new StringTokenizer(rese, "\n");
                    while (st.hasMoreTokens() && (q < tokenizations.size())) {
                        String line = st.nextToken();
                        String theTotalTok = tokenizations.get(q).getText();
                        String theTok = tokenizations.get(q).getText();
                        while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n") || theTok.equals("\r")) {
                            q++;
                            if ((q>0) && (q < tokenizations.size())) {
                                theTok = tokenizations.get(q).getText();
                                theTotalTok += theTok;
                            }
                        }
                        if (line.endsWith("<reference>")) {
                            input += theTotalTok;
                        }
                        q++;
                    }
                    if (input.length() > 1) {
                        List<String> inputs = new ArrayList<String>();
                        inputs.add(input.trim());
                        bufferReference = parsers.getCitationParser().trainingExtraction(inputs);
                    }

                    // write the TEI file to reflect the extract layout of the text as
                    // extracted from the pdf
                    writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + File.separator
                            + PDFFileName.replace(".pdf", GrobidProperties.FILE_ENDING_TEI_HEADER)), false), "UTF-8");
                    writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" 
        					+ PDFFileName.replace(".pdf", "")
                            + "\"/>\n\t</teiHeader>\n\t<text");

                    if (lang != null) {
                        // TODO: why English (Slava)
                        writer.write(" xml:lang=\"en\"");
                    }
                    writer.write(">\n\t\t<front>\n");

                    writer.write(bufferHeader.toString());
                    writer.write("\n\t\t</front>\n\t</text>\n</tei>\n");
                    writer.close();

                    if (bufferAffiliation != null) {
                        if (bufferAffiliation.length() > 0) {
                            Writer writerAffiliation = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + 
        						File.separator
                                    + PDFFileName.replace(".pdf", ".affiliation.tei.xml")), false), "UTF-8");
                            writerAffiliation.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                            writerAffiliation.write("\n<tei xmlns=\"http://www.tei-c.org/ns/1.0\""
                                    + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" " + "xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">");
                            writerAffiliation.write("\n\t<teiHeader>\n\t\t<fileDesc>\n\t\t\t<sourceDesc>");
                            writerAffiliation.write("\n\t\t\t\t<biblStruct>\n\t\t\t\t\t<analytic>\n\t\t\t\t\t\t<author>\n\n");

                            writerAffiliation.write(bufferAffiliation.toString());

                            writerAffiliation.write("\n\t\t\t\t\t\t</author>\n\t\t\t\t\t</analytic>");
                            writerAffiliation.write("\n\t\t\t\t</biblStruct>\n\t\t\t</sourceDesc>\n\t\t</fileDesc>");
                            writerAffiliation.write("\n\t</teiHeader>\n</tei>\n");
                            writerAffiliation.close();
                        }
                    }

                    if (bufferDate != null) {
                        if (bufferDate.length() > 0) {
                            Writer writerDate = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + 
        						File.separator
                                    + PDFFileName.replace(".pdf", ".date.xml")), false), "UTF-8");
                            writerDate.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                            writerDate.write("<dates>\n");

                            writerDate.write(bufferDate.toString());

                            writerDate.write("</dates>\n");
                            writerDate.close();
                        }
                    }

                    if (bufferName != null) {
                        if (bufferName.length() > 0) {
                            Writer writerName = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + 
        						File.separator
                                    + PDFFileName.replace(".pdf", ".authors.tei.xml")), false), "UTF-8");
                            writerName.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                            writerName.write("\n<tei xmlns=\"http://www.tei-c.org/ns/1.0\"" + " xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
                                    + "xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">");
                            writerName.write("\n\t<teiHeader>\n\t\t<fileDesc>\n\t\t\t<sourceDesc>");
                            writerName.write("\n\t\t\t\t<biblStruct>\n\t\t\t\t\t<analytic>\n\n\t\t\t\t\t\t<author>");
                            writerName.write("\n\t\t\t\t\t\t\t<persName>\n");

                            writerName.write(bufferName.toString());

                            writerName.write("\t\t\t\t\t\t\t</persName>\n");
                            writerName.write("\t\t\t\t\t\t</author>\n\n\t\t\t\t\t</analytic>");
                            writerName.write("\n\t\t\t\t</biblStruct>\n\t\t\t</sourceDesc>\n\t\t</fileDesc>");
                            writerName.write("\n\t</teiHeader>\n</tei>\n");
                            writerName.close();
                        }
                    }

                    if (bufferReference != null) {
                        if (bufferReference.length() > 0) {
                            Writer writerReference = new OutputStreamWriter(new FileOutputStream(new File(pathTEI + 
        						File.separator
                                    + PDFFileName.replace(".pdf", ".header-reference.xml")), false), "UTF-8");
                            writerReference.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                            writerReference.write("<citations>\n");

                            writerReference.write(bufferReference.toString());

                            writerReference.write("</citations>\n");
                            writerReference.close();
                        }
                    }
                }
            }
            else {
                System.out.println("no header found");
            }
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            throw new GrobidException("An exception occurred while running Grobid.", e);
        } finally {
            DocumentSource.close(documentSource, true);
        }
    }

    /**
     * Extract results from a labelled header. If boolean intro is true, the
     * extraction is stopped at the first "intro" tag identified (this tag marks
     * the begining of the description).
     *
     * @param result        result
     * @param intro         if intro
     * @param tokenizations list of tokens
     * @param biblio        biblio item
     * @return a biblio item
     */
    public BiblioItem resultExtraction(String result, boolean intro, List<LayoutToken> tokenizations, BiblioItem biblio) {
        StringTokenizer st = new StringTokenizer(result, "\n");
        String s1 = null;
        String s2 = null;
        String lastTag = null;

        int p = 0;

        while (st.hasMoreTokens()) {
            boolean addSpace = false;
            String tok = st.nextToken().trim();

            if (tok.length() == 0) {
                continue;
            }
            StringTokenizer stt = new StringTokenizer(tok, "\t");
            List<String> localFeatures = new ArrayList<String>();
            int i = 0;

            // boolean newLine = false;
            int ll = stt.countTokens();
            while (stt.hasMoreTokens()) {
                String s = stt.nextToken().trim();
                if (i == 0) {
                    //s2 = TextUtilities.HTMLEncode(s);
                    s2 = s;
                    int p0 = p;
                    boolean strop = false;
                    while ((!strop) && (p < tokenizations.size())) {
                        String tokOriginal = tokenizations.get(p).getText();
                        if (tokOriginal.equals(" ")) {
                            addSpace = true;
                        } else if (tokOriginal.equals(s)) {
                            strop = true;
                        }
                        p++;
                    }
                    if (p == tokenizations.size()) {
                        // either we are at the end of the header, or we might have
                        // a problematic token in tokenization for some reasons
                        if ((p - p0) > 2) {
                            // we loose the synchronicity, so we reinit p for the next token
                            p = p0;
                        }
                    }
                } else if (i == ll - 1) {
                    s1 = s;
                } else {
                    // if (s.equals("LINESTART"))
                    // newLine = true;
                    localFeatures.add(s);
                }
                i++;
            }

            if ((s1.equals("<title>")) || (s1.equals("I-<title>"))) {
                if (biblio.getTitle() != null) {
                    if (localFeatures.contains("LINESTART")) {
                        biblio.setTitle(biblio.getTitle() + " " + s2);
                    } else if (addSpace) {
                        biblio.setTitle(biblio.getTitle() + " " + s2);
                    } else
                        biblio.setTitle(biblio.getTitle() + s2);
                } else
                    biblio.setTitle(s2);
            } else if ((s1.equals("<author>")) || (s1.equals("I-<author>"))) {
                if ((lastTag == null) || ((lastTag != null) && (lastTag.endsWith("<author>")))) {
                    if (biblio.getAuthors() != null) {
                        if (addSpace) {
                            biblio.setAuthors(biblio.getAuthors() + " " + s2);
                        } else
                            biblio.setAuthors(biblio.getAuthors() + s2);
                    } else
                        biblio.setAuthors(s2);
                } else {
                    if (biblio.getAuthors() != null) {
                        if (addSpace) {
                            biblio.setAuthors(biblio.getAuthors() + " \n" + s2);
                        } else
                            biblio.setAuthors(biblio.getAuthors() + "\n" + s2);
                    } else
                        biblio.setAuthors(s2);
                }
            } else if ((s1.equals("<tech>")) || (s1.equals("I-<tech>"))) {
                biblio.setItem(BiblioItem.TechReport);
                if (biblio.getBookType() != null) {
                    if (addSpace) {
                        biblio.setBookType(biblio.getBookType() + " " + s2);
                    } else
                        biblio.setBookType(biblio.getBookType() + s2);
                } else
                    biblio.setBookType(s2);
            } else if ((s1.equals("<location>")) || (s1.equals("I-<location>"))) {
                if (biblio.getLocation() != null) {
                    if (addSpace)
                        biblio.setLocation(biblio.getLocation() + " " + s2);
                    else
                        biblio.setLocation(biblio.getLocation() + s2);
                } else
                    biblio.setLocation(s2);
            } else if ((s1.equals("<date>")) || (s1.equals("I-<date>"))) {
                // it appears that the same date is quite often repeated,
                // we should check, before adding a new date segment, if it is
                // not already present

                if (biblio.getPublicationDate() != null) {
                    if (addSpace) {
                        biblio.setPublicationDate(biblio.getPublicationDate() + " " + s2);
                    } else
                        biblio.setPublicationDate(biblio.getPublicationDate() + s2);
                } else
                    biblio.setPublicationDate(s2);
            } else if ((s1.equals("<date-submission>")) || (s1.equals("I-<date-submission>"))) {
                // it appears that the same date is quite often repeated,
                // we should check, before adding a new date segment, if it is
                // not already present

                if (biblio.getSubmissionDate() != null) {
                    if (addSpace) {
                        biblio.setSubmissionDate(biblio.getSubmissionDate() + " " + s2);
                    } else
                        biblio.setSubmissionDate(biblio.getSubmissionDate() + s2);
                } else
                    biblio.setSubmissionDate(s2);
            } else if ((s1.equals("<pages>")) || (s1.equals("<page>")) | (s1.equals("I-<pages>")) || (s1.equals("I-<page>"))) {
                if (biblio.getPageRange() != null) {
                    if (addSpace) {
                        biblio.setPageRange(biblio.getPageRange() + " " + s2);
                    } else
                        biblio.setPageRange(biblio.getPageRange() + s2);
                } else
                    biblio.setPageRange(s2);
            } else if ((s1.equals("<editor>")) || (s1.equals("I-<editor>"))) {
                if (biblio.getEditors() != null) {
                    if (addSpace) {
                        biblio.setEditors(biblio.getEditors() + " " + s2);
                    } else {
                        biblio.setEditors(biblio.getEditors() + s2);
                    }
                } else
                    biblio.setEditors(s2);
            } else if ((s1.equals("<institution>")) || (s1.equals("I-<institution>"))) {
                if (biblio.getInstitution() != null) {
                    if (addSpace) {
                        biblio.setInstitution(biblio.getInstitution() + "; " + s2);
                    } else
                        biblio.setInstitution(biblio.getInstitution() + s2);
                } else
                    biblio.setInstitution(s2);
            } else if ((s1.equals("<note>")) || (s1.equals("I-<note>"))) {
                if (biblio.getNote() != null) {
                    if (addSpace) {
                        biblio.setNote(biblio.getNote() + " " + s2);
                    } else
                        biblio.setNote(biblio.getNote() + s2);
                } else
                    biblio.setNote(s2);
            } else if ((s1.equals("<abstract>")) || (s1.equals("I-<abstract>"))) {
                if (biblio.getAbstract() != null) {
                    if (addSpace) {
                        biblio.setAbstract(biblio.getAbstract() + " " + s2);
                    } else
                        biblio.setAbstract(biblio.getAbstract() + s2);
                } else
                    biblio.setAbstract(s2);
            } else if ((s1.equals("<reference>")) || (s1.equals("I-<reference>"))) {
                if (biblio.getReference() != null) {
                    if (addSpace) {
                        biblio.setReference(biblio.getReference() + " " + s2);
                    } else
                        biblio.setReference(biblio.getReference() + s2);
                } else
                    biblio.setReference(s2);
            } else if ((s1.equals("<grant>")) || (s1.equals("I-<grant>"))) {
                if (biblio.getGrant() != null) {
                    if (addSpace) {
                        biblio.setGrant(biblio.getGrant() + " " + s2);
                    } else
                        biblio.setGrant(biblio.getGrant() + s2);
                } else
                    biblio.setGrant(s2);
            } else if ((s1.equals("<copyright>")) || (s1.equals("I-<copyright>"))) {
                if (biblio.getCopyright() != null) {
                    if (addSpace) {
                        biblio.setCopyright(biblio.getCopyright() + " " + s2);
                    } else
                        biblio.setCopyright(biblio.getCopyright() + s2);
                } else
                    biblio.setCopyright(s2);
            } else if ((s1.equals("<affiliation>")) || (s1.equals("I-<affiliation>"))) {
                // affiliation **makers** should be marked SINGLECHAR LINESTART
                if (biblio.getAffiliation() != null) {
                    if ((lastTag != null) && (s1.equals(lastTag) || lastTag.equals("I-<affiliation>"))) {
                        if (s1.equals("I-<affiliation>")) {
                            biblio.setAffiliation(biblio.getAffiliation() + " ; " + s2);
                        } else if (addSpace) {
                            biblio.setAffiliation(biblio.getAffiliation() + " " + s2);
                        } else
                            biblio.setAffiliation(biblio.getAffiliation() + s2);
                    } else
                        biblio.setAffiliation(biblio.getAffiliation() + " ; " + s2);
                } else
                    biblio.setAffiliation(s2);
            } else if ((s1.equals("<address>")) || (s1.equals("I-<address>"))) {
                if (biblio.getAddress() != null) {
                    if (addSpace) {
                        biblio.setAddress(biblio.getAddress() + " " + s2);
                    } else
                        biblio.setAddress(biblio.getAddress() + s2);
                } else
                    biblio.setAddress(s2);
            } else if ((s1.equals("<email>")) || (s1.equals("I-<email>"))) {
                if (biblio.getEmail() != null) {
                    if (s1.equals("I-<email>"))
                        biblio.setEmail(biblio.getEmail() + " ; " + s2);
                    else if (addSpace)
                        biblio.setEmail(biblio.getEmail() + " " + s2);
                    else
                        biblio.setEmail(biblio.getEmail() + s2);
                } else
                    biblio.setEmail(s2);
            } else if ((s1.equals("<pubnum>")) || (s1.equals("I-<pubnum>"))) {
                if (biblio.getPubnum() != null) {
                    if (addSpace)
                        biblio.setPubnum(biblio.getPubnum() + " " + s2);
                    else
                        biblio.setPubnum(biblio.getPubnum() + s2);
                } else
                    biblio.setPubnum(s2);
            } else if ((s1.equals("<keyword>")) || (s1.equals("I-<keyword>"))) {
                if (biblio.getKeyword() != null) {
                    if (localFeatures.contains("LINESTART")) {
                        biblio.setKeyword(biblio.getKeyword() + " \n " + s2);
                    } else if (addSpace)
                        biblio.setKeyword(biblio.getKeyword() + " " + s2);
                    else
                        biblio.setKeyword(biblio.getKeyword() + s2);
                } else
                    biblio.setKeyword(s2);
            } else if ((s1.equals("<phone>")) || (s1.equals("I-<phone>"))) {
                if (biblio.getPhone() != null) {
                    if (addSpace)
                        biblio.setPhone(biblio.getPhone() + " " + s2);
                    else
                        biblio.setPhone(biblio.getPhone() + s2);
                } else
                    biblio.setPhone(s2);
            } else if ((s1.equals("<degree>")) || (s1.equals("I-<degree>"))) {
                if (biblio.getDegree() != null) {
                    if (addSpace)
                        biblio.setDegree(biblio.getDegree() + " " + s2);
                    else
                        biblio.setDegree(biblio.getDegree() + s2);
                } else
                    biblio.setDegree(s2);
            } else if ((s1.equals("<web>")) || (s1.equals("I-<web>"))) {
                if (biblio.getWeb() != null) {
                    if (addSpace)
                        biblio.setWeb(biblio.getWeb() + " " + s2);
                    else
                        biblio.setWeb(biblio.getWeb() + s2);
                } else
                    biblio.setWeb(s2);
            } else if ((s1.equals("<dedication>")) || (s1.equals("I-<dedication>"))) {
                if (biblio.getDedication() != null) {
                    if (addSpace)
                        biblio.setDedication(biblio.getDedication() + " " + s2);
                    else
                        biblio.setDedication(biblio.getDedication() + s2);
                } else
                    biblio.setDedication(s2);
            } else if ((s1.equals("<submission>")) || (s1.equals("I-<submission>"))) {
                if (biblio.getSubmission() != null) {
                    if (addSpace)
                        biblio.setSubmission(biblio.getSubmission() + " " + s2);
                    else
                        biblio.setSubmission(biblio.getSubmission() + s2);
                } else
                    biblio.setSubmission(s2);
            } else if ((s1.equals("<entitle>")) || (s1.equals("I-<entitle>"))) {
                if (biblio.getEnglishTitle() != null) {
                    if (s1.equals(lastTag)) {
                        if (localFeatures.contains("LINESTART")) {
                            biblio.setEnglishTitle(biblio.getEnglishTitle() + " " + s2);
                        } else if (addSpace)
                            biblio.setEnglishTitle(biblio.getEnglishTitle() + " " + s2);
                        else
                            biblio.setEnglishTitle(biblio.getEnglishTitle() + s2);
                    } else
                        biblio.setEnglishTitle(biblio.getEnglishTitle() + " ; " + s2);
                } else
                    biblio.setEnglishTitle(s2);
            } else if (((s1.equals("<intro>")) || (s1.equals("I-<intro>"))) && intro) {
                return biblio;
            }
            lastTag = s1;
        }

        return biblio;
    }

    /**
     * Extract results from a labelled header in the training format without any
     * string modification.
     *
     * @param result        result
     * @param intro         if intro
     * @param tokenizations list of tokens
     * @return a result
     */
    private StringBuilder trainingExtraction(String result, boolean intro, List<LayoutToken> tokenizations) {
        // this is the main buffer for the whole header
        StringBuilder buffer = new StringBuilder();

        StringTokenizer st = new StringTokenizer(result, "\n");
        String s1 = null;
        String s2 = null;
        String lastTag = null;

        int p = 0;

        while (st.hasMoreTokens()) {
            boolean addSpace = false;
            String tok = st.nextToken().trim();

            if (tok.length() == 0) {
                continue;
            }
            StringTokenizer stt = new StringTokenizer(tok, "\t");
            // List<String> localFeatures = new ArrayList<String>();
            int i = 0;

            boolean newLine = false;
            int ll = stt.countTokens();
            while (stt.hasMoreTokens()) {
                String s = stt.nextToken().trim();
                if (i == 0) {
                    s2 = TextUtilities.HTMLEncode(s);
                    //s2 = s;

                    boolean strop = false;
                    while ((!strop) && (p < tokenizations.size())) {
                        String tokOriginal = tokenizations.get(p).t();
                        if (tokOriginal.equals(" ")
                                || tokOriginal.equals("\u00A0")) {
                            addSpace = true;
                        } else if (tokOriginal.equals(s)) {
                            strop = true;
                        }
                        p++;
                    }
                } else if (i == ll - 1) {
                    s1 = s;
                } else {
                    if (s.equals("LINESTART"))
                        newLine = true;
                    // localFeatures.add(s);
                }
                i++;
            }

            if (newLine) {
                buffer.append("<lb/>");
            }

            String lastTag0 = null;
            if (lastTag != null) {
                if (lastTag.startsWith("I-")) {
                    lastTag0 = lastTag.substring(2, lastTag.length());
                } else {
                    lastTag0 = lastTag;
                }
            }
            String currentTag0 = null;
            if (s1 != null) {
                if (s1.startsWith("I-")) {
                    currentTag0 = s1.substring(2, s1.length());
                } else {
                    currentTag0 = s1;
                }
            }

            if (lastTag != null) {
                testClosingTag(buffer, currentTag0, lastTag0);
            }

            boolean output;

            output = writeField(buffer, s1, lastTag0, s2, "<title>", "<docTitle>\n\t<titlePart>", addSpace);
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<author>", "<byline>\n\t<docAuthor>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<location>", "<address>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<address>", "<address>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<date>", "<date>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<date-submission>", "<date type=\"submission\">", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<booktitle>", "<booktitle>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<pages>", "<pages>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<publisher>", "<publisher>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<journal>", "<journal>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<institution>", "<byline>\n\t<affiliation>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<affiliation>", "<byline>\n\t<affiliation>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<volume>", "<volume>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<editor>", "<editor>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<note>", "<note type=\"other\">", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<abstract>", "<div type=\"abstract\">", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<email>", "<email>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<pubnum>", "<idno>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<keyword>", "<keyword>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<phone>", "<phone>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<degree>", "<note type=\"degree\">", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<web>", "<ptr type=\"web\">", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<dedication>", "<dedication>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<submission>", "<note type=\"submission\">", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<entitle>", "<note type=\"title\">", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<reference>", "<reference>", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<copyright>", "<note type=\"copyright\">", addSpace);
            }
            if (!output) {
                // noinspection UnusedAssignment
                output = writeField(buffer, s1, lastTag0, s2, "<grant>", "<note type=\"grant\">", addSpace);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<intro>", "<p type=\"introduction\">", addSpace);
            }

            /*if (((s1.equals("<intro>")) || (s1.equals("I-<intro>"))) && intro) {
                break;
            }*/
            lastTag = s1;

            if (!st.hasMoreTokens()) {
                if (lastTag != null) {
                    testClosingTag(buffer, "", currentTag0);
                }
            }
        }

        return buffer;
    }

    private void testClosingTag(StringBuilder buffer, String currentTag0, String lastTag0) {
        if (!currentTag0.equals(lastTag0)) {
            // we close the current tag
            if (lastTag0.equals("<title>")) {
                buffer.append("</titlePart>\n\t</docTitle>\n");
            } else if (lastTag0.equals("<author>")) {
                buffer.append("</docAuthor>\n\t</byline>\n");
            } else if (lastTag0.equals("<location>")) {
                buffer.append("</address>\n");
            } else if (lastTag0.equals("<date>")) {
                buffer.append("</date>\n");
            } else if (lastTag0.equals("<abstract>")) {
                buffer.append("</div>\n");
            } else if (lastTag0.equals("<address>")) {
                buffer.append("</address>\n");
            } else if (lastTag0.equals("<date-submission>")) {
                buffer.append("</date>\n");
            } else if (lastTag0.equals("<booktitle>")) {
                buffer.append("</booktitle>\n");
            } else if (lastTag0.equals("<pages>")) {
                buffer.append("</pages>\n");
            } else if (lastTag0.equals("<email>")) {
                buffer.append("</email>\n");
            } else if (lastTag0.equals("<publisher>")) {
                buffer.append("</publisher>\n");
            } else if (lastTag0.equals("<institution>")) {
                buffer.append("</affiliation>\n\t</byline>\n");
            } else if (lastTag0.equals("<keyword>")) {
                buffer.append("</keyword>\n");
            } else if (lastTag0.equals("<affiliation>")) {
                buffer.append("</affiliation>\n\t</byline>\n");
            } else if (lastTag0.equals("<note>")) {
                buffer.append("</note>\n");
            } else if (lastTag0.equals("<reference>")) {
                buffer.append("</reference>\n");
            } else if (lastTag0.equals("<copyright>")) {
                buffer.append("</note>\n");
            } else if (lastTag0.equals("<grant>")) {
                buffer.append("</note>\n");
            } else if (lastTag0.equals("<entitle>")) {
                buffer.append("</note>\n");
            } else if (lastTag0.equals("<submission>")) {
                buffer.append("</note>\n");
            } else if (lastTag0.equals("<dedication>")) {
                buffer.append("</dedication>\n");
            } else if (lastTag0.equals("<web>")) {
                buffer.append("</ptr>\n");
            } else if (lastTag0.equals("<phone>")) {
                buffer.append("</phone>\n");
            } else if (lastTag0.equals("<pubnum>")) {
                buffer.append("</idno>\n");
            } else if (lastTag0.equals("<degree>")) {
                buffer.append("</note>\n");
            } else if (lastTag0.equals("<intro>")) {
                buffer.append("</p>\n");
            }
        }
    }

    private boolean writeField(StringBuilder buffer, String s1, String lastTag0, String s2, String field, String outField, boolean addSpace) {
        boolean result = false;
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
            if (s1.equals(lastTag0) || (s1).equals("I-" + lastTag0)) {
                if (addSpace)
                    buffer.append(" ").append(s2);
                else
                    buffer.append(s2);
            } else
                buffer.append("\n\t").append(outField).append(s2);
        }
        return result;
    }

    /**
     * Consolidate an existing list of recognized citations based on access to
     * external internet bibliographic databases.
     *
     * @param resHeader original biblio item
     * @return consolidated biblio item
     */
    public BiblioItem consolidateHeader(BiblioItem resHeader) {
        try {
            if (consolidator == null) {
                consolidator = new Consolidation();
            }
            consolidator.openDb();
            List<BiblioItem> bibis = new ArrayList<BiblioItem>();
            boolean valid = consolidator.consolidate(resHeader, bibis);
            if ((valid) && (bibis.size() > 0)) {
                BiblioItem bibo = bibis.get(0);
                if (bibo != null) {
                    BiblioItem.correct(resHeader, bibo);
                }
            }
            consolidator.closeDb();
        } catch (Exception e) {
            // e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return resHeader;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
