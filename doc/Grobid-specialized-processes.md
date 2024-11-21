# GROBID specialised processing (aka flavors)

**This feature is under development and is not yet finalized, in particular the names of the models will change**

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

At the moment there are the following models available: 


| Identifier            | Description                                                                                                                                       | Advantages and Limitations                                                                                                                                                                                                                                                                                           |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `article/light`       | Simple process that extracts only title, authors, publication date and doi from the header, and put everything else in the body                   | Simple model that can work with any document and bring the advantage of pdfalto processing which solves many issue with text ordering and column recognition. Limitation are that all noise not being part of the article, such as references, page numbers, headnotes, and footnotes are also included in the body. |
| `article/light-ref`   | Simple process that extracts only title, authors, publication date and doi from the header, the references, and put everything else in the body   | Variation of the `article/light` that includes the recognision of references. More versatile than `article/light` in the realm of variation of scientific articles, such as corrections, erratums, letters which may contain references.                                                                             |

## Benchmarking

The evaluation of the flavors is performed in the same way as the standard processing for scientific articles. 
However, the evaluation is performed on a reduced set of fields: 

| Flavor              | Header fields                        | Fulltext fields | Citation fields                 | 
|---------------------|--------------------------------------|-----------------|---------------------------------|
| `article/light`     | `title`, `first author`, `authors`   | N/A             | N/A                             |
| `article/light-ref` | `title`, `first author`, `authors`   | N/A             | Same as the standard processing |

