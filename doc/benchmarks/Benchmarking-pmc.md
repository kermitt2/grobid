# Benchmarking PubMed Central

## General

This is the end-to-end benchmarking result for GROBID version **0.8.2** against the `PMC_sample_1943` dataset, see
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

Evaluation on 1943 random PDF PMC files out of 1943 PDF from 1943 different journals (0 PDF parsing failure).

Runtime for processing 1943 PDF: **1467** seconds, (0.75s per PDF) on Ubuntu 22.04, 16 CPU (32 threads), 128GB RAM and
with a GeForce GTX 1080 Ti GPU.

Note: with CRF only models, runtime is 470s (0.24 seconds per PDF) with 4 CPU, 8 threads.

## Header metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 15.38     | 15.07     | 15.22     | 1911    |
| authors                     | 91.6      | 91.55     | 91.57     | 1941    |
| first_author                | 96.24     | 96.19     | 96.21     | 1941    |
| keywords                    | 42.05     | 37.75     | 39.79     | 1380    |
| title                       | 83.86     | 83.69     | 83.77     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **68.07** | **66.68** | **67.37** | 9116    |
| all fields (macro avg.)     | 65.82     | 64.85     | 65.31     | 9116    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 58.14     | 56.99     | 57.56     | 1911    |
| authors                     | 93.56     | 93.51     | 93.53     | 1941    |
| first_author                | 96.7      | 96.65     | 96.68     | 1941    |
| keywords                    | 48.26     | 43.33     | 45.67     | 1380    |
| title                       | 91.44     | 91.25     | 91.34     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **80.07** | **78.44** | **79.25** | 9116    |
| all fields (macro avg.)     | 77.62     | 76.35     | 76.96     | 9116    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 86.12     | 84.41     | 85.25     | 1911    |
| authors                     | 95.57     | 95.52     | 95.54     | 1941    |
| first_author                | 96.91     | 96.86     | 96.88     | 1941    |
| keywords                    | 69.01     | 61.96     | 65.29     | 1380    |
| title                       | 97.83     | 97.63     | 97.73     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.68** | **88.84** | **89.75** | 9116    |
| all fields (macro avg.)     | 89.09     | 87.27     | 88.14     | 9116    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| abstract                    | 82.22     | 80.59    | 81.4      | 1911    |
| authors                     | 94.43     | 94.38    | 94.41     | 1941    |
| first_author                | 96.24     | 96.19    | 96.21     | 1941    |
| keywords                    | 57.38     | 51.52    | 54.3      | 1380    |
| title                       | 95.56     | 95.37    | 95.47     | 1943    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **87.37** | **85.6** | **86.47** | 9116    |
| all fields (macro avg.)     | 85.17     | 83.61    | 84.36     | 9116    |

#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	148 (strict) 
Total correct instances: 	619 (soft) 
Total correct instances: 	1142 (Levenshtein) 
Total correct instances: 	954 (ObservedRatcliffObershelp) 

Instance-level recall:	7.62	(strict) 
Instance-level recall:	31.86	(soft) 
Instance-level recall:	58.78	(Levenshtein) 
Instance-level recall:	49.1	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.06     | 76.08     | 79.42     | 85778   |
| date                        | 94.66     | 84.04     | 89.03     | 87067   |
| first_author                | 89.83     | 82.25     | 85.88     | 85778   |
| inTitle                     | 73.34     | 71.68     | 72.5      | 81007   |
| issue                       | 91.23     | 87.4      | 89.28     | 16635   |
| page                        | 94.64     | 83.49     | 88.72     | 80501   |
| title                       | 79.8      | 75.15     | 77.41     | 80736   |
| volume                      | 96.17     | 89.58     | 92.76     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.31** | **80.53** | **83.78** | 597569  |
| all fields (macro avg.)     | 87.84     | 81.21     | 84.37     | 597569  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.54     | 76.51     | 79.87     | 85778   |
| date                        | 94.66     | 84.04     | 89.03     | 87067   |
| first_author                | 90        | 82.41     | 86.04     | 85778   |
| inTitle                     | 85.1      | 83.17     | 84.12     | 81007   |
| issue                       | 91.23     | 87.4      | 89.28     | 16635   |
| page                        | 94.64     | 83.49     | 88.72     | 80501   |
| title                       | 91.58     | 86.24     | 88.83     | 80736   |
| volume                      | 96.17     | 89.58     | 92.76     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.71** | **83.67** | **87.05** | 597569  |
| all fields (macro avg.)     | 90.86     | 84.11     | 87.33     | 597569  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.27     | 81.76     | 85.35     | 85778   |
| date                        | 94.66     | 84.04     | 89.03     | 87067   |
| first_author                | 90.22     | 82.61     | 86.25     | 85778   |
| inTitle                     | 86.35     | 84.4      | 85.36     | 81007   |
| issue                       | 91.23     | 87.4      | 89.28     | 16635   |
| page                        | 94.64     | 83.49     | 88.72     | 80501   |
| title                       | 93.92     | 88.45     | 91.1      | 80736   |
| volume                      | 96.17     | 89.58     | 92.76     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.07** | **84.91** | **88.35** | 597569  |
| all fields (macro avg.)     | 92.06     | 85.22     | 88.48     | 597569  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.02     | 78.78     | 82.24     | 85778   |
| date                        | 94.66     | 84.04     | 89.03     | 87067   |
| first_author                | 89.85     | 82.27     | 85.89     | 85778   |
| inTitle                     | 83.66     | 81.77     | 82.71     | 81007   |
| issue                       | 91.23     | 87.4      | 89.28     | 16635   |
| page                        | 94.64     | 83.49     | 88.72     | 80501   |
| title                       | 93.52     | 88.07     | 90.71     | 80736   |
| volume                      | 96.17     | 89.58     | 92.76     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.11** | **84.03** | **87.43** | 597569  |
| all fields (macro avg.)     | 91.22     | 84.43     | 87.67     | 597569  |

#### Instance-level results

```
Total expected instances: 		90125
Total extracted instances: 		85287
Total correct instances: 		38637 (strict) 
Total correct instances: 		50746 (soft) 
Total correct instances: 		55610 (Levenshtein) 
Total correct instances: 		52155 (RatcliffObershelp) 

Instance-level precision:	45.3 (strict) 
Instance-level precision:	59.5 (soft) 
Instance-level precision:	65.2 (Levenshtein) 
Instance-level precision:	61.15 (RatcliffObershelp) 

Instance-level recall:	42.87	(strict) 
Instance-level recall:	56.31	(soft) 
Instance-level recall:	61.7	(Levenshtein) 
Instance-level recall:	57.87	(RatcliffObershelp) 

Instance-level f-score:	44.05 (strict) 
Instance-level f-score:	57.86 (soft) 
Instance-level f-score:	63.41 (Levenshtein) 
Instance-level f-score:	59.47 (RatcliffObershelp) 

Matching 1 :	68177

Matching 2 :	4123

Matching 3 :	1866

Matching 4 :	664

Total matches :	74830
```

#### Citation context resolution

```

Total expected references: 	 90125 - 46.38 references per article
Total predicted references: 	 85287 - 43.89 references per article

Total expected citation contexts: 	 139835 - 71.97 citation contexts per article
Total predicted citation contexts: 	 115010 - 59.19 citation contexts per article

Total correct predicted citation contexts: 	 97399 - 50.13 citation contexts per article
Total wrong predicted citation contexts: 	 17611 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 84.69
Recall citation contexts: 	 69.65
fscore citation contexts: 	 76.44
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| figure_title                | 31.7      | 26.64     | 28.95     | 7281    |
| reference_citation          | 58.14     | 58.81     | 58.47     | 134196  |
| reference_figure            | 60.62     | 68.25     | 64.21     | 19330   |
| reference_table             | 82.94     | 89.63     | 86.15     | 7327    |
| section_title               | 73.91     | 67.76     | 70.7      | 27619   |
| table_title                 | 68.03     | 49.89     | 57.56     | 3971    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **60.73** | **60.74** | **60.73** | 199724  |
| all fields (macro avg.)     | 62.56     | 60.16     | 61.01     | 199724  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| figure_title                | 79.61     | 66.91     | 72.71     | 7281    |
| reference_citation          | 62.43     | 63.14     | 62.78     | 134196  |
| reference_figure            | 61.13     | 68.83     | 64.75     | 19330   |
| reference_table             | 83.11     | 89.82     | 86.34     | 7327    |
| section_title               | 79.41     | 72.8      | 75.96     | 27619   |
| table_title                 | 94.37     | 69.2      | 79.85     | 3971    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **66.25** | **66.26** | **66.26** | 199724  |
| all fields (macro avg.)     | 76.68     | 71.78     | 73.73     | 199724  |

**Document-level ratio results**

| label                       | precision | recall | f1    | support |
|-----------------------------|-----------|--------|-------|---------|
|                             |           |        |       |         |
| **all fields (micro avg.)** | **0**     | **0**  | **0** | 0       |
| all fields (macro avg.)     | 0         | 0      | 0     | 0       |

Evaluation metrics produced in 1287.487 seconds

