package org.grobid.core.sax;

import org.grobid.core.layout.Block;
import org.grobid.core.document.Document;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.TextUtilities;
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
	private List<String> images = null;

	private StringBuffer blabla = null;
	private ArrayList<String> tokenizations = null;

	private Document doc = null;

	private int currentPage = -1;

	public PDF2XMLSaxParser() {
		blabla = new StringBuffer();
		tokenizations = new ArrayList<>();
	}

	public PDF2XMLSaxParser(Document d, List<String> im) {
		doc = d;
		blabla = new StringBuffer();
		images = im;
		tokenizations = new ArrayList<>();
	}

	public ArrayList<String> getTokenization() {
		return tokenizations;
	}

	public void characters(char[] ch, int start, int length) {
		accumulator.append(ch, start, length);
	}

	public String getText() {
		String res = accumulator.toString().trim();
		res = res.replace("\u00A0", " "); // stdandard NO-BREAK SPACE are viewed
											// as space
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
			block.addToken(token);
			accumulator.setLength(0);
			tokenizations.add("\n");
		} else if (qName.equals("METADATA")) {
			accumulator.setLength(0);
		} else if (qName.equals("TOKEN")) {
			String tok0 = TextUtilities.clean(getText());

			if (block.getStartToken() == -1) {
				block.setStartToken(tokenizations.size());
			}

			if (tok0.length() > 0) {
				StringTokenizer st = new StringTokenizer(tok0,
						TextUtilities.fullPunctuations, true);
				boolean diaresis = false;
				boolean accent = false;
				boolean keepLast = false;
				while (st.hasMoreTokens()) {

					diaresis = false;
					accent = false;
					keepLast = false;

					String tok = st.nextToken();
					if (tok.length() > 0) {

						LayoutToken token = new LayoutToken();

						if ((previousToken != null) && (tok != null)
								&& (previousToken.length() > 0)
								&& (tok.length() > 0) && blabla.length() > 0) {

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

								tokenizations.remove(tokenizations.size() - 1);
								if (tokenizations.size() > 0) {
									tokenizations
											.remove(tokenizations.size() - 1);
								}

								blabla.deleteCharAt(blabla.length() - 1);
								if (blabla.length() > 0) {
									blabla.deleteCharAt(blabla.length() - 1);
								}

								removeLastCharacterIfPresent(previousTok);

								if (updatedChar != null) {
									blabla.append(updatedChar);
									previousTok.setText(previousTok.getText()
											+ updatedChar);
								}

								blabla.append(tok.substring(1, tok.length()));
								previousTok.setText(previousTok.getText()
										+ tok.substring(1, tok.length()));
								tokenizations.add(previousTok.getText());

								diaresis = (modifierClass == ModifierClass.DIAERESIS
										|| modifierClass == ModifierClass.NORDIC_RING
										|| modifierClass == ModifierClass.CZECH_CARON
										|| modifierClass == ModifierClass.TILDE || modifierClass == ModifierClass.CEDILLA);

								accent = (modifierClass == ModifierClass.ACUTE_ACCENT
										|| modifierClass == ModifierClass.CIRCUMFLEX || modifierClass == ModifierClass.GRAVE_ACCENT);

								if (rightClass != ModifierClass.NOT_A_MODIFIER) {
									tok = ""; // resetting current token as it
												// is a single-item
								}
							}

						}

						if (tok != null) {
							tok = tok.replace(" ", "");
						}

						if ((!diaresis) && (!accent)) {
							// blabla.append(" ");
							blabla.append(tok);
							token.setText(tok);

							tokenizations.add(tok);
						} else {
							tok = "";
							keepLast = true;
						}

						/*
						 * StringTokenizer st0 = new StringTokenizer(tok0,
						 * TextUtilities.fullPunctuations, true);
						 * while(st0.hasMoreTokens()) { String tok =
						 * st0.nextToken(); tokenizations.add(tok); }
						 * tokenizations.add(" ");
						 */

						/*
						 * boolean punct1 = false; boolean punct2 = false;
						 * boolean punct3 = false; String content = null; int i
						 * = 0; for(; i<TextUtilities.punctuations.length();
						 * i++) { if (tok.length() > 0) { if
						 * (tok.charAt(tok.length()-1) ==
						 * TextUtilities.punctuations.charAt(i)) { punct1 =
						 * true; content = tok.substring(0, tok.length()-1); if
						 * (tok.length() > 1) { int j = 0; for(;
						 * j<TextUtilities.punctuations.length(); j++) { if
						 * (tok.charAt(tok.length()-2) ==
						 * TextUtilities.punctuations.charAt(j)) { punct3 =
						 * true; content = tok.substring(0, tok.length()-2); } }
						 * } break; } } } if (tok.length() > 0) { if (
						 * (tok.startsWith("(")) && (tok.length() > 1) ) { if
						 * ((punct3) && (tok.length() > 2)) content =
						 * tok.substring(1, tok.length()-2); else if (punct1)
						 * content = tok.substring(1, tok.length()-1); else
						 * content = tok.substring(1, tok.length()); punct2 =
						 * true; token.setText("("); } else if (
						 * (tok.startsWith("[")) && (tok.length() > 1) ) { if
						 * ((punct3) && (tok.length() > 2)) content =
						 * tok.substring(1, tok.length()-2); else if (punct1)
						 * content = tok.substring(1, tok.length()-1); else
						 * content = tok.substring(1, tok.length()); punct2 =
						 * true; token.setText("["); } else if (
						 * (tok.startsWith("\"")) && (tok.length() > 1) ) { if
						 * ((punct3) && (tok.length() > 2)) content =
						 * tok.substring(1, tok.length()-2); else if (punct1)
						 * content = tok.substring(1, tok.length()-1); else
						 * content = tok.substring(1, tok.length()); punct2 =
						 * true; token.setText("\""); } }
						 */
						if (currentRotation)
							currentFontSize = currentFontSize / 2;

						/*
						 * if (punct2) { if (currentFont != null)
						 * token.setFont(currentFont.toLowerCase()); else
						 * token.setFont("default");
						 * token.setItalic(currentItalic);
						 * token.setBold(currentBold);
						 * token.setRotation(currentRotation);
						 * token.setColorFont(colorFont); token.setX(currentX);
						 * token.setY(currentY); token.setWidth(currentWidth);
						 * token.setHeight(currentHeight);
						 * token.setFontSize(currentFontSize);
						 * block.addToken(token);
						 * 
						 * token = new LayoutToken(); token.setText(content); }
						 * if (punct1) { token.setText(content); if (currentFont
						 * != null) token.setFont(currentFont.toLowerCase());
						 * else token.setFont("default");
						 * token.setItalic(currentItalic);
						 * token.setBold(currentBold);
						 * token.setRotation(currentRotation);
						 * token.setColorFont(colorFont); token.setX(currentX);
						 * token.setY(currentY); token.setWidth(currentWidth);
						 * token.setHeight(currentHeight);
						 * token.setFontSize(currentFontSize);
						 * block.addToken(token);
						 * 
						 * if (punct3) { token = new LayoutToken();
						 * token.setText(""+tok.charAt(tok.length()-2)); if
						 * (currentFont != null)
						 * token.setFont(currentFont.toLowerCase()); else
						 * token.setFont("default");
						 * token.setItalic(currentItalic);
						 * token.setBold(currentBold);
						 * token.setRotation(currentRotation);
						 * token.setColorFont(colorFont); token.setX(currentX);
						 * token.setY(currentY); token.setWidth(currentWidth);
						 * token.setHeight(currentHeight);
						 * token.setFontSize(currentFontSize);
						 * block.addToken(token); }
						 * 
						 * token = new LayoutToken();
						 * token.setText(""+tok.charAt(tok.length()-1)); }
						 */
						if (currentFont != null)
							token.setFont(currentFont.toLowerCase());
						else
							token.setFont("default");
						token.setItalic(currentItalic);
						token.setBold(currentBold);
						token.setRotation(currentRotation);
						token.setColorFont(colorFont);
						token.setX(currentX);
						token.setY(currentY);
						token.setWidth(currentWidth);
						token.setHeight(currentHeight);
						token.setFontSize(currentFontSize);

						if (!diaresis && !accent) {
							block.addToken(token);
						}

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
						if (block.getX() == 0.0)
							block.setX(currentX);
						if (block.getY() == 0.0)
							block.setY(currentY);
						if (block.getWidth() == 0.0)
							block.setWidth(currentWidth);
						if (block.getHeight() == 0.0)
							block.setHeight(currentHeight);
						if (block.getFontSize() == 0.0)
							block.setFontSize(currentFontSize);

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
				if (tokenizations.size() > 0) {
					String justBefore = tokenizations
							.get(tokenizations.size() - 1);
					if (!justBefore.endsWith("-")) {
						tokenizations.add(" ");
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
				tokenizations.add("\n");
				block.setText(blabla.toString());
				block.setNbTokens(nbTokens);
				doc.addBlock(block);
			}
			Block block0 = new Block();
			block0.setText("@PAGE\n");
			block0.setNbTokens(0);
			block0.setPage(currentPage);
			doc.addBlock(block0);
			block = new Block();
			block.setPage(currentPage);
			blabla = new StringBuffer();
			nbTokens = 0;
			// blabla.append("\n@block\n");
			tokenizations.add("\n");
		} else if (qName.equals("IMAGE")) {
			if (block != null) {
				blabla.append("\n");
				block.setText(blabla.toString());
				block.setNbTokens(nbTokens);
				doc.addBlock(block);
			}
			block = new Block();
			block.setPage(currentPage);
			blabla = new StringBuffer();
			if (images.size() > 0) {
				blabla.append("@IMAGE " + images.get(images.size() - 1) + "\n");
			}
			block.setText(blabla.toString());
			block.setNbTokens(nbTokens);
			if (block.getX() == 0.0)
				block.setX(currentX);
			if (block.getY() == 0.0)
				block.setY(currentY);
			if (block.getWidth() == 0.0)
				block.setWidth(currentWidth);
			if (block.getHeight() == 0.0)
				block.setHeight(currentHeight);
			doc.addBlock(block);
			blabla = new StringBuffer();
			nbTokens = 0;
			block = new Block();
			block.setPage(currentPage);
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
			tokenizations.add("\n");
			block.setText(blabla.toString());
			block.setNbTokens(nbTokens);

			block.setWidth(currentX - block.getX() + currentWidth);
			block.setHeight(currentY - block.getY() + currentHeight);

			doc.addBlock(block);
			// blabla = new StringBuffer();
			nbTokens = 0;
			block = null;
		} else if (qName.equals("xi:include")) {
			if (block != null) {
				blabla.append("\n");
				block.setText(blabla.toString());
				block.setNbTokens(nbTokens);
				doc.addBlock(block);
			}
			block = new Block();
			block.setPage(currentPage);
			blabla = new StringBuffer();
			blabla.append("@IMAGE " + images.get(images.size() - 1) + "\n");
			block.setText(blabla.toString());
			block.setNbTokens(nbTokens);
			doc.addBlock(block);
			blabla = new StringBuffer();
			nbTokens = 0;
			block = new Block();
			block.setPage(currentPage);
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

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("id")) {
						;
					} else if (name.equals("number")) {

					} else if (name.equals("width")) {

					} else if (name.equals("height")) {

					}
				}
			}

			/*
			 * if (block != null) { blabla.append("\n");
			 * tokenizations.add("\n"); block.setText(blabla.toString());
			 * block.setNbTokens(nbTokens); doc.addBlock(block); } Block block0
			 * = new Block(); block0.setText("@PAGE\n"); block0.setNbTokens(0);
			 * doc.addBlock(block0);
			 */
			/*
			 * block = new Block(); blabla = new StringBuffer(); nbTokens = 0;
			 * //blabla.append("\n@block\n"); tokenizations.add("\n");
			 */
		} else if (qName.equals("BLOCK")) {
			block = new Block();
			blabla = new StringBuffer();
			nbTokens = 0;
			block.setPage(currentPage);
			// blabla.append("\n@block\n");
		} else if (qName.equals("IMAGE")) {
			int length = atts.getLength();

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("href")) {
						// if (images == null)
						// images = new ArrayList<String>();
						images.add(value);
					} else if (name.equals("x")) {
						double x = Double.parseDouble(value);
						if (x != currentX) {
							currentX = x;
						}
					} else if (name.equals("y")) {
						double y = Double.parseDouble(value);
						if (y != currentY) {
							currentY = y;
						}
					} else if (name.equals("width")) {
						double width = Double.parseDouble(value);
						if (width != currentWidth) {
							currentWidth = width;
						}
					} else if (name.equals("height")) {
						double height = Double.parseDouble(value);
						if (height != currentHeight) {
							currentHeight = height;
						}
					}
				}
			}

		} else if (qName.equals("TEXT")) {
			int length = atts.getLength();

			// Process each attribute
			for (int i = 0; i < length; i++) {
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
			}
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
							currentX = x;
						}
					} else if (name.equals("y")) {
						double y = Double.parseDouble(value);
						if (y != currentY) {
							currentY = y;
						}
					} else if (name.equals("base")) {
						double base = Double.parseDouble(value);

					} else if (name.equals("width")) {
						double width = Double.parseDouble(value);
						if (width != currentWidth) {
							currentWidth = width;
						}
					} else if (name.equals("height")) {
						double height = Double.parseDouble(value);
						if (height != currentHeight) {
							currentHeight = height;
						}
					}

				}
			}
		} else if (qName.equals("xi:include")) {
			int length = atts.getLength();

			// Process each attribute
			for (int i = 0; i < length; i++) {
				// Get names and values for each attribute
				String name = atts.getQName(i);
				String value = atts.getValue(i);

				if ((name != null) && (value != null)) {
					if (name.equals("href")) {
						// if (images == null)
						// images = new ArrayList<String>();
						images.add(value);
					}
				}
			}

		}
		// accumulator.setLength(0);
	}

}
