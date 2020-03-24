package org.grobid.core.engines;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentNode;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.document.TEIMonographFormatter;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorMonograph;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.*;
import org.grobid.core.lexicon.FastMatcher;
import org.grobid.core.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;

import static org.apache.commons.lang3.StringUtils.*;

import java.lang.StringBuilder;

/**
 * Realise a high level segmentation of a monograph. Monograph is to be understood here in the context library cataloging,
 * basically as a standalone book. The monograph could be an ebook (novels), a conference proceedings volume, a book
 * collection volume, a phd/msc thesis, a standalone report (with toc, etc.), a manual (with multiple chapters).
 * Monographs, here, are NOT magazine volumes, journal issues, newspapers, standalone chapters, standalone scholar articles,
 * tables of content, reference works, dictionaries, encyclopedia volumes, graphic novels.
 *
 * @author Patrice Lopez
 */
public class MonographParser extends AbstractParser {
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MonographParser.class);

    private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();

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
    private EngineParsers parsers;

    public MonographParser() {
        super(GrobidModels.MONOGRAPH);
        this.parsers = parsers;
        tmpPath = GrobidProperties.getTempPath();
    }

    /*
     * Machine-learning recognition of the complete monograph structures.
     *
     * Segment a PDF document into high level zones:
     *  cover, title, publisher, summary, biography, advertisement, toc, tof,
     *  preface, dedication, unit, reference, annex, index, glossary, back, other.
     *
     * @param input file
     * @return the built TEI
     */

    public String processing(File input) throws Exception {
        if (tmpPath == null) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        }
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                tmpPath.getAbsolutePath() + "' does not exists.");
        }

        if (!input.exists()) {
            throw new GrobidResourceException("Cannot process the monograph model, because the file '" +
                input.getAbsolutePath() + "' does not exists.");
        }

        DocumentSource documentSource = null;
        String lang = null, result = null;
        try {
            Language langID = new Language();
            if (!input.exists()) {
                throw new GrobidResourceException("Cannot process the monograph model, because the file '" +
                    input.getAbsolutePath() + "' does not exists.");
            }

            documentSource = DocumentSource.fromPdf(input, -1, -1, false, true, true);
            Document doc = new Document(documentSource);
            GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance(); // or "GrobidAnalysisConfig.builder().build()"
            if (config.getAnalyzer() != null)
                doc.setAnalyzer(config.getAnalyzer());
            doc.addTokenizedDocument(config);
            List<Block> blocks = doc.getBlocks();

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

            result = prosesDocument(doc, config);

            return result;
        } finally {
            if (documentSource != null) {
                documentSource.close(true, true, true);
            }
        }
    }

    public String prosesDocument(Document doc, GrobidAnalysisConfig config) {
        List<LayoutToken> tokenizations = doc.getTokenizations();
        String result = null;

        if (tokenizations.size() > GrobidProperties.getPdfTokensMax()) {
            throw new GrobidException("The document has " + tokenizations.size() + " tokens, but the limit is " + GrobidProperties.getPdfTokensMax(),
                GrobidExceptionStatus.TOO_MANY_TOKENS);
        }

        doc.produceStatistics();
        // get feature data by lines
        String content = getAllLinesFeatured(doc);

        // get feature data by blocks
        //String content = getAllBlocksFeatured(doc);
        if (isNotEmpty(trim(content))) {
            // if the feature data are not empty, give them the labels
            if (isNotBlank(content)) {
                String labelledResult = label(content);
                // extract the labeled data
                result = resultExtraction(labelledResult, doc, config);
            }
        }

        // process into monographResults
        return result;
    }

    /**
     * Extract results from a labelled block of monograph.
     *
     * @param resultWithLabel :   featured block of monograph with the label
     * @return a monograph item
     */

    public String resultExtraction(String resultWithLabel, Document doc, GrobidAnalysisConfig config) {
        StringBuilder buffer = new StringBuilder();
        TEIMonographFormatter teiFormatter = null;

        try {
            teiFormatter = new TEIMonographFormatter();

            // ========= TEI-header =========
            buffer.append(teiFormatter.toTEIHeaderMonograph(doc, config));

            // ========= TEI-body =========
            // opening tag of a document
            buffer.append(teiFormatter.toTEIBodyMonograph(doc));

            List<Block> blocks = doc.getBlocks();
            int currentBlockIndex = 0;
            int indexLine = 0;

            // separate the resulted data by lines
            StringTokenizer resultByLines = new StringTokenizer(resultWithLabel, "\n");
            String label = null; // current label/tag
            String firstToken = null; // current lexical token
            String secondToken = null; // current second lexical token
            String lastTag = null;
            String firstLine = null;

            while (resultByLines.hasMoreTokens()) {
                String line = resultByLines.nextToken().trim();
                if (line.length() == 0) {
                    continue;
                }
                // separate the line by tokens
                StringTokenizer tokensInLine = new StringTokenizer(line, " \t");
                List<String> localFeatures = new ArrayList<String>();
                int i = 0; // the position of a token in a line
                int numberTokensInLine = tokensInLine.countTokens();
                while (tokensInLine.hasMoreTokens()) {
                    String token = tokensInLine.nextToken().trim();
                    if (i == 0) {
                        firstToken = TextUtilities.HTMLEncode(token); // lexical token
                    } else if (i == 1) {
                        secondToken = TextUtilities.HTMLEncode(token); // second lexical token
                    } else if (i == numberTokensInLine - 1) { // label/tag is the last position of token of each line
                        label = token; // current label
                    } else {
                        localFeatures.add(token); // keep the feature values in case they appear useful
                    }
                    i++;
                }

                /*as we process the document segmentation line by line, we don't use the usual
                tokenization to rebuild the text flow, but we get each line again from the
                text stored in the document blocks (similarly as when generating the features)*/
                String lineInBlock = null;
                line = null;
                while ((lineInBlock == null) && (currentBlockIndex < blocks.size())) {
                    Block block = blocks.get(currentBlockIndex);

                    // collect the token information from each block
                    List<LayoutToken> tokens = block.getTokens();
                    if (tokens == null) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    }

                    // colect the text from each block
                    String localText = block.getText();
                    if ((localText == null) || (localText.trim().length() == 0)) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    }

                    // separated the text in each block by lines
                    String[] lines = localText.split("[\\n\\r]");
                    if ((lines.length == 0) || (indexLine >= lines.length)) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    } else {

                        lineInBlock = lines[indexLine];
                        indexLine++;
                        if (lineInBlock.trim().length() == 0) {
                            lineInBlock = null;
                            continue;
                        }

                        if (TextUtilities.filterLine(lineInBlock)) {
                            lineInBlock = null;
                            continue;
                        }
                    }
                }
                line = TextUtilities.HTMLEncode(lineInBlock);
                String lastTag0 = null;
                if (lastTag != null) {
                    if (lastTag.startsWith("I-")) {
                        lastTag0 = lastTag.substring(2, lastTag.length());
                    } else {
                        lastTag0 = lastTag;
                    }
                }
                String currentTag0 = null;
                if (label != null) {
                    if (label.startsWith("I-")) {
                        currentTag0 = label.substring(2, label.length());
                    } else {
                        currentTag0 = label;
                    }
                }

                // output the result by lines
                line = line.replace("@BULLET", "\u2022");

                if (lastTag == null) { // only for the first line
                    firstLine = line;
                } else { // the rest of the lines
                    // process every block
                    /*if (!label.equals(lastTag) && (firstLine == null)) {
                        addTagClose(buffer, lastTag0); // close the previous tag
                        buffer.append(currentTag0);
                    }
                    if (firstLine != null){
                        buffer.append(currentTag0);
                        buffer.append(firstLine).append(line);
                        firstLine = null;
                    } else{
                        buffer.append(line);
                    }

                    if (!resultByLines.hasMoreTokens()) {
                        addTagClose(buffer, lastTag0);
                    }*/

                    // process every different tag
                    if (firstLine != null) {
                        buffer.append(currentTag0);
                        buffer.append(firstLine).append(line);
                        firstLine = null;
                    } else {
                        if (!currentTag0.equals(lastTag0) && (firstLine == null)) {
                            addTagClose(buffer, lastTag0);
                            buffer.append(currentTag0);
                        }
                        buffer.append(line);
                    }
                    if (!resultByLines.hasMoreTokens()) {
                        addTagClose(buffer, lastTag0);
                    }
                }

                lastTag = label;

            }

            // closing tag of a document
            buffer.append("\t</text>\n</TEI>\n");

        } catch (Exception e) {
            e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid training" +
                " data generation for monograph.", e);
        }
        return buffer.toString();
    }

    private void addTagClose(StringBuilder buffer, String currentTag) {
        // we close the current tag
        if (currentTag.equals("<cover>")) {
            buffer.append("</cover>\n\n");
        } else if (currentTag.equals("<title>")) {
            buffer.append("</title>\n\n");
        } else if (currentTag.equals("<publisher>")) {
            buffer.append("</publisher>\n\n");
        } else if (currentTag.equals("<summary>")) {
            buffer.append("</summary>\n\n");
        } else if (currentTag.equals("<biography>")) {
            buffer.append("</biography>\n\n");
        } else if (currentTag.equals("<advertisement>")) {
            buffer.append("</advertisement>\n\n");
        } else if (currentTag.equals("<toc>")) {
            buffer.append("</toc>\n\n");
        } else if (currentTag.equals("<tof>")) {
            buffer.append("</tof>\n\n");
        } else if (currentTag.equals("<preface>")) {
            buffer.append("</preface>\n\n");
        } else if (currentTag.equals("<dedication>")) {
            buffer.append("</dedication>\n\n");
        } else if (currentTag.equals("<unit>")) {
            buffer.append("</unit>\n\n");
        } else if (currentTag.equals("<reference>")) {
            buffer.append("</reference>\n\n");
        } else if (currentTag.equals("<annex>")) {
            buffer.append("</annex>\n\n");
        } else if (currentTag.equals("<index>")) {
            buffer.append("</index>\n\n");
        } else if (currentTag.equals("<glossary>")) {
            buffer.append("</glossary>\n\n");
        } else if (currentTag.equals("<back>")) {
            buffer.append("</back>\n\n");
        } else if (currentTag.equals("<other>")) {
            buffer.append("</other>\n\n");
        }
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

        /* list of textual patterns at the head and foot of pages which can be re-occur on several pages
         (typically indicating a publisher foot or head notes)*/
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
                                    patterns.put(pattern, Integer.valueOf(1));
                                    firstTimePattern.put(pattern, false);
                                } else
                                    patterns.put(pattern, Integer.valueOf(nb + 1));
                            }
                        }
                    }
                }
            }
        }

        String featuresAsString = getFeatureVectorBlocksAsString(doc, patterns, firstTimePattern);

        return featuresAsString;
    }

    private String getFeatureVectorBlocksAsString(Document doc, Map<String, Integer> patterns,
                                                  Map<String, Boolean> firstTimePattern) {
        StringBuilder featureMonograph = new StringBuilder();
        int documentLength = doc.getDocumentLenghtChar();

        String currentFont = null;
        int currentFontSize = -1;

        boolean newPage;
        int mm = 0; // page position
        int nn = 0; // document position
        int pageLength = 0; // length of the current page
        double pageHeight = 0.0;

        // vector for features
        FeaturesVectorMonograph features = null;
        FeaturesVectorMonograph previousFeatures = null;

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

            int maxBlockLength = 0;
            for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                Block block = page.getBlocks().get(blockIndex);

                boolean graphicVector = false;
                boolean graphicBitmap = false;

                boolean lastPageBlock = false;
                boolean firstPageBlock = false;

                if (blockIndex == 0) {
                    firstPageBlock = true;
                }

                if (blockIndex == page.getBlocks().size() - 1) {
                    lastPageBlock = true;
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
                if (localText == null)
                    continue;

                if (localText.length() > maxBlockLength)
                    maxBlockLength = localText.length();

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
                // set the max length of the lines in the block, in number of characters
                int maxLineLength = 0;
                for (int p = 0; p < lines.length; p++) {
                    if (lines[p].length() > maxLineLength)
                        maxLineLength = lines[p].length();
                }
                List<LayoutToken> tokens = block.getTokens();
                if ((tokens == null) || (tokens.size() == 0)) {
                    continue;
                }

                // treat the information by blocks

                // for the layout information of the block, we take simply the first layout token
                LayoutToken token = null;
                if (tokens.size() > 0)
                    token = tokens.get(0);

                double coordinateLineY = token.getY();

                features = new FeaturesVectorMonograph();

                features.block = localText;

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

                /* we take tokens from the first two lines of each block
                then we consider the first token of the line as usual lexical CRF token
                and the second token of the line as feature */
                String line0 = lines[0], line1 = null;
                if (lines.length >= 2) {
                    line1 = lines[1];
                } else {
                    line1 = lines[0];
                }

                StringTokenizer tokens0 = new StringTokenizer(line0, " \t");
                StringTokenizer tokens1 = new StringTokenizer(line1, " \t");
                String text00 = null, text01 = null, text10 = null, text11 = null;

                if (tokens0.hasMoreTokens())
                    text00 = tokens0.nextToken();

                if (tokens0.hasMoreTokens())
                    text01 = tokens0.nextToken();

                if (tokens1.hasMoreTokens())
                    text10 = tokens1.nextToken();

                if (tokens1.hasMoreTokens())
                    text11 = tokens1.nextToken();

                if (text00 == null)
                    continue;

                if (text01 == null)
                    continue;

                if (text10 == null)
                    continue;

                if (text11 == null)
                    continue;

                // final sanitisation and filtering
                text00 = text00.replaceAll("[ \n]", "").trim();
                text01 = text01.replaceAll("[ \n]", "").trim();
                text10 = text10.replaceAll("[ \n]", "").trim();
                text11 = text11.replaceAll("[ \n]", "").trim();

                if ((text00.length() == 0) || (text01.length() == 0) ||
                    (text10.length() == 0) || (text11.length() == 0) ||
                    (TextUtilities.filterLine(localText))) {
                    continue;
                }

                features.string = text00;
                features.secondString = text01;
                //features.thirdString = text10;
                //features.fourthString = text11;

                features.firstPageBlock = firstPageBlock;
                features.lastPageBlock = lastPageBlock;

                //features.blockLength = featureFactory.linearScaling(localText.length(), maxBlockLength, BLOCKSCALE);
                features.punctuationProfile = TextUtilities.punctuationProfile(localText);

                if (graphicBitmap) {
                    features.bitmapAround = true;
                }
                if (graphicVector) {
                    features.vectorAround = true;
                }

                features.punctType = null;

                if (firstPageBlock) {
                    features.blockStatus = "BLOCKSTART"; // the first block of each page
                } else if (lastPageBlock) {
                    features.blockStatus = "BLOCKEND"; // the last block of each page
                } else {
                    features.blockStatus = "BLOCKIN"; // the rest of the blocks in page
                }

                if (newPage) {
                    features.pageStatus = "PAGESTART";
                    newPage = false;
                    if (previousFeatures != null)
                        previousFeatures.pageStatus = "PAGEEND";
                } else {
                    features.pageStatus = "PAGEIN";
                    newPage = false;
                }

                if (localText.length() == 1) {
                    features.singleChar = true;
                }

                if (Character.isUpperCase(localText.charAt(0))) {
                    features.capitalisation = "INITCAP";
                }

                if (featureFactory.test_all_capital(localText)) {
                    features.capitalisation = "ALLCAP";
                }

                if (featureFactory.test_digit(localText)) {
                    features.digit = "CONTAINSDIGITS";
                }

                if (featureFactory.test_common(localText)) {
                    features.commonName = true;
                }

                if (featureFactory.test_names(localText)) {
                    features.properName = true;
                }

                if (featureFactory.test_month(localText)) {
                    features.month = true;
                }

                Matcher m = featureFactory.isDigit.matcher(localText);
                if (m.find()) {
                    features.digit = "ALLDIGIT";
                }

                Matcher m2 = featureFactory.year.matcher(localText);
                if (m2.find()) {
                    features.year = true;
                }

                Matcher m3 = featureFactory.email.matcher(localText);
                if (m3.find()) {
                    features.email = true;
                }

                Matcher m4 = featureFactory.http.matcher(localText);
                if (m4.find()) {
                    features.http = true;
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

                if (token.isBold())
                    features.bold = true;

                if (token.isItalic())
                    features.italic = true;

                if (features.capitalisation == null)
                    features.capitalisation = "NOCAPS";

                if (features.digit == null)
                    features.digit = "NODIGIT";

                features.relativeDocumentPosition = featureFactory
                    .linearScaling(nn, documentLength, NBBINS_POSITION);

                features.relativePagePositionChar = featureFactory
                    .linearScaling(mm, pageLength, NBBINS_POSITION);

                int pagePos = featureFactory
                    .linearScaling(coordinateLineY, pageHeight, NBBINS_POSITION);

                if (pagePos > NBBINS_POSITION)
                    pagePos = NBBINS_POSITION;

                features.relativePagePosition = pagePos;

                if (spacingPreviousBlock != 0.0) {
                    features.spacingWithPreviousBlock = featureFactory
                        .linearScaling(spacingPreviousBlock - doc.getMinBlockSpacing(), doc.getMaxBlockSpacing() - doc.getMinBlockSpacing(), NBBINS_SPACE);
                }

                features.inMainArea = inPageMainArea;

                if (density != -1.0) {
                    features.characterDensity = featureFactory
                        .linearScaling(density - doc.getMinCharacterDensity(), doc.getMaxCharacterDensity() - doc.getMinCharacterDensity(), NBBINS_DENSITY);
                }

                // lowest position of the block
                lowestPos = block.getY() + block.getHeight();

                // update page-level and document-level positions
                if (tokens != null) {
                    mm += tokens.size();
                    nn += tokens.size();
                }

                if (previousFeatures != null) {
                    String vector = previousFeatures.printVector();
                    featureMonograph.append(vector);
                }
                previousFeatures = features;
            }
        }

        if (previousFeatures != null)
            featureMonograph.append(previousFeatures.printVector());
        return featureMonograph.toString();
    }

    /**
     * Addition of the features at line level for the complete document.
     * <p/>
     * This is an alternative to the token level, where the unit for labeling is the line - so allowing even
     * faster processing and involving less features.
     * Lexical features becomes block prefix and suffix, the feature text unit is the first 10 characters of the
     * block without space.
     * The dictionary flags are at block level (i.e. the block contains a name mention, a place mention, a year, etc.)
     * Regarding layout features: font, size and style are the one associated to the first token of the block.
     */
    public String getAllLinesFeatured(Document doc) {

        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }

        //guaranteeing quality of service. Otherwise, there are some PDF that may contain 300k blocks and thousands of extracted "images" that ruins the performance
        if (blocks.size() > GrobidProperties.getPdfBlocksMax()) {
            throw new GrobidException("Postprocessed document is too big, contains: " + blocks.size(), GrobidExceptionStatus.TOO_MANY_BLOCKS);
        }

        /* list of textual patterns at the head and foot of pages which can be re-occur on several pages
         (typically indicating a publisher foot or head notes)*/
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
                            String[] lines = localText.split("[\\n\\r]");
                            if (lines.length > 0) {
                                String line = lines[0];
                                String pattern = featureFactory.getPattern(line);
                                if (pattern.length() > 8) {
                                    Integer nb = patterns.get(pattern);
                                    if (nb == null) {
                                        patterns.put(pattern, Integer.valueOf(1));
                                        firstTimePattern.put(pattern, false);
                                    } else
                                        patterns.put(pattern, Integer.valueOf(nb + 1));
                                }
                            }
                        }
                    }
                }
            }
        }

        String featuresAsString = getFeatureVectorLinesAsString(doc, patterns, firstTimePattern);

        return featuresAsString;
    }

    private String getFeatureVectorLinesAsString(Document doc, Map<String, Integer> patterns,
                                                 Map<String, Boolean> firstTimePattern) {
        StringBuilder featureMonograph = new StringBuilder();
        int documentLength = doc.getDocumentLenghtChar();

        String currentFont = null;
        int currentFontSize = -1;

        boolean newPage;
        int mm = 0; // page position
        int nn = 0; // document position
        int pageLength = 0; // length of the current page
        double pageHeight = 0.0;

        // vector for features
        FeaturesVectorMonograph features = null;
        FeaturesVectorMonograph previousFeatures = null;

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

            for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
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
                if (localText == null)
                    continue;

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
                // set the max length of the lines in the block, in number of characters
                int maxLineLength = 0;
                for (int p = 0; p < lines.length; p++) {
                    if (lines[p].length() > maxLineLength)
                        maxLineLength = lines[p].length();
                }
                List<LayoutToken> tokens = block.getTokens();
                if ((tokens == null) || (tokens.size() == 0)) {
                    continue;
                }
                // treat the information by lines
                for (int li = 0; li < lines.length; li++) {
                    String line = lines[li];

                    // for the layout information of the block, we take simply the first layout token
                    LayoutToken token = null;
                    if (tokens.size() > 0)
                        token = tokens.get(0);

                    double coordinateLineY = token.getY();

                    features = new FeaturesVectorMonograph();
                    features.token = token;
                    features.line = line;

                    if ((blockIndex < 2) || (blockIndex > page.getBlocks().size() - 2)) {
                        String pattern = featureFactory.getPattern(line);
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

                    // we consider the first token of the line as usual lexical CRF token
                    // and the second token of the line as feature
                    StringTokenizer st2 = new StringTokenizer(line, " \t");
                    // alternatively, use a grobid analyser
                    String text = null;
                    String text2 = null;
                    if (st2.hasMoreTokens())
                        text = st2.nextToken();
                    if (st2.hasMoreTokens())
                        text2 = st2.nextToken();

                    if (text == null)
                        continue;

                    // final sanitisation and filtering
                    text = text.replaceAll("[ \n]", "");
                    text = text.trim();

                    if ((text.length() == 0) || (TextUtilities.filterLine(line))) {
                        continue;
                    }

                    features.string = text;
                    features.secondString = text2;

                    features.firstPageBlock = firstPageBlock;
                    features.lastPageBlock = lastPageBlock;
                    features.lineLength = featureFactory.linearScaling(line.length(), maxLineLength, LINESCALE);

                    features.punctuationProfile = TextUtilities.punctuationProfile(line);

                    if (graphicBitmap) {
                        features.bitmapAround = true;
                    }
                    if (graphicVector) {
                        features.vectorAround = true;
                    }

                    features.lineStatus = null;
                    features.punctType = null;

                    if ((li == 0) ||
                        ((previousFeatures != null) && previousFeatures.blockStatus.equals("BLOCKEND"))) {
                        features.blockStatus = "BLOCKSTART";
                    } else if (li == lines.length - 1) {
                        features.blockStatus = "BLOCKEND";
                    } else if (features.blockStatus == null) {
                        features.blockStatus = "BLOCKIN";
                    }

                    if (newPage) {
                        features.pageStatus = "PAGESTART";
                        newPage = false;
                        if (previousFeatures != null)
                            previousFeatures.pageStatus = "PAGEEND";
                    } else {
                        features.pageStatus = "PAGEIN";
                        newPage = false;
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

                    Matcher m = featureFactory.isDigit.matcher(text);
                    if (m.find()) {
                        features.digit = "ALLDIGIT";
                    }

                    Matcher m2 = featureFactory.year.matcher(text);
                    if (m2.find()) {
                        features.year = true;
                    }

                    Matcher m3 = featureFactory.email.matcher(text);
                    if (m3.find()) {
                        features.email = true;
                    }

                    Matcher m4 = featureFactory.http.matcher(text);
                    if (m4.find()) {
                        features.http = true;
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

                    if (token.isBold())
                        features.bold = true;

                    if (token.isItalic())
                        features.italic = true;

                    // HERE horizontal information
                    // CENTERED
                    // LEFTAJUSTED
                    // CENTERED

                    if (features.capitalisation == null)
                        features.capitalisation = "NOCAPS";

                    if (features.digit == null)
                        features.digit = "NODIGIT";

                    features.relativeDocumentPosition = featureFactory
                        .linearScaling(nn, documentLength, NBBINS_POSITION);
                    features.relativePagePositionChar = featureFactory
                        .linearScaling(mm, pageLength, NBBINS_POSITION);
                    int pagePos = featureFactory
                        .linearScaling(coordinateLineY, pageHeight, NBBINS_POSITION);
                    if (pagePos > NBBINS_POSITION)
                        pagePos = NBBINS_POSITION;
                    features.relativePagePosition = pagePos;

                    if (spacingPreviousBlock != 0.0) {
                        features.spacingWithPreviousBlock = featureFactory
                            .linearScaling(spacingPreviousBlock - doc.getMinBlockSpacing(), doc.getMaxBlockSpacing() - doc.getMinBlockSpacing(), NBBINS_SPACE);
                    }

                    features.inMainArea = inPageMainArea;

                    if (density != -1.0) {
                        features.characterDensity = featureFactory
                            .linearScaling(density - doc.getMinCharacterDensity(), doc.getMaxCharacterDensity() - doc.getMinCharacterDensity(), NBBINS_DENSITY);
                    }

                    if (previousFeatures != null) {
                        String vector = previousFeatures.printVector();
                        featureMonograph.append(vector);
                    }
                    previousFeatures = features;
                }

                // lowest position of the block
                lowestPos = block.getY() + block.getHeight();

                // update page-level and document-level positions
                if (tokens != null) {
                    mm += tokens.size();
                    nn += tokens.size();
                }
            }
        }
        if (previousFeatures != null)
            featureMonograph.append(previousFeatures.printVector());

        return featureMonograph.toString();
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
        DocumentSource documentSource = null;
        Document doc = null;
        List<Block> blocks = null;
        Writer writer = null;
        StringBuilder builder = null;
        String lang = null;
        Language langID = null;
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
                // gather the features by blocks
                //String rawText = getAllBlocksFeatured(doc);

                // gather the features by lines
                String rawText = getAllLinesFeatured(doc);

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
            //String rawText = getAllBlocksFeatured(doc);
            // Let us now take care of the raw file
            writer = new OutputStreamWriter(new FileOutputStream(outputRawFile, false), "UTF-8");
            //writer.write(rawText);
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


    public static void close(DocumentSource source, boolean cleanImages, boolean cleanAnnotations, boolean cleanOutline) {
        if (source != null) {
            source.close(cleanImages, cleanAnnotations, cleanOutline);
        }
    }
}
