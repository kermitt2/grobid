package org.grobid.core;

import org.grobid.core.engines.Engine;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.grobid.core.impl.GrobidPoolingFactoryImpl;

public interface GrobidPoolingFactory extends PoolableObjectFactory {
    public GenericObjectPool newPoolInstance = GrobidPoolingFactoryImpl.newPoolInstance();

    public GrobidPoolingFactory newInstance = org.grobid.core.impl.GrobidPoolingFactoryImpl.newInstance();
    public Boolean startUp = org.grobid.core.impl.GrobidPoolingFactoryImpl.init();

    public Engine createEngine();
}
