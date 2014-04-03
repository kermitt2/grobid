package org.grobid.core.engines.tagging;


import org.grobid.core.utilities.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * User: zholudev
 * Date: 4/2/14
 */
public class GenericTaggerUtils {

    public static final String START_ENTITY_LABEL_PREFIX = "I-";

    /**
     * @param labeledResult labeled result from a tagger
     * @return a list of pairs - first element in a pair is a token itself, the second is a label (e.g. <footnote> or I-<footnote>)
     * Note an empty line in the result will be transformed to a 'null' pointer of a pair
     */
    public static List<Pair<String, String>> getTokensAndLabels(String labeledResult) {
        String[] lines = labeledResult.split("\n");
        List<Pair<String, String>> res = new ArrayList<>(lines.length);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                res.add(null);
                continue;
            }
            String[] splits = line.split("\t| ");
            res.add(new Pair<>(splits[0], splits[splits.length - 1]));
        }
        return res;
    }

    // I-<citation> --> <citation>
    // <citation> --> <citation>
    public static String getPlainLabel(String label) {
        return label.startsWith(START_ENTITY_LABEL_PREFIX) ? label.substring(2) : label;
    }

    public static boolean isBeginningOfEntity(String label) {
        return label.startsWith(START_ENTITY_LABEL_PREFIX);
    }
}
