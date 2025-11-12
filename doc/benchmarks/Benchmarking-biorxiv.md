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
| abstract                    | 2.31      | 2.26      | 2.28      | 1990    |
| authors                     | 73.86     | 73.24     | 73.55     | 1999    |
| first_author                | 94.45     | 93.74     | 94.09     | 1997    |
| keywords                    | 63.09     | 63.77     | 63.43     | 839     |
| title                       | 84.87     | 83.55     | 84.2      | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **63.98** | **63.31** | **63.64** | 8825    |
| all fields (macro avg.)     | 63.72     | 63.31     | 63.51     | 8825    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 60.33     | 59.15     | 59.73     | 1990    |
| authors                     | 76.24     | 75.59     | 75.91     | 1999    |
| first_author                | 95.06     | 94.34     | 94.7      | 1997    |
| keywords                    | 69.1      | 69.85     | 69.47     | 839     |
| title                       | 93.19     | 91.75     | 92.47     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **80.08** | **79.24** | **79.66** | 8825    |
| all fields (macro avg.)     | 78.78     | 78.13     | 78.46     | 8825    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| abstract                    | 78.93     | 77.39     | 78.15    | 1990    |
| authors                     | 89.56     | 88.79     | 89.17    | 1999    |
| first_author                | 95.36     | 94.64     | 95       | 1997    |
| keywords                    | 81.49     | 82.36     | 81.92    | 839     |
| title                       | 96.9      | 95.4      | 96.15    | 2000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **89.37** | **88.43** | **88.9** | 8825    |
| all fields (macro avg.)     | 88.45     | 87.72     | 88.08    | 8825    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 75.7      | 74.22     | 74.96     | 1990    |
| authors                     | 82.19     | 81.49     | 81.84     | 1999    |
| first_author                | 94.45     | 93.74     | 94.09     | 1997    |
| keywords                    | 74.17     | 74.97     | 74.57     | 839     |
| title                       | 95.78     | 94.3      | 95.04     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **85.81** | **84.91** | **85.36** | 8825    |
| all fields (macro avg.)     | 84.46     | 83.74     | 84.1      | 8825    |

#### Instance-level results

```
Total expected instances: 	2000
Total correct instances: 	34 (strict) 
Total correct instances: 	768 (soft) 
Total correct instances: 	1226 (Levenshtein) 
Total correct instances: 	1041 (ObservedRatcliffObershelp) 

Instance-level recall:	1.7	(strict) 
Instance-level recall:	38.4	(soft) 
Instance-level recall:	61.3	(Levenshtein) 
Instance-level recall:	52.05	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| authors                     | 87.59     | 80.26    | 83.76     | 97183   |
| date                        | 91.09     | 83.79    | 87.29     | 97630   |
| doi                         | 72.16     | 82.53    | 77        | 16894   |
| first_author                | 94.57     | 86.61    | 90.42     | 97183   |
| inTitle                     | 80.59     | 75.82    | 78.13     | 96430   |
| issue                       | 94.92     | 87.68    | 91.16     | 30312   |
| page                        | 93.71     | 76.66    | 84.33     | 88597   |
| pmcid                       | 66.02     | 76.33    | 70.8      | 807     |
| pmid                        | 66.99     | 76.97    | 71.63     | 2093    |
| title                       | 84.27     | 79.42    | 81.78     | 92463   |
| volume                      | 95.43     | 92.25    | 93.81     | 87709   |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **89.05** | **82.3** | **85.54** | 707301  |
| all fields (macro avg.)     | 84.3      | 81.67    | 82.74     | 707301  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 88.73     | 81.31     | 84.86    | 97183   |
| date                        | 91.09     | 83.79     | 87.29    | 97630   |
| doi                         | 76.64     | 87.65     | 81.78    | 16894   |
| first_author                | 95.01     | 87        | 90.83    | 97183   |
| inTitle                     | 89.91     | 84.59     | 87.17    | 96430   |
| issue                       | 94.92     | 87.68     | 91.16    | 30312   |
| page                        | 93.71     | 76.66     | 84.33    | 88597   |
| pmcid                       | 75.24     | 86.99     | 80.69    | 807     |
| pmid                        | 71.52     | 82.18     | 76.48    | 2093    |
| title                       | 92.46     | 87.14     | 89.72    | 92463   |
| volume                      | 95.43     | 92.25     | 93.81    | 87709   |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **91.82** | **84.85** | **88.2** | 707301  |
| all fields (macro avg.)     | 87.7      | 85.2      | 86.19    | 707301  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 93.87     | 86.01     | 89.77     | 97183   |
| date                        | 91.09     | 83.79     | 87.29     | 97630   |
| doi                         | 77.81     | 88.98     | 83.02     | 16894   |
| first_author                | 95.15     | 87.14     | 90.97     | 97183   |
| inTitle                     | 90.76     | 85.39     | 87.99     | 96430   |
| issue                       | 94.92     | 87.68     | 91.16     | 30312   |
| page                        | 93.71     | 76.66     | 84.33     | 88597   |
| pmcid                       | 75.24     | 86.99     | 80.69     | 807     |
| pmid                        | 71.56     | 82.23     | 76.52     | 2093    |
| title                       | 95.56     | 90.06     | 92.73     | 92463   |
| volume                      | 95.43     | 92.25     | 93.81     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.1**  | **86.04** | **89.43** | 707301  |
| all fields (macro avg.)     | 88.65     | 86.11     | 87.12     | 707301  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 90.85     | 83.25     | 86.88     | 97183   |
| date                        | 91.09     | 83.79     | 87.29     | 97630   |
| doi                         | 77.07     | 88.13     | 82.23     | 16894   |
| first_author                | 94.62     | 86.65     | 90.46     | 97183   |
| inTitle                     | 88.65     | 83.4      | 85.95     | 96430   |
| issue                       | 94.92     | 87.68     | 91.16     | 30312   |
| page                        | 93.71     | 76.66     | 84.33     | 88597   |
| pmcid                       | 66.02     | 76.33     | 70.8      | 807     |
| pmid                        | 66.99     | 76.97     | 71.63     | 2093    |
| title                       | 94.64     | 89.19     | 91.84     | 92463   |
| volume                      | 95.43     | 92.25     | 93.81     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.15** | **85.16** | **88.52** | 707301  |
| all fields (macro avg.)     | 86.73     | 84.03     | 85.13     | 707301  |

#### Instance-level results

```
Total expected instances: 		98799
Total extracted instances: 		98516
Total correct instances: 		41624 (strict) 
Total correct instances: 		52146 (soft) 
Total correct instances: 		56043 (Levenshtein) 
Total correct instances: 		53037 (RatcliffObershelp) 

Instance-level precision:	42.25 (strict) 
Instance-level precision:	52.93 (soft) 
Instance-level precision:	56.89 (Levenshtein) 
Instance-level precision:	53.84 (RatcliffObershelp) 

Instance-level recall:	42.13	(strict) 
Instance-level recall:	52.78	(soft) 
Instance-level recall:	56.72	(Levenshtein) 
Instance-level recall:	53.68	(RatcliffObershelp) 

Instance-level f-score:	42.19 (strict) 
Instance-level f-score:	52.86 (soft) 
Instance-level f-score:	56.81 (Levenshtein) 
Instance-level f-score:	53.76 (RatcliffObershelp) 

Matching 1 :	75407

Matching 2 :	5484

Matching 3 :	4416

Matching 4 :	2268

Total matches :	87575
```

#### Citation context resolution

```

Total expected references: 	 98797 - 49.4 references per article
Total predicted references: 	 98516 - 49.26 references per article

Total expected citation contexts: 	 142862 - 71.43 citation contexts per article
Total predicted citation contexts: 	 134306 - 67.15 citation contexts per article

Total correct predicted citation contexts: 	 112437 - 56.22 citation contexts per article
Total wrong predicted citation contexts: 	 21869 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 83.72
Recall citation contexts: 	 78.7
fscore citation contexts: 	 81.13
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
| availability_stmt           | 32.71     | 27.58     | 29.93     | 446     |
| conflict_stmt               | 69.47     | 58.29     | 63.39     | 609     |
| contribution_stmt           | 46.7      | 44.17     | 45.4      | 609     |
| figure_title                | 4.35      | 2.37      | 3.07      | 22978   |
| funding_stmt                | 4.47      | 24.77     | 7.57      | 747     |
| reference_citation          | 72.01     | 70.94     | 71.47     | 147470  |
| reference_figure            | 70.21     | 77.09     | 73.49     | 47984   |
| reference_table             | 45.59     | 86.45     | 59.7      | 5957    |
| section_title               | 71.51     | 69.9      | 70.69     | 32398   |
| table_title                 | 7.47      | 2.7       | 3.97      | 3925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **65.65** | **64.98** | **65.31** | 263123  |
| all fields (macro avg.)     | 42.45     | 46.43     | 42.87     | 263123  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 55.32     | 46.64     | 50.61     | 446     |
| conflict_stmt               | 84.93     | 71.26     | 77.5      | 609     |
| contribution_stmt           | 76.22     | 72.09     | 74.09     | 609     |
| figure_title                | 68.26     | 37.19     | 48.14     | 22978   |
| funding_stmt                | 4.76      | 26.37     | 8.06      | 747     |
| reference_citation          | 84.3      | 83.05     | 83.67     | 147470  |
| reference_figure            | 70.85     | 77.79     | 74.16     | 47984   |
| reference_table             | 46.02     | 87.26     | 60.26     | 5957    |
| section_title               | 77.06     | 75.32     | 76.18     | 32398   |
| table_title                 | 82.73     | 29.91     | 43.94     | 3925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **76.94** | **76.16** | **76.55** | 263123  |
| all fields (macro avg.)     | 65.04     | 60.69     | 59.66     | 263123  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 86.44     | 84.3      | 85.36     | 446     |
| conflict_stmt               | 96.78     | 83.91     | 89.89     | 609     |
| contribution_stmt           | 94.74     | 94.58     | 94.66     | 609     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.13** | **87.92** | **90.45** | 1664    |
| all fields (macro avg.)     | 92.65     | 87.6      | 89.97     | 1664    |

Evaluation metrics produced in 1627.614 seconds

