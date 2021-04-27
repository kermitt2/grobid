package org.grobid.core.utilities;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModel;
import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.grobid.core.exceptions.GrobidPropertyException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.Consolidation.GrobidConsolidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class loads contains all names of grobid-properties and provide methods
 * to load grobid-properties from a property file. Each property will be copied
 * to a system property having the same name.
 *
 * @author Florian Zipser, Patrice
 */
public class GrobidProperties {
    public static final Logger LOGGER = LoggerFactory.getLogger(GrobidProperties.class);

    public static final String FILE_ENDING_TEI_HEADER = ".header.tei.xml";
    public static final String FILE_ENDING_TEI_FULLTEXT = ".fulltext.tei.xml";

    static final String FOLDER_NAME_MODELS = "models";
    static final String FILE_NAME_MODEL = "model";
    private static final String GROBID_VERSION_FILE = "/grobid-version.txt";
    static final String UNKNOWN_VERSION_STR = "unknown";

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
     * Default consolidation service, if used
     */
    private static GrobidConsolidationService consolidationService = GrobidConsolidationService.CROSSREF;

    /**
     * Path to pdf to xml converter.
     */
    private static File pathToPdfToXml = null;

    /**
     * Determines the path of grobid-home for all objects of this class. When
     * #GROBID_HOME_PATH is set, all created objects will refer to that
     * path. When it is reset, old object refer to the old path whereas objects
     * created after reset will refer to the new path.
     */
    static File GROBID_HOME_PATH = null;

    private static String GROBID_VERSION = null;

    /**
     * Path to grobid.property.
     */
    static File GROBID_PROPERTY_PATH = null;

    /**
     * Internal property object, where all properties are defined.
     */
    private static Properties props = null;
    private static String pythonVirtualEnv = "";

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
     * Returns an instance of {@link GrobidProperties} object. If no one is set, then
     * it creates one. {@inheritDoc #GrobidProperties()}
     */
    public static GrobidProperties getInstance() {
        if (grobidProperties == null) {
            return getNewInstance();
        } else {
            return grobidProperties;
        }
    }

    /**
     * Returns an instance of {@link GrobidProperties} object based on a custom grobid-home directory.
     * If no one is set, then it creates one. {@inheritDoc #GrobidProperties()}
     */
    public static GrobidProperties getInstance(GrobidHomeFinder grobidHomeFinder) {
        synchronized (GrobidProperties.class) {
            GROBID_HOME_PATH = grobidHomeFinder.findGrobidHomeOrFail();
        }
        return getInstance();
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
     * @param pProps the props to set
     */
    private static void setProps(final Properties pProps) {
        props = pProps;
    }


    /**
     * Load the path to GROBID_HOME from the env-entry set in web.xml.
     */
    private static void assignGrobidHomePath() {
        if (GROBID_HOME_PATH == null) {
            synchronized (GrobidProperties.class) {
                if (GROBID_HOME_PATH == null) {
                    GROBID_HOME_PATH = new GrobidHomeFinder().findGrobidHomeOrFail();
                }
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

    public static String getGrobidHome() {
        return GROBID_HOME_PATH.getPath();
    }

    /**
     * Set the GROBID_HOME path.
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
    @VisibleForTesting
    static void loadGrobidPropertiesPath() {
        LOGGER.debug("loading grobid.properties");
        if (GROBID_PROPERTY_PATH == null) {
            synchronized (GrobidProperties.class) {
                if (GROBID_PROPERTY_PATH == null) {
                    GROBID_PROPERTY_PATH = new GrobidHomeFinder().findGrobidPropertiesOrFail(GROBID_HOME_PATH);
                }
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
     * Return the value corresponding to the property key. If the properties are not initialised, it returns null
     *
     * @param pkey the property key
     * @return the value of the property.
     */
    protected static String getPropertyValue(final String pkey) {
        Properties props = getProps();
        if (props != null) {
            return props.getProperty(pkey);
        }
        return null;
    }

    /**
     * Return the value corresponding to the property key. If this value or the properties has not been loaded, is
     * null, return the default value.
     *
     * @param pkey        the property key
     * @param pDefaultVal the default value
     * @return the value of the property, pDefaultVal else.
     */
    protected static String getPropertyValue(final String pkey, final String pDefaultVal) {
        Properties props = getProps();
        if (props == null) {
            return pDefaultVal;
        }
        String prop = props.getProperty(pkey);
        return StringUtils.isNotBlank(prop) ? prop.trim() : pDefaultVal;
    }

    /**
     * Return the value corresponding to the property key. If this value is
     * null, return the default value.
     *
     * @param pkey the property key
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

    private void init() {
        setProps(new Properties());

        assignGrobidHomePath();
        loadGrobidPropertiesPath();
        setContextExecutionServer(false);

        try {
            getProps().load(new FileInputStream(getGrobidPropertiesPath()));
        } catch (IOException exp) {
            throw new GrobidPropertyException("Cannot open file of grobid.properties at location '" + GROBID_PROPERTY_PATH.getAbsolutePath()
                + "'", exp);
        } catch (Exception exp) {
            throw new GrobidPropertyException("Cannot open file of grobid properties " + getGrobidPropertiesPath().getAbsolutePath(), exp);
        }

        getProps().putAll(getEnvironmentVariableOverrides(System.getenv()));

        initializePaths();
        //checkProperties();
        loadPdf2XMLPath();
        loadCrfEngine();
    }

    /** Return the distinct values of all the engines that are needed */
    public static Set<GrobidCRFEngine> getDistinctModels() {
        final Set<GrobidCRFEngine> modelSpecificEngines = new HashSet<>(getModelSpecificEngines());
        modelSpecificEngines.add(getGrobidCRFEngine());

        return modelSpecificEngines;
    }

    /** Return the distinct values of all the engines specified in the individual model configuration in the property file **/
    public static Set<GrobidCRFEngine> getModelSpecificEngines() {
        return getProps().keySet().stream()
            .filter(k -> ((String) k).startsWith(GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE + '.'))
            .map(k -> GrobidCRFEngine.get(StringUtils.lowerCase(getPropertyValue((String) k))))
            .distinct()
            .collect(Collectors.toSet());
    }

    protected static void loadCrfEngine() {
        grobidCRFEngine = GrobidCRFEngine.get(getPropertyValue(GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE,
            GrobidCRFEngine.WAPITI.name()));
    }

    /**
     * Returns the current version of GROBID
     *
     * @return GROBID version
     */
    public static String getVersion() {
        if (GROBID_VERSION == null) {
            synchronized (GrobidProperties.class) {
                if (GROBID_VERSION == null) {
                    String grobidVersion = UNKNOWN_VERSION_STR;
                    try (InputStream is = GrobidProperties.class.getResourceAsStream(GROBID_VERSION_FILE)) {
                        grobidVersion = IOUtils.toString(is, "UTF-8");
                    } catch (IOException e) {
                        LOGGER.error("Cannot read Grobid version from resources", e);
                    }
                    GROBID_VERSION = grobidVersion;
                }
            }
        }
        return GROBID_VERSION;
    }

    protected static Map<String, String> getEnvironmentVariableOverrides(Map<String, String> environmentVariablesMap) {
        Map<String, String> properties = new EnvironmentVariableProperties(
            environmentVariablesMap, "(GROBID__|ORG__GROBID__).+"
        ).getProperties();
        LOGGER.info("environment variables overrides: {}", properties);
        return properties;
    }

    /**
     * Initialize the different paths set in the configuration file
     * grobid.properties.
     */
    protected static void initializePaths() {
        Enumeration<?> properties = getProps().propertyNames();
        for (String propKey; properties.hasMoreElements(); ) {
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
     */
    protected static void checkProperties() {
        LOGGER.debug("Checking Properties");
        Enumeration<?> properties = getProps().propertyNames();
        for (String propKey; properties.hasMoreElements(); ) {
            propKey = (String) properties.nextElement();
            if (propKey.equals("grobid.delft.python.virtualEnv"))
                continue;
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
     * Returns the installation path of DeLFT if set, null otherwise. It is required for using
     * a Deep Learning sequence labelling engine.
     *
     * @return folder that contains the local install of DeLFT
     */
    public static String getDeLFTPath() {
        return getPropertyValue(GrobidPropertyKeys.PROP_GROBID_DELFT_PATH);
    }

    public static String getDeLFTFilePath() {
        String rawPath = getPropertyValue(GrobidPropertyKeys.PROP_GROBID_DELFT_PATH);
        File pathFile = new File(rawPath);
        if (!Files.exists(Paths.get(rawPath).toAbsolutePath())) {
            rawPath = "../" + rawPath;
            pathFile = new File(rawPath);
        }
        return pathFile.getAbsolutePath();
    }

    public static String getGluttonHost() {
        return getPropertyValue(GrobidPropertyKeys.PROP_GLUTTON_HOST);
    }

    public static Integer getGluttonPort() {
        String val = getPropertyValue(GrobidPropertyKeys.PROP_GLUTTON_PORT);
        if (val != null && val.equals("null"))
            val = null;
        if (val == null)
            return null;
        else
            return Integer.valueOf(val);
    }

    public static boolean useELMo() {
        String rawValue = getPropertyValue(GrobidPropertyKeys.PROP_GROBID_DELFT_ELMO);
        if (rawValue.equals("true"))
            return true;
        else if (rawValue.equals("false"))
            return false;
        return false;
    }

    public static String getDelftArchitecture() {
        return getPropertyValue(GrobidPropertyKeys.PROP_DELFT_ARCHITECTURE);
    }

    public static void setDelftArchitecture(final String theArchitecture) {
        setPropertyValue(GrobidPropertyKeys.PROP_DELFT_ARCHITECTURE, theArchitecture);
    }

    /**
     * Returns the host for a proxy connection, given in the grobid-property
     * file.
     *
     * @return host for connecting crossref
     */
    public static String getProxyHost() {
        String val = getPropertyValue(GrobidPropertyKeys.PROP_PROXY_HOST);
        if (val != null && val.equals("null"))
            val = null;
        return val;
    }

    /**
     * Sets the host a proxy connection, given in the grobid-property file.
     *
     * @param host for connecting crossref
     */
    public static void setProxyHost(final String host) {
        setPropertyValue(GrobidPropertyKeys.PROP_PROXY_HOST, host);
        System.setProperty("http.proxyHost", "host");
        System.setProperty("https.proxyHost", "host");
    }

    /**
     * Returns the port for a proxy connection, given in the grobid-property
     * file.
     *
     * @return port for connecting crossref
     */
    public static Integer getProxyPort() {
        String val = getPropertyValue(GrobidPropertyKeys.PROP_PROXY_PORT);
        if (val != null && val.equals("null"))
            val = null;
        if (val == null)
            return null;
        else
            return Integer.valueOf(val);
    }

    /**
     * Set the "mailto" parameter to be used in the crossref query and in User-Agent
     * header, as recommended by CrossRef REST API documentation.
     *
     * @param mailto email parameter to be used for requesting crossref
     */
    public static void setCrossrefMailto(final String mailto) {
        setPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_MAILTO, mailto);
    }

    /**
     * Get the "mailto" parameter to be used in the crossref query and in User-Agent
     * header, as recommended by CrossRef REST API documentation.
     *
     * @return string of the email parameter to be used for requesting crossref
     */
    public static String getCrossrefMailto() {
        String val = getPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_MAILTO);
        if (val != null && val.equals("null"))
            val = null;
        if (val != null && val.length() == 0)
            val = null;
        return val;
    }

    /**
     * Set the Crossref Metadata Plus authorization token to be used for Crossref
     * requests for the subscribers of this service.  This token will ensure that said
     * requests get directed to a pool of machines that are reserved for "Plus" SLA users.
     *
     * @param token authorization token to be used for requesting crossref
     */
    public static void setCrossrefToken(final String token) {
        setPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_TOKEN, token);
    }

    /**
     * Get the Crossref Metadata Plus authorization token to be used for Crossref
     * requests for the subscribers of this service.  This token will ensure that said
     * requests get directed to a pool of machines that are reserved for "Plus" SLA users.
     *
     * @return authorization token to be used for requesting crossref
     */
    public static String getCrossrefToken() {
        String val = getPropertyValue(GrobidPropertyKeys.PROP_CROSSREF_TOKEN);
        if (val != null && val.equals("null"))
            val = null;
        if (val != null && val.length() == 0)
            val = null;
        return val;
    }

    /**
     * Sets the port for a proxy connection, given in the grobid-property file.
     *
     * @param port for connecting crossref
     */
    public static void setProxyPort(final String port) {
        setPropertyValue(GrobidPropertyKeys.PROP_PROXY_PORT, port);
        System.setProperty("http.proxyPort", port);
        System.setProperty("https.proxyPort", port);
    }

    public static Integer getPdfToXMLMemoryLimitMb() {
        return Integer.parseInt(getPropertyValue(GrobidPropertyKeys.PROP_3RD_PARTY_PDFTOXML_MEMORY_LIMIT, "2048"), 10);
    }

    public static Integer getPdfToXMLTimeoutMs() {
        return Integer.parseInt(getPropertyValue(GrobidPropertyKeys.PROP_3RD_PARTY_PDFTOXML_TIMEOUT_SEC, "60"), 10) * 1000;
    }

    /**
     * Returns the number of threads, given in the grobid-property file.
     *
     * @return number of threads
     */
    public static Integer getNBThreads() {
        Integer nbThreadsConfig = Integer.valueOf(getPropertyValue(GrobidPropertyKeys.PROP_NB_THREADS));
        if (nbThreadsConfig.intValue() == 0) {
            return Integer.valueOf(Runtime.getRuntime().availableProcessors());
        }
        return nbThreadsConfig;
    }


    // PDFs with more blocks will be skipped

    public static Integer getPdfBlocksMax() {
        return Integer.valueOf(getPropertyValue(GrobidPropertyKeys.PROP_PDF_BLOCKS_MAX, "100000"));
    }

    public static Integer getPdfTokensMax() {
        return Integer.valueOf(getPropertyValue(GrobidPropertyKeys.PROP_PDF_TOKENS_MAX, "1000000"));
    }

    /**
     * Sets the number of threads, given in the grobid-property file.
     *
     * @param nbThreads umber of threads
     */
    public static void setNBThreads(final String nbThreads) {
        setPropertyValue(GrobidPropertyKeys.PROP_NB_THREADS, nbThreads);
    }

    public static String getLanguageDetectorFactory() {
        String factoryClassName = getPropertyValue(GrobidPropertyKeys.PROP_LANG_DETECTOR_FACTORY);
        if (StringUtils.isBlank(factoryClassName)) {
            throw new GrobidPropertyException("Language detection is enabled but a factory class name is not provided");
        }
        return factoryClassName;
    }

    /**
     * Sets if a language id shall be used, given in the grobid-property file.
     *
     * @param useLanguageId true, if a language id shall be used
     */
    /*public static void setUseLanguageId(final String useLanguageId) {
        setPropertyValue(GrobidPropertyKeys.PROP_USE_LANG_ID, useLanguageId);
    }*/

    public static String getSentenceDetectorFactory() {
        String factoryClassName = getPropertyValue(GrobidPropertyKeys.PROP_SENTENCE_DETECTOR_FACTORY);
        if (StringUtils.isBlank(factoryClassName)) {
            throw new GrobidPropertyException("Sentence detection is enabled but a factory class name is not provided");
        }
        return factoryClassName;
    }

    /**
     * Returns the path to the home folder of pdf to xml converter.
     */
    public static void loadPdf2XMLPath() {
        LOGGER.debug("loading pdf to xml command path");
        String pathName = getPropertyValue(GrobidPropertyKeys.PROP_3RD_PARTY_PDFTOXML);

        pathToPdfToXml = new File(pathName);
        if (!pathToPdfToXml.exists()) {
            throw new GrobidPropertyException(
                "Path to 3rd party program (pdf to xml) doesn't exists. Please set the path to the pdf to xml program in the file grobid.properties with the property grobid.3rdparty.pdf2xml");
        }

        pathToPdfToXml = new File(pathToPdfToXml, Utilities.getOsNameAndArch());

        LOGGER.debug("pdf to xml executable home directory set to " + pathToPdfToXml.getAbsolutePath());
    }

    /**
     * Returns the path to the home folder of pdf to xml program.
     *
     * @return path to pdf to xml program
     */
    public static File getPdfToXMLPath() {
        return pathToPdfToXml;
    }

    private static String getModelPropertySuffix(final String modelName) {
        return modelName.replaceAll("-", "_");
    }

    private static String getGrobidCRFEngineName(final String modelName) {
        String defaultEngineName = GrobidProperties.getGrobidCRFEngine().name();
        return getPropertyValue(
            GrobidPropertyKeys.PROP_GROBID_CRF_ENGINE + "." + getModelPropertySuffix(modelName),
            defaultEngineName
        );
    }

    public static GrobidCRFEngine getGrobidCRFEngine(final String modelName) {
        String engineName = getGrobidCRFEngineName(modelName);
        if (grobidCRFEngine.name().equals(engineName)) {
            return grobidCRFEngine;
        }
        return GrobidCRFEngine.get(engineName);
    }

    public static GrobidCRFEngine getGrobidCRFEngine(final GrobidModel model) {
        return getGrobidCRFEngine(model.getModelName());
    }

    public static GrobidCRFEngine getGrobidCRFEngine() {
        return grobidCRFEngine;
    }

    public static File getModelPath(final GrobidModel model) {
        String extension = getGrobidCRFEngine(model).getExt();
        return new File(get_GROBID_HOME_PATH(), FOLDER_NAME_MODELS + File.separator
            + model.getFolderName() + File.separator
            + FILE_NAME_MODEL + "." + extension);
    }

    public static File getModelPath() {
        return new File(get_GROBID_HOME_PATH(), FOLDER_NAME_MODELS);
    }

    public static File getTemplatePath(final File resourcesDir, final GrobidModel model) {
        File theFile = new File(resourcesDir, "dataset/" + model.getFolderName()
            + "/crfpp-templates/" + model.getTemplateName());
        if (!theFile.exists()) {
            theFile = new File("resources/dataset/" + model.getFolderName()
                + "/crfpp-templates/" + model.getTemplateName());
        }
        return theFile;
    }

    public static File getEvalCorpusPath(final File resourcesDir, final GrobidModel model) {
        File theFile = new File(resourcesDir, "dataset/" + model.getFolderName() + "/evaluation/");
        if (!theFile.exists()) {
            theFile = new File("resources/dataset/" + model.getFolderName() + "/evaluation/");
        }
        return theFile;
    }

    public static File getCorpusPath(final File resourcesDir, final GrobidModel model) {
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
    }

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
     * Returns the consolidation service to be used.
     *
     * @return the consolidation service to be used
     */
    public static GrobidConsolidationService getConsolidationService() {
        return GrobidConsolidationService.get(getPropertyValue(GrobidPropertyKeys.PROP_CONSOLIDATION_SERVICE));
    }

    /**
     * Set which consolidation service to use
     */
    public static void setConsolidationService(String service) {
        setPropertyValue(GrobidPropertyKeys.PROP_CONSOLIDATION_SERVICE, service);
    }

    /**
     * Returns if the execution context is stand alone or server.
     *
     * @return the context of execution. Return false if the property value is
     * not readable.
     */
    public static Boolean isContextExecutionServer() {
        return Utilities.stringToBoolean(getPropertyValue(GrobidPropertyKeys.PROP_GROBID_IS_CONTEXT_SERVER, "false"));
    }

    /**
     * Set if the execution context is stand alone or server.
     *
     * @param state true to set the context of execution to server, false else.
     */
    public static void setContextExecutionServer(Boolean state) {
        setPropertyValue(GrobidPropertyKeys.PROP_GROBID_IS_CONTEXT_SERVER, state.toString());
    }

    /**
     * Sets the GROBID version.
     */
    public static void setVersion(final String version) {
        setPropertyValue(GrobidPropertyKeys.PROP_GROBID_VERSION, version);
    }


    public static String getPythonVirtualEnv() {
        return getPropertyValue(GrobidPropertyKeys.PYTHON_VIRTUALENV_DIRECTORY);
    }

    public static void setPythonVirtualEnv(String pythonVirtualEnv) {
        setPropertyValue(GrobidPropertyKeys.PYTHON_VIRTUALENV_DIRECTORY, pythonVirtualEnv);
    }
}
