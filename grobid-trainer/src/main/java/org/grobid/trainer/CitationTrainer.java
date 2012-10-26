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
import java.io.*;
import java.util.List;


/**
 * @author Patrice Lopez
 */
public class CitationTrainer extends AbstractTrainer {

    public CitationTrainer() {
        super(GrobidModels.CITATION);
    }


    /**
     * Add the selected features to the header model training
     */
    public int createCRFPPData(File sourceFile, File outputPath) {
        System.out.println(sourceFile);
        System.out.println(outputPath);
        int nbExamples = 0;

        List<List<OffsetPosition>> journalsPositions;
        List<List<OffsetPosition>> abbrevJournalsPositions;
        List<List<OffsetPosition>> conferencesPositions;
        List<List<OffsetPosition>> publishersPositions;

        try {
            // we process all tei files in the output directory
            File[] refFiles = sourceFile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });

            if (refFiles == null)
                return 0;

            System.out.println(refFiles.length + " files");

            // the file for writing the training data
            Writer writer2 = new OutputStreamWriter(new FileOutputStream(outputPath), "UTF8");

            int n = 0;
            for (; n < refFiles.length; n++) {
                File teifile = refFiles[n];
                String name = teifile.getName();
                System.out.println(name);

                TEICitationSaxParser parser2 = new TEICitationSaxParser();
                // get a factory
                SAXParserFactory spf = SAXParserFactory.newInstance();
                //get a new instance of parser
                SAXParser par = spf.newSAXParser();
                par.parse(teifile, parser2);

                List<String> labeled = parser2.getLabeledResult();
                nbExamples = parser2.nbCitations;

                journalsPositions = parser2.journalsPositions;
                abbrevJournalsPositions = parser2.abbrevJournalsPositions;
                conferencesPositions = parser2.conferencesPositions;
                publishersPositions = parser2.publishersPositions;

                String citation = FeaturesVectorCitation.addFeaturesCitation(labeled,
                        journalsPositions,
                        abbrevJournalsPositions,
                        conferencesPositions,
                        publishersPositions);

                // format with features for sequence tagging...
                writer2.write(citation);
            }

            writer2.close();
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
        return nbExamples;
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