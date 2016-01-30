/**
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grobid.core.engines;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.annotations.TeiStAXParser;
import org.grobid.core.data.Affiliation;
import org.grobid.core.data.BibDataSet;
import org.grobid.core.data.BiblioItem;
import org.grobid.core.data.BiblioSet;
import org.grobid.core.data.ChemicalEntity;
import org.grobid.core.data.PatentItem;
import org.grobid.core.data.Person;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.lang.Language;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.Utilities;
import org.grobid.core.utilities.counters.CntManager;
import org.grobid.core.utilities.counters.impl.CntManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for managing the extraction of bibliographical informations from PDF
 * documents or raw text.
 *
 * @author Patrice Lopez
 */
public class Engine implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);

    // path where the pdf file is stored
//	public String path = null;

    // Name of the pdf file
//	public String fileName = null;

    private final EngineParsers parsers = new EngineParsers();
    //TODO: when using one instance of Engine in e.g. grobid-service, then make this field not static
    private static CntManager cntManager = CntManagerFactory.getCntManager();

    // The list of accepted languages
    // the languages are encoded in ISO 3166
    // if null, all languages are accepted.
    private List<String> acceptedLanguages = null;

    /**
     * Parse a sequence of authors from a header, i.e. containing possibly
     * reference markers.
     *
     * @param authorSequence - the string corresponding to a raw sequence of names
     * @return the list of structured author object
     */
    public List<Person> processAuthorsHeader(String authorSequence) throws Exception {
        List<String> inputs = new ArrayList<String>();
        inputs.add(authorSequence);
        List<Person> result = parsers.getAuthorParser().processingHeader(inputs);
        //close();
        return result;
    }

    /**
     * Parse a sequence of authors from a citation, i.e. containing no reference
     * markers.
     *
     * @param authorSequence - the string corresponding to a raw sequence of names
     * @return the list of structured author object
     */
    public List<Person> processAuthorsCitation(String authorSequence) throws Exception {
        List<String> inputs = new ArrayList<String>();
        inputs.add(authorSequence);
        List<Person> result = parsers.getAuthorParser().processingCitation(inputs);
        //close();
        return result;
    }

    /**
     * Parse a list of independent sequences of authors from citations.
     *
     * @param authorSequences - the list of strings corresponding each to a raw sequence of
     *                        names.
     * @return the list of all recognized structured author objects for each
     *         sequence of authors.
     */
    public List<List<Person>> processAuthorsCitationLists(List<String> authorSequences) throws Exception {
        return null;
    }

    /**
     * Parse a text block corresponding to an affiliation+address.
     *
     * @param addressBlock - the string corresponding to a raw affiliation+address
     * @return the list of all recognized structured affiliation objects.
     * @throws IOException
     */
    public List<Affiliation> processAffiliation(String addressBlock) throws IOException {
        return parsers.getAffiliationAddressParser().processing(addressBlock);
    }

    /**
     * Parse a list of text blocks corresponding to an affiliation+address.
     *
     * @param addressBlocks - the list of strings corresponding each to a raw
     *                      affiliation+address.
     * @return the list of all recognized structured affiliation objects for
     *         each sequence of affiliation + address block.
     */
    public List<List<Affiliation>> processAffiliations(List<String> addressBlocks) throws Exception {
        List<List<Affiliation>> results = null;
        for (String addressBlock : addressBlocks) {
            List<Affiliation> localRes = parsers.getAffiliationAddressParser().processing(addressBlock);
            if (results == null) {
                results = new ArrayList<List<Affiliation>>();
            }
            results.add(localRes);
        }
        return results;
    }

    /**
     * Parse a raw string containing dates.
     *
     * @param dateBlock - the string containing raw dates.
     * @return the list of all structured date objects recognized in the string.
     * @throws IOException
     */
    public List<org.grobid.core.data.Date> processDate(String dateBlock) throws IOException {
        List<org.grobid.core.data.Date> result = parsers.getDateParser().processing(dateBlock);
        //close();
        return result;
    }

    /**
     * Parse a list of raw dates.
     *
     * @param dateBlocks - the list of strings each containing raw dates.
     * @return the list of all structured date objects recognized in the string
     *         for each inputed string.
     */
    public List<List<org.grobid.core.data.Date>> processDates(List<String> dateBlocks) {
        return null;
    }

    /**
     * Apply a parsing model for a given single raw reference string based on
     * CRF
     *
     * @param reference   : the reference string to be processed
     * @param consolidate - the consolidation option allows GROBID to exploit Crossref
     *                    web services for improving header information
     * @return the recognized bibliographical object
     */
    public BiblioItem processRawReference(String reference, boolean consolidate) {
        if (reference != null) {
            reference = reference.replaceAll("\\\\", "");
        }
        return parsers.getCitationParser().processing(reference, consolidate);
    }

    /**
     * Apply a parsing model for a set of raw reference text based on CRF
     *
     * @param references  : the list of raw reference string to be processed
     * @param consolidate - the consolidation option allows GROBID to exploit Crossref
     *                    web services for improving header information
     * @return the list of recognized bibliographical objects
     */
    public List<BiblioItem> processRawReferences(List<String> references, boolean consolidate) throws Exception {
        if (references == null)
            return null;
        if (references.size() == 0)
            return null;
        List<BiblioItem> results = new ArrayList<BiblioItem>();
        for (String reference : references) {
            BiblioItem bit = parsers.getCitationParser().processing(reference, consolidate);
            results.add(bit);
        }
        return results;
    }

    /**
     * Constructor for the Grobid engine instance.
     */
    public Engine() {
        /*
         * Runtime.getRuntime().addShutdownHook(new Thread() {
		 * 
		 * @Override public void run() { try { close(); } catch (IOException e)
		 * { LOGGER.error("Failed to close all resources: " + e); } } });
		 */
    }

    /**
     * Apply a parsing model to the reference block of a PDF file based on CRF
     *
     * @param inputFile   : the path of the PDF file to be processed
     * @param consolidate - the consolidation option allows GROBID to exploit Crossref
     *                    web services for improving header information
     * @return the list of parsed references as bibliographical objects enriched
     *         with citation contexts
     */
    public List<BibDataSet> processReferences(File inputFile, boolean consolidate) {
        return parsers.getCitationParser()
			.processingReferenceSection(inputFile, parsers.getReferenceSegmenterParser(), consolidate);
    }

    /**
     * Create training data for the segmentation of a reference block based on a PDF file containing
     * a reference section and the current reference segmentation model 
     *
     * @param input   : the path of the PDF file to be processed
     * @param pathTEI : the path of the training data will be written as TEI file
	 * @param id : an optional ID to be used in the TEI file, -1 if not to be used 
     */
    public void createTrainingReferenceSegmentation(File input, String pathTEI, int id) throws Exception {
        if (input == null) {
            throw new GrobidResourceException("Cannot process pdf file, because input file was null.");
        }
        if (!input.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because input file '" +
                    input.getAbsolutePath() + "' does not exists.");
        }
		File resultPathFile = new File(pathTEI);
        if (!resultPathFile.exists()) {
            if (!resultPathFile.mkdirs()) {
                throw new GrobidResourceException("Cannot start parsing, because cannot create "
                        + "output path for tei files on location '" 
						+ resultPathFile.getAbsolutePath() + "'.");
            }
        }

        try {
            // general segmentation
            Document doc = parsers.getSegmentationParser().processing(input, GrobidAnalysisConfig.defaultInstance());
			String referencesStr = doc.getDocumentPartText(SegmentationLabel.REFERENCES);
            if (!referencesStr.isEmpty()) {
				//String tei = parsers.getReferenceSegmenterParser().createTrainingData2(referencesStr, id);
				org.grobid.core.utilities.Pair<String,String> result = 
					parsers.getReferenceSegmenterParser().createTrainingData(doc, id);
				String tei = result.getA();
				String raw = result.getB();	
				if (tei != null) {
                    String outPath = pathTEI + "/" + 
						input.getName().replace(".pdf", ".training.referenceSegmenter.tei.xml");
                    Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPath), false), "UTF-8");
                    writer.write(tei + "\n");
                    writer.close();

					// generate also the raw vector file with the features
					outPath = pathTEI + "/" + input.getName().replace(".pdf", ".training.referenceSegmenter");
                    writer = new OutputStreamWriter(new FileOutputStream(new File(outPath), false), "UTF-8");
                    writer.write(raw + "\n");
                    writer.close();

					// also write the raw text as it is before reference segmentation
					String outPathRawtext = pathTEI + "/" + input.getName()
						.replace(".pdf", ".training.referenceSegmenter.rawtxt");
					Writer strWriter = new OutputStreamWriter(
						new FileOutputStream(new File(outPathRawtext), false), "UTF-8");
					strWriter.write(referencesStr + "\n");
					strWriter.close();
                }
			}
		}
		catch (IOException e) {
            throw new GrobidException("An IO exception occurred while running Grobid.", e);
        }
    }

	/**
     * Create training data for the segmentation of a reference block based on a repository of PDF files 
	 * containing a reference section and the current reference segmentation model 
     *
     * @param directoryPath   : the path of the repository of PDF files to be processed
     * @param resultPath : the path to the repository where to write the training data as a TEI files
	 * @param id : an optional ID to be used in the TEI file, -1 if not to be used 
     */
    public int batchCreateTrainingReferenceSegmentation(String directoryPath, String resultPath, int id) {
		return batchCreateTraining(directoryPath, resultPath, id, 3);
    }

    /**
     * Download a PDF file.
     *
     * @param url     URL of the PDF to download
     * @param dirName directory where to store the downloaded PDF
     * @param name file name
     */
    public String downloadPDF(String url, String dirName, String name) {
        return Utilities.uploadFile(url, dirName, name);
    }

    /**
     * Give the list of languages for which an extraction is allowed. If null,
     * any languages will be processed
     *
     * @return the list of languages to be processed coded in ISO 3166.
     */
    public List<String> getAcceptedLanguages() {
        return acceptedLanguages;
    }

    /**
     * Add a language to the list of accepted languages.
     *
     * @param lang the language in ISO 3166 to be added
     */
    public void addAcceptedLanguages(String lang) {
        if (acceptedLanguages == null) {
            acceptedLanguages = new ArrayList<String>();
        }
        acceptedLanguages.add(lang);
    }

    /**
     * Perform a language identification
     *
     * @param ext part
     * @return language
     */
    public Language runLanguageId(String filePath, String ext) {
        try {
            // we just skip the 50 first lines and get the next approx. 5000
            // first characters,
            // which should give a ~100% accuracy for the supported languages
            String text = "";
            FileInputStream fileIn = new FileInputStream(filePath.substring(0, filePath.length() - 3) + ext);
            InputStreamReader reader = new InputStreamReader(fileIn, "UTF-8");
            BufferedReader bufReader = new BufferedReader(reader);
            String line;
            // int nbLine = 0;
            int nbChar = 0;
            while (((line = bufReader.readLine()) != null) && (nbChar < 5000)) {
                if (line.length() == 0)
                    continue;
                text += " " + line;
                nbChar += line.length();
            }
            bufReader.close();
            LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
            return languageUtilities.runLanguageId(text);
        } catch (IOException e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
    }

    /**
     * Basic run for language identification, default is on the body of the
     * current document.
     *
     * @return language id
     */
    public Language runLanguageId(String filePath) {
        return runLanguageId(filePath, "body");
    }

    /**
     * Apply a parsing model for the header of a PDF file based on CRF, using
     * first three pages of the PDF
     *
     * @param inputFile   : the path of the PDF file to be processed
     * @param consolidate - the consolidation option allows GROBID to exploit Crossref
     *                    web services for improving header information
     * @param result      bib result
     * @return the TEI representation of the extracted bibliographical
     *         information
     */
    public String processHeader(String inputFile, boolean consolidate, BiblioItem result) {
        GrobidAnalysisConfig config = new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder().startPage(0).endPage(2).build();
        return processHeader(inputFile, consolidate, config, result);
    }

    /**
     * Apply a parsing model for the header of a PDF file based on CRF, using
     * dynamic range of pages as header
     *
     * @param inputFile   : the path of the PDF file to be processed
     * @param consolidate - the consolidation option allows GROBID to exploit Crossref
     *                    web services for improving header information
     * @param result      bib result
     * @return the TEI representation of the extracted bibliographical
     *         information
     */
    public String processHeader(String inputFile, boolean consolidate, GrobidAnalysisConfig config, BiblioItem result) {
        // normally the BiblioItem reference must not be null, but if it is the
        // case, we still continue
        // with a new instance, so that the resulting TEI string is still
        // delivered
        if (result == null) {
            result = new BiblioItem();
        }

        Pair<String, Document> resultTEI = parsers.getHeaderParser()
			.processing2(inputFile, consolidate, result, config);
		//Pair<String, Document> resultTEI = parsers.getHeaderParser().processing(inputFile, consolidate, result);
        Document doc = resultTEI.getRight();
        //close();
        return resultTEI.getLeft();
    }

    /**
     * Use the segmentation model to identify the header section of a PDF file, then apply a parsing model for the
     * header based on CRF
     *
     * @param inputFile   : the path of the PDF file to be processed
     * @param consolidate - the consolidation option allows GROBID to exploit Crossref
     *                    web services for improving header information
     * @param result      bib result
     * @return the TEI representation of the extracted bibliographical
     *         information
     */
    public String segmentAndProcessHeader(File inputFile, boolean consolidate, BiblioItem result) {
        // normally the BiblioItem reference must not be null, but if it is the
        // case, we still continue
        // with a new instance, so that the resulting TEI string is still
        // delivered
        if (result == null) {
            result = new BiblioItem();
        }

        Pair<String, Document> resultTEI = parsers.getHeaderParser().processing(inputFile, result,
                GrobidAnalysisConfig.builder().consolidateHeader(consolidate).build());
        Document doc = resultTEI.getRight();
        //close();
        return resultTEI.getLeft();
    }

    /**
     * Create training data for the header model based on the application of the
     * current header model on a new PDF
     *
     * @param inputFile  : the path of the PDF file to be processed
     * @param pathHeader : the path where to put the header with layout features
     * @param pathTEI    : the path where to put the annotated TEI representation (the
     *                   file to be corrected for gold-level training data)
     * @param id         : an optional ID to be used in the TEI file and the header
     *                   file
     */
    public void createTrainingHeader(String inputFile, String pathHeader, String pathTEI, int id) {
        Document doc = parsers.getHeaderParser().createTrainingHeader(inputFile, pathHeader, pathTEI);
    }

    /**
     * Create training data for the full text model based on the application of
     * the current full text model on a new PDF
     *
     * @param inputFile    : the path of the PDF file to be processed
     * @param pathFullText : the path where to put the full text with layout features
     * @param pathTEI      : the path where to put the annotated TEI representation (the
     *                     file to be corrected for gold-level training data)
     * @param id           : an optional ID to be used in the TEI file and the full text
     *                     file, -1 if not used
     */
    public void createTrainingFullText(File inputFile, String pathFullText, String pathTEI, int id) {
        Document doc = parsers.getFullTextParser().createTrainingFullText(inputFile, pathFullText, pathTEI, id);
    }

    /**
     * Create training data for the segmenation model based on the application of
     * the current segmentation model on a new PDF
     *
     * @param inputFile        : the path of the PDF file to be processed
     * @param pathSegmentation : the path where to put the segmentation text with layout features
     * @param pathTEI          : the path where to put the annotated TEI representation (the
     *                         file to be corrected for gold-level training data)
     * @param id               : an optional ID to be used in the TEI file and the segmentation text
     *                         file, -1 if not used
     */
    public void createTrainingSegmentation(String inputFile, String pathSegmentation, String pathTEI, int id) {
        parsers.getSegmentationParser().createTrainingSegmentation(inputFile, pathSegmentation, pathTEI, id);
    }

    /**
     * Parse and convert the current article into TEI, this method performs the
     * whole parsing and conversion process. If onlyHeader is true, than only
     * the tei header data will be created.
     *
     * @param inputFile            - absolute path to the pdf to be processed
     * @param consolidateHeader    - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving header information
     * @param consolidateCitations - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving citations information
	 * @return the resulting structured document as a TEI string.
     */
//    public String fullTextToTEI(File inputFile,
//								GrobidAnalysisConfig config) throws Exception {
//
//		return fullTextToTEI(inputFile, consolidateHeader, consolidateCitations, null, -1, -1, false);
//	}

    /**
     * Parse and convert the current article into TEI, this method performs the
     * whole parsing and conversion process. If onlyHeader is true, than only
     * the tei header data will be created.
     *
     * @param inputFile            - absolute path to the pdf to be processed
     * @param consolidateHeader    - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving header information
     * @param consolidateCitations - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving citations information
     * @param assetPath if not null, the PDF assets (embedded images) will be extracted and 
	 * saved under the indicated repository path		
	 * @return the resulting structured document as a TEI string.
     */
//    public String fullTextToTEI(String inputFile,
//								GrobidAnalysisConfig config) throws Exception {
//		return fullTextToTEI(inputFile, config);
//	}

    /**
     *
     * //TODO: remove invalid JavaDoc once refactoring is done and tested (left for easier reference)
     * Parse and convert the current article into TEI, this method performs the
     * whole parsing and conversion process. If onlyHeader is true, than only
     * the tei header data will be created.
     *
     * @param inputFile            - absolute path to the pdf to be processed
     * @param config               - Grobid config
	 * @return the resulting structured document as a TEI string.
     */

    public String fullTextToTEI(File inputFile,
                                GrobidAnalysisConfig config) throws Exception {
        return fullTextToTEIDoc(inputFile, config).getTei();
    }

    public Document fullTextToTEIDoc(File inputFile,
                                     GrobidAnalysisConfig config) throws Exception {
        FullTextParser fullTextParser = parsers.getFullTextParser();

        // replace by the commented version for the new full ML text parser
        Document resultDoc;
        LOGGER.debug("Starting processing fullTextToTEI on " + inputFile);
        long time = System.currentTimeMillis();
        resultDoc = fullTextParser.processing(inputFile, config);
        LOGGER.debug("Ending processing fullTextToTEI on " + inputFile + ". Time to process: "
			+ (System.currentTimeMillis() - time) + "ms");
        if (config.getPdfAssetPath() != null) {
            FileUtils.write(new File(config.getPdfAssetPath(), Files.getNameWithoutExtension(inputFile.getName()) + ".xml"), resultDoc.getTei());
        }
        return resultDoc;
    }

    public Document fullTextToTEIDoc(DocumentSource documentSource,
                                     GrobidAnalysisConfig config) throws Exception {
        FullTextParser fullTextParser = parsers.getFullTextParser();

        // replace by the commented version for the new full ML text parser
        Document resultDoc;
        LOGGER.debug("Starting processing fullTextToTEI on " + documentSource);
        long time = System.currentTimeMillis();
        resultDoc = fullTextParser.processing(documentSource, config);
        LOGGER.debug("Ending processing fullTextToTEI on " + documentSource + ". Time to process: "
                + (System.currentTimeMillis() - time) + "ms");
        if (config.getPdfAssetPath() != null) {
            FileUtils.write(new File(config.getPdfAssetPath(), Files.getNameWithoutExtension(documentSource.getPdfFile().getName()) + ".xml"), resultDoc.getTei());
        }
        return resultDoc;
    }

    /**
     * Process all the PDF in a given directory with a header extraction and
     * produce the corresponding training data format files for manual
     * correction. The goal of this method is to help to produce additional
     * traning data based on an existing model.
     *
     * @param directoryPath - the path to the directory containing PDF to be processed.
     * @param resultPath    - the path to the directory where the results as XML files
     *                      shall be written.
     * @param ind           - identifier integer to be included in the resulting files to
     *                      identify the training case. This is optional: no identifier
     *                      will be included if ind = -1
     * @return the number of processed files.
     */
    public int batchCreateTrainingHeader(String directoryPath, String resultPath, int ind) {
        return batchCreateTraining(directoryPath, resultPath, ind, 0);
    }

    /**
     * Process all the PDF in a given directory with a fulltext extraction and
     * produce the corresponding training data format files for manual
     * correction. The goal of this method is to help to produce additional
     * traning data based on an existing model.
     *
     * @param directoryPath - the path to the directory containing PDF to be processed.
     * @param resultPath    - the path to the directory where the results as XML files
     *                      shall be written.
     * @param ind           - identifier integer to be included in the resulting files to
     *                      identify the training case. This is optional: no identifier
     *                      will be included if ind = -1
     * @return the number of processed files.
     */
    public int batchCreateTrainingFulltext(String directoryPath, String resultPath, int ind) {
        return batchCreateTraining(directoryPath, resultPath, ind, 1);
    }

    /**
     * Process all the PDF in a given directory with a segmentation process and
     * produce the corresponding training data format files for manual
     * correction. The goal of this method is to help to produce additional
     * traning data based on an existing model.
     *
     * @param directoryPath - the path to the directory containing PDF to be processed.
     * @param resultPath    - the path to the directory where the results as XML files
     *                      shall be written.
     * @param ind           - identifier integer to be included in the resulting files to
     *                      identify the training case. This is optional: no identifier
     *                      will be included if ind = -1
     * @return the number of processed files.
     */
    public int batchCreateTrainingSegmentation(String directoryPath, String resultPath, int ind) {
        return batchCreateTraining(directoryPath, resultPath, ind, 2);
    }

    private int batchCreateTraining(String directoryPath, String resultPath, int ind, int type) {
        try {
            File path = new File(directoryPath);
            // we process all pdf files in the directory
            File[] refFiles = path.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    System.out.println(name);
                    return name.endsWith(".pdf") || name.endsWith(".PDF");
                }
            });

            if (refFiles == null)
                return 0;

            System.out.println(refFiles.length + " files to be processed.");

            int n = 0;
			if (ind == -1) {
				// for undefined identifier (value at -1), we initialize it to 0
				n = 1;
			}
            // for (; n < refFiles.length; n++) {
            for (final File pdfFile : refFiles) {
                try {
                    // File pdfFile = refFiles[n];
                    // if (pdfFile.getAbsolutePath().endsWith(".pdf")) {
                    if (type == 0) {
                        createTrainingHeader(pdfFile.getPath(), resultPath, resultPath, ind + n);
                    } else if (type == 1) {
                        createTrainingFullText(pdfFile, resultPath, resultPath, ind + n);
                    } else if (type == 2) {
                        createTrainingSegmentation(pdfFile.getPath(), resultPath, resultPath, ind + n);
                    } else if (type == 3) {
                        createTrainingReferenceSegmentation(pdfFile, resultPath, ind + n);
                    }
                } catch (final Exception exp) {
                    LOGGER.error("An error occured while processing the following pdf: " 
						+ pdfFile.getPath() + ": " + exp);
                }
				if (ind != -1)
					n++;
            }

            return refFiles.length;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occured while running Grobid batch.", exp);
        }
    }

    /**
     * Extract the headers for all PDF files in a given directory and produce
     * the results as an XML file TEI conformant.
     *
     * @param directoryPath - the path to the directory containing PDF to be processed.
     * @param resultPath    - the path to the directory where the results as XML files
     *                      shall be written.
     * @param consolidate   - the consolidation option allows GROBID to exploit Crossref
     *                      web services for improving header information
     * @return the number of processed files.
     */
    public int batchProcessHeader(String directoryPath, String resultPath, boolean consolidate) 
		throws Exception {
        return batchProcess(directoryPath, resultPath, consolidate, consolidate, 0);
    }

    /**
     * Extract the fulltext for all PDF files in a given directory and produce
     * the results as an XML file TEI conformant.
     *
     * @param directoryPath        - the path to the directory containing PDF to be processed.
     * @param resultPath           - the path to the directory where the results as XML files
     *                             shall be written.
     * @param consolidateHeader    - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving header information
     * @param consolidateCitations - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving citations information
     * @return the number of processed files.
     */
    public int batchProcessFulltext(String directoryPath, String resultPath, boolean consolidateHeader, 
		boolean consolidateCitations) {
        return batchProcess(directoryPath, resultPath, consolidateHeader, consolidateCitations, 1);
    }

    /**
     * @param directoryPath        input path, folder where the pdf files are supposed to be
     *                             located
     * @param resultPath           output path, folder where the tei files pdfs are written to
     * @param consolidateHeader    consolidate header
     * @param consolidateCitations consolidate citations
     * @param type                 type of the method
     * @return exit code
     */
    private int batchProcess(String directoryPath, String resultPath, boolean consolidateHeader, 
		boolean consolidateCitations, int type) {
        if (directoryPath == null) {
            throw new GrobidResourceException("Cannot start parsing, because the input path, "
                    + "where the pdf files are supposed to be located is null.");
        }
        if (resultPath == null) {
            throw new GrobidResourceException("Cannot start parsing, because the output path, "
                    + "where the tei files will be written to is null.");
        }
        File path = new File(directoryPath);
        if (!path.exists()) {
            throw new GrobidResourceException("Cannot start parsing, because the input path, "
                    + "where the pdf files are supposed to be located '" + path.getAbsolutePath() 
					+ "' does not exists.");
        }
        File resultPathFile = new File(resultPath);
        if (!resultPathFile.exists()) {
            if (!resultPathFile.mkdirs()) {
                throw new GrobidResourceException("Cannot start parsing, because cannot create "
                        + "output path for tei files on location '" + resultPathFile.getAbsolutePath() 
						+ "'.");
            }
        }

        try {
            // we process all pdf files in the directory
            File[] refFiles = path.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf") || name.endsWith(".PDF");
                }
            });

            if (refFiles == null)
                return 0;

            // System.out.println(refFiles.length + " files to be processed.");

            int n = 0;
            for (; n < refFiles.length; n++) {
                File pdfFile = refFiles[n];
                if (!pdfFile.exists()) {
                    throw new GrobidResourceException("A problem occurs in reading pdf file '" 
						+ pdfFile.getAbsolutePath()
                        + "'. The file does not exists. ");
                }
                if (type == 0) {
                    // BiblioItem res = processHeader(pdfFile.getPath(),
                    // consolidateHeader);
                    BiblioItem res = new BiblioItem();
                    String tei = processHeader(pdfFile.getPath(), consolidateHeader, res);
                    // if (res!= null) {
                    if (tei != null) {
                        String outPath = resultPath + "/" + pdfFile.getName().replace(".pdf", 
							GrobidProperties.FILE_ENDING_TEI_HEADER);
                        Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPath), 
							false), "UTF-8");
                        // writer.write(res.toTEI(0) + "\n");
                        writer.write(tei + "\n");
                        writer.close();
                    }
                } else if (type == 1) {
                    GrobidAnalysisConfig config = GrobidAnalysisConfig.builder()
                            .consolidateHeader(consolidateHeader)
                            .consolidateCitations(consolidateCitations)
                            .build();
                    String tei = fullTextToTEI(pdfFile, config);
                    if (tei != null) {
                        String outPath = resultPath + "/" + pdfFile.getName().replace(".pdf", 
							GrobidProperties.FILE_ENDING_TEI_FULLTEXT);
                        Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPath), 
							false), "UTF-8");
                        writer.write(tei + "\n");
                        writer.close();
                    }
                }
				/*
				 * else if (type == 2) { processCitations(pdfFile.getPath(),
				 * resultPath, resultPath); }
				 */
            }

            return refFiles.length;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    /**
     * Get the TEI XML string corresponding to the recognized header text
     */
    public static String header2TEI(BiblioItem resHeader) {
        return resHeader.toTEI(0);
    }

    /**
     * Get the BibTeX string corresponding to the recognized header text
     */
    public static String header2BibTeX(BiblioItem resHeader) {
        return resHeader.toBibTeX();
    }

    /**
     * Get the TEI XML string corresponding to the recognized citation section
     */
    public static String references2TEI2(String path, List<BibDataSet> resBib) {
        StringBuilder result = new StringBuilder();
        result.append("<tei>\n");

        BiblioSet bs = new BiblioSet();

        for (BibDataSet bib : resBib) {
            BiblioItem bit = bib.getResBib();
            bit.buildBiblioSet(bs, path);
        }

        result.append(bs.toTEI());
        result.append("<listbibl>\n");

        for (BibDataSet bib : resBib) {
            BiblioItem bit = bib.getResBib();
            result.append("\n").append(bit.toTEI2(bs));
        }
        result.append("\n</listbibl>\n</tei>\n");

        return result.toString();
    }

    /**
     * Get the TEI XML string corresponding to the recognized citation section,
     * with pointers and advanced structuring
     */
    public static String references2TEI(String path, List<BibDataSet> resBib) {
        StringBuilder result = new StringBuilder();
        result.append("<listbibl>\n");

        int p = 0;
        for (BibDataSet bib : resBib) {
            BiblioItem bit = bib.getResBib();
            bit.setPath(path);
            result.append("\n").append(bit.toTEI(p));
            p++;
        }
        result.append("\n</listbibl>\n");
        return result.toString();
    }

    /**
     * Get the BibTeX string corresponding to the recognized citation section
     */
    public String references2BibTeX(String path, List<BibDataSet> resBib) {
        StringBuilder result = new StringBuilder();

        for (BibDataSet bib : resBib) {
            BiblioItem bit = bib.getResBib();
            bit.setPath(path);
            result.append("\n").append(bit.toBibTeX());
        }

        return result.toString();
    }

    /**
     * Get the TEI XML string corresponding to the recognized citation section
     * for a particular citation
     */
    public static String reference2TEI(String path, List<BibDataSet> resBib, int i) {
        StringBuilder result = new StringBuilder();

        if (resBib != null) {
            if (i <= resBib.size()) {
                BibDataSet bib = resBib.get(i);
                BiblioItem bit = bib.getResBib();
                bit.setPath(path);
                result.append(bit.toTEI(i));
            }
        }

        return result.toString();
    }

    /**
     * Get the BibTeX string corresponding to the recognized citation section
     * for a given citation
     */
    public static String reference2BibTeX(String path, List<BibDataSet> resBib, int i) {
        StringBuilder result = new StringBuilder();

        if (resBib != null) {
            if (i <= resBib.size()) {
                BibDataSet bib = resBib.get(i);
                BiblioItem bit = bib.getResBib();
                bit.setPath(path);
                result.append(bit.toBibTeX());
            }
        }

        return result.toString();
    }

    /**
     * Extract and parse patent references within a patent. Result are provided
     * as PatentItem containing both "WISIWIG" results (the patent reference
     * attributes as they appear in the text) and the attributes in DOCDB format
     * (format according to WIPO and ISO standards). Offset positions are given
     * in the PatentItem object.
     *
     * @param text - the string corresponding to the text body of the patent.
     * @return the list of extracted and parserd patent references as PatentItem
     *         object.
     */
    /*public List<PatentItem> processPatentCitationsInPatent(String text) throws Exception {
        List<PatentItem> patents = new ArrayList<PatentItem>();
        // we initialize the attribute individually for readability...
        boolean filterDuplicate = false;
        boolean consolidate = false;
        parsers.getReferenceExtractor().extractAllReferencesString(text, filterDuplicate, consolidate, patents, null);
        return patents;
    }*/

    /**
     * Extract and parse non patent references within a patent. Result are
     * provided as a BibDataSet with offset position instanciated relative to
     * input text.
     *
     * @param text                 - the string corresponding to the text body of the patent.
     * @param consolidateCitations - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving citations information
     * @return the list of extracted and parserd non patent references as
     *         BiblioItem object.
     */
    /*public List<BibDataSet> processNPLCitationsInPatent(String text, boolean consolidateCitations) throws Exception {
        List<BibDataSet> articles = new ArrayList<BibDataSet>();
        // we initialize the attribute individually for readability...
        boolean filterDuplicate = false;
        parsers.getReferenceExtractor().extractAllReferencesString(text, filterDuplicate, consolidateCitations, null, articles);
        return articles;
    }*/

    /**
     * Extract and parse both patent and non patent references within a patent
     * text. Result are provided as a BibDataSet with offset position
     * instanciated relative to input text and as PatentItem containing both
     * "WISIWIG" results (the patent reference attributes as they appear in the
     * text) and the attributes in DOCDB format (format according to WIPO and
     * ISO standards). Patent references' offset positions are also given in the
     * PatentItem object.
     *
     * @param text                 - the string corresponding to the text body of the patent.
     * @param nplResults           - the list of extracted and parsed non patent references as
     *                             BiblioItem object. This list must be instanciated before
     *                             calling the method for receiving the results.
     * @param patentResults        - the list of extracted and parsed patent references as
     *                             PatentItem object. This list must be instanciated before
     *                             calling the method for receiving the results.
     * @param consolidateCitations - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving citations information
     * @return the list of extracted and parserd patent and non-patent references
     *         encoded in TEI.
     */
    public String processAllCitationsInPatent(String text, List<BibDataSet> nplResults, List<PatentItem> patentResults,
                                              boolean consolidateCitations) throws Exception {
        if ((nplResults == null) && (patentResults == null)) {
            return null;
        }
        // we initialize the attribute individually for readability...
        boolean filterDuplicate = false;
        return parsers.getReferenceExtractor().extractAllReferencesString(text, filterDuplicate, 
			consolidateCitations, patentResults, nplResults);
    }

    /**
     * Extract and parse both patent and non patent references within a patent
     * in ST.36 format. Result are provided as a BibDataSet with offset position
     * instanciated relative to input text and as PatentItem containing both
     * "WISIWIG" results (the patent reference attributes as they appear in the
     * text) and the attributes in DOCDB format (format according to WIPO and
     * ISO standards). Patent references' offset positions are also given in the
     * PatentItem object.
     *
     * @param xmlPath              xml path
     * @param nplResults           - the list of extracted and parsed non patent references as
     *                             BiblioItem object. This list must be instanciated before
     *                             calling the method for receiving the results.
     * @param patentResults        - the list of extracted and parsed patent references as
     *                             PatentItem object. This list must be instanciated before
     *                             calling the method for receiving the results.
     * @param consolidateCitations - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving citations information
     * @return the list of extracted and parserd patent and non-patent references
     *         encoded in TEI.
     * @throws Exception if sth. went wrong
     */
    public String processAllCitationsInXMLPatent(String xmlPath, List<BibDataSet> nplResults, 
			List<PatentItem> patentResults,
            boolean consolidateCitations) throws Exception {
        if ((nplResults == null) && (patentResults == null)) {
            return null;
        }
        // we initialize the attribute individually for readability...
        boolean filterDuplicate = false;
        return parsers.getReferenceExtractor().extractAllReferencesXMLFile(xmlPath, filterDuplicate, 
			consolidateCitations, patentResults, nplResults);
    }

    /**
     * Extract and parse both patent and non patent references within a patent
     * in PDF format. Result are provided as a BibDataSet with offset position
     * instanciated relative to input text and as PatentItem containing both
     * "WISIWIG" results (the patent reference attributes as they appear in the
     * text) and the attributes in DOCDB format (format according to WIPO and
     * ISO standards). Patent references' offset positions are also given in the
     * PatentItem object.
     *
     * @param pdfPath              pdf path
     * @param nplResults           - the list of extracted and parsed non patent references as
     *                             BiblioItem object. This list must be instanciated before
     *                             calling the method for receiving the results.
     * @param patentResults        - the list of extracted and parsed patent references as
     *                             PatentItem object. This list must be instanciated before
     *                             calling the method for receiving the results.
     * @param consolidateCitations - the consolidation option allows GROBID to exploit Crossref
     *                             web services for improving citations information
     * @return the list of extracted and parserd patent and non-patent references
     *         encoded in TEI.
     * @throws Exception if sth. went wrong
     */
    public String processAllCitationsInPDFPatent(String pdfPath, List<BibDataSet> nplResults,
                                                 List<PatentItem> patentResults,
                                                 boolean consolidateCitations) throws Exception {
        if ((nplResults == null) && (patentResults == null)) {
            return null;
        }
        // we initialize the attribute individually for readability...
        boolean filterDuplicate = false;
        return parsers.getReferenceExtractor().extractAllReferencesPDFFile(pdfPath, filterDuplicate,
                consolidateCitations, patentResults, nplResults);
    }

    public void processCitationPatentTEI(String teiPath, String outTeiPath,
                                         boolean consolidateCitations) throws Exception {
        try {
            InputStream inputStream = new FileInputStream(new File(teiPath));
            OutputStream output = new FileOutputStream(new File(outTeiPath));
            final TeiStAXParser parser = new TeiStAXParser(inputStream, output, false, 
				consolidateCitations);
            parser.parse();
            inputStream.close();
            output.close();
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }


    /**
     * Process an XML patent document with a patent citation extraction and
     * produce the corresponding training data format files for manual
     * correction. The goal of this method is to help to produce additional
     * traning data based on an existing model.
     *
     * @param pathXML    - the path to the XML patent document to be processed.
     * @param resultPath - the path to the directory where the results as XML files
     *                   shall be written.
     */
    public void createTrainingPatentCitations(String pathXML, String resultPath) 
		throws Exception {
        parsers.getReferenceExtractor().generateTrainingData(pathXML, resultPath);
    }

    /**
     * Process all the XML patent documents in a given directory with a patent
     * citation extraction and produce the corresponding training data format
     * files for manual correction. The goal of this method is to help to
     * produce additional traning data based on an existing model.
     *
     * @param directoryPath - the path to the directory containing XML files to be
     *                      processed.
     * @param resultPath    - the path to the directory where the results as XML files
     *                      shall be written.
     * @return the number of processed files.
     */
    public int batchCreateTrainingPatentcitations(String directoryPath, String resultPath) 
		throws Exception {
        try {
            File path = new File(directoryPath);
            // we process all xml files in the directory
            File[] refFiles = path.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml") || name.endsWith(".XML") ||
                            name.endsWith(".xml.gz") || name.endsWith(".XML.gz");
                }
            });

            if (refFiles == null)
                return 0;

            // System.out.println(refFiles.length + " files to be processed.");

            int n = 0;
            for (; n < refFiles.length; n++) {
                File xmlFile = refFiles[n];
                createTrainingPatentCitations(xmlFile.getPath(), resultPath);
            }

            return refFiles.length;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    /**
     * Extract chemical names from text.
     *
     * @param text - text to be processed.
     * @return List of chemical entites as POJO.
     */
    public List<ChemicalEntity> extractChemicalEntities(String text) throws Exception {
        return parsers.getChemicalParser().extractChemicalEntities(text);
    }

    /**
     * Print the abstract content. Useful for term extraction.
     */
    public String getAbstract(Document doc) throws Exception {
        String abstr = doc.getResHeader().getAbstract();
        abstr = abstr.replace("@BULLET", " • ");
        return abstr;
    }

    /**
     * Return all the reference titles. Maybe useful for term extraction.
     */
    public String printRefTitles(List<BibDataSet> resBib) throws Exception {
        StringBuilder accumulated = new StringBuilder();
        for (BibDataSet bib : resBib) {
            BiblioItem bit = bib.getResBib();

            if (bit.getTitle() != null) {
                accumulated.append(bit.getTitle()).append("\n");
            }
        }

        return accumulated.toString();
    }

    @Override
    public synchronized void close() throws IOException {
        parsers.close();
    }

    public static void setCntManager(CntManager cntManager) {
        Engine.cntManager = cntManager;
    }

    public static CntManager getCntManager() {
        return cntManager;
    }

    public EngineParsers getParsers() {
        return parsers;
    }
}