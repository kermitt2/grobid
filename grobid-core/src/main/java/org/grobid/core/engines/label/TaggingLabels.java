package org.grobid.core.engines.label;

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
    public static final String EQUATION_MARKER_LABEL = "<equation_marker>";

    public final static String PARAGRAPH_LABEL = "<paragraph>";
    public final static String ITEM_LABEL = "<item>";
    public final static String OTHER_LABEL = "<other>";
    public final static String SECTION_LABEL = "<section>";
    public final static String FIGURE_LABEL = "<figure>";
    public final static String TABLE_LABEL = "<table>";
    public final static String EQUATION_LAB = "<equation>";
    public final static String EQUATION_ID_LABEL = "<equation_label>";
    public final static String DESCRIPTION_LABEL = "<figDesc>";
    public final static String HEADER_LABEL = "<figure_head>";
    public final static String TRASH_LABEL = "<trash>";
    public final static String LABEL_LABEL = "<label>";
    public final static String DATE_LABEL = "<date>";
    public final static String DATE_YEAR_LABEL = "<year>";
    public final static String DATE_MONTH_LABEL = "<month>";
    public final static String DATE_DAY_LABEL = "<day>";

    public final static String TITLE_LABEL = "<title>";
    public final static String ABSTRACT_LABEL = "<abstract>";
    public final static String AUTHOR_LABEL = "<author>";
    public final static String TECH_LABEL = "<tech>";
    public final static String LOCATION_LABEL = "<location>";
    public final static String DATESUB_LABEL = "<date-submission>";
    public final static String PAGE_LABEL = "<page>";
    public final static String EDITOR_LABEL = "<editor>";
    public final static String INSTITUTION_LABEL = "<institution>";
    public final static String NOTE_LABEL = "note";
    public final static String REFERENCE_LABEL = "<reference>";
    public final static String GRANT_LABEL = "<grant>";
    public final static String COPYRIGHT_LABEL = "<copyright>";
    public final static String AFFILIATION_LABEL = "<affiliation>";
    public final static String ADDRESS_LABEL = "<address>";
    public final static String EMAIL_LABEL = "<email>";
    public final static String PUBMUN_LABEL = "<pubnum>";
    public final static String KEYWORD_LABEL = "<keyword>";
    public final static String PHONE_LABEL = "<phone>";
    public final static String DEGREE_LABEL = "<degree>";
    public final static String WEB_LABEL = "<web>";
    public final static String DEDICATION_LABEL = "<dedication>";
    public final static String SUBMISSION_LABEL = "<submission>";
    public final static String ENTITLE_LABEL = "<entitle>";
    public final static String INTRO_LABEL = "<intro>";

    public static final TaggingLabel CITATION_MARKER = new TaggingLabelImpl(GrobidModels.FULLTEXT, CITATION_MARKER_LABEL);
    public static final TaggingLabel TABLE_MARKER = new TaggingLabelImpl(GrobidModels.FULLTEXT, TABLE_MARKER_LABEL);
    public static final TaggingLabel FIGURE_MARKER = new TaggingLabelImpl(GrobidModels.FULLTEXT, FIGURE_MARKER_LABEL);
    public static final TaggingLabel EQUATION_MARKER = new TaggingLabelImpl(GrobidModels.FULLTEXT, EQUATION_MARKER_LABEL);
    public static final TaggingLabel PARAGRAPH = new TaggingLabelImpl(GrobidModels.FULLTEXT, PARAGRAPH_LABEL);
    public static final TaggingLabel ITEM = new TaggingLabelImpl(GrobidModels.FULLTEXT, ITEM_LABEL);
    public static final TaggingLabel OTHER = new TaggingLabelImpl(GrobidModels.FULLTEXT, OTHER_LABEL);
    public static final TaggingLabel SECTION = new TaggingLabelImpl(GrobidModels.FULLTEXT, SECTION_LABEL);
    public static final TaggingLabel FIGURE = new TaggingLabelImpl(GrobidModels.FULLTEXT, FIGURE_LABEL);
    public static final TaggingLabel TABLE = new TaggingLabelImpl(GrobidModels.FULLTEXT, TABLE_LABEL);
    public static final TaggingLabel EQUATION = new TaggingLabelImpl(GrobidModels.FULLTEXT, EQUATION_LAB);
    public static final TaggingLabel EQUATION_LABEL = new TaggingLabelImpl(GrobidModels.FULLTEXT, EQUATION_ID_LABEL);

    public static final TaggingLabel HEADER_DATE = new TaggingLabelImpl(GrobidModels.HEADER, DATE_LABEL);
    public static final TaggingLabel HEADER_TITLE = new TaggingLabelImpl(GrobidModels.HEADER, TITLE_LABEL);
    public static final TaggingLabel HEADER_ABSTRACT = new TaggingLabelImpl(GrobidModels.HEADER, ABSTRACT_LABEL);
    public static final TaggingLabel HEADER_AUTHOR = new TaggingLabelImpl(GrobidModels.HEADER, AUTHOR_LABEL);
    public static final TaggingLabel HEADER_TECH = new TaggingLabelImpl(GrobidModels.HEADER, TECH_LABEL);
    public static final TaggingLabel HEADER_LOCATION = new TaggingLabelImpl(GrobidModels.HEADER, LOCATION_LABEL);
    public static final TaggingLabel HEADER_DATESUB = new TaggingLabelImpl(GrobidModels.HEADER, DATESUB_LABEL);
    public static final TaggingLabel HEADER_PAGE = new TaggingLabelImpl(GrobidModels.HEADER, PAGE_LABEL);
    public static final TaggingLabel HEADER_EDITOR = new TaggingLabelImpl(GrobidModels.HEADER, EDITOR_LABEL);
    public static final TaggingLabel HEADER_INSTITUTION = new TaggingLabelImpl(GrobidModels.HEADER, INSTITUTION_LABEL);
    public static final TaggingLabel HEADER_NOTE = new TaggingLabelImpl(GrobidModels.HEADER, NOTE_LABEL);
    public static final TaggingLabel HEADER_OTHER = new TaggingLabelImpl(GrobidModels.HEADER, OTHER_LABEL);
    public static final TaggingLabel HEADER_REFERENCE = new TaggingLabelImpl(GrobidModels.HEADER, REFERENCE_LABEL);
    public static final TaggingLabel HEADER_GRANT = new TaggingLabelImpl(GrobidModels.HEADER, GRANT_LABEL);
    public static final TaggingLabel HEADER_COPYRIGHT = new TaggingLabelImpl(GrobidModels.HEADER, COPYRIGHT_LABEL);
    public static final TaggingLabel HEADER_AFFILIATION = new TaggingLabelImpl(GrobidModels.HEADER, AFFILIATION_LABEL);
    public static final TaggingLabel HEADER_ADDRESS = new TaggingLabelImpl(GrobidModels.HEADER, ADDRESS_LABEL);
    public static final TaggingLabel HEADER_EMAIL = new TaggingLabelImpl(GrobidModels.HEADER, EMAIL_LABEL);
    public static final TaggingLabel HEADER_PUBNUM = new TaggingLabelImpl(GrobidModels.HEADER, PUBMUN_LABEL);
    public static final TaggingLabel HEADER_KEYWORD = new TaggingLabelImpl(GrobidModels.HEADER, KEYWORD_LABEL);
    public static final TaggingLabel HEADER_PHONE = new TaggingLabelImpl(GrobidModels.HEADER, PHONE_LABEL);
    public static final TaggingLabel HEADER_DEGREE = new TaggingLabelImpl(GrobidModels.HEADER, DEGREE_LABEL);
    public static final TaggingLabel HEADER_WEB = new TaggingLabelImpl(GrobidModels.HEADER, WEB_LABEL);
    public static final TaggingLabel HEADER_DEDICATION = new TaggingLabelImpl(GrobidModels.HEADER, DEDICATION_LABEL);
    public static final TaggingLabel HEADER_SUBMISSION = new TaggingLabelImpl(GrobidModels.HEADER, SUBMISSION_LABEL);
    public static final TaggingLabel HEADER_ENTITLE = new TaggingLabelImpl(GrobidModels.HEADER, ENTITLE_LABEL);
    public static final TaggingLabel HEADER_INTRO = new TaggingLabelImpl(GrobidModels.HEADER, INTRO_LABEL);

    public static final TaggingLabel DATE_YEAR = new TaggingLabelImpl(GrobidModels.DATE, DATE_YEAR_LABEL);
    public static final TaggingLabel DATE_MONTH = new TaggingLabelImpl(GrobidModels.DATE, DATE_MONTH_LABEL);
    public static final TaggingLabel DATE_DAY = new TaggingLabelImpl(GrobidModels.DATE, DATE_DAY_LABEL);

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
        register(EQUATION_MARKER);
        register(PARAGRAPH);
        register(ITEM);
        register(OTHER);
        register(SECTION);
        register(FIGURE);
        register(TABLE);
        register(EQUATION);
        register(EQUATION_LABEL);

        //header
        register(HEADER_DATE);
        register(HEADER_TITLE);
        register(HEADER_ABSTRACT);
        register(HEADER_AUTHOR);
        register(HEADER_LOCATION);
        register(HEADER_DATESUB);
        register(HEADER_EDITOR);
        register(HEADER_INSTITUTION);
        register(HEADER_NOTE);
        register(HEADER_OTHER);
        register(HEADER_REFERENCE);
        register(HEADER_GRANT);
        register(HEADER_COPYRIGHT);
        register(HEADER_AFFILIATION);
        register(HEADER_ADDRESS);
        register(HEADER_EMAIL);
        register(HEADER_PUBNUM);
        register(HEADER_KEYWORD);
        register(HEADER_PHONE);
        register(HEADER_DEGREE);
        register(HEADER_WEB);
        register(HEADER_DEDICATION);
        register(HEADER_SUBMISSION);
        register(HEADER_ENTITLE);
        register(HEADER_INTRO);

        //date
        register(DATE_YEAR);
        register(DATE_MONTH);
        register(DATE_DAY);

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
