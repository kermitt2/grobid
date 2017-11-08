package org.grobid.service.exceptions.mapper;

import com.google.inject.Inject;
import org.grobid.service.exceptions.GrobidServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GrobidServiceExceptionMapper implements ExceptionMapper<GrobidServiceException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapper.class);

    @Context
    protected HttpHeaders headers;

    @Context
    private UriInfo uriInfo;

    @Inject
    private GrobidExceptionsTranslationUtility mapper;

    @Inject
    public GrobidServiceExceptionMapper() {

    }

    @Override
    public Response toResponse(GrobidServiceException exception) {
        return mapper.processException(exception, exception.getResponseCode());
    }
}
