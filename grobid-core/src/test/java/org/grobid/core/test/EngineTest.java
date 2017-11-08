package org.grobid.core.test;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * User: zholudev Date: 11/21/11 Time: 7:17 PM
 */
public abstract class EngineTest {
    protected static Engine engine;

    @BeforeClass
    public static void setUpClass() throws Exception {
        engine = GrobidFactory.getInstance().getEngine();
    }

    @AfterClass
    public static void closeResources() throws Exception {
        engine.close();
    }
}
