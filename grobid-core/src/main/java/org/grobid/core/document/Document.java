package org.grobid.core.document;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.SortedSetMultimap;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.engines.CitationParser;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.HeaderParser;
import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.engines.citations.LabeledReferenceResult;
import org.grobid.core.engines.citations.ReferenceSegmenter;
import org.grobid.core.engines.citations.RegexReferenceSegmenter;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorHeader;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.Cluster;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.process.ProcessRunner;
import org.grobid.core.sax.PDF2XMLSaxParser;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.Utilities;
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
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;
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
    private static final int KILLED_DUE_2_TIMEOUT = 143;

    private String path = null; // path where the pdf file is stored

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
    private List<BibDataSet> bibDataSets = null;

    private DocumentNode top = null;

    private final BiblioItem resHeader = null;

    private String tei;

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
            blocks = new ArrayList<>();
        blocks.add(b);
    }

    public void setPdf(String pdfPath) {
        path = pdfPath;
    }

    public void setPathXML(String pathXML) {
        this.pathXML = pathXML;
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
        List<String> tokenizationsHeader = new ArrayList<>();
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
        List<String> tokenizationsFulltext = new ArrayList<>();
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
        List<String> tokenizationsReferences = new ArrayList<>();

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

    public String getPDFPath() {
        return path;
    }

	/*
     * private static String pdftoxml = GrobidProperties.getPdf2XMLPath()
	 * .getAbsolutePath() +
	 * "/pdftoxml -blocks -noImage -noImageInline -fullFontName "; private
	 * static String pdftoxml2 = GrobidProperties.getPdf2XMLPath()
	 * .getAbsolutePath() + "/pdftoxml -blocks -noImageInline -fullFontName ";
	 */

    private static final long timeout = 20000; // timeout 20 second for
    // producing the low level xml
    // representation of a pdf
    // WARNING: it might be too
    // short for ebook !

    protected String getPdf2xml(boolean full) {
        String pdf2xml = GrobidProperties.getPdf2XMLPath().getAbsolutePath();

        pdf2xml += GrobidProperties.isContextExecutionServer() ? "/pdftoxml_server" : "/pdftoxml";

        if (full) {
            pdf2xml += " -blocks -noImageInline -fullFontName ";
        } else {
            pdf2xml += " -blocks -noImage -noImageInline -fullFontName ";
        }
        return pdf2xml;
    }

    /**
     * Create an XML representation from a pdf file. If tout is true (default),
     * a timeout is used. If force is true, the xml file is always regenerated,
     * even if already present (default is false, it can save up to 50% overall
     * runtime). If full is true, the extraction covers also images within the
     * pdf, which is relevant for fulltext extraction.
     */
    public String pdf2xml(boolean tout, boolean force, int startPage,
                          int endPage, String pdfPath, String tmpPath, boolean full)
            throws Exception {
        LOGGER.debug("start pdf2xml");
        long time = System.currentTimeMillis();
        String pdftoxml0;

        pdftoxml0 = getPdf2xml(full);

        if (startPage > 0)
            pdftoxml0 += " -f " + startPage + " ";
        if (endPage > 0)
            pdftoxml0 += " -l " + endPage + " ";

        // if the XML representation already exists, no need to redo the
        // conversion,
        // except if the force parameter is set to true
        String tmpPathXML = tmpPath + "/" + KeyGen.getKey() + ".lxml";
        File f = new File(tmpPathXML);

        if ((!f.exists()) || force) {
            List<String> cmd = new ArrayList<>();
            String[] tokens = pdftoxml0.split(" ");
            for (String token : tokens) {
                if (token.trim().length() > 0)
                    cmd.add(token);
            }
            cmd.add(pdfPath);
            cmd.add(tmpPathXML);
            if (GrobidProperties.isContextExecutionServer()) {
                tmpPathXML = processPdf2Xml(pdfPath, tmpPathXML, cmd);
            } else {
                tmpPathXML = processPdf2XmlThreadMode(tout, pdfPath,
                        tmpPathXML, cmd);
            }

        }
        LOGGER.debug("end pdf2xml. Time to process:"
                + (System.currentTimeMillis() - time) + "ms");
        return tmpPathXML;
    }

    /**
     * Process the conversion of pdf to xml format using thread calling native
     * executable.
     *
     * @param tout       timeout
     * @param pdfPath    path to pdf
     * @param tmpPathXML temporary path to save the converted file
     * @param cmd        arguments to call the executable pdf2xml
     * @return the path the the converted file.
     */
    protected String processPdf2XmlThreadMode(boolean tout, String pdfPath,
                                              String tmpPathXML, List<String> cmd) {
        LOGGER.debug("Executing: " + cmd.toString());
        ProcessRunner worker = new ProcessRunner(cmd, "pdf2xml[" + pdfPath + "]", true);

        worker.start();

        try {
            if (tout) {
                worker.join(timeout);
            } else {
                worker.join(50000); // max 50 second even without predefined
                // timeout
            }
            if (worker.getExitStatus() == null) {
                tmpPathXML = null;
                throw new RuntimeException("PDF to XML conversion timed out");
            }

            if (worker.getExitStatus() != 0) {
                throw new RuntimeException(
                        "PDF to XML conversion failed due to: "
                                + worker.getErrorStreamContents());
            }
        } catch (InterruptedException ex) {
            tmpPathXML = null;
            worker.interrupt();
            Thread.currentThread().interrupt();
        } finally {
            worker.interrupt();
        }
        return tmpPathXML;
    }

    /**
     * Process the conversion of pdf to xml format calling native executable. No
     * thread used for the execution.
     *
     * @param pdfPath    path to pdf
     * @param tmpPathXML temporary path to save the converted file
     * @param cmd        arguments to call the executable pdf2xml
     * @return the path the the converted file.
     * @throws TimeoutException
     */
    protected String processPdf2Xml(String pdfPath, String tmpPathXML,
                                    List<String> cmd) throws TimeoutException {
        LOGGER.debug("Executing: " + cmd.toString());
        Integer exitCode = org.grobid.core.process.ProcessPdf2Xml.process(cmd);

        if (exitCode == null) {
//			tmpPathXML = null;
            throw new RuntimeException("An error occured while converting pdf "
                    + pdfPath);
        } else if (exitCode == KILLED_DUE_2_TIMEOUT) {
            throw new TimeoutException("PDF to XML conversion timed out");
        } else if (exitCode != 0) {
            throw new RuntimeException(
                    "PDF to XML conversion failed with error code: " + exitCode);
        }

        return tmpPathXML;
    }

    public boolean cleanLxmlFile(String thePathXML, boolean cleanImages) {
        boolean success = false;

        try {
            if (thePathXML != null) {
                File fff = new File(thePathXML);
                if (fff.exists()) {
                    success = fff.delete();
                    if (!success) {
                        throw new GrobidResourceException("Deletion of temporary .lxml file failed for file '" + fff.getAbsolutePath() + "'");
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof GrobidResourceException) {
                throw (GrobidResourceException) e;
            } else {
                throw new GrobidResourceException("An exception occured while deleting .lxml file '" + thePathXML + "'.", e);
            }
        }

        // if cleanImages is true, we also remove the corresponding image
        // resources subdirectory
        if (cleanImages) {
            try {
                if (thePathXML != null) {
                    File fff = new File(thePathXML + "_data");
                    if (fff.exists()) {
                        if (fff.isDirectory()) {
                            success = Utilities.deleteDir(fff);

                            if (!success) {
                                throw new GrobidResourceException(
                                        "Deletion of temporary image files failed for file '" + fff.getAbsolutePath() + "'");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (e instanceof GrobidResourceException) {
                    throw (GrobidResourceException) e;
                } else {
                    throw new GrobidResourceException("An exception occured while deleting .lxml file '" + thePathXML + "'.", e);
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
    public List<String> addFeaturesDocument() throws IOException,
            ParserConfigurationException, SAXException {
        List<String> images = new ArrayList<>();
        PDF2XMLSaxParser parser = new PDF2XMLSaxParser(this, images);


        tokenizations = null;

        File file = new File(pathXML);
        FileInputStream in = new FileInputStream(file);

        // get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        // get a new instance of parser
        SAXParser p = spf.newSAXParser();
        try {
            p.parse(in, parser);
            tokenizations = parser.getTokenization();
        } catch (Exception e) {
            throw new GrobidException("An exception occurs.", e);
        }
        in.close();
        // we filter out possible line numbering for review works
        // filterLineNumber();
        return tokenizations;
    }

    /**
     * First pass to detect structures: remove page header/footer, identify
     * section numbering, identify Figure and table blocks.
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
    /*public void toTEI(HeaderParser headerParser,
                      ReferenceSegmenter referenceSegmenter,
                      CitationParser citationParser, boolean consolidateHeader,
                      boolean consolidateCitations, boolean peer, BiblioItem catalogue,
                      boolean withStyleSheet, boolean onlyHeader) throws Exception {
        addFeaturesDocument();

        if (blocks == null) {
            return;
        }

        BasicStructureBuilder.firstPass(this);
        String header = getHeader(false);
        if (header == null) {
            header = getHeaderLastHope();
        }
        String body = getBody();
        List<BibDataSet> bds = new ArrayList<>();

        if (!onlyHeader) {
            if (titleMatchNum) {
                // have we miss a section title? in case of standard numbering
                // we can try to recover missing ones
                int i = 0;
                for (Block block : blocks) {
                    Integer ii = i;

                    if ((!blockFooters.contains(ii))
                            && (!blockDocumentHeaders.contains(ii))
                            && (!blockHeaders.contains(ii))
                            && (!blockReferences.contains(ii))
                            && (!blockFigures.contains(ii))
                            && (!blockTables.contains(ii))
                            && (!blockSectionTitles.contains(ii))
                            && (!acknowledgementBlocks.contains(ii))) {
                        String localText = block.getText();

                        if (localText != null) {
                            localText = localText.replace("\n", " ");
                            localText = localText.replace("  ", " ");
                            localText = localText.trim();
                            if ((localText.length() > 9)
                                    && (localText.length() < 150)) {

                                Matcher m1 = BasicStructureBuilder.headerNumbering1
                                        .matcher(localText);
                                Matcher m2 = BasicStructureBuilder.headerNumbering2
                                        .matcher(localText);
                                if (m1.find()) {
                                    if (block.getNbTokens() < 10) {
                                        int count1 = TextUtilities
                                                .countDigit(localText);
                                        if (count1 < localText.length() / 4) {
                                            if (!blockSectionTitles
                                                    .contains(ii))
                                                blockSectionTitles.add(ii);
                                        }
                                    }
                                } else if (m2.find()) {
                                    if (block.getNbTokens() < 10) {
                                        int count1 = TextUtilities
                                                .countDigit(localText);
                                        if (count1 < localText.length() / 4) {
                                            if (!blockSectionTitles
                                                    .contains(ii))
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

            List<String> tokenizations = getTokenizationsReferences();
            if ((tokenizations == null) || (tokenizations.size() == 0)) {
                // we need to tokenize the reference section, as it has not been
                // done until this stage
                String refSection = getReferences();
                StringTokenizer st = new StringTokenizer(refSection, " \t\n"
                        + TextUtilities.fullPunctuations, true);
                while (st.hasMoreTokens()) {
                    tokenizations.add(st.nextToken());
                }
            }

            List<LabeledReferenceResult> refs;
            if (citationParser != null) {
                refs = referenceSegmenter.extract(Joiner.on("").join(tokenizations));
                if (refs != null) {
                    for (LabeledReferenceResult ref: refs) {
                        BiblioItem bib = citationParser.processing(ref.getReferenceText(),
                                consolidateCitations);

                        BibDataSet bd = new BibDataSet();
                        bd.setResBib(bib);
                        bd.setRefSymbol(ref.getLabel());
                        bd.setRawBib(ref.getReferenceText());
                        bds.add(bd);
                    }
                }
            }
        }
        // BiblioItem biblio =
        // headerParser.processingHeaderBlock(consolidateHeader, this);
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
                            Matcher ma1 = BasicStructureBuilder.introduction
                                    .matcher(localText);
                            Matcher ma2 = BasicStructureBuilder.references
                                    .matcher(localText);
                            if ((ma1.find()) || (ma2.find())) {
                                if (((localText.startsWith("1.")) || (localText
                                        .startsWith("1 ")))
                                        || ((localText.startsWith("2.")) || (localText
                                        .startsWith("2 ")))) {
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

                if ((!blockFooters.contains(ii))
                        && (!blockDocumentHeaders.contains(ii))
                        && (!blockHeaders.contains(ii))
                        && (!blockReferences.contains(ii))
                        && (!blockFigures.contains(ii))
                        && (!blockTables.contains(ii))
                        && (!blockSectionTitles.contains(ii))
                        && (!acknowledgementBlocks.contains(ii))) {
                    String localText = block.getText();

                    if (localText != null) {
                        localText = localText.replace("  ", " ");
                        localText = localText.trim();
                        String lastLocalText = null;

                        StringTokenizer st = new StringTokenizer(localText,
                                "\n");
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
        tei = teiFormater.toTEIBody(biblio, bds, peer, withStyleSheet,
                onlyHeader);
    }
	*/

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

                        if (innd != -1) {
//							if (candidate == true) {
//							}
                        } else
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
            i++;
        }
    }

    public String getHeaderFeatured(boolean firstPass, boolean getHeader,
                                    boolean withRotation) {
        if (getHeader) {
            // String theHeader = getHeaderZFN(firstPass);
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
                switch (text) {
                    case "(":
                    case "[":
                        features.punctType = "OPENBRACKET";
                        break;
                    case ")":
                    case "]":
                        features.punctType = "ENDBRACKET";
                        break;
                    case ".":
                        features.punctType = "DOT";
                        break;
                    case ",":
                        features.punctType = "COMMA";
                        break;
                    case "-":
                        features.punctType = "HYPHEN";
                        break;
                    case "\"":
                    case "\'":
                    case "`":
                        features.punctType = "QUOTE";
                        break;
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

    // heuristics to get the header section... should be replaced be a global
    // CRF structure recognition
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
            blockFooters = new ArrayList<>();

        if (blockHeaders == null)
            blockHeaders = new ArrayList<>();

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
                blockFooters = new ArrayList<>();
            }
            if (blockDocumentHeaders == null) {
                blockDocumentHeaders = new ArrayList<>();
            }
            if (blockHeaders == null) {
                blockHeaders = new ArrayList<>();
            }
            if (blockReferences == null) {
                blockReferences = new TreeSet<>();
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
        List<String> results = new ArrayList<>();
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
        return labeledBlocks.get(segmentationLabel.getLabel());
    }

    public String getDocumentPartText(SegmentationLabel segmentationLabel) {
        return getDocumentPieceText(getDocumentPart(segmentationLabel));
    }
}