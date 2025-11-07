# Benchmarking biorXiv

## General

This is the end-to-end benchmarking result for GROBID version **0.8.2** against the `bioRxiv` test set (
`biorxiv-10k-test-2000`), see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for
reproducing this evaluation.

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

Evaluation on 2000 PDF preprints out of 2000 (no failure).

Runtime for processing 2000 PDF: **1713** seconds (0.85 seconds per PDF file) on Ubuntu 22.04, 16 CPU (32 threads),
128GB RAM and with a GeForce GTX 1080 Ti GPU.

Note: with CRF only models runtime is 622s (0.31 second per PDF) with 4 CPU, 8 threads.

## Header metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| abstract                    | 2.42      | 2.36      | 2.39     | 1990    |
| authors                     | 85.12     | 84.39     | 84.75    | 1999    |
| first_author                | 96.92     | 96.19     | 96.56    | 1997    |
| keywords                    | 58.13     | 59.24     | 58.68    | 839     |
| title                       | 77.33     | 76.6      | 76.97    | 2000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **65**    | **64.41** | **64.7** | 8825    |
| all fields (macro avg.)     | 63.98     | 63.76     | 63.87    | 8825    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 60.57     | 59.2      | 59.87     | 1990    |
| authors                     | 85.52     | 84.79     | 85.15     | 1999    |
| first_author                | 97.12     | 96.39     | 96.76     | 1997    |
| keywords                    | 63.27     | 64.48     | 63.87     | 839     |
| title                       | 79.45     | 78.7      | 79.08     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **79.05** | **78.33** | **78.69** | 8825    |
| all fields (macro avg.)     | 77.19     | 76.71     | 76.95     | 8825    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| abstract                    | 80.72     | 78.89     | 79.8     | 1990    |
| authors                     | 92.63     | 91.85     | 92.24    | 1999    |
| first_author                | 97.38     | 96.64     | 97.01    | 1997    |
| keywords                    | 79.3      | 80.81     | 80.05    | 839     |
| title                       | 91.97     | 91.1      | 91.53    | 2000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **89.61** | **88.79** | **89.2** | 8825    |
| all fields (macro avg.)     | 88.4      | 87.86     | 88.13    | 8825    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 77.22     | 75.48     | 76.34     | 1990    |
| authors                     | 88.7      | 87.94     | 88.32     | 1999    |
| first_author                | 96.92     | 96.19     | 96.56     | 1997    |
| keywords                    | 70.99     | 72.35     | 71.66     | 839     |
| title                       | 87.63     | 86.8      | 87.21     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.04** | **85.26** | **85.65** | 8825    |
| all fields (macro avg.)     | 84.29     | 83.75     | 84.02     | 8825    |

#### Instance-level results

```
Total expected instances:       2000
Total correct instances:        40 (strict)
Total correct instances:        728 (soft)
Total correct instances:        1237 (Levenshtein)
Total correct instances:        1066 (ObservedRatcliffObershelp)

Instance-level recall:  2       (strict)
Instance-level recall:  36.4    (soft)
Instance-level recall:  61.85   (Levenshtein)
Instance-level recall:  53.3    (RatcliffObershelp)
```

## Citation metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.2      | 83.12     | 85.58     | 97183   |
| date                        | 91.71     | 86.15     | 88.84     | 97630   |
| doi                         | 70.86     | 83.85     | 76.81     | 16894   |
| first_author                | 95.08     | 89.53     | 92.22     | 97183   |
| inTitle                     | 82.9      | 79.31     | 81.06     | 96430   |
| issue                       | 94.35     | 91.93     | 93.13     | 30312   |
| page                        | 94.99     | 78.22     | 85.79     | 88597   |
| pmcid                       | 66.44     | 86.12     | 75.01     | 807     |
| pmid                        | 69.99     | 84.57     | 76.59     | 2093    |
| title                       | 84.9      | 83.42     | 84.16     | 92463   |
| volume                      | 96.27     | 95.07     | 95.66     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.87** | **85.21** | **87.48** | 707301  |
| all fields (macro avg.)     | 85.06     | 85.57     | 84.99     | 707301  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.35     | 84.21     | 86.71     | 97183   |
| date                        | 91.71     | 86.15     | 88.84     | 97630   |
| doi                         | 75.34     | 89.16     | 81.67     | 16894   |
| first_author                | 95.51     | 89.93     | 92.64     | 97183   |
| inTitle                     | 92.37     | 88.38     | 90.33     | 96430   |
| issue                       | 94.35     | 91.93     | 93.13     | 30312   |
| page                        | 94.99     | 78.22     | 85.79     | 88597   |
| pmcid                       | 75.72     | 98.14     | 85.48     | 807     |
| pmid                        | 74.42     | 89.92     | 81.44     | 2093    |
| title                       | 93.25     | 91.63     | 92.43     | 92463   |
| volume                      | 96.27     | 95.07     | 95.66     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.69** | **87.88** | **90.22** | 707301  |
| all fields (macro avg.)     | 88.48     | 89.34     | 88.56     | 707301  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.61     | 89.16     | 91.81     | 97183   |
| date                        | 91.71     | 86.15     | 88.84     | 97630   |
| doi                         | 77.58     | 91.81     | 84.1      | 16894   |
| first_author                | 95.66     | 90.08     | 92.78     | 97183   |
| inTitle                     | 93.36     | 89.32     | 91.29     | 96430   |
| issue                       | 94.35     | 91.93     | 93.13     | 30312   |
| page                        | 94.99     | 78.22     | 85.79     | 88597   |
| pmcid                       | 75.72     | 98.14     | 85.48     | 807     |
| pmid                        | 74.42     | 89.92     | 81.44     | 2093    |
| title                       | 96.08     | 94.41     | 95.24     | 92463   |
| volume                      | 96.27     | 95.07     | 95.66     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.01** | **89.14** | **91.51** | 707301  |
| all fields (macro avg.)     | 89.52     | 90.38     | 89.6      | 707301  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 91.57     | 86.3      | 88.85     | 97183   |
| date                        | 91.71     | 86.15     | 88.84     | 97630   |
| doi                         | 76.04     | 89.98     | 82.42     | 16894   |
| first_author                | 95.13     | 89.58     | 92.27     | 97183   |
| inTitle                     | 91.13     | 87.19     | 89.11     | 96430   |
| issue                       | 94.35     | 91.93     | 93.13     | 30312   |
| page                        | 94.99     | 78.22     | 85.79     | 88597   |
| pmcid                       | 66.44     | 86.12     | 75.01     | 807     |
| pmid                        | 69.99     | 84.57     | 76.59     | 2093    |
| title                       | 95.41     | 93.75     | 94.57     | 92463   |
| volume                      | 96.27     | 95.07     | 95.66     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.05** | **88.22** | **90.57** | 707301  |
| all fields (macro avg.)     | 87.55     | 88.08     | 87.48     | 707301  |

#### Instance-level results

```
Total expected instances:               98799
Total extracted instances:              97808
Total correct instances:                43695 (strict)
Total correct instances:                54689 (soft)
Total correct instances:                58863 (Levenshtein)
Total correct instances:                55597 (RatcliffObershelp)

Instance-level precision:       44.67 (strict)
Instance-level precision:       55.91 (soft)
Instance-level precision:       60.18 (Levenshtein)
Instance-level precision:       56.84 (RatcliffObershelp)

Instance-level recall:  44.23   (strict)
Instance-level recall:  55.35   (soft)
Instance-level recall:  59.58   (Levenshtein)
Instance-level recall:  56.27   (RatcliffObershelp)

Instance-level f-score: 44.45 (strict)
Instance-level f-score: 55.63 (soft)
Instance-level f-score: 59.88 (Levenshtein)
Instance-level f-score: 56.56 (RatcliffObershelp)

Matching 1 :    79152

Matching 2 :    4442

Matching 3 :    4360

Matching 4 :    2101

Total matches : 90055
```

#### Citation context resolution

```

Total expected references:       98797 - 49.4 references per article
Total predicted references:      97808 - 48.9 references per article

Total expected citation contexts:        142862 - 71.43 citation contexts per article
Total predicted citation contexts:       134498 - 67.25 citation contexts per article

Total correct predicted citation contexts:       115971 - 57.99 citation contexts per article
Total wrong predicted citation contexts:         18527 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     86.23
Recall citation contexts:        81.18
fscore citation contexts:        83.62
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 29.61     | 25.56     | 27.44     | 446     |
| figure_title                | 4.29      | 2.34      | 3.03      | 22978   |
| funding_stmt                | 3.46      | 22.95     | 6.01      | 745     |
| reference_citation          | 72.02     | 70.94     | 71.48     | 147470  |
| reference_figure            | 70.41     | 77.14     | 73.62     | 47984   |
| reference_table             | 45.65     | 86.74     | 59.82     | 5957    |
| section_title               | 71.35     | 69.91     | 70.62     | 32398   |
| table_title                 | 7.41      | 2.7       | 3.96      | 3925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **65.48** | **65.06** | **65.27** | 261903  |
| all fields (macro avg.)     | 38.02     | 44.79     | 39.5      | 261903  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 50.65     | 43.72     | 46.93     | 446     |
| figure_title                | 68.34     | 37.29     | 48.25     | 22978   |
| funding_stmt                | 3.68      | 24.43     | 6.39      | 745     |
| reference_citation          | 84.34     | 83.08     | 83.7      | 147470  |
| reference_figure            | 71.05     | 77.84     | 74.29     | 47984   |
| reference_table             | 46.07     | 87.53     | 60.36     | 5957    |
| section_title               | 76.91     | 75.37     | 76.13     | 32398   |
| table_title                 | 82.8      | 30.17     | 44.22     | 3925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **76.72** | **76.22** | **76.47** | 261903  |
| all fields (macro avg.)     | 60.48     | 57.43     | 55.04     | 261903  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 84.8      | 86.32     | 85.56     | 446     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **84.8**  | **86.32** | **85.56** | 446     |
| all fields (macro avg.)     | 84.8      | 86.32     | 85.56     | 446     |

Evaluation metrics produced in 1607.353 seconds

