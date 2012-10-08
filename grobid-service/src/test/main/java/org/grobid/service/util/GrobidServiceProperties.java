package org.grobid.service.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.grobid.core.exceptions.GrobidPropertyException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.service.exceptions.GrobidServicePropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles all properties, which can be set for the grobid-service
 * project. it is directly extended by the {@link GrobidProperties} class and
 * therefore also contains all properties neccessary for the grobid-core
 * project. A file defining properties for grobid-service must have the name '
 * {@value #FILE_GROBID_SERVICE_PROPERTIES}' and can be contained in path either
 * located by the system property {@value GrobidProperties#PROP_GROBID_HOME} or
 * the system property {@value #PROP_GROBID_HOME} or given by context property
 * (retrieved via InitialContext().lookup(...)). If both are set this class will
 * try to load the file in {@value GrobidProperties#PROP_GROBID_HOME} first.
 * 
 * @author Florian Zipser
 * 
 */
public class GrobidServiceProperties extends GrobidProperties {

	/**
	 * The Logger.
	 */
	public static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidServiceProperties.class);

	/**
	 * A static {@link GrobidProperties} object containing all properties used
	 * by grobid.
	 */
	private static GrobidServiceProperties grobidServiceProperties = null;

	/**
	 * Returns a static {@link GrobidServiceProperties} object. If no one is
	 * set, than it creates one. {@inheritDoc #GrobidServiceProperties()}
	 * 
	 * @return
	 */
	public static GrobidServiceProperties getInstance() {
		if (grobidServiceProperties == null)
			return (getNewInstance());
		else
			return (grobidServiceProperties);
	}

	/**
	 * Creates a new {@link GrobidServiceProperties} object, initializes it and
	 * returns it. {@inheritDoc #GrobidServiceProperties()} First checks to find
	 * the grobid home folder by resolving the given context. When no context
	 * properties exist, The detection will be given to
	 * {@link GrobidProperties#detectGrobidHomePath()}.
	 * 
	 * @return
	 */
	public static synchronized GrobidServiceProperties getNewInstance() {
		try {
			grobidServiceProperties = new GrobidServiceProperties();
		} catch (NamingException nexp) {
			throw new GrobidPropertyException(
					"Could not get the initial context", nexp);
		}
		return grobidServiceProperties;
	}

	/**
	 * Initializes a {@link GrobidServiceProperties} object by reading the
	 * property file.
	 * 
	 * @throws NamingException
	 * 
	 */
	public GrobidServiceProperties() throws NamingException {
		super(new InitialContext(null));
		LOGGER.debug("Instanciating GrobidServiceProperties");
		String grobidServicePath;
		try {
			grobidServicePath = (String) context.lookup("java:comp/env/"
					+ GrobidPropertyKeys.PROP_GROBID_SERVICE_PROPERTY);
		} catch (Exception exp) {
			throw new GrobidServicePropertyException(
					"Could not load the path to grobid_serive.properties from the context",
					exp);
		}
		File grobidServicePropFile = new File(grobidServicePath);

		// exception if prop file does not exist
		if (grobidServicePropFile == null || !grobidServicePropFile.exists()) {
			throw new GrobidServicePropertyException(
					"Could not read grobid_service.properties, the file '"
							+ grobidServicePropFile + "' does not exist.");
		}

		// load server properties and copy them to this properties
		try {
			Properties serviceProps = new Properties();
			serviceProps.load(new FileInputStream(grobidServicePropFile));
			getProps().putAll(serviceProps);
		} catch (FileNotFoundException e) {
			throw new GrobidServicePropertyException(
					"Cannot load properties from file " + grobidServicePropFile
							+ "''.");
		} catch (IOException e) {
			throw new GrobidServicePropertyException(
					"Cannot load properties from file " + grobidServicePropFile
							+ "''.");
		}
	}

	/**
	 * Returns the password for admin page given by property
	 * {@value #PROP_GROBID_SERVICE_ADMIN_PW}.
	 * 
	 * @return password for admin page
	 */
	public static String getAdminPw() {
		return getPropertyValue(GrobidPropertyKeys.PROP_GROBID_SERVICE_ADMIN_PW);
	}

}
