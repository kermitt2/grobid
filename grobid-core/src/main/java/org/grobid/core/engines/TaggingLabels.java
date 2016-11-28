package org.grobid.core.engines;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.utilities.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by lfoppiano on 25/11/16.
 */
public class TaggingLabels {

    protected static final ConcurrentMap<Pair<GrobidModel, String>, TaggingLabel> cache = new ConcurrentHashMap<>();

    public static final String CITATION_MARKER_LABEL = "<citation_marker>";
    public static final String TABLE_MARKER_LABEL = "<table_marker>";
    public static final String FIGURE_MARKER_LABEL = "<figure_marker>";

    public final static String PARAGRAPH_LABEL = "<paragraph>";
    public final static String ITEM_LABEL = "<item>";
    public final static String OTHER_LABEL = "<other>";
    public final static String SECTION_LABEL = "<section>";
    public final static String FIGURE_LABEL = "<figure>";
    public final static String TABLE_LABEL = "<table>";
    public final static String EQUATION_LABEL = "<equation>";
    public final static String DESCRIPTION_LABEL = "<figDesc>";
    public final static String HEADER_LABEL = "<figure_head>";
    public final static String TRASH_LABEL = "<trash>";
    public final static String LABEL_LABEL = "<label>";

    public static final TaggingLabel CITATION_MARKER = new TaggingLabelImpl(GrobidModels.FULLTEXT, CITATION_MARKER_LABEL);
    public static final TaggingLabel TABLE_MARKER = new TaggingLabelImpl(GrobidModels.FULLTEXT, TABLE_MARKER_LABEL);
    public static final TaggingLabel FIGURE_MARKER = new TaggingLabelImpl(GrobidModels.FULLTEXT, FIGURE_MARKER_LABEL);
    public static final TaggingLabel PARAGRAPH = new TaggingLabelImpl(GrobidModels.FULLTEXT, PARAGRAPH_LABEL);
    public static final TaggingLabel ITEM = new TaggingLabelImpl(GrobidModels.FULLTEXT, ITEM_LABEL);
    public static final TaggingLabel OTHER = new TaggingLabelImpl(GrobidModels.FULLTEXT, OTHER_LABEL);
    public static final TaggingLabel SECTION = new TaggingLabelImpl(GrobidModels.FULLTEXT, SECTION_LABEL);
    public static final TaggingLabel FIGURE = new TaggingLabelImpl(GrobidModels.FULLTEXT, FIGURE_LABEL);
    public static final TaggingLabel TABLE = new TaggingLabelImpl(GrobidModels.FULLTEXT, TABLE_LABEL);
    public static final TaggingLabel EQUATION = new TaggingLabelImpl(GrobidModels.FULLTEXT, EQUATION_LABEL);

    public static final TaggingLabel FIG_DESC = new TaggingLabelImpl(GrobidModels.FIGURE, DESCRIPTION_LABEL);
    public static final TaggingLabel FIG_HEAD = new TaggingLabelImpl(GrobidModels.FIGURE, HEADER_LABEL);
    public static final TaggingLabel FIG_TRASH = new TaggingLabelImpl(GrobidModels.FIGURE, TRASH_LABEL);
    public static final TaggingLabel FIG_LABEL = new TaggingLabelImpl(GrobidModels.FIGURE, LABEL_LABEL);
    public static final TaggingLabel FIG_OTHER = new TaggingLabelImpl(GrobidModels.FIGURE, OTHER_LABEL);

    public static final TaggingLabel TBL_DESC = new TaggingLabelImpl(GrobidModels.TABLE, DESCRIPTION_LABEL);
    public static final TaggingLabel TBL_HEAD = new TaggingLabelImpl(GrobidModels.TABLE, HEADER_LABEL);
    public static final TaggingLabel TBL_TRASH = new TaggingLabelImpl(GrobidModels.TABLE, TRASH_LABEL);
    public static final TaggingLabel TBL_LABEL = new TaggingLabelImpl(GrobidModels.TABLE, LABEL_LABEL);
    public static final TaggingLabel TBL_OTHER = new TaggingLabelImpl(GrobidModels.TABLE, OTHER_LABEL);

    protected static void register(TaggingLabel label) {
        cache.putIfAbsent(new Pair<>(label.getGrobidModel(), label.getLabel()), label);
    }

    static {
        //fulltext
        register(CITATION_MARKER);
        register(TABLE_MARKER);
        register(FIGURE_MARKER);
        register(PARAGRAPH);
        register(ITEM);
        register(OTHER);
        register(SECTION);
        register(FIGURE);
        register(TABLE);
        register(EQUATION);

        //figures
        register(FIG_DESC);
        register(FIG_HEAD);
        register(FIG_TRASH);
        register(FIG_LABEL);
        register(FIG_OTHER);

        // table
        register(TBL_DESC);
        register(TBL_HEAD);
        register(TBL_TRASH);
        register(TBL_LABEL);
        register(TBL_OTHER);
    }


    protected TaggingLabels() {
    }

    public static TaggingLabel labelFor(final GrobidModel model, final String label) {
        final String plainLabel = GenericTaggerUtils.getPlainLabel(label);

        cache.putIfAbsent(new Pair<>(model, plainLabel.toString(/*null-check*/)),
                new TaggingLabelImpl(model, plainLabel));

        return cache.get(new Pair(model, plainLabel));
    }
}
