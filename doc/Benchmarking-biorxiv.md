# Benchmarking biorXiv

## General

This is the end-to-end benchmarking result for GROBID version **0.6.2** against the `bioRxiv` test set (`biorxiv-10k-test-2000`), see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using **CRF Wapiti** only as sequence labelling engine. Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service are similar but much slower). However, giving that preprints are processed, the consolidation usually fails because biblio-glutton uses an old snapshot of the CrossRef metadata (from end of 2019). 

More recent versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Deep Learning models provide higher accuracy at the cost of slower runtime and more expensive CPU resources. 

Evaluation on 1998 PDF preprints out of 2000 (1 PDF parsing timeout and 1 PDF "too many blocks" interruption).

Runtime for processing 2000 PDF: **1321s** (1,51 PDF per second) on Ubuntu 16.04, 4 CPU i7-4790K (8 threads), 16GB RAM (workstation bought in 2015 for 1600 euros).



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


