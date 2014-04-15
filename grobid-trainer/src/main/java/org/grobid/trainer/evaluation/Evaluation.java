package org.grobid.trainer.evaluation;

import org.grobid.core.engines.CitationParser;
import org.grobid.core.engines.EngineParsers;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorCitation;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.trainer.sax.TEICitationSaxParser;
import org.grobid.trainer.sax.TEIHeaderSaxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

//import org.grobid.core.engines.HeaderParser;

/**
 * Class for managing the extraction of bibliographical information from pdf documents.
 *
 * @author Patrice Lopez
 */
public class Evaluation {
    private CitationParser citationParser = null;
//    private HeaderParser headerParser = null;

    private String evaluationCitationPath = null;
    private String evaluationHeaderPathTEI = null;
//    private String evaluationHeaderPathHeader = null;

    private void setPropertyInfo() {
//        evaluationCitationPath = System.getProperty(GrobidProperties.PROP_RESOURCE_PATH) + "/dataset/citation/evaluation/citations.xml";
//        evaluationHeaderPathTEI = System.getProperty(GrobidProperties.PROP_RESOURCE_PATH) + "/dataset/header/evaluation/tei";
//        evaluationHeaderPathHeader = System.getProperty(GrobidProperties.PROP_RESOURCE_PATH) + "/dataset/header/evaluation/header";
    }

    public Evaluation() {
        this.setPropertyInfo();
    }

    /**
     * Evaluate CRF++ results, label by label, with error rate, precision, recall and f0 measures
     * @return report
     */
    public String evaluateCitation() {
        // word level
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Integer> counterObserved = new ArrayList<Integer>(); // true positive
        ArrayList<Integer> counterExpected = new ArrayList<Integer>(); // all expected
        ArrayList<Integer> counterFalsePositive = new ArrayList<Integer>(); // false positive
        ArrayList<Integer> counterFalseNegative = new ArrayList<Integer>(); // false negative

        // field level
//        ArrayList<String> labels2 = new ArrayList<String>();
//        ArrayList<Integer> counterObserved2 = new ArrayList<Integer>(); // true positive
//        ArrayList<Integer> counterExpected2 = new ArrayList<Integer>(); // all expected
//        ArrayList<Integer> counterFalsePositive2 = new ArrayList<Integer>(); // false positive
//        ArrayList<Integer> counterFalseNegative2 = new ArrayList<Integer>(); // false negative
//
//        // instance level
//        ArrayList<Integer> counterObserved3 = new ArrayList<Integer>(); // true positive
//        ArrayList<Integer> counterExpected3 = new ArrayList<Integer>(); // all expected
//        ArrayList<Integer> counterFalsePositive3 = new ArrayList<Integer>(); // false positive
//        ArrayList<Integer> counterFalseNegative3 = new ArrayList<Integer>(); // false negative

        StringBuilder report = new StringBuilder();
        //DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        try {
            if (citationParser == null) {
                citationParser = new CitationParser(new EngineParsers());
            }

            // dis.available() returns 0 if the file does not have more lines.
            TEICitationSaxParser parser = new TEICitationSaxParser();
            // get a factory
            SAXParserFactory spf = SAXParserFactory.newInstance();
            //get a new instance of parser
            SAXParser par = spf.newSAXParser();
            par.parse(evaluationCitationPath, parser);

            ArrayList<String> labeled = parser.getLabeledResult();
            int nbTestExamples = parser.nbCitations;
            System.out.println(nbTestExamples + " evaluation text reference strings.");

            List<List<OffsetPosition>> journalsPositions;
            List<List<OffsetPosition>> abbrevJournalsPositions;
            List<List<OffsetPosition>> conferencesPositions;
            List<List<OffsetPosition>> publishersPositions;

            journalsPositions = parser.journalsPositions;
            abbrevJournalsPositions = parser.abbrevJournalsPositions;
            conferencesPositions = parser.conferencesPositions;
            publishersPositions = parser.publishersPositions;

            String citation = FeaturesVectorCitation.addFeaturesCitation(labeled,
                    journalsPositions,
                    abbrevJournalsPositions,
                    conferencesPositions,
                    publishersPositions);
            //System.out.println(citation);
            String res = citationParser.label(citation);
            //System.out.println(res);
            StringTokenizer st = new StringTokenizer(res, "\n");
            int z = 0;

            while (st.hasMoreTokens()) {
                String l2 = labeled.get(z).trim();
                if (l2.length() == 0) {
                    z++;
                    l2 = labeled.get(z).trim();
                }
                String l = st.nextToken();

                StringTokenizer st2 = new StringTokenizer(l, "\t");
                String obtainedLabel = null;
                String expectedLabel = null;
                while (st2.hasMoreTokens()) {
                    obtainedLabel = st2.nextToken().trim();
                    if (obtainedLabel.startsWith("I-")) {
                        obtainedLabel = obtainedLabel.substring(2, obtainedLabel.length());
                    }
                }
                int ind = l2.indexOf(" ");
                if (ind != -1) {
                    expectedLabel = l2.substring(ind, l2.length()).trim();
                    if (expectedLabel.startsWith("I-")) {
                        expectedLabel = expectedLabel.substring(2, expectedLabel.length());
                    }
                }
                //System.out.println(expectedLabel + " / " + obtainedLabel);

                if ((expectedLabel == null) | (obtainedLabel == null))
                    continue;
                int indbis = labels.indexOf(expectedLabel);
                if (indbis != -1) {
                    if (expectedLabel.equals(obtainedLabel)) {
                        Integer count = counterObserved.get(indbis);
                        Integer newCount = count + 1;
                        counterObserved.set(indbis, newCount);
                    } else {
                        int ind2 = labels.indexOf(obtainedLabel);

                        if (ind2 != -1) {
                            Integer count = counterFalsePositive.get(ind2);
                            Integer newCount = count + 1;
                            counterFalsePositive.set(ind2, newCount);
                        } else {
                            labels.add(obtainedLabel);
                            counterFalsePositive.add(1);
                            counterObserved.add(0);
                            counterExpected.add(0);
                            counterFalseNegative.add(0);
                        }

                        Integer count2 = counterFalseNegative.get(indbis);
                        Integer newCount2 = count2 + 1;
                        counterFalseNegative.set(indbis, newCount2);
                    }
                    Integer count = counterExpected.get(indbis);
                    Integer newCount = count + 1;
                    counterExpected.set(indbis, newCount);
                } else {
                    labels.add(expectedLabel);

                    if (expectedLabel.equals(obtainedLabel)) {
                        counterObserved.add(1); // true positives
                        counterFalsePositive.add(0);
                        counterFalseNegative.add(0);
                    } else {
                        counterObserved.add(0);
                        counterFalsePositive.add(0);
                        counterFalseNegative.add(1);

                        int ind2 = labels.indexOf(obtainedLabel);
                        if (ind2 != -1) {
                            Integer count = counterFalsePositive.get(ind2);
                            Integer newCount = count + 1;
                            counterFalsePositive.set(ind2, newCount);
                        } else {
                            labels.add(obtainedLabel);
                            counterFalsePositive.add(1);
                            counterObserved.add(0);
                            counterExpected.add(0);
                            counterFalseNegative.add(0);
                        }
                    }

                    counterExpected.add(1);

                }

                z++;
            }

            // print report
            int i = 0;

            // word
            report.append("\nWord-level results\n");
            report.append("\nlabel\t\taccuracy\tprecision\trecall\t\tf1\n\n");
            int cumulated_tp = 0;
            int cumulated_fp = 0;
            int cumulated_tn = 0;
            int cumulated_fn = 0;
//            int cumulated_all = 0;
            double cumulated_f0 = 0.0;
            int totalTokens = 0;

            DecimalFormat df = new DecimalFormat("#.####");

            while (i < labels.size()) {
                totalTokens += counterExpected.get(i);
                i++;
            }

            System.out.println("total: " + totalTokens + " tokens.");

            i = 0;
            while (i < labels.size()) {
                String label = labels.get(i);
                report.append(label);
                if (label.length() <= 11)
                    report.append("\t");

                int tp = counterObserved.get(i); // true positives
                int fp = counterFalsePositive.get(i); // false positves
                int fn = counterFalseNegative.get(i); // false negative
                int tn = totalTokens - tp - (fp + fn); // true negatives
//                int all = counterExpected.get(i); // all expected

                //System.out.println(label + "*****: " + "true positives: " + tp + "\nfalse positives:" + fp +
                //				"\ntrue negatives: " + tn + "\nfalse negatives: " + fn);
                //System.out.println("all: " + all);

                double accuracy = (double) (tp + tn) / (tp + fp + tn + fn);
                report.append("\t").append(df.format(accuracy));

                double precision = (double) (tp) / (tp + fp);
                report.append("\t\t").append(df.format(precision));

                double recall = (double) (tp) / (tp + fn);
                report.append("\t\t").append(df.format(recall));

                double f0 = (2 * precision * recall) / (precision + recall);
                report.append("\t\t").append(df.format(f0));

                report.append("\n");

                cumulated_tp += tp;
                cumulated_fp += fp;
                cumulated_tn += tn;
                cumulated_fn += fn;
//                cumulated_all += all;
                cumulated_f0 += f0;
                i++;
            }
            report.append("\n");
            report.append("all labels\t");

            double accuracy = (double) (cumulated_tp + cumulated_tn) /
                    (cumulated_tp + cumulated_fp + cumulated_tn + cumulated_fn);
            report.append("\t").append(df.format(accuracy));

            double precision = (double) cumulated_tp / (cumulated_tp + cumulated_fp);
            report.append("\t\t").append(df.format(precision));

            double recall = (double) cumulated_tp / (cumulated_tp + cumulated_fn);
            report.append("\t\t").append(df.format(recall));

            //double f0 = (2 * precision * recall) / (precision + recall);
            double f0 = cumulated_f0 / labels.size();
            report.append("\t\t").append(df.format(f0));

            report.append("\n");
        } catch (Exception e) {
//			e.printStackTrace();
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }

        return report.toString();
    }


    /**
     * Evaluate CRF++ results, label by label, with error rate, precision, recall and f0 measures
     * @return report of a header
     */
    public String evaluateHeader() {
        // word level
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Integer> counterObserved = new ArrayList<Integer>(); // true positive
        ArrayList<Integer> counterExpected = new ArrayList<Integer>(); // all expected
        ArrayList<Integer> counterFalsePositive = new ArrayList<Integer>(); // false positive
        ArrayList<Integer> counterFalseNegative = new ArrayList<Integer>(); // false negative

        // field level
//        ArrayList<String> labels2 = new ArrayList<String>();
//        ArrayList<Integer> counterObserved2 = new ArrayList<Integer>(); // true positive
//        ArrayList<Integer> counterExpected2 = new ArrayList<Integer>(); // all expected
//        ArrayList<Integer> counterFalsePositive2 = new ArrayList<Integer>(); // false positive
//        ArrayList<Integer> counterFalseNegative2 = new ArrayList<Integer>(); // false negative
//
//        // instance level
//        ArrayList<Integer> counterObserved3 = new ArrayList<Integer>(); // true positive
//        ArrayList<Integer> counterExpected3 = new ArrayList<Integer>(); // all expected
//        ArrayList<Integer> counterFalsePositive3 = new ArrayList<Integer>(); // false positive
//        ArrayList<Integer> counterFalseNegative3 = new ArrayList<Integer>(); // false negative

        StringBuilder report = new StringBuilder();

        try {
            // open the result repository
            File file = new File(evaluationHeaderPathTEI);
            File[] refFiles = file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tei");
                }
            });

            if (refFiles == null)
                return null;

            int n = 0;
            for (; n < refFiles.length; n++) {
                File teifile = refFiles[n];
                String name = teifile.getName();
                //System.out.println(name);

                TEIHeaderSaxParser parser2 = new TEIHeaderSaxParser();
                parser2.setFileName(name);
                //parser2.pdfs = pdfs;
                // get a factory
                SAXParserFactory spf = SAXParserFactory.newInstance();
                //get a new instance of parser
                SAXParser par = spf.newSAXParser();
                par.parse(teifile, parser2);

//                ArrayList<String> labeled = parser2.getLabeledResult();
                // at this stage we have the expected results, line per line

//                String prefix = parser2.getPDFName();
                // the corresponding header is in the header repository with the identified prefix


                String l = "";
                if (l.length() == 0) continue;
                // the two last tokens, separated by a tabulation, gives the expected label
                // and, last, the resulting label
                StringTokenizer st = new StringTokenizer(l, "\t");
                String currentToken = null;
                String previousToken = null;
                while (st.hasMoreTokens()) {
                    currentToken = st.nextToken();

                    if (st.hasMoreTokens())
                        previousToken = currentToken;
                }

                //System.out.println(previousToken + " / " + currentToken);

                if ((previousToken == null) | (currentToken == null))
                    continue;
                // previousToken : expected
                // currentToken : obtained
                int ind = labels.indexOf(previousToken);
                if (ind != -1) {
                    if (previousToken.equals(currentToken)) {
                        Integer count = counterObserved.get(ind);
                        Integer newCount = count + 1;
                        counterObserved.set(ind, newCount);
                    } else {
                        int ind2 = labels.indexOf(currentToken);

                        if (ind2 != -1) {
                            Integer count = counterFalsePositive.get(ind2);
                            Integer newCount = count + 1;
                            counterFalsePositive.set(ind2, newCount);
                        } else {
                            labels.add(currentToken);
                            counterFalsePositive.add(1);
                            counterObserved.add(0);
                            counterExpected.add(0);
                            counterFalseNegative.add(0);
                        }

                        Integer count2 = counterFalseNegative.get(ind);
                        Integer newCount2 = count2 + 1;
                        counterFalseNegative.set(ind, newCount2);
                    }
                    Integer count = counterExpected.get(ind);
                    Integer newCount = count + 1;
                    counterExpected.set(ind, newCount);
                } else {
                    labels.add(previousToken);

                    if (previousToken.equals(currentToken)) {
                        counterObserved.add(1); // true positives
                        counterFalsePositive.add(0);
                        counterFalseNegative.add(0);
                    } else {
                        counterObserved.add(0);
                        counterFalsePositive.add(0);
                        counterFalseNegative.add(1);

                        int ind2 = labels.indexOf(currentToken);
                        if (ind2 != -1) {
                            Integer count = counterFalsePositive.get(ind2);
                            Integer newCount = count + 1;
                            counterFalsePositive.set(ind2, newCount);
                        } else {
                            labels.add(currentToken);
                            counterFalsePositive.add(1);
                            counterObserved.add(0);
                            counterExpected.add(0);
                            counterFalseNegative.add(0);
                        }
                    }

                    counterExpected.add(1);

                }
            }

            // print report
            int i = 0;

            // word
            report.append("\nWord-level results\n");
            report.append("\nlabel\t\taccuracy\tprecision\trecall\t\tf1\n\n");
            int cumulated_tp = 0;
            int cumulated_fp = 0;
            int cumulated_tn = 0;
            int cumulated_fn = 0;
//            int cumulated_all = 0;
            double cumulated_f0 = 0.0;
            int totalTokens = 0;

            DecimalFormat df = new DecimalFormat("#.####");

            while (i < labels.size()) {
                totalTokens += counterExpected.get(i);
                i++;
            }

            System.out.println("total: " + totalTokens);

            i = 0;
            while (i < labels.size()) {
                String label = labels.get(i);
                report.append(label);
                if (label.length() < 11)
                    report.append("\t");

                int tp = counterObserved.get(i); // true positives
                int fp = counterFalsePositive.get(i); // false positves
                int fn = counterFalseNegative.get(i); // false negative
                int tn = totalTokens - tp - (fp + fn); // true negatives
                int all = counterExpected.get(i); // all expected

                System.out.println(label + "*****: " + "true positives: " + tp + "\nfalse positives:" + fp +
                        "\ntrue negatives: " + tn + "\nfalse negatives: " + fn);
                System.out.println("all: " + all);

                double accuracy = (double) (tp + tn) / (tp + fp + tn + fn);
                report.append("\t").append(df.format(accuracy));

                double precision = (double) (tp) / (tp + fp);
                report.append("\t\t").append(df.format(precision));

                double recall = (double) (tp) / (tp + fn);
                report.append("\t\t").append(df.format(recall));

                double f0 = (2 * precision * recall) / (precision + recall);
                report.append("\t\t").append(df.format(f0));

                report.append("\n");

                cumulated_tp += tp;
                cumulated_fp += fp;
                cumulated_tn += tn;
                cumulated_fn += fn;
//                cumulated_all += all;
                cumulated_f0 += f0;
                i++;
            }
            report.append("\n");
            report.append("all labels\t");

            double accuracy = (double) (cumulated_tp + cumulated_tn) / (cumulated_tp + cumulated_fp + cumulated_tn + cumulated_fn);
            report.append("\t").append(df.format(accuracy));

            double precision = (double) cumulated_tp / (cumulated_tp + cumulated_fp);
            report.append("\t\t").append(df.format(precision));

            double recall = (double) cumulated_tp / (cumulated_tp + cumulated_fn);
            report.append("\t\t").append(df.format(recall));

            //double f0 = (2 * precision * recall) / (precision + recall);
            double f0 = cumulated_f0 / labels.size();
            report.append("\t\t").append(df.format(f0));

            report.append("\n");

            // field: a field is simply a sequence of word...
            // we do a second pass...
            //file = new File(path);
            /*fis = new FileInputStream(file);

                           // Here BufferedInputStream is added for fast reading.
                           bis = new BufferedInputStream(fis);
                           dis = new DataInputStream(bis);

                          boolean good = true;
                            String currentField = null;

                        // dis.available() returns 0 if the file does not have more lines.
                        while (dis.available() != 0) {
                            // read the line
                            String l = dis.readLine();
                            if (l.length() == 0) continue;

                            // the two last tokens, separated by a tabulation, gives the expected label and, last, the resulting label
                            StringTokenizer st = new StringTokenizer(l, "\t");
                            String currentToken = null;
                            String previousToken = null;
                            while (st.hasMoreTokens()) {
                                currentToken = st.nextToken();

                            if (st.hasMoreTokens())
                                previousToken = currentToken;
                            }

                            //System.out.println(previousToken + " / " + currentToken);
            */
/*				if ( (previousToken == null) |  (currentToken == null) )
					continue;
				// previousToken : expected
				// currentToken : obtained
				if (currentField == null)
					currentField = previousToken;
				else if (!currentField.equals(previousToken)) {
					// new field starts here
					currentField = previousToken;
					int ind = labels2.indexOf(previousToken);
					if (ind != -1) {
						if (good) {
							Integer count = counterObserved2.get(ind);
							Integer newCount = new Integer(count.intValue()+1);
							counterObserved2.set(ind, newCount);
						}
						else {
							int ind2 = labels2.indexOf(currentToken);

							if (ind2 != -1) {
								Integer count = counterFalse2.get(ind2);
								Integer newCount = new Integer(count.intValue()+1);
								counterFalse2.set(ind2, newCount);
							}
							else {
								labels2.add(currentToken);
								counterFalse2.add(new Integer(1));
								counterObserved2.add(new Integer(0));
								counterExpected2.add(new Integer(0));
							}
						}
						Integer count = counterExpected2.get(ind);
						Integer newCount = new Integer(count.intValue()+1);
						counterExpected2.set(ind, newCount);
					}
					else {
						labels2.add(previousToken);

						if (good) {
							counterObserved2.add(new Integer(1));
							counterFalse2.add(new Integer(0));
						}
						else {
							counterObserved2.add(new Integer(0));
							counterFalse2.add(new Integer(1));
						}

						counterExpected2.add(new Integer(1));
					}

					if (previousToken.equals(currentToken)) {
						good = true;
					}

	    		}
	    		else {
	    			// test and propagate word result in the rest of the field
	    			if (!previousToken.equals(currentToken)) {
	    				good = false;
	    				currentField = previousToken;
	    			}
	    		}
	    	}*/

/*	    	dis.close();
	    	bis.close();
    	
	    	i = 0;
	    	report.append("\nField-level results\n");
	    	report.append("\nlabel\t\taccuracy\tprecision\trecall\t\tf1\n\n");
    	
	    	cumulated_tp = 0;
	    	cumulated_fp = 0;
	    	cumulated_tn = 0;
	    	cumulated_fn = 0;
	    	cumulated_f0 = 0.0;
	    	cumulated_all = 0;

			totalTokens = 0;
			while(i<labels2.size()) {
	    		totalTokens += counterExpected2.get(i).intValue();
	    		i++;
	    	}
    	
	    	i = 0;
	*/
            /*    	while(i<labels2.size()) {
                   String label = labels2.get(i);
                   report.append(label);
                   if (label.length() < 11)
                       report.append("\t");

                   int tp = counterObserved2.get(i).intValue(); // true positives
                   int fp = counterFalse2.get(i).intValue(); // false positves
                   int tn = totalTokens - tp - (2 * fp); // true negatives
                   int fn = counterFalse2.get(i).intValue(); // false negative
                   int all = counterExpected2.get(i).intValue(); // all expected

                   accuracy = (double) (tp + tn) / ( tp + fp + tn + fn );
                   report.append("\t"+df.format(accuracy));

                   precision = (double) (tp) / (tp + fp);
                   report.append("\t\t"+df.format(precision));

                   recall = (double) (tp) / (tp + fn);;
                   report.append("\t\t"+df.format(recall));

                   f0 = (2 * precision * recall) / (precision + recall);
                   report.append("\t\t"+df.format(f0));

                   report.append("\n");

                   cumulated_tp += tp;
                   cumulated_fp += fp;
                   cumulated_tn += tn;
                   cumulated_fn += fn;
                   cumulated_all += all;
                   cumulated_f0 += f0;
                   i++;
               }
               report.append("\n");
               report.append("all fields\t");

               accuracy = (double) cumulated_tp / cumulated_all;
               report.append("\t"+df.format(accuracy));

               precision = (double) cumulated_tp / (cumulated_tp + cumulated_fp);
               report.append("\t\t"+df.format(precision));

               recall = (double) cumulated_tp / (cumulated_tp + cumulated_fn);
               report.append("\t\t"+df.format(recall));

               //f0 = (2 * precision * recall) / (precision + recall);
               f0 = cumulated_f0 / labels2.size();
               report.append("\t\t"+df.format(f0));

               report.append("\n");


               */

            // instance: separated by a new line in the result file

        } catch (Exception e) {
            // If an exception is generated, print a stack trace
//	    	e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
//	    	return null; 
        }

        return report.toString();
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        Evaluation util;
        try {
            util = new Evaluation();
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }

        if (args.length > 0) {
            try {
                if (args[0].equals("citation")) {
                    System.out.print("Evaluate results for " + args[0] + " model\n");
                    System.out.println(util.evaluateCitation());
                } else if (args[0].equals("header")) {
                    System.out.print("Evaluate results for " + args[0] + " model\n");
                    System.out.println(util.evaluateHeader());
                }
            } catch (Exception e) {
                throw new GrobidException("An exception occurred while running Grobid.", e);
            }
        }
    }

}