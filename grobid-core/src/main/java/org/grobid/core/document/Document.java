package org.grobid.core.document;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.engines.CitationParser;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.HeaderParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorFulltext;
import org.grobid.core.features.FeaturesVectorHeader;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.Cluster;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.process.ProcessRunner;
import org.grobid.core.sax.PDF2XMLSaxParser;
import org.grobid.core.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for representing, processing and exchanging a document item.
 *
 * @author Patrice Lopez
 */

public class Document {
    private static final Logger LOGGER = LoggerFactory.getLogger(Document.class);

    private String path = null; // path where the pdf file is stored

    private String pathXML = null; // XML representation of the current PDF file

    private int beginBody = -1;
    private int beginReferences = -1;

    public boolean titleMatchNum = false; // true if the section titles of the document ar numbered
    private String lang = null;

    public ArrayList<Block> blocks = null;
    public ArrayList<Cluster> clusters = null;

    public ArrayList<Integer> blockHeaders = null;
    public ArrayList<Integer> blockFooters = null;
    public ArrayList<Integer> blockSectionTitles = null;
    public ArrayList<Integer> acknowledgementBlocks = null;
    public ArrayList<Integer> blockDocumentHeaders = null;
    public ArrayList<Integer> blockReferences = null;

    public ArrayList<Integer> blockTables = null;
    public ArrayList<Integer> blockFigures = null;
    public ArrayList<Integer> blockHeadTables = null;
    public ArrayList<Integer> blockHeadFigures = null;

    private FeatureFactory featureFactory = null;

    // original tokenization and tokens - in order to recreate the original strings and spacing
    private ArrayList<String> tokenizations = null;

    // list of bibliographical references with context
    public ArrayList<BibDataSet> bibDataSets = null;

    public DocumentNode top = null;

    public Document() {
        top = new DocumentNode("top", "0");
    }

    public Document(String pdfPath, String repositPath) {
        path = pdfPath;
        top = new DocumentNode("top", "0");
    }

    public void setLanguage(String l) {
        lang = l;
    }

    public String getLanguage() {
        return lang;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    public void addBlock(Block b) {
        if (blocks == null)
            blocks = new ArrayList<Block>();
        blocks.add(b);
    }

    public void setPdf(String pdfPath) {
        path = pdfPath;
    }


    public void setPathXML(String pathXML) {
        this.pathXML = pathXML;
    }

    public ArrayList<String> getTokenizations() {
        return tokenizations;
    }

    public ArrayList<String> getTokenizationsHeader() {
        ArrayList<String> tokenizationsHeader = new ArrayList<String>();
        for (Integer blocknum : blockDocumentHeaders) {
            Block blo = blocks.get(blocknum.intValue());
            int tokens = blo.getStartToken();
            int tokene = blo.getEndToken();
            for (int i = tokens; i < tokene; i++) {
                tokenizationsHeader.add(tokenizations.get(i));
            }
        }

        return tokenizationsHeader;
    }

    public ArrayList<String> getTokenizationsFulltext() {
        ArrayList<String> tokenizationsFulltext = new ArrayList<String>();
        for (Block blo : blocks) {
            int tokens = blo.getStartToken();
            int tokene = blo.getEndToken();
            for (int i = tokens; i < tokene; i++) {
                tokenizationsFulltext.add(tokenizations.get(i));
            }
        }

        return tokenizationsFulltext;
    }

    public ArrayList<String> getTokenizationsReferences() {
        ArrayList<String> tokenizationsReferences = new ArrayList<String>();
        for (Integer blocknum : blockReferences) {
            Block blo = blocks.get(blocknum.intValue());
            int tokens = blo.getStartToken();
            int tokene = blo.getEndToken();
            for (int i = tokens; i < tokene; i++) {
                tokenizationsReferences.add(tokenizations.get(i));
            }
        }

        return tokenizationsReferences;
    }

    public String getPDFPath() {
        return path;
    }

//    private static String pdftoxml =
//            GrobidProperties.getInstance().getBinPath() + "/xpdf/pdftoxml -blocks -noImage -noImageInline ";
//    private static String pdftoxml2 =
//    		GrobidProperties.getInstance().getBinPath() + "/xpdf/pdftoxml -blocks -noImageInline ";

    private static String pdftoxml =
            GrobidProperties.getInstance().getPdf2XMLPath().getAbsolutePath() + "/pdftoxml -blocks -noImage -noImageInline -fullFontName ";
    private static String pdftoxml2 =
            GrobidProperties.getInstance().getPdf2XMLPath().getAbsolutePath() + "/pdftoxml -blocks -noImageInline -fullFontName ";

    private static final long timeout = 20000; // timeout 20 second for producing the low 
    // level xml representation of a pdf
    // WARNING: it might be too short for ebook !

    /**
     * Create an XML representation from a pdf file. If tout is true (default), a timeout is used.
     * If force is true, the xml file is always regenerated, even if already present
     * (default is false, it can save up to 50% overall runtime).
     * If full is true, the extraction covers also images within the pdf, which is relevant for
     * fulltext extraction.
     */
    public String pdf2xml(boolean tout,
                          boolean force,
                          int startPage,
                          int endPage,
                          String pdfPath,
                          String tmpPath,
                          boolean full) throws Exception {
    	LOGGER.debug("start pdf2xml");
        String pdftoxml0;
        if (full) {
            pdftoxml0 = pdftoxml2;
        } else {
            pdftoxml0 = pdftoxml;
        }

        if (startPage > 0)
            pdftoxml0 += " -f " + startPage + " ";
        if (endPage > 0)
            pdftoxml0 += " -l " + endPage + " ";

        // if the XML representation already exists, no need to redo the conversion,
        // except if the force parameter is set to true
        String tmpPathXML = tmpPath + "/" + KeyGen.getKey() + ".lxml";
        File f = new File(tmpPathXML);

        if ((!f.exists()) || force) {
            String cmd = pdftoxml0 + pdfPath + " " + tmpPathXML;
            LOGGER.info("Executing: " + cmd);
            ProcessRunner worker = new ProcessRunner(cmd, "pdf2xml[" + pdfPath + "]", true);
            worker.start();

            try {
                if (tout) {
                    worker.join(timeout);
                } else {
                    worker.join(50000); // max 50 second even without predefined timeout
                }
                if (worker.getExitStatus() == null) {
                    tmpPathXML = null;
                    throw new RuntimeException("PDF to XML conversion timed out");
                }

                if (worker.getExitStatus() != 0) {
                    throw new RuntimeException("PDF to XML conversion failed due to: " + worker.getErrorStreamContents());
                }
            } catch (InterruptedException ex) {
                tmpPathXML = null;
                worker.interrupt();
                Thread.currentThread().interrupt();
            } finally {
                worker.interrupt();
            }
        }
        LOGGER.debug("end pdf2xml");
        return tmpPathXML;
    }

    public boolean cleanLxmlFile(String thePathXML,
                                 boolean cleanImages) {
        boolean success = false;

        try {
            if (thePathXML != null) {
                File fff = new File(thePathXML);
                if (fff.exists()) {
                    success = fff.delete();
                    if (!success) {
                        throw new GrobidResourceException("Deletion of temporary .lxml file failed for file '"
                                + fff.getAbsolutePath() + "'");
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof GrobidResourceException) {
                throw (GrobidResourceException) e;
            } else {
                throw new GrobidResourceException("An exception occured while deleting .lxml file '" +
                        thePathXML + "'.", e);
            }
        }

        // if cleanImages is true, we also remove the corresponding image resources subdirectory
        if (cleanImages) {
            try {
                if (thePathXML != null) {
                    File fff = new File(thePathXML + "_data");
                    if (fff.exists()) {
                        if (fff.isDirectory()) {
                            success = Utilities.deleteDir(fff);

                            if (!success) {
                                throw new GrobidResourceException("Deletion of temporary image files failed for file '"
                                        + fff.getAbsolutePath() + "'");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (e instanceof GrobidResourceException) {
                    throw (GrobidResourceException) e;
                } else {
                    throw new GrobidResourceException("An exception occured while deleting .lxml file '" +
                            thePathXML + "'.", e);
                }
            }
        }

        return success;
    }


    /**
     * Prepare features for structure annotations
     *
     * @return list of features
     * @throws java.io.IOException      when a file can not be opened
     * @throws javax.xml.parsers.ParserConfigurationException
     *                                  when parsing
     * @throws org.xml.sax.SAXException when parsing
     */
    public ArrayList<String> addFeaturesDocument() throws IOException, ParserConfigurationException, SAXException {
        ArrayList<String> images = new ArrayList<String>();
        PDF2XMLSaxParser parser = new PDF2XMLSaxParser(this, images);

        tokenizations = null;

        File file = new File(pathXML);
        FileInputStream in = new FileInputStream(file);

        // get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        //get a new instance of parser
        SAXParser p = spf.newSAXParser();
        try {
            p.parse(in, parser);
            tokenizations = parser.getTokenization();
        } catch (Exception e) {
            throw new GrobidException("An exception occurs.", e);
        }
        in.close();
        // we filter out possible line numbering for review works
        //filterLineNumber();
        return tokenizations;
    }

    /**
     * First pass to detect structures: remove page header/footer, identify section numbering,
     * identify Figure and table blocks.
     */
    public void firstPass() {
        try {
            BasicStructureBuilder.firstPass(this);
        } catch (Exception e) {
            throw new GrobidException("An exception occurs.", e);
        }
    }

    /**
     * Create a TEI representation of the document
     */
    public String toTEI(HeaderParser headerParser,
                        CitationParser citationParser,
                        boolean consolidateHeader,
                        boolean consolidateCitations,
                        boolean peer,
                        BiblioItem catalogue,
                        boolean withStyleSheet,
                        boolean onlyHeader) throws Exception {
        addFeaturesDocument();

        if (blocks == null) {
            return null;
        }

        BasicStructureBuilder.firstPass(this);
        String header = getHeader(false);
        if (header == null) {
            header = getHeaderLastHope();
        }
        String body = getBody();
        List<BibDataSet> bds = new ArrayList<BibDataSet>();

        if (!onlyHeader) {
            if (titleMatchNum) {
                // have we miss a section title? in case of standard numbering we can try to recover  missing ones
                int i = 0;
                for (Block block : blocks) {
                    Integer ii = i;

                    if ((!blockFooters.contains(ii)) && (!blockDocumentHeaders.contains(ii)) &&
                            (!blockHeaders.contains(ii)) && (!blockReferences.contains(ii)) &&
                            (!blockFigures.contains(ii)) && (!blockTables.contains(ii)) &&
                            (!blockSectionTitles.contains(ii)) &&
                            (!acknowledgementBlocks.contains(ii))
                            ) {
                        String localText = block.getText();

                        if (localText != null) {
                            localText = localText.replace("\n", " ");
                            localText = localText.replace("  ", " ");
                            localText = localText.trim();
                            if ((localText.length() > 9) && (localText.length() < 150)) {

                                Matcher m1 = BasicStructureBuilder.headerNumbering1.matcher(localText);
                                Matcher m2 = BasicStructureBuilder.headerNumbering2.matcher(localText);
                                if (m1.find()) {
                                    if (block.getNbTokens() < 10) {
                                        int count1 = TextUtilities.countDigit(localText);
                                        if (count1 < localText.length() / 4) {
                                            if (!blockSectionTitles.contains(ii))
                                                blockSectionTitles.add(ii);
                                        }
                                    }
                                } else if (m2.find()) {
                                    if (block.getNbTokens() < 10) {
                                        int count1 = TextUtilities.countDigit(localText);
                                        if (count1 < localText.length() / 4) {
                                            if (!blockSectionTitles.contains(ii))
                                                blockSectionTitles.add(ii);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    i++;
                }
            }

            ArrayList<String> tokenizations = getTokenizationsReferences();
			if ((tokenizations == null) || (tokenizations.size() == 0)) {
				// we need to tokenize the reference section, as it has not been done until this stage
				String refSection = getReferences();
				StringTokenizer st = new StringTokenizer(refSection, " \t\n" + TextUtilities.fullPunctuations, true);
                while (st.hasMoreTokens()) {
					tokenizations.add(st.nextToken()); 
				}
			}

            List<String> refs = null;
            if (citationParser != null) {
                refs = citationParser.segmentReferences(tokenizations);
                if (refs != null) {
                    for (String refString : refs) {
                        BiblioItem bib = citationParser.processing(refString, consolidateCitations);
                        BibDataSet bd = new BibDataSet();
                        bd.setResBib(bib);
                        bd.setRawBib(refString);
                        bds.add(bd);
                    }
                }
            }
        }
        //BiblioItem biblio = headerParser.processingHeaderBlock(consolidateHeader, this);
        BiblioItem biblio = new BiblioItem();
        headerParser.processingHeaderBlock(consolidateHeader, this, biblio);

        if (catalogue != null) {
            BiblioItem.correct(biblio, catalogue);
        }
        LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
        Language lang = languageUtilities.runLanguageId(body);


        if (lang != null) {
            setLanguage(lang.getLangId());
            biblio.setLanguage(lang.getLangId());
        }

        if (!onlyHeader) {
            // re-adjust the introduction boundary
            int i = 0;
            for (Block block : blocks) {
                Integer ii = i;
                if (blockDocumentHeaders.contains(ii)) {
                    String localText = block.getText();

                    if (localText != null) {
                        localText = localText.trim();
                        localText = localText.replace("\n", " ");
                        localText = localText.replace("  ", " ");
                        localText = localText.trim();
                        if (localText.length() > 0) {
                            Matcher ma1 = BasicStructureBuilder.introduction.matcher(localText);
                            Matcher ma2 = BasicStructureBuilder.references.matcher(localText);
                            if ((ma1.find()) || (ma2.find())) {
                                if (((localText.startsWith("1.")) || (localText.startsWith("1 "))) ||
                                        ((localText.startsWith("2.")) || (localText.startsWith("2 ")))) {
                                    blockDocumentHeaders.remove(ii);
                                    break;
                                }
                            }
                        }
                    }
                }
                i++;
            }

            // ACM-type specific cleaning
            i = 0;
            for (Block block : blocks) {
                Integer ii = i;

                if ((!blockFooters.contains(ii)) && (!blockDocumentHeaders.contains(ii)) &&
                        (!blockHeaders.contains(ii)) && (!blockReferences.contains(ii)) &&
                        (!blockFigures.contains(ii)) && (!blockTables.contains(ii)) &&
                        (!blockSectionTitles.contains(ii)) &&
                        (!acknowledgementBlocks.contains(ii))
                        ) {
                    String localText = block.getText();

                    if (localText != null) {
                        localText = localText.replace("  ", " ");
                        localText = localText.trim();
                        String lastLocalText = null;

                        StringTokenizer st = new StringTokenizer(localText, "\n");
                        while (st.hasMoreTokens()) {
                            lastLocalText = st.nextToken().trim();
                        }

                        if (lastLocalText != null) {
                            if (lastLocalText.startsWith("* Contact author:")) {
                                String newLocalText = "";
                                st = new StringTokenizer(localText, "\n");
                                boolean start = true;
                                while (st.hasMoreTokens()) {
                                    if (start) {
                                        String toto = st.nextToken();
                                        if (st.hasMoreTokens())
                                            newLocalText += toto;
                                        start = false;
                                    } else {
                                        String toto = st.nextToken();
                                        if (st.hasMoreTokens()) {
                                            newLocalText += "\n" + toto;
                                        }
                                    }
                                }
                                block.setText(newLocalText);
                                break;
                            }
                        }
                    }
                }
                i++;
            }

            reconnectBlocks();
        }

        TEIFormater teiFormater = new TEIFormater(this);
        String res = teiFormater.toTEIBody(biblio,
                bds,
                peer,
                withStyleSheet,
                onlyHeader);
        return res;
    }

    /**
     * Try to reconnect blocks cut because of layout constraints (new col., new page, inserted figure, etc.)
     */
    public void reconnectBlocks() throws Exception {
        int i = 0;
        //ArrayList<Block> newBlocks = new ArrayList<Block>();
        boolean candidate = false;
        int candidateIndex = -1;
        for (Block block : blocks) {
            Integer ii = i;
            if ((!blockFooters.contains(ii)) && (!blockDocumentHeaders.contains(ii)) &&
                    (!blockHeaders.contains(ii)) && (!blockReferences.contains(ii)) &&
                    (!blockSectionTitles.contains(ii)) && (!blockFigures.contains(ii)) &&
                    (!blockTables.contains(ii)) &&
                    (!blockHeadFigures.contains(ii)) && (!blockHeadTables.contains(ii))
                    ) {
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

                        if (innd != -1) {
                            if (candidate == true) {
                            }
                        } else
                            // test if the block starts without upper case
                            if (text.length() > 2) {
                                char c1 = text.charAt(0);
                                char c2 = text.charAt(1);
                                if (Character.isLetter(c1) && Character.isLetter(c2) &&
                                        !Character.isUpperCase(c1) && !Character.isUpperCase(c2)) {
                                    // this block is ok for merging with the previous candidate
                                    if (candidate) {
                                        Block target = blocks.get(candidateIndex);
                                        // we simply move tokens
                                        ArrayList<LayoutToken> theTokens = block.getTokens();
                                        for (LayoutToken tok : theTokens) {
                                            target.addToken(tok);
                                        }
                                        target.setText(target.getText() + "\n" + block.getText());
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
                            // test the position of the last token, which should be close
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
                                    if ((((c1 == '-') || (c1 == ')')) && Character.isLetter(c2)) |
                                            (Character.isLetter(c1) && Character.isLetter(c2))) {
                                        // this block is a candidate for merging with the next one
                                        candidate = true;
                                        candidateIndex = i;
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

    public String getHeaderFeatured(boolean firstPass, boolean getHeader, boolean withRotation) {
        if (getHeader) {
            //String theHeader = getHeaderZFN(firstPass);
            String theHeader = getHeader(firstPass);
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
            Block block = blocks.get(blocknum.intValue());
            boolean newline;
            boolean previousNewline = false;
            endblock = false;
            ArrayList<LayoutToken> tokens = block.getTokens();
            if (tokens == null) continue;
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
                text = text.trim();
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
                if ((text.equals("(")) || (text.equals("["))) {
                    features.punctType = "OPENBRACKET";
                } else if ((text.equals(")")) || (text.equals("]"))) {
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
                                    if ((toto.length() != 0) &&
                                            (!(toto.startsWith("@IMAGE"))) &&
                                            (!text.contains(".pbm")) &&
                                            (!text.contains(".vec")) &&
                                            (!text.contains(".jpg"))) {
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

                //CENTERED
                //LEFTAJUSTED

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
    static private int nbBins = 12;

    public String getFulltextFeatured(boolean firstPass, boolean getHeader) {
        //System.out.println(getAllBlocks());

        //if (getHeader)
        //	getHeader(firstPass);
        featureFactory = FeatureFactory.getInstance();
        StringBuffer fulltext = new StringBuffer();
        String currentFont = null;
        int currentFontSize = -1;

        // vector for features
        FeaturesVectorFulltext features = null;
        FeaturesVectorFulltext previousFeatures = null;
        boolean endblock = false;
        boolean endPage = true;
        boolean newPage = true;
        boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        int documentLength = 0;
        int pageLength = 0; //length of the current page
        // we calculate current document length
        for (Block block : blocks) {
            //documentLength += block.getEndToken() - block.getStartToken();
            ArrayList<LayoutToken> tokens = block.getTokens();
            if (tokens != null) {
                documentLength += tokens.size();
            }
        }
        //System.out.println("documentLength: " + documentLength);
        int blockPos = 0;
        for (Block block : blocks) {
            // we estimate the length of the page
            if (start || endPage) {
                boolean stop = false;
                pageLength = 0;
                for (int z = blockPos; (z < blocks.size()) && !stop; z++) {
                    String localText2 = blocks.get(z).getText();
                    if (localText2 != null) {
                        if (localText2.indexOf("@PAGE") != -1) {
                            if (pageLength > 0) {
                                if (blocks.get(z).getTokens() != null) {
                                    pageLength += blocks.get(z).getTokens().size();
                                }
                                stop = true;
                                break;
                            }
                        } else {
                            if (blocks.get(z).getTokens() != null) {
                                pageLength += blocks.get(z).getTokens().size();
                            }
                        }
                    }
                }
                //System.out.println("pageLength: " + pageLength);
            }
            if (start) {
                newPage = true;
                start = false;
            }
            boolean newline = true;
            boolean previousNewline = false;
            endblock = false;

            if (endPage) {
                newPage = true;
                mm = 0;
            }

            String localText = block.getText();
            if (localText != null) {
                if (localText.indexOf("@PAGE") != -1) {
                    mm = 0;
                    //pageLength = 0;
                    endPage = true;
                    newPage = false;
                } else {
                    endPage = false;
                }
            }

            ArrayList<LayoutToken> tokens = block.getTokens();
            if (tokens == null) {
                blockPos++;
                continue;
            }
            int n = 0; // token position in current block

            while (n < tokens.size()) {
                LayoutToken token = tokens.get(n);
                features = new FeaturesVectorFulltext();
                features.token = token;

                String text = token.getText();
                if (text == null) {
                    n++;
                    mm++;
                    nn++;
                    continue;
                }
                text = text.trim();
                if (text.length() == 0) {
                    n++;
                    mm++;
                    nn++;
                    continue;
                }

                if (text.equals("\n")) {
                    newline = true;
                    previousNewline = true;
                    n++;
                    mm++;
                    nn++;
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
                    mm++;
                    nn++;
                    continue;
                }

                features.string = text;

                if (newline)
                    features.lineStatus = "LINESTART";
                Matcher m0 = featureFactory.isPunct.matcher(text);
                if (m0.find()) {
                    features.punctType = "PUNCT";
                }
                if ((text.equals("(")) || (text.equals("["))) {
                    features.punctType = "OPENBRACKET";
                } else if ((text.equals(")")) || (text.equals("]"))) {
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
                                    if ((toto.length() != 0) &&
                                            (!(toto.startsWith("@IMAGE"))) &&
                                            (text.indexOf(".pbm") == -1) &&
                                            (text.indexOf(".vec") == -1) &&
                                            (text.indexOf(".jpg") == -1)) {
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
                    else if (features.blockStatus == null) {
                        features.blockStatus = "BLOCKEND";
                        endblock = true;
                    }
                }

                if (newPage) {
                    features.pageStatus = "PAGESTART";
                    newPage = false;
                    endPage = false;
                    if (previousFeatures != null)
                        previousFeatures.pageStatus = "PAGEEND";
                } else {
                    features.pageStatus = "PAGEIN";
                    newPage = false;
                    endPage = false;
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

                // HERE horizontal information
                //CENTERED
                //LEFTAJUSTED
                //CENTERED

                if (features.capitalisation == null)
                    features.capitalisation = "NOCAPS";

                if (features.digit == null)
                    features.digit = "NODIGIT";

                if (features.punctType == null)
                    features.punctType = "NOPUNCT";

                features.relativeDocumentPosition = featureFactory.relativeLocation(nn, documentLength, nbBins);
                //System.out.println(mm + " / " + pageLength);
                features.relativePagePosition = featureFactory.relativeLocation(mm, pageLength, nbBins);

                //fulltext.append(features.printVector());
                if (previousFeatures != null)
                    fulltext.append(previousFeatures.printVector());
                n++;
                mm++;
                nn++;
                previousFeatures = features;
            }
            blockPos++;

        }
        if (previousFeatures != null)
            fulltext.append(previousFeatures.printVector());

        return fulltext.toString();
    }


    // heuristics to get the header section... should be replaced be a global CRF structure recognition
    // model
    public String getHeader(boolean firstPass) {
        if (firstPass)
            firstPass();

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
                    // second large block, it should be the first paragraph of the body
                    beginBody = i;
                    for (int j = 0; j <= i + 1; j++) {
                        Integer inte = new Integer(j);
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
                Matcher m = BasicStructureBuilder.introduction.matcher(localText);
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
                        // we clearly found the begining of the body with the introduction section
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

            accumulated.append(localText + "\n");
            i++;
        }

        return res;
    }

    /**
     * We return the first page as header estimation... better than nothing when nothing is
     * not acceptable.
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

        /*beginBody = i-1;
          for(int j = 0; j<i; j++) {
              Integer inte = new Integer(j);
              if (!blockDocumentHeaders.contains(inte))
                  blockDocumentHeaders.add(inte);
          }
          res = accumulated.toString();

          return res;*/
        return null;
    }


    /**
     * We try to match the introduction section in a safe way, and consider if minimum requirements
     * are met the blocks before this position as header.
     */
    public String getHeaderByIntroduction() {
        String res = null;
        StringBuffer accumulated = new StringBuffer();
        int i = 0;
        for (Block block : blocks) {
            String localText = block.getText();
            localText = localText.trim();

            Matcher m = BasicStructureBuilder.introductionStrict.matcher(localText);
            if (m.find()) {
                accumulated.append(localText);
                beginBody = i;
                for (int j = 0; j < i + 1; j++) {
                    Integer inte = new Integer(j);
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
     * Return the body of the document. getHeader() and getReferences() must have been called before.
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
            Integer ii = new Integer(i);

            if (blockDocumentHeaders.contains(ii)) {
                String localText = block.getText();
                if (localText != null) {
                    localText = localText.trim();
                    localText = localText.replace("  ", " ");
                    // we check if we have a Wiley publication - there is always the DOI around
                    // in a single block

                    if (localText.startsWith("DOI: 10.1002")) {
                        wiley = true;
                    }
                }
            }

            if ((!blockFooters.contains(ii)) && (!blockDocumentHeaders.contains(ii)) &
                    (!blockHeaders.contains(ii)) && wiley) {
                String localText = block.getText();

                if (localText != null) {
                    localText = localText.trim();
                    localText = localText.replace("  ", " ");

                    // the keyword block needs to join the header section
                    if (localText.startsWith("Keywords: ")) {
                        // the block before the keyword block is part of the abstract and needs to be
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
            //if ( (i >= beginBody) && (i < beginReferences) ) {

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
                blockReferences = new ArrayList<Integer>();
            }

            if ((!blockFooters.contains(ii)) && (!blockDocumentHeaders.contains(ii)) &&
                    (!blockHeaders.contains(ii)) && (!blockReferences.contains(ii))) {
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
                            //featureFactory = new FeatureFactory();
                        }
                        localText = TextUtilities.dehyphenize(localText);
                        accumulated.append(localText + "\n");
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
        StringBuffer accumulated = new StringBuffer();
        int i = 0;
        for (Block block : blocks) {
            accumulated.append("@block " + i + "\n" + block.getText() + "\n");
            i++;
        }
        return accumulated.toString();
    }

    /**
     * Return all blocks without markers.
     * <p/>
     * Ignore the toIgnore1 th blocks and the blocks after toIgnore2  (included)
     */
    public String getAllBlocksClean(int toIgnore1, int toIgnore2) {
        StringBuilder accumulated = new StringBuilder();
        if (toIgnore2 == -1)
            toIgnore2 = blocks.size() + 1;
        int i = 0;
        if (blocks != null) {
            for (Block block : blocks) {
                if ((i >= toIgnore1) && (i < toIgnore2)) {
                    accumulated.append(block.getText() + "\n");
                }
                i++;
            }
        }
        return accumulated.toString();
    }


    /**
     * Return all textual content except metadata.
     */
    public String getAllBody(Engine engine,
                             BiblioItem biblio,
                             List<BibDataSet> bds,
                             boolean withBookTitle) {
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

            if ((!blockDocumentHeaders.contains(ii)) && (!blockReferences.contains(ii))) {
                if ((!blockHeaders.contains(ii)) && (!blockFooters.contains(ii))
                        && (!blockFigures.contains(ii)) && (!blockTables.contains(ii))) {
                    String localText = block.getText();
                    if (localText != null) {
                        localText = localText.trim();
                        //String originalLocalText = localText;
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
                            localText = TextUtilities.dehyphenizeHard(localText);
                            accumulated.append(localText + "\n");
                        } else if (blockHeadFigures.contains(ii)) {
                            int innd = localText.indexOf("@IMAGE");
                            if (innd == -1) {
                                accumulated.append(localText + "\n");
                            }
                        } else if (blockHeadTables.contains(ii)) {
                            accumulated.append(localText + "\n");
                        } else if (localText.startsWith("@BULLET")) {
                            localText = localText.replace("@BULLET", " • ");
                            accumulated.append(localText + "\n");
                        } else {
                            if ((!localText.startsWith("@IMAGE")) &&
                                    (!localText.startsWith("@PAGE")))
                                accumulated.append(localText + "\n");
                        }
                    }
                }
            }
            i++;
        }

        for (BibDataSet bib : bds) {
            BiblioItem bit = bib.getResBib();

            if (bit.getTitle() != null) {
                accumulated.append(bit.getTitle() + "\n");
            }

            if (withBookTitle) {
                if (bit.getJournal() != null) {
                    accumulated.append(bit.getJournal() + "\n");
                }

                if (bit.getBookTitle() != null) {
                    accumulated.append(bit.getBookTitle() + "\n");
                }
            }
        }

        return accumulated.toString();
    }

    /**
     * Get the introduction, i.e. first section after header until next section title
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

                    if (localText.startsWith("Permission to make digital or hard copies")) {
                        i++;
                        continue;
                    }

                    if ((blockSectionTitles.contains(ii)) && (add > 0)) {
                        return introduction.toString();
                    } else if (blockSectionTitles.contains(ii))
                        add++;
                    else {
                        if ((!localText.startsWith("@IMAGE")) &&
                                (!localText.startsWith("@PAGE"))) {
                            localText = localText.replace("@BULLET", " • ");
                            introduction.append(localText + "\n");
                        }
                    }
                }
            }
            i++;
        }

        return introduction.toString();
    }

    /**
     * Get the conclusion, i.e. section before the references or the acknowlegement
     */
    public String getConclusion(Engine engine) throws Exception {
        String conclusion = "";
        int add = 0;
        for (int i = beginReferences - 1; i > 0; i--) {
            Integer ii = i;
            Block block = blocks.get(i);

            if ((!blockDocumentHeaders.contains(ii)) && (!blockReferences.contains(ii)) &&
                    (!blockHeaders.contains(ii)) && (!blockFooters.contains(ii)) &&
                    (!blockFigures.contains(ii)) && (!blockTables.contains(ii))) {

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
                        if ((!localText.startsWith("@IMAGE")) && (!localText.startsWith("@PAGE"))) {
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
                    titles.append(localText + "\n");
                }
            }
            i++;
        }

        return titles.toString();
    }

    public String getReferences() {
        blockReferences = new ArrayList<Integer>();
        StringBuilder accumulated = new StringBuilder();
        // we start from the end of the document
        int i = blocks.size() - 1;
        beginReferences = -1;
        String prefix = null;
        int bad = 0;
        while ((i > 0) && (bad < 20) && (i > beginBody + 2)) {
            Block block = blocks.get(i);
            String localText = block.getText();
            if (localText != null)
                localText = localText.trim();
            else {
                i--;
                continue;
            }

            if (localText.equals("@PAGE")) {
                localText = "\n";

                // if the next block is just a number, that's a page number that needs to be throw out too
                if (i > 1) {
                    Block nextBlock = blocks.get(i - 1);
                    String nextLocalText = nextBlock.getText();
                    if (featureFactory == null) {
                        featureFactory = FeatureFactory.getInstance();
                    }
                    if (featureFactory.test_number(nextLocalText)) {
                        i = i - 1;
                        continue;
                    }
                }
            }

            if (prefix == null) {
                if (localText.length() > 0) {
                    if ((localText.charAt(0) == '[') || (localText.charAt(0) == '(')) {
                        prefix = "" + localText.charAt(0);
                    }

                }
            }

            if (block.getNbTokens() < 5) {
                Matcher m = BasicStructureBuilder.references.matcher(localText);
                if (m.find()) {
                    // we clearly found the begining of the references
                    beginReferences = i;
                    return accumulated.toString();
                }
            }

            if (prefix == null) {
                accumulated.insert(0, localText + "\n");
                blockReferences.add(0, new Integer(i));
            } else if (localText.length() == 0) {
                bad++;
            } else if (localText.charAt(0) == prefix.charAt(0)) {
                accumulated.insert(0, localText + "\n");
                bad = 0;
                blockReferences.add(0, new Integer(i));
            } else {
                bad++;
            }
            i--;
        }
        beginReferences = i;
        return accumulated.toString();
    }

    static public Pattern DOIPattern = Pattern.compile("(10\\.\\d{4,5}\\/[\\S]+[^;,.\\s])");

    /*
      *  Try to match a DOI in the first page, independently from any preliminar segmentation.
      *  This can be useful for improving the chance to find a DOI in headers or footnotes.
      */
    public ArrayList<String> getDOIMatches() {
        ArrayList<String> results = new ArrayList<String>();
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

    /**
     * Special optimized header recognition for ZFN articles
     */
    public String getHeaderZFN(boolean firstPass) {
        if (firstPass) {
            firstPass();
        }
        //blockDocumentHeaders = new ArrayList<Integer>();
        String res = null;

        int headerStart = 0;
        int headerEnd = -1;

        beginBody = -1;
        StringBuilder accumulated = new StringBuilder();
        int i = 0;
        int nbLarge = 0;
        boolean abstractCandidate = false;

        // as a first pass we identify the first block of text with the largest font
        // in ZFN from 1948 to 2001, this is often the title which is the
        // begining of the ZFN header
        double maxHeight = 0.0;
        int nbBlock = blocks.size();

        for (Block block : blocks) {
            if (i > (nbBlock))
                break;
            String localText = block.getText();
            if (localText != null) {
                if (localText.startsWith("@PAGE"))
                    break;
            }
            ArrayList<LayoutToken> tokens = block.getTokens();
            if (tokens != null) {
                if (tokens.size() > 0) {
                    LayoutToken token = null;
                    if (tokens.size() > 1)
                        token = tokens.get(1); // to avoid big first letter of paragraph...
                    else
                        token = tokens.get(0);

                    // we also want to avoid big letters being actually mathematical symbols
                    // so we restrict to core latin characters


                    double height = token.getFontSize();
                    // we can slightly boost the height in case we have bold face
                    if (token.getBold())
                        height = height * 1.1;

                    if (height > maxHeight) {
                        headerStart = i;
                        maxHeight = height;
                    }
                }
            }

            i++;
        }

        // alternative first pass: we try to identify the ZFN label , i.e. "Z. Naturforsch."
        // the title is generally the third block above...
        int headerStart2 = -1;
        i = 0;
        boolean notizen = false;
        int notizenID = -1;
        int NaturforschIndex = -1;
        for (Block block : blocks) {
            if (i > (nbBlock))
                break;
            String localText = block.getText();
            if (localText != null) {
                localText = localText.trim();
                if (localText.startsWith("@PAGE"))
                    break;

                if (localText.startsWith("NOTIZEN")) {
                    notizen = true;
                    notizenID = i;
                    System.out.println("NOTIZEN paper");
                }

                if ((localText.startsWith("Z. Naturforsch")) ||
                        //(localText.startsWith("Z. Naturforschg.")) ||
                        (localText.startsWith("(Z. Naturforsch")) ||
                        (localText.startsWith("(Z. Naturforschg")) ||
                        (localText.startsWith("' (Z. Naturforschg")) ||
                        (localText.startsWith("(Z. ISTaturforschg")) ||
                        (localText.startsWith("Z. Naojrforsch."))
                        ) {
                    System.out.println("Naturforschg ref. at " + i);
                    NaturforschIndex = i;
                    // we search for the hightest local previous block as local title candidate
                    double maxHeight2 = 0.0;
                    int j = i - 1;
                    for (; (j >= i - 5) && (j > 0); j--) {
                        Block b = blocks.get(j);
                        ArrayList<LayoutToken> tokens2 = b.getTokens();
                        if (tokens2 != null) {
                            if (tokens2.size() > 0) {
                                LayoutToken token2 = null;
                                if (tokens2.size() > 1)
                                    token2 = tokens2.get(1); // to avoid big first letter of paragraph...
                                else
                                    token2 = tokens2.get(0);

                                double height2 = token2.getFontSize();
                                // we can slightly boost the height in case we have bold face
                                if (token2.getBold())
                                    height2 = height2 * 1.0;

                                if (height2 >= maxHeight2) {
                                    headerStart2 = j;
                                    maxHeight2 = height2;
                                }
                            }
                        }
                    }

                    if (notizen) {
                        if (notizenID == (i - 3))
                            headerStart2 = i - 2;
                        else
                            headerStart2 = i - 3;
                        // we check if the block just before the candidate start has the same
                        // layout attribute
                        if (headerStart2 - 1 >= 0) {
                            Block b1 = blocks.get(headerStart2);
                            Block b2 = blocks.get(headerStart2 - 1);

                            String localText2 = b2.getText();
                            if (localText2 != null) {
                                localText2 = localText2.trim();
                                //System.out.println(localText2);
                                if (!localText2.startsWith("NOTIZEN")) {
                                    // we compare font, case and fontsize
                                    String font1 = b1.getFont();
                                    String font2 = b2.getFont();
                                    boolean bold1 = b1.getBold();
                                    boolean bold2 = b2.getBold();
                                    Double size1 = b1.getFontSize();
                                    Double size2 = b2.getFontSize();
                                    //System.out.println("font1: " + font1 + ", font2: " + font2 +
                                    //", bold1: " + bold1 + ", bold2: " + bold2 + ", size1: " + size1 +
                                    //", size2: " + size2);
                                    if ((font1.equals(font2)) && (bold1 == bold2) &&
                                            (Double.compare(size1, size2) == 0)) {
                                        headerStart2 = headerStart2 - 1;
                                        //System.out.println("start minus one");
                                    }
                                }

                                //System.out.println("NOTIZEN exception: start at " + headerStart2);

                            }
                        }
                    }
                    //if (i > 3)
                    //	headerStart2 = i-3;
                    //else
                    //	headerStart2 = i;
                    break;
                }
            }
            i++;
        }

        if (headerStart2 != -1) {
            if (headerStart != 0)
                headerStart = headerStart2;
        }

        System.out.println("headerStart: " + headerStart + " with height: " + maxHeight);
        Integer integ = headerStart;
        blockDocumentHeaders.add(integ);

        // as second pass, we identify the end of the abstract and the begining of
        // introduction/description
        i = 0;
        if (!notizen) {
            for (Block block : blocks) {
                if (i >= headerStart) {
                    String localText = block.getText();
                    if (localText != null)
                        localText = localText.trim();

                    if ((NaturforschIndex != -1) && (i <= NaturforschIndex)) {
                        // we are between header start and NaturforschIndex
                    } else if (block.getNbTokens() > 20) {
                        if (!abstractCandidate) {
                            // first large block, it should be the abstract
                            abstractCandidate = true;
                            System.out.println("abstract found at " + i);
                        } else if (beginBody == -1) {
                            // second large block, it should be the first paragraph of the body
                            beginBody = i;
                            for (int j = headerStart; j < i-1; j++) {
                                Integer inte = new Integer(j);
                                if (!blockDocumentHeaders.contains(inte))
                                    blockDocumentHeaders.add(inte);
                            }
                            res = accumulated.toString();
                            nbLarge = 1;
                        } else {
                            nbLarge++;
                            if (nbLarge > 3) {
                                System.out.println("too much large block: end at " + headerEnd);
                                return res;
                            }
                        }
                    } else {
                        //System.out.println(localText);
                        Matcher m = BasicStructureBuilder.introduction.matcher(localText);
                        if (abstractCandidate) {
                            if (m.find()) {
                                System.out.println("intro pattern found in: " + localText);
                                // we clearly found the begining of the body
                                beginBody = i;
                                headerEnd = i - 1;
                                for (int j = headerStart; j < i; j++) {
                                    if (!blockDocumentHeaders.contains(j))
                                        blockDocumentHeaders.add(j);
                                }
                                System.out.println("abstract and intro found: end at " + headerEnd);
                                return accumulated.toString();
                            } else if (beginBody != -1) {
                                if (localText.startsWith("(1|I|A)\\.\\s")) {
                                    beginBody = i;
                                    headerEnd = i - 1;
                                    for (int j = headerStart; j < i; j++) {
                                        if (!blockDocumentHeaders.contains(j))
                                            blockDocumentHeaders.add(j);
                                    }
                                    System.out.println("intro found: end at " + headerEnd);
                                    return accumulated.toString();
                                }
                            }
                        } else {
                            if (m.find()) {
                                // we clearly found the begining of the body
                                beginBody = i;
                                headerEnd = i - 1;
                                for (int j = headerStart; j < i; j++) {
                                    if (!blockDocumentHeaders.contains(j))
                                        blockDocumentHeaders.add(j);
                                }
                                res = accumulated.toString();
                                //return accumulated.toString();
                            }
                        }
                    }

                    if ((i > 8) && (i > (blocks.size() * 0.8)) && (NaturforschIndex != -1) &&
                            (i <= (NaturforschIndex + 2))) {
                        if (beginBody != -1) {
                            System.out.println("safe end at " + headerEnd);
                            return res;
                        } else {
                            System.out.println("header not found.");
                            return null;
                        }
                    }

                    accumulated.append(localText);
                }
                i++;
            }
        }

        if (notizen) {
            int j = headerStart;
            for (; (j <= headerStart + 5) && (j < blocks.size()); j++) {
                Block b = blocks.get(j);
                String localText = b.getText();
                accumulated.append(localText);
                if (!blockDocumentHeaders.contains(j))
                    blockDocumentHeaders.add(j);
            }
            res = accumulated.toString();
            beginBody = j;
            headerEnd = j - 1;
            System.out.println("NOTIZEN exception: end at " + headerEnd);
        }

        System.out.println("normal end at " + headerEnd);

        return res;
    }


}