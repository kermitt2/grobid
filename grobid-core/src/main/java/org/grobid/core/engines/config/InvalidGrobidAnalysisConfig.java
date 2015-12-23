package org.grobid.core.engines.config;

/**
 * Created by zholudev on 25/08/15.

 * Exception for invalid configs
 */
public class InvalidGrobidAnalysisConfig extends RuntimeException {
    public InvalidGrobidAnalysisConfig(String message) {
        super(message);
    }
}
