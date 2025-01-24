# Benchmarking PLOS

## General

This is the end-to-end benchmarking result for GROBID version **0.8.1** against the `PLOS` test set, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models.  

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 1000 PDF preprints out of 1000 (no failure).

Runtime for processing 1000 PDF: **999** seconds, (0.99 seconds per PDF) on Ubuntu 22.04, 16 CPU (32 threads), 128GB RAM and with a GeForce GTX 1080 Ti GPU.

Note: with CRF only models runtime is 304s (0.30 seconds per PDF) with 4 CPU, 8 threads. 


## Header metadata 

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 13.51 | 13.54 | 13.53 | 960 |
| authors | 99.07 | 99.07 | 99.07 | 969 |
| first_author | 99.28 | 99.28 | 99.28 | 969 |
| keywords | 0 | 0 | 0 | 0 |
| title | 95.86 | 95 | 95.43 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **77.15** | **77.01** | **77.08** | 3898 |
| all fields (macro avg.) | 76.93 | 76.72 | 76.83 | 3898 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 50.52 | 50.62 | 50.57 | 960 |
| authors | 99.07 | 99.07 | 99.07 | 969 |
| first_author | 99.28 | 99.28 | 99.28 | 969 |
| keywords | 0 | 0 | 0 | 0 |
| title | 99.5 | 98.6 | 99.05 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **87.23** | **87.07** | **87.15** | 3898 |
| all fields (macro avg.) | 87.09 | 86.89 | 86.99 | 3898 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 76.51 | 76.67 | 76.59 | 960 |
| authors | 99.38 | 99.38 | 99.38 | 969 |
| first_author | 99.38 | 99.38 | 99.38 | 969 |
| keywords | 0 | 0 | 0 | 0 |
| title | 99.7 | 98.8 | 99.25 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.81** | **93.64** | **93.72** | 3898 |
| all fields (macro avg.) | 93.74 | 93.56 | 93.65 | 3898 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 66.74 | 66.88 | 66.81 | 960 |
| authors | 99.28 | 99.28 | 99.28 | 969 |
| first_author | 99.28 | 99.28 | 99.28 | 969 |
| keywords | 0 | 0 | 0 | 0 |
| title | 99.6 | 98.7 | 99.15 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.31** | **91.15** | **91.23** | 3898 |
| all fields (macro avg.) | 91.22 | 91.03 | 91.13 | 3898 |


#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	142 (strict) 
Total correct instances: 	490 (soft) 
Total correct instances: 	727 (Levenshtein) 
Total correct instances: 	643 (ObservedRatcliffObershelp) 

Instance-level recall:	14.2	(strict) 
Instance-level recall:	49	(soft) 
Instance-level recall:	72.7	(Levenshtein) 
Instance-level recall:	64.3	(RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 81.18 | 78.43 | 79.78 | 44770 |
| date | 84.63 | 81.26 | 82.91 | 45457 |
| first_author | 91.49 | 88.36 | 89.9 | 44770 |
| inTitle | 81.68 | 83.58 | 82.62 | 42795 |
| issue | 93.62 | 92.7 | 93.16 | 18983 |
| page | 93.71 | 77.57 | 84.88 | 40844 |
| title | 59.98 | 60.49 | 60.23 | 43101 |
| volume | 95.9 | 96.12 | 96.01 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **84.24** | **81.46** | **82.83** | 321178 |
| all fields (macro avg.) | 85.27 | 82.31 | 83.69 | 321178 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 81.49 | 78.73 | 80.09 | 44770 |
| date | 84.63 | 81.26 | 82.91 | 45457 |
| first_author | 91.71 | 88.57 | 90.11 | 44770 |
| inTitle | 85.52 | 87.51 | 86.5 | 42795 |
| issue | 93.62 | 92.7 | 93.16 | 18983 |
| page | 93.71 | 77.57 | 84.88 | 40844 |
| title | 91.98 | 92.75 | 92.37 | 43101 |
| volume | 95.9 | 96.12 | 96.01 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.34** | **86.38** | **87.83** | 321178 |
| all fields (macro avg.) | 89.82 | 86.9 | 88.25 | 321178 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 90.65 | 87.58 | 89.09 | 44770 |
| date | 84.63 | 81.26 | 82.91 | 45457 |
| first_author | 92.25 | 89.09 | 90.64 | 44770 |
| inTitle | 86.46 | 88.47 | 87.46 | 42795 |
| issue | 93.62 | 92.7 | 93.16 | 18983 |
| page | 93.71 | 77.57 | 84.88 | 40844 |
| title | 94.58 | 95.38 | 94.98 | 43101 |
| volume | 95.9 | 96.12 | 96.01 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.19** | **88.17** | **89.65** | 321178 |
| all fields (macro avg.) | 91.48 | 88.52 | 89.89 | 321178 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 84.94 | 82.07 | 83.48 | 44770 |
| date | 84.63 | 81.26 | 82.91 | 45457 |
| first_author | 91.49 | 88.36 | 89.9 | 44770 |
| inTitle | 85.17 | 87.15 | 86.15 | 42795 |
| issue | 93.62 | 92.7 | 93.16 | 18983 |
| page | 93.71 | 77.57 | 84.88 | 40844 |
| title | 93.97 | 94.76 | 94.36 | 43101 |
| volume | 95.9 | 96.12 | 96.01 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.02** | **87.04** | **88.5** | 321178 |
| all fields (macro avg.) | 90.43 | 87.5 | 88.86 | 321178 |


#### Instance-level results

```
Total expected instances: 		48449
Total extracted instances: 		48231
Total correct instances: 		13495 (strict) 
Total correct instances: 		22261 (soft) 
Total correct instances: 		24910 (Levenshtein) 
Total correct instances: 		23261 (RatcliffObershelp) 

Instance-level precision:	27.98 (strict) 
Instance-level precision:	46.15 (soft) 
Instance-level precision:	51.65 (Levenshtein) 
Instance-level precision:	48.23 (RatcliffObershelp) 

Instance-level recall:	27.85	(strict) 
Instance-level recall:	45.95	(soft) 
Instance-level recall:	51.41	(Levenshtein) 
Instance-level recall:	48.01	(RatcliffObershelp) 

Instance-level f-score:	27.92 (strict) 
Instance-level f-score:	46.05 (soft) 
Instance-level f-score:	51.53 (Levenshtein) 
Instance-level f-score:	48.12 (RatcliffObershelp) 

Matching 1 :	35374

Matching 2 :	1262

Matching 3 :	3266

Matching 4 :	1801

Total matches :	41703
```


#### Citation context resolution
```

Total expected references: 	 48449 - 48.45 references per article
Total predicted references: 	 48231 - 48.23 references per article

Total expected citation contexts: 	 69755 - 69.75 citation contexts per article
Total predicted citation contexts: 	 73609 - 73.61 citation contexts per article

Total correct predicted citation contexts: 	 56730 - 56.73 citation contexts per article
Total wrong predicted citation contexts: 	 16879 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 77.07
Recall citation contexts: 	 81.33
fscore citation contexts: 	 79.14
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 54.27 | 52.25 | 53.24 | 779 |
| figure_title | 2.16 | 0.94 | 1.31 | 8943 |
| funding_stmt | 5.47 | 30.66 | 9.29 | 1507 |
| reference_citation | 86.72 | 94.54 | 90.46 | 69741 |
| reference_figure | 72.02 | 54 | 61.72 | 11010 |
| reference_table | 84.23 | 92.07 | 87.98 | 5159 |
| section_title | 77.13 | 65.75 | 70.99 | 17540 |
| table_title | 1.03 | 0.54 | 0.71 | 6092 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **73.58** | **73.82** | **73.7** | 120771 |
| all fields (macro avg.) | 47.88 | 48.84 | 46.96 | 120771 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 79.87 | 76.89 | 78.35 | 779 |
| figure_title | 81.18 | 35.31 | 49.22 | 8943 |
| funding_stmt | 7 | 39.22 | 11.88 | 1507 |
| reference_citation | 86.73 | 94.56 | 90.48 | 69741 |
| reference_figure | 72.48 | 54.34 | 62.11 | 11010 |
| reference_table | 84.41 | 92.27 | 88.16 | 5159 |
| section_title | 78.12 | 66.6 | 71.9 | 17540 |
| table_title | 15.93 | 8.36 | 10.96 | 6092 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **76.95** | **77.19** | **77.07** | 120771 |
| all fields (macro avg.) | 63.21 | 58.44 | 57.88 | 120771 |


**Document-level ratio results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 99.73 | 96.28 | 97.98 | 779 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **99.73** | **96.28** | **97.98** | 779 |
| all fields (macro avg.) | 99.73 | 96.28 | 97.98 | 779 |

Evaluation metrics produced in 1136.421 seconds
