package org.grobid.trainer;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;

import java.io.File;

/**
 * @author Patrice Lopez
 */
public interface Trainer {

    int createCRFPPData(File corpusPath, File outputFile);

	int createCRFPPData(File corpusPath, File outputTrainingFile, File outputEvalFile, double splitRatio);

    void train();

    /**
     *
     * @return a report
     */
    String evaluate();

	String splitTrainEvaluate(Double split);

    GrobidModel getModel();
}