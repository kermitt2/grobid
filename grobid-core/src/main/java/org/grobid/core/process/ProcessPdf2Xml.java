package org.grobid.core.process;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessPdf2Xml {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProcessPdf2Xml.class);

	/**
	 * Process the conversion.
	 */
	public static Integer process(final String cmd) {
		Process process = null;
		Integer exit = null;
		try {
			// int exit;
			process = Runtime.getRuntime().exec(cmd);

			exit = process.waitFor();

		} catch (InterruptedException ignore) {
			// Process needs to be destroyed -- it's done in the finally block
		} catch (IOException ioExp) {
			LOGGER.error("IOException while launching the command {} : {}",
					cmd, ioExp.getMessage());
		} finally {
			if (process != null) {
				IOUtils.closeQuietly(process.getInputStream());
				IOUtils.closeQuietly(process.getOutputStream());

				IOUtils.closeQuietly(process.getErrorStream());

				process.destroy();

				if (exit == null || exit != 0) {
					LOGGER.error("pdftoxml process finished with error code: "
							+ exit + ". " + cmd);
				}
			}
		}
		return exit;
	}

}
