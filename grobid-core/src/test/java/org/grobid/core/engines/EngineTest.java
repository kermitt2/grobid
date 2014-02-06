package org.grobid.core.engines;


import org.grobid.core.GrobidModels;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.LibraryLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class EngineTest {

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }

    @Test
    public void testGetNewModel() {
        // assertEquals("Wrong value of getModel", "-m "+GrobidModels.CITATION.getModelPath()+" ", GrobidModels.CITATION.getModelPath());
    }

    @Test
    public void testMultiThreading() throws InterruptedException {
        final Engine engine = GrobidFactory.getInstance().getEngine();
        final String cit = " M. Kitsuregawa, H. Tanaka, and T. Moto-oka. Application of hash to data base machine and its architecture. New Generation Computing,  1 (1),   1983.";

        long t = System.currentTimeMillis();
        int n = 3;
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            threads[i] = new  Thread() {
                @Override
                public void run() {
                    int cnt = 0;
                    for (int i = 0; i < 100; i++) {
                        try {
                            engine.processRawReference(cit, false);
                        } catch (Exception e) {
                            //no op
                        }
                        if (++cnt % 10 == 0) {
                            System.out.println(cnt);
                        }
                    }

                }
            };
        }
        for (int i =0; i < n; i++) {
            threads[i].start();
//            threads[i].join();
        }

        for (int i =0; i < n;i++) {
            threads[i].join();
        }



        System.out.println(System.currentTimeMillis() - t);
    }
}
