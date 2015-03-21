package org.grobid.core.factory;

import java.util.NoSuchElementException;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.grobid.core.engines.Engine;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrobidPoolingFactory extends AbstractEngineFactory implements
		PoolableObjectFactory {

	/**
	 * A pool which contains objects of type Engine for the conversion.
	 */
	private static volatile GenericObjectPool grobidEnginePool = null;
	private static volatile Boolean grobidEnginePoolControl = false;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrobidPoolingFactory.class);

	/**
	 * Constructor.
	 */
	protected GrobidPoolingFactory() {
		fullInit();
	}

	/**
	 * Creates a pool for {@link Engine} objects. So a number of objects is
	 * always available and ready to start immediatly.
	 * 
	 * @return GenericObjectPool
	 */
	protected static GenericObjectPool newPoolInstance() {
		if (grobidEnginePool == null) {
			// initialize grobidEnginePool
			LOGGER.debug("synchronized newPoolInstance");
			synchronized (grobidEnginePoolControl) {
				if (grobidEnginePool == null) {
					grobidEnginePool = new GenericObjectPool(GrobidPoolingFactory.newInstance());
					//grobidEnginePool.setFactory(GrobidPoolingFactory.newInstance());
					grobidEnginePool
							.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
					grobidEnginePool.setMaxWait(GrobidProperties.getPoolMaxWait());
					grobidEnginePool.setMaxActive(GrobidProperties.getMaxPoolConnections());
					grobidEnginePool.setTestWhileIdle(false);
					grobidEnginePool.setLifo(false);
					grobidEnginePool.setTimeBetweenEvictionRunsMillis(2000);
					grobidEnginePool.setMaxIdle(0);
				}
			}
		}
		return grobidEnginePool;
	}

	/**
	 * Obtains an instance from this pool.<br>
	 * 
	 * By contract, clients must call {@link GrobidPoolingFactory#returnEngine}
	 * when they finish to use the engine.
	 */
	public static synchronized Engine getEngineFromPool() {
		if (grobidEnginePool == null) {
			grobidEnginePool = newPoolInstance();
		}
		Engine engine = null;
		try {
			engine = (Engine) grobidEnginePool.borrowObject();
		} catch (NoSuchElementException nseExp) {
			throw new NoSuchElementException();
		} catch (Exception exp) {
			throw new GrobidException("An error occurred while getting an engine from the engine pool", exp);
		}
		LOGGER.info("Number of Engines in pool active/max: "
				+ grobidEnginePool.getNumActive() + "/"
				+ grobidEnginePool.getMaxActive());
		return engine;
	}

	/**
	 * By contract, engine must have been obtained using
	 * {@link GrobidPoolingFactory#getEngineFromPool}.<br>
	 */
	public static void returnEngine(Engine engine) {
		try {
			//engine.close();
			grobidEnginePool.returnObject(engine);
		} catch (Exception exp) {
			throw new GrobidException(
					"An error occurred while returning an engine from the engine pool", exp);
		}
	}

	/**
	 * Creates and returns an instance of GROBIDFactory. The init() method will
	 * be called.
	 * 
	 * @return
	 */
	protected static GrobidPoolingFactory newInstance() {
		return new GrobidPoolingFactory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activateObject(Object arg0) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroyObject(Object arg0) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object makeObject() throws Exception {
		return (createEngine());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void passivateObject(Object arg0) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validateObject(Object arg0) {
		return false;
	}

}
