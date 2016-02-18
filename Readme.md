# GROBID

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/kermitt2/grobid.svg?branch=master)](https://travis-ci.org/kermitt2/grobid)
[![Coverage Status](https://coveralls.io/repos/kermitt2/grobid/badge.svg)](https://coveralls.io/r/kermitt2/grobid)
[![Documentation Status](https://readthedocs.org/projects/grobid/badge/?version=latest)](https://readthedocs.org/projects/grobid/?badge=latest)


## Purpose

GROBID (or Grobid) means GeneRation Of BIbliographic Data. 

GROBID is a machine learning library for extracting, parsing and re-structuring raw documents such as PDF into structured TEI-encoded documents with a particular focus on technical and scientific publications. First developments started in 2008 as a hobby. In 2011 the tool has been made available in open source. Work on GROBID has been steady as side project since the beginning and is expected to continue until at least 2020 :)

The following functionalities are available:

+ Header extraction and parsing from article in PDF format. The extraction here covers the usual bibliographical information (e.g. title, abstract, authors, affiliations, keywords, etc.).
+ References extraction and parsing from articles in PDF format. References in footnotes are supported, although still work in progress. They are rare in technical and scientific articles, but frequent for publications in the humanities and social sciences. All the usual publication metadata are covered. 
+ Parsing of references in isolation.
+ Extraction of patent and non-patent references in patent publications.
+ Parsing of names (e.g. person title, fornames, middlename, etc.), in particular author names in header, and author names in references (two distinct models).
+ Parsing of affiliation and address blocks. 
+ Parsing of dates (ISO normalized day, month, year).
+ Full text extraction from PDF articles, including a model for the the overall document segmentation and a model for the structuring of the text body. 
+ In a complete PDF processing, GROBID manages 55 final labels used to build relatively fine-grained structures, from traditional publication metadata (title, author first/last/middlenames, affiliation types, detailed address, journal, volume, issue, pages, etc.) to full text structures (section title, paragraph, reference markers, head/foot notes, figure headers, etc.). 

GROBID includes batch processing, a comprehensive RESTful API, a JAVA API, a relatively generic evaluation framework (precision, recall, etc.) and the semi-automatic generation of training data. 

GROBID can be considered as production ready. Deployment in production includes ResearchGate, HAL Research Archive, the European Patent Office, INIST, Mendeley, CERN, ... 

The key aspects of GROBID are the following ones:

+ Written in Java, with JNI call to native CRF libraries. 
+ High performance - on a modern but low profile MacBook Pro: header extraction from 4000 PDF in 10 minutes (or from 3 PDF per second with the RESTful API), parsing of 3000 references in 18 seconds. [INIST](http://www.inist.fr/lang=en) recently scaled GROBID REST service for processing 1 million PDF in 1 day on a Xeon 10 CPU E5-2660 and 10 GB memory (3GB used in average) with 9 threads.
+ Lazy loading of models and resources. Depending on the selectd process, only the required data are loaded in memory. For instance, extracting only metadata header from a PDF requires less than 2 GB memory in a multithreading usage, extracting citations uses around 3GB and extracting all the PDF structure around 4GB.  
+ Robust and fast PDF processing based on Xpdf and dedicated post-processing.
+ Modular and reusable machine learning models. The extractions are based on Linear Chain Conditional Random Fields which is currently the state of the art in bibliographical information extraction and labeling. The specialized CRF models are cascaded to realize a complete document structure.  
+ Full encoding in [__TEI__](http://www.tei-c.org/Guidelines/P5), both for the training corpus and the parsed results.
+ Reinforcement of extracted bibliographical data via online call to Crossref (optional), export in OpenURL, etc. for easier integration into Digital Library environments. 
+ Rich bibliographical processing: fine grained parsing of author names, dates, affiliations, addresses, etc. but also for instance quite reliable automatic attachment of affiliations and emails to authors. 
+ "Automatic Generation" of pre-formatted training data based on new pdf documents, for supporting semi-automatic training data generation. 
+ Support for CJK and Arabic languages based on customized Lucene analyzers provided by WIPO.

The GROBID extraction and parsing algorithms uses the [Wapiti CRF library](http://wapiti.limsi.fr). [CRF++ library](http://crfpp.googlecode.com/svn/trunk/doc/index.html) is not supported since GROBID version 0.4. The C++ libraries are transparently integrated as JNI with dynamic call based on the current OS. 

GROBID should run properly "out of the box" on MacOS X, Linux (32 and 64 bits). GROBID does currently not run on Windows environments because the required and up-to-date CRF native binaries are not yet compiled for this platform (contributors to work on Windows support are very welcome!).

## GROBID documentation

Visit the [GROBID documentation](http://grobid.readthedocs.org) for more detailed information.

## License

GROBID is distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). 

Main author and contact: Patrice Lopez (<patrice.lopez@science-miner.com>)

## References

Please simply refer to the github project:

Grobid (2008-2015) <https://github.com/kermitt2/grobid>

See the [GROBID documentation](http://grobid.readthedocs.org/en/latest/References) for more related resources. 
