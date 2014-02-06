package org.grobid.core.engines.ebook;

import org.chasen.crfpp.Tagger;
import org.grobid.core.GrobidModels;
import org.grobid.core.document.Document;
import org.grobid.core.engines.AbstractParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.TextUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Structure extraction of an ebook.
 *
 * @author Patrice Lopez
 */

public class BookStructureParser extends AbstractParser {
    private File tmpPath = null;

    public BookStructureParser() {
        super(GrobidModels.EBOOK);
        GrobidProperties.getInstance();
		tmpPath = GrobidProperties.getTempPath();
    }

    /**
     * Process the full text of the specified ebook pdf and format the result as training data.
     */
    public void createTrainingFullTextEbook(String inputFile,
                                            String pathFullText,
                                            String pathTEI,
                                            int id) throws Exception {
        Document doc = new Document(inputFile, tmpPath.getAbsolutePath());
        String pathXML = null;
        try {
            int startPage = -1;
            int endPage = -1;
            File file = new File(inputFile);
            String PDFFileName = file.getName();
            pathXML = doc.pdf2xml(false, false, startPage, endPage, inputFile, tmpPath.getAbsolutePath(), false); //without timeout,
            //no force pdf reloading
            // inputFile is the pdf file, tmpPath is the tmp directory for the lxml file,
            // path is the resource path
            // we do not extract the images in the pdf file
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

            // we write the header untagged
            String outPathFulltext = pathFullText + "/" + PDFFileName.replace(".pdf", ".fulltext");
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");
            writer.write(fulltext + "\n");
            writer.close();

            // clear internal context
            StringTokenizer st = new StringTokenizer(fulltext, "\n");
            String rese = getTaggerResult(st);

            StringBuffer bufferFulltext = trainingExtraction(rese, tokenizations);

            // write the TEI file to reflect the extract layout of the text as extracted from the pdf
            writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
                    "/" + PDFFileName.replace(".pdf", GrobidProperties.FILE_ENDING_TEI_FULLTEXT)), false), "UTF-8");
            writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
                    "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">\n");

            writer.write(bufferFulltext.toString());
            writer.write("\n\t</text>\n</tei>\n");
            writer.close();
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            doc.cleanLxmlFile(pathXML, false);
        }
    }

    /**
     * Extract results from a labelled header in the training format without any string modification.
     */
    private StringBuffer trainingExtraction(String result,
                                            ArrayList<String> tokenizations) {
        // this is the main buffer for the whole header
        StringBuffer buffer = new StringBuffer();

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
            StringTokenizer stt = new StringTokenizer(tok, " \t");
            ArrayList<String> localFeatures = new ArrayList<String>();
            int i = 0;

            boolean newLine = false;
            boolean newPage = false;
            int ll = stt.countTokens();
            while (stt.hasMoreTokens()) {
                String s = stt.nextToken().trim();
                if (i == 0) {
                    s2 = TextUtilities.HTMLEncode(s); // lexical token

                    boolean strop = false;
                    while ((!strop) & (p < tokenizations.size())) {
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
                    if (s.equals("LINESTART")) {
                        newLine = true;
                    } else if (s.equals("PAGESTART")) {
                        newPage = true;
                    }
                    localFeatures.add(s);
                }
                i++;
            }

            if (newLine) {
                buffer.append("<lb/>");
            }
            if (newPage) {
                // only for more readable presentation
                //buffer.append("\n");
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
                testClosingTag(buffer, currentTag0, lastTag0, s1);
            }

            boolean output;

            output = writeField(buffer, s1, lastTag0, s2, "<front>", "<front>", addSpace, 3);
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<other>", "<other>", addSpace, 3);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag, s2, "<toc>", "<toc>", addSpace, 3);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<section>", "<section>", addSpace, 3);
            }
            if (!output) {
                output = writeFieldBeginEnd(buffer, s1, lastTag0, s2, "<page>", "<page>", addSpace, 3);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<reference>", "<reference>", addSpace, 3);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<index>", "<index>", addSpace, 3);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<page_header>", "<page_header>", addSpace, 3);
            }
            if (!output) {
                output = writeField(buffer, s1, lastTag0, s2, "<page_footnote>", "<page_footnote>", addSpace, 3);
            }

            lastTag = s1;

            if (!st.hasMoreTokens()) {
                if (lastTag != null) {
                    testClosingTag(buffer, "", currentTag0, s1);
                }
            }
        }

        return buffer;
    }

    private boolean writeField(StringBuffer buffer,
                               String s1,
                               String lastTag0,
                               String s2,
                               String field,
                               String outField,
                               boolean addSpace,
                               int nbIndent) {
        boolean result = false;
        if ((s1.equals(field)) | (s1.equals("I-" + field))) {
            result = true;
            if (s1.equals(lastTag0) | s1.equals("I-" + lastTag0)) {
                if (addSpace)
                    buffer.append(" ").append(s2);
                else
                    buffer.append(s2);
            } else {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField).append(s2);
            }
        }
        return result;
    }

    /**
     * This is for writing fields for fields where begin and end of field matter, like paragraph or item
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
        if ((s1.equals(field)) | (s1.equals("I-" + field))) {
            result = true;
            if (lastTag0.equals("I-" + field)) {
                if (addSpace)
                    buffer.append(" ").append(s2);
                else
                    buffer.append(s2);
            } else if (lastTag0.equals(field) & s1.equals(field)) {
                if (addSpace)
                    buffer.append(" ").append(s2);
                else
                    buffer.append(s2);
            } else {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField).append(s2);
            }
        }
        return result;
    }

    private boolean testClosingTag(StringBuffer buffer,
                                   String currentTag0,
                                   String lastTag0,
                                   String currentTag) {
        boolean res = false;
        if (!currentTag0.equals(lastTag0) | currentTag.equals("I-<page>")) {
            res = true;
            // we close the current tag
            if (lastTag0.equals("<other>")) {
                buffer.append("</other>\n");
            } else if (lastTag0.equals("<front>")) {
                buffer.append("</front>\n");
            } else if (lastTag0.equals("<page>")) {
                buffer.append("</page>\n");
            } else if (lastTag0.equals("<toc>")) {
                buffer.append("</toc>\n");
            } else if (lastTag0.equals("<section>")) {
                buffer.append("</section>\n");
            } else if (lastTag0.equals("<reference>")) {
                buffer.append("</reference>\n");
            } else if (lastTag0.equals("<index>")) {
                buffer.append("</index>\n");
            } else if (lastTag0.equals("<page_header>")) {
                buffer.append("</page_header>\n");
            } else if (lastTag0.equals("<page_footnote>")) {
                buffer.append("</page_footnote>\n");
            } else {
                res = false;
            }

        }
        return res;
    }


}