package org.grobid.service.util;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.core.utilities.XmlUtils;

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
	 */
	public GrobidProperty(String pKey, String pValue) {
		setKey(pKey);
		setValue(pValue);
		type = inferType(pKey, pValue);
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
	
	@Override
	public boolean equals(Object arg0) {
		GrobidProperty prop = (GrobidProperty)arg0;
		EqualsBuilder eqb = new EqualsBuilder();
		eqb.append(key, prop.getKey());
		eqb.append(value, prop.getValue());
		eqb.append(type, prop.getType());
		return eqb.build();
	}
	
	@Override
	public String toString(){
		StringBuilder str = new StringBuilder();
		str.append(XmlUtils.startTag("property"));
		str.append(XmlUtils.fullTag("key", getKey()));
		str.append(XmlUtils.fullTag("value", getValue()));
		str.append(XmlUtils.fullTag("type", getType().toString()));
		str.append(XmlUtils.endTag("property"));
		return str.toString();
	}

	/**
	 * Return whether the type is String, boolean, integer or file.
	 * 
	 * @param pKey
	 *            The key of the parameter.
	 * @param pValue
	 *            The value of the parameter.
	 * @return One type of {@link GrobidProperty.TYPE}, empty string if pValue
	 *         is null or empty.
	 */
	protected static TYPE inferType(String pKey, String pValue) {
		TYPE type = TYPE.UNKNOWN;
		if (GrobidPropertyKeys.PROP_GROBID_SERVICE_ADMIN_PW
				.equalsIgnoreCase(pKey)) {
			type = TYPE.PASSWORD;
		} else if (StringUtils.isNotBlank(pValue)) {
			pValue = pValue.trim();
			if (isBoolean(pValue))
				type = TYPE.BOOLEAN;
			else if (isInteger(pValue))
				type = TYPE.INTEGER;
			else if (isFile(pValue))
				type = TYPE.FILE;
			else
				type = TYPE.STRING;
		}

		return type;
	}

	private static boolean isBoolean(String pValue) {
		return StringUtils.equalsIgnoreCase(pValue, "TRUE")
				|| StringUtils.equalsIgnoreCase(pValue, "FALSE");
	}

	private static boolean isInteger(String pValue) {
		try {
			Integer.parseInt(pValue);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private static boolean isFile(String pValue) {
		File file = new File(pValue);
		return file.exists();
	}

}
