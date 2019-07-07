package org.grobid.service.exceptions;

import org.grobid.core.exceptions.GrobidException;
import javax.ws.rs.core.Response;

public class GrobidServiceException extends GrobidException {

    private static final long serialVersionUID = -756089338090769910L;
    private Response.Status responseCode;

    public GrobidServiceException(Response.Status responseCode) {
        super();
        this.responseCode = responseCode;
    }

    public GrobidServiceException(String msg, Response.Status responseCode) {
        super(msg);
        this.responseCode = responseCode;
    }

    public GrobidServiceException(String msg, Throwable cause, Response.Status responseCode) {
        super(msg, cause);
        this.responseCode = responseCode;
    }

    public Response.Status getResponseCode() {
        return responseCode;
    }
}
