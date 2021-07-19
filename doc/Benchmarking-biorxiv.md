# Benchmarking biorXiv

## General

This is the end-to-end benchmarking result for GROBID version **0.7.0** against the `bioRxiv` test set (`biorxiv-10k-test-2000`), see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:
- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model
- **CRF Wapiti** as sequence labelling engine for all other models. 

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 1999 PDF preprints out of 2000 (1 PDF "too many blocks" interruption).

Runtime for processing 2000 PDF: **1169s** (1,71 PDF per second) on Ubuntu 16.04, 4 CPU i7-4790K (8 threads), 16GB RAM (workstation bought in 2015 for 1600 euros) and with a GeForce GTX 1050 Ti GPU.

## Header metadata 

Evaluation on 1999 random PDF files out of 2000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 2.24 | 2.16 | 2.2 | 1989 |
| authors | 84.06 | 83.13 | 83.59 | 1998 |
| first_author | 94.69 | 93.74 | 94.21 | 1996 |
| keywords | 59.91 | 60.91 | 60.4 | 839 |
| title | 86.84 | 84.14 | 85.47 | 1999 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **66.58** | **65.39** | **65.98** | 8821 |
| all fields (macro avg.) | 65.54 | 64.82 | 65.17 | 8821 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 57.42 | 55.46 | 56.42 | 1989 |
| authors | 84.62 | 83.68 | 84.15 | 1998 |
| first_author | 94.79 | 93.84 | 94.31 | 1996 |
| keywords | 65.77 | 66.87 | 66.31 | 839 |
| title | 92.36 | 89.49 | 90.9 | 1999 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **80.78** | **79.33** | **80.05** | 8821 |
| all fields (macro avg.) | 78.99 | 77.87 | 78.42 | 8821 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 76.83 | 74.21 | 75.5 | 1989 |
| authors | 92.46 | 91.44 | 91.95 | 1998 |
| first_author | 95.19 | 94.24 | 94.71 | 1996 |
| keywords | 78.31 | 79.62 | 78.96 | 839 |
| title | 95.25 | 92.3 | 93.75 | 1999 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **88.85** | **87.26** | **88.05** | 8821 |
| all fields (macro avg.) | 87.61 | 86.36 | 86.97 | 8821 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 73.87 | 71.34 | 72.58 | 1989 |
| authors | 88.16 | 87.19 | 87.67 | 1998 |
| first_author | 94.69 | 93.74 | 94.21 | 1996 |
| keywords | 71.28 | 72.47 | 71.87 | 839 |
| title | 93.96 | 91.05 | 92.48 | 1999 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **86.11** | **84.57** | **85.34** | 8821 |
| all fields (macro avg.) | 84.39 | 83.16 | 83.76 | 8821 |


#### Instance-level results

```
Total expected instances:   1999
Total correct instances:    34 (strict) 
Total correct instances:    753 (soft) 
Total correct instances:    1158 (Levenshtein) 
Total correct instances:    1026 (ObservedRatcliffObershelp) 

Instance-level recall:  1.7 (strict) 
Instance-level recall:  37.67   (soft) 
Instance-level recall:  57.93   (Levenshtein) 
Instance-level recall:  51.33   (RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 1999 random PDF files out of 2000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 86.67 | 78.42 | 82.34 | 97138 |
| date | 91.41 | 83.27 | 87.15 | 97585 |
| doi | 72.61 | 80.38 | 76.3 | 16893 |
| first_author | 93.54 | 84.57 | 88.83 | 97138 |
| inTitle | 81.53 | 77.13 | 79.27 | 96384 |
| issue | 93.61 | 85.63 | 89.44 | 30282 |
| page | 96.52 | 78.67 | 86.69 | 88558 |
| pmcid | 63.26 | 63.57 | 63.41 | 807 |
| pmid | 66.96 | 75.63 | 71.03 | 2093 |
| title | 84.29 | 80.7 | 82.45 | 92423 |
| volume | 95.57 | 92.79 | 94.16 | 87671 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.25** | **82.2** | **85.58** | 706972 |
| all fields (macro avg.) | 84.18 | 80.07 | 81.92 | 706972 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 87.94 | 79.56 | 83.54 | 97138 |
| date | 91.41 | 83.27 | 87.15 | 97585 |
| doi | 77.11 | 85.35 | 81.02 | 16893 |
| first_author | 94 | 84.99 | 89.27 | 97138 |
| inTitle | 91.18 | 86.26 | 88.65 | 96384 |
| issue | 93.61 | 85.63 | 89.44 | 30282 |
| page | 96.52 | 78.67 | 86.69 | 88558 |
| pmcid | 73.74 | 74.1 | 73.92 | 807 |
| pmid | 71.36 | 80.6 | 75.7 | 2093 |
| title | 92.48 | 88.55 | 90.47 | 92423 |
| volume | 95.57 | 92.79 | 94.16 | 87671 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.1** | **84.83** | **88.32** | 706972 |
| all fields (macro avg.) | 87.72 | 83.62 | 85.46 | 706972 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 92.69 | 83.86 | 88.06 | 97138 |
| date | 91.41 | 83.27 | 87.15 | 97585 |
| doi | 79.82 | 88.36 | 83.87 | 16893 |
| first_author | 94.15 | 85.12 | 89.41 | 97138 |
| inTitle | 92.13 | 87.16 | 89.58 | 96384 |
| issue | 93.61 | 85.63 | 89.44 | 30282 |
| page | 96.52 | 78.67 | 86.69 | 88558 |
| pmcid | 73.74 | 74.1 | 73.92 | 807 |
| pmid | 71.4 | 80.65 | 75.75 | 2093 |
| title | 95.33 | 91.27 | 93.26 | 92423 |
| volume | 95.57 | 92.79 | 94.16 | 87671 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.36** | **85.99** | **89.53** | 706972 |
| all fields (macro avg.) | 88.76 | 84.63 | 86.48 | 706972 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 89.79 | 81.23 | 85.3 | 97138 |
| date | 91.41 | 83.27 | 87.15 | 97585 |
| doi | 79.05 | 87.5 | 83.06 | 16893 |
| first_author | 93.59 | 84.61 | 88.87 | 97138 |
| inTitle | 89.87 | 85.03 | 87.38 | 96384 |
| issue | 93.61 | 85.63 | 89.44 | 30282 |
| page | 96.52 | 78.67 | 86.69 | 88558 |
| pmcid | 63.26 | 63.57 | 63.41 | 807 |
| pmid | 66.96 | 75.63 | 71.03 | 2093 |
| title | 94.52 | 90.5 | 92.47 | 92423 |
| volume | 95.57 | 92.79 | 94.16 | 87671 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.42** | **85.12** | **88.62** | 706972 |
| all fields (macro avg.) | 86.74 | 82.59 | 84.45 | 706972 |


#### Instance-level results

```
Total expected instances:       98753
Total extracted instances:      103498
Total correct instances:        41273 (strict) 
Total correct instances:        51887 (soft) 
Total correct instances:        56012 (Levenshtein) 
Total correct instances:        52881 (RatcliffObershelp) 

Instance-level precision:   39.88 (strict) 
Instance-level precision:   50.13 (soft) 
Instance-level precision:   54.12 (Levenshtein) 
Instance-level precision:   51.09 (RatcliffObershelp) 

Instance-level recall:  41.79   (strict) 
Instance-level recall:  52.54   (soft) 
Instance-level recall:  56.72   (Levenshtein) 
Instance-level recall:  53.55   (RatcliffObershelp) 

Instance-level f-score: 40.81 (strict) 
Instance-level f-score: 51.31 (soft) 
Instance-level f-score: 55.39 (Levenshtein) 
Instance-level f-score: 52.29 (RatcliffObershelp) 

Matching 1 :    75680

Matching 2 :    4230

Matching 3 :    6093

Matching 4 :    2284

Total matches : 88287
```


#### Citation context resolution
```

Total expected references:   98712 - 49.38 references per article
Total predicted references:      103442 - 51.75 references per article

Total expected citation contexts:    142737 - 71.4 citation contexts per article
Total predicted citation contexts:   134945 - 67.51 citation contexts per article

Total correct predicted citation contexts:   111261 - 55.66 citation contexts per article
Total wrong predicted citation contexts:     23684 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     82.45
Recall citation contexts:    77.95
fscore citation contexts:    80.14
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 1999 random PDF files out of 2000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 4.13 | 3.57 | 3.83 | 13162 |
| reference_citation | 70.63 | 70.48 | 70.55 | 147404 |
| reference_figure | 73.72 | 65.91 | 69.6 | 47965 |
| reference_table | 48.12 | 80.66 | 60.28 | 5951 |
| section_title | 71.28 | 71.04 | 71.16 | 32384 |
| table_title | 4.54 | 4.09 | 4.3 | 2957 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **66.55** | **65.6** | **66.07** | 249823 |
| all fields (macro avg.) | 45.4 | 49.29 | 46.62 | 249823 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 67.36 | 58.3 | 62.51 | 13162 |
| reference_citation | 82.28 | 82.1 | 82.19 | 147404 |
| reference_figure | 74.42 | 66.53 | 70.25 | 47965 |
| reference_table | 48.55 | 81.38 | 60.81 | 5951 |
| section_title | 75.06 | 74.8 | 74.93 | 32384 |
| table_title | 50.73 | 45.72 | 48.1 | 2957 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **77.57** | **76.47** | **77.01** | 249823 |
| all fields (macro avg.) | 66.4 | 68.14 | 66.47 | 249823 |

Evaluation metrics produced in 1132.55 seconds













































## Header metadata 

Evaluation on 1998 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 2.19 | 2.06 | 2.12 | 1988 |
| authors | 73.78 | 70.31 | 72 | 1997 |
| first_author | 93.12 | 88.82 | 90.92 | 1995 |
| keywords | 55.41 | 54.95 | 55.18 | 839 |
| title | 78.64 | 71.32 | 74.8 | 1998 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **61.3** | **57.88** | **59.54** | 8817 |
| all fields (macro avg.) | 60.63 | 57.49 | 59 | 8817 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 55.12 | 51.96 | 53.5 | 1988 |
| authors | 75.56 | 72.01 | 73.74 | 1997 |
| first_author | 93.69 | 89.37 | 91.48 | 1995 |
| keywords | 60.22 | 59.71 | 59.96 | 839 |
| title | 90.01 | 81.63 | 85.62 | 1998 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **76.72** | **72.43** | **74.51** | 8817 |
| all fields (macro avg.) | 74.92 | 70.94 | 72.86 | 8817 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 73.91 | 69.67 | 71.72 | 1988 |
| authors | 90.54 | 86.28 | 88.36 | 1997 |
| first_author | 94.48 | 90.13 | 92.25 | 1995 |
| keywords | 72.36 | 71.75 | 72.05 | 839 |
| title | 93.65 | 84.93 | 89.08 | 1998 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **86.56** | **81.72** | **84.07** | 8817 |
| all fields (macro avg.) | 84.99 | 80.55 | 82.69 | 8817 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 70.7 | 66.65 | 68.62 | 1988 |
| authors | 83.34 | 79.42 | 81.33 | 1997 |
| first_author | 93.17 | 88.87 | 90.97 | 1995 |
| keywords | 65.87 | 65.32 | 65.59 | 839 |
| title | 92.05 | 83.48 | 87.56 | 1998 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **82.89** | **78.26** | **80.51** | 8817 |
| all fields (macro avg.) | 81.03 | 76.75 | 78.81 | 8817 |


#### Instance-level results

```
Total expected instances:   1998
Total correct instances:    30 (strict) 
Total correct instances:    596 (soft) 
Total correct instances:    1000 (Levenshtein) 
Total correct instances:    862 (ObservedRatcliffObershelp) 

Instance-level recall:  1.5 (strict) 
Instance-level recall:  29.83   (soft) 
Instance-level recall:  50.05   (Levenshtein) 
Instance-level recall:  43.14   (RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 1998 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 86.86 | 70.58 | 77.88 | 97092 |
| date | 90.05 | 74.78 | 81.71 | 97539 |
| doi | 72.86 | 72.76 | 72.81 | 16893 |
| first_author | 93.63 | 76.04 | 83.92 | 97092 |
| inTitle | 80.98 | 69.64 | 74.88 | 96338 |
| issue | 95.02 | 78.68 | 86.08 | 30279 |
| page | 95.07 | 71.56 | 81.66 | 88516 |
| pmcid | 66.07 | 59.11 | 62.39 | 807 |
| pmid | 67.75 | 67.56 | 67.66 | 2093 |
| title | 84.05 | 71.76 | 77.42 | 92377 |
| volume | 94.83 | 83.41 | 88.75 | 87626 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **88.83** | **74.03** | **80.76** | 706652 |
| all fields (macro avg.) | 84.29 | 72.35 | 77.74 | 706652 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 88.12 | 71.61 | 79.01 | 97092 |
| date | 90.05 | 74.78 | 81.71 | 97539 |
| doi | 77.64 | 77.54 | 77.59 | 16893 |
| first_author | 94.08 | 76.4 | 84.33 | 97092 |
| inTitle | 90.26 | 77.63 | 83.47 | 96338 |
| issue | 95.02 | 78.68 | 86.08 | 30279 |
| page | 95.07 | 71.56 | 81.66 | 88516 |
| pmcid | 76.87 | 68.77 | 72.6 | 807 |
| pmid | 71.78 | 71.57 | 71.67 | 2093 |
| title | 92.17 | 78.7 | 84.9 | 92377 |
| volume | 94.83 | 83.41 | 88.75 | 87626 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.62** | **76.35** | **83.29** | 706652 |
| all fields (macro avg.) | 87.81 | 75.51 | 81.07 | 706652 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 92.75 | 75.36 | 83.16 | 97092 |
| date | 90.05 | 74.78 | 81.71 | 97539 |
| doi | 79.08 | 78.98 | 79.03 | 16893 |
| first_author | 94.24 | 76.53 | 84.46 | 97092 |
| inTitle | 91.1 | 78.34 | 84.24 | 96338 |
| issue | 95.02 | 78.68 | 86.08 | 30279 |
| page | 95.07 | 71.56 | 81.66 | 88516 |
| pmcid | 76.87 | 68.77 | 72.6 | 807 |
| pmid | 71.83 | 71.62 | 71.72 | 2093 |
| title | 95.31 | 81.38 | 87.79 | 92377 |
| volume | 94.83 | 83.41 | 88.75 | 87626 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.83** | **77.37** | **84.4** | 706652 |
| all fields (macro avg.) | 88.74 | 76.31 | 81.93 | 706652 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 89.91 | 73.06 | 80.61 | 97092 |
| date | 90.05 | 74.78 | 81.71 | 97539 |
| doi | 78.23 | 78.13 | 78.18 | 16893 |
| first_author | 93.68 | 76.08 | 83.97 | 97092 |
| inTitle | 89.01 | 76.55 | 82.31 | 96338 |
| issue | 95.02 | 78.68 | 86.08 | 30279 |
| page | 95.07 | 71.56 | 81.66 | 88516 |
| pmcid | 66.07 | 59.11 | 62.39 | 807 |
| pmid | 67.75 | 67.56 | 67.66 | 2093 |
| title | 94.33 | 80.54 | 86.89 | 92377 |
| volume | 94.83 | 83.41 | 88.75 | 87626 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.9** | **76.59** | **83.55** | 706652 |
| all fields (macro avg.) | 86.72 | 74.5 | 80.02 | 706652 |


#### Instance-level results

```
Total expected instances:       98707
Total extracted instances:      96483
Total correct instances:        37769 (strict) 
Total correct instances:        47362 (soft) 
Total correct instances:        50792 (Levenshtein) 
Total correct instances:        48107 (RatcliffObershelp) 

Instance-level precision:   39.15 (strict) 
Instance-level precision:   49.09 (soft) 
Instance-level precision:   52.64 (Levenshtein) 
Instance-level precision:   49.86 (RatcliffObershelp) 

Instance-level recall:  38.26   (strict) 
Instance-level recall:  47.98   (soft) 
Instance-level recall:  51.46   (Levenshtein) 
Instance-level recall:  48.74   (RatcliffObershelp) 

Instance-level f-score: 38.7 (strict) 
Instance-level f-score: 48.53 (soft) 
Instance-level f-score: 52.04 (Levenshtein) 
Instance-level f-score: 49.29 (RatcliffObershelp) 

Matching 1 :    67251

Matching 2 :    4362

Matching 3 :    5614

Matching 4 :    2042

Total matches : 79269
```


#### Citation context resolution
```

Total expected references:   98705 - 49.4 references per article
Total predicted references:      96483 - 48.29 references per article

Total expected citation contexts:    142688 - 71.42 citation contexts per article
Total predicted citation contexts:   123504 - 61.81 citation contexts per article

Total correct predicted citation contexts:   98549 - 49.32 citation contexts per article
Total wrong predicted citation contexts:     24955 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     79.79
Recall citation contexts:    69.07
fscore citation contexts:    74.04
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 1998 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 3.29 | 2.73 | 2.99 | 13147 |
| reference_citation | 70.52 | 66.38 | 68.39 | 147296 |
| reference_figure | 73.83 | 67.2 | 70.36 | 47901 |
| reference_table | 49.41 | 78.97 | 60.79 | 5938 |
| section_title | 69.31 | 67.48 | 68.38 | 32360 |
| table_title | 3.52 | 2.91 | 3.19 | 2951 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **66.33** | **62.87** | **64.56** | 249593 |
| all fields (macro avg.) | 44.98 | 47.61 | 45.68 | 249593 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 64.44 | 53.45 | 58.43 | 13147 |
| reference_citation | 82.57 | 77.72 | 80.07 | 147296 |
| reference_figure | 74.45 | 67.76 | 70.95 | 47901 |
| reference_table | 49.76 | 79.52 | 61.21 | 5938 |
| section_title | 73.07 | 71.14 | 72.1 | 32360 |
| table_title | 48.36 | 40.02 | 43.8 | 2951 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **77.31** | **73.28** | **75.24** | 249593 |
| all fields (macro avg.) | 65.44 | 64.94 | 64.43 | 249593 |

Evaluation metrics produced in 1133.739 seconds


