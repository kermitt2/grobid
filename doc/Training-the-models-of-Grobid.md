<h1>Training and evaluating GROBID models</h1>

## Models

Grobid uses different CRF models depending on the labeling task to be realized. For a complex extraction and parsing tasks (for instance header extraction and parsing), several models are used in cascade. The current models are the following:

* affiliation-address

* date

* citation

* header

* name-citation

* name-header

* patent

* segmentation

* reference-segmentation

The following models are experimental and it is not advised to use them at this time:

* fulltext

* figure

* table

* chemical entities

* ebook

The models are located under `grobid-home/models`. Each of these models can be retrained using amended or additional training data. For production, a model is trained with all the available training data to maximize the performance. For development purposes, it is also possible to evaluate a model with part of the training data. 

## Train and evaluate

The sub-project grobid-trainer is be used for training. The training data is located under the grobid-trainer/resources folder, more precisely under `grobid-trainer/resources/dataset/*MODEL*/corpus/` 
where *MODEL* is the name of the model (so for instance, `grobid-trainer/resources/dataset/date/corpus/`). 

When generating a new model, a segmentation of data can be done (e.g. 80%-20%) between TEI files for training and for evaluating. This segmentation can be done following two manner: 

- manually: annotated data are moved into two folders, data for training have to be present under `grobid-trainer/resources/dataset/*MODEL*/corpus/`, and data for evaluation under `grobid-trainer/resources/dataset/*MODEL*/evaluation/`. 

- automatically: The data present under `grobid-trainer/resources/dataset/header/corpus` are randomly split following a given ratio (e.g. 0.8 for 80%). The first part is used for training and the second for evaluation.

There are different ways to generate the new model and run the evaluation, whether running the training and the evaluation of the new model separately or not, and whether to split automatically the training data or not. For any methods, the newly generated models are saved directly under grobid-home/models and replace the previous one. A rollback can be made by replacing the newly generated model by the backup record (`<model name>.crf.old`).

### Train and evaluation in one command
Run the following maven command to execute both training and evaluation: 
```bash
> mvn generate-resources -P`<maven goal. I.E: train_name-header>` -e
```
The goal names, are set in the file grobid-trainer/pom.xml (`train_header`, `train_date`, `train_name_header`, `train_name_citation`, `train_citation`, `train_affiliation_address`, `train_fulltext`, `train_patent_citation`).

The files used for the training are located under `grobid-trainer/resources/dataset/*MODEL*/corpus`, and the evaluation files under `grobid-trainer/resources/dataset/*MODEL*/evaluation`. 

Examples for training the header model: 
```bash
> mvn generate-resources -Ptrain_header -e
```
Examples for training the model for names in header: 
```bash
> mvn generate-resources -Ptrain_name_header -e
```

### Train and evaluation separately
Go in grobid-trainer/target and run the class TrainerRunner:

Train (genarate the new model):
```bash
> java -Xmx1024m -jar grobid-trainer-`<current version>`.one-jar.jar 0 `<name of the model>` -gH /path/to/grobid-home
```
The training files considered are located under `grobid-trainer/resources/dataset/*MODEL*/corpus`

Evaluate:
```bash
> java -Xmx1024m -jar grobid-trainer-`<current version>`.one-jar.jar 1 `<name of the model>` -gH /path/to/grobid-home
```

The considered evaluation files are located under `grobid-trainer/resources/dataset/*MODEL*/evaluation`

Automatically split data, train and evaluate:
```bash
> java -Xmx1024m -jar grobid-trainer-`<current version>`.one-jar.jar 2 `<name of the model>` -gH /path/to/grobid-home -s `<segmentation ratio as a number between 0 and 1, e.g. 0.8 for 80%>`
```

For instance, training the date model with a ratio of 75% for training and 25% for evaluation:
```bash
> java -Xmx1024m -jar grobid-trainer-`<current version>`.one-jar.jar 2 date -gH /path/to/grobid-home -s 0.75
```

A ratio of 1.0 means that all the data available under `grobid-trainer/resources/dataset/*MODEL*/corpus/` will be used for training the model, and the evaluation will be empty. *Automatic split data, train and evaluate* is for the moment only available for the following models: header, citation, date, name-citation, name-header and affiliation-address.

Several runs with different files to evaluate can be made to have a more reliable evaluation (e.g. 10 fold cross-validation). For the time being, such segmentation and iterative evaluation is not implemented. 


## Generation of training data
	
To generate some training datas from some input pdf, the batch grobid-core-`<current version>`.one-jar.jar can be used: [Grobid batch](Grobid-batch-quick-start) (createTrainingHeader, createTrainingFulltext, createTrainingPatentcitations, createTrainingSegmentation, createTrainingReferenceSegmentation).

In the case of `createTrainingHeader`, for each pdf in input directory GROBID generates 1 header file (`*.training.header`)  and a collection (one per model used) of TEI files (`*.training.[model_name].tei.xml`). Each model has separate training data, and thus uses separate files. So we have one file for header (`*.training.header.tei.xml`), one for dates (`*.training.date.tei.xml`), one for names, etc...

If you wish to maintain the training corpus as gold standard, these automatically generated data have to be checked and corrected manually before being moved to the training/evaluation folder of the corresponding model. For correcting/checking these data, the guidelines presented in the next section must be followed to ensure the consistency of the whole training sets. 


## Training guidelines

A guideline is available describing the different models and how the corresponding training data should be annotated: [Guideline for training data](https://github.com/kermitt2/grobid/blob/master/grobid-trainer/doc/GuidelinesTrainingData.pdf). 

The encoding of affiliations is further described in this additional guideline: [Guideline for affiliations](https://github.com/kermitt2/grobid/blob/master/grobid-trainer/doc/affiliation-guidelines.pdf).
