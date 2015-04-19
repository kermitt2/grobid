package org.grobid.trainer;

import org.grobid.core.jni.WapitiModel;
import org.grobid.core.GrobidModels;
import org.grobid.trainer.SegmentationTrainer;
	
import java.io.File;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public class WapitiTrainer implements GenericTrainer {

    public static final String WAPITI = "wapiti";

    @Override
    public void train(File template, File trainingData, File outputModel, int numThreads, GrobidModels model) {
		// parameters
		double epsilon = 0.00001; // default size of the interval for stopping criterion
		int window = 20; // default similar to CRF++
		// adjusting depending on the model to be trained
		if (model.getModelName().equals("segmentation")) {
			epsilon = SegmentationTrainer.epsilon;
			window = SegmentationTrainer.window;
		}
        WapitiModel.train(template, trainingData, outputModel, "--nthread " + numThreads +
//       		" --algo sgd-l1"
			" -e " + epsilon +
			" -w " + window +
			""
        );
    }

    @Override
    public String getName() {
        return WAPITI;
    }
}
