package org.grobid.trainer.evaluation;

import org.grobid.core.utilities.TextUtilities;

import java.io.PrintStream;

/**
 * Represent all different evaluation given a specific model
 */
public class ModelStats {
    private int totalInstances;
    private int correctInstance;
    private Stats tokenStats;
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

    public void setTokenStats(Stats tokenStats) {
        this.tokenStats = tokenStats;
    }

    public Stats getTokenStats() {
        return tokenStats;
    }

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

        // report token-level results
        Stats wordStats = getTokenStats();
        report.append("\n===== Token-level results =====\n\n");
        report.append(wordStats.getReport());

        // report field-level results
        Stats fieldStats = getFieldStats();
        report.append("\n===== Field-level results =====\n");
        report.append(fieldStats.getReport());

        // instance-level: instances are separated by a new line in the result file
        report.append("\n===== Instance-level results =====\n\n");
        report.append(String.format("%-27s %d\n", "Total expected instances:", getTotalInstances()));
        report.append(String.format("%-27s %d\n", "Correct instances:", getCorrectInstance()));
        report.append(String.format("%-27s %s\n",
            "Instance-level recall:",
            TextUtilities.formatTwoDecimals(getInstanceRecall() * 100)));

        return report.toString();
    }
}
