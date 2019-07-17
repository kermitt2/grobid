package org.grobid.trainer;

import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GenericTagger;

import java.io.File;

/**
 * @author Patrice Lopez
 */
public interface Trainer {

    int createCRFPPData(File corpusPath, File outputFile);

	int createCRFPPData(File corpusPath, File outputTrainingFile, File outputEvalFile, double splitRatio);

    void train();

    String evaluate();

    String evaluate(boolean includeRawResults);

    String evaluate(GenericTagger tagger, boolean includeRawResults);

	String splitTrainEvaluate(Double split);

	String nFoldEvaluate(int folds);

	String nFoldEvaluate(int folds, boolean includeRawResults);

    GrobidModel getModel();
}