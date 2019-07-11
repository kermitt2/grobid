<h1>Introduction</h1>

## Status

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/kermitt2/grobid.svg?branch=master)](https://travis-ci.org/kermitt2/grobid)
[![Coverage Status](https://coveralls.io/repos/kermitt2/grobid/badge.svg)](https://coveralls.io/r/kermitt2/grobid)
[![Documentation Status](https://readthedocs.org/projects/grobid/badge/?version=latest)](https://readthedocs.org/projects/grobid/?badge=latest)
[![Docker Status](https://images.microbadger.com/badges/version/lfoppiano/grobid.svg)](https://hub.docker.com/r/lfoppiano/grobid/ "Latest Docker HUB image")

## Purpose

GROBID (or Grobid, but not GroBid nor GroBiD) means GeneRation Of BIbliographic Data. 

GROBID is a machine learning library for extracting, parsing and re-structuring raw documents such as PDF into structured XML/TEI encoded documents with a particular focus on technical and scientific publications. First developments started in 2008 as a hobby. In 2011 the tool has been made available in open source. Work on GROBID has been steady as side project since the beginning and is expected to continue until at least 2020 :)

The following functionalities are available:

+ Header extraction and parsing from article in PDF format. The extraction here covers the bibliographical information (e.g. title, abstract, authors, affiliations, keywords, etc.).
+ References extraction and parsing from articles in PDF format. References in footnotes are supported, although still work in progress. They are rare in technical and scientific articles, but frequent for publications in the humanities and social sciences. 
+ Parsing of references in isolation.
+ Extraction of patent and non-patent references in patent publications.
+ Parsing of names, in particular author names in header, and author names in references (two distinct models).
+ Parsing of affiliation and address blocks. 
+ Parsing of dates.
+ Full text extraction from PDF articles, including a model for the the overall document segmentation and a model for the structuring of the text body.

GROBID includes batch processing, a comprehensive RESTful API, a JAVA API, a docker container, a relatively generic evaluation framework (precision, recall, etc.) and the semi-automatic generation of training data. 

GROBID can be considered as production ready. Deployments in production includes ResearchGate, HAL Research Archive, the European Patent Office, INIST, Mendeley, CERN, Internet Archive, ...

The key aspects of GROBID are the following ones:

+ Written in Java, with JNI call to native CRF libraries. 
+ Speed - on a modern but low profile MacBook Pro: header extraction from 4000 PDF in 10 minutes (or from 3 PDF per second with the RESTful API), parsing of 3000 references in 18 seconds. 
+ Speed and Scalability: [INIST](http://www.inist.fr) recently scaled GROBID REST service for extracting bibliographical references of 1 million PDF in 1 day on a Xeon 10 CPU E5-2660 and 10 GB memory (3GB used in average) with 9 threads - so around 11.5 PDF per second. The complete processing of 395,000 PDF (IOP) with full text structuring was performed in 12h46mn with 16 threads, 0.11s per PDF (~1,72s per PDF with single thread).
+ Lazy loading of models and resources. Depending on the selected process, only the required data are loaded in memory. For instance, extracting only metadata header from a PDF requires less than 2 GB memory in a multithreading usage, extracting citations uses around 3GB and extracting all the PDF structure around 4GB.  
+ Robust and fast PDF processing with [pdfalto](https://github.com/kermitt2/pdfalto), based on Xpdf, and dedicated post-processing.
+ Modular and reusable machine learning models. The extractions are based on Linear Chain Conditional Random Fields which is currently the state of the art in bibliographical information extraction and labeling. The specialized CRF models are cascaded to build a complete document structure.  
+ Full encoding in [__TEI__](http://www.tei-c.org/Guidelines/P5/index.xml), both for the training corpus and the parsed results.
+ Reinforcement of extracted bibliographical data via online call to CrossRef (optional), export in OpenURL, BibTeX, etc. for easier integration into Digital Library environments. 
+ Rich bibliographical processing: fine grained parsing of author names, dates, affiliations, addresses, etc. but also for instance quite reliable automatic attachment of affiliations and emails to authors. 
+ "Automatic Generation" of pre-formatted training data based on new PDF documents, for supporting semi-automatic training data generation. 
+ Support for CJK and Arabic languages based on customized Lucene analyzers provided by WIPO.

The GROBID extraction and parsing algorithms uses by default the [Wapiti CRF library](http://wapiti.limsi.fr). On Linux (64 bits), as alternative, it is possible to perform the sequence labeling with [DeLFT](https://github.com/kermitt2/delft) deep learning models (typically BidLSTM-CRF with or without ELMo) instead of Wapiti CRF models, using a native integration via [JEP](https://github.com/ninia/jep). The native libraries are transparently integrated as JNI with dynamic call based on the current OS. 

GROBID should run properly "out of the box" on macOS, Linux (32 and 64 bits) and Windows. 

## Credits

The main author is Patrice Lopez (patrice.lopez@science-miner.com).

Many thanks to:

* Vyacheslav Zholudev (ResearchGate)
* Luca Foppiano (NIMS) 
* Achraf Azhar (Inria)
* Christopher Boumenot (Microsoft) in particular for the Windows support
* Laurent Romary (Inria)
* CERN contributors Andreas la Roi and Micha Moskovic
* Florian Zipser (Humboldt University) who developed the first historical version of the REST API in 2011
* the other contributors from ResearchGate: Michael Häusler, Kyryl Bilokurov, Artem Oboturov
* Damien Ridereau (Infotel)
* Daniel Ecer (eLife)
* Vitalii Bezsheiko (PKP)
* Bruno Pouliquen (WIPO) for the custom analyzers for Eastern languages
* Thomas Lavergne, Olivier Cappé and François Yvon for Wapiti
* Taku Kudo for CRF++ (not used anymore, but all the same, thanks!)
* Hervé Déjean and his colleagues from Xerox Research Centre Europe, for xml2pdf
* and the other contributors: Dmitry Katsubo, Phil Gooch, Romain Loth, Maud Medves, Chris Mattmann, Sujen Shah, Joseph Boyd, Guillaume Muller, ...

