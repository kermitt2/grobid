package org.grobid.core.utilities;

import org.grobid.core.exceptions.GrobidException;

import org.grobid.core.lang.Language;
import org.grobid.core.lang.LanguageDetectorFactory;

/**
 * Class for using language guessers (singleton).
 *
 * @author Patrice Lopez
 * @author Vyacheslav Zholudev
 */
public class LanguageUtilities {
    private static LanguageUtilities instance = null;

    private boolean useLanguageId = false;
    private LanguageDetectorFactory ldf = null;

    public static synchronized LanguageUtilities getInstance() {
        if (instance == null) {
            instance = new LanguageUtilities();
        }
        return instance;
    }

    /**
     * Hidden constructor
     */
    @SuppressWarnings({"unchecked"})
    private LanguageUtilities() {
        useLanguageId = GrobidProperties.getInstance().isUseLanguageId();
        if (useLanguageId) {
            String className = GrobidProperties.getInstance().getLanguageDetectorFactory();
            try {
                ldf = (LanguageDetectorFactory) Class.forName(className).newInstance();
            } catch (ClassCastException e) {
                throw new GrobidException("Class " + className + " must implement " + LanguageDetectorFactory.class.getName());
            } catch (ClassNotFoundException e) {
                throw new GrobidException("Class " + className + " were not found in the classpath. " +
                        "Make sure that it is provided correctly is in the classpath");
            } catch (InstantiationException e) {
                throw new GrobidException("Class " + className + " should have a default constructor");
            } catch (IllegalAccessException e) {
                throw new GrobidException(e);
            }
        }
    }

    /**
     * Basic run for language identification, return the language code and confidence
     * score separated by a semicolon
     * @param text text to classify
     * @return language ids concatenated with ;
     */
    public Language runLanguageId(String text) {
        if (!useLanguageId) {
            return null;
        }
        return ldf.getInstance().detect(text);
   }

}