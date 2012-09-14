package org.grobid.core.exceptions;

public class GrobidResourceException extends GrobidException {

    /**
     *
     */
    private static final long serialVersionUID = -3337770841815682150L;

    public GrobidResourceException() {
        super();
    }

    public GrobidResourceException(String message) {
        super(message);
    }

    public GrobidResourceException(Throwable cause) {
        super(cause);
    }

    public GrobidResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
