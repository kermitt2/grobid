package org.grobid.core.engines;

/**
 * User: zholudev
 * Date: 4/3/14
 */
public enum SegmentationLabel {
    /**
     * 	cover page <cover>,
     document header <header>,
     page footer <footnote>,
     page header <headnote>,
     document body <body>,
     bibliographical section <references>,
     page number <page>,
     ? each bibliographical references in the biblio section <ref>,
     annexes <annex>
     */
    COVER("<cover>"),
    HEADER("<header>"),
    FOOTER("<footer>"),
    BODY("<body>"),
    PAGE_NUMBER("<page>"),
    ANNEX("<annex>"),
    REFERENCES("<references>");

    private String label;
    SegmentationLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
