package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.trainer.sax.TEIMonographItem;
import org.grobid.trainer.sax.TEIMonographSaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;

/**
 * @author Patrice Lopez
 */
public class MonographTrainer extends AbstractTrainer {
    public static final Logger LOGGER = LoggerFactory.getLogger(MonographTrainer.class);

    public MonographTrainer() {
        super(GrobidModels.MONOGRAPH);

        // adjusting CRF training parameters for this model (only with Wapiti)
        epsilon = 0.0000001;
        window = 50;
        nbMaxIterations = 1000;
        /*epsilon = 0.0001;
        window = 20;*/
    }

    @Override
    public int createCRFPPData(final File corpusPath, final File outputFile) {
        return addFeaturesMonograph(corpusPath.getAbsolutePath() + "/tei",
            corpusPath.getAbsolutePath() + "/raw",
            outputFile, null, 1.0);
    }

    /**
     * Add the selected features for the monograph model
     *
     * @param corpusDir          path where corpus files are located
     * @param trainingOutputPath path where to store the temporary training data
     * @param evalOutputPath     path where to store the temporary evaluation data
     * @param splitRatio         ratio to consider for separating training and evaluation data, e.g. 0.8 for 80%
     * @return the total number of used corpus items
     */
    @Override
    public int createCRFPPData(final File corpusDir,
                               final File trainingOutputPath,
                               final File evalOutputPath,
                               double splitRatio) {
        return addFeaturesMonograph(corpusDir.getAbsolutePath() + "/tei",
            corpusDir.getAbsolutePath() + "/raw",
            trainingOutputPath,
            evalOutputPath,
            splitRatio);
    }

    /**
     * Add the selected features for the monograph model
     *
     * @param sourceTEIPathLabel path to corpus TEI files
     * @param sourceRawPathLabel path to corpus raw files
     * @param trainingOutputPath path where to store the temporary training data
     * @param evalOutputPath     path where to store the temporary evaluation data
     * @param splitRatio         ratio to consider for separating training and evaluation data, e.g. 0.8 for 80%
     * @return number of examples
     */
    public int addFeaturesMonograph(String sourceTEIPathLabel,
                                    String sourceRawPathLabel,
                                    final File trainingOutputPath,
                                    final File evalOutputPath,
                                    double splitRatio) {
        int totalExamples = 0;
        try {
            System.out.println("sourceTEIPathLabel: " + sourceTEIPathLabel);
            System.out.println("sourceRawPathLabel: " + sourceRawPathLabel);

            // we need first to generate the labeled files from the TEI annotated files
            File input = new File(sourceTEIPathLabel);
            // we process all tei files in the output directory
            File[] refFiles = input.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tei.xml") || name.endsWith(".tei");
                }
            });

            if (refFiles == null) {
                throw new IllegalStateException("Folder " + sourceTEIPathLabel +
                    " does not seem to contain training data. Please check");
            }

            LOGGER.info("Processing " + refFiles.length + " tei files");

            // the file for writing the training data
            OutputStream os2 = null;
            Writer writer2 = null;
            if (trainingOutputPath != null) {
                os2 = new FileOutputStream(trainingOutputPath);
                writer2 = new OutputStreamWriter(os2, "UTF8");
            }

            // the file for writing the evaluation data
            OutputStream os3 = null;
            Writer writer3 = null;
            if (evalOutputPath != null) {
                os3 = new FileOutputStream(evalOutputPath);
                writer3 = new OutputStreamWriter(os3, "UTF8");
            }

            System.out.println("Training data under: " + trainingOutputPath);
            System.out.println("Evaluation data under: " + evalOutputPath);

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

            int n = 0;
            for (; n < refFiles.length; n++) { // read the labeled files from the TEI annotated files
                final File teifile = refFiles[n];
                TEIMonographSaxParser parser2 = new TEIMonographSaxParser();

                String name = teifile.getName();
                LOGGER.info("Processing: " + name);

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(teifile, parser2);

                List<TEIMonographItem> labeled = parser2.getMonographItems(); // get the labeled tokens extracted from the TEI annotated files
                totalExamples += parser2.getTotalReferences();

                // we can now add the features
                // we open the featured file (raw CRF files without the label)
                File rawCorpusDir = new File(new File(sourceRawPathLabel).getAbsolutePath());
                if (!rawCorpusDir.exists()) {
                    throw new IllegalStateException("Folder " + rawCorpusDir.getAbsolutePath() +
                        " does not exist. Please have a look!");
                }

                File theRawFile = new File(new File(sourceRawPathLabel).getAbsolutePath() + File.separator +
                    name.replace(".tei.xml", ""));
                if (!theRawFile.exists()) {
                    System.out.println("Raw file " + theRawFile +
                        " does not exist. Please have a look!");
                    continue;
                }

                // read the raw CRF file
                BufferedReader bis = new BufferedReader(
                    new InputStreamReader(new FileInputStream(
                        rawCorpusDir.getAbsolutePath() + File.separator +
                            name.replace(".tei.xml", "")), "UTF8"));

                StringBuilder referenceText = new StringBuilder();

                // read by lines the raw CRF file and add the tags
                String line = bis.readLine();
                String token1 = null, token2 = null, token3 = null, text = null;
                int totFound = 0, lastPositionFound = 0, lastPosition = 0, totData = 0;
                boolean found = false;
                while (line != null) {
                    String lines[] = line.split(" ");
                    /* every line of a raw file contains 3 tokens:
                        1 - the first token of the first line of every block
                        2 - the first token of the longest line of every block
                        3 - the last token of the last line of every block
                     */
                    token1 = UnicodeUtil.normaliseTextAndRemoveSpaces(lines[0]);
                    token2 = UnicodeUtil.normaliseTextAndRemoveSpaces(lines[1]);
                    token3 = UnicodeUtil.normaliseTextAndRemoveSpaces(lines[2]);

                    String currentLocalText = null, currentTag = null;
                    if (lastPosition >= labeled.size() - 1) {
                        lastPosition = lastPositionFound;
                    }

                    for (int i = lastPosition; i < labeled.size(); i++) {
                        // get the text and the label from TEI data
                        currentLocalText = labeled.get(i).getText();
                        currentTag = labeled.get(i).getLabel();

                        if (currentLocalText.contains(token1) || currentLocalText.contains(token2) ||
                            currentLocalText.contains(token3)) { // if they are found
                            found = true;
                            totFound++;
                            lastPositionFound = i;
                            referenceText.append(line).append(" ").append(currentTag).append("\n");
                        }

                        if (found || lastPosition >= labeled.size() - 1) {
                            found = false;
                            break;
                        } else {
                            lastPosition++;
                        }

                    }

                    totData++;
                    line = bis.readLine();
                }
                System.out.println("Total data found between CRF and TEI files " + totFound + " from total " + totData + " examples.");

                bis.close();

                if ((writer2 == null) && (writer3 != null))
                    writer3.write(referenceText.toString() + "\n \n");
                if ((writer2 != null) && (writer3 == null))
                    writer2.write(referenceText.toString() + "\n \n");
                else {
                    if (Math.random() <= splitRatio && writer2 != null) {
                        writer2.write(referenceText.toString() + "\n \n");
                    } else if (writer3 != null) {
                        writer3.write(referenceText.toString() + "\n \n");
                    }
                }

                if ((writer2 == null) && (writer3 != null))
                    writer3.write(referenceText.toString() + "\n \n");
            }

            if (writer2 != null) {
                writer2.close();
                os2.close();
            }

            if (writer3 != null) {
                writer3.close();
                os3.close();
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while training/evaluation monograph model.", e);
        }
        return totalExamples;
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        GrobidProperties.getInstance();
        AbstractTrainer.runTraining(new MonographTrainer());
        System.out.println(AbstractTrainer.runEvaluation(new MonographTrainer()));
        System.exit(0);
    }

}