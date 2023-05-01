package org.grobid.core.utilities.glutton;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.ClientProtocolException;
import org.grobid.core.utilities.crossref.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to the Glutton bibliographical service
 *
 */
public class GluttonClient extends CrossrefClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(GluttonClient.class);
    
    private static volatile GluttonClient instance;

    //private volatile ExecutorService executorService;
        
    //private static boolean limitAuto = true;
    //private volatile TimedSemaphore timedSemaphore;

    // this list is used to maintain a list of Futures that were submitted,
    // that we can use to check if the requests are completed
    //private volatile Map<Long, List<Future<?>>> futures = new HashMap<>();

    public static GluttonClient getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
    private static synchronized void getNewInstance() {
        LOGGER.debug("Get new instance of GluttonClient");
        instance = new GluttonClient();
    }

    /**
     * Hidden constructor
     */
    private GluttonClient() {
        super();
        /*this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
        this.timedSemaphore = null;
        this.futures = new HashMap<>();*/
        int nThreads = Runtime.getRuntime().availableProcessors();
        //int nThreads = (int) Math.ceil((double)Runtime.getRuntime().availableProcessors() / 2);
        LOGGER.debug("nThreads: " + nThreads);
        this.executorService = Executors.newFixedThreadPool(nThreads*2);
        //setLimits(20, 1000); // default calls per second
    }

    /*public void setLimits(int iterations, int interval) {
        if ((this.timedSemaphore == null)
            || (this.timedSemaphore.getLimit() != iterations)
            || (this.timedSemaphore.getPeriod() != interval)) {
            // custom executor to prevent stopping JVM from exiting
            this.timedSemaphore = new TimedSemaphore(new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                }
            }), interval, TimeUnit.MILLISECONDS, iterations);
        }
    }*/

    /*public synchronized void checkLimits() throws InterruptedException {
        if (this.limitAuto) {
            synchronized(this.timedSemaphore) {
                printLog(null, "timedSemaphore acquire... current total: " + this.timedSemaphore.getAcquireCount() + 
                    ", still available: " + this.timedSemaphore.getAvailablePermits() );
                this.timedSemaphore.acquire();
            }
        }
    }*/

    public static void printLog(GluttonRequest<?> request, String message) {
        LOGGER.debug((request != null ? request+": " : "")+message);
        //System.out.println((request != null ? request+": " : "")+message);
    }

    /**
     * Push a request in pool to be executed as soon as possible, then wait a response through the listener.
     */
    public <T extends Object> void pushRequest(GluttonRequest<T> request, CrossrefRequestListener<T> listener, 
        long threadId) throws URISyntaxException, ClientProtocolException, IOException {
        if (listener != null)
            request.addListener(listener);
        synchronized(this) {
            Future<?> f = executorService.submit(new GluttonRequestTask<T>(this, request));
            List<Future<?>> localFutures = this.futures.get(Long.valueOf(threadId));
            if (localFutures == null)
                localFutures = new ArrayList<Future<?>>();
            localFutures.add(f);
            this.futures.put(Long.valueOf(threadId), localFutures);
//System.out.println("add request to thread " + threadId + " / current total for the thread: " +  localFutures.size());         
        }
    }
    
    /**
     * Push a request in pool to be executed soon as possible, then wait a response through the listener.
     * 
     * @param params        query parameters, can be null, ex: ?query.title=[title]&query.author=[author]
     * @param deserializer  json response deserializer, ex: WorkDeserializer to convert Work to BiblioItem
     * @param threadId      the java identifier of the thread providing the request (e.g. via Thread.currentThread().getId())
     * @param listener      catch response from request
     */
    @Override
    public <T extends Object> void pushRequest(String model, Map<String, String> params, CrossrefDeserializer<T> deserializer, 
            long threadId, CrossrefRequestListener<T> listener) throws URISyntaxException, ClientProtocolException, IOException {
        GluttonRequest<T> request = new GluttonRequest<T>(model, params, deserializer);
        synchronized(this) {
            this.<T>pushRequest(request, listener, threadId);
        }
    }

    /**
     * Wait for all request from a specific thread to be completed
     */
    /*public void finish(long threadId) {
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
                logger.error("Glutton request execution fails");
            }
        }
    }*/

    /*@Override
    public void close() throws IOException {
        timedSemaphore.shutdown();
    }*/
}