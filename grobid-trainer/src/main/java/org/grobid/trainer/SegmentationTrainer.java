package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.trainer.sax.TEISegmentationSaxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;

public class SegmentationTrainer extends AbstractTrainer {

    public SegmentationTrainer() {
        super(GrobidModels.SEGMENTATION);
    }

    @Override
    public int createCRFPPData(File corpusPath, File outputFile) {
        return addFeaturesSegmentation(corpusPath.getAbsolutePath() + "/tei",
                corpusPath.getAbsolutePath() + "/raw",
                outputFile, null, 1.0);
    }

    /**
     * Add the selected features for the segmentation model
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
        return addFeaturesSegmentation(corpusDir.getAbsolutePath() + "/tei",
                corpusDir.getAbsolutePath() + "/raw",
                trainingOutputPath,
                evalOutputPath,
                splitRatio);
    }

    /**
     * Add the selected features for the segmentation model
     *
     * @param sourceTEIPathLabel path to corpus TEI files
     * @param sourceRawPathLabel path to corpus raw files
     * @param trainingOutputPath path where to store the temporary training data
     * @param evalOutputPath     path where to store the temporary evaluation data
     * @param splitRatio         ratio to consider for separating training and evaluation data, e.g. 0.8 for 80%
     * @return number of examples
     */
    public int addFeaturesSegmentation(String sourceTEIPathLabel,
                                       String sourceRawPathLabel,
                                       final File trainingOutputPath,
                                       final File evalOutputPath,
                                       double splitRatio) {
        int totalExamples = 0;
        try {
            System.out.println("sourceTEIPathLabel: " + sourceTEIPathLabel);
            System.out.println("sourceRawPathLabel: " + sourceRawPathLabel);
            System.out.println("trainingOutputPath: " + trainingOutputPath);
            System.out.println("evalOutputPath: " + evalOutputPath);

            // we need first to generate the labeled files from the TEI annotated files
            File input = new File(sourceTEIPathLabel);
            // we process all tei files in the output directory
            File[] refFiles = input.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tei.xml") || name.endsWith(".tei");
                }
            });

            if (refFiles == null) {
                return 0;
            }

            System.out.println(refFiles.length + " tei files");

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

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

            for (File tf : refFiles) {
                String name = tf.getName();
                LOGGER.info("Processing: " + name);

                TEISegmentationSaxParser parser2 = new TEISegmentationSaxParser();

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(tf, parser2);

                List<String> labeled = parser2.getLabeledResult();

                // we can now add the features
                // we open the featured file
                try {
                    File theRawFile = new File(sourceRawPathLabel + File.separator + name.replace(".tei.xml", ""));
                    if (!theRawFile.exists()) {
                        LOGGER.error("The raw file does not exist: " + theRawFile.getPath());
                        continue;
                    }

                    // removing the @newline
                    /*List<String> newLabeled = new ArrayList<String>();
                    for(String label : labeled) {
                        if (!label.startsWith("@newline"))
                            newLabeled.add(label);
                    } 
                    labeled = newLabeled;*/

/*StringBuilder temp = new StringBuilder();
for(String label : labeled) {
    temp.append(label);
}
FileUtils.writeStringToFile(new File("/tmp/expected-"+name+".txt"), temp.toString());*/
                
                    int q = 0;
                    BufferedReader bis = new BufferedReader(
                            new InputStreamReader(new FileInputStream(theRawFile), "UTF8"));
                    StringBuilder segmentation = new StringBuilder();
                    String line = null;
                    int l = 0;
                    String previousTag = null;
                    int nbInvalid = 0;
                    while ((line = bis.readLine()) != null) {
                        l++;
                        int ii = line.indexOf(' ');
                        String token = null;
                        if (ii != -1) {
                            token = line.substring(0, ii);
                            // unicode normalisation of the token - it should not be necessary if the training data
                            // has been gnerated by a recent version of grobid
                            token = UnicodeUtil.normaliseTextAndRemoveSpaces(token);
                        }
                        // we get the label in the labelled data file for the same token
                        for (int pp = q; pp < labeled.size(); pp++) {
                            String localLine = labeled.get(pp);
                            StringTokenizer st = new StringTokenizer(localLine, " \t");
                            if (st.hasMoreTokens()) {
                                String localToken = st.nextToken();
                                // unicode normalisation of the token - it should not be necessary if the training data
                                // has been gnerated by a recent version of grobid
                                localToken = UnicodeUtil.normaliseTextAndRemoveSpaces(localToken);
                                if (localToken.equals(token)) {
                                    String tag = st.nextToken();
                                    segmentation.append(line).append(" ").append(tag);
                                    previousTag = tag;
                                    q = pp + 1;
                                    nbInvalid = 0;
                                    //pp = q + 10;
                                    break;
                                }
                            }
                            if (pp - q > 5) {
                                //LOGGER.warn(name + " / Segmentation trainer: TEI and raw file unsynchronized at raw line " + l + " : " + localLine);
                                nbInvalid++;
                                // let's reuse the latest tag
                                if (previousTag != null)
                                   segmentation.append(line).append(" ").append(previousTag);
                                break;
                            }
                        }
                        if (nbInvalid > 20) {
                            // too many consecutive synchronization issues
                            break;
                        }
                    }
                    bis.close();
                    if (nbInvalid < 10) {
                        if ((writer2 == null) && (writer3 != null))
                            writer3.write(segmentation.toString() + "\n");
                        if ((writer2 != null) && (writer3 == null))
                            writer2.write(segmentation.toString() + "\n");
                        else {
                            if (Math.random() <= splitRatio)
                                writer2.write(segmentation.toString() + "\n");
                            else
                                writer3.write(segmentation.toString() + "\n");
                        }
                    } else {
                        LOGGER.warn(name + " / too many synchronization issues, file not used in training data and to be fixed!");
                    }
                } catch (Exception e) {
                   LOGGER.error("Fail to open or process raw file", e);
                }
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
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return totalExamples;
    }

    /**
     * Add the selected features to the author model training for headers
     *
     * @param sourceTEIPathLabel path to TEI files
     * @param sourceRawPathLabel path to raw files
     * @param outputPath         output train file
     * @return number of examples
     */
    /*public int addFeaturesSegmentation2(String sourceTEIPathLabel,
                                        String sourceRawPathLabel,
                                        File outputPath) {
        int totalExamples = 0;
        try {
            System.out.println("sourceTEIPathLabel: " + sourceTEIPathLabel);
            System.out.println("sourceRawPathLabel: " + sourceRawPathLabel);
            System.out.println("outputPath: " + outputPath);

            // we need first to generate the labeled files from the TEI annotated files
            File input = new File(sourceTEIPathLabel);
            // we process all tei files in the output directory
            File[] refFiles = input.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tei.xml");
                }
            });

            if (refFiles == null) {
                return 0;
            }

            System.out.println(refFiles.length + " tei files");

            // the file for writing the training data
            OutputStream os2 = new FileOutputStream(outputPath);
            Writer writer2 = new OutputStreamWriter(os2, "UTF8");

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

//            int n = 0;
            for (File tf : refFiles) {
                String name = tf.getName();
                System.out.println(name);

                TEISegmentationSaxParser parser2 = new TEISegmentationSaxParser();

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(tf, parser2);

                List<String> labeled = parser2.getLabeledResult();
                //totalExamples += parser2.n;

                // we can now add the features
                // we open the featured file
                int q = 0;
                BufferedReader bis = new BufferedReader(
                        new InputStreamReader(new FileInputStream(
                                sourceRawPathLabel + File.separator + name.replace(".tei.xml", "")), "UTF8"));

                StringBuilder segmentation = new StringBuilder();

                String line;
//                String lastTag = null;
                while ((line = bis.readLine()) != null) {
                    int ii = line.indexOf(' ');
                    String token = null;
                    if (ii != -1)
                        token = line.substring(0, ii);
//                    boolean found = false;
                    // we get the label in the labelled data file for the same token
                    for (int pp = q; pp < labeled.size(); pp++) {
                        String localLine = labeled.get(pp);
                        StringTokenizer st = new StringTokenizer(localLine, " ");
                        if (st.hasMoreTokens()) {
                            String localToken = st.nextToken();

                            if (localToken.equals(token)) {
                                String tag = st.nextToken();
                                segmentation.append(line).append(" ").append(tag);
//                                lastTag = tag;
//                                found = true;
                                q = pp + 1;
                                pp = q + 10;
                            }
                        }
                        if (pp - q > 5) {
                            break;
                        }
                    }
                }
                bis.close();

                // format with features for sequence tagging...
                writer2.write(segmentation.toString() + "\n");
            }

            writer2.close();
            os2.close();
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return totalExamples;
    }*/

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        GrobidProperties.getInstance();
        AbstractTrainer.runTraining(new SegmentationTrainer());
        System.out.println(AbstractTrainer.runEvaluation(new SegmentationTrainer()));
        System.exit(0);
    }
}