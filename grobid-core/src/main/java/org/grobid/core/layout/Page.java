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
}	