# Annotation guidelines for the Monograph Model

## Introduction

Monograph segments are recognized by the `monograph` model. This model is one of several Grobid models that are used to analyze the contents of books (in PDF files).  This is different from other models since the `monograph` model wants to recognize the general sections (e.g., cover, title, publisher,toc, unit).

The `monograph` model attempts to recognize 17 following objects:
* cover page (front of the book)
* title page (secondary title page)
* publisher page (publication information, including usually the copyrights info)
* summary (include executive summary)
* biography
* advertising (other works by the author/publisher)
* table of content (toc)
* table/list of figures (tof)
* preface (foreword)
* dedication 
* unit (chapter or standalone article)
* reference (a full chapter of references, not to be confused with references attached to an article)
* annex
* index
* glossary (also abbreviations and acronyms)
* back cover page
* other
        
## Analysis
The result of Pdf extraction with the monograph models is in TEI formet <tei>. Each document has a `<header>`  and a `<text>` sections where we find the text flow of the whole book with the TEI markup. The tags of the level monograph are as follows:
* `<cover>`         :  cover page information
* `<title>`         :  secondary title page information
* `<publisher>`     :  publication information
* `<summary>`       :  summary, including executive summary
* `<biography>`     :  biography information
* `<advertising>`   :  other works by the author/publisher
* `<toc>`           :  table of content
* `<tof>`           :  table/list of figures
* `<preface>`       :  foreword
* `<dedication>`    :  dedication
* `<unit>`          :  chapter or standalone article
* `<reference>`     :  a full chapter of references
* `<annex>`         :  annex
* `<index>`         :  index
* `<glossary>`      :  glossary, including abbreviations and acronyms
* `<back>`          :  back cover page information
* `<other>`         :  other information not included in previous TEI markups

