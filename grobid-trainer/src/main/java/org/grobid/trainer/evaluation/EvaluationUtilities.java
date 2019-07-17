package org.grobid.trainer.evaluation;

import org.chasen.crfpp.Tagger;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;

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
     * @param ress   list
     * @param tagger a tagger
     * @return a report
     */
    public static String taggerRun(List<String> ress, Tagger tagger) {
        // clear internal context
        tagger.clear();
        StringBuilder res = new StringBuilder();

        // we have to re-inject the pre-tags because they are removed by the JNI
        // parse method
        ArrayList<String> pretags = new ArrayList<>();
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
                pretags = new ArrayList<>();
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

    public static ModelStats evaluateStandard(String path, final GenericTagger tagger) {
        return evaluateStandard(path, tagger::label);
    }

    public static ModelStats evaluateStandard(String path, Function<List<String>, String> taggerFunction) {
        String theResult = null;

        try {
            final BufferedReader bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));

            String line = null;
            List<String> instance = new ArrayList<>();
            while ((line = bufReader.readLine()) != null) {
                instance.add(line);
            }
            long time = System.currentTimeMillis();
            theResult = taggerFunction.apply(instance);
            bufReader.close();
            System.out.println("Labeling took: " + (System.currentTimeMillis() - time) + " ms");
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while evaluating Grobid.", e);
        }

        return computeStats(theResult);
    }

    public static ModelStats computeStats(String theResult) {
        ModelStats modelStats = new ModelStats();
        modelStats.setRawResults(theResult);
        // report token-level results
//        Stats wordStats = tokenLevelStats(theResult);
//        modelStats.setTokenStats(wordStats);

        // report field-level results
        Stats fieldStats = fieldLevelStats(theResult);
        modelStats.setFieldStats(fieldStats);

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

        modelStats.setTotalInstances(totalInstance);
        modelStats.setCorrectInstance(correctInstance);

        return modelStats;
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
			/*if (!obtainedLabel.equals(expectedLabel)) {
                logger.warn("Disagreement / expected: " + expectedLabel + " / obtained: " + obtainedLabel);
			}*/
        }
        return wordStats;
    }

    public static Stats fieldLevelStats(String theResult) {
        Stats fieldStats = new Stats();

        // field: a field is simply a sequence of token with the same label

        // we build first the list of fields in expected and obtained result
        // with offset positions
        List<Pair<String, OffsetPosition>> expectedFields = new ArrayList<>();
        List<Pair<String, OffsetPosition>> obtainedFields = new ArrayList<>();
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

            if ((obtainedLabel == null) || (expectedLabel == null))
                continue;

            if ((previousObtainedLabel != null) &&
                (!obtainedLabel.equals(getPlainLabel(previousObtainedLabel)))) {
                // new obtained field
                currentObtainedPosition.end = pos - 1;
                Pair theField = new Pair<>(getPlainLabel(previousObtainedLabel),
                    currentObtainedPosition);
                currentObtainedPosition = new OffsetPosition();
                currentObtainedPosition.start = pos;
                obtainedFields.add(theField);
            }

            if ((previousExpectedLabel != null) &&
                (!expectedLabel.equals(getPlainLabel(previousExpectedLabel)))) {
                // new expected field
                currentExpectedPosition.end = pos - 1;
                Pair theField = new Pair<>(getPlainLabel(previousExpectedLabel),
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
            Pair theField = new Pair<>(getPlainLabel(previousObtainedLabel),
                currentObtainedPosition);
            obtainedFields.add(theField);
        }

        if ((previousExpectedLabel != null)) {
            currentExpectedPosition.end = pos - 1;
            Pair theField = new Pair<>(getPlainLabel(previousExpectedLabel),
                currentExpectedPosition);
            expectedFields.add(theField);
        }

        // we then simply compared the positions and labels of the two fields and update
        // statistics
        int obtainedFieldIndex = 0;
        List<Pair<String, OffsetPosition>> matchedObtainedFields = new ArrayList<Pair<String, OffsetPosition>>();
        for (Pair<String, OffsetPosition> expectedField : expectedFields) {
            expectedLabel = expectedField.getA();
            int expectedStart = expectedField.getB().start;
            int expectedEnd = expectedField.getB().end;

            LabelStat labelStat = fieldStats.getLabelStat(getPlainLabel(expectedLabel));
            labelStat.incrementExpected();

            // try to find a match in the obtained fields
            boolean found = false;
            for (int i = obtainedFieldIndex; i < obtainedFields.size(); i++) {
                obtainedLabel = obtainedFields.get(i).getA();
                if (!expectedLabel.equals(obtainedLabel))
                    continue;
                if ((expectedStart == obtainedFields.get(i).getB().start) &&
                    (expectedEnd == obtainedFields.get(i).getB().end)) {
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
        for (Pair<String, OffsetPosition> obtainedField : obtainedFields) {
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
        return stats.getOldReport();
    }

}
