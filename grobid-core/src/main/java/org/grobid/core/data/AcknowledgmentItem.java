package org.grobid.core.data;

import org.grobid.core.utilities.TextUtilities;

/**
 * A class for saving and exchancing information regarding acknowledgment item consisting of the text,
 * the type label (affiliation, educational institution, individual, etc)
 * and the bounding box coordinates.
 *
 *  Created by Tanti, 2019
 */


public class AcknowledgmentItem {
    String label;
    String text;
    String coords;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCoords() {
        return coords;
    }

    public void setCoords(String coords) {
        this.coords = coords;
    }

    // result of acknowledgment string processing
    public String toTEI(){
        StringBuilder tei = new StringBuilder();
        if (label== null) {
            return null;
        } else {

            tei.append("<").append(label).append(">").
                append(TextUtilities.HTMLEncode(text)).
                append("</").append(label).append(">");
        }
        return tei.toString();
    }


}
