
## Header metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 92.14     | 91.86     | 92        | 1941    |
| first_author                | 96.33     | 96.03     | 96.18     | 1941    |
| title                       | 84.4      | 83.53     | 83.96     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.97** | **90.47** | **90.72** | 5825    |
| all fields (macro avg.)     | 90.96     | 90.47     | 90.72     | 5825    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.06     | 93.77     | 93.91     | 1941    |
| first_author                | 96.69     | 96.39     | 96.54     | 1941    |
| title                       | 92.1      | 91.15     | 91.62     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.29** | **93.77** | **94.03** | 5825    |
| all fields (macro avg.)     | 94.28     | 93.77     | 94.02     | 5825    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 96.38     | 96.08     | 96.23     | 1941    |
| first_author                | 97        | 96.7      | 96.85     | 1941    |
| title                       | 98.23     | 97.22     | 97.72     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **97.2**  | **96.67** | **96.94** | 5825    |
| all fields (macro avg.)     | 97.21     | 96.67     | 96.94     | 5825    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 95.25     | 94.95     | 95.1      | 1941    |
| first_author                | 96.33     | 96.03     | 96.18     | 1941    |
| title                       | 96.26     | 95.27     | 95.76     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **95.94** | **95.42** | **95.68** | 5825    |
| all fields (macro avg.)     | 95.94     | 95.42     | 95.68     | 5825    |

#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	1509 (strict) 
Total correct instances: 	1672 (soft) 
Total correct instances: 	1820 (Levenshtein) 
Total correct instances: 	1763 (ObservedRatcliffObershelp) 

Instance-level recall:	77.66	(strict) 
Instance-level recall:	86.05	(soft) 
Instance-level recall:	93.67	(Levenshtein) 
Instance-level recall:	90.74	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 82.92     | 75.64     | 79.11     | 85778   |
| date                        | 94.33     | 83.47     | 88.57     | 87067   |
| first_author                | 89.64     | 81.75     | 85.51     | 85778   |
| inTitle                     | 72.88     | 71.09     | 71.98     | 81007   |
| issue                       | 89.96     | 87.44     | 88.68     | 16635   |
| page                        | 94.06     | 82.82     | 88.08     | 80501   |
| title                       | 79.47     | 74.58     | 76.95     | 80736   |
| volume                      | 95.71     | 88.98     | 92.22     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.93** | **79.98** | **83.31** | 597569  |
| all fields (macro avg.)     | 87.37     | 80.72     | 83.89     | 597569  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.39     | 76.08     | 79.56     | 85778   |
| date                        | 94.33     | 83.47     | 88.57     | 87067   |
| first_author                | 89.81     | 81.91     | 85.68     | 85778   |
| inTitle                     | 84.61     | 82.54     | 83.56     | 81007   |
| issue                       | 89.96     | 87.44     | 88.68     | 16635   |
| page                        | 94.06     | 82.82     | 88.08     | 80501   |
| title                       | 91.23     | 85.62     | 88.34     | 80736   |
| volume                      | 95.71     | 88.98     | 92.22     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.33** | **83.11** | **86.57** | 597569  |
| all fields (macro avg.)     | 90.39     | 83.61     | 86.84     | 597569  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.04     | 81.23     | 84.96     | 85778   |
| date                        | 94.33     | 83.47     | 88.57     | 87067   |
| first_author                | 90.01     | 82.08     | 85.86     | 85778   |
| inTitle                     | 85.86     | 83.75     | 84.79     | 81007   |
| issue                       | 89.96     | 87.44     | 88.68     | 16635   |
| page                        | 94.06     | 82.82     | 88.08     | 80501   |
| title                       | 93.55     | 87.8      | 90.58     | 80736   |
| volume                      | 95.71     | 88.98     | 92.22     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.66** | **84.33** | **87.84** | 597569  |
| all fields (macro avg.)     | 91.56     | 84.7      | 87.97     | 597569  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 85.85     | 78.32     | 81.91     | 85778   |
| date                        | 94.33     | 83.47     | 88.57     | 87067   |
| first_author                | 89.66     | 81.77     | 85.53     | 85778   |
| inTitle                     | 83.2      | 81.15     | 82.16     | 81007   |
| issue                       | 89.96     | 87.44     | 88.68     | 16635   |
| page                        | 94.06     | 82.82     | 88.08     | 80501   |
| title                       | 93.15     | 87.42     | 90.2      | 80736   |
| volume                      | 95.71     | 88.98     | 92.22     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.72** | **83.47** | **86.94** | 597569  |
| all fields (macro avg.)     | 90.74     | 83.92     | 87.17     | 597569  |

#### Instance-level results

```
Total expected instances: 		90125
Total extracted instances: 		85408
Total correct instances: 		38366 (strict) 
Total correct instances: 		50411 (soft) 
Total correct instances: 		55199 (Levenshtein) 
Total correct instances: 		51802 (RatcliffObershelp) 

Instance-level precision:	44.92 (strict) 
Instance-level precision:	59.02 (soft) 
Instance-level precision:	64.63 (Levenshtein) 
Instance-level precision:	60.65 (RatcliffObershelp) 

Instance-level recall:	42.57	(strict) 
Instance-level recall:	55.93	(soft) 
Instance-level recall:	61.25	(Levenshtein) 
Instance-level recall:	57.48	(RatcliffObershelp) 

Instance-level f-score:	43.71 (strict) 
Instance-level f-score:	57.44 (soft) 
Instance-level f-score:	62.89 (Levenshtein) 
Instance-level f-score:	59.02 (RatcliffObershelp) 

Matching 1 :	67678

Matching 2 :	4126

Matching 3 :	1857

Matching 4 :	668

Total matches :	74329
```

#### Citation context resolution

```

Total expected references: 	 90125 - 46.38 references per article
Total predicted references: 	 85408 - 43.96 references per article

Total expected citation contexts: 	 139835 - 71.97 citation contexts per article
Total predicted citation contexts: 	 0 - 0 citation contexts per article

Total correct predicted citation contexts: 	 0 - 0 citation contexts per article
Total wrong predicted citation contexts: 	 0 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 NaN
Recall citation contexts: 	 0
fscore citation contexts: 	 NaN
```

Evaluation metrics produced in 1506.41 seconds
