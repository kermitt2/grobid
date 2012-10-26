package org.grobid.service.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 
 * Class containing the key, value and type of a grobid Propety.
 * 
 * @author Damien
 * 
 */
public class GrobidProperty {

	/**
	 * Possible types for grobid properties.
	 */
	public static enum TYPE {
		STRING, BOOLEAN, INTEGER, FILE, PASSWORD, UNKNOWN
	}

	/**
	 * The key of the property.
	 */
	String key;

	/**
	 * The value of the property.
	 */
	String value;

	/**
	 * The type of the property
	 */
	TYPE type;

	/**
	 * Constructor.
	 * 
	 * @param pKey
	 *            the key
	 * @param pValue
	 *            the value
	 * @param pType
	 *            the type
	 */
	public GrobidProperty(String pKey, String pValue, TYPE pType) {
		key = pKey;
		value = pValue;
		type = pType;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param pKey
	 *            the key to set
	 */
	public void setKey(String pKey) {
		key = StringUtils.isBlank(pKey) ? StringUtils.EMPTY : pKey;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param pValue
	 *            the value to set
	 */
	public void setValue(String pValue) {
		value = StringUtils.isBlank(pValue) ? StringUtils.EMPTY : pValue;
	}

	/**
	 * @return the type
	 */
	public TYPE getType() {
		return type;
	}

	/**
	 * @param pType
	 *            the type to set
	 */
	public void setType(TYPE pType) {
		this.type = pType;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder();
		hcb.append(key);
		return hcb.toHashCode();
	}

}
