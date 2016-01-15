package org.grobid.core.features;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;

import org.grobid.core.layout.LayoutToken;

/**
 * Class for features used for header parsing.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorHeader {
    public LayoutToken token = null; // not a feature, reference value
    public String string = null; // lexical feature
    public String label = null; // label if known
    public String blockStatus = null; // one of BLOCKSTART, BLOCKIN, BLOCKEND
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
    public String alignmentStatus = null; // one of ALIGNEDLEFT, INDENTED, CENTERED - applied to the whole line
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

    public String printVector() {
        return printVector(true);
    }

    public String printVector(boolean withRotation) {
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

        // 10 first features written at this stage

        // block information (1)
        res.append(" " + blockStatus);
        //res.append(" 0");

        // line information (1)
        res.append(" " + lineStatus);
		
		// line position/identation
		//res.append(" " + alignmentStatus);

        // font information (1)
        res.append(" " + fontStatus);
        //res.append(" 0");

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

        if (withRotation) {
            if (rotation)
                res.append(" 1");
            else
                res.append(" 0");
        }

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

        // 20 first features written at this stage

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

        if (containDash)
            res.append(" 1");
        else
            res.append(" 0");

        // punctuation information (2)
        res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        // 30 features written at this point

        if (containPunct)
            res.append(" 1");
        else
            res.append(" 0");

        // label - for training data (1)
        if (label != null)
            res.append(" " + label + "\n");
        else
            res.append(" 0\n");

        return res.toString();
    }

    /**
     * Add feature for header recognition to labeled data in a file. The boolean indicates
     * if the label set should be limited or not to only title and date.
     */
    static public String addFeaturesHeader(File file, boolean restrict) throws Exception {
        BufferedReader bis = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF8"));

        String line = null;
        List<String> lines = new ArrayList<String>();
        while ((line = bis.readLine()) != null) {
            lines.add(line);
        }
        bis.close();

        return addFeaturesHeader(lines, restrict);
    }

    /**
     * Add feature for header recognition. The boolean indicates if the label set
     * should be limited or not to only title and date.
     */
    static public String addFeaturesHeader(List<String> lines, boolean restrict) throws Exception {
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        String line;
        StringBuffer header = new StringBuffer();
        boolean newline = true;
        boolean newBlock = true;
        String currentFont = null;
        int currentFontSize = -1;
        int n = 0;

        boolean endblock = false;
        String previousTag = null;
        String previousText = null;
        FeaturesVectorHeader features = null;
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
                header.append(" \n");
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

            if (filter) continue;

            features = new FeaturesVectorHeader();
            features.string = text;

            if (newline) {
                features.lineStatus = "LINESTART";
                outputLineStatus = true;
            }
            if (newBlock) {
                features.blockStatus = "BLOCKSTART";
                outputBlockStatus = true;
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
                if (!outputBlockStatus) {
                    features.blockStatus = "BLOCKSTART";
                    outputBlockStatus = true;
                }
            } else if (lines.size() == n + 1) {
                if (!outputLineStatus) {
                    features.lineStatus = "LINEEND";
                    outputLineStatus = true;
                }
                if (!outputBlockStatus) {
                    features.blockStatus = "BLOCKEND";
                    outputBlockStatus = true;
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
                            if (!outputBlockStatus) {
                                features.blockStatus = "BLOCKEND";
                                outputBlockStatus = true;
                            }
                        } else if (newLine.equals("@newline")) {
                            endline = true;
                            if (!outputLineStatus) {
                                features.lineStatus = "LINEEND";
                                outputLineStatus = true;
                            }
                        } else {
                            int indd = newLine.indexOf(" ");
                            if (indd != -1) {
                                String nextText = newLine.substring(0, indd);
                                String nextTag = newLine.substring(indd + 1, newLine.length());
                                //if (nextTag.equals("\n"))
                                //	endline = true;
                                if (!tag.equals(nextTag)) {
                                    // we check the nature of the transition to end or not the block

                                    if (nextTag.equals("<title>")) {
                                        endline = true;
                                        if (!outputBlockStatus) {
                                            features.blockStatus = "BLOCKEND";
                                            outputBlockStatus = true;
                                        }
                                        endblock = true;
                                    } else if (nextTag.equals("<author>")) {
                                        endline = true;
                                        if (!outputBlockStatus) {
                                            features.blockStatus = "BLOCKEND";
                                            outputBlockStatus = true;
                                        }
                                        endblock = true;
                                    } else if (nextTag.equals("<abstract>")) {
                                        endline = true;
                                        if (!outputBlockStatus) {
                                            features.blockStatus = "BLOCKEND";
                                            outputBlockStatus = true;
                                        }
                                        endblock = true;
                                    }
                                }
                            }
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
            newBlock = false;

            features.fontStatus = "SAMEFONT";
            features.fontSize = "SAMEFONTSIZE";

            if (!outputBlockStatus) {
                features.blockStatus = "BLOCKIN";
                outputBlockStatus = true;
            }
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

            if (featureFactory.test_common(text)) {
                features.commonName = true;
            }

            if (featureFactory.test_names(text)) {
                features.properName = true;
            }

            if (featureFactory.test_month(text)) {
                features.month = true;
            }

            if (text.indexOf("-") != -1) {
                features.containDash = true;
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

            Matcher m3 = featureFactory.EMAIL.matcher(text);
            if (m3.find()) {
                features.email = true;
            }

            Matcher m4 = featureFactory.HTTP.matcher(text);
            if (m4.find()) {
                features.http = true;
            }

            if (features.punctType == null)
                features.punctType = "NOPUNCT";

            if (restrict) {
                if ((tag.equals("<title>")) | (tag.equals("<date>")))
                    features.label = tag;
                else
                    features.label = "<other>";
            } else
                features.label = tag;

            header.append(features.printVector(true));

            previousTag = tag;
            previousText = text;
            n++;
        }

        return header.toString();
    }


}