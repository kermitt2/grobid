package org.grobid.service.process;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidPoolingFactory;
import org.grobid.service.parser.Xml2HtmlParser;
import org.grobid.service.util.GrobidRestUtils;
import org.grobid.service.util.GrobidServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

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
	 *            - the data of origin document
	 * @param htmlFormat
	 *            - if the result has to be formatted to be displayed as html.
	 * @return a response object which contains a TEI representation of the
	 *         header part
	 */
	public static Response processStatelessHeaderDocument(
			InputStream inputStream, boolean htmlFormat) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		File originFile = null;
		Engine engine = null;
		try {
			originFile = GrobidRestUtils.writeInputFile(inputStream);

			if (originFile == null) {
				response = Response.status(Status.INTERNAL_SERVER_ERROR)
						.build();
			} else {
				// starts conversion process
				engine = GrobidRestUtils.getEngine(isparallelExec);
				if (isparallelExec) {
					retVal = engine.processHeader(originFile.getAbsolutePath(),
							false, null);
				} else {
					synchronized (engine) {
						retVal = engine.processHeader(
								originFile.getAbsolutePath(), false, null);
					}
				}

				if ((retVal == null) || (retVal.isEmpty())) {
					response = Response.status(Status.NO_CONTENT).build();
				} else {
					if (htmlFormat) {
						response = Response.status(Status.OK)
								.entity(formatAsHTML(retVal))
								.type(MediaType.APPLICATION_XML).build();
					} else {
						response = Response.status(Status.OK).entity(retVal)
								.type(MediaType.APPLICATION_XML).build();
					}
				}
			}
		} catch (NoSuchElementException nseExp) {
			LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.");
			response = Response.status(Status.SERVICE_UNAVAILABLE).build();
		} catch (TimeoutException timeoutExp) {
			LOGGER.error("Grobid timed out: " + timeoutExp);
			response = Response.status(408).build();
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
	 * @param tei
	 *            the xml input.
	 * @return html formatted String.
	 * @throws SAXException
	 * @throws IOException
	 */
	protected static String formatAsHTML(String tei) throws SAXException,
			IOException {
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
	 * @param inputStream
	 *            zip containing the datas of origin document.
	 * @return Response containing the TEI files representing the header part.
	 */
	public static Response processStatelessBulkHeaderDocument(
			InputStream inputStream) {
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
	 * @param inputStream
	 *            the data of origin document
	 * @param htmlFormat
	 *            - if the result has to be formatted to be displayed as html.
	 * @return a response object mainly contain the TEI representation of the
	 *         full text
	 */
	public static Response processStatelessFulltextDocument(
			InputStream inputStream, boolean htmlFormat) {
		LOGGER.debug(methodLogIn());
		Response response = null;
		String retVal = null;
		boolean isparallelExec = GrobidServiceProperties.isParallelExec();
		File originFile = null;
		Engine engine = null;
		try {
			originFile = GrobidRestUtils.writeInputFile(inputStream);

			if (originFile == null) {
				response = Response.status(Status.INTERNAL_SERVER_ERROR)
						.build();
			} else {
				// starts conversion process
				engine = GrobidRestUtils.getEngine(isparallelExec);
				if (isparallelExec) {
					retVal = engine.fullTextToTEI(originFile.getAbsolutePath(),
							false, false);
					GrobidPoolingFactory.returnEngine(engine);
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
					if (htmlFormat) {
						response = Response.status(Status.OK)
								.entity(formatAsHTML(retVal))
								.type(MediaType.APPLICATION_XML).build();
					} else {
						response = Response.status(Status.OK).entity(retVal)
								.type(MediaType.APPLICATION_XML).build();
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
