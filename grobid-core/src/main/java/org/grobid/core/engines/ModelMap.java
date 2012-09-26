package org.grobid.core.engines;

import java.util.HashMap;
import java.util.Map;

import org.chasen.crfpp.Model;
import org.chasen.crfpp.Tagger;
import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class the create a tagger from a given model or reuse it if it already
 * exists.
 * 
 */
public class ModelMap {

	/**
	 * The logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ModelMap.class);

	/**
	 * Contains all the taggers corresponding to theirs models.
	 */
	// private static Map<String, Tagger> taggers = null;

	/**
	 * Map that contains all the models loaded in memory.
	 */
	private static Map<String, Model> models = null;

	/**
	 * Return the model corresponding to the given path. Models are loaded in
	 * memory if they don't exist.
	 * 
	 * @param modelPath
	 *            the path to the model
	 * @return the model corresponding to the given path.
	 */
	protected static Model getModel(String modelPath) {
		LOGGER.debug("start getModel");
		if (models == null) {
			models = new HashMap<String, Model>();
		}
		if (models.get(modelPath) == null) {
			LOGGER.info("Loading model " + modelPath + " in memory");
			models.put(modelPath, new Model("-m " + modelPath + " "));
		}
		LOGGER.debug("end getModel");
		return models.get(modelPath);
	}

	public static Tagger getTagger(GrobidModels grobidModel) {
		LOGGER.debug("start getTagger");
		Tagger tagger;
		try {
			Model model = getModel(grobidModel.getModelPath());
			tagger = model.createTagger();
		} catch (Throwable thb) {
			throw new GrobidException("Cannot instantiate a tagger: " + thb);
		}
		LOGGER.debug("end getTagger");
		return tagger;
	}

	/*
	 * public static Tagger getTaggerOld(GrobidModels model) {
	 * LOGGER.debug("start getTagger"); if (taggers == null) { taggers = new
	 * HashMap<String, Tagger>(); } if (taggers.get(model) == null) {
	 * GrobidProperties.getInstance(); File modelPath =
	 * GrobidProperties.getModelPath(model);
	 * 
	 * if (!modelPath.exists()) { throw new
	 * RuntimeException("The file path to the " + model.name() +
	 * " CRF model is invalid: " + modelPath.getAbsolutePath()); } String cmd =
	 * "-m " + modelPath.getAbsolutePath() + " ";
	 * LOGGER.info("Parameters to CRF++ tagger for model {}: '{}'",
	 * model.name(), cmd); Tagger tagger; try { tagger = new Tagger(cmd);
	 * taggers.put(model.getModelName(), tagger);
	 * LOGGER.info("new Tagger added to TaggerMap: " + tagger); } catch
	 * (NoClassDefFoundError e) { throw new GrobidException(
	 * "Cannot instantiate a tagger for command '" + cmd + "'."); } catch
	 * (Throwable thb) { throw new GrobidException(
	 * "Cannot instantiate a tagger for command '" + cmd + "': " + thb); } }
	 * LOGGER.debug("end getTagger"); return taggers.get(model); }
	 */

}
