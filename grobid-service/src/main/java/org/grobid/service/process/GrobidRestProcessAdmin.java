package org.grobid.service.process;

import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.cypher.SHA1;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.service.util.GrobidServiceProperties;
import org.grobid.service.utils.GrobidRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Damien
 * 
 */
public class GrobidRestProcessAdmin {

	/**
	 * The class Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidRestProcessAdmin.class);

	/**
	 * Returns the admin view of all properties used for running grobid.
	 * 
	 * @param sha1
	 *            the password
	 * 
	 * @return returns a response object containing the admin infos in html
	 *         syntax.
	 */
	public static Response getAdminParams(String sha1) {
		Response response = null;
		try {
			LOGGER.debug("called getDescription_html()...");
			String pass = GrobidServiceProperties.getAdminPw();
			if (StringUtils.isNotBlank(pass) && StringUtils.isNotBlank(sha1)
					&& pass.equals(SHA1.getSHA1(sha1))) {
				StringBuffer htmlCode = new StringBuffer();

				htmlCode.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
				htmlCode.append("<html>");
				htmlCode.append("<head>");
				htmlCode.append("<title>grobid-service - admin</title>");
				htmlCode.append("</head>");
				htmlCode.append("<body>");
				htmlCode.append("<table border=\"1\">");
				htmlCode.append("<tr><td>property</td><td>value</td></tr>");
				htmlCode.append("<th><td colspan=\"2\">java properties</td></th>");
				htmlCode.append("<tr><td>os name</td><td>"
						+ System.getProperty("os.name") + "</td></tr>");
				htmlCode.append("<tr><td>os version</td><td>"
						+ System.getProperty("sun.arch.data.model")
						+ "</td></tr>");
				htmlCode.append("<th><td colspan=\"2\">grobid_service.properties</td></th>");

				Properties props = GrobidServiceProperties.getProps();
				for (Object property : props.keySet()) {
					htmlCode.append("<tr><td>" + property + "</td><td>"
							+ props.getProperty((String) property)
							+ "</td></tr>");
				}
				htmlCode.append("<th><td colspan=\"2\">grobid.properties</td></th>");
				props = GrobidProperties.getProps();
				for (Object property : props.keySet()) {
					htmlCode.append("<tr><td>" + property + "</td><td>"
							+ props.getProperty((String) property)
							+ "</td></tr>");
				}

				htmlCode.append("</table>");
				htmlCode.append("</body>");
				htmlCode.append("</html>");

				response = Response.status(Status.OK)
						.entity(htmlCode.toString()).type(MediaType.TEXT_HTML)
						.build();
			} else {
				response = Response.status(Status.FORBIDDEN).build();
			}
		} catch (Exception e) {
			LOGGER.error(
					"Cannot response the description for grobid-service. ", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return response;
	}
	
	/**
	 * Process SHA1.
	 * 
	 * @param sha1
	 *            string to hash
	 * @return Response containing the value hashed.
	 */
	public static Response processSHA1(String sha1) {
		LOGGER.debug(">> processSHA1");
		Response response = null;
		String retVal = null;
		try {
			retVal = SHA1.getSHA1(sha1);
			if (GrobidRestUtils.isResultOK(retVal)) {
				response = Response.status(Status.OK).entity(retVal)
						.type(MediaType.TEXT_PLAIN).build();
			} else {
				response = Response.status(Status.NO_CONTENT).build();

			}
		} catch (Exception exp) {
			LOGGER.error("An unexpected exception occurs. ", exp);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		LOGGER.debug("<< processSHA1");
		return response;
	}

}
