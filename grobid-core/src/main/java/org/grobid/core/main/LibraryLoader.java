package org.grobid.core.main;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.jni.PythonEnvironmentConfig;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;

//import org.grobid.core.mock.MockContext;

/**
 * @author Slava, Patrice
 */
public class LibraryLoader {

    private static Logger LOGGER = LoggerFactory.getLogger(LibraryLoader.class);

    //a name of a native CRF++ library without an extension
    public static final String CRFPP_NATIVE_LIB_NAME = "libcrfpp";
    public static final String WAPITI_NATIVE_LIB_NAME = "libwapiti";
    public static final String DELFT_NATIVE_LIB_NAME_LINUX = "libjep";
    public static final String DELFT_NATIVE_LIB_NAME = "jep";

    private static boolean loaded = false;

//    private static boolean isContextMocked = false;

    public static void load() {
        if (!loaded) {
            LOGGER.info("Loading external native sequence labelling library");
            LOGGER.debug(getLibraryFolder());

            if (GrobidProperties.getGrobidCRFEngine() != GrobidCRFEngine.CRFPP &&
                GrobidProperties.getGrobidCRFEngine() != GrobidCRFEngine.WAPITI &&
                GrobidProperties.getGrobidCRFEngine() != GrobidCRFEngine.DELFT) {
                throw new IllegalStateException("Unsupported sequence labelling engine: " + GrobidProperties.getGrobidCRFEngine());
            }

            File libraryFolder = new File(getLibraryFolder());
            if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
                LOGGER.error("Unable to find a native sequence labelling library: Folder "
                    + libraryFolder + " does not exist");
                throw new RuntimeException(
                    "Unable to find a native sequence labelling library: Folder "
                        + libraryFolder + " does not exist");
            }

            if (GrobidProperties.getGrobidCRFEngine() == GrobidCRFEngine.CRFPP) {
                File[] files = libraryFolder.listFiles(file -> file.getName().toLowerCase().startsWith(CRFPP_NATIVE_LIB_NAME));

                if (ArrayUtils.isEmpty(files)) {
                    LOGGER.error("Unable to find a native CRF++ library: No files starting with "
                        + CRFPP_NATIVE_LIB_NAME
                        + " are in folder " + libraryFolder);
                    throw new RuntimeException(
                        "Unable to find a native CRF++ library: No files starting with "
                            + CRFPP_NATIVE_LIB_NAME
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

            }

            if (GrobidProperties.getGrobidCRFEngine() == GrobidCRFEngine.WAPITI ||
                GrobidProperties.getGrobidCRFEngine() == GrobidCRFEngine.DELFT) {
                // note: if DeLFT is used, we still make Wapiti available for models not existing in DeLFT (currently segmentation and 
                // fulltext)
                File[] wapitiLibFiles = libraryFolder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.startsWith(WAPITI_NATIVE_LIB_NAME);
                    }
                });

                if (isEmpty(wapitiLibFiles)) {
                    LOGGER.info("No wapiti library in the Grobid home folder");
                } else {
                    LOGGER.info("Loading Wapiti native library...");
                    if (GrobidProperties.getGrobidCRFEngine() == GrobidCRFEngine.DELFT) {
                        // if DeLFT will be used, we must not load libstdc++, it would create a conflict with tensorflow libstdc++ version
                        // so we temporary rename the lib so that it is not loaded in this case
                        // note that we know that, in this case, the local lib can be ignored because as DeFLT and tensorflow are installed
                        // we are sure that a compatible libstdc++ lib is installed on the system and can be dynamically loaded

                        String libstdcppPath = libraryFolder.getAbsolutePath() + File.separator + "libstdc++.so.6";
                        File libstdcppFile = new File(libstdcppPath);
                        if (libstdcppFile.exists()) {
                            File libstdcppFileNew = new File(libstdcppPath + ".new");
                            libstdcppFile.renameTo(libstdcppFileNew);
                        }
                    }
                    try {
                        System.load(wapitiLibFiles[0].getAbsolutePath());
                    } finally {
                        if (GrobidProperties.getGrobidCRFEngine() == GrobidCRFEngine.DELFT) {
                            // restore libstdc++
                            String libstdcppPathNew = libraryFolder.getAbsolutePath() + File.separator + "libstdc++.so.6.new";
                            File libstdcppFileNew = new File(libstdcppPathNew);
                            if (libstdcppFileNew.exists()) {
                                File libstdcppFile = new File(libraryFolder.getAbsolutePath() + File.separator + "libstdc++.so.6");
                                libstdcppFileNew.renameTo(libstdcppFile);
                            }
                        }
                    }
                }
            }


            if (GrobidProperties.getGrobidCRFEngine() == GrobidCRFEngine.DELFT) {
                LOGGER.info("Loading JEP native library for DeLFT... " + libraryFolder.getAbsolutePath());
                // actual loading will be made at JEP initialization, so we just need to add the path in the 
                // java.library.path (JEP will anyway try to load from java.library.path, so explicit file 
                // loading here will not help)
                try {
                    addLibraryPath(libraryFolder.getAbsolutePath());

                    PythonEnvironmentConfig pythonEnvironmentConfig = PythonEnvironmentConfig.getInstance();
                    if (pythonEnvironmentConfig.isEmpty()) {
                        LOGGER.info("No python environment configured");
                    } else {
                        LOGGER.info("Configuring python environment: " + pythonEnvironmentConfig.getVirtualEnv());
                        LOGGER.info("Adding library paths " + Arrays.toString(pythonEnvironmentConfig.getNativeLibPaths()));
                        for (Path path : pythonEnvironmentConfig.getNativeLibPaths()) {
                            if (Files.exists(path)) {
                                addLibraryPath(path.toString());
                            } else {
                                LOGGER.warn(path.toString() + " does not exists. Skipping it. ");
                            }
                        }

                        if (SystemUtils.IS_OS_MAC) {
//                            System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + libraryFolder.getAbsolutePath());
                            System.loadLibrary("python" + pythonEnvironmentConfig.getPythonVersion() + "m");
                            System.loadLibrary(DELFT_NATIVE_LIB_NAME);
                        } else if (SystemUtils.IS_OS_LINUX) {
                            System.loadLibrary(DELFT_NATIVE_LIB_NAME);
                        } else if (SystemUtils.IS_OS_WINDOWS) {
                            throw new UnsupportedOperationException("Delft on Windows is not supported.");
                        }
                    }

                } catch (Exception e) {
                    throw new GrobidException("Loading JEP native library for DeLFT failed", e);
                }
            }


            loaded = true;
            LOGGER.info("Native library for sequence labelling loaded");
        }
    }

    public static void addLibraryPath(String pathToAdd) throws Exception {
        Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        String[] paths = (String[]) usrPathsField.get(null);

        for (String path : paths)
            if (path.equals(pathToAdd))
                return;

        String[] newPaths = new String[paths.length + 1];
        System.arraycopy(paths, 0, newPaths, 1, paths.length);
        newPaths[0] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }

    //    /**
//     * Initialize the context with mock parameters if they doesn't already
//     * exist.
//     */
//    protected static void mockContextIfNotSet() {
//        try {
//            new InitialContext().lookup("java:comp/env/"
//                    + GrobidPropertyKeys.PROP_GROBID_HOME);
//            LOGGER.debug("The property " + GrobidPropertyKeys.PROP_GROBID_HOME
//                    + " already exists. No mocking of context made.");
//        } catch (Exception exp) {
//            LOGGER.debug("The property " + GrobidPropertyKeys.PROP_GROBID_HOME
//                    + " does not exist. Mocking the context.");
////            try {
////                MockContext.setInitialContext();
////                isContextMocked = true;
////            } catch (Exception mexp) {
////                LOGGER.error("Could not mock the context." + mexp);
////                throw new GrobidException("Could not mock the context.",  mexp);
////            }
//        }
//    }
//
    public static String getLibraryFolder() {
        GrobidProperties.getInstance();
        // TODO: change to fetching the basic dir from GrobidProperties object
        return String.format("%s" + File.separator + "%s", GrobidProperties
            .getNativeLibraryPath().getAbsolutePath(), Utilities
            .getOsNameAndArch());
    }
}
