package org.grobid.core.layout;

import java.io.File;
import java.util.ArrayList;

/**
 * Class for representing graphical objects occurring within a document.
 *
 * @author Patrice Lopez
 */
public class GraphicObject {
    private String filePath = null;

    // I am too lazy to do an enum type...
    public static final int UNKNOWN = -1; // unknown object type
    public static final int BITMAP = 2; // pure graphic object
    public static final int VECTOR = 3; // pure graphic object

    private int type = UNKNOWN;

    // position in the global tokenization
    private int startPosition = -1;
    private int endPosition = -1;

    private int blockNumber = -1;

    private BoundingBox boundingBox = null;
    
    // in case of vector image, we don't have a boundingBox from pdf2xml, simply the page information
    private int page = -1;
    
    public boolean used;

    /**
     * Return the full path of the file corresponding to the graphic object, useful
     * as internal implementation information only
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Return an URI for the file corresponding to the graphic object, in pratice a 
     * portable relative path usable for data exchange
     */
    public String getURI() {
        int ind = filePath.lastIndexOf("/");
        if (ind != -1) {
            int ind2 = filePath.substring(0, ind-1).lastIndexOf("/");
            if (ind2 != -1)
                return filePath.substring(0, ind2);
        }
//        return filePath;
        return new File(filePath).getName();
    }

    public int getType() {
        return type;
    }

    public void setFilePath(String path) {
        this.filePath = path;
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

    public double getX() {
        if (boundingBox != null)
            return boundingBox.getX();
        else 
            return 0.0;
    }

    public double getY() {
        if (boundingBox != null)
            return boundingBox.getY();
        else 
            return 0.0;
    }

    public double getWidth() {
        if (boundingBox != null)
            return boundingBox.getWidth();
        else 
            return 0.0;
    }

    public double getHeight() {
        if (boundingBox != null)
            return boundingBox.getHeight();
        else 
            return 0.0;
    }

    public int getPage() {
        if (boundingBox != null)
            return boundingBox.getPage();
        else 
            return page;
    }

    /*public void setX(double x1) {
        this.x = Math.abs(x1);
    }

    public void setY(double y1) {
        this.y = Math.abs(y1);
    }

    public void setWidth(double x2) {
        this.width = Math.abs(x2);
    }

    public void setHeight(double y2) {
        this.height = Math.abs(y2);
    }*/

    public void setPage(int page) {
        this.page = page;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox box) {
        boundingBox = box;
    }

    public String toString() {
        StringBuffer res = new StringBuffer();
        if (type == this.BITMAP) {
            res.append("Graphic Bitmap [");
        } else if (type == this.VECTOR) {
            res.append("Vector Graphic [");
        } else {
            res.append("Unknown [");
        }

        if (startPosition != -1) {
            res.append(startPosition);
        }
        res.append("-");
        if (endPosition != -1) {
            res.append(endPosition);
        }
        res.append("]: \t");
        if (filePath != null) {
 			res.append(filePath + "\t");
        } else {
           	res.append("\t");
        }

        res.append("(" + boundingBox.toString() + "\t");

        return res.toString();
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}