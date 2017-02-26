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
     * note in margin <marginnote>,
     * document body <body>,
     * bibliographical section <references>,
     * page number <page>,
     * annexes <annex>,
	 * acknowledgement <acknowledgement>,
     * other <other>,
	 * toc <toc> -> not yet used because not yet training data for this
     */
    COVER("<cover>"),
    HEADER("<header>"),
    FOOTNOTE("<footnote>"),
	HEADNOTE("<headnote>"),
    MARGINNOTE("<marginnote>"),
    BODY("<body>"),
    PAGE_NUMBER("<page>"),
    ANNEX("<annex>"),
    REFERENCES("<references>"),
	ACKNOWLEDGEMENT("<acknowledgement>"),
    OTHER("<other>"),
	TOC("<toc>");

    private String label;
    SegmentationLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
