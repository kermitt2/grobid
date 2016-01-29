package org.grobid.core.utilities;

import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.lexicon.Lexicon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.*;

/**
 * Class for holding static methods for text processing.
 *
 * @author Patrice Lopez
 */
public class TextUtilities {

	public static final String punctuations = " ,:;?.!)-–\"“”‘’'`$]*\u2666\u2665\u2663\u2660";
    public static final String fullPunctuations = "([ ,:;?.!/)-–\"“”‘’'`$]*\u2666\u2665\u2663\u2660";
    public static String delimiters = " \n\t\u00A0" + fullPunctuations;

	public static final String OR = "|";
	public static final String NEW_LINE = "\n";
    public static final String SPACE = " ";
    public static final String COMMA = ",";
    public static final String QUOTE = "'";
    public static final String END_BRACKET = ")";
    public static final String START_BRACKET = "(";
    public static final String SHARP = "#";
    public static final String COLON = ":";
    public static final String DOUBLE_QUOTE="\"";
    public static final String ESC_DOUBLE_QUOTE="&quot;";
	public static final String LESS_THAN = "<";
	public static final String ESC_LESS_THAN = "&lt;";
	public static final String GREATER_THAN = ">";
	public static final String ESC_GREATER_THAN = "&gt;";
	public static final String AND = "&";
	public static final String ESC_AND = "&amp;";
	public static final String SLASH = "/";

    /**
     * Replace numbers in the string by a dummy character for string distance evaluations
     *
     * @param string the string to be processed.
     * @return Returns the string with numbers replaced by 'X'.
     */
    public static String shadowNumbers(String string) {
        int i = 0;
        if (string == null)
            return string;
        String res = "";
        while (i < string.length()) {
            char c = string.charAt(i);
            if (Character.isDigit(c))
                res += 'X';
            else
                res += c;
            i++;
        }
        return res;
    }

    /**
     * Test for the current string contains at least one digit.
     *
     * @param tok the string to be processed.
     * @return true if contains a digit
     */
    public static boolean test_digit(String tok) {
        if (tok == null)
            return false;
        if (tok.length() == 0)
            return false;
        char a;
        for (int i = 0; i < tok.length(); i++) {
            a = tok.charAt(i);
            if (Character.isDigit(a))
                return true;
        }
        return false;
    }

    /**
     * Text extracted from a PDF is usually hyphenized, which is not desirable. This 
	 * version supposes that the ends of line are preserved in the input text. 
     *
     * @param text the string to be processed with preserved end of lines.
     * @return Returns the dehyphenized string.
     */
    public static String dehyphenize(String text) {
        if (text == null)
            return null;
        String res = "";
        StringTokenizer st = new StringTokenizer(text, "\n");
        boolean hyphen = false;
        boolean failure = false;
        String lastToken = null;
        boolean isFirstToken = true;
        String line = null;
        while (st.hasMoreTokens()) {
            if (line != null) {
                isFirstToken = false;
            }
            line = st.nextToken();

            if (hyphen) {
                // we get the first token
                StringTokenizer st2 = new StringTokenizer(line, " ,.);!");
                if (st2.countTokens() > 1) {
                    String firstToken = st2.nextToken();

                    // we check if the composed token is in the lexicon
                    String hyphenToken = lastToken + firstToken;
                    boolean dehyph = true;
                    // if number, we do not dehyphenize!
                    if (test_digit(hyphenToken))
                        dehyph = false;
                    else if (Character.isUpperCase(firstToken.charAt(0))) {
                        // if capital letter at the begining of the first token, we do not dehyphenize!
                        dehyph = false;
                    }

                    //if (lex.inDictionary(hyphenToken.toLowerCase())) {
                    if (dehyph) {
                        // if yes, it is hyphenization
                        res += hyphenToken;
                        line = line.substring(firstToken.length(), line.length());
                    } else {
                        // if not
                        res += lastToken + "-\n";
                        failure = true;
                    }
                } else
                    res += lastToken + "-\n";
                hyphen = false;
            }

            if (line.length() > 0) {
                if (line.charAt(line.length() - 1) == '-') {
                    // we get the last token
                    hyphen = true;
                    int ind0 = line.lastIndexOf(' ');
                    int ind1 = line.lastIndexOf('(');
                    if (ind1 > ind0)
                        ind0 = ind1;
                    if (ind0 != -1) {
                        lastToken = line.substring(ind0 + 1, line.length() - 1);
                        line = line.substring(0, ind0 + 1);
                        res += SPACE + line;
                    } else
                        lastToken = line.substring(0, line.length() - 1);
                } else {
                    if (failure) {
                        res += line + (st.hasMoreTokens() ? "\n" : "");
                        failure = false;
                    } else
                        res += (isFirstToken ? "" : SPACE) + line + (st.hasMoreTokens() ? "\n" : "");
                }
            }
        }
        res = res.replace("  ", SPACE);
        return res;
    }


    /**
     * Text extracted from a PDF is usually hyphenized, which is not desirable.
     * This version supposes that the end of line are lost and than hyphenation
     * could appear everywhere. So a dictionary is used to control the recognition
     * of hyphen. 
     *
     * @param text the string to be processed without preserved end of lines.
     * @return Returns the dehyphenized string.
     */
    public static String dehyphenizeHard(String text) {
        if (text == null)
            return null;
        String res = "";


        text.replaceAll("\n", SPACE);

        StringTokenizer st = new StringTokenizer(text, "-");
        boolean hyphen = false;
        boolean failure = false;
        String lastToken = null;
        while (st.hasMoreTokens()) {
            String section = st.nextToken().trim();

            if (hyphen) {
                // we get the first token
                StringTokenizer st2 = new StringTokenizer(section, " ,.);!");
                if (st2.countTokens() > 0) {
                    String firstToken = st2.nextToken();

                    // we check if the composed token is in the lexicon
                    String hyphenToken = lastToken + firstToken;
                    //System.out.println(hyphenToken);
                    /*if (lex == null)
                             featureFactory.loadLexicon();*/
                    Lexicon lex = Lexicon.getInstance();
                    FeatureFactory featureFactory = FeatureFactory.getInstance();
                    if (lex.inDictionary(hyphenToken.toLowerCase()) &
                            !(featureFactory.test_digit(hyphenToken))) {
                        // if yes, it is hyphenization
                        res += firstToken;
                        section = section.substring(firstToken.length(), section.length());
                    } else {
                        // if not
                        res += "-";
                        failure = true;
                    }
                } else {
                    res += "-";
                }
                hyphen = false;
            }

            // we get the last token
            hyphen = true;
            int ind0 = section.lastIndexOf(' ');
            int ind1 = section.lastIndexOf('(');
            if (ind1 > ind0)
                ind0 = ind1;
            if (ind0 != -1) {
                lastToken = section.substring(ind0 + 1, section.length());
            } else
                lastToken = section.substring(0, section.length());

            if (failure) {
                res += section;
                failure = false;
            } else
                res += SPACE + section;
        }

        res = res.replace(" . ", ". ");
        res = res.replace("  ", SPACE);

        return res.trim();
    }

    /**
     * Levenstein distance between two strings
     *
     * @param s the first string to be compared.
     * @param t the second string to be compared.
     * @return Returns the Levenshtein distance.
     */
    public static int getLevenshteinDistance(String s, String t) {
        //if (s == null || t == null) {
        //	throw new IllegalArgumentException("Strings must not be null");
        //}
        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n + 1]; //'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }

    /**
     * Appending nb times the char c to the a StringBuffer...
     */
    public final static void appendN(StringBuffer buffer, char c, int nb) {
        for (int i = 0; i < nb; i++) {
            buffer.append(c);
        }
    }

    /**
     * To replace accented characters in a unicode string by unaccented equivalents:
     * é -> e, ü -> ue, ß -> ss, etc. following the standard transcription conventions
     *
     * @param input the string to be processed.
     * @return Returns the string without accent.
     */
    public final static String removeAccents(String input) {
        if (input == null)
            return null;
        final StringBuffer output = new StringBuffer();
        for (int i = 0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case '\u00C0': // Ã€
                case '\u00C1': // Ã
                case '\u00C2': // Ã‚
                case '\u00C3': // Ãƒ
                case '\u00C5': // Ã…
                    output.append("A");
                    break;
                case '\u00C4': // Ã„
                case '\u00C6': // Ã†
                    output.append("AE");
                    break;
                case '\u00C7': // Ã‡
                    output.append("C");
                    break;
                case '\u00C8': // Ãˆ
                case '\u00C9': // Ã‰
                case '\u00CA': // ÃŠ
                case '\u00CB': // Ã‹
                    output.append("E");
                    break;
                case '\u00CC': // ÃŒ
                case '\u00CD': // Ã
                case '\u00CE': // ÃŽ
                case '\u00CF': // Ã
                    output.append("I");
                    break;
                case '\u00D0': // Ã
                    output.append("D");
                    break;
                case '\u00D1': // Ã‘
                    output.append("N");
                    break;
                case '\u00D2': // Ã’
                case '\u00D3': // Ã“
                case '\u00D4': // Ã”
                case '\u00D5': // Ã•
                case '\u00D8': // Ã˜
                    output.append("O");
                    break;
                case '\u00D6': // Ã–
                case '\u0152': // Å’
                    output.append("OE");
                    break;
                case '\u00DE': // Ãž
                    output.append("TH");
                    break;
                case '\u00D9': // Ã™
                case '\u00DA': // Ãš
                case '\u00DB': // Ã›
                    output.append("U");
                    break;
                case '\u00DC': // Ãœ
                    output.append("UE");
                    break;
                case '\u00DD': // Ã
                case '\u0178': // Å¸
                    output.append("Y");
                    break;
                case '\u00E0': // Ã
                case '\u00E1': // Ã¡
                case '\u00E2': // Ã¢
                case '\u00E3': // Ã£
                case '\u00E5': // Ã¥
                    output.append("a");
                    break;
                case '\u00E4': // Ã¤
                case '\u00E6': // Ã¦
                    output.append("ae");
                    break;
                case '\u00E7': // Ã§
                    output.append("c");
                    break;
                case '\u00E8': // Ã¨
                case '\u00E9': // Ã©
                case '\u00EA': // Ãª
                case '\u00EB': // Ã«
                    output.append("e");
                    break;
                case '\u00EC': // Ã¬
                case '\u00ED': // Ã
                case '\u00EE': // Ã®
                case '\u00EF': // Ã¯
                    output.append("i");
                    break;
                case '\u00F0': // Ã°
                    output.append("d");
                    break;
                case '\u00F1': // Ã±
                    output.append("n");
                    break;
                case '\u00F2': // Ã²
                case '\u00F3': // Ã³
                case '\u00F4': // Ã´
                case '\u00F5': // Ãµ
                case '\u00F8': // Ã¸
                    output.append("o");
                    break;
                case '\u00F6': // Ã¶
                case '\u0153': // Å“
                    output.append("oe");
                    break;
                case '\u00DF': // ÃŸ
                    output.append("ss");
                    break;
                case '\u00FE': // Ã¾
                    output.append("th");
                    break;
                case '\u00F9': // Ã¹
                case '\u00FA': // Ãº
                case '\u00FB': // Ã»
                    output.append("u");
                    break;
                case '\u00FC': // Ã¼
                    output.append("ue");
                    break;
                case '\u00FD': // Ã½
                case '\u00FF': // Ã¿
                    output.append("y");
                    break;
                default:
                    output.append(input.charAt(i));
                    break;
            }
        }
        return output.toString();
    }

    // ad hoc stopword list for the cleanField method
    public final static List<String> stopwords =
            Arrays.asList("the", "of", "and", "du", "de le", "de la", "des", "der", "an", "und");

    /**
     * Remove useless punctuation at the end and begining of a metadata field.
     * <p/>
     * Still experimental ! Use with care !
     */
    public final static String cleanField(String input0, boolean applyStopwordsFilter) {
        if (input0 == null) {
            return null;
        }
        if (input0.length() == 0) {
            return null;
        }
        String input = input0.replace(",,", ",");
        input = input.replace(", ,", ",");
        int n = input.length();

        // characters at the end
        for (int i = input.length() - 1; i > 0; i--) {
            char c = input.charAt(i);
            if ((c == ',') ||
                    (c == ' ') ||
                    (c == '.') ||
                    (c == '-') ||
                    (c == '_') ||
                    (c == '/') ||
                    //(c == ')') ||
                    //(c == '(') ||
                    (c == ':')) {
                n = i;
            } else if (c == ';') {
                // we have to check if we have an html entity finishing
                if (i - 3 >= 0) {
                    char c0 = input.charAt(i - 3);
                    if (c0 == '&') {
                        break;
                    }
                }
                if (i - 4 >= 0) {
                    char c0 = input.charAt(i - 4);
                    if (c0 == '&') {
                        break;
                    }
                }
                if (i - 5 >= 0) {
                    char c0 = input.charAt(i - 5);
                    if (c0 == '&') {
                        break;
                    }
                }
                if (i - 6 >= 0) {
                    char c0 = input.charAt(i - 6);
                    if (c0 == '&') {
                        break;
                    }
                }
                n = i;
            } else break;
        }

        input = input.substring(0, n);

        // characters at the begining
        n = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == ',') ||
                    (c == ' ') ||
                    (c == '.') ||
                    (c == ';') ||
                    (c == '-') ||
                    (c == '_') ||
                    //(c == ')') ||
                    //(c == '(') ||
                    (c == ':')) {
                n = i;
            } else break;
        }

        input = input.substring(n, input.length()).trim();

        if ((input.endsWith(")")) && (input.startsWith("("))) {
            input = input.substring(1, input.length() - 1).trim();
        }

        if ((input.length() > 12) &&
                (input.endsWith("&quot;")) &&
                (input.startsWith("&quot;"))) {
            input = input.substring(6, input.length() - 6).trim();
        }

        if (applyStopwordsFilter) {
            boolean stop = false;
            while (!stop) {
                stop = true;
                for (String word : stopwords) {
                    if (input.endsWith(SPACE + word)) {
                        input = input.substring(0, input.length() - word.length()).trim();
                        stop = false;
                        break;
                    }
                }
            }
        }

        return input.trim();
    }

    /**
     * Segment piece of text following a list of segmentation characters.
     * "hello, world." -> [ "hello", ",", "world", "." ]
     *
     * @param input the string to be processed.
     * @param input the characters creating a segment (typically space and punctuations).
     * @return Returns the string without accent.
     */
    public final static List<String> segment(String input, String segments) {
        if (input == null)
            return null;
        ArrayList<String> result = new ArrayList<String>();
        String token = null;
        String seg = " \n\t";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int ind = seg.indexOf(c);
            if (ind != -1) {
                if (token != null) {
                    result.add(token);
                    token = null;
                }
            } else {
                int ind2 = segments.indexOf(c);
                if (ind2 == -1) {
                    if (token == null)
                        token = "" + c;
                    else
                        token += c;
                } else {
                    if (token != null) {
                        result.add(token);
                        token = null;
                    }
                    result.add("" + segments.charAt(ind2));
                }
            }
        }
        if (token != null)
            result.add(token);
        return result;
    }


    /**
     * Encode a string to be displayed in HTML
     * <p/>
     * If fullHTML encode, then all unicode characters above 7 bits are converted into
     * HTML entitites
     */
    public static String HTMLEncode(String string) {
        return HTMLEncode(string, false);
    }

    public static String HTMLEncode(String string, boolean fullHTML) {
        if (string == null)
            return null;
        if (string.length() == 0)
            return string;
        string = string.replace("@BULLET", "•");
        StringBuffer sb = new StringBuffer(string.length());
        // true if last char was blank
        boolean lastWasBlankChar = false;
        int len = string.length();
        char c;

        for (int i = 0; i < len; i++) {
            c = string.charAt(i);
            if (c == ' ') {
                // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss
                // word breaking
                if (lastWasBlankChar) {
                    lastWasBlankChar = false;
                    //sb.append("&nbsp;");
                } else {
                    lastWasBlankChar = true;
                    sb.append(' ');
                }
            } else {
                lastWasBlankChar = false;
                //
                // HTML Special Chars
                if (c == '"')
                    sb.append("&quot;");
                else if (c == '\'')
                    sb.append("&apos;");
                else if (c == '&') {
                    boolean skip = false;
                    // we don't want to recode an existing hmlt entity
                    if (string.length() > i + 3) {
                        char c2 = string.charAt(i + 1);
                        char c3 = string.charAt(i + 2);
                        char c4 = string.charAt(i + 3);
                        if (c2 == 'a') {
                            if (c3 == 'm') {
                                if (c4 == 'p') {
                                    if (string.length() > i + 4) {
                                        char c5 = string.charAt(i + 4);
                                        if (c5 == ';') {
                                            skip = true;
                                        }
                                    }
                                }
                            }
                        } else if (c2 == 'q') {
                            if (c3 == 'u') {
                                if (c4 == 'o') {
                                    if (string.length() > i + 5) {
                                        char c5 = string.charAt(i + 4);
                                        char c6 = string.charAt(i + 5);
                                        if (c5 == 't') {
                                            if (c6 == ';') {
                                                    skip = true;
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (c2 == 'l' || c2 == 'g') {
                            if (c3 == 't') {
                                if (c4 == ';') {
                                    skip = true;
                                }
                            }
                        }
                    }
                    if (!skip) {
                        sb.append("&amp;");
                    } else {
                        sb.append("&");
                    }
                } else if (c == '<')
                    sb.append("&lt;");
                else if (c == '>')
                    sb.append("&gt;");
                    /*else if (c == '\n') {
                         // warning: this can be too much html!
                         sb.append("&lt;br/&gt;");
                     }*/
                else {
                    int ci = 0xffff & c;
                    if (ci < 160) {
                        // nothing special only 7 Bit
                        sb.append(c);
                    } else {
                        if (fullHTML) {
                            // Not 7 Bit use the unicode system
                            sb.append("&#");
                            sb.append(new Integer(ci).toString());
                            sb.append(';');
                        } else
                            sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String normalizeRegex(String string) {
        string = string.replace("&", "\\\\&");
        string = string.replace("+", "\\\\+");
        return string;
    }

    /*
      * To convert the InputStream to String we use the BufferedReader.readLine()
      * method. We iterate until the BufferedReader return null which means
      * there's no more data to read. Each line will appended to a StringBuilder
      * and returned as String.
      */
    static public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
//			e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
//				e.printStackTrace();
                throw new GrobidException("An exception occured while running Grobid.", e);
            }
        }

        return sb.toString();
    }

    /**
     * Count the number of digit in a given string.
     *
     * @param text the string to be processed.
     * @return Returns the number of digit chracaters in the string...
     */
    static public int countDigit(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c))
                count++;
        }
        return count;
    }

    /**
     * Map special ligature and characters coming from the pdf
     */
    static public String clean(String token) {
        if (token == null)
            return null;
        if (token.length() == 0)
            return token;
        String res = "";
        int i = 0;
        while (i < token.length()) {
            switch (token.charAt(i)) {
                // ligature
                case '\uFB00': {
                    res += "ff";
                    break;
                }
                case '\uFB01': {
                    res += "fi";
                    break;
                }
                case '\uFB02': {
                    res += "fl";
                    break;
                }
                case '\uFB03': {
                    res += "ffi";
                    break;
                }
                case '\uFB04': {
                    res += "ffl";
                    break;
                }
                case '\uFB06': {
                    res += "st";
                    break;
                }
                case '\uFB05': {
                    res += "ft";
                    break;
                }
                case '\u00E6': {
                    res += "ae";
                    break;
                }
                case '\u00C6': {
                    res += "AE";
                    break;
                }
                case '\u0153': {
                    res += "oe";
                    break;
                }
                case '\u0152': {
                    res += "OE";
                    break;
                }
                // quote
                case '\u201C': {
                    res += " \" ";
                    break;
                }
                case '\u201D': {
                    res += " \" ";
                    break;
                }
                case '\u201E': {
                    res += " \" ";
                    break;
                }
                case '\u201F': {
                    res += " \" ";
                    break;
                }
                case '\u2019': {
                    res += "'";
                    break;
                }
                case '\u2018': {
                    res += "'";
                    break;
                }
                // bullet
                case '\u2022': {
                    res += "@BULLET";
                    break;
                }
                case '\u2023': {
                    res += "@BULLET";
                    break;
                }
                case '\u2043': {
                    res += "@BULLET";
                    break;
                }
                case '\u204C': {
                    res += "@BULLET";
                    break;
                }
                case '\u204D': {
                    res += "@BULLET";
                    break;
                }
                case '\u2219': {
                    res += "@BULLET";
                    break;
                }
                case '\u25C9': {
                    res += "@BULLET";
                    break;
                }
                case '\u25D8': {
                    res += "@BULLET";
                    break;
                }
                case '\u25E6': {
                    res += "@BULLET";
                    break;
                }
                case '\u2619': {
                    res += "@BULLET";
                    break;
                }
                case '\u2765': {
                    res += "@BULLET";
                    break;
                }
                case '\u2767': {
                    res += "@BULLET";
                    break;
                }
                case '\u29BE': {
                    res += "@BULLET";
                    break;
                }
                case '\u29BF': {
                    res += "@BULLET";
                    break;
                }
                // asterix
                case '\u2217': {
                    res += " * ";
                    break;
                }
                // typical author/affiliation markers
                case '\u2020': {
                    res += SPACE + '\u2020';
                    break;
                }
                case '\u2021': {
                    res += SPACE + '\u2021';
                    break;
                }
                case '\u00A7': {
                    res += SPACE + '\u00A7';
                    break;
                }
                case '\u00B6': {
                    res += SPACE + '\u00B6';
                    break;
                }
                case '\u204B': {
                    res += SPACE + '\u204B';
                    break;
                }
                case '\u01C2': {
                    res += SPACE + '\u01C2';
                    break;
                }
                // default
                default: {
                    res += token.charAt(i);
                    break;
                }
            }
            i++;
        }
        return res;
    }

    public static String formatTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return twoDForm.format(d);
    }

    public static String formatFourDecimals(double d) {
        DecimalFormat fourDForm = new DecimalFormat("#.####");
        return fourDForm.format(d);
    }

    public static boolean isAllUpperCase(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isUpperCase(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllLowerCase(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isLowerCase(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static List<String> generateEmailVariants(String firstName, String lastName) {
        // current heuristics:
        // "First Last"
        // "First L"
        // "F Last"
        // "First"
        // "Last"
        // "Last First"
        // "Last F"

        List<String> variants = new ArrayList<String>();

        if (lastName != null) {
            variants.add(lastName);

            if (firstName != null) {
                variants.add(firstName + SPACE + lastName);
                variants.add(lastName + SPACE + firstName);

                if (firstName.length()>1) {
                    String firstInitial = firstName.substring(0,1);

                    variants.add(firstInitial + SPACE + lastName);
                    variants.add(lastName + SPACE + firstInitial);
                }

                if (lastName.length()>1) {
                    String lastInitial = lastName.substring(0,1);

                    variants.add(firstName + SPACE + lastInitial);
                }
            }
        } else {
            if (firstName != null) {
                variants.add(firstName);
            }
        }

        return variants;
    }

    /**
     * This is a re-implementation of the capitalizeFully of Apache commons lang, because it appears not working
     * properly.
     * <p/>
     * Convert a string so that each word is made up of a titlecase character and then a series of lowercase
     * characters. Words are defined as token delimited by one of the character in delimiters or the begining
     * of the string.
     */
    public static String capitalizeFully(String input, String delimiters) {
        if (input == null) {
            return null;
        }

        //input = input.toLowerCase();
        String output = "";
        boolean toUpper = true;
        for (int c = 0; c < input.length(); c++) {
            char ch = input.charAt(c);

            if (delimiters.indexOf(ch) != -1) {
                toUpper = true;
                output += ch;
            } else {
                if (toUpper == true) {
                    output += Character.toUpperCase(ch);
                    toUpper = false;
                } else {
                    output += Character.toLowerCase(ch);
                }
            }
        }
        return output;
    }

    public static String wordShape(String word) {
        StringBuilder shape = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (Character.isLetter(c)) {
                if (Character.isUpperCase(c)) {
                    shape.append("X");
                } else {
                    shape.append("x");
                }
            } else if (Character.isDigit(c)) {
                shape.append("d");
            } else {
                shape.append(c);
            }
        }

        StringBuilder finalShape = new StringBuilder().append(shape.charAt(0));

        String suffix = "";
        if (word.length() > 2) {
            suffix = shape.substring(shape.length() - 2);
        } else if (word.length() > 1) {
            suffix = shape.substring(shape.length() - 1);
        }

        StringBuilder middle = new StringBuilder();
        if (shape.length() > 3) {
            char ch = shape.charAt(1);
            for (int i = 1; i < shape.length() - 2; i++) {
                middle.append(ch);
                while (ch == shape.charAt(i) && i < shape.length() - 2) {
                    i++;
                }
                ch = shape.charAt(i);
            }

            if (ch != middle.charAt(middle.length() - 1)) {
                middle.append(ch);
            }
        }
        return finalShape.append(middle).append(suffix).toString();

    }

    public static String wordShapeTrimmed(String word) {
        StringBuilder shape = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (Character.isLetter(c)) {
                if (Character.isUpperCase(c)) {
                    shape.append("X");
                } else {
                    shape.append("x");
                }
            } else if (Character.isDigit(c)) {
                shape.append("d");
            } else {
                shape.append(c);
            }
        }

        StringBuilder middle = new StringBuilder();

        char ch = shape.charAt(0);
        for (int i = 0; i < shape.length(); i++) {
            middle.append(ch);
            while (ch == shape.charAt(i) && i < shape.length() - 1) {
                i++;
            }
            ch = shape.charAt(i);
        }

        if (ch != middle.charAt(middle.length() - 1)) {
            middle.append(ch);
        }


        return middle.toString();

    }
	
	/**
	 * Give the punctuation profile of a line, i.e. the concatenation of all the punctuations 
	 * occuring in the line. 
	 * 
	 * @param line
	 *            the string corresponding to a line
	 * @return the punctuation profile as a string, empty string is no punctuation
	 * @throws Exception
	 */
	public static String punctuationProfile(String line) {
		String profile = "";
		if ((line == null) || (line.length() == 0)) {
			return profile;
		}
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == ' ') {
				continue;
			}
			if (fullPunctuations.indexOf(c) != -1)
				profile += c;
		}
		return profile;
	}
	
	/**
	 * Return the number of token in a line given an existing global tokenization and a current 
	 * start position of the line in this global tokenization. 
	 * 
	 * @param line
	 *            the string corresponding to a line
 	 * @param currentLinePos
	 *            position of the line in the tokenization	
 	 * @param tokenization
	 *            the global tokenization where the line appears			
	 * @return the punctuation profile as a string, empty string is no punctuation
	 * @throws Exception
	 */
	public static int getNbTokens(String line, int currentLinePos, List<String> tokenization) 
	throws Exception {
		if ( (line == null) || (line.length() == 0) )
			return 0;
		String currentToken = tokenization.get(currentLinePos);
		while ( (currentLinePos <tokenization.size()) && 
				(currentToken.equals(" ") || currentToken.equals("\n") ) ) {
			currentLinePos++;
			currentToken = tokenization.get(currentLinePos);
		}
		if (!line.trim().startsWith(currentToken)) {
			System.out.println("out of sync. : " + currentToken);
			throw new IllegalArgumentException("line start does not match given tokenization start");
		}
		int nbTokens = 0;
		int posMatch = 0; // current position in line
		for(int p = currentLinePos; p < tokenization.size(); p++) {
			currentToken = tokenization.get(p);
			posMatch = line.indexOf(currentToken, posMatch);
			if (posMatch == -1)
				break;
			nbTokens++;
		}
		return nbTokens;
	}
	
	/**
	 * Ensure that special XML characters are correctly encoded.  
	 */
    public static String trimEncodedCharaters(String string) {
        return string.replaceAll("&amp\\s+;", "&amp;").
                replaceAll("&quot\\s+;|&amp;quot\\s*;", "&quot;").
                replaceAll("&lt\\s+;|&amp;lt\\s*;", "&lt;").
                replaceAll("&gt\\s+;|&amp;gt\\s*;", "&gt;").
                replaceAll("&apos\\s+;|&amp;apos\\s*;", "&apos;");
    }
	
    public static boolean filterLine(String line) {
        boolean filter = false;
		if ( (line == null) || (line.length() == 0) )
			filter = true;
        else if (line.contains("@IMAGE") || line.contains("@PAGE")) {
            filter = true;
        } else if (line.contains(".pbm") || line.contains(".ppm") || 
				   line.contains(".vec") || line.contains(".jpg") ||
				   line.contains(".png")) {
		    filter = true;
        }
        return filter;
    }
	
	/**
	 * The equivalent of String.replaceAll() for StringBuilder
	 */
	public static StringBuilder replaceAll(StringBuilder sb, String regex, String replacement) {
		Pattern pattern = Pattern.compile(regex);
	    Matcher m = pattern.matcher(sb);
	    int start = 0;
	    while (m.find(start)) {
	        sb.replace(m.start(), m.end(), replacement);
	        start = m.start() + replacement.length();
	    }
		return sb;
	}
}
