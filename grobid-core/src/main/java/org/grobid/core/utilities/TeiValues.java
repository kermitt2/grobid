/**
 * 
 */
package org.grobid.core.utilities;

/**
 * @author Damien
 * 
 */
public interface TeiValues {

	/*-----------*/
	/* TAG NAMES */
	/*-----------*/
	public static final String TAG_TEI = "TEI";
	public static final String TAG_TEI_HEADER = "teiHeader";
	public static final String TAG_FILE_DESC = "fileDesc";
	public static final String TAG_NOTES_STMT = "notesStmt";
	public static final String TAG_NOTE = "note";
	public static final String TAG_LIST = "list";
	public static final String TAG_IDNO = "idno";
	public static final String TAG_DIV = "div";
	public static final String TAG_P = "p";
	public static final String TAG_ITEM = "item";
	public static final String TAG_DATE = "date";
	public static final String TAG_AUTHOR = "author";
	public static final String TAG_APP_INFO = "appInfo";
	public static final String TAG_APPLICATION = "application";
	public static final String TAG_LABEL = "label";
	public static final String TAG_PTR = "ptr";
	public static final String TAG_CERTAINTY = "certainty";

	/*------------------*/
	/* ATTRIBUTES NAMES */
	/*------------------*/
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_SUB_TYPE = "subtype";
	public static final String ATTR_ID = "id";
	public static final String ATTR_WHEN = "when";
	public static final String ATTR_VERSION = "version";
	public static final String ATTR_IDENT = "ident";
	public static final String ATTR_TARGET = "target";
	public static final String ATTR_CERT = "cert";

	/*-------------------*/
	/* ATTRIBUTES VALUES */
	/*-------------------*/
	public static final String VAL_PUBLICATION = "publication";
	public static final String VAL_EXCHANGE_DOC = "exchange-document";
	public static final String VAL_COUNTRY = "country";
	public static final String VAL_DOC_NUMBER = "doc-number";
	public static final String VAL_STANDOFF_ANOTATION = "standoff-annotation";
	public static final String VAL_AUTOMATIC_ANOTATION = "automatic-annotation";
	public static final String VAL_CLAIMS = "claims";
	public static final String VAL_DESCRIPTION = "description";
	public static final String VAL_SOFTWARE_APPLICATION = "softwareApplication";
	public static final String VAL_STRING_RANGE = "string-range";
	public static final String VAL_LOW = "low";
	public static final String VAL_MEDIUM = "medium";
	public static final String VAL_HIGH = "high";

	/*--------*/
	/* OTHERS */
	/*--------*/
	public static final String GROBID = "GROBID";
	public static final String GROBID_LABEL = "GROBID Annotator";
	public static final String VERS_1_0 = "1.0";
	public static final String DEGREE_OF_CONFIDENCE=" degree of confidence";
	public static final String CERTAINTY_VERY_LOW_LABEL = "very low";
	public static final String CERTAINTY_LOW_LABEL = "low";
	public static final String CERTAINTY_MEDIUM_LABEL = "medium";
	public static final String CERTAINTY_HIGH_LABEL = "high";
	public static final String CERTAINTY_VERY_HIGH_LABEL = "very high";
	public static final String NO_CERTAINTY = "no";
	public static final String XML = "xml";
	public static final String W3C_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

}
