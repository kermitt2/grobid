package org.grobid.service.process;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import java.util.List;
import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.grobid.core.annotations.TeiStAXParser;
import org.grobid.core.engines.Engine;
import org.grobid.core.data.PatentItem;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.factory.GrobidPoolingFactory;
import org.grobid.service.parser.Xml2HtmlParser;
import org.grobid.service.util.GrobidRestUtils;
import org.grobid.service.util.GrobidServiceProperties;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Damien, Patrice
 */
public class GrobidRestProcessFiles {

    /**
     * The class Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GrobidRestProcessFiles.class);

    /**
     * Uploads the origin document which shall be extracted into TEI and
     * extracts only the header data.
     *
     * @param inputStream the data of origin document
     * @param consolidate consolidation parameter for the header extraction
     * @param htmlFormat  if the result has to be formatted to be displayed as html
     * @return a response object which contains a TEI representation of the
     *         header part
     */
    public static Response processStatelessHeaderDocument(final InputStream inputStream,
                                                          final boolean consolidate,
                                                          final boolean htmlFormat) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal = null;
        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;
        try {
            originFile = GrobidRestUtils.writeInputFile(inputStream);

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process
                engine = GrobidRestUtils.getEngine(isparallelExec);
                if (isparallelExec) {
                    retVal = engine.processHeader(originFile.getAbsolutePath(), consolidate, null);
                    //retVal = engine.segmentAndProcessHeader(originFile.getAbsolutePath(), consolidate, null);
					GrobidPoolingFactory.returnEngine(engine);
					engine = null;
                } else {
                    synchronized (engine) {
                        retVal = engine.processHeader(originFile.getAbsolutePath(), consolidate, null);
                        //retVal = engine.segmentAndProcessHeader(originFile.getAbsolutePath(), consolidate, null);
                    }
                }

                if ((retVal == null) || (retVal.isEmpty())) {
                    response = Response.status(Status.NO_CONTENT).build();
                } else {
                    if (htmlFormat) {
                        response = Response.status(Status.OK).entity(formatAsHTML(retVal)).type(MediaType.APPLICATION_XML).build();
                    } else {
                        response = Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
                    }
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occured: " + exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            GrobidRestUtils.removeTempFile(originFile);
            if (isparallelExec && engine != null) {
                GrobidPoolingFactory.returnEngine(engine);
            }
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * Return the tei formatted to be displayed in html.
     *
     * @param tei the xml input.
     * @return html formatted String.
     * @throws SAXException
     * @throws IOException
     */
    protected static String formatAsHTML(final String tei) throws SAXException, IOException {
        XMLReader xmlr = XMLReaderFactory.createXMLReader();
        Xml2HtmlParser parser = new Xml2HtmlParser();
        xmlr.setContentHandler(parser);
        xmlr.setErrorHandler(parser);
        InputStream xmlStream = new ByteArrayInputStream(tei.getBytes("UTF-8"));
        xmlr.parse(new InputSource(xmlStream));
        return parser.getHTML();
    }

    /**
     * Uploads the zip file, extract pdf files and extract them into TEI. Only
     * the header data is extracted.
     *
     * @param inputStream zip containing the datas of origin document.
     * @return Response containing the TEI files representing the header part.
     */
    public static Response processStatelessBulkHeaderDocument(final InputStream inputStream) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        LOGGER.debug(methodLogIn());
        try {
            File originFile = GrobidRestUtils.writeInputFile(inputStream);
            LOGGER.info("originFile=" + originFile);
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        LOGGER.debug(methodLogOut());

        return response;
    }

    /**
     * Uploads the origin document which shall be extracted into TEI.
     *
     * @param inputStream the data of origin document
     * @param consolidate the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving header information		
     * @param htmlFormat  if the result has to be formatted to be displayed as html.
   	 * @param startPage give the starting page to consider in case of segmentation of the 
   	 * PDF, -1 for the first page (default) 
   	 * @param endPage give the end page to consider in case of segmentation of the 
   	 * PDF, -1 for the last page (default)
	 * @param generateIDs if true, generate random attribute id on the textual elements of 
	 * the resulting TEI 		
     * @return a response object mainly contain the TEI representation of the
     *         full text
     */
    public static Response processStatelessFulltextDocument(final InputStream inputStream,
                                                            final boolean consolidate,
                                                            final boolean htmlFormat,
															final int startPage,
															final int endPage, 
															final boolean generateIDs) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal = null;
        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;
        try {
            originFile = GrobidRestUtils.writeInputFile(inputStream);

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process
				engine = GrobidRestUtils.getEngine(isparallelExec);
                if (isparallelExec) {
                    retVal = engine.fullTextToTEI(originFile.getAbsolutePath(), 
						consolidate, false, null, startPage, endPage, generateIDs);
                    GrobidPoolingFactory.returnEngine(engine);
					engine = null;
                } else {
                    synchronized (engine) {
                        retVal = engine.fullTextToTEI(originFile.getAbsolutePath(), 
							consolidate, false, null, startPage, endPage, generateIDs);
                    }
                }

                GrobidRestUtils.removeTempFile(originFile);

                if (!GrobidRestUtils.isResultOK(retVal)) {
                    response = Response.status(Status.NO_CONTENT).build();
                } else {
                    if (htmlFormat) {
                        response = Response.status(Status.OK).entity(formatAsHTML(retVal)).
							type(MediaType.APPLICATION_XML).build();
                    } else {
                        response = Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
                    }
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            GrobidRestUtils.removeTempFile(originFile);
            if (isparallelExec && (engine != null)) {
                GrobidPoolingFactory.returnEngine(engine);
            }
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * Process a patent document encoded in TEI for extracting and parsing citations in the description body.
     *
     * @param pInputStream The input stream to process.
     * @return StreamingOutput wrapping the response in streaming while parsing
     *         the input.
     */
    public static StreamingOutput processCitationPatentTEI(final InputStream pInputStream,
                                                           final boolean consolidate) {
        LOGGER.debug(methodLogIn());
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    final TeiStAXParser parser = new TeiStAXParser(pInputStream, output, false, consolidate);
                    parser.parse();
                } catch (Exception exp) {
                    throw new WebApplicationException(exp);
                }
            }
        };
    }

    /**
     * Process a patent document in PDF for extracting and parsing citations in the description body.
     *
     * @param inputStream the data of origin document
     * @return a response object mainly containing the TEI representation of the
     *         citation
     */
    public static Response processCitationPatentPDF(final InputStream inputStream,
                                                    final boolean consolidate) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal = null;
        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;
        try {
            originFile = GrobidRestUtils.writeInputFile(inputStream);

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process
                engine = GrobidRestUtils.getEngine(isparallelExec);
                List<PatentItem> patents = new ArrayList<PatentItem>();
                List<BibDataSet> articles = new ArrayList<BibDataSet>();
                if (isparallelExec) {
                    retVal = engine.processAllCitationsInPDFPatent(originFile.getAbsolutePath(),
                            articles, patents, consolidate);
                    GrobidPoolingFactory.returnEngine(engine);
					engine = null;
                } else {
                    synchronized (engine) {
                        retVal = engine.processAllCitationsInPDFPatent(originFile.getAbsolutePath(),
                                articles, patents, consolidate);
                    }
                }

                GrobidRestUtils.removeTempFile(originFile);

                if (!GrobidRestUtils.isResultOK(retVal)) {
                    response = Response.status(Status.NO_CONTENT).build();
                } else {
                    response = Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            GrobidRestUtils.removeTempFile(originFile);
            if (isparallelExec && engine != null) {
                GrobidPoolingFactory.returnEngine(engine);
            }
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * Process a patent document encoded in ST.36 for extracting and parsing citations in the description body.
     *
     * @param inputStream the data of origin document
     * @return a response object mainly containing the TEI representation of the
     *         citation
     */
    public static Response processCitationPatentST36(final InputStream inputStream,
                                                     final boolean consolidate) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal = null;
        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;
        try {
            originFile = GrobidRestUtils.writeInputFile(inputStream);

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process
                engine = GrobidRestUtils.getEngine(isparallelExec);
                List<PatentItem> patents = new ArrayList<PatentItem>();
                List<BibDataSet> articles = new ArrayList<BibDataSet>();
                if (isparallelExec) {
                    retVal = engine.processAllCitationsInXMLPatent(originFile.getAbsolutePath(),
                            articles, patents, consolidate);
                    GrobidPoolingFactory.returnEngine(engine);
					engine = null;
                } else {
                    synchronized (engine) {
                        retVal = engine.processAllCitationsInXMLPatent(originFile.getAbsolutePath(),
                                articles, patents, consolidate);
                    }
                }

                GrobidRestUtils.removeTempFile(originFile);

                if (!GrobidRestUtils.isResultOK(retVal)) {
                    response = Response.status(Status.NO_CONTENT).build();
                } else {
                    response = Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            GrobidRestUtils.removeTempFile(originFile);
            if (isparallelExec && engine != null) {
                GrobidPoolingFactory.returnEngine(engine);
            }
        }
        LOGGER.debug(methodLogOut());
        return response;
    }


    /**
     * Uploads the origin document, extract and parser all its references.
     *
     * @param inputStream the data of origin document
     * @param consolidate if the result has to be consolidated with CrossRef access.
     * @return a response object mainly contain the TEI representation of the
     *         full text
     */
    public static Response processStatelessReferencesDocument(final InputStream inputStream,
                                                              final boolean consolidate) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal = null;
        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;
        try {
            originFile = GrobidRestUtils.writeInputFile(inputStream);

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process
				engine = GrobidRestUtils.getEngine(isparallelExec);
                List<BibDataSet> results = null;
                if (isparallelExec) {
                    results = engine.processReferences(originFile.getAbsolutePath(), consolidate);
                    GrobidPoolingFactory.returnEngine(engine);
					engine = null;
                } else {
                    synchronized (engine) {
                        results = engine.processReferences(originFile.getAbsolutePath(), consolidate);
                    }
                }

                GrobidRestUtils.removeTempFile(originFile);

                StringBuffer result = new StringBuffer();
                // dummy header
                result.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" " +
                        "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                        "\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
                result.append("\t<teiHeader/>\n\t<text/>\n\t<front/>\n\t<body/>\n\t<back>\n\t\t<listBibl>\n");
                for (BibDataSet res : results) {
                    result.append(res.toTEI());
                    result.append("\n");
                }
                result.append("\t\t</listBibl>\n\t</back>\n</TEI>\n");

                retVal = result.toString();

                if (!GrobidRestUtils.isResultOK(retVal)) {
                    response = Response.status(Status.NO_CONTENT).build();
                } else {
                    /*if (htmlFormat) {
						response = Response.status(Status.OK).entity(formatAsHTML(retVal)).type(MediaType.APPLICATION_XML).build();
					} else {*/
                    response = Response.status(Status.OK).entity(retVal).type(MediaType.APPLICATION_XML).build();
                    //}
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            GrobidRestUtils.removeTempFile(originFile);
            if (isparallelExec && engine != null) {
                GrobidPoolingFactory.returnEngine(engine);
            }
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * @return
     */
    public static String methodLogIn() {
        return ">> " + GrobidRestProcessFiles.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    /**
     * @return
     */
    public static String methodLogOut() {
        return "<< " + GrobidRestProcessFiles.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

}
