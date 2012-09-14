package org.grobid.core;

import org.apache.commons.lang3.StringUtils;

public enum GrobidModels {
    AFFIILIATON_ADDRESS("affiliation-address"),
    CITATION("citation"),
    DATE("date"),
    EBOOK("ebook"),
    ENTITIES_CHEMISTRY("entities/chemistry"),
    FULLTEXT("fulltext"),
    HEADER("header"),
    NAMES_CITATION("name/citation"),
    NAMES_HEADER("name/header"),
    PATENT_PATENT("patent/patent"),
    PATENT_NPL("patent/npl"),
    PATENT_ALL("patent/all");

    private String folderName;

    GrobidModels(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getModelName() {
        return folderName.replaceAll("/", "-");
    }

    public String getTemplateName() {
        return StringUtils.substringBefore(folderName, "/") + ".template";
    }

    @Override
    public String toString() {
        return folderName;
    }
}
