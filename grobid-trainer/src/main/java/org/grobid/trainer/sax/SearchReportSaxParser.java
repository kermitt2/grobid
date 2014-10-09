package org.grobid.trainer.sax;

import org.grobid.core.data.Classification;
import org.grobid.core.exceptions.GrobidException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.Writer;
import java.util.ArrayList;

/**
 * SAX parser for the search report information of a patent document.
 *
 * @author Patrice Lopez
 */
public class SearchReportSaxParser extends DefaultHandler {

    private StringBuffer accumulator = new StringBuffer(); // Accumulate parsed text
    private StringBuffer accumulatorRef = new StringBuffer(); // Accumulate parsed text

    private String PatentNumber = null;
    private String Global_Language_Code = null;
    private String Content = null;
    private ArrayList<String> CitedPatentNumber = null;
    private ArrayList<Integer> CitationID = null;

    private boolean localDocument = false;

    // working variables
    private String local_cat = null;
    private String cited_number = null;
    private String current_classification_scheme = null;

    // for storing the path to the original file
    private String Path = null;

    public ArrayList<String> referencesPatent = null;
    public ArrayList<String> referencesNPL = null;
    public ArrayList<String> citations = null; // search report citations
    public ArrayList<Classification> classes = null;

    private boolean writeFiles = false;
    private Writer writer = null;

    private boolean npl = false; // indicate if the current reference is to patent or to a npl
    private boolean ref = false; // are we reading a ref?

    private boolean refFound = false;

    private int nbNPLRef = 0;
    private int nbPatentRef = 0;
    public int nbAllRef = 0;

    public boolean patentReferences = false;
    public boolean nplReferences = false;

    // we apply an aggressive tokenization where we keep as token only sequences
    // of letters or digits,
    // any other characters being a new token
    private static final String rege = "[a-zA-Z\\u00E9\\u00E8\\u00E0\\u00E4\\u00F6\\u00EF\\u00FC\\u00F9\\u00E7\\u00C0\\u00C9\\u00C8\\u00DF\\u00F4\\u00E2\\u00EA\\u00EE\\u00FB]+|[0-9]+|\\S";

    public SearchReportSaxParser() {
    }

    public SearchReportSaxParser(String dirPath, Writer writer0) {
        Path = dirPath;
        writeFiles = true;

        //CLEANER = Pattern.compile("(\\()([\\d, ][a-z]?)+(\\))");
        //patent_pattern = Pattern.compile("([UEWDJFA])[\\.\\s]?([SPOERKU])[\\.\\s]?-?(A|B)?\\s?-?([\\s,0-9/-]+)");

        writer = writer0;
    }

    public void characters(char[] buffer, int start, int length) {
        if (ref)
            accumulatorRef.append(buffer, start, length);
        else
            accumulator.append(buffer, start, length);
    }

    public String getText() {
        //System.out.println(accumulator.toString().trim());
        return accumulator.toString().trim();
    }

    public int getNbNPLRef() {
        return nbNPLRef;
    }

    public int getNbPatentRef() {
        return nbPatentRef;
    }

    public int getNbPatentCitations() {
        return nbPatentRef;
    }

    public int getNbNPLCitations() {
        return nbNPLRef;
    }

    public String getRefText() {
        return accumulatorRef.toString().trim();
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {
        if ((qName.equals("citPatent")) | (qName.equals("citNPL"))) {
            String refString = getRefText();
            refString = refString.replace("\n", " ");
            refString = refString.replace("  ", " ");

            if (npl & ref) {
                if (referencesNPL == null)
                    referencesNPL = new ArrayList<String>();
                referencesNPL.add(refString);
                refFound = true;
                if (nplReferences)
                    nbNPLRef++;
            } else if (ref) {
                if (referencesPatent == null)
                    referencesPatent = new ArrayList<String>();
                referencesPatent.add(refString);
                refFound = true;
                if (patentReferences)
                    nbPatentRef++;
            }

            if (refFound) {
                if (writeFiles) {
                    // we tokenize the text
                    TokenizerFactory tf = new RegExTokenizerFactory(rege);
                    char[] cs = refString.toCharArray();
                    Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
                    String token = tokenizer.nextToken();
                    int i = 0;
                    while (token != null) {
                        try {
                            writer.write(token + "\t");
                            token = tokenizer.nextToken();
                            if (npl) {
                                if (i == 0)
                                    writer.write("citNPLBegin\n");
                                    //else if (token == null)
                                    //writer.write("citNPLEnd\n");
                                else
                                    writer.write("citNPL\n");
                            } else {
                                if (i == 0)
                                    writer.write("citPatentBegin\n");
                                    //else if (token == null)
                                    //writer.write("citPatentEnd\n");
                                else
                                    writer.write("citPatent\n");
                            }
                        } catch (Exception e) {
//							e.printStackTrace();
                            throw new GrobidException("An exception occurred while running Grobid.", e);
                        }
                        i++;
                    }
                }
            }

            // we register the citation, the citation context will be marked in a later stage
            /*if (citations == null)
                   citations = new ArrayList<String>();
               citations.add(cited_number);
                   //pstmt.setString(3,local_cat);
               accumulator.setLength(0);
               */
            ref = false;
        } else if (qName.equals("classification-scheme")) {
            current_classification_scheme = getRefText();
            //cl.classes.add(current_classification_scheme);
            if (writeFiles) {
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = current_classification_scheme.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
                String token = tokenizer.nextToken();
                int i = 0;
                while (token != null) {
                    try {
                        writer.write(token + "\t");
                        token = tokenizer.nextToken();
                        if (i == 0)
                            writer.write("classification-schemeBegin\n");
                            //else if (token == null)
                            //writer.write("classification-schemeEnd\n");
                        else
                            writer.write("classification-scheme\n");
                    } catch (Exception e) {
//						e.printStackTrace();
                        throw new GrobidException("An exception occured while running Grobid.", e);
                    }
                    i++;
                }
            }
            accumulatorRef.setLength(0);
        } else if (qName.equals("classification-symbol")) {
            Classification cl = new Classification();
            cl.setClassificationScheme(current_classification_scheme);
            if (cl.getClasses() == null)
                cl.setClasses(new ArrayList<String>());
            String cla = getRefText();
            cl.getClasses().add(cla);
            if (writeFiles) {
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = cla.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
                String token = tokenizer.nextToken();
                int i = 0;
                while (token != null) {
                    try {
                        writer.write(token + "\t");
                        token = tokenizer.nextToken();
                        if (i == 0)
                            writer.write("classification-symbolBegin\n");
                            //else if (token == null)
                            //writer.write("classification-symbolEnd\n");
                        else
                            writer.write("classification-symbol\n");
                    } catch (Exception e) {
//						e.printStackTrace();
                        throw new GrobidException("An exception occured while running Grobid.", e);
                    }
                    i++;
                }
            }
            accumulatorRef.setLength(0);
        } else if (qName.equals("category")) {
            String cat = getRefText();
            //cl.classes.add(current_classification_scheme);
            if (writeFiles) {
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = cat.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
                String token = tokenizer.nextToken();
                int i = 0;
                while (token != null) {
                    try {
                        writer.write(token + "\t");
                        token = tokenizer.nextToken();
                        if (i == 0)
                            writer.write("categoryBegin\n");
                            //else if (token == null)
                            //writer.write("categoryEnd\n");
                        else
                            writer.write("category\n");
                    } catch (Exception e) {
//						e.printStackTrace();
                        throw new GrobidException("An exception occured while running Grobid.", e);
                    }
                    i++;
                }
            }
            accumulatorRef.setLength(0);
        } else if (qName.equals("passage")) {
            String passage = getRefText();
            //cl.classes.add(current_classification_scheme);
            if (writeFiles) {
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = passage.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
                String token = tokenizer.nextToken();
                int i = 0;
                while (token != null) {
                    try {
                        writer.write(token + "\t");
                        token = tokenizer.nextToken();
                        if (i == 0)
                            writer.write("passageBegin\n");
                            //else if (token == null)
                            //writer.write("passageEnd\n");
                        else
                            writer.write("passage\n");
                    } catch (Exception e) {
//						e.printStackTrace();
                        throw new GrobidException("An exception occured while running Grobid.", e);
                    }
                    i++;
                }
            }
            accumulatorRef.setLength(0);
        } else if (qName.equals("claims")) {
            String passage = getRefText();
            //cl.classes.add(current_classification_scheme);
            if (writeFiles) {
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = passage.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
                String token = tokenizer.nextToken();
                int i = 0;
                while (token != null) {
                    try {
                        writer.write(token + "\t");
                        token = tokenizer.nextToken();
                        if (i == 0)
                            writer.write("claimsBegin\n");
                            //else if (token == null)
                            //writer.write("claimsEnd\n");
                        else
                            writer.write("claims\n");
                    } catch (Exception e) {
//						e.printStackTrace();
                        throw new GrobidException("An exception occured while running Grobid.", e);
                    }
                    i++;
                }
            }
            accumulatorRef.setLength(0);
        } else if (qName.equals("date")) {
            String date = getRefText();
            //cl.classes.add(current_classification_scheme);
            if (writeFiles) {
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = date.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
                String token = tokenizer.nextToken();
                int i = 0;
                while (token != null) {
                    try {
                        writer.write(token + "\t");
                        token = tokenizer.nextToken();
                        if (i == 0)
                            writer.write("dateBegin\n");
                            //else if (token == null)
                            //writer.write("dateEnd\n");
                        else
                            writer.write("date\n");
                    } catch (Exception e) {
//						e.printStackTrace();
                        throw new GrobidException("An exception occured while running Grobid.", e);
                    }
                    i++;
                }
            }
            accumulatorRef.setLength(0);
        } else if (qName.equals("text")) {
            //if (refFound) {
            String content = getText();

            // we tokenize the text
            TokenizerFactory tf = new RegExTokenizerFactory(rege);
            char[] cs = content.toCharArray();
            Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);

            if (writeFiles) {
                // we print only a window of N words
                String token = tokenizer.nextToken();
                //int i = 0;
                while (token != null) {
                    try {
                        token = token.trim();
                        if (token.length() > 0) {
                            writer.write(token + "\t" + "other\n");
                        }
                    } catch (Exception e) {
//							e.printStackTrace();
                        throw new GrobidException("An exception occured while running Grobid.", e);
                    }
                    token = tokenizer.nextToken();
                    //i++;
                }
            }

            accumulator.setLength(0);
            //}
        }

    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts) throws SAXException {
        if (qName.equals("text")) {
            nbNPLRef = 0;
            nbPatentRef = 0;
            nbAllRef = 0;
            int length = atts.getLength();

            // Process each attribute
            for (int i = 0; i < length; i++) {
                // Get names and values for each attribute
                String name = atts.getQName(i);
                String value = atts.getValue(i);

                if (name != null) {
                    if (name.equals("lang")) {
                        Global_Language_Code = value.toLowerCase();
                    }
                }
            }

            CitedPatentNumber = new ArrayList<String>();
            accumulatorRef.setLength(0);
            accumulator.setLength(0);
        } else if (qName.equals("classification-symbol")) {
            if (writeFiles) {
                String content = getText();

                // we output what has been read so far in the description
                // we tokenize the text
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = content.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);

                String[] tokens = tokenizer.tokenize();
                int nbTokens = tokens.length;

                //String token = tokenizer.nextToken();
                //while(token != null) {
                for (int j = 0; j < nbTokens; j++) {
                    //if ( (j > (nbTokens-N) ) | (refFound & (j < N) ) ){
                    String token = tokens[j].trim();
                    if (token.length() > 0) {
                        try {
                            writer.write(tokens[j] + "\t" + "other\n");
                        } catch (Exception e) {
//								e.printStackTrace();
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                    //}
                }
            }
            accumulatorRef.setLength(0);
            accumulator.setLength(0);
            ref = true;
        } else if (qName.equals("classification-scheme")) {
            if (writeFiles) {
                String content = getText();

                // we output what has been read so far in the description
                // we tokenize the text
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = content.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);

                String[] tokens = tokenizer.tokenize();
                int nbTokens = tokens.length;

                //String token = tokenizer.nextToken();
                //while(token != null) {
                for (int j = 0; j < nbTokens; j++) {
                    //if ( (j > (nbTokens-N) ) | (refFound & (j < N) ) ){
                    String token = tokens[j].trim();
                    if (token.length() > 0) {
                        try {
                            writer.write(tokens[j] + "\t" + "other\n");
                        } catch (Exception e) {
//								e.printStackTrace();
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                    //}
                }
            }
            accumulatorRef.setLength(0);
            accumulator.setLength(0);
            ref = true;
        } else if (qName.equals("category")) {
            if (writeFiles) {
                String content = getText();

                // we output what has been read so far in the description
                // we tokenize the text
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = content.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);

                String[] tokens = tokenizer.tokenize();
                int nbTokens = tokens.length;

                //String token = tokenizer.nextToken();
                //while(token != null) {
                for (int j = 0; j < nbTokens; j++) {
                    //if ( (j > (nbTokens-N) ) | (refFound & (j < N) ) ){
                    String token = tokens[j].trim();
                    if (token.length() > 0) {
                        try {
                            writer.write(tokens[j] + "\t" + "other\n");
                        } catch (Exception e) {
//								e.printStackTrace();
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                    //}
                }
            }
            accumulatorRef.setLength(0);
            accumulator.setLength(0);
            ref = true;
        } else if (qName.equals("passage")) {
            if (writeFiles) {
                String content = getText();

                // we output what has been read so far in the description
                // we tokenize the text
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = content.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);

                String[] tokens = tokenizer.tokenize();
                int nbTokens = tokens.length;

                //String token = tokenizer.nextToken();
                //while(token != null) {
                for (int j = 0; j < nbTokens; j++) {
                    //if ( (j > (nbTokens-N) ) | (refFound & (j < N) ) ){
                    String token = tokens[j].trim();
                    if (token.length() > 0) {
                        try {
                            writer.write(tokens[j] + "\t" + "other\n");
                        } catch (Exception e) {
//								e.printStackTrace();
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                    //}
                }
            }
            accumulatorRef.setLength(0);
            accumulator.setLength(0);
            ref = true;
        } else if (qName.equals("claims")) {
            if (writeFiles) {
                String content = getText();

                // we output what has been read so far in the description
                // we tokenize the text
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = content.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);

                String[] tokens = tokenizer.tokenize();
                int nbTokens = tokens.length;

                //String token = tokenizer.nextToken();
                //while(token != null) {
                for (int j = 0; j < nbTokens; j++) {
                    //if ( (j > (nbTokens-N) ) | (refFound & (j < N) ) ){
                    String token = tokens[j].trim();
                    if (token.length() > 0) {
                        try {
                            writer.write(tokens[j] + "\t" + "other\n");
                        } catch (Exception e) {
//								e.printStackTrace();
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                    //}
                }
            }
            accumulatorRef.setLength(0);
            accumulator.setLength(0);
            ref = true;
        } else if (qName.equals("date")) {
            if (writeFiles) {
                String content = getText();

                // we output what has been read so far in the description
                // we tokenize the text
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = content.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);

                String[] tokens = tokenizer.tokenize();
                int nbTokens = tokens.length;

                //String token = tokenizer.nextToken();
                //while(token != null) {
                for (int j = 0; j < nbTokens; j++) {
                    //if ( (j > (nbTokens-N) ) | (refFound & (j < N) ) ){
                    String token = tokens[j].trim();
                    if (token.length() > 0) {
                        try {
                            writer.write(tokens[j] + "\t" + "other\n");
                        } catch (Exception e) {
//								e.printStackTrace();
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                    //}
                }
            }
            accumulatorRef.setLength(0);
            accumulator.setLength(0);
            ref = true;
        } else if ((qName.equals("citPatent")) | (qName.equals("citNPL"))) {
            accumulatorRef.setLength(0);
            if (writeFiles) {
                String content = getText();

                // we output what has been read so far in the description
                // we tokenize the text
                TokenizerFactory tf = new RegExTokenizerFactory(rege);
                char[] cs = content.toCharArray();
                Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);

                String[] tokens = tokenizer.tokenize();
                int nbTokens = tokens.length;

                //String token = tokenizer.nextToken();
                //while(token != null) {
                for (int j = 0; j < nbTokens; j++) {
                    //if ( (j > (nbTokens-N) ) | (refFound & (j < N) ) ){
                    String token = tokens[j].trim();
                    if (token.length() > 0) {
                        try {
                            writer.write(tokens[j] + "\t" + "other\n");
                        } catch (Exception e) {
//								e.printStackTrace();
                            throw new GrobidException("An exception occured while running Grobid.", e);
                        }
                    }
                    //}
                }
            }

            accumulator.setLength(0);

            if (qName.equals("citPatent")) {
                npl = false;
                ref = true;
            } else if (qName.equals("citNPL")) {
                npl = true;
                ref = true;
            }
        }

    }

}
