## Project reference

If you want to cite this work, please simply refer to the github project:

```
GROBID (2008-2021) <https://github.com/kermitt2/grobid>
```

Please do not include a particular person name to emphasize the project and the tool ! 

We also ask you not to cite any old research papers, but the current project itself, because, yes, we can try to cite a software project in the bibliographical references and not just mention it in a foot note ;) 
Well, it might be (likely) rejected by reviewers, the editorial style or editors, but at least you tried ! 

Here's a BibTeX entry using the [Software Heritage](https://www.softwareheritage.org/) project-level permanent identifier:

```bibtex
@misc{GROBID,
    title = {GROBID},
    howpublished = {\url{https://github.com/kermitt2/grobid}},
    publisher = {GitHub},
    year = {2008--2022},
    archivePrefix = {swh},
    eprint = {1:dir:dab86b296e3c3216e2241968f0d63b68e8209d3c}
}
```

## Presentations on Grobid

The following presentations are reminders that old machine learning stuff is not like good wine. Please use this project repository for up-to-date information. 

[GROBID in 30 slides](grobid-04-2015.pdf) (2015).

[GROBID in 20 slides](GROBID.pdf) (2012).

P. Lopez. Automatic Extraction and Resolution of Bibliographical References in Patent Documents. First Information Retrieval Facility Conference (IRFC), Vienna, May 2010. LNCS 6107, pp. 120-135. Springer, Heidelberg, 2010.

## Evaluation and usages

The following articles are provided for information - it does not mean that we agree with all their statements about Grobid (please refer to the present documentation for the actual features and capacities of the tool) or with all the various methodologies used for evaluation, but they all explore interesting aspects related to Grobid. 

- M. Lipinski, K. Yao, C. Breitinger, J. Beel, and B. Gipp. [Evaluation of Header Metadata Extraction Approaches and Tools for Scientific PDF Documents](http://docear.org/papers/Evaluation_of_Header_Metadata_Extraction_Approaches_and_Tools_for_Scientific_PDF_Documents.pdf), in Proceedings of the 13th ACM/IEEE-CS Joint Conference on Digital Libraries (JCDL), Indianapolis, IN, USA, 2013. 

- Joseph Boyd. [Automatic Metadata Extraction The High Energy Physics Use Case](https://preprints.cern.ch/record/2039361/files/CERN-THESIS-2015-105.pdf). Master Thesis, EPFL, Switzerland, 2015. 

- Phil Gooch and Kris Jack, [How well does Mendeley’s Metadata Extraction Work?](https://krisjack.wordpress.com/2015/03/12/how-well-does-mendeleys-metadata-extraction-work/), 2015.

- [Meta-eval](https://github.com/allenai/meta-eval), 2015.

- D. Tkaczyk, A. Collins, P. Sheridan, & J. Beel. Evaluation and Comparison of Open Source Bibliographic Reference Parsers: A Business Use Case. [arXiv:1802.01168](https://arxiv.org/pdf/1802.01168), 2018.

- Kyle Lo, Lucy Lu Wang, Mark Neumann, Rodney Kinney and Dan S. Weld. [S2ORC: The Semantic Scholar Open Research Corpus](https://arxiv.org/pdf/1911.02782.pdf). [arXiv:1911.02782](https://arxiv.org/abs/1911.02782), [github](https://github.com/allenai/s2-gorc), 2019

- [CORD-19: The COVID-19 Open Research Dataset](https://arxiv.org/pdf/2004.10706.pdf). 2020. [https://pages.semanticscholar.org/coronavirus-research](https://pages.semanticscholar.org/coronavirus-research), [arXiv:2004.10706](https://arxiv.org/abs/2004.10706). 
(See also [here](https://discourse.cord-19.semanticscholar.org/t/faqs-about-cord-19-dataset/94))

- Mark Grennan and Joeran Beel. [Synthetic vs. Real Reference Strings for Citation Parsing, and the Importance of Re-training and Out-Of-Sample Data for Meaningful Evaluations: Experiments with GROBID, GIANT and Cora](https://arxiv.org/pdf/2004.10410.pdf). [arXiv:2004.10410](https://arxiv.org/abs/2004.10410), 2020.

- J.M. Nicholson, M. Mordaunt, P. Lopez, A. Uppala, D. Rosati, N.P. Rodrigues, P. Grabitz, S.C. Rife. 
[scite: a smart citation index that displays the context of citations and classifies their intent using deep learning](https://www.biorxiv.org/content/10.1101/2021.03.15.435418v1); bioRxiv preprint 2021. doi: https://doi.org/10.1101/2021.03.15.435418 

## Articles on CRF for bibliographical reference parsing

For archeological purposes, the first paper has been the main motivation and influence for starting GROBID. 

- Fuchun Peng and Andrew McCallum. [Accurate Information Extraction from Research Papers using Conditional Random Fields](https://www.aclweb.org/anthology/N04-1042.pdf). Proceedings of Human Language Technology Conference and North American Chapter of the Association for Computational Linguistics (HLT-NAACL), 2004.

- Isaac G. Councill, C. Lee Giles, Min-Yen Kan. [ParsCit: An open-source CRF reference string parsing package](http://www.lrec-conf.org/proceedings/lrec2008/pdf/166_paper.pdf). In Proceedings of the Language Resources and Evaluation Conference (LREC), Marrakesh, Morrocco, 2008.

## Datasets

For end-to-end evaluation:

- [PMC_sample_1943](https://grobid.s3.amazonaws.com/PMC_sample_1943.zip)

- [bioRxiv 10k](https://zenodo.org/record/3873702)

For layout/zoning identification:

- [GROTOAP2](https://repod.icm.edu.pl/dataset.xhtml?persistentId=doi:10.18150/8527338)

- [PubLayNet](https://github.com/ibm-aur-nlp/PubLayNet)

- [DocBank](https://github.com/doc-analysis/DocBank)

## Similar open source tools

- [parsCit](https://github.com/knmnyn/ParsCit)

- [Neural-ParsCit](https://github.com/WING-NUS/Neural-ParsCit)

- [CERMINE](https://github.com/CeON/CERMINE)

- [Science Parse](https://github.com/allenai/science-parse) 

- [science Parse v2](https://github.com/allenai/spv2) 

- [BILBO](https://github.com/OpenEdition/bilbo)

## Transformer/Layout joint approaches (open source)

- [LayoutLM](https://github.com/microsoft/unilm/tree/master/layoutlm)

- [LayoutLMv2](https://github.com/microsoft/unilm/tree/master/layoutlmv2)

- [VILA](https://github.com/allenai/VILA)

## Other

Created in the context of [PdfPig](https://github.com/UglyToad/PdfPig), the following page is a great collection of resources on Document Layout Analysis: [https://github.com/BobLd/DocumentLayoutAnalysis](https://github.com/BobLd/DocumentLayoutAnalysis/)
