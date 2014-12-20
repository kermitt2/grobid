## License

Grobid is distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). 

Main author and contact: Patrice Lopez (<patrice.lopez@inria.fr>)

## Purpose

GROBID means GeneRation Of BIbliographic Data (or something like that). However, we usually simply use the name Grobid. 

Grobid is a machine learning library for extracting, parsing and TEI-encoding of bibliographical information at large, with a particular focus on technical and scientific articles. The following models are available:

+ Header extraction and parsing from article in PDF format (e.g. title, abstract, authors, affiliations, keywords, etc.).
+ References extraction and parsing from article in PDF format. References in footnotes are supported, although still not very reliably. They are rare in technical and scientific articles, but frequent for publications in the humanities and social sciences. 
+ Parsing of references in isolation.
+ Extraction of patent and non-patent references in patent publications.
+ Parsing of names, in particular author names in header, and author names in references (two distinct models).
+ Parsing of affiliation and address blocks. 
+ Parsing of dates.
+ Full text extraction from PDF articles, although still experimental. Our work currently focus on more robust full text extraction and restructuring. 

Grobid includes batch processing, a comprehensive RESTful API, a JAVA library, a relatively generic evaluation framework (precision, recall, etc.) and the semi-automatic generation of training data. 

The key aspects of Grobid are the following ones:

+ Written in Java, with JNI call to native CRF libraries. 
+ High performance - on a modern but low profile MacBook Pro: header extraction from 4000 PDF in 10 minutes (or from 3 PDF per second with the RESTful API), parsing of 3000 references in 18 seconds.
+ Modular and reusable machine learning models. The extractions are based on Linear Chain Conditional Random Fields which is currently the state of the art in bibliographical information extraction and labeling.  
+ Full encoding in [__TEI__](http://www.tei-c.org/Guidelines/P5), both for the training corpus and the parsed results.
+ Reinforcement of extracted bibliographical data via online call to Crossref (optional), export in OpenURL, etc. for easier integration into Digital Library environments. 
+ Rich bibliographical processing: fine grained parsing of author names, dates, affiliations, addresses, etc. but also quite reliable automatic attachment of affiliations and emails to authors. 
+ "Automatic Generation" of pre-formatted training data based on new pdf documents, for supporting semi-automatic training data generation. 

The default Grobid extraction and parsing algorithms uses 
the Wapiti CRF library, but it is also possible to use the [CRF++ library](http://crfpp.googlecode.com/svn/trunk/doc/index.html). These C++ libraries are transparently integrated as JNI with dynamic call based on the current OS. 

Grobid should run properly "out of the box" on MacOS X, Linux (32 & 64), following the guidelines bellow. Grobid may run on Windows (JDK 32 bits) environments with the CRF++ library only, but we cannot ensure it and we currently cannot provide help for this platform (contributors to work on Windows support are very welcome!).

## Installation

See the Grobid Wiki quick start pages: 

+ [Grobid service quick start](https://github.com/grobid/grobid/wiki/Grobid-service-quick-start)
+ [Grobid batch quick start](https://github.com/grobid/grobid/wiki/Grobid-batch-quick-start)

	Grobid build relies on maven, and should be standard with respect to Open Source developments. You normally only need to build the project with maven to have it running. 


## Usage

See the Grobid Wiki pages: 

+ [Grobid service quick start](https://github.com/grobid/grobid/wiki/Grobid-service-quick-start)
+ [Grobid batch quick start](https://github.com/grobid/grobid/wiki/Grobid-batch-quick-start)
+ [Grobid java library](https://github.com/grobid/grobid/wiki/Grobid-java-library)

1. You only need the grobid-core and grobid-home subprojects for running Grobid as a library. grobid-trainer is dedicated to training, grobid-service implements the REST interface, grobid-home contains the static data, in particular the CRF models. You should now be able to build and launch the tests with mvn:

	> cd grobid-core
	> mvn test

2. The Grobid Java API is currently accessible in the class org.grobid.core.engines.Engine

3. The REST API can be deployed using the generated war file, via:

	> mvn package

4. Re-training the Grobid models is described in the following Grobid Wiki page: 

	[Training the different models of Grobid](https://github.com/grobid/grobid/wiki/Train-the-model-of-Grobid)

	The newly trained models are placed under grobid-home. An automatic evaluation of the newly created model is done after training as explained in the next point.  

5. One can run precision/recall etc. evaluation at different levels via the evaluation classes and against the test corpus present under:
grobid-trainer/resources/dataset/*MODEL*/evaluation/
where *MODEL* is the name of the model (so for instance, grobid-trainer/resources/dataset/date/evaluation/)

	See the class TrainerRunner.java as an example how to call the evaluation. To modify the evaluation files for a given model, add or removes the files in the corresponding evaluation directory.

6. To have Grobid generating training data, use the corresponding methods available in the Grobid API. A set of files in the TEI training format will be generated based on the current models. These files can then be corrected manually in case of errors, then moved to the training data folders (for instance, grobid-trainer/resources/dataset/date/corpus/). Simply then retrain to have an updated model taking into account the new training data. 

7. In general, you can simply look at the test files under grobid-core/src/test/java/org/grobid/core/test/ to see how to use the Grobid API and modify or adapt these test on your own PDF or textual data.

## Credits

The main author is Patrice Lopez (INRIA).

Many thanks to:

* Laurent Romary (INRIA), as project promoter and TEI pope. 
* Florian Zipser (Humboldt University & INRIA) who developed the REST API
* the contributors from ResearchGate: Vyacheslav Zholudev, Michael Häusler and Kyryl Bilokurov.
* Damien Ridereau (Infotel)
* Dmitry Katsubo (EPO)
* Thomas Lavergne, Olivier Cappé and François Yvon for Wapiti
* Taku Kudo for CRF++
* Hervé Déjean and his colleagues from Xerox Research Centre Europe, for xml2pdf
* and the other contributors (Phil Gooch, Maud Medves, ...)

Thank you to Inria, ResearchGate and the EPO for their help and time invested to improve Grobid. 

## References


Please simply refer to the github project:

Grobid (2008-2015) <https://github.com/grobid/grobid>

