package org.grobid.core.utilities.crossref;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.apache.http.client.ClientProtocolException;
import org.grobid.core.utilities.crossref.CrossrefRequestListener.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request pool to get data from api.crossref.org without exceeding limits
 * supporting multi-thread.
 *
 * Note: the provided interval for the query rate returned by CrossRef appeared to be note reliable, 
 * so we have to use the rate limit (X-Rate-Limit-Interval) as a global parallel query limit, without 
 * interval consideration.  
 * See https://github.com/kermitt2/grobid/pull/725
 * 
 */
public class CrossrefClient implements Closeable {
	public static final Logger logger = LoggerFactory.getLogger(CrossrefClient.class);
	
	protected static volatile CrossrefClient instance;

	protected volatile ExecutorService executorService;

	protected int max_pool_size = 1;
	protected static boolean limitAuto = true;

	// this list is used to maintain a list of Futures that were submitted,
	// that we can use to check if the requests are completed
	//private List<Future<?>> futures = new ArrayList<Future<?>>();
	protected volatile Map<Long, List<Future<?>>> futures = new HashMap<>();

	public static CrossrefClient getInstance() {
        if (instance == null) {
			getNewInstance();
		}
        return instance;
    }

    /**
     * Creates a new instance.
     */
	private static synchronized void getNewInstance() {
		logger.debug("Get new instance of CrossrefClient");
		instance = new CrossrefClient();
	}

    /**
     * Hidden constructor
     */
    protected CrossrefClient() {
    	// note: by default timeout with newCachedThreadPool is set to 60s, which might be too much for crossref usage,
    	// hanging grobid significantly, so we might want to use rather a custom instance of ThreadPoolExecutor and set 
    	// the timeout sifferently
		this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
		this.futures = new HashMap<>();
		setLimits(1, 1000);
	}

	public static void printLog(CrossrefRequest<?> request, String message) {
		logger.debug((request != null ? request+": " : "")+message);
		//System.out.println((request != null ? request+": " : "")+message);
	}
	
	public void setLimits(int iterations, int interval) {
		this.setMax_pool_size(iterations);
		// interval is not usable anymore, we need to wait termination of threads independently from any time interval
	}
	
	public void updateLimits(int iterations, int interval) {
		if (this.limitAuto) {
			//printLog(null, "Updating limits... " + iterations + " / " + interval);
			this.setLimits(iterations, interval);
			// note: interval not used anymore
		}
	}
	
	/**
	 * Push a request in pool to be executed as soon as possible, then wait a response through the listener.
	 * API Documentation : https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md
	 */
	public <T extends Object> void pushRequest(CrossrefRequest<T> request, CrossrefRequestListener<T> listener, 
		long threadId) throws URISyntaxException, ClientProtocolException, IOException {
		if (listener != null)
			request.addListener(listener);
		synchronized(this) {
			// we limit the number of active threads to the crossref api dynamic limit returned in the response header
			while(((ThreadPoolExecutor)executorService).getActiveCount() >= this.getMax_pool_size()) {
				try {
					TimeUnit.MICROSECONDS.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Future<?> f = executorService.submit(new CrossrefRequestTask<T>(this, request));
			List<Future<?>> localFutures = this.futures.get(Long.valueOf(threadId));
			if (localFutures == null)
				localFutures = new ArrayList<Future<?>>();
			localFutures.add(f);
			this.futures.put(threadId, localFutures);
			logger.debug("add request to thread " + threadId +
					"active threads count is now " + ((ThreadPoolExecutor) executorService).getActiveCount()
			);
//System.out.println("add request to thread " + threadId + " / current total for the thread: " +  localFutures.size());			
		}
	}
	
	/**
	 * Push a request in pool to be executed soon as possible, then wait a response through the listener.
	 * @see <a href="https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md">Crossref API Documentation</a>
	 * 
	 * @param params		query parameters, can be null, ex: ?query.title=[title]&query.author=[author]
	 * @param deserializer	json response deserializer, ex: WorkDeserializer to convert Work to BiblioItem
	 * @param threadId		the java identifier of the thread providing the request (e.g. via Thread.currentThread().getId())
	 * @param listener		catch response from request
	 */
	public <T extends Object> void pushRequest(String model, Map<String, String> params, CrossrefDeserializer<T> deserializer, 
			long threadId, CrossrefRequestListener<T> listener) throws URISyntaxException, ClientProtocolException, IOException {
		CrossrefRequest<T> request = new CrossrefRequest<T>(model, params, deserializer);
		synchronized(this) {
			this.<T>pushRequest(request, listener, threadId);
		}
	}

	/**
	 * Wait for all request from a specific thread to be completed
	 */
	public void finish(long threadId) {
		synchronized(this.futures) {
			try {
				List<Future<?>> threadFutures = this.futures.get(Long.valueOf(threadId));
				if (threadFutures != null) {
//System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< thread: " + threadId + " / waiting for " + threadFutures.size() + " requests to finish...");
					for(Future<?> future : threadFutures) {
						future.get();
						// get will block until the future is done
					}
					this.futures.remove(threadId);
				}
			} catch (InterruptedException ie) {
			 	// Preserve interrupt status
			 	Thread.currentThread().interrupt();
			} catch (ExecutionException ee) {
				logger.error("CrossRef request execution fails");
			}
		}
	}

	public int getMax_pool_size() {
		return max_pool_size;
	}

	public void setMax_pool_size(int max_pool_size) {
		this.max_pool_size = max_pool_size;
	}

	@Override
	public void close() throws IOException {
		executorService.shutdown();
	}
}
