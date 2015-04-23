package org.grobid.core.document;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.SortedSetMultimap;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorHeader;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.Cluster;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.sax.PDF2XMLSaxParser;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for representing, processing and exchanging a document item.
 *
 * @author Patrice Lopez
 */

public class Document {

    private static final Logger LOGGER = LoggerFactory.getLogger(Document.class);

    /**
     * Exit code got when pdf2xml took too much time and has been killed by pdf2xml_server.
     */

//    private String path = null; // path where the pdf file is stored

    private String pathXML = null; // XML representation of the current PDF file

    private int beginBody = -1;
    private int beginReferences = -1;

    private boolean titleMatchNum = false; // true if the section titles of the document are numbered
    private String lang = null;

    private List<Block> blocks = null;
    private List<Cluster> clusters = null;

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
    private List<String> tokenizations = null;

    // list of bibliographical references with context

    private Map<String, BibDataSet> teiIdToBibDataSets = null;
    private List<BibDataSet> bibDataSets = null;

    private DocumentNode top = null;

    private final BiblioItem resHeader = null;

    private String tei;

    public Document(DocumentSource documentSource) {
        top = new DocumentNode("top", "0");
        setPathXML(documentSource.getXmlFile());
    }

//    public Document(String pdfPath, String repositPath) {
////        path = pdfPath;
//        top = new DocumentNode("top", "0");
//    }

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

//    public void setPdf(String pdfPath) {
//        path = pdfPath;
//    }

    private void setPathXML(File pathXML) {
        this.pathXML = pathXML.getAbsolutePath();
    }

    public List<String> getTokenizations() {
        return tokenizations;
    }

//    private List<LayoutToken> getLayoutTokens(DocumentPiece dp) {
//        List<LayoutToken> toks
//        if (dp.a.getBlockPtr() == dp.b.getBlockPtr()) {
//            getBlocks().get(dp.a.getBlockPtr()).getTokens().subList(dp.a.getTokenPtr(), dp.b.getTokenPtr() + 1);
//        } else {
//
//        }
//        getBlocks().get(dp.a.getBlockPtr()).getTokens().subList(dp.get)
//    }


    public List<String> getTokenizationsHeader() {
        List<String> tokenizationsHeader = new ArrayList<String>();
        for (Integer blocknum : blockDocumentHeaders) {
            Block blo = blocks.get(blocknum);
            int tokens = blo.getStartToken();
            int tokene = blo.getEndToken();
            for (int i = tokens; i < tokene; i++) {
                tokenizationsHeader.add(tokenizations.get(i));
            }
        }

        return tokenizationsHeader;
    }

    public List<String> getTokenizationsFulltext() {
        List<String> tokenizationsFulltext = new ArrayList<String>();
        for (Block blo : blocks) {
            int tokens = blo.getStartToken();
            int tokene = blo.getEndToken();
            for (int i = tokens; i < tokene; i++) {
                tokenizationsFulltext.add(tokenizations.get(i));
            }
        }

        return tokenizationsFulltext;
    }

    public List<String> getTokenizationsReferences() {
        List<String> tokenizationsReferences = new ArrayList<String>();

        for (DocumentPiece dp : blockReferences) {
            tokenizationsReferences.addAll(tokenizations.subList(dp.a.getTokenDocPos(), dp.b.getTokenDocPos()));
        }

//        for (Integer blocknum : blockReferences) {
//            Block blo = blocks.get(blocknum);
//            int tokens = blo.getStartToken();
//            int tokene = blo.getEndToken();
//            for (int i = tokens; i < tokene; i++) {
//                tokenizationsReferences.add(tokenizations.get(i));
//            }
//        }

        return tokenizationsReferences;
    }

//    public String getPDFPath() {
//        return path;
//    }

	/*
     * private static String pdftoxml = GrobidProperties.getPdf2XMLPath()
	 * .getAbsolutePath() +
	 * "/pdftoxml -blocks -noImage -noImageInline -fullFontName "; private
	 * static String pdftoxml2 = GrobidProperties.getPdf2XMLPath()
	 * .getAbsolutePath() + "/pdftoxml -blocks -noImageInline -fullFontName ";
	 */


    // producing the low level xml
    // representation of a pdf
    // WARNING: it might be too
    // short for ebook !



    /**
     * Parser PDF2XML output representation and get the tokenized form of the document.
     *
     * @return list of features
     */
    public List<String> addTokenizedDocument() {
        List<String> images = new ArrayList<String>();
        PDF2XMLSaxParser parser = new PDF2XMLSaxParser(this, images);

        tokenizations = null;

        File file = new File(pathXML);
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);

            // get a factory
            SAXParserFactory spf = SAXParserFactory.newInstance();
            // get a new instance of parser
            SAXParser p = spf.newSAXParser();

            p.parse(in, parser);
            tokenizations = parser.getTokenization();
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

    /**
     * Try to reconnect blocks cut because of layout constraints (new col., new
     * page, inserted figure, etc.)
     */
    public void reconnectBlocks() throws Exception {
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
    }

    public String getHeaderFeatured(boolean getHeader,
                                    boolean withRotation) {
        if (getHeader) {
            // String theHeader = getHeaderZFN(firstPass);
            String theHeader = getHeader();
            if (theHeader == null) {
                getHeaderLastHope();
            } else if (theHeader.trim().length() == 1) {
                getHeaderLastHope();
            }
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
				text = text.replace(" ","");
                if (text.length() == 0) {
                    n++;
                    continue;
                }

                if (text.equals("\n")) {
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

                boolean filter = false;
                if (text.startsWith("@IMAGE")) {
                    filter = true;
                } else if (text.contains(".pbm")) {
                    filter = true;
                } else if (text.contains(".vec")) {
                    filter = true;
                } else if (text.contains(".jpg")) {
                    filter = true;
                }

                if (filter) {
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
                                    if ((toto.length() != 0)
                                            && (!(toto.startsWith("@IMAGE")))
                                            && (!text.contains(".pbm"))
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

        return header.toString();
    }

    // default bins for relative position
    private static final int nbBins = 12;

    // heuristics to get the header section... should be should be a global
    // CRF structure recognition
    // model
    public String getHeader() {
        //if (firstPass)
        BasicStructureBuilder.firstPass(this);

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
     */
    public String getHeaderLastHope() {
        String res;
        StringBuilder accumulated = new StringBuilder();
        int i = 0;
        for (Block block : blocks) {
            String localText = block.getText();
            localText = localText.trim();
            localText = localText.replace("  ", " ");

            if (localText.contains("@PAGE")) {
                beginBody = i;
                for (int j = 0; j < i + 1; j++) {
                    Integer inte = j;
                    if (!blockDocumentHeaders.contains(inte))
                        blockDocumentHeaders.add(inte);
                }
                res = accumulated.toString();

                return res;
            }

            accumulated.append(localText);
            i++;
        }

		/*
         * beginBody = i-1; for(int j = 0; j<i; j++) { Integer inte = new
		 * Integer(j); if (!blockDocumentHeaders.contains(inte))
		 * blockDocumentHeaders.add(inte); } res = accumulated.toString();
		 * 
		 * return res;
		 */
        return null;
    }

    /**
     * We try to match the introduction section in a safe way, and consider if
     * minimum requirements are met the blocks before this position as header.
     */
    public String getHeaderByIntroduction() {
        String res;
        StringBuilder accumulated = new StringBuilder();
        int i = 0;
        for (Block block : blocks) {
            String localText = block.getText();
            localText = localText.trim();

            Matcher m = BasicStructureBuilder.introductionStrict
                    .matcher(localText);
            if (m.find()) {
                accumulated.append(localText);
                beginBody = i;
                for (int j = 0; j < i + 1; j++) {
                    Integer inte = j;
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
     * Return the body of the document. getHeader() and getReferences() must
     * have been called before.
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
     * Return all blocks.
     */
    public String getAllBlocks() {
        StringBuilder accumulated = new StringBuilder();
        int i = 0;
        for (Block block : blocks) {
            accumulated.append("@block ").append(i).append("\n").append(block.getText()).append("\n");
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

    /**
     * Return all textual content except metadata.
     */
    public String getAllBody(Engine engine, BiblioItem biblio,
                             List<BibDataSet> bds, boolean withBookTitle) {
        StringBuilder accumulated = new StringBuilder();
        int i = 0;

        if (biblio.getTitle() != null) {
            accumulated.append(biblio.getTitle()).append("\n");
        }

        if (biblio.getJournal() != null) {
            accumulated.append(biblio.getJournal()).append("\n");
        }

        if (biblio.getAbstract() != null) {
            accumulated.append(biblio.getAbstract()).append("\n");
        }

        for (Block block : blocks) {
            Integer ii = i;

            if ((!blockDocumentHeaders.contains(ii))
                    && (!blockReferences.contains(ii))) {
                if ((!blockHeaders.contains(ii))
                        && (!blockFooters.contains(ii))
                        && (!blockFigures.contains(ii))
                        && (!blockTables.contains(ii))) {
                    String localText = block.getText();
                    if (localText != null) {
                        localText = localText.trim();
                        // String originalLocalText = localText;
                        localText = TextUtilities.dehyphenize(localText);
                        localText = localText.replace("\n", " ");
                        localText = localText.replace("\t", " ");
                        localText = localText.replace("  ", " ");
                        localText = localText.trim();

                        if (localText.length() == 0) {
                            i++;
                            continue;
                        }

                        // section
                        if (blockSectionTitles.contains(ii)) {
                            // dehyphenization of section titles
                            localText = TextUtilities
                                    .dehyphenizeHard(localText);
                            accumulated.append(localText).append("\n");
                        } else if (blockHeadFigures.contains(ii)) {
                            int innd = localText.indexOf("@IMAGE");
                            if (innd == -1) {
                                accumulated.append(localText).append("\n");
                            }
                        } else if (blockHeadTables.contains(ii)) {
                            accumulated.append(localText).append("\n");
                        } else if (localText.startsWith("@BULLET")) {
                            localText = localText.replace("@BULLET", " • ");
                            accumulated.append(localText).append("\n");
                        } else {
                            if ((!localText.startsWith("@IMAGE"))
                                    && (!localText.startsWith("@PAGE")))
                                accumulated.append(localText).append("\n");
                        }
                    }
                }
            }
            i++;
        }

        for (BibDataSet bib : bds) {
            BiblioItem bit = bib.getResBib();

            if (bit.getTitle() != null) {
                accumulated.append(bit.getTitle()).append("\n");
            }

            if (withBookTitle) {
                if (bit.getJournal() != null) {
                    accumulated.append(bit.getJournal()).append("\n");
                }

                if (bit.getBookTitle() != null) {
                    accumulated.append(bit.getBookTitle()).append("\n");
                }
            }
        }

        return accumulated.toString();
    }

    /**
     * Get the introduction, i.e. first section after header until next section
     * title
     */
    public String getIntroduction(Engine engine) throws Exception {
        StringBuilder introduction = new StringBuilder();

        int i = 0;
        int add = 0;
        for (Block block : blocks) {
            Integer ii = i;

            if (!blockDocumentHeaders.contains(ii)) {
                String localText = block.getText();
                if (localText != null) {
                    localText = localText.trim();
                    localText = TextUtilities.dehyphenize(localText);
                    localText = localText.replace("\n", " ");
                    localText = localText.replace("\t", " ");
                    localText = localText.replace("  ", " ");
                    localText = localText.trim();

                    if (localText.length() == 0) {
                        i++;
                        continue;
                    }

                    if (localText
                            .startsWith("Permission to make digital or hard copies")) {
                        i++;
                        continue;
                    }

                    if ((blockSectionTitles.contains(ii)) && (add > 0)) {
                        return introduction.toString();
                    } else if (blockSectionTitles.contains(ii))
                        add++;
                    else {
                        if ((!localText.startsWith("@IMAGE"))
                                && (!localText.startsWith("@PAGE"))) {
                            localText = localText.replace("@BULLET", " • ");
                            introduction.append(localText).append("\n");
                        }
                    }
                }
            }
            i++;
        }

        return introduction.toString();
    }

    /**
     * Get the conclusion, i.e. section before the references or the
     * acknowlegement
     */
    public String getConclusion(Engine engine) throws Exception {
        String conclusion = "";
        int add = 0;
        for (int i = beginReferences - 1; i > 0; i--) {
            Integer ii = i;
            Block block = blocks.get(i);

            if ((!blockDocumentHeaders.contains(ii))
                    && (!blockReferences.contains(ii))
                    && (!blockHeaders.contains(ii))
                    && (!blockFooters.contains(ii))
                    && (!blockFigures.contains(ii))
                    && (!blockTables.contains(ii))) {

                String localText = block.getText();
                if (localText != null) {
                    localText = localText.trim();
                    localText = TextUtilities.dehyphenize(localText);
                    localText = localText.replace("\n", " ");
                    localText = localText.replace("\t", " ");
                    localText = localText.replace("  ", " ");
                    localText = localText.trim();

                    if (localText.length() == 0) {
                        continue;
                    }

                    if (localText.startsWith("Permission to make digital or hard copies")) {
                        continue;
                    }

                    if (blockSectionTitles.contains(ii))
                        return conclusion;
                    else {
                        if ((!localText.startsWith("@IMAGE"))
                                && (!localText.startsWith("@PAGE"))) {
                            localText = localText.replace("@BULLET", " • ");
                            conclusion = localText + "\n" + conclusion;
                            add++;
                        }
                    }

                    if (add == 5)
                        return conclusion;
                }
            }
        }

        return conclusion;
    }

    /**
     * Get all section titles
     */
    public String getSectionTitles() throws Exception {
        StringBuilder titles = new StringBuilder();

        int i = 0;
        for (Block block : blocks) {
            Integer ii = i;

            if (blockSectionTitles.contains(ii)) {
                String localText = block.getText();
                if (localText != null) {
                    localText = localText.trim();
                    localText = TextUtilities.dehyphenize(localText);
                    localText = localText.replace("\n", " ");
                    localText = localText.replace("\t", " ");
                    localText = localText.replace("  ", " ");
                    localText = localText.trim();

                    if (localText.length() == 0) {
                        i++;
                        continue;
                    }

                    if (localText.startsWith("Permission to make digital or hard copies")) {
                        i++;
                        continue;
                    }

                    localText = TextUtilities.dehyphenizeHard(localText);
                    titles.append(localText).append("\n");
                }
            }
            i++;
        }

        return titles.toString();
    }

    public String getReferences() {
        throw new IllegalStateException("Please use segmentation model for getting references");

//        blockReferences = new TreeSet<>();
//        StringBuilder accumulated = new StringBuilder();
//        // we start from the end of the document
//        int i = blocks.size() - 1;
//        beginReferences = -1;
//        String prefix = null;
//        int bad = 0;
//        while ((i > 0) && (bad < 20) && (i > beginBody + 2)) {
//            Block block = blocks.get(i);
//            String localText = block.getText();
//            if (localText != null)
//                localText = localText.trim();
//            else {
//                i--;
//                continue;
//            }
//
//            if (localText.equals("@PAGE")) {
//                localText = "\n";
//
//                // if the next block is just a number, that's a page number that
//                // needs to be throw out too
//                if (i > 1) {
//                    Block nextBlock = blocks.get(i - 1);
//                    String nextLocalText = nextBlock.getText();
//                    if (featureFactory == null) {
//                        featureFactory = FeatureFactory.getInstance();
//                    }
//                    if (featureFactory.test_number(nextLocalText)) {
//                        i = i - 1;
//                        continue;
//                    }
//                }
//            }
//
//            if (prefix == null) {
//                if (localText.length() > 0) {
//                    if ((localText.charAt(0) == '[')
//                            || (localText.charAt(0) == '(')) {
//                        prefix = "" + localText.charAt(0);
//                    }
//
//                }
//            }
//
//            if (block.getNbTokens() < 5) {
//                Matcher m = BasicStructureBuilder.references.matcher(localText);
//                if (m.find()) {
//                    // we clearly found the beginning of the references
//                    beginReferences = i;
//                    return accumulated.toString();
//                }
//            }
//
//            if (prefix == null) {
//                accumulated.insert(0, localText + "\n");
//                blockReferences.add(0, i);
//            } else if (localText.length() == 0) {
//                bad++;
//            } else if (localText.charAt(0) == prefix.charAt(0)) {
//                accumulated.insert(0, localText + "\n");
//                bad = 0;
//                blockReferences.add(0, i);
//            } else {
//                bad++;
//            }
//            i--;
//        }
//        beginReferences = i;
//        return accumulated.toString();
    }

    static public final Pattern DOIPattern = Pattern
            .compile("(10\\.\\d{4,5}\\/[\\S]+[^;,.\\s])");

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

    public List<Integer> getBlockHeaders() {
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
    }

    public List<Integer> getBlockDocumentHeaders() {
        return blockDocumentHeaders;
    }

    public SortedSet<DocumentPiece> getBlockReferences() {
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

    public void setBibDataSets(List<BibDataSet> bibDataSets) {
        this.bibDataSets = bibDataSets;
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
    public List<String> getDocumentPieceTokenization(DocumentPiece dp) {
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
}