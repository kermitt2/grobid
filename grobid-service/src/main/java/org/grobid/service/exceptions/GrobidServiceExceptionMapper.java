package org.grobid.service.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Provider
public class GrobidServiceExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionMapper.class);

    @Context
    protected HttpHeaders headers;

    @Context
    private UriInfo uriInfo;


    @Inject
    public GrobidServiceExceptionMapper() {
    }
    
    protected Status getStatus(Throwable exception) {
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        if (exception instanceof GrobidServiceException) {
            status = ((GrobidServiceException) exception).getResponseCode();
        } else if (exception instanceof GrobidException && ((GrobidException) exception).getStatus() != null) {
            status = GrobidStatusToHttpStatusMapper.getStatusCode(((GrobidException) exception).getStatus());
        } else if (exception instanceof WebApplicationException) {
            Response.Status exceptionStatus =
                    Response.Status.fromStatusCode(((WebApplicationException) exception).getResponse().getStatus());
            if (exceptionStatus != null) {
                status = exceptionStatus;
            }
        }
        return status;
    }

    @Override
    public Response toResponse(Throwable exception) {
        Response.Status status = getStatus(exception);

        String exceptionName = exception.getClass().getCanonicalName();
        if (exception.getClass().equals(RuntimeException.class) &&
                exception.getCause() != null) {
            exceptionName = exception.getCause().getClass().getCanonicalName();
        }

        List<String> descriptions = getExceptionDescriptions(exception, status);

        try {
            fillMdc(exception, status);
            if (status.getFamily() == Response.Status.Family.SERVER_ERROR) {
                LOGGER.error("Exception Not Mapped , Server Error", exception);
            } else if (status == Status.NOT_FOUND) {
                LOGGER.info("Exception Mapped {}", descriptions);
            } else {
                LOGGER.info("Exception Mapped {}", descriptions);
            }

            String requestUri = uriInfo.getRequestUri().toString();
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(buildJson(exceptionName, descriptions, status, null, requestUri))
                    .build();
        } finally {
            cleanMdc();
        }
    }

    protected String getExceptionName(Throwable exception) {
        String exceptionName = exception.getClass().getCanonicalName();
        if (exception.getClass().equals(RuntimeException.class) &&
                exception.getCause() != null) {
            exceptionName = exception.getCause().getClass().getCanonicalName();
        }
        return exceptionName;
    }


    protected void fillMdc(Throwable exception, Status status) {
        MDC.put("ExceptionName", getExceptionName(exception));
        MDC.put("StatusCode", String.valueOf(status.getStatusCode()));
        MDC.put("ReasonPhrase", status.getReasonPhrase());
        MDC.put("StatusFamily", status.getFamily().toString());
        MDC.put("StackTrace", Throwables.getStackTraceAsString(exception));
    }

    protected void cleanMdc() {
        MDC.remove("ExceptionName");
        MDC.remove("StringErrorCode");
        MDC.remove("StatusCode");
        MDC.remove("ReasonPhrase");
        MDC.remove("StatusFamily");
        MDC.remove("StackTrace");
    }


    protected List<String> getExceptionDescriptions(Throwable exception, Response.Status status) {
        List<String> descriptions = new ArrayList<>();

        Throwable currentException = exception;
        int maxIterations = 0;
        while (currentException != null) {
            StringBuilder sb = new StringBuilder(50);
            sb.append(currentException.getClass().getName());
            if (currentException.getMessage() != null) {
                sb.append(":").append(currentException.getMessage());
            }
            descriptions.add(sb.toString());
            currentException = currentException.getCause();
            maxIterations++;
            if (maxIterations > 4) {
                break;
            }
        }
        return descriptions;
    }


    public String buildJson(String type, List<String> descriptions, Status status, GrobidExceptionStatus grobidExceptionStatus, String requestUri) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("type", type);
        root.put("description", Joiner.on("\n").join(descriptions));
        root.put("code", status.getStatusCode());
        root.put("requestUri", requestUri);
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            root.put("correlationId", correlationId);
        }
        if (grobidExceptionStatus != null) {
            root.put("grobidExceptionStatus", grobidExceptionStatus.name());
        }

        String json;
        try {
            json = mapper.writeValueAsString(root);
        } catch (IOException e) {
            LOGGER.warn("Error in ServiceExceptionMapper: ", e);
            json = "{\"description\": \"Internal error: " + e.getMessage() + "\"}";
        }
        return json;
    }


}
