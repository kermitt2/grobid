package org.grobid.core.utilities.crossref;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.apache.http.client.ClientProtocolException;
import org.grobid.core.utilities.crossref.CrossrefRequestListener.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request pool to get data from api.crossref.org without exceeding limits
 * supporting multi-thread.
 *
 * @author Vincent Kaestle, Patrice
 */
public class CrossrefClient {
	public static final Logger logger = LoggerFactory.getLogger(CrossrefRequestTask.class);
	
	private static volatile CrossrefClient instance;

	private volatile ExecutorService executorService;
		
	private static boolean limitAuto = true;
	private volatile TimedSemaphore timedSemaphore;

	// this list is used to maintain a list of Futures that were submitted,
	// that we can use to check if the requests are completed
	//private List<Future<?>> futures = new ArrayList<Future<?>>();
	private volatile Map<Long, List<Future<?>>> futures = new HashMap<>();

	public static CrossrefClient getInstance() {
        if (instance == null)
			getNewInstance();
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
    private CrossrefClient() {
		this.executorService = Executors.newCachedThreadPool();
		this.timedSemaphore = null;
		this.futures = new HashMap<>();
		setLimits(1, 1000);
	}

	public static void printLog(CrossrefRequest<?> request, String message) {
		logger.info((request != null ? request+": " : "")+message);
		//System.out.println((request != null ? request+": " : "")+message);
	}
	
	public void setLimits(int iterations, int interval) {
		if ((this.timedSemaphore == null)
			|| (this.timedSemaphore.getLimit() != iterations)
			|| (this.timedSemaphore.getPeriod() != interval)) {
			this.timedSemaphore = new TimedSemaphore(interval, TimeUnit.MILLISECONDS, iterations);
			//printLog(null, "!!!!!!!!!!!!!!!!!!!!!!! Setting timedSemaphore limits... " + iterations + " / " + interval);
		}
	}
	
	public void updateLimits(int iterations, int interval) {
		if (this.limitAuto) {
			//printLog(null, "Updating limits... " + iterations + " / " + interval);
			this.setLimits(iterations / 2, interval);
		}
	}
	
	public synchronized void checkLimits() throws InterruptedException {
		if (this.limitAuto) {
			synchronized(this.timedSemaphore) {
				printLog(null, "timedSemaphore acquire... current total: " + this.timedSemaphore.getAcquireCount() + 
					", still available: " + this.timedSemaphore.getAvailablePermits() );
				this.timedSemaphore.acquire();
			}
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
			Future<?> f = executorService.submit(new CrossrefRequestTask<T>(this, request));
			List<Future<?>> localFutures = this.futures.get(new Long(threadId));
			if (localFutures == null)
				localFutures = new ArrayList<Future<?>>();
			localFutures.add(f);
			this.futures.put(new Long(threadId), localFutures);
//System.out.println("add request to thread " + threadId + " / current total for the thread: " +  localFutures.size());			
		}
	}
	
	/**
	 * Push a request in pool to be executed soon as possible, then wait a response through the listener.
	 * @see <a href="https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md">Crossref API Documentation</a>
	 * 
	 * @param model 		key in crossref, ex: "works", "journals"..
	 * @param id 			model identifier, can be null, ex: doi for a work
	 * @param params		query parameters, can be null, ex: ?query.title=[title]&query.author=[author]
	 * @param deserializer	json response deserializer, ex: WorkDeserializer to convert Work to BiblioItem
	 * @param threadId		the java identifier of the thread providing the request (e.g. via Thread.currentThread().getId())
	 * @param listener		catch response from request
	 */
	public <T extends Object> void pushRequest(String model, String id, Map<String, String> params, CrossrefDeserializer<T> deserializer, 
			long threadId, CrossrefRequestListener<T> listener) throws URISyntaxException, ClientProtocolException, IOException {
		CrossrefRequest<T> request = new CrossrefRequest<T>(model, id, params, deserializer);
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
				List<Future<?>> threadFutures = this.futures.get(new Long(threadId));
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
	
}
