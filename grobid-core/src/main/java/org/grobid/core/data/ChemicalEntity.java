package org.grobid.core.data;

import org.grobid.core.utilities.OffsetPosition;

/**
 * Class for managing chemical entities.
 *
 * @author Patrice Lopez
 */
public class ChemicalEntity {
    // attribute
    String rawName = null;
    String inchi = null;
    String smiles = null;

    OffsetPosition offsets = null;

    public ChemicalEntity() {
        offsets = new OffsetPosition();
    }

    public ChemicalEntity(String raw) {
        offsets = new OffsetPosition();
        this.rawName = raw;
    }

    public String getRawName() {
        return rawName;
    }

    public String getInchi() {
        return inchi;
    }

    public String getSmiles() {
        return smiles;
    }

    public void setRawName(String raw) {
        this.rawName = raw;
    }

    public void setInchi(String inchi) {
        this.inchi = inchi;
    }

    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

    public void setOffsetStart(int start) {
        offsets.start = start;
    }

    public int getOffsetStart() {
        return offsets.start;
    }

    public void setOffsetEnd(int end) {
        offsets.end = end;
    }

    public int getOffsetEnd() {
        return offsets.end;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(rawName + "\t" + inchi + "\t" + smiles + "\t" + offsets.toString());
        return buffer.toString();
    }

	// TODO: CML encoding
}
