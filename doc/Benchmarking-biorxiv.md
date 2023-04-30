# Benchmarking biorXiv

## General

This is the end-to-end benchmarking result for GROBID version **0.7.3** against the `bioRxiv` test set (`biorxiv-10k-test-2000`), see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models.  

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 2000 PDF preprints out of 2000 (no failure).

Runtime for processing 2000 PDF: **3133s** (1.56 second per PDF) on Ubuntu 16.04, 4 CPU i7-4790K (8 threads), 16GB RAM (workstation bought in 2015 for 1600 euros) and with a GeForce GTX 1050 Ti GPU.

Note: with CRF only models runtime is 622s (0.31 second per PDF). 

## Header metadata 

Evaluation on 2000 random PDF files out of 2000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 2.2 | 2.16 | 2.18 | 1990 |
| authors | 73.66 | 73.04 | 73.35 | 1999 |
| first_author | 94.3 | 93.59 | 93.94 | 1997 |
| keywords | 58.71 | 59.83 | 59.27 | 839 |
| title | 82.36 | 81.5 | 81.93 | 2000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **62.91** | **62.37** | **62.64** | 8825 |
| all fields (macro avg.) | 62.25 | 62.02 | 62.13 | 8825 |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 59.71 | 58.54 | 59.12 | 1990 |
| authors | 75.93 | 75.29 | 75.61 | 1999 |
| first_author | 94.9 | 94.19 | 94.55 | 1997 |
| keywords | 63.86 | 65.08 | 64.46 | 839 |
| title | 90.8 | 89.85 | 90.32 | 2000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **78.8** | **78.12** | **78.46** | 8825 |
| all fields (macro avg.) | 77.04 | 76.59 | 76.81 | 8825 |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 80.22 | 78.64 | 79.42 | 1990 |
| authors | 89.61 | 88.84 | 89.22 | 1999 |
| first_author | 95.21 | 94.49 | 94.85 | 1997 |
| keywords | 79.42 | 80.93 | 80.17 | 839 |
| title | 94.59 | 93.6 | 94.09 | 2000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **88.91** | **88.15** | **88.53** | 8825 |
| all fields (macro avg.) | 87.81 | 87.3 | 87.55 | 8825 |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 76.88 | 75.38 | 76.12 | 1990 |
| authors | 82.04 | 81.34 | 81.69 | 1999 |
| first_author | 94.3 | 93.59 | 93.94 | 1997 |
| keywords | 71.35 | 72.71 | 72.02 | 839 |
| title | 93.58 | 92.6 | 93.09 | 2000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **85.23** | **84.5** | **84.86** | 8825 |
| all fields (macro avg.) | 83.63 | 83.12 | 83.37 | 8825 |


#### Instance-level results

```
Total expected instances:   2000
Total correct instances:    29 (strict) 
Total correct instances:    718 (soft) 
Total correct instances:    1207 (Levenshtein) 
Total correct instances:    1024 (ObservedRatcliffObershelp) 

Instance-level recall:  1.45    (strict) 
Instance-level recall:  35.9    (soft) 
Instance-level recall:  60.35   (Levenshtein) 
Instance-level recall:  51.2    (RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 2000 random PDF files out of 2000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 88.14 | 83.22 | 85.61 | 97183 |
| date | 91.69 | 86.3 | 88.91 | 97630 |
| doi | 70.83 | 83.79 | 76.77 | 16894 |
| first_author | 95.05 | 89.67 | 92.28 | 97183 |
| inTitle | 82.83 | 79.41 | 81.08 | 96430 |
| issue | 94.33 | 92.04 | 93.17 | 30312 |
| page | 94.97 | 78.34 | 85.85 | 88597 |
| pmcid | 66.38 | 86.12 | 74.97 | 807 |
| pmid | 70.06 | 84.95 | 76.79 | 2093 |
| title | 84.96 | 83.66 | 84.3 | 92463 |
| volume | 96.23 | 95.23 | 95.72 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.85** | **85.35** | **87.54** | 707301 |
| all fields (macro avg.) | 85.04 | 85.7 | 85.04 | 707301 |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 89.3 | 84.32 | 86.74 | 97183 |
| date | 91.69 | 86.3 | 88.91 | 97630 |
| doi | 75.32 | 89.09 | 81.63 | 16894 |
| first_author | 95.48 | 90.07 | 92.7 | 97183 |
| inTitle | 92.32 | 88.51 | 90.38 | 96430 |
| issue | 94.33 | 92.04 | 93.17 | 30312 |
| page | 94.97 | 78.34 | 85.85 | 88597 |
| pmcid | 75.64 | 98.14 | 85.44 | 807 |
| pmid | 74.47 | 90.3 | 81.62 | 2093 |
| title | 93.21 | 91.79 | 92.5 | 92463 |
| volume | 96.23 | 95.23 | 95.72 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **92.66** | **88.01** | **90.27** | 707301 |
| all fields (macro avg.) | 88.45 | 89.47 | 88.61 | 707301 |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 94.57 | 89.29 | 91.85 | 97183 |
| date | 91.69 | 86.3 | 88.91 | 97630 |
| doi | 77.59 | 91.78 | 84.09 | 16894 |
| first_author | 95.63 | 90.21 | 92.84 | 97183 |
| inTitle | 93.3 | 89.44 | 91.33 | 96430 |
| issue | 94.33 | 92.04 | 93.17 | 30312 |
| page | 94.97 | 78.34 | 85.85 | 88597 |
| pmcid | 75.64 | 98.14 | 85.44 | 807 |
| pmid | 74.47 | 90.3 | 81.62 | 2093 |
| title | 96.04 | 94.57 | 95.3 | 92463 |
| volume | 96.23 | 95.23 | 95.72 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.98** | **89.27** | **91.57** | 707301 |
| all fields (macro avg.) | 89.5 | 90.51 | 89.65 | 707301 |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 91.52 | 86.41 | 88.89 | 97183 |
| date | 91.69 | 86.3 | 88.91 | 97630 |
| doi | 76.02 | 89.93 | 82.39 | 16894 |
| first_author | 95.1 | 89.71 | 92.33 | 97183 |
| inTitle | 91.06 | 87.29 | 89.13 | 96430 |
| issue | 94.33 | 92.04 | 93.17 | 30312 |
| page | 94.97 | 78.34 | 85.85 | 88597 |
| pmcid | 66.38 | 86.12 | 74.97 | 807 |
| pmid | 70.06 | 84.95 | 76.79 | 2093 |
| title | 95.35 | 93.89 | 94.61 | 92463 |
| volume | 96.23 | 95.23 | 95.72 | 87709 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.01** | **88.35** | **90.62** | 707301 |
| all fields (macro avg.) | 87.52 | 88.2 | 87.53 | 707301 |

#### Instance-level results

```
Total expected instances:       98799
Total extracted instances:      98068
Total correct instances:        43806 (strict) 
Total correct instances:        54774 (soft) 
Total correct instances:        58973 (Levenshtein) 
Total correct instances:        55696 (RatcliffObershelp) 

Instance-level precision:   44.67 (strict) 
Instance-level precision:   55.85 (soft) 
Instance-level precision:   60.13 (Levenshtein) 
Instance-level precision:   56.79 (RatcliffObershelp) 

Instance-level recall:  44.34   (strict) 
Instance-level recall:  55.44   (soft) 
Instance-level recall:  59.69   (Levenshtein) 
Instance-level recall:  56.37   (RatcliffObershelp) 

Instance-level f-score: 44.5 (strict) 
Instance-level f-score: 55.65 (soft) 
Instance-level f-score: 59.91 (Levenshtein) 
Instance-level f-score: 56.58 (RatcliffObershelp) 

Matching 1 :    79286

Matching 2 :    4449

Matching 3 :    4366

Matching 4 :    2086

Total matches : 90187
```


#### Citation context resolution
```

Total expected references:   98797 - 49.4 references per article
Total predicted references:      98068 - 49.03 references per article

Total expected citation contexts:    142862 - 71.43 citation contexts per article
Total predicted citation contexts:   135679 - 67.84 citation contexts per article

Total correct predicted citation contexts:   116704 - 58.35 citation contexts per article
Total wrong predicted citation contexts:     18975 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     86.01
Recall citation contexts:    81.69
fscore citation contexts:    83.8
```

## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 2000 random PDF files out of 2000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 0 | 0 | 0 | 0 |
| figure_title | 4.24 | 2.01 | 2.72 | 22978 |
| funding_stmt | 0 | 0 | 0 | 0 |
| reference_citation | 71.04 | 71.33 | 71.18 | 147470 |
| reference_figure | 70.59 | 67.74 | 69.13 | 47984 |
| reference_table | 48.12 | 83.06 | 60.94 | 5957 |
| section_title | 72.6 | 69.59 | 71.07 | 32399 |
| table_title | 4.34 | 2.88 | 3.46 | 3925 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **66.59** | **63.58** | **65.05** | 260713 |
| all fields (macro avg.) | 45.16 | 49.43 | 46.42 | 260713 |


#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 0 | 0 | 0 | 0 |
| figure_title | 69.47 | 32.89 | 44.65 | 22978 |
| funding_stmt | 0 | 0 | 0 | 0 |
| reference_citation | 83.03 | 83.37 | 83.2 | 147470 |
| reference_figure | 71.21 | 68.34 | 69.75 | 47984 |
| reference_table | 48.57 | 83.83 | 61.51 | 5957 |
| section_title | 76.48 | 73.31 | 74.87 | 32399 |
| table_title | 51.4 | 34.09 | 40.99 | 3925 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **77.68** | **74.17** | **75.89** | 260713 |
| all fields (macro avg.) | 66.7 | 62.64 | 62.49 | 260713 |

