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
| authors                     | 75.26     | 74.26     | 74.76     | 983     |
| first_author                | 92.47     | 91.34     | 91.91     | 982     |
| title                       | 87.07     | 84.86     | 85.95     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **66.3**  | **64.94** | **65.61** | 3933    |
| all fields (macro avg.)     | 66.11     | 64.95     | 65.53     | 3933    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| abstract                    | 22.56     | 21.85    | 22.2      | 984     |
| authors                     | 75.57     | 74.57    | 75.06     | 983     |
| first_author                | 92.47     | 91.34    | 91.91     | 982     |
| title                       | 95.1      | 92.68    | 93.88     | 984     |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **71.57** | **70.1** | **70.83** | 3933    |
| all fields (macro avg.)     | 71.43     | 70.11    | 70.76     | 3933    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 47.74     | 46.24     | 46.98     | 984     |
| authors                     | 89.18     | 88        | 88.58     | 983     |
| first_author                | 92.78     | 91.65     | 92.21     | 982     |
| title                       | 96.56     | 94.11     | 95.32     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **81.67** | **79.99** | **80.82** | 3933    |
| all fields (macro avg.)     | 81.57     | 80        | 80.77     | 3933    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 44.7      | 43.29     | 43.99     | 984     |
| authors                     | 80.93     | 79.86     | 80.39     | 983     |
| first_author                | 92.47     | 91.34     | 91.91     | 982     |
| title                       | 96.56     | 94.11     | 95.32     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **78.76** | **77.14** | **77.94** | 3933    |
| all fields (macro avg.)     | 78.67     | 77.15     | 77.9      | 3933    |

#### Instance-level results

```
Total expected instances: 	984
Total correct instances: 	74 (strict) 
Total correct instances: 	198 (soft) 
Total correct instances: 	387 (Levenshtein) 
Total correct instances: 	343 (ObservedRatcliffObershelp) 

Instance-level recall:	7.52	(strict) 
Instance-level recall:	20.12	(soft) 
Instance-level recall:	39.33	(Levenshtein) 
Instance-level recall:	34.86	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.46     | 78.35     | 78.9      | 63265   |
| date                        | 95.92     | 94.19     | 95.04     | 63662   |
| first_author                | 94.84     | 93.49     | 94.16     | 63265   |
| inTitle                     | 95.84     | 94.88     | 95.36     | 63213   |
| issue                       | 1.99      | 75        | 3.88      | 16      |
| page                        | 96.28     | 95.42     | 95.85     | 53375   |
| title                       | 90.28     | 90.87     | 90.58     | 62044   |
| volume                      | 97.9      | 98.38     | 98.14     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.72** | **92.13** | **92.42** | 429889  |
| all fields (macro avg.)     | 81.56     | 90.07     | 81.49     | 429889  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.59     | 78.49     | 79.04     | 63265   |
| date                        | 95.92     | 94.19     | 95.04     | 63662   |
| first_author                | 94.92     | 93.57     | 94.24     | 63265   |
| inTitle                     | 96.32     | 95.35     | 95.83     | 63213   |
| issue                       | 1.99      | 75        | 3.88      | 16      |
| page                        | 96.28     | 95.42     | 95.85     | 53375   |
| title                       | 95.95     | 96.58     | 96.27     | 62044   |
| volume                      | 97.9      | 98.38     | 98.14     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.65** | **93.05** | **93.35** | 429889  |
| all fields (macro avg.)     | 82.36     | 90.87     | 82.29     | 429889  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 93.34     | 92.05     | 92.69     | 63265   |
| date                        | 95.92     | 94.19     | 95.04     | 63662   |
| first_author                | 95.37     | 94.01     | 94.68     | 63265   |
| inTitle                     | 96.66     | 95.68     | 96.17     | 63213   |
| issue                       | 1.99      | 75        | 3.88      | 16      |
| page                        | 96.28     | 95.42     | 95.85     | 53375   |
| title                       | 97.7      | 98.34     | 98.02     | 62044   |
| volume                      | 97.9      | 98.38     | 98.14     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.03** | **95.41** | **95.72** | 429889  |
| all fields (macro avg.)     | 84.39     | 92.88     | 84.31     | 429889  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.75     | 85.55     | 86.14     | 63265   |
| date                        | 95.92     | 94.19     | 95.04     | 63662   |
| first_author                | 94.86     | 93.5      | 94.18     | 63265   |
| inTitle                     | 96.33     | 95.36     | 95.84     | 63213   |
| issue                       | 1.99      | 75        | 3.88      | 16      |
| page                        | 96.28     | 95.42     | 95.85     | 53375   |
| title                       | 97.55     | 98.18     | 97.86     | 62044   |
| volume                      | 97.9      | 98.38     | 98.14     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.92** | **94.31** | **94.61** | 429889  |
| all fields (macro avg.)     | 83.45     | 91.95     | 83.37     | 429889  |

#### Instance-level results

```
Total expected instances: 		63664
Total extracted instances: 		66563
Total correct instances: 		42395 (strict) 
Total correct instances: 		45241 (soft) 
Total correct instances: 		52904 (Levenshtein) 
Total correct instances: 		49493 (RatcliffObershelp) 

Instance-level precision:	63.69 (strict) 
Instance-level precision:	67.97 (soft) 
Instance-level precision:	79.48 (Levenshtein) 
Instance-level precision:	74.36 (RatcliffObershelp) 

Instance-level recall:	66.59	(strict) 
Instance-level recall:	71.06	(soft) 
Instance-level recall:	83.1	(Levenshtein) 
Instance-level recall:	77.74	(RatcliffObershelp) 

Instance-level f-score:	65.11 (strict) 
Instance-level f-score:	69.48 (soft) 
Instance-level f-score:	81.25 (Levenshtein) 
Instance-level f-score:	76.01 (RatcliffObershelp) 

Matching 1 :	58716

Matching 2 :	1020

Matching 3 :	1249

Matching 4 :	365

Total matches :	61350
```

#### Citation context resolution

```

Total expected references: 	 63664 - 64.7 references per article
Total predicted references: 	 66563 - 67.65 references per article

Total expected citation contexts: 	 109022 - 110.79 citation contexts per article
Total predicted citation contexts: 	 99896 - 101.52 citation contexts per article

Total correct predicted citation contexts: 	 96061 - 97.62 citation contexts per article
Total wrong predicted citation contexts: 	 3835 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 96.16
Recall citation contexts: 	 88.11
fscore citation contexts: 	 91.96
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
| availability_stmt           | 29.95     | 28.21     | 29.05     | 585     |
| figure_title                | 0.01      | 0         | 0         | 31718   |
| funding_stmt                | 5.34      | 24.1      | 8.74      | 921     |
| reference_citation          | 55.36     | 55.72     | 55.54     | 108949  |
| reference_figure            | 56.47     | 49.9      | 52.98     | 68926   |
| reference_table             | 67.56     | 74.09     | 70.67     | 2381    |
| section_title               | 85        | 74.6      | 79.46     | 21831   |
| table_title                 | 0.19      | 0.05      | 0.08      | 1925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **54.75** | **47.86** | **51.07** | 237236  |
| all fields (macro avg.)     | 37.48     | 38.33     | 37.07     | 237236  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 39.2      | 36.92     | 38.03     | 585     |
| figure_title                | 49.01     | 15.15     | 23.15     | 31718   |
| funding_stmt                | 5.34      | 24.1      | 8.74      | 921     |
| reference_citation          | 91.01     | 91.6      | 91.3      | 108949  |
| reference_figure            | 56.73     | 50.13     | 53.23     | 68926   |
| reference_table             | 67.6      | 74.13     | 70.71     | 2381    |
| section_title               | 85.88     | 75.37     | 80.28     | 21831   |
| table_title                 | 96.77     | 26.44     | 41.53     | 1925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **76.34** | **66.74** | **71.22** | 237236  |
| all fields (macro avg.)     | 61.44     | 49.23     | 50.87     | 237236  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 96.16     | 94.19     | 95.16     | 585     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.16** | **94.19** | **95.16** | 585     |
| all fields (macro avg.)     | 96.16     | 94.19     | 95.16     | 585     |

Evaluation metrics produced in 744.95 seconds



