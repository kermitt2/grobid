package org.grobid.core.impl;

import org.grobid.core.engines.Engine;
import org.grobid.core.engines.ModelMap;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;

public class GrobidFactory {

	private static GrobidFactory factory = null;
	private static boolean isInited = false;

	// private static Engine engine = null;

	/**
	 * {@inheritDoc GrobidFactory#instance}
	 */
	public static GrobidFactory getInstance() {
		if (factory == null)
			factory = newInstance();
		return factory;
	}

	/**
	 * {@inheritDoc GrobidFactory#newInstance}
	 */
	protected static GrobidFactory newInstance() {
		init();
		return new GrobidFactory();
	}

	/**
	 * Initializes all necessary things for starting grobid. For instance the
	 * environmaent variable {@link GrobidFactory#ENV_GROBID_HOME} is checked.
	 */
	protected static boolean init() {
		if (!isInited) {
			LibraryLoader.load();
			ModelMap.initModels();
			GrobidProperties.getInstance();
			Lexicon.getInstance();
			isInited = true;
		}
		return isInited;
	}

	private static Engine engine;

	public synchronized Engine getEngine() {
		// System.out.println("Start getEngine");
		if (engine == null) {
			engine = createEngine();
		}
		// System.out.println("Stop getEngine");
		return engine;
	}

	public Engine createEngine() {
		Engine retVal = null;
		retVal = new Engine();
		return retVal;
	}
}
