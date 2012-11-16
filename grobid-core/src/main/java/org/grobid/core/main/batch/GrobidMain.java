package org.grobid.core.main.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.ProcessEngine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Utilities;

/**
 * The entrance point, to start grobid from command line
 * 
 * @author Florian Zipser
 * @version 2.0
 */
public class GrobidMain {

	private static enum commandList {
		processHeader, fullTextToTEI, processDate, processAuthorsHeader, processAuthorsCitation, processAffiliation, processRawReference
	};

	/**
	 * The engine used for processing.
	 */
	private static Engine engine;

	/**
	 * Arguments of the batch.
	 */
	private static GrobidMainArgs gbdArgs = new GrobidMainArgs();

	protected static List<String> commandListToList() {
		List<String> list = new ArrayList<String>();
		for (commandList currValue : commandList.values()) {
			list.add(currValue.toString());
		}
		return list;
	}

	protected static void inferParamsNotSet() {
		if (gbdArgs.getPath2grobidHome() == null) {
			gbdArgs.setPath2grobidHome(new File("grobid-home").getAbsolutePath());
		}
		if (gbdArgs.getPath2grobidProperty() == null) {
			gbdArgs.setPath2grobidProperty(new File("grobid.properties").getAbsolutePath());
		}
		if (gbdArgs.getPath2pdfs() == null) {
			gbdArgs.setPath2pdfs(new File(".").getAbsolutePath());
		}
		if (gbdArgs.getPath2Output() == null) {
			gbdArgs.setPath2Output(new File(".").getAbsolutePath());
		}
	}

	protected static void initProcess() {
		try {
			inferParamsNotSet();
			MockContext.setInitialContext(gbdArgs.getPath2grobidHome(),
					gbdArgs.getPath2grobidProperty());
		} catch (Exception e) {
			System.err.println("Grobid initialisation failed");
		}
		GrobidProperties.getInstance();
		engine = GrobidFactory.getInstance().createEngine();
	}

	/**
	 * @return String to display for help.
	 */
	protected static String getHelp() {
		StringBuffer help = new StringBuffer();
		help.append("HELP GROBID\n");
		help.append("-h: display help\n");
		help.append("-gH: give the path to grobid home directory\n");
		help.append("-gP: give the path  to grobid.properties\n");
		help.append("-dPdf: give the path to the directory where input pdf are saved. Needed only for pdf conversion methods.\n");
		help.append("-dOut: give the path to the directory where results are saved. Output directory is the curent directory if not set.\n");
		help.append("-exe: give the command to execute on the pdf. The Value should be one of these:\n");
		help.append("\t" + commandListToList() + "\n");
		return help.toString();
	}

	/**
	 * Process batch given the args.
	 * 
	 * @param args
	 *            batch args
	 */
	protected static boolean processArgs(String[] args) {
		boolean result = true;
		if (args.length == 0) {
			System.out.println(getHelp());
			result = false;
		} else {
			String currArg;
			for (int i = 0; i < args.length; i++) {
				currArg = args[i];
				if (currArg.equals("-h")) {
					System.out.println(getHelp());
					break;
				}
				if (currArg.equals("-gH")) {
					gbdArgs.setPath2grobidHome(args[i + 1]);
					i++;
					continue;
				}
				if (currArg.equals("-gP")) {
					gbdArgs.setPath2grobidProperty(args[i + 1]);
					i++;
					continue;
				}
				if (currArg.equals("-dPdf")) {
					gbdArgs.setPath2pdfs(args[i + 1]);
					gbdArgs.setPdf(true);
					i++;
					continue;
				}
				if (currArg.equals("-s")) {
					gbdArgs.setInput(args[i + 1]);
					gbdArgs.setPdf(false);
					i++;
					continue;
				}
				if (currArg.equals("-dOut")) {
					gbdArgs.setPath2Output(args[i + 1]);
					i++;
					continue;
				}
				if (currArg.equals("-exe")) {
					final String command = args[i + 1];
					if (commandListToList().contains(command)) {
						commandList.valueOf(command);
						gbdArgs.setProcessMethodName(command);
						i++;
						continue;
					} else {
						System.err
								.println("-exe value should be one value from this list: "
										+ commandListToList());
						result = false;
						break;
					}

				}
			}
		}
		return result;
	}

	protected static void processPdf() throws Exception, NoSuchMethodException,
			FileNotFoundException, IOException {
		Object result = Utilities.launchMethod(engine,
				new Object[] { gbdArgs.getPath2pdfs(), false, null },
				new Class[] { String.class, boolean.class, BiblioItem.class },
				gbdArgs.getProcessMethodName());

		Utilities.writeInFile(
				gbdArgs.getPath2Output()
						+ File.separator
						+ new File(gbdArgs.getPath2pdfs()).getName().replace(
								"pdf", "tei.xml"), result.toString());
	}

	protected static void processInputString() throws Exception,
			NoSuchMethodException, FileNotFoundException, IOException {
		Object result = Utilities.launchMethod(engine,
				new Object[] { gbdArgs.getInput() },
				new Class[] { String.class }, gbdArgs.getProcessMethodName());

		Utilities.writeInFile(gbdArgs.getPath2Output() + File.separator
				+ "result", result.toString());
		System.out.println(result.toString());
	}

	protected static void processRawReference() throws Exception,
			NoSuchMethodException, FileNotFoundException, IOException {
		Object result = Utilities.launchMethod(engine,
				new Object[] { gbdArgs.getInput(), false }, new Class[] {
						String.class, boolean.class },
				gbdArgs.getProcessMethodName());

		Utilities.writeInFile(gbdArgs.getPath2Output() + File.separator
				+ "result", result.toString());
		System.out.println(result.toString());
	}

	/**
	 * Starts grobid from command line using the following parameters:
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) throws Exception {
		//System.out.println(ProcessEngine.getMethods());
		if (processArgs(args)) {
			initProcess();

			if (StringUtils.equals(commandList.processHeader.toString(),
					gbdArgs.getProcessMethodName())
					|| StringUtils.equals(commandList.fullTextToTEI.toString(),
							gbdArgs.getProcessMethodName())) {
				File pdfDirectory = new File(gbdArgs.getPath2pdfs());
				for (File currPdf : pdfDirectory.listFiles()) {
					if(currPdf.getName().contains(".pdf")){
						gbdArgs.setPath2pdfs(currPdf.getAbsolutePath());
						processPdf();
					}
				}
			} else if (StringUtils.equals(
					commandList.processRawReference.toString(),
					gbdArgs.getProcessMethodName())) {
				processRawReference();
			} else {
				processInputString();
			}
			engine.close();
		}

	}

}
