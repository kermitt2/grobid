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

    private static final ConcurrentMap<Pair<GrobidModel, String>, ITaggingLabel> cache = new ConcurrentHashMap<>();

    public final static String CITATION_MARKER_STRING = "<citation_marker>";
    public static final String TABLE_MARKER_STRING = "<table_marker>";
    public static final String FIGURE_MARKER_STRING = "<figure_marker>";

    public final static String PARAGRAPH_STRING = "<paragraph>";
    public final static String ITEM_STRING = "<item>";
    public final static String OTHER_STRING = "<other>";
    public final static String SECTION_STRING = "<section>";
    public final static String FIGURE_STRING = "<figure>";
    public final static String TABLE_STRING = "<table>";
    public final static String EQUATION_STRING = "<equation>";
    public final static String DESCRIPTION_STRING = "<figDesc>";
    public final static String HEADER_STRING = "<figure_head>";
    public final static String TRASH_STRING = "<trash>";
    public final static String LABEL_STRING = "<label>";

    public static final TaggingLabel CITATION_MARKER = new TaggingLabel(GrobidModels.FULLTEXT, CITATION_MARKER_STRING);
    public static final TaggingLabel TABLE_MARKER = new TaggingLabel(GrobidModels.FULLTEXT, TABLE_MARKER_STRING);
    public static final TaggingLabel FIGURE_MARKER = new TaggingLabel(GrobidModels.FULLTEXT, FIGURE_MARKER_STRING);
    public static final TaggingLabel PARAGRAPH = new TaggingLabel(GrobidModels.FULLTEXT, PARAGRAPH_STRING);
    public static final TaggingLabel ITEM = new TaggingLabel(GrobidModels.FULLTEXT, ITEM_STRING);
    public static final TaggingLabel OTHER = new TaggingLabel(GrobidModels.FULLTEXT, OTHER_STRING);
    public static final TaggingLabel SECTION = new TaggingLabel(GrobidModels.FULLTEXT, SECTION_STRING);
    public static final TaggingLabel FIGURE = new TaggingLabel(GrobidModels.FULLTEXT, FIGURE_STRING);
    public static final TaggingLabel TABLE = new TaggingLabel(GrobidModels.FULLTEXT, TABLE_STRING);
    public static final TaggingLabel EQUATION = new TaggingLabel(GrobidModels.FULLTEXT, EQUATION_STRING);

    public static final TaggingLabel FIG_DESC = new TaggingLabel(GrobidModels.FIGURE, DESCRIPTION_STRING);
    public static final TaggingLabel FIG_HEAD = new TaggingLabel(GrobidModels.FIGURE, HEADER_STRING);
    public static final TaggingLabel FIG_TRASH = new TaggingLabel(GrobidModels.FIGURE, TRASH_STRING);
    public static final TaggingLabel FIG_LABEL = new TaggingLabel(GrobidModels.FIGURE, LABEL_STRING);
    public static final TaggingLabel FIG_OTHER = new TaggingLabel(GrobidModels.FIGURE, OTHER_STRING);

    public static final TaggingLabel TBL_DESC = new TaggingLabel(GrobidModels.TABLE, DESCRIPTION_STRING);
    public static final TaggingLabel TBL_HEAD = new TaggingLabel(GrobidModels.TABLE, HEADER_STRING);
    public static final TaggingLabel TBL_TRASH = new TaggingLabel(GrobidModels.TABLE, TRASH_STRING);
    public static final TaggingLabel TBL_LABEL = new TaggingLabel(GrobidModels.TABLE, LABEL_STRING);
    public static final TaggingLabel TBL_OTHER = new TaggingLabel(GrobidModels.TABLE, OTHER_STRING);

    static {
        //fulltext
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, CITATION_MARKER_STRING), CITATION_MARKER);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, TABLE_MARKER_STRING), TABLE_MARKER);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, FIGURE_MARKER_STRING), FIGURE_MARKER);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, PARAGRAPH_STRING), PARAGRAPH);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, ITEM_STRING), ITEM);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, OTHER_STRING), OTHER);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, SECTION_STRING), SECTION);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, FIGURE_STRING), FIGURE);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, TABLE_STRING), TABLE);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FULLTEXT, EQUATION_STRING), EQUATION);

        //figures
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FIGURE, DESCRIPTION_STRING), FIG_DESC);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FIGURE, HEADER_STRING), FIG_HEAD);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FIGURE, TRASH_STRING), FIG_TRASH);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FIGURE, LABEL_STRING), FIG_LABEL);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.FIGURE, OTHER_STRING), FIG_OTHER);

        // table
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.TABLE, DESCRIPTION_STRING), TBL_DESC);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.TABLE, HEADER_STRING), TBL_HEAD);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.TABLE, TRASH_STRING), TBL_TRASH);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.TABLE, LABEL_STRING), TBL_LABEL);
        cache.putIfAbsent(new Pair<GrobidModel, String>(GrobidModels.TABLE, OTHER_STRING), TBL_OTHER);
    }


    private TaggingLabels() {
    }

    public static ITaggingLabel labelFor(final GrobidModel model, final String label) {
        final String plainLabel = GenericTaggerUtils.getPlainLabel(label);

        cache.putIfAbsent(new Pair<>(model, plainLabel.toString(/*null-check*/)),
                new TaggingLabel(model, label));

        return cache.get(new Pair(model, plainLabel));
    }




}
