package org.grobid.trainer;

public final class LabelStat {
    private int falsePositive = 0;
    private int falseNegative = 0;
    private int observed = 0; // this is true positives
    private int expected = 0; // total expected number of items with this label

    public void incrementFalseNegative() { 
        this.incrementFalseNegative(1); 
    }
    
    public void incrementFalsePositive() { 
        this.incrementFalsePositive(1); 
    }
    
    public void incrementObserved() { 
        this.incrementObserved(1);
    }
    
    public void incrementExpected() { 
        this.incrementExpected(1); 
    }

    public void incrementFalseNegative(int count) { 
        this.falseNegative += count; 
    }
    
    public void incrementFalsePositive(int count) { 
        this.falsePositive += count; 
    }
    
    public void incrementObserved(int count) { 
        this.observed += count; 
    }

    public void incrementExpected(int count) { 
        this.expected += count; 
    }

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

    public void setFalsePositive(int falsePositive) {
        this.falsePositive = falsePositive;
    }

    public void setFalseNegative(int falseNegative) {
        this.falseNegative = falseNegative;
    }

    public void setObserved(int observed) {
        this.observed = observed;
    }

    public void setExpected(int expected) {
        this.expected = expected;
    }

    public static LabelStat create() {
        return new LabelStat();
    }

    public double getPrecision() {
        if (observed == 0.0) {
            return 0.0;
        }
       // if ((falsePositive + falseNegative) >= observed)
       //     return 0.0;
        //return (double) (observed - (falsePositive + falseNegative) ) / (observed);
        return (double) observed / (falsePositive + observed);
    }

    public double getRecall() {
        if (expected == 0.0)
            return 0.0;
    //    if ((falsePositive + falseNegative) >= observed)
    //        return 0.0;
        //return (double) (observed - (falsePositive + falseNegative) ) / (expected);
        return (double) observed / (expected);
    }

    public double getF1Score() {
        double precision = getPrecision();
        double recall = getRecall();

        if ( (precision == 0) && (recall == 0.0) )
            return 0.0;

        return (2.0 * precision * recall) / (precision + recall);
    }

    @Override 
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
            .append("falsePositive: ").append(falsePositive)
            .append("; falseNegative: ").append(falseNegative)
            .append("; observed: ").append(observed)
            .append("; expected: ").append(expected);
        return builder.toString();
    }
}
