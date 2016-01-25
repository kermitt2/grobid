package org.grobid.core.features;

import org.grobid.core.layout.LayoutToken;

/**
 * Class for features used for fulltext parsing.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorFulltext {
    public LayoutToken token = null; // not a feature, reference value
    public String string = null; // lexical feature
    public String label = null; // label if known
    public String blockStatus = null; // one of BLOCKSTART, BLOCKIN, BLOCKEND
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
    public String fontStatus = null; // one of NEWFONT, SAMEFONT
    public String fontSize = null; // one of HIGHERFONT, SAMEFONTSIZE, LOWERFONT
    public String alignmentStatus = null; // one of ALIGNEDLEFT, INDENTED, CENTERED - applied to the whole line
    public boolean bold = false;
    public boolean italic = false;
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS
    public String digit;  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;

    public String punctType = null; 
    // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)

    public int relativeDocumentPosition = -1; 
    public int relativePagePositionChar = -1; 
    public int relativePagePosition = -1; 

	// graphic in closed proximity of the current block
    public boolean bitmapAround = false;
    public boolean vectorAround = false;
	
	// if a graphic is in close proximity of the current block, characteristics of this graphic 
    public int closestGraphicHeight = -1; 
    public int closestGraphicWidth = -1; 
    public int closestGraphicSurface = -1; 
	
    public int spacingWithPreviousBlock = 0; // discretized 
    public int characterDensity = 0; // discretized

    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuffer res = new StringBuffer();

        // token string (1)
        res.append(string);

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

		// at this stage, we have written 10 features

        // block information (1)
        res.append(" " + blockStatus);

        // line information (1)
        res.append(" " + lineStatus);
		
		// line position/identation (1)
		res.append(" " + alignmentStatus);

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

		// at this stage, we have written 20 features

        // punctuation information (1)
        res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        // relative document position (1)
        res.append(" " + relativeDocumentPosition);

        // relative page position (1)
        res.append(" " + relativePagePosition);

		// proximity of a graphic to the current block (2)
        if (bitmapAround)
            res.append(" 1");
        else
            res.append(" 0");

        /*if (vectorAround)
            res.append(" 1");
        else
            res.append(" 0");*/
		
        // space with previous block, discretised (1)
        //res.append(" " + spacingWithPreviousBlock);
        //res.append(" " + 0);

        // character density of the previous block, discretised (1)
        //res.append(" " + characterDensity);
        //res.append(" " + 0);

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