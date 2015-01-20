package org.grobid.core.features;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Class for features used for header parsing.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorReferenceSegmenter {
    // default bins for relative position, set experimentally
    static private int nbBins = 12;

    public String string = null; // lexical feature
    public String label = null; // label if known
    public String blockStatus = null; // one of BLOCKSTART, BLOCKIN, BLOCKEND
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
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
    public String punctType = null; // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)
    public boolean containPunct = false;
    public int relativePosition = -1;

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

        if (email)
            res.append(" 1");
        else
            res.append(" 0");

        if (http)
            res.append(" 1");
        else
            res.append(" 0");

        // punctuation information (1)
        res.append(" ").append(punctType); // in case the token is a punctuation (NO otherwise)

        // relative position in the sequence (1)
        res.append(" ").append(relativePosition);

        // label - for training data (1)
        if (label != null)
            res.append(" ").append(label).append("\n");
        else
            res.append(" 0\n");

        return res.toString();
    }


    /**
     * Add feature for citation parsing.
     */
    static public String addFeaturesReferenceSegmenter(List<String> lines) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();
        String line;
        StringBuilder citation = new StringBuilder();
        boolean newline = true;
//        String currentFont = null;
//        int currentFontSize = -1;
        int n = 0; // overall token number - we have one token per line

        int sentenceNb = 0;
        int currentJournalPositions = 0;
        int currentAbbrevJournalPositions = 0;
        int currentConferencePositions = 0;
        int currentPublisherPositions = 0;
        boolean isJournalToken;
        boolean isAbbrevJournalToken;
        boolean isConferenceToken;
        boolean isPublisherToken;
        boolean skipTest;

        String previousTag = null;
        String previousText = null;
        FeaturesVectorReferenceSegmenter features = null;
        int mm = 0; // token position in the sentence
        int sentenceLenth = 0; // length of the current sentence
        while (n < lines.size()) {
            boolean outputLineStatus = false;
            isJournalToken = false;
            isAbbrevJournalToken = false;
            isConferenceToken = false;
            isPublisherToken = false;
            skipTest = false;
            if (mm == 0) {
                // check the length of the current sentence
                sentenceLenth = 0;
                int q = 0;
                while ((sentenceLenth == 0) & (n + q < lines.size())) {
                    String truc = lines.get(n + q);
                    if (truc != null) {
                        if (truc.trim().length() == 0) {
                            sentenceLenth = q;
                        }
                    }
                    q++;
                }
            }

            line = lines.get(n);

            if (line == null) {
                n++;
                newline = true;
                continue;
            }
            line = line.trim();
            if (line.length() == 0) {
                // we start a new sentence in the sense of CRF++
                citation.append("\n \n");
                n++;
                mm = 0;
                sentenceLenth = 0;
                newline = true;
                sentenceNb++;
                currentJournalPositions = 0;
                currentAbbrevJournalPositions = 0;
                currentConferencePositions = 0;
                currentPublisherPositions = 0;
                continue;
            }

            if (line.equals("@newline")) {
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
            } else if (text.contains(".pbm")) {
                filter = true;
            } else if (text.contains(".vec")) {
                filter = true;
            } else if (text.contains(".jpg")) {
                filter = true;
            }

            if (filter) {
                n++;
                continue;
            }

            features = new FeaturesVectorReferenceSegmenter();
            features.string = text;
            features.relativePosition = featureFactory.relativeLocation(mm, sentenceLenth, nbBins);

            if (newline) {
                features.lineStatus = "LINESTART";
                outputLineStatus = true;
                newline = false;
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
                        if (newLine.equals("@newline")) {
                            endline = true;
                        } else if (newLine.trim().length() == 0) {
                            endline = true;
                        } else {
                            /*int indd = newLine.indexOf(" ");
                                   if (indd != -1) {
                                       String nextText = newLine.substring(0,indd);
                                       String nextTag = newLine.substring(indd+1,newLine.length());
                                   }*/
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
            }

            if (Character.isUpperCase(text.charAt(0))) {
                features.capitalisation = "INITCAP";
            }

            if (featureFactory.test_all_capital(text)) {
                features.capitalisation = "ALLCAP";
            }

            if (featureFactory.test_digit(text)) {
                features.digit = "CONTAINSDIGITS";
            }

            if (featureFactory.test_common(text)) {
                features.commonName = true;
            }

            if (featureFactory.test_names(text)) {
                features.properName = true;
            }

            if (featureFactory.test_month(text)) {
                features.month = true;
            }

            if (text.contains("-")) {
                features.containDash = true;
            }

            Matcher m = featureFactory.isDigit.matcher(text);
            if (m.find()) {
                features.digit = "ALLDIGIT";
            }

            Matcher m2 = featureFactory.YEAR.matcher(text);
            if (m2.find()) {
                features.year = true;
            }

            Matcher m3 = featureFactory.EMAIL.matcher(text);
            if (m3.find()) {
                features.email = true;
            }

            Matcher m4 = featureFactory.HTTP.matcher(text);
            if (m4.find()) {
                features.http = true;
            }

            /*Matcher m5 = featureFactory.ACRONYM.matcher(text);
               if (m5.find()) {
                   features.acronym = true;
               }*/

            if (features.capitalisation == null)
                features.capitalisation = "NOCAPS";

            if (features.digit == null)
                features.digit = "NODIGIT";

            if (features.punctType == null)
                features.punctType = "NOPUNCT";

            features.label = tag;
            citation.append(features.printVector());

            previousTag = tag;
            previousText = text;
            n++;
            mm++;
        }

        return citation.toString();
    }

}