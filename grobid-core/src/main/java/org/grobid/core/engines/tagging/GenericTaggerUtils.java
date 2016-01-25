package org.grobid.core.engines.tagging;


import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * User: zholudev
 * Date: 4/2/14
 */
public class GenericTaggerUtils {

    public static final String START_ENTITY_LABEL_PREFIX = "I-";
    public static final Pattern SEPARATOR_PATTERN = Pattern.compile("\t| ");

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

    // <token, label, feature_string>
    public static List<Triple<String, String, String>> getTokensWithLabelsAndFeatures(String labeledResult,
                                                                                      boolean addFeatureString) {
        String[] lines = labeledResult.split("\n");
        List<Triple<String, String, String>> res = new ArrayList<>(lines.length);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                res.add(null);
                continue;
            }
            List<String> splitList = Lists.newArrayList(Splitter.on(SEPARATOR_PATTERN).split(line));
//            String[] splits = line.split("\t| ");
            String featureString = addFeatureString ? Joiner.on("\t").join(splitList.subList(0, splitList.size() - 1)) : null;
            res.add(new Triple<>(
                    splitList.get(0),
                    splitList.get(splitList.size() - 1),
                    featureString));
        }
        return res;
    }

    // I-<citation> --> <citation>
    // <citation> --> <citation>
    public static String getPlainLabel(String label) {
        return label == null ? null : label.startsWith(START_ENTITY_LABEL_PREFIX) ? label.substring(2) : label;
    }

    public static boolean isBeginningOfEntity(String label) {
        return label.startsWith(START_ENTITY_LABEL_PREFIX);
    }
}
