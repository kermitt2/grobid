package org.grobid.core.data;

import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.layout.LayoutToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing a funding organization.
 * Optionally the funder is identified by its DOI at CrossRef funder registry.
 */

public class Funder {
    private String fullName = null;
    private List<LayoutToken> fullNameLayoutTokens = new ArrayList<>();

    private String abbreviatedName = null;
    private List<LayoutToken> abbreviatedNameLayoutTokens = new ArrayList<>();

    private String doi = null;

    // country or regional area (e.g. EU)
    private String country = null;
    private String countryCode = null;
    private String address = null;

    private Date startActiveDate = null;
    private Date endActiveDate = null;
    private Funder preceededBy = null;
    private Funder followedBy = null;

    private String url = null;

    private List<LayoutToken> layoutTokens = new ArrayList<>();
    
    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void appendFullNameLayoutTokens(List<LayoutToken> layoutTokens) {
        this.fullNameLayoutTokens.addAll(layoutTokens);
    }

    public List<LayoutToken> getFullNameLayoutTokens() {
        return this.fullNameLayoutTokens;
    }

    public String getAbbreviatedName() {
        return this.abbreviatedName;
    }

    public void setAbbreviatedName(String abbreviatedName) {
        this.abbreviatedName = abbreviatedName;
    }

    public void appendAbbreviatedNameLayoutTokens(List<LayoutToken> layoutTokens) {
        this.abbreviatedNameLayoutTokens.addAll(layoutTokens);
    }

    public List<LayoutToken> getAbbreviatedNameLayoutTokens() {
        return this.abbreviatedNameLayoutTokens;
    }

    public String getDoi() {
        return this.doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getStartActiveDate() {
        return this.startActiveDate;
    }

    public void setStartActiveDate(Date startActiveDate) {
        this.startActiveDate = startActiveDate;
    }

    public Date getEndActiveDate() {
        return this.endActiveDate;
    }

    public void setEndActiveDate(Date endActiveDate) {
        this.endActiveDate = endActiveDate;
    }

    public Funder getPreceededBy() {
        return this.preceededBy;
    }

    public void setPreceededBy(Funder preceededBy) {
        this.preceededBy = preceededBy;
    }

    public Funder getFollowedBy() {
        return this.followedBy;
    }

    public void setFollowedBy(Funder followedBy) {
        this.followedBy = followedBy;
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

    public void addLayoutTokens(List<LayoutToken> layoutTokens) {
        this.layoutTokens.addAll(layoutTokens);
    }
}