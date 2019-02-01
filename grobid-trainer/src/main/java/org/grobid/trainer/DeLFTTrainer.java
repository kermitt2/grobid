package org.grobid.trainer;

import org.grobid.core.GrobidModel;
import org.grobid.core.jni.DeLFTModel;
import org.grobid.core.GrobidModels;
import org.grobid.trainer.SegmentationTrainer;
import java.math.BigDecimal;

import java.io.File;

/**
 * @author: Patrice
 */
public class DeLFTTrainer implements GenericTrainer {

    public static final String DELFT = "delft";

    @Override
    public void train(File template, File trainingData, File outputModel, int numThreads, GrobidModel model) {
        DeLFTModel.train(model.getModelName(), trainingData, outputModel);
    }

    @Override
    public String getName() {
        return DELFT;
    }
    
    /**
     * None of this below is used by DeLFT
     */
    @Override
    public void setEpsilon(double epsilon) {
    }
    
    @Override
    public void setWindow(int window) {
    }
    
    @Override
    public double getEpsilon() {
        return 0.0;
    }
    
    @Override
    public int getWindow() {
        return 0;
    }

    @Override
    public void setNbMaxIterations(int interations) {
    }
    
    @Override
    public int getNbMaxIterations() {
        return 0;
    }
}
