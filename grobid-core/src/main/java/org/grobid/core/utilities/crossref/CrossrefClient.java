package org.grobid.core.utilities.crossref;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.grobid.core.utilities.crossref.CrossrefRequestListener.Response;

/**
 * Request pool to get data from api.crossref.org without exceeding limits.
 *
 * @author Vincent Kaestle, Patrice
 */
public class CrossrefClient {
		
	/**
	 * Request distribution during time
	 */
	public static enum RequestMode {
		/**
		 * Send requests much as limit accepts in one interval, then stop until interval finished, then restart 
		 */
		MUCHTHENSTOP,
		/**
		 * Send requests regularly as limit accepts 
		 */
		REGULARLY
	}
	
	protected ExecutorService executorService;
	
	public Response<?> lastResponse;
	public RequestMode requestMode;
	public int itFromLastInterval;
	public long firstItTime;
		
	public CrossrefClient(RequestMode requestMode) {
		//this.executorService = Executors.newSingleThreadExecutor();
		//if (requestMode == RequestMode.MUCHTHENSTOP)*/
		this.executorService = Executors.newCachedThreadPool();
		this.lastResponse = null;
		this.requestMode = requestMode;
		this.itFromLastInterval = 0;
		this.firstItTime = System.currentTimeMillis();
	}
	
	public CrossrefClient() {
		this(RequestMode.MUCHTHENSTOP);
		//this(RequestMode.REGULARLY);
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
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
		 	//pool.shutdownNow(); // will be explicitely called by close()
		 	// Preserve interrupt status
		 	Thread.currentThread().interrupt();
		}
	} 
}
