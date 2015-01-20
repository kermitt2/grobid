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
		double eta = 0.00001; // default similar to CRF++
		int window = 20; // default similar to CRF++
		// adjusting depending on the model to be trained
		if (model.getModelName().equals("segmentation")) {
			eta = SegmentationTrainer.eta;
			window = SegmentationTrainer.window;
		}
        WapitiModel.train(template, trainingData, outputModel, "--nthread " + numThreads +
//       		" --algo sgd-l1"
			" -e " + eta +
			" -w " + window +
			""
        );
    }

    @Override
    public String getName() {
        return WAPITI;
    }
}
