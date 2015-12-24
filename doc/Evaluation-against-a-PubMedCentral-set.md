<h1>Evaluation of GROBID against PubMed Central</h1>

Individual models can be evaluated as explained in [Training the different models of Grobid](Training-the-models-of-Grobid.md).

For an end-to-end evaluation, covering the whole extraction process from the parsing of PDF to the end result of the cascading of several CRF models, GROBID includes an evaluation progress against a set of [PubMed Central](http://www.ncbi.nlm.nih.gov/pmc) articles. For its publications, PubMed Central provides both PDF and fulltext XML files in the [NLM](http://www.ncbi.nlm.nih.gov/pmc/pmcdoc/tagging-guidelines/article/style.html) format. Keeping in mind some limits described bellow, it is possible to estimate the ability of Grobid to extract and normalize the content of the PDF documents for matching the quality of the NLM file. 

## Getting PubMedCentral gold-standard data 

We are currently evaluating GROBID using the `PMC_sample_1943` dataset compiled by Alexandru Constantin. The dataset is available at this [url](http://pdfx.cs.man.ac.uk/serve/PMC_sample_1943.zip) (1.5GB in size). The sample dataset contains 1943 articles from 1943 different journals corresponding to the latest publications from a 2011 snapshot. 

Any similar PubMed Central set of articles could normally be used, as long they follow the same directory structure: one directory per article containing at least the corresponding PDF file and the reference NLM file. 

We suppose in the following that the archive is decompressed under `PATH_TO_PMC/PMC_sample_1943/`.


## Running and evaluating 

Under ```grobid-trainer/```, the following command line is used to run and evaluate Grobid on the dataset:
```bash
> mvn compile exec:exec -PPubMedCentralEval -Dpmc=*PATH_TO_PMC/PMC_sample_1943* -Drun=1
```
The parameters `run` indicates if GROBID has to be executed on all the PDF of the data set. The resulting TEI file will be added in each article subdirectory. If you only want to run the evaluation without re-executing Grobid on the PDF, set the parameter to 0:
```bash
> mvn compile exec:exec -PPubMedCentralEval -Dpmc=*PATH_TO_PMC/PMC_sample_1943* -Drun=0
```
It is also possible to set a ratio of evaluation data to be used expressed as a number between 0 and 1 introduced by the parameter `fileRatio`. For instance, if you want to evaluate Grobid against only 10% of the PubMedCentral files, use:
```bash
> mvn compile exec:exec -PPubMedCentralEval -Dpmc=*PATH_TO_PMC/PMC_sample_1943* -Drun=0 -DfileRatio=0.1
```

## Evaluation results

The evaluation provides precision, recall and f-score for the different fields in the header and bibliographical references (structures of the body arriving soon!). In addition, the scores are also computed at *instance* level, which means at the level of a complete header or complete citation.


## Matching techniques


The evaluation covers four different string matching techniques for textual fields, based on the existing evaluation approaches observed in the litterature:

* __strict__, i.e. exact match,

* __soft__ corresponding to matching ignoring punctuations, character case and space character mismatches,

* __relative Levenshtein distance__ relative to the max length of two strings

* [__Ratcliff/Obershelp similarity__](http://xlinux.nist.gov/dads/HTML/ratcliffObershelp.html) 

These macthing variants only apply to textual fields, not numerical and dates fields (such as volume, issue, dates, pages).



## Limits

A relatively important number of citations in the NLM files are encoded only as raw string, for example in the first file of the set `AAPS_J_2011_Mar_9_13(2)_230-239/12248_2011_Article_9260.nxml`: 

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

