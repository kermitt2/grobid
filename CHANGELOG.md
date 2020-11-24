# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

+ Option to get sentence segmented text in extracted structures (choice between the Pragmatic Segmenter, integrated via JRuby, and OpenNLP sentence detector)
+ Option to get PDF coordinates for `<s>` structures

### Changed

+ Update of TEI XML schema to allow `<s>` structurs in the result

### Fixed

+ Structuration of abstract is back
+ Deprecated CrossRef `query.title` field for the CrossRef consolidation service

## [0.6.1] – 2020-08-12

### Added

+ Support of line number (typically in preprints)
+ End-to-end evaluation and benchmark for preprints using the bioRxiv 10k dataset 
+ Check whether PDF annotation is orcid and add orcid to author in the TEI result
+ Configuration for making sequence labeling engine (CRF Wapiti or Deep Learning) specific to models
+ Add a developers guide and a FAQ section in the documentation
+ Visualization of formulas on PDF layout in the demo console
+ Feature for subscript/superscript style in fulltext model

### Changed

+ New significantly improved header model: with new features, new training data (600 new annotated examples, old training data is entirely removed), new labels and updated data structures in line with the other models
+ Update of the segmentation models with more training data
+ Removal of heuristics related to the header
+ Update to gradle 6.5.1 to support JDK 13 and 14
+ TEI schemas 
+ Windows is not supported in this release

### Fixed

+ Preserve affiliations after consolidation of the authors 
+ Environment variable config override for all properties 
+ Unfrequent duplication of the abstract in the TEI result
+ Incorrect merging of affiliations
+ Noisy parentheses in the bibliographical reference markers
+ In the console demo, fix the output filename wrongly taken from the input form when the text form is used
+ Synchronisation of the language detection singleton initialisation in case of multithread environment
+ Other minor fixes

## [0.6.0] – 2020-04-24

### Added

+ Table content structuring (thanks to @Vitaliy-1), see [PR #546](https://github.com/kermitt2/grobid/pull/546)
+ Support for `application/x-bibtex` at `/api/processReferences` and `/api/processCitation` (thanks to @koppor)
+ Optionally include raw affiliation string in the TEI result
+ Add dummy model for facilitating test in Grobid modules
+ Allow environment variables for config properties values to ease Docker config 
+ ChangeLog

### Changed

+ Improve CORS configuration #527 (thank you @lfoppiano)
+ Documentation improvements
+ Update of segmentation and fulltext model and training data
+ Better handling of affiliation block fragments
+ Improved DOI string recognition
+ More robust n-fold cross validation (case of shared grobid-home)

### Fixed

+ Fixed flags of pdf2xml in `Dockerfile`
+ Some fixes for better TEI result format conformance 
+ Other minor fixes

## [0.5.6] – 2019-10-16

### Added

+ n-fold cross evaluation and better evaluation report (thanks to @lfoppiano)

### Changed

+ Better abstract structuring (with citation contexts)
+ Improved PMC ID and PMID recognition
+ Improved subscript/superscript and font style recognition (via [pdfalto](https://github.com/kermitt2/pdfalto))
+ Improved JEP integration (support of python virtual environment for using DeLFT Deep Learning library, thanks @de-code and @lfoppiano)
+ Improved dehyphenization (thanks to @lfoppiano)

### Fixed

+ Several bug fixes (thanks @de-code, @bnewbold, @Vitaliy-1 and @lfoppiano)

## [0.5.5] – 2019-05-29

### Changed

+ Using [pdfalto](https://github.com/kermitt2/pdfalto) instead of pdf2xml for the first PDF parsing stage, with many improvements in robustness, ICU support, unknown glyph/font normalization (thanks in particular to @aazhar)
+ Improvement and full review of the integration of consolidation services, supporting [biblio-glutton](https://github.com/kermitt2/biblio-glutton) (additional identifiers and Open Access links) and [Crossref REST API](https://github.com/CrossRef/rest-api-doc) (add specific user agent, email and token for Crossref Metadata Plus)
+ Updated lexicon #396

### Fixed

+ Fix bounding box issues for some PDF #330

## [0.5.4] – 2019-02-12

### Added

+ Transparent usage of [DeLFT](https://github.com/kermitt2/delft) deep learning models (BidLSTM-CRF/ELMo) instead of Wapiti CRF models, native integration via [JEP](https://github.com/ninia/jep)
+ Support of [biblio-glutton](https://github.com/kermitt2/biblio-glutton) as DOI/metadata matching service, alternative to crossref REST API

### Changed

+ Improvement of citation context identification and matching (+9% recall with similar precision, for PMC sample 1943 articles, from 43.35 correct citation contexts per article to 49.98 correct citation contexts per article)
+ Citation callout now in abstract, figure and table captions
+ Structured abstract (including update of TEI schema)

### Fixed

+ Bug fixes and some more parameters: by default using all available threads when training (thanks [@de-code](https://github.com/de-code)) and possibility to load models at the start of the service

## [0.5.3] – 2018-11-25

### Added

+ Support of proxy for calling crossref with Apache HttpClient

### Changed

+ Improvement of consolidation options and processing (better handling of CrossRef API, but the best is coming soon ;)
+ Better recall for figure and table identification (thanks to @detonator413)

### Fixed

+ Minor bug fixing

## [0.5.2] – 2018-10-17

### Added

+ Added [Grobid clients](https://grobid.readthedocs.io/en/latest/Grobid-service/#clients-for-grobid-web-services) for Java, Python and NodeJS
+ Added metrics in the REST entrypoint (accessible via <http://localhost:8071>)
+ Added counters for consolidation tasks and consolidation results
+ Added case sensitiveness option in lexicon/FastMatcher

### Changed

+ Updated documentation

### Fixed

+ Corrected back status codes from the REST API when no available engine (503 is back again to inform the client to wait, it was removed by error in version 0.5.0 and 0.5.1 for PDF processing services only, see documentation of the REST API)
+ Bugfixing #339, #322, #300, and others

## [0.5.1] – 2018-01-29

### Fixed

+ Various bug fixes

## [0.5.0] – 2017-11-09

### Changed

+ Migrate from maven to gradle for faster, more flexible and more stable build, release, etc.
+ Usage of Dropwizard for web services
+ Move the Grobid service manual to [readthedocs](http://grobid.readthedocs.io/en/latest/Grobid-service/)
+ (thanks to @detonator413 and @lfoppiano for this release! future work in versions 0.5.* will focus again on improving PDF parsing and structuring accuracy)

## [0.4.4] – 2017-10-13

### Fixed

+ Fixed issue that was making the release build not working

## [0.4.3] – 2017-10-07

### Added

+ New training data and features for bibliographical references, in particular for covering HEP domain (INSPIRE), arXiv identifier, DOI and url (thanks @iorala and @michamos !)
+ Support for CrossRef REST API (instead of the slow OpenURL-style API which requires a CrossRef account), in particular for multithreading usage (thanks @Vi-dot)
+ Unicode normalisation and more robust body extraction (thanks @aoboturov)

### Changed

+ Updated models: f-score improvement on the PubMed Central sample, bibliographical references +2.5%, header +7%  
+ Improve training data generation and documentation (thanks @jfix)
+ Update of the pdf2xml fork for Windows (thanks @lfoppiano)

### Fixed

+ fixes, tests, documentation

## [0.4.2] – 2017-08-05

### Added

+ Identification of equations (with PDF coordinates)
+ End-to-end evaluation with Pub2TEI conversions

### Changed

+ f-score improvement for the PubMed Central sample: fulltext +10-14%, header +0.5%, citations +0.5%
+ More robust PDF parsing

### Fixed

+ many fixes and refactoring

## [0.4.1] – 2016-10-02

### Added

+ Support for Windows thanks to the contributions of Christopher Boumenot!
+ Support for Docker
+ New web services for PDF annotation and updated web console application

### Changed

+ Some improvements on figure/table extraction - but still experimental at this stage (work in progress, as the whole full text model)

### Fixed

+ Fixes and refactoring.

## [0.4.0] – 2016-10-02

### Changed

+ Improvement of the recognition of citations thanks to refinements of CRF features - +4% in f-score for the PubMed Central sample.
+ Improvement of the full text model, with new features and the introduction of two additional models for figures and tables.
+ More robust synchronization of CRF sequence with PDF areas, resulting in improved bounding box calculations for locating annotations in the PDF documents.
+ Improved general robustness thanks to better token alignments.

[Unreleased]: https://github.com/kermitt2/grobid/compare/0.6.0...HEAD
[0.6.0]: https://github.com/kermitt2/grobid/compare/0.5.6...0.6.0
[0.5.6]: https://github.com/kermitt2/grobid/compare/0.5.5...0.5.6
[0.5.5]: https://github.com/kermitt2/grobid/compare/0.5.4...0.5.5
[0.5.4]: https://github.com/kermitt2/grobid/compare/0.5.3...0.5.4
[0.5.3]: https://github.com/kermitt2/grobid/compare/0.5.2...0.5.3
[0.5.2]: https://github.com/kermitt2/grobid/compare/0.5.1...0.5.2
[0.5.1]: https://github.com/kermitt2/grobid/compare/0.5.0...0.5.1
[0.5.0]: https://github.com/kermitt2/grobid/compare/grobid-parent-0.4.4...0.5.0
[0.4.4]: https://github.com/kermitt2/grobid/compare/grobid-parent-0.4.3...grobid-parent-0.4.4
[0.4.3]: https://github.com/kermitt2/grobid/compare/grobid-parent-0.4.2...grobid-parent-0.4.3
[0.4.2]: https://github.com/kermitt2/grobid/compare/grobid-parent-0.4.1...grobid-parent-0.4.2
[0.4.1]: https://github.com/kermitt2/grobid/compare/grobid-parent-0.4.0...grobid-parent-0.4.1
[0.4.0]: https://github.com/kermitt2/grobid/compare/grobid-parent-0.3.9...grobid-parent-0.4.0

<!-- markdownlint-disable-file MD024 MD033 -->
