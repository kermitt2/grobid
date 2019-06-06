package org.grobid.core.engines.tagging;

import jep.Jep;
import jep.JepConfig;
import org.grobid.core.main.LibraryLoader;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeLFTTaggerIntegrationTest {

    DeLFTTagger target;

    @Test
    public void setUp() throws Exception {
        LibraryLoader.load();
        System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + LibraryLoader.getLibraryFolder());
        System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + "/anaconda3/envs/tensorflow/lib");
        System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + "/anaconda3/envs/tensorflow/lib/python3.6/site-packages/");

        System.out.println(System.getProperty("java.library.path"));

        System.loadLibrary("python3.6m");

        JepConfig config = new JepConfig();
        config.setInteractive(false);
        config.setClassLoader(this.getClass().getClassLoader());

//        Jep jep = new Jep(config);

        System.out.println(LibraryLoader.getLibraryFolder());

        System.loadLibrary("jep");
    }

}