package org.grobid.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class GrobidServicePropConfiguration {
    @NotEmpty
    @JsonProperty
    private String grobidHome;

    @NotEmpty
    @JsonProperty
    private String grobidServiceProperties;

    @JsonProperty
    private String grobidProperties;

    public String getGrobidHome() {
        return grobidHome;
    }

    public void setGrobidHome(String grobidHome) {
        this.grobidHome = grobidHome;
    }

    public String getGrobidServiceProperties() {
        return grobidServiceProperties;
    }

    public void setGrobidServiceProperties(String grobidServiceProperties) {
        this.grobidServiceProperties = grobidServiceProperties;
    }

    public String getGrobidProperties() {
        return grobidProperties;
    }

    public void setGrobidProperties(String grobidProperties) {
        this.grobidProperties = grobidProperties;
    }
}
