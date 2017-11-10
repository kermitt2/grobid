package org.grobid.core.utilities;

import org.apache.commons.io.IOUtils;
import org.grobid.core.exceptions.GrobidResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Utilities related to file and directory management.
 * <p>
 * Created by lfoppiano on 04/08/16.
 */
public class IOUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtilities.class);

    /**
     * Creates a file and writes some content in it.
     *
     * @param file    The file to write in.
     * @param content the content to write
     * @throws IOException
     */
    public static void writeInFile(String file, String content)
            throws IOException {
        FileWriter filew = new FileWriter(new File(file));
        BufferedWriter buffw = new BufferedWriter(filew);
        buffw.write(content);
        buffw.close();
    }

    /**
     * Read a file and return the content.
     *
     * @param pPathToFile path to file to read.
     * @return String contained in the document.
     * @throws IOException
     */
    public static String readFile(String pPathToFile) throws IOException {
        StringBuffer out = new StringBuffer();
        FileInputStream inputStrem = new FileInputStream(new File(pPathToFile));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        while ((len = inputStrem.read(buf)) > 0) {
            outStream.write(buf, 0, len);
            out.append(outStream.toString());
        }
        IOUtils.closeQuietly(inputStrem);
        IOUtils.closeQuietly(outStream);

        return out.toString();
    }

    /**
     * Write an input stream in temp directory.
     */
    public static File writeInputFile(InputStream inputStream) {
        LOGGER.debug(">> set origin document for stateless service'...");

        File originFile = null;
        OutputStream out = null;
        try {
            originFile = newTempFile("origin", ".pdf");

            out = new FileOutputStream(originFile);

            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            LOGGER.error(
                    "An internal error occurs, while writing to disk (file to write '"
                            + originFile + "').", e);
            originFile = null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                inputStream.close();
            } catch (IOException e) {
                LOGGER.error("An internal error occurs, while writing to disk (file to write '"
                        + originFile + "').", e);
                originFile = null;
            }
        }
        return originFile;
    }

    /**
     * Creates a new not used temporary file and returns it.
     */
    public static File newTempFile(String fileName, String extension) {
        try {
            return File.createTempFile(fileName, extension, GrobidProperties.getTempPath());
        } catch (IOException e) {
            throw new GrobidResourceException(
                    "Could not create temprorary file, '" + fileName + "." +
                            extension + "' under path '" + GrobidProperties.getTempPath() + "'.", e);
        }
    }

    /**
     * Delete the temporary file.
     */
    public static void removeTempFile(final File file) {
        
        try {
            // sanity cleaning
            Utilities.deleteOldies(GrobidProperties.getTempPath(), 300);
            LOGGER.debug("Removing " + file.getAbsolutePath());
            file.delete();
        } catch (Exception exp) {
            LOGGER.error("Error while deleting the temporary file: ", exp);
        }
    }

    /**
     * Delete temporary directory.
     */
    public static void removeTempDirectory(final String path) {
        
        try {
            LOGGER.debug("Removing " + path);
            File theDirectory = new File(path);
            if (theDirectory.exists()) {
                theDirectory.delete();
            }
        } catch (Exception exp) {
            LOGGER.error("Error while deleting the temporary directory: ", exp);
        }
    }
}
