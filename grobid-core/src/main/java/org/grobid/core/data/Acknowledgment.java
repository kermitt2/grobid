package org.grobid.core.data;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.document.Document;
import org.grobid.core.document.TEIFormatter;
import org.grobid.core.document.xml.XmlBuilderUtils;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.TextUtilities;

import java.util.List;
import java.util.StringTokenizer;

import static org.grobid.core.document.xml.XmlBuilderUtils.addXmlId;
import static org.grobid.core.document.xml.XmlBuilderUtils.textNode;

/**
 * Class for representing and exchanging acknowledgment information.
 *
 *  Created by Tanti, 2019
 */

public class Acknowledgment {
    private String affiliation = null;
    private String educationalInstitution = null;
    private String fundingAgency = null;
    private String grantName = null;
    private String grantNumber = null;
    private String individual = null;
    private String otherInstitution = null;
    private String projectName = null;
    private String researchInstitution = null;
    private List<BoundingBox> coordinates = null;
    private List<LayoutToken> layoutTokens;

    public List<LayoutToken> getLayoutTokens() {
        return layoutTokens;
    }

    public void setLayoutTokens(List<LayoutToken> layoutTokens) {
        this.layoutTokens = layoutTokens;
    }

    public List<BoundingBox> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<BoundingBox> coordinates) {
        this.coordinates = coordinates;
    }


    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getEducationalInstitution() {
        return educationalInstitution;
    }

    public void setEducationalInstitution(String educationalInstitution) {
        this.educationalInstitution = educationalInstitution;
    }

    public String getFundingAgency() {
        return fundingAgency;
    }

    public void setFundingAgency(String fundingAgency) {
        this.fundingAgency = fundingAgency;
    }

    public String getGrantName() {
        return grantName;
    }

    public void setGrantName(String grantName) {
        this.grantName = grantName;
    }

    public String getGrantNumber() {
        return grantNumber;
    }

    public void setGrantNumber(String grantNumber) {
        this.grantNumber = grantNumber;
    }

    public String getIndividual() {
        return individual;
    }

    public void setIndividual(String individual) {
        this.individual = individual;
    }

    public String getOtherInstitution() {
        return otherInstitution;
    }

    public void setOtherInstitution(String otherInstitution) {
        this.otherInstitution = otherInstitution;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getResearchInstitution() {
        return researchInstitution;
    }

    public void setResearchInstitution(String researchInstitution) {
        this.researchInstitution = researchInstitution;
    }

    public boolean isNotNull() {
        if ((affiliation == null) &&
            (educationalInstitution == null) &&
            (fundingAgency == null) &&
            (grantName == null) &&
            (grantNumber == null) &&
            (individual == null) &&
            (otherInstitution == null) &&
            (projectName == null) &&
            (researchInstitution == null))
            return false;
        else
            return true;
    }

    public void clean() {
        if (affiliation != null) {
            affiliation = TextUtilities.cleanField(affiliation, true);
            if (affiliation.length() < 2)
                affiliation = null;
        }

        if (educationalInstitution != null) {
            educationalInstitution = TextUtilities.cleanField(educationalInstitution, true);
            if (educationalInstitution.length() < 2)
                educationalInstitution = null;
        }

        if (fundingAgency != null) {
            fundingAgency = TextUtilities.cleanField(fundingAgency, true);
            if (fundingAgency.length() < 2)
                fundingAgency = null;
        }

        if (grantName != null) {
            grantName = TextUtilities.cleanField(grantName, true);
            if (grantName.length() < 2)
                grantName = null;
        }

        if (grantNumber != null) {
            grantNumber = TextUtilities.cleanField(grantNumber, true);
            if (grantNumber.length() < 2)
                grantNumber = null;
        }

        if (individual != null) {
            individual = TextUtilities.cleanField(individual, true);
            if (individual.length() < 2)
                individual = null;
        }

        if (otherInstitution != null) {
            otherInstitution = TextUtilities.cleanField(otherInstitution, true);
            if (otherInstitution.length() < 2)
                otherInstitution = null;
        }

        if (projectName != null) {
            projectName = TextUtilities.cleanField(projectName, true);
            if (projectName.length() < 2)
                projectName = null;
        }

        if (researchInstitution != null) {
            researchInstitution = TextUtilities.cleanField(researchInstitution, true);
            if (researchInstitution.length() < 2)
                researchInstitution = null;
        }
    }

    public int getStart() {
        if ( (layoutTokens != null) && (layoutTokens.size()>0) )
            return layoutTokens.get(0).getOffset();
        else
            return -1;
    }

    // result of acknowledgment string processing
    public String toTEI(){
        StringBuilder tei = new StringBuilder();
        if (!isNotNull()) {
            return null;
        } else {
            tei.append("<acknowledgment>");

            if (affiliation != null) {
                tei.append("<affiliation>").append(TextUtilities.HTMLEncode(affiliation)).append("</affiliation>");
            }

            if (educationalInstitution != null) {
                tei.append("<educationalInstitution>").append(TextUtilities.HTMLEncode(educationalInstitution)).append("</educationalInstitution>");
            }

            if (fundingAgency != null) {
                tei.append("<fundingAgency>").append(TextUtilities.HTMLEncode(fundingAgency)).append("</fundingAgency>");
            }

            if (grantName != null) {
                tei.append("<grantName>").append(TextUtilities.HTMLEncode(grantName)).append("</grantName>");
            }

            if (grantNumber != null) {
                tei.append("<grantNumber>").append(TextUtilities.HTMLEncode(grantNumber)).append("</grantNumber>");
            }

            if (individual != null) {
                tei.append("<individual>").append(TextUtilities.HTMLEncode(individual)).append("</individual>");
            }

            if (otherInstitution != null) {
                tei.append("<otherInstitution>").append(TextUtilities.HTMLEncode(otherInstitution)).append("</otherInstitution>");
            }

            if (projectName != null) {
                tei.append("<projectName>").append(TextUtilities.HTMLEncode(projectName)).append("</projectName>");
            }

            if (researchInstitution != null) {
                tei.append("<researchInstitution>").append(TextUtilities.HTMLEncode(researchInstitution)).append("</researchInstitution>");
            }
            tei.append("</acknowledgment>");
        }
        return tei.toString();
    }



    public String toTEI(int n, int indent, GrobidAnalysisConfig config) {
        StringBuilder tei = new StringBuilder();
        boolean generateIDs = config.isGenerateTeiIds();
        try {
            for (int i = 0; i < indent; i++) {
                tei.append("\t");
            }
            tei.append("<listAcknowledment>");
            boolean withCoords = (config.getGenerateTeiCoordinates() != null) && (config.getGenerateTeiCoordinates().contains("listAcknowledment"));
            tei.append(" ");
            if (withCoords)
                tei.append(TEIFormatter.getCoordsAttribute(coordinates, withCoords)).append(" ");
            // title
            for (int i = 0; i < indent + 2; i++) {
                tei.append("\t");
            }
            if (affiliation != null) {
                tei.append("<affiliation>").append(TextUtilities.HTMLEncode(affiliation)).append("</affiliation>");
            }

            if (educationalInstitution != null) {
                tei.append("<educationalInstitution>").append(TextUtilities.HTMLEncode(educationalInstitution)).append("</educationalInstitution>");
            }

            if (fundingAgency != null) {
                tei.append("<fundingAgency>").append(TextUtilities.HTMLEncode(fundingAgency)).append("</fundingAgency>");
            }

            if (grantName != null) {
                tei.append("<grantName>").append(TextUtilities.HTMLEncode(grantName)).append("</grantName>");
            }

            if (grantNumber != null) {
                tei.append("<grantNumber>").append(TextUtilities.HTMLEncode(grantNumber)).append("</grantNumber>");
            }

            if (individual != null) {
                tei.append("<individual>").append(TextUtilities.HTMLEncode(individual)).append("</individual>");
            }

            if (otherInstitution != null) {
                tei.append("<otherInstitution>").append(TextUtilities.HTMLEncode(otherInstitution)).append("</otherInstitution>");
            }

            if (projectName != null) {
                tei.append("<projectName>").append(TextUtilities.HTMLEncode(projectName)).append("</projectName>");
            }

            if (researchInstitution != null) {
                tei.append("<researchInstitution>").append(TextUtilities.HTMLEncode(researchInstitution)).append("</researchInstitution>");
            }

            for (int i = 0; i < indent; i++) {
                tei.append("\t");
            }
            tei.append("</listAcknowledment>\n");
        } catch (Exception e) {
            throw new GrobidException("Cannot convert  bibliographical item into a TEI, " +
                "because of nested exception.", e);
        }

        return tei.toString();
    }
}

