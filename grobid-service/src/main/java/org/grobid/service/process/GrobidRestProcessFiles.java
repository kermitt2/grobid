package org.grobid.service.process;

import org.grobid.core.annotations.TeiStAXParser;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidPoolingFactory;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.KeyGen;
import org.grobid.service.parser.Xml2HtmlParser;
import org.grobid.service.util.GrobidRestUtils;
import org.grobid.service.util.GrobidServiceProperties;
import org.grobid.core.visualization.CitationsVisualizer;
import org.grobid.core.visualization.BlockVisualizer;
import org.grobid.core.visualization.FigureTableVisualizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
     * header part
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
                    //TODO: sync does not make sense
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
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getCause().getMessage()).build();
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
     *                    web services for improving header information
     * @param htmlFormat  if the result has to be formatted to be displayed as html.
     * @param startPage   give the starting page to consider in case of segmentation of the
     *                    PDF, -1 for the first page (default)
     * @param endPage     give the end page to consider in case of segmentation of the
     *                    PDF, -1 for the last page (default)
     * @param generateIDs if true, generate random attribute id on the textual elements of
     *                    the resulting TEI
     * @return a response object mainly contain the TEI representation of the
     * full text
     */
    public static Response processStatelessFulltextDocument(final InputStream inputStream,
                                                            final boolean consolidate,
                                                            final boolean htmlFormat,
                                                            final int startPage,
                                                            final int endPage,
                                                            final boolean generateIDs) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal;
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
                GrobidAnalysisConfig config =
                        GrobidAnalysisConfig.builder()
                                .consolidateHeader(consolidate)
                                .consolidateCitations(false)
                                .startPage(startPage)
                                .endPage(endPage)
                                .generateTeiIds(generateIDs)
                                .build();

                retVal = engine.fullTextToTEI(originFile,
                        config);

                if (isparallelExec) {
                    GrobidPoolingFactory.returnEngine(engine);
                    engine = null;
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
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getCause().getMessage()).build();
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
     * Uploads the origin document which shall be extracted into TEI + assets in a ZIP
     * archive.
     *
     * @param inputStream the data of origin document
     * @param consolidate the consolidation option allows GROBID to exploit Crossref
     *                    web services for improving header information
     * @param startPage   give the starting page to consider in case of segmentation of the
     *                    PDF, -1 for the first page (default)
     * @param endPage     give the end page to consider in case of segmentation of the
     *                    PDF, -1 for the last page (default)
     * @param generateIDs if true, generate random attribute id on the textual elements of
     *                    the resulting TEI
     * @return a response object mainly contain the TEI representation of the
     * full text
     */
    public static Response processStatelessFulltextAssetDocument(final InputStream inputStream,
                                                                 final boolean consolidate,
                                                                 final int startPage,
                                                                 final int endPage,
                                                                 final boolean generateIDs) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal;
        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;
        String assetPath = null;
        try {
            originFile = GrobidRestUtils.writeInputFile(inputStream);

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // set the path for the asset files
                assetPath = GrobidProperties.getTempPath().getPath() + File.separator + KeyGen.getKey();

                // starts conversion process
                engine = GrobidRestUtils.getEngine(isparallelExec);
                GrobidAnalysisConfig config =
                        GrobidAnalysisConfig.builder()
                                .consolidateHeader(consolidate)
                                .consolidateCitations(false)
                                .startPage(startPage)
                                .endPage(endPage)
                                .generateTeiIds(generateIDs)
                                .pdfAssetPath(new File(assetPath))
                                .build();

                retVal = engine.fullTextToTEI(originFile, config);

                if (isparallelExec) {
                    GrobidPoolingFactory.returnEngine(engine);
                    engine = null;
                }

                GrobidRestUtils.removeTempFile(originFile);

                if (!GrobidRestUtils.isResultOK(retVal)) {
                    response = Response.status(Status.NO_CONTENT).build();
                } else {

                    response = Response.status(Status.OK).type("application/zip").build();

                    ByteArrayOutputStream ouputStream = new ByteArrayOutputStream();
                    ZipOutputStream out = new ZipOutputStream(ouputStream);
                    out.putNextEntry(new ZipEntry("tei.xml"));
                    out.write(retVal.getBytes(Charset.forName("UTF-8")));
                    // put now the assets, i.e. all the files under the asset path
                    File assetPathDir = new File(assetPath);
                    if (assetPathDir.exists()) {
                        File[] files = assetPathDir.listFiles();
                        if (files != null) {
                            byte[] buffer = new byte[1024];
                            for (final File currFile : files) {
                                if (currFile.getName().toLowerCase().endsWith(".jpg")
                                        || currFile.getName().toLowerCase().endsWith(".png")) {
                                    try {
                                        ZipEntry ze = new ZipEntry(currFile.getName());
                                        out.putNextEntry(ze);
                                        FileInputStream in = new FileInputStream(currFile);
                                        int len;
                                        while ((len = in.read(buffer)) > 0) {
                                            out.write(buffer, 0, len);
                                        }
                                        in.close();
                                        out.closeEntry();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    out.finish();

                    response = Response
                            .ok()
                            .type("application/zip")
                            .entity(ouputStream.toByteArray())
                            .header("Content-Disposition", "attachment; filename=\"result.zip\"")
                            .build();
                    out.close();
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getCause().getMessage()).build();
        } finally {
            GrobidRestUtils.removeTempFile(originFile);
            if (assetPath != null) {
                GrobidRestUtils.removeTempDirectory(assetPath);
            }
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
     * the input.
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
     * citation
     */
    public static Response processCitationPatentPDF(final InputStream inputStream,
                                                    final boolean consolidate) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal;
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
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getCause().getMessage()).build();
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
     * citation
     */
    public static Response processCitationPatentST36(final InputStream inputStream,
                                                     final boolean consolidate) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal;
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
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getCause().getMessage()).build();
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
     * full text
     */
    public static Response processStatelessReferencesDocument(final InputStream inputStream,
                                                              final boolean consolidate) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal;
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
                    results = engine.processReferences(originFile, consolidate);
                    GrobidPoolingFactory.returnEngine(engine);
                    engine = null;
                } else {
                    synchronized (engine) {
                        //TODO: VZ: sync on local var does not make sense
                        results = engine.processReferences(originFile, consolidate);
                    }
                }

                GrobidRestUtils.removeTempFile(originFile);

                StringBuilder result = new StringBuilder();
                // dummy header
                result.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" " +
                        "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                        "\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
                result.append("\t<teiHeader/>\n\t<text>\n\t\t<front/>\n\t\t<body/>\n\t\t<back>\n\t\t\t<listBibl>\n");
                int p = 0;
                for (BibDataSet res : results) {
                    result.append(res.toTEI(p));
                    result.append("\n");
                    p++;
                }
                result.append("\t\t\t</listBibl>\n\t\t</back>\n\t</text>\n</TEI>\n");

                retVal = result.toString();

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
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getCause().getMessage()).build();
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
     * Uploads the origin PDF, process it and return the PDF augmented with annotations.
     *
     * @param inputStream the data of origin PDF
     * @param fileName the name of origin PDF
     * @param type gives type of annotation
     * @return a response object containing the annotated PDF
     */
    public static Response processPDFAnnotation(final InputStream inputStream,
                                                final String fileName,
                                                final GrobidRestUtils.Annotation type) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;
        PDDocument out = null;
        try {
            originFile = GrobidRestUtils.writeInputFile(inputStream);

            GrobidAnalysisConfig config = new GrobidAnalysisConfig.
                GrobidAnalysisConfigBuilder().build();

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process
                final PDDocument document = PDDocument.load(originFile);
                engine = GrobidRestUtils.getEngine(isparallelExec);
				DocumentSource documentSource = DocumentSource.fromPdf(originFile);
                if (isparallelExec) {
                    Document teiDoc = engine.fullTextToTEIDoc(originFile, config);
                    if (type == GrobidRestUtils.Annotation.CITATION) {
                        out = CitationsVisualizer.annotatePdfWithCitations(document, teiDoc);
					}
                    else if (type == GrobidRestUtils.Annotation.BLOCK) {
                        out = BlockVisualizer.annotateBlocks(document, documentSource.getXmlFile(), 
								teiDoc, true, true, false);
					}
                    else if (type == GrobidRestUtils.Annotation.FIGURE) {
                        out = FigureTableVisualizer.annotateFigureAndTables(document, documentSource.getXmlFile(), 
								teiDoc, true, true, true);
					}
                    GrobidPoolingFactory.returnEngine(engine);
                    engine = null;
                } else {
                    synchronized (engine) {
                        //TODO: VZ: sync on local var does not make sense
                        Document teiDoc = engine.fullTextToTEIDoc(originFile, config);
                        if (type == GrobidRestUtils.Annotation.CITATION) {
                            out = CitationsVisualizer.annotatePdfWithCitations(document, teiDoc);
						}
                        else if (type == GrobidRestUtils.Annotation.BLOCK) {
                            out = BlockVisualizer.annotateBlocks(document, documentSource.getXmlFile(), 
								teiDoc, true, true, false);
						}
	                    else if (type == GrobidRestUtils.Annotation.FIGURE) {
	                        out = FigureTableVisualizer.annotateFigureAndTables(document, documentSource.getXmlFile(), 
									teiDoc, true, true, true);
						}
                    } 
                }

                GrobidRestUtils.removeTempFile(originFile);

                if (out != null) {
                    response = Response.status(Status.OK).type("application/pdf").build();
                    ByteArrayOutputStream ouputStream = new ByteArrayOutputStream();
                    out.save(ouputStream);
                    response = Response
                            .ok()
                            .type("application/pdf")
                            .entity(ouputStream.toByteArray())
                            .header("Content-Disposition", "attachment; filename=\"" + fileName
                                    //.replace(".pdf", ".annotated.pdf")
                                    //.replace(".PDF", ".annotated.PDF") 
                                    + "\"")
                            .build();
                }
                else {
                    response = Response.status(Status.NO_CONTENT).build();
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getCause().getMessage()).build();
        } finally {
            GrobidRestUtils.removeTempFile(originFile);
            if (out != null) {
                try {
                    out.close();
                }
                catch(Exception exp) {
                    LOGGER.error("Error when closing PDDocument. ", exp);
                }
            }
            if (isparallelExec && engine != null) {
                GrobidPoolingFactory.returnEngine(engine);
            }
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    public static String methodLogIn() {
        return ">> " + GrobidRestProcessFiles.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    public static String methodLogOut() {
        return "<< " + GrobidRestProcessFiles.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

}
