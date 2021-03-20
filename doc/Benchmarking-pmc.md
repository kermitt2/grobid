# Benchmarking PubMed Central

## General

This is the end-to-end benchmarking result for GROBID version **0.6.2** against the `PMC_sample_1943` dataset, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using **CRF Wapiti** only as sequence labelling engine. Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service are similar but much slower). 

More recent versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Deep Learning models provide higher accuracy at the cost of slower runtime and more expensive CPU resources. 

Evaluation on 1943 random PDF PMC files out of 1943 PDF from 1943 different journals (0 PDF parsing failure).

Runtime for processing 1943 PDF: **836s** (2,33 PDF per second) on Ubuntu 16.04, 4 CPU i7-4790K (8 threads), 16GB RAM (workstation bought in 2015 for 1800 euros).

## Header metadata 

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 15.85 | 15.59 | 15.72 | 1911 |
| authors | 92.3 | 92.07 | 92.18 | 1941 |
| first_author | 95.97 | 95.72 | 95.85 | 1941 |
| keywords | 66.5 | 58.12 | 62.03 | 1380 |
| title | 86.92 | 86.21 | 86.56 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **72.26** | **70.43** | **71.33** | 9116 |
| all fields (macro avg.) | 71.51 | 69.54 | 70.47 | 9116 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 59.2 | 58.24 | 58.72 | 1911 |
| authors | 92.77 | 92.53 | 92.65 | 1941 |
| first_author | 96.07 | 95.83 | 95.95 | 1941 |
| keywords | 75.46 | 65.94 | 70.38 | 1380 |
| title | 94.45 | 93.67 | 94.06 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **84.4** | **82.26** | **83.32** | 9116 |
| all fields (macro avg.) | 83.59 | 81.24 | 82.35 | 9116 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 86.81 | 85.4 | 86.1 | 1911 |
| authors | 95.92 | 95.67 | 95.8 | 1941 |
| first_author | 96.38 | 96.14 | 96.26 | 1941 |
| keywords | 85.24 | 74.49 | 79.51 | 1380 |
| title | 97.41 | 96.6 | 97 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.97** | **90.61** | **91.77** | 9116 |
| all fields (macro avg.) | 92.35 | 89.66 | 90.93 | 9116 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 82.23 | 80.9 | 81.56 | 1911 |
| authors | 94.37 | 94.13 | 94.25 | 1941 |
| first_author | 95.97 | 95.72 | 95.85 | 1941 |
| keywords | 81.01 | 70.8 | 75.56 | 1380 |
| title | 97.04 | 96.24 | 96.64 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.92** | **88.61** | **89.75** | 9116 |
| all fields (macro avg.) | 90.13 | 87.56 | 88.77 | 9116 |


#### Instance-level results

```
Total expected instances:   1943
Total correct instances:    202 (strict) 
Total correct instances:    797 (soft) 
Total correct instances:    1259 (Levenshtein) 
Total correct instances:    1153 (ObservedRatcliffObershelp) 

Instance-level recall:  10.4    (strict) 
Instance-level recall:  41.02   (soft) 
Instance-level recall:  64.8    (Levenshtein) 
Instance-level recall:  59.34   (RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 84.49 | 74.79 | 79.35 | 85778 |
| date | 93.28 | 81.48 | 86.98 | 87067 |
| first_author | 90.99 | 80.53 | 85.44 | 85778 |
| inTitle | 72.07 | 69.53 | 70.78 | 81007 |
| issue | 89.27 | 82.97 | 86 | 16635 |
| page | 94.85 | 83.56 | 88.85 | 80501 |
| title | 78.97 | 72.17 | 75.42 | 80736 |
| volume | 95.44 | 87.12 | 91.09 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **87.07** | **78.58** | **82.61** | 597569 |
| all fields (macro avg.) | 87.42 | 79.02 | 82.99 | 597569 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 85.18 | 75.41 | 80 | 85778 |
| date | 93.28 | 81.48 | 86.98 | 87067 |
| first_author | 91.28 | 80.78 | 85.71 | 85778 |
| inTitle | 83.61 | 80.66 | 82.11 | 81007 |
| issue | 89.27 | 82.97 | 86 | 16635 |
| page | 94.85 | 83.56 | 88.85 | 80501 |
| title | 90.08 | 82.34 | 86.03 | 80736 |
| volume | 95.44 | 87.12 | 91.09 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.4** | **81.59** | **85.77** | 597569 |
| all fields (macro avg.) | 90.38 | 81.79 | 85.85 | 597569 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 89.89 | 79.57 | 84.42 | 85778 |
| date | 93.28 | 81.48 | 86.98 | 87067 |
| first_author | 91.35 | 80.85 | 85.78 | 85778 |
| inTitle | 84.63 | 81.65 | 83.11 | 81007 |
| issue | 89.27 | 82.97 | 86 | 16635 |
| page | 94.85 | 83.56 | 88.85 | 80501 |
| title | 93.24 | 85.22 | 89.05 | 80736 |
| volume | 95.44 | 87.12 | 91.09 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.66** | **82.72** | **86.96** | 597569 |
| all fields (macro avg.) | 91.5 | 82.8 | 86.91 | 597569 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 87.3 | 77.28 | 81.99 | 85778 |
| date | 93.28 | 81.48 | 86.98 | 87067 |
| first_author | 91.01 | 80.54 | 85.46 | 85778 |
| inTitle | 82.2 | 79.3 | 80.73 | 81007 |
| issue | 89.27 | 82.97 | 86 | 16635 |
| page | 94.85 | 83.56 | 88.85 | 80501 |
| title | 92.26 | 84.33 | 88.12 | 80736 |
| volume | 95.44 | 87.12 | 91.09 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.76** | **81.91** | **86.11** | 597569 |
| all fields (macro avg.) | 90.7 | 82.07 | 86.15 | 597569 |


#### Instance-level results

```
Total expected instances:       90125
Total extracted instances:      88824
Total correct instances:        38412 (strict) 
Total correct instances:        49926 (soft) 
Total correct instances:        54186 (Levenshtein) 
Total correct instances:        51130 (RatcliffObershelp) 

Instance-level precision:   43.25 (strict) 
Instance-level precision:   56.21 (soft) 
Instance-level precision:   61 (Levenshtein) 
Instance-level precision:   57.56 (RatcliffObershelp) 

Instance-level recall:  42.62   (strict) 
Instance-level recall:  55.4    (soft) 
Instance-level recall:  60.12   (Levenshtein) 
Instance-level recall:  56.73   (RatcliffObershelp) 

Instance-level f-score: 42.93 (strict) 
Instance-level f-score: 55.8 (soft) 
Instance-level f-score: 60.56 (Levenshtein) 
Instance-level f-score: 57.14 (RatcliffObershelp) 

Matching 1 :    64923

Matching 2 :    4694

Matching 3 :    2744

Matching 4 :    681

Total matches : 73042
```


#### Citation context resolution
```

Total expected references:   90125 - 46.38 references per article
Total predicted references:      88824 - 45.71 references per article

Total expected citation contexts:    139835 - 71.97 citation contexts per article
Total predicted citation contexts:   120560 - 62.05 citation contexts per article

Total correct predicted citation contexts:   98016 - 50.45 citation contexts per article
Total wrong predicted citation contexts:     22544 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     81.3
Recall citation contexts:    70.09
fscore citation contexts:    75.28
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 27.92 | 21.75 | 24.45 | 7058 |
| reference_citation | 57.31 | 58.97 | 58.13 | 134196 |
| reference_figure | 63.44 | 63.91 | 63.67 | 19330 |
| reference_table | 82.74 | 84.21 | 83.47 | 7327 |
| section_title | 75.63 | 67.1 | 71.11 | 27619 |
| table_title | 57.69 | 54.84 | 56.23 | 3784 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **60.32** | **60.11** | **60.22** | 199314 |
| all fields (macro avg.) | 60.79 | 58.46 | 59.51 | 199314 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 73.19 | 57 | 64.09 | 7058 |
| reference_citation | 61.47 | 63.25 | 62.34 | 134196 |
| reference_figure | 63.97 | 64.44 | 64.21 | 19330 |
| reference_table | 82.9 | 84.37 | 83.63 | 7327 |
| section_title | 80.56 | 71.47 | 75.75 | 27619 |
| table_title | 80.51 | 76.53 | 78.47 | 3784 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **65.54** | **65.31** | **65.43** | 199314 |
| all fields (macro avg.) | 73.77 | 69.51 | 71.41 | 199314 |

Evaluation metrics produced in 924.038 seconds

