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
| abstract                    | 2         | 1.96      | 1.98      | 1990    |
| authors                     | 83.64     | 82.84     | 83.24     | 1999    |
| first_author                | 95.81     | 94.99     | 95.4      | 1997    |
| keywords                    | 47.64     | 46.96     | 47.3      | 839     |
| title                       | 75.1      | 74.35     | 74.72     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **62.81** | **62.02** | **62.41** | 8825    |
| all fields (macro avg.)     | 60.84     | 60.22     | 60.53     | 8825    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 55.06     | 53.87     | 54.46     | 1990    |
| authors                     | 84.04     | 83.24     | 83.64     | 1999    |
| first_author                | 96.01     | 95.19     | 95.6      | 1997    |
| keywords                    | 52.18     | 51.37     | 51.77     | 839     |
| title                       | 77.31     | 76.5      | 76.9      | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **75.73** | **74.76** | **75.25** | 8825    |
| all fields (macro avg.)     | 72.92     | 72.03     | 72.47     | 8825    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 82.02     | 80.25     | 81.13     | 1990    |
| authors                     | 91.87     | 91        | 91.43     | 1999    |
| first_author                | 96.21     | 95.39     | 95.8      | 1997    |
| keywords                    | 72.88     | 71.75     | 72.31     | 839     |
| title                       | 90.1      | 89.15     | 89.62     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **88.45** | **87.32** | **87.88** | 8825    |
| all fields (macro avg.)     | 86.62     | 85.51     | 86.06     | 8825    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| abstract                    | 78.79     | 77.09     | 77.93    | 1990    |
| authors                     | 87.32     | 86.49     | 86.91    | 1999    |
| first_author                | 95.81     | 94.99     | 95.4     | 1997    |
| keywords                    | 60.65     | 59.71     | 60.18    | 839     |
| title                       | 85.7      | 84.8      | 85.25    | 2000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **84.45** | **83.37** | **83.9** | 8825    |
| all fields (macro avg.)     | 81.65     | 80.62     | 81.13    | 8825    |

#### Instance-level results

```
Total expected instances: 	2000
Total correct instances: 	31 (strict) 
Total correct instances: 	612 (soft) 
Total correct instances: 	1186 (Levenshtein) 
Total correct instances: 	1004 (ObservedRatcliffObershelp) 

Instance-level recall:	1.55	(strict) 
Instance-level recall:	30.6	(soft) 
Instance-level recall:	59.3	(Levenshtein) 
Instance-level recall:	50.2	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.18     | 83.22     | 85.63     | 97183   |
| date                        | 91.74     | 86.28     | 88.92     | 97630   |
| doi                         | 70.91     | 83.66     | 76.76     | 16894   |
| first_author                | 95.07     | 89.64     | 92.28     | 97183   |
| inTitle                     | 82.87     | 79.4      | 81.1      | 96430   |
| issue                       | 94.37     | 92.07     | 93.2      | 30312   |
| page                        | 95        | 78.31     | 85.85     | 88597   |
| pmcid                       | 66.44     | 86.12     | 75.01     | 807     |
| pmid                        | 69.98     | 84.85     | 76.7      | 2093    |
| title                       | 84.9      | 83.53     | 84.21     | 92463   |
| volume                      | 96.27     | 95.19     | 95.73     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.88** | **85.31** | **87.53** | 707301  |
| all fields (macro avg.)     | 85.07     | 85.66     | 85.04     | 707301  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.33     | 84.31     | 86.75     | 97183   |
| date                        | 91.74     | 86.28     | 88.92     | 97630   |
| doi                         | 75.4      | 88.95     | 81.62     | 16894   |
| first_author                | 95.5      | 90.05     | 92.69     | 97183   |
| inTitle                     | 92.37     | 88.5      | 90.39     | 96430   |
| issue                       | 94.37     | 92.07     | 93.2      | 30312   |
| page                        | 95        | 78.31     | 85.85     | 88597   |
| pmcid                       | 75.72     | 98.14     | 85.48     | 807     |
| pmid                        | 74.39     | 90.21     | 81.54     | 2093    |
| title                       | 93.25     | 91.74     | 92.49     | 92463   |
| volume                      | 96.27     | 95.19     | 95.73     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.7**  | **87.99** | **90.28** | 707301  |
| all fields (macro avg.)     | 88.49     | 89.43     | 88.61     | 707301  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.61     | 89.29     | 91.87     | 97183   |
| date                        | 91.74     | 86.28     | 88.92     | 97630   |
| doi                         | 77.66     | 91.62     | 84.07     | 16894   |
| first_author                | 95.65     | 90.19     | 92.84     | 97183   |
| inTitle                     | 93.35     | 89.44     | 91.35     | 96430   |
| issue                       | 94.37     | 92.07     | 93.2      | 30312   |
| page                        | 95        | 78.31     | 85.85     | 88597   |
| pmcid                       | 75.72     | 98.14     | 85.48     | 807     |
| pmid                        | 74.39     | 90.21     | 81.54     | 2093    |
| title                       | 96.1      | 94.55     | 95.32     | 92463   |
| volume                      | 96.27     | 95.19     | 95.73     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.03** | **89.25** | **91.57** | 707301  |
| all fields (macro avg.)     | 89.53     | 90.48     | 89.65     | 707301  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 91.55     | 86.41     | 88.91     | 97183   |
| date                        | 91.74     | 86.28     | 88.92     | 97630   |
| doi                         | 76.11     | 89.8      | 82.39     | 16894   |
| first_author                | 95.12     | 89.69     | 92.32     | 97183   |
| inTitle                     | 91.12     | 87.29     | 89.16     | 96430   |
| issue                       | 94.37     | 92.07     | 93.2      | 30312   |
| page                        | 95        | 78.31     | 85.85     | 88597   |
| pmcid                       | 66.44     | 86.12     | 75.01     | 807     |
| pmid                        | 69.98     | 84.85     | 76.7      | 2093    |
| title                       | 95.43     | 93.88     | 94.65     | 92463   |
| volume                      | 96.27     | 95.19     | 95.73     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.06** | **88.33** | **90.63** | 707301  |
| all fields (macro avg.)     | 87.56     | 88.17     | 87.53     | 707301  |

#### Instance-level results

```
Total expected instances: 		98799
Total extracted instances: 		97920
Total correct instances: 		43767 (strict) 
Total correct instances: 		54768 (soft) 
Total correct instances: 		58974 (Levenshtein) 
Total correct instances: 		55690 (RatcliffObershelp) 

Instance-level precision:	44.7 (strict) 
Instance-level precision:	55.93 (soft) 
Instance-level precision:	60.23 (Levenshtein) 
Instance-level precision:	56.87 (RatcliffObershelp) 

Instance-level recall:	44.3	(strict) 
Instance-level recall:	55.43	(soft) 
Instance-level recall:	59.69	(Levenshtein) 
Instance-level recall:	56.37	(RatcliffObershelp) 

Instance-level f-score:	44.5 (strict) 
Instance-level f-score:	55.68 (soft) 
Instance-level f-score:	59.96 (Levenshtein) 
Instance-level f-score:	56.62 (RatcliffObershelp) 

Matching 1 :	79256

Matching 2 :	4463

Matching 3 :	4346

Matching 4 :	2110

Total matches :	90175
```

#### Citation context resolution

```

Total expected references: 	 98797 - 49.4 references per article
Total predicted references: 	 97920 - 48.96 references per article

Total expected citation contexts: 	 142862 - 71.43 citation contexts per article
Total predicted citation contexts: 	 134464 - 67.23 citation contexts per article

Total correct predicted citation contexts: 	 115975 - 57.99 citation contexts per article
Total wrong predicted citation contexts: 	 18489 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 86.25
Recall citation contexts: 	 81.18
fscore citation contexts: 	 83.64
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
| availability_stmt           | 29.03     | 26.23     | 27.56     | 446     |
| conflict_stmt               | 67.96     | 57.47     | 62.28     | 609     |
| contribution_stmt           | 47.12     | 43.02     | 44.98     | 609     |
| figure_title                | 4.28      | 2.32      | 3.01      | 22978   |
| funding_stmt                | 3.66      | 23.96     | 6.34      | 747     |
| reference_citation          | 71.99     | 70.8      | 71.39     | 147470  |
| reference_figure            | 70.37     | 77.1      | 73.58     | 47984   |
| reference_table             | 45.78     | 86.7      | 59.93     | 5957    |
| section_title               | 71.29     | 69.78     | 70.53     | 32398   |
| table_title                 | 7.84      | 2.83      | 4.16      | 3925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **65.45** | **64.89** | **65.17** | 263123  |
| all fields (macro avg.)     | 41.93     | 46.02     | 42.38     | 263123  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 50.62     | 45.74     | 48.06     | 446     |
| conflict_stmt               | 83.88     | 70.94     | 76.87     | 609     |
| contribution_stmt           | 77.34     | 70.61     | 73.82     | 609     |
| figure_title                | 68.69     | 37.3      | 48.35     | 22978   |
| funding_stmt                | 3.94      | 25.84     | 6.84      | 747     |
| reference_citation          | 84.34     | 82.95     | 83.64     | 147470  |
| reference_figure            | 71.02     | 77.81     | 74.26     | 47984   |
| reference_table             | 46.19     | 87.48     | 60.46     | 5957    |
| section_title               | 76.84     | 75.21     | 76.01     | 32398   |
| table_title                 | 83.39     | 30.06     | 44.19     | 3925    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **76.77** | **76.11** | **76.43** | 263123  |
| all fields (macro avg.)     | 64.63     | 60.39     | 59.25     | 263123  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 83.09     | 90.36     | 86.57     | 446     |
| conflict_stmt               | 96.26     | 84.56     | 90.03     | 609     |
| contribution_stmt           | 94.88     | 91.3      | 93.05     | 609     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.78** | **88.58** | **90.15** | 1664    |
| all fields (macro avg.)     | 91.41     | 88.74     | 89.89     | 1664    |

Evaluation metrics produced in 1615.897 seconds

