package org.grobid.core.utilities.crossref;

import java.time.Duration;
import java.util.List;

/**
 * Listener to catch response from a CrossrefRequest.
 *
 * @author Vincent Kaestle
 */
public abstract class CrossrefRequestListener<T extends Object> {
	
	
	public static class Response<T> {
		public int status = -1;
		public List<T> results = null;
		public int interval;
		public int limitIterations;
		public long time;
		public String errorMessage;
		public Exception errorException;
		
		public Response() {
			this.status = -1;
			this.results = null;
			this.interval = 0;
			this.limitIterations = 1;
			this.time = System.currentTimeMillis();
			this.errorMessage = null;
			this.errorException = null;
		}
		
		public void setTimeLimit(String limitInterval, String limitLimit) {
			this.interval = (int)Duration.parse("PT"+limitInterval.toUpperCase()).toMillis();
			this.limitIterations = Integer.parseInt(limitLimit);
		}
		
		public void setException(Exception e, CrossrefRequest<T> request) {
			errorException = e;
			errorMessage = e.getClass().getName()+" thrown during request execution : "+request.toString()+"\n"+e.getMessage();
		}
		
		public int getOneStepTime() {
			return interval/limitIterations;
		}
		
		public String toSring() {
			return "Response (status:"+status+" timeLimit:"+interval+"/"+limitIterations+", results:"+results.size();
		}
	}
	
	/**
	 * Called when request executed and get any response
	 */
	public void onResponse(Response<T> response) {}
	
	/**
	 * Called when request succeed and response format is as expected
	 */
	public abstract void onSuccess(List<T> results);
	
	/**
	 * Called when request gives an error
	 */
	public abstract void onError(int status, String message, Exception exception);

	public void notify(Response<T> response) {
		
		onResponse(response);
		
		if (response.results != null && response.results.size() > 0)
			onSuccess(response.results);
		
		if (response.errorMessage != null || response.errorException != null)
			onError(response.status, response.errorMessage, response.errorException);
	}
}
