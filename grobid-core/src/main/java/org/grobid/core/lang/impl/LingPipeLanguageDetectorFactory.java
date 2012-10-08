package org.grobid.core.lang.impl;

import org.grobid.core.lang.LanguageDetector;
import org.grobid.core.lang.LanguageDetectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: zholudev
 * Date: 11/24/11
 * Time: 11:10 AM
 */
public class LingPipeLanguageDetectorFactory implements LanguageDetectorFactory{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(LingPipeLanguageDetectorFactory.class);
    private static LanguageDetector instance = null;

    public LanguageDetector getInstance() {
    	// synchronized (this.getClass()) {
            if (instance == null) {
                getNewInstance();
            }
        // }
        return instance;
    }

    /**
     * return new instance.
     */
	private synchronized void getNewInstance() {
		LOGGER.debug("synchronized getNewInstance");
		instance = new LingPipeLanguageDetector();
	}
}
