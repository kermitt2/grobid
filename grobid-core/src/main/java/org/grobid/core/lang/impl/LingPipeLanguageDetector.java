package org.grobid.core.lang.impl;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classifier;
import com.aliasi.util.AbstractExternalizable;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lang.Language;
import org.grobid.core.lang.LanguageDetector;
import org.grobid.core.utilities.GrobidProperties;

import java.io.File;
import java.util.StringTokenizer;

/**
 * User: zholudev
 * Date: 11/24/11
 * Time: 11:05 AM
 */
public class LingPipeLanguageDetector implements LanguageDetector {

    // classifier for language identification
    public Classifier<String, Classification> classifierLanguageId = null;

    @SuppressWarnings({"unchecked"})
    LingPipeLanguageDetector() {
        String modelPath = GrobidProperties.getInstance().getLexiconPath() + "/languageID/langid.classifier";
        try {
            classifierLanguageId = (Classifier<String, Classification>) AbstractExternalizable.readObject(new File(modelPath));
        } catch (Exception e) {
            throw new GrobidException("Cannot read object '" + modelPath + "'.", e);
        }

    }

    public Language detect(String text) {
        Classification classification = classifierLanguageId.classify(text);
        String res = classification.toString();
        StringTokenizer st = new StringTokenizer(res, "\n");
        if (st.hasMoreTokens()) {
            st.nextToken();  // we skip first line
            String top = st.nextToken(); // top result

            StringTokenizer st2 = new StringTokenizer(top, " ");
            if (st2.hasMoreTokens()) {
                String cat = st2.nextToken();
                String lang = cat.substring(2, cat.length());
                st2.nextToken(); // skip the score
                String confidence = st2.nextToken(); // confidence score
                return new Language(lang, Double.parseDouble(confidence));
            }
        }
        return null;
    }
}
