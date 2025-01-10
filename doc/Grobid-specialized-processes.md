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
curl -v --form input=@./XP123456.pdf --form "flavor=sdo/ietf" localhost:8070/api/processFulltextDocument
```

Following, an updated view of the cascade architecture:

![cascade-with-flavors.png](img/cascade-with-flavors.png)

## Flavors

At the moment, the flavored processes are available as follows:

| Name                                                      | Identifier | Flavored models          | Description                                   | Advantages                                                   | Limitations |
|-----------------------------------------------------------|------------|--------------------------|-----------------------------------------------|--------------------------------------------------------------|-------------|
| Internet Engineering Task Force (IETF) Standard Documents | `sdo/ietf` | `segmentation`, `header` | Processing of the IETF Standard documentation | Supports the procesisng of a different flavor of documents   |             | 
| 3GPP Working Procedures Standard Documents                | `sdo/3gpp` | N/A                      |                                               |


## Training the specialised flavor models  

The training data for the flavors are following the same structure as the standard models. 
In other words the annotated training data for, e.g., the lightweight segmentation model with references, for articles, are following the guidelines as the standard grobid segmentation model. 
The Grobid parser select automatically the right subset of labels to include. 
However, this can be implemented at discretion of the user, so for example a flavor `sdo/ietf` for parsing standards documents for IETF, can be following their specific guidelines.

For training the specialised models the same procedure as for the standard models is used, but the flavor is indicated in the training command, e.g. to train the segmentation model for the flavor `article/light`: 

```shell
./gradlew train_segmentation_sdo_ietf
```

or the header model for the flavor `sdo/ietf`: 

```shell
./gradlew train_header_article_sdo_ietf
```
