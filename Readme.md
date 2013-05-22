## License

Grobid is distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). 

Main author and contact: Patrice Lopez (<patrice.lopez@inria.fr>)

## Purpose

GROBID means GeneRation Of BIbliographic Data (or something like that). However, we usually simply use the name Grobid. 

Grobid is a machine learning library for extracting, parsing and TEI-encoding of bibliographical information at large, with a particular focus on technical and scientific articles. The following models are available:

+ Header extraction and parsing from article in PDF format (e.g. title, abstract, authors, affiliations, keywords, etc.).
+ References extraction and parsing from article in PDF format. There is no support yet for references in footnotes! They are rare in technical and scientific articles, but frequent for publications in the humanities and social sciences. We will work in the next months to cover also this sort of references. 
+ Parsing of references in isolation.
+ Extraction of patent and non-patent references in patent publications.
+ Parsing of names, in particular author names in header, and author names in references (two distinct models).
+ Parsing of affiliation and address blocks. 
+ Parsing of dates.
+ Full text extraction from PDF articles, although still experimental. Our work in the next months will focus on robust full text extraction. 

Grobid includes a relatively generic evaluation framework (precision, recall, etc.) and a RESTful API. 

The key aspects of Grobid are the following ones:

+ Written in Java (with JNI call).
+ High performance - on a modern but low profile MacBook Pro: header extraction from 4000 PDF in 10 minutes, parsing of 3000 references in 18 seconds.
+ Modular and reusable machine learning models. The extractions are based on Linear Chain Conditional Random Fields which is currently the state of the art in bibliographical information extraction and labeling.  
+ Full encoding in [__TEI__](http://www.tei-c.org/Guidelines/P5), both for the training corpus and the parsed results.
+ Reinforcement of extracted bibliographical data via online call to Crossref (optional), export in OpenURL, etc. for easier integration into Digital Library environments. 
+ Rich bibliographical processing: fine grained parsing of author names, dates, affiliations, addresses, etc. but also quite reliable automatic attachment of affiliations to corresponding authors. 
+ "Automatic Generation" of pre-formatted training data based on new pdf documents, for supporting semi-automatic training data generation. 

The current Grobid extraction and parsing algorithms uses 
the [CRF++ library](http://crfpp.googlecode.com/svn/trunk/doc/index.html). This C++ library is transparently integrated as JNI with dynamic call based on the current OS. 

Grobid should run properly on MacOS X, Linux (32 & 64) and Windows (32) environments "out of the box", following the guidelines bellow. 

## Installation

See the Grobid Wiki quick start pages: 

+ [Grobid service quick start](https://github.com/grobid/grobid/wiki/Grobid-service-quick-start)
+ [Grobid batch quick start](https://github.com/grobid/grobid/wiki/Grobid-batch-quick-start)

	Grobid build relies on maven, and should be standard with respect to Open Source developments. You normally only need to build the project with maven to have it running. 

1. clone the github Grobid project: 

	> git clone git://github.com/grobid/grobid.git

	build it with maven:

	> mvn package

2. For convenience, pdf2xml (based on xpdf and compiled for Linux 32, Linux 64, Mac OS X, and Windows 32) is coming with Grobid. The right (static) binaries according to your architecture will be used automatically. 
For reference, the pdf2xml project is located here: <http://sourceforge.net/projects/pdf2xml>

3. Have a look at the main property file under: grobid/grobid-home/config/grobid.properties
where you can set up your online call to Crossref, a cache to Crossref call with a MySQL database and runtime properties.

4. Note that by default Grobid uses Lingpipe for language identification. You might want to turn language identification off in the above mentioned Grobid property file (Lingpipe is not distributed under an Apache license) or use another language identifier (see the language identifier interface under grobid-core/lang and its implementation under Lingpipe grobid-core/lang/impl).

## Usage

See the Grobid Wiki pages: 

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

* Laurent Romary (INRIA), as project godfather and TEI pope. 
* Florian Zipser (Humboldt University & INRIA) who developed the REST API
* the contributors from ResearchGate: Vyacheslav Zholudev, Michael Häusler and Kyryl Bilokurov.
* Damien Ridereau (Infotel)
* Dmitry Katsubo (EPO)
* Taku Kudo for CRF++
* Hervé Déjean and his colleagues from Xerox Research Centre Europe, for xml2pdf
* and the other contributors (Maud Medves, Ozair Saleem, ...)

Thank you to Inria, ResearchGate and the EPO for their help and time invested to improve Grobid. 

## References

If you want to cite Grobid, you can use this old paper:

GROBID: Combining Automatic Bibliographic Data Recognition and Term Extraction for Scholarship Publications. P. Lopez. Proceedings of the 13th European Conference on Digital Library (ECDL), Corfu, Greece, 2009.

You might better simply refer to the github project:

<https://github.com/grobid/grobid>

