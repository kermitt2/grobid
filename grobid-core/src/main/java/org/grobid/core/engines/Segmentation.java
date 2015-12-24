package org.grobid.core.engines;

import eugfc.imageio.plugins.PNMRegistry;
import org.apache.commons.io.FileUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.document.BasicStructureBuilder;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorSegmentation;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

// for image conversion we're using an ImageIO plugin for PPM format support
// see https://github.com/eug/imageio-pnm
// the jar for this plugin is located in the local repository

/**
 * Realise a high level segmentation of a document into cover page, document header, page footer,
 * page header, document body, bibliographical section, each bibliographical references in
 * the biblio section and finally the possible annexes.
 *
 * @author Patrice Lopez
 */
public class Segmentation extends AbstractParser {

	/*
        10 labels for this model:
	 		cover page <cover>, 
			document header <header>, 
			page footer <footnote>, 
			page header <headnote>, 
			document body <body>, 
			bibliographical section <references>, 
			page number <page>,
			annexes <annex>,
		    acknowledgement <acknowledgement>,
		    toc <toc> -> not yet used because not yet training data for this
	*/

    private static final Logger LOGGER = LoggerFactory.getLogger(Segmentation.class);

    private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();

    // default bins for relative position
    private static final int NBBINS = 12;

    // projection scale for line length
    private static final int LINESCALE = 10;


    /**
     * TODO some documentation...
     */
    public Segmentation() {
        super(GrobidModels.SEGMENTATION);
    }

    /**
     * Segment a PDF document into high level zones: cover page, document header,
     * page footer, page header, body, page numbers, biblio section and annexes.
     *
     * @param input filename of pdf file
     * @return Document object with segmentation informations
     */
//    public Document processing(File input) {
//        return processing(input, null);
//    }

    /**
     * Segment a PDF document into high level zones: cover page, document header,
     * page footer, page header, body, page numbers, biblio section and annexes.
     *
     * @param input     filename of pdf file
     * @param assetPath if not null, the PDF assets (embedded images) will be extracted and
     *                  saved under the indicated repository path
     * @return Document object with segmentation informations
     */
//    public Document processing(String input, String assetPath) {
//        return processing(input, assetPath, -1, -1);
//    }

    /**
     * Segment a PDF document into high level zones: cover page, document header,
     * page footer, page header, body, page numbers, biblio section and annexes.
     *
     * @param input     filename of pdf file
     * @return Document object with segmentation informations
     */
    public Document processing(File input, GrobidAnalysisConfig config) {
        if (input == null) {
            throw new GrobidResourceException("Cannot process pdf file, because input file was null.");
        }
        if (!input.exists()) {
            throw new GrobidResourceException("Cannot process pdf file, because input file '" +
                    input.getAbsolutePath() + "' does not exist.");
        }

        DocumentSource documentSource = null;
        try {
            boolean assets = false;
            if (config.getPdfAssetPath() != null) {
                assets = true;
            }

            documentSource = DocumentSource.fromPdf(input, config.getStartPage(), config.getEndPage(), assets);
            Document doc = new Document(documentSource);

            List<LayoutToken> tokenizations = doc.addTokenizedDocument();

            if (doc.getBlocks() == null) {
                throw new GrobidException("PDF parsing resulted in empty content");
            }

//for(Block block : doc.getBlocks()) {
//    System.out.println(block.getText() + block.getPage() 
//        + " [ x:" + block.x + ", y:" + block.y + ", width:" + block.width + ", height:" + block.height + "]\n\n");
//}

            String content = //getAllTextFeatured(doc, headerMode);
                    getAllLinesFeatured(doc);
            if ((content != null) && (content.trim().length() > 0)) {
                String labelledResult = label(content);
                // set the different sections of the Document object
                doc = BasicStructureBuilder.generalResultSegmentation(doc, labelledResult, tokenizations);


                // if assets is true, the images are still there under directory pathXML+"_data"
                // we copy them to the assetPath directory
                File assetFile = config.getPdfAssetPath();
                if (assetFile != null) {
                    // copy the files under the directory pathXML+"_data"
                    // we copy the asset files into the path specified by assetPath

                    if (!assetFile.exists()) {
                        // we create it
                        if (assetFile.mkdir()) {
                            LOGGER.debug("Directory created: " + assetFile.getPath());
                        } else {
                            LOGGER.error("Failed to create directory: " + assetFile.getPath());
                        }
                    }
                    PNMRegistry.registerAllServicesProviders();

                    // filter all .jpg and .png files
                    File directoryPath = new File(documentSource.getXmlFile().getAbsolutePath() + "_data");
                    if (directoryPath.exists()) {
                        File[] files = directoryPath.listFiles();
                        if (files != null) {
                            for (final File currFile : files) {
                                if (currFile.getName().toLowerCase().endsWith(".png")) {
                                    try {
                                        FileUtils.copyFileToDirectory(currFile, assetFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else if (currFile.getName().toLowerCase().endsWith(".jpg")
                                        || currFile.getName().toLowerCase().endsWith(".ppm")
                                    //	|| currFile.getName().toLowerCase().endsWith(".pbm")
                                        ) {
                                    try {
                                        final BufferedImage bi = ImageIO.read(currFile);
                                        String outputfilePath = null;
                                        if (currFile.getName().toLowerCase().endsWith(".jpg")) {
                                            outputfilePath = assetFile.getPath() + File.separator +
                                                    currFile.getName().toLowerCase().replace(".jpg", ".png");
                                        }
										/*else if (currFile.getName().toLowerCase().endsWith(".pbm")) {
											outputfilePath = assetFile.getPath() + File.separator +
												 currFile.getName().toLowerCase().replace(".pbm",".png");
										}*/
                                        else {
                                            outputfilePath = assetFile.getPath() + File.separator +
                                                    currFile.getName().toLowerCase().replace(".ppm", ".png");
                                        }
                                        ImageIO.write(bi, "png", new File(outputfilePath));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
					// update the path of the image description stored in Document
					List<GraphicObject> images = doc.getImages();
					if (images != null) {
						String subPath = assetFile.getPath();
						int ind = subPath.lastIndexOf("/");
						if (ind != -1)
							subPath = subPath.substring(ind+1, subPath.length());
						for(GraphicObject image : images) {
							String fileImage = image.getFilePath();
							fileImage = fileImage.replace(".ppm", ".png")
											.replace(".jpg", ".png");
							ind = fileImage.indexOf("/");
							image.setFilePath(subPath + fileImage.substring(ind, fileImage.length()));
						}
					}
                }
            }
            return doc;
        } finally {
            // keep it clean when leaving...
            if (config.getPdfAssetPath() == null) {
                // remove the pdf2xml tmp file
                //DocumentSource.close(documentSource, false);
                DocumentSource.close(documentSource, true);
            } else {
                // remove the pdf2xml tmp files, including the sub-directories
                DocumentSource.close(documentSource, true);
            }
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
     * <p/>
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
        if ((blocks == null) || blocks.size() == 0) {
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

        List<LayoutToken> tokenizationsBody = new ArrayList<LayoutToken>();
        List<LayoutToken> tokenizations = doc.getTokenizations();

        // we calculate current document length and intialize the body tokenization structure
        for (Block block : blocks) {
            List<LayoutToken> tokens = block.getTokens();
            if (tokens == null)
                continue;
            documentLength += tokens.size();
        }

		double pageHeight = 0.0;
        boolean graphicVector = false;
        boolean graphicBitmap = false;
        for (int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
            Block block = blocks.get(blockIndex);

            // we estimate the length of the page where the current block is
            // and initialise the punctuation profiles per line	
            if (start || endPage) {
                boolean stop = false;
                pageLength = 0;
				double pageMaxY = 0.0;
				double pageMinY = 1000000.0;
                for (int z = blockIndex; (z < blocks.size()) && !stop; z++) {
                    String localText2 = blocks.get(z).getText();
                    if (localText2 != null) {
                        if (localText2.contains("@PAGE")) {
                            if (pageLength > 0) {
                                if (blocks.get(z).getTokens() != null) {
                                    pageLength += blocks.get(z).getTokens().size();
									if ((blocks.get(z).getY() != 0.0) && (blocks.get(z).getY() < pageMinY))
										pageMinY = blocks.get(z).getY();
									if ((blocks.get(z).getY() != 0.0) && (blocks.get(z).getY() > pageMaxY))
										pageMaxY = blocks.get(z).getY();
                                }
                                stop = true;
                                break;
                            }
							else {
                                if (blocks.get(z).getTokens() != null) {
									if ((blocks.get(z).getY() != 0.0) && (blocks.get(z).getY() < pageMinY))
										pageMinY = blocks.get(z).getY();
									if ((blocks.get(z).getY() != 0.0) && (blocks.get(z).getY() > pageMaxY))
										pageMaxY = blocks.get(z).getY();
                                }
							}
                        } else {
                            if (blocks.get(z).getTokens() != null) {
                                pageLength += blocks.get(z).getTokens().size();
								LayoutToken firstToken = blocks.get(z).getTokens().get(0);
								LayoutToken lastToken = blocks.get(z).getTokens()
									.get(blocks.get(z).getTokens().size() -1);
								if ((firstToken.getY() != 0.0) && (firstToken.getY() < pageMinY))
									pageMinY = firstToken.getY();
								if ((firstToken.getY() != 0.0) && (firstToken.getY() > pageMaxY))
									pageMaxY = firstToken.getY();
								if ((lastToken.getY() != 0.0) && (lastToken.getY() > pageMaxY))
									pageMaxY = lastToken.getY();
                            }
                        }
                    }
                }
				pageHeight = pageMaxY - pageMinY;
				//System.out.println(pageMaxY + " " + pageMinY);
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

            // check if we have a graphical object connected to the current block
            List<GraphicObject> localImages = Document.getConnectedGraphics(block, doc);
            if (localImages != null) {
                for(GraphicObject localImage : localImages) {
                    if (localImage.getType() == GraphicObject.BITMAP) 
                        graphicVector = true;
                    if (localImage.getType() == GraphicObject.VECTOR) 
                        graphicBitmap = true;
                }
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
			// set the max length of the lines in the block, in number of characters
			int maxLineLength = 0;
			for(int p=0; p<lines.length; p++) {
				if (lines[p].length() > maxLineLength) 
					maxLineLength = lines[p].length();
			}
            List<LayoutToken> tokens = block.getTokens();
            for (int li = 0; li < lines.length; li++) {
                String line = lines[li];
                boolean firstPageBlock = false;
                boolean lastPageBlock = false;

                if (newPage)
                    firstPageBlock = true;
                if (endPage)
                    lastPageBlock = true;

                if ((tokens == null) || (tokens.size() == 0)) {
                    continue;
                }
                // for the layout information of the block, we take simply the first layout token
				LayoutToken token = null;
				if (tokens.size() > 0)
					token = tokens.get(0);

				double coordinateLineY = token.getY();

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
                if ((text == null) ||
                        (text.trim().length() == 0) ||
                        (text.trim().equals("\n")) ||
                        (text.trim().equals("\r")) ||
                        (text.trim().equals("\n\r")) ||
                        (TextUtilities.filterLine(line))) {
                    continue;
                }

                text = text.trim();

                features.string = text;
                features.secondString = text2;

                features.firstPageBlock = firstPageBlock;
                features.lastPageBlock = lastPageBlock;
                //features.lineLength = line.length() / LINESCALE;
                features.lineLength = featureFactory
                        .relativeLocation(line.length(), maxLineLength, LINESCALE);
				
                features.punctuationProfile = TextUtilities.punctuationProfile(line);

                if (graphicBitmap) {
                	features.bitmapAround = true;
                }
                if (graphicVector) {
                	features.vectorAround = true;
                }

                features.lineStatus = null;
                features.punctType = null;

                if ((li == 0) ||
                        ((previousFeatures != null) && previousFeatures.blockStatus.equals("BLOCKEND"))) {
                    features.blockStatus = "BLOCKSTART";
                } else if (li == lines.length - 1) {
                    features.blockStatus = "BLOCKEND";
                    endblock = true;
                } else if (features.blockStatus == null) {
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
                features.relativePagePositionChar = featureFactory
                        .relativeLocation(mm, pageLength, NBBINS);
				
				int pagePos = featureFactory
                        .relativeLocation(coordinateLineY, pageHeight, NBBINS);
				if (pagePos > NBBINS)
					pagePos = NBBINS;
                features.relativePagePosition = pagePos;
//System.out.println(coordinateLineY + "\t" + pageHeight);

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
        DocumentSource documentSource = null;
        try {
            File file = new File(inputFile);

            documentSource = DocumentSource.fromPdfWithImages(file, -1, -1);
            Document doc = new Document(documentSource);

            String PDFFileName = file.getName();
            doc.addTokenizedDocument();

            if (doc.getBlocks() == null) {
                throw new Exception("PDF parsing resulted in empty content");
            }

            String fulltext = //getAllTextFeatured(doc, false);
                    getAllLinesFeatured(doc);
            List<LayoutToken> tokenizations = doc.getTokenizationsFulltext();

            // we write the full text untagged (but featurized)
            String outPathFulltext = pathFullText + File.separator + 
				PDFFileName.replace(".pdf", ".training.segmentation");
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");
            writer.write(fulltext + "\n");
            writer.close();

			// also write the raw text as seen before segmentation
			StringBuffer rawtxt = new StringBuffer();
			for(LayoutToken txtline : tokenizations) {
				rawtxt.append(txtline.getText());
			}
			String outPathRawtext = pathFullText + File.separator + 
				PDFFileName.replace(".pdf", ".training.segmentation.rawtxt");
			FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");

            if ((fulltext != null) && (fulltext.length() > 0)) {
                String rese = label(fulltext);
                StringBuffer bufferFulltext = trainingExtraction(rese, tokenizations, doc);

                // write the TEI file to reflect the extact layout of the text as extracted from the pdf
                writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
                        File.separator + 
						PDFFileName.replace(".pdf", ".training.segmentation.tei.xml")), false), "UTF-8");
                writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
                        "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">\n");

                writer.write(bufferFulltext.toString());
                writer.write("\n\t</text>\n</tei>\n");
                writer.close();
            }

        } catch (Exception e) {
			e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid training" +
                    " data generation for segmentation model.", e);
        } finally {
            DocumentSource.close(documentSource, true);
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
                                            List<LayoutToken> tokenizations,
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
                while ((line == null) && (currentBlockIndex < blocks.size())) {
                    Block block = blocks.get(currentBlockIndex);
                    List<LayoutToken> tokens = block.getTokens();
                    if (tokens == null) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    }
                    String localText = block.getText();
                    if ((localText == null) || (localText.trim().length() == 0)) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    }
                    //String[] lines = localText.split("\n");
                    String[] lines = localText.split("[\\n\\r]");
                    if ((lines.length == 0) || (indexLine >= lines.length)) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    } else {
                        line = lines[indexLine];
                        indexLine++;
                        if (line.trim().length() == 0) {
                            line = null;
                            continue;
                        }

                        if (TextUtilities.filterLine(line)) {
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
                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<acknowledgement>", "<div type=\"acknowledgement\">", addSpace, 3);
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
            line = line.replace("@BULLET", "\u2022");
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
            } else if (lastTag0.equals("<acknowledgement>")) {
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

}