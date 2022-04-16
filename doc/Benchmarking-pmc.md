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
| authors | 93.54 | 93.2 | 93.37 | 1941 |
| first_author | 96.64 | 96.29 | 96.46 | 1941 |
| keywords | 68.47 | 64.2 | 66.27 | 1380 |
| title | 86.43 | 86.21 | 86.32 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **72.88** | **71.75** | **72.31** | 9116 |
| all fields (macro avg.) | 72.24 | 71.14 | 71.67 | 9116 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 60.96 | 59.81 | 60.38 | 1911 |
| authors | 94.16 | 93.82 | 93.99 | 1941 |
| first_author | 96.79 | 96.45 | 96.62 | 1941 |
| keywords | 76.74 | 71.96 | 74.27 | 1380 |
| title | 93.4 | 93.15 | 93.27 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **85.11** | **83.8** | **84.45** | 9116 |
| all fields (macro avg.) | 84.41 | 83.04 | 83.71 | 9116 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 88.48 | 86.81 | 87.64 | 1911 |
| authors | 96.54 | 96.19 | 96.36 | 1941 |
| first_author | 97.1 | 96.75 | 96.93 | 1941 |
| keywords | 86.24 | 80.87 | 83.47 | 1380 |
| title | 97.78 | 97.53 | 97.66 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.76** | **92.31** | **93.03** | 9116 |
| all fields (macro avg.) | 93.23 | 91.63 | 92.41 | 9116 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 84.64 | 83.05 | 83.84 | 1911 |
| authors | 95.5 | 95.16 | 95.33 | 1941 |
| first_author | 96.64 | 96.29 | 96.46 | 1941 |
| keywords | 82.15 | 77.03 | 79.51 | 1380 |
| title | 96.54 | 96.29 | 96.42 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.78** | **90.36** | **91.06** | 9116 |
| all fields (macro avg.) | 91.09 | 89.56 | 90.31 | 9116 |


#### Instance-level results

```
Total expected instances:   1943
Total correct instances:    220 (strict) 
Total correct instances:    874 (soft) 
Total correct instances:    1382 (Levenshtein) 
Total correct instances:    1263 (ObservedRatcliffObershelp) 

Instance-level recall:  11.32   (strict) 
Instance-level recall:  44.98   (soft) 
Instance-level recall:  71.13   (Levenshtein) 
Instance-level recall:  65  (RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 82.66 | 75.42 | 78.87 | 85778 |
| date | 94.52 | 82.97 | 88.37 | 87067 |
| first_author | 89.11 | 81.28 | 85.01 | 85778 |
| inTitle | 72.09 | 70.9 | 71.49 | 81007 |
| issue | 89.36 | 82.99 | 86.06 | 16635 |
| page | 95.82 | 85.15 | 90.17 | 80501 |
| title | 79.18 | 74.78 | 76.92 | 80736 |
| volume | 95.58 | 88.89 | 92.11 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **86.86** | **79.99** | **83.28** | 597569 |
| all fields (macro avg.) | 87.29 | 80.3 | 83.63 | 597569 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 83.19 | 75.9 | 79.38 | 85778 |
| date | 94.52 | 82.97 | 88.37 | 87067 |
| first_author | 89.3 | 81.45 | 85.19 | 85778 |
| inTitle | 83.41 | 82.02 | 82.71 | 81007 |
| issue | 89.36 | 82.99 | 86.06 | 16635 |
| page | 95.82 | 85.15 | 90.17 | 80501 |
| title | 90.45 | 85.42 | 87.86 | 80736 |
| volume | 95.58 | 88.89 | 92.11 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.16** | **83.03** | **86.45** | 597569 |
| all fields (macro avg.) | 90.2 | 83.1 | 86.48 | 597569 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 88.43 | 80.69 | 84.38 | 85778 |
| date | 94.52 | 82.97 | 88.37 | 87067 |
| first_author | 89.5 | 81.64 | 85.39 | 85778 |
| inTitle | 84.73 | 83.32 | 84.02 | 81007 |
| issue | 89.36 | 82.99 | 86.06 | 16635 |
| page | 95.82 | 85.15 | 90.17 | 80501 |
| title | 92.73 | 87.57 | 90.08 | 80736 |
| volume | 95.58 | 88.89 | 92.11 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.44** | **84.21** | **87.68** | 597569 |
| all fields (macro avg.) | 91.33 | 84.15 | 87.57 | 597569 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 85.54 | 78.05 | 81.62 | 85778 |
| date | 94.52 | 82.97 | 88.37 | 87067 |
| first_author | 89.12 | 81.29 | 85.03 | 85778 |
| inTitle | 82.03 | 80.67 | 81.34 | 81007 |
| issue | 89.36 | 82.99 | 86.06 | 16635 |
| page | 95.82 | 85.15 | 90.17 | 80501 |
| title | 92.32 | 87.18 | 89.67 | 80736 |
| volume | 95.58 | 88.89 | 92.11 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.53** | **83.37** | **86.8** | 597569 |
| all fields (macro avg.) | 90.54 | 83.4 | 86.8 | 597569 |


#### Instance-level results

```
Total expected instances:       90125
Total extracted instances:      87993
Total correct instances:        39270 (strict) 
Total correct instances:        51037 (soft) 
Total correct instances:        55672 (Levenshtein) 
Total correct instances:        52383 (RatcliffObershelp) 

Instance-level precision:   44.63 (strict) 
Instance-level precision:   58 (soft) 
Instance-level precision:   63.27 (Levenshtein) 
Instance-level precision:   59.53 (RatcliffObershelp) 

Instance-level recall:  43.57   (strict) 
Instance-level recall:  56.63   (soft) 
Instance-level recall:  61.77   (Levenshtein) 
Instance-level recall:  58.12   (RatcliffObershelp) 

Instance-level f-score: 44.09 (strict) 
Instance-level f-score: 57.31 (soft) 
Instance-level f-score: 62.51 (Levenshtein) 
Instance-level f-score: 58.82 (RatcliffObershelp) 

Matching 1 :    67350

Matching 2 :    3900

Matching 3 :    2255

Matching 4 :    708

Total matches : 74213
```


#### Citation context resolution
```

Total expected references:   90125 - 46.38 references per article
Total predicted references:      87993 - 45.29 references per article

Total expected citation contexts:    139835 - 71.97 citation contexts per article
Total predicted citation contexts:   120230 - 61.88 citation contexts per article

Total correct predicted citation contexts:   99811 - 51.37 citation contexts per article
Total wrong predicted citation contexts:     20419 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     83.02
Recall citation contexts:    71.38
fscore citation contexts:    76.76
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 1943 random PDF files out of 1943 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 30.87 | 25.49 | 27.92 | 7058 |
| reference_citation | 57.64 | 59.34 | 58.48 | 134196 |
| reference_figure | 64.38 | 63.12 | 63.75 | 19330 |
| reference_table | 82.73 | 83.8 | 83.26 | 7327 |
| section_title | 77.05 | 67.56 | 72 | 27619 |
| table_title | 57.13 | 53.17 | 55.08 | 3784 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **60.8** | **60.43** | **60.62** | 199314 |
| all fields (macro avg.) | 61.63 | 58.75 | 60.08 | 199314 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 79.05 | 65.27 | 71.5 | 7058 |
| reference_citation | 61.87 | 63.7 | 62.77 | 134196 |
| reference_figure | 64.96 | 63.69 | 64.32 | 19330 |
| reference_table | 82.88 | 83.95 | 83.41 | 7327 |
| section_title | 81.96 | 71.87 | 76.58 | 27619 |
| table_title | 81.74 | 76.08 | 78.81 | 3784 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **66.27** | **65.87** | **66.07** | 199314 |
| all fields (macro avg.) | 75.41 | 70.76 | 72.9 | 199314 |

Evaluation metrics produced in 983.108 seconds
















