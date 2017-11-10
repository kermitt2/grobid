package org.grobid.core.utilities;

/**
 * This class contains all the keys of the properties files.
 *
 * @author Damien Ridereau
 */
public interface GrobidPropertyKeys {

    String PROP_GROBID_IS_CONTEXT_SERVER = "grobid.is.context.server";

    String PROP_GROBID_VERSION = "org.grobid.version";
    String PROP_TMP_PATH = "grobid.temp.path";
    //    public static final String PROP_BIN_PATH = "grobid.bin.path";
    String PROP_NATIVE_LIB_PATH = "grobid.nativelibrary.path";
    String PROP_3RD_PARTY_PDF2XML = "grobid.3rdparty.pdf2xml.path";
    String PROP_3RD_PARTY_PDF2XML_MEMORY_LIMIT = "grobid.3rdparty.pdf2xml.memory.limit.mb";
    String PROP_3RD_PARTY_PDF2XML_TIMEOUT_SEC = "grobid.3rdparty.pdf2xml.memory.timeout.sec";

    String PROP_GROBID_CRF_ENGINE = "grobid.crf.engine";
    String PROP_USE_LANG_ID = "grobid.use_language_id";
    String PROP_LANG_DETECTOR_FACTORY = "grobid.language_detector_factory";

    String PROP_CROSSREF_ID = "grobid.crossref_id";
    String PROP_CROSSREF_PW = "grobid.crossref_pw";
    String PROP_CROSSREF_HOST = "grobid.crossref_host";
    String PROP_CROSSREF_PORT = "grobid.crossref_port";

    /*String PROP_MYSQL_HOST = "grobid.mysql_host";
    String PROP_MYSQL_PORT = "grobid.mysql_port";
    String PROP_MYSQL_USERNAME = "grobid.mysql_username";
    String PROP_MYSQL_PW = "grobid.mysql_passwd";
    String PROP_MYSQL_DB_NAME = "grobid.mysql_db_name";*/

    String PROP_PROXY_HOST = "grobid.proxy_host";
    String PROP_PROXY_PORT = "grobid.proxy_port";

    String PROP_NB_THREADS = "grobid.nb_threads";

    String PROP_PDF_BLOCKS_MAX = "grobid.pdf.blocks.max";
    String PROP_PDF_TOKENS_MAX = "grobid.pdf.tokens.max";

    String PROP_GROBID_MAX_CONNECTIONS = "org.grobid.max.connections";
    String PROP_GROBID_POOL_MAX_WAIT = "org.grobid.pool.max.wait";

    /**
     * Determines if properties like the firstnames, lastnames country codes and
     * dictionaries are supposed to be read from $GROBID_HOME path or not
     * (possible values (true|false) default is false)
     */
    String PROP_RESOURCE_INHOME = "grobid.resources.inHome";

    /**
     * The name of the env-entry located in the web.xml, via which the
     * grobid-service.propeties path is set.
     */
    String PROP_GROBID_HOME = "org.grobid.home";

    /**
     * The name of the env-entry located in the web.xml, via which the
     * grobid.propeties path is set.
     */
    String PROP_GROBID_PROPERTY = "org.grobid.property";

    /**
     * The name of the system property, via which the grobid home folder can be
     * located.
     */
//    String PROP_GROBID_SERVICE_PROPERTY = "org.grobid.property.service";

    /**
     * name of the property setting the admin password
     */
    String PROP_GROBID_SERVICE_ADMIN_PW = "org.grobid.service.admin.pw";

    /**
     * If set to true, parallel execution will be done, else a queuing of
     * requests will be done.
     */
    String PROP_GROBID_SERVICE_IS_PARALLEL_EXEC = "org.grobid.service.is.parallel.execution";

    /**
     * The defined paths to create.
     */
    String[] PATHES_TO_CREATE = {PROP_TMP_PATH};

}
