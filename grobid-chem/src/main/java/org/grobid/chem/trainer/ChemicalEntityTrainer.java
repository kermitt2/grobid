package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.mock.MockContext;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorChemicalEntity;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.trainer.sax.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Patrice Lopez
 */
public class ChemicalEntityTrainer extends AbstractTrainer {

    public ChemicalEntityTrainer() {
        super(GrobidModels.ENTITIES_CHEMISTRY);
    }

    public int createCRFPPData(File sourcePathLabel,
                               File outputPath, File dummy, double ratio) {
        return createCRFPPData(sourcePathLabel, outputPath);
    }

	/**
     * Add the selected features to the author model training for headers
     */
    public int createCRFPPData2(File sourcePathLabel,
                               File outputPath) {
        int totalExamples = 0;
        try {
			System.out.println("sourcePathLabel: " + sourcePathLabel);
            System.out.println("outputPath: " + outputPath);            

            // then we convert the tei files into the usual CRF label format
            // we process all tei files in the output directory
            File[] refFiles = sourcePathLabel.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml") && name.startsWith("WO");
                }
            });

            if (refFiles == null) {
                return 0;
            }

            System.out.println(refFiles.length + " xml files");

            // the file for writing the training data
            Writer writer = new OutputStreamWriter(new FileOutputStream(outputPath), "UTF8");

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

			ChemicalScrapbookSaxParser parser = null;            
			int n = 0;
            for (; n < refFiles.length; n++) {
				parser = new ChemicalScrapbookSaxParser();
                File theFile = refFiles[n];
                String name = theFile.getName();
                System.out.println(name);

	            SAXParser p = spf.newSAXParser();
	            p.parse(theFile, parser);
	            List<String> labeled = parser.getLabeledResult();
	            //System.out.println(labeled);
	
	            // we can now add the features
	            List<OffsetPosition> chemicalTokenPositions = null;
	            List<OffsetPosition> chemicalNamesTokenPositions = null;

	            addFeatures(labeled, writer, chemicalTokenPositions, chemicalNamesTokenPositions);
	            writer.write("\n");
			}
			
	        writer.close();
	    } 
		catch (Exception e) {
	        throw new GrobidException("An exception occured while running Grobid.", e);
	    }
	    return totalExamples;
	}




    /**
     * Add the selected features to the author model training for headers
     */
    public int createCRFPPData(File sourcePathLabel,
                               File outputPath
    ) {
        int totalExamples = 0;
        try {
            System.out.println("sourcePathLabel: " + sourcePathLabel);
            System.out.println("outputPath: " + outputPath);

            // then we convert the tei files into the usual CRF label format
            // we process all tei files in the output directory
            File[] refFiles = sourcePathLabel.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml") && name.startsWith("WO");
                }
            });

            if (refFiles == null) {
                return 0;
            }

            System.out.println(refFiles.length + " tei files");

            // the file for writing the training data
            Writer writer = new OutputStreamWriter(new FileOutputStream(outputPath), "UTF8");

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();
            String name;
            ArrayList<ArrayList<String>> chemicalAnnotations = null;
            ArrayList<ArrayList<String>> chemicalFormulas = null;
            ArrayList<ArrayList<String>> chemicalSubstances = null;
            ArrayList<ArrayList<String>> chemicalClassNames = null;
            ArrayList<ArrayList<String>> chemicalLigand = null;

            int n = 0;
            for (; n < refFiles.length; n++) {
                File thefile = refFiles[n];

                if (thefile.getName().endsWith(".words.xml")) {
                    // chemical names
                    name = thefile.getName().replace(".words.xml", "");
                    System.out.println(name);

                    File theOtherFile = new File(thefile.getPath().replace(".words.xml",
                            ".HC.chemical-names.xml"));
                    if (theOtherFile.exists()) {
                        // get the chemical names first
                        ChemicalNameSaxParser parser2 = new ChemicalNameSaxParser();

                        //get a new instance of parser
                        SAXParser p = spf.newSAXParser();
                        p.parse(thefile, parser2);

                        chemicalAnnotations = parser2.getChemicalAnnotations();
                        totalExamples += parser2.getNumberEntities();
                    }

                    theOtherFile = new File(thefile.getPath().replace(".words.xml",
                            ".HC.formula-names.xml"));
                    if (theOtherFile.exists()) {
                        ChemicalFormulasSaxParser parser3 = new ChemicalFormulasSaxParser();

                        //get a new instance of parser
                        SAXParser p2 = spf.newSAXParser();
                        p2.parse(theOtherFile, parser3);

                        chemicalFormulas = parser3.getChemicalFormulas();
                        totalExamples += parser3.getNumberEntities();
                    }

                    theOtherFile = new File(thefile.getPath().replace(".words.xml",
                            ".HC.substance-names.xml"));
                    if (theOtherFile.exists()) {
                        ChemicalSubstancesSaxParser parser4 = new ChemicalSubstancesSaxParser();

                        //get a new instance of parser
                        SAXParser p2 = spf.newSAXParser();
                        p2.parse(theOtherFile, parser4);

                        chemicalSubstances = parser4.getChemicalSubstances();
                        totalExamples += parser4.getNumberEntities();
                    }

                    theOtherFile = new File(thefile.getPath().replace(".words.xml",
                            ".HC.class-names.xml"));
                    if (theOtherFile.exists()) {
                        ChemicalClassNamesSaxParser parser5 = new ChemicalClassNamesSaxParser();

                        //get a new instance of parser
                        SAXParser p2 = spf.newSAXParser();
                        p2.parse(theOtherFile, parser5);

                        chemicalClassNames = parser5.getChemicalClassNames();
                        totalExamples += parser5.getNumberEntities();
                    }

                    theOtherFile = new File(thefile.getPath().replace(".words.xml",
                            ".HC.ligand.xml"));
                    if (theOtherFile.exists()) {
                        ChemicalLigandSaxParser parser6 = new ChemicalLigandSaxParser();

                        //get a new instance of parser
                        SAXParser p2 = spf.newSAXParser();
                        p2.parse(theOtherFile, parser6);

                        chemicalLigand = parser6.getChemicalLigand();
                        totalExamples += parser6.getNumberEntities();
                    }
                }

                List<String> chemicalAnnotationsList = new ArrayList<String>();
                List<String> chemicalAnnotationsStartsList = new ArrayList<String>();
                if (chemicalAnnotations != null) {
                    for (ArrayList<String> toto : chemicalAnnotations) {
                        String first = toto.get(0).trim();
                        String last = toto.get(1).trim();

                        // double check first and last order
                        if (first.length() > last.length()) {
                            last = toto.get(0);
                            first = toto.get(1);
                        } else if ((first.length() == last.length()) && (first.compareToIgnoreCase(last) > 0)) {
                            last = toto.get(0);
                            first = toto.get(1);
                        }
                        chemicalAnnotationsStartsList.add(first);
                        if (!last.equals(first)) {
                            String next = first;
                            while (!next.equals(last)) {
                                int ind = next.lastIndexOf("_");
                                String numb = next.substring(ind + 1, next.length());
                                try {
                                    Integer numbi = Integer.parseInt(numb);
                                    next = next.substring(0, ind + 1) + (numbi + 1);
                                } catch (NumberFormatException e) {
                                    throw new GrobidException("An exception occured while running Grobid.", e);
                                }
                                if (!next.equals(first)) {
                                    chemicalAnnotationsList.add(next);
                                }
                            }
                        }
                        chemicalAnnotationsList.add(last);
                    }
                }

                List<String> chemicalFormulasList = new ArrayList<String>();
                List<String> chemicalFormulasStartsList = new ArrayList<String>();
                if (chemicalFormulas != null) {
                    for (ArrayList<String> toto : chemicalFormulas) {
                        String first = toto.get(0).trim();
                        String last = toto.get(1).trim();

                        // double check first and last order
                        if (first.length() > last.length()) {
                            last = toto.get(0);
                            first = toto.get(1);
                        } else if ((first.length() == last.length()) && (first.compareToIgnoreCase(last) > 0)) {
                            last = toto.get(0);
                            first = toto.get(1);
                        }
                        chemicalFormulasStartsList.add(first);
                        if (!last.equals(first)) {
                            String next = first;
                            while (!next.equals(last)) {
                                int ind = next.lastIndexOf("_");
                                String numb = next.substring(ind + 1, next.length());
                                try {
                                    Integer numbi = Integer.parseInt(numb);
                                    next = next.substring(0, ind + 1) + (numbi + 1);
                                } catch (NumberFormatException e) {
                                    throw new GrobidException("An exception occured while running Grobid.", e);
                                }
                                if (!next.equals(first)) {
                                    chemicalFormulasList.add(next);
                                }
                            }
                        }
                        chemicalFormulasList.add(last);
                    }
                }

                List<String> chemicalSubstancesList = new ArrayList<String>();
                List<String> chemicalSubstancesStartsList = new ArrayList<String>();
                if (chemicalSubstances != null) {
                    for (ArrayList<String> toto : chemicalSubstances) {
                        String first = toto.get(0).trim();
                        String last = toto.get(1).trim();

                        // double check first and last order
                        if (first.length() > last.length()) {
                            last = toto.get(0);
                            first = toto.get(1);
                        } else if ((first.length() == last.length()) && (first.compareToIgnoreCase(last) > 0)) {
                            last = toto.get(0);
                            first = toto.get(1);
                        }
                        chemicalSubstancesStartsList.add(first);
                        if (!last.equals(first)) {
                            String next = first;
                            while (!next.equals(last)) {
                                int ind = next.lastIndexOf("_");
                                String numb = next.substring(ind + 1, next.length());
                                try {
                                    Integer numbi = Integer.parseInt(numb);
                                    next = next.substring(0, ind + 1) + (numbi + 1);
                                } catch (NumberFormatException e) {
                                    throw new GrobidException("An exception occured while running Grobid.", e);
                                }
                                if (!next.equals(first)) {
                                    chemicalSubstancesList.add(next);
                                }
                            }
                        }
                        chemicalSubstancesList.add(last);
                    }
                }

                List<String> chemicalClassNamesList = new ArrayList<String>();
                List<String> chemicalClassNamesStartsList = new ArrayList<String>();
                if (chemicalClassNames != null) {
                    for (ArrayList<String> toto : chemicalClassNames) {
                        String first = toto.get(0).trim();
                        String last = toto.get(1).trim();

                        // double check first and last order
                        if (first.length() > last.length()) {
                            last = toto.get(0);
                            first = toto.get(1);
                        } else if ((first.length() == last.length()) && (first.compareToIgnoreCase(last) > 0)) {
                            last = toto.get(0);
                            first = toto.get(1);
                        }
                        chemicalClassNamesStartsList.add(first);
                        if (!last.equals(first)) {
                            String next = first;
                            while (!next.equals(last)) {
                                int ind = next.lastIndexOf("_");
                                String numb = next.substring(ind + 1, next.length());
                                try {
                                    Integer numbi = Integer.parseInt(numb);
                                    next = next.substring(0, ind + 1) + (numbi + 1);
                                } catch (NumberFormatException e) {
                                    throw new GrobidException("An exception occured while running Grobid.", e);
                                }
                                if (!next.equals(first)) {
                                    chemicalClassNamesList.add(next);
                                }
                            }
                        }
                        chemicalClassNamesList.add(last);
                    }
                }

                List<String> chemicalLigandList = new ArrayList<String>();
                List<String> chemicalLigandStartsList = new ArrayList<String>();
                if (chemicalLigand != null) {
                    for (ArrayList<String> toto : chemicalLigand) {
                        String first = toto.get(0).trim();
                        String last = toto.get(1).trim();

                        // double check first and last order
                        if (first.length() > last.length()) {
                            last = toto.get(0);
                            first = toto.get(1);
                        } else if ((first.length() == last.length()) && (first.compareToIgnoreCase(last) > 0)) {
                            last = toto.get(0);
                            first = toto.get(1);
                        }
                        chemicalLigandStartsList.add(first);
                        if (!last.equals(first)) {
                            String next = first;
                            while (!next.equals(last)) {
                                int ind = next.lastIndexOf("_");
                                String numb = next.substring(ind + 1, next.length());
                                try {
                                    Integer numbi = Integer.parseInt(numb);
                                    next = next.substring(0, ind + 1) + (numbi + 1);
                                } catch (NumberFormatException e) {
                                    throw new GrobidException("An exception occured while running Grobid.", e);
                                }
                                if (!next.equals(first)) {
                                    chemicalLigandList.add(next);
                                }
                            }
                        }
                        chemicalLigandList.add(last);
                    }
                }

                // we need now to map the word id on the actual word flow
                ChemicalWordsSaxParser parser = new ChemicalWordsSaxParser();
                parser.setChemicalAnnotations(chemicalAnnotationsList, chemicalAnnotationsStartsList);
                parser.setChemicalFormulas(chemicalFormulasList, chemicalFormulasStartsList);
                parser.setChemicalSubstances(chemicalSubstancesList, chemicalSubstancesStartsList);
                parser.setChemicalClassNames(chemicalClassNamesList, chemicalClassNamesStartsList);
                parser.setChemicalLigand(chemicalLigandList, chemicalLigandStartsList);
                File thefileWords;
                try {
                    thefileWords = new File(thefile.getParent() + "/"
                            + thefile.getName().replace(".HC.chemical-names.xml", ".words.xml"));
                } catch (Exception e) {
                    throw new GrobidException("An exception occured while running Grobid.", e);
                }

                List<String> labeled;
                if (thefileWords != null) {
                    SAXParser p = spf.newSAXParser();
                    p.parse(thefileWords, parser);
                    labeled = parser.getLabeledResult();

                    //System.out.println(labeled);
                    // we can now add the features
                    List<OffsetPosition> chemicalTokenPositions = null;
                    List<OffsetPosition> chemicalNamesTokenPositions = null;

                    addFeatures(labeled, writer, chemicalTokenPositions, chemicalNamesTokenPositions);
                    writer.write("\n");
                }
            }

            writer.close();
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return totalExamples;
    }


    @SuppressWarnings({"UnusedParameters"})
    public void addFeatures(List<String> texts,
                            Writer writer,
                            List<OffsetPosition> chemicalTokenPositions,
                            List<OffsetPosition> chemicalNamesTokenPositions) {
        int totalLine = texts.size();
        int posit = 0;
        boolean isChemicalToken = false;
        boolean isChemicalNameToken = false;
        try {
            for (String line : texts) {
                FeaturesVectorChemicalEntity featuresVector =
                        FeaturesVectorChemicalEntity.addFeaturesChemicalEntities(line,
                                totalLine,
                                posit,
                                isChemicalToken,
                                isChemicalNameToken);
                if (featuresVector.label == null)
                    continue;
                writer.write(featuresVector.printVector());
                writer.write("\n");
                writer.flush();
                posit++;
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }


    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
		try {
			String pGrobidHome = "../grobid-home";
			String pGrobidProperties = "../grobid-home/config/grobid.properties";

			MockContext.setInitialContext(pGrobidHome, pGrobidProperties);
		    GrobidProperties.getInstance();
		
        	Trainer trainer = new ChemicalEntityTrainer();
	        AbstractTrainer.runTraining(trainer);
	        AbstractTrainer.runEvaluation(trainer);
		}
		catch (Exception e) {
	    	// If an exception is generated, print a stack trace
		    e.printStackTrace();
		}
		finally {
			try {
				MockContext.destroyInitialContext();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
    }
}