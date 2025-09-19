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
| abstract                    | 13.33     | 13.33     | 13.33     | 960     |
| authors                     | 99.07     | 99.07     | 99.07     | 969     |
| first_author                | 99.28     | 99.28     | 99.28     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 95.97     | 95.3      | 95.63     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **77.18** | **77.04** | **77.11** | 3898    |
| all fields (macro avg.)     | 76.91     | 76.75     | 76.83     | 3898    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| abstract                    | 50.52     | 50.52     | 50.52    | 960     |
| authors                     | 99.07     | 99.07     | 99.07    | 969     |
| first_author                | 99.28     | 99.28     | 99.28    | 969     |
| keywords                    | 0         | 0         | 0        | 0       |
| title                       | 99.6      | 98.9      | 99.25    | 1000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **87.28** | **87.12** | **87.2** | 3898    |
| all fields (macro avg.)     | 87.12     | 86.94     | 87.03    | 3898    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| abstract                    | 76.67     | 76.67     | 76.67    | 960     |
| authors                     | 99.48     | 99.48     | 99.48    | 969     |
| first_author                | 99.38     | 99.38     | 99.38    | 969     |
| keywords                    | 0         | 0         | 0        | 0       |
| title                       | 99.7      | 99        | 99.35    | 1000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **93.88** | **93.71** | **93.8** | 3898    |
| all fields (macro avg.)     | 93.81     | 93.63     | 93.72    | 3898    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| abstract                    | 66.56     | 66.56     | 66.56     | 960     |
| authors                     | 99.38     | 99.38     | 99.38     | 969     |
| first_author                | 99.28     | 99.28     | 99.28     | 969     |
| keywords                    | 0         | 0         | 0         | 0       |
| title                       | 99.7      | 99        | 99.35     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.34** | **91.17** | **91.26** | 3898    |
| all fields (macro avg.)     | 91.23     | 91.06     | 91.14     | 3898    |

#### Instance-level results

```
Total expected instances:       1000
Total correct instances:        142 (strict)
Total correct instances:        491 (soft)
Total correct instances:        729 (Levenshtein)
Total correct instances:        641 (ObservedRatcliffObershelp)

Instance-level recall:  14.2    (strict)
Instance-level recall:  49.1    (soft)
Instance-level recall:  72.9    (Levenshtein)
Instance-level recall:  64.1    (RatcliffObershelp)
```

## Citation metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.18     | 78.43     | 79.78     | 44770   |
| date                        | 84.64     | 81.25     | 82.91     | 45457   |
| first_author                | 91.49     | 88.36     | 89.9      | 44770   |
| inTitle                     | 81.69     | 83.58     | 82.62     | 42795   |
| issue                       | 93.63     | 92.71     | 93.17     | 18983   |
| page                        | 93.72     | 77.57     | 84.88     | 40844   |
| title                       | 59.97     | 60.48     | 60.23     | 43101   |
| volume                      | 95.91     | 96.12     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **84.25** | **81.46** | **82.83** | 321178  |
| all fields (macro avg.)     | 85.28     | 82.31     | 83.69     | 321178  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.5      | 78.74     | 80.09     | 44770   |
| date                        | 84.64     | 81.25     | 82.91     | 45457   |
| first_author                | 91.71     | 88.57     | 90.11     | 44770   |
| inTitle                     | 85.52     | 87.51     | 86.5      | 42795   |
| issue                       | 93.63     | 92.71     | 93.17     | 18983   |
| page                        | 93.72     | 77.57     | 84.88     | 40844   |
| title                       | 91.98     | 92.75     | 92.37     | 43101   |
| volume                      | 95.91     | 96.12     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.34** | **86.38** | **87.84** | 321178  |
| all fields (macro avg.)     | 89.83     | 86.9      | 88.26     | 321178  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 90.66     | 87.59     | 89.09     | 44770   |
| date                        | 84.64     | 81.25     | 82.91     | 45457   |
| first_author                | 92.25     | 89.09     | 90.64     | 44770   |
| inTitle                     | 86.46     | 88.48     | 87.46     | 42795   |
| issue                       | 93.63     | 92.71     | 93.17     | 18983   |
| page                        | 93.72     | 77.57     | 84.88     | 40844   |
| title                       | 94.58     | 95.38     | 94.98     | 43101   |
| volume                      | 95.91     | 96.12     | 96.02     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.19** | **88.17** | **89.65** | 321178  |
| all fields (macro avg.)     | 91.48     | 88.52     | 89.89     | 321178  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 84.94     | 82.07     | 83.48    | 44770   |
| date                        | 84.64     | 81.25     | 82.91    | 45457   |
| first_author                | 91.49     | 88.36     | 89.9     | 44770   |
| inTitle                     | 85.17     | 87.15     | 86.15    | 42795   |
| issue                       | 93.63     | 92.71     | 93.17    | 18983   |
| page                        | 93.72     | 77.57     | 84.88    | 40844   |
| title                       | 93.97     | 94.76     | 94.36    | 43101   |
| volume                      | 95.91     | 96.12     | 96.02    | 40458   |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **90.02** | **87.04** | **88.5** | 321178  |
| all fields (macro avg.)     | 90.43     | 87.5      | 88.86    | 321178  |

#### Instance-level results

```
Total expected instances:               48449
Total extracted instances:              48221
Total correct instances:                13495 (strict)
Total correct instances:                22265 (soft)
Total correct instances:                24914 (Levenshtein)
Total correct instances:                23267 (RatcliffObershelp)

Instance-level precision:       27.99 (strict)
Instance-level precision:       46.17 (soft)
Instance-level precision:       51.67 (Levenshtein)
Instance-level precision:       48.25 (RatcliffObershelp)

Instance-level recall:  27.85   (strict)
Instance-level recall:  45.96   (soft)
Instance-level recall:  51.42   (Levenshtein)
Instance-level recall:  48.02   (RatcliffObershelp)

Instance-level f-score: 27.92 (strict)
Instance-level f-score: 46.06 (soft)
Instance-level f-score: 51.54 (Levenshtein)
Instance-level f-score: 48.14 (RatcliffObershelp)

Matching 1 :    35376

Matching 2 :    1259

Matching 3 :    3266

Matching 4 :    1799

Total matches : 41700
```

#### Citation context resolution

```

Total expected references:       48449 - 48.45 references per article
Total predicted references:      48221 - 48.22 references per article

Total expected citation contexts:        69755 - 69.75 citation contexts per article
Total predicted citation contexts:       73164 - 73.16 citation contexts per article

Total correct predicted citation contexts:       56709 - 56.71 citation contexts per article
Total wrong predicted citation contexts:         16455 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     77.51
Recall citation contexts:        81.3
fscore citation contexts:        79.36
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
| availability_stmt           | 54        | 51.99     | 52.98     | 779     |
| figure_title                | 0.2       | 0.1       | 0.13      | 8943    |
| funding_stmt                | 5.47      | 30.72     | 9.28      | 1507    |
| reference_citation          | 87.96     | 94.35     | 91.04     | 69741   |
| reference_figure            | 74.18     | 85.72     | 79.53     | 11010   |
| reference_table             | 70.28     | 94.3      | 80.54     | 5159    |
| section_title               | 72.63     | 66.19     | 69.26     | 17540   |
| table_title                 | 0         | 0         | 0         | 6092    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **74.06** | **76.67** | **75.34** | 120771  |
| all fields (macro avg.)     | 45.59     | 52.92     | 47.85     | 120771  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| availability_stmt           | 79.73     | 76.77    | 78.22     | 779     |
| figure_title                | 90.96     | 45.79    | 60.91     | 8943    |
| funding_stmt                | 6.99      | 39.28    | 11.87     | 1507    |
| reference_citation          | 87.96     | 94.36    | 91.05     | 69741   |
| reference_figure            | 74.42     | 86       | 79.8      | 11010   |
| reference_table             | 70.44     | 94.51    | 80.72     | 5159    |
| section_title               | 78.4      | 71.45    | 74.76     | 17540   |
| table_title                 | 53.33     | 7.5      | 13.15     | 6092    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **78.73** | **81.5** | **80.09** | 120771  |
| all fields (macro avg.)     | 67.78     | 64.46    | 61.31     | 120771  |

**Document-level ratio results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| availability_stmt           | 100       | 96.28     | 98.1     | 779     |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **100**   | **96.28** | **98.1** | 779     |
| all fields (macro avg.)     | 100       | 96.28     | 98.1     | 779     |

Evaluation metrics produced in 795.257 seconds


