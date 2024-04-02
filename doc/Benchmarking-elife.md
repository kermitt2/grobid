# Benchmarking eLife

## General

This is the end-to-end benchmarking result for GROBID version **0.7.3** against the `eLife` test set, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models.  

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 984 PDF preprints out of 984 (no failure).

Runtime for processing 984 PDF: **2002s** (2.03 seconds per PDF) on Ubuntu 16.04, 4 CPU i7-4790K (8 threads), 16GB RAM (workstation bought in 2015 for 1600 euros) and with a GeForce GTX 1050 Ti GPU.

Note: with CRF only models runtime is 492s (0.50 seconds per PDF). 


## Header metadata 

Evaluation on 984 random PDF files out of 984 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 9.39 | 8.94 | 9.16 | 984 |
| authors | 72.68 | 72.53 | 72.61 | 983 |
| first_author | 91.03 | 90.94 | 90.98 | 982 |
| title | 86.84 | 86.48 | 86.66 | 984 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **65.61** | **64.71** | **65.16** | 3933 |
| all fields (macro avg.) | 64.98 | 64.72 | 64.85 | 3933 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 24.12 | 22.97 | 23.53 | 984 |
| authors | 72.88 | 72.74 | 72.81 | 983 |
| first_author | 91.03 | 90.94 | 90.98 | 982 |
| title | 95 | 94.61 | 94.81 | 984 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **71.28** | **70.3** | **70.79** | 3933 |
| all fields (macro avg.) | 70.76 | 70.31 | 70.53 | 3933 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 48.67 | 46.34 | 47.48 | 984 |
| authors | 86.34 | 86.16 | 86.25 | 983 |
| first_author | 91.34 | 91.24 | 91.29 | 982 |
| title | 96.33 | 95.93 | 96.13 | 984 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **81.03** | **79.91** | **80.47** | 3933 |
| all fields (macro avg.) | 80.67 | 79.92 | 80.29 | 3933 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 46.53 | 44.31 | 45.39 | 984 |
| authors | 78.29 | 78.13 | 78.21 | 983 |
| first_author | 91.03 | 90.94 | 90.98 | 982 |
| title | 96.33 | 95.93 | 96.13 | 984 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **78.4** | **77.32** | **77.85** | 3933 |
| all fields (macro avg.) | 78.04 | 77.33 | 77.68 | 3933 |


#### Instance-level results

```
Total expected instances:   984
Total correct instances:    75 (strict) 
Total correct instances:    212 (soft) 
Total correct instances:    383 (Levenshtein) 
Total correct instances:    341 (ObservedRatcliffObershelp) 

Instance-level recall:  7.62    (strict) 
Instance-level recall:  21.54   (soft) 
Instance-level recall:  38.92   (Levenshtein) 
Instance-level recall:  34.65   (RatcliffObershelp) 
```

## Citation metadata 

Evaluation on 984 random PDF files out of 984 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 79.39 | 78.17 | 78.78 | 63265 |
| date | 95.86 | 93.99 | 94.92 | 63662 |
| first_author | 94.76 | 93.27 | 94.01 | 63265 |
| inTitle | 95.76 | 94.66 | 95.21 | 63213 |
| issue | 1.99 | 75 | 3.87 | 16 |
| page | 96.26 | 95.2 | 95.72 | 53375 |
| title | 90.42 | 90.84 | 90.63 | 62044 |
| volume | 97.86 | 98.17 | 98.01 | 61049 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.69** | **91.94** | **92.31** | 429889 |
| all fields (macro avg.) | 81.54 | 89.91 | 81.39 | 429889 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 79.53 | 78.31 | 78.91 | 63265 |
| date | 95.86 | 93.99 | 94.92 | 63662 |
| first_author | 94.84 | 93.35 | 94.09 | 63265 |
| inTitle | 96.24 | 95.13 | 95.68 | 63213 |
| issue | 1.99 | 75 | 3.87 | 16 |
| page | 96.26 | 95.2 | 95.72 | 53375 |
| title | 95.92 | 96.37 | 96.14 | 62044 |
| volume | 97.86 | 98.17 | 98.01 | 61049 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.59** | **92.84** | **93.22** | 429889 |
| all fields (macro avg.) | 82.31 | 90.69 | 82.17 | 429889 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 93.27 | 91.83 | 92.55 | 63265 |
| date | 95.86 | 93.99 | 94.92 | 63662 |
| first_author | 95.29 | 93.79 | 94.54 | 63265 |
| inTitle | 96.57 | 95.46 | 96.01 | 63213 |
| issue | 1.99 | 75 | 3.87 | 16 |
| page | 96.26 | 95.2 | 95.72 | 53375 |
| title | 97.65 | 98.11 | 97.88 | 62044 |
| volume | 97.86 | 98.17 | 98.01 | 61049 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **95.97** | **95.2** | **95.58** | 429889 |
| all fields (macro avg.) | 84.34 | 92.69 | 84.19 | 429889 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 86.69 | 85.36 | 86.02 | 63265 |
| date | 95.86 | 93.99 | 94.92 | 63662 |
| first_author | 94.78 | 93.29 | 94.03 | 63265 |
| inTitle | 96.25 | 95.14 | 95.69 | 63213 |
| issue | 1.99 | 75 | 3.87 | 16 |
| page | 96.26 | 95.2 | 95.72 | 53375 |
| title | 97.5 | 97.96 | 97.73 | 62044 |
| volume | 97.86 | 98.17 | 98.01 | 61049 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **94.86** | **94.1** | **94.48** | 429889 |
| all fields (macro avg.) | 83.4 | 91.76 | 83.25 | 429889 |


#### Instance-level results

```
Total expected instances:       63664
Total extracted instances:      66480
Total correct instances:        42383 (strict) 
Total correct instances:        45147 (soft) 
Total correct instances:        52790 (Levenshtein) 
Total correct instances:        49397 (RatcliffObershelp) 

Instance-level precision:   63.75 (strict) 
Instance-level precision:   67.91 (soft) 
Instance-level precision:   79.41 (Levenshtein) 
Instance-level precision:   74.3 (RatcliffObershelp) 

Instance-level recall:  66.57   (strict) 
Instance-level recall:  70.91   (soft) 
Instance-level recall:  82.92   (Levenshtein) 
Instance-level recall:  77.59   (RatcliffObershelp) 

Instance-level f-score: 65.13 (strict) 
Instance-level f-score: 69.38 (soft) 
Instance-level f-score: 81.13 (Levenshtein) 
Instance-level f-score: 75.91 (RatcliffObershelp) 

Matching 1 :    58594

Matching 2 :    1015

Matching 3 :    1241

Matching 4 :    367

Total matches : 61217
```


#### Citation context resolution
```

Total expected references:   63664 - 64.7 references per article
Total predicted references:      66480 - 67.56 references per article

Total expected citation contexts:    109022 - 110.79 citation contexts per article
Total predicted citation contexts:   99415 - 101.03 citation contexts per article

Total correct predicted citation contexts:   95626 - 97.18 citation contexts per article
Total wrong predicted citation contexts:     3789 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     96.19
Recall citation contexts:    87.71
fscore citation contexts:    91.76
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 984 random PDF files out of 984 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 28.79 | 25.64 | 27.12 | 585 |
| figure_title | 0.02 | 0.01 | 0.01 | 31718 |
| funding_stmt | 12.03 | 24 | 16.03 | 921 |
| reference_citation | 55.45 | 55.66 | 55.56 | 108949 |
| reference_figure | 56.74 | 49.86 | 53.08 | 68926 |
| reference_table | 68.27 | 73.46 | 70.77 | 2381 |
| section_title | 85.2 | 74.2 | 79.32 | 21831 |
| table_title | 0.45 | 0.16 | 0.23 | 1925 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **55.46** | **47.77** | **51.33** | 237236 |
| all fields (macro avg.) | 38.37 | 37.87 | 37.76 | 237236 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 38.77 | 34.53 | 36.53 | 585 |
| figure_title | 48.83 | 15.12 | 23.09 | 31718 |
| funding_stmt | 12.03 | 24 | 16.03 | 921 |
| reference_citation | 91.02 | 91.37 | 91.2 | 108949 |
| reference_figure | 57.02 | 50.1 | 53.34 | 68926 |
| reference_table | 68.35 | 73.54 | 70.85 | 2381 |
| section_title | 86.08 | 74.96 | 80.13 | 21831 |
| table_title | 80.63 | 27.9 | 41.45 | 1925 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **77.3** | **66.58** | **71.54** | 237236 |
| all fields (macro avg.) | 60.34 | 48.94 | 51.58 | 237236 |


**Document-level ratio results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 96.3 | 89.06 | 92.54 | 585 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **96.3** | **89.06** | **92.54** | 585 |
| all fields (macro avg.) | 96.3 | 89.06 | 92.54 | 585 |


