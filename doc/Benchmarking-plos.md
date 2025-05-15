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
| abstract                    | 13.4      | 13.44     | 13.42     | 960     |
| authors                     | 98.97     | 98.97     | 98.97     | 969     |
| first_author                | 99.17     | 99.17     | 99.17     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 95.87     | 95.1      | 95.48     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **77.06** | **76.96** | **77.01** | 3898    |
| all fields (macro avg.)     | 76.85     | 76.67     | 76.76     | 3898    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 50.26     | 50.42     | 50.34     | 960     |
| authors                     | 98.97     | 98.97     | 98.97     | 969     |
| first_author                | 99.17     | 99.17     | 99.17     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 99.5      | 98.7      | 99.1      | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.11** | **86.99** | **87.05** | 3898    |
| all fields (macro avg.)     | 86.97     | 86.81     | 86.89     | 3898    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| abstract                    | 76.43     | 76.67     | 76.55    | 960     |
| authors                     | 99.38     | 99.38     | 99.38    | 969     |
| first_author                | 99.28     | 99.28     | 99.28    | 969     |
| keywords                    | 0         | 0         | 0        | 0       |
| title                       | 99.7      | 98.9      | 99.3     | 1000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **93.76** | **93.64** | **93.7** | 3898    |
| all fields (macro avg.)     | 93.7      | 93.56     | 93.63    | 3898    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| abstract                    | 66.46     | 66.67    | 66.56     | 960     |
| authors                     | 99.28     | 99.28    | 99.28     | 969     |
| first_author                | 99.17     | 99.17    | 99.17     | 969     |
| keywords                    | 0         | 0        | 0         | 0       |
| title                       | 99.6      | 98.8     | 99.2      | 1000    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **91.22** | **91.1** | **91.16** | 3898    |
| all fields (macro avg.)     | 91.13     | 90.98    | 91.05     | 3898    |

#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	141 (strict) 
Total correct instances: 	488 (soft) 
Total correct instances: 	727 (Levenshtein) 
Total correct instances: 	641 (ObservedRatcliffObershelp) 

Instance-level recall:	14.1	(strict) 
Instance-level recall:	48.8	(soft) 
Instance-level recall:	72.7	(Levenshtein) 
Instance-level recall:	64.1	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.18     | 78.43     | 79.78     | 44770   |
| date                        | 84.64     | 81.26     | 82.91     | 45457   |
| first_author                | 91.48     | 88.36     | 89.9      | 44770   |
| inTitle                     | 81.69     | 83.59     | 82.63     | 42795   |
| issue                       | 93.64     | 92.73     | 93.18     | 18983   |
| page                        | 93.71     | 77.58     | 84.88     | 40844   |
| title                       | 59.98     | 60.49     | 60.23     | 43101   |
| volume                      | 95.91     | 96.13     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **84.25** | **81.46** | **82.83** | 321178  |
| all fields (macro avg.)     | 85.28     | 82.32     | 83.69     | 321178  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.5      | 78.74     | 80.09     | 44770   |
| date                        | 84.64     | 81.26     | 82.91     | 45457   |
| first_author                | 91.7      | 88.57     | 90.11     | 44770   |
| inTitle                     | 85.53     | 87.52     | 86.51     | 42795   |
| issue                       | 93.64     | 92.73     | 93.18     | 18983   |
| page                        | 93.71     | 77.58     | 84.88     | 40844   |
| title                       | 91.99     | 92.76     | 92.37     | 43101   |
| volume                      | 95.91     | 96.13     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.34** | **86.39** | **87.84** | 321178  |
| all fields (macro avg.)     | 89.83     | 86.91     | 88.26     | 321178  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 90.66     | 87.59     | 89.1      | 44770   |
| date                        | 84.64     | 81.26     | 82.91     | 45457   |
| first_author                | 92.24     | 89.09     | 90.64     | 44770   |
| inTitle                     | 86.47     | 88.48     | 87.47     | 42795   |
| issue                       | 93.64     | 92.73     | 93.18     | 18983   |
| page                        | 93.71     | 77.58     | 84.88     | 40844   |
| title                       | 94.59     | 95.39     | 94.98     | 43101   |
| volume                      | 95.91     | 96.13     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.19** | **88.17** | **89.66** | 321178  |
| all fields (macro avg.)     | 91.48     | 88.53     | 89.9      | 321178  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 84.94     | 82.07     | 83.48     | 44770   |
| date                        | 84.64     | 81.26     | 82.91     | 45457   |
| first_author                | 91.48     | 88.36     | 89.9      | 44770   |
| inTitle                     | 85.18     | 87.16     | 86.16     | 42795   |
| issue                       | 93.64     | 92.73     | 93.18     | 18983   |
| page                        | 93.71     | 77.58     | 84.88     | 40844   |
| title                       | 93.97     | 94.76     | 94.36     | 43101   |
| volume                      | 95.91     | 96.13     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.02** | **87.04** | **88.51** | 321178  |
| all fields (macro avg.)     | 90.43     | 87.5      | 88.86     | 321178  |

#### Instance-level results

```
Total expected instances: 		48449
Total extracted instances: 		48242
Total correct instances: 		13495 (strict) 
Total correct instances: 		22264 (soft) 
Total correct instances: 		24908 (Levenshtein) 
Total correct instances: 		23262 (RatcliffObershelp) 

Instance-level precision:	27.97 (strict) 
Instance-level precision:	46.15 (soft) 
Instance-level precision:	51.63 (Levenshtein) 
Instance-level precision:	48.22 (RatcliffObershelp) 

Instance-level recall:	27.85	(strict) 
Instance-level recall:	45.95	(soft) 
Instance-level recall:	51.41	(Levenshtein) 
Instance-level recall:	48.01	(RatcliffObershelp) 

Instance-level f-score:	27.91 (strict) 
Instance-level f-score:	46.05 (soft) 
Instance-level f-score:	51.52 (Levenshtein) 
Instance-level f-score:	48.12 (RatcliffObershelp) 

Matching 1 :	35378

Matching 2 :	1256

Matching 3 :	3271

Matching 4 :	1800

Total matches :	41705
```

#### Citation context resolution

```

Total expected references: 	 48449 - 48.45 references per article
Total predicted references: 	 48242 - 48.24 references per article

Total expected citation contexts: 	 69755 - 69.75 citation contexts per article
Total predicted citation contexts: 	 73473 - 73.47 citation contexts per article

Total correct predicted citation contexts: 	 56648 - 56.65 citation contexts per article
Total wrong predicted citation contexts: 	 16825 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 77.1
Recall citation contexts: 	 81.21
fscore citation contexts: 	 79.1
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
| availability_stmt           | 53.87     | 51.86     | 52.84     | 779     |
| figure_title                | 2.16      | 0.94      | 1.31      | 8943    |
| funding_stmt                | 5.48      | 30.92     | 9.31      | 1507    |
| reference_citation          | 86.75     | 94.43     | 90.43     | 69741   |
| reference_figure            | 71.7      | 54.01     | 61.61     | 11010   |
| reference_table             | 84.32     | 92.25     | 88.11     | 5159    |
| section_title               | 77.1      | 65.78     | 70.99     | 17540   |
| table_title                 | 0.12      | 0.02      | 0.03      | 6092    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **74.96** | **73.74** | **74.34** | 120771  |
| all fields (macro avg.)     | 47.69     | 48.78     | 46.83     | 120771  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 79.6      | 76.64     | 78.09     | 779     |
| figure_title                | 81.32     | 35.33     | 49.26     | 8943    |
| funding_stmt                | 6.98      | 39.35     | 11.85     | 1507    |
| reference_citation          | 86.77     | 94.44     | 90.44     | 69741   |
| reference_figure            | 72.15     | 54.35     | 62        | 11010   |
| reference_table             | 84.5      | 92.44     | 88.29     | 5159    |
| section_title               | 78.09     | 66.62     | 71.9      | 17540   |
| table_title                 | 51.4      | 7.24      | 12.69     | 6092    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **78.36** | **77.08** | **77.71** | 120771  |
| all fields (macro avg.)     | 67.6      | 58.3      | 58.07     | 120771  |

**Document-level ratio results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| availability_stmt           | 99.73     | 96.28     | 97.98     | 779     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.73** | **96.28** | **97.98** | 779     |
| all fields (macro avg.)     | 99.73     | 96.28     | 97.98     | 779     |

Evaluation metrics produced in 424.765 seconds
