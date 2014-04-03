package org.grobid.core.document;

/**
 * User: zholudev
 * Date: 4/1/14
 */
public class DocumentPiece implements Comparable<DocumentPiece>{
    //for easier access make them final, but public
    public final DocumentPointer a;
    public final DocumentPointer b;

    public DocumentPiece(DocumentPointer a, DocumentPointer b) {
        if (a.compareTo(b) > 0) {
            throw new IllegalArgumentException("Invalid document piece: " + a + "-" + b);
        }
        this.a = a;
        this.b = b;
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
