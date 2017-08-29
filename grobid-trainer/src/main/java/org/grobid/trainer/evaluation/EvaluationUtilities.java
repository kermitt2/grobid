package org.grobid.trainer.evaluation;

import com.google.common.base.Function;
import org.chasen.crfpp.Tagger;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;
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
			res.append(System.lineSeparator());
		}
		res.append(System.lineSeparator());

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
		
		report.append("\n===== Instance-level results =====\n\n");
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
				// instance done
				totalInstance++;
				if (allGood) {
					correctInstance++;
				}
				// we reinit for a new instance
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
		StringTokenizer stt = new StringTokenizer(theResult, System.lineSeparator());
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

		// field: a field is simply a sequence of token with the same label
		
		// we build first the list of fields in expected and obtained result
		// with offset positions
		List<Pair<String,OffsetPosition>> expectedFields = new ArrayList<Pair<String,OffsetPosition>>(); 
		List<Pair<String,OffsetPosition>> obtainedFields = new ArrayList<Pair<String,OffsetPosition>>(); 
		StringTokenizer stt = new StringTokenizer(theResult, System.lineSeparator());
		String line = null;
		String previousExpectedLabel = null;
		String previousObtainedLabel = null;
		int pos = 0; // current token index
		OffsetPosition currentObtainedPosition = new OffsetPosition();
		currentObtainedPosition.start = 0;
		OffsetPosition currentExpectedPosition = new OffsetPosition();
		currentExpectedPosition.start = 0;
		String obtainedLabel = null;
		String expectedLabel = null;
		while (stt.hasMoreTokens()) {
			line = stt.nextToken();
			obtainedLabel = null;
			expectedLabel = null;
			StringTokenizer st = new StringTokenizer(line, "\t ");	
			while (st.hasMoreTokens()) {
				obtainedLabel = st.nextToken();
				if (st.hasMoreTokens()) {
					expectedLabel = obtainedLabel;
				}
			}

			if ( (obtainedLabel == null) || (expectedLabel == null) )
				continue;

			if ((previousObtainedLabel != null) && 
				(!obtainedLabel.equals(getPlainLabel(previousObtainedLabel)))) {
				// new obtained field
				currentObtainedPosition.end = pos - 1;
				Pair theField = new Pair<String,OffsetPosition>(getPlainLabel(previousObtainedLabel), 
					currentObtainedPosition);
				currentObtainedPosition = new OffsetPosition();
				currentObtainedPosition.start = pos;
				obtainedFields.add(theField);
			}

			if ((previousExpectedLabel != null) && 
				(!expectedLabel.equals(getPlainLabel(previousExpectedLabel)))) {
				// new expected field
				currentExpectedPosition.end = pos - 1;
				Pair theField = new Pair<String,OffsetPosition>(getPlainLabel(previousExpectedLabel), 
					currentExpectedPosition);
				currentExpectedPosition = new OffsetPosition();
				currentExpectedPosition.start = pos;
				expectedFields.add(theField);
			}

			previousExpectedLabel = expectedLabel;
			previousObtainedLabel = obtainedLabel;
			pos++;
		}
		// last fields of the sequence
		if ((previousObtainedLabel != null)) {
			currentObtainedPosition.end = pos - 1;
			Pair theField = new Pair<String,OffsetPosition>(getPlainLabel(previousObtainedLabel), 
				currentObtainedPosition);
			obtainedFields.add(theField);
		}

		if ((previousExpectedLabel != null)) {
			currentExpectedPosition.end = pos - 1;
			Pair theField = new Pair<String,OffsetPosition>(getPlainLabel(previousExpectedLabel), 
				currentExpectedPosition);
			expectedFields.add(theField);
		}

		// we then simply compared the positions and labels of the two fields and update 
		// statistics
		int obtainedFieldIndex = 0;
		List<Pair<String,OffsetPosition>> matchedObtainedFields = new ArrayList<Pair<String,OffsetPosition>>(); 
		for(Pair<String,OffsetPosition> expectedField : expectedFields) {
			expectedLabel = expectedField.getA();
			int expectedStart = expectedField.getB().start;
			int expectedEnd = expectedField.getB().end;

			LabelStat labelStat = fieldStats.getLabelStat(getPlainLabel(expectedLabel));
			labelStat.incrementExpected(); 

			// try to find a match in the obtained fields
			boolean found = false;
			for(int i=obtainedFieldIndex; i<obtainedFields.size(); i++) {
				obtainedLabel = obtainedFields.get(i).getA();
				if (!expectedLabel.equals(obtainedLabel)) 
					continue;
				if ( (expectedStart == obtainedFields.get(i).getB().start) &&
					 (expectedEnd == obtainedFields.get(i).getB().end) ) {
					// we have a match
					labelStat.incrementObserved(); // TP
					found = true;
					obtainedFieldIndex = i;
					matchedObtainedFields.add(obtainedFields.get(i));
					break;
				}
				// if we went too far, we can stop the pain
				if (expectedEnd < obtainedFields.get(i).getB().start) {
					break;
				}
			}
			if (!found) {
				labelStat.incrementFalseNegative();
			}
		}

		// all the obtained fields without match in the expected fields are false positive
		for(Pair<String,OffsetPosition> obtainedField :obtainedFields) {
			if (!matchedObtainedFields.contains(obtainedField)) {
				obtainedLabel = obtainedField.getA();
				LabelStat labelStat = fieldStats.getLabelStat(getPlainLabel(obtainedLabel));
				labelStat.incrementFalsePositive();
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
			totalFields += labelStat.getFalseNegative();
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
			if (accuracy < 0.0)
				accuracy = 0.0;

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
		double accuracy = 0.0;
		if (cumulated_tp + cumulated_fp + cumulated_tn + cumulated_fn != 0.0)
			accuracy = (double) (cumulated_tp + cumulated_tn) / (cumulated_tp + cumulated_fp + cumulated_tn + cumulated_fn);
		accuracy = Math.min(1.0, accuracy);

		double precision = 0.0;
		if (cumulated_tp + cumulated_fp != 0)
			precision = (double) cumulated_tp / (cumulated_tp + cumulated_fp);
		precision = Math.min(1.0, precision);

		//recall = ((double) cumulated_tp) / (cumulated_tp + cumulated_fn);
		double recall = 0.0;
		if (cumulated_all != 0.0)
			recall = ((double) cumulated_tp) / (cumulated_all);
		recall = Math.min(1.0, recall);

		double f0 = 0.0;
		if (precision + recall != 0.0)
			f0 = (2 * precision * recall) / (precision + recall);

		report.append(String.format("%-20s %-12s %-12s %-12s %-7s (micro average)\n",
				"all fields",
				TextUtilities.formatTwoDecimals(accuracy * 100),
				TextUtilities.formatTwoDecimals(precision * 100),
				TextUtilities.formatTwoDecimals(recall * 100),
				TextUtilities.formatTwoDecimals(f0 * 100)));

		// macro average over measures
		if (totalValidFields == 0)
			accuracy = 0.0;
		else
			accuracy = Math.min(1.0, cumulated_accuracy / (totalValidFields));
		
		if (totalValidFields == 0)
			precision = 0.0;
		else
			precision = Math.min(1.0, cumulated_precision / totalValidFields);
	
		if (totalValidFields == 0)
			recall = 0.0;
		else
			recall = Math.min(1.0, cumulated_recall / totalValidFields);
		
		if (totalValidFields == 0)
			f0 = 0.0;
		else
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
