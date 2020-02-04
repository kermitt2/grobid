package org.grobid.service;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.glassfish.jersey.media.multipart.FormDataParam;

import org.grobid.service.process.AltoRestProcessFiles;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.io.InputStream;


/**
 * RESTful service for the GROBID system.
 *
 * @author FloZi, Damien, Patrice
 */

@Timed
@Singleton
@Path(GrobidPaths.PATH_PDF_ALTO)
public class AltoRestService {

    private static final String INPUT = "input";

    @Inject
    public AltoRestService(){

    }

    @Inject
    private AltoRestProcessFiles restProcessFiles;

    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processPDFReferenceAlto(@FormDataParam(INPUT) InputStream inputStream) throws Exception {

       
        return restProcessFiles.processPDFReferenceAlto(inputStream);
    }
}