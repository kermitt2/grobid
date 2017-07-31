package org.grobid.service.exceptions.mapper;

import com.google.inject.Inject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Inject
    public WebApplicationExceptionMapper() {
    }

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response.Status exceptionStatus = Response.Status.fromStatusCode(exception.getResponse().getStatus());
        if (exceptionStatus != null) {
            return Response.status(exceptionStatus).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
