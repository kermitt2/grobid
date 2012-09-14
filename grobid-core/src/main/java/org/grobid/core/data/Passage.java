package org.grobid.core.data;

/**
 * Class for managing passage of citations.
 *
 * @author Patrice Lopez
 */
public class Passage {
    private int pageBegin = -1;
    private int pageEnd = -1;
    private int lineBegin = -1;
    private int lineEnd = -1;

    private String colBegin = null;
    private String colEnd = null;

    private String figure = null;
    private String table = null;

    private String rawPassage = null;

    public int getPageBegin() {
        return pageBegin;
    }

    public int getPageEnd() {
        return pageEnd;
    }

    public int getLineBegin() {
        return lineBegin;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    public String getColBegin() {
        return colBegin;
    }

    public String getColEnd() {
        return colEnd;
    }

    public String getFigure() {
        return figure;
    }

    public String getTable() {
        return table;
    }

    public String getRawPassage() {
        return rawPassage;
    }

    public void setRawPassage(String s) {
        rawPassage = s;
    }
}
