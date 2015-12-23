package org.grobid.core.features;

import java.util.List;
import java.util.regex.Matcher;
import org.grobid.core.layout.LayoutToken;

/**
 * Class for features used for header parsing.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorReferenceSegmenter {
    // default bins for relative position, set experimentally
	public LayoutToken token = null; // not a feature, reference value
	
    public String string = null; // lexical feature
    public String label = null; // label if known
    public String blockStatus = null; // one of BLOCKSTART, BLOCKIN, BLOCKEND
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
	public String alignmentStatus = null; // one of ALIGNEDLEFT, INDENT, CENTERED, applied to the whole line
    public String fontStatus = null; // one of NEWFONT, SAMEFONT
    public String fontSize = null; // one of HIGHERFONT, SAMEFONTSIZE, LOWERFONT
    public boolean bold = false;
    public boolean italic = false;
    public boolean rotation = false;
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS
    public String digit;  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;
    public boolean containDash = false;
    public boolean properName = false;
    public boolean commonName = false;
    public boolean firstName = false;

    public boolean locationName = false;

    public boolean year = false;
    public boolean month = false;
    public boolean email = false;
    public boolean http = false;
    //public boolean acronym = false;
    public String punctType = null; // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT
    public boolean containPunct = false;
    public int relativePosition = -1;
	public int lineLength = 0;
	public String punctuationProfile = null; // the punctuations of the current line of the token

    // true if the token is part of a predefinied name (single or multi-token)
    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuilder res = new StringBuilder();

        // token string (1)
        res.append(string);

        // lowercase string (1)
        res.append(" ").append(string.toLowerCase());

        // prefix (4)
        res.append(" ").append(string.substring(0, 1));

        if (string.length() > 1)
            res.append(" ").append(string.substring(0, 2));
        else
            res.append(" ").append(string.substring(0, 1));

        if (string.length() > 2)
            res.append(" ").append(string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" ").append(string.substring(0, 2));
        else
            res.append(" ").append(string.substring(0, 1));

        if (string.length() > 3)
            res.append(" ").append(string.substring(0, 4));
        else if (string.length() > 2)
            res.append(" ").append(string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" ").append(string.substring(0, 2));
        else
            res.append(" ").append(string.substring(0, 1));

        // suffix (4)
        res.append(" ").append(string.charAt(string.length() - 1));

        if (string.length() > 1)
            res.append(" ").append(string.substring(string.length() - 2, string.length()));
        else
            res.append(" ").append(string.charAt(string.length() - 1));

        if (string.length() > 2)
            res.append(" ").append(string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" ").append(string.substring(string.length() - 2, string.length()));
        else
            res.append(" ").append(string.charAt(string.length() - 1));

        if (string.length() > 3)
            res.append(" ").append(string.substring(string.length() - 4, string.length()));
        else if (string.length() > 2)
            res.append(" ").append(string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" ").append(string.substring(string.length() - 2, string.length()));
        else
            res.append(" ").append(string.charAt(string.length() - 1));

        // line information (1)
        res.append(" ").append(lineStatus);
		
		// line position/indentation (1)
		res.append(" " + alignmentStatus);

        // capitalisation (1)
        if (digit.equals("ALLDIGIT"))
            res.append(" NOCAPS");
        else
            res.append(" ").append(capitalisation);

        // digit information (1)
        res.append(" ").append(digit);

        // character information (1)
        if (singleChar)
            res.append(" 1");
        else
            res.append(" 0");

        // lexical information (8)
        if (properName)
            res.append(" 1");
        else
            res.append(" 0");

        if (commonName)
            res.append(" 1");
        else
            res.append(" 0");

        if (firstName)
            res.append(" 1");
        else
            res.append(" 0");

        if (locationName)
            res.append(" 1");
        else
            res.append(" 0");

        if (year)
            res.append(" 1");
        else
            res.append(" 0");

        if (month)
            res.append(" 1");
        else
            res.append(" 0");

        /*if (email)
            res.append(" 1");
        else
            res.append(" 0");
		*/
        if (http)
            res.append(" 1");
        else
            res.append(" 0");

        // punctuation information (1)
        res.append(" ").append(punctType); // in case the token is a punctuation (NO otherwise)

        // relative length on the line as compared to the max line length on a predefined scale (1)
        res.append(" ").append(relativePosition);

		// relative position in the line on a predefined scale (1)
		res.append(" " + lineLength);

        // block information (1)
		//if (blockStatus != null)
		res.append(" " + blockStatus);

		// punctuation profile
		if ( (punctuationProfile == null) || (punctuationProfile.length() == 0) )
			res.append(" no");
		else {
			int theLength = punctuationProfile.length();
			if (theLength > 10) 
				theLength = 10;
			res.append(" " + theLength);
		}
        // label - for training data (1)
        if (label != null)
            res.append(" ").append(label).append("\n");
        else
            res.append(" 0\n");

        return res.toString();
    }
}
