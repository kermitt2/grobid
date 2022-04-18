package org.grobid.core.data;

/**
 * Representing the context of a reference (to biblio/formula/table/figure)
 */
public class DataSetContext {
    public String context;
    private String documentCoords;
    private String teiId;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDocumentCoords() {
        return documentCoords;
    }

    public void setDocumentCoords(String documentCoords) {
        this.documentCoords = documentCoords;
    }

    public String getTeiId() {
        return teiId;
    }

    public void setTeiId(String teiId) {
        this.teiId = teiId;
    }
}
