# Benchmarking biorXiv

## General

This is the end-to-end benchmarking result for GROBID version **0.8.1** against the `bioRxiv` test set (`biorxiv-10k-test-2000`), see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models.  

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 2000 PDF preprints out of 2000 (no failure).

Runtime for processing 2000 PDF: **1713** seconds (0.85 seconds per PDF file) on Ubuntu 22.04, 16 CPU (32 threads), 128GB RAM and with a GeForce GTX 1080 Ti GPU.

Note: with CRF only models runtime is 622s (0.31 second per PDF) with 4 CPU, 8 threads. 


## Header metadata 

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|---               |---         |---        |---         |---      |
| abstract | 2.36 | 2.31 | 2.34 | 1989 |
| authors | 84.3 | 83.58 | 83.94 | 1998 |
| first_author | 96.97 | 96.24 | 96.61 | 1996 |
| keywords | 58.9 | 59.95 | 59.42 | 839 |
| title | 77.77 | 76.99 | 77.38 | 1999 |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **64.95** | **64.38** | **64.66** | 8821 |
| all fields (macro avg.) | 64.06 | 63.82 | 63.94 | 8821 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 60.08 | 58.87 | 59.47 | 1989 |
| authors | 84.76 | 84.03 | 84.39 | 1998 |
| first_author | 97.17 | 96.44 | 96.81 | 1996 |
| keywords | 64.05 | 65.2 | 64.62 | 839 |
| title | 80.04 | 79.24 | 79.64 | 1999 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **78.98** | **78.29** | **78.63** | 8821 |
| all fields (macro avg.) | 77.22 | 76.76 | 76.99 | 8821 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 80.66 | 79.03 | 79.84 | 1989 |
| authors | 92.48 | 91.69 | 92.08 | 1998 |
| first_author | 97.43 | 96.69 | 97.06 | 1996 |
| keywords | 79.63 | 81.05 | 80.33 | 839 |
| title | 92.02 | 91.1 | 91.55 | 1999 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.6** | **88.82** | **89.21** | 8821 |
| all fields (macro avg.) | 88.44 | 87.91 | 88.17 | 8821 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 77.37 | 75.82 | 76.59 | 1989 |
| authors | 88.64 | 87.89 | 88.26 | 1998 |
| first_author | 96.97 | 96.24 | 96.61 | 1996 |
| keywords | 71.43 | 72.71 | 72.06 | 839 |
| title | 87.97 | 87.09 | 87.53 | 1999 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **86.18** | **85.43** | **85.81** | 8821 |
| all fields (macro avg.) | 84.48 | 83.95 | 84.21 | 8821 |


#### Instance-level results

```
Total expected instances: 	1999
Total correct instances: 	39 (strict) 
Total correct instances: 	726 (soft) 
Total correct instances: 	1236 (Levenshtein) 
Total correct instances: 	1071 (ObservedRatcliffObershelp) 

Instance-level recall:	1.95	(strict) 
Instance-level recall:	36.32	(soft) 
Instance-level recall:	61.83	(Levenshtein) 
Instance-level recall:	53.58	(RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 88.21 | 83.17 | 85.61 | 97116 |
| date | 91.71 | 86.23 | 88.88 | 97563 |
| doi | 70.87 | 83.8 | 76.79 | 16894 |
| first_author | 95.1 | 89.59 | 92.26 | 97116 |
| inTitle | 82.96 | 79.41 | 81.15 | 96363 |
| issue | 94.35 | 91.87 | 93.1 | 30255 |
| page | 95.02 | 78.29 | 85.85 | 88534 |
| pmcid | 66.38 | 86.12 | 74.97 | 807 |
| pmid | 70.07 | 85 | 76.81 | 2093 |
| title | 84.95 | 83.53 | 84.23 | 92402 |
| volume | 96.28 | 95.16 | 95.72 | 87646 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.9** | **85.28** | **87.53** | 706789 |
| all fields (macro avg.) | 85.08 | 85.65 | 85.04 | 706789 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 89.36 | 84.26 | 86.74 | 97116 |
| date | 91.71 | 86.23 | 88.88 | 97563 |
| doi | 75.36 | 89.1 | 81.65 | 16894 |
| first_author | 95.53 | 89.99 | 92.68 | 97116 |
| inTitle | 92.38 | 88.43 | 90.36 | 96363 |
| issue | 94.35 | 91.87 | 93.1 | 30255 |
| page | 95.02 | 78.29 | 85.85 | 88534 |
| pmcid | 75.64 | 98.14 | 85.44 | 807 |
| pmid | 74.48 | 90.35 | 81.65 | 2093 |
| title | 93.29 | 91.73 | 92.5 | 92402 |
| volume | 96.28 | 95.16 | 95.72 | 87646 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.7** | **87.94** | **90.26** | 706789 |
| all fields (macro avg.) | 88.49 | 89.41 | 88.6 | 706789 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 94.63 | 89.23 | 91.85 | 97116 |
| date | 91.71 | 86.23 | 88.88 | 97563 |
| doi | 77.61 | 91.77 | 84.1 | 16894 |
| first_author | 95.67 | 90.13 | 92.82 | 97116 |
| inTitle | 93.37 | 89.38 | 91.33 | 96363 |
| issue | 94.35 | 91.87 | 93.1 | 30255 |
| page | 95.02 | 78.29 | 85.85 | 88534 |
| pmcid | 75.64 | 98.14 | 85.44 | 807 |
| pmid | 74.48 | 90.35 | 81.65 | 2093 |
| title | 96.1 | 94.49 | 95.29 | 92402 |
| volume | 96.28 | 95.16 | 95.72 | 87646 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **94.03** | **89.2** | **91.55** | 706789 |
| all fields (macro avg.) | 89.53 | 90.46 | 89.64 | 706789 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 91.58 | 86.35 | 88.89 | 97116 |
| date | 91.71 | 86.23 | 88.88 | 97563 |
| doi | 76.05 | 89.93 | 82.41 | 16894 |
| first_author | 95.15 | 89.64 | 92.31 | 97116 |
| inTitle | 91.15 | 87.26 | 89.16 | 96363 |
| issue | 94.35 | 91.87 | 93.1 | 30255 |
| page | 95.02 | 78.29 | 85.85 | 88534 |
| pmcid | 66.38 | 86.12 | 74.97 | 807 |
| pmid | 70.07 | 85 | 76.81 | 2093 |
| title | 95.45 | 93.85 | 94.64 | 92402 |
| volume | 96.28 | 95.16 | 95.72 | 87646 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.07** | **88.29** | **90.62** | 706789 |
| all fields (macro avg.) | 87.56 | 88.15 | 87.52 | 706789 |


#### Instance-level results

```
Total expected instances: 		98732
Total extracted instances: 		97845
Total correct instances: 		43742 (strict) 
Total correct instances: 		54712 (soft) 
Total correct instances: 		58893 (Levenshtein) 
Total correct instances: 		55623 (RatcliffObershelp) 

Instance-level precision:	44.71 (strict) 
Instance-level precision:	55.92 (soft) 
Instance-level precision:	60.19 (Levenshtein) 
Instance-level precision:	56.85 (RatcliffObershelp) 

Instance-level recall:	44.3	(strict) 
Instance-level recall:	55.41	(soft) 
Instance-level recall:	59.65	(Levenshtein) 
Instance-level recall:	56.34	(RatcliffObershelp) 

Instance-level f-score:	44.5 (strict) 
Instance-level f-score:	55.66 (soft) 
Instance-level f-score:	59.92 (Levenshtein) 
Instance-level f-score:	56.59 (RatcliffObershelp) 

Matching 1 :	79187

Matching 2 :	4426

Matching 3 :	4362

Matching 4 :	2074

Total matches :	90049
```


#### Citation context resolution
```

Total expected references: 	 98730 - 49.37 references per article
Total predicted references: 	 97845 - 48.92 references per article

Total expected citation contexts: 	 142727 - 71.36 citation contexts per article
Total predicted citation contexts: 	 135792 - 67.9 citation contexts per article

Total correct predicted citation contexts: 	 116724 - 58.36 citation contexts per article
Total wrong predicted citation contexts: 	 19068 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 85.96
Recall citation contexts: 	 81.78
fscore citation contexts: 	 83.82
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 29.85 | 26.23 | 27.92 | 446 |
| figure_title | 4.28 | 2.02 | 2.75 | 22974 |
| funding_stmt | 3.76 | 25.37 | 6.55 | 745 |
| reference_citation | 71.01 | 71.41 | 71.21 | 147335 |
| reference_figure | 70.53 | 67.77 | 69.12 | 47978 |
| reference_table | 47.96 | 83.14 | 60.83 | 5955 |
| section_title | 72.69 | 69.72 | 71.17 | 32390 |
| table_title | 4.25 | 2.8 | 3.38 | 3923 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **65.27** | **63.47** | **64.36** | 261746 |
| all fields (macro avg.) | 38.04 | 43.56 | 39.12 | 261746 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 49.74 | 43.72 | 46.54 | 446 |
| figure_title | 69.67 | 32.97 | 44.76 | 22974 |
| funding_stmt | 3.96 | 26.71 | 6.9 | 745 |
| reference_citation | 83.04 | 83.51 | 83.27 | 147335 |
| reference_figure | 71.15 | 68.37 | 69.73 | 47978 |
| reference_table | 48.4 | 83.91 | 61.39 | 5955 |
| section_title | 76.58 | 73.45 | 74.98 | 32390 |
| table_title | 51.01 | 33.62 | 40.53 | 3923 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **76.18** | **74.08** | **75.12** | 261746 |
| all fields (macro avg.) | 56.69 | 55.78 | 53.51 | 261746 |


**Document-level ratio results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 83.94 | 87.89 | 85.87 | 446 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **83.94** | **87.89** | **85.87** | 446 |
| all fields (macro avg.) | 83.94 | 87.89 | 85.87 | 446 |

Evaluation metrics produced in 2262.531 seconds



