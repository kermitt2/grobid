package org.grobid.service.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Damien
 * 
 */
public class GrobidRestUtils {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidRestUtils.class);

	// type of PDF annotation for visualization purposes
	public enum Annotation { CITATION, BLOCK, FIGURE};

	/**
	 * Check whether the result is null or empty.
	 */
	public static boolean isResultOK(String result) {
		return StringUtils.isBlank(result) ? false : true;
	}

}
