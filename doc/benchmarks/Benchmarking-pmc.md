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
| abstract                    | 16.3      | 16.06     | 16.18     | 1911    |
| authors                     | 88.5      | 88.41     | 88.45     | 1941    |
| first_author                | 96.29     | 96.19     | 96.24     | 1941    |
| keywords                    | 66.44     | 64.13     | 65.27     | 1380    |
| title                       | 82.8      | 82.76     | 82.78     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **70.64** | **70.02** | **70.33** | 9116    |
| all fields (macro avg.)     | 70.06     | 69.51     | 69.78     | 9116    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 62.05     | 61.17     | 61.61     | 1911    |
| authors                     | 90.77     | 90.67     | 90.72     | 1941    |
| first_author                | 96.75     | 96.65     | 96.7      | 1941    |
| keywords                    | 74.85     | 72.25     | 73.53     | 1380    |
| title                       | 92.53     | 92.49     | 92.51     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **84.1**  | **83.36** | **83.73** | 9116    |
| all fields (macro avg.)     | 83.39     | 82.65     | 83.01     | 9116    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 89.28     | 88.02     | 88.64     | 1911    |
| authors                     | 95.15     | 95.05     | 95.1      | 1941    |
| first_author                | 96.91     | 96.81     | 96.86     | 1941    |
| keywords                    | 84.91     | 81.96     | 83.41     | 1380    |
| title                       | 98.09     | 98.04     | 98.07     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.43** | **92.61** | **93.01** | 9116    |
| all fields (macro avg.)     | 92.87     | 91.98     | 92.42     | 9116    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 85.24     | 84.04     | 84.64     | 1911    |
| authors                     | 92.26     | 92.17     | 92.22     | 1941    |
| first_author                | 96.29     | 96.19     | 96.24     | 1941    |
| keywords                    | 80.41     | 77.61     | 78.98     | 1380    |
| title                       | 96.24     | 96.19     | 96.22     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.77** | **89.97** | **90.37** | 9116    |
| all fields (macro avg.)     | 90.09     | 89.24     | 89.66     | 9116    |

#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	207 (strict) 
Total correct instances: 	848 (soft) 
Total correct instances: 	1372 (Levenshtein) 
Total correct instances: 	1220 (ObservedRatcliffObershelp) 

Instance-level recall:	10.65	(strict) 
Instance-level recall:	43.64	(soft) 
Instance-level recall:	70.61	(Levenshtein) 
Instance-level recall:	62.79	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 82.13     | 74.78     | 78.28     | 85778   |
| date                        | 93.83     | 82.77     | 87.95     | 87067   |
| first_author                | 89.27     | 81.26     | 85.08     | 85778   |
| inTitle                     | 71.55     | 70.15     | 70.84     | 81007   |
| issue                       | 91.26     | 85.1      | 88.07     | 16635   |
| page                        | 93.98     | 82.52     | 87.88     | 80501   |
| title                       | 77.96     | 72.58     | 75.17     | 80736   |
| volume                      | 95.71     | 87.96     | 91.67     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.3**  | **79.05** | **82.51** | 597569  |
| all fields (macro avg.)     | 86.96     | 79.64     | 83.12     | 597569  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 82.59     | 75.2      | 78.72     | 85778   |
| date                        | 93.83     | 82.77     | 87.95     | 87067   |
| first_author                | 89.45     | 81.42     | 85.24     | 85778   |
| inTitle                     | 82.87     | 81.25     | 82.05     | 81007   |
| issue                       | 91.26     | 85.1      | 88.07     | 16635   |
| page                        | 93.98     | 82.52     | 87.88     | 80501   |
| title                       | 89.29     | 83.13     | 86.1      | 80736   |
| volume                      | 95.71     | 87.96     | 91.67     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.58** | **82.06** | **85.66** | 597569  |
| all fields (macro avg.)     | 89.87     | 82.42     | 85.96     | 597569  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.25     | 80.35     | 84.12     | 85778   |
| date                        | 93.83     | 82.77     | 87.95     | 87067   |
| first_author                | 89.63     | 81.59     | 85.42     | 85778   |
| inTitle                     | 83.91     | 82.27     | 83.08     | 81007   |
| issue                       | 91.26     | 85.1      | 88.07     | 16635   |
| page                        | 93.98     | 82.52     | 87.88     | 80501   |
| title                       | 92.52     | 86.14     | 89.22     | 80736   |
| volume                      | 95.71     | 87.96     | 91.67     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.01** | **83.37** | **87.02** | 597569  |
| all fields (macro avg.)     | 91.13     | 83.59     | 87.18     | 597569  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 85.01     | 77.41     | 81.03     | 85778   |
| date                        | 93.83     | 82.77     | 87.95     | 87067   |
| first_author                | 89.29     | 81.27     | 85.09     | 85778   |
| inTitle                     | 81.48     | 79.89     | 80.68     | 81007   |
| issue                       | 91.26     | 85.1      | 88.07     | 16635   |
| page                        | 93.98     | 82.52     | 87.88     | 80501   |
| title                       | 91.55     | 85.23     | 88.28     | 80736   |
| volume                      | 95.71     | 87.96     | 91.67     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.02** | **82.46** | **86.07** | 597569  |
| all fields (macro avg.)     | 90.26     | 82.77     | 86.33     | 597569  |

#### Instance-level results

```
Total expected instances: 		90125
Total extracted instances: 		88623
Total correct instances: 		37414 (strict) 
Total correct instances: 		48861 (soft) 
Total correct instances: 		53552 (Levenshtein) 
Total correct instances: 		50226 (RatcliffObershelp) 

Instance-level precision:	42.22 (strict) 
Instance-level precision:	55.13 (soft) 
Instance-level precision:	60.43 (Levenshtein) 
Instance-level precision:	56.67 (RatcliffObershelp) 

Instance-level recall:	41.51	(strict) 
Instance-level recall:	54.21	(soft) 
Instance-level recall:	59.42	(Levenshtein) 
Instance-level recall:	55.73	(RatcliffObershelp) 

Instance-level f-score:	41.86 (strict) 
Instance-level f-score:	54.67 (soft) 
Instance-level f-score:	59.92 (Levenshtein) 
Instance-level f-score:	56.2 (RatcliffObershelp) 

Matching 1 :	65770

Matching 2 :	4950

Matching 3 :	2392

Matching 4 :	706

Total matches :	73818
```

#### Citation context resolution

```

Total expected references: 	 90125 - 46.38 references per article
Total predicted references: 	 88623 - 45.61 references per article

Total expected citation contexts: 	 139835 - 71.97 citation contexts per article
Total predicted citation contexts: 	 117777 - 60.62 citation contexts per article

Total correct predicted citation contexts: 	 96797 - 49.82 citation contexts per article
Total wrong predicted citation contexts: 	 20980 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 82.19
Recall citation contexts: 	 69.22
fscore citation contexts: 	 75.15
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| figure_title                | 31.88     | 26.53     | 28.96    | 7281    |
| reference_citation          | 58.24     | 59.45     | 58.84    | 134196  |
| reference_figure            | 60.59     | 68.26     | 64.19    | 19330   |
| reference_table             | 83.02     | 89.7      | 86.23    | 7327    |
| section_title               | 72.6      | 67.72     | 70.07    | 27619   |
| table_title                 | 67.86     | 49.61     | 57.32    | 3971    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **60.65** | **61.16** | **60.9** | 199724  |
| all fields (macro avg.)     | 62.36     | 60.21     | 60.94    | 199724  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| figure_title                | 79.16     | 65.88     | 71.91     | 7281    |
| reference_citation          | 62.49     | 63.79     | 63.13     | 134196  |
| reference_figure            | 61.09     | 68.83     | 64.73     | 19330   |
| reference_table             | 83.19     | 89.87     | 86.4      | 7327    |
| section_title               | 78.01     | 72.77     | 75.3      | 27619   |
| table_title                 | 94.25     | 68.9      | 79.6      | 3971    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **66.1**  | **66.65** | **66.37** | 199724  |
| all fields (macro avg.)     | 76.36     | 71.67     | 73.51     | 199724  |

**Document-level ratio results**

| label                       | precision | recall | f1    | support |
|-----------------------------|-----------|--------|-------|---------|
|                             |           |        |       |         |
| **all fields (micro avg.)** | **0**     | **0**  | **0** | 0       |
| all fields (macro avg.)     | 0         | 0      | 0     | 0       |

Evaluation metrics produced in 1294.959 seconds

