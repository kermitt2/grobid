package org.grobid.trainer;

import java.io.File;

import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;

/**
 * Training application for training a target model.
 * 
 * @author Patrice Lopez
 */
public class TrainerRunner {

	enum RunType {
		TRAIN, EVAL;

		public static RunType getRunType(int i) {
			for (RunType t : values()) {
				if (t.ordinal() == i) {
					return t;
				}
			}

			throw new IllegalStateException("Unsupported RunType with ordinal " + i);
		}
	}

	/**
	 * Initialize the batch.
	 */
	protected static void initProcess(final String path2GbdHome, final String path2GbdProperties) {
		try {
			MockContext.setInitialContext(path2GbdHome, path2GbdProperties);
		} catch (final Exception exp) {
			System.err.println("Grobid initialisation failed: " + exp);
		}
		GrobidProperties.getInstance();
	}

	/**
	 * Command line execution.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		if (args.length < 4) {
			throw new IllegalStateException(
					"Usage: {0 - train, 1 - evaluate} {affiliation,chemical,date,citation,ebook,fulltext,header,name-citation,name-header,patent} -pH /path/to/Grobid/home");
		}

		String path2GbdHome = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-gH")) {
				path2GbdHome = args[i + 1];
				i++;
				continue;
			}
		}

		if (path2GbdHome == null) {
			throw new IllegalStateException(
					"Usage: {0 - train, 1 - evaluate} {affiliation,chemical,date,citation,ebook,fulltext,header,name-citation,name-header,patent} -pH /path/to/Grobid/home");
		}

		final String path2GbdProperties = path2GbdHome + File.separator + "config" + File.separator + "grobid.properties";

		System.out.println("path2GbdHome=" + path2GbdHome + "   path2GbdProperties=" + path2GbdProperties);
		initProcess(path2GbdHome, path2GbdProperties);

		RunType mode = RunType.getRunType(Integer.parseInt(args[0]));
		String model = args[1];

		AbstractTrainer trainer;

		if (model.equals("affiliation") || model.equals("affiliation-address")) {
			trainer = new AffiliationAddressTrainer();
		} else if (model.equals("chemical")) {
			trainer = new ChemicalEntityTrainer();
		} else if (model.equals("date")) {
			trainer = new DateTrainer();
		} else if (model.equals("citation")) {
			trainer = new CitationTrainer();
		} else if (model.equals("ebook")) {
			trainer = new EbookTrainer();
		} else if (model.equals("fulltext")) {
			trainer = new FulltextTrainer();
		} else if (model.equals("header")) {
			trainer = new HeaderTrainer();
		} else if (model.equals("name-citation")) {
			trainer = new NameCitationTrainer();
		} else if (model.equals("name-header")) {
			trainer = new NameHeaderTrainer();
		} else if (model.equals("patent")) {
			trainer = new PatentParserTrainer();
		} else {
			throw new IllegalStateException("The model " + model + " is unknown.");
		}

		switch (mode) {
		case TRAIN:
			AbstractTrainer.runTraining(trainer);
			break;
		case EVAL:
			AbstractTrainer.runEvaluation(trainer);
			break;
		default:
			throw new IllegalStateException("Invalid RunType: " + mode.name());
		}

	}

}