package org.grobid.core.engines.ebook;

import org.grobid.core.GrobidModels;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.AbstractParser;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorFulltext;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.TextUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * Structure extraction of an ebook.
 *
 * @author Patrice Lopez
 */

public class BookStructureParser extends AbstractParser {
    private File tmpPath = null;

    // default bins for relative position
    private static final int NBBINS = 12;

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
        File file = new File(inputFile);
        DocumentSource source = null;
        try {
            source = DocumentSource.fromPdf(file);
            Document doc = new Document(DocumentSource.fromPdf(file));
            String PDFFileName = file.getName();
            doc.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());

            if (doc.getBlocks() == null) {
                throw new Exception("PDF parsing resulted in empty content");
            }

            String fulltext = getFulltextFeatured(doc);
            List<LayoutToken> tokenizations = doc.getTokenizationsFulltext();

            // we write the header untagged
            String outPathFulltext = pathFullText + "/" + PDFFileName.replace(".pdf", ".fulltext");
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");
            writer.write(fulltext + "\n");
            writer.close();

            // clear internal context
            String rese = label(fulltext);

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
            if (source != null) {
                source.close(false);
            }
        }
    }

    public String getFulltextFeatured(Document doc) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();
        StringBuilder fulltext = new StringBuilder();
        String currentFont = null;
        int currentFontSize = -1;

        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }

        // vector for features
        FeaturesVectorFulltext features;
        FeaturesVectorFulltext previousFeatures = null;
        boolean endblock;
        boolean endPage = true;
        boolean newPage = true;
        boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        int documentLength = 0;
        int pageLength = 0; // length of the current page

        List<LayoutToken> tokenizationsBody = new ArrayList<LayoutToken>();
        List<LayoutToken> tokenizations = doc.getTokenizations();

        // we calculate current document length and intialize the body tokenization structure
        for (Block block : blocks) {
            List<LayoutToken> tokens = block.getTokens();
            if (tokens == null)
                continue;
            documentLength += tokens.size();
        }

        //int blockPos = dp1.getBlockPtr();
        for (int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
            Block block = blocks.get(blockIndex);

            // we estimate the length of the page where the current block is
            if (start || endPage) {
                boolean stop = false;
                pageLength = 0;
                for (int z = blockIndex; (z < blocks.size()) && !stop; z++) {
                    String localText2 = blocks.get(z).getText();
                    if (localText2 != null) {
                        if (localText2.contains("@PAGE")) {
                            if (pageLength > 0) {
                                if (blocks.get(z).getTokens() != null) {
                                    pageLength += blocks.get(z).getTokens()
                                            .size();
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
                // System.out.println("pageLength: " + pageLength);
            }
            if (start) {
                newPage = true;
                start = false;
            }
            boolean newline;
            boolean previousNewline = false;
            endblock = false;

            if (endPage) {
                newPage = true;
                mm = 0;
            }

            String localText = block.getText();
            if (localText != null) {
                if (localText.contains("@PAGE")) {
                    mm = 0;
                    // pageLength = 0;
                    endPage = true;
                    newPage = false;
                } else {
                    endPage = false;
                }
            }

            List<LayoutToken> tokens = block.getTokens();
            if (tokens == null) {
                //blockPos++;
                continue;
            }

            int n = 0;// token position in current block
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
                    else if (features.blockStatus == null) {
                        features.blockStatus = "BLOCKEND";
                        endblock = true;
                    }
                }

                if (newPage) {
                    //features.pageStatus = "PAGESTART";
                    newPage = false;
                    endPage = false;
                    if (previousFeatures != null) {
                        //previousFeatures.pageStatus = "PAGEEND";
					}
                } else {
                    //features.pageStatus = "PAGEIN";
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

                /*if (featureFactory.test_common(text)) {
                    features.commonName = true;
                }

                if (featureFactory.test_names(text)) {
                    features.properName = true;
                }

                if (featureFactory.test_month(text)) {
                    features.month = true;
                }*/

                Matcher m = featureFactory.isDigit.matcher(text);
                if (m.find()) {
                    features.digit = "ALLDIGIT";
                }

                /*Matcher m2 = featureFactory.YEAR.matcher(text);
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
                }*/

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

                if (features.punctType == null)
                    features.punctType = "NOPUNCT";

                features.relativeDocumentPosition = featureFactory
                        .linearScaling(nn, documentLength, NBBINS);
                // System.out.println(mm + " / " + pageLength);
                features.relativePagePosition = featureFactory
                        .linearScaling(mm, pageLength, NBBINS);

                // fulltext.append(features.printVector());
                if (previousFeatures != null)
                    fulltext.append(previousFeatures.printVector());
                n++;
                mm++;
                nn++;
                previousFeatures = features;
            }
        }
        if (previousFeatures != null)
            fulltext.append(previousFeatures.printVector());

        return fulltext.toString();
    }

    /**
     * Extract results from a labelled header in the training format without any string modification.
     */
    private StringBuffer trainingExtraction(String result,
                                            List<LayoutToken> tokenizations) {
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
            List<String> localFeatures = new ArrayList<String>();
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
                        String tokOriginal = tokenizations.get(p).t();
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