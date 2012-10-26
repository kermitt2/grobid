package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorAffiliationAddress;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.trainer.sax.TEIAffiliationAddressSaxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Patrice Lopez
 * @author Vyacheslav Zholudev
 */
public class AffiliationAddressTrainer extends AbstractTrainer {

    public AffiliationAddressTrainer() {
        super(GrobidModels.AFFIILIATON_ADDRESS);
    }


    /**
     * Add the selected features to the affiliation/address model training for headers
     *
     * @param sourcePathLabel corpus folder
     * @param outputPath      model output path
     * @return a number of used corpus items
     */
    @Override
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath) {
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

            ArrayList<String> labeled;
            List<List<OffsetPosition>> placesPositions;

            for (File tf : refFiles) {
                String name = tf.getName();
                System.out.println(name);

                TEIAffiliationAddressSaxParser parser2 = new TEIAffiliationAddressSaxParser();

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(tf, parser2);

                labeled = parser2.getLabeledResult();
                placesPositions = parser2.placesPositions;
                totalExamples += parser2.n;

                // we can now add the features
                String affAdd =
                        FeaturesVectorAffiliationAddress.addFeaturesAffiliationAddress(labeled, placesPositions);

                // format with features for sequence tagging...
                writer2.write(affAdd + "\n");
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
        Trainer trainer = new AffiliationAddressTrainer();
        AbstractTrainer.runTraining(trainer);
        AbstractTrainer.runEvaluation(trainer);
        MockContext.destroyInitialContext();
    }

}