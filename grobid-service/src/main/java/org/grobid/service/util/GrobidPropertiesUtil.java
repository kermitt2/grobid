package org.grobid.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.core.utilities.XmlUtils;

/**
 * Utility methods for managing Grobid properties.
 * 
 * @author Damien
 * 
 */
public class GrobidPropertiesUtil {

	/**
	 * Return a list of {@link GrobidProperty} containing all the properties of
	 * Grobid.
	 * 
	 * @return List<GrobidProperty>
	 */
	public static List<GrobidProperty> getAllPropertiesList() {
		List<GrobidProperty> gbdProperties = new ArrayList<GrobidProperty>();
		Properties props = GrobidServiceProperties.getProps();
		for (Object property : props.keySet()) {
			final String currProperty = (String) property;
			gbdProperties.add(new GrobidProperty(currProperty, props.getProperty(currProperty)));
		}
		props = GrobidProperties.getProps();
		for (Object property : props.keySet()) {
			final String currProperty = (String) property;
			gbdProperties.add(new GrobidProperty(currProperty, props.getProperty(currProperty)));
		}
		return gbdProperties;
	}

	/**
	 * Build an xml representation of all grobid properties.
	 * 
	 * @return String
	 */
	public static String getAllPropertiesListXml() {
		StringBuilder gbdProperties = new StringBuilder();
		gbdProperties.append(XmlUtils.startTag("properties"));
		for (GrobidProperty currProp : getAllPropertiesList()) {
			if (!GrobidPropertyKeys.PROP_GROBID_IS_CONTEXT_SERVER.equals(currProp.getKey())) {
				gbdProperties.append(currProp.toString());
			}
		}
		gbdProperties.append(XmlUtils.endTag("properties"));
		return gbdProperties.toString();
	}

}
