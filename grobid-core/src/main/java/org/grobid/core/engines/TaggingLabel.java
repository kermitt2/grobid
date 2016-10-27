package org.grobid.core.engines;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.utilities.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    //TODO: move them in the underlying modules

    // labels for quantities/measurements
    QUANTITY_VALUE_ATOMIC(GrobidModels.QUANTITIES, "<valueAtomic>"),
    QUANTITY_VALUE_LEAST(GrobidModels.QUANTITIES, "<valueLeast>"),
    QUANTITY_VALUE_MOST(GrobidModels.QUANTITIES, "<valueMost>"),
    QUANTITY_VALUE_LIST(GrobidModels.QUANTITIES, "<valueList>"),
    QUANTITY_UNIT_LEFT(GrobidModels.QUANTITIES, "<unitLeft>"),
    QUANTITY_UNIT_RIGHT(GrobidModels.QUANTITIES, "<unitRight>"),
    QUANTITY_VALUE_BASE(GrobidModels.QUANTITIES, "<valueBase>"),
    QUANTITY_VALUE_RANGE(GrobidModels.QUANTITIES, "<valueRange>"),
    QUANTITY_OTHER(GrobidModels.QUANTITIES, "<other>"),

    // unit of measurements
    UNIT_VALUE_BASE(GrobidModels.UNITS, "<base>"),
    UNIT_VALUE_POW(GrobidModels.UNITS, "<pow>"),
    UNIT_VALUE_PREFIX(GrobidModels.UNITS, "<prefix>"),
    UNIT_VALUE_OTHER(GrobidModels.UNITS, "<other>"),

    // labels for astronomical entity recognition
    ASTRO_OBJECT(GrobidModels.ASTRO, "<object>"),
    ASTRO_OTHER(GrobidModels.ASTRO, "<other>"),

    DICTIONARY_LEXICAL_ENTRIES_ENTRY(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, "<entry>"),
    DICTIONARY_LEXICAL_ENTRIES_ETYM(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, "<etym>"),
    DICTIONARY_LEXICAL_ENTRIES_METAMARK(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, "<metamark>"),
    DICTIONARY_LEXICAL_ENTRIES_FORM(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, "<form>"),
    DICTIONARY_LEXICAL_ENTRIES_RE(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, "<re>"),
    DICTIONARY_LEXICAL_ENTRIES_NOTE(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, "<note>"),
    DICTIONARY_LEXICAL_ENTRIES_SENSE(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, "<sense>");



    private final GrobidModel grobidModel;
    private final String label;

    private static final ConcurrentMap<Pair<GrobidModel, String>, TaggingLabel> cache = new ConcurrentHashMap<>();

    static {
        for (TaggingLabel l : values()) {
            cache.put(new Pair<>(l.grobidModel, l.label), l);
        }
    }

    TaggingLabel(GrobidModel grobidModel, String label) {
        this.grobidModel = grobidModel;
        this.label = label;
    }

    public GrobidModel getGrobidModel() {
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

    /*public static TaggingLabel labelFor(final GrobidModel model, final String label) {
        if (cache.isEmpty()) {
            for (TaggingLabels l : values()) {
                cache.putIfAbsent(new Pair<GrobidModel, String>(l.getGrobidModel(), l.getLabel()), l);
            }
        }

        final String plainLabel = GenericTaggerUtils.getPlainLabel(label);

        cache.putIfAbsent(new Pair<GrobidModel, String>(model, plainLabel.toString(*//* null-check *//*)),
                new TaggingLabel() {
                    @Override
                    public String getLabel() {
                        return plainLabel;
                    }

                    @Override
                    public GrobidModel getGrobidModel() {
                        return model;
                    }
                }
        );

        return cache.get(new Pair(model, plainLabel));
    }*/

}
