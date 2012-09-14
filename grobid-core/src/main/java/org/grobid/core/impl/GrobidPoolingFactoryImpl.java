package org.grobid.core.impl;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.grobid.core.GrobidPoolingFactory;
import org.grobid.core.engines.Engine;
import org.grobid.core.main.LibraryLoader;

public class GrobidPoolingFactoryImpl implements GrobidPoolingFactory {

	/**
	 * A pool which contains objects of type Engine for the conversion.
	 */
	private static volatile GenericObjectPool grobidEnginePool = null;
	private static volatile Boolean grobidEnginePoolControl = false;

	/**
	 * Creates a pool for {@link Engine} objects. So a number of objects is
	 * always available and ready to start immediatly.
	 * 
	 * @return
	 */
	public static GenericObjectPool newPoolInstance() {
		if (grobidEnginePool == null) {
			// initialize grobidEnginePool
			synchronized (grobidEnginePoolControl) {
				if (grobidEnginePool == null) {
					// TODO check if settings make sence in real application
					grobidEnginePool = new GenericObjectPool();
					grobidEnginePool
							.setFactory(GrobidPoolingFactory.newInstance);
					grobidEnginePool
							.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
					grobidEnginePool.setLifo(false);
					grobidEnginePool.setMinIdle(5);
					grobidEnginePool.setMaxIdle(30);
					grobidEnginePool.setMaxActive(30);
					grobidEnginePool.setTestWhileIdle(false);
					grobidEnginePool.setTimeBetweenEvictionRunsMillis(1000);
				}
			}
		}// initialize grobidEnginePool
		return (grobidEnginePool);
	}

	/**
	 * Creates and returns an instance of GROBIDFactory. The init() method will
	 * be called.
	 * 
	 * @return
	 */
	public static GrobidPoolingFactory newInstance() {
		init();
		return (new GrobidPoolingFactoryImpl());
	}

	private static boolean isInited = false;

	/**
	 * Initializes all necessary things for starting grobid. For instance the
	 * environmaent variable GrobidPoolingFactory.ENV_GROBID_HOME is checked.
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

	// --------------------- stuff for poolableFactory
	public void activateObject(Object arg0) throws Exception {
	}

	public void destroyObject(Object arg0) throws Exception {
	}

	public Object makeObject() throws Exception {
		return (createEngine());
	}

	public void passivateObject(Object arg0) throws Exception {
	}

	public boolean validateObject(Object arg0) {
		return false;
	}

}
