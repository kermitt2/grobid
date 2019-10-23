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

public class AcknowledgmentParserOld extends AbstractParser {
    public AcknowledgmentParserOld() {
        super(GrobidModels.ACKNOWLEDGMENT);
    }

    /**
     * Processing of acknowledgment
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
            for (String tok : tokenizations) {
                if (!tok.equals(" ") && !tok.equals("\n")) {
                    tok = tok.replaceAll("[ \n]", "");
                    acknowledgmentBlocks.add(tok + " <acknowledgment>");
                }
            }

            String headerAcknowledgment = FeaturesVectorAcknowledgment.addFeaturesAcknowledgmentString(acknowledgmentBlocks);
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
                } else if (s1.equals("<grantName>") || s1.equals("I-<grantName>")) {
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
}
