
## Header metadata 

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 99.18 | 99.28 | 99.23 | 969 |
| first_author | 99.48 | 99.59 | 99.54 | 969 |
| title | 95.89 | 95.7 | 95.8 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **98.16** | **98.16** | **98.16** | 2938 |
| all fields (macro avg.) | 98.18 | 98.19 | 98.19 | 2938 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 99.18 | 99.28 | 99.23 | 969 |
| first_author | 99.48 | 99.59 | 99.54 | 969 |
| title | 99.5 | 99.3 | 99.4 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **99.39** | **99.39** | **99.39** | 2938 |
| all fields (macro avg.) | 99.39 | 99.39 | 99.39 | 2938 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 99.48 | 99.59 | 99.54 | 969 |
| first_author | 99.59 | 99.69 | 99.64 | 969 |
| title | 99.7 | 99.5 | 99.6 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **99.59** | **99.59** | **99.59** | 2938 |
| all fields (macro avg.) | 99.59 | 99.59 | 99.59 | 2938 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 99.38 | 99.48 | 99.43 | 969 |
| first_author | 99.48 | 99.59 | 99.54 | 969 |
| title | 99.7 | 99.5 | 99.6 | 1000 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **99.52** | **99.52** | **99.52** | 2938 |
| all fields (macro avg.) | 99.52 | 99.52 | 99.52 | 2938 |


#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	952 (strict) 
Total correct instances: 	988 (soft) 
Total correct instances: 	992 (Levenshtein) 
Total correct instances: 	991 (ObservedRatcliffObershelp) 

Instance-level recall:	95.2	(strict) 
Instance-level recall:	98.8	(soft) 
Instance-level recall:	99.2	(Levenshtein) 
Instance-level recall:	99.1	(RatcliffObershelp) 
```


## Citation metadata 

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 81.13 | 78.41 | 79.75 | 44770 |
| date | 84.56 | 81.24 | 82.87 | 45457 |
| first_author | 91.44 | 88.34 | 89.86 | 44770 |
| inTitle | 81.61 | 83.57 | 82.58 | 42795 |
| issue | 93.48 | 92.7 | 93.09 | 18983 |
| page | 93.63 | 77.54 | 84.83 | 40844 |
| title | 59.94 | 60.47 | 60.2 | 43101 |
| volume | 95.82 | 96.1 | 95.96 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **84.18** | **81.44** | **82.78** | 321178 |
| all fields (macro avg.) | 85.2 | 82.29 | 83.64 | 321178 |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 81.45 | 78.71 | 80.06 | 44770 |
| date | 84.56 | 81.24 | 82.87 | 45457 |
| first_author | 91.66 | 88.55 | 90.08 | 44770 |
| inTitle | 85.44 | 87.49 | 86.45 | 42795 |
| issue | 93.48 | 92.7 | 93.09 | 18983 |
| page | 93.63 | 77.54 | 84.83 | 40844 |
| title | 91.92 | 92.74 | 92.33 | 43101 |
| volume | 95.82 | 96.1 | 95.96 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.27** | **86.36** | **87.79** | 321178 |
| all fields (macro avg.) | 89.74 | 86.88 | 88.21 | 321178 |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 90.61 | 87.57 | 89.06 | 44770 |
| date | 84.56 | 81.24 | 82.87 | 45457 |
| first_author | 92.19 | 89.07 | 90.6 | 44770 |
| inTitle | 86.38 | 88.45 | 87.41 | 42795 |
| issue | 93.48 | 92.7 | 93.09 | 18983 |
| page | 93.63 | 77.54 | 84.83 | 40844 |
| title | 94.52 | 95.35 | 94.93 | 43101 |
| volume | 95.82 | 96.1 | 95.96 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **91.11** | **88.15** | **89.61** | 321178 |
| all fields (macro avg.) | 91.4 | 88.5 | 89.84 | 321178 |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---               |---         |---        |---         |---      |
| authors | 84.9 | 82.05 | 83.45 | 44770 |
| date | 84.56 | 81.24 | 82.87 | 45457 |
| first_author | 91.44 | 88.34 | 89.86 | 44770 |
| inTitle | 85.09 | 87.13 | 86.1 | 42795 |
| issue | 93.48 | 92.7 | 93.09 | 18983 |
| page | 93.63 | 77.54 | 84.83 | 40844 |
| title | 93.9 | 94.73 | 94.31 | 43101 |
| volume | 95.82 | 96.1 | 95.96 | 40458 |
|                  |            |           |            |         |
| **all fields (micro avg.)** | **89.94** | **87.02** | **88.46** | 321178 |
| all fields (macro avg.) | 90.35 | 87.48 | 88.81 | 321178 |


#### Instance-level results

```
Total expected instances: 		48449
Total extracted instances: 		48344
Total correct instances: 		13485 (strict) 
Total correct instances: 		22253 (soft) 
Total correct instances: 		24898 (Levenshtein) 
Total correct instances: 		23252 (RatcliffObershelp) 

Instance-level precision:	27.89 (strict) 
Instance-level precision:	46.03 (soft) 
Instance-level precision:	51.5 (Levenshtein) 
Instance-level precision:	48.1 (RatcliffObershelp) 

Instance-level recall:	27.83	(strict) 
Instance-level recall:	45.93	(soft) 
Instance-level recall:	51.39	(Levenshtein) 
Instance-level recall:	47.99	(RatcliffObershelp) 

Instance-level f-score:	27.86 (strict) 
Instance-level f-score:	45.98 (soft) 
Instance-level f-score:	51.45 (Levenshtein) 
Instance-level f-score:	48.04 (RatcliffObershelp) 

Matching 1 :	35367

Matching 2 :	1257

Matching 3 :	3269

Matching 4 :	1801

Total matches :	41694
```


#### Citation context resolution
```

Total expected references: 	 48449 - 48.45 references per article
Total predicted references: 	 48344 - 48.34 references per article

Total expected citation contexts: 	 69755 - 69.75 citation contexts per article
Total predicted citation contexts: 	 0 - 0 citation contexts per article

Total correct predicted citation contexts: 	 0 - 0 citation contexts per article
Total wrong predicted citation contexts: 	 0 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 NaN
Recall citation contexts: 	 0
fscore citation contexts: 	 NaN
```

Evaluation metrics produced in 893.539 seconds
