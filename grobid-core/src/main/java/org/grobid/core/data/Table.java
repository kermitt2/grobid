package org.grobid.core.data;

import java.net.URI;
import java.lang.StringBuilder;
import java.util.*;

import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.TextUtilities;

/**
 * Class for representing a table.
 *
 * @author Patrice Lopez
 */
public class Table extends Figure {
		
    public Table() {
    	caption = new StringBuilder();
    	header = new StringBuilder();
    	content = new StringBuilder();
    	label = new StringBuilder();
    }

	@Override
    public String toTEI(int indent, GrobidAnalysisConfig config) {
        StringBuilder theTable = new StringBuilder();
        theTable.append("\n");
       	for(int i=0; i<indent; i++)
			theTable.append("\t");
		theTable.append("<figure type=\"table\"");
		if (id != null) {
			theTable.append(" xml:id=\"tab_" + id + "\"");
		}
		if (config.isGenerateTeiCoordinates())
			theTable.append(" coords=\"" + getCoordinates() + "\"");
		theTable.append(">\n");
		if (header != null) {
	       	for(int i=0; i<indent+1; i++)
				theTable.append("\t");
			theTable.append("<head>").append(cleanString(
				TextUtilities.HTMLEncode(header.toString())))
				.append("</head>\n");
		}
		if (caption != null) {
			for(int i=0; i<indent+1; i++)
				theTable.append("\t");
			theTable.append("<figDesc>").append(cleanString(
				TextUtilities.HTMLEncode(TextUtilities.dehyphenize(caption.toString()))))
				.append("</figDesc>\n");
		}
		if (uri != null) {
	       	for(int i=0; i<indent+1; i++)
				theTable.append("\t");
			theTable.append("<graphic url=\"" + uri + "\" />\n");
		}
		if (content != null) {
	       	for(int i=0; i<indent+1; i++)
				theTable.append("\t");
			theTable.append("<table>").append(cleanString(
				TextUtilities.HTMLEncode(content.toString())))
				.append("</table>\n");
		}
		for(int i=0; i<indent; i++)
			theTable.append("\t");
		theTable.append("</figure>\n");
        return theTable.toString();
    }

    private String cleanString(String input) {
    	return input.replace("\n", " ").replace("  ", " ").trim();
    }
}