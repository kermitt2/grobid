package org.grobid.core;

import org.grobid.core.engines.Engine;

public class EngineFactory {

	private static Engine engine;

	public static Engine getEngine() {
		if (engine == null) {
			engine = new Engine();
		}
		return engine;
	}

}
