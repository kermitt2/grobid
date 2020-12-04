package org.grobid.core.utilities;

/**
 * This class contains all the keys of the properties files.
 *
 */
public interface GrobidPropertyKeys {

    String PROP_GROBID_IS_CONTEXT_SERVER = "grobid.is.context.server";

    String PROP_GROBID_VERSION = "org.grobid.version";
    String PROP_TMP_PATH = "grobid.temp.path";
    String PROP_NATIVE_LIB_PATH = "grobid.nativelibrary.path";
    String PROP_3RD_PARTY_PDFTOXML = "grobid.3rdparty.pdf2xml.path";
    String PROP_3RD_PARTY_PDFTOXML_MEMORY_LIMIT = "grobid.3rdparty.pdf2xml.memory.limit.mb";
    String PROP_3RD_PARTY_PDFTOXML_TIMEOUT_SEC = "grobid.3rdparty.pdf2xml.memory.timeout.sec";

    String PROP_GROBID_CRF_ENGINE = "grobid.crf.engine";
    String PROP_GROBID_DELFT_PATH = "grobid.delft.install";
    String PROP_GROBID_DELFT_ELMO = "grobid.delft.useELMo";
    String PROP_DELFT_ARCHITECTURE = "grobid.delft.architecture";

    String PROP_LANG_DETECTOR_FACTORY = "grobid.language_detector_factory";
    String PROP_SENTENCE_DETECTOR_FACTORY = "grobid.sentence_detector_factory";

    String PROP_CROSSREF_ID = "grobid.crossref_id";
    String PROP_CROSSREF_PW = "grobid.crossref_pw";
    String PROP_CROSSREF_HOST = "grobid.crossref_host";
    String PROP_CROSSREF_PORT = "grobid.crossref_port";

    /** 
     * If indicated, include a "mailto" parameter in the crossref query and User-Agent 
     * header, as recommended by CrossRef REST API documentation. 
     */
    String PROP_CROSSREF_MAILTO = "org.grobid.crossref.mailto";

    /** 
     * For indicating a Crossref Metadata Plus authorization token to be used for Crossref
     * requests for the subscribers of this service.  This token will ensure that said 
     * requests get directed to a pool of machines that are reserved for "Plus" SLA users.
     */
    String PROP_CROSSREF_TOKEN = "org.grobid.crossref.token";

    String PROP_PROXY_HOST = "grobid.proxy_host";
    String PROP_PROXY_PORT = "grobid.proxy_port";

    String PROP_NB_THREADS = "grobid.nb_threads";

    String PROP_PDF_BLOCKS_MAX = "grobid.pdf.blocks.max";
    String PROP_PDF_TOKENS_MAX = "grobid.pdf.tokens.max";

    String PROP_GROBID_MAX_CONNECTIONS = "org.grobid.max.connections";
    String PROP_GROBID_POOL_MAX_WAIT = "org.grobid.pool.max.wait";

    String PROP_GLUTTON_HOST = "org.grobid.glutton.host";
    String PROP_GLUTTON_PORT = "org.grobid.glutton.port";

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
     * name of the property setting the admin password
     */
    String PROP_GROBID_SERVICE_ADMIN_PW = "org.grobid.service.admin.pw";

    /**
     * If set to true, parallel execution will be done, else a queuing of
     * requests will be done.
     */
    String PROP_GROBID_SERVICE_IS_PARALLEL_EXEC = "org.grobid.service.is.parallel.execution";

    /**
     * Bibliographical data consolidation service to be used, either "crossref" for CrossRef 
     * REST API or "glutton" for https://github.com/kermitt2/biblio-glutton
     */
    String PROP_CONSOLIDATION_SERVICE = "grobid.consolidation.service";

    /**
     * The defined paths to create.
     */
    String[] PATHES_TO_CREATE = {PROP_TMP_PATH};


    String PYTHON_VIRTUALENV_DIRECTORY = "grobid.delft.python.virtualEnv";
}
