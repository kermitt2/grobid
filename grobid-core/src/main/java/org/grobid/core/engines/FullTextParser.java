package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.document.BasicStructureBuilder;
import org.grobid.core.document.Document;
import org.grobid.core.document.TEIFormater;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.lang.Language;
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
import java.util.StringTokenizer;

/**
 * @author Patrice Lopez
 */
public class FullTextParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FullTextParser.class);

    private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
    
    private HeaderParser headerParser = null;
    private CitationParser citationParser = null;
    //	private String tmpPathName = null;
    private Document doc = null;
    private File tmpPath = null;
    private String pathXML = null;
	private BiblioItem resHeader = null;  

    /**
     * TODO some documentation...
     */
    public FullTextParser() {
        super(GrobidModels.FULLTEXT);
        tmpPath = GrobidProperties.getInstance().getTempPath();
    }

    /**
     * TODO some documentation...
     *
     * @param input                filename of pdf file
     * @param consolidateHeader if consolidate header
     * @param consolidateCitations if consolidate citations
     * @return result
     */
    
    public String processing(String input, boolean consolidateHeader, boolean consolidateCitations) {
        if (input == null) {
            throw new GrobidResourceException("Cannot process pdf file, because input file was null.");
        }
        File inputFile = new File(input);
        if (!inputFile.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because input file '" +
                    inputFile.getAbsolutePath() + "' does not exists.");
        }
        if (tmpPath == null) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        }
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                    tmpPath.getAbsolutePath() + "' does not exists.");
        }
        doc = new Document(input, tmpPath.getAbsolutePath());
        try {
            int startPage = -1;
            int endPage = -1;
            pathXML = doc.pdf2xml(true, false, startPage, endPage, input, tmpPath.getAbsolutePath(), false);
            //with timeout,
            //no force pdf reloading
            // input is the pdf absolute path, tmpPath is the temp. directory for the temp. lxml file,
            // path is the resource path
            // and we don't extract images in the PDF file
            if (pathXML == null) {
                throw new GrobidResourceException("PDF parsing fails, " +
                        "because path of where to store xml file is null.");
            }
            doc.setPathXML(pathXML);
            //doc.addFeaturesDocument();

            if (headerParser == null) {
                headerParser = new HeaderParser();
            }
            if (citationParser == null) {
                citationParser = new CitationParser();
            }

            String tei = doc.toTEI(headerParser, citationParser, consolidateHeader, consolidateCitations,
                    false, null, false, false);
			resHeader = doc.getResHeader();
            LOGGER.debug(tei);
            return tei;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            // keep it clean when leaving...
            doc.cleanLxmlFile(pathXML, false);
        }
    }

    public String processing2(String input,
                              boolean consolidateHeader,
                              boolean consolidateCitations) throws Exception {
        if (input == null) {
            throw new GrobidResourceException("Cannot process pdf file, because input file was null.");
        }
        File inputFile = new File(input);
        if (!inputFile.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because input file '" +
                    inputFile.getAbsolutePath() + "' does not exists.");
        }
        if (tmpPath == null) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        }
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                    tmpPath.getAbsolutePath() + "' does not exists.");
        }
        doc = new Document(input, tmpPath.getAbsolutePath());
        try {
            int startPage = -1;
            int endPage = -1;
            pathXML = doc.pdf2xml(true, false, startPage, endPage, input, tmpPath.getAbsolutePath(), true);
            //with timeout,
            //no force pdf reloading
            // input is the pdf absolute path, tmpPath is the temp. directory for the temp. lxml file,
            // path is the resource path
            // and we process images in the pdf file
            if (pathXML == null) {
                throw new Exception("PDF parsing fails");
            }
            doc.setPathXML(pathXML);
            ArrayList<String> tokenizations = doc.addFeaturesDocument();

            if (doc.getBlocks() == null) {
                throw new Exception("PDF parsing resulted in empty content");
            }

            String fulltext = doc.getFulltextFeatured(true, true);

//            StringTokenizer st = new StringTokenizer(fulltext, "\n");
            String rese = label(fulltext);

            // set the different sections of the Document object
            doc = BasicStructureBuilder.resultSegmentation(doc, rese, tokenizations);

            // header processing
            if (headerParser == null) {
                headerParser = new HeaderParser();
            }
            resHeader = new BiblioItem();
            headerParser.processingHeaderBlock(consolidateHeader, doc, resHeader);
            // the language identification is normally done during the header parsing, but only
            // based on header information.
            // LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
            Language langu = languageUtilities.runLanguageId(resHeader.getTitle() + "\n" + doc.getBody());
            if (langu != null) {
                String lang = langu.getLangId();
                doc.setLanguage(lang);
                resHeader.setLanguage(lang);
            }

            // citation processing
            if (citationParser == null) {
                citationParser = new CitationParser();
            }
            ArrayList<BibDataSet> resCitations;

            //ArrayList<String> tokenizationsRef = doc.getTokenizationsReferences();
            //System.out.println(tokenizationsRef.toString());

            //resCitations = BasicStructureBuilder.giveReferenceSegments(doc);
            resCitations = doc.bibDataSets;
			
            if (resCitations != null) {
                for (BibDataSet bds : resCitations) {
                    String marker = bds.getRefSymbol();
                    if (marker != null) {
                        marker = marker.replace(".", "");
                        marker = marker.replace(" ", "");
                        bds.setRefSymbol(marker);
                    }
                    BiblioItem bib = citationParser.processing(bds.getRawBib(), consolidateCitations);
                    bds.setResBib(bib);
                }
            }

            // final combination
            return toTEI(doc, rese, tokenizations, resHeader, false, null, false);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            // keep it clean when leaving...
            doc.cleanLxmlFile(pathXML, false);
        }
    }


    /**
     * Process the full text of the specified pdf and format the result as training data.
     *
     * @param inputFile input file
     * @param pathFullText path to fulltext
     * @param pathTEI path to TEI
     * @param id id
     */
    public void createTrainingFullText(String inputFile,
                                       String pathFullText,
                                       String pathTEI,
                                       int id) {
        if (tmpPath == null)
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                    tmpPath.getAbsolutePath() + "' does not exists.");
        }
        doc = new Document(inputFile, tmpPath.getAbsolutePath());
        try {
            int startPage = -1;
            int endPage = -1;
            File file = new File(inputFile);
            if (!file.exists()) {
                throw new GrobidResourceException("Cannot train for fulltext, becuase file '" +
                        file.getAbsolutePath() + "' does not exists.");
            }
            String PDFFileName = file.getName();
            pathXML = doc.pdf2xml(true, false, startPage, endPage, inputFile, tmpPath.getAbsolutePath(), true);
            //with timeout,
            //no force pdf reloading
            // pathPDF is the pdf file, tmpPath is the tmp directory for the lxml file,
            // path is the resource path
            // and we don't extract images in the pdf file
            if (pathXML == null) {
                throw new Exception("PDF parsing fails");
            }
            doc.setPathXML(pathXML);
            doc.addFeaturesDocument();

            if (doc.getBlocks() == null) {
                throw new Exception("PDF parsing resulted in empty content");
            }

            String fulltext = doc.getFulltextFeatured(true, true);
            ArrayList<String> tokenizations = doc.getTokenizationsFulltext();

            // we write the full text untagged
            String outPathFulltext = pathFullText + "/" + PDFFileName.replace(".pdf", ".training.fulltext");
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");
            writer.write(fulltext + "\n");
            writer.close();

//            StringTokenizer st = new StringTokenizer(fulltext, "\n");
            String rese = label(fulltext);
            StringBuffer bufferFulltext = trainingExtraction(rese, tokenizations);

            // write the TEI file to reflect the extract layout of the text as extracted from the pdf
            writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
                    "/" + PDFFileName.replace(".pdf", ".training.fulltext.tei.xml")), false), "UTF-8");
            writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
                    "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">\n");

            writer.write(bufferFulltext.toString());
            writer.write("\n\t</text>\n</tei>\n");
            writer.close();

            // output of the identified citations as traning date

            // buffer for the reference block
            StringBuilder allBufferReference = new StringBuilder();
            // we need to rebuild the found citation string as it appears
            String input = "";
            ArrayList<String> inputs = new ArrayList<String>();
            int q = 0;
            StringTokenizer st = new StringTokenizer(rese, "\n");
            while (st.hasMoreTokens() && (q < tokenizations.size())) {
                String line = st.nextToken();
                String theTotalTok = tokenizations.get(q);
                String theTok = tokenizations.get(q);
                while (theTok.equals(" ") || theTok.equals("\t") || theTok.equals("\n")) {
                    q++;
                    theTok = tokenizations.get(q);
                    theTotalTok += theTok;
                }
                if (line.endsWith("I-<reference>")) {
                    if (input.trim().length() > 1) {
                        inputs.add(input.trim());
                        input = "";
                    }
                    input += "\n" + theTotalTok;
                } else if (line.endsWith("<reference>")) {
                    input += theTotalTok;
                }
                q++;
            }
            if (input.trim().length() > 1) {
                inputs.add(input.trim());
                if (citationParser == null) {
                    citationParser = new CitationParser();
                }
                for (String inpu : inputs) {
                    ArrayList<String> inpus = new ArrayList<String>();
                    inpus.add(inpu);
                    StringBuilder bufferReference = citationParser.trainingExtraction(inpus);
                    if (bufferReference != null) {
                        allBufferReference.append(bufferReference.toString()).append("\n");
                    }
                }
            }

            if (allBufferReference != null) {
                if (allBufferReference.length() > 0) {
                    Writer writerReference = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
                            "/" + PDFFileName.replace(".pdf", ".training.references.xml")), false), "UTF-8");
                    writerReference.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    writerReference.write("<citations>\n");

                    writerReference.write(allBufferReference.toString());

                    writerReference.write("</citations>\n");
                    writerReference.close();
                }
            }

        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid training" +
                    " data generation for full text.", e);
        } finally {
            doc.cleanLxmlFile(pathXML, true);
        }
    }

    /**
     * Return the Document object of the last processed pdf file.
     */
    public Document getDoc() {
        return doc;
    }

	/**
     * Return the Biblio object corresponding to the last processed pdf file.
     */
    public BiblioItem getResHeader() {
        return resHeader;
    }

    /**
     * Extract results from a labelled full text in the training format without any string modification.
     *
     * @param result reult
     * @param tokenizations toks
     * @return extraction
     */
    private StringBuffer trainingExtraction(String result,
                                            ArrayList<String> tokenizations) {
        // this is the main buffer for the whole full text
        StringBuffer buffer = new StringBuffer();
        try {
            StringTokenizer st = new StringTokenizer(result, "\n");
            String s1 = null;
            String s2 = null;
            String lastTag = null;

            // current token position
            int p = 0;
            boolean start = true;
            boolean openFigure = false;
            boolean headFigure = false;
            boolean descFigure = false;
            boolean tableBlock = false;

            while (st.hasMoreTokens()) {
                boolean addSpace = false;
                String tok = st.nextToken().trim();

                if (tok.length() == 0) {
                    continue;
                }
                StringTokenizer stt = new StringTokenizer(tok, " \t");
                ArrayList<String> localFeatures = new ArrayList<String>();
                int i = 0;

                boolean newLine = false;
                int ll = stt.countTokens();
                while (stt.hasMoreTokens()) {
                    String s = stt.nextToken().trim();
                    if (i == 0) {
                        s2 = TextUtilities.HTMLEncode(s); // lexical token

                        boolean strop = false;
                        while ((!strop) && (p < tokenizations.size())) {
                            String tokOriginal = tokenizations.get(p);
                            if (tokOriginal.equals(" ")) {
                                addSpace = true;
                            } else if (tokOriginal.equals(s)) {
                                strop = true;
                            }
                            p++;
                        }
                    } else if (i == ll - 1) {
                        s1 = s; // current tag
                    } else {
                        if (s.equals("LINESTART"))
                            newLine = true;
                        localFeatures.add(s);
                    }
                    i++;
                }

                if (newLine && !start) {
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

                boolean closeParagraph = false;
                if (lastTag != null) {
                    closeParagraph = testClosingTag(buffer, currentTag0, lastTag0, s1);
                }

                boolean output = false;

                if (!currentTag0.equals("<table>") &&
                        !currentTag0.equals("<trash>") &&
                        !currentTag0.equals("<figure_head>") &&
                        !currentTag0.equals("<label>")) {
                    if (openFigure) {
                        buffer.append("\n\t\t\t</figure>\n\n");
                    }
                    openFigure = false;
                    headFigure = false;
                    descFigure = false;
                    tableBlock = false;
                }

                output = writeField(buffer, s1, lastTag0, s2, "<header>", "<front>", addSpace, 3);
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<other>", "<note type=\"other\">", addSpace, 3);
                }
                // for paragraph we must distinguish starting and closing tags
                if (!output) {
                    if (closeParagraph) {
                        output = writeFieldBeginEnd(buffer, s1, "", s2, "<paragraph>", "<p>", addSpace, 3);
                    } else {
                        output = writeFieldBeginEnd(buffer, s1, lastTag, s2, "<paragraph>", "<p>", addSpace, 3);
                    }
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<page_header>", "<note place=\"headnote\">",
                            addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<page_footnote>", "<note place=\"footnote\">",
                            addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<page>", "<page>", addSpace, 3);
                }
                if (!output) {
                    output = writeFieldBeginEnd(buffer, s1, lastTag0, s2, "<reference>", "<bibl>", addSpace, 3);
                }
                if (!output) {
                    if (closeParagraph) {
                        output = writeField(buffer, s1, "", s2, "<reference_marker>", "<label>", addSpace, 3);
                    } else
                        output = writeField(buffer, s1, lastTag0, s2, "<reference_marker>", "<label>", addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<citation_marker>", "<ref type=\"biblio\">",
                            addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<section>", "<head>", addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<subsection>", "<head>", addSpace, 3);
                }
                if (!output) {
                    if (openFigure) {
                        output = writeField(buffer, s1, lastTag0, s2, "<trash>", "<trash>", addSpace, 4);
                    } else {
                        //output = writeField(buffer, s1, lastTag0, s2, "<trash>", "<figure>\n\t\t\t\t<trash>",
                        output = writeField(buffer, s1, lastTag0, s2, "<trash>", "<trash>",
                                addSpace, 3);
                        if (output) {
                            openFigure = true;
                        }
                    }
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<equation>", "<formula>", addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<figure_marker>", "<ref type=\"figure\">",
                            addSpace, 3);
                }
                if (!output) {
                    if (openFigure) {
                        if (tableBlock && (!lastTag0.equals("<table>")) && (currentTag0.equals("<table>"))) {
                            buffer.append("\n\t\t\t</figure>\n\n");
                            output = writeField(buffer, s1, lastTag0, s2, "<figure>\n\t\t\t\t<table>", "<figure>",
                                    addSpace, 3);
                            if (output) {
                                tableBlock = true;
                                descFigure = false;
                                headFigure = false;
                            }
                        } else {
                            output = writeField(buffer, s1, lastTag0, s2, "<table>", "<table>", addSpace, 4);
                            if (output) {
                                tableBlock = true;
                            }
                        }
                    } else {
                        output = writeField(buffer, s1, lastTag0, s2, "<table>", "<figure>\n\t\t\t\t<table>",
                                addSpace, 3);
                        if (output) {
                            openFigure = true;
                            tableBlock = true;
                        }
                    }
                }
                if (!output) {
                    if (openFigure) {
                        if (descFigure && (!lastTag0.equals("<label>")) && (currentTag0.equals("<label>"))) {
                            buffer.append("\n\t\t\t</figure>\n\n");
                            output = writeField(buffer, s1, lastTag0, s2, "<label>", "<figure>\n\t\t\t\t<figDesc>",
                                    addSpace, 3);
                            if (output) {
                                descFigure = true;
                                tableBlock = false;
                                headFigure = false;
                            }
                        } else {
                            output = writeField(buffer, s1, lastTag0, s2, "<label>", "<figDesc>", addSpace, 4);
                            if (output) {
                                descFigure = true;
                            }
                        }
                    } else {
                        output = writeField(buffer, s1, lastTag0, s2, "<label>", "<figure>\n\t\t\t\t<figDesc>",
                                addSpace, 3);
                        if (output) {
                            openFigure = true;
                            descFigure = true;
                        }
                    }
                }
                if (!output) {
                    if (openFigure) {
                        if (headFigure && (!lastTag0.equals("<figure_head>")) &&
                                (currentTag0.equals("<figure_head>"))) {
                            buffer.append("\n\t\t\t</figure>\n\n");
                            output = writeField(buffer, s1, lastTag0, s2, "<figure_head>", "<figure>\n\t\t\t\t<head>",
                                    addSpace, 3);
                            if (output) {
                                descFigure = false;
                                tableBlock = false;
                                headFigure = true;
                            }
                        } else {
                            output = writeField(buffer, s1, lastTag0, s2, "<figure_head>", "<head>", addSpace, 4);
                            if (output) {
                                headFigure = true;
                            }
                        }
                    } else {
                        output = writeField(buffer, s1, lastTag0, s2, "<figure_head>", "<figure>\n\t\t\t\t<head>",
                                addSpace, 3);
                        if (output) {
                            openFigure = true;
                            headFigure = true;
                        }
                    }
                }
                // for item we must distinguish starting and closing tags
                if (!output) {
                    output = writeFieldBeginEnd(buffer, s1, lastTag, s2, "<item>", "<item>", addSpace, 3);
                }

                lastTag = s1;

                if (!st.hasMoreTokens()) {
                    if (lastTag != null) {
                        testClosingTag(buffer, "", currentTag0, s1);
                    }
                    if (openFigure) {
                        buffer.append("\n\t\t\t</figure>\n\n");
                    }
                }
                if (start) {
                    start = false;
                }
            }

            return buffer;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    /**
     * TODO some documentation...
     *
     * @param buffer
     * @param s1
     * @param lastTag0
     * @param s2
     * @param field
     * @param outField
     * @param addSpace
     * @param nbIndent
     * @return
     */
    private boolean writeField(StringBuffer buffer,
                               String s1,
                               String lastTag0,
                               String s2,
                               String field,
                               String outField,
                               boolean addSpace,
                               int nbIndent) {
        boolean result = false;
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
            if (s1.equals(lastTag0) || s1.equals("I-" + lastTag0)) {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            }
            /*else if (lastTag0 == null) {
                   for(int i=0; i<nbIndent; i++) {
                       buffer.append("\t");
                   }
                     buffer.append(outField+s2);
               }*/
            else if (field.equals("<citation_marker>")) {
                if (addSpace)
                    buffer.append(" " + outField + s2);
                else
                    buffer.append(outField + s2);
            } else if (field.equals("<figure_marker>")) {
                if (addSpace)
                    buffer.append(" " + outField + s2);
                else
                    buffer.append(outField + s2);
            } else if (field.equals("<reference_marker>")) {
                if (!lastTag0.equals("<reference>") && !lastTag0.equals("<reference_marker>")) {
                    for (int i = 0; i < nbIndent; i++) {
                        buffer.append("\t");
                    }
                    buffer.append("<bibl>");
                }
                if (addSpace)
                    buffer.append(" " + outField + s2);
                else
                    buffer.append(outField + s2);
            } else if (lastTag0 == null) {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField + s2);
            } else if (!lastTag0.equals("<citation_marker>") && !lastTag0.equals("<figure_marker>")
                    && !lastTag0.equals("<figure>")) {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField + s2);
            } else {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            }
        }
        return result;
    }

    /**
     * This is for writing fields for fields where begin and end of field matter, like paragraph or item
     *
     * @param buffer
     * @param s1
     * @param lastTag0
     * @param s2
     * @param field
     * @param outField
     * @param addSpace
     * @param nbIndent
     * @return
     */
    private boolean writeFieldBeginEnd(StringBuffer buffer,
                                       String s1,
                                       String lastTag0,
                                       String s2,
                                       String field,
                                       String outField,
                                       boolean addSpace,
                                       int nbIndent) {
        boolean result = false;
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
            if (lastTag0.equals("I-" + field)) {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            } else if (lastTag0.equals(field) && s1.equals(field)) {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            } else if (!lastTag0.equals("<citation_marker>") && !lastTag0.equals("<figure_marker>")
                    && !lastTag0.equals("<figure>") && !lastTag0.equals("<reference_marker>")) {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField + s2);
            } else {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            }
        }
        return result;
    }

    /**
     * TODO some documentation
     *
     * @param buffer
     * @param currentTag0
     * @param lastTag0
     * @param currentTag
     * @return
     */
    private boolean testClosingTag(StringBuffer buffer,
                                   String currentTag0,
                                   String lastTag0,
                                   String currentTag) {
        boolean res = false;
        // reference_marker and citation_marker are two exceptions because they can be embedded

        if (!currentTag0.equals(lastTag0) || currentTag.equals("I-<paragraph>") || currentTag.equals("I-<item>")) {
            if (currentTag0.equals("<citation_marker>") || currentTag0.equals("<figure_marker>")) {
                return res;
            }

            res = false;
            // we close the current tag
            if (lastTag0.equals("<other>")) {
                buffer.append("</note>\n\n");
            } else if (lastTag0.equals("<header>")) {
                buffer.append("</front>\n\n");
            } else if (lastTag0.equals("<page_header>")) {
                buffer.append("</note>\n\n");
            } else if (lastTag0.equals("<page_footnote>")) {
                buffer.append("</note>\n\n");
            } else if (lastTag0.equals("<reference>")) {
                buffer.append("</bibl>\n\n");
                res = true;
            } else if (lastTag0.equals("<paragraph>")) {
                buffer.append("</p>\n\n");
                res = true;
            } else if (lastTag0.equals("<section>")) {
                buffer.append("</head>\n\n");
            } else if (lastTag0.equals("<subsection>")) {
                buffer.append("</head>\n\n");
            } else if (lastTag0.equals("<equation>")) {
                buffer.append("</formula>\n\n");
            } else if (lastTag0.equals("<table>")) {
                buffer.append("</table>\n");
            } else if (lastTag0.equals("<label>")) {
                buffer.append("</figDesc>\n");
            } else if (lastTag0.equals("<figure_head>")) {
                buffer.append("</head>\n\n");
            } else if (lastTag0.equals("<item>")) {
                buffer.append("</item>\n\n");
            } else if (lastTag0.equals("<trash>")) {
                buffer.append("</trash>\n\n");
            } else if (lastTag0.equals("<reference_marker>")) {
                buffer.append("</label>");
            } else if (lastTag0.equals("<citation_marker>")) {
                buffer.append("</ref>");
            } else if (lastTag0.equals("<figure_marker>")) {
                buffer.append("</ref>");
            } else if (lastTag0.equals("<page>")) {
                buffer.append("</page>\n\n");
            } else {
                res = false;
            }

        }
        return res;
    }

    /**
     * Create the TEI representation for a document based on the parsed header, references
     * and body sections.
     */
    private String toTEI(Document doc,
                         String rese,
                         ArrayList<String> tokenizations,
                         BiblioItem resHeader,
                         boolean peer,
                         BiblioItem catalogue,
                         boolean withStyleSheet) {
        if (doc.blocks == null) {
            return null;
        }
        TEIFormater teiFormater = new TEIFormater(doc);
        StringBuffer tei;
        try {
            tei = teiFormater.toTEIHeader(resHeader, peer, withStyleSheet, null);
            tei = teiFormater.toTEIBodyML(tei, rese, resHeader, doc.bibDataSets, tokenizations, doc);
            tei = teiFormater.toTEIReferences(tei, doc.bibDataSets);

            tei.append("\t\t</back>\n");
            tei.append("\t</text>\n");
            tei.append("</TEI>\n");
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return tei.toString();
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (headerParser != null) {
            headerParser.close();
            headerParser = null;
        }
        if (citationParser != null) {
            citationParser.close();
            citationParser = null;
        }

    }
}