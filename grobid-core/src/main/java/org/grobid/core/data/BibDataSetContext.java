package org.grobid.core.data;

/**
 * Created by zholudev on 07/01/16.
 * Representing context of a reference
 */
public class BibDataSetContext {
    public String context;
    /*
        RgPath of citation reference within @context
     */

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
