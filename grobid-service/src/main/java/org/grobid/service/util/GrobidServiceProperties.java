package org.grobid.service.util;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.core.utilities.Utilities;
import org.grobid.service.GrobidServiceConfiguration;
import org.grobid.service.exceptions.GrobidServicePropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * This class handles all properties, which can be set for the grobid-service
 * project. it is directly extended by the {@link GrobidProperties} class and
 * therefore also contains all properties neccessary for the grobid-core
 * project. A file defining properties for grobid-service must have the name '
 * FILE_GROBID_SERVICE_PROPERTIES' and can be contained in path either
 * located by the system property {@value GrobidPropertyKeys#PROP_GROBID_HOME} or
 * the system property {@value GrobidPropertyKeys#PROP_GROBID_HOME} or given by context property
 * (retrieved via InitialContext().lookup(...)). If both are set this class will
 * try to load the file in {@value GrobidPropertyKeys#PROP_GROBID_HOME} first.
 *
 * @author Florian Zipser
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
     * A static {@link GrobidProperties} object containing all properties used
     * by grobid.
     */
    private static GrobidServiceProperties grobidServiceProperties = null;
    private GrobidServiceConfiguration configuration;

    /**
     * Returns a static {@link GrobidServiceProperties} object. If no one is
     * set, than it creates one. {@inheritDoc #GrobidServiceProperties()}
     *
     * @return
     */
    public static GrobidServiceProperties getInstance(GrobidServiceConfiguration configuration) {
        if (grobidServiceProperties == null)
            return getNewInstance(configuration);
        else
            return grobidServiceProperties;
    }

    /**
     * Reload GrobidServiceProperties.
     */
    public static void reload(GrobidServiceConfiguration configuration) {
        getNewInstance(configuration);
    }

    /**
     * Creates a new {@link GrobidServiceProperties} object, initializes it and
     * returns it. {@inheritDoc #GrobidServiceProperties()} First checks to find
     * the grobid home folder by resolving the given context. When no context
     * properties exist, The detection will be given to
     *
     * @return
     */
    protected static synchronized GrobidServiceProperties getNewInstance(GrobidServiceConfiguration configuration) {
        LOGGER.debug("Start GrobidServiceProperties.getNewInstance");
        grobidServiceProperties = new GrobidServiceProperties(configuration);
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

    protected static void setProps(Properties pProps) {
        props = pProps;
    }


    protected static void init() {
    }

    /**
     * Initializes a {@link GrobidServiceProperties} object by reading the
     * property file.
     */
    public GrobidServiceProperties(GrobidServiceConfiguration configuration) {
        this.configuration = configuration;
        LOGGER.debug("Instantiating GrobidServiceProperties");
        init();
        setProps(new Properties());
//		String grobidServicePath;
//		try {
//			grobidServicePath = (String) context.lookup("java:comp/env/"
//					+ GrobidPropertyKeys.PROP_GROBID_SERVICE_PROPERTY);
//		} catch (Exception exp) {
//			throw new GrobidServicePropertyException(
//					"Could not load the path to grobid_service.properties from the context",
//					exp);
//		}
        File grobidServicePropFile = new File(configuration.getGrobid().getGrobidServiceProperties()).getAbsoluteFile();

        // exception if prop file does not exist
        if (!grobidServicePropFile.exists()) {
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
     * @param pkey the property key
     * @return the value of the property.
     */
    protected static String getPropertyValue(String pkey) {
        return getProps().getProperty(pkey);
    }

    /**
     * Return the value corresponding to the property key. If this value is
     * null, return the default value.
     *
     * @param pkey the property key
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
     *
     * @return password for admin page
     */
    public static String getAdminPw() {
        return getPropertyValue(GrobidPropertyKeys.PROP_GROBID_SERVICE_ADMIN_PW);
    }

    /**
     * Returns the password for admin page given by property
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
     * @param pKey   key to replace
     * @param pValue value to replace
     * @throws IOException
     */
    public static void updatePropertyFile(String pKey, String pValue) {
        GrobidProperties.updatePropertyFile(getGrobidPropertiesPath(), pKey,
                pValue);
    }

}
