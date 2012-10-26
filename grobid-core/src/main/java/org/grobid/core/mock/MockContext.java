package org.grobid.core.mock;

import java.io.File;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.grobid.core.utilities.GrobidPropertyKeys;

/**
 * @author Damien
 * 
 */
public class MockContext {

	public static String GROBID_HOME_PATH = System.getProperty("user.dir")
			+ File.separator + ".." + File.separator + "grobid-home";

	public static String GROBID_PROPERTY_PATH = GROBID_HOME_PATH
			+ File.separator + "config" + File.separator + "grobid.properties";

	public static String GROBID_PROPERTY_SERVICE_PATH = GROBID_HOME_PATH
			+ File.separator + "config" + File.separator
			+ "grobid_service.properties";

	/**
	 * Set initial context with the given arguments.
	 * 
	 * @throws Exception
	 */
	public static void setInitialContext() throws Exception {
		// Create initial context
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"org.apache.naming.java.javaURLContextFactory");
		System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
		InitialContext ic = new InitialContext();
		ic.createSubcontext("java:");
		ic.createSubcontext("java:comp");
		ic.createSubcontext("java:comp/env");

		// Binds the values
		ic.bind("java:comp/env/" + GrobidPropertyKeys.PROP_GROBID_HOME,
				GROBID_HOME_PATH);
		ic.bind("java:comp/env/" + GrobidPropertyKeys.PROP_GROBID_PROPERTY,
				GROBID_PROPERTY_PATH);
		ic.bind("java:comp/env/"
				+ GrobidPropertyKeys.PROP_GROBID_SERVICE_PROPERTY,
				GROBID_PROPERTY_SERVICE_PATH);
	}

	/**
	 * Set initial context with the given arguments.
	 * 
	 * @param pGrobidHome
	 *            path to grobid_home
	 * @throws Exception
	 */
	public static void setInitialContext(String pGrobidHome) throws Exception {
		GROBID_HOME_PATH = pGrobidHome;
		setInitialContext();
	}

	/**
	 * Set initial context with the given arguments.
	 * 
	 * @param pGrobidHome
	 *            path to grobid_home
	 * @param pGrobidProperties
	 *            path to grobid.properties
	 * @throws Exception
	 */
	public static void setInitialContext(String pGrobidHome,
			String pGrobidProperties) throws Exception {
		GROBID_HOME_PATH = pGrobidHome;
		GROBID_PROPERTY_PATH = pGrobidProperties;
		setInitialContext();
	}

	/**
	 * Set initial context with the given arguments.
	 * 
	 * @param pGrobidHome
	 *            path to grobid_home
	 * @param pGrobidProperties
	 *            path to grobid.properties
	 * @param pGrobidServiceProperties
	 *            path to grobid_service.properties
	 * @throws Exception
	 */
	public static void setInitialContext(String pGrobidHome,
			String pGrobidProperties, String pGrobidServiceProperties)
			throws Exception {
		GROBID_HOME_PATH = pGrobidHome;
		GROBID_PROPERTY_PATH = pGrobidProperties;
		GROBID_PROPERTY_SERVICE_PATH = pGrobidServiceProperties;
		setInitialContext();
	}

	/**
	 * Remove the initial context.
	 * 
	 * @throws Exception
	 */
	public static void destroyInitialContext() throws Exception {
		InitialContext ic = new InitialContext();
		ic.unbind("java:comp/env/" + GrobidPropertyKeys.PROP_GROBID_HOME);
		ic.unbind("java:comp/env/" + GrobidPropertyKeys.PROP_GROBID_PROPERTY);
		ic.unbind("java:comp/env/"
				+ GrobidPropertyKeys.PROP_GROBID_SERVICE_PROPERTY);
		ic.destroySubcontext("java:comp/env");
		ic.destroySubcontext("java:comp");
		ic.destroySubcontext("java:");
		ic.close();
		System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
		System.clearProperty(Context.URL_PKG_PREFIXES);
	}

}
