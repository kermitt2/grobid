# Benchmarking PubMed Central

## General

This is the end-to-end benchmarking result for GROBID version **0.7.0** against the `PMC_sample_1943` dataset, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

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
| abstract | 16.11 | 15.8 | 15.95 | 1911 |
| authors | 93.07 | 92.68 | 92.88 | 1941 |
| first_author | 96.07 | 95.67 | 95.87 | 1941 |
| keywords | 68.26 | 64.06 | 66.09 | 1380 |
| title | 86.84 | 86.62 | 86.73 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **72.71** | **71.58** | **72.14** | 9116 |
| all fields (macro avg.) | 72.07 | 70.97 | 71.5 | 9116 |


#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 60.91 | 59.76 | 60.33 | 1911 |
| authors | 93.53 | 93.15 | 93.34 | 1941 |
| first_author | 96.17 | 95.78 | 95.97 | 1941 |
| keywords | 76.53 | 71.81 | 74.09 | 1380 |
| title | 94.74 | 94.49 | 94.61 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **85.09** | **83.76** | **84.42** | 9116 |
| all fields (macro avg.) | 84.37 | 83 | 83.67 | 9116 |


#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 88.48 | 86.81 | 87.64 | 1911 |
| authors | 96.43 | 96.03 | 96.23 | 1941 |
| first_author | 96.48 | 96.08 | 96.28 | 1941 |
| keywords | 86.02 | 80.72 | 83.29 | 1380 |
| title | 97.83 | 97.58 | 97.71 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.58** | **92.12** | **92.85** | 9116 |
| all fields (macro avg.) | 93.05 | 91.45 | 92.23 | 9116 |


#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 84.64 | 83.05 | 83.84 | 1911 |
| authors | 94.98 | 94.59 | 94.79 | 1941 |
| first_author | 96.07 | 95.67 | 95.87 | 1941 |
| keywords | 81.93 | 76.88 | 79.33 | 1380 |
| title | 97.37 | 97.12 | 97.24 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.69** | **90.26** | **90.97** | 9116 |
| all fields (macro avg.) | 91 | 89.46 | 90.21 | 9116 |


#### Instance-level results

```
Total expected instances:   1943
Total correct instances:    218 (strict) 
Total correct instances:    869 (soft) 
Total correct instances:    1365 (Levenshtein) 
Total correct instances:    1256 (ObservedRatcliffObershelp) 

Instance-level recall:  11.22   (strict) 
Instance-level recall:  44.72   (soft) 
Instance-level recall:  70.25   (Levenshtein) 
Instance-level recall:  64.64   (RatcliffObershelp) 
```

## Citation metadata 

Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 82.47 | 75.38 | 78.77 | 85778 |
| date | 94.46 | 82.98 | 88.35 | 87067 |
| first_author | 89.11 | 81.43 | 85.09 | 85778 |
| inTitle | 72.17 | 70.95 | 71.56 | 81007 |
| issue | 89.04 | 83.14 | 85.99 | 16635 |
| page | 95.94 | 85.15 | 90.22 | 80501 |
| title | 79 | 74.48 | 76.67 | 80736 |
| volume | 95.92 | 89.01 | 92.34 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **86.86** | **79.99** | **83.29** | 597569 |
| all fields (macro avg.) | 87.26 | 80.31 | 83.62 | 597569 |


#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 83.01 | 75.87 | 79.28 | 85778 |
| date | 94.46 | 82.98 | 88.35 | 87067 |
| first_author | 89.3 | 81.6 | 85.28 | 85778 |
| inTitle | 83.54 | 82.13 | 82.83 | 81007 |
| issue | 89.04 | 83.14 | 85.99 | 16635 |
| page | 95.94 | 85.15 | 90.22 | 80501 |
| title | 90.45 | 85.28 | 87.79 | 80736 |
| volume | 95.92 | 89.01 | 92.34 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.2** | **83.06** | **86.48** | 597569 |
| all fields (macro avg.) | 90.21 | 83.15 | 86.51 | 597569 |


#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 88.31 | 80.72 | 84.35 | 85778 |
| date | 94.46 | 82.98 | 88.35 | 87067 |
| first_author | 89.5 | 81.79 | 85.47 | 85778 |
| inTitle | 84.84 | 83.4 | 84.11 | 81007 |
| issue | 89.04 | 83.14 | 85.99 | 16635 |
| page | 95.94 | 85.15 | 90.22 | 80501 |
| title | 92.83 | 87.52 | 90.1 | 80736 |
| volume | 95.92 | 89.01 | 92.34 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.5** | **84.26** | **87.73** | 597569 |
| all fields (macro avg.) | 91.36 | 84.21 | 87.62 | 597569 |


#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 85.37 | 78.03 | 81.54 | 85778 |
| date | 94.46 | 82.98 | 88.35 | 87067 |
| first_author | 89.13 | 81.44 | 85.11 | 85778 |
| inTitle | 82.17 | 80.78 | 81.47 | 81007 |
| issue | 89.04 | 83.14 | 85.99 | 16635 |
| page | 95.94 | 85.15 | 90.22 | 80501 |
| title | 92.38 | 87.1 | 89.66 | 80736 |
| volume | 95.92 | 89.01 | 92.34 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.58** | **83.41** | **86.85** | 597569 |
| all fields (macro avg.) | 90.55 | 83.45 | 86.83 | 597569 |


#### Instance-level results

```
Total expected instances:       90125
Total extracted instances:      87994
Total correct instances:        39070 (strict) 
Total correct instances:        50916 (soft) 
Total correct instances:        55618 (Levenshtein) 
Total correct instances:        52284 (RatcliffObershelp) 

Instance-level precision:   44.4 (strict) 
Instance-level precision:   57.86 (soft) 
Instance-level precision:   63.21 (Levenshtein) 
Instance-level precision:   59.42 (RatcliffObershelp) 

Instance-level recall:  43.35   (strict) 
Instance-level recall:  56.49   (soft) 
Instance-level recall:  61.71   (Levenshtein) 
Instance-level recall:  58.01   (RatcliffObershelp) 

Instance-level f-score: 43.87 (strict) 
Instance-level f-score: 57.17 (soft) 
Instance-level f-score: 62.45 (Levenshtein) 
Instance-level f-score: 58.71 (RatcliffObershelp) 

Matching 1 :    67183

Matching 2 :    4042

Matching 3 :    2332

Matching 4 :    739

Total matches : 74296
```

#### Citation context resolution
```

Total expected references:   90125 - 46.38 references per article
Total predicted references:      87994 - 45.29 references per article

Total expected citation contexts:    139835 - 71.97 citation contexts per article
Total predicted citation contexts:   121136 - 62.34 citation contexts per article

Total correct predicted citation contexts:   100034 - 51.48 citation contexts per article
Total wrong predicted citation contexts:     21102 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     82.58
Recall citation contexts:    71.54
fscore citation contexts:    76.66
```

## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 30.89 | 25.49 | 27.93 | 7058 |
| reference_citation | 57.33 | 59.18 | 58.24 | 134196 |
| reference_figure | 64.42 | 63.15 | 63.78 | 19330 |
| reference_table | 82.75 | 83.81 | 83.28 | 7327 |
| section_title | 77.06 | 67.58 | 72.01 | 27619 |
| table_title | 57.17 | 53.12 | 55.07 | 3784 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **60.59** | **60.32** | **60.46** | 199314 |
| all fields (macro avg.) | 61.6 | 58.72 | 60.05 | 199314 |


#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 79.17 | 65.33 | 71.59 | 7058 |
| reference_citation | 61.41 | 63.39 | 62.38 | 134196 |
| reference_figure | 65 | 63.71 | 64.35 | 19330 |
| reference_table | 82.9 | 83.96 | 83.43 | 7327 |
| section_title | 81.97 | 71.88 | 76.59 | 27619 |
| table_title | 81.85 | 76.06 | 78.85 | 3784 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **65.95** | **65.66** | **65.8** | 199314 |
| all fields (macro avg.) | 75.38 | 70.72 | 72.86 | 199314 |

Evaluation metrics produced in 916.228 seconds
