package org.grobid.service.exceptions;

import javax.ws.rs.core.Response;

public class GrobidServicePropertyException extends GrobidServiceException {

    private static final long serialVersionUID = -756080338090769910L;

    public GrobidServicePropertyException() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }

    public GrobidServicePropertyException(String msg) {
        super(msg, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public GrobidServicePropertyException(String msg, Throwable cause) {
        super(msg, cause, Response.Status.INTERNAL_SERVER_ERROR);
    }
}
