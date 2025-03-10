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
import org.apache.commons.io.IOUtils;
import org.grobid.core.analyzers.Analyzer;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.*;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.counters.FigureCounters;
import org.grobid.core.engines.counters.TableRejectionCounters;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.Cluster;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.GraphicObjectType;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.PDFAnnotation;
import org.grobid.core.layout.Page;
import org.grobid.core.layout.VectorGraphicBoxCalculator;
import org.grobid.core.sax.*;
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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Class for representing, processing and exchanging a document item.
 *
 */

public class Document implements Serializable {

    public static final long serialVersionUID = 1L;

    protected static final Logger LOGGER = LoggerFactory.getLogger(Document.class);
    public static final int MAX_FIG_BOX_DISTANCE = 70;
    protected transient final DocumentSource documentSource;

    protected String pathXML = null; // XML representation of the current PDF file

    protected String lang = null;

    // layout structure of the document
    protected transient List<Page> pages = null;
    protected transient List<Cluster> clusters = null;
    protected transient List<Block> blocks = null;

    protected List<Integer> blockDocumentHeaders = null;

    protected transient FeatureFactory featureFactory = null;

    // map of labels (e.g. <reference> or <footnote>) to document pieces
    protected transient SortedSetMultimap<String, DocumentPiece> labeledBlocks;

    // original tokenization and tokens - in order to recreate the original
    // strings and spacing
    protected List<LayoutToken> tokenizations = null;

    // list of bibliographical references with context
    protected transient Map<String, BibDataSet> teiIdToBibDataSets = null;
    protected transient List<BibDataSet> bibDataSets = null;

    // header of the document - if extracted and processed
    protected transient BiblioItem resHeader = null;

    // full text as tructure TEI - if extracted and processed
    protected String tei;

    protected transient ReferenceMarkerMatcher referenceMarkerMatcher;

    public void setImages(List<GraphicObject> images) {
        this.images = images;
    }

    // list of bitmaps and vector graphics of the document
    protected transient List<GraphicObject> images = null;

    // list of PDF annotations as present in the PDF source file
    protected transient List<PDFAnnotation> pdfAnnotations = null;

    // the document outline (or bookmark) embedded in the PDF, if present
    protected transient DocumentNode outlineRoot = null;

    protected transient Metadata metadata = null;

    protected transient Multimap<Integer, GraphicObject> imagesPerPage = LinkedListMultimap.create();

    // some statistics regarding the document - useful for generating the features
    protected double maxCharacterDensity = 0.0;
    protected double minCharacterDensity = 0.0;
    protected double maxBlockSpacing = 0.0;
    protected double minBlockSpacing = 0.0;
    protected int documentLenghtChar = -1; // length here is expressed as number of characters

    // not used
    protected int beginBody = -1;
    protected int beginReferences = -1;

    protected boolean titleMatchNum = false; // true if the section titles of the document are numbered

    protected transient List<Figure> figures;
    protected transient Predicate<GraphicObject> validGraphicObjectPredicate;
    protected int m;

    protected transient List<Table> tables;
    protected transient List<Equation> equations;

    // the analyzer/tokenizer used for processing this document
    protected transient Analyzer analyzer = GrobidAnalyzer.getInstance();

    // map of sequence of LayoutTokens for the fulltext model labels
    //Map<String, List<LayoutTokenization>> labeledTokenSequences = null;

    protected double byteSize = 0; 

    public Document(DocumentSource documentSource) {
        this.documentSource = documentSource;
        setPathXML(documentSource.getXmlFile());
        this.byteSize = documentSource.getByteSize();
    }

    protected Document() {
        this.documentSource = null;
    }

    public static Document createFromText(String text) {
        Document doc = new Document();
        doc.fromText(text);
        if (text != null) {
            try {
                final byte[] utf8Bytes = text.getBytes(StandardCharsets.UTF_8);
                doc.byteSize = utf8Bytes.length;
            } catch(Exception e) {
                LOGGER.warn("Could not set the original text document size in bytes for UTF-8 encoding");
            }
        }
        return doc;
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

    public List<PDFAnnotation> getPDFAnnotations() {
        return pdfAnnotations;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Set the path to the XML file generated by xml2pdf
     */
    protected void setPathXML(File pathXML) {
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

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public Analyzer getAnalyzer() {
        return this.analyzer;
    }

    public List<LayoutToken> fromText(final String text) {
        List<String> toks = null;
        try {
            toks = GrobidAnalyzer.getInstance().tokenize(text);
        } catch (Exception e) {
            LOGGER.error("Fail tokenization for " + text, e);
        }

        tokenizations = toks.stream().map(LayoutToken::new).collect(Collectors.toList());

        blocks = new ArrayList<>();
        Block b = new Block();
        for (LayoutToken lt : tokenizations) {
            b.addToken(lt);
        }

        Page p = new Page(1);
        b.setPage(p);
        //b.setText(text);
        pages = new ArrayList<>();
        pages.add(p);
        blocks.add(b);
        p.addBlock(b);
        b.setStartToken(0);
        b.setEndToken(toks.size() - 1);

        images = new ArrayList<>();
        return tokenizations;
    }

    /**
    * See https://github.com/kermitt2/grobid/pull/475
    * Ignore invalid unicode characters
    *
    *  @author Daniel Ecer
    */
    protected static void parseInputStream(InputStream in, SAXParser saxParser, DefaultHandler handler) 
        throws SAXException, IOException {
        CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder();
        utf8Decoder.onMalformedInput(CodingErrorAction.IGNORE);
        utf8Decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
        saxParser.parse(new InputSource(new InputStreamReader(in, utf8Decoder)), handler);
    }

    protected static void parseInputStream(InputStream in, SAXParserFactory saxParserFactory, DefaultHandler handler) 
        throws SAXException, IOException, ParserConfigurationException {
        parseInputStream(in, saxParserFactory.newSAXParser(), handler);
    }

    /**
     * Parser PDFALTO output representation and get the tokenized form of the document.
     *
     * @return list of features
     */
    public List<LayoutToken> addTokenizedDocument(GrobidAnalysisConfig config) {
        // The XML generated by pdfalto might contains invalid UTF characters due to the "garbage-in" of the PDF,
        // which will result in a "fatal" parsing failure (the joy of XML!). The solution could be to prevent
        // having those characters in the input XML by cleaning it first

        images = new ArrayList<>();
        PDFALTOSaxHandler parser = new PDFALTOSaxHandler(this, images);

        // we set possibly the particular analyzer to be used for tokenization of the PDF elements
        if (config.getAnalyzer() != null)
            parser.setAnalyzer(config.getAnalyzer());
        pdfAnnotations = new ArrayList<PDFAnnotation>();
        PDFALTOAnnotationSaxHandler parserAnnot = new PDFALTOAnnotationSaxHandler(this, pdfAnnotations);
        PDFALTOOutlineSaxHandler parserOutline = new PDFALTOOutlineSaxHandler(this);
        PDFMetadataSaxHandler parserMetadata = new PDFMetadataSaxHandler(this);

        // get a SAX parser factory
        SAXParserFactory spf = SAXParserFactory.newInstance();

        tokenizations = null;

        File file = new File(pathXML);
		File fileAnnot = new File(pathXML+"_annot.xml");
        File fileOutline = new File(pathXML+"_outline.xml");
        File fileMetadata = new File(pathXML+"_metadata.xml");
        FileInputStream in = null;
        try {
            // parsing of the pdfalto file
            in = new FileInputStream(file);
            // in = new XMLFilterFileInputStream(file); // -> to filter invalid XML characters

            // get a new instance of parser
            parseInputStream(in, spf, parser);
            tokenizations = parser.getTokenization();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error("Cannot close input stream", e);
                }
            }
        } catch (GrobidException e) {
            throw e;
        } catch (Exception e) {
            throw new GrobidException("Cannot parse file: " + file, e, GrobidExceptionStatus.PARSING_ERROR);
        } finally {
            IOUtils.closeQuietly(in);
        }

        if (fileAnnot.exists()) {
            try {
                // parsing of the annotation XML file (for annotations in the PDF)
                in = new FileInputStream(fileAnnot);
                SAXParser p = spf.newSAXParser();
                p.parse(in, parserAnnot);
            } catch (GrobidException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.warn("Cannot parse file: " + fileAnnot, e, GrobidExceptionStatus.PARSING_ERROR);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

        if (fileOutline.exists()) {
            try {
                // parsing of the outline XML file (for PDF bookmark) - not used currently so minor
                in = new FileInputStream(fileOutline);
                SAXParser p = spf.newSAXParser();
                p.parse(in, parserOutline);
                outlineRoot = parserOutline.getRootNode();
            } catch (GrobidException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.warn("Cannot parse file: " + fileOutline, e, GrobidExceptionStatus.PARSING_ERROR);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        if (fileMetadata.exists()) {
            try {
                // parsing of the metadata XML file (for PDF embedded metadata)
                in = new FileInputStream(fileMetadata);
                SAXParser p = spf.newSAXParser();
                p.parse(in, parserMetadata);
                metadata = parserMetadata.getMetadata();
            } catch (GrobidException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.warn("Cannot parse file: " + fileMetadata, e, GrobidExceptionStatus.PARSING_ERROR);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

        if (getBlocks() == null) {
            throw new GrobidException("PDF parsing resulted in empty content", GrobidExceptionStatus.NO_BLOCKS);
        }

        // calculating main area
        calculatePageMainAreas();

        // calculating boxes for pages
        if (config.isProcessVectorGraphics()) {
            try {
                for (GraphicObject o : VectorGraphicBoxCalculator.calculate(this).values()) {
                    images.add(o);
                }
            } catch (Exception e) {
                throw new GrobidException("Cannot process vector graphics: " + file, e, GrobidExceptionStatus.PARSING_ERROR);
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
            if (elements.size() > 100) {
                imagesPerPage.removeAll(pageNum);
                Engine.getCntManager().i(FigureCounters.TOO_MANY_FIGURES_PER_PAGE);
            } else {
                ArrayList<GraphicObject> res = glueImagesIfNecessary(pageNum, Lists.newArrayList(elements));
                if (res != null) {
                    imagesPerPage.removeAll(pageNum);
                    imagesPerPage.putAll(pageNum, res);
                }
            }
        }

        // we filter out possible line numbering for review works
        // filterLineNumber();
        return tokenizations;
    }

    private void calculatePageMainAreas() {
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
    }

    protected ArrayList<GraphicObject> glueImagesIfNecessary(Integer pageNum, List<GraphicObject> graphicObjects) {

        List<Pair<Integer, Integer>> toGlue = new ArrayList<>();
//        List<GraphicObject> cur = new ArrayList<>();

//        List<GraphicObject> graphicObjects = new ArrayList<>(objs);

        int start = 0, end = 0;
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

    protected static int getCoordItem(ElementCounter<Integer> cnt, boolean getMin) {
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
        List<Page> pages = getPages();
        int p = 0;
        for (Page page : pages) {
            if ((page.getBlocks() != null) && (page.getBlocks().size() > 0)) {
                for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                    Block block = page.getBlocks().get(blockIndex);
                    String localText = block.getText();
                    if ((localText != null) && (localText.length() > 0)) {
                        localText = localText.trim();
                        Matcher DOIMatcher = TextUtilities.DOIPattern.matcher(localText);
                        while (DOIMatcher.find()) {
                            String theDOI = DOIMatcher.group();
                            if (!results.contains(theDOI)) {
                                results.add(theDOI);
                            }
                        }
                    }
                }
            }
            if (p > 1)
                break;
            p++;
        }
        return results;
    }

    public String getTei() {
        return tei;
    }

    public void setTei(String tei) {
        this.tei = tei;
    }

    public List<Integer> getBlockDocumentHeaders() {
        return blockDocumentHeaders;
    }

    public DocumentNode getOutlineRoot() {
        return outlineRoot;
    }

    public void setOutlineRoot(DocumentNode outlineRoot) {
        this.outlineRoot = outlineRoot;
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

    public void setBlockDocumentHeaders(List<Integer> blockDocumentHeaders) {
        this.blockDocumentHeaders = blockDocumentHeaders;
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
        // some cleaning of the labels
        if (this.bibDataSets != null) {
            for (BibDataSet bds : this.bibDataSets) {
                String marker = bds.getRefSymbol();
                if (marker != null) {
                    //marker = marker.replace(".", "");
                    //marker = marker.replace(" ", "");
                    marker = marker.replaceAll("[\\.\\[\\]()\\-\\s]", "");
                    bds.setRefSymbol(marker);
                }
            }
        }
        int cnt = 0;
        for (BibDataSet bds : bibDataSets) {
            bds.getResBib().setOrdinal(cnt++);
        }
    }

    public synchronized ReferenceMarkerMatcher getReferenceMarkerMatcher() throws EntityMatcherException {
        if (referenceMarkerMatcher == null) {
            if (this.bibDataSets != null)
                referenceMarkerMatcher = new ReferenceMarkerMatcher(this.bibDataSets, Engine.getCntManager());
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

    // helper
    public List<LayoutToken> getDocumentPieceTokenization(DocumentPiece dp) {
        return tokenizations.subList(dp.getLeft().getTokenDocPos(), dp.getRight().getTokenDocPos() + 1);
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

    /**
     *  Get the document part corresponding to a particular segment type
     */
    public SortedSet<DocumentPiece> getDocumentPart(TaggingLabel segmentationLabel) {
        if (labeledBlocks == null) {
            LOGGER.debug("labeledBlocks is null");
            return null;
        }
        if (segmentationLabel.getLabel() == null) {
            System.out.println("segmentationLabel.getLabel()  is null");
        }
        return labeledBlocks.get(segmentationLabel.getLabel());
    }

    public String getDocumentPartText(TaggingLabel segmentationLabel) {
        SortedSet<DocumentPiece> pieces = getDocumentPart(segmentationLabel);
        if (pieces == null) {
            return null;
        } else {
            return getDocumentPieceText(getDocumentPart(segmentationLabel));
        }
    }

    /**
     * Give the list of LayoutToken corresponding to some document parts and
     * a global document tokenization.
     */
    public static List<LayoutToken> getTokenizationParts(SortedSet<DocumentPiece> documentParts,
                                                         List<LayoutToken> tokenizations) {
        if (documentParts == null)
            return null;

        List<LayoutToken> tokenizationParts = new ArrayList<>();
        for (DocumentPiece docPiece : documentParts) {
            DocumentPointer dp1 = docPiece.getLeft();
            DocumentPointer dp2 = docPiece.getRight();

            int tokens = dp1.getTokenDocPos();
            int tokene = dp2.getTokenDocPos();
            for (int i = tokens; i < tokene; i++) {
                tokenizationParts.add(tokenizations.get(i));
            }
        }
        return tokenizationParts;
    }

    public BibDataSet getBibDataSetByTeiId(String teiId) {
        return teiIdToBibDataSets.get(teiId);
    }

    protected static double MIN_DISTANCE = 100.0;

    /**
     * Return the list of graphical object touching the given block.
     */
    public static List<GraphicObject> getConnectedGraphics(Block block, Document doc) {
        List<GraphicObject> images = null;
        for (GraphicObject image : doc.getImages()) {
            if (block.getPageNumber() != image.getPage())
                continue;
            if (((Math.abs((image.getY() + image.getHeight()) - block.getY()) < MIN_DISTANCE) ||
                    (Math.abs(image.getY() - (block.getY() + block.getHeight())) < MIN_DISTANCE)) //||
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

    // deal with false positives, with footer stuff, etc.
    public void postProcessTables() {
        for (Table table : tables) {
            if (!table.firstCheck()) {
                table.setGoodTable(false);
                continue;
            }

            // cleaning up tokens
            List<LayoutToken> fullDescResult = new ArrayList<>();
            BoundingBox curBox = BoundingBox.fromLayoutToken(table.getFullDescriptionTokens().get(0));
            int distanceThreshold = 200;
            for (LayoutToken fdt : table.getFullDescriptionTokens()) {
                BoundingBox b = BoundingBox.fromLayoutToken(fdt);
                if (b.getX() < 0) {
                    fullDescResult.add(fdt);
                    continue;
                }
                if (b.distanceTo(curBox) > distanceThreshold) {
                    Engine.getCntManager().i(TableRejectionCounters.HEADER_NOT_CONSECUTIVE);
                    table.setGoodTable(false);
                    break;
                } else {
                    curBox = curBox.boundBox(b);
                    fullDescResult.add(fdt);
                }
            }
            table.getFullDescriptionTokens().clear();
            table.getFullDescriptionTokens().addAll(fullDescResult);

            List<LayoutToken> contentResult = new ArrayList<>();

            curBox = BoundingBox.fromLayoutToken(table.getContentTokens().get(0));
            for (LayoutToken fdt : table.getContentTokens()) {
                BoundingBox b = BoundingBox.fromLayoutToken(fdt);
                if (b.getX() < 0) {
                    contentResult.add(fdt);
                    continue;
                }

                if (b.distanceTo(curBox) > distanceThreshold) {
                    break;
                } else {
                    curBox = curBox.boundBox(b);
                    contentResult.add(fdt);
                }
            }
            table.getContentTokens().clear();
            table.getContentTokens().addAll(contentResult);

            table.setGoodTable(table.secondCheck());
        }
    }

    public void assignGraphicObjectsToFigures() {
    /**
     * This method assigns graphic objects to figures based on the proximity of the graphic object to the figure caption.
     * It also modifies captions and textarea for existing figures
     * @return the modified figures
     */
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
                    f.setCaption(new StringBuilder(LayoutTokensUtil.toText(LayoutTokensUtil.dehyphenize(realCaptionTokens))));
                    f.setCaptionLayoutTokens(realCaptionTokens);
                    pageFigures.add(f);
                }
            }

            if (pageFigures.isEmpty()) {
                continue;
            }

            List<GraphicObject> it = Lists.newArrayList(Iterables.filter(imagesPerPage.get(pageNum), Figure.GRAPHIC_OBJECT_PREDICATE));

            // filtering those images that for some reason are outside of main area
            it = it.stream().filter(go -> {
                BoundingBox mainArea = getPage(go.getBoundingBox().getPage()).getMainArea();
                return mainArea.intersect(go.getBoundingBox());
            }).collect(Collectors.toList());

            List<GraphicObject> vectorBoxGraphicObjects =
                    Lists.newArrayList(Iterables.filter(imagesPerPage.get(pageNum), Figure.VECTOR_BOX_GRAPHIC_OBJECT_PREDICATE));

            // case where figure caption is covered almost precisely but the vector graphics box -- filter those out - they are covered by caption anyway
            vectorBoxGraphicObjects = vectorBoxGraphicObjects.stream().filter(go -> {
                for (Figure f : pageFigures) {
                    BoundingBox intersection = BoundingBoxCalculator.calculateOneBox(f.getLayoutTokens(), true).boundingBoxIntersection(go.getBoundingBox());
                    if(intersection != null && intersection.area() / go.getBoundingBox().area() > 0.5) {
                        return false;
                    }
                }
                return true;
            }).collect(Collectors.toList());

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

            // easy case when we don't have any vector boxes -- easier to correlation figure captions with bitmap images
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
                        Engine.getCntManager().i("FigureCounters", "ASSIGNED_GRAPHICS_TO_FIGURES");
                    }

                }
            } else {
                if (pageFigures.size() != graphicObjects.size()) {
                    Engine.getCntManager().i(FigureCounters.SKIPPED_DUE_TO_MISMATCH_OF_CAPTIONS_AND_VECTOR_AND_BITMAP_GRAPHICS);
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
                        Engine.getCntManager().i("FigureCounters", "ASSIGNED_GRAPHICS_TO_FIGURES");
                    }

                }

            }

        }

        // special case, when we didn't detect figures, but there is a nice figure on this page
        int maxPage = pages.size();
        for (int pageNum = 1; pageNum <= maxPage; pageNum++) {
            if (!figureMap.containsKey(pageNum)) {

                ArrayList<GraphicObject> it = Lists.newArrayList(Iterables.filter(imagesPerPage.get(pageNum), Figure.GRAPHIC_OBJECT_PREDICATE));

                List<GraphicObject> vectorBoxGraphicObjects = 
                    Lists.newArrayList(Iterables.filter(imagesPerPage.get(pageNum), Figure.VECTOR_BOX_GRAPHIC_OBJECT_PREDICATE));

                List<GraphicObject> graphicObjects = new ArrayList<>();

                l:
                for (GraphicObject bgo : it) {
                    // intersecting with vector graphics is dangerous, so better skip than have a false positive
                    for (GraphicObject vgo : vectorBoxGraphicObjects) {
                        if (bgo.getBoundingBox().intersect(vgo.getBoundingBox())) {
                            continue l;
                        }
                    }
                    // if graphics object intersect between each other, it's most likely a composition and we cannot take just 1
                    for (GraphicObject bgo2 : it) {
                        if (bgo2 != bgo && bgo.getBoundingBox().intersect(bgo2.getBoundingBox())) {
                            continue l;
                        }
                    }

                    graphicObjects.add(bgo);
                }

                graphicObjects.addAll(vectorBoxGraphicObjects);

                if (graphicObjects.size() == it.size()) {
                    for (GraphicObject o : graphicObjects) {

                        if (badStandaloneFigure(o)) {
                            Engine.getCntManager().i(FigureCounters.SKIPPED_BAD_STANDALONE_FIGURES);
                            continue;
                        }

                        Figure f = new Figure();
                        f.setPage(pageNum);
                        f.setGraphicObjects(Collections.singletonList(o));

                        figures.add(f);
                        Engine.getCntManager().i("FigureCounters", "STANDALONE_FIGURES");
                        LOGGER.debug("Standalone figure on page: " + pageNum);

                    }
                }
            }
        }

    }

    private boolean badStandaloneFigure(GraphicObject o) {
        if (o.getBoundingBox().area() < 50000) {
            Engine.getCntManager().i(FigureCounters.SKIPPED_SMALL_STANDALONE_FIGURES);
            return true;
        }

        if (o.getBoundingBox().area() / pages.get(o.getPage() - 1).getMainArea().area() > 0.6) {
            Engine.getCntManager().i(FigureCounters.SKIPPED_BIG_STANDALONE_FIGURES);
            return true;
        }

        return false;
    }

    protected boolean isValidBitmapGraphicObject(GraphicObject go) {
        if (go.getWidth() * go.getHeight() < 1000) {
            return false;
        }

        if (go.getWidth() < 50) {
            return false;
        }

        if (go.getHeight() < 50) {
            return false;
        }

        BoundingBox mainArea = getPage(go.getBoundingBox().getPage()).getMainArea();

        if (!mainArea.contains(go.getBoundingBox()) && go.getWidth() * go.getHeight() < 10000) {
            return false;
        }

        return true;
    }

    // graphic boxes could overlap captions, we need to cut this from a vector box
    protected void recalculateVectorBoxCoords(Figure f, GraphicObject g) {

        //TODO: make it robust - now super simplistic

        BoundingBox captionBox = BoundingBoxCalculator.calculateOneBox(f.getLayoutTokens(), true);
        BoundingBox originalGoBox = g.getBoundingBox();
        if (captionBox.intersect(originalGoBox)) {
            int p = originalGoBox.getPage();

            double cx1 = captionBox.getX();
            double cx2 = captionBox.getX2();
            double cy1 = captionBox.getY();
            double cy2 = captionBox.getY2();

            double fx1 = originalGoBox.getX();
            double fx2 = originalGoBox.getX2();
            double fy1 = originalGoBox.getY();
            double fy2 = originalGoBox.getY2();


            m = 5;
            BoundingBox bestBox = null;
            try {
                //if caption is on the bottom
                BoundingBox bottomArea = BoundingBox.fromTwoPoints(p, fx1, fy1, fx2, cy1 - m);
                bestBox = bottomArea;
            } catch (Exception e) {
                // no op
            }

            try {
                // caption is on the right
                BoundingBox rightArea = BoundingBox.fromTwoPoints(p, fx1, fy1, cx1 - m, fy2);
                if (bestBox == null || rightArea.area() > bestBox.area()) {
                    bestBox = rightArea;
                }
            } catch (Exception e) {
                //no op
            }

            try {
                BoundingBox topArea = BoundingBox.fromTwoPoints(p, fx1, cy2 + m, fx2, fy2);
                if (bestBox == null || topArea.area() > bestBox.area()) {
                    bestBox = topArea;
                }
            } catch (Exception e) {
                //no op
            }

            try {
                BoundingBox leftArea = BoundingBox.fromTwoPoints(p, cx2 + m, fy1, fx2, fy2);
                if (bestBox == null || leftArea.area() > bestBox.area()) {
                    bestBox = leftArea;
                }
            } catch (Exception e) {
                //no op
            }

            if (bestBox != null && bestBox.area() > 600) {
                g.setBoundingBox(bestBox);
            }
        }

//        if (captionBox.intersect(originalGoBox)) {
//            if (originalGoBox.getY() < captionBox.getY() - 5) {
//                g.setBoundingBox(BoundingBox.fromTwoPoints(p, originalGoBox.getX(), originalGoBox.getY(), originalGoBox.getX2(), captionBox.getY() - 5));
//            }
//        }

    }

    protected List<LayoutToken> getFigureLayoutTokens(Figure f) {
        List<LayoutToken> result = new ArrayList<>();
        Iterator<Integer> it = f.getBlockPtrs().iterator();

        while (it.hasNext()) {
            Integer blockPtr = it.next();

            Block figBlock = getBlocks().get(blockPtr);
            String norm = LayoutTokensUtil.toText(figBlock.getTokens()).trim().toLowerCase();
            if (norm.startsWith("fig")
                || norm.startsWith("abb")
                || norm.startsWith("scheme")
                || norm.startsWith("photo")
                || norm.startsWith("gambar")
                || norm.startsWith("quadro")
                || norm.startsWith("wykres")
                || norm.startsWith("fuente")
            ) {
                result.addAll(figBlock.getTokens());

                while (it.hasNext()) {
                    BoundingBox prevBlock = BoundingBox.fromPointAndDimensions(figBlock.getPageNumber(), figBlock.getX(), figBlock.getY(), figBlock.getWidth(), figBlock.getHeight());
                    blockPtr = it.next();
                    Block b = getBlocks().get(blockPtr);
                    if (BoundingBox.fromPointAndDimensions(b.getPageNumber(), b.getX(), b.getY(), b.getWidth(), b.getHeight()).distanceTo(prevBlock) < 15) {
                        result.addAll(b.getTokens());
                        figBlock = b;
                    } else {
                        // A TEMPORARY trick would be to iterate to all the following blocks
                        // and place them into the discarded token list of the figure
                        f.addDiscardedPieceTokens(b.getTokens());
                        while (it.hasNext()) {
                            blockPtr = it.next();
                            figBlock = getBlocks().get(blockPtr);
                            f.addDiscardedPieceTokens(figBlock.getTokens());
                        }
                        break;
                    }
                }
                break;
            } else {
                f.addDiscardedPieceTokens(figBlock.getTokens());
//                LOGGER.info("BAD_FIGIRE_LABEL: " + norm);
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

//    public static void setConnectedGraphics(Figure figure,
//                                            List<LayoutToken> tokenizations,
//                                            Document doc) {
//        try {
//            List<GraphicObject> localImages = null;
//            // set the intial figure area based on its layout tokens
//            LayoutToken startToken = figure.getStartToken();
//            LayoutToken endToken = figure.getEndToken();
//            int start = figure.getStart();
//            int end = figure.getEnd();
//
//            double maxRight = 0.0; // right border of the figure
//            double maxLeft = 10000.0; // left border of the figure
//            double maxUp = 10000.0; // upper border of the figure
//            double maxDown = 0.0; // bottom border of the figure
//            for (int i = start; i <= end; i++) {
//                LayoutToken current = tokenizations.get(i);
//                if ((figure.getPage() == -1) && (current.getPage() != -1))
//                    figure.setPage(current.getPage());
//                if ((current.x >= 0.0) && (current.x < maxLeft))
//                    maxLeft = current.x;
//                if ((current.y >= 0.0) && (current.y < maxUp))
//                    maxUp = current.y;
//                if ((current.x >= 0.0) && (current.x + current.width > maxRight))
//                    maxRight = current.x + current.width;
//                if ((current.y >= 0.0) && (current.y + current.height > maxDown))
//                    maxDown = current.y + current.height;
//            }
//
//            figure.setX(maxLeft);
//            figure.setY(maxUp);
//            figure.setWidth(maxRight - maxLeft);
//            figure.setHeight(maxDown - maxUp);
//
//            // attach connected graphics based on estimated figure area
//            for (GraphicObject image : doc.getImages()) {
//                if (image.getType() == GraphicObjectType.VECTOR)
//                    continue;
//                if (figure.getPage() != image.getPage())
//                    continue;
////System.out.println(image.toString());
//                if (((Math.abs((image.getY() + image.getHeight()) - figure.getY()) < MIN_DISTANCE) ||
//                        (Math.abs(image.getY() - (figure.getY() + figure.getHeight())) < MIN_DISTANCE)) //||
//                    //( (Math.abs((image.x+image.width) - figure.getX()) < MIN_DISTANCE) ||
//                    //(Math.abs(image.x - (figure.getX()+figure.getWidth())) < MIN_DISTANCE) )
//                        ) {
//                    // the image is at a distance of at least MIN_DISTANCE from one border
//                    // of the block on the vertical/horizontal axis
//                    if (localImages == null)
//                        localImages = new ArrayList<GraphicObject>();
//                    localImages.add(image);
//                }
//            }
//
//            // re-evaluate figure area with connected graphics
//            if (localImages != null) {
//                for (GraphicObject image : localImages) {
//                    if (image.getX() < maxLeft)
//                        maxLeft = image.getX();
//                    if (image.getY() < maxUp)
//                        maxUp = image.getY();
//                    if (image.getX() + image.getWidth() > maxRight)
//                        maxRight = image.getX() + image.getWidth();
//                    if (image.getY() + image.getHeight() > maxDown)
//                        maxDown = image.getY() + image.getHeight();
//                }
//            }
//
//            figure.setGraphicObjects(localImages);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void produceStatistics() {
        // document length in characters
        // we calculate current document length and initialize the body tokenization structure
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

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setEquations(List<Equation> equations) {
        this.equations = equations;
    }

    public List<Equation> getEquations() {
        return equations;
    }

    public void setResHeader(BiblioItem resHeader) {
        this.resHeader = resHeader;
    }


    static public List<LayoutToken> getTokens(List<LayoutToken> tokenizations, int offsetBegin, int offsetEnd) {
        return getTokensFrom(tokenizations, offsetBegin, offsetEnd, 0);
    }

    static public List<LayoutToken> getTokensFrom(List<LayoutToken> tokenizations,
                                                  int offsetBegin,
                                                  int offsetEnd,
                                                  int startTokenIndex) {
        List<LayoutToken> result = new ArrayList<LayoutToken>();
        for (int p = startTokenIndex; p < tokenizations.size(); p++) {
            LayoutToken currentToken = tokenizations.get(p);
            if ((currentToken == null) || (currentToken.getText() == null))
                continue;
            if (currentToken.getOffset() + currentToken.getText().length() < offsetBegin)
                continue;
            if (currentToken.getOffset() > offsetEnd)
                return result;
            result.add(currentToken);
        }
        return result;
    }

    /**
     * Initialize the mapping between sequences of LayoutToken and 
     * fulltext model labels. 
     * @param labeledResult labeled sequence as produced by the CRF model
     * @param tokenization List of LayoutToken for the body parts
     */
    /*public void generalFullTextResultMapping(String labeledResult, List<LayoutToken> tokenizations) {
        if (labeledTokenSequences == null)
            labeledTokenSequences = new TreeMap<String, List<LayoutTokenization>>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.FULLTEXT, labeledResult, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();
        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            List<LayoutToken> clusterTokens = cluster.concatTokens();
            List<LayoutTokenization> theList = labeledTokenSequences.get(clusterLabel.toString());
            if (theList == null)
                theList = new ArrayList<LayoutTokenization>();
            LayoutTokenization newTokenization = new LayoutTokenization(clusterTokens);
            theList.add(newTokenization);
            labeledTokenSequences.put(clusterLabel.getLabel(), theList);
        }
    }*/

    public double getByteSize() {
        return byteSize;
    }

    public void setByteSize(double size) {
        byteSize = size;
    }

    public String getMD5() {
        if (documentSource != null)
            return documentSource.getMD5();
        else
            return null;
    }
}
