package org.grobid.core.data;

import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;

import java.util.List;

public class MonographItem {
    private String text;
    private String label;
    private List<LayoutToken> tokens;
    BoundingBox boundingBox;

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<LayoutToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<LayoutToken> tokens) {
        this.tokens = tokens;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
