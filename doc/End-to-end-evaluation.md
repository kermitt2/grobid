<h1>End-to-end evaluation</h1>

Individual models can be evaluated as explained in [Training the different models of Grobid](Training-the-models-of-Grobid.md).

For an end-to-end evaluation, covering the whole extraction process from the parsing of PDF to the end result of the cascading of several CRF models, GROBID includes two possible evaluation progresses:

* against a set of [PubMed Central](http://www.ncbi.nlm.nih.gov/pmc) articles. For its publications, PubMed Central provides both PDF and fulltext XML files in the [NLM](http://www.ncbi.nlm.nih.gov/pmc/pmcdoc/tagging-guidelines/article/style.html) format. Keeping in mind some limits described bellow, it is possible to estimate the ability of Grobid to extract and normalize the content of the PDF documents for matching the quality of the NLM file. 

* against TEI documents produced by [Pub2TEI](http://github.com/kermitt2/Pub2TEI). Pub2TEI is a set of XSLT that permit to tranform various _native_ XML publishers (including Elsevier, Wiley, Springer, etc. XML formats) into a common TEI format. This TEI format can be used as groundtruth structure information for evaluating GROBID output, keeping in mind some limits described bellow. 

## Getting PubMedCentral gold-standard data 

We are currently evaluating GROBID using the `PMC_sample_1943` dataset compiled by Alexandru Constantin. The dataset is available at this [url](https://grobid.s3.amazonaws.com/PMC_sample_1943.zip) (around 1.5GB in size). The sample dataset contains 1943 articles from 1943 different journals corresponding to the latest publications from a 2011 snapshot. 

Any similar PubMed Central set of articles could normally be used, as long they follow the same directory structure: one directory per article containing at least the corresponding PDF file and the reference NLM file. 

We suppose in the following that the archive is decompressed under `PATH_TO_PMC/PMC_sample_1943/`.

## Getting publisher gold-standard data 

Some publishers release publications in XML format complementary to PDF in Open Access, allowing text mining (see for instance the dedicated subset of PMC publications). On contractual basis, it is possible to acquire native XML from mainstream publishers. Unfortunately, each publisher uses a different XML schema and covering all these formats would be a very time-consuming work. To ease the processing of these XML documents, the projet [Pub2TEI](http://github.com/kermitt2/Pub2TEI) permits to transform the native XML formats of a dozen mainstream publishers into a common TEI format which is the same as the output of GROBID. 

See [Pub2TEI](http://github.com/kermitt2/Pub2TEI) for converting native publisher XML into usable TEI. 

## Directory structure

For running the evaluation, the tool assumes that the files are organised in a set of directory in the following way: 

* a root directory containing one sub-directory per article

* each article sub-directory containing at least the PDF version and a gold XML structured version of the article (in NLM format for PubMedCentral evaluation or in TEI format for the Pub2TEI-based evaluation). See the diagram bellow - the name of the sub-directory and the files is free.  

* extension for files generated with [Pub2TEI](http://github.com/kermitt2/Pub2TEI) is `.pub2tei.tei.xml`. Extension for NLM files is `.nxlm`. GROBID will generate additional TEI files with extension `.fulltext.tei.xml`. 

```
├── article1
│   ├── article1.pdf
│   └── article1.pub2tei.tei.xml
│   └── article1.nxml
│  
└── articles2
│   ├── article2.pdf
│   └── article2.pub2tei.tei.xml
│   └── article2.nxml
...
```

## Running and evaluating 

### PubMedCentral

Under ```grobid/```, the following command line is used to run and evaluate Grobid on the dataset:
```bash
> ./gradlew PubMedCentralEval -Pp2t=ABS_PATH_TO_PMC/PMC_sample_1943 -Prun=1
```
The parameters `run` indicates if GROBID has to be executed on all the PDF of the data set. The resulting TEI file will be added in each article subdirectory. If you only want to run the evaluation without re-executing Grobid on the PDF, set the parameter to 0:
```bash
> ./gradlew PubMedCentralEval -Pp2t=ABS_PATH_TO_PMC/PMC_sample_1943 -Prun=0
```
It is also possible to set a ratio of evaluation data to be used expressed as a number between 0 and 1 introduced by the parameter `fileRatio`. For instance, if you want to evaluate Grobid against only 10% of the PubMedCentral files, use:
```bash
> ./gradlew PubMedCentralEval -Pp2t=ABS_PATH_TO_PMC/PMC_sample_1943 -Prun=0 -PfileRatio=0.1
```

### Pub2TEI-based

Under ```grobid/```, the following command line is used to run and evaluate Grobid on the dataset:
```bash
> ./gradlew Pub2TeiEval -Pp2t=ABS_PATH_TO_TEI/ -Prun=1
```
The parameters `run` indicates if GROBID has to be executed on all the PDF of the data set. The resulting GROBID TEI file will be added in each article subdirectory. If you only want to run the evaluation without re-executing Grobid on the PDF, set the parameter to 0:
```bash
> ./gradlew Pub2TeiEval -Pp2t=ABS_PATH_TO_TEI/ -Prun=0
```
It is also possible to set a ratio of evaluation data to be used expressed as a number between 0 and 1 introduced by the parameter `fileRatio`. For instance, if you want to evaluate Grobid against only 10% of the Pub2TEI-produced files, use:
```bash
> ./gradlew Pub2TeiEval -Pp2t=ABS_PATH_TO_TEI/ -Prun=0 -PfileRatio=0.1
```

## Evaluation results

The evaluation provides precision, recall and f-score for the different fields in the header and bibliographical references. In addition, the scores are also computed at *instance* level, which means at the level of a complete header or complete citation.

An experimental evaluation for the structures of the full text body is also proposed. This is not reliable in the current state, because most of the annotations of the full texts in PudMed Central are not uniform. For instance, the numbering of the section header is sometime included in the section header annotation, sometime not. The PubMed Central annotations will need to be standardized as a pre-process for a meaningful evaluation, which is a task planned in the next releases. 

## Matching techniques


The evaluation covers four different string matching techniques for textual fields, based on the existing evaluation approaches observed in the litterature:

* __strict__, i.e. exact match,

* __soft__ corresponding to matching ignoring punctuations, character case and space character mismatches,

* __relative Levenshtein distance__ relative to the max length of two strings

* [__Ratcliff/Obershelp similarity__](http://xlinux.nist.gov/dads/HTML/ratcliffObershelp.html) 

These macthing variants only apply to textual fields, not numerical and dates fields (such as volume, issue, dates, pages).



## Limits

### Non structured information in XML "gold" data

A relatively important number of citations in the NLM files (and other native publisher XML) are encoded only as raw string, for example in the first file of the set `AAPS_J_2011_Mar_9_13(2)_230-239/12248_2011_Article_9260.nxml`: 

```xml
	  ...
      <ref id="CR9">
        <label>9.</label>
        <mixed-citation publication-type="other">Pira&#xF1;a and PCluster: a modeling environment and cluster infrastructure for NONMEM. Keizer RJ, van Benten M, Beijnen JH, Schellens JH, Huitema AD. Comput Methods Programs Biomed. 2011;101(1):72&#x2013;9.</mixed-citation>
      </ref>
      <ref id="CR10">
        <label>10.</label>
        <mixed-citation publication-type="other">Holford, N. VPC, the visual predictive check&#x2014;superiority to standard diagnostic (Rorschach) plots. In: PAGE 2005. 2005.</mixed-citation>
      </ref>
	  ...
```
(this file contains for instance 3 non-encoded citations out of 18)

As a consequence, the fields extracted by GROBID will not match any reference 'expected' values and will all be considered as false positive. The scores for the citation structures are thus lower than the actual performance of the system. 

### Character encoding and glyphs

XML and PDF content frequently contain many differences at character-level. This is due to PDF which tend to use particular glyphs for enhancing visual rendering. Those special glyths are often loaded in the PDF itself and uses particular unicode not matching unicode of characters in XML. Similarly some special charcaters are expressed with fonts (for instance using a Greek font to render a λ, using the unicode of the letter _l_). 

### Ordering and presentation variants of structures and sub-structures

The order of some structures might also be changed from the logical representation (XML) to the particular PDF presentation base on (unknown) style transformation. 

Still related to style rendering, text can be post-processed. For instance, in a bibliographical reference, forenames can be present in full form in the XML, but shorten to intials only in the PDF.

### Evaluation criteria

The tool uses currently rather strict evaluation criteria. For instance, `authors` field is considered correct only if the whole set of authors, including the order of authors, match. More partial and fine-grained matching is not implemented yet. 

*Given these limit, this evaluation cannot be considered currently as a reliable absolute evaluation (how good GROBID will extract valid and usable structures from PDF), but rather as a way to keep track of progress from one version of GROBID to another one and avoid regressions.*

