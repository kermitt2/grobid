package org.grobid.core.engines.citations;

/**
 * User: zholudev
 * Date: 4/15/14
 */
public class LabeledReferenceResult {
    private String label = null;
    private final String referenceText;

    public LabeledReferenceResult(String label, String referenceText) {
        this.label = label;
        this.referenceText = referenceText;
    }

    public LabeledReferenceResult(String referenceText) {
        this.referenceText = referenceText;
    }

    public String getLabel() {
        return label;
    }

    public String getReferenceText() {
        return referenceText;
    }

    @Override
    public String toString() {
        return "** " + (label == null ? "" : label) + " ** " + referenceText;
    }
}
