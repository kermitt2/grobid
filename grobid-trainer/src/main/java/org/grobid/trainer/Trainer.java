package org.grobid.trainer;

import org.grobid.core.GrobidModels;

import java.io.File;

/**
 * @author Patrice Lopez
 */
public interface Trainer {

    public int createCRFPPData(File corpusPath, File outputFile);

	public int createCRFPPData(File corpusPath, File outputTrainingFile, File outputEvalFile, double splitRatio);

    public void train();

    /**
     *
     * @return a report
     */
    public String evaluate();

	public String splitTrainEvaluate(Double split);

    public GrobidModels getModel();
}