package org.grobid.core.impl;

import org.grobid.core.GrobidFactory;
import org.grobid.core.engines.Engine;
import org.grobid.core.main.LibraryLoader;

public class GrobidFactoryImpl implements GrobidFactory {

    /**
     * {@inheritDoc GrobidFactory#instance}
     */
    public static GrobidFactory instance() {
        if (factory == null)
            factory = newInstance();
        return (factory);
    }

    /**
     * {@inheritDoc GrobidFactory#newInstance}
     */
    public static GrobidFactory newInstance() {
    	init();
        return new GrobidFactoryImpl();
    }

    private static GrobidFactory factory = null;
    private static boolean isInited = false;

    /**
     * Initializes all necessary things for starting grobid. For instance the environmaent
     * variable {@link GrobidFactory#ENV_GROBID_HOME} is checked.
     */
    public static boolean init() {
        if (!isInited) {
            LibraryLoader.load();
            isInited = true;
        }

        return isInited;
    }

    public Engine createEngine() {
        Engine retVal = null;
        retVal = new Engine();

        return (retVal);
    }
}
