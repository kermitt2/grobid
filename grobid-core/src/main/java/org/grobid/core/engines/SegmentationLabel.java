package org.grobid.core.engines;

/**
 * @author Slava
 * Date: 4/3/14
 */
public enum SegmentationLabel {
    /**
     * 	cover page <cover>,
     * document header <header>,
     * page footer <footnote>,
     * page header <headnote>,
     * document body <body>,
     * bibliographical section <references>,
     * page number <page>,
     * annexes <annex>,
	 * acknowledgement <acknowledgement>,
	 * toc <toc> -> not yet used because not yet training data for this
     */
    COVER("<cover>"),
    HEADER("<header>"),
    FOOTNOTE("<footnote>"),
	HEADNOTE("<headnote>"),
    BODY("<body>"),
    PAGE_NUMBER("<page>"),
    ANNEX("<annex>"),
    REFERENCES("<references>"),
	ACKNOWLEDGEMENT("<acknowledgement>"),
	TOC("toc");

    private String label;
    SegmentationLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
