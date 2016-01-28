package org.grobid.core.exceptions;

public class GrobidException extends RuntimeException {
    private GrobidExceptionStatus status = GrobidExceptionStatus.GENERAL;

    /**
     *
     */
    private static final long serialVersionUID = -3337770841815682150L;


    public GrobidException() {
        super();
    }

    public GrobidException(GrobidExceptionStatus grobidExceptionStatus) {
        super();
        this.status = grobidExceptionStatus;
    }

    public GrobidException(String message) {
        super(message);
    }

    public GrobidException(String message, GrobidExceptionStatus grobidExceptionStatus) {
        super(message);
        this.status = grobidExceptionStatus;
    }

    public GrobidException(Throwable cause, GrobidExceptionStatus grobidExceptionStatus) {
        super(cause);
        this.status = grobidExceptionStatus;
    }

    public GrobidException(Throwable cause) {
        super(cause);
    }

    public GrobidException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrobidException(String message, Throwable cause, GrobidExceptionStatus grobidExceptionStatus) {
        super(message, cause);
        this.status = grobidExceptionStatus;
    }

    @Override
    public String getMessage() {
        return status != null ? "[" + status + "] " + super.getMessage() : super.getMessage();
    }

    public GrobidExceptionStatus getStatus() {
        return status;
    }
}
