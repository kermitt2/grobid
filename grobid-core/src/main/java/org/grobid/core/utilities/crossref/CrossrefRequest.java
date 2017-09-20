package org.grobid.core.utilities.crossref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * GET crossref request
 * @see <a href="https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md">Crossref API Documentation</a>
 *
 * @author Vincent Kaestle
 */
public class CrossrefRequest<T extends Object> extends Observable {

	protected static final String BASE_URL = "https://api.crossref.org";
	
	/**
	 * Model key in crossref, ex: "works", "journals"..
	 * @see <a href="https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md">Crossref API Documentation</a>
	 */
	public String model;
	
	/**
	 * Model identifier in crossref, can be null, ex: doi for a work
	 * @see <a href="https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md">Crossref API Documentation</a> 
	 */
	public String id;

	/**
	 * Query parameters, can be null, ex: ?query.title=[title]&query.author=[author]
	 * @see <a href="https://github.com/CrossRef/rest-api-doc/blob/master/rest_api.md">Crossref API Documentation</a>
	 */
	public Map<String, String> params;

	/**
	 * JSON response deserializer, ex: WorkDeserializer to convert Work to BiblioItem
	 */
	protected CrossrefDeserializer<T> deserializer;
	
	protected ArrayList<CrossrefRequestListener<T>> listeners;
	
	public CrossrefRequest(String model, String id, Map<String, String> params, CrossrefDeserializer<T> deserializer) {
		this.model = model;
		this.id = id;
		this.params = params;
		this.deserializer = deserializer;
		this.listeners = new ArrayList<CrossrefRequestListener<T>>();
	}
	
	/**
	 * Add listener to catch response when request is executed.
	 */
	public void addListener(CrossrefRequestListener<T> listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Notify all connected listeners
	 */
	protected void notifyListeners(CrossrefRequestListener.Response<T> message) {
		for (CrossrefRequestListener<T> listener : listeners)
			listener.notify(message);
	}
	
	/**
	 * Execute request, handle response by sending to listeners a CrossrefRequestListener.Response
	 */
	public void execute() {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		try {
			URIBuilder uriBuilder = new URIBuilder(BASE_URL);
			
			String path = model;
			if (id != null && !id.isEmpty())
				path += "/"+id;
			
			uriBuilder.setPath(path);
		    
			if (params != null)
				for (Entry<String, String> cursor : params.entrySet())
					uriBuilder.setParameter(cursor.getKey(), cursor.getValue());
			
            HttpGet httpget = new HttpGet(uriBuilder.build());
            
            ResponseHandler<Void> responseHandler = new ResponseHandler<Void>() {

				@Override
				public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					
					CrossrefRequestListener.Response<T> message = new CrossrefRequestListener.Response<T>();
					
					message.status = response.getStatusLine().getStatusCode();
					
					Header limitIntervalHeader = response.getFirstHeader("X-Rate-Limit-Interval");
					Header limitLimitHeader = response.getFirstHeader("X-Rate-Limit-Limit");
					if (limitIntervalHeader != null && limitLimitHeader != null)
						message.setTimeLimit(limitIntervalHeader.getValue(), limitLimitHeader.getValue());
					
					if (message.status < 200 || message.status >= 300) {
						message.errorMessage = response.getStatusLine().getReasonPhrase();
						notifyListeners(message);
					}
					
					HttpEntity entity = response.getEntity();
					
					if (entity != null) {
						String body = EntityUtils.toString(entity);
						message.results = deserializer.parse(body);
					}
					
					notifyListeners(message);

					return null;
				}
            	
            };
            
            httpclient.execute(httpget, responseHandler);
            
		} catch (Exception e) {
			
			CrossrefRequestListener.Response<T> message = new CrossrefRequestListener.Response<T>();
			message.setException(e, this);
			notifyListeners(message);
        } finally {
            try {
				httpclient.close();
			} catch (IOException e) {			
				CrossrefRequestListener.Response<T> message = new CrossrefRequestListener.Response<T>();
				message.setException(e, this);
				notifyListeners(message);
				
			}
        }
	}
	
	public String toString() {
		String str = model+"/";
		if (id != null)
			str += id;
		str += " (";
		if (params != null) {
			for (Entry<String, String> cursor : params.entrySet())
				str += ","+cursor.getKey()+"="+cursor.getValue();
		}
		str += ")";
		return str;
	}
}
