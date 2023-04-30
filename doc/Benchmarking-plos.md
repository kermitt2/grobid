# Benchmarking PLOS

## General

This is the end-to-end benchmarking result for GROBID version **0.7.3** against the `PLOS` test set, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. 

The following end-to-end results are using:

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the header model

- **BidLSTM_ChainCRF_FEATURES** as sequence labeling for the reference-segmenter model

- **BidLSTM-CRF-FEATURES** as sequence labeling for the citation model

- **BidLSTM_CRF_FEATURES** as sequence labeling for the affiliation-address model

- **CRF Wapiti** as sequence labelling engine for all other models.  

Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service should be similar but much slower). 

Other versions of these benchmarks with variants and **Deep Learning models** (e.g. newer master snapshots) are available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc). Note that Deep Learning models might provide higher accuracy, but at the cost of slower runtime and more expensive CPU/GPU resources. 

Evaluation on 1000 PDF preprints out of 1000 (no failure).

Runtime for processing 1000 PDF: **1831s** (1.83 second per PDF) on Ubuntu 16.04, 4 CPU i7-4790K (8 threads), 16GB RAM (workstation bought in 2015 for 1600 euros) and with a GeForce GTX 1050 Ti GPU.

Note: with CRF only models runtime is 304s (0.30 seconds per PDF). 


## Header metadata 

Evaluation on 1000 random PDF files out of 1000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 13.58 | 13.65 | 13.61 | 960 |
| authors | 98.97 | 99.07 | 99.02 | 969 |
| first_author | 99.28 | 99.38 | 99.33 | 969 |
| keywords | 0 | 0 | 0 | 0 |
| title | 95.65 | 94.5 | 95.07 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **77.04** | **76.94** | **76.99** | 3898 |
| all fields (macro avg.) | 76.87 | 76.65 | 76.76 | 3898 |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 50.57 | 50.83 | 50.7 | 960 |
| authors | 98.97 | 99.07 | 99.02 | 969 |
| first_author | 99.28 | 99.38 | 99.33 | 969 |
| keywords | 0 | 0 | 0 | 0 |
| title | 99.29 | 98.1 | 98.69 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **87.13** | **87.02** | **87.07** | 3898 |
| all fields (macro avg.) | 87.03 | 86.85 | 86.94 | 3898 |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 76.68 | 77.08 | 76.88 | 960 |
| authors | 99.28 | 99.38 | 99.33 | 969 |
| first_author | 99.38 | 99.48 | 99.43 | 969 |
| keywords | 0 | 0 | 0 | 0 |
| title | 99.7 | 98.5 | 99.09 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **93.81** | **93.69** | **93.75** | 3898 |
| all fields (macro avg.) | 93.76 | 93.61 | 93.68 | 3898 |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| abstract | 66.94 | 67.29 | 67.12 | 960 |
| authors | 99.18 | 99.28 | 99.23 | 969 |
| first_author | 99.28 | 99.38 | 99.33 | 969 |
| keywords | 0 | 0 | 0 | 0 |
| title | 99.49 | 98.3 | 98.89 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.29** | **91.17** | **91.23** | 3898 |
| all fields (macro avg.) | 91.22 | 91.06 | 91.14 | 3898 |


#### Instance-level results

```
Total expected instances:   1000
Total correct instances:    139 (strict) 
Total correct instances:    488 (soft) 
Total correct instances:    727 (Levenshtein) 
Total correct instances:    643 (ObservedRatcliffObershelp) 

Instance-level recall:  13.9    (strict) 
Instance-level recall:  48.8    (soft) 
Instance-level recall:  72.7    (Levenshtein) 
Instance-level recall:  64.3    (RatcliffObershelp) 
```

## Citation metadata 

Evaluation on 1000 random PDF files out of 1000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 81.18 | 78.43 | 79.78 | 44770 |
| date | 84.62 | 81.24 | 82.9 | 45457 |
| first_author | 91.48 | 88.35 | 89.88 | 44770 |
| inTitle | 81.67 | 83.57 | 82.61 | 42795 |
| issue | 93.63 | 92.68 | 93.15 | 18983 |
| page | 93.69 | 77.57 | 84.87 | 40844 |
| title | 60.02 | 60.53 | 60.28 | 43101 |
| volume | 95.89 | 96.11 | 96 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **84.24** | **81.45** | **82.82** | 321178 |
| all fields (macro avg.) | 85.27 | 82.31 | 83.68 | 321178 |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 81.49 | 78.73 | 80.09 | 44770 |
| date | 84.62 | 81.24 | 82.9 | 45457 |
| first_author | 91.69 | 88.56 | 90.1 | 44770 |
| inTitle | 85.51 | 87.5 | 86.49 | 42795 |
| issue | 93.63 | 92.68 | 93.15 | 18983 |
| page | 93.69 | 77.57 | 84.87 | 40844 |
| title | 91.97 | 92.75 | 92.36 | 43101 |
| volume | 95.89 | 96.11 | 96 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.33** | **86.37** | **87.82** | 321178 |
| all fields (macro avg.) | 89.81 | 86.89 | 88.24 | 321178 |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 90.65 | 87.58 | 89.08 | 44770 |
| date | 84.62 | 81.24 | 82.9 | 45457 |
| first_author | 92.23 | 89.08 | 90.63 | 44770 |
| inTitle | 86.46 | 88.47 | 87.45 | 42795 |
| issue | 93.63 | 92.68 | 93.15 | 18983 |
| page | 93.69 | 77.57 | 84.87 | 40844 |
| title | 94.57 | 95.37 | 94.97 | 43101 |
| volume | 95.89 | 96.11 | 96 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.17** | **88.16** | **89.64** | 321178 |
| all fields (macro avg.) | 91.47 | 88.51 | 89.88 | 321178 |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 84.94 | 82.06 | 83.47 | 44770 |
| date | 84.62 | 81.24 | 82.9 | 45457 |
| first_author | 91.48 | 88.35 | 89.88 | 44770 |
| inTitle | 85.16 | 87.14 | 86.14 | 42795 |
| issue | 93.63 | 92.68 | 93.15 | 18983 |
| page | 93.69 | 77.57 | 84.87 | 40844 |
| title | 93.95 | 94.74 | 94.34 | 43101 |
| volume | 95.89 | 96.11 | 96 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **90** | **87.03** | **88.49** | 321178 |
| all fields (macro avg.) | 90.42 | 87.49 | 88.84 | 321178 |


#### Instance-level results

```
Total expected instances:       48449
Total extracted instances:      48250
Total correct instances:        13512 (strict) 
Total correct instances:        22263 (soft) 
Total correct instances:        24909 (Levenshtein) 
Total correct instances:        23261 (RatcliffObershelp) 

Instance-level precision:   28 (strict) 
Instance-level precision:   46.14 (soft) 
Instance-level precision:   51.62 (Levenshtein) 
Instance-level precision:   48.21 (RatcliffObershelp) 

Instance-level recall:  27.89   (strict) 
Instance-level recall:  45.95   (soft) 
Instance-level recall:  51.41   (Levenshtein) 
Instance-level recall:  48.01   (RatcliffObershelp) 

Instance-level f-score: 27.95 (strict) 
Instance-level f-score: 46.05 (soft) 
Instance-level f-score: 51.52 (Levenshtein) 
Instance-level f-score: 48.11 (RatcliffObershelp) 

Matching 1 :    35372

Matching 2 :    1257

Matching 3 :    3268

Matching 4 :    1799

Total matches : 41696
```


#### Citation context resolution
```

Total expected references:   48449 - 48.45 references per article
Total predicted references:      48250 - 48.25 references per article

Total expected citation contexts:    69755 - 69.75 citation contexts per article
Total predicted citation contexts:   73696 - 73.7 citation contexts per article

Total correct predicted citation contexts:   56769 - 56.77 citation contexts per article
Total wrong predicted citation contexts:     16927 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     77.03
Recall citation contexts:    81.38
fscore citation contexts:    79.15
```


## Fulltext structures 

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.

Evaluation on 1000 random PDF files out of 1000 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 50.87 | 49.04 | 49.93 | 779 |
| figure_title | 2.11 | 0.92 | 1.28 | 8943 |
| funding_stmt | 49.49 | 35.17 | 41.12 | 1507 |
| reference_citation | 86.69 | 94.65 | 90.49 | 69741 |
| reference_figure | 72.05 | 54.06 | 61.77 | 11010 |
| reference_table | 84.28 | 92.07 | 88 | 5159 |
| section_title | 77.18 | 65.8 | 71.04 | 17540 |
| table_title | 1.13 | 0.59 | 0.78 | 6092 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **78.38** | **73.93** | **76.09** | 120771 |
| all fields (macro avg.) | 52.97 | 49.04 | 50.55 | 120771 |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 79.36 | 76.51 | 77.91 | 779 |
| figure_title | 81.2 | 35.36 | 49.26 | 8943 |
| funding_stmt | 56.4 | 40.08 | 46.86 | 1507 |
| reference_citation | 86.7 | 94.66 | 90.5 | 69741 |
| reference_figure | 72.51 | 54.41 | 62.17 | 11010 |
| reference_table | 84.46 | 92.27 | 88.19 | 5159 |
| section_title | 78.17 | 66.65 | 71.96 | 17540 |
| table_title | 15.98 | 8.39 | 11 | 6092 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **81.93** | **77.28** | **79.54** | 120771 |
| all fields (macro avg.) | 69.35 | 58.54 | 62.23 | 120771 |

**Document-level ratio results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| availability_stmt | 99.47 | 96.41 | 97.91 | 779 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **99.47** | **96.41** | **97.91** | 779 |
| all fields (macro avg.) | 99.47 | 96.41 | 97.91 | 779 |

Evaluation metrics produced in 555.701 seconds

