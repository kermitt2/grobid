# Benchmarking PubMed Central

## General

This is the end-to-end benchmarking result for GROBID version **0.7.2** against the `PMC_sample_1943` dataset, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:
- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model
- **CRF Wapiti** as sequence labelling engine for all other models. 

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 1943 random PDF PMC files out of 1943 PDF from 1943 different journals (0 PDF parsing failure).

Runtime for processing 1943 PDF: **797s** (2.44 PDF per second) on Ubuntu 16.04, 4 CPU i7-4790K (8 threads), 16GB RAM (workstation bought in 2015 for 1600 euros) and with a GeForce GTX 1050 Ti GPU.

## Header metadata 

Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 16.08 | 15.8 | 15.94 | 1911 |
| authors | 93.55 | 93.46 | 93.51 | 1941 |
| first_author | 96.7 | 96.6 | 96.65 | 1941 |
| keywords | 67.65 | 63.04 | 65.27 | 1380 |
| title | 86.64 | 86.41 | 86.52 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **72.83** | **71.74** | **72.28** | 9116 |
| all fields (macro avg.) | 72.12 | 71.06 | 71.58 | 9116 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 61.02 | 59.97 | 60.49 | 1911 |
| authors | 94.17 | 94.08 | 94.12 | 1941 |
| first_author | 96.85 | 96.75 | 96.8 | 1941 |
| keywords | 76.28 | 71.09 | 73.59 | 1380 |
| title | 93.55 | 93.31 | 93.43 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **85.12** | **83.85** | **84.48** | 9116 |
| all fields (macro avg.) | 84.38 | 83.04 | 83.69 | 9116 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 88.45 | 86.92 | 87.67 | 1911 |
| authors | 96.49 | 96.39 | 96.44 | 1941 |
| first_author | 97.16 | 97.06 | 97.11 | 1941 |
| keywords | 85.85 | 80 | 82.82 | 1380 |
| title | 97.94 | 97.68 | 97.81 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.74** | **92.34** | **93.04** | 9116 |
| all fields (macro avg.) | 93.18 | 91.61 | 92.37 | 9116 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 84.61 | 83.15 | 83.87 | 1911 |
| authors | 95.56 | 95.47 | 95.52 | 1941 |
| first_author | 96.7 | 96.6 | 96.65 | 1941 |
| keywords | 81.73 | 76.16 | 78.84 | 1380 |
| title | 96.75 | 96.5 | 96.62 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.79** | **90.42** | **91.1** | 9116 |
| all fields (macro avg.) | 91.07 | 89.58 | 90.3 | 9116 |


#### Instance-level results

```
Total expected instances:   1943
Total correct instances:    218 (strict) 
Total correct instances:    868 (soft) 
Total correct instances:    1375 (Levenshtein) 
Total correct instances:    1257 (ObservedRatcliffObershelp) 

Instance-level recall:  11.22   (strict) 
Instance-level recall:  44.67   (soft) 
Instance-level recall:  70.77   (Levenshtein) 
Instance-level recall:  64.69   (RatcliffObershelp) 
```

## Citation metadata 

Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).


#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 81.77 | 75.55 | 78.54 | 85778 |
| date | 94.24 | 83.67 | 88.64 | 87067 |
| first_author | 88.65 | 81.88 | 85.13 | 85778 |
| inTitle | 71.86 | 71.29 | 71.57 | 81007 |
| issue | 88.56 | 82.58 | 85.47 | 16635 |
| page | 95.29 | 85.48 | 90.12 | 80501 |
| title | 79.17 | 75.37 | 77.23 | 80736 |
| volume | 94.38 | 89.28 | 91.76 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **86.35** | **80.41** | **83.28** | 597569 |
| all fields (macro avg.) | 86.74 | 80.64 | 83.56 | 597569 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 82.26 | 75.99 | 79 | 85778 |
| date | 94.24 | 83.67 | 88.64 | 87067 |
| first_author | 88.84 | 82.05 | 85.31 | 85778 |
| inTitle | 83.39 | 82.72 | 83.05 | 81007 |
| issue | 88.56 | 82.58 | 85.47 | 16635 |
| page | 95.29 | 85.48 | 90.12 | 80501 |
| title | 90.39 | 86.05 | 88.17 | 80736 |
| volume | 94.38 | 89.28 | 91.76 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.66** | **83.49** | **86.47** | 597569 |
| all fields (macro avg.) | 89.67 | 83.48 | 86.44 | 597569 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 87.92 | 81.23 | 84.44 | 85778 |
| date | 94.24 | 83.67 | 88.64 | 87067 |
| first_author | 89.05 | 82.25 | 85.51 | 85778 |
| inTitle | 84.66 | 83.98 | 84.32 | 81007 |
| issue | 88.56 | 82.58 | 85.47 | 16635 |
| page | 95.29 | 85.48 | 90.12 | 80501 |
| title | 92.67 | 88.22 | 90.39 | 80736 |
| volume | 94.38 | 89.28 | 91.76 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91** | **84.74** | **87.76** | 597569 |
| all fields (macro avg.) | 90.85 | 84.59 | 87.58 | 597569 |


#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 84.73 | 78.28 | 81.37 | 85778 |
| date | 94.24 | 83.67 | 88.64 | 87067 |
| first_author | 88.67 | 81.9 | 85.15 | 85778 |
| inTitle | 82.03 | 81.37 | 81.7 | 81007 |
| issue | 88.56 | 82.58 | 85.47 | 16635 |
| page | 95.29 | 85.48 | 90.12 | 80501 |
| title | 92.26 | 87.83 | 89.99 | 80736 |
| volume | 94.38 | 89.28 | 91.76 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.05** | **83.86** | **86.84** | 597569 |
| all fields (macro avg.) | 90.02 | 83.8 | 86.77 | 597569 |


#### Instance-level results

```
Total expected instances:       90125
Total extracted instances:      87336
Total correct instances:        38921 (strict) 
Total correct instances:        50854 (soft) 
Total correct instances:        55689 (Levenshtein) 
Total correct instances:        52233 (RatcliffObershelp) 

Instance-level precision:   44.56 (strict) 
Instance-level precision:   58.23 (soft) 
Instance-level precision:   63.76 (Levenshtein) 
Instance-level precision:   59.81 (RatcliffObershelp) 

Instance-level recall:  43.19   (strict) 
Instance-level recall:  56.43   (soft) 
Instance-level recall:  61.79   (Levenshtein) 
Instance-level recall:  57.96   (RatcliffObershelp) 

Instance-level f-score: 43.86 (strict) 
Instance-level f-score: 57.31 (soft) 
Instance-level f-score: 62.76 (Levenshtein) 
Instance-level f-score: 58.87 (RatcliffObershelp) 

Matching 1 :    67832

Matching 2 :    4081

Matching 3 :    2068

Matching 4 :    796

Total matches : 74777
```

#### Citation context resolution
```

Total expected references:   90125 - 46.38 references per article
Total predicted references:      87336 - 44.95 references per article

Total expected citation contexts:    139835 - 71.97 citation contexts per article
Total predicted citation contexts:   118720 - 61.1 citation contexts per article

Total correct predicted citation contexts:   98822 - 50.86 citation contexts per article
Total wrong predicted citation contexts:     19898 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     83.24
Recall citation contexts:    70.67
fscore citation contexts:    76.44
```

## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).


#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 31.41 | 25.33 | 28.04 | 7058 |
| reference_citation | 57.68 | 59.22 | 58.44 | 134196 |
| reference_figure | 64.56 | 63.11 | 63.82 | 19330 |
| reference_table | 82.84 | 83.5 | 83.17 | 7327 |
| section_title | 74.94 | 67.61 | 71.09 | 27619 |
| table_title | 57.94 | 55.26 | 56.57 | 3784 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **60.69** | **60.38** | **60.54** | 199314 |
| all fields (macro avg.) | 61.56 | 59.01 | 60.19 | 199314 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 78.54 | 63.35 | 70.13 | 7058 |
| reference_citation | 61.85 | 63.51 | 62.67 | 134196 |
| reference_figure | 65.13 | 63.67 | 64.39 | 19330 |
| reference_table | 82.99 | 83.65 | 83.32 | 7327 |
| section_title | 79.67 | 71.88 | 75.57 | 27619 |
| table_title | 82.07 | 78.28 | 80.13 | 3784 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **66.04** | **65.7** | **65.87** | 199314 |
| all fields (macro avg.) | 75.04 | 70.72 | 72.7 | 199314 |

Evaluation metrics produced in 1118.358 seconds

Evaluation metrics produced in 983.108 seconds
















