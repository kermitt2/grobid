package org.grobid.core.jni;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import java.nio.file.Path;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.exceptions.GrobidResourceException;

import jep.Jep;
import jep.JepConfig;
import jep.JepException;
import jep.SubInterpreter;
import jep.SharedInterpreter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a classifier variant for using DeLFT deep learning models, we use 
 * JEP as JNI CPython interpreter.
 * JEP presents the following constraint: A thread that creates a JEP instance
 * must be reused for all method calls to that JEP instance. For ensuring this,
 * we pool the Jep instances in a singleton class.
 */
public class JEPThreadPoolClassifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(JEPThreadPoolClassifier.class);

    private int POOL_SIZE = 1;

    private ExecutorService executor;
    private ConcurrentMap<Long, Jep> jepInstances;

    private static volatile JEPThreadPoolClassifier instance;

    public static JEPThreadPoolClassifier getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
    private static synchronized void getNewInstance() {
        LOGGER.debug("Get new instance of JEPThreadPoolClassifier");
        instance = new JEPThreadPoolClassifier();
    }

    /**
     * Hidden constructor
     */
    private JEPThreadPoolClassifier() {
        // creating a pool of POOL_SIZE threads
        //executor = Executors.newFixedThreadPool(POOL_SIZE); 
        executor = Executors.newSingleThreadExecutor();
        // each of these threads is associated to a JEP instance
        jepInstances = new ConcurrentHashMap<>();
    }

    private File getAndValidateDelftPath() {
        File delftPath = new File(GrobidProperties.getDeLFTFilePath());
        if (!delftPath.exists()) {
            throw new GrobidResourceException("DeLFT installation path does not exist");
        }
        if (!delftPath.isDirectory()) {
            throw new GrobidResourceException("DeLFT installation path is not a directory");
        }
        return delftPath;
    }

    private JepConfig getJepConfig(File delftPath, Path sitePackagesPath) {
        JepConfig config = new JepConfig();
        config.addIncludePaths(delftPath.getAbsolutePath());
        config.redirectStdout(System.out);
        config.redirectStdErr(System.err);
        if (sitePackagesPath != null) {
            config.addIncludePaths(sitePackagesPath.toString());
        }
        config.setClassLoader(Thread.currentThread().getContextClassLoader());
        return config;
    }

    private void initializeJepInstance(Jep jep, File delftPath) throws JepException {
        // import packages
        jep.eval("import os");
        jep.eval("import json");
        jep.eval("os.chdir('" + delftPath.getAbsolutePath() + "')");
        jep.eval("from delft.utilities.Embeddings import Embeddings");
        //jep.eval("from delft.utilities.Utilities import split_data_and_labels");
        jep.eval("import delft.textClassification");
        jep.eval("from delft.textClassification import Classifier");
        //jep.eval("from delft.textClassification.reader import load_dataseer_corpus_csv");
        //jep.eval("from delft.textClassification.reader import vectorize as vectorizer");
    }

    private Jep createJEPInstance() {
        Jep jep = null;
        boolean success = false;
        try {
            File delftPath = this.getAndValidateDelftPath();
            JepConfig config = this.getJepConfig(
                delftPath,
                PythonEnvironmentConfig.getInstance().getSitePackagesPath()
            );
            //jep = new SubInterpreter(config);
            try {
                SharedInterpreter.setConfig(config);
            } catch(Exception e) {
                LOGGER.info("JEP interpreter already initialized");
            }
            jep = new SharedInterpreter();
            this.initializeJepInstance(jep, delftPath);
            success = true;
            return jep;
        } catch(JepException e) {
            LOGGER.error("JEP initialization failed", e);
            throw new RuntimeException("JEP initialization failed", e);
        } catch(GrobidResourceException e) {
            LOGGER.error("DeLFT installation path invalid, JEP initialization failed", e);
            throw new RuntimeException("DeLFT installation path invalid, JEP initialization failed", e);
        } catch (UnsatisfiedLinkError e) {
            LOGGER.error("JEP environment not correctly installed or has incompatible binaries, JEP initialization failed", e);
            throw new RuntimeException("JEP environment not correctly installed or has incompatible binaries, JEP initialization failed", e);
        } finally {
            if (!success) {
                if (jep != null) {
                    try {
                        jep.close();
                    } catch (JepException e) {
                        LOGGER.error("Failed to close JEP instance", e);
                    }
                } else {
                    LOGGER.error("JEP initialisation failed");
                    throw new RuntimeException("JEP initialisation failed");
                }
            }
        }
    }

    /**
     * To be called by the thread executing python commands via JEP.
     * The method will return to the thread its dedicated Jep instance
     * (or create one the first time).
     */
    public synchronized Jep getJEPInstance() {
        long threadId = Thread.currentThread().getId();
        Jep jep = jepInstances.get(threadId);
        if (jep == null) {
            LOGGER.info("Creating JEP instance for thread " + threadId);
            jep = this.createJEPInstance();
            jepInstances.put(threadId, jep);
        }
        try {
            jep.isValidThread();
        } catch (JepException e) {
            LOGGER.warn("JEP instance no longer usable, creating new instance", e);
            jep = this.createJEPInstance();
            jepInstances.put(threadId, jep);
        }
        return jep;
    }

    public void run(Runnable task) throws InterruptedException {
        LOGGER.debug("running thread: " + Thread.currentThread().getId());
        Future future = executor.submit(task);
        // wait until done (in ms)
        while (!future.isDone()) {
            Thread.sleep(1);
        }
    }

    public String call(Callable<String> task) throws InterruptedException, ExecutionException {
        Future<String> future = executor.submit(task);
        // block until done
        return future.get();
    }

}