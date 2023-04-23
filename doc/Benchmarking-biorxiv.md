# Benchmarking biorXiv

## General

This is the end-to-end benchmarking result for GROBID version **0.7.2** against the `bioRxiv` test set (`biorxiv-10k-test-2000`), see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:
- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model
- **BidLSTM-CRF-FEATURES** as sequence labeling for the reference-segmenter model
- **CRF Wapiti** as sequence labelling engine for all other models. 

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 2000 PDF preprints out of 2000 (no failure).

Runtime for processing 2000 PDF: **1169s** (1,71 PDF per second) on Ubuntu 16.04, 4 CPU i7-4790K (8 threads), 16GB RAM (workstation bought in 2015 for 1600 euros) and with a GeForce GTX 1050 Ti GPU.

## Header metadata 

Evaluation on 2000 random PDF files out of 2000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 2.22 | 2.16 | 2.19 | 1990 |
| authors | 83.5 | 82.54 | 83.02 | 1999 |
| first_author | 96.76 | 95.74 | 96.25 | 1997 |
| keywords | 62.95 | 61.98 | 62.46 | 839 |
| title | 80.62 | 78 | 79.29 | 2000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **65.73** | **64.42** | **65.07** | 8825 |
| all fields (macro avg.) | 65.21 | 64.08 | 64.64 | 8825 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 58.99 | 57.39 | 58.18 | 1990 |
| authors | 83.96 | 82.99 | 83.47 | 1999 |
| first_author | 96.96 | 95.94 | 96.45 | 1997 |
| keywords | 68.89 | 67.82 | 68.35 | 839 |
| title | 84.55 | 81.8 | 83.15 | 2000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **80.03** | **78.44** | **79.23** | 8825 |
| all fields (macro avg.) | 78.67 | 77.19 | 77.92 | 8825 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 78.77 | 76.63 | 77.69 | 1990 |
| authors | 92.11 | 91.05 | 91.57 | 1999 |
| first_author | 97.27 | 96.24 | 96.75 | 1997 |
| keywords | 81.23 | 79.98 | 80.6 | 839 |
| title | 92.3 | 89.3 | 90.78 | 2000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.31** | **87.52** | **88.41** | 8825 |
| all fields (macro avg.) | 88.34 | 86.64 | 87.48 | 8825 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 74.79 | 72.76 | 73.76 | 1990 |
| authors | 87.65 | 86.64 | 87.14 | 1999 |
| first_author | 96.76 | 95.74 | 96.25 | 1997 |
| keywords | 73.97 | 72.82 | 73.39 | 839 |
| title | 89.61 | 86.7 | 88.13 | 2000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **85.99** | **84.27** | **85.12** | 8825 |
| all fields (macro avg.) | 84.56 | 82.94 | 83.74 | 8825 |


#### Instance-level results

```
Total expected instances:   2000
Total correct instances:    37 (strict) 
Total correct instances:    739 (soft) 
Total correct instances:    1173 (Levenshtein) 
Total correct instances:    1011 (ObservedRatcliffObershelp) 

Instance-level recall:  1.85    (strict) 
Instance-level recall:  36.95   (soft) 
Instance-level recall:  58.65   (Levenshtein) 
Instance-level recall:  50.55   (RatcliffObershelp) 
```

## Citation metadata 

Evaluation on 2000 random PDF files out of 2000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 87.59 | 82.36 | 84.9 | 97183 |
| date | 91.08 | 85.14 | 88.01 | 97630 |
| doi | 69.76 | 82.17 | 75.46 | 16894 |
| first_author | 94.55 | 88.82 | 91.59 | 97183 |
| inTitle | 81.96 | 78.34 | 80.11 | 96430 |
| issue | 94.2 | 86.07 | 89.96 | 30312 |
| page | 96.49 | 79.41 | 87.12 | 88597 |
| pmcid | 65.34 | 79.43 | 71.7 | 807 |
| pmid | 69.22 | 84.14 | 75.95 | 2093 |
| title | 84.54 | 82.87 | 83.7 | 92463 |
| volume | 95.46 | 94.59 | 95.02 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.46** | **84.45** | **86.89** | 707301 |
| all fields (macro avg.) | 84.56 | 83.94 | 83.96 | 707301 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 88.78 | 83.48 | 86.05 | 97183 |
| date | 91.08 | 85.14 | 88.01 | 97630 |
| doi | 74.24 | 87.46 | 80.31 | 16894 |
| first_author | 95 | 89.24 | 92.03 | 97183 |
| inTitle | 91.45 | 87.41 | 89.38 | 96430 |
| issue | 94.2 | 86.07 | 89.96 | 30312 |
| page | 96.49 | 79.41 | 87.12 | 88597 |
| pmcid | 75.03 | 91.2 | 82.33 | 807 |
| pmid | 73.19 | 88.96 | 80.31 | 2093 |
| title | 92.68 | 90.86 | 91.76 | 92463 |
| volume | 95.46 | 94.59 | 95.02 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.27** | **87.1** | **89.61** | 707301 |
| all fields (macro avg.) | 87.96 | 87.62 | 87.48 | 707301 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 94 | 88.38 | 91.1 | 97183 |
| date | 91.08 | 85.14 | 88.01 | 97630 |
| doi | 77.52 | 91.32 | 83.86 | 16894 |
| first_author | 95.16 | 89.39 | 92.19 | 97183 |
| inTitle | 92.49 | 88.4 | 90.4 | 96430 |
| issue | 94.2 | 86.07 | 89.96 | 30312 |
| page | 96.49 | 79.41 | 87.12 | 88597 |
| pmcid | 75.03 | 91.2 | 82.33 | 807 |
| pmid | 73.23 | 89.01 | 80.35 | 2093 |
| title | 95.61 | 93.73 | 94.66 | 92463 |
| volume | 95.46 | 94.59 | 95.02 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.64** | **88.4** | **90.94** | 707301 |
| all fields (macro avg.) | 89.12 | 88.79 | 88.64 | 707301 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 90.99 | 85.56 | 88.19 | 97183 |
| date | 91.08 | 85.14 | 88.01 | 97630 |
| doi | 75.83 | 89.32 | 82.02 | 16894 |
| first_author | 94.6 | 88.86 | 91.64 | 97183 |
| inTitle | 90.1 | 86.12 | 88.06 | 96430 |
| issue | 94.2 | 86.07 | 89.96 | 30312 |
| page | 96.49 | 79.41 | 87.12 | 88597 |
| pmcid | 65.34 | 79.43 | 71.7 | 807 |
| pmid | 69.22 | 84.14 | 75.95 | 2093 |
| title | 94.88 | 93.01 | 93.93 | 92463 |
| volume | 95.46 | 94.59 | 95.02 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.64** | **87.45** | **89.97** | 707301 |
| all fields (macro avg.) | 87.11 | 86.51 | 86.51 | 707301 |


#### Instance-level results

```
Total expected instances:       98799
Total extracted instances:      97876
Total correct instances:        42805 (strict) 
Total correct instances:        53633 (soft) 
Total correct instances:        58050 (Levenshtein) 
Total correct instances:        54700 (RatcliffObershelp) 

Instance-level precision:   43.73 (strict) 
Instance-level precision:   54.8 (soft) 
Instance-level precision:   59.31 (Levenshtein) 
Instance-level precision:   55.89 (RatcliffObershelp) 

Instance-level recall:  43.33   (strict) 
Instance-level recall:  54.28   (soft) 
Instance-level recall:  58.76   (Levenshtein) 
Instance-level recall:  55.36   (RatcliffObershelp) 

Instance-level f-score: 43.53 (strict) 
Instance-level f-score: 54.54 (soft) 
Instance-level f-score: 59.03 (Levenshtein) 
Instance-level f-score: 55.62 (RatcliffObershelp) 

Matching 1 :    77920

Matching 2 :    4611

Matching 3 :    4611

Matching 4 :    2441

Total matches : 89583
```


#### Citation context resolution
```

Total expected references:   98797 - 49.4 references per article
Total predicted references:      97876 - 48.94 references per article

Total expected citation contexts:    142862 - 71.43 citation contexts per article
Total predicted citation contexts:   136750 - 68.38 citation contexts per article

Total correct predicted citation contexts:   116589 - 58.29 citation contexts per article
Total wrong predicted citation contexts:     20161 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     85.26
Recall citation contexts:    81.61
fscore citation contexts:    83.39
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 2000 random PDF files out of 2000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 23.43 | 18.39 | 20.6 | 446 |
| figure_title | 4.19 | 3.55 | 3.84 | 13172 |
| funding_stmt | 52.12 | 36.38 | 42.85 | 745 |
| reference_citation | 71.15 | 71.26 | 71.21 | 147470 |
| reference_figure | 74.3 | 66.17 | 70 | 47984 |
| reference_table | 48.84 | 80.66 | 60.84 | 5957 |
| section_title | 72.78 | 69.4 | 71.05 | 32398 |
| table_title | 4.32 | 3.88 | 4.09 | 2961 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **67.14** | **65.73** | **66.43** | 251133 |
| all fields (macro avg.) | 43.89 | 43.71 | 43.06 | 251133 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 52 | 40.81 | 45.73 | 446 |
| figure_title | 68.53 | 58.04 | 62.85 | 13172 |
| funding_stmt | 59.04 | 41.21 | 48.54 | 745 |
| reference_citation | 83.08 | 83.21 | 83.14 | 147470 |
| reference_figure | 75.02 | 66.81 | 70.68 | 47984 |
| reference_table | 49.25 | 81.33 | 61.35 | 5957 |
| section_title | 76.72 | 73.16 | 74.9 | 32398 |
| table_title | 50.75 | 45.63 | 48.05 | 2961 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **78.42** | **76.77** | **77.59** | 251133 |
| all fields (macro avg.) | 64.3 | 61.27 | 61.9 | 251133 |

Evaluation metrics produced in 1222.528 seconds
