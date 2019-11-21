package org.grobid.core.annotater;

import org.grobid.core.data.AcknowledgmentItem;

import java.util.List;

public class AcknowledgmentAnnotated {
    private String text;
    private List<AcknowledgmentItem> annotations;

    public void setAnnotations(List<AcknowledgmentItem> annotations) {
        this.annotations = annotations;
    }

    public List<AcknowledgmentItem> getAnnotations() {
        return annotations;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
