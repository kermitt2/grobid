package org.grobid.core.utilities;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.engines.label.TaggingLabels;

public class LabelUtils {
    /**
     * Post-process text labeled by the fulltext model on chunks that are known to be text (no table, or figure)
     * It converts table and figure labels to paragraph labels.
     */
    public static String postProcessFullTextLabeledText(String fulltextLabeledText) {
        if (fulltextLabeledText == null)
            return null;
        StringBuilder result = new StringBuilder();

        String[] lines = fulltextLabeledText.split("\n");
        String previousLabel = null;
        for(int i=0; i<lines.length; i++) {
            String line = lines[i];
            if (line == null || line.trim().length() == 0)
                continue;
            String[] pieces = line.split("\t");
            String label = pieces[pieces.length-1];
            if (label.equals("I-"+ TaggingLabels.FIGURE.getLabel()) || label.equals("I-"+TaggingLabels.TABLE.getLabel())) {
                if (previousLabel == null || !previousLabel.endsWith(TaggingLabels.PARAGRAPH.getLabel())) {
                    pieces[pieces.length-1] = "I-"+TaggingLabels.PARAGRAPH.getLabel();
                } else {
                    pieces[pieces.length-1] = TaggingLabels.PARAGRAPH.getLabel();
                }
            } else if (label.equals(TaggingLabels.FIGURE.getLabel()) || label.equals(TaggingLabels.TABLE.getLabel())) {
                pieces[pieces.length-1] = TaggingLabels.PARAGRAPH.getLabel();
            }
            for(int j=0; j<pieces.length; j++) {
                if (j != 0)
                    result.append("\t");
                result.append(pieces[j]);
            }
            previousLabel = label;
            result.append("\n");
        }

        return result.toString();
    }

    public static String adjustInvalidSequenceOfStartLabels(String fulltextLabeledText) {
        if (fulltextLabeledText == null)
            return null;
        StringBuilder result = new StringBuilder();

        String[] lines = fulltextLabeledText.split("\n");
        String previousLabel = null;
        for(int i=0; i<lines.length; i++) {
            String line = lines[i];
            if (StringUtils.isBlank(line))
                continue;
            String[] pieces = line.split("\t");
            String label = pieces[pieces.length-1];
            if (label.equals("I-"+TaggingLabels.FIGURE.getLabel())) {
                if (previousLabel == null) {
                    continue;
                } else if (previousLabel.equals("I-"+TaggingLabels.FIGURE.getLabel())) {
                    pieces[pieces.length-1] = TaggingLabels.FIGURE.getLabel();
                }
            } else if (label.equals("I-"+TaggingLabels.TABLE.getLabel())) {
                if (previousLabel == null) {
                    continue;
                } else if (previousLabel.equals("I-"+TaggingLabels.TABLE.getLabel())) {
                    pieces[pieces.length-1] = TaggingLabels.TABLE.getLabel();
                }
            }

            for(int j=0; j<pieces.length; j++) {
                if (j != 0) {
                    result.append("\t");
                }
                result.append(pieces[j]);
            }
            previousLabel = label;
            result.append("\n");
        }

        return result.toString();
    }
}
