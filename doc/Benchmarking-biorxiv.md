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

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 2.37      | 2.31      | 2.34      | 1990    |
| authors                     | 73.61     | 72.99     | 73.3      | 1999    |
| first_author                | 94.25     | 93.54     | 93.89     | 1997    |
| keywords                    | 58.43     | 59.48     | 58.95     | 839     |
| title                       | 82.38     | 81.6      | 81.99     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **62.95** | **62.37** | **62.66** | 8825    |
| all fields (macro avg.)     | 62.21     | 61.98     | 62.09     | 8825    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| abstract                    | 60.31     | 58.94    | 59.62     | 1990    |
| authors                     | 75.88     | 75.24    | 75.56     | 1999    |
| first_author                | 94.85     | 94.14    | 94.5      | 1997    |
| keywords                    | 63.7      | 64.84    | 64.26     | 839     |
| title                       | 90.86     | 90       | 90.43     | 2000    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **78.92** | **78.2** | **78.56** | 8825    |
| all fields (macro avg.)     | 77.12     | 76.63    | 76.87     | 8825    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 80.98     | 79.15     | 80.05     | 1990    |
| authors                     | 89.66     | 88.89     | 89.27     | 1999    |
| first_author                | 95.16     | 94.44     | 94.8      | 1997    |
| keywords                    | 79.51     | 80.93     | 80.21     | 839     |
| title                       | 94.6      | 93.7      | 94.15     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.1**  | **88.28** | **88.69** | 8825    |
| all fields (macro avg.)     | 87.98     | 87.42     | 87.7      | 8825    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| abstract                    | 77.48     | 75.73    | 76.59     | 1990    |
| authors                     | 82.04     | 81.34    | 81.69     | 1999    |
| first_author                | 94.25     | 93.54    | 93.89     | 1997    |
| keywords                    | 71.55     | 72.82    | 72.18     | 839     |
| title                       | 93.59     | 92.7     | 93.14     | 2000    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **85.38** | **84.6** | **84.99** | 8825    |
| all fields (macro avg.)     | 83.78     | 83.23    | 83.5      | 8825    |

#### Instance-level results

```
Total expected instances: 	2000
Total correct instances: 	30 (strict) 
Total correct instances: 	722 (soft) 
Total correct instances: 	1212 (Levenshtein) 
Total correct instances: 	1033 (ObservedRatcliffObershelp) 

Instance-level recall:	1.5	(strict) 
Instance-level recall:	36.1	(soft) 
Instance-level recall:	60.6	(Levenshtein) 
Instance-level recall:	51.65	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.21     | 83.25     | 85.66     | 97183   |
| date                        | 91.71     | 86.28     | 88.91     | 97630   |
| doi                         | 70.78     | 83.84     | 76.76     | 16894   |
| first_author                | 95.1      | 89.68     | 92.31     | 97183   |
| inTitle                     | 82.94     | 79.46     | 81.16     | 96430   |
| issue                       | 94.34     | 91.91     | 93.11     | 30312   |
| page                        | 95.03     | 78.34     | 85.88     | 88597   |
| pmcid                       | 66.44     | 86.12     | 75.01     | 807     |
| pmid                        | 69.74     | 84.66     | 76.48     | 2093    |
| title                       | 84.93     | 83.57     | 84.25     | 92463   |
| volume                      | 96.29     | 95.21     | 95.75     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.89** | **85.34** | **87.56** | 707301  |
| all fields (macro avg.)     | 85.05     | 85.67     | 85.03     | 707301  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.36     | 84.34     | 86.78     | 97183   |
| date                        | 91.71     | 86.28     | 88.91     | 97630   |
| doi                         | 75.26     | 89.14     | 81.61     | 16894   |
| first_author                | 95.52     | 90.08     | 92.72     | 97183   |
| inTitle                     | 92.39     | 88.52     | 90.41     | 96430   |
| issue                       | 94.34     | 91.91     | 93.11     | 30312   |
| page                        | 95.03     | 78.34     | 85.88     | 88597   |
| pmcid                       | 75.72     | 98.14     | 85.48     | 807     |
| pmid                        | 74.14     | 90.01     | 81.31     | 2093    |
| title                       | 93.26     | 91.77     | 92.51     | 92463   |
| volume                      | 96.29     | 95.21     | 95.75     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.7**  | **88.01** | **90.29** | 707301  |
| all fields (macro avg.)     | 88.46     | 89.43     | 88.59     | 707301  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.62     | 89.31     | 91.89     | 97183   |
| date                        | 91.71     | 86.28     | 88.91     | 97630   |
| doi                         | 77.51     | 91.8      | 84.05     | 16894   |
| first_author                | 95.67     | 90.22     | 92.87     | 97183   |
| inTitle                     | 93.37     | 89.46     | 91.37     | 96430   |
| issue                       | 94.34     | 91.91     | 93.11     | 30312   |
| page                        | 95.03     | 78.34     | 85.88     | 88597   |
| pmcid                       | 75.72     | 98.14     | 85.48     | 807     |
| pmid                        | 74.14     | 90.01     | 81.31     | 2093    |
| title                       | 96.1      | 94.56     | 95.32     | 92463   |
| volume                      | 96.29     | 95.21     | 95.75     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.03** | **89.26** | **91.58** | 707301  |
| all fields (macro avg.)     | 89.5      | 90.48     | 89.63     | 707301  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 91.58     | 86.44     | 88.94     | 97183   |
| date                        | 91.71     | 86.28     | 88.91     | 97630   |
| doi                         | 75.95     | 89.96     | 82.37     | 16894   |
| first_author                | 95.15     | 89.72     | 92.36     | 97183   |
| inTitle                     | 91.15     | 87.33     | 89.2      | 96430   |
| issue                       | 94.34     | 91.91     | 93.11     | 30312   |
| page                        | 95.03     | 78.34     | 85.88     | 88597   |
| pmcid                       | 66.44     | 86.12     | 75.01     | 807     |
| pmid                        | 69.74     | 84.66     | 76.48     | 2093    |
| title                       | 95.45     | 93.92     | 94.68     | 92463   |
| volume                      | 96.29     | 95.21     | 95.75     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.07** | **88.35** | **90.65** | 707301  |
| all fields (macro avg.)     | 87.53     | 88.17     | 87.52     | 707301  |

#### Instance-level results

```
Total expected instances: 		98799
Total extracted instances: 		97985
Total correct instances: 		43762 (strict) 
Total correct instances: 		54757 (soft) 
Total correct instances: 		58953 (Levenshtein) 
Total correct instances: 		55682 (RatcliffObershelp) 

Instance-level precision:	44.66 (strict) 
Instance-level precision:	55.88 (soft) 
Instance-level precision:	60.17 (Levenshtein) 
Instance-level precision:	56.83 (RatcliffObershelp) 

Instance-level recall:	44.29	(strict) 
Instance-level recall:	55.42	(soft) 
Instance-level recall:	59.67	(Levenshtein) 
Instance-level recall:	56.36	(RatcliffObershelp) 

Instance-level f-score:	44.48 (strict) 
Instance-level f-score:	55.65 (soft) 
Instance-level f-score:	59.92 (Levenshtein) 
Instance-level f-score:	56.59 (RatcliffObershelp) 

Matching 1 :	79261

Matching 2 :	4449

Matching 3 :	4371

Matching 4 :	2115

Total matches :	90196
```

#### Citation context resolution

```

Total expected references: 	 98797 - 49.4 references per article
Total predicted references: 	 97985 - 48.99 references per article

Total expected citation contexts: 	 142862 - 71.43 citation contexts per article
Total predicted citation contexts: 	 134429 - 67.21 citation contexts per article

Total correct predicted citation contexts: 	 115925 - 57.96 citation contexts per article
Total wrong predicted citation contexts: 	 18504 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 86.24
Recall citation contexts: 	 81.14
fscore citation contexts: 	 83.61
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
| availability_stmt           | 29.77     | 26.23     | 27.89     | 446     |
| figure_title                | 4.58      | 2.18      | 2.95      | 22978   |
| funding_stmt                | 3.65      | 24.97     | 6.37      | 745     |
| reference_citation          | 71.81     | 70.77     | 71.29     | 147470  |
| reference_figure            | 70.6      | 72.13     | 71.35     | 47984   |
| reference_table             | 49.77     | 84.87     | 62.75     | 5957    |
| section_title               | 70.76     | 69.5      | 70.13     | 32398   |
| table_title                 | 7.15      | 2.32      | 3.5       | 3925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **65.91** | **63.93** | **64.91** | 261903  |
| all fields (macro avg.)     | 38.51     | 44.12     | 39.53     | 261903  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 49.11     | 43.27     | 46.01     | 446     |
| figure_title                | 71.1      | 33.85     | 45.87     | 22978   |
| funding_stmt                | 3.89      | 26.58     | 6.78      | 745     |
| reference_citation          | 84.1      | 82.88     | 83.48     | 147470  |
| reference_figure            | 71.23     | 72.78     | 72        | 47984   |
| reference_table             | 50.21     | 85.61     | 63.3      | 5957    |
| section_title               | 76.25     | 74.89     | 75.56     | 32398   |
| table_title                 | 84.84     | 27.52     | 41.55     | 3925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **77.06** | **74.74** | **75.88** | 261903  |
| all fields (macro avg.)     | 61.34     | 55.92     | 54.32     | 261903  |

**Document-level ratio results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| availability_stmt           | 83.8      | 88.12     | 85.9     | 446     |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **83.8**  | **88.12** | **85.9** | 446     |
| all fields (macro avg.)     | 83.8      | 88.12     | 85.9     | 446     |

Evaluation metrics produced in 1597.027 seconds



