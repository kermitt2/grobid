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
| abstract                    | 16.84     | 16.54     | 16.69     | 1911    |
| authors                     | 89.63     | 89.49     | 89.56     | 1941    |
| first_author                | 96.49     | 96.34     | 96.42     | 1941    |
| keywords                    | 65.65     | 64.13     | 64.88     | 1380    |
| title                       | 83.22     | 82.96     | 83.09     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **71.04** | **70.43** | **70.73** | 9116    |
| all fields (macro avg.)     | 70.37     | 69.89     | 70.13     | 9116    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 63.97     | 62.79     | 63.37     | 1911    |
| authors                     | 91.8      | 91.65     | 91.72     | 1941    |
| first_author                | 96.96     | 96.81     | 96.88     | 1941    |
| keywords                    | 74.33     | 72.61     | 73.46     | 1380    |
| title                       | 92.82     | 92.54     | 92.68     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **84.74** | **84.01** | **84.37** | 9116    |
| all fields (macro avg.)     | 83.97     | 83.28     | 83.62     | 9116    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 90.72     | 89.06     | 89.89     | 1911    |
| authors                     | 95.56     | 95.41     | 95.49     | 1941    |
| first_author                | 97.11     | 96.96     | 97.04     | 1941    |
| keywords                    | 84.5      | 82.54     | 83.5      | 1380    |
| title                       | 98.66     | 98.35     | 98.51     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.9**  | **93.09** | **93.49** | 9116    |
| all fields (macro avg.)     | 93.31     | 92.47     | 92.88     | 9116    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 86.89     | 85.3      | 86.08     | 1911    |
| authors                     | 93.45     | 93.3      | 93.37     | 1941    |
| first_author                | 96.49     | 96.34     | 96.42     | 1941    |
| keywords                    | 79.9      | 78.04     | 78.96     | 1380    |
| title                       | 96.7      | 96.4      | 96.55     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.41** | **90.62** | **91.02** | 9116    |
| all fields (macro avg.)     | 90.68     | 89.88     | 90.28     | 9116    |

#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	212 (strict) 
Total correct instances: 	883 (soft) 
Total correct instances: 	1424 (Levenshtein) 
Total correct instances: 	1266 (ObservedRatcliffObershelp) 

Instance-level recall:	10.91	(strict) 
Instance-level recall:	45.45	(soft) 
Instance-level recall:	73.29	(Levenshtein) 
Instance-level recall:	65.16	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.07     | 76.34     | 79.56     | 85778   |
| date                        | 94.66     | 84.3      | 89.18     | 87067   |
| first_author                | 89.81     | 82.52     | 86.01     | 85778   |
| inTitle                     | 73.33     | 71.94     | 72.63     | 81007   |
| issue                       | 91.32     | 87.42     | 89.33     | 16635   |
| page                        | 94.64     | 83.78     | 88.88     | 80501   |
| title                       | 79.76     | 75.39     | 77.51     | 80736   |
| volume                      | 96.12     | 89.85     | 92.88     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.29** | **80.78** | **83.91** | 597569  |
| all fields (macro avg.)     | 87.84     | 81.44     | 84.5      | 597569  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.54     | 76.78     | 80.02     | 85778   |
| date                        | 94.66     | 84.3      | 89.18     | 87067   |
| first_author                | 89.98     | 82.67     | 86.17     | 85778   |
| inTitle                     | 85.03     | 83.43     | 84.22     | 81007   |
| issue                       | 91.32     | 87.42     | 89.33     | 16635   |
| page                        | 94.64     | 83.78     | 88.88     | 80501   |
| title                       | 91.51     | 86.5      | 88.93     | 80736   |
| volume                      | 96.12     | 89.85     | 92.88     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.69** | **83.93** | **87.18** | 597569  |
| all fields (macro avg.)     | 90.85     | 84.34     | 87.45     | 597569  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.24     | 82.02     | 85.47     | 85778   |
| date                        | 94.66     | 84.3      | 89.18     | 87067   |
| first_author                | 90.19     | 82.87     | 86.37     | 85778   |
| inTitle                     | 86.29     | 84.66     | 85.46     | 81007   |
| issue                       | 91.32     | 87.42     | 89.33     | 16635   |
| page                        | 94.64     | 83.78     | 88.88     | 80501   |
| title                       | 93.87     | 88.73     | 91.23     | 80736   |
| volume                      | 96.12     | 89.85     | 92.88     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.04** | **85.17** | **88.47** | 597569  |
| all fields (macro avg.)     | 92.04     | 85.45     | 88.6      | 597569  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.01     | 79.05     | 82.38     | 85778   |
| date                        | 94.66     | 84.3      | 89.18     | 87067   |
| first_author                | 89.82     | 82.53     | 86.03     | 85778   |
| inTitle                     | 83.61     | 82.03     | 82.81     | 81007   |
| issue                       | 91.32     | 87.42     | 89.33     | 16635   |
| page                        | 94.64     | 83.78     | 88.88     | 80501   |
| title                       | 93.46     | 88.34     | 90.83     | 80736   |
| volume                      | 96.12     | 89.85     | 92.88     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.08** | **84.29** | **87.55** | 597569  |
| all fields (macro avg.)     | 91.2      | 84.66     | 87.79     | 597569  |

#### Instance-level results

```
Total expected instances: 		90125
Total extracted instances: 		85690
Total correct instances: 		38819 (strict) 
Total correct instances: 		50956 (soft) 
Total correct instances: 		55833 (Levenshtein) 
Total correct instances: 		52377 (RatcliffObershelp) 

Instance-level precision:	45.3 (strict) 
Instance-level precision:	59.47 (soft) 
Instance-level precision:	65.16 (Levenshtein) 
Instance-level precision:	61.12 (RatcliffObershelp) 

Instance-level recall:	43.07	(strict) 
Instance-level recall:	56.54	(soft) 
Instance-level recall:	61.95	(Levenshtein) 
Instance-level recall:	58.12	(RatcliffObershelp) 

Instance-level f-score:	44.16 (strict) 
Instance-level f-score:	57.97 (soft) 
Instance-level f-score:	63.51 (Levenshtein) 
Instance-level f-score:	59.58 (RatcliffObershelp) 

Matching 1 :	68390

Matching 2 :	4135

Matching 3 :	1866

Matching 4 :	658

Total matches :	75049
```

#### Citation context resolution

```

Total expected references: 	 90125 - 46.38 references per article
Total predicted references: 	 85690 - 44.1 references per article

Total expected citation contexts: 	 139835 - 71.97 citation contexts per article
Total predicted citation contexts: 	 115002 - 59.19 citation contexts per article

Total correct predicted citation contexts: 	 97408 - 50.13 citation contexts per article
Total wrong predicted citation contexts: 	 17594 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 84.7
Recall citation contexts: 	 69.66
fscore citation contexts: 	 76.45
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
| figure_title                | 32.81     | 25.77     | 28.86     | 7281    |
| reference_citation          | 58.12     | 58.7      | 58.41     | 134196  |
| reference_figure            | 60.7      | 67.92     | 64.11     | 19330   |
| reference_table             | 83.95     | 89.55     | 86.66     | 7327    |
| section_title               | 73.81     | 67.73     | 70.64     | 27619   |
| table_title                 | 66.56     | 45.56     | 54.09     | 3971    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **60.81** | **60.51** | **60.66** | 199724  |
| all fields (macro avg.)     | 62.66     | 59.2      | 60.46     | 199724  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| figure_title                | 80.57     | 63.27     | 70.88     | 7281    |
| reference_citation          | 62.43     | 63.05     | 62.74     | 134196  |
| reference_figure            | 61.18     | 68.46     | 64.62     | 19330   |
| reference_table             | 84.16     | 89.76     | 86.87     | 7327    |
| section_title               | 79.13     | 72.62     | 75.73     | 27619   |
| table_title                 | 94.44     | 64.64     | 76.75     | 3971    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **66.24** | **65.92** | **66.08** | 199724  |
| all fields (macro avg.)     | 76.99     | 70.3      | 72.93     | 199724  |

**Document-level ratio results**

| label                       | precision | recall | f1    | support |
|-----------------------------|-----------|--------|-------|---------|
|                             |           |        |       |         |
| **all fields (micro avg.)** | **0**     | **0**  | **0** | 0       |
| all fields (macro avg.)     | 0         | 0      | 0     | 0       |

Evaluation metrics produced in 1291.229 seconds




