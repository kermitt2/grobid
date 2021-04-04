package org.grobid.core.utilities;

import java.util.List;

/**
 * This class is a bean for the YAML configuation of the GROBID instance.  
 *
 */
public class GrobidConfig {

    public GrobidParameters grobid;

    static class GrobidParameters {
        public String grobidHome = "grobid-home";
        public String temp = "./tmp";
        public String nativelibrary = "./lib";

        public PdfParameters pdf;
        public ConsolidationParameters consolidation;
        public HostParameters proxy;

        public String language_detector_factory;
        public String sentence_detector_factory;
  
        public int max_connections = 10;  
        public int pool_max_wait = 1;
        public int nb_threads = 0;

        public DelftParameters delft; 
        public List<ModelParameters> models;
    }

    static class PdfParameters {
        public PdfAltoParameters pdfalto;
        public int blocks_max = 100000;
        public int tokens_max = 1000000;
    }

    static class PdfAltoParameters {
        public String path;
        public int memory_limit_mb = 6096;
        public int timeout_sec = 60;
    }

    static class ConsolidationParameters {
        public String service;
        public HostParameters glutton;
        public CrossrefParameters crossref;
    }

    static class CrossrefParameters {
        public String mailto;
        public String token;
    }

    static class HostParameters {
        public String type;
        public String host;
        public int port;
    }
    
    static class DelftParameters {
        /**
         * Generic parameters relative to the DeLFT engine
         */
        public String install;
        public String python_virtualEnv;
    }

    static class WapitiModelParameters {
        /**
         * Parameters relative to a specific Wapiti model
         */
        public double epsilon = 0.00001;
        public int window = 20;
        public int nbMaxIterations = 2000;
    }

    static class DelftModelParameters {
        /**
         * Parameters relative to a specific DeLFT model
         */
        public String architecture;
        public boolean useELMo = false;
    }

    static class ModelParameters {   
        public String name;   /* name of model */
        public String engine; /* value wapiti or delft */ 

        public WapitiModelParameters wapiti;
        public DelftModelParameters delft;   
    }
}