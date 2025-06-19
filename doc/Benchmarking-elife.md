# Benchmarking eLife

## General

This is the end-to-end benchmarking result for GROBID version **0.8.2** against the `eLife` test set, see
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

Evaluation on 984 PDF preprints out of 984 (no failure).

Runtime for processing 984 PDF: **1131** seconds (1.15 seconds per PDF file) on Ubuntu 22.04, 16 CPU (32 threads), 128GB
RAM and with a GeForce GTX 1080 Ti GPU.

Note: with CRF only models runtime is 492s (0.50 seconds per PDF) with 4 CPU, 8 threads.

## Header metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 9.65      | 9.35      | 9.5       | 984     |
| authors                     | 73.97     | 73.14     | 73.55     | 983     |
| first_author                | 92.49     | 91.55     | 92.02     | 982     |
| title                       | 86.42     | 84.76     | 85.58     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **65.87** | **64.68** | **65.27** | 3933    |
| all fields (macro avg.)     | 65.63     | 64.7      | 65.16     | 3933    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 22.46     | 21.75     | 22.1      | 984     |
| authors                     | 74.28     | 73.45     | 73.86     | 983     |
| first_author                | 92.49     | 91.55     | 92.02     | 982     |
| title                       | 94.4      | 92.58     | 93.48     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **71.1**  | **69.82** | **70.46** | 3933    |
| all fields (macro avg.)     | 70.91     | 69.83     | 70.36     | 3933    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 47.64     | 46.14     | 46.88     | 984     |
| authors                     | 88.37     | 87.39     | 87.88     | 983     |
| first_author                | 92.8      | 91.85     | 92.32     | 982     |
| title                       | 95.85     | 94        | 94.92     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **81.31** | **79.84** | **80.56** | 3933    |
| all fields (macro avg.)     | 81.17     | 79.85     | 80.5      | 3933    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 44.7      | 43.29     | 43.99     | 984     |
| authors                     | 80.04     | 79.15     | 79.59     | 983     |
| first_author                | 92.49     | 91.55     | 92.02     | 982     |
| title                       | 95.75     | 93.9      | 94.82     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **78.38** | **76.96** | **77.67** | 3933    |
| all fields (macro avg.)     | 78.25     | 76.97     | 77.6      | 3933    |

#### Instance-level results

```
Total expected instances: 	984
Total correct instances: 	74 (strict) 
Total correct instances: 	197 (soft) 
Total correct instances: 	385 (Levenshtein) 
Total correct instances: 	340 (ObservedRatcliffObershelp) 

Instance-level recall:	7.52	(strict) 
Instance-level recall:	20.02	(soft) 
Instance-level recall:	39.13	(Levenshtein) 
Instance-level recall:	34.55	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall | f1        | support |
|-----------------------------|-----------|--------|-----------|---------|
| authors                     | 79.43     | 78.23  | 78.83     | 63265   |
| date                        | 95.89     | 94.05  | 94.96     | 63662   |
| first_author                | 94.83     | 93.37  | 94.09     | 63265   |
| inTitle                     | 95.83     | 94.74  | 95.28     | 63213   |
| issue                       | 2.01      | 75     | 3.91      | 16      |
| page                        | 96.26     | 95.29  | 95.77     | 53375   |
| title                       | 90.31     | 90.77  | 90.54     | 62044   |
| volume                      | 97.89     | 98.25  | 98.07     | 61049   |
|                             |           |        |           |         |
| **all fields (micro avg.)** | **92.71** | **92** | **92.35** | 429889  |
| all fields (macro avg.)     | 81.56     | 89.96  | 81.43     | 429889  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.56     | 78.37     | 78.96     | 63265   |
| date                        | 95.89     | 94.05     | 94.96     | 63662   |
| first_author                | 94.91     | 93.45     | 94.17     | 63265   |
| inTitle                     | 96.31     | 95.21     | 95.76     | 63213   |
| issue                       | 2.01      | 75        | 3.91      | 16      |
| page                        | 96.26     | 95.29     | 95.77     | 53375   |
| title                       | 95.97     | 96.46     | 96.22     | 62044   |
| volume                      | 97.89     | 98.25     | 98.07     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.64** | **92.92** | **93.28** | 429889  |
| all fields (macro avg.)     | 82.35     | 90.76     | 82.23     | 429889  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 93.31     | 91.9      | 92.6      | 63265   |
| date                        | 95.89     | 94.05     | 94.96     | 63662   |
| first_author                | 95.36     | 93.89     | 94.62     | 63265   |
| inTitle                     | 96.63     | 95.53     | 96.08     | 63213   |
| issue                       | 2.01      | 75        | 3.91      | 16      |
| page                        | 96.26     | 95.29     | 95.77     | 53375   |
| title                       | 97.7      | 98.19     | 97.94     | 62044   |
| volume                      | 97.89     | 98.25     | 98.07     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.01** | **95.27** | **95.64** | 429889  |
| all fields (macro avg.)     | 84.38     | 92.76     | 84.24     | 429889  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.73     | 85.43     | 86.08     | 63265   |
| date                        | 95.89     | 94.05     | 94.96     | 63662   |
| first_author                | 94.85     | 93.38     | 94.11     | 63265   |
| inTitle                     | 96.31     | 95.22     | 95.76     | 63213   |
| issue                       | 2.01      | 75        | 3.91      | 16      |
| page                        | 96.26     | 95.29     | 95.77     | 53375   |
| title                       | 97.55     | 98.04     | 97.8      | 62044   |
| volume                      | 97.89     | 98.25     | 98.07     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.91** | **94.18** | **94.54** | 429889  |
| all fields (macro avg.)     | 83.44     | 91.83     | 83.31     | 429889  |

#### Instance-level results

```
Total expected instances: 		63664
Total extracted instances: 		66225
Total correct instances: 		42341 (strict) 
Total correct instances: 		45181 (soft) 
Total correct instances: 		52829 (Levenshtein) 
Total correct instances: 		49432 (RatcliffObershelp) 

Instance-level precision:	63.94 (strict) 
Instance-level precision:	68.22 (soft) 
Instance-level precision:	79.77 (Levenshtein) 
Instance-level precision:	74.64 (RatcliffObershelp) 

Instance-level recall:	66.51	(strict) 
Instance-level recall:	70.97	(soft) 
Instance-level recall:	82.98	(Levenshtein) 
Instance-level recall:	77.65	(RatcliffObershelp) 

Instance-level f-score:	65.2 (strict) 
Instance-level f-score:	69.57 (soft) 
Instance-level f-score:	81.34 (Levenshtein) 
Instance-level f-score:	76.11 (RatcliffObershelp) 

Matching 1 :	58638

Matching 2 :	1009

Matching 3 :	1243

Matching 4 :	369

Total matches :	61259
```

#### Citation context resolution

```

Total expected references: 	 63664 - 64.7 references per article
Total predicted references: 	 66225 - 67.3 references per article

Total expected citation contexts: 	 109022 - 110.79 citation contexts per article
Total predicted citation contexts: 	 99442 - 101.06 citation contexts per article

Total correct predicted citation contexts: 	 95753 - 97.31 citation contexts per article
Total wrong predicted citation contexts: 	 3689 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 96.29
Recall citation contexts: 	 87.83
fscore citation contexts: 	 91.87
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 30.24     | 28.38     | 29.28     | 585     |
| figure_title                | 0.02      | 0.01      | 0.01      | 31718   |
| funding_stmt                | 5.38      | 24.97     | 8.85      | 921     |
| reference_citation          | 56.85     | 55.78     | 56.31     | 108949  |
| reference_figure            | 57.96     | 51.82     | 54.72     | 68926   |
| reference_table             | 72.58     | 73.71     | 73.14     | 2381    |
| section_title               | 82.71     | 75.92     | 79.17     | 21831   |
| table_title                 | 0         | 0         | 0         | 1925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **55.87** | **48.57** | **51.96** | 237236  |
| all fields (macro avg.)     | 38.22     | 38.82     | 37.69     | 237236  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| availability_stmt           | 41.17     | 38.63    | 39.86     | 585     |
| figure_title                | 48.14     | 14.98    | 22.85     | 31718   |
| funding_stmt                | 5.38      | 24.97    | 8.85      | 921     |
| reference_citation          | 93.28     | 91.52    | 92.39     | 108949  |
| reference_figure            | 58.26     | 52.09    | 55        | 68926   |
| reference_table             | 72.75     | 73.88    | 73.31     | 2381    |
| section_title               | 83.68     | 76.82    | 80.1      | 21831   |
| table_title                 | 97.56     | 29.09    | 44.82     | 1925    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **77.54** | **67.4** | **72.12** | 237236  |
| all fields (macro avg.)     | 62.53     | 50.25    | 52.15     | 237236  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 96.83     | 93.85     | 95.31     | 585     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.83** | **93.85** | **95.31** | 585     |
| all fields (macro avg.)     | 96.83     | 93.85     | 95.31     | 585     |

Evaluation metrics produced in 1352.949 seconds


