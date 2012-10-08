package org.grobid.core.main;

import java.io.File;
import java.io.FileFilter;

import javax.naming.InitialContext;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.GrobidPropertyKeys;
import org.grobid.core.utilities.Utilities;
import org.grobid.mock.MockContext;
import org.slf4j.LoggerFactory;

/**
 * User: zholudev Date: 11/17/11 Time: 1:49 PM
 */
public class LibraryLoader {

	private static org.slf4j.Logger logger = LoggerFactory
			.getLogger(LibraryLoader.class);

	private static boolean loaded = false;

	private static boolean isContextMocked = false;

	public static void load() {
		if (!loaded) {
			logger.info("Loading external library crfpp");
			mockContextIfNotSet();
			logger.debug(getLibraryFolder());
			File libraryFolder = new File(getLibraryFolder());
			if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
				logger.error("Unable to find a native CRF++ library: Folder "
						+ libraryFolder + " does not exist");
				throw new RuntimeException(
						"Unable to find a native CRF++ library: Folder "
								+ libraryFolder + " does not exist");
			}

			File[] files = libraryFolder.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.getName().toLowerCase()
							.startsWith(GrobidConstants.CRFPP_NATIVE_LIB_NAME);
				}
			});

			if (files.length == 0) {
				logger.error("Unable to find a native CRF++ library: No files starting with "
						+ GrobidConstants.CRFPP_NATIVE_LIB_NAME
						+ " are in folder " + libraryFolder);
				throw new RuntimeException(
						"Unable to find a native CRF++ library: No files starting with "
								+ GrobidConstants.CRFPP_NATIVE_LIB_NAME
								+ " are in folder " + libraryFolder);
			}

			if (files.length > 1) {
				logger.error("Unable to load a native CRF++ library: More than 1 library exists in "
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
				logger.error("Unable to load a native CRF++ library, although it was found under path "
						+ libPath);
				throw new RuntimeException(
						"Unable to load a native CRF++ library, although it was found under path "
								+ libPath, e);
			}

			loaded = true;

			if (isContextMocked) {
				try {
					MockContext.destroyInitialContext();
				} catch (Exception exp) {
					logger.error("Could not unmock the context." + exp);
					new GrobidException("Could not unmock the context." + exp);
				}
				isContextMocked = false;
			}
			logger.info("Library crfpp loaded");
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
			logger.debug("The property " + GrobidPropertyKeys.PROP_GROBID_HOME
					+ " already exists. No mocking of context made.");
		} catch (Exception exp) {
			logger.debug("The property " + GrobidPropertyKeys.PROP_GROBID_HOME
					+ " does not exist. Mocking the context.");
			try {
				MockContext.setInitialContext();
				isContextMocked = true;
			} catch (Exception mexp) {
				logger.error("Could not mock the context." + mexp);
				new GrobidException("Could not mock the context." + mexp);
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
