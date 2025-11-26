# Benchmarking PLOS

## General

This is the end-to-end benchmarking result for GROBID version **0.8.2** against the `PLOS` test set, see
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

Evaluation on 1000 PDF preprints out of 1000 (no failure).

Runtime for processing 1000 PDF: **999** seconds, (0.99 seconds per PDF) on Ubuntu 22.04, 16 CPU (32 threads), 128GB RAM
and with a GeForce GTX 1080 Ti GPU.

Note: with CRF only models runtime is 304s (0.30 seconds per PDF) with 4 CPU, 8 threads.

## Header metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 13.16     | 13.44     | 13.3      | 960     |
| authors                     | 99.07     | 98.97     | 99.02     | 969     |
| first_author                | 99.28     | 99.17     | 99.23     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 95.99     | 95.8      | 95.9      | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **76.83** | **77.14** | **76.98** | 3898    |
| all fields (macro avg.)     | 76.88     | 76.84     | 76.86     | 3898    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| abstract                    | 49.49     | 50.52    | 50        | 960     |
| authors                     | 99.07     | 98.97    | 99.02     | 969     |
| first_author                | 99.28     | 99.17    | 99.23     | 969     |
| keywords                    | 0         | 0        | 0         | 0       |
| title                       | 99.6      | 99.4     | 99.5      | 1000    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **86.84** | **87.2** | **87.02** | 3898    |
| all fields (macro avg.)     | 86.86     | 87.02    | 86.94     | 3898    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 74.59     | 76.15     | 75.36     | 960     |
| authors                     | 99.48     | 99.38     | 99.43     | 969     |
| first_author                | 99.38     | 99.28     | 99.33     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 99.7      | 99.5      | 99.6      | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.28** | **93.66** | **93.47** | 3898    |
| all fields (macro avg.)     | 93.29     | 93.58     | 93.43     | 3898    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| abstract                    | 64.69     | 66.04    | 65.36     | 960     |
| authors                     | 99.28     | 99.17    | 99.23     | 969     |
| first_author                | 99.28     | 99.17    | 99.23     | 969     |
| keywords                    | 0         | 0        | 0         | 0       |
| title                       | 99.7      | 99.5     | 99.6      | 1000    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **90.73** | **91.1** | **90.91** | 3898    |
| all fields (macro avg.)     | 90.74     | 90.97    | 90.85     | 3898    |

#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	129 (strict) 
Total correct instances: 	475 (soft) 
Total correct instances: 	703 (Levenshtein) 
Total correct instances: 	619 (ObservedRatcliffObershelp) 

Instance-level recall:	12.9	(strict) 
Instance-level recall:	47.5	(soft) 
Instance-level recall:	70.3	(Levenshtein) 
Instance-level recall:	61.9	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 80.97     | 74.65     | 77.68     | 44770   |
| date                        | 83.62     | 78.42     | 80.94     | 45457   |
| first_author                | 91.11     | 83.96     | 87.39     | 44770   |
| inTitle                     | 78.89     | 79.35     | 79.12     | 42795   |
| issue                       | 93.42     | 85.78     | 89.44     | 18983   |
| page                        | 89.88     | 73.26     | 80.72     | 40844   |
| title                       | 58.47     | 56.02     | 57.22     | 43101   |
| volume                      | 92.49     | 91.55     | 92.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **82.59** | **77.22** | **79.81** | 321178  |
| all fields (macro avg.)     | 83.61     | 77.87     | 80.57     | 321178  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 81.28     | 74.93     | 77.97    | 44770   |
| date                        | 83.62     | 78.42     | 80.94    | 45457   |
| first_author                | 91.32     | 84.15     | 87.59    | 44770   |
| inTitle                     | 82.23     | 82.71     | 82.47    | 42795   |
| issue                       | 93.42     | 85.78     | 89.44    | 18983   |
| page                        | 89.88     | 73.26     | 80.72    | 40844   |
| title                       | 89.73     | 85.97     | 87.81    | 43101   |
| volume                      | 92.49     | 91.55     | 92.02    | 40458   |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **87.44** | **81.75** | **84.5** | 321178  |
| all fields (macro avg.)     | 88        | 82.1      | 84.87    | 321178  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 90.12     | 83.08     | 86.46     | 44770   |
| date                        | 83.62     | 78.42     | 80.94     | 45457   |
| first_author                | 91.83     | 84.62     | 88.08     | 44770   |
| inTitle                     | 82.91     | 83.4      | 83.16     | 42795   |
| issue                       | 93.42     | 85.78     | 89.44     | 18983   |
| page                        | 89.88     | 73.26     | 80.72     | 40844   |
| title                       | 93.43     | 89.51     | 91.43     | 43101   |
| volume                      | 92.49     | 91.55     | 92.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.33** | **83.52** | **86.33** | 321178  |
| all fields (macro avg.)     | 89.71     | 83.7      | 86.53     | 321178  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| authors                     | 84.62     | 78.01    | 81.18     | 44770   |
| date                        | 83.62     | 78.42    | 80.94     | 45457   |
| first_author                | 91.11     | 83.96    | 87.39     | 44770   |
| inTitle                     | 81.83     | 82.32    | 82.08     | 42795   |
| issue                       | 93.42     | 85.78    | 89.44     | 18983   |
| page                        | 89.88     | 73.26    | 80.72     | 40844   |
| title                       | 92.07     | 88.21    | 90.1      | 43101   |
| volume                      | 92.49     | 91.55    | 92.02     | 40458   |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **88.14** | **82.4** | **85.17** | 321178  |
| all fields (macro avg.)     | 88.63     | 82.69    | 85.48     | 321178  |

#### Instance-level results

```
Total expected instances: 		48449
Total extracted instances: 		48595
Total correct instances: 		12628 (strict) 
Total correct instances: 		20908 (soft) 
Total correct instances: 		23332 (Levenshtein) 
Total correct instances: 		21857 (RatcliffObershelp) 

Instance-level precision:	25.99 (strict) 
Instance-level precision:	43.03 (soft) 
Instance-level precision:	48.01 (Levenshtein) 
Instance-level precision:	44.98 (RatcliffObershelp) 

Instance-level recall:	26.06	(strict) 
Instance-level recall:	43.15	(soft) 
Instance-level recall:	48.16	(Levenshtein) 
Instance-level recall:	45.11	(RatcliffObershelp) 

Instance-level f-score:	26.03 (strict) 
Instance-level f-score:	43.09 (soft) 
Instance-level f-score:	48.09 (Levenshtein) 
Instance-level f-score:	45.05 (RatcliffObershelp) 

Matching 1 :	33438

Matching 2 :	1690

Matching 3 :	2960

Matching 4 :	1504

Total matches :	39592
```

#### Citation context resolution

```

Total expected references: 	 48449 - 48.45 references per article
Total predicted references: 	 48595 - 48.59 references per article

Total expected citation contexts: 	 69755 - 69.75 citation contexts per article
Total predicted citation contexts: 	 74511 - 74.51 citation contexts per article

Total correct predicted citation contexts: 	 54458 - 54.46 citation contexts per article
Total wrong predicted citation contexts: 	 20053 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 73.09
Recall citation contexts: 	 78.07
fscore citation contexts: 	 75.5
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 52.64     | 48.65     | 50.57     | 779     |
| conflict_stmt               | 93.8      | 67.67     | 78.62     | 962     |
| figure_title                | 0.18      | 0.09      | 0.12      | 8943    |
| funding_stmt                | 6.31      | 31.79     | 10.53     | 1507    |
| reference_citation          | 87.93     | 95.27     | 91.45     | 69741   |
| reference_figure            | 74.16     | 85.82     | 79.56     | 11010   |
| reference_table             | 70.25     | 94.3      | 80.52     | 5159    |
| section_title               | 72.66     | 66.24     | 69.3      | 17540   |
| table_title                 | 0         | 0         | 0         | 6092    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **74.82** | **77.13** | **75.96** | 121733  |
| all fields (macro avg.)     | 50.88     | 54.43     | 51.19     | 121733  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 79.86     | 73.81     | 76.72     | 779     |
| conflict_stmt               | 96.25     | 69.44     | 80.68     | 962     |
| figure_title                | 92.87     | 45.76     | 61.31     | 8943    |
| funding_stmt                | 7.97      | 40.15     | 13.29     | 1507    |
| reference_citation          | 87.93     | 95.28     | 91.46     | 69741   |
| reference_figure            | 74.4      | 86.1      | 79.82     | 11010   |
| reference_table             | 70.41     | 94.51     | 80.7      | 5159    |
| section_title               | 78.44     | 71.51     | 74.81     | 17540   |
| table_title                 | 53.22     | 7.47      | 13.1      | 6092    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **79.48** | **81.93** | **80.69** | 121733  |
| all fields (macro avg.)     | 71.26     | 64.89     | 63.54     | 121733  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 99.31     | 92.43     | 95.74     | 779     |
| conflict_stmt               | 100       | 72.14     | 83.82     | 962     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.65** | **81.22** | **89.49** | 1741    |
| all fields (macro avg.)     | 99.66     | 82.28     | 89.78     | 1741    |

Evaluation metrics produced in 833.225 seconds



