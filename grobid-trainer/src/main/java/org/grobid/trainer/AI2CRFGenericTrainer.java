package org.grobid.trainer;

import org.allenai.ml.sequences.crf.conll.Trainer;
import org.grobid.core.GrobidModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AI2CRFGenericTrainer implements GenericTrainer {
    public static final Logger LOGGER = LoggerFactory.getLogger(AI2CRFGenericTrainer.class);
    public static final String CRF = "ai2-ml";
    private final org.allenai.ml.sequences.crf.conll.Trainer trainer;

    // default training parameters (not exploited by CRFPP so far, it requires to extend the JNI)
    private double epsilon = 0.00001; // default size of the interval for stopping criterion
    private int window = 20; // default similar to CRF++

    public AI2CRFGenericTrainer() {
        trainer = new Trainer();
    }

    @Override
    public void train(File template, File trainingData, File outputModel, int numThreads, GrobidModels model) {
        Trainer.Opts opts = new Trainer.Opts();
        opts.templateFile = template.getAbsolutePath();
        opts.trainPath = trainingData.getAbsolutePath();
        opts.modelPath = outputModel.getAbsolutePath();
        opts.numThreads = numThreads;
        opts.featureKeepProb = 0.1;
        opts.maxIterations = 300;
        trainer.trainAndSaveModel(opts);
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
