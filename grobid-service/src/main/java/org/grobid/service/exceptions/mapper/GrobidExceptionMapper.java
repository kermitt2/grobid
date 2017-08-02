package org.grobid.service.exceptions.mapper;

import com.google.inject.Inject;
import org.grobid.core.exceptions.GrobidException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GrobidExceptionMapper implements ExceptionMapper<GrobidException> {

    @Context
    protected HttpHeaders headers;

    @Context
    private UriInfo uriInfo;

    @Inject
    private GrobidExceptionsTranslationUtility mapper;


    @Inject
    public GrobidExceptionMapper() {

    }

    @Override
    public Response toResponse(GrobidException exception) {
        return mapper.processException(exception, GrobidStatusToHttpStatusMapper.getStatusCode(exception.getStatus()));
    }
}
