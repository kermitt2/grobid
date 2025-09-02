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
| abstract                    | 16.89     | 16.54     | 16.71     | 1911    |
| authors                     | 92.78     | 92.63     | 92.7      | 1941    |
| first_author                | 96.8      | 96.65     | 96.73     | 1941    |
| keywords                    | 65.58     | 63.91     | 64.73     | 1380    |
| title                       | 84.46     | 84.2      | 84.33     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **72.08** | **71.39** | **71.73** | 9116    |
| all fields (macro avg.)     | 71.3      | 70.79     | 71.04     | 9116    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 63.98     | 62.64     | 63.3      | 1911    |
| authors                     | 94.74     | 94.59     | 94.66     | 1941    |
| first_author                | 97.21     | 97.06     | 97.14     | 1941    |
| keywords                    | 74.2      | 72.32     | 73.25     | 1380    |
| title                       | 92        | 91.71     | 91.86     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **85.25** | **84.43** | **84.84** | 9116    |
| all fields (macro avg.)     | 84.43     | 83.66     | 84.04     | 9116    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 90.86     | 88.96     | 89.9      | 1911    |
| authors                     | 96.65     | 96.5      | 96.57     | 1941    |
| first_author                | 97.47     | 97.32     | 97.4      | 1941    |
| keywords                    | 84.61     | 82.46     | 83.52     | 1380    |
| title                       | 98.24     | 97.94     | 98.09     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.17** | **93.28** | **93.72** | 9116    |
| all fields (macro avg.)     | 93.57     | 92.64     | 93.1      | 9116    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 87.07     | 85.24     | 86.14     | 1911    |
| authors                     | 95.67     | 95.52     | 95.59     | 1941    |
| first_author                | 96.8      | 96.65     | 96.73     | 1941    |
| keywords                    | 79.93     | 77.9      | 78.9      | 1380    |
| title                       | 96.18     | 95.88     | 96.03     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.89** | **91.02** | **91.45** | 9116    |
| all fields (macro avg.)     | 91.13     | 90.24     | 90.68     | 9116    |

#### Instance-level results

```
Total expected instances:       1943
Total correct instances:        216 (strict)
Total correct instances:        906 (soft)
Total correct instances:        1445 (Levenshtein)
Total correct instances:        1297 (ObservedRatcliffObershelp)

Instance-level recall:  11.12   (strict)
Instance-level recall:  46.63   (soft)
Instance-level recall:  74.37   (Levenshtein)
Instance-level recall:  66.75   (RatcliffObershelp)
```

## Citation metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.11     | 75.94     | 79.36     | 85778   |
| date                        | 94.69     | 83.83     | 88.93     | 87067   |
| first_author                | 89.85     | 82.09     | 85.8      | 85778   |
| inTitle                     | 73.27     | 71.45     | 72.35     | 81007   |
| issue                       | 91.43     | 87.44     | 89.39     | 16635   |
| page                        | 94.68     | 83.31     | 88.63     | 80501   |
| title                       | 79.78     | 74.95     | 77.29     | 80736   |
| volume                      | 96.17     | 89.37     | 92.64     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.32** | **80.34** | **83.69** | 597569  |
| all fields (macro avg.)     | 87.87     | 81.05     | 84.3      | 597569  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.58     | 76.37     | 79.81     | 85778   |
| date                        | 94.69     | 83.83     | 88.93     | 87067   |
| first_author                | 90.02     | 82.24     | 85.96     | 85778   |
| inTitle                     | 85.03     | 82.92     | 83.97     | 81007   |
| issue                       | 91.43     | 87.44     | 89.39     | 16635   |
| page                        | 94.68     | 83.31     | 88.63     | 80501   |
| title                       | 91.55     | 86.01     | 88.69     | 80736   |
| volume                      | 96.17     | 89.37     | 92.64     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.73** | **83.48** | **86.95** | 597569  |
| all fields (macro avg.)     | 90.9      | 83.94     | 87.25     | 597569  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.29     | 81.59     | 85.27     | 85778   |
| date                        | 94.69     | 83.83     | 88.93     | 87067   |
| first_author                | 90.24     | 82.44     | 86.17     | 85778   |
| inTitle                     | 86.28     | 84.14     | 85.2      | 81007   |
| issue                       | 91.43     | 87.44     | 89.39     | 16635   |
| page                        | 94.68     | 83.31     | 88.63     | 80501   |
| title                       | 93.9      | 88.22     | 90.97     | 80736   |
| volume                      | 96.17     | 89.37     | 92.64     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.07** | **84.72** | **88.24** | 597569  |
| all fields (macro avg.)     | 92.09     | 85.04     | 88.4      | 597569  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.05     | 78.63     | 82.18     | 85778   |
| date                        | 94.69     | 83.83     | 88.93     | 87067   |
| first_author                | 89.87     | 82.1      | 85.81     | 85778   |
| inTitle                     | 83.59     | 81.52     | 82.55     | 81007   |
| issue                       | 91.43     | 87.44     | 89.39     | 16635   |
| page                        | 94.68     | 83.31     | 88.63     | 80501   |
| title                       | 93.5      | 87.84     | 90.58     | 80736   |
| volume                      | 96.17     | 89.37     | 92.64     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.12** | **83.84** | **87.33** | 597569  |
| all fields (macro avg.)     | 91.25     | 84.26     | 87.59     | 597569  |

#### Instance-level results

```
Total expected instances:               90125
Total extracted instances:              85141
Total correct instances:                38534 (strict)
Total correct instances:                50633 (soft)
Total correct instances:                55471 (Levenshtein)
Total correct instances:                52032 (RatcliffObershelp)

Instance-level precision:       45.26 (strict)
Instance-level precision:       59.47 (soft)
Instance-level precision:       65.15 (Levenshtein)
Instance-level precision:       61.11 (RatcliffObershelp)

Instance-level recall:  42.76   (strict)
Instance-level recall:  56.18   (soft)
Instance-level recall:  61.55   (Levenshtein)
Instance-level recall:  57.73   (RatcliffObershelp)

Instance-level f-score: 43.97 (strict)
Instance-level f-score: 57.78 (soft)
Instance-level f-score: 63.3 (Levenshtein)
Instance-level f-score: 59.37 (RatcliffObershelp)

Matching 1 :    67991

Matching 2 :    4123

Matching 3 :    1868

Matching 4 :    661

Total matches : 74643
```

#### Citation context resolution

```

Total expected references:       90125 - 46.38 references per article
Total predicted references:      85141 - 43.82 references per article

Total expected citation contexts:        139835 - 71.97 citation contexts per article
Total predicted citation contexts:       114496 - 58.93 citation contexts per article

Total correct predicted citation contexts:       96976 - 49.91 citation contexts per article
Total wrong predicted citation contexts:         17520 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     84.7
Recall citation contexts:        69.35
fscore citation contexts:        76.26
```

## Fulltext structures

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from
the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are
thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can
be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| figure_title                | 31.53     | 26.55    | 28.82     | 7281    |
| reference_citation          | 58.14     | 58.76    | 58.45     | 134196  |
| reference_figure            | 60.59     | 68.27    | 64.2      | 19330   |
| reference_table             | 82.87     | 89.52    | 86.06     | 7327    |
| section_title               | 73.58     | 67.75    | 70.55     | 27619   |
| table_title                 | 67.76     | 49.58    | 57.26     | 3971    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **60.68** | **60.7** | **60.69** | 199724  |
| all fields (macro avg.)     | 62.41     | 60.07    | 60.89     | 199724  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| figure_title                | 79.55     | 66.98     | 72.73     | 7281    |
| reference_citation          | 62.42     | 63.09     | 62.75     | 134196  |
| reference_figure            | 61.09     | 68.84     | 64.73     | 19330   |
| reference_table             | 83.04     | 89.71     | 86.25     | 7327    |
| section_title               | 79.09     | 72.82     | 75.83     | 27619   |
| table_title                 | 94.22     | 68.95     | 79.63     | 3971    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **66.2**  | **66.22** | **66.21** | 199724  |
| all fields (macro avg.)     | 76.57     | 71.73     | 73.65     | 199724  |

**Document-level ratio results**

| label                       | precision | recall | f1    | support |
|-----------------------------|-----------|--------|-------|---------|
|                             |           |        |       |         |
| **all fields (micro avg.)** | **0**     | **0**  | **0** | 0       |
| all fields (macro avg.)     | 0         | 0      | 0     | 0       |

Evaluation metrics produced in 1311.519 seconds
