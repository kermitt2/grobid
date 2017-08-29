package org.grobid.core.utilities.crossref;

import java.util.List;

import org.grobid.core.utilities.crossref.CrossrefClient.RequestMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task to execute its request at the right time.
 *
 * @author Vincent Kaestle
 */
public class CrossrefRequestTask<T extends Object> extends CrossrefRequestListener<T> implements Runnable {
	
	public static final Logger logger = LoggerFactory
			.getLogger(CrossrefRequestTask.class);


	public static void printLog(CrossrefRequest<?> request, String message) {
		logger.info((request != null ? request+": " : "")+message);
	}
	
	
	protected CrossrefClient client;
	protected CrossrefRequest<T> request;
	
	public CrossrefRequestTask(CrossrefClient client, CrossrefRequest<T> request) {
		this.client = client;
		this.request = request;
		
		printLog(request, "New request in the pool");
	}
	
	@Override
	public void run() {
			
		int stepElapsedTime =  client.lastResponse != null ? (int)(System.currentTimeMillis() - client.lastResponse.time) : 0;
		printLog(request, "Trying at "+stepElapsedTime+"ms from last reponse");
		
		try {
			if (client.requestMode == RequestMode.REGULARLY) {
				
				if (client.lastResponse != null && stepElapsedTime <= client.lastResponse.getOneStepTime()) {
					int sleepTime = (int)(client.lastResponse.getOneStepTime()-stepElapsedTime);
					printLog(request, ".. but sleep for "+sleepTime+"ms");
					Thread.sleep(sleepTime);
				}
				
				printLog(request, ".. executing at "+(client.lastResponse != null ? (int)(System.currentTimeMillis() - client.lastResponse.time) : 0)+"ms from last reponse");
			}
			else if (client.requestMode == RequestMode.MUCHTHENSTOP) {
				
				long intervalElapsedTime =  System.currentTimeMillis() - client.firstItTime;
				
				if (client.lastResponse != null && (client.itFromLastInterval > client.lastResponse.limitIterations) && (intervalElapsedTime < client.lastResponse.interval)) {
					int sleepTime = (int)(client.lastResponse.interval-intervalElapsedTime);
					printLog(request, ".. but sleep for "+sleepTime+"ms");
					Thread.sleep(sleepTime);
				}
				
				printLog(request, ".. executing at "+(System.currentTimeMillis() - client.firstItTime)+"ms from last interval ("+client.itFromLastInterval+"it)");
			}
			
			request.addListener(this);
			request.execute();
			
			
		} catch (Exception e) {
			Response<T> message = new Response<T>();
			message.setException(e, request);
			request.notifyListeners(message);
		}
		
		printLog(request, "Task finished");
	}
	
	@Override
	public void onResponse(Response<T> response) {
		client.lastResponse = response;
		
		if (client.requestMode == RequestMode.MUCHTHENSTOP) {
			client.itFromLastInterval++;
			
			long intervalElapsedTime =  System.currentTimeMillis() - client.firstItTime;
			
			if (intervalElapsedTime > client.lastResponse.interval) {
				client.firstItTime = System.currentTimeMillis();
				client.itFromLastInterval = 1;
				printLog(null, "New interval !");
			}
		}
	}

	@Override
	public void onSuccess(List<T> results) {}

	@Override
	public void onError(int status, String message, Exception exception) {}

}
