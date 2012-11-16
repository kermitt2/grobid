package org.grobid.core.sax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.utilities.TeiValues;
import org.xml.sax.Attributes;

public class TEIParsedInfo implements Cloneable {

	/**
	 * Content of the document.
	 */
	private StringBuffer description;

	/**
	 * If description has to be appended.
	 */
	private boolean appendDescription;

	/**
	 * If the tei publication has already some annotation.
	 */
	private boolean isAlreadyAnnotated;

	/**
	 * The document number of the publication.
	 */
	private String pubDocNumber;

	private static final String[] path2DocNumber = { "TEI", "notesStmt",
			"note", "idno" };

	/**
	 * Contains xml tags.
	 */
	private List<String> xmlPath;

	/**
	 * Contains the attributes of the current tag.
	 */
	private Attributes currTagAttrs;

	/**
	 * Empty constructor.
	 */
	public TEIParsedInfo() {
		xmlPath = new ArrayList<String>();
		description = new StringBuffer();
		appendDescription = false;
		isAlreadyAnnotated = false;
	}

	/**
	 * Check if the current tag is the document number of the publication.
	 * 
	 * @return true if it is the document number, false else.
	 */
	public boolean isCurrTagDocumber() {
		return xmlPath.containsAll(Arrays.asList(path2DocNumber))
				&& isCurrTagAttrsContainingAttribute(TeiValues.ATTR_TYPE,
						TeiValues.VAL_DOC_NUMBER);
	}

	/**
	 * @return the pubDocNumber
	 */
	public String getPubDocNumber() {
		return pubDocNumber;
	}

	/**
	 * @param pubDocNumber
	 *            the pubDocNumber to set
	 */
	public void setPubDocNumber(String pubDocNumber) {
		this.pubDocNumber = pubDocNumber;
	}

	/**
	 * Appends the string representation of a subarray of the char array
	 * argument to this sequence. Characters of the char array str, starting at
	 * index offset, are appended, in order, to the contents of this sequence.
	 * The length of this sequence increases by the value of len.
	 * 
	 * @param str
	 *            - the characters to be appended.
	 * @param offset
	 *            - the index of the first char to append.
	 * @param len
	 *            - the number of chars to append.
	 */
	public void appendDescription(char[] str, int offset, int len) {
		description.append(str, offset, len);
	}

	/**
	 * Add pDescription to description.
	 * 
	 * @param pDescription
	 */
	public void appendDescription(String pDescription) {
		description.append(pDescription);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description.toString();
	}

	/**
	 * @return the isAlreadyAnnotated
	 */
	public boolean isAlreadyAnnotated() {
		return isAlreadyAnnotated;
	}

	/**
	 * @param isAlreadyAnnotated
	 *            the isAlreadyAnnotated to set
	 */
	public void setAlreadyAnnotated(boolean isAlreadyAnnotated) {
		this.isAlreadyAnnotated = isAlreadyAnnotated;
	}

	/**
	 * @return the appendDescription
	 */
	public boolean isAppendDescription() {
		return appendDescription;
	}

	/**
	 * @param appendDescription
	 *            the appendDescription to set
	 */
	public void setAppendDescription(boolean appendDescription) {
		this.appendDescription = appendDescription;
	}

	/**
	 * Checks if the current tag is <div type="description">
	 * 
	 * @return boolean.
	 */
	public boolean isDescriptionTag() {
		return TeiValues.TAG_DIV.equalsIgnoreCase(getLastXmlPath())
				&& isCurrTagAttrsContainingAttribute(TeiValues.ATTR_TYPE,
						TeiValues.VAL_DESCRIPTION)
				&& doesXmlPathContainsTag(TeiValues.TAG_TEI);
	}

	/**
	 * @return the xmlPath
	 */
	public List<String> getXmlPath() {
		return xmlPath;
	}

	/**
	 * Return the last element of the list.
	 * 
	 * @return String
	 */
	public String getLastXmlPath() {
		return xmlPath.get(xmlPath.size() - 1);
	}

	/**
	 * Add pTagName at the end of the list xmlPath.
	 * 
	 * @param pTagName
	 *            - the name of the tag.
	 */
	public void appendTag2XmlPath(String pTagName) {
		xmlPath.add(pTagName);
	}

	/**
	 * Remove the last element of the list xmlPath.
	 */
	public void removeLastXmlPath() {
		xmlPath.remove(xmlPath.size() - 1);
	}

	/**
	 * Check if xmlPath list contains the tag pTag.
	 * 
	 * @param pTag
	 *            the looked for tag.
	 * @return true if contained in the list, false else.
	 */
	public boolean doesXmlPathContainsTag(String pTag) {
		return xmlPath.contains(pTag);
	}

	/**
	 * @param currTagAttrs
	 *            the currTagAttrs to set
	 */
	public void setCurrTagAttrs(Attributes currTagAttrs) {
		this.currTagAttrs = currTagAttrs;
	}

	/**
	 * Check if the current attributes contains an attribute having a name
	 * pAttName and equals to pValue.
	 * 
	 * @param pAttName
	 *            the name looked for.
	 * @param pValue
	 *            the value looked for.
	 * @return boolean
	 */
	public boolean isCurrTagAttrsContainingAttribute(String pAttName,
			String pValue) {
		return StringUtils.equalsIgnoreCase(pValue,
				currTagAttrs.getValue(pAttName));
	}

	@Override
	public TEIParsedInfo clone() {
		return null;
	}

}
