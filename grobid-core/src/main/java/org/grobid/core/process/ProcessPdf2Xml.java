package org.grobid.core.process;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessPdf2Xml {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProcessPdf2Xml.class);

	private String cmd;
	private Integer exit;

	public String getErrorStreamContents() {
		return errorStreamContents;
	}

	private String errorStreamContents;

	// private boolean useStreamGobbler;
	// StreamGobbler sgIn;
	// StreamGobbler sgErr;

	public ProcessPdf2Xml(String cmd, String name, boolean useStreamGobbler) {
		this.cmd = cmd;
		// this.useStreamGobbler = useStreamGobbler;
	}

	public void process() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);

//			if (useStreamGobbler) {
//				sgIn = new StreamGobbler(process.getInputStream());
//				sgErr = new StreamGobbler(process.getErrorStream());
//			}

			exit = process.waitFor();
		} catch (InterruptedException ignore) {
			// Process needs to be destroyed -- it's done in the finally block
		} catch (IOException e) {
			LOGGER.error("IOException while launching the command {} : {}",
					cmd, e.getMessage());
		} finally {
			if (process != null) {
				IOUtils.closeQuietly(process.getInputStream());
				IOUtils.closeQuietly(process.getOutputStream());
				try {
					errorStreamContents = IOUtils.toString(process
							.getErrorStream());
				} catch (IOException e) {
					LOGGER.error(
							"Error retrieving error stream from process: {}", e);
				}
				IOUtils.closeQuietly(process.getErrorStream());

				process.destroy();
			}

			/*if (useStreamGobbler) {
				try {
					if (sgIn != null) {
						sgIn.close();
					}
				} catch (IOException e) {
					LOGGER.error(
							"IOException while closing the stream gobbler: {}",
							e);
				}

				try {
					if (sgErr != null) {
						sgErr.close();
					}
				} catch (IOException e) {
					LOGGER.error(
							"IOException while closing the stream gobbler: {}",
							e);
				}
			}*/
		}

	}

	public Integer getExitStatus() {
		return exit;
	}

}
