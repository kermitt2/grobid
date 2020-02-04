package org.grobid.service.process;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidPoolingFactory;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.utilities.IOUtilities;
import org.grobid.service.exceptions.GrobidServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.util.NoSuchElementException;

import org.grobid.core.engines.EngineParsers;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.apache.xerces.impl.dv.util.Base64;
import org.grobid.core.document.Document;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Base64.*;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

/**
 * Web services consuming a file
 */
@Singleton
public class AltoRestProcessFiles extends GrobidRestProcessFiles {

    private static final Logger LOGGER = LoggerFactory.getLogger(AltoRestProcessFiles.class);

    @Inject
    public AltoRestProcessFiles() {

    }

    /**
     * Uploads the origin PDF, process it and return PDF annotations for references
     * in JSON.
     *
     * @param inputStream the data of origin PDF
     * @return a response object containing the JSON annotations
     */
    public Response processPDFReferenceAlto(final InputStream inputStream) throws Exception {
        LOGGER.debug(methodLogIn());

        GrobidAnalysisConfig config = new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder().withPreprocessImages(true)
                .withProcessVectorGraphics(false).build();

        Response response = null;
        File originFile, xmlFile = null;
        Engine engine = null;
        final EngineParsers parsers = new EngineParsers();
        try {
            engine = Engine.getEngine(true);
            // conservative check, if no engine is free in the pool a NoSuchElementException
            // is normally thrown
            if (engine == null) {
                parsers.close();
                throw new GrobidServiceException("No GROBID engine available", Status.SERVICE_UNAVAILABLE);
            }

            originFile = IOUtilities.writeInputFile(inputStream);
            if (originFile == null) {
                LOGGER.error("The input file cannot be written.");
                parsers.close();
                throw new GrobidServiceException("The input file cannot be written.", Status.INTERNAL_SERVER_ERROR);
            }
            final DocumentSource docSource = DocumentSource.fromPdf(originFile, -1, -1, true, false, false);
            xmlFile = docSource.getXmlFile();
            docSource.setCleanupXml( false);
            originFile.delete();

            // reading text file into stream, try-with-resources
            String content = null;
            try {
                try (InputStream in = new FileInputStream(xmlFile)) {
                    final byte[] bytes = new byte[(int) xmlFile.length()];

                    int offset = 0;
                    while (offset < bytes.length) {
                        final int result = in.read(bytes, offset, bytes.length - offset);
                        if (result == -1) {
                            break;
                        }
                        offset += result;
                    }
                    content = new String(bytes, StandardCharsets.UTF_8);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            

            // real pdf document and concat all images no svg to end file with struct
            // <all><content>alto content <content><images><image name="name"
            // value="base64"/></images></all>
            String base64imagesxml = parsers.getSegmentationParser().processing(docSource, config).getImages().stream()
                    .filter(image -> !image.getFilePath().toString().endsWith(".svg")).map(image -> {
                        try 
                        {
                            LOGGER.debug(" image  string"+ image.getFilePath());
                            LOGGER.debug(" image  "+ get(image.getFilePath()));
                            File tempFile = new File(image.getFilePath());
                            LOGGER.debug(" image Exists "+ tempFile.exists());
                            
                            return "<image name=''" + image.getFilePath().toString() + " value=" + java.util.Base64
                                    .getEncoder().encodeToString(readAllBytes(get(image.getFilePath()))) + "'/>";
                        } catch (IOException e) {
                            LOGGER.error("An unexpected exception occurs to read image  "+ image.getFilePath().toString()+"-"+e);
                            return " error image "+image.getFilePath().toString();                        }
                    })
            .reduce("", String::concat);
            docSource.setCleanupXml( true);
            DocumentSource.close(docSource, true, true, true);
            parsers.close();
            

            //if(base64imagesxml.length()!=0){
            content = "<all><content>"+ content +"</content><images>"+ base64imagesxml +"<images><all>";
            //}
            if (content != null) {
                response = Response.status(Status.OK).entity(content)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML + "; charset=UTF-8").build();
            }
            /*} else {
                response = Response.status(Status.NO_CONTENT).build();
            }*/

        } catch (final NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (final Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            if (xmlFile != null)
                IOUtilities.removeTempFile(xmlFile);

            if (engine != null) {
                GrobidPoolingFactory.returnEngine(engine);
            }
        }
        LOGGER.debug(methodLogOut());
        return response;
    }
}