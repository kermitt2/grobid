# Annotation guidelines for the 'header' model

## Introduction

For the following guidelines, it is expected that training data has been generated as explained [here](../Training-the-models-of-Grobid/#generation-of-training-data).

In Grobid, the document "header" corresponds to the metadata information sections about the document. This is typically all the information at the beginning of the artilce (often called the "front"): title, authors, publication information, affiliations, abstrac, keywords, correspondence information, submission information, etc. but not only. Some of these elements can be located in the footnotes of the first page (e.g. affiliation of the authors), or at the end of the article (full list of authors, detailed affiliation and contact, how to cite). 

For identifying the exact pieces of information to be part of the "header" segments, see the [annotation guidelines of the segmentation model](segmentation.md). 

The following TEI elements are used by the header model:

* `<titlePart>` for the document title
* `<docAuthor>`  for the author list, including callout markers 
* `<affiliation>` for the authors affiliation information, including callout markers 
* `<address>` identifies the address elements of the affiliations 
* `<note type="doctype">` for indication on the document type
* `<div type="abstract">` for the document abstract
* `<keyword>` identifies the list of keywords, subject terms or classifications for the document
* `<reference>` identifies the reference information (how to cite) for the document that can appear in the document itself 
* `<email>` for the email of author or editor
* `<editor>` for the person name information of the document editors
* `<submission>` identifies the submission/acceptance information about the document
* `<note type="copyright">` identifies copyrights statements (copyrights holder, waiver like CC licenses, etc.) 
* `<idno>` for the strong identifiers of the document (DOI, arXiv identifier, PII, etc.)
* `<phone>` for phone number
* `<page>` for identifying a page number present in the header parts
* `<note type="group">` to identify a group name (typically a working group or a collaboration)

Note that the mark-up follows approximatively the [TEI](http://www.tei-c.org) when used for inline encoding. 

Encoding the header section is challenging because of the variety of information that can appear in these parts, sometimes in unexpected imbricated manners. In addition, some information are often redundant (for example authors and affiliations mentioned two times with different level of details). These annotation guidelines are thus particularly important to follow to ensure stable encoding practices in the complete training data and to avoid the machine learning models to learn contradictory labelling, resulting in poorer performance and less valuable training data. 

> Note: It is recommended to study the existing training documents for the header model first to see some examples of how these elements should be used.

## Analysis

The following sections provide detailed information and examples on how to handle certain typical cases.


