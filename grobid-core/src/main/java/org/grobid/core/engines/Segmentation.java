package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.document.BasicStructureBuilder;
import org.grobid.core.document.Document;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorSegmentation;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * Realise a high level segmentation of a document into cover page, document header, page footer,
 * page header, document body, bibliographical section, each bibliographical references in
 * the biblio section and finally the possible annexes.
 *
 * @author Patrice Lopez
 */
public class Segmentation extends AbstractParser {

	/*
        8 labels for this model:
	 		cover page <cover>, 
			document header <header>, 
			page footer <footnote>, 
			page header <headnote>, 
			document body <body>, 
			bibliographical section <references>, 
			page number <page>,
			annexes <annex>
	*/

    private static final Logger LOGGER = LoggerFactory.getLogger(Segmentation.class);

    private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();

	// default bins for relative position
    private static final int NBBINS = 12;
	
	// projection scale for line length
	private static final int LINESCALE = 10; 
		
    private File tmpPath = null;

    /**
     * TODO some documentation...
     */
    public Segmentation() {
        super(GrobidModels.SEGMENTATION);
        tmpPath = GrobidProperties.getTempPath();
    }

    /**
     *  Segment a PDF document into high level zones: cover page, document header, 
     *	page footer, page header, body, page numbers, biblio section and annexes.
     *
     * @param input filename of pdf file
     * @return Document object with segmentation informations
     */
	public Document processing(String input) {
		return processing(input, null);
	}
	
    /**
     *  Segment a PDF document into high level zones: cover page, document header, 
     *	page footer, page header, body, page numbers, biblio section and annexes.
     *
     * @param input filename of pdf file
     * @param assetPath if not null, the PDF assets (embedded images) will be extracted and 
     * saved under the indicated repository path
     * @return Document object with segmentation informations
     */
    public Document processing(String input, String assetPath) {
		return processing(input, assetPath, -1, -1);
	}

    /**
     *  Segment a PDF document into high level zones: cover page, document header, 
     *	page footer, page header, body, page numbers, biblio section and annexes.
     *
     * @param input filename of pdf file
     * @param assetPath if not null, the PDF assets (embedded images) will be extracted and 
     * saved under the indicated repository path
	 * @param startPage give the starting page to consider in case of segmentation of the 
	 * PDF, -1 for the first page (default) 
	 * @param endPage give the end page to consider in case of segmentation of the 
	 * PDF, -1 for the last page (default) 	
     * @return Document object with segmentation informations
     */
    public Document processing(String input, String assetPath, int startPage, int endPage) {
        if (input == null) {
            throw new GrobidResourceException("Cannot process pdf file, because input file was null.");
        }
        File inputFile = new File(input);
        if (!inputFile.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because input file '" +
                    inputFile.getAbsolutePath() + "' does not exists.");
        }
        if (tmpPath == null) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        }
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                    tmpPath.getAbsolutePath() + "' does not exists.");
        }
        Document doc = new Document(input, tmpPath.getAbsolutePath());
        String pathXML = null;
        try {
            //int startPage = -1;
            //int endPage = -1;
			boolean assets = false;
			if (assetPath!= null)
				assets = true;
            pathXML = doc.pdf2xml(true, false, startPage, endPage, input, tmpPath.getAbsolutePath(), assets);
            //with timeout,
            //no force pdf reloading
            // input is the pdf absolute path, tmpPath is the temp. directory for the temp. lxml file,
            // path is the resource path
            // and we extract images in the PDF file according to the assets parameters
            if (pathXML == null) {
                throw new GrobidResourceException("PDF parsing fails, " +
                        "because path of where to store xml file is null.");
            }
            doc.setPathXML(pathXML);
            List<String> tokenizations = doc.addTokenizedDocument();

            if (doc.getBlocks() == null) {
                throw new GrobidException("PDF parsing resulted in empty content");
            }

			String content = //getAllTextFeatured(doc, headerMode);
				getAllLinesFeatured(doc);
			if ( (content != null) && (content.trim().length() > 0) ) {
	            String labelledResult = label(content);
				/*try {
	            	FileUtils.writeStringToFile(new File("/tmp/x1.txt"), labelledResult);
					FileUtils.writeStringToFile(new File("/tmp/x2.txt"), tokenizations.toString());
				}
				catch(Exception e) {
					e.printStackTrace();
				}*/
	            // set the different sections of the Document object
	            doc = BasicStructureBuilder.generalResultSegmentation(doc, labelledResult, tokenizations);

				//System.out.println(doc.getBlockHeaders());
	            //System.out.println("------------------");
				//System.out.println(doc.getDocumentPieceText(doc.getDocumentPart(SegmentationLabel.HEADER)));
				//System.out.println("------------------");

				//System.out.println(doc.getDocumentPieceText(doc.getDocumentPart(SegmentationLabel.BODY)));
				//System.out.println("------------------");

	            //System.out.println(doc.getBlockReferences());
	            //System.out.println("------------------");
	            //System.out.println(doc.getDocumentPieceText(doc.getDocumentPart(SegmentationLabel.REFERENCES)));
				//System.out.println("------------------");
	            //LOGGER.debug(labelledResult);
				
	            //System.out.println(doc.getDocumentPieceText(doc.getDocumentPart(SegmentationLabel.FOOTNOTE)));
				//System.out.println("------------------");
				
				// if assets is true, the images are still there under directory pathXML+"_data"
				// we copy them to the assetPath directory
				if (assetPath != null) {
					// copy the files under the directory pathXML+"_data"
					//...
				}
			}
            return doc;
        } finally {
            // keep it clean when leaving...
			if (assetPath == null)
				doc.cleanLxmlFile(pathXML, false);
			else 
				doc.cleanLxmlFile(pathXML, true);
        }
    }
	
	/**
     *  Addition of the features at token level for the complete document
	 */	
	/*public String getAllTextFeatured(Document doc, boolean headerMode) {
		FeatureFactory featureFactory = FeatureFactory.getInstance();
        StringBuilder fulltext = new StringBuilder();
        String currentFont = null;
        int currentFontSize = -1;

		List<Block> blocks = doc.getBlocks();
		if ( (blocks == null) || blocks.size() == 0) {
			return null;
		}

        // vector for features
        FeaturesVectorSegmentation features;
        FeaturesVectorSegmentation previousFeatures = null;
        boolean endblock;
        boolean endPage = true;
        boolean newPage = true;
        boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        int documentLength = 0;
        int pageLength = 0; // length of the current page

		List<String> tokenizationsBody = new ArrayList<String>();
		List<String> tokenizations = doc.getTokenizations();

        // we calculate current document length and intialize the body tokenization structure
		for(Block block : blocks) {
			List<LayoutToken> tokens = block.getTokens();
			if (tokens == null) 
				continue;
			documentLength += tokens.size();
		}
		if (headerMode) {
			// if we only aim to process the header, we extracted only the two first pages, so
			// we estimate the global length to for instance 10 pages, so 5 times more.
			documentLength = documentLength * 5;
		}

		//int blockPos = dp1.getBlockPtr();
		for(int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
           	Block block = blocks.get(blockIndex);				

			// this is the list of punctuation profiles for each line of the current block
			List<String> punctuationProfiles = new ArrayList<String>();

			// this is the list of line length (scaled) for each line of the current block
			List<Integer> lineLengths = new ArrayList<Integer>();

          	// we estimate the length of the page where the current block is
			// and initialise the punctuation profiles per line
            if (start || endPage) {
                boolean stop = false;
                pageLength = 0;
                for (int z = blockIndex; (z < blocks.size()) && !stop; z++) {
                    String localText2 = blocks.get(z).getText();
                    if (localText2 != null) {
                        if (localText2.contains("@PAGE")) {
                            if (pageLength > 0) {
                                if (blocks.get(z).getTokens() != null) {
                                    pageLength += blocks.get(z).getTokens()
                                            .size();
                                }
                                stop = true;
                                break;
                            }
                        } else {
                            if (blocks.get(z).getTokens() != null) {
                                pageLength += blocks.get(z).getTokens().size();
                            }
                        }
                    }
                }
                // System.out.println("pageLength: " + pageLength);
            }
            if (start) {
                newPage = true;
                start = false;
            }
            boolean newline;
            boolean previousNewline = false;
            endblock = false;

            if (endPage) {
                newPage = true;
                mm = 0;
            }

            String localText = block.getText();
            if (localText != null) {
                if (localText.contains("@PAGE")) {
                    mm = 0;
                    // pageLength = 0;
                    endPage = true;
                    newPage = false;
                } else {
                    endPage = false;
                }
            }

			int indexPunctuationProfile = 0;
			String currentPunctuationProfile = "";
			StringTokenizer st = new StringTokenizer(localText, "\n");
			while(st.hasMoreTokens()) {
				String line = st.nextToken();
				punctuationProfiles.add(TextUtilities.punctuationProfile(line));
				lineLengths.add(new Integer(line.length() % LINESCALE));
			}
			boolean firstPageBlock = false;
			boolean lastPageBlock = false;
			
			if (newPage)
				firstPageBlock = true;
			if (endPage)
				lastPageBlock = true;
			
            List<LayoutToken> tokens = block.getTokens();
            if (tokens == null) {
                //blockPos++;
                continue;
            }

			int n = 0;// token position in current block
            while (n < tokens.size()) {
                LayoutToken token = tokens.get(n);
                features = new FeaturesVectorSegmentation();
                features.token = token;

                String text = token.getText();
                if (text == null) {
                    n++;
                    mm++;
                    nn++;
                    continue;
                }
                text = text.trim();
                if (text.length() == 0) {
                    n++;
                    mm++;
                    nn++;
                    continue;
                }

                if (text.equals("\n")) {
                    newline = true;
                    previousNewline = true;
                    n++;
                    mm++;
                    nn++;
					indexPunctuationProfile++;
                    continue;
                } else
                    newline = false;

                if (previousNewline) {
                    newline = true;
                    previousNewline = false;
                }

                if (Segmentation.filterLine(text)) {
                    n++;
                    mm++;
                    nn++;
                    continue;
                }
				
				int currentLineLength = 0;
				if (indexPunctuationProfile < punctuationProfiles.size())
					currentPunctuationProfile = punctuationProfiles.get(indexPunctuationProfile);
				if (indexPunctuationProfile < lineLengths.size())
					currentLineLength = lineLengths.get(indexPunctuationProfile);

                features.string = text;
				features.punctuationProfile = currentPunctuationProfile;
				features.firstPageBlock = firstPageBlock;
				features.lastPageBlock = lastPageBlock;
				features.lineLength = currentLineLength;

                if (newline)
                    features.lineStatus = "LINESTART";
                Matcher m0 = featureFactory.isPunct.matcher(text);
                if (m0.find()) {
                    features.punctType = "PUNCT";
                }
                if (text.equals("(") || text.equals("[")) {
                    features.punctType = "OPENBRACKET";

                } else if (text.equals(")") || text.equals("]")) {
                    features.punctType = "ENDBRACKET";

                } else if (text.equals(".")) {
                    features.punctType = "DOT";

                } else if (text.equals(",")) {
                    features.punctType = "COMMA";

                } else if (text.equals("-")) {
                    features.punctType = "HYPHEN";

                } else if (text.equals("\"") || text.equals("\'") || text.equals("`")) {
                    features.punctType = "QUOTE";

                }

                if (n == 0) {
                    features.lineStatus = "LINESTART";
                    features.blockStatus = "BLOCKSTART";
                } else if (n == tokens.size() - 1) {
                    features.lineStatus = "LINEEND";
                    previousNewline = true;
                    features.blockStatus = "BLOCKEND";
                    endblock = true;
                } else {
                    // look ahead...
                    boolean endline = false;

                    int ii = 1;
                    boolean endloop = false;
                    while ((n + ii < tokens.size()) && (!endloop)) {
                        LayoutToken tok = tokens.get(n + ii);
                        if (tok != null) {
                            String toto = tok.getText();
                            if (toto != null) {
                                if (toto.equals("\n")) {
                                    endline = true;
                                    endloop = true;
                                } else {
                                    if ((toto.length() != 0)
                                            && (!(toto.startsWith("@IMAGE")))
                                            && (!text.contains(".pbm"))
                                            && (!text.contains(".vec"))
                                            && (!text.contains(".jpg"))) {
                                        endloop = true;
                                    }
                                }
                            }
                        }

                        if (n + ii == tokens.size() - 1) {
                            endblock = true;
                            endline = true;
                        }

                        ii++;
                    }

                    if ((!endline) && !(newline)) {
                        features.lineStatus = "LINEIN";
                    } else if (!newline) {
                        features.lineStatus = "LINEEND";
                        previousNewline = true;
                    }

                    if ((!endblock) && (features.blockStatus == null))
                        features.blockStatus = "BLOCKIN";
                    else if (features.blockStatus == null) {
                        features.blockStatus = "BLOCKEND";
                        endblock = true;
                    }
                }

                if (newPage) {
                    features.pageStatus = "PAGESTART";
                    newPage = false;
                    endPage = false;
                    if (previousFeatures != null)
                        previousFeatures.pageStatus = "PAGEEND";
                } else {
                    features.pageStatus = "PAGEIN";
                    newPage = false;
                    endPage = false;
                }

                if (text.length() == 1) {
                    features.singleChar = true;
                }

                if (Character.isUpperCase(text.charAt(0))) {
                    features.capitalisation = "INITCAP";
                }

                if (featureFactory.test_all_capital(text)) {
                    features.capitalisation = "ALLCAP";
                }

                if (featureFactory.test_digit(text)) {
                    features.digit = "CONTAINSDIGITS";
                }

                if (featureFactory.test_common(text)) {
                    features.commonName = true;
                }

                if (featureFactory.test_names(text)) {
                    features.properName = true;
                }

                if (featureFactory.test_month(text)) {
                    features.month = true;
                }

                Matcher m = featureFactory.isDigit.matcher(text);
                if (m.find()) {
                    features.digit = "ALLDIGIT";
                }

                Matcher m2 = featureFactory.YEAR.matcher(text);
                if (m2.find()) {
                    features.year = true;
                }

                Matcher m3 = featureFactory.EMAIL.matcher(text);
                if (m3.find()) {
                    features.email = true;
                }

                Matcher m4 = featureFactory.HTTP.matcher(text);
                if (m4.find()) {
                    features.http = true;
                }

                if (currentFont == null) {
                    currentFont = token.getFont();
                    features.fontStatus = "NEWFONT";
                } else if (!currentFont.equals(token.getFont())) {
                    currentFont = token.getFont();
                    features.fontStatus = "NEWFONT";
                } else
                    features.fontStatus = "SAMEFONT";

                int newFontSize = (int) token.getFontSize();
                if (currentFontSize == -1) {
                    currentFontSize = newFontSize;
                    features.fontSize = "HIGHERFONT";
                } else if (currentFontSize == newFontSize) {
                    features.fontSize = "SAMEFONTSIZE";
                } else if (currentFontSize < newFontSize) {
                    features.fontSize = "HIGHERFONT";
                    currentFontSize = newFontSize;
                } else if (currentFontSize > newFontSize) {
                    features.fontSize = "LOWERFONT";
                    currentFontSize = newFontSize;
                }

                if (token.getBold())
                    features.bold = true;

                if (token.getItalic())
                    features.italic = true;

                // HERE horizontal information
                // CENTERED
                // LEFTAJUSTED
                // CENTERED

                if (features.capitalisation == null)
                    features.capitalisation = "NOCAPS";

                if (features.digit == null)
                    features.digit = "NODIGIT";

                if (features.punctType == null)
                    features.punctType = "NOPUNCT";

                features.relativeDocumentPosition = featureFactory
                        .relativeLocation(nn, documentLength, NBBINS);
                // System.out.println(mm + " / " + pageLength);
                features.relativePagePosition = featureFactory
                        .relativeLocation(mm, pageLength, NBBINS);

                // fulltext.append(features.printVector());
                if (previousFeatures != null) {
                    String vector = previousFeatures.printVector();
                    if (vector.split(" ").length < 5 || vector.contains("ZHUNFROOHJH")) {
                        int asf = 0;
                    }
                    fulltext.append(vector);
                }
                n++;
                mm++;
                nn++;
                previousFeatures = features;
           	}
        }
        if (previousFeatures != null)
            fulltext.append(previousFeatures.printVector());

        return fulltext.toString();
	}*/
	
	/**
	 * Addition of the features at line level for the complete document.  
	 *	
	 * This is an alternative to the token level, where the unit for labeling is the line - so allowing faster 
	 * processing and involving less features. 
	 * Lexical features becomes line prefix and suffix, the feature text unit is the first 10 characters of the 
	 * line without space.
	 * The dictionnary flags are at line level (i.e. the line contains a name mention, a place mention, a year, etc.)
	 * Regarding layout features: font, size and style are the one associated to the first token of the line. 
	 */
	public static String getAllLinesFeatured(Document doc) {
		FeatureFactory featureFactory = FeatureFactory.getInstance();
        StringBuilder fulltext = new StringBuilder();
        String currentFont = null;
        int currentFontSize = -1;

		List<Block> blocks = doc.getBlocks();
		if ( (blocks == null) || blocks.size() == 0) {
			return null;
		}

        // vector for features
        FeaturesVectorSegmentation features;
        FeaturesVectorSegmentation previousFeatures = null;
        boolean endblock;
        boolean endPage = true;
        boolean newPage = true;
        boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        int documentLength = 0;
        int pageLength = 0; // length of the current page

		List<String> tokenizationsBody = new ArrayList<String>();
		List<String> tokenizations = doc.getTokenizations();

        // we calculate current document length and intialize the body tokenization structure
		for(Block block : blocks) {
			List<LayoutToken> tokens = block.getTokens();
			if (tokens == null) 
				continue;
			documentLength += tokens.size();
		}

		//int blockPos = dp1.getBlockPtr();
		for(int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
           	Block block = blocks.get(blockIndex);				

          	// we estimate the length of the page where the current block is
			// and initialise the punctuation profiles per line
            if (start || endPage) {
                boolean stop = false;
                pageLength = 0;
                for (int z = blockIndex; (z < blocks.size()) && !stop; z++) {
                    String localText2 = blocks.get(z).getText();
                    if (localText2 != null) {
                        if (localText2.contains("@PAGE")) {
                            if (pageLength > 0) {
                                if (blocks.get(z).getTokens() != null) {
                                    pageLength += blocks.get(z).getTokens()
                                            .size();
                                }
                                stop = true;
                                break;
                            }
                        } else {
                            if (blocks.get(z).getTokens() != null) {
                                pageLength += blocks.get(z).getTokens().size();
                            }
                        }
                    }
                }
                // System.out.println("pageLength: " + pageLength);
            }
            if (start) {
                newPage = true;
                start = false;
            }
            endblock = false;

            if (endPage) {
                newPage = true;
                mm = 0;
            }

            String localText = block.getText();
            if (localText != null) {
                if (localText.contains("@PAGE")) {
                    mm = 0;
                    endPage = true;
                    newPage = false;
                } else {
                    endPage = false;
                }
            }

			String[] lines = localText.split("[\\n\\r]");
			List<LayoutToken> tokens = block.getTokens();			
			for(int li=0; li < lines.length; li++) {	
				String line = lines[li];
				boolean firstPageBlock = false;
				boolean lastPageBlock = false;
			
				if (newPage)
					firstPageBlock = true;
				if (endPage)
					lastPageBlock = true;
	            
	            if ( (tokens == null) || (tokens.size() == 0) ) {
	                continue;
	            }
				// for the layout information of the block, we take simply the first layout token
				LayoutToken token = tokens.get(0);
				
                features = new FeaturesVectorSegmentation();
                features.token = token;
				features.line = line;

				StringTokenizer st2 = new StringTokenizer(line, " \t");
				String text = null;
				String text2 = null;
				if (st2.hasMoreTokens())
					text = st2.nextToken();		
				if (st2.hasMoreTokens())
					text2 = st2.nextToken();
                if ( (text == null) || 
					(text.trim().length() == 0) || 
					(text.trim().equals("\n")) || 
					(text.trim().equals("\r")) || 
					(text.trim().equals("\n\r")) || 
					(Segmentation.filterLine(line)) ) {
                    continue;
                }

				text = text.trim();

                features.string = text;
				features.secondString = text2;
				
				features.firstPageBlock = firstPageBlock;
				features.lastPageBlock = lastPageBlock;
				features.lineLength = line.length() / LINESCALE;
				features.punctuationProfile = TextUtilities.punctuationProfile(line);
				
                features.lineStatus = null;
				features.punctType = null;
				
                if ( (li == 0) || 
					((previousFeatures != null) && previousFeatures.blockStatus.equals("BLOCKEND")) ) {
                    features.blockStatus = "BLOCKSTART";
                } else if (li == lines.length-1) {
                    features.blockStatus = "BLOCKEND";
                    endblock = true;
                }
				else if (features.blockStatus == null) {
                 	features.blockStatus = "BLOCKIN";
                }

                if (newPage) {
                    features.pageStatus = "PAGESTART";
                    newPage = false;
                    endPage = false;
                    if (previousFeatures != null)
                        previousFeatures.pageStatus = "PAGEEND";
                } else {
                    features.pageStatus = "PAGEIN";
                    newPage = false;
                    endPage = false;
                }

                if (text.length() == 1) {
                    features.singleChar = true;
                }

                if (Character.isUpperCase(text.charAt(0))) {
                    features.capitalisation = "INITCAP";
                }

                if (featureFactory.test_all_capital(text)) {
                    features.capitalisation = "ALLCAP";
                }

                if (featureFactory.test_digit(text)) {
                    features.digit = "CONTAINSDIGITS";
                }

                if (featureFactory.test_common(text)) {
                    features.commonName = true;
                }

                if (featureFactory.test_names(text)) {
                    features.properName = true;
                }

                if (featureFactory.test_month(text)) {
                    features.month = true;
                }

                Matcher m = featureFactory.isDigit.matcher(text);
                if (m.find()) {
                    features.digit = "ALLDIGIT";
                }

                Matcher m2 = featureFactory.YEAR.matcher(text);
                if (m2.find()) {
                    features.year = true;
                }

                Matcher m3 = featureFactory.EMAIL.matcher(text);
                if (m3.find()) {
                    features.email = true;
                }

                Matcher m4 = featureFactory.HTTP.matcher(text);
                if (m4.find()) {
                    features.http = true;
                }

                if (currentFont == null) {
                    currentFont = token.getFont();
                    features.fontStatus = "NEWFONT";
                } else if (!currentFont.equals(token.getFont())) {
                    currentFont = token.getFont();
                    features.fontStatus = "NEWFONT";
                } else
                    features.fontStatus = "SAMEFONT";

                int newFontSize = (int) token.getFontSize();
                if (currentFontSize == -1) {
                    currentFontSize = newFontSize;
                    features.fontSize = "HIGHERFONT";
                } else if (currentFontSize == newFontSize) {
                    features.fontSize = "SAMEFONTSIZE";
                } else if (currentFontSize < newFontSize) {
                    features.fontSize = "HIGHERFONT";
                    currentFontSize = newFontSize;
                } else if (currentFontSize > newFontSize) {
                    features.fontSize = "LOWERFONT";
                    currentFontSize = newFontSize;
                }

                if (token.getBold())
                    features.bold = true;

                if (token.getItalic())
                    features.italic = true;

                // HERE horizontal information
                // CENTERED
                // LEFTAJUSTED
                // CENTERED

                if (features.capitalisation == null)
                    features.capitalisation = "NOCAPS";

                if (features.digit == null)
                    features.digit = "NODIGIT";

                //if (features.punctType == null)
                //    features.punctType = "NOPUNCT";

                features.relativeDocumentPosition = featureFactory
                        .relativeLocation(nn, documentLength, NBBINS);
                features.relativePagePosition = featureFactory
                        .relativeLocation(mm, pageLength, NBBINS);

                // fulltext.append(features.printVector());
                if (previousFeatures != null) {
                    String vector = previousFeatures.printVector();
                    fulltext.append(vector);
                }
                previousFeatures = features;
           	}
			// update page-level and document-level positions
			if (tokens != null) {
				mm += tokens.size();
				nn += tokens.size();
			}
        }
        if (previousFeatures != null)
            fulltext.append(previousFeatures.printVector());

        return fulltext.toString();
	}
	

    /**
     * Process the content of the specified pdf and format the result as training data.
     *
     * @param inputFile    input file
     * @param pathFullText path to fulltext
     * @param pathTEI      path to TEI
     * @param id           id
     */
    public void createTrainingSegmentation(String inputFile,
                                           String pathFullText,
                                           String pathTEI,
                                           int id) {
        if (tmpPath == null)
            throw new GrobidResourceException("Cannot process pdf file, because temp path is null.");
        if (!tmpPath.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because temp path '" +
                    tmpPath.getAbsolutePath() + "' does not exists.");
        }
        Document doc = new Document(inputFile, tmpPath.getAbsolutePath());
        String pathXML = null;
        try {
            int startPage = -1;
            int endPage = -1;
            File file = new File(inputFile);
            if (!file.exists()) {
                throw new GrobidResourceException("Cannot train for fulltext, becuase file '" +
                        file.getAbsolutePath() + "' does not exists.");
            }
            String PDFFileName = file.getName();
            pathXML = doc.pdf2xml(true, false, startPage, endPage, inputFile, tmpPath.getAbsolutePath(), true);
            //with timeout,
            //no force pdf reloading
            // pathPDF is the pdf file, tmpPath is the tmp directory for the lxml file,
            // path is the resource path
            // and we don't extract images in the pdf file
            if (pathXML == null) {
                throw new Exception("PDF parsing fails");
            }
            doc.setPathXML(pathXML);
            doc.addTokenizedDocument();

            if (doc.getBlocks() == null) {
                throw new Exception("PDF parsing resulted in empty content");
            }

            String fulltext = //getAllTextFeatured(doc, false);
				getAllLinesFeatured(doc);
            List<String> tokenizations = doc.getTokenizationsFulltext();

            // we write the full text untagged
            String outPathFulltext = pathFullText + "/" + PDFFileName.replace(".pdf", ".training.segmentation");
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");
            writer.write(fulltext + "\n");
            writer.close();
			
			if ( (fulltext != null) && (fulltext.length() > 0) ) {
	            String rese = label(fulltext);
	            StringBuffer bufferFulltext = trainingExtraction(rese, tokenizations, doc);

	            // write the TEI file to reflect the extact layout of the text as extracted from the pdf
	            writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
	                    "/" + PDFFileName.replace(".pdf", ".training.segmentation.tei.xml")), false), "UTF-8");
	            writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
	                    "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">\n");

	            writer.write(bufferFulltext.toString());
	            writer.write("\n\t</text>\n</tei>\n");
	            writer.close();
			}

        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid training" +
                    " data generation for segmentation model.", e);
        } finally {
            doc.cleanLxmlFile(pathXML, true);
        }
    }

    /**
     * Extract results from a labelled full text in the training format without any string modification.
     *
     * @param result        reult
     * @param tokenizations toks
     * @return extraction
     */
    private StringBuffer trainingExtraction(String result,
                                            List<String> tokenizations,
											Document doc) {
        // this is the main buffer for the whole full text
        StringBuffer buffer = new StringBuffer();		
        try {
			List<Block> blocks = doc.getBlocks();
			int currentBlockIndex = 0;
			int indexLine = 0;
			
            StringTokenizer st = new StringTokenizer(result, "\n");
            String s1 = null; // current label/tag
            String s2 = null; // current lexical token
			String s3 = null; // current second lexical token
            String lastTag = null;

            // current token position
            int p = 0;
            boolean start = true;

            while (st.hasMoreTokens()) {
                boolean addSpace = false;
                String tok = st.nextToken().trim();
				String line = null; // current line
				
                if (tok.length() == 0) {
                    continue;
                }
                StringTokenizer stt = new StringTokenizer(tok, " \t");
                List<String> localFeatures = new ArrayList<String>();
                int i = 0;

                boolean newLine = true;
                int ll = stt.countTokens();
                while (stt.hasMoreTokens()) {
                    String s = stt.nextToken().trim();
                    if (i == 0) {
                        s2 = TextUtilities.HTMLEncode(s); // lexical token
                    } else if (i == 1) {
                        s3 = TextUtilities.HTMLEncode(s); // second lexical token
                    } else if (i == ll - 1) {
                        s1 = s; // current label
                    } else {
                        localFeatures.add(s); // we keep the feature values in case they appear useful
                    }
                    i++;
                }
				
				// as we process the document segmentation line by line, we don't use the usual 
				// tokenization to rebuild the text flow, but we get each line again from the 
				// text stored in the document blocks (similarly as when generating the features) 
				line = null;
				while( (line == null) && (currentBlockIndex < blocks.size()) ) {
					Block block = blocks.get(currentBlockIndex);
		            List<LayoutToken> tokens = block.getTokens();
		            if (tokens == null) {
						currentBlockIndex++;
						indexLine = 0;
		                continue;
		            }
					String localText = block.getText();
					if ( (localText == null) || (localText.trim().length() == 0) ) {
						currentBlockIndex++;
						indexLine = 0;
						continue;
					}
					//String[] lines = localText.split("\n");
					String[] lines = localText.split("[\\n\\r]");
					if ( (lines.length == 0) || (indexLine >= lines.length)) {
						currentBlockIndex++;
						indexLine = 0;
						continue;
					}
					else {
						line = lines[indexLine];
						indexLine++;
						if (line.trim().length() == 0) {
							line = null;
							continue;
						}
					
						if (Segmentation.filterLine(line)) {
							line = null;
							continue;
						}
					}
				}					
				
				line = TextUtilities.HTMLEncode(line);
								
                if (newLine && !start) {
                    buffer.append("<lb/>");
                }

                String lastTag0 = null;
                if (lastTag != null) {
                    if (lastTag.startsWith("I-")) {
                        lastTag0 = lastTag.substring(2, lastTag.length());
                    } else {
                        lastTag0 = lastTag;
                    }
                }
                String currentTag0 = null;
                if (s1 != null) {
                    if (s1.startsWith("I-")) {
                        currentTag0 = s1.substring(2, s1.length());
                    } else {
                        currentTag0 = s1;
                    }
                }

                //boolean closeParagraph = false;
                if (lastTag != null) {
                    //closeParagraph = 
                    testClosingTag(buffer, currentTag0, lastTag0, s1);
                }

                boolean output;

                output = writeField(buffer, line, s1, lastTag0, s2, "<header>", "<front>", addSpace, 3);
                /*if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<other>", "<note type=\"other\">", addSpace, 3);
                }*/
                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<headnote>", "<note place=\"headnote\">",
                            addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<footnote>", "<note place=\"footnote\">",
                            addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<page>", "<page>", addSpace, 3);
                }
                if (!output) {
                    //output = writeFieldBeginEnd(buffer, s1, lastTag0, s2, "<reference>", "<listBibl>", addSpace, 3);
                    output = writeField(buffer, line, s1, lastTag0, s2, "<references>", "<listBibl>", addSpace, 3);
                }
                if (!output) {
                    //output = writeFieldBeginEnd(buffer, s1, lastTag0, s2, "<body>", "<body>", addSpace, 3);
                    output = writeField(buffer, line, s1, lastTag0, s2, "<body>", "<body>", addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<cover>", "<titlePage>", addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<annex>", "<div type=\"annex\">", addSpace, 3);
                }
                /*if (!output) {
                    if (closeParagraph) {
                        output = writeField(buffer, s1, "", s2, "<reference_marker>", "<label>", addSpace, 3);
                    } else
                        output = writeField(buffer, s1, lastTag0, s2, "<reference_marker>", "<label>", addSpace, 3);
                }*/
                /*if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<citation_marker>", "<ref type=\"biblio\">",
                            addSpace, 3);
                }*/
                /*if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<figure_marker>", "<ref type=\"figure\">",
                            addSpace, 3);
                }*/
                lastTag = s1;

                if (!st.hasMoreTokens()) {
                    if (lastTag != null) {
                        testClosingTag(buffer, "", currentTag0, s1);
                    }
                }
                if (start) {
                    start = false;
                }
            }

            return buffer;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    /**
     * TODO some documentation...
     *
     * @param buffer
     * @param s1
     * @param lastTag0
     * @param s2
     * @param field
     * @param outField
     * @param addSpace
     * @param nbIndent
     * @return
     */
    private boolean writeField(StringBuffer buffer,
							   String line,
                               String s1,
                               String lastTag0,
                               String s2,
                               String field,
                               String outField,
                               boolean addSpace,
                               int nbIndent) {
        boolean result = false;
        // filter the output path
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
           	line= line.replace("@BULLET", "\u2022");
            // if previous and current tag are the same, we output the token
            if (s1.equals(lastTag0) || s1.equals("I-" + lastTag0)) {
   				buffer.append(line);
            }
            /*else if (lastTag0 == null) {
                   for(int i=0; i<nbIndent; i++) {
                       buffer.append("\t");
                   }
                     buffer.append(outField+s2);
               }*/
            /*else if (field.equals("<citation_marker>")) {
                if (addSpace)
                    buffer.append(" " + outField + s2);
                else
                    buffer.append(outField + s2);
            } else if (field.equals("<figure_marker>")) {
                if (addSpace)
                    buffer.append(" " + outField + s2);
                else
                    buffer.append(outField + s2);
            } else if (field.equals("<reference_marker>")) {
                if (!lastTag0.equals("<references>") && !lastTag0.equals("<reference_marker>")) {
                    for (int i = 0; i < nbIndent; i++) {
                        buffer.append("\t");
                    }
                    buffer.append("<bibl>");
                }
                if (addSpace)
                    buffer.append(" " + outField + s2);
                else
                    buffer.append(outField + s2);
            } */
            else if (lastTag0 == null) {
                // if previous tagname is null, we output the opening xml tag
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField).append(line);
            } else if (!lastTag0.equals("<titlePage>")) {
                // if the previous tagname is not titlePage, we output the opening xml tag
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField).append(line);
            } else {
                // otherwise we continue by ouputting the token
                buffer.append(line);
            }
        }
        return result;
    }

    /**
     * This is for writing fields for fields where begin and end of field matter, like paragraph or item
     *
     * @param buffer
     * @param s1
     * @param lastTag0
     * @param s2
     * @param field
     * @param outField
     * @param addSpace
     * @param nbIndent
     * @return
     */
    /*private boolean writeFieldBeginEnd(StringBuffer buffer,
                                       String s1,
                                       String lastTag0,
                                       String s2,
                                       String field,
                                       String outField,
                                       boolean addSpace,
                                       int nbIndent) {
        boolean result = false;
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
            if (lastTag0.equals("I-" + field)) {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            } /*else if (lastTag0.equals(field) && s1.equals(field)) {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            } else if (!lastTag0.equals("<citation_marker>") && !lastTag0.equals("<figure_marker>")
                    && !lastTag0.equals("<figure>") && !lastTag0.equals("<reference_marker>")) {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField + s2);
            } 
			else {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            }
        }
        return result;
    }*/

    /**
     * TODO some documentation
     *
     * @param buffer
     * @param currentTag0
     * @param lastTag0
     * @param currentTag
     * @return
     */
    private boolean testClosingTag(StringBuffer buffer,
                                   String currentTag0,
                                   String lastTag0,
                                   String currentTag) {
        boolean res = false;
        // reference_marker and citation_marker are two exceptions because they can be embedded

        if (!currentTag0.equals(lastTag0)) {
            /*if (currentTag0.equals("<citation_marker>") || currentTag0.equals("<figure_marker>")) {
                return res;
            }*/

            res = false;
            // we close the current tag
            if (lastTag0.equals("<header>")) {
                buffer.append("</front>\n\n");
            } else if (lastTag0.equals("<body>")) {
                buffer.append("</body>\n\n");
            } else if (lastTag0.equals("<headnote>")) {
                buffer.append("</note>\n\n");
            } else if (lastTag0.equals("<footnote>")) {
                buffer.append("</note>\n\n");
            } else if (lastTag0.equals("<references>")) {
                buffer.append("</listBibl>\n\n");
                res = true;
            } else if (lastTag0.equals("<page>")) {
                buffer.append("</page>\n\n");
            } else if (lastTag0.equals("<cover>")) {
                buffer.append("</titlePage>\n\n");
            } else if (lastTag0.equals("<annex>")) {
                buffer.append("</div>\n\n");
            } else {
                res = false;
            }

        }
        return res;
    }

    @Override
    public void close() throws IOException {
        super.close();
        // ...
    }
	
	public static boolean filterLine(String line) {
        boolean filter = false;
        if (line.contains("@IMAGE")) {
            filter = true;
        } else if (line.contains(".pbm")) {
            filter = true;
        } else if (line.contains(".vec")) {
            filter = true;
        } else if (line.contains(".jpg")) {
            filter = true;
        }
		return filter;
	}
	
}