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
| abstract                    | 2.36      | 2.31      | 2.33      | 1990    |
| authors                     | 85.07     | 84.39     | 84.73     | 1999    |
| first_author                | 96.92     | 96.24     | 96.58     | 1997    |
| keywords                    | 58.78     | 59.83     | 59.3      | 839     |
| title                       | 77.39     | 76.65     | 77.02     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **65**    | **64.48** | **64.74** | 8825    |
| all fields (macro avg.)     | 64.1      | 63.89     | 63.99     | 8825    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 60.27     | 59.15     | 59.7      | 1990    |
| authors                     | 85.48     | 84.79     | 85.13     | 1999    |
| first_author                | 97.13     | 96.44     | 96.78     | 1997    |
| keywords                    | 63.93     | 65.08     | 64.5      | 839     |
| title                       | 79.45     | 78.7      | 79.08     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **79.03** | **78.39** | **78.71** | 8825    |
| all fields (macro avg.)     | 77.25     | 76.83     | 77.04     | 8825    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 80.49     | 78.99     | 79.74     | 1990    |
| authors                     | 92.59     | 91.85     | 92.21     | 1999    |
| first_author                | 97.38     | 96.7      | 97.04     | 1997    |
| keywords                    | 79.74     | 81.17     | 80.45     | 839     |
| title                       | 91.97     | 91.1      | 91.53     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.58** | **88.86** | **89.22** | 8825    |
| all fields (macro avg.)     | 88.43     | 87.96     | 88.19     | 8825    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 77.42     | 75.98     | 76.69     | 1990    |
| authors                     | 88.65     | 87.94     | 88.3      | 1999    |
| first_author                | 96.92     | 96.24     | 96.58     | 1997    |
| keywords                    | 71.43     | 72.71     | 72.06     | 839     |
| title                       | 87.63     | 86.8      | 87.21     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.11** | **85.42** | **85.76** | 8825    |
| all fields (macro avg.)     | 84.41     | 83.93     | 84.17     | 8825    |

#### Instance-level results

```
Total expected instances: 	2000
Total correct instances: 	40 (strict) 
Total correct instances: 	737 (soft) 
Total correct instances: 	1244 (Levenshtein) 
Total correct instances: 	1078 (ObservedRatcliffObershelp) 

Instance-level recall:	2	(strict) 
Instance-level recall:	36.85	(soft) 
Instance-level recall:	62.2	(Levenshtein) 
Instance-level recall:	53.9	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.2      | 83.24     | 85.65     | 97183   |
| date                        | 91.69     | 86.28     | 88.91     | 97630   |
| doi                         | 70.78     | 83.91     | 76.79     | 16894   |
| first_author                | 95.09     | 89.67     | 92.3      | 97183   |
| inTitle                     | 82.9      | 79.43     | 81.13     | 96430   |
| issue                       | 94.35     | 91.99     | 93.16     | 30312   |
| page                        | 95.01     | 78.36     | 85.89     | 88597   |
| pmcid                       | 66.38     | 86.12     | 74.97     | 807     |
| pmid                        | 69.87     | 85        | 76.7      | 2093    |
| title                       | 84.95     | 83.6      | 84.27     | 92463   |
| volume                      | 96.27     | 95.23     | 95.74     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.88** | **85.35** | **87.55** | 707301  |
| all fields (macro avg.)     | 85.05     | 85.71     | 85.05     | 707301  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.36     | 84.33     | 86.77     | 97183   |
| date                        | 91.69     | 86.28     | 88.91     | 97630   |
| doi                         | 75.25     | 89.21     | 81.64     | 16894   |
| first_author                | 95.52     | 90.07     | 92.71     | 97183   |
| inTitle                     | 92.35     | 88.48     | 90.37     | 96430   |
| issue                       | 94.35     | 91.99     | 93.16     | 30312   |
| page                        | 95.01     | 78.36     | 85.89     | 88597   |
| pmcid                       | 75.64     | 98.14     | 85.44     | 807     |
| pmid                        | 74.27     | 90.35     | 81.53     | 2093    |
| title                       | 93.28     | 91.81     | 92.54     | 92463   |
| volume                      | 96.27     | 95.23     | 95.74     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.69** | **88.01** | **90.29** | 707301  |
| all fields (macro avg.)     | 88.45     | 89.48     | 88.61     | 707301  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.61     | 89.29     | 91.88     | 97183   |
| date                        | 91.69     | 86.28     | 88.91     | 97630   |
| doi                         | 77.51     | 91.88     | 84.09     | 16894   |
| first_author                | 95.67     | 90.21     | 92.86     | 97183   |
| inTitle                     | 93.34     | 89.43     | 91.34     | 96430   |
| issue                       | 94.35     | 91.99     | 93.16     | 30312   |
| page                        | 95.01     | 78.36     | 85.89     | 88597   |
| pmcid                       | 75.64     | 98.14     | 85.44     | 807     |
| pmid                        | 74.27     | 90.35     | 81.53     | 2093    |
| title                       | 96.08     | 94.57     | 95.32     | 92463   |
| volume                      | 96.27     | 95.23     | 95.74     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.01** | **89.27** | **91.58** | 707301  |
| all fields (macro avg.)     | 89.5      | 90.52     | 89.65     | 707301  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 91.57     | 86.42     | 88.92     | 97183   |
| date                        | 91.69     | 86.28     | 88.91     | 97630   |
| doi                         | 75.95     | 90.04     | 82.4      | 16894   |
| first_author                | 95.14     | 89.71     | 92.35     | 97183   |
| inTitle                     | 91.12     | 87.31     | 89.17     | 96430   |
| issue                       | 94.35     | 91.99     | 93.16     | 30312   |
| page                        | 95.01     | 78.36     | 85.89     | 88597   |
| pmcid                       | 66.38     | 86.12     | 74.97     | 807     |
| pmid                        | 69.87     | 85        | 76.7      | 2093    |
| title                       | 95.43     | 93.92     | 94.67     | 92463   |
| volume                      | 96.27     | 95.23     | 95.74     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.05** | **88.36** | **90.64** | 707301  |
| all fields (macro avg.)     | 87.53     | 88.22     | 87.53     | 707301  |

#### Instance-level results

```
Total expected instances: 		98799
Total extracted instances: 		98065
Total correct instances: 		43778 (strict) 
Total correct instances: 		54779 (soft) 
Total correct instances: 		58956 (Levenshtein) 
Total correct instances: 		55678 (RatcliffObershelp) 

Instance-level precision:	44.64 (strict) 
Instance-level precision:	55.86 (soft) 
Instance-level precision:	60.12 (Levenshtein) 
Instance-level precision:	56.78 (RatcliffObershelp) 

Instance-level recall:	44.31	(strict) 
Instance-level recall:	55.44	(soft) 
Instance-level recall:	59.67	(Levenshtein) 
Instance-level recall:	56.35	(RatcliffObershelp) 

Instance-level f-score:	44.48 (strict) 
Instance-level f-score:	55.65 (soft) 
Instance-level f-score:	59.9 (Levenshtein) 
Instance-level f-score:	56.56 (RatcliffObershelp) 

Matching 1 :	79295

Matching 2 :	4427

Matching 3 :	4373

Matching 4 :	2086

Total matches :	90181
```

#### Citation context resolution

```

Total expected references: 	 98797 - 49.4 references per article
Total predicted references: 	 98065 - 49.03 references per article

Total expected citation contexts: 	 142862 - 71.43 citation contexts per article
Total predicted citation contexts: 	 135569 - 67.78 citation contexts per article

Total correct predicted citation contexts: 	 116580 - 58.29 citation contexts per article
Total wrong predicted citation contexts: 	 18989 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 85.99
Recall citation contexts: 	 81.6
fscore citation contexts: 	 83.74
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| availability_stmt           | 29.82     | 26.68    | 28.17     | 446     |
| figure_title                | 4.26      | 2.02     | 2.74      | 22978   |
| funding_stmt                | 3.63      | 24.97    | 6.33      | 745     |
| reference_citation          | 71.03     | 71.22    | 71.12     | 147470  |
| reference_figure            | 70.41     | 67.81    | 69.09     | 47984   |
| reference_table             | 48.12     | 83.35    | 61.01     | 5957    |
| section_title               | 72.75     | 69.96    | 71.33     | 32398   |
| table_title                 | 6.72      | 2.09     | 3.19      | 3925    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **65.58** | **63.4** | **64.47** | 261903  |
| all fields (macro avg.)     | 38.34     | 43.51    | 39.12     | 261903  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 50.13     | 44.84     | 47.34     | 446     |
| figure_title                | 69.85     | 33.16     | 44.97     | 22978   |
| funding_stmt                | 3.82      | 26.31     | 6.67      | 745     |
| reference_citation          | 83.06     | 83.28     | 83.17     | 147470  |
| reference_figure            | 71.03     | 68.41     | 69.7      | 47984   |
| reference_table             | 48.58     | 84.15     | 61.6      | 5957    |
| section_title               | 76.68     | 73.75     | 75.18     | 32398   |
| table_title                 | 84.77     | 26.37     | 40.23     | 3925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **76.46** | **73.92** | **75.17** | 261903  |
| all fields (macro avg.)     | 60.99     | 55.03     | 53.61     | 261903  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 83.47     | 89.46     | 86.36     | 446     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **83.47** | **89.46** | **86.36** | 446     |
| all fields (macro avg.)     | 83.47     | 89.46     | 86.36     | 446     |

Evaluation metrics produced in 888.703 seconds
``



