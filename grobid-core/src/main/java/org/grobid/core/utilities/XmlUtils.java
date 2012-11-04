package org.grobid.core.utilities;

public class XmlUtils {

	public static final String TAG_START_LOW = "<";
	public static final String TAG_START_SUP = ">";
	public static final String TAG_START_SLASH_SUP = "/>";
	public static final String TAG_END_LOW_SLASH = "</";
	public static final String TAG_END_SUP = ">";

	/**
	 * Build a start tag.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @return requested tag.
	 */
	public static String startTag(String pTagName) {
		StringBuilder tag = new StringBuilder();
		tag.append(TAG_START_LOW);
		tag.append(pTagName);
		tag.append(TAG_START_SUP);
		return tag.toString();
	}

	/**
	 * Build a tag <code><tagName/></code>.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @return requested tag.
	 */
	public static String startEndTag(String pTagName) {
		StringBuilder tag = new StringBuilder();
		tag.append(TAG_START_LOW);
		tag.append(pTagName);
		tag.append(TAG_START_SLASH_SUP);
		return tag.toString();
	}

	/**
	 * Build a end tag.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @return requested tag.
	 */
	public static String endTag(String pTagName) {
		StringBuilder tag = new StringBuilder();
		tag.append(TAG_END_LOW_SLASH);
		tag.append(pTagName);
		tag.append(TAG_END_SUP);
		return tag.toString();
	}

	/**
	 * Build a full tag.
	 * 
	 * @param pTagName
	 *            name of the tag.
	 * @param pValue
	 *            value inside the tag.
	 * @return requested tag.
	 */
	public static String fullTag(String pTagName, String pValue) {
		StringBuilder tag = new StringBuilder();
		tag.append(startTag(pTagName));
		tag.append(pValue);
		tag.append(endTag(pTagName));
		return tag.toString();
	}

}
