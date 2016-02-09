package org.grobid.core.document;

import com.google.common.collect.Iterables;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.Cluster;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for building basic structures in a document item.
 *
 * @author Patrice Lopez
 */
public class BasicStructureBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicStructureBuilder.class);

	// note: these regular expressions will disappear as a new CRF model is now covering 
	// the overall document segmentation
    static public Pattern introduction =
            Pattern.compile("^\\b*(Introduction?|Einleitung|INTRODUCTION|Acknowledge?ments?|Acknowledge?ment?|Background?|Content?|Contents?|Motivations?|1\\.\\sPROBLEMS?|1\\.(\\n)?\\sIntroduction?|1\\.\\sINTRODUCTION|I\\.(\\s)+Introduction|1\\.\\sProblems?|I\\.\\sEinleitung?|1\\.\\sEinleitung?|1\\sEinleitung?|1\\sIntroduction?)",
                    Pattern.CASE_INSENSITIVE);
    static public Pattern introductionStrict =
            Pattern.compile("^\\b*(1\\.\\sPROBLEMS?|1\\.(\\n)?\\sIntroduction?|1\\.(\\n)?\\sContent?|1\\.\\sINTRODUCTION|I\\.(\\s)+Introduction|1\\.\\sProblems?|I\\.\\sEinleitung?|1\\.\\sEinleitung?|1\\sEinleitung?|1\\sIntroduction?)",
                    Pattern.CASE_INSENSITIVE);

    static public Pattern abstract_ = Pattern.compile("^\\b*\\.?(abstract?|résumé?|summary?|zusammenfassung?)",
            Pattern.CASE_INSENSITIVE);
    static public Pattern keywords = Pattern.compile("^\\b*\\.?(keyword?|key\\s*word?|mots\\s*clefs?)",
            Pattern.CASE_INSENSITIVE);

    static public Pattern references =
            Pattern.compile("^\\b*(References?|REFERENCES?|Bibliography|BIBLIOGRAPHY|" +
                    "References?\\s+and\\s+Notes?|References?\\s+Cited|REFERENCE?\\s+CITED|REFERENCES?\\s+AND\\s+NOTES?|Références|Literatur|" +
                    "LITERATURA|Literatur|Referências|BIBLIOGRAFIA|Literaturverzeichnis|Referencias|LITERATURE CITED|References and Notes)", Pattern.CASE_INSENSITIVE);
    static public Pattern header = Pattern.compile("^((\\d\\d?)|([A-Z](I|V|X)*))(\\.(\\d)*)*\\s(\\D+)");
    //    static public Pattern header2 = Pattern.compile("^\\d\\s\\D+");
    static public Pattern figure = Pattern.compile("(figure\\s|fig\\.|sch?ma)", Pattern.CASE_INSENSITIVE);
    static public Pattern table = Pattern.compile("^(T|t)able\\s|tab|tableau", Pattern.CASE_INSENSITIVE);
    static public Pattern equation = Pattern.compile("^(E|e)quation\\s");
    private static Pattern acknowledgement = Pattern.compile("(acknowledge?ments?|acknowledge?ment?)",
            Pattern.CASE_INSENSITIVE);
    static public Pattern headerNumbering1 = Pattern.compile("^(\\d+)\\.?\\s");
    static public Pattern headerNumbering2 = Pattern.compile("^((\\d+)\\.)+(\\d+)\\s");
    static public Pattern headerNumbering3 = Pattern.compile("^((\\d+)\\.)+\\s");
    static public Pattern headerNumbering4 = Pattern.compile("^([A-Z](I|V|X)*(\\.(\\d)*)*\\s)");
//    static public Pattern enumeratedList = Pattern.compile("^|\\s(\\d+)\\.?\\s");

    private static Pattern startNum = Pattern.compile("^(\\d)+\\s");
    private static Pattern endNum = Pattern.compile("\\s(\\d)+$");

    /**
     * Filter out line numbering possibly present in the document. This can be frequent for
     * document in a review/submission format and degrades strongly the machine learning
     * extraction results. 
	 *
	 * -> Not used !
     *
     * @param doc a document
     * @return if found numbering
     */
    public boolean filterLineNumber(Document doc) {
        // we first test if we have a line numbering by checking if we have an increasing integer
        // at the begin or the end of each block
        boolean numberBeginLine = false;
        boolean numberEndLine = false;

        boolean foundNumbering = false;

        int currentNumber = -1;
        int lastNumber = -1;
        int i = 0;
        for (Block block : doc.getBlocks()) {
//            Integer ii = i;

            String localText = block.getText();
            List<LayoutToken> tokens = block.getTokens();

            if ((localText != null) && (tokens != null)) {
                if (tokens.size() > 0) {
                    // we get the first and last token iof the block
                    //String tok1 = tokens.get(0).getText();
                    //String tok2 = tokens.get(tokens.size()).getText();
                    localText = localText.trim();

                    Matcher ma1 = startNum.matcher(localText);
                    Matcher ma2 = endNum.matcher(localText);

                    if (ma1.find()) {
                        String groupStr = ma1.group(0);
                        try {
                            currentNumber = Integer.parseInt(groupStr);
                            numberBeginLine = true;
                        } catch (NumberFormatException e) {
                            currentNumber = -1;
                        }
                    } else if (ma2.find()) {
                        String groupStr = ma2.group(0);
                        try {
                            currentNumber = Integer.parseInt(groupStr);
                            numberEndLine = true;
                        } catch (NumberFormatException e) {
                            currentNumber = -1;
                        }
                    }

                    if (lastNumber != -1) {
                        if (currentNumber == lastNumber + 1) {
                            foundNumbering = true;
                            break;
                        }
                    } else
                        lastNumber = currentNumber;
                }
            }
            i++;

            if (i > 5) {
                break;
            }
        }

        i = 0;
        if (foundNumbering) {
            // we have a line numbering, so we filter them
            int counter = 1; // we start at 1, if the actual start is 0,
            // it will remain (as it is negligeable)

            for (Block block : doc.getBlocks()) {

                String localText = block.getText();
                List<LayoutToken> tokens = block.getTokens();

                if ((localText != null) && (tokens.size() > 0)) {

                    if (numberEndLine) {
                        Matcher ma2 = endNum.matcher(localText);

                        if (ma2.find()) {
                            String groupStr = ma2.group(0);
                            if (groupStr.trim().equals("" + counter)) {
                                localText = localText.substring(0, localText.length() - groupStr.length());
                                block.setText(localText);
                                tokens.remove(tokens.size() - 1);
                                counter++;
                            }
                        }

                    } else if (numberBeginLine) {
                        Matcher ma1 = endNum.matcher(localText);

                        if (ma1.find()) {
                            String groupStr = ma1.group(0);
                            if (groupStr.trim().equals("" + counter)) {
                                localText = localText.substring(groupStr.length(), localText.length() - 1);
                                block.setText(localText);
                                tokens.remove(0);
                                counter++;
                            }
                        }

                    }
                }
                i++;
            }
        }

        return foundNumbering;
    }

    /**
     * First pass to detect basic structures: remove page header/footer, identify section numbering,
     * identify Figure and table blocks.
     *
	 * -> to be removed at some point!
	 * 
     * @param doc a document
     */
    /*static public void firstPass(Document doc) {
        if (doc == null) {
            throw new NullPointerException();
        }
        if (doc.getBlocks() == null) {
            throw new NullPointerException();
        }

        int i = 0;
        List<Integer> blockHeaders = new ArrayList<Integer>();
        List<Integer> blockFooters = new ArrayList<Integer>();
        List<Integer> blockSectionTitles = new ArrayList<Integer>();
        List<Integer> acknowledgementBlocks = new ArrayList<Integer>();
        List<Integer> blockTables = new ArrayList<Integer>();
        List<Integer> blockFigures = new ArrayList<Integer>();
        List<Integer> blockHeadTables = new ArrayList<Integer>();
        List<Integer> blockHeadFigures = new ArrayList<Integer>();
        List<Integer> blockDocumentHeaders = new ArrayList<Integer>();

        doc.setTitleMatchNum(false);
        try {
            for (Block block : doc.getBlocks()) {
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();

                Matcher ma1 = BasicStructureBuilder.introduction.matcher(localText);
                Matcher ma2 = BasicStructureBuilder.references.matcher(localText);

                if ((ma1.find()) || (ma2.find())) {
                    if (((localText.startsWith("1.")) || (localText.startsWith("1 "))) ||
                            ((localText.startsWith("2.")) || (localText.startsWith("2 "))) ||
                            (localText.startsWith("Contents")))
                        doc.setTitleMatchNum(true);
                    //System.out.println("Title section identified: block " + i + ", " + localText);
                    blockSectionTitles.add(i);
                } else {
                    StringTokenizer st = new StringTokenizer(localText, "\n");
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();

                        if (token.startsWith("@PAGE")) {
                            // current block should give the header/footors
                            if (i > 4) {
                                if (doc.getBlocks().get(i - 5).getNbTokens() < 20) {
                                    Integer i2 = i - 5;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            if (i > 3) {
                                if (doc.getBlocks().get(i - 4).getNbTokens() < 20) {
                                    Integer i2 = i - 4;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            if (i > 2) {
                                if (doc.getBlocks().get(i - 3).getNbTokens() < 20) {
                                    Integer i2 = i - 3;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            if (i > 1) {
                                if (doc.getBlocks().get(i - 2).getNbTokens() < 20) {
                                    Integer i2 = i - 2;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            if (i > 0) {
                                if (doc.getBlocks().get(i - 1).getNbTokens() < 20) {
                                    Integer i2 = i - 1;
                                    if (!blockFooters.contains(i2))
                                        blockFooters.add(i2);
                                }
                            }
                            blockFooters.add(i);

                            // page header candidates
                            blockHeaders.add(i);
                            if (i < doc.getBlocks().size() - 1) {
                                if (doc.getBlocks().get(i + 1).getNbTokens() < 20) {
                                    Integer i2 = i + 1;
                                    if (!blockHeaders.contains(i2))
                                        blockHeaders.add(i + 1);
                                }
                            }
                            if (i < doc.getBlocks().size() - 2) {
                                if (doc.getBlocks().get(i + 2).getNbTokens() < 20) {
                                    Integer i2 = i + 2;
                                    if (!blockHeaders.contains(i2))
                                        blockHeaders.add(i + 2);
                                }
                            }
                            if (i < doc.getBlocks().size() - 3) {
                                if (doc.getBlocks().get(i + 3).getNbTokens() < 20) {
                                    Integer i2 = i + 3;
                                    if (!blockHeaders.contains(i2))
                                        blockHeaders.add(i + 3);
                                }
                            }
                            if (i < doc.getBlocks().size() - 4) {
                                if (doc.getBlocks().get(i + 4).getNbTokens() < 20) {
                                    Integer i2 = i + 4;
                                    if (!blockHeaders.contains(i2))
                                        blockHeaders.add(i + 4);
                                }
                            }
                            // more ??
                        }

                    }
                }

                // clustering of blocks per font (for section header and figure/table detections)
                addBlockToCluster(i, doc);

                i++;
            }

            // try to find the cluster of section titles
            Cluster candidateCluster = null;
            //System.out.println("nb clusters: " + clusters.size());
            for (Cluster cluster : doc.getClusters()) {
                if ((cluster.getNbBlocks() < (doc.getBlocks().size() / 5)) && (cluster.getNbBlocks() < 20)) {
                    List<Integer> blo = cluster.getBlocks2();
                    for (Integer b : blo) {
                        if (blockSectionTitles.contains(b)) {
                            if (candidateCluster == null) {
                                candidateCluster = cluster;
                                break;
                            }
                            //else if (cluster.getFontSize() >= candidateCluster.getFontSize())
                            //	candidateCluster = cluster;
                        }
                    }
                }
            }
            if (candidateCluster != null) {
                List<Integer> newBlockSectionTitles = new ArrayList<Integer>();
                for (Integer bl : blockSectionTitles) {
                    if (!newBlockSectionTitles.contains(bl))
                        newBlockSectionTitles.add(bl);
                }

                List<Integer> blockClusterTitles = candidateCluster.getBlocks2();
                if (blockClusterTitles.size() < 20) {
                    for (Integer bl : blockClusterTitles) {
                        if (!newBlockSectionTitles.contains(bl))
                            newBlockSectionTitles.add(bl);
                    }
                }

                blockSectionTitles = newBlockSectionTitles;
            }

            // aknowledgement section recognition
            boolean ackn = false;
            i = 0;
            for (Block block : doc.getBlocks()) {
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();

                //System.out.println(i + ": " + localText+"\n");

                Integer iii = i;
                Matcher m3 = BasicStructureBuilder.acknowledgement.matcher(localText);
                if ((m3.find()) && (blockSectionTitles.contains(iii))) {
                    acknowledgementBlocks.add(iii);
                    ackn = true;
                    //int index = blockSectionTitles.indexOf(iii);
                    //blockSectionTitles.remove(index);
                } else if ((ackn) && (blockSectionTitles.contains(iii))) {
                    ackn = false;
                    break;
                } else if (ackn) {
                    Matcher m4 = BasicStructureBuilder.references.matcher(localText);
                    if ((ackn) && (!blockFooters.contains(iii)) && (!m4.find())) {
                        acknowledgementBlocks.add(iii);
                    } else if (m4.find()) {
                        ackn = false;
                        break;
                    }
                }
                i++;
            }

            // we remove references headers in blockSectionTitles
            int index = -1;
            for (Integer ii : blockSectionTitles) {
                Block block = doc.getBlocks().get(ii);
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();
                Matcher m4 = BasicStructureBuilder.references.matcher(localText);
                if (m4.find()) {
                    index = blockSectionTitles.indexOf(ii);
                    break;
                }
            }
            if (index != -1) {
                blockSectionTitles.remove(index);
            }

            // we check headers repetition from page to page to decide if it is an header or not
            ArrayList<Integer> toRemove = new ArrayList<Integer>();
            for (Integer ii : blockHeaders) {
                String localText = (doc.getBlocks().get(ii)).getText().trim();
                localText = TextUtilities.shadowNumbers(localText);
                int length = localText.length();
                if (length > 160)
                    toRemove.add(ii);
                else {
                    //System.out.println("header candidate: " + localText);
                    // evaluate distance with other potential headers
                    boolean valid = false;
                    for (Integer ii2 : blockHeaders) {
                        if (ii.intValue() != ii2.intValue()) {
                            String localText2 = doc.getBlocks().get(ii2).getText().trim();
                            if (localText2.length() < 160) {
                                localText2 = TextUtilities.shadowNumbers(localText2);
                                double dist = (double) TextUtilities.getLevenshteinDistance(localText, localText2) / length;
                                //System.out.println("dist with " + localText2 + " : " + dist);
                                if (dist < 0.25) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!valid) {
                        toRemove.add(ii);
                    }
                }
            }

            for (Integer ii : toRemove) {
                blockHeaders.remove(ii);
            }

            // same for footers
            toRemove = new ArrayList<Integer>();
            for (Integer ii : blockFooters) {
                String localText = (doc.getBlocks().get(ii)).getText().trim();
                localText = TextUtilities.shadowNumbers(localText);
                int length = localText.length();
                if (length > 160)
                    toRemove.add(ii);
                else {
                    //System.out.println("footer candidate: " + localText);
                    // evaluate distance with other potential headers
                    boolean valid = false;
                    for (Integer ii2 : blockFooters) {
                        if (ii.intValue() != ii2.intValue()) {
                            String localText2 = doc.getBlocks().get(ii2).getText().trim();
                            if (localText2.length() < 160) {
                                localText2 = TextUtilities.shadowNumbers(localText2);
                                double dist = (double) TextUtilities.getLevenshteinDistance(localText, localText2) / length;
                                if (dist < 0.25) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!valid) {
                        toRemove.add(ii);
                    }
                }
            }

            for (Integer ii : toRemove) {
                blockFooters.remove(ii);
            }

            // a special step for added banner repositoryies such HAL
            i = 0;
            for (Block block : doc.getBlocks()) {
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();

                //HAL
                if (localText.startsWith("Author manuscript, published in")) {
                    Double y = block.getY();
                    //System.out.println("HAL banner found, " + "block " + i + ", y = " + y);
                    if (Math.abs(y - 12.538) < 2) { // reference position
                        //blockHeaders.add(new Integer(i));
                        blockDocumentHeaders.add(i);
                        //System.out.println("HAL banner added as header block");
                        break;
                    }
                }

                // ACM publications
                //System.out.println("test ACM " + i);
                //System.out.println(localText);
                if (localText.startsWith("Permission to make digital or hard copies")) {
                    blockFooters.add(i);
                    break;
                }

                // arXiv, etc. put here
                // IOP

                if (localText.startsWith("Confidential: ") && (localText.contains("IOP"))) {
                    blockDocumentHeaders.add(i);
                    //System.out.println("IOP banner added as header block");
                    break;
                }
                i++;
            }

            // we try to recognize here table and figure blocks
            // the idea is that the textual elements are not located as the normal text blocks
            // this is recognized by exploiting the cluster of blocks starting up and down front the block
            // containing a table or a figure marker
            // two different runs, one for figures and one for tables (everything could be done in one step)
            i = 0;
            for (Block block : doc.getBlocks()) {
                String localText = block.getText().trim();
                localText = localText.replace("\n", " ");
                localText = localText.replace("  ", " ");
                localText = localText.trim();

                Matcher m = BasicStructureBuilder.figure.matcher(localText);
                Matcher m2 = BasicStructureBuilder.table.matcher(localText);

                double width = block.getWidth();
                boolean bold = block.getBold();

                // table
                //if ( (m2.find()) && (localText.length() < 200) ) {
                if ((m2.find()) && ((bold) || (localText.length() < 200))) {
                    if (!blockHeadTables.contains(i)) {
                        blockHeadTables.add(i);
                    }
                    // we also put all the small blocks before and after the marker
                    int j = i - 1;
                    while ((j > i - 15) && (j > 0)) {
                        Block b = doc.getBlocks().get(j);
                        if (b.getText() != null) {
                            if ((b.getText().length() < 160) || (width < 50)) {
                                if ((!blockTables.contains(j)) && (!blockSectionTitles.contains(j)) &&
                                        (!blockHeaders.contains(j)) && (!blockFooters.contains(j))
                                        )
                                    blockTables.add(j);
                            } else
                                j = 0;
                        }
                        j--;
                    }

                    j = i + 1;
                    while ((j < i + 15) && (j < doc.getBlocks().size())) {
                        Block b = doc.getBlocks().get(j);
                        if (b.getText() != null) {
                            if ((b.getText().length() < 160) || (width < 50)) {
                                if ((!blockTables.contains(j)) && (!blockSectionTitles.contains(j)) &&
                                        (!blockHeaders.contains(j)) && (!blockFooters.contains(j))
                                        )
                                    blockTables.add(j);
                            } else
                                j = doc.getBlocks().size();
                        }
                        j++;
                    }
                }
                // figure
                //else if ( (m.find()) && (localText.length() < 200) ) {
                else if ((m.find()) && ((bold) || (localText.length() < 200))) {
                    if (!blockHeadFigures.contains(i))
                        blockHeadFigures.add(i);
                    // we also put all the small blocks before and after the marker
                    int j = i - 1;
                    boolean imageFound = false;
                    while ((j > i - 15) && (j > 0)) {
                        Block b = doc.getBlocks().get(j);

                        if (b.getText() != null) {
                            String localText2 = b.getText().trim();
                            //localText = localText.replace("\n", " ");
                            localText2 = localText2.replace("  ", " ");
                            localText2 = localText2.trim();

                            if ((localText2.startsWith("@IMAGE")) && (!imageFound)) {
                                //System.out.println(localText2);
                                block.setText(block.getText() + " " + localText2);
                                //System.out.println(block.getText());
                                imageFound = true;
                            }

                            if ((localText2.length() < 160) || (width < 50)) {
                                if ((!blockFigures.contains(j)) && (!blockSectionTitles.contains(j)) &&
                                        (!blockHeaders.contains(j)) && (!blockFooters.contains(j))
                                        )
                                    blockFigures.add(j);
                            } else
                                j = 0;
                        }
                        j--;
                    }

                    j = i + 1;
                    while ((j < i + 15) && (j < doc.getBlocks().size())) {
                        Block b = doc.getBlocks().get(j);
                        if (b.getText() != null) {
                            if ((b.getText().trim().length() < 160) || (width < 50)) {
                                if ((!blockFigures.contains(j)) && (!blockSectionTitles.contains(j)) &&
                                        (!blockHeaders.contains(j)) && (!blockFooters.contains(j))
                                        )
                                    blockFigures.add(j);
                            } else
                                j = doc.getBlocks().size();
                        }
                        j++;
                    }
                }
                i++;
            }
        } finally {
            doc.setBlockHeaders(blockHeaders);
            doc.setBlockFooters(blockFooters);
            doc.setBlockSectionTitles(blockSectionTitles);
            doc.setAcknowledgementBlocks(acknowledgementBlocks);
            doc.setBlockTables(blockTables);
            doc.setBlockFigures(blockFigures);
            doc.setBlockHeadTables(blockHeadTables);
            doc.setBlockHeadFigures(blockHeadFigures);
            doc.setBlockDocumentHeaders(blockDocumentHeaders);
        }
    }*/

    /**
     * Cluster the blocks following the font, style and size aspects
     *
	 * -> not used at this stage, but could be an interesting feature in the full text model in the future 
	 *
     * @param b   integer
     * @param doc a document
     */
    private static void addBlockToCluster(Integer b, Document doc) {
        // get block features
        Block block = doc.getBlocks().get(b);
        String font = block.getFont();
        boolean bold = block.getBold();
        boolean italic = block.getItalic();
        double fontSize = block.getFontSize();
        boolean found = false;

        if (font == null) {
            font = "unknown";
        }
        //System.out.println(font + " " + bold + " " + italic + " " + fontSize );

        if (doc.getClusters() == null) {
            doc.setClusters(new ArrayList<Cluster>());
        } else {
            for (Cluster cluster : doc.getClusters()) {
                String font2 = cluster.getFont();
                if (font2 == null)
                    font2 = "unknown";
                if (font.equals(font2) &&
                        (bold == cluster.getBold()) &
                                (italic == cluster.getItalic()) &
                                (fontSize == cluster.getFontSize())) {
                    cluster.addBlock2(b);
                    found = true;
                }
            }
        }

        if (!found) {
            Cluster cluster = new Cluster();
            cluster.setFont(font);
            cluster.setBold(bold);
            cluster.setItalic(italic);
            cluster.setFontSize(fontSize);
            cluster.addBlock2(b);
            doc.getClusters().add(cluster);
        }

    }	
	
    static public Document generalResultSegmentation(Document doc, String labeledResult, List<LayoutToken> documentTokens) {
        List<Pair<String, String>> labeledTokens = GenericTaggerUtils.getTokensAndLabels(labeledResult);

        SortedSetMultimap<String, DocumentPiece> labeledBlocks = TreeMultimap.create();
        doc.setLabeledBlocks(labeledBlocks);

		/*try {
        	FileUtils.writeStringToFile(new File("/tmp/x1.txt"), labeledResult);
			FileUtils.writeStringToFile(new File("/tmp/x2.txt"), documentTokens.toString());
		}
		catch(Exception e) {
			e.printStackTrace();
		}*/

        List<Block> docBlocks = doc.getBlocks();
		int indexLine = 0;		
        int blockIndex = 0;
        int p = 0; // position in the labeled result 
		int currentLineEndPos = 0; // position in the global doc. tokenization of the last 
								// token of the current line
		int currentLineStartPos = 0; // position in the global doc. 
									 // tokenization of the first token of the current line
		String line = null;
		
		//DocumentPointer pointerA = DocumentPointer.START_DOCUMENT_POINTER;
		// the default first block might not contain tokens but only bitmap - in this case we move
		// to the first block containing some LayoutToken objects
        while (docBlocks.get(blockIndex).getTokens() == null
                //TODO: make things right
//                || docBlocks.get(blockIndex).getStartToken() == -1
                ) {
            blockIndex++;
        }
        DocumentPointer pointerA = new DocumentPointer(doc, blockIndex, docBlocks.get(blockIndex).getStartToken());
		
        DocumentPointer currentPointer = null;
        DocumentPointer lastPointer = null;

        String curLabel;
        String curPlainLabel = null;
        String lastPlainLabel = null;

        int lastTokenInd = -1;
        for (int i = docBlocks.size() - 1; i >=0; i--) {
            int endToken = docBlocks.get(i).getEndToken();
            if (endToken != -1) {
                lastTokenInd = endToken;
                break;
            }
        }

        // we do this concatenation trick so that we don't have to process stuff after the main loop
        // no copying of lists happens because of this, so it's ok to concatenate
        String ignoredLabel = "@IGNORED_LABEL@";
        for (Pair<String, String> labeledTokenPair :
                Iterables.concat(labeledTokens, 
					Collections.singleton(new Pair<String, String>("IgnoredToken", ignoredLabel)))) {
            if (labeledTokenPair == null) {
                p++;
                continue;
            }

			// as we process the document segmentation line by line, we don't use the usual 
			// tokenization to rebuild the text flow, but we get each line again from the 
			// text stored in the document blocks (similarly as when generating the features) 
			line = null;
			while( (line == null) && (blockIndex < docBlocks.size()) ) {
				Block block = docBlocks.get(blockIndex);
		        List<LayoutToken> tokens = block.getTokens();
				String localText = block.getText();
		        if ( (tokens == null) || (localText == null) || (localText.trim().length() == 0) ) {
					blockIndex++;
					indexLine = 0;
					if (blockIndex < docBlocks.size()) {
						block = docBlocks.get(blockIndex);
						currentLineStartPos = block.getStartToken();
					}
		            continue;
		        }
				String[] lines = localText.split("[\\n\\r]");
				if ( (lines.length == 0) || (indexLine >= lines.length)) {
					blockIndex++;
					indexLine = 0;
					if (blockIndex < docBlocks.size()) {
						block = docBlocks.get(blockIndex);
						currentLineStartPos = block.getStartToken();
					}
					continue;
				}
				else {
					line = lines[indexLine];
					indexLine++;
					if ( (line.trim().length() == 0) || (TextUtilities.filterLine(line)) ) {
						line = null;
						continue;
					}

					if (currentLineStartPos > lastTokenInd)
						continue;
					
					// adjust the start token position in documentTokens to this non trivial line
					// first skip possible space characters and tabs at the beginning of the line
					while( (documentTokens.get(currentLineStartPos).t().equals(" ") ||
							documentTokens.get(currentLineStartPos).t().equals("\t") )
					 	&& (currentLineStartPos != lastTokenInd)) {
					 	currentLineStartPos++;
					}
					if (!labeledTokenPair.a.startsWith(documentTokens.get(currentLineStartPos).getText())) {
						while(currentLineStartPos < block.getEndToken()) {
							if (documentTokens.get(currentLineStartPos).t().equals("\n")
							 || documentTokens.get(currentLineStartPos).t().equals("\r")) {
								 // move to the start of the next line, but ignore space characters and tabs
								 currentLineStartPos++;
								 while( (documentTokens.get(currentLineStartPos).t().equals(" ") ||
									 	documentTokens.get(currentLineStartPos).t().equals("\t") )
								 	&& (currentLineStartPos != lastTokenInd)) {
								 	currentLineStartPos++;
								 }
								 if ((currentLineStartPos != lastTokenInd) && 
								 	labeledTokenPair.a.startsWith(documentTokens.get(currentLineStartPos).getText())) {
									 break;
								 }
							 }
							 currentLineStartPos++;
						}
					}

					// what is then the position of the last token of this line?
					currentLineEndPos = currentLineStartPos;
					while(currentLineEndPos < block.getEndToken()) {
						if (documentTokens.get(currentLineEndPos).t().equals("\n")
						 || documentTokens.get(currentLineEndPos).t().equals("\r")) {
							currentLineEndPos--;
							break;
						}
						currentLineEndPos++;
					}
				}
			}
            curLabel = labeledTokenPair.b;
            curPlainLabel = GenericTaggerUtils.getPlainLabel(curLabel);
			
			/*System.out.println("-------------------------------");
			System.out.println("block: " + blockIndex);
			System.out.println("line: " + line);
			System.out.println("token: " + labeledTokenPair.a);
			System.out.println("curPlainLabel: " + curPlainLabel);
			System.out.println("lastPlainLabel: " + lastPlainLabel);
			if ((currentLineStartPos < lastTokenInd) && (currentLineStartPos != -1))
				System.out.println("currentLineStartPos: " + currentLineStartPos + 
											" (" + documentTokens.get(currentLineStartPos) + ")");
			if ((currentLineEndPos < lastTokenInd) && (currentLineEndPos != -1))
				System.out.println("currentLineEndPos: " + currentLineEndPos + 
											" (" + documentTokens.get(currentLineEndPos) + ")");*/
			
			if (blockIndex == docBlocks.size()) {
				break;
			}

            currentPointer = new DocumentPointer(doc, blockIndex, currentLineEndPos);

            // either a new entity starts or a new beginning of the same type of entity
			if ((!curPlainLabel.equals(lastPlainLabel)) && (lastPlainLabel != null)) {	
				if ( (pointerA.getTokenDocPos() <= lastPointer.getTokenDocPos()) &&
				   	(pointerA.getTokenDocPos() != -1) ) {
					labeledBlocks.put(lastPlainLabel, new DocumentPiece(pointerA, lastPointer));
				}
                pointerA = new DocumentPointer(doc, blockIndex, currentLineStartPos);
				//System.out.println("add segment for: " + lastPlainLabel + ", until " + (currentLineStartPos-2));
            }

            //updating stuff for next iteration
            lastPlainLabel = curPlainLabel;
            lastPointer = currentPointer;
			currentLineStartPos = currentLineEndPos+2; // one shift for the EOL, one for the next line
            p++;
        }
		
		if (blockIndex == docBlocks.size()) {
			// the last labelled piece has still to be added
			if ((!curPlainLabel.equals(lastPlainLabel)) && (lastPlainLabel != null)) {	
				if ( (pointerA.getTokenDocPos() <= lastPointer.getTokenDocPos()) && 
					(pointerA.getTokenDocPos() != -1) ) {
					labeledBlocks.put(lastPlainLabel, new DocumentPiece(pointerA, lastPointer));
					//System.out.println("add segment for: " + lastPlainLabel + ", until " + (currentLineStartPos-2));
				}
			}
		}

        return doc;
    }

    /**
     * Set the main segments of the document based on the full text parsing results
     *
     * @param doc           a document
     * @param labeledResult string
     * @param tokenizations tokens
     * @return a document
     */
    static public Document resultSegmentation(Document doc, String labeledResult, List<String> tokenizations) {
        if (doc == null) {
            throw new NullPointerException("Document is null");
        }
        if (doc.getBlocks() == null) {
            throw new NullPointerException("Blocks of the documents are null");
        }
        //System.out.println(tokenizations.toString());
//        int i = 0;
//        boolean first = true;
        List<Integer> blockHeaders = new ArrayList<Integer>();
        List<Integer> blockFooters = new ArrayList<Integer>();
        List<Integer> blockDocumentHeaders = new ArrayList<Integer>();
        List<Integer> blockSectionTitles = new ArrayList<Integer>();

        SortedSet<DocumentPiece> blockReferences = new TreeSet<DocumentPiece>();

        doc.setBibDataSets(new ArrayList<BibDataSet>());

//        StringTokenizer st = new StringTokenizer(labeledResult, "\n");

        String[] lines = labeledResult.split("\n");

        String currentTag = null;
        String s2 = null;
        String lastTag = null;
        String lastPlainTag = null;

        int p = 0; // index in the results' tokenization (st)
        int blockIndex = 0;

        BibDataSet bib = null;

        DocumentPointer pointerA = null;
//        DocumentPointer pointerB = null;
        DocumentPointer currentPointer;
        DocumentPointer lastPointer = null;


        for (String line : lines) {
//        while (st.hasMoreTokens()) {

            for (; blockIndex < doc.getBlocks().size() - 1; blockIndex++) {
//                int startTok = doc.getBlocks().get(blockIndex).getStartToken();
                int endTok = doc.getBlocks().get(blockIndex).getEndToken();

                if (endTok >= p) {
                    break;
                }
            }

            ArrayList<String> localFeatures = new ArrayList<String>();
            boolean addSpace = false;

//            String tok = st.nextToken().trim();
            line = line.trim();

            StringTokenizer stt = new StringTokenizer(line, "\t");
            int j = 0;

            boolean newLine = false;
            int ll = stt.countTokens();
            while (stt.hasMoreTokens()) {
                String s = stt.nextToken().trim();
                if (j == 0) {
                    s2 = s;
                    boolean strop = false;
                    while ((!strop) && (p < tokenizations.size())) {
                        String tokOriginal = tokenizations.get(p);
                        if (tokOriginal.equals(" ")
                                | tokOriginal.equals("\n")
                                | tokOriginal.equals("\r")
                                | tokOriginal.equals("\t")) {
                            addSpace = true;
                            p++;
                        } else if (tokOriginal.equals("")) {
                            p++;
                        } else //if (tokOriginal.equals(s))
                        {
                            strop = true;
                        }

                    }
                } else if (j == ll - 1) {
                    currentTag = s; // current tag
                } else {
                    if (s.equals("LINESTART")) {
                        newLine = true;
                    }
                    localFeatures.add(s);
                }
                j++;
            }

            if (lastTag != null) {
                if (lastTag.startsWith("I-")) {
                    lastPlainTag = lastTag.substring(2, lastTag.length());
                } else {
                    lastPlainTag = lastTag;
                }
            }


            String currentPlainTag = null;
            if (currentTag != null) {
                if (currentTag.startsWith("I-")) {
                    currentPlainTag = currentTag.substring(2, currentTag.length());
                } else {
                    currentPlainTag = currentTag;
                }
            }


            currentPointer = new DocumentPointer(doc, blockIndex, p);


            if (lastPlainTag != null && !currentPlainTag.equals(lastPlainTag) && lastPlainTag.equals("<references>")) {
                blockReferences.add(new DocumentPiece(pointerA, lastPointer));
                pointerA = currentPointer;
            }

            if (currentPlainTag.equals("<header>")) {
                if (!blockDocumentHeaders.contains(blockIndex)) {
                    blockDocumentHeaders.add(blockIndex);
                    //System.out.println("add block header: " + blockIndexInteger.intValue());
                }

            } else if (currentPlainTag.equals("<references>")) {//                    if (!blockReferences.contains(blockIndex)) {
//                        blockReferences.add(blockIndex);
//                        //System.out.println("add block reference: " + blockIndexInteger.intValue());
//                    }

                if (currentTag.equals("I-<references>")) {
                    pointerA = new DocumentPointer(doc, blockIndex, p);
                    if (bib != null) {
                        if (bib.getRawBib() != null) {
                            doc.getBibDataSets().add(bib);
                            bib = new BibDataSet();
                        }
                    } else {
                        bib = new BibDataSet();
                    }
                    bib.setRawBib(s2);
                } else {
                    if (addSpace) {
                        if (bib == null) {
                            bib = new BibDataSet();
                            bib.setRawBib(" " + s2);
                        } else {
                            bib.setRawBib(bib.getRawBib() + " " + s2);
                        }
                    } else {
                        if (bib == null) {
                            bib = new BibDataSet();
                            bib.setRawBib(s2);
                        } else {
                            bib.setRawBib(bib.getRawBib() + s2);
                        }
                    }
                }

//                case "<reference_marker>":
//                    if (!blockReferences.contains(blockIndex)) {
//                        blockReferences.add(blockIndex);
//                        //System.out.println("add block reference: " + blockIndexInteger.intValue());
//                    }
//
//                    if (currentTag.equals("I-<reference_marker>")) {
//                        if (bib != null) {
//                            if (bib.getRefSymbol() != null) {
//                                doc.getBibDataSets().add(bib);
//                                bib = new BibDataSet();
//                            }
//                        } else {
//                            bib = new BibDataSet();
//                        }
//                        bib.setRefSymbol(s2);
//                    } else {
//                        if (addSpace) {
//                            if (bib == null) {
//                                bib = new BibDataSet();
//                                bib.setRefSymbol(s2);
//                            } else {
//                                bib.setRefSymbol(bib.getRefSymbol() + " " + s2);
//                            }
//                        } else {
//                            if (bib == null) {
//                                bib = new BibDataSet();
//                                bib.setRefSymbol(s2);
//                            } else {
//                                bib.setRefSymbol(bib.getRefSymbol() + s2);
//                            }
//                        }
//                    }
//                    break;
            } else if (currentPlainTag.equals("<page_footnote>")) {
                if (!blockFooters.contains(blockIndex)) {
                    blockFooters.add(blockIndex);
                    //System.out.println("add block foot note: " + blockIndexInteger.intValue());
                }

            } else if (currentPlainTag.equals("<page_header>")) {
                if (!blockHeaders.contains(blockIndex)) {
                    blockHeaders.add(blockIndex);
                    //System.out.println("add block page header: " + blockIndexInteger.intValue());
                }

            } else if (currentPlainTag.equals("<section>")) {
                if (!blockSectionTitles.contains(blockIndex)) {
                    blockSectionTitles.add(blockIndex);
                    //System.out.println("add block page header: " + blockIndexInteger.intValue());
                }

            }

            lastTag = currentTag;
            p++;
            lastPointer = currentPointer;
        }

        if (bib != null) {
            doc.getBibDataSets().add(bib);
        }


        if (!lastPointer.equals(pointerA)) {
            if (lastPlainTag.equals("<references>")) {
                blockReferences.add(new DocumentPiece(pointerA, lastPointer));
            }
        }

        doc.setBlockHeaders(blockHeaders);
        doc.setBlockFooters(blockFooters);
        doc.setBlockDocumentHeaders(blockDocumentHeaders);
        doc.setBlockReferences(blockReferences);
        doc.setBlockSectionTitles(blockSectionTitles);

        return doc;
    }

}