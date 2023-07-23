package org.grobid.core.data;

import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.layout.LayoutToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing a funding/grant.
 */
public class Funding {
    private Funder funder = null;
    private String rawFunderString = null;
    
    // program or call
    private String programFullName = null;
    private String programAbbreviatedName = null;

    private String grantNumber  = null;
    private String rawGrantNumber  = null;

    private String project = null;
    private String url = null;

    private List<LayoutToken> layoutTokens = new ArrayList<>();

    public Funder getFunder() {
        return this.funder;
    }

    public void setFunder(Funder funder) {
        this.funder = funder;
    }

    public String getRawFunderString() {
        return this.rawFunderString;
    }

    public void setRawFunderString(String rawFunderString) {
        this.rawFunderString = rawFunderString;
    }

    public String getProgramFullName() {
        return this.programFullName;
    }

    public void setProgramFullName(String programFullName) {
        this.programFullName = programFullName;
    }

    public String getProgramAbbreviatedName() {
        return this.programAbbreviatedName;
    }

    public void setProgramAbbreviatedName(String programAbbreviatedName) {
        this.programAbbreviatedName = programAbbreviatedName;
    }

    public String getGrantNumber() {
        return this.grantNumber;
    }

    public void setGrantNumber(String grantNumber) {
        this.grantNumber = grantNumber;
    }

    public String getRawGrantNumber() {
        return this.rawGrantNumber;
    }

    public void setRawGrantNumber(String rawGrantNumber) {
        this.rawGrantNumber = rawGrantNumber;
    }

    public String getProject() {
        return this.project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<LayoutToken> getLayoutTokens() {
        return this.layoutTokens;
    }

    public void setLayoutTokens(List<LayoutToken> layoutTokens) {
        this.layoutTokens = layoutTokens;
    }
}
