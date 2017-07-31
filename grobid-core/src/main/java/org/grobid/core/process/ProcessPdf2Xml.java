package org.grobid.core.process;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ProcessPdf2Xml {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessPdf2Xml.class);

    /**
     * Process the conversion.
     */
    public static Integer process(List<String> cmd) {
        Process process = null;
        ProcessBuilder builder = null;
        Integer exit = null;
        String message = "error message cannot be retrieved";
        try {
            builder = new ProcessBuilder(cmd);
            process = builder.start();
            exit = process.waitFor();
            message = IOUtils.toString(process.getErrorStream());

        } catch (InterruptedException ignore) {
            // Process needs to be destroyed -- it's done in the finally block
            LOGGER.warn("pdf2xml process is about to be killed.");
        } catch (IOException ioExp) {
            LOGGER.error("IOException while launching the command {} : {}",
                    cmd, ioExp.getMessage());
        } finally {
            if (process != null) {
                IOUtils.closeQuietly(process.getInputStream(), process.getOutputStream(), process.getErrorStream());

                process.destroy();

                if (exit == null || exit != 0) {
                    LOGGER.error("pdftoxml process finished with error code: "
                            + exit + ". " + cmd);
                    LOGGER.error("pdftoxml return message: \n" + message);
                }
            }
        }
        return exit;
    }

}
