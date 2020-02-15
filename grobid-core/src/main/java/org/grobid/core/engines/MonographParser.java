package org.grobid.core.engines;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.Monograph;
import org.grobid.core.data.MonographItem;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentNode;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.document.TEIMonographFormatter;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.engines.tagging.TaggerFactory;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorMonograph;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.*;
import org.grobid.core.lexicon.FastMatcher;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import static org.grobid.core.engines.label.TaggingLabels.*;

/**
 * Realise a high level segmentation of a monograph. Monograph is to be understood here in the context library cataloging,
 * basically as a standalone book. The monograph could be an ebook (novels), a conference proceedings volume, a book
 * collection volume, a phd/msc thesis, a standalone report (with toc, etc.), a manual (with multiple chapters).
 * Monographs, here, are NOT magazine volumes, journal issues, newspapers, standalone chapters, standalone scholar articles,
 * tables of content, reference works, dictionaries, encyclopedia volumes, graphic novels.
 *
 * @author Patrice Lopez
 */
public class MonographParser {
    /**
     * 17 labels for this model:
     * cover page (front of the book)
     * title page (secondary title page)
     * publisher page (publication information, including usually the copyrights info)
     * summary (include executive summary)
     * biography
     * advertising (other works by the author/publisher)
     * table of content
     * table/list of figures
     * preface (foreword)
     * dedication (I dedicate this label to my family and my thesis director ;)
     * unit (chapter or standalone article)
     * reference (a full chapter of references, not to be confused with references attached to an article)
     * annex
     * index
     * glossary (also abbreviations and acronyms)
     * back cover page
     * other
     */
    private final GenericTagger monographParser;

    private static final Logger LOGGER = LoggerFactory.getLogger(MonographParser.class);

    private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();

    private EngineParsers parsers;

    // default bins for relative position
    private static final int NBBINS_POSITION = 12;

    // default bins for inter-block spacing
    private static final int NBBINS_SPACE = 5;

    // default bins for block character density
    private static final int NBBINS_DENSITY = 5;

    // projection scale for line length
    private static final int LINESCALE = 10;

    // projection scale for block length
    private static final int BLOCKSCALE = 10;

    private FeatureFactory featureFactory = FeatureFactory.getInstance();

    private File tmpPath = null;

    public MonographParser() {
        monographParser = TaggerFactory.getTagger(GrobidModels.MONOGRAPH);
    }

    /**
     * Processing with application of the monograph model
     */
    //public Pair<String, Document> processing(File input) throws Exception{
    public String processing(File input) throws Exception {
        DocumentSource documentSource = null;
        String lang = null;
        try {
            if (!input.exists()) {
                throw new GrobidResourceException("Cannot process the monograph model, because the file '" +
                    input.getAbsolutePath() + "' does not exists.");
            }

            documentSource = DocumentSource.fromPdf(input, -1, -1, false, true, true);
            Document doc = new Document(documentSource);
            GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance(); // or "GrobidAnalysisConfig.builder().build()"
            doc.addTokenizedDocument(config);
            List<Block> blocks = doc.getBlocks();
            Language langID = new Language();

            if (blocks == null) {
                throw new Exception("PDF parsing resulted in empty content");
            } else {
                // detect the language
                String contentSample = "";
                int sampleLength = 0;
                for (int i = 0; i < blocks.size(); i++) {
                    contentSample += doc.getBlocks().get(i).getText();
                    if (sampleLength > 500) // it's assumed we need 500 characters of sample content for detecting the language
                        break;
                }

                langID = languageUtilities.getInstance().runLanguageId(contentSample);
                if (langID != null) {
                    lang = langID.getLang();
                } else {
                    lang = "en"; // by default, id = english
                }
            }

            doc.setLanguage(lang);
            doc.produceStatistics();

            String tei = processingMonographBlock(doc, config);
            //return new ImmutablePair<String, Document>(tei, doc);
            return tei;
        } finally {
            if (documentSource != null) {
                documentSource.close(true, true, true);
            }
        }
    }

    /**
     * Document processing after application of the monograph model
     */
    public String processingMonographBlock(Document doc, GrobidAnalysisConfig config) {
        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }

        //guaranteeing quality of service. Otherwise, there are some PDF that may contain 300k blocks and thousands of extracted "images" that ruins the performance
        if (blocks.size() > GrobidProperties.getPdfBlocksMax()) {
            throw new GrobidException("Postprocessed document is too big, contains: " + blocks.size(), GrobidExceptionStatus.TOO_MANY_BLOCKS);
        }

        TEIMonographFormatter teiFormatter = null;
        ArrayList<MonographItem> monographItems = null;
        MonographItem resultMonograph = null;
        StringBuilder result = new StringBuilder();

        try {
            GenericTagger tagger = monographParser;
            // list of textual patterns at the head and foot of pages which can be re-occur on several pages
            // (typically indicating a publisher foot or head notes)
            Map<String, Integer> patterns = new TreeMap<String, Integer>();
            Map<String, Boolean> firstTimePattern = new TreeMap<String, Boolean>();

            boolean newPage = false;
            String currentFont = null, currentFontString = null, currentFontSizeString = null;
            int currentFontSize = -1;
            int documentLength = doc.getDocumentLenghtChar();

            int mm = 0; // page position
            int nn = 0; // document position
            int pageLength = 0; // length of the current page
            double pageHeight = 0.0;
            int relativePagePositionChar = 0;

            teiFormatter = new TEIMonographFormatter();

            // add TEI header information (not much information here)
            result.append(teiFormatter.toTEIHeaderMonograph(doc, config));

            // add TEI body with the extraction result from the monograph model
            if (doc.getLanguage() != null) {
                result.append("\t<text xml:lang=\"").append(doc.getLanguage()).append("\">\n");
            } else {
                result.append("\t<text>\n");
            }

            for (Page page : doc.getPages()) {
                pageHeight = page.getHeight();
                newPage = true;
                double spacingPreviousBlock = 0.0; // discretized
                int spacingWithPreviousBlock = 0;
                double lowestPos = 0.0;
                pageLength = page.getPageLengthChar();
                BoundingBox pageBoundingBox = page.getMainArea();
                mm = 0;

                if ((page.getBlocks() == null) || (page.getBlocks().size() == 0))
                    continue;

                boolean lastPageBlock = false;
                boolean firstPageBlock = false;
                int relativeDocumentPosition;
                int characterDensity = 0;

                // put the result in monograph item as list
                monographItems = new ArrayList<>();
                String resultWithLabel = null;

                for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                    Monograph monograph = new Monograph();
                    String label = null;
                    resultMonograph = new MonographItem();
                    Block block = page.getBlocks().get(blockIndex);
                    List<LayoutToken> tokenization = blocks.get(blockIndex).getTokens();

                    String localText = block.getText();
                    if (localText.length() == 0 || localText == null) {
                        continue;
                    }

                    if ((localText != null) && (localText.length() > 0)) {
                        String[] lines = localText.split("[\\n\\r]");
                        if (lines.length > 0) {
                            String line = lines[0];
                            String pattern = featureFactory.getPattern(line);
                            if (pattern.length() > 8) {
                                Integer nb = patterns.get(pattern);
                                if (nb == null) {
                                    patterns.put(pattern, Integer.valueOf("1"));
                                    firstTimePattern.put(pattern, false);
                                } else
                                    patterns.put(pattern, Integer.valueOf(nb + 1));
                            }
                        }
                    }

                    if (lowestPos > block.getY()) {
                        // we have a vertical shift, which can be due to a change of column or other particular layout formatting
                        spacingPreviousBlock = doc.getMaxBlockSpacing() / 5.0; // default
                    } else
                        spacingPreviousBlock = block.getY() - lowestPos;

                    // space with previous block
                    if (spacingPreviousBlock != 0.0) {
                        spacingWithPreviousBlock = featureFactory
                            .linearScaling(spacingPreviousBlock - doc.getMinBlockSpacing(), doc.getMaxBlockSpacing() - doc.getMinBlockSpacing(), NBBINS_SPACE);
                    }

                    // check whether the first or last block of each page
                    if (blockIndex == blocks.size() - 1) {
                        lastPageBlock = true;
                    }

                    if (blockIndex == 0) {
                        firstPageBlock = true;
                    }

                    // character density of the block
                    double density = 0.0;
                    if ((block.getHeight() != 0.0) && (block.getWidth() != 0.0) &&
                        (block.getText() != null) && (!block.getText().contains("@PAGE")) &&
                        (!block.getText().contains("@IMAGE")))
                        density = (double) block.getText().length() / (block.getHeight() * block.getWidth());

                    // character density of the previous block
                    if (density != -1.0) {
                        characterDensity = featureFactory
                            .linearScaling(density - doc.getMinCharacterDensity(), doc.getMaxCharacterDensity() - doc.getMinCharacterDensity(), NBBINS_DENSITY);
                    }

                    // is the current block in the main area of the page or not?
                    boolean inPageMainArea = true;
                    BoundingBox blockBoundingBox = BoundingBox.fromPointAndDimensions(page.getNumber(),
                        block.getX(), block.getY(), block.getWidth(), block.getHeight());
                    if (pageBoundingBox == null || (!pageBoundingBox.contains(blockBoundingBox) && !pageBoundingBox.intersect(blockBoundingBox)))
                        inPageMainArea = false;

                    List<LayoutToken> tokens = block.getTokens();
                    if ((tokens == null) || (tokens.size() == 0)) {
                        continue;
                    }

                    // for the layout information of the block, we take simply the first layout token
                    LayoutToken token = null;
                    if (tokens.size() > 0)
                        token = tokens.get(0);

                    double coordinateLineY = token.getY();

                    // font information
                    if (currentFont == null) {
                        currentFont = token.getFont();
                        currentFontString = "NEWFONT";
                    } else if (!currentFont.equals(token.getFont())) {
                        currentFont = token.getFont();
                        currentFontString = "NEWFONT";
                    } else
                        currentFontString = "SAMEFONT";

                    // font size information
                    int newFontSize = (int) token.getFontSize();
                    if (currentFontSize == -1) {
                        currentFontSize = newFontSize;
                        currentFontSizeString = "HIGHERFONT";
                    } else if (currentFontSize == newFontSize) {
                        currentFontSizeString = "SAMEFONTSIZE";
                    } else if (currentFontSize < newFontSize) {
                        currentFontSizeString = "HIGHERFONT";
                        currentFontSize = newFontSize;
                    } else if (currentFontSize > newFontSize) {
                        currentFontSizeString = "LOWERFONT";
                        currentFontSize = newFontSize;
                    }

                    // relative document position
                    relativeDocumentPosition = featureFactory.linearScaling(nn, documentLength, NBBINS_POSITION);

                    int pagePos = featureFactory.linearScaling(coordinateLineY, pageHeight, NBBINS_POSITION);
                    if (pagePos > NBBINS_POSITION)
                        pagePos = NBBINS_POSITION;

                    // relative page position characters
                    relativePagePositionChar = featureFactory.linearScaling(mm, pageLength, NBBINS_POSITION);

                    // add features
                    String featuredBlockMonograph = getBlockMonographFeatured(doc, blocks.get(blockIndex), tokenization,
                        firstPageBlock, lastPageBlock, newPage, currentFontString, currentFontSizeString, relativeDocumentPosition,
                        pagePos, relativePagePositionChar, inPageMainArea, spacingWithPreviousBlock, characterDensity);

                    // lowest position of the block
                    lowestPos = block.getY() + block.getHeight();

                    // update page-level and document-level positions
                    if (tokens != null) {
                        mm += tokens.size();
                        nn += tokens.size();
                    }

                    // labeling the featured monograph by blocks
                    if ((featuredBlockMonograph != null) && (featuredBlockMonograph.trim().length() > 0)) {
                        resultWithLabel = tagger.label(featuredBlockMonograph);
                        // TaggingTokenClusteror gives error if there is no label found, it should be one of labels defined
                        //label = resultExtraction(resultWithLabel, tokens);
                        label = monograph.getLabel(resultWithLabel);
                    }
                    // blocks are separated by two carriage returns, they are from the text for the better view in TEI
                    localText = localText.replaceAll("\n", "");
                    resultMonograph.setText(localText);
                    resultMonograph.setTokens(tokens);
                    resultMonograph.setBoundingBox(blockBoundingBox);
                    //label.replaceAll("<", "").replaceAll(">", "");
                    resultMonograph.setLabel(label);
                    monographItems.add(resultMonograph);

                    result.append(teiFormatter.toTEIMonographPerItem(doc, resultMonograph));
                }
                newPage = false;
            }

            result.append("\t</text>\n");
            result.append("</TEI>\n");

            return result.toString();

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
    }

    /**
     * Return the monograph block with features to be processed by the CRF model
     */
    public String getBlockMonographFeatured(Document doc, Block block, List<LayoutToken> tokens, boolean firstBlock,
                                            boolean lastBlock, boolean newPage, String currentFontString, String currentFontSizeString,
                                            int relativeDocumentPosition, int pagePos, int relativePagePositionChar, boolean inPageMainArea,
                                            int spacingWithPreviousBlock, int characterDensity) {
        StringBuilder monographFeatured = new StringBuilder();
        FeatureFactory featureFactory = FeatureFactory.getInstance();
        String str = null;
        boolean graphicVector = false;
        boolean graphicBitmap = false;

        // vector for features
        FeaturesVectorMonograph features = new FeaturesVectorMonograph();
        FeaturesVectorMonograph previousFeatures = new FeaturesVectorMonograph();

        // list of textual patterns at the head and foot of pages which can be re-occur on several pages
        // (typically indicating a publisher foot or head notes)
        Map<String, Integer> patterns = new TreeMap<String, Integer>();
        Map<String, Boolean> firstTimePattern = new TreeMap<String, Boolean>();

        String localText = block.getText();
        if ((localText != null) && (localText.length() > 0)) {
            String pattern = featureFactory.getPattern(localText);
            if (pattern.length() > 8) {
                Integer nb = patterns.get(pattern);
                if (nb == null) {
                    patterns.put(pattern, Integer.valueOf("1"));
                    firstTimePattern.put(pattern, false);
                } else
                    patterns.put(pattern, Integer.valueOf(nb + 1));
            }
        }

        /*********add features*********/

        // check if we have a graphical object connected to the current block
        List<GraphicObject> localImages = Document.getConnectedGraphics(block, doc);
        if (localImages != null) {
            for (GraphicObject localImage : localImages) {
                if (localImage.getType() == GraphicObjectType.BITMAP)
                    graphicBitmap = true;
                if (localImage.getType() == GraphicObjectType.VECTOR)
                    graphicVector = true;
            }
        }

        String[] lines = localText.split("[\\n\\r]");
        int linesSize = lines.length;
        // set the max length of the lines in the block, in number of characters
        int maxLineLength = 0, idxMaxLength = 0;
        for (int p = 0; p < linesSize; p++) {
            if (lines[p].length() > maxLineLength) {
                maxLineLength = lines[p].length();
                idxMaxLength = p;
            }
        }

        // for the layout information of the block, we take simply the first layout token
        LayoutToken token = null;
        if (tokens.size() > 0)
            token = tokens.get(0);

        double coordinateLineY = token.getY();

        // not a feature, the complete processed block
        features.block = localText;

        // pattern repeated on several pages
        String pattern = featureFactory.getPattern(localText);
        Integer nb = patterns.get(pattern);
        if ((nb != null) && (nb > 1)) {
            features.repetitivePattern = true;

            Boolean firstTimeDone = firstTimePattern.get(pattern);
            if ((firstTimeDone != null) && !firstTimeDone) {
                features.firstRepetitivePattern = true;
                firstTimePattern.put(pattern, true);
            }
        }

        // the first string is the first token of the first line of the block
        str = lines[0];
        if (str.length() > 0 || str != null) {
            String[] splitStr = StringUtils.split(str);
            features.string = splitStr[0];
        } else {
            features.string = null;
        }

        // the second string is the first token of the longest line of the block
        str = lines[idxMaxLength];
        if (str.length() > 0) {
            String[] splitStr = StringUtils.split(str);
            features.secondString = splitStr[0];
        } else {
            features.secondString = null;
        }

        // the third string is the first token of the last line of the block
        str = lines[linesSize - 1];
        if (str.length() > 0) {
            String[] splitStr = StringUtils.split(str);
            features.thirdString = splitStr[0];
        } else {
            features.thirdString = null;
        }

        features.firstPageBlock = firstBlock;
        features.lastPageBlock = lastBlock;

        // HERE horizontal information
        // CENTERED
        // LEFTAJUSTED
        // CENTERED

        // page information
        if (newPage) {
            features.pageStatus = "PAGESTART";
            newPage = false;
            if (previousFeatures != null)
                previousFeatures.pageStatus = "PAGEEND";
        } else {
            features.pageStatus = "PAGEIN";
            newPage = false;
        }

        // font information
        features.fontStatus = currentFontString;

        // font size information
        features.fontSize = currentFontSizeString;

        // remove the break line or tab, only test the text
        localText = localText.replaceAll("[\n\t]", "");

        // capitalisation
        if (Character.isUpperCase(localText.charAt(0))) {
            features.capitalisation = "INITCAP";
        }

        if (featureFactory.test_all_capital(localText)) {
            features.capitalisation = "ALLCAP";
        }

        if (features.capitalisation == null)
            features.capitalisation = "NOCAPS";

        // digit information
        if (featureFactory.test_digit(localText)) {
            features.digit = "CONTAINSDIGITS";
        }

        Matcher m = featureFactory.isDigit.matcher(localText);
        if (m.find()) {
            features.digit = "ALLDIGIT";
        }

        if (features.digit == null)
            features.digit = "NODIGIT";

        // character information
        if (localText.length() == 1) {
            features.singleChar = true;
        }

        // lexical information
        if (featureFactory.test_names(localText)) {
            features.properName = true;
        }

        if (featureFactory.test_common(localText)) {
            features.commonName = true;
        }

        if (featureFactory.test_first_names(localText)) {
            features.firstName = true;
        }

        Matcher m2 = featureFactory.year.matcher(localText);
        if (m2.find()) {
            features.year = true;
        }

        if (featureFactory.test_month(localText)) {
            features.month = true;
        }

        Matcher m3 = featureFactory.email.matcher(localText);
        if (m3.find()) {
            features.email = true;
        }

        Matcher m4 = featureFactory.http.matcher(localText);
        if (m4.find()) {
            features.http = true;
        }

        if (featureFactory.test_city(localText)) {
            features.locationName = true;
        }

        // relative document position
        features.relativeDocumentPosition = relativeDocumentPosition;

        // relative page position coordinate
        features.relativePagePosition = pagePos;

        // relative page position characters
        features.relativePagePositionChar = relativePagePositionChar;

        // punctuation profile and number of punctuation characters in the line
        features.punctuationProfile = TextUtilities.punctuationProfile(localText);

        // change the linelength as the block size
        features.blockLength = localText.length() / BLOCKSCALE;

        // bitmap
        if (graphicBitmap) {
            features.bitmapAround = true;
        }

        // vector
        if (graphicVector) {
            features.vectorAround = true;
        }

        // if the block is in the page main area
        features.inMainArea = inPageMainArea;

        // space with previous block
        features.spacingWithPreviousBlock = spacingWithPreviousBlock;

        // character density of the previous block
        features.characterDensity = characterDensity;

        // return the result of pair of (featured of blocks extracted from Pdf document) + (the token information)
        return features.printVector();
    }

    /**
     * Extract results from a labelled block of monograph.
     *
     * @param resultWithLabel :   featured block of monograph with the label
     * @param tokenizations   :   list of tokens
     * @return a monograph item
     */
    private String resultExtraction(String resultWithLabel, List<LayoutToken> tokenizations) {
        String label = null;
        TaggingTokenClusteror clusteror = null;

        try {
            if (resultWithLabel.contains(COVER_LABEL) || resultWithLabel.contains(TITLE_LABEL) || resultWithLabel.contains(PUBLISHER_LABEL)
                || resultWithLabel.contains(SUMMARY_LABEL) || resultWithLabel.contains(BIOGRAPHY_LABEL) || resultWithLabel.contains(ADVERTISEMENT_LABEL)
                || resultWithLabel.contains(TOC_LABEL) || resultWithLabel.contains(TOF_LABEL) || resultWithLabel.contains(PREFACE_LABEL)
                || resultWithLabel.contains(DEDICATION_LABEL) || resultWithLabel.contains(UNIT_LABEL) || resultWithLabel.contains(REFERENCE_LABEL)
                || resultWithLabel.contains(ANNEX_LABEL) || resultWithLabel.contains(INDEX_LABEL) || resultWithLabel.contains(GLOSSARY_LABEL)
                || resultWithLabel.contains(BACK_LABEL) || resultWithLabel.contains(OTHER_LABEL)) {
                clusteror = new TaggingTokenClusteror(GrobidModels.MONOGRAPH, resultWithLabel, tokenizations);

                if (clusteror != null) {
                    List<TaggingTokenCluster> clusters = clusteror.cluster();

                    for (TaggingTokenCluster cluster : clusters) {
                        if (cluster == null) {
                            continue;
                        }

                        TaggingLabel clusterLabel = cluster.getTaggingLabel();
                        Engine.getCntManager().i(clusterLabel);
                        List<LayoutToken> tokens = cluster.concatTokens();
                        //String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(cluster.concatTokens()));
                        String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(tokens));

                        if (clusterLabel.equals(MONOGRAPH_COVER)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_TITLE)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_PUBLISHER)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_SUMMARY)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_BIOGRAPHY)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_ADVERTISEMENT)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_TOC)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_TOF)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_PREFACE)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_DEDICATION)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_UNIT)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_REFERENCE)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_ANNEX)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_INDEX)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_GLOSSARY)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_BACK)) {
                            label = cluster.getTaggingLabel().getLabel();
                        } else if (clusterLabel.equals(MONOGRAPH_OTHER)) {
                            label = cluster.getTaggingLabel().getLabel();
                        }
                    }
                }
            } else {
                label = "<undefined>";
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
        return label;
    }

    /**
     * Addition of the features at block level for the complete document.
     * <p/>
     * This is an alternative to the token and line level, where the unit for labeling is the block - so allowing even
     * faster processing and involving less features.
     * Lexical features becomes block prefix and suffix, the feature text unit is the first 10 characters of the
     * block without space.
     * The dictionary flags are at block level (i.e. the block contains a name mention, a place mention, a year, etc.)
     * Regarding layout features: font, size and style are the one associated to the first token of the block.
     */
    public String getAllBlocksFeatured(Document doc) {

        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }

        //guaranteeing quality of service. Otherwise, there are some PDF that may contain 300k blocks and thousands of extracted "images" that ruins the performance
        if (blocks.size() > GrobidProperties.getPdfBlocksMax()) {
            throw new GrobidException("Postprocessed document is too big, contains: " + blocks.size(), GrobidExceptionStatus.TOO_MANY_BLOCKS);
        }

        // list of textual patterns at the head and foot of pages which can be re-occur on several pages
        // (typically indicating a publisher foot or head notes)
        Map<String, Integer> patterns = new TreeMap<String, Integer>();
        Map<String, Boolean> firstTimePattern = new TreeMap<String, Boolean>();

        for (Page page : doc.getPages()) {
            // we just look at the two first and last blocks of the page
            if ((page.getBlocks() != null) && (page.getBlocks().size() > 0)) {
                for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                    if ((blockIndex < 2) || (blockIndex > page.getBlocks().size() - 2)) {
                        Block block = page.getBlocks().get(blockIndex);
                        String localText = block.getText();
                        if ((localText != null) && (localText.length() > 0)) {
                            String pattern = featureFactory.getPattern(localText);
                            if (pattern.length() > 8) {
                                Integer nb = patterns.get(pattern);
                                if (nb == null) {
                                    patterns.put(pattern, Integer.valueOf("1"));
                                    firstTimePattern.put(pattern, false);
                                } else
                                    patterns.put(pattern, Integer.valueOf(nb + 1));
                            }
                        }
                    }
                }
            }
        }

        String featuresAsString = getFeatureVectorsAsString(doc, patterns, firstTimePattern);

        return featuresAsString;
    }

    private String getFeatureVectorsAsString(Document doc, Map<String, Integer> patterns,
                                             Map<String, Boolean> firstTimePattern) {
        StringBuilder featureMonographBlock = new StringBuilder();
        int documentLength = doc.getDocumentLenghtChar();

        String currentFont = null;
        int currentFontSize = -1;

        boolean newPage;
        int mm = 0; // page position
        int nn = 0; // document position
        int pageLength = 0; // length of the current page
        double pageHeight = 0.0;
        //int pageNum = 0;

        // vector for features
        FeaturesVectorMonograph features;
        FeaturesVectorMonograph previousFeatures = null;

        // take only 20% of the first pages and 10% of the last pages (70% pages are considered always as chapter or unit)
        /*int pageSizeTotal = doc.getPages().size();
        int firstPageSize = pageSizeTotal * 20 / 100;
        int lastPageSize = pageSizeTotal - (pageSizeTotal * 10 / 100);*/
        for (Page page : doc.getPages()) {
            pageHeight = page.getHeight();
            newPage = true;
            double spacingPreviousBlock = 0.0; // discretized
            double lowestPos = 0.0;
            pageLength = page.getPageLengthChar();
            BoundingBox pageBoundingBox = page.getMainArea();
            mm = 0;

            if ((page.getBlocks() == null) || (page.getBlocks().size() == 0))
                continue;

            /*if (pageNum > firstPageSize && pageNum < lastPageSize)
                continue;*/

            for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                String str = null;
                Block block = page.getBlocks().get(blockIndex);

                boolean graphicVector = false;
                boolean graphicBitmap = false;

                boolean lastPageBlock = false;
                boolean firstPageBlock = false;

                if (blockIndex == page.getBlocks().size() - 1) {
                    lastPageBlock = true;
                }

                if (blockIndex == 0) {
                    firstPageBlock = true;
                }

                // check if we have a graphical object connected to the current block
                List<GraphicObject> localImages = Document.getConnectedGraphics(block, doc);
                if (localImages != null) {
                    for (GraphicObject localImage : localImages) {
                        if (localImage.getType() == GraphicObjectType.BITMAP)
                            graphicBitmap = true;
                        if (localImage.getType() == GraphicObjectType.VECTOR)
                            graphicVector = true;
                    }
                }

                if (lowestPos > block.getY()) {
                    // we have a vertical shift, which can be due to a change of column or other particular layout formatting
                    spacingPreviousBlock = doc.getMaxBlockSpacing() / 5.0; // default
                } else
                    spacingPreviousBlock = block.getY() - lowestPos;

                String localText = block.getText();
                if (localText.length() == 0 || localText == null) {
                    continue;
                }

                // character density of the block
                double density = 0.0;
                if ((block.getHeight() != 0.0) && (block.getWidth() != 0.0) &&
                    (block.getText() != null) && (!block.getText().contains("@PAGE")) &&
                    (!block.getText().contains("@IMAGE")))
                    density = (double) block.getText().length() / (block.getHeight() * block.getWidth());

                // is the current block in the main area of the page or not?
                boolean inPageMainArea = true;
                BoundingBox blockBoundingBox = BoundingBox.fromPointAndDimensions(page.getNumber(),
                    block.getX(), block.getY(), block.getWidth(), block.getHeight());
                if (pageBoundingBox == null || (!pageBoundingBox.contains(blockBoundingBox) && !pageBoundingBox.intersect(blockBoundingBox)))
                    inPageMainArea = false;

                String[] lines = localText.split("[\\n\\r]");
                int linesSize = lines.length;
                // set the max length of the lines in the block, in number of characters
                int maxLineLength = 0, idxMaxLength = 0;
                for (int p = 0; p < linesSize; p++) {
                    if (lines[p].length() > maxLineLength) {
                        maxLineLength = lines[p].length();
                        idxMaxLength = p;
                    }
                }
                List<LayoutToken> tokens = block.getTokens();
                if ((tokens == null) || (tokens.size() == 0)) {
                    continue;
                }

                // for the monograph model, we treat the information by blocks rather than by lines
                //for (int li = 0; li < lines.length; li++) {
                //String line = lines[li];

                // for the layout information of the block, we take simply the first layout token
                LayoutToken token = null;
                if (tokens.size() > 0)
                    token = tokens.get(0);

                double coordinateLineY = token.getY();

                features = new FeaturesVectorMonograph();
                //features.token = token;
                //features.line = line;

                // not a feature, the complete processed block
                features.block = localText;

                // pattern repeated on several pages
                if ((blockIndex < 2) || (blockIndex > page.getBlocks().size() - 2)) {
                    String pattern = featureFactory.getPattern(localText);
                    Integer nb = patterns.get(pattern);
                    if ((nb != null) && (nb > 1)) {
                        features.repetitivePattern = true;

                        Boolean firstTimeDone = firstTimePattern.get(pattern);
                        if ((firstTimeDone != null) && !firstTimeDone) {
                            features.firstRepetitivePattern = true;
                            firstTimePattern.put(pattern, true);
                        }
                    }
                }

                // the first string is the first token of the first line of the block
                str = lines[0];
                if (str.length() > 0 || str != null) {
                    String[] splitStr = StringUtils.split(str);
                    features.string = splitStr[0];
                } else {
                    features.string = null;
                }

                // the second string is the first token of the longest line of the block
                str = lines[idxMaxLength];
                if (str.length() > 0) {
                    String[] splitStr = StringUtils.split(str);
                    features.secondString = splitStr[0];
                } else {
                    features.secondString = null;
                }

                // the third string is the first token of the last line of the block
                str = lines[linesSize - 1];
                if (str.length() > 0) {
                    String[] splitStr = StringUtils.split(str);
                    features.thirdString = splitStr[0];
                } else {
                    features.thirdString = null;
                }

                features.firstPageBlock = firstPageBlock;
                features.lastPageBlock = lastPageBlock;

                // block information: since the data processed by blocks, not by lines, the blockStatus will always be BLOCKSTART
                //features.blockStatus = null;

                // HERE horizontal information
                // CENTERED
                // LEFTAJUSTED
                // CENTERED

                // page information
                if (newPage) {
                    features.pageStatus = "PAGESTART";
                    newPage = false;
                    if (previousFeatures != null)
                        previousFeatures.pageStatus = "PAGEEND";
                } else {
                    features.pageStatus = "PAGEIN";
                    newPage = false;
                }

                // font information
                if (currentFont == null) {
                    currentFont = token.getFont();
                    features.fontStatus = "NEWFONT";
                } else if (!currentFont.equals(token.getFont())) {
                    currentFont = token.getFont();
                    features.fontStatus = "NEWFONT";
                } else
                    features.fontStatus = "SAMEFONT";

                // font size information
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

                // string type information
                /*if (token.getBold())
                    features.bold = true;

                if (token.getItalic())
                    features.italic = true;*/

                // remove the break line or tab, only test the text
                localText = localText.replaceAll("[\n\t]", "");

                // capitalisation
                if (Character.isUpperCase(localText.charAt(0))) {
                    features.capitalisation = "INITCAP";
                }

                if (featureFactory.test_all_capital(localText)) {
                    features.capitalisation = "ALLCAP";
                }

                if (features.capitalisation == null)
                    features.capitalisation = "NOCAPS";

                // digit information
                if (featureFactory.test_digit(localText)) {
                    features.digit = "CONTAINSDIGITS";
                }

                Matcher m = featureFactory.isDigit.matcher(localText);
                if (m.find()) {
                    features.digit = "ALLDIGIT";
                }

                if (features.digit == null)
                    features.digit = "NODIGIT";

                // character information
                if (localText.length() == 1) {
                    features.singleChar = true;
                }

                // lexical information
                if (featureFactory.test_names(localText)) {
                    features.properName = true;
                }

                if (featureFactory.test_common(localText)) {
                    features.commonName = true;
                }

                if (featureFactory.test_first_names(localText)) {
                    features.firstName = true;
                }

                Matcher m2 = featureFactory.year.matcher(localText);
                if (m2.find()) {
                    features.year = true;
                }

                if (featureFactory.test_month(localText)) {
                    features.month = true;
                }

                Matcher m3 = featureFactory.email.matcher(localText);
                if (m3.find()) {
                    features.email = true;
                }

                Matcher m4 = featureFactory.http.matcher(localText);
                if (m4.find()) {
                    features.http = true;
                }

                if (featureFactory.test_city(localText)) {
                    features.locationName = true;
                }

                /*if (features.punctType == null)
                    features.punctType = "NOPUNCT";*/

                // relative document position
                features.relativeDocumentPosition = featureFactory
                    .linearScaling(nn, documentLength, NBBINS_POSITION);
                //System.out.println(nn + " " + documentLength + " " + NBBINS_POSITION + " " + features.relativeDocumentPosition);

                // relative page position coordinate
                int pagePos = featureFactory
                    .linearScaling(coordinateLineY, pageHeight, NBBINS_POSITION);
                //System.out.println(coordinateLineY + " " + pageHeight + " " + NBBINS_POSITION + " " + pagePos);
                if (pagePos > NBBINS_POSITION)
                    pagePos = NBBINS_POSITION;
                features.relativePagePosition = pagePos;
                //System.out.println(coordinateLineY + "\t" + pageHeight);

                // relative page position characters
                features.relativePagePositionChar = featureFactory
                    .linearScaling(mm, pageLength, NBBINS_POSITION);
                //System.out.println(mm + " " + pageLength + " " + NBBINS_POSITION + " " + features.relativePagePositionChar);

                // punctuation profile and number of punctuation characters in the line
                features.punctuationProfile = TextUtilities.punctuationProfile(localText);

                // change the linelength as the block size
                features.blockLength = localText.length() / BLOCKSCALE;
                //features.lineLength = line.length() / LINESCALE;
                    /*features.lineLength = featureFactory
                            .linearScaling(line.length(), maxLineLength, LINESCALE);*/

                // bitmap
                if (graphicBitmap) {
                    features.bitmapAround = true;
                }

                // vector
                if (graphicVector) {
                    features.vectorAround = true;
                }

                // if the block is in the page main area
                features.inMainArea = inPageMainArea;

                // space with previous block
                if (spacingPreviousBlock != 0.0) {
                    features.spacingWithPreviousBlock = featureFactory
                        .linearScaling(spacingPreviousBlock - doc.getMinBlockSpacing(), doc.getMaxBlockSpacing() - doc.getMinBlockSpacing(), NBBINS_SPACE);
                }

                // character density of the previous block
                if (density != -1.0) {
                    features.characterDensity = featureFactory
                        .linearScaling(density - doc.getMinCharacterDensity(), doc.getMaxCharacterDensity() - doc.getMinCharacterDensity(), NBBINS_DENSITY);
                    //System.out.println((density-doc.getMinCharacterDensity()) + " " + (doc.getMaxCharacterDensity()-doc.getMinCharacterDensity()) + " " + NBBINS_DENSITY + " " + features.characterDensity);
                }

                if (previousFeatures != null) {
                    String vector = previousFeatures.printVector();
                    featureMonographBlock.append(vector);
                }
                previousFeatures = features;

                //System.out.println((spacingPreviousBlock-doc.getMinBlockSpacing()) + " " + (doc.getMaxBlockSpacing()-doc.getMinBlockSpacing()) + " " + NBBINS_SPACE + " "
                //    + featureFactory.linearScaling(spacingPreviousBlock-doc.getMinBlockSpacing(), doc.getMaxBlockSpacing()-doc.getMinBlockSpacing(), NBBINS_SPACE));

                // lowest position of the block
                lowestPos = block.getY() + block.getHeight();

                // update page-level and document-level positions
                if (tokens != null) {
                    mm += tokens.size();
                    nn += tokens.size();
                }
            }
            //pageNum++;
        }
        if (previousFeatures != null)
            featureMonographBlock.append(previousFeatures.printVector());

        return featureMonographBlock.toString();
    }

    /**
     * Create a blank training data for new monograph model
     *
     * @param inputFile input PDF file
     * @param pathRaw   path to raw monograph featured sequence
     * @param pathTEI   path to TEI, the file is not labeled yet
     * @param id        id
     */
    public Document createBlankTrainingFromPDF(File inputFile,
                                               String pathRaw,
                                               String pathTEI,
                                               int id) {
        if (tmpPath == null)
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                tmpPath.getAbsolutePath() + "' does not exists.");
        }
        DocumentSource documentSource = null;
        Document doc = null;
        List<Block> blocks = null;
        Writer writer = null;
        StringBuilder builder = null;
        try {
            builder = new StringBuilder();
            if (!inputFile.exists()) {
                throw new GrobidResourceException("Cannot train for monograph, because file '" +
                    inputFile.getAbsolutePath() + "' does not exists.");
            }
            String pdfFileName = inputFile.getName();

            File outputTEIFile = new File(pathTEI + "/" + pdfFileName.replace(".pdf", "training.monograph.tei.xml"));
            File outputRawFile = new File(pathRaw + "/" + pdfFileName.replace(".pdf", "training.monograph"));

            documentSource = DocumentSource.fromPdf(inputFile, -1, -1, false, true, true);
            doc = new Document(documentSource);
            doc.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());
            blocks = doc.getBlocks();

            if (blocks == null) {
                throw new Exception("PDF parsing resulted in empty content");
            } else {
                String lang = null;
                String text = doc.getBlocks().get(0).getText(); // get only the text from the first block as example to recognize the language
                Language langID = languageUtilities.getInstance().runLanguageId(text);
                if (langID != null) {
                    lang = langID.getLang();
                } else {
                    lang = "en"; // by default, id = english
                }

                builder.append("<?xml version=\"1.0\" ?>\n<tei xml:space=\"preserve\">\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
                    "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"" + lang + "\">\n");

                // output an XML document based on the provided outline and the tokenization
                List<LayoutToken> tokens = doc.getTokenizations();
                // create blank training data
                if (tokens != null) {
                    for (LayoutToken token : tokens) {
                        if (token.getText() != null) {
                            builder.append(TextUtilities.HTMLEncode(token.getText()));
                        }
                    }
                }

                builder.append("</text>\n</tei>");
                // write the TEI file
                writer = new OutputStreamWriter(new FileOutputStream(outputTEIFile, false), "UTF-8");
                writer.write(builder.toString());
                writer.close();

                // besides the tagged TEI file, we also need the raw file with some key layout featuresAsString
                String rawText = getAllBlocksFeatured(doc);
                // Let us now take care of the raw file
                writer = new OutputStreamWriter(new FileOutputStream(outputRawFile, false), "UTF-8");
                writer.write(rawText);
                writer.close();
            }

            return doc;

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid training" +
                " data generation for monograph.", e);
        } finally {
            DocumentSource.close(documentSource, true, true, true);
        }
    }

    /**
     * Process the specified pdf and format the result as training data for the monograph model.
     *
     * @param inputFile input PDF file
     *                  /* @param pathFullText path to raw monograph featured sequence
     * @param pathTEI   path to TEI
     * @param id        id
     */
    public Document createTrainingFromPDF(File inputFile,
                                          String pathRaw,
                                          String pathTEI,
                                          int id) {
        if (tmpPath == null)
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                tmpPath.getAbsolutePath() + "' does not exists.");
        }
        DocumentSource documentSource = null;
        Document doc = null;
        try {
            if (!inputFile.exists()) {
                throw new GrobidResourceException("Cannot train for monograph, because file '" +
                    inputFile.getAbsolutePath() + "' does not exists.");
            }
            String pdfFileName = inputFile.getName();

            File outputTEIFile = new File(pathTEI + "/" + pdfFileName.replace(".pdf", "training.monograph.tei.xml"));
            File outputRawFile = new File(pathRaw + "/" + pdfFileName.replace(".pdf", "training.monograph"));

            documentSource = DocumentSource.fromPdf(inputFile, -1, -1, true, true, true);
            doc = new Document(documentSource);
            doc.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());

            if (doc.getBlocks() == null) {
                throw new Exception("PDF parsing resulted in empty content");
            }

            // TODO language identifier here on content text sample
            String lang = null;
            String text = doc.getBlocks().get(0).getText(); // get only the text from the first block as example to recognize the language
            Language langID = languageUtilities.getInstance().runLanguageId(text);
            if (langID != null) {
                lang = langID.getLang();
            } else {
                lang = "en"; // by default, id = english
            }

            doc.produceStatistics();
            StringBuilder builder = new StringBuilder();
            builder.append("<?xml version=\"1.0\" ?>\n<tei xml:space=\"preserve\">\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
                "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"" + lang + "\">\n");

            // get the document outline
            DocumentNode outlineRoot = doc.getOutlineRoot();

            // output an XML document based on the provided outline and the tokenization
            List<LayoutToken> tokens = doc.getTokenizations();

            DocumentNode currentNode = outlineRoot;
            // preorder traversal of the table of contents
            LinkedList<DocumentNode> stackTOC = new LinkedList<DocumentNode>();
            String gornString = ""; //will keep (the prefix for) a Gorn adress
            int oldDepth = 0; //numbers that will indicate
            int currentDepth = 0; // the current depth in the table of contents
            boolean tocExists = (currentNode != null);
            if (tocExists) {
                builder.append("<div type=\"contents\">\n");
                currentNode.setAddress("*");
                stackTOC.push(currentNode);
                oldDepth = 0;
            }
            while (stackTOC.size() > 0) {
                currentNode = stackTOC.pop();
                // at this point, the page at which the chapter/section,
                // referenced by current node, starts
                // is given by currentNode.getBoundingBox().getPage()
                gornString = currentNode.getAddress();
                if (gornString != "*") {
                    currentDepth = (gornString.split("[.]")).length;
                    if (currentDepth > oldDepth) {
                        for (int i = 0; i < currentDepth; i++) {
                            builder.append("\t");
                        }
                        builder.append("<list>\n");
                    }
                    while (currentDepth < oldDepth) {
                        for (int i = 0; i < oldDepth; i++) {
                            builder.append("\t");
                        }
                        builder.append("</list>\n");
                        oldDepth--;
                    }
                    for (int i = 0; i < currentDepth; i++) {
                        builder.append("\t");
                    }
                    oldDepth = currentDepth;
                    builder.append("<item n=\""
                        + gornString + "\"> "
                        + currentNode.getLabel()
                        + " </item>\n");
                }
                if (gornString == "*") {
                    gornString = "";
                } else {
                    gornString += ".";
                }
                List<DocumentNode> children = currentNode.getChildren();
                if (children != null) {
                    int s = children.size();
                    while (s > 0) {
                        s = s - 1;
                        currentNode = children.get(s);
                        currentNode.setAddress(gornString + String.valueOf(s));
                        stackTOC.push(currentNode);
                    }
                }
            }
            if (tocExists) {
                while (0 < currentDepth) {
                    for (int i = 0; i < currentDepth; i++) {
                        builder.append("\t");
                    }
                    builder.append("</list>\n");
                    currentDepth--;
                }
                builder.append("</div>\n");
            }

            // So far we transcribed the table of contexts in the beginning of the text
            // Now we will tag it inside the text.
            if (tocExists) {
                outlineRoot = doc.getOutlineRoot();
                //recall that stackTOC is an empty stack at this point, it arrived empty from the previous usage
                stackTOC.push(outlineRoot);
                // as before, stackTOC will keep the stack of chapters, sections, etc., that are yet to be tagged
                // we push outlineRoot first to get again to the root of the document outline
                List<OffsetPosition> results;
                int tokenCtr = 0;
                int numberOfTokens = tokens.size();
                // We initialize the position of the starting and the ending token of a title of a chapter / section
                // so they will be actually used only when an useful number
                // is written into them, not earlier.
                int startTokenOffset = numberOfTokens;
                int endTokenOffset = -1;
                List<DocumentNode> children;
                int depth = 0; // depth of the chapter title in the TOC tree
                int nbOpenDivs = 0; // counts the opened div tags for the chapters, sections etc.
                //String currentTitle = "" ;
                builder.append("DEBUGGING: The total number of tokens is "
                    + numberOfTokens
                    + "\n");
                while (stackTOC.size() > 0 && tokenCtr < numberOfTokens) {
                    currentNode = stackTOC.pop();
                    // In order to avoid writing the "null" label from the
                    // outlineRoot into the TEI and also
                    // just in case the outline was ill-formed we search
                    // the first node (first wrt preorder traversal)
                    // such that the title kept in the node is non trivial
                    while (currentNode.getLabel() == null) {
                        children = currentNode.getChildren();
                        if (children != null) {
                            int s = children.size();
                            while (s > 0) {
                                s = s - 1;
                                currentNode = children.get(s);
                                stackTOC.push(currentNode);
                            }
                        }
                        currentNode = stackTOC.pop();
                    }
                    // currentTitle = currentNode.getLabel() ;
                    // at the moment we exit the while loop, currentNode keeps non-trivial information about the title of a section
                    // we will search instances of this title using FastMatcher
                    // builder.append("DEBUGGING: Looking for title "
                    //                     + currentTitle
                    //                     + "\n");
                    // We compute the depth:
                    depth = currentNode.getAddress().split("[.]").length;
                    // Exemple of how to use FastMatcher :
                    // matcher.loadTerm("un titre", GrobidAnalyzer.getInstance(), true);
                    // results = matcher.matchLayoutToken(tokens, true, false);
                    // then results contains a list of indices i,j such that
                    // the i-th token in the list of layout tokens doc.getTokenizations()
                    // is the first token of an instance of the queried sequence of tokens
                    boolean ignoreDelimiters = true;
                    boolean caseSensitive = false;
                    FastMatcher matcher = new FastMatcher(); //at this stage, the terms attribute of matcher is an empty dictionary
                    //int nbTermsInTitle =
                    matcher.loadTerm(currentNode.getLabel(),
                        GrobidAnalyzer.getInstance(),
                        ignoreDelimiters, // true
                        caseSensitive); // false
                    // builder.append("DEBUGGING: Loaded  " + nbTermsInTitle + " terms from the title " + currentNode.getLabel() + "\n"); //it should always be 1
                    results = matcher.matchLayoutToken(tokens,
                        ignoreDelimiters, // true
                        caseSensitive); // false
                    if (results.size() > 0) {//if some instance of the title is found
                        // The list results may contain more than one instance of the title we are looking for.
                        // We want the position of the last instance that is on the same page that the outline is pointing to.
                        // We search for the last one because the table of contents is usually before the contents.
                        int currentNodesPage = currentNode.getBoundingBox().getPage();
                        int index = results.size() - 1;
                        while (index >= 0 && results.get(index).start >= tokenCtr && tokens.get(results.get(index).start).getPage() > currentNodesPage) {
                            index--;
                        }
                        if (index >= 0 && results.get(index).start >= tokenCtr && tokens.get(results.get(index).start).getPage() == currentNodesPage) {
                            // We proceed to update the starting and ending positions
                            // of the title only if the title have actually been found
                            // at the right page, and later than the previous chapter
                            //builder.append("DEBUGGING: Found " + results.size() + " instances of " + currentTitle + "\n");
                            //builder.append("DEBUGGING: " + currentTitle + " is at page " + currentNodesPage + "\n");
                            startTokenOffset = results.get(index).start;
                            //builder.append("DEBUGGING: " + currentTitle + " starts at token " + startTokenOffset + "\n");
                            endTokenOffset = results.get(index).end;
                            //builder.append("DEBUGGING: " + currentTitle + " ends at token " + endTokenOffset + "\n");
                        }
                        // else {
                        //     builder.append("DEBUGGING : the title " + currentTitle + " was found at page(s): " );
                        //     for ( index = 0; index < results.size(); index++ ) { builder.append( tokens.get(results.get(index).start).getPage() + ", "); }
                        //     builder.append( "\nDEBUGGING : it should have been in page " + currentNodesPage + "\n" );
                        // }
                    }
                    while (tokenCtr <= endTokenOffset && tokenCtr < numberOfTokens) {
                        if (tokenCtr == startTokenOffset) { //if we are about to write the starting token of a title of a chapter/section
                            while (nbOpenDivs >= depth) { //if the new chapter is at a lower or equal level
                                // compared to the previously written chapter title
                                // then we need to close the previous chapters before writing the current one.
                                builder.append("</div>\n");
                                nbOpenDivs--;
                            }
                            //then we write the section title opening tag
                            String currentTitleNormalized = StringUtils.normalizeSpace(UnicodeUtil.normaliseText(currentNode.getLabel())).toLowerCase();
                            String[] currentTitleSplit = currentTitleNormalized.split("[" + TextUtilities.SPACE + "]");
                            String firstInTitle = currentTitleSplit[0];
                            String lastInTitle = currentTitleSplit[currentTitleSplit.length - 1];
                            builder.append("<div n=\"" + currentNode.getAddress() + "\" type=\"");
                            if (firstInTitle == "partie" || lastInTitle == "partie") {
                                builder.append("part");
                            } else if (firstInTitle == "chapitre") {
                                builder.append("chapter");
                            } else if (firstInTitle == "bibliographie" || lastInTitle == "bibliographiques") {
                                builder.append("bibliogr");
                            } else {
                                switch (currentTitleNormalized) {
                                    case "remerciements":
                                        builder.append("ack");
                                        break;
                                    case "sommaire":
                                        builder.append("contents");
                                        break;
                                    case "introduction":
                                        builder.append("preface");
                                        break;
                                    case "prologue":
                                        builder.append("preface");
                                        break;
                                    case "annexe":
                                        builder.append("appendix");
                                        break;
                                    case "épilogue":
                                        builder.append("appendix");
                                        break;
                                    case "appendices":
                                        builder.append("appendix");
                                        break;
                                    case "index":
                                        builder.append("index");
                                        break;
                                    case "abréviations":
                                        builder.append("glossary");
                                        break;
                                    case "glossaire":
                                        builder.append("glossary");
                                        break;
                                    //TODO add other cases as the data increase
                                    default:
                                        builder.append("chapter");
                                }
                            }
                            builder.append("\">\n<head> ");
                            nbOpenDivs++;
                        }
                        builder.append(TextUtilities.HTMLEncode(tokens.get(tokenCtr).getText()));
                        if (tokenCtr == endTokenOffset) {
                            // We add the closing delimiters from the title, if needed
                            // At first, we skip by the first delimiter, if there is any
                            // (in French question mark should always preceded by a space, that's why for the first step we include space)
                            if (TextUtilities.delimiters.indexOf(tokens.get(tokenCtr + 1).getText()) != -1) {
                                builder.append(tokens.get(tokenCtr + 1).getText());
                                tokenCtr++;
                            }
                            while (TextUtilities.fullPunctuations.indexOf(tokens.get(tokenCtr + 1).getText()) != -1) {
                                builder.append(tokens.get(tokenCtr + 1).getText());
                                tokenCtr++;
                            }
                            //builder.append(" </head>\n"); // we close the chapter title
                            // we add all the sections of the chapter to our stack
                            children = currentNode.getChildren();
                            if (children != null) {
                                int s = children.size();
                                while (s > 0) {
                                    s = s - 1;
                                    currentNode = children.get(s);
                                    stackTOC.push(currentNode);
                                }
                            }
                            // we update the depth
                            depth = currentNode.getAddress().split("[.]").length;
                        }
                        tokenCtr++;
                    }
                }
                // Once all the chapters/sections have been opened,
                // transcribe the rest of the text
                while (tokenCtr < numberOfTokens) {
                    builder.append(TextUtilities.HTMLEncode(tokens.get(tokenCtr).getText()));
                    tokenCtr++;
                }
                //then close the chapters that are still open
                while (nbOpenDivs > 0) {
                    builder.append("</div>\n");
                    nbOpenDivs--;
                }
            } else {
                for (LayoutToken token : tokens) {
                    builder.append(TextUtilities.HTMLEncode(token.getText()));
                }
            }

            builder.append("</text>\n</tei>");

            // write the TEI file
            Writer writer = new OutputStreamWriter(new FileOutputStream(outputTEIFile, false), "UTF-8");
            writer.write(builder.toString());
            writer.close();

            // besides the tagged TEI file, we also need the raw file with some key layout featuresAsString
            String rawText = getAllBlocksFeatured(doc);
            // Let us now take care of the raw file
            writer = new OutputStreamWriter(new FileOutputStream(outputRawFile, false), "UTF-8");
            writer.write(rawText);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid training" +
                " data generation for monograph.", e);
        } finally {
            DocumentSource.close(documentSource, true, true, true);
        }

        return doc;
    }


    public void close() throws IOException {
        try {
            monographParser.close();
        } catch (Exception e) {
            LOGGER.warn("Cannot close the parser: " + e.getMessage());
        }
    }
}
