# Benchmarking PubMed Central

## General

This is the end-to-end benchmarking result for GROBID version **0.8.2** against the `PMC_sample_1943` dataset, see
the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation.

The following end-to-end results are using:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models.

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton)
service (the results with CrossRef REST API as consolidation service should be similar but much slower).

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are
available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models
might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources.

Evaluation on 1943 random PDF PMC files out of 1943 PDF from 1943 different journals (0 PDF parsing failure).

Runtime for processing 1943 PDF: **1467** seconds, (0.75s per PDF) on Ubuntu 22.04, 16 CPU (32 threads), 128GB RAM and
with a GeForce GTX 1080 Ti GPU.

Note: with CRF only models, runtime is 470s (0.24 seconds per PDF) with 4 CPU, 8 threads.

## Header metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 16.64     | 16.33     | 16.48     | 1911    |
| authors                     | 92.83     | 92.68     | 92.76     | 1941    |
| first_author                | 96.75     | 96.6      | 96.67     | 1941    |
| keywords                    | 65.3      | 63.7      | 64.49     | 1380    |
| title                       | 84.46     | 84.2      | 84.33     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **71.96** | **71.31** | **71.64** | 9116    |
| all fields (macro avg.)     | 71.2      | 70.7      | 70.95     | 9116    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| abstract                    | 63.79     | 62.59    | 63.18     | 1911    |
| authors                     | 94.79     | 94.64    | 94.72     | 1941    |
| first_author                | 97.16     | 97.01    | 97.09     | 1941    |
| keywords                    | 74        | 72.17    | 73.07     | 1380    |
| title                       | 92        | 91.71    | 91.86     | 1943    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **85.17** | **84.4** | **84.78** | 9116    |
| all fields (macro avg.)     | 84.35     | 83.63    | 83.98     | 9116    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 90.67     | 88.96     | 89.8      | 1911    |
| authors                     | 96.7      | 96.55     | 96.62     | 1941    |
| first_author                | 97.42     | 97.27     | 97.34     | 1941    |
| keywords                    | 84.25     | 82.17     | 83.2      | 1380    |
| title                       | 98.24     | 97.94     | 98.09     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.08** | **93.23** | **93.65** | 9116    |
| all fields (macro avg.)     | 93.46     | 92.58     | 93.01     | 9116    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 86.88     | 85.24     | 86.05     | 1911    |
| authors                     | 95.72     | 95.57     | 95.64     | 1941    |
| first_author                | 96.75     | 96.6      | 96.67     | 1941    |
| keywords                    | 79.64     | 77.68     | 78.65     | 1380    |
| title                       | 96.18     | 95.88     | 96.03     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.81** | **90.98** | **91.39** | 9116    |
| all fields (macro avg.)     | 91.03     | 90.2      | 90.61     | 9116    |

#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	215 (strict) 
Total correct instances: 	906 (soft) 
Total correct instances: 	1441 (Levenshtein) 
Total correct instances: 	1294 (ObservedRatcliffObershelp) 

Instance-level recall:	11.07	(strict) 
Instance-level recall:	46.63	(soft) 
Instance-level recall:	74.16	(Levenshtein) 
Instance-level recall:	66.6	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.05     | 76.29     | 79.53     | 85778   |
| date                        | 94.62     | 84.21     | 89.11     | 87067   |
| first_author                | 89.79     | 82.46     | 85.97     | 85778   |
| inTitle                     | 73.22     | 71.83     | 72.52     | 81007   |
| issue                       | 91.07     | 87.58     | 89.29     | 16635   |
| page                        | 94.55     | 83.66     | 88.77     | 80501   |
| title                       | 79.68     | 75.28     | 77.42     | 80736   |
| volume                      | 96.04     | 89.79     | 92.81     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.23** | **80.71** | **83.84** | 597569  |
| all fields (macro avg.)     | 87.75     | 81.39     | 84.43     | 597569  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.52     | 76.72     | 79.98     | 85778   |
| date                        | 94.62     | 84.21     | 89.11     | 87067   |
| first_author                | 89.96     | 82.62     | 86.13     | 85778   |
| inTitle                     | 84.93     | 83.32     | 84.11     | 81007   |
| issue                       | 91.07     | 87.58     | 89.29     | 16635   |
| page                        | 94.55     | 83.66     | 88.77     | 80501   |
| title                       | 91.45     | 86.4      | 88.86     | 80736   |
| volume                      | 96.04     | 89.79     | 92.81     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.62** | **83.85** | **87.11** | 597569  |
| all fields (macro avg.)     | 90.77     | 84.29     | 87.38     | 597569  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall   | f1       | support |
|-----------------------------|-----------|----------|----------|---------|
| authors                     | 89.23     | 81.97    | 85.45    | 85778   |
| date                        | 94.62     | 84.21    | 89.11    | 87067   |
| first_author                | 90.17     | 82.82    | 86.34    | 85778   |
| inTitle                     | 86.18     | 84.55    | 85.36    | 81007   |
| issue                       | 91.07     | 87.58    | 89.29    | 16635   |
| page                        | 94.55     | 83.66    | 88.77    | 80501   |
| title                       | 93.81     | 88.63    | 91.14    | 80736   |
| volume                      | 96.04     | 89.79    | 92.81    | 80067   |
|                             |           |          |          |         |
| **all fields (micro avg.)** | **91.97** | **85.1** | **88.4** | 597569  |
| all fields (macro avg.)     | 91.96     | 85.4     | 88.53    | 597569  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 85.99     | 79        | 82.34     | 85778   |
| date                        | 94.62     | 84.21     | 89.11     | 87067   |
| first_author                | 89.8      | 82.48     | 85.99     | 85778   |
| inTitle                     | 83.5      | 81.92     | 82.7      | 81007   |
| issue                       | 91.07     | 87.58     | 89.29     | 16635   |
| page                        | 94.55     | 83.66     | 88.77     | 80501   |
| title                       | 93.41     | 88.25     | 90.76     | 80736   |
| volume                      | 96.04     | 89.79     | 92.81     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.02** | **84.22** | **87.49** | 597569  |
| all fields (macro avg.)     | 91.12     | 84.61     | 87.72     | 597569  |

#### Instance-level results

```
Total expected instances: 		90125
Total extracted instances: 		85852
Total correct instances: 		38735 (strict) 
Total correct instances: 		50874 (soft) 
Total correct instances: 		55750 (Levenshtein) 
Total correct instances: 		52293 (RatcliffObershelp) 

Instance-level precision:	45.12 (strict) 
Instance-level precision:	59.26 (soft) 
Instance-level precision:	64.94 (Levenshtein) 
Instance-level precision:	60.91 (RatcliffObershelp) 

Instance-level recall:	42.98	(strict) 
Instance-level recall:	56.45	(soft) 
Instance-level recall:	61.86	(Levenshtein) 
Instance-level recall:	58.02	(RatcliffObershelp) 

Instance-level f-score:	44.02 (strict) 
Instance-level f-score:	57.82 (soft) 
Instance-level f-score:	63.36 (Levenshtein) 
Instance-level f-score:	59.43 (RatcliffObershelp) 

Matching 1 :	68306

Matching 2 :	4146

Matching 3 :	1864

Matching 4 :	664

Total matches :	74980
```

#### Citation context resolution

```

Total expected references: 	 90125 - 46.38 references per article
Total predicted references: 	 85852 - 44.19 references per article

Total expected citation contexts: 	 139835 - 71.97 citation contexts per article
Total predicted citation contexts: 	 115396 - 59.39 citation contexts per article

Total correct predicted citation contexts: 	 97251 - 50.05 citation contexts per article
Total wrong predicted citation contexts: 	 18145 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 84.28
Recall citation contexts: 	 69.55
fscore citation contexts: 	 76.21
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| figure_title                | 31.6      | 24.56     | 27.64     | 7281    |
| reference_citation          | 57.39     | 58.69     | 58.04     | 134196  |
| reference_figure            | 60.94     | 65.99     | 63.36     | 19330   |
| reference_table             | 82.94     | 88.89     | 85.81     | 7327    |
| section_title               | 76.31     | 67.68     | 71.74     | 27619   |
| table_title                 | 66.77     | 45.08     | 53.82     | 3971    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **60.5**  | **60.23** | **60.37** | 199724  |
| all fields (macro avg.)     | 62.66     | 58.48     | 60.07     | 199724  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| figure_title                | 78.88     | 61.3      | 68.99     | 7281    |
| reference_citation          | 61.64     | 63.04     | 62.33     | 134196  |
| reference_figure            | 61.41     | 66.5      | 63.85     | 19330   |
| reference_table             | 83.11     | 89.08     | 85.99     | 7327    |
| section_title               | 81.18     | 72        | 76.32     | 27619   |
| table_title                 | 94.11     | 63.54     | 75.86     | 3971    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **65.8**  | **65.52** | **65.66** | 199724  |
| all fields (macro avg.)     | 76.72     | 69.24     | 72.22     | 199724  |

**Document-level ratio results**

| label                       | precision | recall | f1    | support |
|-----------------------------|-----------|--------|-------|---------|
|                             |           |        |       |         |
| **all fields (micro avg.)** | **0**     | **0**  | **0** | 0       |
| all fields (macro avg.)     | 0         | 0      | 0     | 0       |

Evaluation metrics produced in 673.711 seconds




