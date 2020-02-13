package org.grobid.core.data;

import org.grobid.core.document.BasicStructureBuilder;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.TextUtilities;

import java.io.File;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Class for representing the monograph information
 *
 * @created by Tanti, 01/2020
 *
 * 17 labels for this model:
 * cover page (front of the book)
 * title page (secondary title page)
 * publisher page (publication information, including usually the copyrights info)
 * summary (include executive summary)
 * biography
 * advertising (other works by the author/publisher)
 * table of content
 * table/list of figures
 * preface (foreword)
 * dedication (I dedicate this label to my family and my thesis director ;)
 * unit (chapter or standalone article)
 * reference (a full chapter of references, not to be confused with references attached to an article)
 * annex
 * index
 * glossary (also abbreviations and acronyms)
 * back cover page
 * other
 */

public class Monograph {
    private String cover = null;
    private String title = null;
    private String publisher = null;
    private String summary = null;
    private String biography = null;
    private String advertising = null;
    private String toc = null;
    private String tof = null;
    private String preface = null;
    private String dedication = null;
    private String unit = null;
    private String reference = null;
    private String annex = null;
    private String index = null;
    private String glossary = null;
    private String back = null;
    private String other = null;

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getAdvertising() {
        return advertising;
    }

    public void setAdvertising(String advertising) {
        this.advertising = advertising;
    }

    public String getToc() {
        return toc;
    }

    public void setToc(String toc) {
        this.toc = toc;
    }

    public String getTof() {
        return tof;
    }

    public void setTof(String tof) {
        this.tof = tof;
    }

    public String getPreface() {
        return preface;
    }

    public void setPreface(String preface) {
        this.preface = preface;
    }

    public String getDedication() {
        return dedication;
    }

    public void setDedication(String dedication) {
        this.dedication = dedication;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAnnex() {
        return annex;
    }

    public void setAnnex(String annex) {
        this.annex = annex;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getGlossary() {
        return glossary;
    }

    public void setGlossary(String glossary) {
        this.glossary = glossary;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public boolean isNotNull() {
        if ((cover == null) &&
                (title == null) &&
                (publisher == null) &&
                (summary == null) &&
                (biography == null) &&
                (advertising == null) &&
                (toc == null) &&
                (tof == null) &&
                (preface == null) &&
                (dedication == null) &&
                (unit == null) &&
                (reference == null) &&
                (annex == null) &&
                (index == null) &&
                (glossary == null) &&
                (back == null) &&
                (other == null))
            return false;
        else
            return true;
    }

    // result of monograph string processing
    public StringBuilder toTEI() {
        StringBuilder tei = new StringBuilder();
        if (!isNotNull()) {
            return null;
        } else {
            if (cover != null) {
                //tei.append("<cover>").append(TextUtilities.HTMLEncode(getCover())).append("</cover>");
                tei.append("<cover>").append(TextUtilities.HTMLEncode(cover)).append("</cover>");
            }
            if (title != null) {
                tei.append("<title>").append(TextUtilities.HTMLEncode(title)).append("</title>");
            }
            if (publisher != null) {
                tei.append("<publisher>").append(TextUtilities.HTMLEncode(publisher)).append("</publisher>");
            }
            if (summary != null) {
                tei.append("<summary>").append(TextUtilities.HTMLEncode(summary)).append("</summary>");
            }
            if (biography != null) {
                tei.append("<biography>").append(TextUtilities.HTMLEncode(biography)).append("</biography>");
            }
            if (advertising != null) {
                tei.append("<advertising>").append(TextUtilities.HTMLEncode(advertising)).append("</advertising>");
            }
            if (toc != null) {
                tei.append("<toc>").append(TextUtilities.HTMLEncode(toc)).append("</toc>");
            }
            if (tof != null) {
                tei.append("<tof>").append(TextUtilities.HTMLEncode(tof)).append("</tof>");
            }
            if (preface != null) {
                tei.append("<preface>").append(TextUtilities.HTMLEncode(preface)).append("</preface>");
            }
            if (dedication != null) {
                tei.append("<dedication>").append(TextUtilities.HTMLEncode(dedication)).append("</dedication>");
            }
            if (unit != null) {
                tei.append("<unit>").append(TextUtilities.HTMLEncode(unit)).append("</unit>");
            }
            if (reference != null) {
                tei.append("<reference>").append(TextUtilities.HTMLEncode(reference)).append("</reference>");
            }
            if (annex != null) {
                tei.append("<annex>").append(TextUtilities.HTMLEncode(annex)).append("</annex>");
            }
            if (index != null) {
                tei.append("<index>").append(TextUtilities.HTMLEncode(index)).append("</index>");
            }
            if (glossary != null) {
                tei.append("<glossary>").append(TextUtilities.HTMLEncode(glossary)).append("</glossary>");
            }
            if (back != null) {
                tei.append("<back>").append(TextUtilities.HTMLEncode(back)).append("</back>");
            }
            if (other != null) {
                tei.append("<other>").append(TextUtilities.HTMLEncode(other)).append("</other>");
            }
        }
        return tei;
    }
}
