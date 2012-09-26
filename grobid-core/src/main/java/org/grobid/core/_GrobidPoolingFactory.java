package org.grobid.core;

import org.grobid.core.engines.Engine;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.grobid.core.impl._GrobidPoolingFactoryImpl;

public interface _GrobidPoolingFactory extends PoolableObjectFactory {
    public GenericObjectPool newPoolInstance = _GrobidPoolingFactoryImpl.newPoolInstance();

    public _GrobidPoolingFactory newInstance = org.grobid.core.impl._GrobidPoolingFactoryImpl.newInstance();
    public Boolean startUp = org.grobid.core.impl._GrobidPoolingFactoryImpl.init();

    public Engine createEngine();
}
