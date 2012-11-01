package org.grobid.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.grobid.core.utilities.GrobidProperties;

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
			final String currProperty = (String) property;
			gbdProperties.add(new GrobidProperty(currProperty, props
					.getProperty(currProperty)));
		}
		props = GrobidProperties.getProps();
		for (Object property : props.keySet()) {
			final String currProperty = (String) property;
			gbdProperties.add(new GrobidProperty(currProperty, props
					.getProperty(currProperty)));
		}
		return gbdProperties;
	}

	

}
