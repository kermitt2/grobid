package org.grobid.core.engines;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.Date;
import org.grobid.core.main.batch.GrobidMainArgs;
import org.grobid.core.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessEngine {
	
	/**
	 * The logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessEngine.class);

	private static Engine engine;

	public ProcessEngine() {
		engine = new Engine();
	}

	/**
	 * Process the headers using gbdArgs parameters.
	 * 
	 * @param gbdArgs
	 *            the parameters.
	 * @throws Exception
	 */
	public void processHeader(GrobidMainArgs gbdArgs) throws Exception {
		File pdfDirectory = new File(gbdArgs.getPath2pdfs());
		String result = StringUtils.EMPTY;
		for (File currPdf : pdfDirectory.listFiles()) {
			gbdArgs.setPath2pdfs(currPdf.getAbsolutePath());
			result = engine.processHeader(gbdArgs.getPath2pdfs(), false, null);
			Utilities.writeInFile(
					gbdArgs.getPath2Output()
							+ File.separator
							+ new File(gbdArgs.getPath2pdfs()).getName()
									.replace("pdf", "tei.xml"),
					result.toString());
		}
	}

	/**
	 * Process the fulltext using gbdArgs parameters.
	 * 
	 * @param gbdArgs
	 *            the parameters.
	 * @throws Exception
	 */
	public void processFullText(GrobidMainArgs gbdArgs) throws Exception {
		File pdfDirectory = new File(gbdArgs.getPath2pdfs());
		String result = StringUtils.EMPTY;
		for (File currPdf : pdfDirectory.listFiles()) {
			gbdArgs.setPath2pdfs(currPdf.getAbsolutePath());
			result = engine.fullTextToTEI(gbdArgs.getPath2pdfs(), false, false);
			Utilities.writeInFile(
					gbdArgs.getPath2Output()
							+ File.separator
							+ new File(gbdArgs.getPath2pdfs()).getName()
									.replace("pdf", "tei.xml"),
					result.toString());
		}
	}

	/**
	 * Process the date using gbdArgs parameters.
	 * 
	 * @param gbdArgs
	 *            the parameters.
	 * @throws Exception
	 */
	public void processDate(GrobidMainArgs gbdArgs) throws Exception {
		List<Date> result = engine.processDate(gbdArgs.getInput());
		Utilities.writeInFile(gbdArgs.getPath2Output() + File.separator
				+ "result", result.toString());
		LOGGER.info(result.toString());
	}

	/**
	 * List the engine methods that can be called.
	 * 
	 * @return List<String> containing the list of the methods.
	 */
	public static List<String> getMethods() {
		Class<?> pClass = new ProcessEngine().getClass();
		List<String> availableMethods = new ArrayList<String>();
		for (Method method : pClass.getMethods()) {
			if (isUsableMethod(method.getName())) {
				availableMethods.add(method.getName());
			}
		}
		return availableMethods;
	}

	/**
	 * Check if the method is usable.
	 * 
	 * @param pMethod
	 *            method name.
	 * @return if it is usable
	 */
	protected static boolean isUsableMethod(String pMethod) {
		boolean isUsable = StringUtils.equals("wait", pMethod);
		isUsable |= StringUtils.equals("equals", pMethod);
		isUsable |= StringUtils.equals("toString", pMethod);
		isUsable |= StringUtils.equals("hashCode", pMethod);
		isUsable |= StringUtils.equals("getClass", pMethod);
		isUsable |= StringUtils.equals("notify", pMethod);
		isUsable |= StringUtils.equals("notifyAll", pMethod);
		isUsable |= StringUtils.equals("isUsableMethod", pMethod);
		isUsable |= StringUtils.equals("getMethods", pMethod);
		return !isUsable;
	}

}
