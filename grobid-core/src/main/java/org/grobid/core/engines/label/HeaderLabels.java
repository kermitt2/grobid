package org.grobid.core.engines.label;

import org.grobid.core.GrobidModels;

public class HeaderLabels extends TaggingLabels{
    /**
     */

    public final static String ABSTRACT_LABEL = "<abstract>";
    public final static String DATESUB_LABEL = "<date-submission>";
    public final static String PAGE_LABEL = "<page>";
    public final static String EDITOR_LABEL = "<editor>";
    public final static String FUNDING_LABEL = "<funding>";
    public final static String COPYRIGHT_LABEL = "<copyright>";
    public final static String AFFILIATION_LABEL = "<affiliation>";
    public final static String ADDRESS_LABEL = "<address>";
    public final static String EMAIL_LABEL = "<email>";
    public final static String KEYWORD_LABEL = "<keyword>";
    public final static String PHONE_LABEL = "<phone>";
    public final static String DEGREE_LABEL = "<degree>";
    public final static String SUBMISSION_LABEL = "<submission>";
    public final static String ENTITLE_LABEL = "<entitle>";
    //public final static String INTRO_LABEL = "<intro>";
    public final static String VERSION_LABEL = "<version>";
    public final static String DOCTYPE_LABEL = "<doctype>";
    public final static String DOWNLOAD_LABEL = "<date-download>";
    public final static String WORKINGGROUP_LABEL = "<group>";
    public final static String MEETING_LABEL = "<meeting>";


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
    public static final TaggingLabel HEADER_FUNDING = new TaggingLabelImpl(GrobidModels.HEADER, FUNDING_LABEL);
    public static final TaggingLabel HEADER_COPYRIGHT = new TaggingLabelImpl(GrobidModels.HEADER, COPYRIGHT_LABEL);
    public static final TaggingLabel HEADER_AFFILIATION = new TaggingLabelImpl(GrobidModels.HEADER, AFFILIATION_LABEL);
    public static final TaggingLabel HEADER_ADDRESS = new TaggingLabelImpl(GrobidModels.HEADER, ADDRESS_LABEL);
    public static final TaggingLabel HEADER_EMAIL = new TaggingLabelImpl(GrobidModels.HEADER, EMAIL_LABEL);
    public static final TaggingLabel HEADER_PUBNUM = new TaggingLabelImpl(GrobidModels.HEADER, PUBNUM_LABEL);
    public static final TaggingLabel HEADER_KEYWORD = new TaggingLabelImpl(GrobidModels.HEADER, KEYWORD_LABEL);
    public static final TaggingLabel HEADER_PHONE = new TaggingLabelImpl(GrobidModels.HEADER, PHONE_LABEL);
    public static final TaggingLabel HEADER_DEGREE = new TaggingLabelImpl(GrobidModels.HEADER, DEGREE_LABEL);
    public static final TaggingLabel HEADER_WEB = new TaggingLabelImpl(GrobidModels.HEADER, WEB_LABEL);
    public static final TaggingLabel HEADER_DEDICATION = new TaggingLabelImpl(GrobidModels.HEADER, DEDICATION_LABEL);
    public static final TaggingLabel HEADER_SUBMISSION = new TaggingLabelImpl(GrobidModels.HEADER, SUBMISSION_LABEL);
    public static final TaggingLabel HEADER_ENTITLE = new TaggingLabelImpl(GrobidModels.HEADER, ENTITLE_LABEL);
    //public static final TaggingLabel HEADER_INTRO = new TaggingLabelImpl(GrobidModels.HEADER, INTRO_LABEL);
    public static final TaggingLabel HEADER_COLLABORATION = new TaggingLabelImpl(GrobidModels.HEADER, COLLABORATION_LABEL);
    public static final TaggingLabel HEADER_VERSION = new TaggingLabelImpl(GrobidModels.HEADER, VERSION_LABEL);
    public static final TaggingLabel HEADER_DOCTYPE = new TaggingLabelImpl(GrobidModels.HEADER, DOCTYPE_LABEL);
    public static final TaggingLabel HEADER_DOWNLOAD = new TaggingLabelImpl(GrobidModels.HEADER, DOWNLOAD_LABEL);
    public static final TaggingLabel HEADER_WORKINGGROUP = new TaggingLabelImpl(GrobidModels.HEADER, WORKINGGROUP_LABEL);
    public static final TaggingLabel HEADER_MEETING = new TaggingLabelImpl(GrobidModels.HEADER, MEETING_LABEL);
    public static final TaggingLabel HEADER_PUBLISHER = new TaggingLabelImpl(GrobidModels.HEADER, PUBLISHER_LABEL);
    public static final TaggingLabel HEADER_JOURNAL = new TaggingLabelImpl(GrobidModels.HEADER, JOURNAL_LABEL);

    public static final TaggingLabel HEADER_DATA_AVAILABILITY = new TaggingLabelImpl(GrobidModels.HEADER, DATA_AVAILABILITY_LABEL);



    HeaderLabels() {
        super();
    }


    static {
        register(HEADER_DATE);
        register(HEADER_TITLE);
        register(HEADER_ABSTRACT);
        register(HEADER_AUTHOR);
        //register(HEADER_LOCATION);
        //register(HEADER_DATESUB);
        register(HEADER_EDITOR);
        //register(HEADER_INSTITUTION);
        register(HEADER_NOTE);
        register(HEADER_OTHER);
        register(HEADER_REFERENCE);
        register(HEADER_FUNDING);
        register(HEADER_COPYRIGHT);
        register(HEADER_AFFILIATION);
        register(HEADER_ADDRESS);
        register(HEADER_EMAIL);
        register(HEADER_PUBNUM);
        register(HEADER_KEYWORD);
        register(HEADER_PHONE);
        //register(HEADER_DEGREE);
        register(HEADER_WEB);
        //register(HEADER_DEDICATION);
        register(HEADER_SUBMISSION);
        //register(HEADER_ENTITLE);
        //register(HEADER_INTRO);
        //register(HEADER_COLLABORATION);
        //register(HEADER_VERSION);
        register(HEADER_DOCTYPE);
        //register(HEADER_DOWNLOAD);
        register(HEADER_WORKINGGROUP);
        register(HEADER_MEETING);
        register(HEADER_PUBLISHER);
        register(HEADER_JOURNAL);
        register(HEADER_PAGE);
    }

}
