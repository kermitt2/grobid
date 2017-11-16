package org.grobid.trainer;

import java.util.Set;
import java.util.TreeMap;

import org.grobid.core.exceptions.*;

public final class Stats {
    private final TreeMap<String, LabelStat> labelStats;

    public Stats() {
        this.labelStats = new TreeMap<>();
    }

    public Set<String> getLabels() {
        return this.labelStats.keySet();
    }

    public void incrementFalsePositive(String label) {
        this.incrementFalsePositive(label, 1);
    }

    public void incrementFalsePositive(String label, int count) {
        LabelStat labelStat = this.getLabelStat(label);
        if (labelStat == null)
            throw new GrobidException("Unknown label: " + label);
        labelStat.incrementFalsePositive(count);
    }

    public void incrementFalseNegative(String label) {
        this.incrementFalseNegative(label, 1);
    }

    public void incrementFalseNegative(String label, int count) {
        LabelStat labelStat = this.getLabelStat(label);
        if (labelStat == null)
            throw new GrobidException("Unknown label: " + label);
        labelStat.incrementFalseNegative(count);
    }

    public void incrementObserved(String label) {
        this.incrementObserved(label, 1);
    }

    public void incrementObserved(String label, int count) {
        LabelStat labelStat = this.getLabelStat(label);
        if (labelStat == null)
            throw new GrobidException("Unknown label: " + label);
        labelStat.incrementObserved(count);
    }

    public void incrementExpected(String label) {
        this.incrementExpected(label, 1);
    }

    public void incrementExpected(String label, int count) {
        LabelStat labelStat = this.getLabelStat(label);
        if (labelStat == null)
            throw new GrobidException("Unknown label: " + label);
        labelStat.incrementExpected(count);
    }

    public LabelStat getLabelStat(String label) {
        if (this.labelStats.containsKey(label)) {
            return this.labelStats.get(label);
        }

        LabelStat labelStat = LabelStat.create();
        this.labelStats.put(label, labelStat);

        return labelStat;
    }

    public int size() {
        return this.labelStats.size();
    }

    public double getPrecision(String label) {
        LabelStat labelStat = this.getLabelStat(label);
        if (labelStat == null)
            throw new GrobidException("Unknown label: " + label);
        return labelStat.getPrecision();
    }

    public double getRecall(String label) {
        LabelStat labelStat = this.getLabelStat(label);
        if (labelStat == null)
            throw new GrobidException("Unknown label: " + label);
        return labelStat.getRecall();
    }

    public double getF1Score(String label) {
        LabelStat labelStat = this.getLabelStat(label);
        if (labelStat == null)
            throw new GrobidException("Unknown label: " + label);
        return labelStat.getF1Score();
    }

    /**
     * Return the micro average precision, which is the precision calculated
     * on the cumulation of the TP and FP over the whole data
     */
    public double getMicroAveragePrecision() {
        double cumulatedTruePositive = 0.0;
        double cumulatedFalsePositive = 0.0;

        for (String label : getLabels()) {
            if (label.equals("<other>") || label.equals("base") || label.equals("O")) {
                continue;
            }
            final LabelStat labelStat = getLabelStat(label);
            if (labelStat.getExpected() != 0) {
                cumulatedTruePositive += labelStat.getObserved();
                cumulatedFalsePositive += labelStat.getFalsePositive();
            }
        }

        double precision = 0.0;
        if (cumulatedTruePositive + cumulatedFalsePositive != 0)
            precision = cumulatedTruePositive / (cumulatedTruePositive + cumulatedFalsePositive);

        return Math.min(1.0, precision);
    }

    /**
     * Calculate the macro average precision, which is the
     * mean average among all the precision for each label
     */
    public double getMacroAveragePrecision() {
        int totalValidFields = 0;
        double cumulated_precision = 0.0;

        for (String label : getLabels()) {
            if (label.equals("<other>") || label.equals("base") || label.equals("O")) {
                continue;
            }
            final LabelStat labelStat = getLabelStat(label);
            if (labelStat.getExpected() != 0) {
                totalValidFields++;
                double labelPrecision = labelStat.getPrecision();
                cumulated_precision += labelPrecision;
            }
        }

        if (totalValidFields == 0)
            return 0.0;

        return Math.min(1.0, cumulated_precision / totalValidFields);
    }

    public double getMicroAverageRecall() {
        double cumulatedTruePositive = 0.0;
        double cumulatedExpected = 0.0;

        for (String label : getLabels()) {
            if (label.equals("<other>") || label.equals("base") || label.equals("O")) {
                continue;
            }
            final LabelStat labelStat = getLabelStat(label);
            if (labelStat.getExpected() != 0) {
                cumulatedTruePositive += labelStat.getObserved();
                cumulatedExpected += labelStat.getExpected();
            }
        }

        double recall = 0.0;
        if (cumulatedExpected != 0.0)
            recall = cumulatedTruePositive / cumulatedExpected;

        return Math.min(1.0, recall);
    }

    public double getMacroAverageRecall() {
        int totalValidFields = 0;
        double cumulatedRecall = 0.0;

        for (String label : getLabels()) {
            if (label.equals("<other>") || label.equals("base") || label.equals("O")) {
                continue;
            }

            final LabelStat labelStat = getLabelStat(label);
            if (labelStat.getExpected() != 0) {
                totalValidFields++;
                cumulatedRecall += labelStat.getRecall();
            }
        }

        if (totalValidFields == 0)
            return 0.0;

        return Math.min(1.0, cumulatedRecall / totalValidFields);
    }

    public int getTotalFields() {
        int totalFields = 0;
        for (String label : getLabels()) {
            totalFields += getLabelStat(label).getAll();
        }

        return totalFields;
    }

    public double getMicroAverageF1() {
        double precision = getMicroAveragePrecision();
        double recall = getMicroAverageRecall();

        double f1 = 0.0;
        if (precision + recall != 0.0)
            f1 = (2 * precision * recall) / (precision + recall);

        return f1;
    }

    public double getMacroAverageF1() {
        double cumulatedF1 = 0.0;
        int totalValidFields = 0;

        for (String label : getLabels()) {
            if (label.equals("<other>") || label.equals("base") || label.equals("O")) {
                continue;
            }

            final LabelStat labelStat = getLabelStat(label);
            if (labelStat.getExpected() != 0) {
                totalValidFields++;
                double labelF1 = labelStat.getF1Score();
                cumulatedF1 += labelF1;
            }
        }

        if (totalValidFields == 0)
            return 0.0;

        return Math.min(1.0, cumulatedF1 / totalValidFields);
    }
}

