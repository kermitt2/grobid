package org.grobid.core.exceptions;

public class GrobidException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -3337770841815682150L;

    public GrobidException() {
        super();
    }

    public GrobidException(String message) {
        super(message);
    }

    public GrobidException(Throwable cause) {
        super(cause);
    }

    public GrobidException(String message, Throwable cause) {
        super(message, cause);
    }
}
