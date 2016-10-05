package org.grobid.core.test;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * User: zholudev Date: 11/21/11 Time: 7:17 PM
 */
public abstract class EngineTest {
    protected static Engine engine;

    @BeforeClass
    public static void setUpClass() throws Exception {
        MockContext.setInitialContext();
        engine = GrobidFactory.getInstance().createEngine();
    }

    @AfterClass
    public static void destroyInitialContext() throws Exception {
        MockContext.destroyInitialContext();
    }
}
