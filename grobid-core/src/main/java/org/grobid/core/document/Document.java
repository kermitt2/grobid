package org.grobid.core.document;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Figure;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorHeader;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.Cluster;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.GraphicObjectType;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.Page;
import org.grobid.core.layout.VectorGraphicBoxCalculator;
import org.grobid.core.sax.PDF2XMLSaxParser;
import org.grobid.core.utilities.BoundingBoxCalculator;
import org.grobid.core.utilities.ElementCounter;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.Utilities;
import org.grobid.core.utilities.matching.EntityMatcherException;
import org.grobid.core.utilities.matching.ReferenceMarkerMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for representing, processing and exchanging a document item.
 *
 * @author Patrice Lopez
 */

public class Document {

    private static final Logger LOGGER = LoggerFactory.getLogger(Document.class);
    public static final int MAX_FIG_BOX_DISTANCE = 70;
    private final DocumentSource documentSource;

    /**
     * Exit code got when pdf2xml took too much time and has been killed by pdf2xml_server.
     */
    private String pathXML = null; // XML representation of the current PDF file

    private String lang = null;

    // layout structure of the document
    private List<Page> pages = null;
    private List<Cluster> clusters = null;
    private List<Block> blocks = null;

    // not used anymore
    private List<Integer> blockHeaders = null;
    private List<Integer> blockFooters = null;
    private List<Integer> blockSectionTitles = null;
    private List<Integer> acknowledgementBlocks = null;
    private List<Integer> blockDocumentHeaders = null;
    private SortedSet<DocumentPiece> blockReferences = null;
    private List<Integer> blockTables = null;
    private List<Integer> blockFigures = null;
    private List<Integer> blockHeadTables = null;
    private List<Integer> blockHeadFigures = null;

    private FeatureFactory featureFactory = null;

    // map of tokens (e.g. <reference> or <footnote>) to document pieces
    private SortedSetMultimap<String, DocumentPiece> labeledBlocks;

    // original tokenization and tokens - in order to recreate the original
    // strings and spacing
    private List<LayoutToken> tokenizations = null;

    // list of bibliographical references with context
    private Map<String, BibDataSet> teiIdToBibDataSets = null;
    private List<BibDataSet> bibDataSets = null;

    // not used anymore
    private DocumentNode top = null;

    // header of the document - if extracted and processed
    private final BiblioItem resHeader = null;

    // full text as tructure TEI - if extracted and processed
    private String tei;

    private ReferenceMarkerMatcher referenceMarkerMatcher;

    // list of bitmaps and vector graphics of the document
    private List<GraphicObject> images = null;

    private Multimap<Integer, GraphicObject> imagesPerPage = LinkedListMultimap.create();

    // some statistics regarding the document - useful for generating the features
    private double maxCharacterDensity = 0.0;
    private double minCharacterDensity = 0.0;
    private double maxBlockSpacing = 0.0;
    private double minBlockSpacing = 0.0;
    private int documentLenghtChar = -1; // length here is expressed as number of characters

    // not used
    private int beginBody = -1;
    private int beginReferences = -1;

    private boolean titleMatchNum = false; // true if the section titles of the document are numbered

    // the magic DOI regular expression...
    static public final Pattern DOIPattern = Pattern
            .compile("(10\\.\\d{4,5}\\/[\\S]+[^;,.\\s])");
    private List<Figure> figures;
    private Predicate<GraphicObject> validGraphicObjectPredicate;

    public Document(DocumentSource documentSource) {
        top = new DocumentNode("top", "0");
        this.documentSource = documentSource;
        setPathXML(documentSource.getXmlFile());
    }

    public void setLanguage(String l) {
        lang = l;
    }

    public String getLanguage() {
        return lang;
    }

    public BiblioItem getResHeader() {
        return resHeader;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<BibDataSet> getBibDataSets() {
        return bibDataSets;
    }

    public void addBlock(Block b) {
        if (blocks == null)
            blocks = new ArrayList<Block>();
        blocks.add(b);
    }

    public List<GraphicObject> getImages() {
        return images;
    }

    /**
     * Set the path to the XML file generated by xml2pdf
     */
    private void setPathXML(File pathXML) {
        this.pathXML = pathXML.getAbsolutePath();
    }

    public List<LayoutToken> getTokenizations() {
        return tokenizations;
    }

    public int getDocumentLenghtChar() {
        return documentLenghtChar;
    }

    public double getMaxCharacterDensity() {
        return maxCharacterDensity;
    }

    public double getMinCharacterDensity() {
        return minCharacterDensity;
    }

    public double getMaxBlockSpacing() {
        return maxBlockSpacing;
    }

    public double getMinBlockSpacing() {
        return minBlockSpacing;
    }

    // to be removed
    public List<LayoutToken> getTokenizationsHeader() {
        List<LayoutToken> tokenizationsHeader = new ArrayList<LayoutToken>();
        for (Integer blocknum : blockDocumentHeaders) {
            Block blo = blocks.get(blocknum);
            /*int tokens = blo.getStartToken();
            int tokene = blo.getEndToken();
            for (int i = tokens; i < tokene; i++) {
                tokenizationsHeader.add(tokenizations.get(i));
            }*/
            List<LayoutToken> tokens = blo.getTokens();
            if ( (tokens == null) || (tokens.size() == 0) ) {
                continue;
            }
            else {
                for(LayoutToken token : tokens) {
                    tokenizationsHeader.add(token);
                }
            }
        }

        return tokenizationsHeader;
    }

    // to be removed
    public List<LayoutToken> getTokenizationsFulltext() {
        List<LayoutToken> tokenizationsFulltext = new ArrayList<LayoutToken>();
        for (Block blo : blocks) {
            int tokens = blo.getStartToken();
            int tokene = blo.getEndToken();
            for (int i = tokens; i < tokene; i++) {
                tokenizationsFulltext.add(tokenizations.get(i));
            }
        }

        return tokenizationsFulltext;
    }

    // to be removed
    public List<LayoutToken> getTokenizationsReferences() {
        List<LayoutToken> tokenizationsReferences = new ArrayList<LayoutToken>();

        for (DocumentPiece dp : blockReferences) {
            tokenizationsReferences.addAll(tokenizations.subList(dp.a.getTokenDocPos(), dp.b.getTokenDocPos()));
        }

        return tokenizationsReferences;
    }

    /**
     * Parser PDF2XML output representation and get the tokenized form of the document.
     *
     * @return list of features
     */
    public List<LayoutToken> addTokenizedDocument(GrobidAnalysisConfig config) {
        // The XML generated by pdf2xml might contains invalid UTF characters due to the "garbage-in" of the PDF,
        // which will result in a "fatal" parsing failure (the joy of XML!). The solution could be to prevent
        // having those characters in the input XML by cleaning it first

        images = new ArrayList<>();
        PDF2XMLSaxParser parser = new PDF2XMLSaxParser(this, images);


        tokenizations = null;

        File file = new File(pathXML);
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            // in = new XMLFilterFileInputStream(file); // -> to filter invalid XML characters
            // get a factory
            SAXParserFactory spf = SAXParserFactory.newInstance();
            // get a new instance of parser
            SAXParser p = spf.newSAXParser();
            p.parse(in, parser);
            tokenizations = parser.getTokenization();




            if (getBlocks() == null) {
                throw new GrobidException("PDF parsing resulted in empty content", GrobidExceptionStatus.NO_BLOCKS);
            }

            // calculating main area
            ElementCounter<Integer> leftEven = new ElementCounter<>();
            ElementCounter<Integer> rightEven = new ElementCounter<>();
            ElementCounter<Integer> leftOdd = new ElementCounter<>();
            ElementCounter<Integer> rightOdd = new ElementCounter<>();
            ElementCounter<Integer> top = new ElementCounter<>();
            ElementCounter<Integer> bottom = new ElementCounter<>();

            for (Block b : blocks) {
                BoundingBox box = BoundingBoxCalculator.calculateOneBox(b.getTokens());
                if (box != null) {
                    b.setBoundingBox(box);
                }

                //small blocks can indicate that it's page numbers, some journal header info, etc. No need in them
                if (b.getX() == 0 || b.getHeight() < 20 || b.getWidth() < 20 || b.getHeight() * b.getWidth() < 3000) {
                    continue;
                }

                if (b.getPageNumber() % 2 == 0) {
                    leftEven.i((int) b.getX());
                    rightEven.i((int) (b.getX() + b.getWidth()));
                } else {
                    leftOdd.i((int) b.getX());
                    rightOdd.i((int) (b.getX() + b.getWidth()));
                }

                top.i((int) b.getY());
                bottom.i((int) (b.getY() + b.getHeight()));
            }

            if (!leftEven.getCnts().isEmpty() && !leftOdd.getCnts().isEmpty()) {
                int pageEvenX = 0;
                int pageEvenWidth = 0;
                if (pages.size() > 1) {
                    pageEvenX = getCoordItem(leftEven, true);
                    // +1 due to rounding
                    pageEvenWidth = getCoordItem(rightEven, false) - pageEvenX + 1;
                }
                int pageOddX = getCoordItem(leftOdd, true);
                // +1 due to rounding
                int pageOddWidth = getCoordItem(rightOdd, false) - pageOddX + 1;
                int pageY = getCoordItem(top, true);
                int pageHeight = getCoordItem(bottom, false) - pageY + 1;
                for (Page page : pages) {
                    if (page.isEven()) {
                        page.setMainArea(BoundingBox.fromPointAndDimensions(page.getNumber(),
							pageEvenX, pageY, pageEvenWidth, pageHeight));
                    } else {
                        page.setMainArea(BoundingBox.fromPointAndDimensions(page.getNumber(),
							pageOddX, pageY, pageOddWidth, pageHeight));
                    }
                }
            } else {
                for (Page page : pages) {
                    page.setMainArea(BoundingBox.fromPointAndDimensions(page.getNumber(),
						0, 0, page.getWidth(), page.getHeight()));
                }
            }

            // calculating boxes for pages
            if (config.isProcessVectorGraphics()) {
                for (GraphicObject o : VectorGraphicBoxCalculator.calculate(this).values()) {
                    images.add(o);
                }
            }

            // cache images per page
            for (GraphicObject go : images) {
                // filtering out small figures that are likely to be logos and stuff
                if (go.getType() == GraphicObjectType.BITMAP && !isValidBitmapGraphicObject(go)) {
                    continue;
                }
                imagesPerPage.put(go.getPage(), go);
            }

            HashSet<Integer> keys = new HashSet<>(imagesPerPage.keySet());
            for (Integer pageNum : keys) {

                Collection<GraphicObject> elements = imagesPerPage.get(pageNum);
                if (elements.size() > 10) {
                    imagesPerPage.removeAll(pageNum);
                    Engine.getCntManager().i(Figure.Counters.TOO_MANY_FIGURES_PER_PAGE);
                } else {
                    ArrayList<GraphicObject> res = glueImagesIfNecessary(pageNum, Lists.newArrayList(elements));
                    if (res != null) {
                        imagesPerPage.removeAll(pageNum);
                        imagesPerPage.putAll(pageNum, res);
                    }
                }
            }


        } catch (GrobidException e) {
            throw e;
        } catch (Exception e) {
            throw new GrobidException("Cannot parse file: " + file, e, GrobidExceptionStatus.PARSING_ERROR);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error("Cannot close input stream", e);
                }

            }
        }
        // we filter out possible line numbering for review works
        // filterLineNumber();
        return tokenizations;
    }

    private ArrayList<GraphicObject> glueImagesIfNecessary(Integer pageNum, List<GraphicObject> graphicObjects ) {

        List<Pair<Integer, Integer>> toGlue = new ArrayList<>();
//        List<GraphicObject> cur = new ArrayList<>();

//        List<GraphicObject> graphicObjects = new ArrayList<>(objs);

        int start =0 , end = 0;
        for (int i = 1; i < graphicObjects.size(); i++) {
            GraphicObject prev = graphicObjects.get(i - 1);
            GraphicObject cur = graphicObjects.get(i);

            if (prev.getType() != GraphicObjectType.BITMAP || cur.getType() != GraphicObjectType.BITMAP) {
                if (start != end) {
                    toGlue.add(new Pair<>(start, end + 1));
                }
                start = i;
                end = start;

                continue;
            }

            if (Utilities.doubleEquals(prev.getBoundingBox().getWidth(), cur.getBoundingBox().getWidth(), 0.0001)
                    && Utilities.doubleEquals(prev.getBoundingBox().getY2(), cur.getBoundingBox().getY(), 0.0001)

                    ) {
                end++;
            } else {
                if (start != end) {
                    toGlue.add(new Pair<>(start, end + 1));
                }
                start = i;
                end = start;
            }

        }

        if (start != end) {
            toGlue.add(new Pair<>(start, end + 1));
        }


        if (toGlue.isEmpty()) {
            return null;
        }
        for (Pair<Integer, Integer> p : toGlue) {
            BoundingBox box = graphicObjects.get(p.a).getBoundingBox();
            for (int i = p.a + 1; i < p.b; i++) {
                box = box.boundBox(graphicObjects.get(i).getBoundingBox());
            }

            graphicObjects.set(p.a, new GraphicObject(box, GraphicObjectType.VECTOR_BOX));
            for (int i = p.a + 1; i < p.b; i++) {
                graphicObjects.set(i, null);
            }

        }


        validGraphicObjectPredicate = new Predicate<GraphicObject>() {
            @Override
            public boolean apply(GraphicObject graphicObject) {
                return graphicObject != null && isValidBitmapGraphicObject(graphicObject);
            }
        };
        return Lists.newArrayList(Iterables.filter(graphicObjects, validGraphicObjectPredicate));


    }


    private static int getCoordItem(ElementCounter<Integer> cnt, boolean getMin) {
        List<Map.Entry<Integer, Integer>> counts = cnt.getSortedCounts();
        int max = counts.get(0).getValue();

        int res = counts.get(0).getKey();
        for (Map.Entry<Integer, Integer> e : counts) {
            /*if (e.getValue() < max * 0.7) {
                break;
            }*/

            if (getMin) {
                if (e.getKey() < res) {
                    res = e.getKey();
                }
            } else {
                if (e.getKey() > res) {
                    res = e.getKey();
                }
            }
        }
        return res;
    }

    /**
     * Try to reconnect blocks cut because of layout constraints (new col., new
     * page, inserted figure, etc.)
     *
     * -> not used anymore
     *
     */
    /*public void reconnectBlocks() throws Exception {
        int i = 0;
        // List<Block> newBlocks = new ArrayList<Block>();
        boolean candidate = false;
        int candidateIndex = -1;
        for (Block block : blocks) {
            Integer ii = i;
            if ((!blockFooters.contains(ii))
                    && (!blockDocumentHeaders.contains(ii))
                    && (!blockHeaders.contains(ii))
                    && (!blockReferences.contains(ii))
                    && (!blockSectionTitles.contains(ii))
                    && (!blockFigures.contains(ii))
                    && (!blockTables.contains(ii))
                    && (!blockHeadFigures.contains(ii))
                    && (!blockHeadTables.contains(ii))) {
                String text = block.getText();

                if (text != null) {
                    text = text.trim();
                    if (text.length() > 0) {
                        // specific test if we have a new column
                        // TODO
                        // test if we have a special layout block
                        int innd = text.indexOf("@PAGE");
                        if (innd == -1)
                            innd = text.indexOf("@IMAGE");

                        if (innd == -1) {
                            // test if the block starts without upper case
                            if (text.length() > 2) {
                                char c1 = text.charAt(0);
                                char c2 = text.charAt(1);
                                if (Character.isLetter(c1)
                                        && Character.isLetter(c2)
                                        && !Character.isUpperCase(c1)
                                        && !Character.isUpperCase(c2)) {
                                    // this block is ok for merging with the
                                    // previous candidate
                                    if (candidate) {
                                        Block target = blocks.get(candidateIndex);
                                        // we simply move tokens
                                        List<LayoutToken> theTokens = block.getTokens();
                                        for (LayoutToken tok : theTokens) {
                                            target.addToken(tok);
                                        }
                                        target.setText(target.getText() + "\n"
                                                + block.getText());
                                        block.setText("");
                                        block.resetTokens();
                                        candidate = false;
                                    } else {
                                        candidate = false;
                                    }
                                } else {
                                    candidate = false;
                                }
                            } else {
                                candidate = false;
                            }

                            // test if the block ends "suddently"
                            if (text.length() > 2) {
                                // test the position of the last token, which should
                                // be close
                                // to the one of the block + width of the block
                                StringTokenizer st = new StringTokenizer(text, "\n");
                                int lineLength = 0;
                                int nbLines = 0;
                                int p = 0;
                                while (p < st.countTokens() - 1) {
                                    String line = st.nextToken();
                                    lineLength += line.length();
                                    nbLines++;
                                    p++;
                                }

                                if (st.countTokens() > 1) {
                                    lineLength = lineLength / nbLines;
                                    int finalLineLength = st.nextToken().length();

                                    if (Math.abs(finalLineLength - lineLength) < (lineLength / 3)) {

                                        char c1 = text.charAt(text.length() - 1);
                                        char c2 = text.charAt(text.length() - 2);
                                        if ((((c1 == '-') || (c1 == ')')) && Character
                                                .isLetter(c2))
                                                | (Character.isLetter(c1) && Character
                                                .isLetter(c2))) {
                                            // this block is a candidate for merging
                                            // with the next one
                                            candidate = true;
                                            candidateIndex = i;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            i++;
        }
    }*/

    /**
     * Add features in the header section
     * <p/>
     * -> should be moved to the header parser class!
     */
    public String getHeaderFeatured(boolean getHeader,
                                    boolean withRotation) {
        if (getHeader) {
            // String theHeader = getHeaderZFN(firstPass);
            String theHeader = getHeader();
            if ((theHeader == null) || (theHeader.trim().length() <= 1)) {
                theHeader = getHeaderLastHope();
            }
//System.out.println(theHeader);
        }
        featureFactory = FeatureFactory.getInstance();
        StringBuilder header = new StringBuilder();
        String currentFont = null;
        int currentFontSize = -1;

        // vector for features
        FeaturesVectorHeader features;
        boolean endblock;
        for (Integer blocknum : blockDocumentHeaders) {
            Block block = blocks.get(blocknum);
            boolean newline;
            boolean previousNewline = false;
            endblock = false;
            List<LayoutToken> tokens = block.getTokens();
            if (tokens == null)
                continue;
            int n = 0;
            while (n < tokens.size()) {
                LayoutToken token = tokens.get(n);
                features = new FeaturesVectorHeader();
                features.token = token;
                String text = token.getText();
                if (text == null) {
                    n++;
                    continue;
                }
                //text = text.trim();
                text = text.replace(" ", "").replace("\t", "").replace("\u00A0", "");
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
                    features.lineStatus = "LINESTART";
                    features.blockStatus = "BLOCKSTART";
                } else if (n == tokens.size() - 1) {
                    features.lineStatus = "LINEEND";
                    previousNewline = true;
                    features.blockStatus = "BLOCKEND";
                    endblock = true;
                } else {
                    // look ahead...
                    boolean endline = false;

                    int ii = 1;
                    boolean endloop = false;
                    while ((n + ii < tokens.size()) && (!endloop)) {
                        LayoutToken tok = tokens.get(n + ii);
                        if (tok != null) {
                            String toto = tok.getText();
                            if (toto != null) {
                                if (toto.equals("\n")) {
                                    endline = true;
                                    endloop = true;
                                } else {
                                    if ((toto.trim().length() != 0)
                                            && (!text.equals("\u00A0"))
                                            && (!(toto.contains("@IMAGE")))
                                            && (!(toto.contains("@PAGE")))
                                            && (!text.contains(".pbm"))
                                            && (!text.contains(".ppm"))
                                            && (!text.contains(".vec"))
                                            && (!text.contains(".png"))
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

        return header.toString();
    }

    // default bins for relative position
    private static final int nbBins = 12;

    /**
     * heuristics to get the header section...
     * -> it is now covered by the CRF segmentation model
     */
    public String getHeader() {
        //if (firstPass)
        //BasicStructureBuilder.firstPass(this);

        // try first to find the introduction in a safe way
        String tmpRes = getHeaderByIntroduction();
        if (tmpRes != null) {
            if (tmpRes.trim().length() > 0) {
                return tmpRes;
            }
        }

        // we apply a heuristics based on the size of first blocks
        String res = null;
        beginBody = -1;
        StringBuilder accumulated = new StringBuilder();
        int i = 0;
        int nbLarge = 0;
        boolean abstractCandidate = false;
        for (Block block : blocks) {
            String localText = block.getText();
            if ((localText == null) || (localText.startsWith("@"))) {
                accumulated.append("\n");
                continue;
            }
            localText = localText.trim();
            localText = localText.replace("  ", " ");

            Matcher ma0 = BasicStructureBuilder.abstract_.matcher(localText);
            if ((block.getNbTokens() > 60) || (ma0.find())) {
                if (!abstractCandidate) {
                    // first large block, it should be the abstract
                    abstractCandidate = true;
                } else if (beginBody == -1) {
                    // second large block, it should be the first paragraph of
                    // the body
                    beginBody = i;
                    for (int j = 0; j <= i + 1; j++) {
                        Integer inte = j;
                        if (blockDocumentHeaders == null)
                            blockDocumentHeaders = new ArrayList<Integer>();
                        if (!blockDocumentHeaders.contains(inte))
                            blockDocumentHeaders.add(inte);
                    }
                    res = accumulated.toString();
                    nbLarge = 1;
                } else if (block.getNbTokens() > 60) {
                    nbLarge++;
                    if (nbLarge > 5) {
                        return res;
                    }
                }
            } else {
                Matcher m = BasicStructureBuilder.introduction
                        .matcher(localText);
                if (abstractCandidate) {
                    if (m.find()) {
                        // we clearly found the begining of the body
                        beginBody = i;
                        for (int j = 0; j <= i; j++) {
                            Integer inte = j;
                            if (blockDocumentHeaders == null)
                                blockDocumentHeaders = new ArrayList<Integer>();
                            if (!blockDocumentHeaders.contains(inte)) {
                                blockDocumentHeaders.add(inte);
                            }
                        }
                        return accumulated.toString();
                    } else if (beginBody != -1) {
                        if (localText.startsWith("(1|I|A)\\.\\s")) {
                            beginBody = i;
                            for (int j = 0; j <= i; j++) {
                                Integer inte = j;
                                if (blockDocumentHeaders == null)
                                    blockDocumentHeaders = new ArrayList<Integer>();
                                if (!blockDocumentHeaders.contains(inte))
                                    blockDocumentHeaders.add(inte);
                            }
                            return accumulated.toString();
                        }
                    }
                } else {
                    if (m.find()) {
                        // we clearly found the begining of the body with the
                        // introduction section
                        beginBody = i;
                        for (int j = 0; j <= i; j++) {
                            Integer inte = j;
                            if (blockDocumentHeaders == null)
                                blockDocumentHeaders = new ArrayList<Integer>();
                            if (!blockDocumentHeaders.contains(inte))
                                blockDocumentHeaders.add(inte);
                        }
                        res = accumulated.toString();
                    }
                }
            }

            if ((i > 6) && (i > (blocks.size() * 0.6))) {
                if (beginBody != -1) {
                    return res;
                } else
                    return null;
            }

            accumulated.append(localText).append("\n");
            i++;
        }

        return res;
    }

    /**
     * We return the first page as header estimation... better than nothing when
     * nothing is not acceptable.
     * <p/>
     * -> now covered by the CRF segmentation model
     */
    public String getHeaderLastHope() {
        String res;
        StringBuilder accumulated = new StringBuilder();
        int i = 0;
        if ((pages == null) || (pages.size() == 0)) {
            return null;
        }
        for (Page page : pages) {
            if ((page.getBlocks() == null) || (page.getBlocks().size() == 0))
                continue;
            for (Block block : page.getBlocks()) {
                String localText = block.getText();
                if ((localText == null) || (localText.startsWith("@"))) {
                    accumulated.append("\n");
                    continue;
                }
                localText = localText.trim();
                localText = localText.replace("  ", " ");
                accumulated.append(localText);
                Integer inte = new Integer(i);
                if (blockDocumentHeaders == null)
                    blockDocumentHeaders = new ArrayList<Integer>();
                if (!blockDocumentHeaders.contains(inte))
                    blockDocumentHeaders.add(inte);
                i++;
            }
            beginBody = i;
            break;
        }

        return accumulated.toString();
    }

    /**
     * We try to match the introduction section in a safe way, and consider if
     * minimum requirements are met the blocks before this position as header.
     * <p/>
     * -> now covered by the CRF segmentation model
     */
    public String getHeaderByIntroduction() {
        String res;
        StringBuilder accumulated = new StringBuilder();
        int i = 0;
        for (Block block : blocks) {
            String localText = block.getText();
            if ((localText == null) || (localText.startsWith("@"))) {
                accumulated.append("\n");
                continue;
            }
            localText = localText.trim();

            Matcher m = BasicStructureBuilder.introductionStrict
                    .matcher(localText);
            if (m.find()) {
                accumulated.append(localText);
                beginBody = i;
                for (int j = 0; j < i + 1; j++) {
                    Integer inte = j;
                    if (blockDocumentHeaders == null)
                        blockDocumentHeaders = new ArrayList<Integer>();
                    if (!blockDocumentHeaders.contains(inte))
                        blockDocumentHeaders.add(inte);
                }
                res = accumulated.toString();

                return res;
            }

            accumulated.append(localText);
            i++;
        }

        return null;
    }

    /**
     * Return the text content of the body of the document. getHeader() and getReferences() must
     * have been called before.
     * <p/>
     * -> this should be removed at some point... it is only used now as default solution to determine
     * the language of an article with the language identifier
     */
    public String getBody() {
        StringBuilder accumulated = new StringBuilder();

        if (blockFooters == null)
            blockFooters = new ArrayList<Integer>();

        if (blockHeaders == null)
            blockHeaders = new ArrayList<Integer>();

        // Wiley specific pre-treatment
        // it looks very ad-hoc but actually it is
        int i = 0;
        boolean wiley = false;
        for (Block block : blocks) {
            Integer ii = i;

            if (blockDocumentHeaders.contains(ii)) {
                String localText = block.getText();
                if (localText != null) {
                    localText = localText.trim();
                    localText = localText.replace("  ", " ");
                    // we check if we have a Wiley publication - there is always
                    // the DOI around
                    // in a single block

                    if (localText.startsWith("DOI: 10.1002")) {
                        wiley = true;
                    }
                }
            }

            if ((!blockFooters.contains(ii))
                    && (!blockDocumentHeaders.contains(ii))
                    & (!blockHeaders.contains(ii)) && wiley) {
                String localText = block.getText();

                if (localText != null) {
                    localText = localText.trim();
                    localText = localText.replace("  ", " ");

                    // the keyword block needs to join the header section
                    if (localText.startsWith("Keywords: ")) {
                        // the block before the keyword block is part of the
                        // abstract and needs to be
                        // move up in the header section
                        blockDocumentHeaders.add(i - 1);
                        blockDocumentHeaders.add(ii);

                        break;
                    }
                }
            }
            i++;
        }

        i = 0;
        for (Block block : blocks) {
            Integer ii = i;
            // if ( (i >= beginBody) && (i < beginReferences) ) {

            if (blockFooters == null) {
                blockFooters = new ArrayList<Integer>();
            }
            if (blockDocumentHeaders == null) {
                blockDocumentHeaders = new ArrayList<Integer>();
            }
            if (blockHeaders == null) {
                blockHeaders = new ArrayList<Integer>();
            }
            if (blockReferences == null) {
                blockReferences = new TreeSet<DocumentPiece>();
            }

            if ((!blockFooters.contains(ii))
                    && (!blockDocumentHeaders.contains(ii))
                    && (!blockHeaders.contains(ii))
                    && (!blockReferences.contains(ii))) {
                String localText = block.getText();
                if (localText != null) {
                    localText = localText.trim();
                    if (localText.startsWith("@BULLET")) {
                        localText = localText.replace("@BULLET", " • ");
                    }
                    if (localText.startsWith("@IMAGE")) {
                        localText = "";
                    }

                    if (localText.length() > 0) {
                        if (featureFactory == null) {
                            featureFactory = FeatureFactory.getInstance();
                            // featureFactory = new FeatureFactory();
                        }
                        localText = TextUtilities.dehyphenize(localText);
                        accumulated.append(localText).append("\n");
                    }
                }
            }
            i++;
        }
        return accumulated.toString();
    }

    /**
     * Return all blocks without markers.
     * <p/>
     * Ignore the toIgnore1 th blocks and the blocks after toIgnore2 (included)
     */
    public String getAllBlocksClean(int toIgnore1, int toIgnore2) {
        StringBuilder accumulated = new StringBuilder();
        if (toIgnore2 == -1)
            toIgnore2 = blocks.size() + 1;
        int i = 0;
        if (blocks != null) {
            for (Block block : blocks) {
                if ((i >= toIgnore1) && (i < toIgnore2)) {
                    accumulated.append(block.getText()).append("\n");
                }
                i++;
            }
        }
        return accumulated.toString();
    }

    /*
     * Try to match a DOI in the first page, independently from any preliminar
     * segmentation. This can be useful for improving the chance to find a DOI
     * in headers or footnotes.
     */
    public List<String> getDOIMatches() {
        List<String> results = new ArrayList<String>();
        for (Block block : blocks) {
            String localText = block.getText();
            if (localText != null) {
                localText = localText.trim();
                if (localText.contains("@PAGE")) {
                    break;
                } else {
                    Matcher DOIMatcher = DOIPattern.matcher(localText);
                    while (DOIMatcher.find()) {
                        String theDOI = DOIMatcher.group();
                        if (!results.contains(theDOI)) {
                            results.add(theDOI);
                        }
                    }
                }
            }
        }
        return results;
    }

    public String getTei() {
        return tei;
    }

    public void setTei(String tei) {
        this.tei = tei;
    }

    /*public List<Integer> getBlockHeaders() {
        return blockHeaders;
    }

    public List<Integer> getBlockFooters() {
        return blockFooters;
    }

    public List<Integer> getBlockSectionTitles() {
        return blockSectionTitles;
    }

    public List<Integer> getAcknowledgementBlocks() {
        return acknowledgementBlocks;
    }*/

    public List<Integer> getBlockDocumentHeaders() {
        return blockDocumentHeaders;
    }

    /*public SortedSet<DocumentPiece> getBlockReferences() {
        return blockReferences;
    }

    public List<Integer> getBlockTables() {
        return blockTables;
    }

    public List<Integer> getBlockFigures() {
        return blockFigures;
    }

    public List<Integer> getBlockHeadTables() {
        return blockHeadTables;
    }

    public List<Integer> getBlockHeadFigures() {
        return blockHeadFigures;
    }
	*/

    public DocumentNode getTop() {
        return top;
    }

    public void setTop(DocumentNode top) {
        this.top = top;
    }

    public boolean isTitleMatchNum() {
        return titleMatchNum;
    }

    public void setTitleMatchNum(boolean titleMatchNum) {
        this.titleMatchNum = titleMatchNum;
    }

    public List<Page> getPages() {
        return pages;
    }

    // starting from 1
    public Page getPage(int num) {
        return pages.get(num - 1);
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setBlockHeaders(List<Integer> blockHeaders) {
        this.blockHeaders = blockHeaders;
    }

    public void setBlockFooters(List<Integer> blockFooters) {
        this.blockFooters = blockFooters;
    }

    public void setBlockSectionTitles(List<Integer> blockSectionTitles) {
        this.blockSectionTitles = blockSectionTitles;
    }

    public void setAcknowledgementBlocks(List<Integer> acknowledgementBlocks) {
        this.acknowledgementBlocks = acknowledgementBlocks;
    }

    public void setBlockDocumentHeaders(List<Integer> blockDocumentHeaders) {
        this.blockDocumentHeaders = blockDocumentHeaders;
    }

    public void setBlockReferences(SortedSet<DocumentPiece> blockReferences) {
        this.blockReferences = blockReferences;
    }

    public void setBlockTables(List<Integer> blockTables) {
        this.blockTables = blockTables;
    }

    public void setBlockFigures(List<Integer> blockFigures) {
        this.blockFigures = blockFigures;
    }

    public void setBlockHeadTables(List<Integer> blockHeadTables) {
        this.blockHeadTables = blockHeadTables;
    }

    public void setBlockHeadFigures(List<Integer> blockHeadFigures) {
        this.blockHeadFigures = blockHeadFigures;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public void addPage(Page page) {
        if (pages == null)
            pages = new ArrayList<Page>();
        pages.add(page);
    }

    public void setBibDataSets(List<BibDataSet> bibDataSets) {
        this.bibDataSets = bibDataSets;
        int cnt = 0;
        for (BibDataSet bds : bibDataSets) {
            bds.getResBib().setOrdinal(cnt++);
        }
    }

    public synchronized ReferenceMarkerMatcher getReferenceMarkerMatcher() throws EntityMatcherException {
        if (referenceMarkerMatcher == null) {
            referenceMarkerMatcher = new ReferenceMarkerMatcher(bibDataSets, Engine.getCntManager());
        }
        return referenceMarkerMatcher;
    }

    // when calling this method, the tei ids already should be in BibDataSets.BiblioItem
    public void calculateTeiIdToBibDataSets() {
        if (bibDataSets == null) {
            return;
        }

        teiIdToBibDataSets = new HashMap<String, BibDataSet>(bibDataSets.size());
        for (BibDataSet bds : bibDataSets) {
            if (bds.getResBib() != null && bds.getResBib().getTeiId() != null) {
                teiIdToBibDataSets.put(bds.getResBib().getTeiId(), bds);
            }
        }
    }

    public SortedSetMultimap<String, DocumentPiece> getLabeledBlocks() {
        return labeledBlocks;
    }

    public void setLabeledBlocks(SortedSetMultimap<String, DocumentPiece> labeledBlocks) {
        this.labeledBlocks = labeledBlocks;
    }

    //helper
    public List<LayoutToken> getDocumentPieceTokenization(DocumentPiece dp) {
        return tokenizations.subList(dp.a.getTokenDocPos(), dp.b.getTokenDocPos() + 1);
    }

    public String getDocumentPieceText(DocumentPiece dp) {
        return Joiner.on("").join(getDocumentPieceTokenization(dp));
    }

    public String getDocumentPieceText(SortedSet<DocumentPiece> dps) {
        return Joiner.on("\n").join(Iterables.transform(dps, new Function<DocumentPiece, Object>() {
            @Override
            public String apply(DocumentPiece documentPiece) {
                return getDocumentPieceText(documentPiece);
            }
        }));
    }

    public SortedSet<DocumentPiece> getDocumentPart(SegmentationLabel segmentationLabel) {
        if (labeledBlocks == null) {
            LOGGER.debug("labeledBlocks is null");
            return null;
        }
        if (segmentationLabel.getLabel() == null) {
            System.out.println("segmentationLabel.getLabel()  is null");
        }
        return labeledBlocks.get(segmentationLabel.getLabel());
    }

    public String getDocumentPartText(SegmentationLabel segmentationLabel) {
        SortedSet<DocumentPiece> pieces = getDocumentPart(segmentationLabel);
        if (pieces == null) {
            return null;
        } else {
            return getDocumentPieceText(getDocumentPart(segmentationLabel));
        }
    }

    public BibDataSet getBibDataSetByTeiId(String teiId) {
        return teiIdToBibDataSets.get(teiId);
    }

    private static double MIN_DISTANCE = 100.0;

    /**
     * Return the list of graphical object touching the given block.
     */
    public static List<GraphicObject> getConnectedGraphics(Block block, Document doc) {
        List<GraphicObject> images = null;
        for (GraphicObject image : doc.getImages()) {
            if (block.getPageNumber() != image.getPage())
                continue;
            if ( ( (Math.abs((image.getY()+image.getHeight()) - block.getY()) < MIN_DISTANCE) ||
                   (Math.abs(image.getY() - (block.getY()+block.getHeight())) < MIN_DISTANCE) ) //||
                 //( (Math.abs((image.x+image.getWidth()) - block.getX()) < MIN_DISTANCE) ||
                 //  (Math.abs(image.x - (block.getX()+block.getWidth())) < MIN_DISTANCE) )
                 ) {
                // the image is at a distance of at least MIN_DISTANCE from one border 
                // of the block on the vertical/horizontal axis
                if (images == null)
                    images = new ArrayList<GraphicObject>();
                images.add(image);
            }
        }

        return images;
    }

    public void assignGraphicObjectsToFigures() {
        Multimap<Integer, Figure> figureMap = HashMultimap.create();


        for (Figure f : figures) {
            figureMap.put(f.getPage(), f);
        }

        for (Integer pageNum : figureMap.keySet()) {
            List<Figure> pageFigures = new ArrayList<>();
            for (Figure f : figureMap.get(pageNum)) {


                List<LayoutToken> realCaptionTokens = getFigureLayoutTokens(f);
                if (realCaptionTokens != null && !realCaptionTokens.isEmpty()) {
                    f.setLayoutTokens(realCaptionTokens);
                    f.setTextArea(BoundingBoxCalculator.calculate(realCaptionTokens));
                    f.setCaption(new StringBuilder(TextUtilities.dehyphenize(LayoutTokensUtil.toText(realCaptionTokens))));
                    pageFigures.add(f);
                }

                //too simplictic
//                for (Integer blockPtr : f.getBlockPtrs()) {
//                    Block figBlock = getBlocks().get(blockPtr);
//                    if (LayoutTokensUtil.toText(figBlock.getTokens()).trim().toLowerCase().startsWith("fig")) {
//                        f.setLayoutTokens(figBlock.getTokens());
//                        f.setCaption(new StringBuilder(TextUtilities.dehyphenize(LayoutTokensUtil.toText(figBlock.getTokens()))));
//                        pageFigures.add(f);
//                        break;
//                    }
//                }
            }

            if (pageFigures.isEmpty()) {
                continue;
            }

            ArrayList<GraphicObject> it = Lists.newArrayList(Iterables.filter(imagesPerPage.get(pageNum), Figure.GRAPHIC_OBJECT_PREDICATE));

            List<GraphicObject> vectorBoxGraphicObjects = Lists.newArrayList(Iterables.filter(imagesPerPage.get(pageNum), Figure.VECTOR_BOX_GRAPHIC_OBJECT_PREDICATE));

            List<GraphicObject> graphicObjects = new ArrayList<>();

            l:
            for (GraphicObject bgo : it) {
                for (GraphicObject vgo : vectorBoxGraphicObjects) {
                    if (bgo.getBoundingBox().intersect(vgo.getBoundingBox())) {
                        continue l;
                    }
                }
                graphicObjects.add(bgo);
            }

            graphicObjects.addAll(vectorBoxGraphicObjects);


            if (vectorBoxGraphicObjects.isEmpty()) {
                for (Figure figure : pageFigures) {
                    List<LayoutToken> tokens = figure.getLayoutTokens();
                    final BoundingBox figureBox =
                            BoundingBoxCalculator.calculateOneBox(tokens, true);

                    double minDist = MAX_FIG_BOX_DISTANCE * 100;

                    GraphicObject bestGo = null;
                    if (figureBox != null) {
                        for (GraphicObject go : graphicObjects) {
                            // if it's not a bitmap, if it was used or the caption in the figure view
                            if (go.isUsed() || go.getBoundingBox().contains(figureBox)) {
                                continue;
                            }

                            if (!isValidBitmapGraphicObject(go)) {
                                continue;
                            }

                            double dist = figureBox.distanceTo(go.getBoundingBox());
                            if (dist > MAX_FIG_BOX_DISTANCE) {
                                continue;
                            }

                            if (dist < minDist) {
                                minDist = dist;
                                bestGo = go;
                            }
                        }
                    }

                    if (bestGo != null) {
                        bestGo.setUsed(true);
                        figure.setGraphicObjects(Lists.newArrayList(bestGo));
                    }

                }
            } else {
                if (pageFigures.size() != graphicObjects.size()) {
                    Engine.getCntManager().i(Figure.Counters.SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS);
                    continue;
                }

                for (Figure figure : pageFigures) {
                    List<LayoutToken> tokens = figure.getLayoutTokens();
                    final BoundingBox figureBox =
                            BoundingBoxCalculator.calculateOneBox(tokens, true);

                    double minDist = MAX_FIG_BOX_DISTANCE * 100;

                    GraphicObject bestGo = null;
                    if (figureBox != null) {
                        for (GraphicObject go : graphicObjects) {
                            if (go.isUsed()) {
                                continue;
                            }

                            BoundingBox goBox = go.getBoundingBox();

                            if (!getPage(goBox.getPage()).getMainArea().contains(goBox) && go.getWidth() * go.getHeight() < 10000) {
                                continue;
                            }

                            if (go.getType() == GraphicObjectType.BITMAP && !isValidBitmapGraphicObject(go)) {
                                continue;
                            }

                            double dist = figureBox.distanceTo(goBox);
                            if (dist > MAX_FIG_BOX_DISTANCE) {
                                continue;
                            }

                            if (dist < minDist) {
                                minDist = dist;
                                bestGo = go;
                            }
                        }
                    }

                    if (bestGo != null) {
                        bestGo.setUsed(true);
                        // when vector box overlaps the caption, we need to cut that piece from vector graphics
                        if (bestGo.getType() == GraphicObjectType.VECTOR_BOX) {
                            recalculateVectorBoxCoords(figure, bestGo);
                        }
                        figure.setGraphicObjects(Lists.newArrayList(bestGo));
                    }

                }

            }

        }

    }

    private boolean isValidBitmapGraphicObject(GraphicObject go) {
        if (go.getWidth() * go.getHeight() < 1000) {
            return false;
        }

        if (go.getWidth() < 50) {
            return false;
        }

        if (go.getHeight() < 50) {
            return false;
        }

        if (!getPage(go.getBoundingBox().getPage()).getMainArea().contains(go.getBoundingBox()) && go.getWidth() * go.getHeight() < 10000) {
            return false;
        }

        return true;
    }

    // graphic boxes could overlap captions, we need to cut this from a vector box
    private void recalculateVectorBoxCoords(Figure f, GraphicObject g) {

        //TODO: make it robust - now super simplistic
        BoundingBox fb = BoundingBoxCalculator.calculateOneBox(f.getLayoutTokens(), true);
        BoundingBox gb = g.getBoundingBox();

        if (fb.intersect(gb)) {
            if (gb.getY() < fb.getY() - 5) {
                g.setBoundingBox(BoundingBox.fromTwoPoints(gb.getPage(), gb.getX(), gb.getY(), gb.getX2(), fb.getY() - 5));
            }
        }

    }

    private List<LayoutToken> getFigureLayoutTokens(Figure f) {
        List<LayoutToken> result = new ArrayList<>();
        int i = 0;
        Iterator<Integer> it = f.getBlockPtrs().iterator();

        while (it.hasNext()) {
            Integer blockPtr = it.next();

            Block figBlock = getBlocks().get(blockPtr);
            if (LayoutTokensUtil.toText(figBlock.getTokens()).trim().toLowerCase().startsWith("fig")) {
                result.addAll(figBlock.getTokens());

                while (it.hasNext()) {
                    BoundingBox prevBlock = BoundingBox.fromPointAndDimensions(figBlock.getPageNumber(), figBlock.getX(), figBlock.getY(), figBlock.getWidth(), figBlock.getHeight());
                    blockPtr = it.next();
                    Block b = getBlocks().get(blockPtr);
                    if (BoundingBox.fromPointAndDimensions(b.getPageNumber(), b.getX(), b.getY(), b.getWidth(), b.getHeight()).distanceTo(prevBlock) < 15) {
                        result.addAll(b.getTokens());
                        figBlock = b;
                    } else {
                        break;
                    }
                }
                break;
            }
        }


        return result;
    }

    public void setConnectedGraphics2(Figure figure) {

        //TODO: improve - make figures clustering on the page (take all images and captions into account)
        List<LayoutToken> tokens = figure.getLayoutTokens();

        figure.setTextArea(BoundingBoxCalculator.calculate(tokens));

//        if (LayoutTokensUtil.tooFarAwayVertically(figure.getTextArea(), 100)) {
//            return;
//        }

        final BoundingBox figureBox =
                BoundingBoxCalculator.calculateOneBox(tokens, true);

        double minDist = MAX_FIG_BOX_DISTANCE * 100;


        GraphicObject bestGo = null;

        if (figureBox != null) {
            for (GraphicObject go : imagesPerPage.get(figure.getPage())) {
                if (go.getType() != GraphicObjectType.BITMAP || go.isUsed()) {
                    continue;
                }

                BoundingBox goBox =
                        BoundingBox.fromPointAndDimensions(go.getPage(), go.getX(), go.getY(),
                                go.getWidth(), go.getHeight());

                if (!getPage(goBox.getPage()).getMainArea().contains(goBox)) {
                    continue;
                }

                double dist = figureBox.distanceTo(goBox);
                if (dist > MAX_FIG_BOX_DISTANCE) {
                    continue;
                }

                if (dist < minDist) {
                    minDist = dist;
                    bestGo = go;
                }
            }
        }

        if (bestGo != null) {
            bestGo.setUsed(true);
            figure.setGraphicObjects(Lists.newArrayList(bestGo));
        }
    }

    public static void setConnectedGraphics(Figure figure,
                                            List<LayoutToken> tokenizations,
                                            Document doc) {
        try {
            List<GraphicObject> localImages = null;
            // set the intial figure area based on its layout tokens
            LayoutToken startToken = figure.getStartToken();
            LayoutToken endToken = figure.getEndToken();
            int start = figure.getStart();
            int end = figure.getEnd();

            double maxRight = 0.0; // right border of the figure
            double maxLeft = 10000.0; // left border of the figure
            double maxUp = 10000.0; // upper border of the figure
            double maxDown = 0.0; // bottom border of the figure
            for (int i = start; i <= end; i++) {
                LayoutToken current = tokenizations.get(i);
                if ((figure.getPage() == -1) && (current.getPage() != -1))
                    figure.setPage(current.getPage());
                if ((current.x >= 0.0) && (current.x < maxLeft))
                    maxLeft = current.x;
                if ((current.y >= 0.0) && (current.y < maxUp))
                    maxUp = current.y;
                if ((current.x >= 0.0) && (current.x + current.width > maxRight))
                    maxRight = current.x + current.width;
                if ((current.y >= 0.0) && (current.y + current.height > maxDown))
                    maxDown = current.y + current.height;
            }

            figure.setX(maxLeft);
            figure.setY(maxUp);
            figure.setWidth(maxRight - maxLeft);
            figure.setHeight(maxDown - maxUp);

            // attach connected graphics based on estimated figure area
            for (GraphicObject image : doc.getImages()) {
                if (image.getType() == GraphicObjectType.VECTOR)
                    continue;
                if (figure.getPage() != image.getPage())
                    continue;
//System.out.println(image.toString());
                if (((Math.abs((image.getY() + image.getHeight()) - figure.getY()) < MIN_DISTANCE) ||
                        (Math.abs(image.getY() - (figure.getY() + figure.getHeight())) < MIN_DISTANCE)) //||
                    //( (Math.abs((image.x+image.width) - figure.getX()) < MIN_DISTANCE) ||
                    //(Math.abs(image.x - (figure.getX()+figure.getWidth())) < MIN_DISTANCE) )
                        ) {
                    // the image is at a distance of at least MIN_DISTANCE from one border 
                    // of the block on the vertical/horizontal axis
                    if (localImages == null)
                        localImages = new ArrayList<GraphicObject>();
                    localImages.add(image);
                }
            }

            // re-evaluate figure area with connected graphics
            if (localImages != null) {
                for (GraphicObject image : localImages) {
                    if (image.getX() < maxLeft)
                        maxLeft = image.getX();
                    if (image.getY() < maxUp)
                        maxUp = image.getY();
                    if (image.getX() + image.getWidth() > maxRight)
                        maxRight = image.getX() + image.getWidth();
                    if (image.getY() + image.getHeight() > maxDown)
                        maxDown = image.getY() + image.getHeight();
                }
            }

            figure.setGraphicObjects(localImages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void produceStatistics() {
        // document lenght in characters
        // we calculate current document length and intialize the body tokenization structure
        for (Block block : blocks) {
            List<LayoutToken> tokens = block.getTokens();
            if (tokens == null)
                continue;
            documentLenghtChar += tokens.size();
        }

        // block spacing
        maxBlockSpacing = 0.0;
        minBlockSpacing = 10000.0;
        Double previousBlockBottom = 0.0;
        for (Page page : pages) {
            int pageLength = 0;
            if ((page.getBlocks() != null) && (page.getBlocks().size() > 0)) {
                for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                    Block block = page.getBlocks().get(blockIndex);
                    if ((blockIndex != 0) && (previousBlockBottom > 0.0)) {
                        double spacing = block.getY() - previousBlockBottom;
                        if ((spacing > 0.0) && (spacing < page.getHeight())) {
                            if (spacing > maxBlockSpacing)
                                maxBlockSpacing = spacing;
                            else if (spacing < minBlockSpacing)
                                minBlockSpacing = spacing;
                        }
                    }
                    previousBlockBottom = block.getY() + block.getHeight();
                    if (block.getTokens() != null)
                        pageLength += block.getTokens().size();
                }
            }
            page.setPageLengthChar(pageLength);
        }

        // character density is given by the number of characters in the block divided by the block's surface
        maxCharacterDensity = 0.0;
        minCharacterDensity = 1000000.0;
        for (Block block : blocks) {
            if ((block.getHeight() == 0.0) || (block.getWidth() == 0.0))
                continue;
            String text = block.getText();
            if ((text != null) && (!text.contains("@PAGE")) && (!text.contains("@IMAGE"))) {
                double surface = block.getWidth() * block.getHeight();
                
                /*System.out.println("block.width: " + block.width);
                System.out.println("block.height: " + block.height);
                System.out.println("surface: " + surface);
                System.out.println("text length: " + text.length());
                System.out.println("text: " + text + "\n");*/

                double density = ((double) text.length()) / surface;
                if (density < minCharacterDensity)
                    minCharacterDensity = density;
                if (density > maxCharacterDensity)
                    maxCharacterDensity = density;
            }
        }

        /*System.out.println("documentLenghtChar: " + documentLenghtChar);
        System.out.println("maxBlockSpacing: " + maxBlockSpacing);
        System.out.println("minBlockSpacing: " + minBlockSpacing);
        System.out.println("maxCharacterDensity: " + maxCharacterDensity);
        System.out.println("minCharacterDensity: " + minCharacterDensity);*/
    }

    public DocumentSource getDocumentSource() {
        return documentSource;
    }

    public void setFigures(List<Figure> figures) {
        this.figures = figures;
    }

    public List<Figure> getFigures() {
        return figures;
    }
}