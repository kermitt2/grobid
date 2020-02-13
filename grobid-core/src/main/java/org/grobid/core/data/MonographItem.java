package org.grobid.core.data;

import org.grobid.core.layout.Block;

public class MonographItem {
    private Block block;
    private String label;
    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
