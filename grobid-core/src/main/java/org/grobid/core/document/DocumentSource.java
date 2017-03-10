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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: zholudev
 * Date: 3/6/15
 */
public class DocumentSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSource.class);
    //    private static final int DEFAULT_TIMEOUT = 30000;
    private static final int KILLED_DUE_2_TIMEOUT = 143;
    private static final int MISSING_LIBXML2 = 127;
    private static final int MISSING_PDF2XML = 126;
    public static final int PDF2XML_FILES_AMOUNT_LIMIT = 5000;

    private File pdfFile;
    private File xmlFile;
    boolean cleanupXml = false;


    private DocumentSource() {
    }

    public static DocumentSource fromPdf(File pdfFile) {
        return fromPdf(pdfFile, -1, -1);
    }

    /**
     * By default the XML extracted from the PDF is without images, to avoid flooding the grobid-home/tmp directory,
	 * but with the extra annotation file	
     */
    public static DocumentSource fromPdf(File pdfFile, int startPage, int endPage) {
        return fromPdf(pdfFile, startPage, endPage, false, true);
    }

    public static DocumentSource fromPdf(File pdfFile, int startPage, int endPage, 
										 boolean withImages, boolean withAnnotations) {
        if (!pdfFile.exists() || pdfFile.isDirectory()) {
            throw new GrobidException("Input PDF file " + pdfFile + " does not exist or a directory", GrobidExceptionStatus.BAD_INPUT_DATA);
        }

        DocumentSource source = new DocumentSource();
        source.cleanupXml = true;

        try {
            source.xmlFile = source.pdf2xml(null, false, startPage, endPage, pdfFile, GrobidProperties.getTempPath(), withImages, withAnnotations);
        } catch (Exception e) {
            source.close(true, true);
            throw e;
        } finally {
        }
        source.pdfFile = pdfFile;
        return source;
    }

    private String getPdf2xmlCommand(boolean withImage, boolean withAnnotations) {
        String pdf2xml = GrobidProperties.getPdf2XMLPath().getAbsolutePath();
        pdf2xml += GrobidProperties.isContextExecutionServer() ? File.separator + "pdftoxml_server" : File.separator + "pdftoxml";
		pdf2xml += " -blocks -noImageInline -fullFontName ";

        if (!withImage) {
            pdf2xml += " -noImage ";
		}
        if (withAnnotations) {
            pdf2xml += " -annotation ";
        }
        return pdf2xml;
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
						boolean withAnnotations) {
        LOGGER.debug("start pdf2xml");
        long time = System.currentTimeMillis();
        String pdftoxml0;

        pdftoxml0 = getPdf2xmlCommand(withImages, withAnnotations);

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
                tmpPathXML = processPdf2XmlServerMode(pdfPath, tmpPathXML, cmd);
            } else {
                if (!SystemUtils.IS_OS_WINDOWS) {
                    cmd = Arrays.asList("bash", "-c", "ulimit -Sv " +
                            GrobidProperties.getPdf2XMLMemoryLimitMb() * 1024 + " && " + pdftoxml0 + " '" + pdfPath + "' " + tmpPathXML);
                }
                LOGGER.debug("Executing command: " + cmd);

                tmpPathXML = processPdf2XmlThreadMode(timeout, pdfPath, tmpPathXML, cmd);
            }

            File dataFolder = new File(tmpPathXML.getAbsolutePath() + "_data");
            File[] files = dataFolder.listFiles();
            if (files != null && files.length > PDF2XML_FILES_AMOUNT_LIMIT) {
                throw new GrobidException("The temp folder " + dataFolder + " contains " + files.length + " files and exceeds the limit", GrobidExceptionStatus.PARSING_ERROR);
            }
        }
        LOGGER.debug("pdf2xml process finished. Time to process:" + (System.currentTimeMillis() - time) + "ms");
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
    private File processPdf2XmlThreadMode(Integer timeout, File pdfPath,
                                          File tmpPathXML, List<String> cmd) {
        LOGGER.debug("Executing: " + cmd.toString());
        ProcessRunner worker = new ProcessRunner(cmd, "pdf2xml[" + pdfPath + "]", true);

        worker.start();

        try {
            if (timeout != null) {
                worker.join(timeout);
            } else {
                worker.join(GrobidProperties.getPdf2XMLTimeoutMs()); // max 50 second even without predefined
                // timeout
            }
            if (worker.getExitStatus() == null) {
                tmpPathXML = null;
                //killing all child processes harshly
                worker.killProcess();
                close(true, true);
                throw new GrobidException("PDF to XML conversion timed out", GrobidExceptionStatus.TIMEOUT);
            }

            if (worker.getExitStatus() != 0) {
                String errorStreamContents = worker.getErrorStreamContents();
                close(true, true);
                throw new GrobidException("PDF to XML conversion failed on pdf file " + pdfPath + " " +
                        (StringUtils.isEmpty(errorStreamContents) ? "" : ("due to: " + errorStreamContents)),
                        GrobidExceptionStatus.PDF2XML_CONVERSION_FAILURE);
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
    private File processPdf2XmlServerMode(File pdfPath, File tmpPathXML, List<String> cmd) {
        LOGGER.debug("Executing: " + cmd.toString());
        Integer exitCode = org.grobid.core.process.ProcessPdf2Xml.process(cmd);

        if (exitCode == null) {
            throw new GrobidException("An error occurred while converting pdf " + pdfPath, GrobidExceptionStatus.BAD_INPUT_DATA);
        } else if (exitCode == KILLED_DUE_2_TIMEOUT) {
            throw new GrobidException("PDF to XML conversion timed out", GrobidExceptionStatus.TIMEOUT);
        } else if (exitCode == MISSING_PDF2XML) {
            throw new GrobidException("PDF to XML conversion failed. Cannot find pdf2xml executable", GrobidExceptionStatus.PDF2XML_CONVERSION_FAILURE);
        } else if (exitCode == MISSING_LIBXML2) {
            throw new GrobidException("PDF to XML conversion failed. pdf2xml cannot be executed correctly. Has libxml2 been installed in the system? More information can be found in the logs. ", GrobidExceptionStatus.PDF2XML_CONVERSION_FAILURE);
        } else if (exitCode != 0) {
            throw new GrobidException("PDF to XML conversion failed with error code: " + exitCode, GrobidExceptionStatus.BAD_INPUT_DATA);
        }

        return tmpPathXML;
    }

    private boolean cleanXmlFile(File pathToXml, boolean cleanImages, boolean cleanAnnotations) {
        boolean success = false;

        try {
            if (pathToXml != null) {
                if (pathToXml.exists()) {
                    success = pathToXml.delete();
                    if (!success) {
                        throw new GrobidResourceException("Deletion of a temporary XML file failed for file '" + pathToXml.getAbsolutePath() + "'");
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

        return success;
    }


    public void close(boolean cleanImages, boolean cleanAnnotations) {
        try {
            if (cleanupXml) {
                cleanXmlFile(xmlFile, cleanImages, cleanAnnotations);
            }
        } catch (Exception e) {
            LOGGER.error("Cannot cleanup resources (just printing exception):", e);
        }
    }

    public static void close(DocumentSource source, boolean cleanImages, boolean cleanAnnotations) {
        if (source != null) {
            source.close(cleanImages, cleanAnnotations);
        }
    }

    public File getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(File pdfFile) {
        this.pdfFile = pdfFile;
    }

    public File getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(File xmlFile) {
        this.xmlFile = xmlFile;
    }

}



