package org.grobid.trainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorReference;
import org.grobid.core.mock.MockContext;
import org.grobid.core.sax.MarecSaxParser;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.trainer.evaluation.PatentEvaluation;


/**
 * @author Patrice Lopez
 */
public class PatentParserTrainer extends AbstractTrainer{

	// adjusting CRF training parameters for this model (only with Wapiti)
	private double epsilon = 0.0001;
	private int window = 20;

	// the window value indicate the right and left context of text to consider for an annotation when building
	// the training or the test data - the value is experimentally set
	// this window is used to maintain a certain level of occurence of the patent and NPL references, and avoid
	// to have the citation annotation diluted because they are very rare (less than 1 token per 1000)
	private static final int trainWindow = 200;

    public PatentParserTrainer() {
        super(GrobidModels.PATENT_PATENT);
    }

    public int createTrainingData(String trainingDataDir) {
        int nb = 0;
        try {
            String path = new File(new File(getFilePath2Resources(), 
				"dataset/patent/corpus/").getAbsolutePath()).getAbsolutePath();
            createDataSet(null, null, path, trainingDataDir, 0);
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while training Grobid.", e);
        }
        return nb;
    }

    // we have our own train() method that trains several models at once, 
	// therefore we don't need these methods which typically
    // were executed from AbstractTrainer. 
    @Override
    public int createCRFPPData(File corpusPath, File outputFile) {
        return 0;
    }
	@Override
	public int createCRFPPData(File corpusPath, File outputTrainingFile, File outputEvalFile, double splitRatio) {
		return 0;
	}
	

    public void train() {
        createTrainingData(GrobidProperties.getTempPath().getAbsolutePath());
//        String path = new File(new File("resources/dataset/patent/crfpp-templates/").getAbsolutePath()).getAbsolutePath();

        // train the resulting training files with features (based external command line, no JNI
        // binding for the training functions of CRF++)
        //File trainingDataPath1 = new File(GrobidProperties.getTempPath() + "/npl.train");
        //File trainingDataPath2 = new File(GrobidProperties.getTempPath() + "/patent.train");
        File trainingDataPath3 = new File(GrobidProperties.getTempPath() + "/all.train");

       // File templatePath1 = new File(getFilePath2Resources(), "dataset/patent/crfpp-templates/text.npl.references.template");
       //File templatePath2 = new File(getFilePath2Resources(), "dataset/patent/crfpp-templates/text.patent.references.template");
        File templatePath3 = 
			new File(getFilePath2Resources(), "dataset/patent/crfpp-templates/text.references.template");

        GenericTrainer trainer = TrainerFactory.getTrainer();
		trainer.setEpsilon(epsilon);
		trainer.setWindow(window);
        //File modelPath1 = new File(GrobidProperties.getModelPath(GrobidModels.PATENT_NPL).getAbsolutePath() + NEW_MODEL_EXT);
        //File modelPath2 = new File(GrobidProperties.getModelPath(GrobidModels.PATENT_PATENT).getAbsolutePath() + NEW_MODEL_EXT);
        File modelPath3 = 
			new File(GrobidProperties.getModelPath(GrobidModels.PATENT_ALL).getAbsolutePath() + NEW_MODEL_EXT);

        //trainer.train(templatePath1, trainingDataPath1, modelPath1, GrobidProperties.getNBThreads());
        //trainer.train(templatePath2, trainingDataPath2, modelPath2, GrobidProperties.getNBThreads());
        trainer.train(templatePath3, trainingDataPath3, modelPath3, GrobidProperties.getNBThreads(), model);

        //renaming
        //renameModels(GrobidProperties.getModelPath(GrobidModels.PATENT_NPL), modelPath1);
        //renameModels(GrobidProperties.getModelPath(GrobidModels.PATENT_PATENT), modelPath2);
        renameModels(GrobidProperties.getModelPath(GrobidModels.PATENT_ALL), modelPath3);
    }



    /**
     * Create the set of training and evaluation sets from the annotated examples with
     * extraction of citations in the patent description body.
	 * 
	 * @param rank
   	 *            rank associated to the set for n-fold data generation 	
   	 * @param type
   	 *            type of data to be created, 0 is training data, 1 is evaluation data 
	 *
     */
    public void createDataSet(String setName, String rank, String corpusPath, String outputPath, int type) {
        int nbFiles = 0;
        int nbNPLRef = 0;
        int nbPatentRef = 0;
        int maxRef = 0;
        try {
            // PATENT REF. textual data
            // we use a SAX parser on the patent XML files
            MarecSaxParser sax = new MarecSaxParser();
            sax.patentReferences = true;
            sax.nplReferences = false;
            int srCitations = 0;
            int previousSrCitations = 0;
            int withSR = 0;

            List<OffsetPosition> journalsPositions = null;
            List<OffsetPosition> abbrevJournalsPositions = null;
            List<OffsetPosition> conferencesPositions = null;
            List<OffsetPosition> publishersPositions = null;

			if (type == 0) {
				// training set
				sax.setN(trainWindow);
			}
            else {
				// for the test set we enlarge the focus window to include all the document.
             	sax.setN(-1);
           	}
            // get a factory
            /*SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            spf.setFeature("http://xml.org/sax/features/namespaces", false);
            spf.setFeature("http://xml.org/sax/features/validation", false);

            LinkedList<File> fileList = new LinkedList<File>();
            if (setName == null) {
                fileList.add(new File(corpusPath));
            } else if (rank == null) {
                fileList.add(new File(corpusPath));
            } else {
                // n-fold evaluation
                fileList.add(new File(corpusPath + File.separator + setName + "ing" + rank + File.separator));
            }
            Writer writer = null;
            if ((setName == null) || (setName.length() == 0)) {
                writer = new OutputStreamWriter(new FileOutputStream(
                        new File(outputPath + "/patent.train"), false), "UTF-8");
            } else if (rank == null) {
                writer = new OutputStreamWriter(new FileOutputStream(
                        new File(outputPath + "/patent." + setName), false), "UTF-8");
            } else {
                writer = new OutputStreamWriter(new FileOutputStream(
                        new File(outputPath + setName + "ing" + rank + "/patent." + setName), false), "UTF-8");
            }

            while (fileList.size() > 0) {
                File file = fileList.removeFirst();
                if (file.isDirectory()) {
                    for (File subFile : file.listFiles())
                        fileList.addLast(subFile);
                } else {
                    if (file.getName().endsWith(".xml")) {
                        nbFiles++;
                        System.out.println(file.getAbsolutePath());
                        try {
                            //get a new instance of parser
                            SAXParser p = spf.newSAXParser();
                            FileInputStream in = new FileInputStream(file);
                            sax.setFileName(file.getName());
                            p.parse(in, sax);
                            //writer1.write("\n");
                            nbPatentRef += sax.getNbPatentRef();
                            if (sax.citations != null) {
                                if (sax.citations.size() > previousSrCitations) {
                                    previousSrCitations = sax.citations.size();
                                    withSR++;
                                }
                            }
                            journalsPositions = sax.journalsPositions;
                            abbrevJournalsPositions = sax.abbrevJournalsPositions;
                            conferencesPositions = sax.conferencesPositions;
                            publishersPositions = sax.publishersPositions;

                            if (sax.accumulatedText != null) {
                                String text = sax.accumulatedText.toString();
                                if (text.trim().length() > 0) {
                                    // add features for the patent tokens
                                    addFeatures(text,
                                            writer,
                                            journalsPositions,
                                            abbrevJournalsPositions,
                                            conferencesPositions,
                                            publishersPositions);
                                    writer.write("\n \n");
                                }
                            }
                        } catch (Exception e) {
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                }
            }*/

            // NPL REF. textual data
            /*sax = new MarecSaxParser();
            sax.patentReferences = false;
            sax.nplReferences = true;

			if (type == 0) {
				// training set
				sax.setN(trainWindow);
			}
            else {
				// for the test set we enlarge the focus window to include all the document.
             	sax.setN(-1);
           	}
            // get a factory
            spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            spf.setFeature("http://xml.org/sax/features/namespaces", false);
            spf.setFeature("http://xml.org/sax/features/validation", false);

            fileList = new LinkedList<File>();
            if (setName == null) {
                fileList.add(new File(corpusPath));
            } else if (rank == null) {
                fileList.add(new File(corpusPath));
            } else {
                fileList.add(new File(corpusPath + File.separator + setName + "ing" + rank + File.separator));
            }
            if ((setName == null) || (setName.length() == 0)) {
                writer = new OutputStreamWriter(new FileOutputStream(
                        new File(outputPath + "/npl.train"), false), "UTF-8");
            } else if (rank == null) {
                writer = new OutputStreamWriter(new FileOutputStream(
                        new File(outputPath + "/npl." + setName), false), "UTF-8");
            } else {
                writer = new OutputStreamWriter(new FileOutputStream(
                        new File(outputPath + File.separator + setName + "ing" + rank + File.separator + 
						"npl." + setName), false), "UTF-8");
            }
            while (fileList.size() > 0) {
                File file = fileList.removeFirst();
                if (file.isDirectory()) {
                    for (File subFile : file.listFiles())
                        fileList.addLast(subFile);
                } else {
                    if (file.getName().endsWith(".xml")) {
                        //nbFiles++;
                        //String text = Files.readFromFile(file,"UTF-8");

                        try {
                            //get a new instance of parser
                            SAXParser p = spf.newSAXParser();
                            FileInputStream in = new FileInputStream(file);
                            sax.setFileName(file.toString());
                            p.parse(in, sax);
                            //writer2.write("\n");
                            nbNPLRef += sax.getNbNPLRef();
                            if (sax.nbAllRef > maxRef) {
                                maxRef = sax.nbAllRef;
                            }
                            if (sax.citations != null) {
                                if (sax.citations.size() > previousSrCitations) {
                                    previousSrCitations = sax.citations.size();
                                    withSR++;
                                }
                            }
                            journalsPositions = sax.journalsPositions;
                            abbrevJournalsPositions = sax.abbrevJournalsPositions;
                            conferencesPositions = sax.conferencesPositions;
                            publishersPositions = sax.publishersPositions;
                            //totalLength += sax.totalLength;

                            if (sax.accumulatedText != null) {
                                String text = sax.accumulatedText.toString();
                                // add features for NPL
                                addFeatures(text,
                                        writer,
                                        journalsPositions,
                                        abbrevJournalsPositions,
                                        conferencesPositions,
                                        publishersPositions);
                                writer.write("\n");
                            }

                        } catch (Exception e) {
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                }
            }

            if (sax.citations != null)
                srCitations += sax.citations.size();*/

            // Patent + NPL REF. textual data (the "all" model)
            sax = new MarecSaxParser();
            sax.patentReferences = true;
            sax.nplReferences = true;

			if (type == 0) {
				// training set
				sax.setN(trainWindow);
			}
            else {
				// for the test set we enlarge the focus window to include all the document.
             	sax.setN(-1);
           	}
            // get a factory
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            spf.setFeature("http://xml.org/sax/features/namespaces", false);
            spf.setFeature("http://xml.org/sax/features/validation", false);

            LinkedList<File> fileList = new LinkedList<File>();
            if (setName == null) {
                fileList.add(new File(corpusPath));
            } else if (rank == null) {
                fileList.add(new File(corpusPath));
            } else {
                fileList.add(new File(corpusPath + File.separator + setName + "ing" + rank + File.separator));
            }
			
			Writer writer = null;
            if ((setName == null) || (setName.length() == 0)) {
                writer = new OutputStreamWriter(new FileOutputStream(
                        new File(outputPath + File.separator + "all.train"), false), "UTF-8");
            } else if (rank == null) {
                writer = new OutputStreamWriter(new FileOutputStream(
                        new File(outputPath + File.separator + "all." + setName), false), "UTF-8");
            } else {
                writer = new OutputStreamWriter(new FileOutputStream(
                        new File(outputPath + File.separator + setName + "ing" + rank + File.separator + 
							"all." + setName), false), "UTF-8");
            }
            //int totalLength = 0;
            while (fileList.size() > 0) {
                File file = fileList.removeFirst();
                if (file.isDirectory()) {
                    for (File subFile : file.listFiles()) {
                        fileList.addLast(subFile);
                    }
                } else {
                    if (file.getName().endsWith(".xml")) {
						nbFiles++;
                        try {
                            //get a new instance of parser
                            SAXParser p = spf.newSAXParser();
                            FileInputStream in = new FileInputStream(file);
                            sax.setFileName(file.toString());
                            p.parse(in, sax);
                            //writer3.write("\n");
                            nbNPLRef += sax.getNbNPLRef();
                            nbPatentRef += sax.getNbPatentRef();
                            if (sax.nbAllRef > maxRef) {
                                maxRef = sax.nbAllRef;
                            }
                            if (sax.citations != null) {
                                if (sax.citations.size() > previousSrCitations) {
                                    previousSrCitations = sax.citations.size();
                                    withSR++;
                                }
                            }
                            journalsPositions = sax.journalsPositions;
                            abbrevJournalsPositions = sax.abbrevJournalsPositions;
                            conferencesPositions = sax.conferencesPositions;
                            publishersPositions = sax.publishersPositions;
                            //totalLength += sax.totalLength;

                            if (sax.accumulatedText != null) {
                                String text = sax.accumulatedText.toString();
                                // add features for patent+NPL
                                addFeatures(text,
                                        writer,
                                        journalsPositions,
                                        abbrevJournalsPositions,
                                        conferencesPositions,
                                        publishersPositions);
                                writer.write("\n");
                            }
                        } catch (Exception e) {
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                }
            }

            if (sax.citations != null) {
                srCitations += sax.citations.size();
            }
            if (setName != null) {
                System.out.println(setName + "ing on " + nbFiles + " files");
            } else {
                System.out.println("training on " + nbFiles + " files");
            }
            //System.out.println("Number of file with search report: " + withSR);
            System.out.println("Number of references: " + (nbNPLRef + nbPatentRef));
            System.out.println("Number of patent references: " + nbPatentRef);
            System.out.println("Number of NPL references: " + nbNPLRef);
            //System.out.println("Number of search report citations: " + srCitations);
            System.out.println("Average number of references: " +
                    TextUtilities.formatTwoDecimals((double) (nbNPLRef + nbPatentRef) / nbFiles));
            System.out.println("Max number of references in file: " + maxRef);

            /*if ((setName == null) || (setName.length() == 0)) {
                System.out.println("patent data set under: " + outputPath + "/patent.train");
            } else {
                System.out.println("patent data set under: " + outputPath + "/patent." + setName);
            }
            if ((setName == null) || (setName.length() == 0)) {
                System.out.println("npl data set under: " + outputPath + "/npl.train");
            } else {
                System.out.println("npl data set under: " + outputPath + "/npl." + setName);
            }*/
            if ((setName == null) || (setName.length() == 0)) {
                System.out.println("common data set under: " + outputPath + "/all.train");
            } else {
                System.out.println("common data set under: " + outputPath + "/all." + setName);
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
    }

    public void addFeatures(String text,
                            Writer writer,
                            List<OffsetPosition> journalPositions,
                            List<OffsetPosition> abbrevJournalPositions,
                            List<OffsetPosition> conferencePositions,
                            List<OffsetPosition> publisherPositions) {
        try {
            String line;
            StringTokenizer st = new StringTokenizer(text, "\n");
            int totalLine = st.countTokens();

            int posit = 0;
            int currentJournalPositions = 0;
            int currentAbbrevJournalPositions = 0;
            int currentConferencePositions = 0;
            int currentPublisherPositions = 0;
            boolean isJournalToken;
            boolean isAbbrevJournalToken;
            boolean isConferenceToken;
            boolean isPublisherToken;
            boolean skipTest;
            while (st.hasMoreTokens()) {
                isJournalToken = false;
                isAbbrevJournalToken = false;
                isConferenceToken = false;
                isPublisherToken = false;
                skipTest = false;
                line = st.nextToken();
                if (line.trim().length() == 0) {
                    writer.write("\n");
                    posit = 0;
                    continue;
                } else if (line.endsWith("\t<ignore>")) {
                    posit++;
                    continue;
                }
                // check the position of matches for journals
                if (journalPositions != null) {
                    if (currentJournalPositions == journalPositions.size() - 1) {
                        if (journalPositions.get(currentJournalPositions).end < posit) {
                            skipTest = true;
                        }
                    }
                    if (!skipTest) {
                        for (int i = currentJournalPositions; i < journalPositions.size(); i++) {
                            if ((journalPositions.get(i).start <= posit) &&
                                    (journalPositions.get(i).end >= posit)) {
                                isJournalToken = true;
                                currentJournalPositions = i;
                                break;
                            } else if (journalPositions.get(i).start > posit) {
                                isJournalToken = false;
                                currentJournalPositions = i;
                                break;
                            }
                        }
                    }
                }
                // check the position of matches for abbreviated journals
                skipTest = false;
                if (abbrevJournalPositions != null) {
                    if (currentAbbrevJournalPositions == abbrevJournalPositions.size() - 1) {
                        if (abbrevJournalPositions.get(currentAbbrevJournalPositions).end < posit) {
                            skipTest = true;
                        }
                    }
                    if (!skipTest) {
                        for (int i = currentAbbrevJournalPositions; i < abbrevJournalPositions.size(); i++) {
                            if ((abbrevJournalPositions.get(i).start <= posit) &&
                                    (abbrevJournalPositions.get(i).end >= posit)) {
                                isAbbrevJournalToken = true;
                                currentAbbrevJournalPositions = i;
                                break;
                            } else if (abbrevJournalPositions.get(i).start > posit) {
                                isAbbrevJournalToken = false;
                                currentAbbrevJournalPositions = i;
                                break;
                            }
                        }
                    }
                }
                // check the position of matches for conferences
                skipTest = false;
                if (conferencePositions != null) {
                    if (currentConferencePositions == conferencePositions.size() - 1) {
                        if (conferencePositions.get(currentConferencePositions).end < posit) {
                            skipTest = true;
                        }
                    }
                    if (!skipTest) {
                        for (int i = currentConferencePositions; i < conferencePositions.size(); i++) {
                            if ((conferencePositions.get(i).start <= posit) &&
                                    (conferencePositions.get(i).end >= posit)) {
                                isConferenceToken = true;
                                currentConferencePositions = i;
                                break;
                            } else if (conferencePositions.get(i).start > posit) {
                                isConferenceToken = false;
                                currentConferencePositions = i;
                                break;
                            }
                        }
                    }
                }
                // check the position of matches for publishers
                skipTest = false;
                if (publisherPositions != null) {
                    if (currentPublisherPositions == publisherPositions.size() - 1) {
                        if (publisherPositions.get(currentPublisherPositions).end < posit) {
                            skipTest = true;
                        }
                    }
                    if (!skipTest) {
                        for (int i = currentPublisherPositions; i < publisherPositions.size(); i++) {
                            if ((publisherPositions.get(i).start <= posit) &&
                                    (publisherPositions.get(i).end >= posit)) {
                                isPublisherToken = true;
                                currentPublisherPositions = i;
                                break;
                            } else if (publisherPositions.get(i).start > posit) {
                                isPublisherToken = false;
                                currentPublisherPositions = i;
                                break;
                            }
                        }
                    }
                }
                FeaturesVectorReference featuresVector =
                        FeaturesVectorReference.addFeaturesPatentReferences(line,
                                totalLine,
                                posit,
                                isJournalToken,
                                isAbbrevJournalToken,
                                isConferenceToken,
                                isPublisherToken);
                if (featuresVector.label == null)
                    continue;
                writer.write(featuresVector.printVector());
                writer.flush();
                posit++;
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
    }

    @Override
    public String evaluate() {
        //parameter 2 was in the former main() method of ParentEvaluation
        return new PatentEvaluation().evaluate();
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
        AbstractTrainer.runTraining(new PatentParserTrainer());
        MockContext.destroyInitialContext();
    }

}