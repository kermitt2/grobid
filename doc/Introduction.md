<h1>Introduction</h1>

## Status

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![CircleCI](https://circleci.com/gh/kermitt2/grobid.svg?style=svg)](https://circleci.com/gh/kermitt2/grobid)
[![Coverage Status](https://coveralls.io/repos/kermitt2/grobid/badge.svg)](https://coveralls.io/r/kermitt2/grobid)
[![Documentation Status](https://readthedocs.org/projects/grobid/badge/?version=latest)](https://readthedocs.org/projects/grobid/?badge=latest)
[![GitHub release](https://img.shields.io/github/release/kermitt2/grobid.svg)](https://github.com/kermitt2/grobid/releases/)
[![Demo cloud.science-miner.com/grobid](https://img.shields.io/website-up-down-green-red/https/cloud.science-miner.com/grobid.svg)](http://cloud.science-miner.com/grobid)
[![Docker Hub](https://img.shields.io/docker/pulls/lfoppiano/grobid.svg)](https://hub.docker.com/r/lfoppiano/grobid/ "Docker Pulls")
[![Docker Hub](https://img.shields.io/docker/pulls/grobid/grobid.svg)](https://hub.docker.com/r/grobid/grobid/ "Docker Pulls")
[![SWH](https://archive.softwareheritage.org/badge/origin/https://github.com/kermitt2/grobid/)](https://archive.softwareheritage.org/browse/origin/?origin_url=https://github.com/kermitt2/grobid)

## Purpose

GROBID (or Grobid, but not GroBid nor GroBiD) means GeneRation Of BIbliographic Data. 

GROBID is a machine learning library for extracting, parsing and re-structuring raw documents such as PDF into structured XML/TEI encoded documents with a particular focus on technical and scientific publications. First developments started in 2008 as a hobby. In 2011 the tool has been made available in open source. Work on GROBID has been steady as side project since the beginning and is expected to continue as such. 

The following functionalities are available:

- __Header extraction and parsing__ from article in PDF format. The extraction here covers the usual bibliographical information (e.g. title, abstract, authors, affiliations, keywords, etc.).
- __References extraction and parsing__ from articles in PDF format, around .85 f-score against on an independent PubMed Central set of 1943 PDF containing 90,125 references. All the usual publication metadata are covered (including DOI).
- __Citation contexts recognition and linking__ to the full bibliographical references of the article. The accuracy of citation contexts resolution is around 0.75 f-score (which corresponds to both the correct identification of the citation callout and its correct association with a full bibliographical reference).
- Parsing of __references in isolation__ (around 0.89 f-score).
- __Parsing of names__ (e.g. person title, forenames, middlename, etc.), in particular author names in header, and author names in references (two distinct models).
- __Parsing of affiliation and address__ blocks.
- __Parsing of dates__, ISO normalized day, month, year.
- __Full text extraction and structuring__ from PDF articles, including a model for the overall document segmentation and models for the structuring of the text body (paragraph, section titles, reference callout, figure, table, etc.).
- __Consolidation/resolution of the extracted bibliographical references__ using the [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service or the [CrossRef REST API](https://github.com/CrossRef/rest-api-doc). In both cases, DOI resolution performance is higher than 0.95 f-score from PDF extraction.
- __Extraction and parsing of patent and non-patent references in patent__ publications.
- __PDF coordinates__ for extracted information, allowing to create "augmented" interactive PDF.

GROBID includes batch processing, a comprehensive web service API, a JAVA API, a docker container, a relatively generic evaluation framework (precision, recall, n-fold cross-valiation, etc.) and the semi-automatic generation of training data. 

In a complete PDF processing, GROBID manages 55 final labels used to build relatively fine-grained structures, from traditional publication metadata (title, author first/last/middlenames, affiliation types, detailed address, journal, volume, issue, pages, doi, pmid, etc.) to full text structures (section title, paragraph, reference markers, head/foot notes, figure headers, etc.).

GROBID can be considered as production ready. Deployments in production includes ResearchGate, HAL Research Archive, the European Patent Office, INIST-CNRS, Mendeley, CERN (Invenio), Internet Archive, and many others. 

The key aspects of GROBID are the following ones:

+ Written in Java, with JNI call to native CRF libraries and/or Deep Learning libraries via Python JNI bridge. 
+ Speed - on low profile Linux machine (8 threads): header extraction from 4000 PDF in 2 minutes (36 PDF per second with the RESTful API), parsing of 3500 references in 4 seconds, full processing of 4000 PDF (full body, header and reference, structured) in 26 minutes (around 2.5 PDF per second). 
+ Scalability and robustness: We have been able recently to run the complete fulltext processing at around 10.6 PDF per second (around 915,000 PDF per day, around 20M pages per day) during one week on one 16 CPU machine (16 threads, 32GB RAM, no SDD, articles from mainstream publishers), see [here](https://github.com/kermitt2/grobid/issues/443#issuecomment-505208132) (11.3M PDF were processed in 6 days by 2 servers without crash).
+ Lazy loading of models and resources. Depending on the selected process, only the required data are loaded in memory. For instance, extracting only metadata header from a PDF requires less than 2 GB memory in a multithreading usage, extracting citations uses around 3GB and extracting all the PDF structures around 4GB.  
+ Robust and fast PDF processing with [pdfalto](https://github.com/kermitt2/pdfalto), based on xpdf, and dedicated post-processing.
+ Modular and reusable machine learning models for sequence labelling. The default extractions are based on Linear Chain Conditional Random Fields, with the possibility to use various Deep Learning architectures for sequence labelling (including ELMo and BERT-CRF). The specialized sequence labelling models are cascaded to build a complete (hierarchical) document structure.  
+ Full encoding in [__TEI__](http://www.tei-c.org/Guidelines/P5/index.xml), both for the training corpus and the parsed results.
+ Optional consolidation of extracted bibliographical data via online call to [biblio-glutton](https://github.com/kermitt2/biblio-glutton) or the [CrossRef REST API](https://github.com/CrossRef/rest-api-doc), export to OpenURL, BibTeX, etc. for easier integration into Digital Library environments. For scalability, reliability and accuracy, we recommend to use [biblio-glutton](https://github.com/kermitt2/biblio-glutton) when possible.
+ Rich bibliographical processing: fine grained parsing of author names, dates, affiliations, addresses, etc. but also for instance quite reliable automatic attachment of affiliations and emails to authors. 
+ _Automatic generation_ of pre-annotated training data from new PDF documents based on current models, for supporting semi-automatic training data generation. 
+ Support for CJK and Arabic languages based on customized Lucene analyzers provided by WIPO.
+ PDF coordinates for extracted information, allowing to create "augmented" interactive PDF.

The GROBID extraction and parsing algorithms use by default a [fork](https://github.com/kermitt2/wapiti) of [Wapiti CRF library](http://wapiti.limsi.fr). On Linux (64 bits) and MacOS, as alternative, it is possible to perform the sequence labelling with [DeLFT](https://github.com/kermitt2/delft) deep learning models (typically BidLSTM-CRF with or without ELMo, or BERT-CRF, with additional feature channels) instead of Wapiti CRF models, using a native integration via [JEP](https://github.com/ninia/jep). The native libraries, in particular TensorFlow, are transparently integrated as JNI with dynamic call based on the current OS. However, the best Deep Learning algorithms for sequence labelling provide so far relatively limited improvements as compared to CRF, while considerably slower (10 to 1,000 times slower with GPU) and memory-intensive. They should be used when accuracy is the main priority, at the price of reduced scalability. See the related [benchmarking](End-to-end-evaluation.md). 

GROBID should run properly "out of the box" on Linux (32 and 64 bits) and macOS. 

## Credits

The main author is Patrice Lopez (patrice.lopez@science-miner.com). 

Core committers and maintenance: Patrice Lopez (science-miner) and Luca Foppiano (NIMS).

Many thanks to:

* Vyacheslav Zholudev (Sum&Substance, formerly at ResearchGate)
* Achraf Azhar (CCSD)
* Laurent Romary (Inria)
* Daniel Ecer (eLife)
* Vitalii Bezsheiko (PKP)
* Christopher Boumenot (Microsoft) in particular for the Windows support
* CERN contributors Andreas la Roi and Micha Moskovic
* Florian Zipser (Humboldt University) who developed the first historical version of the REST API in 2011
* the other contributors from ResearchGate: Michael Häusler, Kyryl Bilokurov, Artem Oboturov
* Damien Ridereau (Infotel)
* Oliver Kopp (JabRef research)
* Bruno Pouliquen (WIPO) for the custom analyzers for Eastern languages
* Thomas Lavergne, Olivier Cappé and François Yvon for Wapiti
* The JEP team for their great JVM CPython embedding solution
* Taku Kudo for CRF++ (not used anymore, but all the same, thanks!)
* Hervé Déjean and his colleagues from Xerox Research Centre Europe, for xml2pdf
* and the other contributors: Jakob Fix, Tanti Kristanti, Bryan Newbold, Dmitry Katsubo, Phil Gooch, Romain Loth, Maud Medves, Chris Mattmann, Sujen Shah, Joseph Boyd, Guillaume Muller, ...
