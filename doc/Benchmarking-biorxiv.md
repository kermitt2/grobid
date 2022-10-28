# Benchmarking biorXiv

## General

This is the end-to-end benchmarking result for GROBID version **0.7.2** against the `bioRxiv` test set (`biorxiv-10k-test-2000`), see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:
- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model
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
| authors | 87.02 | 80.85 | 83.82 | 97183 |
| date | 90.77 | 84.11 | 87.31 | 97630 |
| doi | 70.52 | 82.47 | 76.03 | 16894 |
| first_author | 94.17 | 87.42 | 90.67 | 97183 |
| inTitle | 81.65 | 77.57 | 79.56 | 96430 |
| issue | 93.96 | 85.3 | 89.42 | 30312 |
| page | 95.99 | 79.12 | 86.74 | 88597 |
| pmcid | 66.59 | 70.88 | 68.67 | 807 |
| pmid | 68.75 | 82.42 | 74.97 | 2093 |
| title | 84.45 | 82.06 | 83.24 | 92463 |
| volume | 95.16 | 93.44 | 94.29 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.16** | **83.48** | **86.23** | 707301 |
| all fields (macro avg.) | 84.46 | 82.33 | 83.15 | 707301 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 88.2 | 81.94 | 84.96 | 97183 |
| date | 90.77 | 84.11 | 87.31 | 97630 |
| doi | 75.08 | 87.8 | 80.94 | 16894 |
| first_author | 94.62 | 87.84 | 91.1 | 97183 |
| inTitle | 91.12 | 86.57 | 88.79 | 96430 |
| issue | 93.96 | 85.3 | 89.42 | 30312 |
| page | 95.99 | 79.12 | 86.74 | 88597 |
| pmcid | 76.83 | 81.78 | 79.23 | 807 |
| pmid | 73.26 | 87.82 | 79.88 | 2093 |
| title | 92.58 | 89.96 | 91.25 | 92463 |
| volume | 95.16 | 93.44 | 94.29 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.96** | **86.1** | **88.94** | 707301 |
| all fields (macro avg.) | 87.96 | 85.97 | 86.72 | 707301 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 93.4 | 86.77 | 89.96 | 97183 |
| date | 90.77 | 84.11 | 87.31 | 97630 |
| doi | 77.25 | 90.35 | 83.29 | 16894 |
| first_author | 94.78 | 87.98 | 91.25 | 97183 |
| inTitle | 92.15 | 87.55 | 89.79 | 96430 |
| issue | 93.96 | 85.3 | 89.42 | 30312 |
| page | 95.99 | 79.12 | 86.74 | 88597 |
| pmcid | 76.83 | 81.78 | 79.23 | 807 |
| pmid | 73.26 | 87.82 | 79.88 | 2093 |
| title | 95.39 | 92.7 | 94.03 | 92463 |
| volume | 95.16 | 93.44 | 94.29 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.28** | **87.34** | **90.21** | 707301 |
| all fields (macro avg.) | 88.99 | 86.99 | 87.74 | 707301 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 90.39 | 83.97 | 87.06 | 97183 |
| date | 90.77 | 84.11 | 87.31 | 97630 |
| doi | 76.48 | 89.45 | 82.46 | 16894 |
| first_author | 94.22 | 87.46 | 90.71 | 97183 |
| inTitle | 89.84 | 85.35 | 87.54 | 96430 |
| issue | 93.96 | 85.3 | 89.42 | 30312 |
| page | 95.99 | 79.12 | 86.74 | 88597 |
| pmcid | 66.59 | 70.88 | 68.67 | 807 |
| pmid | 68.75 | 82.42 | 74.97 | 2093 |
| title | 94.71 | 92.04 | 93.36 | 92463 |
| volume | 95.16 | 93.44 | 94.29 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.33** | **86.45** | **89.29** | 707301 |
| all fields (macro avg.) | 86.99 | 84.87 | 85.68 | 707301 |


#### Instance-level results

```
Total expected instances:       98799
Total extracted instances:      98211
Total correct instances:        42224 (strict) 
Total correct instances:        52981 (soft) 
Total correct instances:        57435 (Levenshtein) 
Total correct instances:        54228 (RatcliffObershelp) 

Instance-level precision:   42.99 (strict) 
Instance-level precision:   53.95 (soft) 
Instance-level precision:   58.48 (Levenshtein) 
Instance-level precision:   55.22 (RatcliffObershelp) 

Instance-level recall:  42.74   (strict) 
Instance-level recall:  53.63   (soft) 
Instance-level recall:  58.13   (Levenshtein) 
Instance-level recall:  54.89   (RatcliffObershelp) 

Instance-level f-score: 42.86 (strict) 
Instance-level f-score: 53.79 (soft) 
Instance-level f-score: 58.31 (Levenshtein) 
Instance-level f-score: 55.05 (RatcliffObershelp) 

Matching 1 :    76960

Matching 2 :    4405

Matching 3 :    4904

Matching 4 :    2510

Total matches : 88779
```


#### Citation context resolution

```
Total expected references:   98797 - 49.4 references per article
Total predicted references:      98211 - 49.11 references per article

Total expected citation contexts:    142862 - 71.43 citation contexts per article
Total predicted citation contexts:   136728 - 68.36 citation contexts per article

Total correct predicted citation contexts:   115327 - 57.66 citation contexts per article
Total wrong predicted citation contexts:     21401 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     84.35
Recall citation contexts:    80.73
fscore citation contexts:    82.5
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
