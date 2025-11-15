package org.grobid.core.engines.config;

/**
 * Exception for invalid configs
 */
public class InvalidGrobidAnalysisConfig extends RuntimeException {
    public InvalidGrobidAnalysisConfig(String message) {
        super(message);
    }
}
