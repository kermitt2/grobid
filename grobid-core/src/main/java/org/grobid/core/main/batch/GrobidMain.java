package org.grobid.core.main.batch;

import java.io.File;
import java.util.List;

import org.grobid.core.engines.ProcessEngine;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Utilities;

/**
 * The entrance point for starting grobid from command line and perform batch processing
 * 
 * @author Florian, Damien, Patrice
 */
public class GrobidMain {

	private static List<String> availableCommands;

	/**
	 * Arguments of the batch.
	 */
	private static GrobidMainArgs gbdArgs;

	/**
	 * Build the path to grobid.properties from the path to grobid-home.
	 * 
	 * @param pPath2GbdHome
	 *            The path to Grobid home.
	 * @return the path to grobid.properties.
	 */
	protected final static String getPath2GbdProperties(final String pPath2GbdHome) {
		return pPath2GbdHome + File.separator + "config" + File.separator + "grobid.properties";
	}

	/**
	 * Infer some parameters not given in arguments.
	 */
	protected static void inferParamsNotSet() {
		String tmpFilePath;
		if (gbdArgs.getPath2grobidHome() == null) {
			tmpFilePath = new File("grobid-home").getAbsolutePath();
			System.out.println("No path set for grobid-home. Using: " + tmpFilePath);
			gbdArgs.setPath2grobidHome(tmpFilePath);
			gbdArgs.setPath2grobidProperty(new File("grobid.properties").getAbsolutePath());
		}
	}

	/**
	 * Initialize the batch.
	 */
	protected static void initProcess() {
		try {
			MockContext.setInitialContext(gbdArgs.getPath2grobidHome(), gbdArgs.getPath2grobidProperty());
		} catch (final Exception exp) {
			System.err.println("Grobid initialisation failed: " + exp);
		}
		GrobidProperties.getInstance();
	}

	/**
	 * @return String to display for help.
	 */
	protected static String getHelp() {
		final StringBuffer help = new StringBuffer();
		help.append("HELP GROBID\n");
		help.append("-h: displays help\n");
		help.append("-gH: gives the path to grobid home directory.\n");
		help.append("-dIn: gives the path to the directory where the files to be processed are located, to be used only when the called method needs it.\n");
		help.append("-dOut: gives the path to the directory where the result files will be saved. The default output directory is the curent directory.\n");
		help.append("-s: is the parameter used for process using string as input and not file.\n");
		help.append("-r: recursive directory processing, default processing is not recursive.\n");
		help.append("-ignoreAssets: do not extract and save the PDF assets (bitmaps, vector graphics), by default the assets are extracted and saved.\n");
		help.append("-exe: gives the command to execute. The value should be one of these:\n");
		help.append("\t" + availableCommands + "\n");
		return help.toString();
	}

	/**
	 * Process batch given the args.
	 * 
	 * @param pArgs
	 *            The arguments given to the batch.
	 */
	protected static boolean processArgs(final String[] pArgs) {
		boolean result = true;
		if (pArgs.length == 0) {
			System.out.println(getHelp());
			result = false;
		} 
		else {
			String currArg;
			for (int i = 0; i < pArgs.length; i++) {
				currArg = pArgs[i];
				if (currArg.equals("-h")) {
					System.out.println(getHelp());
					result = false;
					break;
				}
				if (currArg.equals("-gH")) {
					gbdArgs.setPath2grobidHome(pArgs[i + 1]);
					if (pArgs[i + 1] != null) {
						gbdArgs.setPath2grobidProperty(getPath2GbdProperties(pArgs[i + 1]));
					}
					i++;
					continue;
				}
				if (currArg.equals("-dIn")) {
					if (pArgs[i + 1] != null) {
						gbdArgs.setPath2Input(pArgs[i + 1]);
						gbdArgs.setPdf(true);
					}
					i++;
					continue;
				}
				if (currArg.equals("-s")) {
					if (pArgs[i + 1] != null) {
						gbdArgs.setInput(pArgs[i + 1]);
						gbdArgs.setPdf(false);
					}
					i++;
					continue;
				}
				if (currArg.equals("-dOut")) {
					if (pArgs[i + 1] != null) {
						gbdArgs.setPath2Output(pArgs[i + 1]);
					}
					i++;
					continue;
				}
				if (currArg.equals("-exe")) {
					final String command = pArgs[i + 1];
					if (availableCommands.contains(command)) {
						gbdArgs.setProcessMethodName(command);
						i++;
						continue;
					} 
					else {
						System.err.println("-exe value should be one value from this list: " + availableCommands);
						result = false;
						break;
					}
				}
				if (currArg.equals("-ignoreAssets")) {
					gbdArgs.setSaveAssets(false);
					continue;
				}
				if (currArg.equals("-r")) {
					gbdArgs.setRecursive(true);
					continue;
				}
			}
		}
		return result;
	}

	/**
	 * Starts Grobid from command line using the following parameters:
	 * 
	 * @param args
	 *            The arguments
	 */
	public static void main(final String[] args) throws Exception {
		gbdArgs = new GrobidMainArgs();
		availableCommands = ProcessEngine.getUsableMethods();

		if (processArgs(args)) {
			inferParamsNotSet();
			initProcess();
			ProcessEngine processEngine = new ProcessEngine();
			Utilities.launchMethod(processEngine, new Object[] { gbdArgs }, gbdArgs.getProcessMethodName());
			processEngine.close();
		}

	}

}
