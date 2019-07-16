package org.grobid.core.jni;

import java.util.concurrent.*;  
import java.util.*;
import java.io.*;

import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.exceptions.GrobidResourceException;

import jep.Jep;
import jep.JepConfig;
import jep.JepException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For using DeLFT deep learning models, we use JEP as JNI CPython interpreter. 
 * JEP presents the following constraint: A thread that creates a JEP instance 
 * must be reused for all method calls to that JEP instance. For ensuring this,
 * we pool the Jep instances in a singleton class. 
 */

public class JEPThreadPool { 
    private static final Logger LOGGER = LoggerFactory.getLogger(JEPThreadPool.class);

    private int POOL_SIZE = 1;

    private ExecutorService executor;  
    private Map<Long,Jep> jepInstances;

    private static volatile JEPThreadPool instance;

    public static JEPThreadPool getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
    private static synchronized void getNewInstance() {
        LOGGER.debug("Get new instance of JEPThreadPool");
        instance = new JEPThreadPool();
    }

    /**
     * Hidden constructor
     */
    private JEPThreadPool() {
        // creating a pool of POOL_SIZE threads
        //executor = Executors.newFixedThreadPool(POOL_SIZE); 
        executor = Executors.newSingleThreadExecutor();
        // each of these threads is associated to a JEP instance
        jepInstances = new HashMap<Long,Jep>();
    }

    /**
     * To be called by the thread executing python commands via JEP. 
     * The method will return to the thread its dedicated Jep instance
     * (or create one the first time). 
     */
    public Jep getJEPInstance() {
        if (jepInstances.get(Thread.currentThread().getId()) == null) {
            JepConfig config = new JepConfig();
            config.setRedirectOutputStreams(GrobidProperties.isDeLFTRedirectOutput());
            
            try {
                File delftPath = new File(GrobidProperties.getInstance().getDeLFTFilePath());
                if (!delftPath.exists()) {
                    throw new GrobidResourceException("DeLFT installation path does not exist");
                }
                if (!delftPath.isDirectory()) {
                    throw new GrobidResourceException("DeLFT installation path is not a directory");
                }
                config.addIncludePaths(delftPath.getAbsolutePath());
                config.setClassLoader(Thread.currentThread().getContextClassLoader());
                //System.out.println("jep instance thread: " + Thread.currentThread().getId());
                Jep jep = new Jep(config);
                jepInstances.put(Thread.currentThread().getId(), jep);
                // import packages
                jep.eval("import os");
                jep.eval("import numpy as np");
                jep.eval("import keras.backend as K");
                jep.eval("os.chdir('" + delftPath.getAbsolutePath() + "')");
                String delftPackage = GrobidProperties.getDeLFTPackage();
                LOGGER.debug("delft package: {}", delftPackage);
                jep.eval("from " + delftPackage + ".utilities.Embeddings import Embeddings");
                jep.eval("import " + delftPackage + ".sequenceLabelling");
                jep.eval("from " + delftPackage + ".sequenceLabelling import Sequence");
                jep.eval("from " + delftPackage + ".sequenceLabelling.reader import load_data_and_labels_crf_file");
                jep.eval("from " + delftPackage + ".sequenceLabelling.reader import load_data_crf_string");
                jep.eval("from sklearn.model_selection import train_test_split");
            } catch(JepException e) {
                LOGGER.error("JEP initialization failed", e);
            } catch(GrobidResourceException e) {
                LOGGER.error("DeLFT installation path invalid, JEP initialization failed", e);
            } 
        }
        return jepInstances.get(Thread.currentThread().getId());
    }

    public void run(Runnable task) throws InterruptedException {
        System.out.println("running thread: " + Thread.currentThread().getId());
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