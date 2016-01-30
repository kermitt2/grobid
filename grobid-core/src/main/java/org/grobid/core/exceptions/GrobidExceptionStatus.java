package org.grobid.core.exceptions;

/**
 * User: zholudev
 * Date: 1/19/15
 */
public enum GrobidExceptionStatus {
    BAD_INPUT_DATA,
    TAGGING_ERROR,
    PARSING_ERROR,
    TIMEOUT,
    TOO_MANY_BLOCKS,
    NO_BLOCKS,
    PDF2XML_CONVERSION_FAILURE,
    TOO_MANY_TOKENS,
    GENERAL
}
