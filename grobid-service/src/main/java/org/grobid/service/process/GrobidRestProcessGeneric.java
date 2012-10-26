package org.grobid.service.process;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Damien
 *
 */
public class GrobidRestProcessGeneric {
	
	/**
	 * The class Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidRestProcessGeneric.class);
	
	/**
	 * Returns a string containing true, if the service is alive.
	 * 
	 * @return returns a response object containing the string true if service
	 *         is alive.
	 */
	public static Response isAlive() {
		Response response = null;
		try {
			LOGGER.debug("called isAlive()...");

			String retVal = null;
			try {
				retVal = Boolean.valueOf(true).toString();
			} catch (Exception e) {
				LOGGER.error("COSMATService is not alive, because of: ", e);
				retVal = Boolean.valueOf(false).toString();
			}
			response = Response.status(Status.OK).entity(retVal).build();
		} catch (Exception e) {
			LOGGER.error("" + e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return response;
	}
	
	/**
	 * Returns the description of how to use the grobid-service in a human
	 * readable way (html).
	 * 
	 * @return returns a response object containing a html description
	 */
	public static Response getDescription_html(UriInfo uriInfo) {
		Response response = null;
		try {
			LOGGER.debug("called getDescription_html()...");

			StringBuffer htmlCode = new StringBuffer();

			htmlCode.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
			htmlCode.append("<html>");
			htmlCode.append("<head>");
			htmlCode.append("<title>grobid-service - description</title>");
			htmlCode.append("</head>");
			htmlCode.append("<body>");
			htmlCode.append("<h1>grobid-service documentation</h1>");
			htmlCode.append("This service provides a RESTful interface for using the grobid system. grobid extracts data from pdf files. For more information see: ");
			htmlCode.append("<a href=\"http://hal.inria.fr/inria-00493437_v1/\">http://hal.inria.fr/inria-00493437_v1/</a>");
			htmlCode.append("<br/>");
			htmlCode.append("</body>");
			htmlCode.append("</html>");

			response = Response.status(Status.OK).entity(htmlCode.toString())
					.type(MediaType.TEXT_HTML).build();
		} catch (Exception e) {
			LOGGER.error(
					"Cannot response the description for grobid-service. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return response;
	}

}
