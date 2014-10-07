package org.grobid.core.lang.impl;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.lang.LanguageDetector;
import org.grobid.core.lang.LanguageDetectorFactory;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * User: zholudev
 * Date: 11/24/11
 * Time: 11:10 AM
 */
public class CybozuLanguageDetectorFactory implements LanguageDetectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CybozuLanguageDetectorFactory.class);
    private static LanguageDetector instance = null;

    private static void init() {
        File profilePath = new File(GrobidProperties.getLanguageDetectionResourcePath(), "cybozu/profiles").getAbsoluteFile();
        if (!profilePath.exists() || !profilePath.isDirectory()) {
            throw new IllegalStateException("Profiles path for cybozu language detection does not exist or not a directory: " + profilePath);
        }

        try {
            DetectorFactory.loadProfile(profilePath);
        } catch (LangDetectException e) {
            throw new IllegalStateException("Cannot read profiles for cybozu language detection from: " + profilePath, e);
        }
    }

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
        init();
        LOGGER.debug("synchronized getNewInstance");
        instance = new CybozuLanguageDetector();
    }
}
