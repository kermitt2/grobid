package org.grobid.core.sax;

import org.grobid.core.layout.Block;
import org.grobid.core.layout.GraphicObjectType;
import org.grobid.core.layout.Page;
import org.grobid.core.document.Document;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;

/**
 * SAX parser for XML representation of PDF files obtained via xpdf pdf2xml. All
 * typographical and layout information are defined token by token
 * 
 * @author Patrice Lopez
 */
public class PDF2XMLSaxParser extends DefaultHandler {
	/**
	 * The Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(PDF2XMLSaxParser.class);

	private StringBuffer accumulator = new StringBuffer(); // Accumulate parsed
															// text
	private String currentFont = null;
	private String colorFont = null;
	private String previousToken = null;
	private LayoutToken previousTok = null;
	private double currentFontSize = 0.0;
	private double currentX = 0.0;
	private double currentY = 0.0;
	private double currentWidth = 0.0;
	private double currentHeight = 0.0;
	private boolean currentBold = false;
	private boolean currentItalic = false;
	private boolean currentRotation = false;
	private Block block = null; // current block
	private int nbTokens = 0; // nb tokens in the current block
	private List<GraphicObject> images = null;

	private StringBuffer blabla = null;
	private List<LayoutToken> tokenizations = null;

	private Document doc = null;

    //starting page count from 1 since most of the PDF-related software count pages from 1
	private int currentPage = 0;
	private Page page = null; // the current page object
	private GrobidAnalyzer analyzer = GrobidAnalyzer.getInstance(); 

//	public PDF2XMLSaxParser() {
//		blabla = new StringBuffer();
//		tokenizations = new ArrayList<LayoutToken>();
//	}

	public PDF2XMLSaxParser(Document d, List<GraphicObject> im) {
		doc = d;
		blabla = new StringBuffer();
		images = im;
		tokenizations = new ArrayList<>();
	}

	private void addToken(LayoutToken layoutToken) {
		tokenizations.add(layoutToken);
		if (doc.getBlocks() == null) {
			layoutToken.setBlockPtr(0);
		} else {
			layoutToken.setBlockPtr(doc.getBlocks().size());
		}
		block.addToken(layoutToken);
	}

	private void addBlock(Block block) {
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

	public String getText() {
		String res = accumulator.toString().trim();
		//res = res.replace("\u00A0", " "); // stdandard NO-BREAK SPACE are viewed
											// as space
		res = res.replaceAll("\\p{javaSpaceChar}", " "); // replace all unicode space separators
		 												 // by a usual SPACE
		res = res.replace("\t"," "); // case where tabulation are used as separator
									 // -> replace tabulation with a usual space
		return res.trim();
	}

	private String addDiaeresisToCharV1(Character c) {
		switch (c) {
		case 'a':
			return "\u00E4";
		case 'e':
			return "\u00EB";
		case 'i':
			return "\u00EF";
		case 'l':
			return "\u00EF";
		case 'o':
			return "\u00F6";
		case 'u':
			return "\u00FC";
		default:
			return null;
		}
	}

	private String addDiaeresisToCharV2(Character c) {
		switch (c) {
		case 'a':
			return "\u00E4";
		case 'A':
			return "\u00C4";
			// case 'a' : return "Ã¤");
		case 'e':
			return "\u00EB";
		case 'E':
			return "\u00CB";
		case 'i':
			return "\u00EF";
		case 'l':
			return "\u00EF";
		case 'I':
			return "\u00CF";
		case 'o':
			return "\u00F6";
		case 'O':
			return "\u00D6";
		case 'u':
			return "\u00FC";
		case 'U':
			return "\u00DC";
		default:
			return null;
		}
	}
	
    private String addAcuteAccentToChar(Character c) {
        switch (c) {
            case 'a':
                return  "\u00E1";
            case 'e':
                return "\u00E9";
            case 'i':
            case 'ı':
                return "\u00ED";
            case 'l':
                return "\u00ED";
            case 'o':
                return "\u00F3";
            case 'u':
                return "\u00FA";
            case 'E':
                return "\u00C9";
            default:
                return null;
        }
    }

	private String addGraveAccentToChar(Character c) {
		switch (c) {
		case 'a':
			return "\u00E0";
		case 'e':
			return "\u00E8";
		case 'i':
			return "\u00EC";
		case 'l':
			return "\u00EC";
		case 'o':
			return "\u00F2";
		case 'u':
			return "\u00F9";
		default:
			return null;
		}
	}

	private String addCircumflexAccentToChar(Character c) {
		switch (c) {
		case 'a':
			return "\u00E2";
		case 'e':
			return "\u00EA";
		case 'i':
			return "\u00EE";
		case 'l':
			return "\u00EE";
		case 'o':
			return "\u00F4";
		case 'u':
			return "\u00FB";
		default:
			return null;
		}
	}

	private String addTildeToChar(Character c) {
		switch (c) {
		case 'n':
			return "\u00F1";
		case 'o':
			return "\u00F5";
		case 'a':
			return "\u00E3";
		default:
			return null;
		}
	}

	private String addNordicRingToChar(Character c) {
		switch (c) {
		case 'a':
			return "\u00E5";
		case 'A':
			return "\u00C5";
		case 'u':
			return "\u016F";
		case 'U':
			return "\u016E";
		default:
			return null;
		}
	}

	private String addCzechCaronToChar(Character c) {
		switch (c) {
		case 'r':
			return "\u0159";
		case 'R':
			return "\u0158";
		case 'c':
			return "\u010D";
		case 'C':
			return "\u010C";
		case 'n':
			return "\u0148";
		case 'N':
			return "\u0147";
		case 'z':
			return "\u017E";
		case 'Z':
			return "\u017D";
		case 'e':
			return "\u011B";
		case 'E':
			return "\u011A";
		case 's':
			return "\u0161";
		case 'S':
			return "\u0160";
		default:
			return null;
		}
	}

	private String addCedillaToChar(Character c) {
		switch (c) {
		case 'c':
			return "\u00E7";
		case 'C':
			return "\u00C7";
		default:
			return null;
		}
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
		NOT_A_MODIFIER, DIAERESIS, ACUTE_ACCENT, GRAVE_ACCENT, CIRCUMFLEX, TILDE, NORDIC_RING, CZECH_CARON, CEDILLA,
	}

	private ModifierClass classifyChar(Character c) {
		switch (c) {
		case '\u00A8':
			return ModifierClass.DIAERESIS;

		case '\u00B4':
		case '\u0301':
		case '\u02CA':
			return ModifierClass.ACUTE_ACCENT;

		case '\u0060':
			return ModifierClass.GRAVE_ACCENT;

		case '\u02C6':
			return ModifierClass.CIRCUMFLEX;

		case '\u02DC':
			return ModifierClass.TILDE;

		case '\u02DA':
			return ModifierClass.NORDIC_RING;

		case '\u02C7':
			return ModifierClass.CZECH_CARON;

		case '\u00B8':
			return ModifierClass.CEDILLA;

		default:
			return ModifierClass.NOT_A_MODIFIER;
		}
	}

	boolean isModifier(Character c) {
		return classifyChar(c) != ModifierClass.NOT_A_MODIFIER;
	}

	private String modifyCharacter(Character baseChar, Character modifierChar) {
		String result = null;

		switch (classifyChar(modifierChar)) {
		case DIAERESIS:
			result = addDiaeresisToCharV1(baseChar);
			break;
		case ACUTE_ACCENT:
			result = addAcuteAccentToChar(baseChar);
			break;
		case GRAVE_ACCENT:
			result = addGraveAccentToChar(baseChar);
			break;
		case CIRCUMFLEX:
			result = addCircumflexAccentToChar(baseChar);
			break;
		case TILDE:
			result = addTildeToChar(baseChar);
			break;
		case NORDIC_RING:
			result = addNordicRingToChar(baseChar);
			break;
		case CZECH_CARON:
			result = addCzechCaronToChar(baseChar);
			break;
		case CEDILLA:
			result = addCedillaToChar(baseChar);
			break;
		case NOT_A_MODIFIER:
			result = baseChar.toString();
			break;
		default:
			break;
		}

		if (result == null) {
			LOGGER.debug("FIXME: cannot apply modifier '" + modifierChar
					+ "' to character '" + baseChar + "'");
		}

		return result;
	}

	public void endElement(java.lang.String uri, java.lang.String localName,
			java.lang.String qName) throws SAXException {
		// if (!qName.equals("TOKEN") && !qName.equals("BLOCK") &&
		// !qName.equals("TEXT"))
		// System.out.println(qName);

		if (qName.equals("TEXT")) {
			blabla.append("\n");
			LayoutToken token = new LayoutToken();
			token.setText("\n");
			token.setPage(currentPage);
			nbTokens++;
			accumulator.setLength(0);
//			tokenizations.add("\n");
//			tokenizations.add(token);
			addToken(token);
		} else if (qName.equals("METADATA")) {
			accumulator.setLength(0);
		} else if (qName.equals("TOKEN")) {
			String tok0 = TextUtilities.clean(getText());

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
					LOGGER.debug("Sub-tokenization of pdf2xml token has failed.");
				}
				boolean diaresis;
				boolean accent;
				//while (st.hasMoreTokens()) {

				if (subTokenizations.size() != 0) {
				//{
					// WARNING: ROUGH APPROXIMATION (but better then the same coords)

					double totalLength = 0;
					for (String t : subTokenizations) {
						totalLength += t.length();
					}
					double prevSubWidth = 0;

					for(String tok : subTokenizations) {

						diaresis = false;
						accent = false;

						// WARNING: ROUGH APPROXIMATION (but better then the same coords)
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
								currentFontSize = currentFontSize / 2;
							}

							if (currentFont != null)
								token.setFont(currentFont.toLowerCase());
							else
								token.setFont("default");
							token.setItalic(currentItalic);
							token.setBold(currentBold);
							token.setRotation(currentRotation);
                            token.setPage(currentPage);
							token.setColorFont(colorFont);

							token.setX(subTokX);
							token.setY(currentY);
							token.setWidth(subTokWidth);
							token.setHeight(currentHeight);

//							token.setX(currentX);
//							token.setY(currentY);
//							token.setWidth(currentWidth);
//							token.setHeight(currentHeight);

							token.setFontSize(currentFontSize);

//							if (!diaresis && !accent) {
//
//								block.addToken(token);
//							}

							if (block.getFont() == null) {
								if (currentFont != null)
									block.setFont(currentFont.toLowerCase());
								else
									token.setFont("default");
							}
							if (nbTokens == 0) {
								block.setItalic(currentItalic);
								block.setBold(currentBold);
							}
							if (block.getColorFont() == null)
								block.setColorFont(colorFont);

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
							accumulator.setLength(0);
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
		} else if (qName.equals("PAGE")) {
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
			Block block0 = new Block();
			block0.setText("@PAGE\n");
			block0.setNbTokens(0);
			//block0.setY(currentY);
			addBlock(block0);
			//block = new Block();
			//block.setPage(currentPage);
			blabla = new StringBuffer();
			nbTokens = 0;
			LayoutToken localTok = new LayoutToken("\n");
			localTok.setPage(currentPage);
			addToken(localTok);
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
		else if (qName.equals("BLOCK")) {
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

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (qName.equals("PAGE")) {
			int length = atts.getLength();
			currentPage++;
			page = new Page(currentPage);

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("width")) {
						double width = Double.parseDouble(value);
						page.setWidth(width);
					} else if (name.equals("height")) {
						double height = Double.parseDouble(value);
						page.setHeight(height);
					}
				}
			}
		} else if (qName.equals("BLOCK")) {
			block = new Block();
			blabla = new StringBuffer();
			nbTokens = 0;
			//block.setPage(currentPage);
			// blabla.append("\n@block\n");
		} else if (qName.equals("IMAGE")) {
			int length = atts.getLength();
			GraphicObject image = new GraphicObject();

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("href")) {
						image.setFilePath(value);
						if (value.indexOf(".vec") != -1)
							image.setType(GraphicObjectType.VECTOR);
						else
							image.setType(GraphicObjectType.BITMAP);
					} else if (name.equals("x")) {
						double x = Double.parseDouble(value);
						if (x != currentX) {
							currentX = Math.abs(x);
						}
						//image.setX(x);
					} else if (name.equals("y")) {
						double y = Double.parseDouble(value);
						if (y != currentY) {
							currentY = Math.abs(y);
						}
						//image.setY(y);
					} else if (name.equals("width")) {
						double width = Double.parseDouble(value);
						if (width != currentWidth) {
							currentWidth = Math.abs(width);
						}
						//image.setWidth(width);
					} else if (name.equals("height")) {
						double height = Double.parseDouble(value);
						if (height != currentHeight) {
							currentHeight = Math.abs(height);
						}
						//image.setHeight(height);
					}
				}
			}
			//image.setPage(currentPage);
			images.add(image);
		} else if (qName.equals("TEXT")) {
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
		} else if (qName.equals("TOKEN")) {
			int length = atts.getLength();

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("id")) {
						;
					} else if (name.equals("font-name")) {
						if (!value.equals(currentFont)) {
							currentFont = value;
							blabla.append(" ");
						}
					} else if (name.equals("font-size")) {
						double fontSize = Double.parseDouble(value);
						if (fontSize != currentFontSize) {
							currentFontSize = fontSize;

							blabla.append(" ");
						}
					} else if (name.equals("bold")) {
						if (value.equals("yes")) {
							currentBold = true;
						} else {
							currentBold = false;
						}
					} else if (name.equals("italic")) {
						if (value.equals("yes")) {
							currentItalic = true;
						} else {
							currentItalic = false;
						}
					} else if (name.equals("font-color")) {
						if (!value.equals(colorFont)) {
							colorFont = value;
						}
					} else if (name.equals("rotation")) {
						if (value.equals("0"))
							currentRotation = false;
						else
							currentRotation = true;
					} else if (name.equals("x")) {
						double x = Double.parseDouble(value);
						if (x != currentX) {
							currentX = Math.abs(x);
						}
					} else if (name.equals("y")) {
						double y = Double.parseDouble(value);
						if (y != currentY) {
							currentY = Math.abs(y);
						}
					} else if (name.equals("base")) {
						double base = Double.parseDouble(value);

					} else if (name.equals("width")) {
						double width = Double.parseDouble(value);
						if (width != currentWidth) {
							currentWidth = Math.abs(width);
						}
					} else if (name.equals("height")) {
						double height = Double.parseDouble(value);
						if (height != currentHeight) {
							currentHeight = Math.abs(height);
						}
					}
				}
			}
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
					if (name.equals("href")) {
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
	}

}
