package org.grobid.core.utilities;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lang.SentenceDetectorFactory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for using sentence segmentation (singleton).
 *
 */
public class SentenceUtilities {
    public static final Logger LOGGER = LoggerFactory.getLogger(SentenceUtilities.class);

    private static volatile SentenceUtilities instance = null;

    private SentenceDetectorFactory sdf = null;

    public static SentenceUtilities getInstance() {
        if (instance == null) {
            synchronized (SentenceUtilities.class) {
                if (instance == null) {
                    LOGGER.debug("synchronized getNewInstance");
                    instance = new SentenceUtilities();
                }
            }
        }
        return instance;
    }

    private SentenceUtilities() {
        String className = GrobidProperties.getSentenceDetectorFactory();
        try {
            sdf = (SentenceDetectorFactory) Class.forName(className).newInstance();
        } catch (ClassCastException e) {
            throw new GrobidException("Class " + className
                    + " must implement "
                    + SentenceDetectorFactory.class.getName(), e);
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

    /**
     * Basic run for sentence identification, return the offset positions of the 
     * identified sentences
     *
     * @param text
     *            text to segment into sentences
     * @return list of offset positions for the identified sentence, relative to the input text
     */
    public List<OffsetPosition> runSentenceDetection(String text) {
        try {
            return sdf.getInstance().detect(text);
        } catch (Exception e) {
            LOGGER.warn("Cannot detect sentences. ", e);
            return null;
        }
    }

}