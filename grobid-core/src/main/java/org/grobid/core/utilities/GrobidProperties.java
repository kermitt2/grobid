package org.grobid.core.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.utilities.counters.CntManager;
import org.grobid.core.utilities.counters.impl.CntManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class loads contains all names of grobid-properties and provide methods
 * to load grobid-properties from a property file. Each property will be copied
 * to a system property having the same name.
 * 
 * @author Florian Zipser
 * @version 1.2
 */
public class GrobidProperties {

	public static final Logger LOGGER = LoggerFactory.getLogger(GrobidProperties.class);

	/**
	 * The context of the application.
	 */
	protected static Context context;

    private CntManager cntManager = CntManagerFactory.getCntManager();

	/**
	 * name of property which determines, if grobid runs in test mode.
	 */
	public static final String PROP_TEST_MODE = "grobid.testMode";

	public static final String FILE_ENDING_TEI_HEADER = ".header.tei.xml";
	public static final String FILE_ENDING_TEI_FULLTEXT = ".fulltext.tei.xml";

	public static final String FOLDER_NAME_MODELS = "models";
	public static final String FILE_NAME_MODEL = "model";

	/**
	 * A static {@link GrobidProperties} object containing all properties used
	 * by grobid.
	 */
	private static GrobidProperties grobidProperties = null;

    /**
     * Type of CRF framework used
     */
    private static GrobidCRFEngine grobidCRFEngine = GrobidCRFEngine.WAPITI;


	/**
	 * Path to pdf2xml.
	 */
	private static File pathToPdf2Xml = null;

	/**
	 * Determines the path of grobid-home for all objects of this class. When
	 * #GROBID_HOME_PATH is set, all created objects will refer to that
	 * path. When it is reset, old object refer to the old path whereas objects
	 * created after reset will refer to the new path.
	 */
	protected static File GROBID_HOME_PATH = null;

	/**
	 * Path to grobid.property.
	 */
	protected static File GROBID_PROPERTY_PATH = null;

	/**
	 * Internal property object, where all properties are defined.
	 */
	protected static Properties props = null;

	/**
	 * Resets this class and all its static fields. For instance sets the
	 * current object to null.
	 */
	public static void reset() {
		grobidProperties = null;
		props = null;
		GROBID_HOME_PATH = null;
		GROBID_PROPERTY_PATH = null;
	}

	/**
	 * Returns a static {@link GrobidProperties} object. If no one is set, then
	 * it creates one. {@inheritDoc #GrobidProperties()}
	 * 
	 * @return
	 */
	public static GrobidProperties getInstance() {
		if (grobidProperties == null) {
			return getNewInstance();
        } else {
			return grobidProperties;
        }
	}

	/**
	 * Reload GrobidServiceProperties.
	 */
	public static void reload() {
		getNewInstance();
	}

	/**
	 * Creates a new {@link GrobidProperties} object, initializes it and returns
	 * it. {@inheritDoc #GrobidProperties()}
	 * 
	 * @return GrobidProperties
	 */
	protected static synchronized GrobidProperties getNewInstance() {
		LOGGER.debug("synchronized getNewInstance");
		grobidProperties = new GrobidProperties();
		return grobidProperties;
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
	 * @param pProps
	 *            the props to set
	 */
	protected static void setProps(final Properties pProps) {
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
	public static void setContext(final Context pContext) {
		context = pContext;
	}

	/**
	 * Load the path to GROBID_HOME from the env-entry set in web.xml.
	 */
	public static void load_GROBID_HOME_PATH() {
		LOGGER.debug("loading GROBID_HOME path");

		if (GROBID_HOME_PATH == null) {
			String grobidHomePath;
			try {
				grobidHomePath = (String) context.lookup("java:comp/env/" + GrobidPropertyKeys.PROP_GROBID_HOME);
			} catch (final Exception exp) {
				throw new GrobidPropertyException("Could not set GROBID_HOME", exp);
			}
			File pathToGrobidHome = new File(grobidHomePath);

			try {
				if (!pathToGrobidHome.exists()) {
					LOGGER.error("Cannot set grobid home path to the given one '{}', because it does not exist.", grobidHomePath);
					throw new GrobidPropertyException("Cannot set grobid home path to the given one '" + grobidHomePath
							+ "', because it does not exist.");
				}

			} catch (final SecurityException scExp) {
				throw new GrobidPropertyException("Cannot access the set grobid home path '" + grobidHomePath
						+ "', because of an access permission.", scExp);
			}
			try {
				GROBID_HOME_PATH = pathToGrobidHome.getCanonicalFile();
			} catch (final IOException ioExp) {
				throw new GrobidPropertyException("Cannot set grobid home path to the given one '" + grobidHomePath
						+ "', because it does not exist.");
			}
		}
	}

	/**
	 * Return the GROBID_HOME path.
	 * 
	 * @return grobid home path
	 */
	public static File get_GROBID_HOME_PATH() {
		return GROBID_HOME_PATH;
	}

	public static File getGrobidHomePath() {
		return GROBID_HOME_PATH;
	}

	/**
	 * Set the GROBID_HOME path.
	 *
	 */
	public static void set_GROBID_HOME_PATH(final String pGROBID_HOME_PATH) {
		if (StringUtils.isBlank(pGROBID_HOME_PATH))
			throw new GrobidPropertyException("Cannot set property '" + pGROBID_HOME_PATH + "' to null or empty.");

		File grobidHome = new File(pGROBID_HOME_PATH);
		// exception if prop file does not exist
		if (!grobidHome.exists()) {
			throw new GrobidPropertyException("Could not read GROBID_HOME, the directory '" + pGROBID_HOME_PATH + "' does not exist.");
		}

		try {
			GROBID_HOME_PATH = grobidHome.getCanonicalFile();
		} catch (IOException e) {
			throw new GrobidPropertyException("Cannot set grobid home path to the given one '" + pGROBID_HOME_PATH
					+ "', because it does not exist.");
		}
	}

	/**
	 * Load the path to grobid.properties from the env-entry set in web.xml.
	 */
	public static void loadGrobidPropertiesPath() {
		LOGGER.debug("loading grobid.properties");
		if (GROBID_PROPERTY_PATH == null) {
			String grobidPropertyPath;
			try {
				grobidPropertyPath = (String) context.lookup("java:comp/env/" + GrobidPropertyKeys.PROP_GROBID_PROPERTY);
			} catch (Exception exp) {
				throw new GrobidPropertyException("Could not load the path to grobid.properties from the context", exp);
			}
			File grobidPropertyFile = new File(grobidPropertyPath);

			// exception if prop file does not exist
			if (!grobidPropertyFile.exists()) {
				throw new GrobidPropertyException("Could not read grobid.properties, the file '" + grobidPropertyPath + "' does not exist.");
			}

			try {
				GROBID_PROPERTY_PATH = grobidPropertyFile.getCanonicalFile();
			} catch (IOException e) {
				throw new GrobidPropertyException("Cannot set grobid home path to the given one '" + grobidPropertyPath
						+ "', because it does not exist.");
			}
		}
	}

	/**
	 * Return the GROBID_HOME path.
	 * 
	 * @return grobid properties path
	 */
	public static File getGrobidPropertiesPath() {
		return GROBID_PROPERTY_PATH;
	}

	/**
	 * Set the GROBID_HOME path.
	 *
	 */
	public static void setGrobidPropertiesPath(final String pGrobidPropertiesPath) {
		if (StringUtils.isBlank(pGrobidPropertiesPath))
			throw new GrobidPropertyException("Cannot set property '" + pGrobidPropertiesPath + "' to null or empty.");

		File grobidPropPath = new File(pGrobidPropertiesPath);
		// exception if prop file does not exist
		if (!grobidPropPath.exists()) {
			throw new GrobidPropertyException("Could not read grobid.properties, the file '" + pGrobidPropertiesPath + "' does not exist.");
		}

		try {
			GROBID_PROPERTY_PATH = grobidPropPath.getCanonicalFile();
		} catch (IOException e) {
			throw new GrobidPropertyException("Cannot set grobid home path to the given one '" + pGrobidPropertiesPath
					+ "', because it does not exist.");
		}
	}

	/**
	 * Return the value corresponding to the property key. If this value is
	 * null, return the default value.
	 * 
	 * @param pkey
	 *            the property key
	 * @return the value of the property.
	 */
	protected static String getPropertyValue(final String pkey) {
		return getProps().getProperty(pkey);
	}

	/**
	 * Return the value corresponding to the property key. If this value is
	 * null, return the default value.
	 * 
	 * @param pkey
	 *            the property key
	 * @param pDefaultVal
	 *            the default value
	 * @return the value of the property, pDefaultVal else.
	 */
	protected static String getPropertyValue(final String pkey, final String pDefaultVal) {
		String prop = getProps().getProperty(pkey);
		return StringUtils.isNotBlank(prop) ? prop.trim() : pDefaultVal;
	}

	/**
	 * Return the value corresponding to the property key. If this value is
	 * null, return the default value.
	 * 
	 * @param pkey
	 *            the property key

	 */
	public static void setPropertyValue(final String pkey, final String pValue) {
		if (StringUtils.isBlank(pValue))
			throw new GrobidPropertyException("Cannot set property '" + pkey + "' to null or empty.");
		getProps().put(pkey, pValue);
	}

	/**
	 * Creates a new object and searches, where to find the grobid home folder.
	 * First step is to check if the system property GrobidPropertyKeys.PROP_GROBID_HOME
	 * is set, than the path matching to that property is used. Otherwise, the
	 * method will search a folder named #FILE_GROBID_PROPERTIES_PRIVATE
	 * , if this is is also not set, the method will search for a folder named
	 * FILE_GROBID_PROPERTIES in the current project (current project
	 * means where the system property <em>user.dir</em> points to.)
	 */
	public GrobidProperties() {
		init();
	}

	public GrobidProperties(final Context pContext) {
		init(pContext);
	}

	protected static void init(final Context pContext) {
		setContext(pContext);

		setProps(new Properties());

		load_GROBID_HOME_PATH();
		loadGrobidPropertiesPath();
		setContextExecutionServer(false);

		try {
			getProps().load(new FileInputStream(getGrobidPropertiesPath()));
		} catch (IOException exp) {
			throw new GrobidPropertyException("Cannot open file of grobid.properties at location'" + GROBID_PROPERTY_PATH.getAbsolutePath()
					+ "'", exp);
		} catch (Exception exp) {
			throw new GrobidPropertyException("Cannot open file of grobid properties" + getGrobidPropertiesPath().getAbsolutePath(), exp);
		}

		initializePaths();
		checkProperties();
		loadPdf2XMLPath();
        loadCrfEngine();
	}

    private static void loadCrfEngine() {
        grobidCRFEngine = GrobidCRFEngine.get(getPropertyValue(GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE, GrobidCRFEngine.WAPITI.name()));
    }


    /**
	 * Loads all properties given in property file {@link #GROBID_HOME_PATH}.
	 */
	protected static void init() {
		LOGGER.debug("Initiating property loading");

		Context ctxt;
		try {
			ctxt = new InitialContext();
		} catch (NamingException nexp) {
			throw new GrobidPropertyException("Could not get the initial context", nexp);
		}
		init(ctxt);
	}

	/**
	 * Initialize the different paths set in the configuration file
	 * grobid.properties.
	 */
	protected static void initializePaths() {
		Enumeration<?> properties = getProps().propertyNames();
		for (String propKey; properties.hasMoreElements();) {
			propKey = (String) properties.nextElement();
			String propVal = getPropertyValue(propKey, StringUtils.EMPTY);
			if (propKey.endsWith(".path")) {
				File path = new File(propVal);
				if (!path.isAbsolute()) {
					try {
						getProps().put(propKey,
								new File(get_GROBID_HOME_PATH().getAbsoluteFile(), path.getPath()).getCanonicalFile().toString());
					} catch (IOException e) {
						throw new GrobidResourceException("Cannot read the path of '" + propKey + "'.");
					}
				}
			}
		}

		// start: creating all necessary folders
		for (String path2create : GrobidPropertyKeys.PATHES_TO_CREATE) {
			String prop = getProps().getProperty(path2create);
			if (prop != null) {
				File path = new File(prop);
				if (!path.exists()) {
					LOGGER.debug("creating directory {}", path);
					if (!path.mkdirs())
						throw new GrobidResourceException("Cannot create the folder '" + path.getAbsolutePath() + "'.");
				}
			}
		}
		// end: creating all necessary folders
	}

	/**
	 * Checks if the given properties contains non-empty and non-null values for
	 * the properties of list Grobid properties
	 * 
	 */
	protected static void checkProperties() {
		LOGGER.debug("Checking Properties");
		Enumeration<?> properties = getProps().propertyNames();
		for (String propKey; properties.hasMoreElements();) {
			propKey = (String) properties.nextElement();
			String propVal = getPropertyValue(propKey, StringUtils.EMPTY);
			if (StringUtils.isBlank(propVal)) {
				throw new GrobidPropertyException("The property '" + propKey + "' is null or empty. Please set this value.");
			}
		}
	}

	/**
	 * Returns the temprorary path of grobid
	 * 
	 * @return a directory for temp files
	 */
	public static File getTempPath() {
		return new File(getPropertyValue(GrobidPropertyKeys.PROP_TMP_PATH, System.getProperty("java.io.tmpdir")));
	}

	public static void setNativeLibraryPath(final String nativeLibPath) {
		setPropertyValue(GrobidPropertyKeys.PROP_NATIVE_LIB_PATH, nativeLibPath);
	}

	/**
	 * Returns the content of property GrobidPropertyKeys.PROP_NATIVE_LIB_PATH as
	 * {@link File} object.
	 * 
	 * @return folder that contains all libraries
	 */
	public static File getNativeLibraryPath() {
		return new File(getPropertyValue(GrobidPropertyKeys.PROP_NATIVE_LIB_PATH));
	}

	/**
	 * Returns the id for a connection to crossref, given in the grobid-property
	 * file.
	 * 
	 * @return id for connecting crossref
	 */
	public static String getCrossrefId() {
		return getPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_ID);
	}

	/**
	 * Sets the id for a connection to crossref, given in the grobid-property
	 * file.
	 * 
	 * @param id
	 *            for connecting crossref
	 */
	public static void setCrossrefId(final String id) {
		setPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_ID, id);
	}

	/**
	 * Returns the password for a connection to crossref, given in the
	 * grobid-property file.
	 * 
	 * @return password for connecting crossref
	 */
	public static String getCrossrefPw() {
		return getPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_PW);
	}

	/**
	 * Sets the id for a connection to crossref, given in the grobid-property
	 * file.
	 * 
	 * @param password
	 *            for connecting crossref
	 */
	public static void setCrossrefPw(final String password) {
		setPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_PW, password);
	}

	/**
	 * Returns the host for a connection to crossref, given in the
	 * grobid-property file.
	 * 
	 * @return host for connecting crossref
	 */
	public static String getCrossrefHost() {
		return getPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_HOST);
	}

	/**
	 * Sets the id for a connection to crossref, given in the grobid-property
	 * file.
	 * 
	 * @param host
	 *            for connecting crossref
	 */
	public static void setCrossrefHost(final String host) {
		setPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_HOST, host);
	}

	/**
	 * Returns the port for a connection to crossref, given in the
	 * grobid-property file.
	 * 
	 * @return port for connecting crossref
	 */
	public static Integer getCrossrefPort() {
		return (Integer.valueOf(getPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_PORT)));
	}

	/**
	 * Sets the port for a connection to crossref, given in the grobid-property
	 * file.
	 * 
	 * @param port
	 *            for connecting crossref
	 */
	public static void setCrossrefPort(final String port) {
		setPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_PORT, port);
	}

	/**
	 * Returns the host for a proxy connection, given in the grobid-property
	 * file.
	 * 
	 * @return host for connecting crossref
	 */
	public static String getProxyHost() {
		return getPropertyValue(GrobidPropertyKeys.PROP_PROXY_HOST);
	}

	/**
	 * Sets the host a proxy connection, given in the grobid-property file.
	 * 
	 * @param host
	 *            for connecting crossref
	 */
	public static void setProxyHost(final String host) {
		setPropertyValue(GrobidPropertyKeys.PROP_PROXY_HOST, host);
	}

	/**
	 * Returns the port for a proxy connection, given in the grobid-property
	 * file.
	 * 
	 * @return port for connecting crossref
	 */
	public static Integer getProxyPort() {
		return Integer.valueOf(getPropertyValue(GrobidPropertyKeys.PROP_PROXY_PORT));
	}

	/**
	 * Sets the port for a proxy connection, given in the grobid-property file.
	 * 
	 * @param port
	 *            for connecting crossref
	 */
	public static void setProxyPort(final String port) {
		setPropertyValue(GrobidPropertyKeys.PROP_PROXY_PORT, port);
	}

	/**
	 * Returns the id for a connection to mysql, given in the grobid-property
	 * file.
	 * 
	 * @return database name for connecting mysql
	 */
	public static String getMySQLDBName() {
		return getPropertyValue(GrobidPropertyKeys.PROP_MYSQL_DB_NAME);
	}

	/**
	 * Sets the database name for a connection to mysql, given in the
	 * grobid-property file.
	 * 
	 * @param dbName
	 *            for connecting mysql
	 */
	public static void setMySQLDBName(final String dbName) {
		setPropertyValue(GrobidPropertyKeys.PROP_MYSQL_DB_NAME, dbName);
	}

	/**
	 * Returns the id for a connection to mysql, given in the grobid-property
	 * file.
	 * 
	 * @return username for connecting mysql
	 */
	public static String getMySQLUsername() {
		return getPropertyValue(GrobidPropertyKeys.PROP_MYSQL_USERNAME);
	}

    public static Integer getPdf2XMLMemoryLimitMb() {
        return Integer.parseInt(getPropertyValue(GrobidPropertyKeys.PROP_3RD_PARTY_PDF2XML_MEMORY_LIMIT, "2048"), 10);
    }

	/**
	 * Sets the username for a connection to mysql, given in the grobid-property
	 * file.
	 * 
	 * @param username
	 *            for connecting mysql
	 */
	public static void setMySQLUsername(final String username) {
		setPropertyValue(GrobidPropertyKeys.PROP_MYSQL_USERNAME, username);
	}

	/**
	 * Returns the password for a connection to mysql, given in the
	 * grobid-property file.
	 * 
	 * @return password for connecting mysql
	 */
	public static String getMySQLPw() {
		return getPropertyValue(GrobidPropertyKeys.PROP_MYSQL_PW);
	}

	/**
	 * Sets the id for a connection to mysql, given in the grobid-property file.
	 * 
	 * @param password
	 *            for connecting mysql
	 */
	public static void setMySQLPw(final String password) {
		setPropertyValue(GrobidPropertyKeys.PROP_MYSQL_PW, password);
	}

	/**
	 * Returns the host for a connection to mysql, given in the grobid-property
	 * file.
	 * 
	 * @return host for connecting mysql
	 */
	public static String getMySQLHost() {
		return getPropertyValue(GrobidPropertyKeys.PROP_MYSQL_HOST);
	}

	/**
	 * Sets the id for a connection to mysql, given in the grobid-property file.
	 * 
	 * @param host
	 *            for connecting mysql
	 */
	public static void setMySQLHost(final String host) {
		setPropertyValue(GrobidPropertyKeys.PROP_MYSQL_HOST, host);
	}

	/**
	 * Returns the port for a connection to mysql, given in the grobid-property
	 * file.
	 * 
	 * @return port for connecting mysql
	 */
	public static Integer getMySQLPort() {
		return Integer.valueOf(getPropertyValue(GrobidPropertyKeys.PROP_MYSQL_PORT));
	}

	/**
	 * Sets the port for a connection to mysql, given in the grobid-property
	 * file.
	 * 
	 * @param port
	 *            for connecting mysql
	 */
	public static void setMySQLPort(String port) {
		setPropertyValue(GrobidPropertyKeys.PROP_MYSQL_PORT, port);
	}

	/**
	 * Returns the number of threads, given in the grobid-property file.
	 * 
	 * @return number of threads
	 */
	public static Integer getNBThreads() {
		return Integer.valueOf(getPropertyValue(GrobidPropertyKeys.PROP_NB_THREADS));
	}



	/**
	 * Sets the number of threads, given in the grobid-property file.
	 * 
	 * @param nbThreads
	 *            umber of threads
	 */
	public static void setNBThreads(final String nbThreads) {
		setPropertyValue(GrobidPropertyKeys.PROP_MYSQL_PORT, nbThreads);
	}

	/**
	 * Returns if a language id shall be used, given in the grobid-property
	 * file.
	 * 
	 * @return true if a language id shall be used
	 */
	public static Boolean isUseLanguageId() {
		return Utilities.stringToBoolean(getPropertyValue(GrobidPropertyKeys.PROP_USE_LANG_ID));
	}

	public static String getLanguageDetectorFactory() {
		String factoryClassName = getPropertyValue(GrobidPropertyKeys.PROP_LANG_DETECTOR_FACTORY);
		if (isUseLanguageId() && (StringUtils.isBlank(factoryClassName))) {
			throw new GrobidPropertyException("Language detection is enabled but a factory class name is not provided");
		}
		return factoryClassName;
	}

	/**
	 * Sets if a language id shall be used, given in the grobid-property file.
	 * 
	 * @param useLanguageId
	 *            true, if a language id shall be used
	 */
	public static void setUseLanguageId(final String useLanguageId) {
		setPropertyValue(GrobidPropertyKeys.PROP_USE_LANG_ID, useLanguageId);
	}

	/**
	 * Returns if resources like firstnames, lastnames and countries are
	 * supposed to be read from grobid-home folder, given in the grobid-property
	 * file.
	 * 
	 * @return true if a language id shall be used
	 */
	public static Boolean isResourcesInHome() {
		return Utilities.stringToBoolean(getPropertyValue(GrobidPropertyKeys.PROP_RESOURCE_INHOME, "true"));
	}

	/**
	 * Sets if resources like firstnames, lastnames and countries are supposed
	 * to be read from grobid-home folder, given in the grobid-property file.
	 * 
	 * @param resourceInHome
	 *            true, if a language id shall be used
	 */
	public static void setResourcesInHome(final String resourceInHome) {
		setPropertyValue(GrobidPropertyKeys.PROP_RESOURCE_INHOME, resourceInHome);
	}

	/**
	 * Returns the path to the home folder of pdf2xml.
	 *
	 */
	public static void loadPdf2XMLPath() {
		LOGGER.debug("loading pdf2xml path");
		String pathName = getPropertyValue(GrobidPropertyKeys.PROP_3RD_PARTY_PDF2XML);

		pathToPdf2Xml = new File(pathName);
		if (!pathToPdf2Xml.exists()) {
			throw new GrobidPropertyException(
					"Path to 3rd party program pdf2xml doesn't exists. Please set the path to pdf2xml in the file grobid.properties with the property grobid.3rdparty.pdf2xml");
		}

		pathToPdf2Xml = new File(pathToPdf2Xml, Utilities.getOsNameAndArch());

		LOGGER.debug("pdf2xml home directory set to " + pathToPdf2Xml.getAbsolutePath());
	}

	/**
	 * Returns the path to the home folder of pdf2xml.
	 * 
	 * @return path to pdf2xml
	 */
	public static File getPdf2XMLPath() {
		return pathToPdf2Xml;
	}

    public static GrobidCRFEngine getGrobidCRFEngine() {
        return grobidCRFEngine;
    }

    public static File getModelPath(final GrobidModels model) {
        return new File(get_GROBID_HOME_PATH(), FOLDER_NAME_MODELS + File.separator 
			+ model.getFolderName() + File.separator
            + FILE_NAME_MODEL + "." + grobidCRFEngine.getExt());
    }

    public static File getTemplatePath(final File resourcesDir, final GrobidModels model) {
		File theFile = new File(resourcesDir, "dataset/" + model.getFolderName() 
			+ "/crfpp-templates/" + model.getTemplateName());
		if (!theFile.exists()) {
			theFile = new File("resources/dataset/" + model.getFolderName() 
			+ "/crfpp-templates/" + model.getTemplateName());
		}
		return theFile;
	}

	public static File getEvalCorpusPath(final File resourcesDir, final GrobidModels model) {
		File theFile = new File(resourcesDir, "dataset/" + model.getFolderName() + "/evaluation/");
		if (!theFile.exists()) {
			theFile = new File("resources/dataset/" + model.getFolderName() + "/evaluation/");
		}
		return theFile;
	}

	public static File getCorpusPath(final File resourcesDir, final GrobidModels model) {
		File theFile = new File(resourcesDir, "dataset/" + model.getFolderName() + "/corpus");
		if (!theFile.exists()) {
			theFile = new File("resources/dataset/" + model.getFolderName() + "/corpus");
		}
		return theFile;
	}

	public static String getLexiconPath() {
		return new File(get_GROBID_HOME_PATH(), "lexicon").getAbsolutePath();
	}

    public static File getLanguageDetectionResourcePath() {
        return new File(get_GROBID_HOME_PATH(), "language-detection");
    };

	/**
	 * Returns the maximum parallel connections allowed in the pool.
	 * 
	 * @return the number of connections
	 */
	public static int getMaxPoolConnections() {
		return Integer.parseInt(getPropertyValue(GrobidPropertyKeys.PROP_GROBID_MAX_CONNECTIONS));
	}

	/**
	 * Returns maximum time to wait before timeout when the pool is full.
	 * 
	 * @return time to wait in milliseconds.
	 */
	public static int getPoolMaxWait() {
		return Integer.parseInt(getPropertyValue(GrobidPropertyKeys.PROP_GROBID_POOL_MAX_WAIT)) * 1000;
	}

	/**
	 * Returns if the execution context is stand alone or server.
	 * 
	 * @return the context of execution. Return false if the property value is
	 *         not readable.
	 */
	public static Boolean isContextExecutionServer() {
		return Utilities.stringToBoolean(getPropertyValue(GrobidPropertyKeys.PROP_GROBID_IS_CONTEXT_SERVER, "false"));
	}

	/**
	 * Set if the execution context is stand alone or server.
	 * 
	 * @param state
	 *            true to set the context of execution to server, false else.
	 */
	public static void setContextExecutionServer(Boolean state) {
		setPropertyValue(GrobidPropertyKeys.PROP_GROBID_IS_CONTEXT_SERVER, state.toString());
	}

	/**
	 * Update the input file with the key and value given as argument.
	 * 
	 * @param pPropertyFile
	 *            file to update.
	 * 
	 * @param pKey
	 *            key to replace
	 * @param pValue
	 *            value to replace
	 * @throws IOException
	 */
	public static void updatePropertyFile(File pPropertyFile, String pKey, String pValue) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(pPropertyFile));
		String line, content = StringUtils.EMPTY, lineToReplace = StringUtils.EMPTY;
		while ((line = reader.readLine()) != null) {
			if (line.contains(pKey)) {
				lineToReplace = line;
			}
			content += line + "\r\n";
		}
		reader.close();

		if (!StringUtils.EMPTY.equals(lineToReplace)) {
			String newContent = content.replaceAll(lineToReplace, pKey + "=" + pValue);
			FileWriter writer = new FileWriter(pPropertyFile.getAbsoluteFile());
			writer.write(newContent);
			writer.close();
		}
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
	public static void updatePropertyFile(String pKey, String pValue) throws IOException {
		updatePropertyFile(getGrobidPropertiesPath(), pKey, pValue);
	}

	/**
	 * Returns the current version of GROBID, given in the grobid-property
	 * file, and set by maven.
	 * 
	 * @return GROBID version
	 */
	public static String getVersion() {
		return getPropertyValue(GrobidPropertyKeys.PROP_VERSION);
	}

	/**
	 * Sets the GROBID version.
	 * 
	 * @param version
	 */
	public static void setVersion(final String version) {
		setPropertyValue(GrobidPropertyKeys.PROP_VERSION, version);
	}

}