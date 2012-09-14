package org.grobid.core.document;

import java.util.ArrayList;

/**
 * Class for representing figure or table objects occuring within a document.
 *
 * @author Patrice Lopez
 */
public class NonTextObject {

    private String header = null;
    private String description = null;
    private ArrayList<String> files = null;

    public static final int Unknown = -1; // unknown object type
    public static final int Figure = 0; // figure type
    public static final int Table = 1; // table type
    public static final int GraphicBitmap = 2; // pure graphic object
    public static final int GraphicVectoriel = 3; // pure graphic object

    private int type = -1;

    private int startPosition = -1;
    private int endPosition = -1;
    private int blockNumber = -1;
    private int page = -1;

    private double x1 = 0.0;
    private double y1 = 0.0;
    private double x2 = 0.0;
    private double y2 = 0.0;

    public String getHeader() {
        return this.header;
    }

    public String getDescription() {
        return this.description;
    }

    public ArrayList<String> getFiles() {
        return this.files;
    }

    public int getType() {
        return type;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFiles(ArrayList<String> files) {
        this.files = files;
    }

    public void addFile(String file) {
        if (this.files == null) {
            files = new ArrayList<String>();
        }
        this.files.add(file);
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String toString() {
        StringBuffer res = new StringBuffer();
        if (type == this.Figure) {
            res.append("Figure [");
        } else if (type == this.Table) {
            res.append("Table [");
        } else if (type == this.GraphicBitmap) {
            res.append("Graphic Bitmap [");
        } else if (type == this.GraphicVectoriel) {
            res.append("Graphic Vectoriel [");
        } else {
            res.append("Unknown [");
        }

        if (page != -1) {
            res.append(page + "\t");
        } else {
            res.append("\t");
        }

        if (startPosition != -1) {
            res.append(startPosition);
        }
        res.append("-");
        if (endPosition != -1) {
            res.append(endPosition);
        }
        res.append("]: \t");

        if (header != null) {
            res.append(header + "\t");
        } else {
            res.append("\t");
        }
        if (description != null) {
            res.append(description + "\t");
        } else {
            res.append("\t");
        }
        if (files != null) {
            for (String file : files) {
                res.append(file + "\t");
            }
        } else {
            res.append("\t");
        }
        res.append(header + "\t");

        res.append("(" + x1 + ", " + y1 + ")" + "\t" + "(" + x2 + ", " + y2 + ")" + "\t");

        return res.toString();
    }
}