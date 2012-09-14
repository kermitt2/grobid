package org.grobid.core.utilities;

public class OffsetPosition {
    public int start = -1;
    public int end = -1;

    public String toString() {
        return "" + start + "\t" + end;
    }
}