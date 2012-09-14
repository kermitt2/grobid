package org.grobid.core.data;

import java.util.*;

/**
 * Class for representing a classification.
 *
 * @author Patrice Lopez
 */
public class Classification {
    private String classificationScheme = null;

    private List<String> classes = null;
    private String rawString = null;

    public String getClassificationScheme() {
        return classificationScheme;
    }

    public void setClassificationScheme(String s) {
        classificationScheme = s;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> c) {
        classes = c;
    }

    public String getRawString() {
        return rawString;
    }

    public void setRawString(String s) {
        rawString = s;
    }
}
