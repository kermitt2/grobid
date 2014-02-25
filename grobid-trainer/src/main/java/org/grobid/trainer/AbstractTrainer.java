package org.grobid.trainer;

import java.io.*;

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
 * User: zholudev Date: 11/21/11 Time: 2:25 PM
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

	public AbstractTrainer(final GrobidModels model) {
		GrobidFactory.getInstance().createEngine();
		crfppTrainer = new CRFPPTrainer();
		this.model = model;
		this.trainDataPath = getTempTrainingDataPath();
		this.evalDataPath = getTempEvaluationDataPath();
	}

	@Override
	public void train() {
		final File dataPath = trainDataPath;
		createCRFPPData(getCorpusPath(), dataPath);
		final File tempModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
		final File oldModelPath = GrobidProperties.getModelPath(model);
		crfppTrainer.train(getTemplatePath().getAbsolutePath(), dataPath.getAbsolutePath(), tempModelPath.getAbsolutePath(),
				GrobidProperties.getNBThreads());

		if (!crfppTrainer.what().isEmpty()) {
			LOGGER.warn("CRF++ Trainer warnings:\n" + crfppTrainer.what());
		} else {
			LOGGER.info("No CRF++ Trainer warnings!");
		}
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
		final File tempModelPath = new File(GrobidProperties.getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
		final File oldModelPath = GrobidProperties.getModelPath(model);
		crfppTrainer.train(getTemplatePath().getAbsolutePath(), dataPath.getAbsolutePath(), tempModelPath.getAbsolutePath(),
				GrobidProperties.getNBThreads());

		if (!crfppTrainer.what().isEmpty()) {
			LOGGER.warn("CRF++ Trainer warnings:\n" + crfppTrainer.what());
		} else {
			LOGGER.info("No CRF++ Trainer warnings!");
		}
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

	protected Tagger getTagger() {
		if (tagger == null) {
			tagger = AbstractParser.createTagger(model);
		}

		return tagger;
	}

	protected static final File getFilePath2Resources() {
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
