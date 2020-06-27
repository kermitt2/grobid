# Annotation Guidelines for the Medical Model

## Introduction

For the following guidelines, it is expected that training data has been generated as explained [here](../Training-the-models-of-Grobid/#generation-of-training-data).

The following TEI elements are used by the Segmentation model:

* `<front>` for document header
* `<note place="headnote">` for the page header
* `<note place="footnote">` for the page footer
* `<note place="left">` for the notes on the document left section
* `<note place="right">` for the notes on the document right section
* `<body>` for the document body
* `<page>` for the page numbers
* `<div type="acknowledgment">` for the acknowledgment
* `<other>` for unknown (yet) part

It is necessary to identify these above substructures when interrupting the `<body>`. Figures and tables (including their potential titles, captions and notes) are considered part of the body, so contained by the `<body>` element.

Note that the mark-up follows overall the [TEI](http://www.tei-c.org). 

> Note: This model follows the existing model, the Segmentation model. It is recommended to study the existing training documents for the segmentation model first to see some examples of how these elements should be used here [segmentation](../segmentation.md).
