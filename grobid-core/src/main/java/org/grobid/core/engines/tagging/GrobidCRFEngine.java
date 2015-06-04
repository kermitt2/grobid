package org.grobid.core.engines.tagging;

import java.util.Arrays;

/**
 * User: zholudev
 * Date: 3/31/14
 */
public enum GrobidCRFEngine {
    WAPITI("wapiti"),
    CRFPP("crf"),
    AI2("ai2");

    private final String ext;

    GrobidCRFEngine(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    public static GrobidCRFEngine get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name of a Grobid CRF engine must not be null");
        }

        String n = name.toLowerCase();
        for (GrobidCRFEngine e : values()) {
            System.out.println(e.name().toLowerCase());
            if (e.name().toLowerCase().equals(n)) {
                return e;
            }
        }
        throw new IllegalArgumentException("No Grobid CRF engine with name '" + name +
                "', possible values are: " + Arrays.toString(values()));
    }


}
