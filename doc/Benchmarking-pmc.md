# Benchmarking PubMed Central

## General

This is the end-to-end benchmarking result for GROBID version **0.7.3** against the `PMC_sample_1943` dataset, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models. 

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 1943 random PDF PMC files out of 1943 PDF from 1943 different journals (0 PDF parsing failure).

Runtime for processing 1943 PDF: **2871s** (1.4s per PDF) on Ubuntu 16.04, 4 CPU i7-4790K (8 threads), 16GB RAM (workstation bought in 2015 for 1600 euros) and with a GeForce GTX 1050 Ti GPU.

Note: with CRF only models, runtime is 470s (0.24 seconds per PDF).

## Header metadata 

Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 16.78 | 16.48 | 16.63 | 1911 |
| authors | 89.99 | 89.9 | 89.95 | 1941 |
| first_author | 96.65 | 96.55 | 96.6 | 1941 |
| keywords | 64.99 | 63.62 | 64.3 | 1380 |
| title | 85.39 | 85.13 | 85.26 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **71.5** | **70.93** | **71.22** | 9116 |
| all fields (macro avg.) | 70.76 | 70.34 | 70.55 | 9116 |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 63.83 | 62.69 | 63.25 | 1911 |
| authors | 90.87 | 90.78 | 90.82 | 1941 |
| first_author | 96.85 | 96.75 | 96.8 | 1941 |
| keywords | 73.65 | 72.1 | 72.87 | 1380 |
| title | 93.86 | 93.57 | 93.71 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **84.61** | **83.93** | **84.27** | 9116 |
| all fields (macro avg.) | 83.81 | 83.18 | 83.49 | 9116 |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 91.05 | 89.43 | 90.23 | 1911 |
| authors | 95 | 94.9 | 94.95 | 1941 |
| first_author | 97.06 | 96.96 | 97.01 | 1941 |
| keywords | 84.16 | 82.39 | 83.27 | 1380 |
| title | 98.86 | 98.56 | 98.71 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.83** | **93.08** | **93.45** | 9116 |
| all fields (macro avg.) | 93.23 | 92.45 | 92.83 | 9116 |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 87.11 | 85.56 | 86.33 | 1911 |
| authors | 92.88 | 92.79 | 92.84 | 1941 |
| first_author | 96.65 | 96.55 | 96.6 | 1941 |
| keywords | 79.42 | 77.75 | 78.58 | 1380 |
| title | 97.37 | 97.07 | 97.22 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.44** | **90.71** | **91.07** | 9116 |
| all fields (macro avg.) | 90.69 | 89.94 | 90.31 | 9116 |

#### Instance-level results

```
Total expected instances:   1943
Total correct instances:    220 (strict) 
Total correct instances:    881 (soft) 
Total correct instances:    1414 (Levenshtein) 
Total correct instances:    1272 (ObservedRatcliffObershelp) 

Instance-level recall:  11.32   (strict) 
Instance-level recall:  45.34   (soft) 
Instance-level recall:  72.77   (Levenshtein) 
Instance-level recall:  65.47   (RatcliffObershelp) 
```

## Citation metadata 

Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 83.04 | 76.32 | 79.54 | 85778 |
| date | 94.6 | 84.24 | 89.12 | 87067 |
| first_author | 89.78 | 82.49 | 85.98 | 85778 |
| inTitle | 73.22 | 71.87 | 72.54 | 81007 |
| issue | 91.1 | 87.74 | 89.39 | 16635 |
| page | 94.57 | 83.69 | 88.8 | 80501 |
| title | 79.91 | 75.52 | 77.65 | 80736 |
| volume | 96.02 | 89.81 | 92.81 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **87.25** | **80.77** | **83.88** | 597569 |
| all fields (macro avg.) | 87.78 | 81.46 | 84.48 | 597569 |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 83.51 | 76.75 | 79.99 | 85778 |
| date | 94.6 | 84.24 | 89.12 | 87067 |
| first_author | 89.95 | 82.65 | 86.15 | 85778 |
| inTitle | 84.92 | 83.34 | 84.13 | 81007 |
| issue | 91.1 | 87.74 | 89.39 | 16635 |
| page | 94.57 | 83.69 | 88.8 | 80501 |
| title | 91.44 | 86.42 | 88.86 | 80736 |
| volume | 96.02 | 89.81 | 92.81 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.62** | **83.88** | **87.12** | 597569 |
| all fields (macro avg.) | 90.76 | 84.33 | 87.4 | 597569 |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 89.22 | 81.99 | 85.45 | 85778 |
| date | 94.6 | 84.24 | 89.12 | 87067 |
| first_author | 90.16 | 82.84 | 86.34 | 85778 |
| inTitle | 86.17 | 84.57 | 85.37 | 81007 |
| issue | 91.1 | 87.74 | 89.39 | 16635 |
| page | 94.57 | 83.69 | 88.8 | 80501 |
| title | 93.8 | 88.65 | 91.15 | 80736 |
| volume | 96.02 | 89.81 | 92.81 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.96** | **85.13** | **88.41** | 597569 |
| all fields (macro avg.) | 91.95 | 85.44 | 88.55 | 597569 |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 85.99 | 79.03 | 82.36 | 85778 |
| date | 94.6 | 84.24 | 89.12 | 87067 |
| first_author | 89.8 | 82.51 | 86 | 85778 |
| inTitle | 83.49 | 81.94 | 82.71 | 81007 |
| issue | 91.1 | 87.74 | 89.39 | 16635 |
| page | 94.57 | 83.69 | 88.8 | 80501 |
| title | 93.39 | 88.26 | 90.75 | 80736 |
| volume | 96.02 | 89.81 | 92.81 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.01** | **84.24** | **87.5** | 597569 |
| all fields (macro avg.) | 91.12 | 84.65 | 87.74 | 597569 |

#### Instance-level results

```
Total expected instances:       90125
Total extracted instances:      85893
Total correct instances:        38882 (strict) 
Total correct instances:        50895 (soft) 
Total correct instances:        55770 (Levenshtein) 
Total correct instances:        52316 (RatcliffObershelp) 

Instance-level precision:   45.27 (strict) 
Instance-level precision:   59.25 (soft) 
Instance-level precision:   64.93 (Levenshtein) 
Instance-level precision:   60.91 (RatcliffObershelp) 

Instance-level recall:  43.14   (strict) 
Instance-level recall:  56.47   (soft) 
Instance-level recall:  61.88   (Levenshtein) 
Instance-level recall:  58.05   (RatcliffObershelp) 

Instance-level f-score: 44.18 (strict) 
Instance-level f-score: 57.83 (soft) 
Instance-level f-score: 63.37 (Levenshtein) 
Instance-level f-score: 59.44 (RatcliffObershelp) 

Matching 1 :    68320

Matching 2 :    4150

Matching 3 :    1866

Matching 4 :    665

Total matches : 75001
```

#### Citation context resolution
```

Total expected references:   90125 - 46.38 references per article
Total predicted references:      85893 - 44.21 references per article

Total expected citation contexts:    139835 - 71.97 citation contexts per article
Total predicted citation contexts:   115367 - 59.38 citation contexts per article

Total correct predicted citation contexts:   97270 - 50.06 citation contexts per article
Total wrong predicted citation contexts:     18097 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     84.31
Recall citation contexts:    69.56
fscore citation contexts:    76.23
```

## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 31.4 | 24.57 | 27.57 | 7281 |
| reference_citation | 57.43 | 58.68 | 58.05 | 134196 |
| reference_figure | 61.2 | 65.88 | 63.45 | 19330 |
| reference_table | 83.01 | 88.39 | 85.62 | 7327 |
| section_title | 76.38 | 67.77 | 71.82 | 27619 |
| table_title | 57.29 | 50.29 | 53.56 | 3971 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **60.4** | **60.31** | **60.36** | 199724 |
| all fields (macro avg.) | 61.12 | 59.26 | 60.01 | 199724 |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 78.62 | 61.52 | 69.02 | 7281 |
| reference_citation | 61.68 | 63.03 | 62.35 | 134196 |
| reference_figure | 61.68 | 66.4 | 63.95 | 19330 |
| reference_table | 83.19 | 88.58 | 85.8 | 7327 |
| section_title | 81.23 | 72.08 | 76.38 | 27619 |
| table_title | 81.87 | 71.87 | 76.55 | 3971 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **65.76** | **65.66** | **65.71** | 199724 |
| all fields (macro avg.) | 74.71 | 70.58 | 72.34 | 199724 |
