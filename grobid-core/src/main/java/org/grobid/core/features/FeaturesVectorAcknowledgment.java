package org.grobid.core.features;

import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.utilities.TextUtilities;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * Class for features used for parsing acknowledgment chunk.
 */

public class FeaturesVectorAcknowledgment {
    public String string = null; // lexical feature
    public String label = null; // label if known
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS
    public String digit;  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;
    public boolean properName = false;
    public boolean commonName = false;
    public boolean affiliation = false;
    public boolean educationalInsitution = false;
    public boolean fundingAgency = false;
    public boolean grantName = false;
    public boolean grantNumber = false;
    public boolean individual = false;
    public boolean otherInsitution = false;
    public boolean projectName = false;
    public boolean researchInstitution = false;
    public String punctType = null;
    public String wordShape = null;

    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuffer res = new StringBuffer();

        // token string (1)
        res.append(string);

        // lowercase string
        res.append(" " + string.toLowerCase());

        // prefix (4)
        res.append(" " + TextUtilities.prefix(string, 1));
        res.append(" " + TextUtilities.prefix(string, 2));
        res.append(" " + TextUtilities.prefix(string, 3));
        res.append(" " + TextUtilities.prefix(string, 4));

        // suffix (4)
        res.append(" " + TextUtilities.suffix(string, 1));
        res.append(" " + TextUtilities.suffix(string, 2));
        res.append(" " + TextUtilities.suffix(string, 3));
        res.append(" " + TextUtilities.suffix(string, 4));

        // line information (1)
        res.append(" " + lineStatus);

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

        // proper name
        if (properName)
            res.append(" 1");
        else
            res.append(" 0");

        // common name
        if (commonName)
            res.append(" 1");
        else
            res.append(" 0");

        // lexical information (9)
        if (affiliation)
            res.append(" 1");
        else
            res.append(" 0");

        if (educationalInsitution)
            res.append(" 1");
        else
            res.append(" 0");

        if (fundingAgency)
            res.append(" 1");
        else
            res.append(" 0");

        if (grantName)
            res.append(" 1");
        else
            res.append(" 0");

        if (grantNumber)
            res.append(" 1");
        else
            res.append(" 0");

        if (individual)
            res.append(" 1");
        else
            res.append(" 0");

        if (otherInsitution)
            res.append(" 1");
        else
            res.append(" 0");

        if (projectName)
            res.append(" 1");
        else
            res.append(" 0");

        if (researchInstitution)
            res.append(" 1");
        else
            res.append(" 0");

        // punctuation information (1)
        res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        // label - for training data (1)
        if (label != null)
            res.append(" " + label + "\n");
        else
            res.append(" 0\n");

        return res.toString();
    }

    /**
     * Add feature for acknowledgment parsing.
     */
    static public String addFeaturesAcknowledgment(List<String> lines) throws Exception {
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        StringBuffer result = new StringBuffer();
        List<String> block = null;
        String lineStatus = "LINESTART";
        String line = null;
        for (int i = 0; i < lines.size(); i++) {
            // get the line content
            line = lines.get(i);

            if (line.equals("\n")) {
                result.append("\n \n");
                continue;
            }

            if (line.trim().equals("@newline")) {
                lineStatus = "LINESTART";
                continue;
            }

            if (line.trim().length() == 0) {
                result.append("\n");
                lineStatus = "LINESTART";
            } else {
                // look ahead for line status update
                if ((i + 1) < lines.size()) {
                    String nextLine = lines.get(i + 1);
                    if ((nextLine.trim().length() == 0) || (nextLine.trim().equals("@newline"))) {
                        lineStatus = "LINEEND";
                    }
                } else if ((i + 1) == lines.size()) {
                    lineStatus = "LINEEND";
                }

                FeaturesVectorAcknowledgment vector = addFeaturesAcknowledgment(line, lineStatus);
                result.append(vector.printVector());

                if (lineStatus.equals("LINESTART")) {
                    lineStatus = "LINEIN";
                }
            }
        }

        return result.toString();

    }

    static private FeaturesVectorAcknowledgment addFeaturesAcknowledgment(String line, String lineStatus) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();
        FeaturesVectorAcknowledgment featuresVectorAcknowledgment = new FeaturesVectorAcknowledgment();

        //List<String> tokens = GrobidAnalyzer.getInstance().tokenize(line);
        StringTokenizer st = new StringTokenizer(line.trim(), "\t ");
        //for (String tok : tokens) {
            /*String word = tok;

            String label = null;
            if (tok != null) {
                label = tok;
            }*/

        if (st.hasMoreTokens()) {
            String word = st.nextToken();

            String label = null;
            if (st.hasMoreTokens())
                label = st.nextToken();

            featuresVectorAcknowledgment.string = word;
            featuresVectorAcknowledgment.label = label;

            featuresVectorAcknowledgment.lineStatus = lineStatus;

            if (word.length() == 1) {
                featuresVectorAcknowledgment.singleChar = true;
            }

            // capital
            if (featureFactory.test_all_capital(word)) {
                featuresVectorAcknowledgment.capitalisation = "ALLCAPS";
            } else if (featureFactory.test_first_capital(word)) {
                featuresVectorAcknowledgment.capitalisation = "INITCAP";
            } else
                featuresVectorAcknowledgment.capitalisation = "NOCAP";

            // digit
            if (featureFactory.test_number(word)) {
                featuresVectorAcknowledgment.digit = "ALLDIGIT";
            } else if (featureFactory.test_digit(word)) {
                featuresVectorAcknowledgment.digit = "CONTAINDIGIT";
            } else {
                featuresVectorAcknowledgment.digit = "NODIGIT";
            }

            // common name
            if (featureFactory.test_common(word)) {
                featuresVectorAcknowledgment.commonName = true;
            }

            // proper name
            if (featureFactory.test_names(word)) {
                featuresVectorAcknowledgment.projectName = true;
            }

            // find the punctuations
            Matcher m0 = featureFactory.isPunct.matcher(word);
            if (m0.find()) {
                featuresVectorAcknowledgment.punctType = "PUNCT";
            }

            // token containing special character
            if ((word.equals("(")) | (word.equals("["))) {
                featuresVectorAcknowledgment.punctType = "OPENBRACKET";
            } else if ((word.equals(")")) | (word.equals("]"))) {
                featuresVectorAcknowledgment.punctType = "ENDBRACKET";
            } else if (word.equals(".")) {
                featuresVectorAcknowledgment.punctType = "DOT";
            } else if (word.equals(",")) {
                featuresVectorAcknowledgment.punctType = "COMMA";
            } else if (word.equals("-")) {
                featuresVectorAcknowledgment.punctType = "HYPHEN";
            } else if (word.equals("\"") | word.equals("\'") | word.equals("`")) {
                featuresVectorAcknowledgment.punctType = "QUOTE";
            }

            if (featuresVectorAcknowledgment.capitalisation == null)
                featuresVectorAcknowledgment.capitalisation = "NOCAPS";

            if (featuresVectorAcknowledgment.digit == null)
                featuresVectorAcknowledgment.digit = "NODIGIT";

            if (featuresVectorAcknowledgment.punctType == null)
                featuresVectorAcknowledgment.punctType = "NOPUNCT";

            featuresVectorAcknowledgment.wordShape = TextUtilities.wordShape(word);
        }
        return featuresVectorAcknowledgment;
    }
}
