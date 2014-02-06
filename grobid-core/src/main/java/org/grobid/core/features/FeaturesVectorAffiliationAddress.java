package org.grobid.core.features;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.TextUtilities;

/**
 * Class for features used for parsing a block corresponding to affiliation and address.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorAffiliationAddress {
    public String string = null; // lexical feature
    public String label = null; // label if known
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
    public boolean bold = false;
    public boolean italic = false;
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS
    public String digit;  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;
    public boolean properName = false;
    public boolean commonName = false;
    public boolean firstName = false;
    public boolean locationName = false;
    public boolean countryName = false;
    public String punctType = null;
    public String wordShape = null;
    // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)

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

        // lexical information (5)
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

        if (countryName)
            res.append(" 1");
        else
            res.append(" 0");

        // punctuation information (1)
        res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        res.append(" ").append(wordShape);

        // label - for training data (1)
        if (label != null)
            res.append(" " + label + "\n");
        else
            res.append(" 0\n");



        return res.toString();
    }

    /**
     * Add the features for the affiliation+address model.
     */
    static public String addFeaturesAffiliationAddress(List<String> lines,
                                                       List<List<OffsetPosition>> locationPlaces) throws Exception {
        if (locationPlaces == null) {
            throw new GrobidException("At least one list of gazetter matches positions is null.");
        }
        if (locationPlaces.size() == 0) {
            throw new GrobidException("At least one list of gazetter matches positions is empty.");
        }
        StringBuffer result = new StringBuffer();
        List<String> block = null;
        boolean isPlace = false;
        String lineStatus = "LINESTART";
        int locPlace = 0;
        List<OffsetPosition> currentLocationPlaces = locationPlaces.get(locPlace);
        int currentPosPlaces = 0;
        int mm = 0; // position of the token in the current sentence
        String line = null;

        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);
			isPlace = false;
			if (line.equals("\n")) {
				result.append("\n \n");
				continue;
			}

            // check the position of matches for place names
            boolean skipTest = false;
            if ((currentLocationPlaces != null) && (currentLocationPlaces.size() > 0)) {
                if (currentPosPlaces == currentLocationPlaces.size() - 1) {
                    if (currentLocationPlaces.get(currentPosPlaces).end < mm) {
                        skipTest = true;
                    }
                }
                if (!skipTest) {
                    for (int j = currentPosPlaces; j < currentLocationPlaces.size(); j++) {
                        if ((currentLocationPlaces.get(j).start <= mm) &&
                                (currentLocationPlaces.get(j).end >= mm)) {
                            isPlace = true;
                            currentPosPlaces = j;
                            break;
                        } else if (currentLocationPlaces.get(j).start > mm) {
                            isPlace = false;
                            currentPosPlaces = j;
                            break;
                        }
                    }
                }
            }

            if (line.trim().equals("@newline")) {
                lineStatus = "LINESTART";
                continue;
            }

            if (line.trim().length() == 0) {
                result.append("\n");
                lineStatus = "LINESTART";
                currentLocationPlaces = locationPlaces.get(locPlace);
                currentPosPlaces = 0;
                locPlace++;
                mm = 0;
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

                FeaturesVectorAffiliationAddress vector = addFeaturesAffiliationAddress(line, lineStatus, isPlace);
                result.append(vector.printVector());

                if (lineStatus.equals("LINESTART")) {
                    lineStatus = "LINEIN";
                }
            }
            mm++;
        }

        return result.toString();
    }

    static private FeaturesVectorAffiliationAddress addFeaturesAffiliationAddress(String line,
                                                                                  String lineStatus,
                                                                                  boolean isPlace) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();
        FeaturesVectorAffiliationAddress featuresVector = new FeaturesVectorAffiliationAddress();

        StringTokenizer st = new StringTokenizer(line.trim(), "\t ");
        if (st.hasMoreTokens()) {
            String word = st.nextToken();

            String label = null;
            if (st.hasMoreTokens())
                label = st.nextToken();

            featuresVector.string = word;
            featuresVector.label = label;

            featuresVector.lineStatus = lineStatus;

            if (word.length() == 1) {
                featuresVector.singleChar = true;
            }

            if (featureFactory.test_all_capital(word))
                featuresVector.capitalisation = "ALLCAPS";
            else if (featureFactory.test_first_capital(word))
                featuresVector.capitalisation = "INITCAP";
            else
                featuresVector.capitalisation = "NOCAPS";

            if (featureFactory.test_number(word))
                featuresVector.digit = "ALLDIGIT";
            else if (featureFactory.test_digit(word))
                featuresVector.digit = "CONTAINDIGIT";
            else
                featuresVector.digit = "NODIGIT";

            if (featureFactory.test_common(word))
                featuresVector.commonName = true;

            if (featureFactory.test_names(word))
                featuresVector.properName = true;

            Matcher m0 = featureFactory.isPunct.matcher(word);
            if (m0.find()) {
                featuresVector.punctType = "PUNCT";
            }
            if ((word.equals("(")) | (word.equals("["))) {
                featuresVector.punctType = "OPENBRACKET";
            } else if ((word.equals(")")) | (word.equals("]"))) {
                featuresVector.punctType = "ENDBRACKET";
            } else if (word.equals(".")) {
                featuresVector.punctType = "DOT";
            } else if (word.equals(",")) {
                featuresVector.punctType = "COMMA";
            } else if (word.equals("-")) {
                featuresVector.punctType = "HYPHEN";
            } else if (word.equals("\"") | word.equals("\'") | word.equals("`")) {
                featuresVector.punctType = "QUOTE";
            }

            if (featuresVector.capitalisation == null)
                featuresVector.capitalisation = "NOCAPS";

            if (featuresVector.digit == null)
                featuresVector.digit = "NODIGIT";

            if (featuresVector.punctType == null)
                featuresVector.punctType = "NOPUNCT";

            if (featureFactory.test_country(word)) {
                featuresVector.countryName = true;
            }

            if (isPlace) {
                featuresVector.locationName = true;
            }

            featuresVector.wordShape = TextUtilities.wordShape(word);
        }

        return featuresVector;
    }
}