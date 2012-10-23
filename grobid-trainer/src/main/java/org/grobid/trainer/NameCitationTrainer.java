package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorName;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.mock.MockContext;
import org.grobid.trainer.sax.TEIAuthorSaxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;


/**
 * @author Patrice Lopez
 */
public class NameCitationTrainer extends AbstractTrainer {

    public NameCitationTrainer() {
        super(GrobidModels.NAMES_CITATION);
    }

    /**
     * Add the selected features to the author model training for headers
     */
    public int createCRFPPData(File sourcePathLabel, File outputPath) {
        int totalExamples = 0;
        try {
            System.out.println("sourcePathLabel: " + sourcePathLabel);
            System.out.println("outputPath: " + outputPath);


            // then we convert the tei files into the usual CRF label format
            // we process all tei files in the output directory
            File[] refFiles = sourcePathLabel.listFiles(new FilenameFilter() {
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

            for (File tf : refFiles) {
                String name = tf.getName();
                System.out.println(name);

                TEIAuthorSaxParser parser2 = new TEIAuthorSaxParser();

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(tf, parser2);

                List<String> labeled = parser2.getLabeledResult();
                totalExamples += parser2.n;

                // we can now add the features
                String header = FeaturesVectorName.addFeaturesName(labeled);

                // format with features for sequence tagging...
                writer2.write(header + "\n");
            }

            writer2.close();
            os2.close();
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
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
    	MockContext.setInitialContext();
    	GrobidProperties.getInstance();
        Trainer trainer = new NameCitationTrainer();
        AbstractTrainer.runTraining(trainer);
        AbstractTrainer.runEvaluation(trainer);
        MockContext.destroyInitialContext();

    }
}