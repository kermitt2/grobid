package org.grobid.core.engines;

import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.junit.Assert.*;

public class MonographParserTest {
    private MonographParser target;

    @Before
    public void setUp() throws Exception {
        target = new MonographParser();
    }

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
        GrobidProperties.getInstance();
    }

    @AfterClass
    public static void tearDown() {
        GrobidFactory.reset();
    }


}