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
| abstract                    | 6.62      | 6.4       | 6.51      | 984     |
| authors                     | 48.61     | 48.02     | 48.31     | 983     |
| first_author                | 66.22     | 65.48     | 65.85     | 982     |
| title                       | 84.34     | 82.62     | 83.47     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **51.62** | **50.62** | **51.12** | 3933    |
| all fields (macro avg.)     | 51.45     | 50.63     | 51.04     | 3933    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 16.93     | 16.36     | 16.64     | 984     |
| authors                     | 48.92     | 48.32     | 48.62     | 983     |
| first_author                | 66.22     | 65.48     | 65.85     | 982     |
| title                       | 92.32     | 90.45     | 91.38     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **56.24** | **55.15** | **55.69** | 3933    |
| all fields (macro avg.)     | 56.1      | 55.15     | 55.62     | 3933    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 46.06     | 44.51     | 45.27     | 984     |
| authors                     | 74.36     | 73.45     | 73.9      | 983     |
| first_author                | 66.22     | 65.48     | 65.85     | 982     |
| title                       | 95.54     | 93.6      | 94.56     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **70.62** | **69.26** | **69.94** | 3933    |
| all fields (macro avg.)     | 70.54     | 69.26     | 69.89     | 3933    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall | f1        | support |
|-----------------------------|-----------|--------|-----------|---------|
| abstract                    | 43.43     | 41.97  | 42.69     | 984     |
| authors                     | 56.64     | 55.95  | 56.29     | 983     |
| first_author                | 66.22     | 65.48  | 65.85     | 982     |
| title                       | 94.5      | 92.58  | 93.53     | 984     |
|                             |           |        |           |         |
| **all fields (micro avg.)** | **65.26** | **64** | **64.62** | 3933    |
| all fields (macro avg.)     | 65.2      | 64     | 64.59     | 3933    |

#### Instance-level results

```
Total expected instances: 	984
Total correct instances: 	49 (strict) 
Total correct instances: 	138 (soft) 
Total correct instances: 	262 (Levenshtein) 
Total correct instances: 	234 (ObservedRatcliffObershelp) 

Instance-level recall:	4.98	(strict) 
Instance-level recall:	14.02	(soft) 
Instance-level recall:	26.63	(Levenshtein) 
Instance-level recall:	23.78	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.47     | 78.3      | 78.88     | 63265   |
| date                        | 95.93     | 94.11     | 95.01     | 63662   |
| first_author                | 94.86     | 93.43     | 94.14     | 63265   |
| inTitle                     | 95.84     | 94.79     | 95.31     | 63213   |
| issue                       | 2.01      | 75        | 3.92      | 16      |
| page                        | 96.27     | 95.33     | 95.8      | 53375   |
| title                       | 90.31     | 90.81     | 90.56     | 62044   |
| volume                      | 97.92     | 98.31     | 98.12     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.73** | **92.06** | **92.39** | 429889  |
| all fields (macro avg.)     | 81.58     | 90.01     | 81.47     | 429889  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.61     | 78.43     | 79.01     | 63265   |
| date                        | 95.93     | 94.11     | 95.01     | 63662   |
| first_author                | 94.94     | 93.51     | 94.22     | 63265   |
| inTitle                     | 96.33     | 95.27     | 95.79     | 63213   |
| issue                       | 2.01      | 75        | 3.92      | 16      |
| page                        | 96.27     | 95.33     | 95.8      | 53375   |
| title                       | 95.97     | 96.5      | 96.23     | 62044   |
| volume                      | 97.92     | 98.31     | 98.12     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.66** | **92.98** | **93.32** | 429889  |
| all fields (macro avg.)     | 82.37     | 90.81     | 82.26     | 429889  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 93.35     | 91.98     | 92.66     | 63265   |
| date                        | 95.93     | 94.11     | 95.01     | 63662   |
| first_author                | 95.38     | 93.95     | 94.66     | 63265   |
| inTitle                     | 96.66     | 95.59     | 96.12     | 63213   |
| issue                       | 2.01      | 75        | 3.92      | 16      |
| page                        | 96.27     | 95.33     | 95.8      | 53375   |
| title                       | 97.71     | 98.25     | 97.98     | 62044   |
| volume                      | 97.92     | 98.31     | 98.12     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.04** | **95.34** | **95.69** | 429889  |
| all fields (macro avg.)     | 84.4      | 92.82     | 84.28     | 429889  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.79     | 85.51     | 86.14     | 63265   |
| date                        | 95.93     | 94.11     | 95.01     | 63662   |
| first_author                | 94.87     | 93.44     | 94.15     | 63265   |
| inTitle                     | 96.33     | 95.27     | 95.8      | 63213   |
| issue                       | 2.01      | 75        | 3.92      | 16      |
| page                        | 96.27     | 95.33     | 95.8      | 53375   |
| title                       | 97.56     | 98.1      | 97.83     | 62044   |
| volume                      | 97.92     | 98.31     | 98.12     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.93** | **94.24** | **94.59** | 429889  |
| all fields (macro avg.)     | 83.46     | 91.89     | 83.35     | 429889  |

#### Instance-level results

```
Total expected instances: 		63664
Total extracted instances: 		66125
Total correct instances: 		42372 (strict) 
Total correct instances: 		45216 (soft) 
Total correct instances: 		52867 (Levenshtein) 
Total correct instances: 		49469 (RatcliffObershelp) 

Instance-level precision:	64.08 (strict) 
Instance-level precision:	68.38 (soft) 
Instance-level precision:	79.95 (Levenshtein) 
Instance-level precision:	74.81 (RatcliffObershelp) 

Instance-level recall:	66.56	(strict) 
Instance-level recall:	71.02	(soft) 
Instance-level recall:	83.04	(Levenshtein) 
Instance-level recall:	77.7	(RatcliffObershelp) 

Instance-level f-score:	65.29 (strict) 
Instance-level f-score:	69.68 (soft) 
Instance-level f-score:	81.47 (Levenshtein) 
Instance-level f-score:	76.23 (RatcliffObershelp) 

Matching 1 :	58671

Matching 2 :	1017

Matching 3 :	1244

Matching 4 :	367

Total matches :	61299
```

#### Citation context resolution

```

Total expected references: 	 63664 - 64.7 references per article
Total predicted references: 	 66125 - 67.2 references per article

Total expected citation contexts: 	 109022 - 110.79 citation contexts per article
Total predicted citation contexts: 	 99798 - 101.42 citation contexts per article

Total correct predicted citation contexts: 	 96078 - 97.64 citation contexts per article
Total wrong predicted citation contexts: 	 3720 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 96.27
Recall citation contexts: 	 88.13
fscore citation contexts: 	 92.02
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
| availability_stmt           | 27.94     | 26.84     | 27.38     | 585     |
| figure_title                | 0.07      | 0.02      | 0.03      | 31718   |
| funding_stmt                | 5.78      | 27.47     | 9.55      | 921     |
| reference_citation          | 57.07     | 55.89     | 56.47     | 108949  |
| reference_figure            | 58.4      | 51.03     | 54.47     | 68926   |
| reference_table             | 71.69     | 73.37     | 72.52     | 2381    |
| section_title               | 82.9      | 77.33     | 80.02     | 21831   |
| table_title                 | 0         | 0         | 0         | 1925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **56.03** | **48.52** | **52.01** | 237236  |
| all fields (macro avg.)     | 37.98     | 38.99     | 37.55     | 237236  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 39.32     | 37.78     | 38.54     | 585     |
| figure_title                | 49.76     | 16        | 24.22     | 31718   |
| funding_stmt                | 5.78      | 27.47     | 9.55      | 921     |
| reference_citation          | 93.65     | 91.71     | 92.67     | 108949  |
| reference_figure            | 58.69     | 51.28     | 54.73     | 68926   |
| reference_table             | 71.77     | 73.46     | 72.6      | 2381    |
| section_title               | 83.94     | 78.3      | 81.02     | 21831   |
| table_title                 | 94.91     | 28.1      | 43.37     | 1925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **77.98** | **67.53** | **72.38** | 237236  |
| all fields (macro avg.)     | 62.23     | 50.51     | 52.09     | 237236  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 95.09     | 96.07     | 95.58     | 585     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **95.09** | **96.07** | **95.58** | 585     |
| all fields (macro avg.)     | 95.09     | 96.07     | 95.58     | 585     |

Evaluation metrics produced in 1356.172 seconds
