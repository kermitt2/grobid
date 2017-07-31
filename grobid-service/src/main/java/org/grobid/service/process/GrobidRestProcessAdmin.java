package org.grobid.service.process;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.SHA1;
import org.grobid.service.GrobidServiceConfiguration;
import org.grobid.service.exceptions.GrobidServiceException;
import org.grobid.service.parser.ChangePropertyParser;
import org.grobid.service.util.GrobidPropertiesUtil;
import org.grobid.service.util.GrobidProperty;
import org.grobid.service.util.GrobidRestUtils;
import org.grobid.service.util.GrobidServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Damien
 */
@Singleton
public class GrobidRestProcessAdmin {


	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidRestProcessAdmin.class);
    private GrobidServiceConfiguration configuration;

    @Inject
    public GrobidRestProcessAdmin(GrobidServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the admin view of all properties used for running grobid.
     *
     * @param sha1 the password
     * @return returns a response object containing the admin infos in html
     * syntax.
     */
    public Response getAdminParams(String sha1) {
        Response response;
        try {
            LOGGER.debug("called getDescription_html()...");
            String pass = GrobidServiceProperties.getAdminPw();
            if (StringUtils.isNotBlank(pass) && StringUtils.isNotBlank(sha1)
                    && pass.equals(SHA1.getSHA1(sha1))) {
                StringBuilder htmlCode = new StringBuilder();

                htmlCode.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
                htmlCode.append("<html>");
                htmlCode.append("<head>");
                htmlCode.append("<title>grobid-service - admin</title>");
                htmlCode.append("</head>");
                htmlCode.append("<body>");
                htmlCode.append("<table border=\"1\">");
                htmlCode.append("<tr><td>property</td><td>value</td></tr>");
                htmlCode.append("<th><td colspan=\"2\">java properties</td></th>");
                htmlCode.append("<tr><td>os name</td><td>").append(System.getProperty("os.name")).append("</td></tr>");
                htmlCode.append("<tr><td>os version</td><td>").append(System.getProperty("sun.arch.data.model")).append("</td></tr>");
                htmlCode.append("<th><td colspan=\"2\">grobid_service.properties</td></th>");

                Properties props = GrobidServiceProperties.getProps();
                for (Object property : props.keySet()) {
                    htmlCode.append("<tr><td>").append(property).append("</td><td>").append(props.getProperty((String) property)).append("</td></tr>");
                }
                htmlCode.append("<th><td colspan=\"2\">grobid.properties</td></th>");
                props = GrobidProperties.getProps();
                for (Object property : props.keySet()) {
                    htmlCode.append("<tr><td>").append(property).append("</td><td>").append(props.getProperty((String) property)).append("</td></tr>");
                }

                htmlCode.append("</table>");
                htmlCode.append("</body>");
                htmlCode.append("</html>");

                response = Response.status(Status.OK)
                        .entity(htmlCode.toString()).type(MediaType.TEXT_HTML)
                        .build();
            } else {
                throw new GrobidServiceException("Invalid credentials", Status.FORBIDDEN);
            }
        } catch (Exception e) {
            throw new GrobidServiceException("Cannot response the description for grobid-service. ", e, Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Process SHA1.
     *
     * @param sha1 string to hash
     * @return Response containing the value hashed.
     */
    public Response processSHA1(String sha1) {
        LOGGER.debug(">> processSHA1");
        Response response;
        String retVal;

        retVal = SHA1.getSHA1(sha1);
        if (GrobidRestUtils.isResultNullOrEmpty(retVal)) {
            response = Response.status(Status.OK).entity(retVal)
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            response = Response.status(Status.NO_CONTENT).build();
        }
        LOGGER.debug("<< processSHA1");
        return response;
    }

    /**
     * Return all properties key/value/type in xml format.
     *
     * @param passwordPlain password
     * @return Response containing the properties.
     */
    public Response getAllPropertiesValues(String passwordPlain) {
        LOGGER.debug(">> getAllPropertiesValues");
        Response response;
        String retVal;
        if (StringUtils.equals(GrobidServiceProperties.getAdminPw(),
                SHA1.getSHA1(passwordPlain))) {
            retVal = GrobidPropertiesUtil.getAllPropertiesListXml();
            response = Response.status(Status.OK).entity(retVal)
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            throw new GrobidServiceException("Invalid credentials. ", Status.FORBIDDEN);
        }
        LOGGER.debug("<< getAllPropertiesValues");
        return response;
    }

    /**
     * Dynamically update the value of the property given in XML input file.
     * Also update the property file.
     *
     * @param pXml the xml containing the changes. Xml has to follow that schema:
     *             <code>
     *             <changeProperty>
     *             <password>password</password>
     *             <property>
     *             <key>key</key>
     *             <value>value</value>
     *             <type>type</type>
     *             </property>
     *             </changeProperty>
     *             </code>
     * @return the changed value if processing was a success. HTTP error code
     * else.
     */
    public Response changePropertyValue(String pXml) {
        LOGGER.debug(">> changePropertyValue");
        Response response;
        String result;
        ChangePropertyParser parser = new ChangePropertyParser(pXml);
        if (StringUtils.equals(GrobidServiceProperties.getAdminPw(),
                SHA1.getSHA1(parser.getPassword()))) {

            if (parser.getKey().contains("org.grobid.service")) {
                if (StringUtils.equals(parser.getType(),
                        GrobidProperty.TYPE.PASSWORD.toString())) {
                    String newPwd = SHA1.getSHA1(parser.getValue());
                    GrobidServiceProperties.updatePropertyFile(
                            parser.getKey(), newPwd);
                    GrobidServiceProperties.reload(configuration);
                    result = newPwd;
                } else {
                    GrobidServiceProperties.updatePropertyFile(
                            parser.getKey(), parser.getValue());
                    GrobidServiceProperties.reload(configuration);
                    result = GrobidServiceProperties.getProps().getProperty(parser.getKey(), StringUtils.EMPTY);
                }

            } else {
                GrobidProperties.setPropertyValue(parser.getKey(),
                        parser.getValue());
                GrobidProperties.updatePropertyFile(parser.getKey(),
                        parser.getValue());
                GrobidProperties.reload();
                result = GrobidProperties.getProps().getProperty(parser.getKey(), StringUtils.EMPTY);
            }
            response = Response.status(Status.OK).entity(result)
                    .type(MediaType.TEXT_PLAIN).build();

        } else {
            throw new GrobidServiceException("Invalid credentials. ", Status.FORBIDDEN);
        }
        LOGGER.debug("<< changePropertyValue");
        return response;
    }
}
