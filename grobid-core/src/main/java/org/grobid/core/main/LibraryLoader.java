package org.grobid.core.main;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import javax.naming.InitialContext;

import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.core.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Slava, Patrice
 */
public class LibraryLoader {

    private static Logger LOGGER = LoggerFactory.getLogger(LibraryLoader.class);

    private static boolean loaded = false;

    private static boolean isContextMocked = false;

    public static void load() {
        if (!loaded) {
            LOGGER.info("Loading external native CRF library");
            mockContextIfNotSet();
            LOGGER.debug(getLibraryFolder());
            File libraryFolder = new File(getLibraryFolder());
            if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
                LOGGER.error("Unable to find a native CRF library: Folder "
                        + libraryFolder + " does not exist");
                throw new RuntimeException(
                        "Unable to find a native CRF library: Folder "
                                + libraryFolder + " does not exist");
            }

            if (GrobidProperties.getGrobidCRFEngine() == GrobidCRFEngine.CRFPP) {
                File[] files = libraryFolder.listFiles(new FileFilter() {
                    public boolean accept(File file) {
                        return file.getName().toLowerCase()
                                .startsWith(GrobidConstants.CRFPP_NATIVE_LIB_NAME);
                    }
                });

                if (files.length == 0) {
                    LOGGER.error("Unable to find a native CRF++ library: No files starting with "
                            + GrobidConstants.CRFPP_NATIVE_LIB_NAME
                            + " are in folder " + libraryFolder);
                    throw new RuntimeException(
                            "Unable to find a native CRF++ library: No files starting with "
                                    + GrobidConstants.CRFPP_NATIVE_LIB_NAME
                                    + " are in folder " + libraryFolder);
                }

                if (files.length > 1) {
                    LOGGER.error("Unable to load a native CRF++ library: More than 1 library exists in "
                            + libraryFolder);
                    throw new RuntimeException(
                            "Unable to load a native CRF++ library: More than 1 library exists in "
                                    + libraryFolder);
                }

                String libPath = files[0].getAbsolutePath();
                // finally loading a library

                try {
                    System.load(libPath);
                } catch (Exception e) {
                    LOGGER.error("Unable to load a native CRF++ library, although it was found under path "
                            + libPath);
                    throw new RuntimeException(
                            "Unable to load a native CRF++ library, although it was found under path "
                                    + libPath, e);
                }

            } else if (GrobidProperties.getGrobidCRFEngine() == GrobidCRFEngine.WAPITI) {
                File[] wapitiLibFiles = libraryFolder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.startsWith(GrobidConstants.WAPITI_NATIVE_LIB_NAME);
                    }
                });

                if (wapitiLibFiles.length == 0) {
                    LOGGER.info("No wapiti library in the grobid home folder");
                } else {
                    LOGGER.info("Loading Wapiti native library...");
                    System.load(wapitiLibFiles[0].getAbsolutePath());
                }

            } else {
                throw new IllegalStateException("Unsupported CRF engine: " + GrobidProperties.getGrobidCRFEngine());
            }
            loaded = true;

            if (isContextMocked) {
                try {
                    MockContext.destroyInitialContext();
                } catch (Exception exp) {
                    LOGGER.error("Could not unmock the context." + exp);
                    throw new GrobidException("Could not unmock the context.", exp);
                }
                isContextMocked = false;
            }
            LOGGER.info("Library crfpp loaded");
        }
    }

    /**
     * Initialize the context with mock parameters if they doesn't already
     * exist.
     */
    protected static void mockContextIfNotSet() {
        try {
            new InitialContext().lookup("java:comp/env/"
                    + GrobidPropertyKeys.PROP_GROBID_HOME);
            LOGGER.debug("The property " + GrobidPropertyKeys.PROP_GROBID_HOME
                    + " already exists. No mocking of context made.");
        } catch (Exception exp) {
            LOGGER.debug("The property " + GrobidPropertyKeys.PROP_GROBID_HOME
                    + " does not exist. Mocking the context.");
            try {
                MockContext.setInitialContext();
                isContextMocked = true;
            } catch (Exception mexp) {
                LOGGER.error("Could not mock the context." + mexp);
                throw new GrobidException("Could not mock the context.",  mexp);
            }
        }
    }

    private static String getLibraryFolder() {
        GrobidProperties.getInstance();
        // TODO: change to fetching the basic dir from GrobidProperties object
        return String.format("%s" + File.separator + "%s", GrobidProperties
                .getNativeLibraryPath().getAbsolutePath(), Utilities
                .getOsNameAndArch());
    }
}
