package org.grobid.trainer;

public final class LabelStat {
    private int falsePositive;
    private int falseNegative;
    private int observed;
    private int expected;

    public void incrementFalseNegative() { this.incrementFalseNegative(1); }
    public void incrementFalsePositive() { this.incrementFalsePositive(1); }
    public void incrementObserved() { this.incrementObserved(1);}
    public void incrementExpected() { this.incrementExpected(1); }

    void incrementFalseNegative(int count) { this.falseNegative += count; }
    void incrementFalsePositive(int count) { this.falsePositive += count; }
    void incrementObserved(int count) { this.observed += count; }
    void incrementExpected(int count) { this.expected += count; }

    public int getExpected() {
        return this.expected;
    }
    public int getFalseNegative() {
        return this.falseNegative;
    }
    public int getFalsePositive() {
        return this.falsePositive;
    }
    public int getObserved() {
        return this.observed;
    }

    public static LabelStat create() {
        return new LabelStat();
    }
}
