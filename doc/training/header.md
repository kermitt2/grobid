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
* `<title level="j">` to identify the name of the journal where the article is published
* `<meeting>` to identify the meeting information associated to the publication, if relevant

Note that the mark-up follows approximatively the [TEI](http://www.tei-c.org) when used for inline encoding. 

Encoding the header section is challenging because of the variety of information that can appear in these parts, sometimes in unexpected imbricated manners. In addition, some information are often redundant (for example authors and affiliations mentioned two times with different level of details). These annotation guidelines are thus particularly important to follow to ensure stable encoding practices in the complete training data and to avoid the machine learning models to learn contradictory labelling, resulting in poorer performance and less valuable training data. 

> Note: It is recommended to study the existing training documents for the header model first to see some examples of how these elements should be used.

## Analysis

The following sections provide detailed information and examples on how to handle certain typical cases.


### Title

Subtitles are labelled similarly as title but as an independent field. It's important to keep a break (in term of XML tagging) between the main title and possible subtitles, even if there are next to each other in the text stream. 

Running titles are not labelled at all. 

```xml
    <address>Villejuif, France<lb/></address>

    Running title: HBsAg quantification in anti-HBs positive HBV carriers<lb/>

    Abstract word count: 250<lb/> 
```

In the case of a non English article having an additional English title as translation of the original title: 



### Authors

All mentions of the authors are labelled, including possible repetition of the authors in the correspondence section. The author information might be more detailed in the correspondence part and it will be then part of the job of Grobid to identify repeated authors and to "merge" them. 

```
CORRESPONDENCE<lb/> Address correspondence to
    <byline>
    <docAuthor>Andrea Z. LaCroix, PhD,</docAuthor>
    </byline>
```

The only exception is when indication of authors are given around an email or a phone number. In this case we consider that the occurence of an author name (including abbreviated names) is purely for practical reasons and should be ignored. 

```
Email: Calum J Maclean* -
     <email>calum.maclean@ucl.ac.uk</email>; 
```   

```
    *Corresponding author. Emails:
    <email>cmoser@g.harvard.edu</email>
     (C.J.M.);
    <email>sam@wjh.harvard.edu</email> 
     (S.A.M.)
```

Titles like Ph.D., MD, Dr., etc. must be included in the author field. Full job names like "Dean of...", "Research associate at..." should be excluded when possible, i.e. when it does not break the author sequence:

```xml
   <byline>
    <docAuthor>Peter O&apos;Shannassy</docAuthor>
    </byline> 

    (Ranger, 
```


### Affiliation and address

Similarly as authors, all the mentions of an affiliation are labelled, including in the correspondence parts. Grobid will have to merge appropriately redundant affiliations. It's important to keep markers inside the labelled fields, because they are used to associate the right affiliations to the authors.

Address are labelled with their own tag `<address>`. 

```
<byline>
    <affiliation>2 Institut für Angewandte Physik, Heinrich-Heine-Universität<lb/></affiliation>
    </byline>

    <address>40225 Düsseldorf, Germany<lb/></address>
```


### Document types

Indication of document types are labelled. These indications depend on the editor, document source, etc. We consider as document type the nature of the document (article, review, editorial, etc.), but also some specific aspects that can be highlighted in the presentation format, for instance indication of an "Open Access" publication expressed independently form the copyrights to characterize the document.

```xml
    Annals of General Hospital Psychiatry<lb/>

    <note type="doctype">Open Access<lb/></note>

    <note type="doctype">Primary research<lb/></note>

    <docTitle>
        <titlePart>Brain choline concentrations may not be altered in euthymic<lb/> 
            bipolar disorder patients chronically treated with either lithium or<lb/> 
            sodium valproate<lb/></titlePart>
    </docTitle>
```


### Abstract

In case of several abstracts (in particular the same abstract in difference languages), they are all labelled. In case two abstracts are adjacent in term of text stream, they must be tagged into different abstract blocks, and not under a single `<abstract>` tag. 

Attention must be paid to exclude functional words like "Abstract", "Summary", etc. from the labelled abstract. These are indications that will be exploited by Grobid to predict the start of the asbtract but it's not seomthing that we want to see in the final extraction result.


### Keywords

The `<keyword>` field covers all type of keywords, subject header, classification symbols, etc. 

Specific keyword scheme names like "PACS" or "Mathematics Subject Classification" must be included in the labelled field. 


```xml
<keywords>Mathematics Subject Classification: 83C15, 81U15, 81V80, 17B80, 81R12<lb/></keywords> 
           
<keywords type="pacs">PACS numbers: 02.30.Ik, 03.65.Fd Fd<lb/></keywords>
```


However, generic words like "Keywords", "Key words", etc. which does not bring any information about the nature of the keywords, must be excluded from the field. 

```xml
<date>17 July 2007<lb/></date>

Keywords: 
<keyword>body mass index; weight change; coronary heart disease; follow-up study; Japanese Japanese<lb/></keyword>
```

### Reference

The reference field aims at identifying a field describing the bibliographical reference information to be used to cite about the document which is annotated. As a consequence, the reference field must contain several bibliographical information, ideally all the key information to identify the document in a unique manner following the publication standards. We typically expect here a container name (journal, proceedings name) with volume/issue/page information, possibly with a date information. 

If the reference includes an identifier, in particular a DOI, which cannot be tagged separately without breaking the reference sequence, the identifier must be included in the reference field:



If the title of the journal where the atticle is published appears in isolation, it is not enough to have a "reference", and the tags `<title level="j">` must be used. 



### Emails

Email must be tagged in a way that is limited to an actual email, exlcuding "Email" word, punctuations and person name information. 

### Editors

The name of the editor are tagged similarly as author names. Titles like Prof. Dr. MD. are included in the field, but functional words as "Editor" or "Edited by" must be excluded. 


### Submission and peer review information

The `<submission>` tag is used to identify, in a raw manner, the submission and peer review information present in the header parts. The date information given in this field are not further labelled. 


### Copyrights

`<note type="copyright">` is used to identify passages about the copyrights holders of the document and any relevant licensing information, in particular CC licenses. Email and URL present in this section must not be further tagged:




### String identifiers

`<idno>` is used to identify strong identifiers of the document, in particular DOI, PII, ISSN, ISBN and the major Open Access repository identifiers - arXiv identifiers, HAL ID, ...  

We do not tag report numbers, the identifiers here must have a global accepted level of acceptance beyond a local source of identification. 


### Phone number

We label phone number, including international prefix symbols, but not fax numbers. Punctation and words like "phone", "telephone", etc. must be excluded from the label field. 


### Page number

In case a page number appears in the header part, it is identified with the `<page>` tag. The page number must stand in isolation, not part of a reference information (it will then be labelled as part of the reference under a `<reference>` tag).


### Group name

In contrast to affiliation, a group correspond to a temporary association of persons and/or institutions for a given work. Group name include working groups for standards, for experiments, for particular collaborative study, review or work, or larger "collaboration" as we see in Big Science efforts like astronomy or High Energy Particules. 

In general, group names are introduced as such, in a distinctive manner from affiliations (which are usually also associated to a physical address).

### Journal titles

In case the name of the journal appears alone in the header part, not part of a reference, it is tagged specifically.

### Meeting information

Publications can be associated to a particular meeting event, in particular a conference or a working event for the development of standards. In this case, the meeting information, usually covering a location and dates, and optionally a meeting even name, are labelled in a raw manner with the `<meeting>` tag. Dates and location/address should not be further labelled and detailed. 


If the meeting information is part of a larger reference (e.g. defintion the citation information of the Proceedings of a conference where the article is published), then it has to be labelled also as `<reference>` (this is similar to the journal title case above).

