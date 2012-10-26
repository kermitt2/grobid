package org.grobid.core.bactch;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;

public class CreateTrainingHeader {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err
					.println("Usage: CreateTrainingHeader <input directory> <output directory>");
			System.exit(1);
		}
		MockContext.setInitialContext();
		GrobidProperties.getInstance();

		Engine engine = GrobidFactory.getInstance().createEngine();
		engine.batchCreateTrainingHeader(args[0], args[1], -1);

		MockContext.destroyInitialContext();
	}

}
