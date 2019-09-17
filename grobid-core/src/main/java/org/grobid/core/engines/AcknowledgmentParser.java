package org.grobid.core.engines;

/**
 * @created by Tanti
 */

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Acknowledgment;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorAcknowledgment;
import org.grobid.core.lang.Language;
import org.grobid.core.utilities.TextUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AcknowledgmentParser extends AbstractParser {
    public AcknowledgmentParser() {
        super(GrobidModels.ACKNOWLEDGMENT);
    }

    /**
     * Processing of acknowledgment in header
     */
    public List<Acknowledgment> processing(String input) {
        if (input == null)
            return null;

        List<Acknowledgment> acknowledgments = null;

        List<String> acknowledgmentBlocks = new ArrayList<String>();
        try {
            // force English language for the tokenization only
            List<String> tokenizations = analyzer.tokenize(input, new Language("en", 1.0));
            if (tokenizations.size() == 0)
                return null;
            for(String tok : tokenizations) {
                if (!tok.equals(" ") && !tok.equals("\n")) {
                    tok = tok.replaceAll("[ \n]", "");
                    acknowledgmentBlocks.add(tok + " <acknowledgment>");
                }
            }

            String headerAcknowledgment = FeaturesVectorAcknowledgment.addFeaturesAcknowledgment(acknowledgmentBlocks);
            String res = label(headerAcknowledgment);

            //System.out.print(res.toString());
            StringTokenizer st2 = new StringTokenizer(res, "\n");
            String lastTag = null;
            Acknowledgment acknowledgment = new Acknowledgment();
            int lineCount = 0;
            while (st2.hasMoreTokens()) {
                String line = st2.nextToken();
                if ((line.trim().length() == 0)) {
                    if (acknowledgment.isNotNull()) {
                        if (acknowledgments == null)
                            acknowledgments = new ArrayList<Acknowledgment>();
                        acknowledgments.add(acknowledgment);
                    }
                    acknowledgment = new Acknowledgment();
                    continue;
                }
                StringTokenizer st3 = new StringTokenizer(line, "\t ");
                int ll = st3.countTokens();
                int i = 0;
                String s1 = null;
                String s2 = null;
                while (st3.hasMoreTokens()) {
                    String s = st3.nextToken().trim();
                    if (i == 0) {
                        s2 = s; // string
                    } else if (i == ll - 1) {
                        s1 = s; // label
                    }
                    i++;
                }

                if (s1.equals("<affiliation>") || s1.equals("I-<affiliation>")) {
                    if (acknowledgment.getAffiliation() != null) {
                        if ((s1.equals("I-<affiliation>")) ||
                            (!s1.equals(lastTag) && !lastTag.equals("I-<affiliation>"))
                        ) {
                            // new acknowledgment
                            if (acknowledgment.isNotNull()) {
                                if (acknowledgments == null)
                                    acknowledgments = new ArrayList<Acknowledgment>();
                                acknowledgments.add(acknowledgment);
                            }

                            acknowledgment = new Acknowledgment();
                            acknowledgment.setAffiliation(s2);
                        } else {
                            if (acknowledgment.getAffiliation().length() == 0)
                                acknowledgment.setAffiliation(s2);
                            else
                                acknowledgment.setAffiliation(acknowledgment.getAffiliation() + " " + s2);
                        }
                    } else {
                        acknowledgment.setAffiliation(s2);
                    }
                } else if (s1.equals("<educationalInstitution>") || s1.equals("I-<educationalInstitution>")) {
                    if (acknowledgment.getEducationalInstitution() != null) {
                        if ((s1.equals("I-<educationalInstitution>")) ||
                            (!s1.equals(lastTag) && !lastTag.equals("I-<educationalInstitution>"))
                        ) {
                            // new acknowledgment
                            if (acknowledgment.isNotNull()) {
                                if (acknowledgments == null)
                                    acknowledgments = new ArrayList<Acknowledgment>();
                                acknowledgments.add(acknowledgment);
                            }

                            acknowledgment = new Acknowledgment();
                            acknowledgment.setEducationalInstitution(s2);
                        } else {
                            if (acknowledgment.getEducationalInstitution().length() == 0)
                                acknowledgment.setEducationalInstitution(s2);
                            else
                                acknowledgment.setEducationalInstitution(acknowledgment.getEducationalInstitution() + " " + s2);
                        }
                    } else {
                        acknowledgment.setEducationalInstitution(s2);
                    }
                } else if (s1.equals("<fundingAgency>") || s1.equals("I-<fundingAgency>")) {
                    if (acknowledgment.getFundingAgency() != null) {
                        if ((s1.equals("I-<fundingAgency>")) ||
                            (!s1.equals(lastTag) && !lastTag.equals("I-<fundingAgency>"))
                        ) {
                            // new acknowledgment
                            if (acknowledgment.isNotNull()) {
                                if (acknowledgments == null)
                                    acknowledgments = new ArrayList<Acknowledgment>();
                                acknowledgments.add(acknowledgment);
                            }

                            acknowledgment = new Acknowledgment();
                            acknowledgment.setFundingAgency(s2);
                        } else {
                            if (acknowledgment.getFundingAgency().length() == 0)
                                acknowledgment.setFundingAgency(s2);
                            else
                                acknowledgment.setFundingAgency(acknowledgment.getFundingAgency() + " " + s2);
                        }
                    } else {
                        acknowledgment.setFundingAgency(s2);
                    }
                }else if (s1.equals("<grantName>") || s1.equals("I-<grantName>")) {
                    if (acknowledgment.getGrantName() != null) {
                        if ((s1.equals("I-<grantName>")) ||
                            (!s1.equals(lastTag) && !lastTag.equals("I-<grantName>"))
                        ) {
                            // new acknowledgment
                            if (acknowledgment.isNotNull()) {
                                if (acknowledgments == null)
                                    acknowledgments = new ArrayList<Acknowledgment>();
                                acknowledgments.add(acknowledgment);
                            }

                            acknowledgment = new Acknowledgment();
                            acknowledgment.setGrantName(s2);
                        } else {
                            if (acknowledgment.getGrantName().length() == 0)
                                acknowledgment.setGrantName(s2);
                            else
                                acknowledgment.setGrantName(acknowledgment.getGrantName() + " " + s2);
                        }
                    } else {
                        acknowledgment.setGrantName(s2);
                    }
                } else if (s1.equals("<grantNumber>") || s1.equals("I-<grantNumber>")) {
                    if (acknowledgment.getGrantNumber() != null) {
                        if ((s1.equals("I-<grantNumber>")) ||
                            (!s1.equals(lastTag) && !lastTag.equals("I-<grantNumber>"))
                        ) {
                            // new acknowledgment
                            if (acknowledgment.isNotNull()) {
                                if (acknowledgments == null)
                                    acknowledgments = new ArrayList<Acknowledgment>();
                                acknowledgments.add(acknowledgment);
                            }

                            acknowledgment = new Acknowledgment();
                            acknowledgment.setGrantNumber(s2);
                        } else {
                            if (acknowledgment.getGrantNumber().length() == 0)
                                acknowledgment.setGrantNumber(s2);
                            else
                                acknowledgment.setGrantNumber(acknowledgment.getGrantNumber() + " " + s2);
                        }
                    } else {
                        acknowledgment.setGrantNumber(s2);
                    }
                } else if (s1.equals("<individual>") || s1.equals("I-<individual>")) {
                    if (acknowledgment.getIndividual() != null) {
                        if ((s1.equals("I-<individual>")) ||
                            (!s1.equals(lastTag) && !lastTag.equals("I-<individual>"))
                        ) {
                            // new acknowledgment
                            if (acknowledgment.isNotNull()) {
                                if (acknowledgments == null)
                                    acknowledgments = new ArrayList<Acknowledgment>();
                                acknowledgments.add(acknowledgment);
                            }

                            acknowledgment = new Acknowledgment();
                            acknowledgment.setIndividual(s2);
                        } else {
                            if (acknowledgment.getIndividual().length() == 0)
                                acknowledgment.setIndividual(s2);
                            else
                                acknowledgment.setIndividual(acknowledgment.getIndividual() + " " + s2);
                        }
                    } else {
                        acknowledgment.setIndividual(s2);
                    }
                } else if (s1.equals("<otherInstitution>") || s1.equals("I-<otherInstitution>")) {
                    if (acknowledgment.getOtherInstitution() != null) {
                        if ((s1.equals("I-<otherInstitution>")) ||
                            (!s1.equals(lastTag) && !lastTag.equals("I-<otherInstitution>"))
                        ) {
                            // new acknowledgment
                            if (acknowledgment.isNotNull()) {
                                if (acknowledgments == null)
                                    acknowledgments = new ArrayList<Acknowledgment>();
                                acknowledgments.add(acknowledgment);
                            }

                            acknowledgment = new Acknowledgment();
                            acknowledgment.setOtherInstitution(s2);
                        } else {
                            if (acknowledgment.getOtherInstitution().length() == 0)
                                acknowledgment.setOtherInstitution(s2);
                            else
                                acknowledgment.setOtherInstitution(acknowledgment.getOtherInstitution() + " " + s2);
                        }
                    } else {
                        acknowledgment.setOtherInstitution(s2);
                    }
                } else if (s1.equals("<projectName>") || s1.equals("I-<projectName>")) {
                    if (acknowledgment.getProjectName() != null) {
                        if ((s1.equals("I-<projectName>")) ||
                            (!s1.equals(lastTag) && !lastTag.equals("I-<projectName>"))
                        ) {
                            // new acknowledgment
                            if (acknowledgment.isNotNull()) {
                                if (acknowledgments == null)
                                    acknowledgments = new ArrayList<Acknowledgment>();
                                acknowledgments.add(acknowledgment);
                            }

                            acknowledgment = new Acknowledgment();
                            acknowledgment.setProjectName(s2);
                        } else {
                            if (acknowledgment.getProjectName().length() == 0)
                                acknowledgment.setProjectName(s2);
                            else
                                acknowledgment.setProjectName(acknowledgment.getProjectName() + " " + s2);
                        }
                    } else {
                        acknowledgment.setProjectName(s2);
                    }
                } else if (s1.equals("<researchInstitution>") || s1.equals("I-<researchInstitution>")) {
                    if (acknowledgment.getResearchInstitution() != null) {
                        if ((s1.equals("I-<researchInstitution>")) ||
                            (!s1.equals(lastTag) && !lastTag.equals("I-<researchInstitution>"))
                        ) {
                            // new acknowledgment
                            if (acknowledgment.isNotNull()) {
                                if (acknowledgments == null)
                                    acknowledgments = new ArrayList<Acknowledgment>();
                                acknowledgments.add(acknowledgment);
                            }

                            acknowledgment = new Acknowledgment();
                            acknowledgment.setResearchInstitution(s2);
                        } else {
                            if (acknowledgment.getResearchInstitution().length() == 0)
                                acknowledgment.setResearchInstitution(s2);
                            else
                                acknowledgment.setResearchInstitution(acknowledgment.getResearchInstitution() + " " + s2);
                        }
                    } else {
                        acknowledgment.setResearchInstitution(s2);
                    }
                }

                lastTag = s1;
                lineCount++;
            }
            if (acknowledgment.isNotNull()) {
                if (acknowledgments == null)
                    acknowledgments = new ArrayList<Acknowledgment>();
                acknowledgments.add(acknowledgment);
            }

        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return acknowledgments;
    }


    /**
     * Extract results from a date string in the training format without any string modification.
     */
    public StringBuilder trainingExtraction(List<String> inputs) {
        StringBuilder buffer = new StringBuilder();
        try {
            if (inputs == null)
                return null;

            if (inputs.size() == 0)
                return null;

            List<String> tokenizations = null;
            List<String> acknowledgmentBlocks = new ArrayList<String>();
            for (String input : inputs) {
                if (input == null)
                    continue;

                tokenizations = analyzer.tokenize(input);

                if (tokenizations.size() == 0)
                    return null;

                for(String tok : tokenizations) {
                    if (tok.equals("\n")) {
                        acknowledgmentBlocks.add("@newline");
                    } else if (!tok.equals(" ")) {
                        acknowledgmentBlocks.add(tok + " <acknowledgment>");
                    }
                }
                acknowledgmentBlocks.add("\n");
            }

            String headerAcknowledgment = FeaturesVectorAcknowledgment.addFeaturesAcknowledgment(acknowledgmentBlocks);
            String res = label(headerAcknowledgment);

            // extract results from the processed file

            StringTokenizer st2 = new StringTokenizer(res, "\n");
            String lastTag = null;
            boolean tagClosed = false;
            int q = 0;
            boolean addSpace;
            boolean hasAffiliation = false;
            boolean hasEducationalInstitution = false;
            boolean hasFundingAgency = false;
            boolean hasGrantName = false;
            boolean hasGrantNumber = false;
            boolean hasIndividual = false;
            boolean hasOtherInstitution = false;
            boolean hasProjectName = false;
            boolean hasResearchInstitution = false;

            String lastTag0;
            String currentTag0;
            boolean start = true;
            while (st2.hasMoreTokens()) {
                String line = st2.nextToken();
                addSpace = false;
                if ((line.trim().length() == 0)) {
                    // new acknowledgment
                    buffer.append("</acknowledgment>\n");

                    hasAffiliation = false;
                    hasEducationalInstitution = false;
                    hasFundingAgency = false;
                    hasGrantName = false;
                    hasGrantNumber = false;
                    hasIndividual = false;
                    hasOtherInstitution = false;
                    hasProjectName = false;
                    hasResearchInstitution = false;

                    buffer.append("\t<acknowledgment>");
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

                while (st3.hasMoreTokens()) {
                    String s = st3.nextToken().trim();
                    if (i == 0) {
                        s2 = TextUtilities.HTMLEncode(s); // string
                    }
                    else if (i == ll - 1) {
                        s1 = s; // label
                    }
                    i++;
                }

                if (start && (s1 != null)) {
                    buffer.append("\t<acknowledgment>");
                    start = false;
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

                tagClosed = lastTag0 != null && testClosingTag(buffer, currentTag0, lastTag0);

                String output = writeField(s1, lastTag0, s2, "<affiliation>", "<affiliation>", addSpace, 0);
                if (output != null) {
                    if (lastTag0 != null) {
                        if (hasAffiliation && !lastTag0.equals("<affiliation>")) {
                            buffer.append("</acknowledgment>\n");
                            hasEducationalInstitution = false;
                            hasFundingAgency = false;
                            hasGrantName = false;
                            hasGrantNumber = false;
                            hasIndividual = false;
                            hasOtherInstitution = false;
                            hasProjectName = false;
                            hasResearchInstitution = false;
                            buffer.append("\t<acknowledgment>");
                        }
                    }
                    hasAffiliation = true;
                    buffer.append(output);
                    lastTag = s1;
                    continue;
                }else {
                    output = writeField(s1, lastTag0, s2, "<other>", "<other>", addSpace, 0);
                }

                if(output == null){
                    output = writeField(s1, lastTag0, s2, "<educationalInstitution>", "<educationalInstitution>", addSpace, 0);
                } else {
                    if (lastTag0 != null) {
                        if (hasEducationalInstitution && !lastTag0.equals("<educationalInstitution>")) {
                            buffer.append("</acknowledgment>\n");
                            buffer.append("\t<acknowledgment>");
                        }
                    }
                    buffer.append(output);
                    hasEducationalInstitution = true;
                    lastTag = s1;
                    continue;
                }

                if(output == null){
                    output = writeField(s1, lastTag0, s2, "<fundingAgency>", "<fundingAgency>", addSpace, 0);
                } else {
                    if (lastTag0 != null) {
                        if (hasFundingAgency && !lastTag0.equals("<fundingAgency>")) {
                            buffer.append("</acknowledgment>\n");
                            buffer.append("\t<acknowledgment>");
                        }
                    }
                    buffer.append(output);
                    hasFundingAgency = true;
                    lastTag = s1;
                    continue;
                }

                if(output == null){
                    output = writeField(s1, lastTag0, s2, "<grantName>", "<grantName>", addSpace, 0);
                } else {
                    if (lastTag0 != null) {
                        if (hasGrantName && !lastTag0.equals("<grantName>")) {
                            buffer.append("</acknowledgment>\n");
                            buffer.append("\t<acknowledgment>");
                        }
                    }
                    buffer.append(output);
                    hasGrantName = true;
                    lastTag = s1;
                    continue;
                }

                if(output == null){
                    output = writeField(s1, lastTag0, s2, "<grantNumber>", "<grantNumber>", addSpace, 0);
                } else {
                    if (lastTag0 != null) {
                        if (hasGrantNumber && !lastTag0.equals("<grantNumber>")) {
                            buffer.append("</acknowledgment>\n");
                            buffer.append("\t<acknowledgment>");
                        }
                    }
                    buffer.append(output);
                    hasGrantNumber = true;
                    lastTag = s1;
                    continue;
                }

                if(output == null){
                    output = writeField(s1, lastTag0, s2, "<individual>", "<individual>", addSpace, 0);
                } else {
                    if (lastTag0 != null) {
                        if (hasIndividual && !lastTag0.equals("<individual>")) {
                            buffer.append("</acknowledgment>\n");
                            buffer.append("\t<acknowledgment>");
                        }
                    }
                    buffer.append(output);
                    hasIndividual = true;
                    lastTag = s1;
                    continue;
                }

                if(output == null){
                    output = writeField(s1, lastTag0, s2, "<otherInstitution>", "<otherInstitution>", addSpace, 0);
                } else {
                    if (lastTag0 != null) {
                        if (hasOtherInstitution && !lastTag0.equals("<otherInstitution>")) {
                            buffer.append("</acknowledgment>\n");
                            buffer.append("\t<acknowledgment>");
                        }
                    }
                    buffer.append(output);
                    hasOtherInstitution = true;
                    lastTag = s1;
                    continue;
                }

                if(output == null){
                    output = writeField(s1, lastTag0, s2, "<projectName>", "<projectName>", addSpace, 0);
                } else {
                    if (lastTag0 != null) {
                        if (hasProjectName && !lastTag0.equals("<projectName>")) {
                            buffer.append("</acknowledgment>\n");
                            buffer.append("\t<acknowledgment>");
                        }
                    }
                    buffer.append(output);
                    hasProjectName = true;
                    lastTag = s1;
                    continue;
                }

                if(output == null){
                    output = writeField(s1, lastTag0, s2, "<researchInstitution>", "<researchInstitution>", addSpace, 0);
                } else {
                    if (lastTag0 != null) {
                        if (hasResearchInstitution && !lastTag0.equals("<researchInstitution>")) {
                            buffer.append("</acknowledgment>\n");
                            buffer.append("\t<acknowledgment>");
                        }
                    }
                    buffer.append(output);
                    hasResearchInstitution = true;
                    lastTag = s1;
                    continue;
                }

            }

            if (lastTag != null) {
                if (lastTag.startsWith("I-")) {
                    lastTag0 = lastTag.substring(2, lastTag.length());
                } else {
                    lastTag0 = lastTag;
                }
                currentTag0 = "";
                testClosingTag(buffer, currentTag0, lastTag0);
                buffer.append("</acknowledgment>\n");
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
                              int nbIndent) {
        String result = null;
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            if ((s1.equals("<other>") || s1.equals("I-<other>"))) {
                if (addSpace)
                    result = " " + s2;
                else
                    result = s2;
            } else if (s1.equals(lastTag0) || s1.equals("I-" + lastTag0)) {
                if (addSpace)
                    result = " " + s2;
                else
                    result = s2;
            } else {
                result = "";
                for (int i = 0; i < nbIndent; i++) {
                    result += "\t";
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
                                   String lastTag0) {
        boolean res = false;
        if (!currentTag0.equals(lastTag0)) {
            res = true;
            // we close the current tag
            if (lastTag0.equals("<other>")) {
                buffer.append("");
            } else if (lastTag0.equals("<affiliation>")) {
                buffer.append("</affiliation>");
            } else if (lastTag0.equals("<educationalInstitution>")) {
                buffer.append("</educationalInstitution>");
            } else if (lastTag0.equals("<fundingAgency>")) {
                buffer.append("</fundingAgency>");
            } else if (lastTag0.equals("<grantName>")) {
                buffer.append("</grantName>");
            }else if (lastTag0.equals("<grantNumber>")) {
                buffer.append("</grantNumber>");
            }else if (lastTag0.equals("<individual>")) {
                buffer.append("</individual>");
            }else if (lastTag0.equals("<otherInstitution>")) {
                buffer.append("</otherInstitution>");
            }else if (lastTag0.equals("<projectName>")) {
                buffer.append("</projectName>");
            }else if (lastTag0.equals("<researchInstitution>")) {
                buffer.append("</researchInstitution>");
            }else {
                res = false;
            }

        }
        return res;
    }
}
