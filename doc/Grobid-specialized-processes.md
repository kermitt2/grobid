# GROBID specialised processing (aka flavors)

## Introduction

This is a simple management of alternative models to use when processing a document. 
A model variant (or flavor) is for example an alternative header model trained with its own training data and labels (to cover documents with specific header section different from scholar articles), or an alternative segmentation model for segmenting something else than scholar papers.

To process a document with alternative model(s), we use a string called "flavor" to identify it. 
If the flavor is indicated, the selected model will use the "flavor" model if it exists, and the normal model if the flavor does exist for this model (so defaulting back then to the standard models).

Flavor model training data are always located as subdirectories of the standard training data path, e.g. for the flavor "sdo/ietf", the training data of the header model for this flavor will be under `grobid-trainer/resources/dataset/header/article/light-ref`. 
The training data of the segmentation model for this flavor will be under `grobid-trainer/resources/dataset/segmentation/article/light`, and so on.

For running grobid following a particular flavor, we add the flavor name as additional parameter of the service:

```shell
curl -v --form input=@./nihms834197.pdf --form "flavor=article/light-ref" localhost:8070/api/processFulltextDocument
```

Following, an updated view of the cascade architecture:

![cascade-with-flavors.png](img/cascade-with-flavors.png)

## Flavors

At the moment, the flavored processes are available as follows:

| Name                                          | Identifier          | Flavored models          | Description                                                                                                                                     | Advantages and Limitations                                                                                                                                                                                                                                                                                            |
|-----------------------------------------------|---------------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Article lightweight structure                 | `article/light`     | `segmentation`, `header` | Simple process that extracts only title, authors, publication date and doi from the header, and put everything else in the body                 | Simple model that can work with any document and bring the advantage of pdfalto processing which solves many issue with text ordering and column recognition. Limitation are that all noise not being part of the article, such as references, page numbers, head notes, and footnotes are also included in the body. |
| Article lightweight structure with references | `article/light-ref` | `segmentation`, `header` | Simple process that extracts only title, authors, publication date and doi from the header, the references, and put everything else in the body | Variation of the `article/light` that includes the recognition of references. More versatile than `article/light` in the realm of variation of scientific articles, such as corrections, erratums, letters which may contain references.                                                                              |

## Benchmarking

The evaluation of the flavors is performed in the same way as the standard processing for scientific articles:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models.

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower).

The evaluation, which is usually create grobid files suffixing `fulltext.tei.xml`, will suffix also the flavor, for example `article/light` will be suffixed as `article_light.tei.xml`.
In this way is possible to run evaluation for multiple flavor without loosing the Grobid processed files.

The evaluation is performed on a reduced set of fields:

| Flavor              | Header fields                      | Fulltext fields | Citation fields                  | 
|---------------------|------------------------------------|-----------------|----------------------------------|
| `article/light`     | `title`, `first author`, `authors` | N/A             | N/A                              |
| `article/light-ref` | `title`, `first author`, `authors` | N/A             | Same as the standard processing* |

(*) for this flavor the citation model is included to avoid regressions, as the citation parsing is performed using the standard citation model

The benchmarks results are listed here with links to the full reports.

### Article lightweight structure

| Corpus          | Header avg. f1* | Full report                                                                     | 
|-----------------|-----------------|---------------------------------------------------------------------------------|
| Bioxiv          | 89.4            | [benchmaking-bioxiv.md](benchmarks/flavors/article_light/benchmaking-bioxiv.md) |
| PMC_sample_1943 | 95.71           | [benchmaking-pmc.md](benchmarks/flavors/article_light/benchmaking-pmc.md)       |
| PLOS_1000       | 99.37           | [benchmaking-plos.md](benchmarks/flavors/article_light/benchmaking-plos.md)     |
| eLife_984       | 88.73           | [benchmaking-elife.md](benchmarks/flavors/article_light/benchmaking-elife.md)   |

### Article lightweight structure with references

| Corpus          | Header avg. f1* | Citations avg. f1+ | Full report                                                                         | 
|-----------------|-----------------|--------------------|-------------------------------------------------------------------------------------|
| Bioxiv          | 89.79           | 56.31              | [benchmaking-bioxiv.md](benchmarks/flavors/article_light_ref/benchmaking-bioxiv.md) |
| PMC_sample_1943 | 95.74           | 58.78              | [benchmaking-pmc.md](benchmarks/flavors/article_light_ref/benchmaking-pmc.md)       |
| PLOS_1000       | 99.52           | 48.04              | [benchmaking-plos.md](benchmarks/flavors/article_light_ref/benchmaking-plos.md)     |
| eLife_984       | 91.35           | 76.14              | [benchmaking-elife.md](benchmarks/flavors/article_light_ref/benchmaking-elife.md)   |

(*) avg. micro F1 Ratcliff/Obershelp@0.95

(+) Instance-level f-score (RatcliffObershelp)
