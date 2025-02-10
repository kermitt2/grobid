# Benchmarking eLife

## General

This is the end-to-end benchmarking result for GROBID version **0.8.1** against the `eLife` test set, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models.  

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 984 PDF preprints out of 984 (no failure).

Runtime for processing 984 PDF: **1131** seconds (1.15 seconds per PDF file) on Ubuntu 22.04, 16 CPU (32 threads), 128GB RAM and with a GeForce GTX 1080 Ti GPU.

Note: with CRF only models runtime is 492s (0.50 seconds per PDF) with 4 CPU, 8 threads. 



## Header metadata 

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 9.56 | 9.25 | 9.4 | 984 |
| authors | 75.31 | 74.47 | 74.88 | 983 |
| first_author | 92.49 | 91.55 | 92.02 | 982 |
| title | 87.01 | 85.06 | 86.02 | 984 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **66.33** | **65.06** | **65.69** | 3933 |
| all fields (macro avg.) | 66.09 | 65.08 | 65.58 | 3933 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 22.48 | 21.75 | 22.11 | 984 |
| authors | 75.72 | 74.87 | 75.29 | 983 |
| first_author | 92.49 | 91.55 | 92.02 | 982 |
| title | 95.01 | 92.89 | 93.94 | 984 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **71.62** | **70.25** | **70.93** | 3933 |
| all fields (macro avg.) | 71.42 | 70.26 | 70.84 | 3933 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 47.69 | 46.14 | 46.9 | 984 |
| authors | 88.99 | 88 | 88.49 | 983 |
| first_author | 92.8 | 91.85 | 92.32 | 982 |
| title | 96.47 | 94.31 | 95.38 | 984 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **81.62** | **80.07** | **80.84** | 3933 |
| all fields (macro avg.) | 81.49 | 80.07 | 80.77 | 3933 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 44.64 | 43.19 | 43.9 | 984 |
| authors | 81.17 | 80.26 | 80.72 | 983 |
| first_author | 92.49 | 91.55 | 92.02 | 982 |
| title | 96.47 | 94.31 | 95.38 | 984 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **78.82** | **77.32** | **78.06** | 3933 |
| all fields (macro avg.) | 78.69 | 77.33 | 78 | 3933 |


#### Instance-level results

```
Total expected instances: 	984
Total correct instances: 	73 (strict) 
Total correct instances: 	197 (soft) 
Total correct instances: 	383 (Levenshtein) 
Total correct instances: 	340 (ObservedRatcliffObershelp) 

Instance-level recall:	7.42	(strict) 
Instance-level recall:	20.02	(soft) 
Instance-level recall:	38.92	(Levenshtein) 
Instance-level recall:	34.55	(RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 79.46 | 78.39 | 78.92 | 63265 |
| date | 95.92 | 94.22 | 95.06 | 63662 |
| first_author | 94.86 | 93.54 | 94.2 | 63265 |
| inTitle | 95.83 | 94.91 | 95.37 | 63213 |
| issue | 1.98 | 75 | 3.85 | 16 |
| page | 96.27 | 95.46 | 95.86 | 53375 |
| title | 90.3 | 90.93 | 90.62 | 62044 |
| volume | 97.9 | 98.42 | 98.16 | 61049 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.72** | **92.17** | **92.44** | 429889 |
| all fields (macro avg.) | 81.56 | 90.11 | 81.5 | 429889 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 79.59 | 78.53 | 79.06 | 63265 |
| date | 95.92 | 94.22 | 95.06 | 63662 |
| first_author | 94.94 | 93.62 | 94.28 | 63265 |
| inTitle | 96.32 | 95.39 | 95.85 | 63213 |
| issue | 1.98 | 75 | 3.85 | 16 |
| page | 96.27 | 95.46 | 95.86 | 53375 |
| title | 95.97 | 96.64 | 96.3 | 62044 |
| volume | 97.9 | 98.42 | 98.16 | 61049 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.65** | **93.09** | **93.37** | 429889 |
| all fields (macro avg.) | 82.36 | 90.91 | 82.3 | 429889 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 93.35 | 92.09 | 92.72 | 63265 |
| date | 95.92 | 94.22 | 95.06 | 63662 |
| first_author | 95.39 | 94.07 | 94.72 | 63265 |
| inTitle | 96.64 | 95.72 | 96.18 | 63213 |
| issue | 1.98 | 75 | 3.85 | 16 |
| page | 96.27 | 95.46 | 95.86 | 53375 |
| title | 97.69 | 98.37 | 98.03 | 62044 |
| volume | 97.9 | 98.42 | 98.16 | 61049 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **96.03** | **95.45** | **95.74** | 429889 |
| all fields (macro avg.) | 84.39 | 92.92 | 84.32 | 429889 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 86.77 | 85.6 | 86.18 | 63265 |
| date | 95.92 | 94.22 | 95.06 | 63662 |
| first_author | 94.87 | 93.56 | 94.21 | 63265 |
| inTitle | 96.32 | 95.39 | 95.86 | 63213 |
| issue | 1.98 | 75 | 3.85 | 16 |
| page | 96.27 | 95.46 | 95.86 | 53375 |
| title | 97.54 | 98.22 | 97.88 | 62044 |
| volume | 97.9 | 98.42 | 98.16 | 61049 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **94.92** | **94.35** | **94.64** | 429889 |
| all fields (macro avg.) | 83.45 | 91.98 | 83.38 | 429889 |


#### Instance-level results

```
Total expected instances: 		63664
Total extracted instances: 		66625
Total correct instances: 		42410 (strict) 
Total correct instances: 		45255 (soft) 
Total correct instances: 		52923 (Levenshtein) 
Total correct instances: 		49516 (RatcliffObershelp) 

Instance-level precision:	63.65 (strict) 
Instance-level precision:	67.92 (soft) 
Instance-level precision:	79.43 (Levenshtein) 
Instance-level precision:	74.32 (RatcliffObershelp) 

Instance-level recall:	66.62	(strict) 
Instance-level recall:	71.08	(soft) 
Instance-level recall:	83.13	(Levenshtein) 
Instance-level recall:	77.78	(RatcliffObershelp) 

Instance-level f-score:	65.1 (strict) 
Instance-level f-score:	69.47 (soft) 
Instance-level f-score:	81.24 (Levenshtein) 
Instance-level f-score:	76.01 (RatcliffObershelp) 

Matching 1 :	58743

Matching 2 :	1017

Matching 3 :	1249

Matching 4 :	368

Total matches :	61377
```


#### Citation context resolution
```

Total expected references: 	 63664 - 64.7 references per article
Total predicted references: 	 66625 - 67.71 references per article

Total expected citation contexts: 	 109022 - 110.79 citation contexts per article
Total predicted citation contexts: 	 100059 - 101.69 citation contexts per article

Total correct predicted citation contexts: 	 96222 - 97.79 citation contexts per article
Total wrong predicted citation contexts: 	 3837 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 96.17
Recall citation contexts: 	 88.26
fscore citation contexts: 	 92.04
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 30.35 | 28.38 | 29.33 | 585 |
| figure_title | 0.01 | 0 | 0 | 31718 |
| funding_stmt | 5.4 | 26.06 | 8.95 | 921 |
| reference_citation | 55.34 | 55.8 | 55.57 | 108949 |
| reference_figure | 56.75 | 49.91 | 53.11 | 68926 |
| reference_table | 68.16 | 73.37 | 70.67 | 2381 |
| section_title | 85.07 | 74.75 | 79.58 | 21831 |
| table_title | 0.6 | 0.21 | 0.31 | 1925 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **54.73** | **47.92** | **51.1** | 237236 |
| all fields (macro avg.) | 37.71 | 38.56 | 37.19 | 237236 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 39.67 | 37.09 | 38.34 | 585 |
| figure_title | 49.09 | 15.18 | 23.19 | 31718 |
| funding_stmt | 5.4 | 26.06 | 8.95 | 921 |
| reference_citation | 90.96 | 91.72 | 91.34 | 108949 |
| reference_figure | 57.02 | 50.16 | 53.37 | 68926 |
| reference_table | 68.24 | 73.46 | 70.75 | 2381 |
| section_title | 85.95 | 75.52 | 80.4 | 21831 |
| table_title | 80.77 | 28.16 | 41.76 | 1925 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **76.33** | **66.83** | **71.27** | 237236 |
| all fields (macro avg.) | 59.64 | 49.67 | 51.01 | 237236 |


**Document-level ratio results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 96.64 | 93.5 | 95.05 | 585 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **96.64** | **93.5** | **95.05** | 585 |
| all fields (macro avg.) | 96.64 | 93.5 | 95.05 | 585 |

Evaluation metrics produced in 1929.943 seconds


