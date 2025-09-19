## Header metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 98.97     | 98.97     | 98.97     | 969     |
| first_author                | 99.17     | 99.17     | 99.17     | 969     |
| title                       | 95.66     | 94.7      | 95.18     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **97.92** | **97.58** | **97.75** | 2938    |
| all fields (macro avg.)     | 97.93     | 97.61     | 97.77     | 2938    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 98.97     | 98.97     | 98.97     | 969     |
| first_author                | 99.17     | 99.17     | 99.17     | 969     |
| title                       | 99.19     | 98.2      | 98.69     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.11** | **98.77** | **98.94** | 2938    |
| all fields (macro avg.)     | 99.11     | 98.78     | 98.95     | 2938    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.38     | 99.38     | 99.38     | 969     |
| first_author                | 99.28     | 99.28     | 99.28     | 969     |
| title                       | 99.6      | 98.6      | 99.1      | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.42** | **99.08** | **99.25** | 2938    |
| all fields (macro avg.)     | 99.42     | 99.09     | 99.25     | 2938    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.28     | 99.28     | 99.28     | 969     |
| first_author                | 99.17     | 99.17     | 99.17     | 969     |
| title                       | 99.39     | 98.4      | 98.89     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.28** | **98.94** | **99.11** | 2938    |
| all fields (macro avg.)     | 99.28     | 98.95     | 99.12     | 2938    |

#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	942 (strict) 
Total correct instances: 	976 (soft) 
Total correct instances: 	980 (Levenshtein) 
Total correct instances: 	979 (ObservedRatcliffObershelp) 

Instance-level recall:	94.2	(strict) 
Instance-level recall:	97.6	(soft) 
Instance-level recall:	98	(Levenshtein) 
Instance-level recall:	97.9	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.16     | 78.35     | 79.73     | 44770   |
| date                        | 84.59     | 81.15     | 82.83     | 45457   |
| first_author                | 91.46     | 88.26     | 89.83     | 44770   |
| inTitle                     | 81.66     | 83.49     | 82.56     | 42795   |
| issue                       | 93.58     | 92.56     | 93.07     | 18983   |
| page                        | 93.68     | 77.51     | 84.83     | 40844   |
| title                       | 59.95     | 60.4      | 60.18     | 43101   |
| volume                      | 95.87     | 96.02     | 95.95     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **84.22** | **81.36** | **82.76** | 321178  |
| all fields (macro avg.)     | 85.24     | 82.22     | 83.62     | 321178  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 81.48     | 78.65     | 80.04     | 44770   |
| date                        | 84.59     | 81.15     | 82.83     | 45457   |
| first_author                | 91.68     | 88.47     | 90.04     | 44770   |
| inTitle                     | 85.49     | 87.4      | 86.43     | 42795   |
| issue                       | 93.58     | 92.56     | 93.07     | 18983   |
| page                        | 93.68     | 77.51     | 84.83     | 40844   |
| title                       | 91.96     | 92.66     | 92.31     | 43101   |
| volume                      | 95.87     | 96.02     | 95.95     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.31** | **86.28** | **87.77** | 321178  |
| all fields (macro avg.)     | 89.79     | 86.8      | 88.19     | 321178  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 90.63     | 87.49     | 89.03     | 44770   |
| date                        | 84.59     | 81.15     | 82.83     | 45457   |
| first_author                | 92.21     | 88.99     | 90.57     | 44770   |
| inTitle                     | 86.42     | 88.36     | 87.38     | 42795   |
| issue                       | 93.58     | 92.56     | 93.07     | 18983   |
| page                        | 93.68     | 77.51     | 84.83     | 40844   |
| title                       | 94.55     | 95.27     | 94.91     | 43101   |
| volume                      | 95.87     | 96.02     | 95.95     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.15** | **88.07** | **89.58** | 321178  |
| all fields (macro avg.)     | 91.44     | 88.42     | 89.82     | 321178  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 84.92     | 81.97     | 83.42     | 44770   |
| date                        | 84.59     | 81.15     | 82.83     | 45457   |
| first_author                | 91.46     | 88.26     | 89.83     | 44770   |
| inTitle                     | 85.14     | 87.05     | 86.08     | 42795   |
| issue                       | 93.58     | 92.56     | 93.07     | 18983   |
| page                        | 93.68     | 77.51     | 84.83     | 40844   |
| title                       | 93.93     | 94.65     | 94.29     | 43101   |
| volume                      | 95.87     | 96.02     | 95.95     | 40458   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.98** | **86.94** | **88.43** | 321178  |
| all fields (macro avg.)     | 90.4      | 87.39     | 88.79     | 321178  |

#### Instance-level results

```
Total expected instances: 		48449
Total extracted instances: 		48213
Total correct instances: 		13477 (strict) 
Total correct instances: 		22240 (soft) 
Total correct instances: 		24876 (Levenshtein) 
Total correct instances: 		23235 (RatcliffObershelp) 

Instance-level precision:	27.95 (strict) 
Instance-level precision:	46.13 (soft) 
Instance-level precision:	51.6 (Levenshtein) 
Instance-level precision:	48.19 (RatcliffObershelp) 

Instance-level recall:	27.82	(strict) 
Instance-level recall:	45.9	(soft) 
Instance-level recall:	51.34	(Levenshtein) 
Instance-level recall:	47.96	(RatcliffObershelp) 

Instance-level f-score:	27.88 (strict) 
Instance-level f-score:	46.02 (soft) 
Instance-level f-score:	51.47 (Levenshtein) 
Instance-level f-score:	48.07 (RatcliffObershelp) 

Matching 1 :	35332

Matching 2 :	1251

Matching 3 :	3271

Matching 4 :	1800

Total matches :	41654
```

#### Citation context resolution

```

Total expected references: 	 48449 - 48.45 references per article
Total predicted references: 	 48213 - 48.21 references per article

Total expected citation contexts: 	 69755 - 69.75 citation contexts per article
Total predicted citation contexts: 	 73412 - 73.41 citation contexts per article

Total correct predicted citation contexts: 	 56489 - 56.49 citation contexts per article
Total wrong predicted citation contexts: 	 16923 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 76.95
Recall citation contexts: 	 80.98
fscore citation contexts: 	 78.91
```

Evaluation metrics produced in 390.403 seconds
