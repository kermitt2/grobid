package org.grobid.trainer;

import java.io.File;
import java.io.IOException;

import org.chasen.crfpp.CRFPPTrainer;
import org.chasen.crfpp.Tagger;
import org.grobid.core.GrobidModels;
import org.grobid.core.engines.AbstractParser;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.trainer.evaluation.EvaluationUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: zholudev
 * Date: 11/21/11
 * Time: 2:25 PM
 */
public abstract class AbstractTrainer implements Trainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTrainer.class);
    public static final String OLD_MODEL_EXT = ".old";
    public static final String NEW_MODEL_EXT = ".new";

    protected final CRFPPTrainer crfppTrainer;

    protected GrobidModels model;
    private File trainDataPath;
    private File evalDataPath;
    private Tagger tagger;

    public AbstractTrainer(GrobidModels model) {
    	GrobidFactory.getInstance().createEngine();
        crfppTrainer = new CRFPPTrainer();
        this.model = model;
        this.trainDataPath = getTempTrainingDataPath();
        this.evalDataPath = getTempEvaluationDataPath();
    }


    @Override
    public void train() {
        File dataPath = trainDataPath;
        createCRFPPData(getCorpusPath(), dataPath);
        File tempModelPath = new File(GrobidProperties.getInstance().getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
        File oldModelPath = GrobidProperties.getInstance().getModelPath(model);
        crfppTrainer.train(getTemplatePath().getAbsolutePath(),
                dataPath.getAbsolutePath(),
                tempModelPath.getAbsolutePath(),
                GrobidProperties.getInstance().getNBThreads());

        if (!crfppTrainer.what().isEmpty()) {
            LOGGER.warn("CRF++ Trainer warnings:\n" + crfppTrainer.what());
        } else {
            LOGGER.info("No CRF++ Trainer warnings!");
        }
        //if we are here, that means that training succeeded
        renameModels(oldModelPath, tempModelPath);
    }

    protected void renameModels(File oldModelPath, File tempModelPath) {
        if (oldModelPath.exists()) {
            if (!oldModelPath.renameTo(new File(oldModelPath.getAbsolutePath() + OLD_MODEL_EXT))) {
                LOGGER.warn("Unable to rename old model file: " + oldModelPath.getAbsolutePath());
                return;
            }
        }

        if (!tempModelPath.renameTo(oldModelPath)) {
            LOGGER.warn("Unable to rename new model file: " + tempModelPath);
        }
    }

    @Override
    public String evaluate() {
        createCRFPPData(getEvalCorpusPath(), evalDataPath);
        return EvaluationUtilities.evaluateStandard(evalDataPath.getAbsolutePath(), getTagger());
    }

    protected final File getTempTrainingDataPath()  {
        try {
            return File.createTempFile(model.getModelName(), ".train", GrobidProperties.getInstance().getTempPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create a temporary training file for model: " + model);
        }
    }

    protected final File getTempEvaluationDataPath() {
        try {
            return File.createTempFile(model.getModelName(), ".test", GrobidProperties.getInstance().getTempPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create a temporary evaluation file for model: " + model);
        }
    }

    protected Tagger getTagger() {
        if (tagger == null) {
            tagger = AbstractParser.createTagger(model);
        }

        return tagger;
    }

    protected File getCorpusPath() {
        return GrobidProperties.getInstance().getCorpusPath(new File(new File("resources").getAbsolutePath()), model);
    }

    protected File getTemplatePath() {
        return getTemplatePath(model);
    }

    protected static File getTemplatePath(GrobidModels model) {
        return GrobidProperties.getInstance().getTemplatePath(new File(new File("resources").getAbsolutePath()), model);
    }

    protected File getEvalCorpusPath() {
        return GrobidProperties.getInstance().getEvalCorpusPath(new File(new File("resources").getAbsolutePath()), model);
    }

    public static File getEvalCorpusBasePath() {
        return new File(new File("resources/dataset/patent/evaluation").getAbsolutePath());
    }

    @Override
    public GrobidModels getModel() {
        return model;
    }


    public static void runTraining(Trainer trainer) {
        long start = System.currentTimeMillis();
        trainer.train();
        long end = System.currentTimeMillis();

        System.out.println("Model for " + trainer.getModel() + " created in " + (end - start) +
                " ms");

    }

    public static void runEvaluation(Trainer trainer) {
        long start = System.currentTimeMillis();
        try {
            String report = trainer.evaluate();
            System.out.println(report);
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while evaluating Grobid.", e);
        }
        long end = System.currentTimeMillis();
        System.out.println("Evaluation for " + trainer.getModel() + " model is realized in " + (end - start) + " ms");
    }

}
