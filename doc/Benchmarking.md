# Benchmarking

## End-to-end evaluation

See the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing the evaluations. For end-to-end results with holdout sets, the key points to consider are the following:

- The datasets are independent from the training data used to train the different models involved in the full document processing, in particular several models do not use data from PMC articles at all. As a stable holdout set, it should thus provide a more reliable evaluation than cross-validation metrics.

- The evaluation covers the whole process, including PDF extraction, PDF noisiness and error cascading. It should thus provide a more realistic evaluation for the end user than the model-specific metrics with "clean" data that are usually reported in the literature. 

- As the evaluation data come from XML PMC and the scientific publishers, it contains some encoding errors (publisher data are far from perfect) and are not always complete (for instance some bibliographical references are provided as raw string and not structured). The results are therefore more an indication of error rates than trusful absolute accuracy performances.

- We think that these metrics are very good to compare improvements over time and to catch possible regressions, because relative improvements can be reliable with slighty imperfect evaluation data. 

More recent versions of these benchmarks (for master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc).


## General

The reported end-to-end results are using CRF Wapiti as sequence labelling engine. Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service are similar but much slower). See [below](https://grobid.readthedocs.io/en/latest/Benchmarking/#deep-learning-models) for some evaluations with Deep-Learning architectures. 
