package org.grobid.core.utilities.crossref;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.apache.http.client.ClientProtocolException;
import org.grobid.core.utilities.crossref.CrossrefRequestListener.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request pool to get data from api.crossref.org without exceeding limits.
 *
 * @author Vincent Kaestle, Patrice
 */
public class CrossrefClient {
	
	public static final Logger logger = LoggerFactory.getLogger(CrossrefRequestTask.class);
	
	public static void printLog(CrossrefRequest<?> request, String message) {
		logger.info((request != null ? request+": " : "")+message);
		//System.out.println((request != null ? request+": " : "")+message);
	}
	
	
	protected ExecutorService executorService;
	
	public Response<?> lastResponse;
	
	private boolean limitAuto;
	private int iterationsOffset;
	private TimedSemaphore timedSemaphore;
	private CountDownLatch firstDoneSignal = null;
	
	
	public CrossrefClient() {
		this.executorService = Executors.newCachedThreadPool();
		this.lastResponse = null;
		this.limitAuto = true;
		this.timedSemaphore = null;
		this.iterationsOffset = -12;
	}
	
	public CrossrefClient(int iterations, int interval) {
		this();
		this.limitAuto = false;
		this.iterationsOffset = 0;
		this.setLimits(iterations, interval);
	}
	
	public void setIterationsOffset(int iterationsOffset) {
		this.iterationsOffset = iterationsOffset;
	}
	
	public void setLimits(int iterations, int interval) {
		if ((this.timedSemaphore == null)
			|| (this.timedSemaphore.getLimit() != iterations)
			|| (this.timedSemaphore.getPeriod() != interval)) {
			
			this.timedSemaphore = new TimedSemaphore(interval, TimeUnit.MILLISECONDS, iterations);
		}
	}
	
	public void updateLimits(int iterations, int interval) {
		if (this.limitAuto) {
			//printLog(null, "Updating limits..");
			this.setLimits(iterations + this.iterationsOffset, interval);
		}
	}
	
	public void checkLimits() throws InterruptedException {
		if (this.limitAuto && (firstDoneSignal.getCount() == 0))
			this.timedSemaphore.acquire();
	}
	
	
	public void checkFirstDone() throws InterruptedException {
		if (this.limitAuto) {
			synchronized(this) {
				if (firstDoneSignal == null)
					firstDoneSignal = new CountDownLatch(1);
				else
					firstDoneSignal.await();
			}
		}
	}
	
	public void emitFirstDoneSignal() {
		if (this.limitAuto && (firstDoneSignal.getCount() > 0)) {
			//printLog(null, "Emit firstDoneSignal !");
			firstDoneSignal.countDown();
		}
	}
	
	/**
	 * Push a request in pool to be executed as soon as possible, then wait a response through the listener.
	 * API Documentation : https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md
	 */
	public <T extends Object> void pushRequest(CrossrefRequest<T> request, CrossrefRequestListener<T> listener) throws URISyntaxException, ClientProtocolException, IOException {
		if (listener != null)
			request.addListener(listener);
		executorService.submit(new CrossrefRequestTask<T>(this, request));
	}
	
	/**
	 * Push a request in pool to be executed soon as possible, then may wait a response through the listener previously added to request.
	 * API Documentation : https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md 
	 */
	public <T extends Object> void pushRequest(CrossrefRequest<T> request) throws URISyntaxException, ClientProtocolException, IOException {
		this.<T>pushRequest(request, null);
	}
	
	/**
	 * Push a request in pool to be executed soon as possible, then wait a response through the listener.
	 * @see <a href="https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md">Crossref API Documentation</a>
	 * 
	 * @param model 		key in crossref, ex: "works", "journals"..
	 * @param id 			model identifier, can be null, ex: doi for a work
	 * @param params		query parameters, can be null, ex: ?query.title=[title]&query.author=[author]
	 * @param deserializer	json response deserializer, ex: WorkDeserializer to convert Work to BiblioItem
	 * @param listener		catch response from request
	 */
	public <T extends Object> void pushRequest(String model, String id, Map<String, String> params, CrossrefDeserializer<T> deserializer, CrossrefRequestListener<T> listener) throws URISyntaxException, ClientProtocolException, IOException {
		CrossrefRequest<T> request = new CrossrefRequest<T>(model, id, params, deserializer);
		this.<T>pushRequest(request, listener);
	}

	/**
	 * Ensure that all Executors are shut down to avoid JVM not exiting 
	 */
	public void close() {
		executorService.shutdownNow();
	}
	
	/**
	 * Wait for all request to be completed
	 */
	public void finish() {
		try {
			executorService.shutdown();
			executorService.awaitTermination(20, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
		 	//pool.shutdownNow(); // will be explicitely called by close()
		 	// Preserve interrupt status
		 	Thread.currentThread().interrupt();
		}
	} 
}
