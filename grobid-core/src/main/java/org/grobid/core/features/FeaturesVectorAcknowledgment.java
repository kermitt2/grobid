package org.grobid.core.features;

import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.UnicodeUtil;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * Class contains feature vectors for acknowledgment parser model.
 *
 * @Created by Tanti, 2019
 */

public class FeaturesVectorAcknowledgment {
    public String string = null; // lexical feature
    public String label = null; // for the labe,l if it's  known
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS
    public String digit;  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;
    public boolean properName = false;
    public boolean commonName = false;
    public boolean locationName = false;
    public boolean countryName = false;
    public String punctType = null;
    public String wordShape = null;

    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuffer res = new StringBuffer();

        // token string (1)
        res.append(string);

        // lowercase string (1)
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

        // lexical information (4)
        // proper name (1)
        if (properName)
            res.append(" 1");
        else
            res.append(" 0");

        // common name
        if (commonName)
            res.append(" 1");
        else
            res.append(" 0");

        // location name
        if (locationName)
            res.append(" 1");
        else
            res.append(" 0");

        // country name
        if (countryName)
            res.append(" 1");
        else
            res.append(" 0");

        // punctuation information (1)
        res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        // word shape (1)
        res.append(" ").append(wordShape);

        // label - for training data (1)
        if (label != null)
            res.append(" " + label + "\n");
        else
            res.append(" 0\n");

        return res.toString();
    }

    /**
     * Add feature for acknowledgment parsing with string as input.
     */
    static public String addFeaturesAcknowledgmentString(List<String> lines) throws Exception {

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

        StringTokenizer st = new StringTokenizer(line.trim(), "\t ");

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
                featuresVectorAcknowledgment.capitalisation = "NOCAPS";

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
                featuresVectorAcknowledgment.properName = true;
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

    /**
     * Add feature for acknowledgment parsing with tokens as input.
     */
    static public String addFeaturesAcknowledgment(List<LayoutToken> tokens, List<String> labels) throws Exception {
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        StringBuilder acknowledgment = new StringBuilder();

        String previousTag = null;
        String previousText = null;
        FeaturesVectorAcknowledgment featuresVectorAcknowledgment = null;
        for (int n = 0; n < tokens.size(); n++) {
            LayoutToken token = tokens.get(n);
            String tag = null;

            if ((labels != null) && (labels.size() > 0) && (n < labels.size()))
                tag = labels.get(n);

            boolean outputLineStatus = false;

            String text = token.getText();
            if (text.equals(" ")) {
                continue;
            }

            if (text.equals("\n")) {
                continue;
            }

            // parano normalisation
            text = UnicodeUtil.normaliseTextAndRemoveSpaces(text);
            if (text.trim().length() == 0) {
                continue;
            }

            if (TextUtilities.filterLine(text)) {
                continue;
            }

            featuresVectorAcknowledgment = new FeaturesVectorAcknowledgment();

            featuresVectorAcknowledgment.string = text;
            featuresVectorAcknowledgment.label = tag;

            // line status
            if (n == 0) {
                featuresVectorAcknowledgment.lineStatus = "LINESTART";
                outputLineStatus = true;
            }
            if (n == 0) {
                if (!outputLineStatus) {
                    featuresVectorAcknowledgment.lineStatus = "LINESTART";
                    outputLineStatus = true;
                }
            } else if (tokens.size() == n + 1) {
                if (!outputLineStatus) {
                    featuresVectorAcknowledgment.lineStatus = "LINEEND";
                    outputLineStatus = true;
                }
            }
            if (!outputLineStatus) {
                featuresVectorAcknowledgment.lineStatus = "LINEIN";
                outputLineStatus = true;
            }

            // capitalisation
            if (Character.isUpperCase(text.charAt(0))) {
                featuresVectorAcknowledgment.capitalisation = "INITCAP";
            }

            if (featureFactory.test_all_capital(text)) {
                featuresVectorAcknowledgment.capitalisation = "ALLCAP";
            }

            if (featuresVectorAcknowledgment.capitalisation == null) {
                featuresVectorAcknowledgment.capitalisation = "NOCAPS";
            }

            // digit
            Matcher m = featureFactory.isDigit.matcher(text);
            if (m.find()) {
                featuresVectorAcknowledgment.digit = "ALLDIGIT";
            }

            if (featureFactory.test_digit(text)) {
                featuresVectorAcknowledgment.digit = "CONTAINSDIGITS";
            }

            if (featuresVectorAcknowledgment.digit == null)
                featuresVectorAcknowledgment.digit = "NODIGIT";

            // single character
            if (text.length() == 1) {
                featuresVectorAcknowledgment.singleChar = true;
            }

            // lexical information (4)
            // proper name
            if (featureFactory.test_names(text)) {
                featuresVectorAcknowledgment.properName = true;
            }

            // common name
            if (featureFactory.test_common(text)) {
                featuresVectorAcknowledgment.commonName = true;
            }

            // location name
            if (featureFactory.test_city(text)) {
                featuresVectorAcknowledgment.locationName = true;
            }

            // country name
            if (featureFactory.test_country(text)) {
                featuresVectorAcknowledgment.countryName = true;
            }

            // punctuations
            Matcher m0 = featureFactory.isPunct.matcher(text);
            if (m0.find()) {
                featuresVectorAcknowledgment.punctType = "PUNCT";
            }

            if (featuresVectorAcknowledgment.punctType == null)
                featuresVectorAcknowledgment.punctType = "NOPUNCT";

            if ((text.equals("(")) | (text.equals("["))) {
                featuresVectorAcknowledgment.punctType = "OPENBRACKET";
            } else if ((text.equals(")")) | (text.equals("]"))) {
                featuresVectorAcknowledgment.punctType = "ENDBRACKET";
            } else if (text.equals(".")) {
                featuresVectorAcknowledgment.punctType = "DOT";
            } else if (text.equals(",")) {
                featuresVectorAcknowledgment.punctType = "COMMA";
            } else if (text.equals("-")) {
                featuresVectorAcknowledgment.punctType = "HYPHEN";
            } else if (text.equals("\"") | text.equals("\'") | text.equals("`")) {
                featuresVectorAcknowledgment.punctType = "QUOTE";
            }

            featuresVectorAcknowledgment.wordShape = TextUtilities.wordShape(text);

            acknowledgment.append(featuresVectorAcknowledgment.printVector());

            previousTag = tag;
            previousText = text;

        }

        return acknowledgment.toString();
    }
}