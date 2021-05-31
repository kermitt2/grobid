package org.grobid.core.utilities;

import java.util.List;

/**
 * This class is a bean for the YAML configuation of the GROBID instance.  
 *
 */
public class GrobidConfig {

    public GrobidParameters grobid;

    public static class GrobidParameters {
        public String grobidHome = "grobid-home";
        public String temp = "./tmp";
        public String nativelibrary = "./lib";

        public PdfParameters pdf;
        public ConsolidationParameters consolidation;
        public HostParameters proxy;

        public String languageDetectorFactory;
        public String sentenceDetectorFactory;
  
        public int maxConnections = 10;  
        public int poolMaxWait = 1;
        public int nbThreads = 0;

        public DelftParameters delft; 
        public List<ModelParameters> models;
    }

    public static class PdfParameters {
        public PdfAltoParameters pdfalto;
        public int blocksMax = 100000;
        public int tokensMax = 1000000;
    }

    public static class PdfAltoParameters {
        public String path;
        public int memoryLimitMb = 6096;
        public int timeoutSec = 60;
    }

    public static class ConsolidationParameters {
        public String service;
        public HostParameters glutton;
        public CrossrefParameters crossref;
    }

    public static class CrossrefParameters {
        public String mailto;
        public String token;
    }

    public static class HostParameters {
        public String type;
        public String host;
        public int port;
        public String url;
    }
    
    public static class DelftParameters {
        /**
         * Generic parameters relative to the DeLFT engine
         */
        public String install;
        public String pythonVirtualEnv;
    }

    public static class WapitiModelParameters {
        /**
         * Parameters relative to a specific Wapiti model
         */
        public double epsilon = 0.00001;
        public int window = 20;
        public int nbMaxIterations = 2000;
    }

    public static class DelftModelParameters {
        /**
         * Parameters relative to a specific DeLFT model (train and runtime)
         */
        public String architecture;
        public boolean useELMo = false;
        public String embeddings_name = "glove-840B";

        public DelftModelParameterSet training;
        public DelftModelParameterSet runtime;
    }

    public static class DelftModelParameterSet {
        /**
         * Parameters relative to a specific DeLFT model and either training or runtime
         */
        public int max_sequence_length = -1;
        public int batch_size = -1;
    }

    public static class ModelParameters {   
        public String name;   /* name of model */
        public String engine; /* value wapiti or delft */ 

        public WapitiModelParameters wapiti;
        public DelftModelParameters delft;   
    }
}