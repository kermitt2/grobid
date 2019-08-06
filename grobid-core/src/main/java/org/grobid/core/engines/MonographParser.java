package org.grobid.core.engines;

import eugfc.imageio.plugins.PNMRegistry;
import javafx.geometry.BoundingBox;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.document.BasicStructureBuilder;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentNode;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorMonograph;
import org.grobid.core.layout.*;
import org.grobid.core.lexicon.FastMatcher;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import static org.apache.commons.lang3.StringUtils.*;

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
     *   16 labels for this model:
     *       cover page (front of the book)
     *       title page (secondary title page)
     *       publisher page (publication information, including usually the copyrights info)
     *       summary (include executive summary)
     *       biography
     *       advertising (other works by the author/publisher)
     *       table of content
     *       table/list of figures
     *       preface (foreword)
     *       dedication (I dedicate this label to my family and my thesis director ;)
     *       unit (chapter or standalone article)
     *       reference (a full chapter of references, not to be confused with references attached to an article)
     *       annex
     *       index
     *       glossary (also abbreviations and acronyms)
     *       back cover page
     *       other
	 */

    private static final Logger LOGGER = LoggerFactory.getLogger(MonographParser.class);

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

    private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
    private FeatureFactory featureFactory = FeatureFactory.getInstance();

    private File tmpPath = null;

    /**
     * TODO some documentation...
     */
    public MonographParser() {
        super(GrobidModels.MONOGRAPH);
        tmpPath = GrobidProperties.getTempPath();
    }

    /**
     * Segment a PDF document into high level subdocuments.
     *
     * @param documentSource     document source
     * @return Document object with segmentation information
     */
    public Document processing(DocumentSource documentSource, GrobidAnalysisConfig config) {
        try {
            Document doc = new Document(documentSource);
            if (config.getAnalyzer() != null)
                doc.setAnalyzer(config.getAnalyzer());
            doc.addTokenizedDocument(config);
            doc = prepareDocument(doc);

            // if assets is true, the images are still there under directory pathXML+"_data"
            // we copy them to the assetPath directory

            /*ile assetFile = config.getPdfAssetPath();
            if (assetFile != null) {
                dealWithImages(documentSource, doc, assetFile, config);
            }*/

            return doc;
        } finally {
            // keep it clean when leaving...
            if (config.getPdfAssetPath() == null) {
                // remove the pdf2xml tmp file
                DocumentSource.close(documentSource, false, true, true);
            } else {
                // remove the pdf2xml tmp files, including the sub-directories
                DocumentSource.close(documentSource, true, true, true);
            }
        }
    }

    public Document prepareDocument(Document doc) {

        List<LayoutToken> tokenizations = doc.getTokenizations();
        if (tokenizations.size() > GrobidProperties.getPdfTokensMax()) {
            throw new GrobidException("The document has " + tokenizations.size() + " tokens, but the limit is " + GrobidProperties.getPdfTokensMax(),
                    GrobidExceptionStatus.TOO_MANY_TOKENS);
        }

        doc.produceStatistics();
        //String content = getAllLinesFeatured(doc);
        String content = getAllBlocksFeatured(doc);
        if (isNotEmpty(trim(content))) {
            String labelledResult = label(content);
            // set the different sections of the Document object
            doc = BasicStructureBuilder.generalResultSegmentation(doc, labelledResult, tokenizations);
        }
        return doc;
    }


    /**
     * Addition of the features at line level for the complete document.
     * <p/>
     * This is an alternative to the token level, where the unit for labeling is the line - so allowing faster
     * processing and involving less features.
     * Lexical features becomes line prefix and suffix, the feature text unit is the first 10 characters of the
     * line without space.
     * The dictionary flags are at line level (i.e. the line contains a name mention, a place mention, a year, etc.)
     * Regarding layout features: font, size and style are the one associated to the first token of the line.
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

        //boolean graphicVector = false;
        //boolean graphicBitmap = false;

        // list of textual patterns at the head and foot of pages which can be re-occur on several pages
        // (typically indicating a publisher foot or head notes)
        Map<String, Integer> patterns = new TreeMap<String, Integer>();
        Map<String, Boolean> firstTimePattern = new TreeMap<String, Boolean>();

        for (Page page : doc.getPages()) {
            // we just look at the two first and last blocks of the page
            if ((page.getBlocks() != null) && (page.getBlocks().size() > 0)) {
                for(int blockIndex=0; blockIndex < page.getBlocks().size(); blockIndex++) {
                    if ( (blockIndex < 2) || (blockIndex > page.getBlocks().size()-2)) {
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
                                        patterns.put(pattern, Integer.valueOf("1"));
                                        firstTimePattern.put(pattern, false);
                                    }
                                    else
                                        patterns.put(pattern, Integer.valueOf(nb+1));
                                }
                            }
                        }
                    }
                }
            }
        }

        String featuresAsString = getFeatureVectorsAsString(doc,
                patterns, firstTimePattern);

        return featuresAsString;
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

        //boolean graphicVector = false;
        //boolean graphicBitmap = false;

        // list of textual patterns at the head and foot of pages which can be re-occur on several pages
        // (typically indicating a publisher foot or head notes)
        Map<String, Integer> patterns = new TreeMap<String, Integer>();
        Map<String, Boolean> firstTimePattern = new TreeMap<String, Boolean>();

        for (Page page : doc.getPages()) {
            // we just look at the two first and last blocks of the page
            if ((page.getBlocks() != null) && (page.getBlocks().size() > 0)) {
                for(int blockIndex=0; blockIndex < page.getBlocks().size(); blockIndex++) {
                    if ( (blockIndex < 2) || (blockIndex > page.getBlocks().size()-2)) {
                        Block block = page.getBlocks().get(blockIndex);
                        String localText = block.getText();
                        if ((localText != null) && (localText.length() > 0)) {
                            String pattern = featureFactory.getPattern(localText);
                            if (pattern.length() > 8) {
                                Integer nb = patterns.get(pattern);
                                if (nb == null) {
                                    patterns.put(pattern, Integer.valueOf("1"));
                                    firstTimePattern.put(pattern, false);
                                }
                                else
                                    patterns.put(pattern, Integer.valueOf(nb+1));
                            }
                        }
                    }
                }
            }
        }
        String featuresAsString = getFeatureVectorsAsString(doc,
                patterns, firstTimePattern);

        return featuresAsString;
    }

    private String getFeatureVectorsAsString(Document doc, Map<String, Integer> patterns,
                                     Map<String, Boolean> firstTimePattern) {
        StringBuilder fulltext = new StringBuilder();
        int documentLength = doc.getDocumentLenghtChar();

        String currentFont = null;
        int currentFontSize = -1;

        boolean newPage;
        boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        int pageLength = 0; // length of the current page
        double pageHeight = 0.0;

        // vector for features
        FeaturesVectorMonograph features;
        FeaturesVectorMonograph previousFeatures = null;

        for (Page page : doc.getPages()) {
            pageHeight = page.getHeight();
            newPage = true;
            double spacingPreviousBlock = 0.0; // discretized
            double lowestPos = 0.0;
            pageLength = page.getPageLengthChar();
            org.grobid.core.layout.BoundingBox pageBoundingBox = page.getMainArea();
            mm = 0;
            //endPage = true;

            if ((page.getBlocks() == null) || (page.getBlocks().size() == 0))
                continue;

            for(int blockIndex=0; blockIndex < page.getBlocks().size(); blockIndex++) {
                Block block = page.getBlocks().get(blockIndex);
                /*if (start) {
                    newPage = true;
                    start = false;
                }*/
                boolean graphicVector = false;
                boolean graphicBitmap = false;

                boolean lastPageBlock = false;
                boolean firstPageBlock = false;
                if (blockIndex == page.getBlocks().size()-1) {
                    lastPageBlock = true;
                }

                if (blockIndex == 0) {
                    firstPageBlock = true;
                }

                //endblock = false;

                /*if (endPage) {
                    newPage = true;
                    mm = 0;
                }*/

                // check if we have a graphical object connected to the current block
                List<GraphicObject> localImages = Document.getConnectedGraphics(block, doc);
                if (localImages != null) {
                    for(GraphicObject localImage : localImages) {
                        if (localImage.getType() == GraphicObjectType.BITMAP)
                            graphicBitmap = true;
                        if (localImage.getType() == GraphicObjectType.VECTOR)
                            graphicVector = true;
                    }
                }

                if (lowestPos >  block.getY()) {
                    // we have a vertical shift, which can be due to a change of column or other particular layout formatting
                    spacingPreviousBlock = doc.getMaxBlockSpacing() / 5.0; // default
                } else
                    spacingPreviousBlock = block.getY() - lowestPos;

                String localText = block.getText();
                if (localText == null)
                    continue;

                // character density of the block
                double density = 0.0;
                if ( (block.getHeight() != 0.0) && (block.getWidth() != 0.0) &&
                     (block.getText() != null) && (!block.getText().contains("@PAGE")) &&
                     (!block.getText().contains("@IMAGE")) )
                    density = (double)block.getText().length() / (block.getHeight() * block.getWidth());

                // is the current block in the main area of the page or not?
                boolean inPageMainArea = true;
                org.grobid.core.layout.BoundingBox blockBoundingBox = org.grobid.core.layout.BoundingBox.fromPointAndDimensions(page.getNumber(),
                    block.getX(), block.getY(), block.getWidth(), block.getHeight());
                if (pageBoundingBox == null || (!pageBoundingBox.contains(blockBoundingBox) && !pageBoundingBox.intersect(blockBoundingBox)))
                    inPageMainArea = false;

                String[] lines = localText.split("[\\n\\r]");
    			// set the max length of the lines in the block, in number of characters
    			int maxLineLength = 0;
    			for(int p=0; p<lines.length; p++) {
    				if (lines[p].length() > maxLineLength)
    					maxLineLength = lines[p].length();
    			}
                List<LayoutToken> tokens = block.getTokens();
                if ((tokens == null) || (tokens.size() == 0)) {
                    continue;
                }
                for (int li = 0; li < lines.length; li++) {
                    String line = lines[li];
                    /*boolean firstPageBlock = false;
                    boolean lastPageBlock = false;

                    if (newPage)
                        firstPageBlock = true;
                    if (endPage)
                        lastPageBlock = true;
                    */

                    // for the layout information of the block, we take simply the first layout token
    				LayoutToken token = null;
    				if (tokens.size() > 0)
    					token = tokens.get(0);

    				double coordinateLineY = token.getY();

                    features = new FeaturesVectorMonograph();
                    features.token = token;
                    features.line = line;

                    if ( (blockIndex < 2) || (blockIndex > page.getBlocks().size()-2)) {
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

                    if ( (text.length() == 0) ||
//                            (text.equals("\n")) ||
//                            (text.equals("\r")) ||
//                            (text.equals("\n\r")) ||
                            (TextUtilities.filterLine(line))) {
                        continue;
                    }

                    features.string = text;
                    features.secondString = text2;

                    features.firstPageBlock = firstPageBlock;
                    features.lastPageBlock = lastPageBlock;
                    //features.lineLength = line.length() / LINESCALE;
                    features.lineLength = featureFactory
                            .linearScaling(line.length(), maxLineLength, LINESCALE);

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
                        //endblock = true;
                    } else if (features.blockStatus == null) {
                        features.blockStatus = "BLOCKIN";
                    }

                    if (newPage) {
                        features.pageStatus = "PAGESTART";
                        newPage = false;
                        //endPage = false;
                        if (previousFeatures != null)
                            previousFeatures.pageStatus = "PAGEEND";
                    } else {
                        features.pageStatus = "PAGEIN";
                        newPage = false;
                        //endPage = false;
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

                    // HERE horizontal information
                    // CENTERED
                    // LEFTAJUSTED
                    // CENTERED

                    if (features.capitalisation == null)
                        features.capitalisation = "NOCAPS";

                    if (features.digit == null)
                        features.digit = "NODIGIT";

                    //if (features.punctType == null)
                    //    features.punctType = "NOPUNCT";

                    features.relativeDocumentPosition = featureFactory
                            .linearScaling(nn, documentLength, NBBINS_POSITION);
//System.out.println(nn + " " + documentLength + " " + NBBINS_POSITION + " " + features.relativeDocumentPosition);
                    features.relativePagePositionChar = featureFactory
                            .linearScaling(mm, pageLength, NBBINS_POSITION);
//System.out.println(mm + " " + pageLength + " " + NBBINS_POSITION + " " + features.relativePagePositionChar);
    				int pagePos = featureFactory
                            .linearScaling(coordinateLineY, pageHeight, NBBINS_POSITION);
//System.out.println(coordinateLineY + " " + pageHeight + " " + NBBINS_POSITION + " " + pagePos);
    				if (pagePos > NBBINS_POSITION)
    					pagePos = NBBINS_POSITION;
                    features.relativePagePosition = pagePos;
//System.out.println(coordinateLineY + "\t" + pageHeight);

                    if (spacingPreviousBlock != 0.0) {
                        features.spacingWithPreviousBlock = featureFactory
                            .linearScaling(spacingPreviousBlock-doc.getMinBlockSpacing(), doc.getMaxBlockSpacing()-doc.getMinBlockSpacing(), NBBINS_SPACE);
                    }

                    features.inMainArea = inPageMainArea;

                    if (density != -1.0) {
                        features.characterDensity = featureFactory
                            .linearScaling(density-doc.getMinCharacterDensity(), doc.getMaxCharacterDensity()-doc.getMinCharacterDensity(), NBBINS_DENSITY);
//System.out.println((density-doc.getMinCharacterDensity()) + " " + (doc.getMaxCharacterDensity()-doc.getMinCharacterDensity()) + " " + NBBINS_DENSITY + " " + features.characterDensity);
                    }

                    if (previousFeatures != null) {
                        String vector = previousFeatures.printVector();
                        fulltext.append(vector);
                    }
                    previousFeatures = features;
                }

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
        }
        if (previousFeatures != null)
            fulltext.append(previousFeatures.printVector());

        return fulltext.toString();
    }


    /**
     * Process the specified pdf and format the result as training data for the monograph model.
     *
     * @param inputFile input PDF file
     * @param pathFullText path to raw monograph featured sequence
     * @param pathTEI path to TEI
     * @param id id
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

            File outputTEIFile = new File(pathTEI+"/"+pdfFileName.replace(".pdf", "training.monograph.tei.xml"));
            /* // commented out because it was making a test of the existence of a file before it was even created
               if (!outputTEIFile.exists()) {
                throw new GrobidResourceException("Cannot train for monograph, because directory '" +
                       pathTEI + "' is not valid.");
            }*/
            File outputRawFile = new File(pathRaw+"/"+pdfFileName.replace(".pdf", "training.monograph"));
            /*if (!outputRawFile.exists()) {
                throw new GrobidResourceException("Cannot train for monograph, because directory '" +
                       pathRaw + "' is not valid.");
            }*/

            documentSource = DocumentSource.fromPdf(inputFile, -1, -1, true, true, true);
            doc = new Document(documentSource);
            doc.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());

            if (doc.getBlocks() == null) {
                throw new Exception("PDF parsing resulted in empty content");
            }

            // TODO language identifier here on content text sample
            String lang = "fr";

            doc.produceStatistics();
            StringBuilder builder = new StringBuilder();
            builder.append("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
                "\"/>\n\t</teiHeader>\n\t<text xml:lang=\""+ lang + "\">\n");

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
            boolean tocExists = ( currentNode != null );
            if ( tocExists ) {
                builder.append("<div type=\"contents\">\n");
                currentNode.setAddress("*");
                stackTOC.push(currentNode);
                oldDepth = 0;
            }
            while ( stackTOC.size() > 0) {
                currentNode = stackTOC.pop();
                // at this point, the page at which the chapter/section,
                // referenced by current node, starts
                // is given by currentNode.getBoundingBox().getPage()
                gornString = currentNode.getAddress();
                if ( gornString != "*"){
                    currentDepth = (gornString.split("[.]")).length;
                    if ( currentDepth > oldDepth ) {
                        for ( int i=0; i < currentDepth ; i++ ) {
                            builder.append("\t");
                        }
                        builder.append("<list>\n");
                    }
                    while ( currentDepth < oldDepth ) {
                        for ( int i=0; i < oldDepth ; i++ ) {
                            builder.append("\t");
                        }
                        builder.append("</list>\n");
                        oldDepth--;
                    }
                    for ( int i=0; i < currentDepth; i++ ) {
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
                }
                else {
                    gornString += ".";
                }
                List<DocumentNode> children = currentNode.getChildren();
                if ( children != null ) {
                    int s = children.size();
                    while ( s > 0 ){
                        s = s - 1;
                        currentNode = children.get(s);
                        currentNode.setAddress(gornString+String.valueOf(s));
                        stackTOC.push(currentNode);
                    }
                }
            }
            if ( tocExists ) {
                while ( 0 < currentDepth ) {
                    for ( int i=0; i < currentDepth ; i++ ) {
                        builder.append("\t");
                    }
                    builder.append("</list>\n");
                    currentDepth--;
                }
                builder.append("</div>\n");
            }

            // So far we transcribed the table of contexts in the beginning of the text
            // Now we will tag it inside the text.
            if ( tocExists ) {
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
                //builder.append("DEBUGGING: The total number of tokens is "
                //                        + numberOfTokens
                //                        + "\n");
                while (stackTOC.size()>0 && tokenCtr < numberOfTokens) {
                    currentNode = stackTOC.pop();
                    // In order to avoid writing the "null" label from the
                    // outlineRoot into the TEI and also
                    // just in case the outline was ill-formed we search
                    // the first node (first wrt preorder traversal)
                    // such that the title kept in the node is non trivial
                    while ( currentNode.getLabel() == null ){
                        children = currentNode.getChildren();
                        if ( children != null ) {
                            int s = children.size();
                            while ( s > 0 ){
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
                                     caseSensitive ); // false
                    // builder.append("DEBUGGING: Loaded  " + nbTermsInTitle + " terms from the title " + currentNode.getLabel() + "\n"); //it should always be 1
                    results = matcher.matchLayoutToken(tokens,
                                     ignoreDelimiters, // true
                                     caseSensitive); // false
                    if (results.size()>0){//if some instance of the title is found
                        // The list results may contain more than one instance of the title we are looking for.
                        // We want the position of the last instance that is on the same page that the outline is pointing to.
                        // We search for the last one because the table of contents is usually before the contents.
                        int currentNodesPage = currentNode.getBoundingBox().getPage();
                        int index = results.size() - 1;
                        while (index >= 0 && results.get(index).start >= tokenCtr && tokens.get(results.get(index).start).getPage() > currentNodesPage){
                            index--;
                        }
                        if (index >= 0 && results.get(index).start >= tokenCtr && tokens.get(results.get(index).start).getPage() == currentNodesPage ){
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
                    while ( tokenCtr <= endTokenOffset && tokenCtr < numberOfTokens) {
                        if ( tokenCtr == startTokenOffset) { //if we are about to write the starting token of a title of a chapter/section
                            while ( nbOpenDivs >= depth ) { //if the new chapter is at a lower or equal level
                                // compared to the previously written chapter title
                                // then we need to close the previous chapters before writing the current one.
                                builder.append ( "</div>\n" );
                                nbOpenDivs--;
                            }
                            //then we write the section title opening tag
                            String currentTitleNormalized = StringUtils.normalizeSpace(UnicodeUtil.normaliseText(currentNode.getLabel())).toLowerCase();
                            String[] currentTitleSplit = currentTitleNormalized.split ( "[" + TextUtilities.SPACE + "]" );
                            String firstInTitle = currentTitleSplit [0];
                            String lastInTitle = currentTitleSplit [ currentTitleSplit.length - 1 ];
                            builder.append ( "<div n=\"" + currentNode.getAddress() + "\" type=\"");
                            if (firstInTitle == "partie" || lastInTitle == "partie" ) {
                                builder.append ( "part" );
                            }
                            else if (firstInTitle == "chapitre") {
                                builder.append ( "chapter" );
                            }
                            else if (firstInTitle == "bibliographie" || lastInTitle == "bibliographiques") {
                                builder.append ( "bibliogr" );
                            }
                            else
                            {    switch ( currentTitleNormalized ) {
                                    case "remerciements": builder.append ( "ack" );
                                    break;
                                    case "sommaire": builder.append ( "contents" );
                                    break;
                                    case "introduction": builder.append ( "preface" );
                                    break;
                                    case "prologue": builder.append ( "preface" );
                                    break;
                                    case "annexe": builder.append ( "appendix" );
                                    break;
                                    case "épilogue": builder.append ( "appendix" );
                                    break;
                                    case "appendices": builder.append ( "appendix" );
                                    break;
                                    case "index": builder.append ( "index" );
                                    break;
                                    case "abréviations": builder.append ( "glossary" );
                                    break;
                                    case "glossaire": builder.append ( "glossary" );
                                    break;
                                    //TODO add other cases as the data increase
                                    default: builder.append ( "chapter" );
                                }
                            }
                            builder.append ( "\">\n<head> " );
                            nbOpenDivs++;
                        }
                        builder.append ( TextUtilities.HTMLEncode ( tokens.get(tokenCtr).getText() ) );
                        if ( tokenCtr == endTokenOffset ) {
                            // We add the closing delimiters from the title, if needed
                            // At first, we skip by the first delimiter, if there is any
                            // (in French question mark should always preceded by a space, that's why for the first step we include space)
                            if ( TextUtilities.delimiters.indexOf(tokens.get(tokenCtr+1).getText()) != -1 ) {
                                builder.append( tokens.get(tokenCtr+1).getText());
                                tokenCtr++;
                            }
                            while ( TextUtilities.fullPunctuations.indexOf(tokens.get(tokenCtr+1).getText()) != -1 ) {
                                builder.append(tokens.get(tokenCtr+1).getText());
                                tokenCtr++;
                            }
                            builder.append(" </head>\n"); // we close the chapter title
                            // we add all the sections of the chapter to our stack
                            children = currentNode.getChildren();
                            if ( children != null ) {
                                int s = children.size();
                                while ( s > 0 ){
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
                while ( tokenCtr < numberOfTokens ) {
                    builder.append ( TextUtilities.HTMLEncode ( tokens.get ( tokenCtr ) .getText() ) );
                    tokenCtr++;
                }
                //then close the chapters that are still open
                while ( nbOpenDivs > 0 ){
                    builder.append ( "</div>\n" );
                    nbOpenDivs--;
                }
            } else
            {
                for(LayoutToken token : tokens ) {
                    builder.append ( TextUtilities.HTMLEncode ( token.getText ( ) ) );
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
}
