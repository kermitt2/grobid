package org.grobid.service.process;

import java.io.File;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.grobid.core.engines.Engine;
import org.grobid.service.util.GrobidServiceProperties;
import org.grobid.service.utils.GrobidRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Damien
 * 
 */
public class GrobidRestProcessFiles {

	/**
	 * The class Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidRestProcessFiles.class);

	/**
	 * Uploads the origin document which shall be extracted into TEI and
	 * extracts only the header data.
	 * 
	 * @param inputStream
	 *            the data of origin document
	 * @return a response object which contains a TEI representation of the
	 *         header part
	 */
	public static Response processStatelessHeaderDocument(
			InputStream inputStream) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		try {
			File originFile = GrobidRestUtils.writeInputFile(inputStream);

			if (originFile == null) {
				response = Response.status(Status.INTERNAL_SERVER_ERROR)
						.build();
			} else {
				// starts conversion process
				Engine engine = GrobidRestUtils.getEngine(isparallelExec);
				if (isparallelExec) {
					retVal = engine.processHeader(originFile.getAbsolutePath(),
							false, null);
					engine.close();
				} else {
					synchronized (engine) {
						retVal = engine.processHeader(
								originFile.getAbsolutePath(), false, null);
					}
				}

				GrobidRestUtils.removeTempFile(originFile);

				if ((retVal == null) || (retVal.isEmpty())) {
					response = Response.status(Status.NO_CONTENT).build();
				} else {
					response = Response.status(Status.OK).entity(retVal)
							.type(MediaType.APPLICATION_XML).build();
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			LOGGER.error("An unexpected exception occurs. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		LOGGER.debug(methodLogOut());
		return response;
	}

	/**
	 * Uploads the origin document which shall be extracted into TEI.
	 * 
	 * @param inputStream
	 *            the data of origin document
	 * @return a response object mainly contain the TEI representation of the
	 *         full text
	 */
	public static Response processStatelessFulltextDocument(
			InputStream inputStream) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		try {
			File originFile = GrobidRestUtils.writeInputFile(inputStream);

			if (originFile == null) {
				response = Response.status(Status.INTERNAL_SERVER_ERROR)
						.build();
			} else {
				// starts conversion process
				Engine engine = GrobidRestUtils.getEngine(isparallelExec);
				if (isparallelExec) {
					retVal = engine.fullTextToTEI(originFile.getAbsolutePath(),
							false, false);
					engine.close();
				} else {
					synchronized (engine) {
						retVal = engine.fullTextToTEI(
								originFile.getAbsolutePath(), false, false);
					}
				}

				GrobidRestUtils.removeTempFile(originFile);

				if (!GrobidRestUtils.isResultOK(retVal)) {
					response = Response.status(Status.NO_CONTENT).build();
				} else {
					response = Response.status(Status.OK).entity(retVal)
							.type(MediaType.APPLICATION_XML).build();
				}
			}
		} catch (Throwable e) {
			LOGGER.error("An unexpected exception occurs. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		LOGGER.debug(methodLogOut());
		return response;
	}

	/**
	 * @return
	 */
	public static String methodLogIn() {
		return ">> " + GrobidRestProcessFiles.class.getName() + "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName();
	}

	/**
	 * @return
	 */
	public static String methodLogOut() {
		return "<< " + GrobidRestProcessFiles.class.getName() + "."
				+ Thread.currentThread().getStackTrace()[1].getMethodName();
	}

}
