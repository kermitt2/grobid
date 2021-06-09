package org.grobid.core.document;

public class DocumentPiece implements Comparable<DocumentPiece>{
    //for easier access make them final, but public
    private final DocumentPointer a;
    private final DocumentPointer b;

    public DocumentPiece(DocumentPointer a, DocumentPointer b) {
        if (a.compareTo(b) > 0) {
            throw new IllegalArgumentException("Invalid document piece: " + a + "-" + b);
        }
        this.a = a;
        this.b = b;
    }

    public DocumentPointer getLeft() {
        return a;
    }

    public DocumentPointer getRight() {
        return b;
    }

    @Override
    public String toString() {
        return "(" + a + " - " + b + ")";
    }

    @Override
    public int compareTo(DocumentPiece o) {
        return a.compareTo(o.a);
    }
}
