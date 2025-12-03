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
| abstract                    | 10.01     | 9.55      | 9.78      | 984     |
| authors                     | 76.17     | 76.09     | 76.13     | 983     |
| first_author                | 91.14     | 91.14     | 91.14     | 982     |
| title                       | 54.64     | 54.47     | 54.55     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **58.52** | **57.79** | **58.16** | 3933    |
| all fields (macro avg.)     | 57.99     | 57.81     | 57.9      | 3933    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 25.56     | 24.39     | 24.96     | 984     |
| authors                     | 76.58     | 76.5      | 76.54     | 983     |
| first_author                | 91.14     | 91.14     | 91.14     | 982     |
| title                       | 57.39     | 57.22     | 57.3      | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **63.08** | **62.29** | **62.68** | 3933    |
| all fields (macro avg.)     | 62.67     | 62.31     | 62.49     | 3933    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 50.37     | 48.07     | 49.19     | 984     |
| authors                     | 88.39     | 88.3      | 88.35     | 983     |
| first_author                | 91.45     | 91.45     | 91.45     | 982     |
| title                       | 57.39     | 57.22     | 57.3      | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **72.14** | **71.24** | **71.69** | 3933    |
| all fields (macro avg.)     | 71.9      | 71.26     | 71.57     | 3933    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 48.24     | 46.04     | 47.11     | 984     |
| authors                     | 81.26     | 81.18     | 81.22     | 983     |
| first_author                | 91.14     | 91.14     | 91.14     | 982     |
| title                       | 57.39     | 57.22     | 57.3      | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **69.75** | **68.88** | **69.31** | 3933    |
| all fields (macro avg.)     | 69.51     | 68.89     | 69.19     | 3933    |

#### Instance-level results

```
Total expected instances: 	984
Total correct instances: 	84 (strict) 
Total correct instances: 	236 (soft) 
Total correct instances: 	295 (Levenshtein) 
Total correct instances: 	277 (ObservedRatcliffObershelp) 

Instance-level recall:	8.54	(strict) 
Instance-level recall:	23.98	(soft) 
Instance-level recall:	29.98	(Levenshtein) 
Instance-level recall:	28.15	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 79.64     | 76.56     | 78.07    | 63265   |
| date                        | 95.95     | 91.36     | 93.6     | 63662   |
| first_author                | 94.75     | 91.05     | 92.86    | 63265   |
| inTitle                     | 94.24     | 91.08     | 92.63    | 63213   |
| issue                       | 0.17      | 6.25      | 0.33     | 16      |
| page                        | 95.24     | 92.32     | 93.76    | 53375   |
| title                       | 89.67     | 87.08     | 88.36    | 62044   |
| volume                      | 97.5      | 95.77     | 96.63    | 61049   |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **92.23** | **89.22** | **90.7** | 429889  |
| all fields (macro avg.)     | 80.89     | 78.93     | 79.53    | 429889  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1       | support |
|-----------------------------|-----------|----------|----------|---------|
| authors                     | 79.78     | 76.7     | 78.21    | 63265   |
| date                        | 95.95     | 91.36    | 93.6     | 63662   |
| first_author                | 94.83     | 91.13    | 92.94    | 63265   |
| inTitle                     | 94.69     | 91.51    | 93.07    | 63213   |
| issue                       | 0.17      | 6.25     | 0.33     | 16      |
| page                        | 95.24     | 92.32    | 93.76    | 53375   |
| title                       | 95.31     | 92.56    | 93.91    | 62044   |
| volume                      | 97.5      | 95.77    | 96.63    | 61049   |
|                             |           |          |          |         |
| **all fields (micro avg.)** | **93.14** | **90.1** | **91.6** | 429889  |
| all fields (macro avg.)     | 81.68     | 79.7     | 80.31    | 429889  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 93.05     | 89.46     | 91.22     | 63265   |
| date                        | 95.95     | 91.36     | 93.6      | 63662   |
| first_author                | 95.27     | 91.55     | 93.37     | 63265   |
| inTitle                     | 95.06     | 91.87     | 93.44     | 63213   |
| issue                       | 0.17      | 6.25      | 0.33      | 16      |
| page                        | 95.24     | 92.32     | 93.76     | 53375   |
| title                       | 97.33     | 94.52     | 95.9      | 62044   |
| volume                      | 97.5      | 95.77     | 96.63     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **95.5**  | **92.38** | **93.91** | 429889  |
| all fields (macro avg.)     | 83.7      | 81.64     | 82.28     | 429889  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.78     | 83.43     | 85.07     | 63265   |
| date                        | 95.95     | 91.36     | 93.6      | 63662   |
| first_author                | 94.76     | 91.06     | 92.87     | 63265   |
| inTitle                     | 94.68     | 91.5      | 93.07     | 63213   |
| issue                       | 0.17      | 6.25      | 0.33      | 16      |
| page                        | 95.24     | 92.32     | 93.76     | 53375   |
| title                       | 96.96     | 94.17     | 95.55     | 62044   |
| volume                      | 97.5      | 95.77     | 96.63     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.4**  | **91.32** | **92.83** | 429889  |
| all fields (macro avg.)     | 82.76     | 80.73     | 81.36     | 429889  |

#### Instance-level results

```
Total expected instances: 		63664
Total extracted instances: 		64287
Total correct instances: 		39794 (strict) 
Total correct instances: 		42485 (soft) 
Total correct instances: 		49526 (Levenshtein) 
Total correct instances: 		46427 (RatcliffObershelp) 

Instance-level precision:	61.9 (strict) 
Instance-level precision:	66.09 (soft) 
Instance-level precision:	77.04 (Levenshtein) 
Instance-level precision:	72.22 (RatcliffObershelp) 

Instance-level recall:	62.51	(strict) 
Instance-level recall:	66.73	(soft) 
Instance-level recall:	77.79	(Levenshtein) 
Instance-level recall:	72.93	(RatcliffObershelp) 

Instance-level f-score:	62.2 (strict) 
Instance-level f-score:	66.41 (soft) 
Instance-level f-score:	77.41 (Levenshtein) 
Instance-level f-score:	72.57 (RatcliffObershelp) 

Matching 1 :	56231

Matching 2 :	1593

Matching 3 :	1412

Matching 4 :	453

Total matches :	59689
```

#### Citation context resolution

```

Total expected references: 	 63664 - 64.7 references per article
Total predicted references: 	 64287 - 65.33 references per article

Total expected citation contexts: 	 109022 - 110.79 citation contexts per article
Total predicted citation contexts: 	 99675 - 101.3 citation contexts per article

Total correct predicted citation contexts: 	 92597 - 94.1 citation contexts per article
Total wrong predicted citation contexts: 	 7078 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 92.9
Recall citation contexts: 	 84.93
fscore citation contexts: 	 88.74
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| availability_stmt           | 25.98     | 27.18    | 26.57     | 585     |
| figure_title                | 0.06      | 0.02     | 0.03      | 31718   |
| funding_stmt                | 3.7       | 10.75    | 5.51      | 921     |
| reference_citation          | 57.1      | 55.97    | 56.53     | 108949  |
| reference_figure            | 58.43     | 51.06    | 54.49     | 68926   |
| reference_table             | 70.5      | 73.37    | 71.91     | 2381    |
| section_title               | 83.1      | 77.31    | 80.1      | 21831   |
| table_title                 | 0         | 0        | 0         | 1925    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **56.43** | **48.5** | **52.17** | 237236  |
| all fields (macro avg.)     | 37.36     | 36.96    | 36.89     | 237236  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 36.6      | 38.29     | 37.43     | 585     |
| figure_title                | 49.73     | 16.01     | 24.22     | 31718   |
| funding_stmt                | 3.7       | 10.75     | 5.51      | 921     |
| reference_citation          | 93.67     | 91.82     | 92.73     | 108949  |
| reference_figure            | 58.71     | 51.3      | 54.76     | 68926   |
| reference_table             | 70.58     | 73.46     | 71.99     | 2381    |
| section_title               | 84.14     | 78.27     | 81.1      | 21831   |
| table_title                 | 94.89     | 28        | 43.24     | 1925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **78.56** | **67.52** | **72.62** | 237236  |
| all fields (macro avg.)     | 61.5      | 48.49     | 51.37     | 237236  |

**Document-level ratio results**

| label                       | precision | recall  | f1        | support |
|-----------------------------|-----------|---------|-----------|---------|
| availability_stmt           | 94.15     | 104.62  | 99.11     | 585     |
|                             |           |         |           |         |
| **all fields (micro avg.)** | **94.15** | **100** | **96.99** | 585     |
| all fields (macro avg.)     | 94.15     | 100     | 99.11     | 585     |

Evaluation metrics produced in 1273.88 seconds

