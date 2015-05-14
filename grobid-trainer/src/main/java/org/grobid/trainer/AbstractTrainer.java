package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.engines.tagging.TaggerFactory;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.trainer.evaluation.EvaluationUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Zholudev, Lopez
 */
public abstract class AbstractTrainer implements Trainer {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTrainer.class);
	public static final String OLD_MODEL_EXT = ".old";
	public static final String NEW_MODEL_EXT = ".new";

	// default training parameters (only exploited by Wapiti)
	protected double epsilon = 0.0; // default size of the interval for stopping criterion
	protected int window = 0; // default similar to CRF++

	protected GrobidModels model;
	private File trainDataPath;
	private File evalDataPath;
	private GenericTagger tagger;

	public AbstractTrainer(final GrobidModels model) {
		GrobidFactory.getInstance().createEngine();
		this.model = model;
		this.trainDataPath = getTempTrainingDataPath();
		this.evalDataPath = getTempEvaluationDataPath();
	}

	@Override
	public void train() {
		final File dataPath = trainDataPath;
		createCRFPPData(getCorpusPath(), dataPath);
        GenericTrainer trainer = TrainerFactory.getTrainer();
		if (epsilon != 0.0)
			trainer.setEpsilon(epsilon);
		if (window != 0)
			trainer.setWindow(window);
        final File tempModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
        final File oldModelPath = GrobidProperties.getModelPath(model);
        trainer.train(getTemplatePath(), dataPath, tempModelPath, GrobidProperties.getNBThreads(), model);
		// if we are here, that means that training succeeded
		renameModels(oldModelPath, tempModelPath);
	}

	protected void renameModels(final File oldModelPath, final File tempModelPath) {
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

	@Override
	public String splitTrainEvaluate(Double split) {
		final File dataPath = trainDataPath;
		createCRFPPData(getCorpusPath(), dataPath, evalDataPath, split);
        GenericTrainer trainer = TrainerFactory.getTrainer();
        final File tempModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
        final File oldModelPath = GrobidProperties.getModelPath(model);

        trainer.train(getTemplatePath(), dataPath, tempModelPath, GrobidProperties.getNBThreads(), model);

		// if we are here, that means that training succeeded
		renameModels(oldModelPath, tempModelPath);
		
		return EvaluationUtilities.evaluateStandard(evalDataPath.getAbsolutePath(), getTagger());
	}

	protected final File getTempTrainingDataPath() {
		try {
			return File.createTempFile(model.getModelName(), ".train", GrobidProperties.getTempPath());
		} catch (IOException e) {
			throw new RuntimeException("Unable to create a temporary training file for model: " + model);
		}
	}

	protected final File getTempEvaluationDataPath() {
		try {
			return File.createTempFile(model.getModelName(), ".test", GrobidProperties.getTempPath());
		} catch (IOException e) {
			throw new RuntimeException("Unable to create a temporary evaluation file for model: " + model);
		}
	}

	protected GenericTagger getTagger() {
		if (tagger == null) {
			tagger = TaggerFactory.getTagger(model);
		}

		return tagger;
	}

	protected static File getFilePath2Resources() {
		File theFile = new File(GrobidProperties.get_GROBID_HOME_PATH().getAbsoluteFile() + File.separator + ".." + File.separator
				+ "grobid-trainer" + File.separator + "resources");
		if (!theFile.exists()) {
			theFile = new File("resources");
		}		
		return theFile;		
	}

	protected final File getCorpusPath() {
		return GrobidProperties.getCorpusPath(getFilePath2Resources(), model);
	}

	protected final File getTemplatePath() {
		return getTemplatePath(model);
	}

	protected static File getTemplatePath(final GrobidModels model) {
		return GrobidProperties.getTemplatePath(getFilePath2Resources(), model);
	}

	protected File getEvalCorpusPath() {
		return GrobidProperties.getEvalCorpusPath(getFilePath2Resources(), model);
	}

	public static File getEvalCorpusBasePath() {
		final String path2Evelutation = getFilePath2Resources().getAbsolutePath() + File.separator + "dataset" + File.separator + "patent"
				+ File.separator + "evaluation";
		return new File(path2Evelutation);
	}

	@Override
	public GrobidModels getModel() {
		return model;
	}

	public static void runTraining(final Trainer trainer) {
		long start = System.currentTimeMillis();
		trainer.train();
		long end = System.currentTimeMillis();

		System.out.println("Model for " + trainer.getModel() + " created in " + (end - start) + " ms");

	}

    public File getEvalDataPath() {
        return evalDataPath;
    }

    public static void runEvaluation(final Trainer trainer) {
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
	
	public static void runSplitTrainingEvaluation(final Trainer trainer, Double split) {
		long start = System.currentTimeMillis();
		try { 
			String report = trainer.splitTrainEvaluate(split);
			System.out.println(report);
		} catch (Exception e) {
			throw new GrobidException("An exception occurred while evaluating Grobid.", e);
		}
		long end = System.currentTimeMillis();
		System.out.println("Split, training and evaluation for " + trainer.getModel() + " model is realized in " + (end - start) + " ms");
	}


	
}
