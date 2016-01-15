package org.grobid.core.layout;

import java.util.*;

/**
 * Class for representing a page.
 *
 * @author Patrice Lopez
 */
public class Page {
    private List<Block> blocks = null;
    public double width = 0.0;
    public double height = 0.0;
    public int number = -1;
    public int pageLengthChar = 0;

    public Page(int nb) {
        number = nb;
    }

    public void addBlock(Block b) {
        if (blocks == null)
            blocks = new ArrayList<Block>();
        blocks.add(b);
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setHeight(double d) {
        height = Math.abs(d);
    }

    public double getHeight() {
        return height;
    }

    public void setWidth(double d) {
        width = Math.abs(d);
    }

    public double getWidth() {
        return width;
    }

    public void setPageLengthChar(int length) {
        pageLengthChar = length;
    }

    public int getPageLengthChar() {
        return pageLengthChar;
    }
}	