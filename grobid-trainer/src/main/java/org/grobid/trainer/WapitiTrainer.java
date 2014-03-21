package org.grobid.trainer;

import org.grobid.core.jni.WapitiModel;

import java.io.File;

/**
 * User: zholudev
 * Date: 3/20/14
 */
public class WapitiTrainer implements GenericTrainer {

    public static final String WAPITI = "wapiti";

    @Override
    public void train(File template, File trainingData, File outputModel, int numThreads) {

        WapitiModel.train(template, trainingData, outputModel, "--nthread " + numThreads +
//                " --algo sgd-l1"
                ""
        );
    }

    @Override
    public String getName() {
        return WAPITI;
    }
}
