package org.grobid.service.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.grobid.service.util.GrobidProperty.*;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.utilities.GrobidPropertyKeys;

/**
 * Utility methods for managing Grobid properties.
 * 
 * @author Damien
 * 
 */
public class GrobidPropertiesUtil {

	public static List<GrobidProperty> getAllPropertiesList() {
		List<GrobidProperty> gbdProperties = new ArrayList<GrobidProperty>();
		Properties props = GrobidServiceProperties.getProps();
		for (Object property : props.keySet()) {
			final String propVal = (String) property;
			gbdProperties.add(new GrobidProperty(propVal, props
					.getProperty(propVal), getType(propVal,
					props.getProperty(propVal))));
		}
		return gbdProperties;
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
	protected static TYPE getType(String pKey, String pValue) {
		TYPE type = TYPE.UNKNOWN;
		if (GrobidPropertyKeys.PROP_GROBID_SERVICE_ADMIN_PW
				.equalsIgnoreCase(pKey)) {
			type = TYPE.PASSWORD;
		}
		if (StringUtils.isNotBlank(pValue)) {
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
