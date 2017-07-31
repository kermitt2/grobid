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
}

