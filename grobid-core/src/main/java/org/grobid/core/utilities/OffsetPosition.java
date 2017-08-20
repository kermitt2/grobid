package org.grobid.core.utilities;

public class OffsetPosition implements Comparable<OffsetPosition> {
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

    @Override
    public int compareTo(OffsetPosition pos) {
        if (pos.start < start)
            return 1;
        else if (pos.start == start) {
            if (pos.end < end)
                return 1;
            else if (pos.end == end)
                return 0;
            else 
                return -1;
        } else 
            return -1;
    }
}