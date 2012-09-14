package org.grobid.core.lang;

/**
 * User: zholudev
 * Date: 11/24/11
 * Time: 11:03 AM
 */
public interface LanguageDetector {
    /**
     * Detects a language id that must consist of two letter together with a confidence coefficient.
     * If coefficient cannot be provided for some reason, it should be 1.0
     * @param text text to detect a language from
     * @return a language id together with a confidence coefficient
     */
    public Language detect(String text);
}
