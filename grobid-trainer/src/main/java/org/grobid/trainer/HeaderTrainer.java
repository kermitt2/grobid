package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.trainer.sax.TEIHeaderSaxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
//import java.util.TreeMap;


/**
 * @author Patrice Lopez
 */
public class HeaderTrainer extends AbstractTrainer{

    public HeaderTrainer() {
        super(GrobidModels.HEADER);
    }


	@Override
    public int createCRFPPData(File corpusPath, File trainingOutputPath) {
        return addFeaturesHeaders(corpusPath.getAbsolutePath() + "/tei", 
						  		corpusPath.getAbsolutePath() + "/headers", 
								trainingOutputPath, null, 1.0);
    }

	/**
	 * Add the selected features to a header example set 
	 * 
	 * @param corpusDir
	 *            a path where corpus files are located
	 * @param trainingOutputPath
	 *            path where to store the temporary training data
	 * @param evalOutputPath
	 *            path where to store the temporary evaluation data
	 * @param splitRatio
	 *            ratio to consider for separating training and evaluation data, e.g. 0.8 for 80% 
	 * @return the total number of used corpus items 
	 */
	@Override
	public int createCRFPPData(final File corpusDir, 
							final File trainingOutputPath, 
							final File evalOutputPath, 
							double splitRatio) {
		return addFeaturesHeaders(corpusDir.getAbsolutePath() + "/tei", 
								corpusDir.getAbsolutePath() + "/headers", 
								trainingOutputPath, 
								evalOutputPath, 
								splitRatio);	
	}

    /**
     * Add the selected features to the header model training
     * @param sourceFile source path
     * @param headerPath header path
     * @param outputPath output training file
     * @return number of corpus files
     */
    public int addFeaturesHeaders(String sourceFile,
                              	String headerPath,
                               	final File trainingOutputPath,
					  			final File evalOutputPath, 
								double splitRatio) {
        System.out.println(sourceFile);
        System.out.println(headerPath); 
        System.out.println(trainingOutputPath);
		System.out.println(evalOutputPath);
		
		System.out.println("TEI files: " + sourceFile);
		System.out.println("header info files: " + headerPath);
		if (trainingOutputPath != null)
			System.out.println("outputPath for training data: " + trainingOutputPath);
		if (evalOutputPath != null)
			System.out.println("outputPath for evaluation data: " + evalOutputPath);
		
        int nbExamples = 0;
        try {
            File pathh = new File(sourceFile);
            // we process all tei files in the output directory
            File[] refFiles = pathh.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tei") | name.endsWith(".tei.xml");
                }
            });

            if (refFiles == null)
                return 0;

//            TreeMap<String, String> pdfs = new TreeMap<String, String>();
            nbExamples = refFiles.length;
            System.out.println(nbExamples + " tei files");
 
			// the file for writing the training data
			OutputStream os2 = null;
			Writer writer2 = null;
			if (trainingOutputPath != null) {
				os2 = new FileOutputStream(trainingOutputPath);
				writer2 = new OutputStreamWriter(os2, "UTF8");
			}
		
			// the file for writing the evaluation data
			OutputStream os3 = null;
			Writer writer3 = null;
			if (evalOutputPath != null) {
				os3 = new FileOutputStream(evalOutputPath);
				writer3 = new OutputStreamWriter(os3, "UTF8");
			}

            for (File teifile : refFiles) {
                String name = teifile.getName();
                //System.out.println(name);

                TEIHeaderSaxParser parser2 = new TEIHeaderSaxParser();
                parser2.setFileName(name);
                //parser2.pdfs = pdfs;
                // get a factory
                SAXParserFactory spf = SAXParserFactory.newInstance();
                //get a new instance of parser
                SAXParser par = spf.newSAXParser();
                par.parse(teifile, parser2);

                ArrayList<String> labeled = parser2.getLabeledResult();

                //System.out.println(labeled);
                //System.out.println(parser2.getPDFName()+"._");

                File refDir2 = new File(headerPath);
                String headerFile = null;
                File[] refFiles2 = refDir2.listFiles();
                for (File aRefFiles2 : refFiles2) {
                    String localFileName = aRefFiles2.getName();
                    if (localFileName.equals(parser2.getPDFName() + ".header")) {
                        headerFile = localFileName;
                        break;
                    }

                    if ((localFileName.startsWith(parser2.getPDFName() + "._")) &
                            (localFileName.endsWith(".header"))) {
                        headerFile = localFileName;
                        break;
                    }
                }

                if (headerFile == null)
                    continue;
                //System.out.println(headerFile);
                String pathHeader = headerPath + File.separator + headerFile;
                int p = 0;
                BufferedReader bis = new BufferedReader(
                        new InputStreamReader(new FileInputStream(pathHeader), "UTF8"));

                StringBuilder header = new StringBuilder();

                String line;
//                String lastTag = null;
                while ((line = bis.readLine()) != null) {
                    header.append(line);
                    int ii = line.indexOf(' ');
                    String token = null;
                    if (ii != -1)
                        token = line.substring(0, ii);
//                    boolean found = false;
                    // we get the label in the labelled data file for the same token
                    for (int pp = p; pp < labeled.size(); pp++) {
                        String localLine = labeled.get(pp);
                        StringTokenizer st = new StringTokenizer(localLine, " ");
                        if (st.hasMoreTokens()) {
                            String localToken = st.nextToken();

                            if (localToken.equals(token)) {
                                String tag = st.nextToken();
                                header.append(" ").append(tag);
//                                lastTag = tag;
//                                found = true;
                                p = pp + 1;
                                pp = p + 10;
                            }
                        }
                        if (pp - p > 5) {
                            break;
                        }
                    }
                    /*if (!found) {
                             if (lastTag != null)
                                 header.append(lastTag);
                         }*/
                    header.append("\n");
                }
                bis.close();

                // post process for ensuring continous labelling
                StringBuilder header2 = new StringBuilder();
                String headerStr = header.toString();
                StringTokenizer sto = new StringTokenizer(headerStr, "\n");
                String lastLabel = null;
                String lastLastLabel = null;
                String previousLine = null;
//                String previousPreviousLine = null;

                while (sto.hasMoreTokens()) {
                    String linee = sto.nextToken();
                    StringTokenizer sto2 = new StringTokenizer(linee, " ");
                    String label = null;
                    while (sto2.hasMoreTokens()) {
                        label = sto2.nextToken();
                    }
                    if (label != null) {
                        if (label.length() > 0) {
                            if (!((label.charAt(0) == '<') | (label.startsWith("I-<")))) {
                                label = null;
                            }
                        }
                    }

                    if (previousLine != null) {
                        if ((label != null) & (lastLabel == null) & (lastLastLabel != null)) {
                            if (label.equals(lastLastLabel)) {
                                lastLabel = label;
                                previousLine += " " + label;
                                header2.append(previousLine);
                                header2.append("\n");
                            } else {
                                //if (lastLabel == null)
                                //	previousLine += " <note>";
                                if (lastLabel != null) {
                                    header2.append(previousLine);
                                    header2.append("\n");
                                }
                            }
                        } else {
                            //if (lastLabel == null)
                            //	previousLine += " <note>";
                            if (lastLabel != null) {
                                header2.append(previousLine);
                                header2.append("\n");
                            }
                        }
                    }

//                    previousPreviousLine = previousLine;
                    previousLine = linee;

                    lastLastLabel = lastLabel;
                    lastLabel = label;
                }
                //if (lastLabel == null)
                //	previousLine += " <note>";
                if (lastLabel != null) {
                    header2.append(previousLine);
                    header2.append("\n");
                }
 
				if ( (writer2 == null) && (writer3 != null) )
					writer3.write(header2.toString() + "\n");
				if ( (writer2 != null) && (writer3 == null) )
					writer2.write(header2.toString() + "\n");
				else {		
					if (Math.random() <= splitRatio)
						writer2.write(header2.toString() + "\n");
					else 
						writer3.write(header2.toString() + "\n");
				}
            }

            if (writer2 != null) {
				writer2.close();
				os2.close();
			}
			
			if (writer3 != null) {
				writer3.close();
				os3.close();
			}
			
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return nbExamples;
    }

//    public int createHeaderData(String sourcePDFFile) {
//        getTrainingDataPath();
//        int nb = 0;
//
//        try {
//            File pathh = new File(sourcePDFFile);
//            // we process all pdf files in the input directory
//            File[] refFiles = pathh.listFiles(new FilenameFilter() {
//                public boolean accept(File dir, String name) {
//                    if (name.endsWith(".pdf"))
//                        return true;
//                    else
//                        return false;
//                }
//            });
//
//            if (refFiles == null)
//                return 0;
//
//            int n = 0;
//            for (; n < refFiles.length; n++) {
//                File pdfFile = refFiles[n];
//                String PDFFileName = pdfFile.getName();
//                Document doc = new Document(PDFFileName, tmpPath);
//                try {
//                    int startPage = -1;
//                    int endPage = -1;
//                    String pathXML = doc.pdf2xml(false, true, startPage, endPage,
//                            pdfFile.getPath(), tmpPath, false);
//                    //no timeout,
//                    //force pdf reloading
//                    // pdfFile is the pdf file, tmpPath is the tmp directory for the lxml file,
//                    // path is the resource path
//                    // we do not extract images in the pdf file
//                    if (pathXML == null) {
//                        throw new Exception("PDF parsing fails: " + pdfFile.getPath());
//                    }
//                    doc.setPathXML(pathXML);
//                    ArrayList<String> tokenizations = doc.addFeaturesDocument();
//
//                    if (doc.getBlocks() == null) {
//                        throw new Exception("PDF parsing resulted in empty content");
//                    }
//
//                    doc.firstPass();
//
//                    String header = doc.getHeader(false);
//                    if (header == null) {
//                        header = doc.getHeaderLastHope();
//                    }
//                    header = doc.getHeaderFeatured(false, false, true);
//
//                    // we write the header untagged
//                    String outPathHeader = path + "/corpus/headers/" + PDFFileName.replace(".pdf", ".header");
//                    Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathHeader), false), "UTF-8");
//                    writer.write(header + "\n");
//                    writer.close();
//
//                    doc.cleanLxmlFile(pathXML, true);
//                } catch (Exception e) {
////					e.printStackTrace();
//                    throw new GrobidException("An exception occured while running Grobid.", e);
//                }
//            }
//        } catch (Exception e) {
////			e.printStackTrace();
//            throw new GrobidException("An exception occured while running Grobid.", e);
//        }
//        return nb;
//    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    	MockContext.setInitialContext();
    	GrobidProperties.getInstance();
        AbstractTrainer.runTraining(new HeaderTrainer());
        AbstractTrainer.runEvaluation(new HeaderTrainer());
        MockContext.destroyInitialContext();
    }
}