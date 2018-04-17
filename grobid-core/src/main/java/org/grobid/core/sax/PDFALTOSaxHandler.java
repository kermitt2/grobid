package org.grobid.core.sax;

import org.grobid.core.analyzers.Analyzer;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.document.Document;
import org.grobid.core.layout.*;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.UnicodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ibm.icu.text.Normalizer2;


/**
 * SAX parser for XML-ALTO representation of PDF files obtained via xpdf pdfalto. All
 * typographical and layout information are defined token by token
 *
 * @author Achraf
 */
public class PDFALTOSaxHandler extends DefaultHandler {
	public static final Logger LOGGER = LoggerFactory.getLogger(PDFALTOSaxHandler.class);

    private StringBuilder accumulator = new StringBuilder(); // Accumulate parsed text


	private String previousToken = null;
	private LayoutToken previousTok = null;
	private double currentX = 0.0;
	private double currentY = 0.0;
	private double currentWidth = 0.0;
	private double currentHeight = 0.0;
	private Block block = null; // current block
	private int nbTokens = 0; // nb tokens in the current block
	private List<GraphicObject> images = null;
    private HashMap<String, TextStyles> textStyles = new HashMap<String, TextStyles>();
    private boolean currentRotation = false;

	private StringBuffer blabla = null;
	private List<LayoutToken> tokenizations = null;

	private Document doc = null;

    //starting page count from 1 since most of the PDF-related software count pages from 1
	private int currentPage = 0;
	private Page page = null; // the current page object
	private Analyzer analyzer = GrobidAnalyzer.getInstance(); // use the default one by default ;)

	private int currentOffset = 0;

	public PDFALTOSaxHandler(Document d, List<GraphicObject> im) {
		doc = d;
		blabla = new StringBuffer();
		images = im;
		tokenizations = new ArrayList<>();
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public Analyzer getAnalyzer() {
		return this.analyzer;
	}

	private void addToken(LayoutToken layoutToken) {
		layoutToken.setOffset(currentOffset);
		currentOffset += layoutToken.getText().length();
		tokenizations.add(layoutToken);
		if (doc.getBlocks() == null) {
			layoutToken.setBlockPtr(0);
		} else {
			layoutToken.setBlockPtr(doc.getBlocks().size());
		}
		if (block == null) {
            LOGGER.info("addToken called with null block object: " + layoutToken.toString());
        } else {
            block.addToken(layoutToken);
        }
	}

	private void addBlock(Block block) {
		if (block == null)
			LOGGER.info("addBlock called with null block object");

		if (!block.isNull() && (block.getStartToken() != block.getEndToken())) {
			block.setPage(page);
			doc.addBlock(block);
			page.addBlock(block);
		}
	}

	private void substituteLastToken(LayoutToken tok) {
		if (tokenizations.size()>0) {
			//System.out.println("last tokenizations was: " +
			//	tokenizations.get(tokenizations.size()-1));
			tokenizations.remove(tokenizations.size()-1);
		}
		tokenizations.add(tok);

		if (block == null)
			LOGGER.info("substituteLastToken called with null block object: " + tok.toString());

		if (block.getTokens() != null && !block.getTokens().isEmpty()) {
			block.getTokens().remove(block.getTokens().size() - 1);
		}
		if (doc.getBlocks() == null) {
			tok.setBlockPtr(0);
		} else {
			tok.setBlockPtr(doc.getBlocks().size());
		}
		block.addToken(tok);
	}

	private void removeLastTwoTokens() {
		if (tokenizations.size() > 0) {
			tokenizations.remove(tokenizations.size() - 1);
			if (tokenizations.size() > 0) {
				tokenizations
						.remove(tokenizations.size() - 1);
			}
		}

		if (block == null)
			LOGGER.info("removeLastTwoTokens called with null block object");

		if ((block.getTokens() != null) && (!block.getTokens().isEmpty())) {
			block.getTokens().remove(block.getTokens().size() - 1);
			if (!block.getTokens().isEmpty()) {
				block.getTokens().remove(block.getTokens().size() - 1);
			}
		}
	}

	public List<LayoutToken> getTokenization() {
		return tokenizations;
	}

	public void characters(char[] ch, int start, int length) {
		accumulator.append(ch, start, length);
	}

	public String trimAndNormaliseText(String content) {
		String res = content.trim();
		//res = res.replace("\u00A0", " "); // stdandard NO-BREAK SPACE are viewed
											// as space
		//res = res.replaceAll("\\p{javaSpaceChar}", " "); // replace all unicode space separators
		 												 // by a usual SPACE
		//res = res.replace("\t"," "); // case where tabulation are used as separator
									 // -> replace tabulation with a usual space

		res = UnicodeUtil.normaliseText(res);
		return res.trim();
	}


	private static void removeLastCharacterIfPresent(LayoutToken token) {
		if (token.getText() != null && token.getText().length() > 1) {
			token.setText(token.getText().substring(0,
					token.getText().length() - 1));
		} else {
			token.setText("");
		}
	}

	private enum ModifierClass {
		NOT_A_MODIFIER, DIAERESIS, ACUTE_ACCENT, DOUBLE_ACUTE_ACCENT, GRAVE_ACCENT, DOUBLE_GRAVE_ACCENT, BREVE_ACCENT, INVERTED_BREVE_ACCENT, CIRCUMFLEX, TILDE, NORDIC_RING, CZECH_CARON, CEDILLA, DOT_ABOVE, HOOK, HORN, MACRON, OGONEK,
	}

    /**
     * Classifies diacritics into a diacritic kind, taken from unicode chart : unicode.org/charts/PDF/U0300.pdf
     * @param c
     * @return
     */
    private ModifierClass classifyChar(Character c) {
        switch (c) {
            case '\u0308': //COMBINING DIAERESIS
            case '\u00A8': //DIAERESIS
                return ModifierClass.DIAERESIS;

            case '\u0341':
            case '\u030B': // COMBINING DOUBLE_ACUTE_ACCENT
            case '\u02DD':
                return ModifierClass.DOUBLE_ACUTE_ACCENT;
            case '\u00B4':
            case '\u0301': //COMBINING
            case '\u02CA':
                return ModifierClass.ACUTE_ACCENT;

            case '\u0300': //COMBINING
            case '\u0340':
            case '\u02CB':
            case '\u0060':
                return ModifierClass.GRAVE_ACCENT;

            case '\u030F': //COMBINING
                return ModifierClass.DOUBLE_GRAVE_ACCENT;

            case '\u0306': //COMBINING
            case '\u02D8':
                //case '\uA67C':
                return ModifierClass.BREVE_ACCENT;

            case '\u0311': //COMBINING
            case '\u0484':
            case '\u0487':
                return ModifierClass.INVERTED_BREVE_ACCENT;


            case '\u0302': //COMBINING
            case '\u005E':
            case '\u02C6':
                return ModifierClass.CIRCUMFLEX;


            case '\u0303': //COMBINING
            case '\u007E':
            case '\u02DC':
                return ModifierClass.TILDE;

            case '\u030A': //COMBINING
            case '\u00B0':
            case '\u02DA':
                return ModifierClass.NORDIC_RING;//LOOK AT UNICODE RING BELOW...

            case '\u030C': //COMBINING
            case '\u02C7':
                return ModifierClass.CZECH_CARON;

            case '\u0327': //COMBINING
            case '\u00B8':
                return ModifierClass.CEDILLA;

            case '\u0307': //COMBINING
            case '\u02D9':
                return ModifierClass.DOT_ABOVE;

            case '\u0309': //COMBINING
            case '\u02C0':
                return ModifierClass.HOOK;

            case '\u031B': //COMBINING
                return ModifierClass.HORN;

            case '\u0328': //COMBINING
            case '\u02DB':
            //case '\u1AB7':// combining open mark below
                return ModifierClass.OGONEK;

            case '\u0304': //COMBINING
            case '\u00AF':
            case '\u02C9':
                return ModifierClass.MACRON;
            default:
                return ModifierClass.NOT_A_MODIFIER;
        }
    }

	boolean isModifier(Character c) {
		return classifyChar(c) != ModifierClass.NOT_A_MODIFIER;
	}

	private String modifyCharacter(Character baseChar, Character modifierChar) {
        StringBuilder result = new StringBuilder();
        String diactritic = null;
		switch (classifyChar(modifierChar)) {
		case DIAERESIS:
            diactritic = "\u0308";
			break;
		case ACUTE_ACCENT:
            diactritic = "\u0301";
			break;
		case GRAVE_ACCENT:
            diactritic = "\u0300";
			break;
		case CIRCUMFLEX:
            diactritic = "\u0302";
			break;
		case TILDE:
            diactritic = "\u0303";
			break;
		case NORDIC_RING:
            diactritic = "\u030A";
			break;
		case CZECH_CARON:
            diactritic = "\u030C";
			break;
		case CEDILLA:
            diactritic = "\u0327";
			break;
        case DOUBLE_ACUTE_ACCENT:
            diactritic = "\u030B";
            break;
        case DOUBLE_GRAVE_ACCENT:
            diactritic = "\u030F";
            break;
        case BREVE_ACCENT:
            diactritic = "\u0311";
            break;
        case INVERTED_BREVE_ACCENT:
            diactritic = "\u0311";
            break;
        case DOT_ABOVE:
            diactritic = "\u0307";
            break;
        case HOOK:
            diactritic = "\u0309";
            break;
        case HORN:
            diactritic = "\u031B";
            break;
        case OGONEK:
            diactritic = "\u0328";
            break;
        case MACRON:
            diactritic = "\u0304";
            break;
		case NOT_A_MODIFIER:
			result.append(baseChar.toString());
			break;
		default:
			break;
		}

        if(diactritic != null){
            Normalizer2 base = Normalizer2.getNFKCInstance();
            StringBuilder cs2 = new StringBuilder();
            if(!base.isNormalized(baseChar.toString().subSequence(0, baseChar.toString().length())))
                cs2.append(base.normalize(baseChar.toString().subSequence(0, baseChar.toString().length())));
            else
                cs2.append(baseChar.toString());
            result = base.normalizeSecondAndAppend(cs2, diactritic.subSequence(0, diactritic.length()));
            // AA : drawback, normalizer concats strings when not resolved, e.g : dotless i
        }

		if (result == null) {
			LOGGER.debug("FIXME: cannot apply modifier '" + modifierChar
					+ "' to character '" + baseChar + "'");
		}

		return result.toString();
	}

	public void endElement(String uri, String localName,
			String qName) throws SAXException {
		// if (!qName.equals("TOKEN") && !qName.equals("BLOCK") &&
		// !qName.equals("TEXT"))
		// System.out.println(qName);

		if (qName.equals("TextLine")) {
			blabla.append("\n");
			LayoutToken token = new LayoutToken();
			token.setText("\n");
			token.setPage(currentPage);
			nbTokens++;
			accumulator.setLength(0);
//			tokenizations.add("\n");
//			tokenizations.add(token);
			addToken(token);
		} else if (qName.equals("Description")) {
			accumulator.setLength(0);
		} else if (qName.equals("String")) {

            accumulator.setLength(0);
		} else if (qName.equals("Page")) {
			// page marker are usefull to detect headers (same first line(s)
			// appearing on each page)
			if (block != null) {
				blabla.append("\n");
				LayoutToken localTok = new LayoutToken("\n");
				localTok.setPage(currentPage);
				addToken(localTok);
				block.setText(blabla.toString());
				block.setNbTokens(nbTokens);
				addBlock(block);
				//doc.addBlock(block);
				//page.addBlock(block);
			}
			/*Block block0 = new Block();
			block0.setText("@PAGE\n");
			block0.setNbTokens(0);
			//block0.setY(currentY);
			addBlock(block0);*/
			//block = new Block();
			//block.setPage(currentPage);
			//blabla = new StringBuffer();
			nbTokens = 0;
			/*LayoutToken localTok = new LayoutToken("\n");
			localTok.setPage(currentPage);
			addToken(localTok);*/
			doc.addPage(page);
		} else if (qName.equals("IMAGE")) {
			// this is normally the bitmap graphics
			if (block != null) {
				blabla.append("\n");
				block.setText(blabla.toString());
				block.setNbTokens(nbTokens);
				addBlock(block);
				//doc.addBlock(block);
				//page.addBlock(block);
			}
			block = new Block();
			//block.setPage(currentPage);
			blabla = new StringBuffer();
			if (images.size() > 0) {
				blabla.append("@IMAGE " + images.get(images.size()-1).getFilePath() + "\n");
			}
			int imagePos = images.size()-1;
			if (doc.getBlocks() != null)
				images.get(imagePos).setBlockNumber(doc.getBlocks().size());
			else
				images.get(imagePos).setBlockNumber(0);
			int startPos = 0;
			if (tokenizations.size() > 0)
				startPos = tokenizations.size()-1;
			int endPos = startPos;
			images.get(imagePos).setStartPosition(startPos);
			images.get(imagePos).setEndPosition(endPos);
			block.setText(blabla.toString());
			block.setNbTokens(nbTokens);
			images.get(imagePos).setBoundingBox(BoundingBox.fromPointAndDimensions(currentPage, currentX, currentY, currentWidth, currentHeight));
			block.setBoundingBox(BoundingBox.fromPointAndDimensions(currentPage, currentX, currentY, currentWidth, currentHeight));
			/*if (block.getX() == 0.0)
				block.setX(currentX);
			if (block.getY() == 0.0)
				block.setY(currentY);
			if (block.getWidth() == 0.0)
				block.setWidth(currentWidth);
			if (block.getHeight() == 0.0)
				block.setHeight(currentHeight);*/
			addBlock(block);
			//doc.addBlock(block);
			//page.addBlock(block);
			blabla = new StringBuffer();
			nbTokens = 0;
			//block = new Block();
			//block.setPage(currentPage);
		}
		/*
		 * else if (qName.equals("VECTORIALIMAGES")) { if (block != null) {
		 * blabla.append("\n"); block.setText(blabla.toString());
		 * block.setNbTokens(nbTokens); doc.addBlock(block); } block = new
		 * Block(); block.setPage(currentPage); blabla = new StringBuffer();
		 * blabla.append("@IMAGE " + "vectorial \n");
		 * block.setText(blabla.toString()); block.setNbTokens(nbTokens); if
		 * (block.getX() == 0.0) block.setX(currentX); if (block.getY() == 0.0)
		 * block.setY(currentY); if (block.getWidth() == 0.0)
		 * block.setWidth(currentWidth); if (block.getHeight() == 0.0)
		 * block.setHeight(currentHeight); doc.addBlock(block); blabla = new
		 * StringBuffer(); nbTokens = 0; block = new Block();
		 * block.setPage(currentPage); }
		 */
		else if (qName.equals("TextBlock")) {
			blabla.append("\n");
			LayoutToken localTok = new LayoutToken("\n");
			localTok.setPage(currentPage);
			addToken(localTok);
			block.setText(blabla.toString());

			//PL
			//block.setWidth(currentX - block.getX() + currentWidth);
			//block.setHeight(currentY - block.getY() + currentHeight);

			addBlock(block);
			nbTokens = 0;
			block = null;
		} else if (qName.equals("xi:include")) {
			// this is normally the vector graphics
			// such vector graphics are appliedto the whole page, so there is no x,y coordinates available
			// in the xml - to get them we will need to parse the .vec files
			if (block != null) {
				blabla.append("\n");
				block.setText(blabla.toString());
				block.setNbTokens(nbTokens);
				addBlock(block);
				//doc.addBlock(block);
				//page.addBlock(block);
			}
			block = new Block();
			//block.setPage(currentPage);
			blabla = new StringBuffer();
			blabla.append("@IMAGE " + images.get(images.size()-1).getFilePath() + "\n");
			int imagePos = images.size()-1;
			if (doc.getBlocks() != null)
				images.get(imagePos).setBlockNumber(doc.getBlocks().size());
			else
				images.get(imagePos).setBlockNumber(0);
			int startPos = 0;
			if (tokenizations.size() > 0)
				startPos = tokenizations.size()-1;
			int endPos = startPos;
			images.get(imagePos).setStartPosition(startPos);
			images.get(imagePos).setEndPosition(endPos);
			images.get(imagePos).setPage(currentPage);
			block.setText(blabla.toString());
			block.setNbTokens(nbTokens);
			addBlock(block);
			//doc.addBlock(block);
			//page.addBlock(block);
			blabla = new StringBuffer();
			nbTokens = 0;
			//block = new Block();
			//block.setPage(currentPage);
		}

		/*
		 * else if (qName.equals("DOCUMENT")) {
		 * System.out.println(blabla.toString()); }
		 */

	}

	public void endDocument(){
//		if(CollectionUtils.isEmpty(images)) {
			doc.setImages(images);
//		}
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		if (qName.equals("Page")) {
			int length = atts.getLength();
			currentPage++;
			page = new Page(currentPage);

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("WIDTH")) {
						double width = Double.parseDouble(value);
						page.setWidth(width);
					} else if (name.equals("HEIGHT")) {
						double height = Double.parseDouble(value);
						page.setHeight(height);
					}
				}
			}
		} else if (qName.equals("PrintSpace")) {
            int length = atts.getLength();
            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if ((name != null) && (value != null)) {
                    switch (name) {
                        case "HPOS":
                            double x = Double.parseDouble(value);
                            if (x != currentX) {
                                currentX = Math.abs(x);
                            }
                            break;
                        case "VPOS":
                            double y = Double.parseDouble(value);
                            if (y != currentY) {
                                currentY = Math.abs(y);
                            }
                            break;
                        case "WIDTH":
                            double width = Double.parseDouble(value);
                            if (width != currentWidth) {
                                currentWidth = Math.abs(width);
                            }
                            break;
                        case "HEIGHT":
                            double height = Double.parseDouble(value);
                            if (height != currentHeight) {
                                currentHeight = Math.abs(height);
                            }
                            break;
                    }
                }
            }
        } else if (qName.equals("TextBlock")) {
			block = new Block();
			blabla = new StringBuffer();
			nbTokens = 0;
			//block.setPage(currentPage);
			// blabla.append("\n@block\n");
		} else if (qName.equals("Illustration")) {
			int length = atts.getLength();
			GraphicObject image = new GraphicObject();

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					switch (name) {
						case "FILEID":
							image.setFilePath(value);
							if (value.contains(".vec")) {
								image.setType(GraphicObjectType.VECTOR);
							} else {
								image.setType(GraphicObjectType.BITMAP);
							}
							break;
						case "mask":
							if ("true".equals(value)) {
								image.setMask(true);
							}
							break;
					}
				}
			}
			//image.setPage(currentPage);
			images.add(image);
		} else if (qName.equals("TextLine")) {
			int length = atts.getLength();

			// Process each attribute
			/*for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("id")) {
					} else if (name.equals("x")) {
					} else if (name.equals("y")) {
					} else if (name.equals("width")) {
					} else if (name.equals("height")) {
					}
				}
			}*/
		} else if (qName.equals("String")) {
			int length = atts.getLength();
			String content = null, fontId = null;
            TextStyles textStyle = null;

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("ID")) {
						;
					} else if (name.equals("CONTENT")) {
                        content = value;
                    } else if (name.equals("STYLEREFS")) {
                        fontId = value;
                    }else if (name.equals("rotation")) {
						if (value.equals("0"))
							currentRotation = false;
						else
							currentRotation = true;
					} else if (name.equals("HPOS")) {
						double x = Double.parseDouble(value);
						if (x != currentX) {
							currentX = Math.abs(x);
						}
					} else if (name.equals("VPOS")) {
						double y = Double.parseDouble(value);
						if (y != currentY) {
							currentY = Math.abs(y);
						}
					} else if (name.equals("base")) {
						double base = Double.parseDouble(value);

					} else if (name.equals("WIDTH")) {
						double width = Double.parseDouble(value);
						if (width != currentWidth) {
							currentWidth = Math.abs(width);
						}
					} else if (name.equals("HEIGHT")) {
						double height = Double.parseDouble(value);
						if (height != currentHeight) {
							currentHeight = Math.abs(height);
						}
					}
				}
			}

            // process ligatures
            String tok0 = TextUtilities.clean(trimAndNormaliseText(content));
            textStyle = textStyles.get(fontId);

            if (block.getStartToken() == -1) {
                block.setStartToken(tokenizations.size());
            }

            if (tok0.length() > 0) {
                //StringTokenizer st = new StringTokenizer(tok0,
                //		TextUtilities.delimiters, true);
                List<String> subTokenizations = new ArrayList<>();
                try {
                    // TBD: pass a language object to the tokenize method call
                    subTokenizations = analyzer.tokenize(tok0);
                }
                catch(Exception e) {
                    LOGGER.debug("Sub-tokenization of pdfalto token has failed.");
                }
                boolean diaresis;
                boolean accent;
                //while (st.hasMoreTokens()) {

                if (subTokenizations.size() != 0) {
                    //{
                    // WARNING: ROUGH APPROXIMATION (but better than the same coords)

                    double totalLength = 0;
                    for (String t : subTokenizations) {
                        totalLength += t.length();
                    }
                    double prevSubWidth = 0;

                    for(String tok : subTokenizations) {

                        diaresis = false;
                        accent = false;

                        // WARNING: ROUGH APPROXIMATION (but better than the same coords)
                        // Here to get the right subTokWidth should use the content length.
                        double subTokWidth = (currentWidth * (tok.length() / totalLength));

                        double subTokX = currentX + prevSubWidth;
                        prevSubWidth += subTokWidth;

                        //String tok = st.nextToken();
                        if (tok.length() > 0) {

                            LayoutToken token = new LayoutToken();
                            token.setPage(currentPage);
                            if ( (previousToken != null) && (tok != null)
                                && (previousToken.length() > 0)
                                && (tok.length() > 0)
                                && (blabla.length() > 0)
                                && (previousTok.getText() != null)
                                && (previousTok.getText().length() > 1)	) {

                                Character leftChar = previousTok.getText().charAt(
                                    previousTok.getText().length() - 1);
                                Character rightChar = tok.charAt(0);

                                ModifierClass leftClass = classifyChar(leftChar);
                                ModifierClass rightClass = classifyChar(rightChar);
                                ModifierClass modifierClass = ModifierClass.NOT_A_MODIFIER;

                                if (leftClass != ModifierClass.NOT_A_MODIFIER
                                    || rightClass != ModifierClass.NOT_A_MODIFIER) {
                                    Character baseChar = null;
                                    Character modifierChar = null;

                                    if (leftClass != ModifierClass.NOT_A_MODIFIER) {
                                        if (rightClass != ModifierClass.NOT_A_MODIFIER) {
                                            //assert false;
                                            // keeping characters, but setting class
                                            // to not a modifier
                                            baseChar = leftChar;
                                            modifierChar = rightChar;
                                            modifierClass = ModifierClass.NOT_A_MODIFIER;
                                        } else {
                                            baseChar = rightChar;
                                            modifierChar = leftChar;
                                            modifierClass = leftClass;
                                        }
                                    } else {
                                        baseChar = leftChar;
                                        modifierChar = rightChar;
                                        modifierClass = rightClass;
                                    }

                                    String updatedChar = modifyCharacter(baseChar,
                                        modifierChar);

                                    //System.out.println("\t"+"baseChar: " + baseChar + ", modifierChar: "
                                    //	+ modifierChar +", updatedChar is " + updatedChar);

                                    if (updatedChar != null) {
                                        //System.out.println("\n");
                                        //}
                                        //else {
//										tokenizations.remove(tokenizations.size() - 1);
//										if (tokenizations.size() > 0) {
//											tokenizations
//												.remove(tokenizations.size() - 1);
//										}

                                        removeLastTwoTokens();

                                        blabla.deleteCharAt(blabla.length() - 1);
                                        if (blabla.length() > 0) {
                                            blabla.deleteCharAt(blabla.length() - 1);
                                        }

                                        removeLastCharacterIfPresent(previousTok);
                                        //}
                                        //if (updatedChar != null) {
                                        blabla.append(updatedChar);
                                        previousTok.setText(previousTok.getText()
                                            + updatedChar);

                                        LayoutToken localTok = new LayoutToken(previousTok.getText());
                                        localTok.setPage(currentPage);
                                        localTok.setX(previousTok.getX());
                                        localTok.setY(previousTok.getY());
                                        localTok.setHeight(previousTok.getHeight());
                                        localTok.setWidth(previousTok.getWidth());
                                        localTok.setFontSize(previousTok.getFontSize());
                                        localTok.setColorFont(previousTok.getColorFont());
                                        localTok.setItalic(previousTok.getItalic());
                                        localTok.setBold(previousTok.getBold());
                                        localTok.setRotation(previousTok.getRotation());
                                        addToken(localTok);

//										addToken(previousTok);

                                        //System.out.println("add token layout: " + previousTok.getText());
                                        //System.out.println("add tokenizations: " + previousTok.getText());
                                    }
                                    {
                                        // PL
                                        blabla.append(tok.substring(1, tok.length()));
                                        if (updatedChar != null) {
                                            previousTok.setText(previousTok.getText()
                                                + tok.substring(1, tok.length()));
                                        }
                                        else {
                                            // in this case, the diaresis/accent might be before the charcater
                                            // to be modified and not after as incorrectly considered first
                                            // see issue #47
                                            previousTok.setText(previousTok.getText() + tok);
                                        }

                                        //System.out.println("add token layout: " + previousTok.getText());
                                        LayoutToken localTok = new LayoutToken(previousTok.getText());
                                        localTok.setPage(currentPage);
                                        localTok.setX(previousTok.getX());
                                        localTok.setY(previousTok.getY());
                                        localTok.setHeight(previousTok.getHeight());
                                        // the new token based on the concatenation of the previous token and
                                        // the updated diaresis character
                                        localTok.setWidth(previousTok.getWidth() + subTokWidth);
                                        localTok.setFontSize(previousTok.getFontSize());
                                        localTok.setColorFont(previousTok.getColorFont());
                                        localTok.setItalic(previousTok.getItalic());
                                        localTok.setBold(previousTok.getBold());
                                        localTok.setRotation(previousTok.getRotation());
                                        substituteLastToken(localTok);
                                        //System.out.println("replaced by tokenizations: " + previousTok.getText());
                                    }

                                    diaresis = (modifierClass == ModifierClass.DIAERESIS
                                        || modifierClass == ModifierClass.NORDIC_RING
                                        || modifierClass == ModifierClass.CZECH_CARON
                                        || modifierClass == ModifierClass.TILDE
                                        || modifierClass == ModifierClass.CEDILLA);

                                    accent = (modifierClass == ModifierClass.ACUTE_ACCENT
                                        || modifierClass == ModifierClass.CIRCUMFLEX
                                        || modifierClass == ModifierClass.GRAVE_ACCENT);

                                    if (rightClass != ModifierClass.NOT_A_MODIFIER) {
                                        tok = ""; // resetting current token as it
                                        // is a single-item
                                    }
                                }
                            }

                            if (tok != null) {
                                // actually in certain cases, the extracted string under token can be a chunk of text
                                // with separators that need to be preserved
                                //tok = tok.replace(" ", "");
                            }

                            if ((!diaresis) && (!accent)) {
                                // blabla.append(" ");
                                blabla.append(tok);
                                token.setText(tok);

                                addToken(token);
//								tokenizations.add(token);
                            } else {
                                tok = "";
                                //keepLast = true;
                            }

                            if (currentRotation) {
                                // if the text is rotated, it appears that the font size is multiplied
                                // by 2? we should have a look at pdf2xml for this
                                textStyle.setFontSize(textStyle.getFontSize() / 2);
                            }

                            if (textStyle.getFontName() != null)
                                token.setFont(textStyle.getFontName().toLowerCase());
                            else
                                token.setFont("default");
                            token.setItalic(textStyle.isItalic());
                            token.setBold(textStyle.isBold());
                            token.setRotation(currentRotation);
                            token.setPage(currentPage);
                            token.setColorFont(textStyle.getFontColor());

                            token.setX(subTokX);
                            token.setY(currentY);
                            token.setWidth(subTokWidth);
                            token.setHeight(currentHeight);

//							token.setX(currentX);
//							token.setY(currentY);
//							token.setWidth(currentWidth);
//							token.setHeight(currentHeight);

                            token.setFontSize(textStyle.getFontSize());

//							if (!diaresis && !accent) {
//
//								block.addToken(token);
//							}

                            if (block.getFont() == null) {
                                if (textStyle.getFontName() != null)
                                    block.setFont(textStyle.getFontName().toLowerCase());
                                else
                                    token.setFont("default");
                            }
                            if (nbTokens == 0) {
                                block.setItalic(textStyle.isItalic());
                                block.setBold(textStyle.isBold());
                            }
                            if (block.getColorFont() == null)
                                block.setColorFont(textStyle.getFontColor());

							/*if (block.getX() == 0.0)
								block.setX(currentX);
							if (block.getY() == 0.0)
								block.setY(currentY);
							if (block.getWidth() == 0.0)
								block.setWidth(currentWidth);
							if (block.getHeight() == 0.0)
								block.setHeight(currentHeight);
							if (block.getFontSize() == 0.0)
								block.setFontSize(currentFontSize);*/

                            if (!diaresis && !accent) {
                                previousToken = tok;
                                previousTok = token;
                            } else {
                                previousToken = previousTok.getText();
                            }

                            nbTokens++;
                        }
                    }
                }
                if (tokenizations.size() > 0) {
                    String justBefore = tokenizations
                        .get(tokenizations.size() - 1).t();
                    if (!justBefore.endsWith("-")) {
                        LayoutToken localTok = new LayoutToken(" ");
                        localTok.setPage(currentPage);
                        addToken(localTok);
                        blabla.append(" ");
                    }
                }
            }
            block.setEndToken(tokenizations.size());
		}
		else if (qName.equals("xi:include")) {
			// normally this introduces vector graphics
			int length = atts.getLength();
			GraphicObject image = new GraphicObject();

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("FILEID")) {
						// if (images == null)
						// images = new ArrayList<String>();
						image.setFilePath(value);
						if (value.contains(".vec"))
							image.setType(GraphicObjectType.VECTOR);
						else
							image.setType(GraphicObjectType.BITMAP);
					}
				}
			}
			//image.setPage(currentPage);
			images.add(image);
		}
		// accumulator.setLength(0);

        else if (qName.equals("TextStyle")) {
            int length = atts.getLength();

            TextStyles textStyle = new TextStyles();
            String fontId = null;
            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if ((name != null) && (value != null)) {
                    if (name.equals("ID")) {
                        fontId = value;
                        blabla.append(" ");
                    } else if (name.equals("FONTFAMILY")) {
                        textStyle.setFontName(value);
                        blabla.append(" ");
                    } else if (name.equals("FONTSIZE")) {
                        double fontSize = Double.parseDouble(value);
                        textStyle.setFontSize(fontSize);
                        blabla.append(" ");
                    } else if (name.equals("FONTSTYLE")) {
                        if (value.contains("bold")) {
                            textStyle.setBold(true);
                        } else if (value.contains("italics")){
                            textStyle.setItalic(true);
                        } else {
                            textStyle.setBold(false);
                            textStyle.setItalic(false);
                        }
                        blabla.append(" ");
                    } else if (name.equals("FONTCOLOR")) {
                        textStyle.setFontColor(value);
                    }
//                    else if (name.equals("FONTTYPE")) {
//                        if (value.equals("serif")) {
//                            textStyle.setSerif(true);
//                        } else {
//                            textStyle.setSerif(false);
//                        }
//                        blabla.append(" ");
//                    } else if (name.equals("FONTWIDTH")) {
//                        if (value.equals("proportional")) {
//                            textStyle.setProportional(true);
//                        } else {
//                            textStyle.setProportional(false);
//                        }
//                        blabla.append(" ");
//                    }
//
//                    else if (name.equals("rotation")) {
//                        if (value.equals("0"))
//                            textStyle.setRotation(false);
//                        else
//                            textStyle.setRotation(true);
//                    }
                }
            }
            if(fontId != null)
                textStyles.put(fontId, textStyle);
        }
	}

}

class TextStyles {

    private double fontSize = 0.0;
    private String fontName = null;
    private String fontColor = null;

    private boolean bold = false;
    private boolean italic = false;
    //not used attributes
    private boolean proportional = false;
    private boolean serif = false;

    //private boolean rotation = false;

    public double getFontSize() {
        return fontSize;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

//    public boolean isRotation() {
//        return rotation;
//    }
//
//    public void setRotation(boolean rotation) {
//        this.rotation = rotation;
//    }

    public boolean isProportional() {
        return proportional;
    }

    public void setProportional(boolean proportional) {
        this.proportional = proportional;
    }

    public boolean isSerif() {
        return serif;
    }

    public void setSerif(boolean serif) {
        this.serif = serif;
    }
}
