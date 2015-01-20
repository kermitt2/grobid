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
}
