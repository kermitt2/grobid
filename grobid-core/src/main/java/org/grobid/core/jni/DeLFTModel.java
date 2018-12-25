package org.grobid.core.jni;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;  
import java.io.*;
import java.lang.StringBuilder;
import java.util.*;

import jep.Jep;
import jep.JepConfig;
import jep.JepException;

/**
 * 
 * @author: Patrice
 */
public class DeLFTModel {
    public static final Logger LOGGER = LoggerFactory.getLogger(DeLFTModel.class);

    // Exploit JNI CPython interpreter to execute load and execute a DeLFT deep learning model 
    private String modelName;

    public DeLFTModel(GrobidModel model) {
        this.modelName = "grobid_" + model.getModelName().replace("-", "_");
        try {
            LOGGER.info("Loading DeLFT model for " + model.getModelName() + "...");
            JEPThreadPool.getInstance().run(new InitModel(this.modelName));
        } catch(InterruptedException e) {
            LOGGER.error("DeLFT model " + this.modelName + " initialization failed", e);
        }
    }

    class InitModel implements Runnable { 
        private String modelName;
          
        public InitModel(String modelName) { 
            this.modelName = modelName;
        } 
          
        @Override
        public void run() { 
            Jep jep = JEPThreadPool.getInstance().getJEPInstance(); 
            try { 
                jep.eval(this.modelName+" = sequenceLabelling.Sequence('" + this.modelName.replace("_", "-") + "')");
                jep.eval(this.modelName+".load()");
            } catch(JepException e) {
                LOGGER.error("DeLFT model initialization failed", e);
            } 
        } 
    } 

    class LabelTask implements Callable<String> { 
        private String data;
        private String modelName;

        public LabelTask(String modelName, String data) { 
            System.out.println("label thread: " + Thread.currentThread().getId());
            this.modelName = modelName;
            this.data = data;
        } 
          
        @Override
        public String call() { 
            Jep jep = JEPThreadPool.getInstance().getJEPInstance(); 
            StringBuilder labelledData = new StringBuilder();
            try {
                jep.set("input", this.data);
                jep.eval("x_all, f_all = load_data_crf_string(input)");
                Object objectResults = jep.getValue(this.modelName+".tag(x_all, None)");
                ArrayList<ArrayList<List<String>>> results = (ArrayList<ArrayList<List<String>>>)objectResults;

                // inject the tags
                BufferedReader bufReader = new BufferedReader(new StringReader(data));
                String line;
                int i = 0; // sentence index
                int j = 0; // word index in the sentence
                ArrayList<List<String>> result = results.get(0);
                //System.out.println(result);
                //System.out.println(result.getClass().getName());
                //System.out.println(result.size());
                while( (line=bufReader.readLine()) != null ) {
                    line = line.trim();
                    if (line.length() == 0) {
                        j = 0;
                        i++;
                        result = results.get(i);
                        System.out.println(result);
                    }
                    labelledData.append(line);
                    labelledData.append(" ");
                    //System.out.println(result.get(j));
                    //System.out.println(result.get(j).getClass().getName());
                    List<String> pair = result.get(j);
                    // first is the token, second is the label (DeLFT format)
                    String token = pair.get(0);
                    String label = pair.get(1);
                    labelledData.append(DeLFTModel.delft2grobidLabel(label));
                    labelledData.append("\n");
                    j++;
                }
                
                jep.eval("del input");
                jep.eval("del x_all");
                jep.eval("del f_all");
            } catch(JepException e) {
                LOGGER.error("DeLFT model labelling via JEP failed", e);
            } catch(IOException e) {
                LOGGER.error("DeLFT model labelling failed", e);
            }
            System.out.println(labelledData.toString());
            return labelledData.toString();
        } 
    } 

    public String label(String data) {
        String result = null;
        try {
            result = JEPThreadPool.getInstance().call(new LabelTask(this.modelName, data));
        } catch(InterruptedException e) {
            LOGGER.error("DeLFT model " + this.modelName + " labelling interrupted", e);
        } catch(ExecutionException e) {
            LOGGER.error("DeLFT model " + this.modelName + " labelling failed", e);
        }
        return result;
    }

    public static void train(File trainingData) {
        
    }

    public synchronized void close() {
        /*if (jep != null) {
            try {
                jep.eval("del model");
                jep.close();
            } catch(JepException e) {
                LOGGER.error("Closing DeLFT resources failed", e);
            }
        }*/
    }

    private static String delft2grobidLabel(String label) {
        if (label.equals("O")) {
            label = "<other>";
        } else if (label.startsWith("B-")) {
            label = label.replace("B-", "I-");
        } else if (label.startsWith("I-")) {
            label = label.replace("I-", "");
        } 
        return label;
    }

}