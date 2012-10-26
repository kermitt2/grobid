package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorDate;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.trainer.sax.TEIDateSaxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;


/**
 * @author Patrice Lopez
 * @author Vyacheslav Zholudev
 */
public class DateTrainer extends AbstractTrainer {


    public DateTrainer() {
        super(GrobidModels.DATE);
    }


    /**
     * Add the selected features to the affiliation/address model training for headers
     * @param corpusDir a path where corpus files are located
     * @param modelOutputPath path where to store a model
     * @return a number of corpus items
     */
    public int createCRFPPData (File corpusDir,
                               File modelOutputPath) {
        int totalExamples = 0;
        try {
            System.out.println("sourcePathLabel: " + corpusDir);
            System.out.println("outputPath: " + modelOutputPath);


            // then we convert the tei files into the usual CRF label format
            // we process all tei files in the output directory
            File[] refFiles = corpusDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });


            if (refFiles == null) {
                throw new IllegalStateException("Folder " + corpusDir.getAbsolutePath() + " does not seem to contain training data. Please check");
            }

            System.out.println(refFiles.length + " tei files");

            // the file for writing the training data
            OutputStream os2 = new FileOutputStream(modelOutputPath);
            Writer writer2 = new OutputStreamWriter(os2, "UTF8");

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

            int n = 0;
            for (; n < refFiles.length; n++) {
                File teifile = refFiles[n];
                String name = teifile.getName();
                System.out.println(name);

                TEIDateSaxParser parser2 = new TEIDateSaxParser();

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(teifile, parser2);

                ArrayList<String> labeled = parser2.getLabeledResult();
                totalExamples += parser2.n;

                // we can now add the features
                String headerDates = FeaturesVectorDate.addFeaturesDate(labeled);

                // format with features for sequence tagging...
                writer2.write(headerDates + "\n");
            }

            writer2.close();
            os2.close();
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
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
        DateTrainer trainer = new DateTrainer();
        AbstractTrainer.runTraining(trainer);
        AbstractTrainer.runEvaluation(trainer);
        MockContext.destroyInitialContext();
    }

}