package org.grobid.core.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing an annotation present in a PDF source file. Annotations are area in the PDF document 
 * associated with an action (URI for external web link, goto for internal document link).
 *
 * @author Patrice Lopez
 */
public class PDFAnnotation {
    private String destination = null;
    private List<BoundingBox> boundingBoxes = null;

    // start position of the block in the original tokenization, if known 
    private int startToken = -1;
    // end position of the block in the original tokenization, if known 
    private int endToken = -1;

    // the page in the document where the annotation is located
	// warning: in PDF, the page numbers start at 1 
    private int pageNumber = -1;

    public enum Type {UNKNOWN, GOTO, URI};
    private Type type = Type.UNKNOWN; // default

    public PDFAnnotation() {
    }

    public void setType(Type t) {
        type = t;
    }

    public Type getType() {
        return type;
    }

    public List<BoundingBox> getBoundingBoxes() {
        return boundingBoxes;
    }

    public void setBoundingBoxes(List<BoundingBox> boxes) {
        boundingBoxes = boxes;
    }

	public void addBoundingBox(BoundingBox box) {
        if (boundingBoxes == null) {
        	boundingBoxes = new ArrayList<BoundingBox>();
        };
		boundingBoxes.add(box);
    }

    public int getStartToken() {
        return startToken;
    }

    public int getEndToken() {
        return endToken;
    }

    public void setStartToken(int start) {
        startToken = start;
    }

    public void setEndToken(int end) {
        endToken = end;
    }

    public int getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isNull() {
        if ( (boundingBoxes == null) && (startToken == -1) && (endToken == -1) && (type == null) ) {
            return true;
        }
        else 
            return false;
    }

	public String getDestination() {
		return destination;
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}

    @Override
    public String toString() {
        String res = "PDFAnnotation{" +
				", pageNumber=" + pageNumber +
                ", startToken=" + startToken +
                ", endToken=" + endToken +
                ", type=" + type;
        if (boundingBoxes != null)
            res += ", boundingBoxes=" + boundingBoxes.toString() + '}';
        return res;
    }
	
	/**
	  * Return true if the annotation covers the given LayoutToken, based on their
	  * respective coordinates.
	  */
	public boolean cover(LayoutToken token) {
		if (token == null)
			return false;
		boolean res = false;
		// do we have an astro entity annotation at this location?
		// we need to check the coordinates
		int pageToken = token.getPage();
		if (pageToken == pageNumber) {
			BoundingBox tokenBox = BoundingBox.fromLayoutToken(token);
			for(BoundingBox box : boundingBoxes) {
				if (box.intersect(tokenBox)) {
					// bounding boxes are at least touching, but we need to further check if we 
					// have also a significant surface covered 
					if (box.contains(tokenBox)) {
						res = true;
						break;
					}
					double areaToken = tokenBox.area();
					// the bounding box of the insection 
					BoundingBox intersectionBox = box.boundingBoxIntersection(tokenBox);
					if (intersectionBox != null) {
						double intersectionArea = intersectionBox.area();
						if (intersectionArea > (areaToken / 4)) {
							res = true;
							break;
						}
					}
				}
			}
		}
		return res;
	}
}