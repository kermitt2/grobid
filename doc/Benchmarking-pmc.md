# Benchmarking PubMed Central

## General

This is the end-to-end benchmarking result for GROBID version **0.7.1** against the `PMC_sample_1943` dataset, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

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
| authors | 82.65 | 75.43 | 78.87 | 85778 |
| date | 94.5 | 83.02 | 88.39 | 87067 |
| first_author | 89.17 | 81.36 | 85.09 | 85778 |
| inTitle | 72.28 | 71.02 | 71.64 | 81007 |
| issue | 89.78 | 82.45 | 85.96 | 16635 |
| page | 95.89 | 85.27 | 90.27 | 80501 |
| title | 79.24 | 74.79 | 76.95 | 80736 |
| volume | 95.66 | 88.91 | 92.16 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **86.93** | **80.03** | **83.34** | 597569 |
| all fields (macro avg.) | 87.4 | 80.28 | 83.67 | 597569 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 83.18 | 75.91 | 79.38 | 85778 |
| date | 94.5 | 83.02 | 88.39 | 87067 |
| first_author | 89.36 | 81.54 | 85.27 | 85778 |
| inTitle | 83.83 | 82.37 | 83.09 | 81007 |
| issue | 89.78 | 82.45 | 85.96 | 16635 |
| page | 95.89 | 85.27 | 90.27 | 80501 |
| title | 90.53 | 85.44 | 87.91 | 80736 |
| volume | 95.66 | 88.91 | 92.16 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.27** | **83.1** | **86.54** | 597569 |
| all fields (macro avg.) | 90.34 | 83.12 | 86.55 | 597569 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 88.46 | 80.73 | 84.42 | 85778 |
| date | 94.5 | 83.02 | 88.39 | 87067 |
| first_author | 89.57 | 81.72 | 85.47 | 85778 |
| inTitle | 85.13 | 83.65 | 84.39 | 81007 |
| issue | 89.78 | 82.45 | 85.96 | 16635 |
| page | 95.89 | 85.27 | 90.27 | 80501 |
| title | 92.9 | 87.68 | 90.21 | 80736 |
| volume | 95.66 | 88.91 | 92.16 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.57** | **84.3** | **87.78** | 597569 |
| all fields (macro avg.) | 91.49 | 84.18 | 87.66 | 597569 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 85.54 | 78.07 | 81.63 | 85778 |
| date | 94.5 | 83.02 | 88.39 | 87067 |
| first_author | 89.19 | 81.38 | 85.11 | 85778 |
| inTitle | 82.43 | 80.99 | 81.7 | 81007 |
| issue | 89.78 | 82.45 | 85.96 | 16635 |
| page | 95.89 | 85.27 | 90.27 | 80501 |
| title | 92.45 | 87.26 | 89.78 | 80736 |
| volume | 95.66 | 88.91 | 92.16 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.65** | **83.45** | **86.9** | 597569 |
| all fields (macro avg.) | 90.68 | 83.42 | 86.88 | 597569 |


#### Instance-level results

```
Total expected instances:       90125
Total extracted instances:      88028
Total correct instances:        39137 (strict) 
Total correct instances:        51019 (soft) 
Total correct instances:        55703 (Levenshtein) 
Total correct instances:        52379 (RatcliffObershelp) 

Instance-level precision:   44.46 (strict) 
Instance-level precision:   57.96 (soft) 
Instance-level precision:   63.28 (Levenshtein) 
Instance-level precision:   59.5 (RatcliffObershelp) 

Instance-level recall:  43.43   (strict) 
Instance-level recall:  56.61   (soft) 
Instance-level recall:  61.81   (Levenshtein) 
Instance-level recall:  58.12   (RatcliffObershelp) 

Instance-level f-score: 43.94 (strict) 
Instance-level f-score: 57.28 (soft) 
Instance-level f-score: 62.53 (Levenshtein) 
Instance-level f-score: 58.8 (RatcliffObershelp) 

Matching 1 :    67326

Matching 2 :    3954

Matching 3 :    2342

Matching 4 :    709

Total matches : 74331
```


#### Citation context resolution
```

Total expected references:   90125 - 46.38 references per article
Total predicted references:      88028 - 45.31 references per article

Total expected citation contexts:    139835 - 71.97 citation contexts per article
Total predicted citation contexts:   120327 - 61.93 citation contexts per article

Total correct predicted citation contexts:   100008 - 51.47 citation contexts per article
Total wrong predicted citation contexts:     20319 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     83.11
Recall citation contexts:    71.52
fscore citation contexts:    76.88
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
















