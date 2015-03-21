package org.grobid.trainer.evaluation;

import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluation against PubMedCentral native XML documents.
 *
 * @author Patrice Lopez
 */
public class PubMedCentralEvaluation {
    private static String pubMedCentralPath = null;
	private Engine engine = null;
	
	public PubMedCentralEvaluation(String path) {
		pubMedCentralPath = path;	
	
		File pubMedCentralFile = new File(path);
		if (!pubMedCentralFile.exists()) {
			System.out.println("Path to PubMedCentral is invalid");
		}
		
		String pGrobidHome = GrobidProperties.getGrobidHomePath().getAbsolutePath();
		String pGrobidProperties = GrobidProperties.getGrobidPropertiesPath().getAbsolutePath();

		try {
			MockContext.setInitialContext(pGrobidHome, pGrobidProperties);		
			GrobidProperties.getInstance();
			//System.out.println(">>>>>>>> GROBID_HOME="+GrobidProperties.get_GROBID_HOME_PATH());

			engine = GrobidFactory.getInstance().createEngine();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String evaluation(boolean forceRun) {
		StringBuilder report = new StringBuilder();
		
		if (forceRun) {
			// we run Grobid full text extraction on the PubMedCentral data
            File input = new File(pubMedCentralPath);
            // we process all tei files in the output directory
            File[] refFiles = input.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
					if (dir.isDirectory())
						return true;
					else
						return false;
                }
            });

            if (refFiles == null) {
				report.append("No file in dataset");
                return report.toString();
            }
			
            for (File dir : refFiles) {
				// select the PDF file in the directory
	            File[] refFiles2 = dir.listFiles(new FilenameFilter() {
	                public boolean accept(File dir, String name) {
	                    return name.endsWith(".pdf") || name.endsWith(".PDF");
	                }
	            });

	            if (refFiles2 == null) {
	            	System.out.println("warning: no PDF found under " + dir.getPath());
				    continue;
				}
				if (refFiles2.length != 1) {
	            	System.out.println("warning: more than one PDF found under " + dir.getPath());
				    System.out.println("processing only the first one");
				}

	            final File pdfFile = refFiles2[0];
				
				// run Grobid full text and write the TEI result in the directory
				try {
					String tei = engine.fullTextToTEI(pdfFile.getPath(), false, false);
				} 
				catch (Exception e) {
					e.printStackTrace();
				} 
				finally {
					try {
						MockContext.destroyInitialContext();
					} 
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		// evaluation of the run
		report.append(evaluationRun());
		
		return report.toString();
	}
	
	public String evaluationRun() {
		return null;
	}
	
	public void close() {
		try {
			MockContext.destroyInitialContext();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("usage: command [path to the PubMedCentral dataset]");
		}
		String pubMedCentralPath = args[0];
        try {
            PubMedCentralEvaluation eval = new PubMedCentralEvaluation(pubMedCentralPath);
			String report = eval.evaluation(true);
			System.out.println(report);
			eval.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}