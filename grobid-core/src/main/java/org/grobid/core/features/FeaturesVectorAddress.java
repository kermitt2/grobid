package org.grobid.core.features;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.UnicodeUtil;
import org.grobid.core.layout.LayoutToken;

/**
 * Class for features used for parsing a block corresponding to raw names and addresses
 *
 */
public class FeaturesVectorAddress {
    public String string = null; // lexical feature
    public String label = null; // label if known
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS
    public String digit;  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;
    public boolean properName = false;
    public boolean commonName = false;
    public boolean isKnownPlace = false;
    public boolean isKnownCountry = false;
    
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

        // lexical information (8)
        if (properName)
            res.append(" 1");
        else
            res.append(" 0");

        if (commonName)
            res.append(" 1");
        else
            res.append(" 0");

        if (isKnownPlace)
            res.append(" 1");
        else
            res.append(" 0");

        if (isKnownCountry)
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
    static public String addFeaturesAddress(List<LayoutToken> tokens,
                                                List<String> labels,
                                                List<OffsetPosition> placePositions,
                                                List<OffsetPosition> countryPositions) throws Exception {
        /*if (locationPlaces == null || locationPlaces.size() == 0) {
            throw new GrobidException("At least one list of gazetter matches positions is null.");
        }*/

        StringBuffer result = new StringBuffer();
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        boolean newline = true;
        String previousTag = null;
        String previousText = null;
        FeaturesVectorNameAddress features = null;
        LayoutToken token = null;

        int currentPlacePosition = 0;
        int currentCountryPosition = 0;

        boolean isPlaceToken;
        boolean isCountryToken;
        boolean skipTest;

        for(int n=0; n<tokens.size(); n++) {
            boolean outputLineStatus = false;
            isPlaceToken = false;
            isCountryToken = false;

            skipTest = false;

            token = tokens.get(n);

            //int ind = line.indexOf(" ");
            String text = token.getText();
            if (text.equals(" ")) {
                continue;
            }

            //newline = false;
            if (text.equals("\n")) {
                newline = true;
                continue;
            }

            // parano normalisation
            text = UnicodeUtil.normaliseTextAndRemoveSpaces(text);
            if (text.trim().length() == 0 ) {
                continue;
            }

            // check the position of matches for country
            skipTest = false;
            if (countryPositions != null) {
                if (currentCountryPosition == countryPositions.size() - 1) {
                    if (countryPositions.get(currentCountryPosition).end < n) {
                        skipTest = true;
                    }
                }
                if (!skipTest) {
                    for (int i = currentCountryPosition; i < countryPositions.size(); i++) {
                        if ((countryPositions.get(i).start <= n) &&
                                (countryPositions.get(i).end >= n)) {
                            isCountryToken = true;
                            currentCountryPosition = i;
                            break;
                        } else if (countryPositions.get(i).start > n) {
                            isCountryToken = false;
                            currentCountryPosition = i;
                            break;
                        }
                    }
                }
            }

            // check the position of matches for place
            skipTest = false;
            if (placePositions != null) {
                if (currentPlacePosition == placePositions.size() - 1) {
                    if (placePositions.get(currentPlacePosition).end < n) {
                        skipTest = true;
                    }
                }
                if (!skipTest) {
                    for (int i = currentPlacePosition; i < placePositions.size(); i++) {
                        if ((placePositions.get(i).start <= n) &&
                                (placePositions.get(i).end >= n)) {
                            isPlaceToken = true;
                            currentPlacePosition = i;
                            break;
                        } else if (placePositions.get(i).start > n) {
                            isPlaceToken = false;
                            currentPlacePosition = i;
                            break;
                        }
                    }
                }
            }

            String tag = null;
            if (!CollectionUtils.isEmpty(labels) && (labels.size() > n)) {
                tag = labels.get(n);
            }

            if (TextUtilities.filterLine(text)) {
                continue;
            }

            features = new FeaturesVectorNameAddress();
            features.string = text;

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
                    newline = false;
                }
            } else if (tokens.size() == n + 1) {
                if (!outputLineStatus) {
                    features.lineStatus = "LINEEND";
                    outputLineStatus = true;
                }
            } else {
                // look ahead...
                boolean endline = false;
                int i = 1;
                boolean endloop = false;
                while ((tokens.size() > n + i) & (!endloop)) {
                    String newLine = tokens.get(n + i).getText();
                    if (newLine != null) {
                        if (newLine.equals("\n")) {
                            endline = true;
                            if (!outputLineStatus) {
                                features.lineStatus = "LINEEND";
                                outputLineStatus = true;
                            }
                            endloop = true;
                        } else if (!newLine.equals(" ")) {
                            endloop = true;
                        }
                    }

                    /*if ((endline) & (!outputLineStatus)) {
                        features.lineStatus = "LINEEND";
                        outputLineStatus = true;
                    }*/
                    i++;
                }
            }

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

            if (features.capitalisation == null)
                features.capitalisation = "NOCAPS";

            if (featureFactory.test_digit(text)) {
                features.digit = "CONTAINSDIGITS";
            }

            if (featureFactory.test_common(text)) {
                features.commonName = true;
            }

            Matcher m = featureFactory.isDigit.matcher(text);
            if (m.find()) {
                features.digit = "ALLDIGIT";
            }

            if (features.digit == null)
                features.digit = "NODIGIT";

            if (features.punctType == null)
                features.punctType = "NOPUNCT";

            if (isPlaceToken) {
                features.isKnownPlace = true;
            }

            if (isCountryToken) {
                features.isKnownCountry = true;
            }

            features.wordShape = TextUtilities.wordShape(text);

            features.label = tag;

            result.append(features.printVector());

            previousTag = tag;
            previousText = text;
        }

        return result.toString();
    }

}