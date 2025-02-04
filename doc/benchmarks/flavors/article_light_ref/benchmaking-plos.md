
## Header metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.07     | 99.07     | 99.07     | 969     |
| first_author                | 99.38     | 99.38     | 99.38     | 969     |
| title                       | 95.76     | 94.8      | 95.28     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **98.05** | **97.72** | **97.89** | 2938    |
| all fields (macro avg.)     | 98.07     | 97.75     | 97.91     | 2938    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.07     | 99.07     | 99.07     | 969     |
| first_author                | 99.38     | 99.38     | 99.38     | 969     |
| title                       | 99.29     | 98.3      | 98.79     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.25** | **98.91** | **99.08** | 2938    |
| all fields (macro avg.)     | 99.25     | 98.92     | 99.08     | 2938    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.38     | 99.38     | 99.38     | 969     |
| first_author                | 99.48     | 99.48     | 99.48     | 969     |
| title                       | 99.7      | 98.7      | 99.2      | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.52** | **99.18** | **99.35** | 2938    |
| all fields (macro avg.)     | 99.52     | 99.19     | 99.35     | 2938    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.28     | 99.28     | 99.28     | 969     |
| first_author                | 99.38     | 99.38     | 99.38     | 969     |
| title                       | 99.49     | 98.5      | 98.99     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.39** | **99.05** | **99.22** | 2938    |
| all fields (macro avg.)     | 99.38     | 99.05     | 99.22     | 2938    |

#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	943 (strict) 
Total correct instances: 	978 (soft) 
Total correct instances: 	982 (Levenshtein) 
Total correct instances: 	981 (ObservedRatcliffObershelp) 

Instance-level recall:	94.3	(strict) 
Instance-level recall:	97.8	(soft) 
Instance-level recall:	98.2	(Levenshtein) 
Instance-level recall:	98.1	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.15     | 78.34     | 79.72     | 44770   |
| date                        | 84.59     | 81.14     | 82.83     | 45457   |
| first_author                | 91.45     | 88.25     | 89.82     | 44770   |
| inTitle                     | 81.65     | 83.48     | 82.56     | 42795   |
| issue                       | 93.57     | 92.54     | 93.05     | 18983   |
| page                        | 93.68     | 77.5      | 84.82     | 40844   |
| title                       | 59.96     | 60.41     | 60.18     | 43101   |
| volume                      | 95.86     | 96.01     | 95.94     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **84.21** | **81.35** | **82.76** | 321178  |
| all fields (macro avg.)     | 85.24     | 82.21     | 83.62     | 321178  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.47     | 78.64     | 80.03     | 44770   |
| date                        | 84.59     | 81.14     | 82.83     | 45457   |
| first_author                | 91.67     | 88.46     | 90.04     | 44770   |
| inTitle                     | 85.48     | 87.39     | 86.42     | 42795   |
| issue                       | 93.57     | 92.54     | 93.05     | 18983   |
| page                        | 93.68     | 77.5      | 84.82     | 40844   |
| title                       | 91.95     | 92.65     | 92.3      | 43101   |
| volume                      | 95.86     | 96.01     | 95.94     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.3**  | **86.27** | **87.76** | 321178  |
| all fields (macro avg.)     | 89.78     | 86.79     | 88.18     | 321178  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 90.62     | 87.48     | 89.02     | 44770   |
| date                        | 84.59     | 81.14     | 82.83     | 45457   |
| first_author                | 92.21     | 88.98     | 90.56     | 44770   |
| inTitle                     | 86.41     | 88.35     | 87.37     | 42795   |
| issue                       | 93.57     | 92.54     | 93.05     | 18983   |
| page                        | 93.68     | 77.5      | 84.82     | 40844   |
| title                       | 94.54     | 95.26     | 94.9      | 43101   |
| volume                      | 95.86     | 96.01     | 95.94     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.15** | **88.06** | **89.57** | 321178  |
| all fields (macro avg.)     | 91.44     | 88.41     | 89.81     | 321178  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 84.91     | 81.97     | 83.41     | 44770   |
| date                        | 84.59     | 81.14     | 82.83     | 45457   |
| first_author                | 91.45     | 88.25     | 89.82     | 44770   |
| inTitle                     | 85.13     | 87.04     | 86.07     | 42795   |
| issue                       | 93.57     | 92.54     | 93.05     | 18983   |
| page                        | 93.68     | 77.5      | 84.82     | 40844   |
| title                       | 93.93     | 94.64     | 94.28     | 43101   |
| volume                      | 95.86     | 96.01     | 95.94     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.98** | **86.93** | **88.43** | 321178  |
| all fields (macro avg.)     | 90.39     | 87.38     | 88.78     | 321178  |

#### Instance-level results

```
Total expected instances: 		48449
Total extracted instances: 		48212
Total correct instances: 		13475 (strict) 
Total correct instances: 		22235 (soft) 
Total correct instances: 		24874 (Levenshtein) 
Total correct instances: 		23231 (RatcliffObershelp) 

Instance-level precision:	27.95 (strict) 
Instance-level precision:	46.12 (soft) 
Instance-level precision:	51.59 (Levenshtein) 
Instance-level precision:	48.19 (RatcliffObershelp) 

Instance-level recall:	27.81	(strict) 
Instance-level recall:	45.89	(soft) 
Instance-level recall:	51.34	(Levenshtein) 
Instance-level recall:	47.95	(RatcliffObershelp) 

Instance-level f-score:	27.88 (strict) 
Instance-level f-score:	46.01 (soft) 
Instance-level f-score:	51.47 (Levenshtein) 
Instance-level f-score:	48.07 (RatcliffObershelp) 

Matching 1 :	35329

Matching 2 :	1255

Matching 3 :	3266

Matching 4 :	1801

Total matches :	41651
```

#### Citation context resolution

```

Total expected references: 	 48449 - 48.45 references per article
Total predicted references: 	 48212 - 48.21 references per article

Total expected citation contexts: 	 69755 - 69.75 citation contexts per article
Total predicted citation contexts: 	 0 - 0 citation contexts per article

Total correct predicted citation contexts: 	 0 - 0 citation contexts per article
Total wrong predicted citation contexts: 	 0 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 NaN
Recall citation contexts: 	 0
fscore citation contexts: 	 NaN
```

Evaluation metrics produced in 896.987 seconds
