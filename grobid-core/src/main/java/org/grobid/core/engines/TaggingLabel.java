package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.utilities.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zholudev on 11/01/16.
 * Representing label that can be tagged
 */
public enum TaggingLabel {

    //fulltext
    CITATION_MARKER(GrobidModels.FULLTEXT, "<citation_marker>"),
    TABLE_MARKER(GrobidModels.FULLTEXT, "<table_marker>"),
    FIGURE_MARKER(GrobidModels.FULLTEXT, "<figure_marker>"),
    PARAGRAPH(GrobidModels.FULLTEXT, "<paragraph>"),
    ITEM(GrobidModels.FULLTEXT, "<item>"),
    OTHER(GrobidModels.FULLTEXT, "<other>"),
    SECTION(GrobidModels.FULLTEXT, "<section>"),
    FIGURE(GrobidModels.FULLTEXT, "<figure>"),
    TABLE(GrobidModels.FULLTEXT, "<table>"),
    EQUATION(GrobidModels.FULLTEXT, "<equation>"),
    ;

    private final GrobidModels grobidModel;
    private final String label;

    private static Map<Pair<GrobidModels, String>, TaggingLabel> cache = new HashMap<>();

    static {
        for (TaggingLabel l : values()) {
            cache.put(new Pair<>(l.grobidModel, l.label), l);
        }
    }

    TaggingLabel(GrobidModels grobidModel, String label) {
        this.grobidModel = grobidModel;
        this.label = label;
    }

    public GrobidModels getGrobidModel() {
        return grobidModel;
    }

    public String getLabel() {
        return label;
    }

    public static TaggingLabel getLabel(GrobidModels model, String tag) {
        String plainLabel = GenericTaggerUtils.getPlainLabel(tag);
        TaggingLabel l = cache.get(new Pair<>(model, plainLabel));
        if (l == null) {
            throw new IllegalArgumentException("Label " + plainLabel + " not found for model " + model);
        }
        return l;
    }

}
