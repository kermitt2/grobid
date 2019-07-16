package org.grobid.trainer.evaluation;

import org.grobid.core.utilities.TextUtilities;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represent all different evaluation given a specific model
 */
public class ModelStats {
    private int totalInstances;
    private int correctInstance;
    //    private Stats tokenStats;
    private Stats fieldStats;


    public void setTotalInstances(int totalInstances) {
        this.totalInstances = totalInstances;
    }

    public int getTotalInstances() {
        return totalInstances;
    }

    public void setCorrectInstance(int correctInstance) {
        this.correctInstance = correctInstance;
    }

    public int getCorrectInstance() {
        return correctInstance;
    }

//    public void setTokenStats(Stats tokenStats) {
//        this.tokenStats = tokenStats;
//    }

//    public Stats getTokenStats() {
//        return tokenStats;
//    }

    public void setFieldStats(Stats fieldStats) {
        this.fieldStats = fieldStats;
    }

    public Stats getFieldStats() {
        return fieldStats;
    }

    public double getInstanceRecall() {
        if (getTotalInstances() <= 0) {
            return 0.0d;
        }
        return (double) getCorrectInstance() / (getTotalInstances());
    }

    public String toString() {
        StringBuilder report = new StringBuilder();

        Stats fieldStats = getFieldStats();
        report.append("\n===== Field-level results =====\n");
        report.append(String.format("\n%-20s %-12s %-12s %-12s %-12s %-7s\n\n",
            "label",
            "accuracy",
            "precision",
            "recall",
            "f1",
            "support"));


        for (Map.Entry<String, LabelResult> labelResult : fieldStats.getLabelsResults().entrySet()) {
            report.append(labelResult.getValue());
        }

        report.append("\n");

        report.append(String.format("%-20s %-12s %-12s %-12s %-12s %-7s\n",
            "all (micro avg.)",
            TextUtilities.formatTwoDecimals(fieldStats.getMicroAverageAccuracy() * 100),
            TextUtilities.formatTwoDecimals(fieldStats.getMicroAveragePrecision() * 100),
            TextUtilities.formatTwoDecimals(fieldStats.getMicroAverageRecall() * 100),
            TextUtilities.formatTwoDecimals(fieldStats.getMicroAverageF1() * 100),
            String.valueOf(getSupportSum())));

        report.append(String.format("%-20s %-12s %-12s %-12s %-12s %-7s\n",
            "all (macro avg.)",
            TextUtilities.formatTwoDecimals(fieldStats.getMacroAverageAccuracy() * 100),
            TextUtilities.formatTwoDecimals(fieldStats.getMacroAveragePrecision() * 100),
            TextUtilities.formatTwoDecimals(fieldStats.getMacroAverageRecall() * 100),
            TextUtilities.formatTwoDecimals(fieldStats.getMacroAverageF1() * 100),
            String.valueOf(getSupportSum())));


        // instance-level: instances are separated by a new line in the result file
        report.append("\n===== Instance-level results =====\n\n");
        report.append(String.format("%-27s %d\n", "Total expected instances:", getTotalInstances()));
        report.append(String.format("%-27s %d\n", "Correct instances:", getCorrectInstance()));
        report.append(String.format("%-27s %s\n",
            "Instance-level recall:",
            TextUtilities.formatTwoDecimals(getInstanceRecall() * 100)));

        return report.toString();
    }

    public long getSupportSum() {
        long supportSum = 0;
        for (LabelResult labelResult : fieldStats.getLabelsResults().values()) {
            supportSum += labelResult.getSupport();
        }
        return supportSum;
    }

}
