package org.grobid.core.features;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Class for features used for parsing date chunk.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorDate {
    public String string = null; // lexical feature
    public String label = null; // label if known
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS
    public String digit;  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;
    public boolean containDash = false;
    public boolean year = false;
    public boolean month = false;
    public String punctType = null;
    // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)
    public boolean containPunct = false;

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

        // lexical information (2)
        if (year)
            res.append(" 1");
        else
            res.append(" 0");

        if (month)
            res.append(" 1");
        else
            res.append(" 0");

        // punctuation information (2)
        res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        // label - for training data (1)
        if (label != null)
            res.append(" " + label + "\n");
        else
            res.append(" 0\n");

        return res.toString();
    }

    /**
     * Add feature for date parsing.
     */
    static public String addFeaturesDate(List<String> lines) throws Exception {
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        String line;
        StringBuilder header = new StringBuilder();
        boolean newline = true;
        boolean newBlock = true;
        String currentFont = null;
        int currentFontSize = -1;
        int n = 0;

        boolean endblock = false;
        String previousTag = null;
        String previousText = null;
        FeaturesVectorDate features = null;
        while (n < lines.size()) {
            boolean outputLineStatus = false;
            boolean outputBlockStatus = false;

            line = lines.get(n);

            if (line == null) {
                header.append(" \n");
                newBlock = true;
                newline = true;
                n++;
                continue;
            }
            line = line.trim();
            if (line.length() == 0) {
                header.append("\n \n");
                newBlock = true;
                newline = true;
                n++;
                continue;
            }

            if (line.equals("@newline")) {
                if (newline) {
                    newBlock = true;
                }
                newline = true;
                n++;
                continue;
            }

            int ind = line.indexOf(" ");
            String text = null;
            String tag = null;
            if (ind != -1) {
                text = line.substring(0, ind);
                tag = line.substring(ind + 1, line.length());
            }

            boolean filter = false;
            if (text == null) {
                filter = true;
            } else if (text.length() == 0) {
                filter = true;
            } else if (text.startsWith("@IMAGE")) {
                filter = true;
            } else if (text.indexOf(".pbm") != -1) {
                filter = true;
            } else if (text.indexOf(".vec") != -1) {
                filter = true;
            } else if (text.indexOf(".jpg") != -1) {
                filter = true;
            }

            if (filter) {
                continue;
            }

            features = new FeaturesVectorDate();
            features.string = text;

            if (newline) {
                features.lineStatus = "LINESTART";
                outputLineStatus = true;
            }

            Matcher m0 = featureFactory.isPunct.matcher(text);
            if (m0.find()) {
                features.punctType = "PUNCT";
            }

            if ((text.equals("(")) | (text.equals("["))) {
                features.punctType = "OPENBRACKET";
            } else if ((text.equals(")")) | (text.equals("]"))) {
                features.punctType = "ENDBRACKET";
            } else if (text.equals(".")) {
                features.punctType = "DOT";
            } else if (text.equals(",")) {
                features.punctType = "COMMA";
            } else if (text.equals("-")) {
                features.punctType = "HYPHEN";
            } else if (text.equals("\"") | text.equals("\'") | text.equals("`")) {
                features.punctType = "QUOTE";
            }

            if (n == 0) {
                if (!outputLineStatus) {
                    features.lineStatus = "LINESTART";
                    outputLineStatus = true;
                }
            } else if (lines.size() == n + 1) {
                if (!outputLineStatus) {
                    features.lineStatus = "LINEEND";
                    outputLineStatus = true;
                }
            } else {
                // look ahead...
                boolean endline = false;
                int i = 1;
                boolean endloop = false;
                while ((lines.size() > n + i) & (!endloop)) {
                    String newLine = lines.get(n + i);

                    if (newLine != null) {
                        if (newLine.trim().length() == 0) {
                            endline = true;
                            endblock = true;
                            if (!outputLineStatus) {
                                features.lineStatus = "LINEEND";
                                outputLineStatus = true;
                            }
                        } else if (newLine.equals("@newline")) {
                            endline = true;
                            if (!outputLineStatus) {
                                features.lineStatus = "LINEEND";
                                outputLineStatus = true;
                            }
                        } else {
                            endloop = true;
                        }
                    }

                    if ((endline) & (!outputLineStatus)) {
                        features.lineStatus = "LINEEND";
                        outputLineStatus = true;
                    }
                    i++;
                }
            }

            newline = false;
            if (!outputLineStatus) {
                features.lineStatus = "LINEIN";
                outputLineStatus = true;
            }

            if (text.length() == 1) {
                features.singleChar = true;
                ;
            }

            if (Character.isUpperCase(text.charAt(0))) {
                features.capitalisation = "INITCAP";
            }

            if (featureFactory.test_all_capital(text)) {
                features.capitalisation = "ALLCAP";
            }

            if (features.capitalisation == null)
                features.capitalisation = "NOCAPS";

            if (featureFactory.test_digit(text)) {
                features.digit = "CONTAINSDIGITS";
            }

            if (featureFactory.test_month(text)) {
                features.month = true;
            }

            Matcher m = featureFactory.isDigit.matcher(text);
            if (m.find()) {
                features.digit = "ALLDIGIT";
            }

            if (features.digit == null)
                features.digit = "NODIGIT";

            Matcher m2 = featureFactory.YEAR.matcher(text);
            if (m2.find()) {
                features.year = true;
            }

            if (features.punctType == null)
                features.punctType = "NOPUNCT";

            features.label = tag;

            header.append(features.printVector());

            previousTag = tag;
            previousText = text;
            n++;
        }

        return header.toString();
    }

}