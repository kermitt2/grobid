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
	public static final String TAG_IDNO = "idno";
	public static final String TAG_DIV = "div";

	/*------------------*/
	/* ATTRIBUTES NAMES */
	/*------------------*/
	public static final String ATTR_TYPE = "type";

	/*-------------------*/
	/* ATTRIBUTES VALUES */
	/*-------------------*/
	public static final String VAL_PUBLICATION = "publication";
	public static final String VAL_EXCHANGE_DOC = "exchange-document";
	public static final String VAL_COUNTRY = "country";
	public static final String VAL_DOC_NUMBER = "doc-number";
	public static final String VAL_STANDOFF_ANOTATION = "standoff-annotation";
	public static final String VAL_CLAIMS = "claims";
	public static final String VAL_DESCRIPTION= "description";

}
