package org.grobid.trainer.evaluation;

/**
 * Represent all different evaluation given a specific model
 */
public class ModelStats {
    private int totalInstances;
    private int correctInstance;
    private int instanceAccuracy;
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

    public void setInstanceAccuracy(int instanceAccuracy) {
        this.instanceAccuracy = instanceAccuracy;
    }

    public int getInstanceAccuracy() {
        return instanceAccuracy;
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
}
