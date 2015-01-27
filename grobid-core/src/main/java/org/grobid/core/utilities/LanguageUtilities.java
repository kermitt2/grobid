package org.grobid.core.utilities;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lang.Language;
import org.grobid.core.lang.LanguageDetectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for using language guessers (singleton).
 * 
 * @author Patrice Lopez
 * @author Vyacheslav Zholudev
 */
public class LanguageUtilities {
	public static final Logger LOGGER = LoggerFactory
			.getLogger(LanguageUtilities.class);

	private static LanguageUtilities instance = null;

	private boolean useLanguageId = false;
	private LanguageDetectorFactory ldf = null;

	public static/* synchronized */LanguageUtilities getInstance() {
		if (instance == null) {
			getNewInstance();
		}
		return instance;
	}

	/**
	 * Return a new instance.
	 */
	protected static synchronized void getNewInstance() {
		// GrobidProperties.getInstance();
		LOGGER.debug("synchronized getNewInstance");
		instance = new LanguageUtilities();
	}

	/**
	 * Hidden constructor
	 */
	private LanguageUtilities() {
		useLanguageId = GrobidProperties.isUseLanguageId();
		if (useLanguageId) {
			String className = GrobidProperties.getLanguageDetectorFactory();
			try {
				ldf = (LanguageDetectorFactory) Class.forName(className)
						.newInstance();
			} catch (ClassCastException e) {
				throw new GrobidException("Class " + className
						+ " must implement "
						+ LanguageDetectorFactory.class.getName(), e);
			} catch (ClassNotFoundException e) {
				throw new GrobidException(
						"Class "
								+ className
								+ " were not found in the classpath. "
								+ "Make sure that it is provided correctly is in the classpath", e);
			} catch (InstantiationException e) {
				throw new GrobidException("Class " + className
						+ " should have a default constructor", e);
			} catch (IllegalAccessException e) {
				throw new GrobidException(e);
			}
		}
	}

	/**
	 * Basic run for language identification, return the language code and
	 * confidence score separated by a semicolon
	 * 
	 * @param text
	 *            text to classify
	 * @return language ids concatenated with ;
	 */
	public Language runLanguageId(String text) {
		if (!useLanguageId) {
			return null;
		}
        try {
            return ldf.getInstance().detect(text);
        } catch (Exception e) {
            LOGGER.warn("Cannot detect language because of: " + e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

}