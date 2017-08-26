package org.grobid.trainer.evaluation;

import com.google.common.base.Function;
import org.chasen.crfpp.Tagger;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.engines.tagging.GrobidCRFEngine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.grobid.trainer.LabelStat;
import org.grobid.trainer.Stats;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic evaluation of a single-CRF model processing given an expected result. 
 * 
 * @author Patrice Lopez
 */
public class EvaluationUtilities {
	protected static final Logger logger = LoggerFactory.getLogger(EvaluationUtilities.class);

	/**
	 * Method for running a CRF tagger for evaluation purpose (i.e. with
	 * expected and actual labels).
	 * 
	 * @param ress
	 *            list
	 * @param tagger
	 *            a tagger
	 * @return a report
	 */
	public static String taggerRun(List<String> ress, Tagger tagger) {
		// clear internal context
		tagger.clear();
		StringBuilder res = new StringBuilder();

		// we have to re-inject the pre-tags because they are removed by the JNI
		// parse method
		ArrayList<String> pretags = new ArrayList<String>();
		// add context
		for (String piece : ress) {
			if (piece.trim().length() == 0) {
				// parse and change internal stated as 'parsed'
				if (!tagger.parse()) {
					// throw an exception
					throw new RuntimeException("CRF++ parsing failed.");
				}

				for (int i = 0; i < tagger.size(); i++) {
					for (int j = 0; j < tagger.xsize(); j++) {
						res.append(tagger.x(i, j)).append("\t");
					}
					res.append(pretags.get(i)).append("\t");
					res.append(tagger.y2(i));
					res.append("\n");
				}
				res.append(" \n");
				// clear internal context
				tagger.clear();
				pretags = new ArrayList<String>();
			} else {
				tagger.add(piece);
				tagger.add("\n");
				// get last tag
				StringTokenizer tokenizer = new StringTokenizer(piece, " \t");
				while (tokenizer.hasMoreTokens()) {
					String toke = tokenizer.nextToken();
					if (!tokenizer.hasMoreTokens()) {
						pretags.add(toke);
					}
				}
			}
		}

		// parse and change internal stated as 'parsed'
		if (!tagger.parse()) {
			// throw an exception
			throw new RuntimeException("CRF++ parsing failed.");
		}

		for (int i = 0; i < tagger.size(); i++) {
			for (int j = 0; j < tagger.xsize(); j++) {
				res.append(tagger.x(i, j)).append("\t");
			}
			res.append(pretags.get(i)).append("\t");
			res.append(tagger.y2(i));
			res.append("\n");
		}
		res.append("\n");

		return res.toString();
	}

    public static String evaluateStandard(String path, final GenericTagger tagger) {
        return evaluateStandard(path, new Function<List<String>, String>() {
            @Override
            public String apply(List<String> strings) {
                return tagger.label(strings);
            }
        });
    }

	public static String evaluateStandard(String path, Function<List<String>, String> taggerFunction) {
		String theResult = null;

		try {
			final BufferedReader bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));

			String line = null;
			List<String> citationBlocks = new ArrayList<String>();
			while ((line = bufReader.readLine()) != null) {
				citationBlocks.add(line);
			}
			long time = System.currentTimeMillis();
			theResult = taggerFunction.apply(citationBlocks);
			bufReader.close();

            System.out.println("Labeling took: " + (System.currentTimeMillis() - time) + " ms");
        } catch (Exception e) {
			throw new GrobidException("An exception occurred while evaluating Grobid.", e);
		}

		return reportMetrics(theResult);
	}

	public static String reportMetrics(String theResult) {
        StringBuilder report = new StringBuilder();
        
		Stats wordStats = tokenLevelStats(theResult);

		// report token-level results
		report.append("\n===== Token-level results =====\n\n");
		report.append(computeMetrics(wordStats));

		Stats fieldStats = fieldLevelStats(theResult);

		report.append("\n===== Field-level results =====\n");
		report.append(computeMetrics(fieldStats));
		
		// instance-level: instances are separated by a new line in the result file
		// third pass
		theResult = theResult.replace("\n\n", "\n \n");
		StringTokenizer stt = new StringTokenizer(theResult, "\n");
		boolean allGood = true;
		int correctInstance = 0;
		int totalInstance = 0;
		String line = null;
		while (stt.hasMoreTokens()) {
			line = stt.nextToken();
			if ((line.trim().length() == 0) || (!stt.hasMoreTokens())) {
				// we reinit for a new instance
				totalInstance++;
				if (allGood) {
					correctInstance++;
				}
				allGood = true;
			} else {
				StringTokenizer st = new StringTokenizer(line, "\t ");
				String obtainedLabel = null;
				String expectedLabel = null;
				while (st.hasMoreTokens()) {
					obtainedLabel = getPlainLabel(st.nextToken());
					if (st.hasMoreTokens()) {
						expectedLabel = obtainedLabel;
					}
				}

				if (!obtainedLabel.equals(expectedLabel)) {
					// one error is enough to have the whole instance false, damn!
					allGood = false;
				}
			}
		}

		report.append("\n===== Instance-level results =====\n\n");
		report.append(String.format("%-27s %d\n", "Total expected instances:", totalInstance));
		report.append(String.format("%-27s %d\n", "Correct instances:", correctInstance));
		double accuracy = (double) correctInstance / (totalInstance);
		report.append(String.format("%-27s %s\n",
				"Instance-level recall:",
				TextUtilities.formatTwoDecimals(accuracy * 100)));
	
		return report.toString();
	}

	public static Stats tokenLevelStats(String theResult) {
		Stats wordStats = new Stats();
		String line = null;
		StringTokenizer stt = new StringTokenizer(theResult, "\n");
		while (stt.hasMoreTokens()) {
			line = stt.nextToken();

			if (line.trim().length() == 0) {
				continue;
			}
			// the two last tokens, separated by a tabulation, gives the
			// expected label and, last, the resulting label -> for Wapiti
			StringTokenizer st = new StringTokenizer(line, "\t ");
			String obtainedLabel = null;
			String expectedLabel = null;

			while (st.hasMoreTokens()) {
				obtainedLabel = getPlainLabel(st.nextToken());
				if (st.hasMoreTokens()) {
					expectedLabel = obtainedLabel;
				}
			}
			
			if ((expectedLabel == null) || (obtainedLabel == null)) {
				continue;
			}

			processCounters(wordStats, obtainedLabel, expectedLabel);
			if (!obtainedLabel.equals(expectedLabel)) {
				logger.warn("Disagreement / expected: " + expectedLabel + " / obtained: " + obtainedLabel);
			}
		}
		return wordStats;
	}

	public static Stats fieldLevelStats(String theResult) {
		Stats fieldStats = new Stats();

		// field: a field is simply a sequence of token with the same label...
		// so instead of incrementing stats for each token, we increment when 
		// a sequence of tokens with the same label ends
		// second pass  
		boolean allGood = true;
		String previousExpectedLabel = null;
		String previousObtainedLabel = null;
		StringTokenizer stt = new StringTokenizer(theResult, "\n");
		boolean lastFieldProcessed = false;
		String line = null;
		while (stt.hasMoreTokens()) {
			line = stt.nextToken();
			if ((line.trim().length() == 0) && (previousExpectedLabel != null) && (previousObtainedLabel != null)) {
				// end of instance, so end of last field
				LabelStat previousLabelStat = fieldStats.getLabelStat(previousExpectedLabel);
				if (allGood) {
					previousLabelStat.incrementObserved(); // TP
				} else {
					previousLabelStat.incrementFalseNegative();
				}

				previousLabelStat.incrementExpected(); 

				previousLabelStat = fieldStats.getLabelStat(previousObtainedLabel);
				if (!allGood) {
					previousLabelStat.incrementFalsePositive();
					// erroneous observed field (false positive)
				}
				allGood = true;

				previousExpectedLabel = null;
				previousObtainedLabel = null;

				lastFieldProcessed = true;
				continue;
			}
			lastFieldProcessed = false;

			// the two last tokens, separated by a tabulation, gives the
			// expected label and, last,
			// the resulting label
			StringTokenizer st = new StringTokenizer(line, "\t ");
			String obtainedLabel = null;
			String expectedLabel = null;
			while (st.hasMoreTokens()) {
				obtainedLabel = getPlainLabel(st.nextToken());
				if (st.hasMoreTokens()) {
					expectedLabel = obtainedLabel;
				}
			}

			if ((expectedLabel == null) || (obtainedLabel == null)) {
				previousExpectedLabel = null;
				previousObtainedLabel = null;
				continue;
			}
			if ((previousExpectedLabel != null) && (!expectedLabel.equals(previousExpectedLabel))) {
				// new field starts in the reference data, so we finalize the stats for the previous field
				LabelStat previousLabelStat = fieldStats.getLabelStat(previousExpectedLabel);
				if (allGood) {
					previousLabelStat.incrementObserved(); // TP
				} else {
					previousLabelStat.incrementFalseNegative();
				}

				previousLabelStat.incrementExpected();

				previousLabelStat = fieldStats.getLabelStat(previousObtainedLabel);
				if (!allGood) {
					previousLabelStat.incrementFalsePositive();
					// erroneous observed field (false positive)
				} 

				// reinit the legendary allGood variable for next field
				allGood = true;
			}

			if ((previousObtainedLabel != null) && (!obtainedLabel.equals(previousObtainedLabel))) {
				// new field starts (maybe wrongly) in the obtained labelling, so we finalize the stats for the previous field
				LabelStat previousLabelStat = fieldStats.getLabelStat(previousObtainedLabel);
				if (!allGood) {
					previousLabelStat.incrementFalsePositive();
					// erroneous observed field (false positive)
				} 
			}

			if (!obtainedLabel.equals(expectedLabel)) {
				// the observed field will be wrong
				allGood = false;
			}

			previousExpectedLabel = expectedLabel;
			previousObtainedLabel = obtainedLabel;
		}
		
		// and finally this is for the last field which is closing with the end of the sequence labelling
		// only in case we don't have a final empty line
		// this is new from 26.03.2014 
		if ((previousExpectedLabel != null) && (previousObtainedLabel != null) && !lastFieldProcessed) {

			// end of last field for the expected fields
			LabelStat previousLabelStat = fieldStats.getLabelStat(previousExpectedLabel);
			if (allGood) {
				previousLabelStat.incrementObserved(); // TP
			} else {
				previousLabelStat.incrementFalseNegative();
			}

			previousLabelStat.incrementExpected();

			// end of last field for the observed fields
			previousLabelStat = fieldStats.getLabelStat(previousObtainedLabel);
			if (!allGood) {
				previousLabelStat.incrementFalsePositive();
				// erroneous observed field (false positive)
			} 
		} 

		return fieldStats;
	}

	private static String getPlainLabel(String label) {
		if (label == null)
			return null;
		if (label.startsWith("I-") || label.startsWith("E-") || label.startsWith("B-")) {
			return label.substring(2, label.length());
		} else 
			return label;
    }

    private static void processCounters(Stats stats, String obtained, String expected) {
        LabelStat expectedStat = stats.getLabelStat(expected);
        LabelStat obtainedStat = stats.getLabelStat(obtained);

        expectedStat.incrementExpected();

        if (expected.equals(obtained)) {
            expectedStat.incrementObserved();
        } else {
            expectedStat.incrementFalseNegative();
            obtainedStat.incrementFalsePositive();
        }
    }

	public static String computeMetrics(Stats stats) {
		StringBuilder report = new StringBuilder();
		report.append(String.format("\n%-20s %-12s %-12s %-12s %-7s\n\n",
				"label",
				"accuracy",
				"precision",
				"recall",
				"f1"));

		int cumulated_tp = 0;
		int cumulated_fp = 0;
		int cumulated_tn = 0;
		int cumulated_fn = 0;
		double cumulated_f0 = 0.0;
		double cumulated_accuracy = 0.0;
		double cumulated_precision = 0.0;
		double cumulated_recall = 0.0;
		int cumulated_all = 0;
		int totalValidFields = 0;

		int totalFields = 0;
		for (String label : stats.getLabels()) {
			LabelStat labelStat = stats.getLabelStat(label);
			totalFields += labelStat.getObserved();
			totalFields += labelStat.getFalsePositive();
		}

		for (String label : stats.getLabels()) {
			if (label.equals("<other>") || label.equals("base") || label.equals("O")) {
				continue;
			}

			LabelStat labelStat = stats.getLabelStat(label);
			int tp = labelStat.getObserved(); // true positives
			int fp = labelStat.getFalsePositive(); // false positives
			int fn = labelStat.getFalseNegative(); // false negative
			int tn = totalFields - tp - (fp + fn); // true negatives
			int all = labelStat.getExpected(); // all expected

			if (all != 0) {
				totalValidFields++;
			}

			double accuracy = (double) (tp + tn) / (tp + fp + tn + fn);

			double precision;
			if ((tp + fp) == 0) {
				precision = 0.0;
			} else {
				precision = (double) (tp) / (tp + fp);
			}

			double recall;
			if ((tp == 0) || (all == 0)) {
				recall = 0.0;
			} else {
				recall = (double) (tp) / all;
			}

			double f0;
			if (precision + recall == 0) {
				f0 = 0.0;
			} else {
				f0 = (2 * precision * recall) / (precision + recall);
			}

			report.append(String.format("%-20s %-12s %-12s %-12s %-7s\n",
					label,
					TextUtilities.formatTwoDecimals(accuracy * 100),
					TextUtilities.formatTwoDecimals(precision * 100),
					TextUtilities.formatTwoDecimals(recall * 100),
					TextUtilities.formatTwoDecimals(f0 * 100)));

			cumulated_tp += tp;
			cumulated_fp += fp;
			cumulated_tn += tn;
			cumulated_fn += fn;
			if (all != 0) {
				cumulated_all += all;
				cumulated_f0 += f0;
				cumulated_accuracy += accuracy;
				cumulated_precision += precision;
				cumulated_recall += recall;
			}
		}

		report.append("\n");

		// micro average over measures
		double accuracy = (double) (cumulated_tp + cumulated_tn) / (cumulated_tp + cumulated_fp + cumulated_tn + cumulated_fn);
		accuracy = Math.min(1.0, accuracy);

		double precision = (double) cumulated_tp / (cumulated_tp + cumulated_fp);
		precision = Math.min(1.0, precision);

		//recall = ((double) cumulated_tp) / (cumulated_tp + cumulated_fn);
		double recall = ((double) cumulated_tp) / (cumulated_all);
		recall = Math.min(1.0, recall);

		double f0 = (2 * precision * recall) / (precision + recall);

		report.append(String.format("%-20s %-12s %-12s %-12s %-7s (micro average)\n",
				"all fields",
				TextUtilities.formatTwoDecimals(accuracy * 100),
				TextUtilities.formatTwoDecimals(precision * 100),
				TextUtilities.formatTwoDecimals(recall * 100),
				TextUtilities.formatTwoDecimals(f0 * 100)));

		// macro average over measures
		accuracy = Math.min(1.0, cumulated_accuracy / (totalValidFields));
		precision = Math.min(1.0, cumulated_precision / totalValidFields);
		recall = Math.min(1.0, cumulated_recall / totalValidFields);
		f0 = Math.min(1.0, cumulated_f0 / totalValidFields);

		report.append(String.format("%-20s %-12s %-12s %-12s %-7s (macro average)\n",
				"",
				TextUtilities.formatTwoDecimals(accuracy * 100),
				TextUtilities.formatTwoDecimals(precision * 100),
				TextUtilities.formatTwoDecimals(recall * 100),
				TextUtilities.formatTwoDecimals(f0 * 100)));

		return report.toString();
	}
}
