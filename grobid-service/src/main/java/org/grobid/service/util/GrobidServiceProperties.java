package org.grobid.service.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.core.utilities.Utilities;
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
public class GrobidServiceProperties {

	/**
	 * The Logger.
	 */
	public static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidServiceProperties.class);

	/**
	 * Internal property object, where all properties are defined.
	 */
	protected static Properties props = null;

	/**
	 * Path to grobid_service.property.
	 */
	protected static File GROBID_SERIVCE_PROPERTY_PATH = null;

	/**
	 * The context of the application.
	 */
	protected static Context context;

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
			return getNewInstance();
		else
			return grobidServiceProperties;
	}
	
	/**
	 * Reload GrobidServiceProperties.
	 */
	public static void reload() {
		getNewInstance();
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
	protected static synchronized GrobidServiceProperties getNewInstance() {
		LOGGER.debug("Start GrobidServiceProperties.getNewInstance");
		try {
			grobidServiceProperties = new GrobidServiceProperties();
		} catch (NamingException nexp) {
			throw new GrobidPropertyException(
					"Could not get the initial context", nexp);
		}
		return grobidServiceProperties;
	}

	/**
	 * Returns all grobid-properties.
	 * 
	 * @return properties object
	 */
	public static Properties getProps() {
		return props;
	}

	/**
	 * @param props
	 *            the props to set
	 */
	protected static void setProps(Properties pProps) {
		props = pProps;
	}

	/**
	 * Return the context.
	 * 
	 * @return the context.
	 */
	public static Context getContext() {
		return context;
	}

	/**
	 * Set the context.
	 * 
	 * @param pContext
	 *            the context.
	 */
	public static void setContext(Context pContext) {
		context = pContext;
	}

	/**
	 * Loads all properties given in property file {@link #GROBID_HOME_PATH}.
	 */
	protected static void init() {
		LOGGER.debug("Initiating property loading");
		try {
			setContext(new InitialContext());
		} catch (NamingException nexp) {
			throw new GrobidPropertyException(
					"Could not get the initial context", nexp);
		}
	}

	/**
	 * Initializes a {@link GrobidServiceProperties} object by reading the
	 * property file.
	 * 
	 * @throws NamingException
	 * 
	 */
	public GrobidServiceProperties() throws NamingException {
		LOGGER.debug("Instanciating GrobidServiceProperties");
		init();
		setProps(new Properties());
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
			GROBID_SERIVCE_PROPERTY_PATH = grobidServicePropFile
					.getCanonicalFile();
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

		// prevent NullPointerException if GrobidProperties is not yet
		// instantiated
		if (GrobidProperties.getGrobidHomePath() == null) {
			GrobidProperties.getInstance();
		}
		GrobidProperties.setContextExecutionServer(true);
	}

	public static File getGrobidPropertiesPath() {
		return GROBID_SERIVCE_PROPERTY_PATH;
	}

	/**
	 * Return the value corresponding to the property key. If this value is
	 * null, return the default value.
	 * 
	 * @param pkey
	 *            the property key
	 * @return the value of the property.
	 */
	protected static String getPropertyValue(String pkey) {
		return getProps().getProperty(pkey);
	}

	/**
	 * Return the value corresponding to the property key. If this value is
	 * null, return the default value.
	 * 
	 * @param pkey
	 *            the property key
	 * @return the value of the property.
	 */
	public static void setPropertyValue(String pkey, String pValue) {
		if (StringUtils.isBlank(pValue))
			throw new GrobidPropertyException("Cannot set property '" + pkey
					+ "' to null or empty.");
		getProps().put(pkey, pValue);
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

	/**
	 * Returns the password for admin page given by property
	 * {@value #PROP_GROBID_SERVICE_ADMIN_PW}.
	 * 
	 * @return if the execution is parallel
	 */
	public static boolean isParallelExec() {
		return Utilities
				.stringToBoolean(getPropertyValue(GrobidPropertyKeys.PROP_GROBID_SERVICE_IS_PARALLEL_EXEC));
	}

	/**
	 * Update grobid.properties with the key and value given as argument.
	 * 
	 * @param pKey
	 *            key to replace
	 * @param pValue
	 *            value to replace
	 * @throws IOException
	 */
	public static void updatePropertyFile(String pKey, String pValue)
			throws IOException {
		GrobidProperties.updatePropertyFile(getGrobidPropertiesPath(), pKey,
				pValue);
	}

}
