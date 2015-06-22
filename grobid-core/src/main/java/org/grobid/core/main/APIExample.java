package org.grobid.core.main;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * Simple main class to benchmark a Grobid configuration
 */
public class APIExample {

    private final static int numSecsTimeout = 10;

    private class Worker implements Runnable {
        private final Engine engine;
        private final List<String> pdfPaths;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final List<Double> times = new ArrayList<Double>();
        private long numBytes = 0L;

        public long numProcessed = 0;
        public long totalMillis = 0;
        public long numErrors = 0L;

        public Worker(Engine engine, List<String> pdfPaths) {
            this.engine = engine;
            this.pdfPaths = pdfPaths;
        }

        public void run() {
            List<Future<String>> futures = new ArrayList<Future<String>>();
            try {
                engine.fullTextToTEI(pdfPaths.get(0), false, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Got " + pdfPaths.size() + " papers");
            for (String pdfPathLoop : pdfPaths) {
                final String pdfPath = pdfPathLoop;
                Future<String> future = executor.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        try {
                            //BiblioItem resHeader = new BiblioItem();
                            return engine.fullTextToTEI(pdfPath, false, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "";
                        }

                    }
                });
                futures.add(future);
            }
            long numBytes = 0;
            for (int idx=0; idx < futures.size(); ++idx) {
                Future<String> future = futures.get(idx);
                String pdfPath = pdfPaths.get(idx);
                long start = System.currentTimeMillis();
                try {
                    numBytes += future.get(numSecsTimeout, TimeUnit.SECONDS).length();
                    long stop = System.currentTimeMillis();
                    totalMillis += (stop - start);
                    times.add((double)stop-start);
                    numProcessed++;
                    if (numProcessed % 10 == 0) {
                        System.out.println("Thread " + Thread.currentThread().getName() + " finished " + numProcessed);
                    }
                } catch (Exception e) {
                    System.out.println("Timeout: " + pdfPath);
                    numErrors++;
                    e.printStackTrace();
                }
            }
            System.out.println("Num bytes: " + numBytes);
        }
    }


    public APIExample(List<String> pdfPaths, int numThreads) throws Exception {
        GrobidFactory grobidFactory = getGrobidFactory();
        List<Worker> workers = new ArrayList<Worker>(numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        int workPerThread = pdfPaths.size() / numThreads;
        for (int idx = 0; idx < numThreads; idx++) {
            int start = idx * workPerThread;
            int end = Math.min((idx+1) * workPerThread, pdfPaths.size());
            Worker w = new Worker(grobidFactory.createEngine(), pdfPaths.subList(start, end));
            executorService.submit(w);
            workers.add(w);
        }
        executorService.shutdown();
        executorService.awaitTermination(numSecsTimeout * pdfPaths.size(), TimeUnit.SECONDS);
        long numProcessed = 0L;
        long totalMillis = 0L;
        long numErrors = 0L;
        List<Double> times = new ArrayList<Double>();
        for (Worker worker : workers) {
            System.out.println("Worker " + worker);
            reportHistogram(worker.times);
            totalMillis += worker.totalMillis;
            numProcessed += worker.numProcessed;
            numErrors += worker.numErrors;
            times.addAll(worker.times);
        }
        double avgMillis = (double) totalMillis / (double) numProcessed;
        System.out.printf("Num processed: %d, num errors: %d, avg millis: %.3f\n", numProcessed, numErrors, avgMillis);
        Collections.sort(times);
        reportHistogram(times);
    }

    private static GrobidFactory getGrobidFactory() throws Exception {
        Properties prop = new Properties();
        prop.load(new FileInputStream("grobid-example.properties"));
        String pGrobidHome = prop.getProperty("grobid_example.pGrobidHome");
        String pGrobidProperties = prop.getProperty("grobid_example.pGrobidProperties");

        MockContext.setInitialContext(pGrobidHome, pGrobidProperties);
        GrobidProperties.getInstance();

        System.out.println(">>>>>>>> GROBID_HOME="+GrobidProperties.get_GROBID_HOME_PATH());

        return GrobidFactory.getInstance();
    }

    public static void main(String[] args) throws Exception {
        //GrobidFactory grobidFactory = getGrobidFactory();
        // args[0] should be ACL PDF directory
        File dir = new File(args[0]);
        List<String> pdfPaths = new ArrayList<String>();
        for (File f : dir.listFiles()) {
            if (f.getAbsolutePath().endsWith(".pdf")) {
                pdfPaths.add(f.getAbsolutePath());
            }
        }
        Collections.shuffle(pdfPaths);
        pdfPaths = pdfPaths.subList(0, 500);
        new APIExample(pdfPaths, 6);
        System.exit(0);
    }

    public static void reportHistogram(List<Double> xs) {
        double[] percentile = new double[]{0.25, 0.5, 0.75, 0.9, 0.95, 0.99, 0.999};
        System.out.println("Min: " + xs.get(0));
        System.out.println("Max: " + xs.get(xs.size()-1));
        for (double p : percentile) {
            int idx = (int) (xs.size() * p);
            System.out.printf("%.3f percentile: %.3f\n", p, xs.get(idx));
        }
    }
}
