package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Person;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.engines.tagging.TaggerFactory;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorName;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Patrice Lopez
 */
public class AuthorParser {
	private static Logger LOGGER = LoggerFactory.getLogger(AuthorParser.class);
    private final GenericTagger namesHeaderParser;
    private final GenericTagger namesCitationParser;
	protected GrobidAnalyzer analyzer = GrobidAnalyzer.getInstance();
	
    public AuthorParser() {

//        namesHeaderModel = ModelMap.getModel(GrobidModels.NAMES_HEADER);
//        namesCitationModel = ModelMap.getModel(GrobidModels.NAMES_CITATION);
        namesHeaderParser = TaggerFactory.getTagger(GrobidModels.NAMES_HEADER);
        namesCitationParser = TaggerFactory.getTagger(GrobidModels.NAMES_CITATION);
    }

    /**
     * Processing of authors in header or citation
	 *
	 * @param inputs - list of sequence of author names to be processed.
	 * @param head - if true use the model for header's name, otherwise the model for names in citation
	 * @return List of Person entites as POJO.
     */
    public List<Person> processing(List<String> inputs,
                                   boolean head) {
        if (inputs == null)
            return null;

        if (inputs.size() == 0)
            return null;

        if (inputs.get(inputs.size() - 1) != null) {
            String last = inputs.get(inputs.size() - 1).trim();
            inputs.set(inputs.size() - 1, last.replaceAll("et\\.? al\\.?.*$", ""));
        }

        List<Person> fullAuthors = null;

        ArrayList<String> authorBlocks = new ArrayList<String>();
        try {
            for (String input : inputs) {
                if (input == null)
                    continue;
                //System.out.println(input);

                //StringTokenizer st = new StringTokenizer(input, TextUtilities.fullPunctuations, true);
				// TBD: add the language object in the tokenizer call
				List<String> tokenizations = analyzer.tokenize(input);

                //if (st.countTokens() == 0)
				if (tokenizations.size() == 0)
                    return null;
                //while (st.hasMoreTokens()) {
                //    String tok = st.nextToken();
				for(String tok : tokenizations) {
                    if (!tok.equals(" ")) {
                        authorBlocks.add(tok + " <author>");
                    }
                }
                authorBlocks.add("\n");
                //System.out.println(authorBlocks);
            }

            String header = FeaturesVectorName.addFeaturesName(authorBlocks);
            // clear internal context
//            Tagger tagger = head ? taggerHeader : taggerCitation;
            GenericTagger tagger = head ? namesHeaderParser : namesCitationParser;

            String res = tagger.label(header);
//            StringTokenizer st = new StringTokenizer(header, "\n");
//            AbstractParser.feedTaggerAndParse(tagger, st);

//            StringBuilder res = new StringBuilder();
//            for (int i = 0; i < tagger.size(); i++) {
//                for (int j = 0; j < tagger.xsize(); j++) {
//                    //System.out.print(tagger.x(i, j) + "\t");
//                    res.append(tagger.x(i, j)).append("\t");
//                }
//
//                res.append("<author>" + "\t");
//                res.append(tagger.y2(i));
//                /*if (line.length() == 0)
//                        res.append(" ");*/
//                res.append("\n");
//
//                /*System.out.print("Details");
//                for (int j = 0; j < tagger.ysize(); ++j) {
//                      System.out.print("\t" + tagger.yname(j) + "/prob=" + tagger.prob(i,j)
//                               + "/alpha=" + tagger.alpha(i, j)
//                               + "/beta=" + tagger.beta(i, j));
//                }
//                System.out.print("\n");*/
//            }
//
//            tagger.delete();

            // extract results from the processed file
            StringTokenizer st2 = new StringTokenizer(res, "\n");
            String lastTag = null;
            org.grobid.core.data.Person aut = new Person();
            boolean newMarker = false;
            String currentMarker = null;
            while (st2.hasMoreTokens()) {
                String line = st2.nextToken();
                if ((line.trim().length() == 0)) {
                    if (aut.notNull()) {
                        if (fullAuthors == null)
                            fullAuthors = new ArrayList<Person>();
                        fullAuthors.add(aut);
                    }
                    aut = new Person();
                    continue;
                }
                StringTokenizer st3 = new StringTokenizer(line, "\t");
                int ll = st3.countTokens();
                int i = 0;
                String s1 = null;
                String s2 = null;
                String s3 = "<author>";
                List<String> localFeatures = new ArrayList<String>();
                while (st3.hasMoreTokens()) {
                    String s = st3.nextToken().trim();
                    if (i == 0) {
                        s2 = s; // string
                        //System.out.println("s2 is " + s2);
                    } else if (i == ll - 2) {
                        //s3 = s; // pre-label, in this case it should always be <author>
                        //System.out.println("s3 is " + s3);
                    } else if (i == ll - 1) {
                        s1 = s; // label
                        //System.out.println("s1 is " + s1);
                    } else {
                        localFeatures.add(s);
                    }
                    i++;
                }

                if (s1.equals("<marker>")) {
                    if (aut.notNull()) {
                        if (fullAuthors == null) {
                            fullAuthors = new ArrayList<Person>();
                            fullAuthors.add(aut);
                            aut = new Person();
                        } else if (!fullAuthors.contains(aut)) {
                            fullAuthors.add(aut);
                            aut = new Person();
                        }
                    }

                    if (currentMarker == null)
                        currentMarker = s2;
                    else
                        currentMarker += " " + s2;
                    newMarker = true;
                } else if (s1.equals("I-<marker>")) {
                    if (aut.notNull()) {
                        if (fullAuthors == null)
                            fullAuthors = new ArrayList<Person>();
                        fullAuthors.add(aut);
                        aut = new Person();
                    }
                    currentMarker = s2;
                    newMarker = true;
                } else if (s1.equals("<forename>") || s1.equals("I-<forename>")) {
                    //System.out.println("forename !");
                    if (s3.equals("<author>")) {
                        if (newMarker) {
                            aut.setFirstName(s2);
                            newMarker = false;
                        } else if (aut.getFirstName() != null) {
                            if (s1.equals("I-<forename>")) {
                                // new author
                                if (aut.notNull()) {
                                    if (fullAuthors == null)
                                        fullAuthors = new ArrayList<Person>();
                                    fullAuthors.add(aut);
                                }

                                aut = new Person();
                                aut.setFirstName(s2);
                            } else if (!s1.equals(lastTag) && !lastTag.equals("I-<forename>")) {
                                // new author
                                if (aut.notNull()) {
                                    if (fullAuthors == null)
                                        fullAuthors = new ArrayList<Person>();
                                    fullAuthors.add(aut);
                                }
                                aut = new Person();
                                aut.setFirstName(s2);
                            } else if ((s2.equals("-")) || (s2.equals(".")) || (s2.equals(",")) || (s2.equals(":")))
                                aut.setFirstName(aut.getFirstName() + s2);
                            else {
                                if (aut.getFirstName().length() == 0)
                                    aut.setFirstName(aut.getFirstName() + s2);
                                else if ((aut.getFirstName().charAt(aut.getFirstName().length() - 1) == '-')
                                        | (aut.getFirstName().charAt(aut.getFirstName().length() - 1) == '\''))
                                    aut.setFirstName(aut.getFirstName() + s2);
                                else
                                    aut.setFirstName(aut.getFirstName() + " " + s2);
                            }
                        } else {
                            aut.setFirstName(s2);
                        }
                    }
                } else if (s1.equals("<middlename>") || s1.equals("I-<middlename>")) {
                    //System.out.println("middlename !");
                    if (s3.equals("<author>")) {
                        if (newMarker) {
                            aut.setMiddleName(s2);
                            newMarker = false;
                        } else if (aut.getMiddleName() != null) {
                            if ((s2.equals("-")) || (s2.equals(".")) || (s2.equals(",")) || (s2.equals(":")))
                                aut.setMiddleName(aut.getMiddleName() + s2);
                            else {
                                if (aut.getMiddleName().length() == 0)
                                    aut.setMiddleName(aut.getMiddleName() + s2);
                                else if (
                                        (aut.getMiddleName().charAt(aut.getMiddleName().length() - 1) == '-') |
                                                (aut.getMiddleName().charAt(aut.getMiddleName().length() - 1) == '\''))
                                    aut.setMiddleName(aut.getMiddleName() + s2);
                                else
                                    aut.setMiddleName(aut.getMiddleName() + " " + s2);
                            }
                        } else {
                            aut.setMiddleName(s2);
                        }
                    }
                } else if (s1.equals("<surname>") || s1.equals("I-<surname>")) {
                    if (s3.equals("<author>")) {
                        if (newMarker) {
                            aut.setLastName(s2);
                            newMarker = false;
                        } else if (aut.getLastName() != null) {
                            if (s1.equals("I-<surname>")) {
                                // new author
                                if (aut.notNull()) {
                                    if (fullAuthors == null)
                                        fullAuthors = new ArrayList<Person>();
                                    fullAuthors.add(aut);
                                }
                                aut = new Person();
                                aut.setLastName(s2);
                            } else if ((!s1.equals(lastTag)) && (!lastTag.equals("I-<surname>"))) {
                                // new author
                                if (aut.notNull()) {
                                    if (fullAuthors == null)
                                        fullAuthors = new ArrayList<Person>();
                                    fullAuthors.add(aut);
                                }
                                aut = new Person();
                                aut.setLastName(s2);
                            } else if ((s2.equals("-")) || (s2.equals(".")) || (s2.equals(",")) || (s2.equals(":"))) {
                                aut.setLastName(aut.getLastName() + s2);
                            } else {
                                if (aut.getLastName().length() == 0)
                                    aut.setLastName(s2);
                                else if (
                                        (aut.getLastName().charAt(aut.getLastName().length() - 1) == '-') |
                                                (aut.getLastName().charAt(aut.getLastName().length() - 1) == '\''))
                                    aut.setLastName(aut.getLastName() + s2);
                                else
                                    aut.setLastName(aut.getLastName() + " " + s2);
                            }
                        } else {
                            aut.setLastName(s2);
                        }
                    }
                } else if (s1.equals("<title>") || s1.equals("I-<title>")) {
                    //System.out.println("<title> ! " + "lastTag: " + lastTag);
                    if (s3.equals("<author>")) {
                        if (newMarker) {
                            aut.setTitle(s2);
                            newMarker = false;
                        } else if (aut.getTitle() != null) {
                            if (s1.equals("I-<title>")) {
                                // new author
                                if (aut.notNull()) {
                                    if (fullAuthors == null)
                                        fullAuthors = new ArrayList<Person>();
                                    fullAuthors.add(aut);
                                }
                                aut = new Person();
                                aut.setTitle(s2);
                            } else if ((!s1.equals(lastTag)) && (!lastTag.equals("I-<title>"))) {
                                // new author
                                if (aut.notNull()) {
                                    if (fullAuthors == null)
                                        fullAuthors = new ArrayList<Person>();
                                    fullAuthors.add(aut);
                                }
                                aut = new Person();
                                aut.setTitle(s2);
                            } else if ((s2.equals("-")) || (s2.equals(".")) || (s2.equals(",")) || (s2.equals(":"))) {
                                aut.setTitle(aut.getTitle() + s2);
                            } else {
                                if (aut.getTitle().length() == 0) {
                                    aut.setTitle(aut.getTitle() + s2);
                                } else if (
                                        (aut.getTitle().charAt(aut.getTitle().length() - 1) == '-') |
                                                (aut.getTitle().charAt(aut.getTitle().length() - 1) == '\'')) {
                                    aut.setTitle(aut.getTitle() + s2);
                                } else {
                                    aut.setTitle(aut.getTitle() + " " + s2);
                                }
                            }
                        } else {
                            aut.setTitle(s2);
                        }
                    }
                } else if (s1.equals("<suffix>") || s1.equals("I-<suffix>")) {
                    //System.out.println("<suffix> ! " + "lastTag: " + lastTag);
                    if (s3.equals("<suffix>")) {
                        if (newMarker) {
                            aut.setSuffix(s2);
                            newMarker = false;
                        } else if (aut.getSuffix() != null) {
                            if (s1.equals("I-<suffix>")) {
                                // new author
                                if (aut.notNull()) {
                                    if (fullAuthors == null)
                                        fullAuthors = new ArrayList<Person>();
                                    fullAuthors.add(aut);
                                }
                                aut = new Person();
                                aut.setSuffix(s2);
                            } else if ((!s1.equals(lastTag)) && (!lastTag.equals("I-<suffix>"))) {
                                // new author
                                if (aut.notNull()) {
                                    if (fullAuthors == null)
                                        fullAuthors = new ArrayList<Person>();
                                    fullAuthors.add(aut);
                                }
                                aut = new Person();
                                aut.setSuffix(s2);
                            } else if ((s2.equals("-")) || (s2.equals(".")) || (s2.equals(",")) || (s2.equals(":"))) {
                                aut.setSuffix(aut.getSuffix() + s2);
                            } else {
                                if (aut.getSuffix().length() == 0) {
                                    aut.setSuffix(aut.getSuffix() + s2);
                                } else if (
                                        (aut.getSuffix().charAt(aut.getSuffix().length() - 1) == '-') |
                                                (aut.getSuffix().charAt(aut.getSuffix().length() - 1) == '\'')) {
                                    aut.setSuffix(aut.getSuffix() + s2);
                                } else {
                                    aut.setSuffix(aut.getSuffix() + " " + s2);
                                }
                            }
                        } else {
                            aut.setSuffix(s2);
                        }
                    }
                }

                lastTag = s1;
            }
            if (aut.notNull()) {
                if (fullAuthors == null) {
                    fullAuthors = new ArrayList<Person>();
                }
                fullAuthors.add(aut);
            }

        } catch (Exception e) {
//			e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return fullAuthors;
    }

    /**
     * Extract results from a list of name strings in the training format without any string modification.
	 *
	 * @param inputs - list of sequence of author names to be processed.
	 * @param head - if true use the model for header's name, otherwise the model for names in citation
	 * @return the pseudo-TEI training data
	 */
    public StringBuilder trainingExtraction(List<String> inputs,
                                           boolean head) {
        StringBuilder buffer = new StringBuilder();
        try {
            if (inputs == null) {
                return null;
            }
            if (inputs.size() == 0) {
                return null;
            }
           	List<String> tokenizations = null;
            List<String> authorBlocks = new ArrayList<String>();
            for (String input : inputs) {
                if (input == null)
                    continue;
                //System.out.println("Input: "+input);
                //StringTokenizer st = new StringTokenizer(input, " \t\n" + TextUtilities.fullPunctuations, true);
				tokenizations = analyzer.tokenize(input);
                //if (st.countTokens() == 0)
				if (tokenizations.size() == 0)
                    return null;
                //while (st.hasMoreTokens()) {
                //    String tok = st.nextToken();
				for(String tok : tokenizations) {
                    if (tok.equals("\n")) {
                        authorBlocks.add("@newline");
                    } else if (!tok.equals(" ")) {
                        authorBlocks.add(tok + " <author>");
                    }
                    //tokenizations.add(tok);
                }
                authorBlocks.add("\n");
            }

            String header = FeaturesVectorName.addFeaturesName(authorBlocks);
            // clear internal context
//            Tagger tagger = head ? taggerHeader : taggerCitation;
            GenericTagger tagger = head ? namesHeaderParser : namesCitationParser;
            String res = tagger.label(header);

            // extract results from the processed file
            StringTokenizer st2 = new StringTokenizer(res, "\n");
            String lastTag = null;
            boolean start = true;
            boolean hasMarker = false;
            boolean hasSurname = false;
            boolean hasForename = false;
            boolean tagClosed;
            int q = 0;
            boolean addSpace;
            String lastTag0;
            String currentTag0;
            while (st2.hasMoreTokens()) {
                String line = st2.nextToken();
                addSpace = false;
                if ((line.trim().length() == 0)) {
                    // new author
					if (head)
                    	buffer.append("/t<author>\n");
					else {
						//buffer.append("<author>");
					}
                    continue;
                } else {
                    String theTok = tokenizations.get(q);
                    while (theTok.equals(" ")) {
                        addSpace = true;
                        q++;
                        theTok = tokenizations.get(q);
                    }
                    q++;
                }

                StringTokenizer st3 = new StringTokenizer(line, "\t");
                int ll = st3.countTokens();
                int i = 0;
                String s1 = null;
                String s2 = null;
                boolean newLine = false;
                List<String> localFeatures = new ArrayList<String>();
                while (st3.hasMoreTokens()) {
                    String s = st3.nextToken().trim();
                    if (i == 0) {
                        s2 = TextUtilities.HTMLEncode(s); // string
                    } else if (i == ll - 2) {
                    } else if (i == ll - 1) {
                        s1 = s; // label
                    } else {
                        localFeatures.add(s);
                        if (s.equals("LINESTART") && !start) {
                            newLine = true;
                            start = false;
                        } else if (s.equals("LINESTART")) {
                            start = false;
                        }
                    }
                    i++;
                }

                lastTag0 = null;
                if (lastTag != null) {
                    if (lastTag.startsWith("I-")) {
                        lastTag0 = lastTag.substring(2, lastTag.length());
                    } else {
                        lastTag0 = lastTag;
                    }
                }
                currentTag0 = null;
                if (s1 != null) {
                    if (s1.startsWith("I-")) {
                        currentTag0 = s1.substring(2, s1.length());
                    } else {
                        currentTag0 = s1;
                    }
                }

                tagClosed = lastTag0 != null && testClosingTag(buffer, currentTag0, lastTag0, head);

                if (newLine) {
                    if (tagClosed) {
                        buffer.append("\t\t\t\t\t\t\t<lb/>\n");
                    } else {
                        buffer.append("<lb/>");
                    }

                }

                String output = writeField(s1, lastTag0, s2, "<marker>", "<marker>", addSpace, 8, head);
                if (output != null) {
                    if (hasMarker) {
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t</persName>\n");
                        } else {
                            //buffer.append("</author>\n");
                        }
                        hasForename = false;
                        hasSurname = false;
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t<persName>\n");
                        } else {
                            //buffer.append("<author>\n");
                        }
                        hasMarker = true;
                    }
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                } else {
                    output = writeField(s1, lastTag0, s2, "<other>", "<other>", addSpace, 8, head);
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<forename>", "<forename>", addSpace, 8, head);
                } else {
                    if (buffer.length() > 0) {
                        if (buffer.charAt(buffer.length() - 1) == '\n') {
                            buffer.deleteCharAt(buffer.length() - 1);
                        }
                    }
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<middlename>", "<middlename>", addSpace, 8, head);
                } else {
                    if (hasForename && !currentTag0.equals(lastTag0)) {
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t</persName>\n");
                        } else {
                            //buffer.append("</author>\n");
                        }
                        hasMarker = false;
                        hasSurname = false;
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t<persName>\n");
                        } else {
                            //buffer.append("<author>\n");
                        }
                    }
                    hasForename = true;
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<surname>", "<surname>", addSpace, 8, head);
                } else {
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<title>", "<roleName>", addSpace, 8, head);
                } else {
                    if (hasSurname && !currentTag0.equals(lastTag0)) {
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t</persName>\n");
                        } else {
                            //buffer.append("</author>\n");
                        }
                        hasMarker = false;
                        hasForename = false;
                        if (head) {
                            buffer.append("\t\t\t\t\t\t\t<persName>\n");
                        } else {
                            //buffer.append("<author>\n");
                        }
                    }
                    hasSurname = true;
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output == null) {
                    output = writeField(s1, lastTag0, s2, "<suffix>", "<suffix>", addSpace, 8, head);
                } else {
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }
                if (output != null) {
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }

                lastTag = s1;
            }

            if (lastTag != null) {
                if (lastTag.startsWith("I-")) {
                    lastTag0 = lastTag.substring(2, lastTag.length());
                } else {
                    lastTag0 = lastTag;
                }
                currentTag0 = "";
                testClosingTag(buffer, currentTag0, lastTag0, head);
            }
        } catch (Exception e) {
//			e.printStackTrace();
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return buffer;
    }

    private String writeField(String s1,
                              String lastTag0,
                              String s2,
                              String field,
                              String outField,
                              boolean addSpace,
                              int nbIndent, 
							  boolean head) {
        String result = null;
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            if ((s1.equals("<other>") || s1.equals("I-<other>"))) {
                if (addSpace)
                    result = " " + s2;
                else
                    result = s2;
            } else if ((s1.equals(lastTag0) || s1.equals("I-" + lastTag0))) {
                if (addSpace)
                    result = " " + s2;
                else
                    result = s2;
            } else {
                result = "";
				if (head) {
	                for (int i = 0; i < nbIndent; i++) {
	                    result += "\t";
	                }
				}
				if (addSpace)
					result += " " + outField + s2;
				else		
 					result += outField + s2;
            }
        }
        return result;
    }

    private boolean testClosingTag(StringBuilder buffer,
                                   String currentTag0,
                                   String lastTag0,
								   boolean head) {
        boolean res = false;
        if (!currentTag0.equals(lastTag0)) {
            res = true;
            // we close the current tag
            if (lastTag0.equals("<other>")) {
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<forename>")) {
                buffer.append("</forename>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<middlename>")) {
                buffer.append("</middlename>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<surname>")) {
                buffer.append("</surname>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<title>")) {
                buffer.append("</roleName>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<suffix>")) {
                buffer.append("</suffix>");
				if (head)
					buffer.append("\n");
            } else if (lastTag0.equals("<marker>")) {
                buffer.append("</marker>");
				if (head)
					buffer.append("\n");
            } else {
                res = false;
            }

        }
        return res;
    }

    /**
     * Processing of authors in citations
     */
    public List<Person> processingCitation(List<String> inputs) {
        return processing(inputs, false);
    }

    public List<Person> processingHeader(List<String> inputs) {
        return processing(inputs, true);
    }
	
    public void close() throws IOException {
    }
}