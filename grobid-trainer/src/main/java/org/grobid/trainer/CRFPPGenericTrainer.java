package org.grobid.trainer;

import org.chasen.crfpp.CRFPPTrainer;
import org.grobid.core.GrobidModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public class CRFPPGenericTrainer implements GenericTrainer {
    public static final Logger LOGGER = LoggerFactory.getLogger(CRFPPGenericTrainer.class);
    public static final String CRF = "crf";
    private final CRFPPTrainer crfppTrainer;

	// default training parameters (not exploited by CRFPP so far, it requires to extend the JNI)
	private double epsilon = 0.00001; // default size of the interval for stopping criterion
	private int window = 20; // default similar to CRF++

    public CRFPPGenericTrainer() {
        crfppTrainer = new CRFPPTrainer();
    }

    @Override
    public void train(File template, File trainingData, File outputModel, int numThreads, GrobidModels model) {
        crfppTrainer.train(template.getAbsolutePath(), trainingData.getAbsolutePath(), outputModel.getAbsolutePath(), numThreads);
        if (!crfppTrainer.what().isEmpty()) {
            LOGGER.warn("CRF++ Trainer warnings:\n" + crfppTrainer.what());
        } else {
            LOGGER.info("No CRF++ Trainer warnings!");
        }
    }

    @Override
    public String getName() {
        return CRF;
    }
	
    @Override
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }
	
    @Override
    public void setWindow(int window) {
        this.window = window;
    }
	
    @Override
    public double getEpsilon() {
        return epsilon;
    }
	
    @Override
    public int getWindow() {
        return window;
    }
}
