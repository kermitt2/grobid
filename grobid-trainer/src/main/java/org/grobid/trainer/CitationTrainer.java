package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorCitation;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.trainer.sax.TEICitationSaxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;


/**
 * @author Patrice Lopez
 */
public class CitationTrainer extends AbstractTrainer {

    public CitationTrainer() {
        super(GrobidModels.CITATION);
    }

	/**
	 * Add the selected features to the citations model example set, default 
	 * 
	 * @param corpusDir
	 *            a path where corpus files are located
	 * @return the total number of used corpus items
	 */
	@Override
	public int createCRFPPData(final File corpusDir, final File evalDataPath) {
		return createCRFPPData(corpusDir, evalDataPath, null, 1.0);
	}


//    @Override
//    public String evaluate() {
//        createCRFPPData(getEvalCorpusPath(), getEvalDataPath());
//        return EvaluationUtilities.evaluateStandardWapiti(getEvalDataPath().getAbsolutePath(), new WapitiModel(GrobidModels.CITATION));
//    }

    /**
	 * Add the selected features to the citations model example set
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

			List<List<OffsetPosition>> journalsPositions;
	        List<List<OffsetPosition>> abbrevJournalsPositions;
	        List<List<OffsetPosition>> conferencesPositions;
	        List<List<OffsetPosition>> publishersPositions;

			int n = 0;
			for (; n < refFiles.length; n++) {
				final File teifile = refFiles[n];
				String name = teifile.getName();
				System.out.println(name);

				final TEICitationSaxParser parser2 = new TEICitationSaxParser();

				// get a new instance of parser
				final SAXParser p = spf.newSAXParser();
				p.parse(teifile, parser2);

				final List<String> labeled = parser2.getLabeledResult();
				totalExamples += parser2.nbCitations;
				
				journalsPositions = parser2.journalsPositions;
                abbrevJournalsPositions = parser2.abbrevJournalsPositions;
                conferencesPositions = parser2.conferencesPositions;
                publishersPositions = parser2.publishersPositions;

				// we can now add the features
				String citation = FeaturesVectorCitation.addFeaturesCitation(labeled,
                        journalsPositions,
                        abbrevJournalsPositions,
                        conferencesPositions,
                        publishersPositions);
//System.out.println("["+citation+"]");

				String[] chunks = citation.split("\n \n");
				
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
     * Command line execution.
     *
     * @param args Command line arguments.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    	MockContext.setInitialContext();
    	GrobidProperties.getInstance();
        Trainer trainer = new CitationTrainer();
        AbstractTrainer.runTraining(trainer);
        AbstractTrainer.runEvaluation(trainer);
        MockContext.destroyInitialContext();
    }
}