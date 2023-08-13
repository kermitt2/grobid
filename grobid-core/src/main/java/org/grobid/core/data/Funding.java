package org.grobid.core.data;

import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.KeyGen;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing a funding/grant.
 */
public class Funding {
    private Funder funder = null;

    // this is an identifier for identifying and referencing the funding inside the full document
    private String identifier = null;
    
    // program or call
    private String programFullName = null;
    private List<LayoutToken> programFullNameLayoutTokens = new ArrayList<>();

    private String programAbbreviatedName = null;
    private List<LayoutToken> programAbbreviatedNameLayoutTokens = new ArrayList<>();

    private String grantNumber  = null;
    private List<LayoutToken> grantNumberLayoutTokens = new ArrayList<>();

    private String grantName = null;
    private List<LayoutToken> grantNameLayoutTokens = new ArrayList<>();

    private String projectFullName = null;
    private List<LayoutToken> projectFullNameLayoutTokens = new ArrayList<>();

    private String projectAbbreviatedName = null;
    private List<LayoutToken> projectAbbreviatedNameLayoutTokens = new ArrayList<>();

    private String url = null;
    private List<LayoutToken> urlLayoutTokens = new ArrayList<>();

    private List<LayoutToken> layoutTokens = new ArrayList<>();

    private Date start = null;
    private Date end = null;

    public Funder getFunder() {
        return this.funder;
    }

    public void setFunder(Funder funder) {
        this.funder = funder;
    }

    public String getProgramFullName() {
        return this.programFullName;
    }

    public void setProgramFullName(String programFullName) {
        this.programFullName = programFullName;
    }

    public void appendProgramFullNameLayoutTokens(List<LayoutToken> layoutTokens) {
        this.programFullNameLayoutTokens.addAll(layoutTokens);
    }

    public List<LayoutToken> getProgramFullNameLayoutTokens() {
        return this.programFullNameLayoutTokens;
    }

    public String getProgramAbbreviatedName() {
        return this.programAbbreviatedName;
    }

    public void setProgramAbbreviatedName(String programAbbreviatedName) {
        this.programAbbreviatedName = programAbbreviatedName;
    }

    public void appendProgramAbbreviatedNameLayoutTokens(List<LayoutToken> layoutTokens) {
        this.programAbbreviatedNameLayoutTokens.addAll(layoutTokens);
    }

    public List<LayoutToken> getProgramAbbreviatedNameLayoutTokens() {
        return this.programAbbreviatedNameLayoutTokens;
    }

    public String getGrantNumber() {
        return this.grantNumber;
    }

    public void setGrantNumber(String grantNumber) {
        this.grantNumber = grantNumber;
    }

    public void appendGrantNumberLayoutTokens(List<LayoutToken> layoutTokens) {
        this.grantNumberLayoutTokens.addAll(layoutTokens);
    }

    public List<LayoutToken> getGrantNumberLayoutTokens() {
        return this.grantNumberLayoutTokens;
    }

    public String getGrantName() {
        return this.grantName;
    }

    public void setGrantName(String grantName) {
        this.grantName = grantName;
    }

    public void appendGrantNameLayoutTokens(List<LayoutToken> layoutTokens) {
        this.grantNameLayoutTokens.addAll(layoutTokens);
    }

    public List<LayoutToken> getGrantNameLayoutTokens() {
        return this.grantNameLayoutTokens;
    }

    public String getProjectFullName() {
        return this.projectFullName;
    }

    public void setProjectFullName(String project) {
        this.projectFullName = projectFullName;
    }

    public void appendProjectFullNameLayoutTokens(List<LayoutToken> layoutTokens) {
        this.projectFullNameLayoutTokens.addAll(layoutTokens);
    }

    public List<LayoutToken> getProjectFullNameLayoutTokens() {
        return this.projectFullNameLayoutTokens;
    }

    public String projectAbbreviatedName() {
        return this.projectAbbreviatedName;
    }

    public void setProjectAbbreviatedName(String project) {
        this.projectAbbreviatedName = projectAbbreviatedName;
    }

    public void appendProjectAbbreviatedNameLayoutTokens(List<LayoutToken> layoutTokens) {
        this.projectAbbreviatedNameLayoutTokens.addAll(layoutTokens);
    }

    public List<LayoutToken> getProjectAbbreviatedNameLayoutTokens() {
        return this.projectAbbreviatedNameLayoutTokens;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIdentifier() {
        if (this.identifier == null) {
            String localId = KeyGen.getKey().substring(0, 7);
            this.identifier = "_" + localId;
        }

        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void appendUrlLayoutTokens(List<LayoutToken> layoutTokens) {
        this.urlLayoutTokens.addAll(layoutTokens);
    }

    public List<LayoutToken> getUrlLayoutTokens() {
        return this.urlLayoutTokens;
    }

    public List<LayoutToken> getLayoutTokens() {
        return this.layoutTokens;
    }

    public void setLayoutTokens(List<LayoutToken> layoutTokens) {
        this.layoutTokens = layoutTokens;
    }

    public void addLayoutTokens(List<LayoutToken> layoutTokens) {
        this.layoutTokens.addAll(layoutTokens);
    }

    public boolean isValid() {
        if (funder != null || 
            grantNumber != null || 
            grantName != null || 
            projectFullName != null || 
            projectAbbreviatedName != null || 
            programFullName != null || 
            programAbbreviatedName != null ||
            url != null)
            return true;
        else
            return false;
    }

    public boolean isNonEmptyFunding() {
        if (grantNumber != null || 
            grantName != null || 
            projectFullName != null || 
            projectAbbreviatedName != null || 
            programFullName != null || 
            programAbbreviatedName != null ||
            url != null)
            return true;
        else
            return false;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (funder != null)
            builder.append("funder: " + funder.toString() + "\n");
        if (grantName != null)
            builder.append("grant name: " + grantName.toString() + "\n");
        if (grantNumber != null)
            builder.append("grant number: " + grantNumber.toString() + "\n");
        if (projectFullName != null)
            builder.append("project name: " + projectFullName.toString() + "\n");
        if (projectAbbreviatedName != null)
            builder.append("project abbreviated name: " + projectAbbreviatedName.toString() + "\n");
        if (programFullName != null)
            builder.append("program name: " + programFullName.toString() + "\n");
        if (programAbbreviatedName != null)
            builder.append("program abbreviated name: " + programAbbreviatedName.toString() + "\n");
        if (url != null)
            builder.append("url: " + url.toString() + "\n");
        return builder.toString();
    }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        boolean start = false;
        json.append("{\n");
        if (funder != null) {
            json.append(funder.toJson());
            start = true;
        }
        if (grantNumber != null) {
            if (start) 
                json.append(",\n");
            json.append("\"grantNumber\": \"");
            json.append(grantNumber+ "\"");
            start = true;
        }
        // to be completed...
        
        json.append("\n}");
        return json.toString();
    }

    public String toTEI() {
        return toTEI(0);
    }

    public String toTEI(int nbIndent) {
        StringBuilder tei = new StringBuilder();

        String localType = "funding";
        if (projectFullName != null || projectAbbreviatedName != null)
            localType = "funded-project";

        if (this.identifier == null) {
            String localId = KeyGen.getKey().substring(0, 7);
            this.identifier = "_" + localId;
        }

        for(int i=0; i<nbIndent; i++) 
            tei.append("\t");
        tei.append("<org type=\""+localType+"\" xml:id=\""+this.identifier+"\">\n"); 

        if (grantNumber != null) {
            for(int i=0; i<nbIndent+1; i++) 
                tei.append("\t");
            tei.append("<idno type=\"grant-number\">"+TextUtilities.HTMLEncode(grantNumber)+"</idno>\n");
        }
        if (programFullName != null) {
            for(int i=0; i<nbIndent+1; i++) 
                tei.append("\t");
            tei.append("<orgName type=\"program\" subtype=\"full\">"+TextUtilities.HTMLEncode(programFullName)+"</orgName>\n");
        }
        if (programAbbreviatedName != null) {
            for(int i=0; i<nbIndent+1; i++) 
                tei.append("\t");
            tei.append("<orgName type=\"program\" subtype=\"abbreviated\">"+TextUtilities.HTMLEncode(programAbbreviatedName)+"</orgName>\n");
        }
        if (projectFullName != null) {
            for(int i=0; i<nbIndent+1; i++) 
                tei.append("\t");
            tei.append("<orgName type=\"project\" subtype=\"full\">"+TextUtilities.HTMLEncode(projectFullName)+"</orgName>\n");
        }
        if (projectAbbreviatedName != null) {
            for(int i=0; i<nbIndent+1; i++) 
                tei.append("\t");
            tei.append("<orgName type=\"project\" subtype=\"abbreviated\">"+TextUtilities.HTMLEncode(projectAbbreviatedName)+"</orgName>\n");
        }
        if (url != null) {
            for(int i=0; i<nbIndent+1; i++) 
                tei.append("\t");
            tei.append("<ptr target=\"").append(TextUtilities.HTMLEncode(url)).append("\" />\n");
        }
        if (start != null) {
            String dateString = start.toTEI();
            dateString = dateString.replace("<date ", "<date type=\"start\" ");
            for(int i=0; i<nbIndent+1; i++) 
                tei.append("\t");
            tei.append(dateString);
        }
        if (end != null) {
            String dateString = end.toTEI();
            dateString = dateString.replace("<date ", "<date type=\"end\" ");
            for(int i=0; i<nbIndent+1; i++) 
                tei.append("\t");
            tei.append(dateString);
        }

        for(int i=0; i<nbIndent; i++) 
            tei.append("\t");
        tei.append("</org>\n");

        return tei.toString();
    }
}