package org.grobid.core.features;

import org.grobid.core.layout.LayoutToken;

/**
 * Class for features used for high level segmentation of document.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorSegmentation {
    public LayoutToken token = null; // not a feature, reference value
	public String line = null; // not a feature, the complete processed line
	
    public String string = null; // first lexical feature
	public String secondString = null; // second lexical feature
    public String label = null; // label if known
    public String blockStatus = null; // one of BLOCKSTART, BLOCKIN, BLOCKEND
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
    public String fontStatus = null; // one of NEWFONT, SAMEFONT
    public String fontSize = null; // one of HIGHERFONT, SAMEFONTSIZE, LOWERFONT
    public String pageStatus = null; // one of PAGESTART, PAGEIN, PAGEEND
    public String alignmentStatus = null; // one of ALIGNEDLEFT, INDENT, CENTERED
    public boolean bold = false;
    public boolean italic = false;
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS
    public String digit;  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;
    public boolean properName = false;
    public boolean commonName = false;
    public boolean firstName = false;
    public boolean locationName = false;
    public boolean year = false;
    public boolean month = false;
    public boolean email = false;
    public boolean http = false;
    //public boolean acronym = false;
    public String punctType = null; // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)
    public int relativeDocumentPosition = -1;
    public int relativePagePosition = -1;
	public String punctuationProfile = null; // the punctuations of the current line of the token
	public boolean firstPageBlock = false; 
	public boolean lastPageBlock = false;
	public int lineLength = 0;

    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuffer res = new StringBuffer();

        // token string (1)
        res.append(string);
		
		// second token string
		if (secondString != null)
			res.append(" " + secondString);
		else
			res.append(" " + string);
		
        // lowercase string
        res.append(" " + string.toLowerCase());

        // prefix (4)
        res.append(" " + string.substring(0, 1));

        if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

        if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

        if (string.length() > 3)
            res.append(" " + string.substring(0, 4));
        else if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

		/*
        // suffix (4)
        res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 3)
            res.append(" " + string.substring(string.length() - 4, string.length()));
        else if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));
*/
        // block information (1)
		if (blockStatus != null)
			res.append(" " + blockStatus);
        //res.append(" 0");

        // line information (1)
		if (lineStatus != null)
			res.append(" " + lineStatus);

        // page information (1)
        res.append(" " + pageStatus);

        // alignmet/horizontal position information (1)
        //res.append(" " + alignmentStatus);

        // font information (1)
        res.append(" " + fontStatus);

        // font size information (1)
        res.append(" " + fontSize);

        // string type information (3)
        if (bold)
            res.append(" 1");
        else
            res.append(" 0");

        if (italic)
            res.append(" 1");
        else
            res.append(" 0");

        // capitalisation (1)
        if (digit.equals("ALLDIGIT"))
            res.append(" NOCAPS");
        else
            res.append(" " + capitalisation);

        // digit information (1)
        res.append(" " + digit);

        // character information (1)
        if (singleChar)
            res.append(" 1");
        else
            res.append(" 0");

        // lexical information (9)
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

        if (year)
            res.append(" 1");
        else
            res.append(" 0");

        if (month)
            res.append(" 1");
        else
            res.append(" 0");

        if (email)
            res.append(" 1");
        else
            res.append(" 0");

        if (http)
            res.append(" 1");
        else
            res.append(" 0");

        // punctuation information (2)
		if (punctType != null)
			res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        // relative document position (1)
        res.append(" " + relativeDocumentPosition);

        // relative page position (1)
        res.append(" " + relativePagePosition);
		
		// punctuation profile
		if ( (punctuationProfile == null) || (punctuationProfile.length() == 0) )
			res.append(" no");
		else
			res.append(" " + punctuationProfile);

		// current line length on a predefined scale
		res.append(" " + lineLength);

        // label - for training data (1)
        /*if (label != null)
              res.append(" " + label + "\n");
          else
              res.append(" 0\n");
          */

        res.append("\n");

        return res.toString();
    }

}