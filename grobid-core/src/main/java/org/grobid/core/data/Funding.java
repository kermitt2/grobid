package org.grobid.core.data;

import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.LayoutTokensUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing a funding/grant.
 */
public class Funding {
    private Funder funder = null;
    
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
}
