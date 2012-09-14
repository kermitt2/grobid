package org.grobid.core.lang.impl;

import org.grobid.core.lang.LanguageDetector;
import org.grobid.core.lang.LanguageDetectorFactory;

/**
 * User: zholudev
 * Date: 11/24/11
 * Time: 11:10 AM
 */
public class LingPipeLanguageDetectorFactory implements LanguageDetectorFactory{
    private static LanguageDetector instance = null;

    public LanguageDetector getInstance() {
        synchronized (this.getClass()) {
            if (instance == null) {
                instance = new LingPipeLanguageDetector();
            }
        }
        return instance;
    }
}
