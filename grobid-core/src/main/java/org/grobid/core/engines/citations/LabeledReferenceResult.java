package org.grobid.core.engines.citations;

/**
 * User: zholudev
 * Date: 4/15/14
 */
public class LabeledReferenceResult {
    private String label = null;
    private final String referenceText;
	private String features; // optionally the vector of features corresponding to the token referenceText

    public LabeledReferenceResult(String label, String referenceText) {
        this.label = label;
        this.referenceText = referenceText;
    }

    public LabeledReferenceResult(String referenceText) {
        this.referenceText = referenceText;
    }

    public LabeledReferenceResult(String label, String referenceText, String features) {
        this.label = label;
        this.referenceText = referenceText;
		this.features = features;
    }

    public String getLabel() {
        return label;
    }

    public String getReferenceText() {
        return referenceText;
    }
	
    public String getFeatures() {
        return features;
    }

    @Override
    public String toString() {
        return "** " + (label == null ? "" : label) + " ** " + referenceText;
    }
}
