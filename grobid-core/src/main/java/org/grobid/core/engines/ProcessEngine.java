package org.grobid.core.engines;

import java.io.File;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.PatentItem;
import org.apache.commons.io.FileUtils;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.Affiliation;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.Date;
import org.grobid.core.data.Person;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.batch.GrobidMainArgs;
import org.grobid.core.utilities.Utilities;
import org.grobid.core.utilities.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Perform the batch processing for the different engine methods.
 *
 * @author Damien, Patrice
 */

public class ProcessEngine implements Closeable {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessEngine.class);

    /**
     * The engine.
     */
    private static Engine engine;

    /**
     * @return the engine instance.
     */
    protected Engine getEngine() {
        if (engine == null) {
            engine = GrobidFactory.getInstance().createEngine();
        }
        return engine;
    }

	/**
	 *  Close engine resources. 
	 */
	@Override
	public void close() throws IOException {
		if (engine != null) {
			engine.close();
		}
	} 

    /**
     * Process the headers using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processHeader(final GrobidMainArgs pGbdArgs) throws Exception {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);
        final File pdfDirectory = new File(pGbdArgs.getPath2Input());
		File[] files = pdfDirectory.listFiles();
		if (files == null) {
			LOGGER.warn("No files in directory: " + pdfDirectory);
		}
		else {
			processHeaderDirectory(files, pGbdArgs, pGbdArgs.getPath2Output());
		}
    }
		
    /**
     * Process the header recursively or not using pGbdArgs parameters.
     *
	 * @param files list of files to be processed	
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
	private void processHeaderDirectory(File[] files, final GrobidMainArgs pGbdArgs, String outputPath) {
        if (files != null) {
			boolean recurse = pGbdArgs.isRecursive();
			String result;
            for (final File currPdf : files) {
                try {
                    if (currPdf.getName().toLowerCase().endsWith(".pdf")) {
                        result = getEngine().processHeader(currPdf.getAbsolutePath(), false, null);
						File outputPathFile = new File(outputPath);
						if (!outputPathFile.exists()) {
							outputPathFile.mkdir();
						}
						if (currPdf.getName().endsWith(".pdf")) {
                        	Utilities.writeInFile(outputPath + File.separator
                                + new File(currPdf.getAbsolutePath())
									.getName().replace(".pdf", ".tei.xml"), result.toString());
						}
						else if (currPdf.getName().endsWith(".PDF")) {
                        	Utilities.writeInFile(outputPath + File.separator
                                + new File(currPdf.getAbsolutePath())
									.getName().replace(".PDF", ".tei.xml"), result.toString());
						}
					}
					else if (recurse && currPdf.isDirectory()) {
						File[] newFiles = currPdf.listFiles();
						if (newFiles != null) {
							String newLevel = currPdf.getName();
							processHeaderDirectory(newFiles, pGbdArgs, outputPath + 
								File.separator + newLevel);
						}
                    }
                } 
				catch (final Exception exp) {
                    LOGGER.error("An error occured while processing the file " + currPdf.getAbsolutePath()
                            + ". Continuing the process for the other files");
                }
            }
        } 
	}

    /**
     * Process the full text using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processFullText(final GrobidMainArgs pGbdArgs) throws Exception {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);
        final File pdfDirectory = new File(pGbdArgs.getPath2Input());
		File[] files = pdfDirectory.listFiles();
		if (files == null) {
			LOGGER.warn("No files in directory: " + pdfDirectory);
		}
		else {
			processFullTextDirectory(files, pGbdArgs, pGbdArgs.getPath2Output(), pGbdArgs.getSaveAssets());
            System.out.println(Engine.getCntManager());
		}
	}
		
    /**
     * Process the full text recursively or not using pGbdArgs parameters.
     *
	 * @param files list of files to be processed	
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
	private void processFullTextDirectory(File[] files, final GrobidMainArgs pGbdArgs, String outputPath, boolean saveAssets) {	
        if (files != null) {
			boolean recurse = pGbdArgs.isRecursive();
			String result;
            for (final File currPdf : files) {
                try {
                    if (currPdf.getName().toLowerCase().endsWith(".pdf")) {
                        GrobidAnalysisConfig config = null;
			            // path for saving assets
                        if (saveAssets) {
					        String assetPath = outputPath + File.separator + KeyGen.getKey();
				            config = GrobidAnalysisConfig.builder()
														.pdfAssetPath(new File(assetPath))
														.build();
                        }
                        else
                            config = GrobidAnalysisConfig.builder().build();;
                        result = getEngine().fullTextToTEI(currPdf, config);
						File outputPathFile = new File(outputPath);
						if (!outputPathFile.exists()) {
							outputPathFile.mkdir();
						}
						if (currPdf.getName().endsWith(".pdf")) {
                        	Utilities.writeInFile(outputPath + File.separator
                                + new File(currPdf.getAbsolutePath())
									.getName().replace(".pdf", ".tei.xml"), result.toString());
						}
						else if (currPdf.getName().endsWith(".PDF")) {
                        	Utilities.writeInFile(outputPath + File.separator
                                + new File(currPdf.getAbsolutePath())
									.getName().replace(".PDF", ".tei.xml"), result.toString());
						}
					}
					else if (recurse && currPdf.isDirectory()) {
						File[] newFiles = currPdf.listFiles();
						if (newFiles != null) {
							String newLevel = currPdf.getName();
							processFullTextDirectory(newFiles, pGbdArgs, outputPath + 
								File.separator + newLevel, saveAssets);
						}
                    }
                } 
				catch (final Exception exp) {
                    LOGGER.error("An error occured while processing the file " + currPdf.getAbsolutePath()
                            + ". Continuing the process for the other files");
                }
            }
        } 
    }

    /**
     * Process the date using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processDate(final GrobidMainArgs pGbdArgs) throws Exception {
        inferOutputPath(pGbdArgs);
        final List<Date> result = getEngine().processDate(pGbdArgs.getInput());
        Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.get(0).toTEI());
        LOGGER.info(result.get(0).toTEI());
    }

    /**
     * Process the author header using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processAuthorsHeader(final GrobidMainArgs pGbdArgs) throws Exception {
        inferOutputPath(pGbdArgs);
        final List<Person> result = getEngine().processAuthorsHeader(pGbdArgs.getInput());
        Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.get(0).toTEI());
        LOGGER.info(result.get(0).toTEI());
    }

    /**
     * Process the author citation using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processAuthorsCitation(final GrobidMainArgs pGbdArgs) throws Exception {
        inferOutputPath(pGbdArgs);
        final List<Person> result = getEngine().processAuthorsCitation(pGbdArgs.getInput());
        Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.get(0).toTEI());
        LOGGER.info(result.get(0).toTEI());
    }
	
    /**
     * Process the affiliation using pGbdArgs parameters.
     * 
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processAffiliation(final GrobidMainArgs pGbdArgs) throws Exception {
        inferOutputPath(pGbdArgs);
        final List<Affiliation> result = getEngine().processAffiliation(pGbdArgs.getInput());
        Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.get(0).toTEI());
        LOGGER.info(result.get(0).toTEI());
    }

    /**
     * Process the raw reference using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processRawReference(final GrobidMainArgs pGbdArgs) throws Exception {
        inferOutputPath(pGbdArgs);
        final BiblioItem result = getEngine().processRawReference(pGbdArgs.getInput(), false);
        Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator + "result", result.toTEI(-1));
        LOGGER.info(result.toTEI(-1));
    }

    /**
     * Process all the references using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processReferences(final GrobidMainArgs pGbdArgs) throws Exception {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);
        final File pdfDirectory = new File(pGbdArgs.getPath2Input());
		File[] files = pdfDirectory.listFiles();
		if (files == null) {
			LOGGER.warn("No files in directory: " + pdfDirectory);
		}
		else {
			processReferencesDirectory(files, pGbdArgs, pGbdArgs.getPath2Output());
		}
	}
		
    /**
     * Process the references recursively or not using pGbdArgs parameters.
     *
	 * @param files list of files to be processed	
     * @param pGbdArgs The parameters.
     */
	private void processReferencesDirectory(File[] files, final GrobidMainArgs pGbdArgs, String outputPath) {	
        if (files != null) {
			boolean recurse = pGbdArgs.isRecursive();
			int id = 0;
            for (final File currPdf : files) {
                try {
                    if (currPdf.getName().toLowerCase().endsWith(".pdf")) {
                        final List<BibDataSet> results = 
							getEngine().processReferences(currPdf, false);
						File outputPathFile = new File(outputPath);
						if (!outputPathFile.exists()) {
							outputPathFile.mkdir();
						}

						StringBuilder result = new StringBuilder();
						// dummy header
						result.append("<?xml version=\"1.0\" ?>\n<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" " + 	
						"xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                		"\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");

						result.append("\t<teiHeader>\n\t\t<fileDesc xml:id=\"f_" + id + 
							"\"/>\n\t</teiHeader>\n");

						result.append("\t<text>\n\t\t<front/>\n\t\t<body/>\n\t\t<back>\n\t\t\t<listBibl>\n");
						for(BibDataSet res : results) {
							result.append(res.toTEI());
							result.append("\n");
						}
						result.append("\t\t\t</listBibl>\n\t\t</back>\n\t</text>\n</TEI>\n");

						if (currPdf.getName().endsWith(".pdf")) {
                        	Utilities.writeInFile(outputPath + File.separator
                                + new File(currPdf.getAbsolutePath()).getName().replace(".pdf", ".references.tei.xml"), 
									result.toString());
						}
						else if (currPdf.getName().endsWith(".PDF")) {
                        	Utilities.writeInFile(outputPath + File.separator
                                + new File(currPdf.getAbsolutePath()).getName().replace(".PDF", ".references.tei.xml"), 
									result.toString());
						}
					}
					else if (recurse && currPdf.isDirectory()) {
						File[] newFiles = currPdf.listFiles();
						if (newFiles != null) {
							String newLevel = currPdf.getName();
							processReferencesDirectory(newFiles, pGbdArgs, outputPath + 
								File.separator + newLevel);
						}
                    }
                }
				catch (final Exception exp) {
                    LOGGER.error("An error occured while processing the file " + currPdf.getAbsolutePath()
                            + ". Continuing the process for the other files", exp);
                }
				id++;
			}	 
		}
    }

    /**
     * Generate training data for the header model.
     *
     * @param pGbdArgs The parameters.
     */
    public void createTrainingHeader(final GrobidMainArgs pGbdArgs) {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);
        int result = getEngine().batchCreateTrainingHeader(pGbdArgs.getPath2Input(), pGbdArgs.getPath2Output(), -1);
        LOGGER.info(result + " files processed.");
    }

    /**
     * Generate training data for the full text model.
     *
     * @param pGbdArgs The parameters.
     */
    public void createTrainingFulltext(final GrobidMainArgs pGbdArgs) {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);
        int result = getEngine().batchCreateTrainingFulltext(pGbdArgs.getPath2Input(), pGbdArgs.getPath2Output(), -1);
        LOGGER.info(result + " files processed.");
    }

    /**
     * Generate training data for the segmentation model.
     *
     * @param pGbdArgs The parameters.
     */
    public void createTrainingSegmentation(final GrobidMainArgs pGbdArgs) {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);
        int result = getEngine().batchCreateTrainingSegmentation(pGbdArgs.getPath2Input(), pGbdArgs.getPath2Output(), -1);
        LOGGER.info(result + " files processed.");
    }

	/**
     * Generate training data for the reference segmentation model.
     *
     * @param pGbdArgs The parameters.
     */
    public void createTrainingReferenceSegmentation(final GrobidMainArgs pGbdArgs) {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);
        int result = getEngine().batchCreateTrainingReferenceSegmentation(pGbdArgs.getPath2Input(), pGbdArgs.getPath2Output(), -1);
        LOGGER.info(result + " files processed.");
    }

    /**
     * Generate training data for citation extraction from patent documents.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void createTrainingCitationPatent(final GrobidMainArgs pGbdArgs) throws Exception {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);
        int result = getEngine().batchCreateTrainingPatentcitations(pGbdArgs.getPath2Input(), pGbdArgs.getPath2Output());
        LOGGER.info(result + " files processed.");
    }

    /**
     * Process a patent encoded in TEI using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processCitationPatentTEI(final GrobidMainArgs pGbdArgs) throws Exception {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);

        final File teiDirectory = new File(pGbdArgs.getPath2Input());
        String result = StringUtils.EMPTY;
        for (final File currTEI : teiDirectory.listFiles()) {
            try {
                if (currTEI.getName().toLowerCase().endsWith(".tei") ||
                        currTEI.getName().toLowerCase().endsWith(".tei.xml")) {
                    getEngine().processCitationPatentTEI(pGbdArgs.getPath2Input() + File.separator + currTEI.getName(),
                            pGbdArgs.getPath2Output() + File.separator + currTEI.getName(), false);
                }
            } catch (final Exception exp) {
                LOGGER.error("An error occured while processing the file " + currTEI.getAbsolutePath()
                        + ". Continuing the process for the other files");
            }
        }
    }

    /**
     * Process a patent encoded in ST.36 using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processCitationPatentST36(final GrobidMainArgs pGbdArgs) throws Exception {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);

        final File xmlDirectory = new File(pGbdArgs.getPath2Input());
        String result = StringUtils.EMPTY;
        for (final File currXML : xmlDirectory.listFiles()) {
            try {
                if (currXML.getName().toLowerCase().endsWith(".xml") ||
                        currXML.getName().toLowerCase().endsWith(".xml.gz")) {
                    List<BibDataSet> articles = new ArrayList<BibDataSet>();
                    List<PatentItem> patents = new ArrayList<PatentItem>();
                    result = getEngine().processAllCitationsInXMLPatent(pGbdArgs.getPath2Input() + File.separator + currXML.getName(),
                            articles, patents, false);
                    if (currXML.getName().endsWith(".gz")) {
                        Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator
                                + new File(currXML.getAbsolutePath()).getName().replace(".xml.gz", ".tei.xml"), result);
                    } else {
                        Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator
                                + new File(currXML.getAbsolutePath()).getName().replace(".xml", ".tei.xml"), result);
                    }
                }
            } catch (final Exception exp) {
                LOGGER.error("An error occured while processing the file " + currXML.getAbsolutePath()
                        + ". Continuing the process for the other files");
            }
        }
    }

    /**
     * Process a patent in utf-8 text using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processCitationPatentTXT(final GrobidMainArgs pGbdArgs) throws Exception {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);

        final File txtDirectory = new File(pGbdArgs.getPath2Input());
        String result = StringUtils.EMPTY;
        for (final File currTXT : txtDirectory.listFiles()) {
            try {
                if (currTXT.getName().toLowerCase().endsWith(".txt")) {
                    String inputStr = FileUtils.readFileToString(currTXT, "UTF-8");
                    List<BibDataSet> articles = new ArrayList<BibDataSet>();
                    List<PatentItem> patents = new ArrayList<PatentItem>();
                    result = getEngine().processAllCitationsInPatent(inputStr, articles, patents, false);
                    Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator
                            + new File(currTXT.getAbsolutePath()).getName().replace(".txt", ".tei.xml"), result);
                }
            } catch (final Exception exp) {
                LOGGER.error("An error occured while processing the file " + currTXT.getAbsolutePath()
                        + ". Continuing the process for the other files");
            }
        }
    }

    /**
     * Process a patent available in PDF using pGbdArgs parameters.
     *
     * @param pGbdArgs The parameters.
     * @throws Exception
     */
    public void processCitationPatentPDF(final GrobidMainArgs pGbdArgs) throws Exception {
        inferPdfInputPath(pGbdArgs);
        inferOutputPath(pGbdArgs);

        final File pdfDirectory = new File(pGbdArgs.getPath2Input());
        String result = StringUtils.EMPTY;
        for (final File currPDF : pdfDirectory.listFiles()) {
            try {
                if (currPDF.getName().toLowerCase().endsWith(".pdf")) {
                    List<BibDataSet> articles = new ArrayList<BibDataSet>();
                    List<PatentItem> patents = new ArrayList<PatentItem>();
                    result = getEngine().processAllCitationsInPDFPatent(pGbdArgs.getPath2Input() + 
						File.separator + currPDF.getName(), articles, patents, false);
					if (currPDF.getName().endsWith(".pdf")) {
                    	Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator
                            + new File(currPDF.getAbsolutePath()).getName().replace(".pdf", ".tei.xml"), result);
					}
					else if (currPDF.getName().endsWith(".PDF")) {
                    	Utilities.writeInFile(pGbdArgs.getPath2Output() + File.separator
                            + new File(currPDF.getAbsolutePath()).getName().replace(".PDF", ".tei.xml"), result);
					}
                }
            } catch (final Exception exp) {
                LOGGER.error("An error occured while processing the file " + currPDF.getAbsolutePath()
                        + ". Continuing the process for the other files");
            }
        }
    }

    /**
     * List the engine methods that can be called.
     *
     * @return List<String> containing the list of the methods.
     */
    public final static List<String> getUsableMethods() {
        final Class<?> pClass = new ProcessEngine().getClass();
        final List<String> availableMethods = new ArrayList<String>();
        for (final Method method : pClass.getMethods()) {
            if (isUsableMethod(method.getName())) {
                availableMethods.add(method.getName());
            }
        }
        return availableMethods;
    }

    /**
     * Check if the method is usable.
     *
     * @param pMethod method name.
     * @return if it is usable
     */
    protected final static boolean isUsableMethod(final String pMethod) {
        boolean isUsable = StringUtils.equals("wait", pMethod);
        isUsable |= StringUtils.equals("equals", pMethod);
        isUsable |= StringUtils.equals("toString", pMethod);
        isUsable |= StringUtils.equals("hashCode", pMethod);
        isUsable |= StringUtils.equals("getClass", pMethod);
        isUsable |= StringUtils.equals("notify", pMethod);
        isUsable |= StringUtils.equals("notifyAll", pMethod);
        isUsable |= StringUtils.equals("isUsableMethod", pMethod);
        isUsable |= StringUtils.equals("getUsableMethods", pMethod);
        isUsable |= StringUtils.equals("inferPdfInputPath", pMethod);
        isUsable |= StringUtils.equals("inferOutputPath", pMethod);
        return !isUsable;
    }

    /**
     * Infer the input path for pdfs if not given in arguments.
     *
     * @param pGbdArgs The GrobidArgs.
     */
    protected final static void inferPdfInputPath(final GrobidMainArgs pGbdArgs) {
        String tmpFilePath;
        if (pGbdArgs.getPath2Input() == null) {
            tmpFilePath = new File(".").getAbsolutePath();
            LOGGER.info("No path set for the input directory. Using: " + tmpFilePath);
            pGbdArgs.setPath2Input(tmpFilePath);
        }
    }

    /**
     * Infer the output path if not given in arguments.
     *
     * @param pGbdArgs The GrobidArgs.
     */
    protected final static void inferOutputPath(final GrobidMainArgs pGbdArgs) {
        String tmpFilePath;
        if (pGbdArgs.getPath2Output() == null) {
            tmpFilePath = new File(".").getAbsolutePath();
            LOGGER.info("No path set for the output directory. Using: " + tmpFilePath);
            pGbdArgs.setPath2Output(tmpFilePath);
        }
    }

}
