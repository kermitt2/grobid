package org.grobid.core.utilities;

public class OffsetPosition {
    public int start = -1;
    public int end = -1;

    public OffsetPosition() {
    }

    public OffsetPosition(int start, int end) {
        this.start = start;
        this.end = end;
    }

	public boolean overlaps(OffsetPosition pos) {
		return !((end <= pos.start) || (start >= pos.end)) ;
	}

    public String toString() {
        return "" + start + "\t" + end;
    }
}