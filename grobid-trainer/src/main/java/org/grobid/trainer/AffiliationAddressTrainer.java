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
import java.util.StringTokenizer;


/**
 * @author Patrice Lopez
 * @author Vyacheslav Zholudev
 */
public class AffiliationAddressTrainer extends AbstractTrainer {

    public AffiliationAddressTrainer() {
        super(GrobidModels.AFFIILIATON_ADDRESS);
    }

	/**
	 * Add the selected features to an affiliation/address example set 
	 * 
	 * @param corpusDir
	 *            a path where corpus files are located
	 * @param trainingOutputPath
	 *            path where to store the temporary training data
	 * @return the total number of used corpus items
	 */
	@Override
	public int createCRFPPData(final File corpusDir, final File trainingOutputPath) {
		return createCRFPPData(corpusDir, trainingOutputPath, null, 1.0);
	}

	/**
	 * Add the selected features to an affiliation/address example set 
	 * 
	 * @param corpusDir
	 *            a path where corpus files are located
	 * @param trainingOutputPath
	 *            path where to store the temporary training data
	 * @param evalOutputPath
	 *            path where to store the temporary evaluation data
	 * @param splitRatio
	 *            ratio to consider for separating training and evaluation data, e.g. 0.8 for 80% 
	 * @return the total number of used corpus items
	 */
	@Override
	public int createCRFPPData(final File corpusDir, 
							final File trainingOutputPath, 
							final File evalOutputPath, 
							double splitRatio) {
		int totalExamples = 0;
		try {
			System.out.println("sourcePathLabel: " + corpusDir);
			if (trainingOutputPath != null)
				System.out.println("outputPath for training data: " + trainingOutputPath);
			if (evalOutputPath != null)
				System.out.println("outputPath for evaluation data: " + evalOutputPath);

			// we convert the tei files into the usual CRF label format
			// we process all tei files in the output directory
			final File[] refFiles = corpusDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml");
				}
			}); 

			if (refFiles == null) {
				throw new IllegalStateException("Folder " + corpusDir.getAbsolutePath()
						+ " does not seem to contain training data. Please check");
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
			List<List<OffsetPosition>> placesPositions;
			
			int n = 0;
			for (; n < refFiles.length; n++) {
				final File teifile = refFiles[n];
				String name = teifile.getName();
				//System.out.println(name);

				final TEIAffiliationAddressSaxParser parser2 = new TEIAffiliationAddressSaxParser();

				// get a new instance of parser
				final SAXParser p = spf.newSAXParser();
				p.parse(teifile, parser2);

				final List<String> labeled = parser2.getLabeledResult();
				placesPositions = parser2.placesPositions;
				totalExamples += parser2.n;

				// we can now add the features
                String affAdd =
                        FeaturesVectorAffiliationAddress.addFeaturesAffiliationAddress(labeled, placesPositions);

				// format with features for sequence tagging...
				// given the split ratio we write either in the training file or the evaluation file
				//affAdd = affAdd.replace("\n \n", "\n \n");
				
				String[] chunks = affAdd.split("\n\n");
				
				for(int i=0; i<chunks.length; i++) {
					String chunk = chunks[i];
								
					if ( (writer2 == null) && (writer3 != null) )
						writer3.write(chunk + "\n \n");
					if ( (writer2 != null) && (writer3 == null) )
						writer2.write(chunk + "\n \n");
					else {		
						if (Math.random() <= splitRatio)
							writer2.write(chunk + "\n \n");
						else 
							writer3.write(chunk + "\n \n");
					}
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
			throw new GrobidException("An exception occurred while running Grobid.", e);
		}
		return totalExamples;
	}
	
	


    /**
     * Add the selected features to the affiliation/address model training for headers
     *
     * @param sourcePathLabel corpus folder
     * @param outputPath      model output path
     * @return a number of used corpus items
     */
    /*@Override
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
    }*/

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