package org.grobid.core.exceptions;

public class GrobidPropertyException extends GrobidException {

    /**
     *
     */
    private static final long serialVersionUID = -3337770841815682150L;

    public GrobidPropertyException() {
        super();
    }

    public GrobidPropertyException(String message) {
        super(message);
    }

    public GrobidPropertyException(Throwable cause) {
        super(cause);
    }

    public GrobidPropertyException(String message, Throwable cause) {
        super(message, cause);
    }
}
