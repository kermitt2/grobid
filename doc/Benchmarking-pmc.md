# Benchmarking PubMed Central

## General

This is the end-to-end benchmarking result for GROBID version **0.8.1** against the `PMC_sample_1943` dataset, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models. 

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 1943 random PDF PMC files out of 1943 PDF from 1943 different journals (0 PDF parsing failure).

Runtime for processing 1943 PDF: **1467** seconds, (0.75s per PDF) on Ubuntu 22.04, 16 CPU (32 threads), 128GB RAM and with a GeForce GTX 1080 Ti GPU.

Note: with CRF only models, runtime is 470s (0.24 seconds per PDF) with 4 CPU, 8 threads.




## Header metadata 

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 16.78 | 16.48 | 16.63 | 1911 |
| authors | 92.77 | 92.58 | 92.68 | 1941 |
| first_author | 96.75 | 96.55 | 96.65 | 1941 |
| keywords | 65.6 | 63.99 | 64.78 | 1380 |
| title | 84.56 | 84.25 | 84.4 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **72.02** | **71.37** | **71.7** | 9116 |
| all fields (macro avg.) | 71.29 | 70.77 | 71.03 | 9116 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 63.88 | 62.74 | 63.31 | 1911 |
| authors | 94.68 | 94.49 | 94.58 | 1941 |
| first_author | 97.11 | 96.91 | 97.01 | 1941 |
| keywords | 74.22 | 72.39 | 73.29 | 1380 |
| title | 92.15 | 91.82 | 91.98 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **85.21** | **84.43** | **84.82** | 9116 |
| all fields (macro avg.) | 84.41 | 83.67 | 84.04 | 9116 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 90.68 | 89.06 | 89.86 | 1911 |
| authors | 96.8 | 96.6 | 96.7 | 1941 |
| first_author | 97.42 | 97.22 | 97.32 | 1941 |
| keywords | 84.62 | 82.54 | 83.57 | 1380 |
| title | 98.35 | 97.99 | 98.17 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **94.18** | **93.32** | **93.75** | 9116 |
| all fields (macro avg.) | 93.57 | 92.68 | 93.12 | 9116 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 86.84 | 85.3 | 86.06 | 1911 |
| authors | 95.82 | 95.62 | 95.72 | 1941 |
| first_author | 96.75 | 96.55 | 96.65 | 1941 |
| keywords | 79.87 | 77.9 | 78.87 | 1380 |
| title | 96.33 | 95.99 | 96.16 | 1943 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.89** | **91.05** | **91.47** | 9116 |
| all fields (macro avg.) | 91.12 | 90.27 | 90.69 | 9116 |


#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	218 (strict) 
Total correct instances: 	910 (soft) 
Total correct instances: 	1451 (Levenshtein) 
Total correct instances: 	1303 (ObservedRatcliffObershelp) 

Instance-level recall:	11.22	(strict) 
Instance-level recall:	46.83	(soft) 
Instance-level recall:	74.68	(Levenshtein) 
Instance-level recall:	67.06	(RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 83.04 | 76.17 | 79.46 | 85778 |
| date | 94.64 | 84.08 | 89.04 | 87067 |
| first_author | 89.77 | 82.33 | 85.89 | 85778 |
| inTitle | 73.19 | 71.7 | 72.44 | 81007 |
| issue | 91.11 | 87.62 | 89.33 | 16635 |
| page | 94.58 | 83.55 | 88.72 | 80501 |
| title | 79.68 | 75.16 | 77.36 | 80736 |
| volume | 96.05 | 89.64 | 92.73 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **87.23** | **80.58** | **83.77** | 597569 |
| all fields (macro avg.) | 87.76 | 81.28 | 84.37 | 597569 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 83.51 | 76.6 | 79.91 | 85778 |
| date | 94.64 | 84.08 | 89.04 | 87067 |
| first_author | 89.94 | 82.48 | 86.05 | 85778 |
| inTitle | 84.92 | 83.19 | 84.05 | 81007 |
| issue | 91.11 | 87.62 | 89.33 | 16635 |
| page | 94.58 | 83.55 | 88.72 | 80501 |
| title | 91.45 | 86.27 | 88.79 | 80736 |
| volume | 96.05 | 89.64 | 92.73 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90.63** | **83.72** | **87.04** | 597569 |
| all fields (macro avg.) | 90.77 | 84.18 | 87.33 | 597569 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 89.2 | 81.83 | 85.35 | 85778 |
| date | 94.64 | 84.08 | 89.04 | 87067 |
| first_author | 90.15 | 82.67 | 86.25 | 85778 |
| inTitle | 86.17 | 84.41 | 85.28 | 81007 |
| issue | 91.11 | 87.62 | 89.33 | 16635 |
| page | 94.58 | 83.55 | 88.72 | 80501 |
| title | 93.8 | 88.48 | 91.06 | 80736 |
| volume | 96.05 | 89.64 | 92.73 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.97** | **84.97** | **88.33** | 597569 |
| all fields (macro avg.) | 91.96 | 85.29 | 88.47 | 597569 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 85.97 | 78.86 | 82.27 | 85778 |
| date | 94.64 | 84.08 | 89.04 | 87067 |
| first_author | 89.79 | 82.35 | 85.91 | 85778 |
| inTitle | 83.49 | 81.79 | 82.63 | 81007 |
| issue | 91.11 | 87.62 | 89.33 | 16635 |
| page | 94.58 | 83.55 | 88.72 | 80501 |
| title | 93.4 | 88.11 | 90.67 | 80736 |
| volume | 96.05 | 89.64 | 92.73 | 80067 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.02** | **84.09** | **87.42** | 597569 |
| all fields (macro avg.) | 91.13 | 84.5 | 87.66 | 597569 |


#### Instance-level results

```
Total expected instances: 		90125
Total extracted instances: 		85714
Total correct instances: 		38682 (strict) 
Total correct instances: 		50817 (soft) 
Total correct instances: 		55666 (Levenshtein) 
Total correct instances: 		52220 (RatcliffObershelp) 

Instance-level precision:	45.13 (strict) 
Instance-level precision:	59.29 (soft) 
Instance-level precision:	64.94 (Levenshtein) 
Instance-level precision:	60.92 (RatcliffObershelp) 

Instance-level recall:	42.92	(strict) 
Instance-level recall:	56.39	(soft) 
Instance-level recall:	61.77	(Levenshtein) 
Instance-level recall:	57.94	(RatcliffObershelp) 

Instance-level f-score:	44 (strict) 
Instance-level f-score:	57.8 (soft) 
Instance-level f-score:	63.31 (Levenshtein) 
Instance-level f-score:	59.4 (RatcliffObershelp) 

Matching 1 :	68202

Matching 2 :	4132

Matching 3 :	1867

Matching 4 :	661

Total matches :	74862
```


#### Citation context resolution
```

Total expected references: 	 90125 - 46.38 references per article
Total predicted references: 	 85714 - 44.11 references per article

Total expected citation contexts: 	 139835 - 71.97 citation contexts per article
Total predicted citation contexts: 	 115314 - 59.35 citation contexts per article

Total correct predicted citation contexts: 	 97227 - 50.04 citation contexts per article
Total wrong predicted citation contexts: 	 18087 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 84.32
Recall citation contexts: 	 69.53
fscore citation contexts: 	 76.21
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.


Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 31.5 | 24.64 | 27.65 | 7281 |
| reference_citation | 57.43 | 58.72 | 58.07 | 134196 |
| reference_figure | 61.23 | 65.91 | 63.49 | 19330 |
| reference_table | 82.96 | 88.47 | 85.63 | 7327 |
| section_title | 76.52 | 67.67 | 71.82 | 27619 |
| table_title | 57.5 | 50.57 | 53.81 | 3971 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **60.42** | **60.34** | **60.38** | 199724 |
| all fields (macro avg.) | 61.19 | 59.33 | 60.08 | 199724 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| figure_title | 78.63 | 61.52 | 69.03 | 7281 |
| reference_citation | 61.68 | 63.07 | 62.37 | 134196 |
| reference_figure | 61.71 | 66.43 | 63.98 | 19330 |
| reference_table | 83.14 | 88.66 | 85.81 | 7327 |
| section_title | 81.42 | 72.01 | 76.43 | 27619 |
| table_title | 82.27 | 72.35 | 76.99 | 3971 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **65.79** | **65.7** | **65.74** | 199724 |
| all fields (macro avg.) | 74.81 | 70.67 | 72.44 | 199724 |



