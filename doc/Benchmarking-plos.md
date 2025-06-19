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
| abstract                    | 13.41     | 13.44     | 13.42     | 960     |
| authors                     | 99.07     | 99.07     | 99.07     | 969     |
| first_author                | 99.28     | 99.28     | 99.28     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 95.87     | 95.2      | 95.53     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **77.14** | **77.04** | **77.09** | 3898    |
| all fields (macro avg.)     | 76.91     | 76.75     | 76.83     | 3898    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| abstract                    | 50.62     | 50.73     | 50.68    | 960     |
| authors                     | 99.07     | 99.07     | 99.07    | 969     |
| first_author                | 99.28     | 99.28     | 99.28    | 969     |
| keywords                    | 0         | 0         | 0        | 0       |
| title                       | 99.5      | 98.8      | 99.15    | 1000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **87.26** | **87.15** | **87.2** | 3898    |
| all fields (macro avg.)     | 87.12     | 86.97     | 87.04    | 3898    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 76.3      | 76.46     | 76.38     | 960     |
| authors                     | 99.48     | 99.48     | 99.48     | 969     |
| first_author                | 99.38     | 99.38     | 99.38     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 99.7      | 99        | 99.35     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.78** | **93.66** | **93.72** | 3898    |
| all fields (macro avg.)     | 93.72     | 93.58     | 93.65     | 3898    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| abstract                    | 66.53     | 66.67    | 66.6      | 960     |
| authors                     | 99.38     | 99.38    | 99.38     | 969     |
| first_author                | 99.28     | 99.28    | 99.28     | 969     |
| keywords                    | 0         | 0        | 0         | 0       |
| title                       | 99.7      | 99       | 99.35     | 1000    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **91.32** | **91.2** | **91.26** | 3898    |
| all fields (macro avg.)     | 91.22     | 91.08    | 91.15     | 3898    |

#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	143 (strict) 
Total correct instances: 	492 (soft) 
Total correct instances: 	728 (Levenshtein) 
Total correct instances: 	643 (ObservedRatcliffObershelp) 

Instance-level recall:	14.3	(strict) 
Instance-level recall:	49.2	(soft) 
Instance-level recall:	72.8	(Levenshtein) 
Instance-level recall:	64.3	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.19     | 78.43     | 79.79     | 44770   |
| date                        | 84.64     | 81.25     | 82.91     | 45457   |
| first_author                | 91.49     | 88.36     | 89.9      | 44770   |
| inTitle                     | 81.69     | 83.58     | 82.63     | 42795   |
| issue                       | 93.63     | 92.72     | 93.17     | 18983   |
| page                        | 93.71     | 77.57     | 84.88     | 40844   |
| title                       | 59.99     | 60.49     | 60.24     | 43101   |
| volume                      | 95.91     | 96.12     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **84.25** | **81.46** | **82.83** | 321178  |
| all fields (macro avg.)     | 85.28     | 82.32     | 83.69     | 321178  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.5      | 78.74     | 80.1      | 44770   |
| date                        | 84.64     | 81.25     | 82.91     | 45457   |
| first_author                | 91.71     | 88.57     | 90.11     | 44770   |
| inTitle                     | 85.53     | 87.51     | 86.51     | 42795   |
| issue                       | 93.63     | 92.72     | 93.17     | 18983   |
| page                        | 93.71     | 77.57     | 84.88     | 40844   |
| title                       | 91.99     | 92.75     | 92.37     | 43101   |
| volume                      | 95.91     | 96.12     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.34** | **86.38** | **87.84** | 321178  |
| all fields (macro avg.)     | 89.83     | 86.91     | 88.26     | 321178  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 90.66     | 87.59     | 89.1      | 44770   |
| date                        | 84.64     | 81.25     | 82.91     | 45457   |
| first_author                | 92.25     | 89.09     | 90.64     | 44770   |
| inTitle                     | 86.48     | 88.48     | 87.47     | 42795   |
| issue                       | 93.63     | 92.72     | 93.17     | 18983   |
| page                        | 93.71     | 77.57     | 84.88     | 40844   |
| title                       | 94.59     | 95.38     | 94.98     | 43101   |
| volume                      | 95.91     | 96.12     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.19** | **88.17** | **89.66** | 321178  |
| all fields (macro avg.)     | 91.48     | 88.52     | 89.9      | 321178  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 84.95     | 82.07     | 83.49     | 44770   |
| date                        | 84.64     | 81.25     | 82.91     | 45457   |
| first_author                | 91.49     | 88.36     | 89.9      | 44770   |
| inTitle                     | 85.18     | 87.15     | 86.15     | 42795   |
| issue                       | 93.63     | 92.72     | 93.17     | 18983   |
| page                        | 93.71     | 77.57     | 84.88     | 40844   |
| title                       | 93.98     | 94.76     | 94.37     | 43101   |
| volume                      | 95.91     | 96.12     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.02** | **87.04** | **88.51** | 321178  |
| all fields (macro avg.)     | 90.44     | 87.5      | 88.86     | 321178  |

#### Instance-level results

```
Total expected instances: 		48449
Total extracted instances: 		48228
Total correct instances: 		13494 (strict) 
Total correct instances: 		22259 (soft) 
Total correct instances: 		24908 (Levenshtein) 
Total correct instances: 		23260 (RatcliffObershelp) 

Instance-level precision:	27.98 (strict) 
Instance-level precision:	46.15 (soft) 
Instance-level precision:	51.65 (Levenshtein) 
Instance-level precision:	48.23 (RatcliffObershelp) 

Instance-level recall:	27.85	(strict) 
Instance-level recall:	45.94	(soft) 
Instance-level recall:	51.41	(Levenshtein) 
Instance-level recall:	48.01	(RatcliffObershelp) 

Instance-level f-score:	27.92 (strict) 
Instance-level f-score:	46.05 (soft) 
Instance-level f-score:	51.53 (Levenshtein) 
Instance-level f-score:	48.12 (RatcliffObershelp) 

Matching 1 :	35375

Matching 2 :	1262

Matching 3 :	3266

Matching 4 :	1798

Total matches :	41701
```

#### Citation context resolution

```

Total expected references: 	 48449 - 48.45 references per article
Total predicted references: 	 48228 - 48.23 references per article

Total expected citation contexts: 	 69755 - 69.75 citation contexts per article
Total predicted citation contexts: 	 73175 - 73.17 citation contexts per article

Total correct predicted citation contexts: 	 56596 - 56.6 citation contexts per article
Total wrong predicted citation contexts: 	 16579 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 77.34
Recall citation contexts: 	 81.14
fscore citation contexts: 	 79.19
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1     | support |
|-----------------------------|-----------|-----------|--------|---------|
| availability_stmt           | 53.93     | 51.99     | 52.94  | 779     |
| figure_title                | 1.16      | 0.55      | 0.75   | 8943    |
| funding_stmt                | 5.51      | 30.52     | 9.33   | 1507    |
| reference_citation          | 87.47     | 94.22     | 90.72  | 69741   |
| reference_figure            | 74.36     | 70.26     | 72.25  | 11010   |
| reference_table             | 85.79     | 93.29     | 89.39  | 5159    |
| section_title               | 72.82     | 65.95     | 69.21  | 17540   |
| table_title                 | 0         | 0         | 0      | 6092    |
|                             |           |           |        |         |
| **all fields (micro avg.)** | **74.86** | **75.13** | **75** | 120771  |
| all fields (macro avg.)     | 47.63     | 50.85     | 48.07  | 120771  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 79.49     | 76.64     | 78.04     | 779     |
| figure_title                | 92.28     | 43.45     | 59.08     | 8943    |
| funding_stmt                | 7.05      | 39.08     | 11.95     | 1507    |
| reference_citation          | 87.48     | 94.23     | 90.73     | 69741   |
| reference_figure            | 74.66     | 70.55     | 72.55     | 11010   |
| reference_table             | 85.95     | 93.47     | 89.55     | 5159    |
| section_title               | 78.14     | 70.76     | 74.26     | 17540   |
| table_title                 | 51.89     | 7.42      | 12.98     | 6092    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **79.4**  | **79.69** | **79.55** | 120771  |
| all fields (macro avg.)     | 69.62     | 61.95     | 61.14     | 120771  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 99.87     | 96.41     | 98.11     | 779     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.87** | **96.41** | **98.11** | 779     |
| all fields (macro avg.)     | 99.87     | 96.41     | 98.11     | 779     |

Evaluation metrics produced in 801.87 seconds

