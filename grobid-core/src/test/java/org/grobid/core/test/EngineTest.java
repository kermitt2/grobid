package org.grobid.core.test;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;

/**
 * User: zholudev Date: 11/21/11 Time: 7:17 PM
 */
public abstract class EngineTest {
	protected static Engine engine;

	@AfterClass
	public static void destroyInitialContext() throws Exception {
		MockContext.destroyInitialContext();
	}

	static {
		try {
			MockContext.setInitialContext();
		} catch (Exception e) {
		}
		engine = GrobidFactory.getInstance().createEngine();
	}
}
