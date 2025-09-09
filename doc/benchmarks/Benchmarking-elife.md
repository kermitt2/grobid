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
| abstract                    | 9.53      | 9.25      | 9.39      | 984     |
| authors                     | 74.79     | 73.96     | 74.37     | 983     |
| first_author                | 92.59     | 91.65     | 92.12     | 982     |
| title                       | 86.93     | 85.16     | 86.04     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **66.17** | **64.99** | **65.57** | 3933    |
| all fields (macro avg.)     | 65.96     | 65        | 65.48     | 3933    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 22.3      | 21.65     | 21.97     | 984     |
| authors                     | 75.1      | 74.26     | 74.68     | 983     |
| first_author                | 92.59     | 91.65     | 92.12     | 982     |
| title                       | 94.92     | 92.99     | 93.94     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **71.4**  | **70.12** | **70.75** | 3933    |
| all fields (macro avg.)     | 71.23     | 70.14     | 70.68     | 3933    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 47.43     | 46.04     | 46.73     | 984     |
| authors                     | 88.68     | 87.69     | 88.18     | 983     |
| first_author                | 92.9      | 91.96     | 92.43     | 982     |
| title                       | 96.37     | 94.41     | 95.38     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **81.47** | **80.02** | **80.73** | 3933    |
| all fields (macro avg.)     | 81.35     | 80.02     | 80.68     | 3933    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 44.5      | 43.19     | 43.84     | 984     |
| authors                     | 80.35     | 79.45     | 79.9      | 983     |
| first_author                | 92.59     | 91.65     | 92.12     | 982     |
| title                       | 96.37     | 94.41     | 95.38     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **78.57** | **77.17** | **77.86** | 3933    |
| all fields (macro avg.)     | 78.45     | 77.18     | 77.81     | 3933    |

#### Instance-level results

```
Total expected instances:       984
Total correct instances:        74 (strict)
Total correct instances:        196 (soft)
Total correct instances:        381 (Levenshtein)
Total correct instances:        338 (ObservedRatcliffObershelp)

Instance-level recall:  7.52    (strict)
Instance-level recall:  19.92   (soft)
Instance-level recall:  38.72   (Levenshtein)
Instance-level recall:  34.35   (RatcliffObershelp)
```

## Citation metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.43     | 78.36     | 78.89     | 63265   |
| date                        | 95.89     | 94.19     | 95.03     | 63662   |
| first_author                | 94.83     | 93.51     | 94.17     | 63265   |
| inTitle                     | 95.82     | 94.87     | 95.34     | 63213   |
| issue                       | 2         | 75        | 3.9       | 16      |
| page                        | 96.28     | 95.44     | 95.86     | 53375   |
| title                       | 90.27     | 90.87     | 90.57     | 62044   |
| volume                      | 97.88     | 98.39     | 98.14     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.7**  | **92.13** | **92.42** | 429889  |
| all fields (macro avg.)     | 81.55     | 90.08     | 81.49     | 429889  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.57     | 78.49     | 79.03     | 63265   |
| date                        | 95.89     | 94.19     | 95.03     | 63662   |
| first_author                | 94.91     | 93.59     | 94.25     | 63265   |
| inTitle                     | 96.29     | 95.35     | 95.82     | 63213   |
| issue                       | 2         | 75        | 3.9       | 16      |
| page                        | 96.28     | 95.44     | 95.86     | 53375   |
| title                       | 95.94     | 96.58     | 96.26     | 62044   |
| volume                      | 97.88     | 98.39     | 98.14     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.63** | **93.06** | **93.34** | 429889  |
| all fields (macro avg.)     | 82.35     | 90.88     | 82.28     | 429889  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 93.33     | 92.07     | 92.7      | 63265   |
| date                        | 95.89     | 94.19     | 95.03     | 63662   |
| first_author                | 95.36     | 94.03     | 94.69     | 63265   |
| inTitle                     | 96.62     | 95.67     | 96.14     | 63213   |
| issue                       | 2         | 75        | 3.9       | 16      |
| page                        | 96.28     | 95.44     | 95.86     | 53375   |
| title                       | 97.69     | 98.34     | 98.01     | 62044   |
| volume                      | 97.88     | 98.39     | 98.14     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.01** | **95.42** | **95.71** | 429889  |
| all fields (macro avg.)     | 84.38     | 92.89     | 84.31     | 429889  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.75     | 85.57     | 86.16     | 63265   |
| date                        | 95.89     | 94.19     | 95.03     | 63662   |
| first_author                | 94.84     | 93.53     | 94.18     | 63265   |
| inTitle                     | 96.3      | 95.35     | 95.82     | 63213   |
| issue                       | 2         | 75        | 3.9       | 16      |
| page                        | 96.28     | 95.44     | 95.86     | 53375   |
| title                       | 97.54     | 98.18     | 97.86     | 62044   |
| volume                      | 97.88     | 98.39     | 98.14     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.9**  | **94.32** | **94.61** | 429889  |
| all fields (macro avg.)     | 83.43     | 91.96     | 83.37     | 429889  |

#### Instance-level results

```
Total expected instances:               63664
Total extracted instances:              66152
Total correct instances:                42401 (strict)
Total correct instances:                45243 (soft)
Total correct instances:                52907 (Levenshtein)
Total correct instances:                49502 (RatcliffObershelp)

Instance-level precision:       64.1 (strict)
Instance-level precision:       68.39 (soft)
Instance-level precision:       79.98 (Levenshtein)
Instance-level precision:       74.83 (RatcliffObershelp)

Instance-level recall:  66.6    (strict)
Instance-level recall:  71.07   (soft)
Instance-level recall:  83.1    (Levenshtein)
Instance-level recall:  77.76   (RatcliffObershelp)

Instance-level f-score: 65.32 (strict)
Instance-level f-score: 69.7 (soft)
Instance-level f-score: 81.51 (Levenshtein)
Instance-level f-score: 76.26 (RatcliffObershelp)

Matching 1 :    58715

Matching 2 :    1019

Matching 3 :    1252

Matching 4 :    368

Total matches : 61354
```

#### Citation context resolution

```

Total expected references:       63664 - 64.7 references per article
Total predicted references:      66152 - 67.23 references per article

Total expected citation contexts:        109022 - 110.79 citation contexts per article
Total predicted citation contexts:       99982 - 101.61 citation contexts per article

Total correct predicted citation contexts:       96212 - 97.78 citation contexts per article
Total wrong predicted citation contexts:         3770 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     96.23
Recall citation contexts:        88.25
fscore citation contexts:        92.07
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
| availability_stmt           | 29.21     | 27.86     | 28.52     | 585     |
| figure_title                | 0.07      | 0.02      | 0.03      | 31718   |
| funding_stmt                | 6.18      | 29.53     | 10.22     | 921     |
| reference_citation          | 57.06     | 55.97     | 56.51     | 108949  |
| reference_figure            | 58.42     | 51.02     | 54.47     | 68926   |
| reference_table             | 71.83     | 73.46     | 72.63     | 2381    |
| section_title               | 82.81     | 77.25     | 79.93     | 21831   |
| table_title                 | 0         | 0         | 0         | 1925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **56.03** | **48.56** | **52.03** | 237236  |
| all fields (macro avg.)     | 38.2      | 39.39     | 37.79     | 237236  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| availability_stmt           | 40.68     | 38.8      | 39.72    | 585     |
| figure_title                | 49.75     | 16        | 24.21    | 31718   |
| funding_stmt                | 6.18      | 29.53     | 10.22    | 921     |
| reference_citation          | 93.6      | 91.81     | 92.7     | 108949  |
| reference_figure            | 58.71     | 51.27     | 54.73    | 68926   |
| reference_table             | 71.91     | 73.54     | 72.72    | 2381    |
| section_title               | 83.85     | 78.21     | 80.93    | 21831   |
| table_title                 | 94.26     | 28.16     | 43.36    | 1925    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **77.97** | **67.57** | **72.4** | 237236  |
| all fields (macro avg.)     | 62.37     | 50.92     | 52.32    | 237236  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 96.88     | 95.38     | 96.12     | 585     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.88** | **95.38** | **96.12** | 585     |
| all fields (macro avg.)     | 96.88     | 95.38     | 96.12     | 585     |

Evaluation metrics produced in 1348.778 seconds
