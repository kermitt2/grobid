package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import java.io.File;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public interface GenericTrainer {
    void train(File template, File trainingData, File outputModel, int numThreads, GrobidModels model);
    String getName();
	public void setEpsilon(double epsilon);
	public void setWindow(int window);
	public double getEpsilon();
	public int getWindow();
}
