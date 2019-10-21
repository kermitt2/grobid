package org.grobid.core.document;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.process.ProcessRunner;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Input document to be processed, which could come from a PDF, a doc/docx or directly be an XML file. 
 * If from a PDF document, this is the place where pdfalto is called.
 * If from a doc/docx document, this is the place where a conversion with Apache POI is realized. 
 */
public class DocumentSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSource.class);
    //    private static final int DEFAULT_TIMEOUT = 30000;
    private static final int KILLED_DUE_2_TIMEOUT = 143;
    private static final int MISSING_LIBXML2 = 127;
    private static final int MISSING_PDFTOXML = 126;
    public static final int PDFTOXML_FILES_AMOUNT_LIMIT = 5000;

    private File pdfFile;
    private File docxFile;
    private File xmlFile;
    boolean cleanupXml = false;
    boolean cleanupPdf = false;

    private DocumentSource() {
    }

    public static DocumentSource fromPdf(File pdfFile) {
        return fromPdf(pdfFile, -1, -1);
    }

    public static DocumentSource fromDocx(File docxFile) {
        return fromDocx(docxFile, -1, -1);
    }

    /**
     * By default the XML extracted from the PDF is without images, to avoid flooding the grobid-home/tmp directory,
	 * but with the extra annotation file and with outline	
     */
    public static DocumentSource fromPdf(File pdfFile, int startPage, int endPage) {
        return fromPdf(pdfFile, startPage, endPage, false, true, false);
    }

    public static DocumentSource fromDocx(File docxFile, int startPage, int endPage) {
        return fromDocx(docxFile, startPage, endPage, false, true, false);
    }

    public static DocumentSource fromPdf(File pdfFile, int startPage, int endPage, 
										 boolean withImages, boolean withAnnotations, boolean withOutline) {
        if (!pdfFile.exists() || pdfFile.isDirectory()) {
            throw new GrobidException("Input PDF file " + pdfFile + " does not exist or a directory", 
                GrobidExceptionStatus.BAD_INPUT_DATA);
        }

        DocumentSource source = new DocumentSource();
        source.cleanupXml = true;

        try {
            source.xmlFile = source.pdf2xml(null, false, startPage, endPage, pdfFile, 
                GrobidProperties.getTempPath(), withImages, withAnnotations, withOutline);
        } catch (Exception e) {
            source.close(withImages, withAnnotations, withOutline);
            throw e;
        } finally {
        }
        source.pdfFile = pdfFile;
        return source;
    }

    public static DocumentSource fromDocx(File docxFile, int startPage, int endPage, 
                                         boolean withImages, boolean withAnnotations, boolean withOutline) {
        if (!docxFile.exists() || docxFile.isDirectory()) {
            throw new GrobidException("Input doc/docx file " + docxFile + " does not exist or a directory", 
                GrobidExceptionStatus.BAD_INPUT_DATA);
        }

        DocumentSource source = new DocumentSource();
        source.cleanupXml = true;
        source.cleanupPdf = true;

        // preliminary convert doc/docx file into PDF
        File pdfFile = source.docxToPdf(docxFile, GrobidProperties.getTempPath());
        // create an ALTO representation
        if (pdfFile != null) {
            try {
                source.xmlFile = source.pdf2xml(null, false, startPage, endPage, pdfFile, 
                    GrobidProperties.getTempPath(), withImages, withAnnotations, withOutline);
            } catch (Exception e) {
                source.close(withImages, withAnnotations, withOutline);
                throw e;
            } finally {
                source.cleanPdfFile(pdfFile);
            }
        }
        source.docxFile = docxFile;
        return source;
    }

    private String getPdfToXmlCommand(boolean withImage, boolean withAnnotations, boolean withOutline) {
        StringBuilder pdfToXml = new StringBuilder();
        pdfToXml.append(GrobidProperties.getPdfToXMLPath().getAbsolutePath());
        // bat files sets the path env variable for cygwin dll
        if (SystemUtils.IS_OS_WINDOWS) {
            //pdfalto executable are separated to avoid dll conflicts
            pdfToXml.append(File.separator +"pdfalto");
        }
        pdfToXml.append(
                GrobidProperties.isContextExecutionServer() ? File.separator + "pdfalto_server" : File.separator + "pdfalto");

        pdfToXml.append(" -blocks -noImageInline -fullFontName ");

        if (!withImage) {
            pdfToXml.append(" -noImage ");
		}
        if (withAnnotations) {
            pdfToXml.append(" -annotation ");
        }
        if (withOutline) {
            pdfToXml.append(" -outline ");
        }

//        pdfToXml.append(" -readingOrder ");
//        pdfToXml.append(" -ocr ");

        pdfToXml.append(" -filesLimit 2000 ");

        //System.out.println(pdfToXml);
        //pdfToXml.append(" -conf <path to config> ");
        return pdfToXml.toString();
    }

    /**
     * Create an XML representation from a pdf file. If tout is true (default),
     * a timeout is used. If force is true, the xml file is always regenerated,
     * even if already present (default is false, it can save up to 50% overall
     * runtime). If full is true, the extraction covers also images within the
     * pdf, which is relevant for fulltext extraction.
     */
    public File pdf2xml(Integer timeout, boolean force, int startPage,
                        int endPage, File pdfPath, File tmpPath, boolean withImages, 
						boolean withAnnotations, boolean withOutline) {
        LOGGER.debug("start pdf to xml sub process");
        long time = System.currentTimeMillis();
        String pdftoxml0;

        pdftoxml0 = getPdfToXmlCommand(withImages, withAnnotations, withOutline);

        if (startPage > 0)
            pdftoxml0 += " -f " + startPage + " ";
        if (endPage > 0)
            pdftoxml0 += " -l " + endPage + " ";

        // if the XML representation already exists, no need to redo the
        // conversion,
        // except if the force parameter is set to true
        File tmpPathXML = new File(tmpPath, KeyGen.getKey() + ".lxml");
        xmlFile = tmpPathXML;
        File f = tmpPathXML;

        if ((!f.exists()) || force) {
            List<String> cmd = new ArrayList<>();
            String[] tokens = pdftoxml0.split(" ");
            for (String token : tokens) {
                if (token.trim().length() > 0) {
                    cmd.add(token);
                }
            }
            cmd.add(pdfPath.getAbsolutePath());
            cmd.add(tmpPathXML.getAbsolutePath());
            if (GrobidProperties.isContextExecutionServer()) {
                tmpPathXML = processPdfToXmlServerMode(pdfPath, tmpPathXML, cmd);
            } else {
                if (!SystemUtils.IS_OS_WINDOWS) {
                    cmd = Arrays.asList("bash", "-c", "ulimit -Sv " +
                            GrobidProperties.getPdfToXMLMemoryLimitMb() * 1024 + " && " + pdftoxml0 + " '" + pdfPath + "' " + tmpPathXML);
                }
                LOGGER.debug("Executing command: " + cmd);

                tmpPathXML = processPdfToXmlThreadMode(timeout, pdfPath, tmpPathXML, cmd);
            }

            File dataFolder = new File(tmpPathXML.getAbsolutePath() + "_data");
            File[] files = dataFolder.listFiles();
            if (files != null && files.length > PDFTOXML_FILES_AMOUNT_LIMIT) {
                //throw new GrobidException("The temp folder " + dataFolder + " contains " + files.length + " files and exceeds the limit", 
                //    GrobidExceptionStatus.PARSING_ERROR);
                LOGGER.warn("The temp folder " + dataFolder + " contains " + files.length + 
                    " files and exceeds the limit, only the first " + PDFTOXML_FILES_AMOUNT_LIMIT + " asset files will be kept.");
            }
        }
        LOGGER.debug("pdf to xml sub process process finished. Time to process:" + (System.currentTimeMillis() - time) + "ms");
        return tmpPathXML;
    }

    /**
     * Process the conversion of pdf to xml format using thread calling native
     * executable.
     * <p>
     * Executed NOT in the server mode
     *
     * @param timeout    in ms.   null, if default
     * @param pdfPath    path to pdf
     * @param tmpPathXML temporary path to save the converted file
     * @param cmd        arguments to call the executable pdf2xml
     * @return the path the the converted file.
     */
    private File processPdfToXmlThreadMode(Integer timeout, File pdfPath,
                                          File tmpPathXML, List<String> cmd) {
        LOGGER.debug("Executing: " + cmd.toString());
        ProcessRunner worker = new ProcessRunner(cmd, "pdfalto[" + pdfPath + "]", true);

        worker.start();

        try {
            if (timeout != null) {
                worker.join(timeout);
            } else {
                worker.join(GrobidProperties.getPdfToXMLTimeoutMs()); // max 50 second even without predefined
                // timeout
            }
            if (worker.getExitStatus() == null) {
                tmpPathXML = null;
                //killing all child processes harshly
                worker.killProcess();
                close(true, true, true);
                throw new GrobidException("PDF to XML conversion timed out", GrobidExceptionStatus.TIMEOUT);
            }

            if (worker.getExitStatus() != 0) {
                String errorStreamContents = worker.getErrorStreamContents();
                close(true, true, true);
                throw new GrobidException("PDF to XML conversion failed on pdf file " + pdfPath + " " +
                        (StringUtils.isEmpty(errorStreamContents) ? "" : ("due to: " + errorStreamContents)),
                        GrobidExceptionStatus.PDFTOXML_CONVERSION_FAILURE);
            }
        } catch (InterruptedException ex) {
            tmpPathXML = null;
            worker.interrupt();
            Thread.currentThread().interrupt();
        } finally {
            worker.interrupt();
        }
        return tmpPathXML;
    }

    /**
     * Process the conversion of pdf to xml format calling native executable. No
     * thread used for the execution.
     *
     * @param pdfPath    path to pdf
     * @param tmpPathXML temporary path to save the converted file
     * @param cmd        arguments to call the executable pdf2xml
     * @return the path the the converted file.
     */
    private File processPdfToXmlServerMode(File pdfPath, File tmpPathXML, List<String> cmd) {
        LOGGER.debug("Executing: " + cmd.toString());
        Integer exitCode = org.grobid.core.process.ProcessPdfToXml.process(cmd);

        if (exitCode == null) {
            throw new GrobidException("An error occurred while converting pdf " + pdfPath, GrobidExceptionStatus.BAD_INPUT_DATA);
        } else if (exitCode == KILLED_DUE_2_TIMEOUT) {
            throw new GrobidException("PDF to XML conversion timed out", GrobidExceptionStatus.TIMEOUT);
        } else if (exitCode == MISSING_PDFTOXML) {
            throw new GrobidException("PDF to XML conversion failed. Cannot find pdfalto executable", GrobidExceptionStatus.PDFTOXML_CONVERSION_FAILURE);
        } else if (exitCode == MISSING_LIBXML2) {
            throw new GrobidException("PDF to XML conversion failed. pdfalto cannot be executed correctly. Has libxml2 been installed in the system? More information can be found in the logs. ", GrobidExceptionStatus.PDFTOXML_CONVERSION_FAILURE);
        } else if (exitCode != 0) {
            throw new GrobidException("PDF to XML conversion failed with error code: " + exitCode, GrobidExceptionStatus.BAD_INPUT_DATA);
        }

        return tmpPathXML;
    }

    private boolean cleanXmlFile(File pathToXml, boolean cleanImages, boolean cleanAnnotations, boolean cleanOutline) {
        boolean success = false;

        try {
            if (pathToXml != null) {
                if (pathToXml.exists()) {
                    success = pathToXml.delete();
                    if (!success) {
                        throw new GrobidResourceException("Deletion of a temporary XML file failed for file '" + pathToXml.getAbsolutePath() + "'");
                    }

                    File fff = new File(pathToXml + "_metadata.xml");
                    if (fff.exists()) {
                            success = Utilities.deleteDir(fff);

                            if (!success) {
                                throw new GrobidResourceException(
                                    "Deletion of temporary metadata file failed for file '" + fff.getAbsolutePath() + "'");
                            }
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof GrobidResourceException) {
                throw (GrobidResourceException) e;
            } else {
                throw new GrobidResourceException("An exception occurred while deleting an XML file '" + pathToXml + "'.", e);
            }
        }

        // if cleanImages is true, we also remove the corresponding image
        // resources subdirectory
        if (cleanImages) {
            try {
                if (pathToXml != null) {
                    File fff = new File(pathToXml + "_data");
                    if (fff.exists()) {
                        if (fff.isDirectory()) {
                            success = Utilities.deleteDir(fff);

                            if (!success) {
                                throw new GrobidResourceException(
                                        "Deletion of temporary image files failed for file '" + fff.getAbsolutePath() + "'");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (e instanceof GrobidResourceException) {
                    throw (GrobidResourceException) e;
                } else {
                    throw new GrobidResourceException("An exception occurred while deleting an XML file '" + pathToXml + "'.", e);
                }
            }
        }

        // if cleanAnnotations is true, we also remove the additional annotation file
        if (cleanAnnotations) {
            try {
                if (pathToXml != null) {
                    File fff = new File(pathToXml + "_annot.xml");
                    if (fff.exists()) {
                        success = fff.delete();

                        if (!success) {
                            throw new GrobidResourceException(
                                    "Deletion of temporary annotation file failed for file '" + fff.getAbsolutePath() + "'");
                        }
                    }
                }
            } catch (Exception e) {
                if (e instanceof GrobidResourceException) {
                    throw (GrobidResourceException) e;
                } else {
                    throw new GrobidResourceException("An exception occurred while deleting an XML file '" + pathToXml + "'.", e);
                }
            }
        }

        // if cleanOutline is true, we also remoce the additional outline file
        if (cleanOutline) {
            try {
                if (pathToXml != null) {
                    File fff = new File(pathToXml + "_outline.xml");
                    if (fff.exists()) {
                        success = fff.delete();

                        if (!success) {
                            throw new GrobidResourceException(
                                    "Deletion of temporary outline file failed for file '" + fff.getAbsolutePath() + "'");
                        }
                    }
                }
            } catch (Exception e) {
                if (e instanceof GrobidResourceException) {
                    throw (GrobidResourceException) e;
                } else {
                    throw new GrobidResourceException("An exception occurred while deleting an XML file '" + pathToXml + "'.", e);
                }
            }
        }

        return success;
    }

    private boolean cleanPdfFile(File pathToPdf) {
        boolean success = false;
        try {
            if (pathToPdf != null) {
                if (pathToPdf.exists()) {
                    success = pathToPdf.delete();
                    if (!success) {
                        throw new GrobidResourceException("Deletion of a temporary PDF file failed for file '" + pathToPdf.getAbsolutePath() + "'");
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof GrobidResourceException) {
                throw (GrobidResourceException) e;
            } else {
                throw new GrobidResourceException("An exception occurred while deleting an PDF file '" + pathToPdf + "'.", e);
            }
        }

        return success;
    }

    /**
     * Convert doc/docx file to pdf format using Apache POI (via opensagres converter). 
     * The current thread is used for the execution.
     *
     * @param docxPath  docx/doc file
     * @param tmpPath   temp path to save the converted file
     * @return the converted file or null if conversion was impossible/failed
     */
    private File docxToPdf(File docxFile, File tmpPath) {
        // target PDF file 
        if (docxFile == null || !docxFile.exists()) {
            LOGGER.error("Invalid doc/docx file for PDF conversion");
            return null;
        }

        File pdfFile = new File(tmpPath, KeyGen.getKey() + ".pdf");
        try (
            InputStream is = new FileInputStream(docxFile);
            OutputStream out = new FileOutputStream(pdfFile);
        ) {
            long start = System.currentTimeMillis();
            // load the docx file into XWPFDocument
            XWPFDocument document = new XWPFDocument(is);
            // PDF options
            PdfOptions options = PdfOptions.create();
            
            // note: the default font encoding will be unicode, but it does not always work given the docx fonts,
            // it is possible to set explicitely a font encoding like this:
            // options = PdfOptions.create().fontEncoding("windows-1250");

            // ensure PDF/A conformance level, for safer PDF processing by pdfalto 
            /*options.setConfiguration( new IPdfWriterConfiguration() {
                public void configure( PdfWriter writer ) {
                    writer.setPDFXConformance( PdfWriter.PDFA1A );
                }
            });*/

            // converting XWPFDocument to PDF
            PdfConverter.getInstance().convert(document, out, options);
            LOGGER.info("docx file converted to PDF in : " + (System.currentTimeMillis() - start) + " milli seconds");

            // TBD: for using the more recent version 2.0.2 of fr.opensagres.poi.xwpf.converter.core, see
            // https://stackoverflow.com/questions/51330192/trying-to-make-simple-pdf-document-with-apache-poi
        } catch (Throwable e) {
            LOGGER.error("converting doc/docx into PDF failed", e);
            pdfFile = null;
        }
        return pdfFile;
    }

    public void close(boolean cleanImages, boolean cleanAnnotations, boolean cleanOutline) {
        try {
            if (cleanupXml) {
                cleanXmlFile(xmlFile, cleanImages, cleanAnnotations, cleanOutline);
            } 
            if (cleanupPdf) {
                cleanPdfFile(pdfFile);
            }
        } catch (Exception e) {
            LOGGER.error("Cannot cleanup resources (just printing exception):", e);
        }
    }

    public static void close(DocumentSource source, boolean cleanImages, boolean cleanAnnotations, boolean cleanOutline) {
        if (source != null) {
            source.close(cleanImages, cleanAnnotations, cleanOutline);
        }
    }

    public File getPdfFile() {
        return this.pdfFile;
    }

    public void setPdfFile(File pdfFile) {
        this.pdfFile = pdfFile;
    }

    public File getXmlFile() {
        return this.xmlFile;
    }

    public void setXmlFile(File docxFile) {
        this.xmlFile = xmlFile;
    }

    public File getDocxFile() {
        return this.xmlFile;
    }

    public void setDocxFile(File docxFile) {
        this.docxFile = docxFile;
    }

}



