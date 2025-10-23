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
| abstract                    | 12.74     | 13.12     | 12.93     | 960     |
| authors                     | 98.55     | 98.45     | 98.5      | 969     |
| first_author                | 99.28     | 99.17     | 99.23     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 95.08     | 94.7      | 94.89     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **76.21** | **76.65** | **76.43** | 3898    |
| all fields (macro avg.)     | 76.41     | 76.36     | 76.39     | 3898    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 48.48     | 49.9      | 49.18     | 960     |
| authors                     | 98.55     | 98.45     | 98.5      | 969     |
| first_author                | 99.28     | 99.17     | 99.23     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 98.69     | 98.3      | 98.5      | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.15** | **86.63** | **86.39** | 3898    |
| all fields (macro avg.)     | 86.25     | 86.46     | 86.35     | 3898    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 71.86     | 73.96     | 72.9      | 960     |
| authors                     | 98.97     | 98.86     | 98.92     | 969     |
| first_author                | 99.38     | 99.28     | 99.33     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 99.1      | 98.7      | 98.9      | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.27** | **92.79** | **92.53** | 3898    |
| all fields (macro avg.)     | 92.33     | 92.7      | 92.51     | 3898    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 63.46     | 65.31     | 64.37     | 960     |
| authors                     | 98.76     | 98.66     | 98.71     | 969     |
| first_author                | 99.28     | 99.17     | 99.23     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 98.8      | 98.4      | 98.6      | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90**    | **90.51** | **90.25** | 3898    |
| all fields (macro avg.)     | 90.07     | 90.39     | 90.23     | 3898    |

#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	120 (strict) 
Total correct instances: 	459 (soft) 
Total correct instances: 	679 (Levenshtein) 
Total correct instances: 	603 (ObservedRatcliffObershelp) 

Instance-level recall:	12	(strict) 
Instance-level recall:	45.9	(soft) 
Instance-level recall:	67.9	(Levenshtein) 
Instance-level recall:	60.3	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.18     | 78.42     | 79.78     | 44770   |
| date                        | 84.62     | 81.24     | 82.89     | 45457   |
| first_author                | 91.49     | 88.35     | 89.89     | 44770   |
| inTitle                     | 81.69     | 83.57     | 82.62     | 42795   |
| issue                       | 93.61     | 92.68     | 93.15     | 18983   |
| page                        | 93.71     | 77.57     | 84.88     | 40844   |
| title                       | 59.97     | 60.46     | 60.22     | 43101   |
| volume                      | 95.9      | 96.1      | 96        | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **84.24** | **81.44** | **82.82** | 321178  |
| all fields (macro avg.)     | 85.27     | 82.3      | 83.68     | 321178  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.5      | 78.73     | 80.09     | 44770   |
| date                        | 84.62     | 81.24     | 82.89     | 45457   |
| first_author                | 91.71     | 88.56     | 90.11     | 44770   |
| inTitle                     | 85.52     | 87.49     | 86.5      | 42795   |
| issue                       | 93.61     | 92.68     | 93.15     | 18983   |
| page                        | 93.71     | 77.57     | 84.88     | 40844   |
| title                       | 91.98     | 92.74     | 92.36     | 43101   |
| volume                      | 95.9      | 96.1      | 96        | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.34** | **86.37** | **87.83** | 321178  |
| all fields (macro avg.)     | 89.82     | 86.89     | 88.25     | 321178  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 90.66     | 87.57     | 89.09     | 44770   |
| date                        | 84.62     | 81.24     | 82.89     | 45457   |
| first_author                | 92.25     | 89.08     | 90.64     | 44770   |
| inTitle                     | 86.47     | 88.46     | 87.45     | 42795   |
| issue                       | 93.61     | 92.68     | 93.15     | 18983   |
| page                        | 93.71     | 77.57     | 84.88     | 40844   |
| title                       | 94.58     | 95.37     | 94.97     | 43101   |
| volume                      | 95.9      | 96.1      | 96        | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.18** | **88.16** | **89.64** | 321178  |
| all fields (macro avg.)     | 91.47     | 88.51     | 89.88     | 321178  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 84.94     | 82.06     | 83.48    | 44770   |
| date                        | 84.62     | 81.24     | 82.89    | 45457   |
| first_author                | 91.49     | 88.35     | 89.89    | 44770   |
| inTitle                     | 85.17     | 87.14     | 86.15    | 42795   |
| issue                       | 93.61     | 92.68     | 93.15    | 18983   |
| page                        | 93.71     | 77.57     | 84.88    | 40844   |
| title                       | 93.97     | 94.75     | 94.36    | 43101   |
| volume                      | 95.9      | 96.1      | 96       | 40458   |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **90.02** | **87.03** | **88.5** | 321178  |
| all fields (macro avg.)     | 90.43     | 87.49     | 88.85    | 321178  |

#### Instance-level results

```
Total expected instances: 		48449
Total extracted instances: 		48215
Total correct instances: 		13495 (strict) 
Total correct instances: 		22265 (soft) 
Total correct instances: 		24914 (Levenshtein) 
Total correct instances: 		23267 (RatcliffObershelp) 

Instance-level precision:	27.99 (strict) 
Instance-level precision:	46.18 (soft) 
Instance-level precision:	51.67 (Levenshtein) 
Instance-level precision:	48.26 (RatcliffObershelp) 

Instance-level recall:	27.85	(strict) 
Instance-level recall:	45.96	(soft) 
Instance-level recall:	51.42	(Levenshtein) 
Instance-level recall:	48.02	(RatcliffObershelp) 

Instance-level f-score:	27.92 (strict) 
Instance-level f-score:	46.07 (soft) 
Instance-level f-score:	51.55 (Levenshtein) 
Instance-level f-score:	48.14 (RatcliffObershelp) 

Matching 1 :	35369

Matching 2 :	1259

Matching 3 :	3266

Matching 4 :	1800

Total matches :	41694
```

#### Citation context resolution

```

Total expected references: 	 48449 - 48.45 references per article
Total predicted references: 	 48215 - 48.22 references per article

Total expected citation contexts: 	 69755 - 69.75 citation contexts per article
Total predicted citation contexts: 	 73208 - 73.21 citation contexts per article

Total correct predicted citation contexts: 	 56735 - 56.73 citation contexts per article
Total wrong predicted citation contexts: 	 16473 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 77.5
Recall citation contexts: 	 81.33
fscore citation contexts: 	 79.37
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
| availability_stmt           | 43.5      | 42.49     | 42.99     | 779     |
| conflict_stmt               | 70.86     | 53.85     | 61.19     | 962     |
| figure_title                | 0.2       | 0.1       | 0.13      | 8943    |
| funding_stmt                | 4.96      | 28.47     | 8.45      | 1507    |
| reference_citation          | 87.93     | 94.39     | 91.05     | 69741   |
| reference_figure            | 74.2      | 85.78     | 79.57     | 11010   |
| reference_table             | 70.26     | 94.28     | 80.52     | 5159    |
| section_title               | 72.72     | 66.21     | 69.31     | 17540   |
| table_title                 | 0         | 0         | 0         | 6092    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **73.91** | **76.43** | **75.15** | 121733  |
| all fields (macro avg.)     | 47.18     | 51.73     | 48.13     | 121733  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| availability_stmt           | 65.44     | 63.93    | 64.68     | 779     |
| conflict_stmt               | 72.37     | 54.99    | 62.49     | 962     |
| figure_title                | 92.75     | 45.75    | 61.27     | 8943    |
| funding_stmt                | 6.52      | 37.36    | 11.1      | 1507    |
| reference_citation          | 87.94     | 94.39    | 91.05     | 69741   |
| reference_figure            | 74.44     | 86.06    | 79.83     | 11010   |
| reference_table             | 70.42     | 94.5     | 80.7      | 5159    |
| section_title               | 78.47     | 71.44    | 74.79     | 17540   |
| table_title                 | 53.39     | 7.5      | 13.15     | 6092    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **78.53** | **81.2** | **79.84** | 121733  |
| all fields (macro avg.)     | 66.86     | 61.77    | 59.9      | 121733  |

**Document-level ratio results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| availability_stmt           | 99.61     | 97.69    | 98.64     | 779     |
| conflict_stmt               | 100       | 75.99    | 86.36     | 962     |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **99.8**  | **85.7** | **92.21** | 1741    |
| all fields (macro avg.)     | 99.8      | 86.84    | 92.5      | 1741    |

Evaluation metrics produced in 839.617 seconds


