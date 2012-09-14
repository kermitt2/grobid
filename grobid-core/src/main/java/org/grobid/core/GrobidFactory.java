package org.grobid.core;

import org.grobid.core.engines.Engine;

public interface GrobidFactory {

	/**
	 * Returns the current instance of the {@link GrobidFactory}. If there is none, a new instance will
     * be created.
     *
     * @return an instance of {@link GrobidFactory}
     */
    public GrobidFactory instance = org.grobid.core.impl.GrobidFactoryImpl.instance();
    
    /**
	 * Returns always a new instance the {@link GrobidFactory}.
     *
     * @return an instance of {@link GrobidFactory}
     */
    public GrobidFactory newInstance = org.grobid.core.impl.GrobidFactoryImpl.newInstance();

    public Engine createEngine();
}
