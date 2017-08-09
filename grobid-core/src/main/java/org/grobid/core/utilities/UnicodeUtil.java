package org.grobid.core.utilities;

/**
 * Class for holding static methods for processing related to unicode.
 *
 * @author Patrice Lopez
 */
public class UnicodeUtil {

	// As java \s doesn’t support the Unicode white space property (\s matches
	// [ \t\n\x0B\f\r]), here are the 26 code points of the "official" stable
	// p{White_Space} unicode property
	public static String whitespace_chars = "["
										+ "\\u0009" // CHARACTER TABULATION, \t
			                        	+ "\\u000A"  // LINE FEED (LF), \n -> new line
			                        	+ "\\u000B"  // LINE TABULATION, \v -> new line
			                        	+ "\\u000C"  // FORM FEED (FF) -> break page
			                        	+ "\\u000D"  // CARRIAGE RETURN (CR), \r
			                        	+ "\\u0020"  // SPACE
				                        + "\\u0085"  // NEXT LINE (NEL) -> new line
				                        + "\\u00A0"  // NO-BREAK SPACE
				                        + "\\u1680"  // OGHAM SPACE MARK
				                        + "\\u180E"  // MONGOLIAN VOWEL SEPARATOR
				                        + "\\u2000"  // EN QUAD
				                        + "\\u2001"  // EM QUAD
				                        + "\\u2002"  // EN SPACE
				                        + "\\u2003"  // EM SPACE
				                        + "\\u2004"  // THREE-PER-EM SPACE
				                        + "\\u2005"  // FOUR-PER-EM SPACE
				                        + "\\u2006"  // SIX-PER-EM SPACE
				                        + "\\u2007"  // FIGURE SPACE
				                        + "\\u2008"  // PUNCTUATION SPACE
				                        + "\\u2009"  // THIN SPACE
				                        + "\\u200A"  // HAIR SPACE
				                        + "\\u2028"  // LINE SEPARATOR
				                        + "\\u2029"  // PARAGRAPH SEPARATOR
				                        + "\\u202F"  // NARROW NO-BREAK SPACE
				                        + "\\u205F"  // MEDIUM MATHEMATICAL SPACE
				                        + "\\u3000"  // IDEOGRAPHIC SPACE
				                        + "]";

	// a more restrictive selection of horizontal white space characters than the
	// Unicode p{White_Space} property (which includes new line and vertical spaces)
	public static String my_whitespace_chars = "["
										+"\\u0009"   // CHARACTER TABULATION, \t
			                        	+ "\\u0020"  // SPACE
				                        + "\\u00A0"  // NO-BREAK SPACE
				                        + "\\u1680"  // OGHAM SPACE MARK
				                        + "\\u180E"  // MONGOLIAN VOWEL SEPARATOR
				                        + "\\u2000"  // EN QUAD
				                        + "\\u2001"  // EM QUAD
				                        + "\\u2002"  // EN SPACE
				                        + "\\u2003"  // EM SPACE
				                        + "\\u2004"  // THREE-PER-EM SPACE
				                        + "\\u2005"  // FOUR-PER-EM SPACE
				                        + "\\u2006"  // SIX-PER-EM SPACE
				                        + "\\u2007"  // FIGURE SPACE
				                        + "\\u2008"  // PUNCTUATION SPACE
				                        + "\\u2009"  // THIN SPACE
				                        + "\\u200A"  // HAIR SPACE
				                        + "\\u2028"  // LINE SEPARATOR
				                        + "\\u2029"  // PARAGRAPH SEPARATOR
				                        + "\\u202F"  // NARROW NO-BREAK SPACE
				                        + "\\u205F"  // MEDIUM MATHEMATICAL SPACE
				                        + "\\u3000"  // IDEOGRAPHIC SPACE
				                        + "]";

    // all the horizontal low lines
    public static String horizontal_low_lines_chars = "["
    											  + "\\u005F" // low Line
			    								  + "\\u203F" // undertie
			    								  + "\\u2040" // character tie
			    								  + "\\u2054" // inverted undertie
			    								  + "\\uFE4D" // dashed low line
			    								  + "\\uFE4E" // centreline low line
			    								  + "\\uFE4F" // wavy low line
			    								  + "\\uFF3F" // fullwidth low line
			    								  + "\\uFE33" // Presentation Form For Vertical Low Line
			    								  + "\\uFE34" // Presentation Form For Vertical Wavy Low Line
			    								  + "]";
    // all the vertical lines
    public static String vertical_lines_chars = "["
    										+ "\\u007C" 	// vertical line
			    							+ "\\u01C0" 	// Latin Letter Dental
			    							+ "\\u05C0" 	// Hebrew Punctuation Paseq
			    							+ "\\u2223" 	// Divides
			    							+ "\\u2758"  	// Light Vertical Bar
			    							+ "]";

    // all new lines / "vertical" white spaces
    public static String new_line_chars = "["
    									 + "\\u000C"  // form feed, \f - normally a page break
    									 + "\\u000A"  // line feed, \n
    									 + "\\u000D"  // carriage return, \r
    									 + "\\u000B"  // line tabulation, \v - concretely it's a new line
    									 + "\\u0085"  // next line (NEL)
    									 + "\\u2029"  // PARAGRAPH SEPARATOR, \p{Zp}
    									 + "\\u2028"  // LINE SEPARATOR, \p{Zl}
    									 + "]";

    // all bullets
    public static String bullet_chars = "["
    									+ "\\u2022"  // bullet
 									    + "\\u2023"  // triangular bullet
    									+ "\\u25E6"  // white bullet
										+ "\\u2043"  // hyphen bullet
										+ "\\u204C"  // black leftwards bullet
										+ "\\u204D"  // black rightwards bullet
										+ "\\u2219"  // bullet operator (use in math stuff)
										+ "\\u25D8"  // inverse bullet
										+ "\\u29BE"  // circled white bullet
										+ "\\u29BF"  // circled bullet
										+ "\\u23FA"  // black circle for record
										+ "\\u25CF"  // black circle
										+ "\\u26AB"  // medium black circle
										+ "\\u2B24"  // black large circle
										+ "]";

	/**
     * Normalise the space, EOL and punctuation unicode characters.
     *
     * In particular all the characters which are treated as space in
     * C++ (http://en.cppreference.com/w/cpp/string/byte/isspace)
     * will be replace by the punctuation space character U+2008
     * so that the token can be used to generate a robust feature vector
     * legible as Wapiti input.
     *
     * @param text to be normalised
     * @return normalised string, legible for Wapiti feature generation
     */
    public static String normaliseText(String text) {
        if (text == null)
            return null;

        // see https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html
        // for Unicode character properties supported by Java

        // normalise all horizontal space separator characters 
        text = text.replaceAll(my_whitespace_chars, " ");

        // normalise all EOL - special handling of "\r\n" as one single newline
        text = text.replace("\r\n", "\n").replaceAll(new_line_chars, "\n");

        // normalize dash via the unicode dash punctuation property
        // note: we don't add the "hyphen bullet" character \\u2043 because it's actually a bullet
        text = text.replaceAll("\\p{Pd}", "-");

        // normalize horizontal low lines
        text = text.replaceAll(horizontal_low_lines_chars, "_");

        // normalize vertical lines
        text = text.replaceAll(vertical_lines_chars, "|");

        // bullet normalisation
        text = text.replaceAll(bullet_chars, "•");

        // remove all control charcaters?
        //text = text.replaceAll("\\p{Cntrl}", " ");

        return text;
    }

    /**
     * Unicode normalisation of the token text.
     * Works as the {@link org.grobid.core.utilities.UnicodeUtil#normaliseText(java.lang.String)}, but in addition also removes spaces
     * @param text to be normalised
     * @return normalised string, legible for Wapiti feature generation
     */
    public static String normaliseTextAndRemoveSpaces(String text) {
        // parano sanitising
        return normaliseText(text).replaceAll("[ \n]", "");
    }
}