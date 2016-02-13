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

    //figures
    FIG_DESC(GrobidModels.FIGURE, "<figDesc>"),
    FIG_HEAD(GrobidModels.FIGURE, "<figure_head>"),
    FIG_TRASH(GrobidModels.FIGURE, "<trash>"),
    FIG_LABEL(GrobidModels.FIGURE, "<label>"),
    FIG_OTHER(GrobidModels.FIGURE, "<other>"),


    // table
    TBL_DESC(GrobidModels.TABLE, "<figDesc>"),
    TBL_HEAD(GrobidModels.TABLE, "<figure_head>"),
    TBL_TRASH(GrobidModels.TABLE, "<trash>"),
    TBL_LABEL(GrobidModels.TABLE, "<label>"),
    TBL_OTHER(GrobidModels.TABLE, "<other>"),

    // labels for quantities/measurements
    QUANTITY_VALUE_ATOMIC(GrobidModels.QUANTITIES, "<valueAtomic>"),
    QUANTITY_VALUE_LEAST(GrobidModels.QUANTITIES, "<valueLeast>"),
    QUANTITY_VALUE_MOST(GrobidModels.QUANTITIES, "<valueMost>"),
    QUANTITY_VALUE_LIST(GrobidModels.QUANTITIES, "<valueList>"),
    QUANTITY_UNIT_LEFT(GrobidModels.QUANTITIES, "<unitLeft>"),
    QUANTITY_UNIT_RIGHT(GrobidModels.QUANTITIES, "<unitRight>"),
    QUANTITY_SUBSTANCE(GrobidModels.QUANTITIES, "<substance>"),
    QUANTITY_OTHER(GrobidModels.QUANTITIES, "<other>");

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
