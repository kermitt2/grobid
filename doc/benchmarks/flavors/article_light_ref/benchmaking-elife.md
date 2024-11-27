## Header metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 80.86     | 79.96     | 80.41     | 983     |
| first_author                | 91.77     | 90.84     | 91.3      | 982     |
| title                       | 89.68     | 87.4      | 88.52     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.43** | **86.06** | **86.74** | 2949    |
| all fields (macro avg.)     | 87.44     | 86.06     | 86.74     | 2949    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1       | support |
|-----------------------------|-----------|----------|----------|---------|
| authors                     | 81.17     | 80.26    | 80.72    | 983     |
| first_author                | 91.77     | 90.84    | 91.3     | 982     |
| title                       | 96.56     | 94.11    | 95.32    | 984     |
|                             |           |          |          |         |
| **all fields (micro avg.)** | **89.8**  | **88.4** | **89.1** | 2949    |
| all fields (macro avg.)     | 89.83     | 88.4     | 89.11    | 2949    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 93.11     | 92.07     | 92.58     | 983     |
| first_author                | 92.08     | 91.14     | 91.61     | 982     |
| title                       | 98.02     | 95.53     | 96.76     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.39** | **92.91** | **93.64** | 2949    |
| all fields (macro avg.)     | 94.4      | 92.91     | 93.65     | 2949    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.52     | 85.55     | 86.04     | 983     |
| first_author                | 91.77     | 90.84     | 91.3      | 982     |
| title                       | 98.02     | 95.53     | 96.76     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.08** | **90.64** | **91.35** | 2949    |
| all fields (macro avg.)     | 92.1      | 90.64     | 91.36     | 2949    |

#### Instance-level results

```
Total expected instances: 	984
Total correct instances: 	713 (strict) 
Total correct instances: 	766 (soft) 
Total correct instances: 	854 (Levenshtein) 
Total correct instances: 	814 (ObservedRatcliffObershelp) 

Instance-level recall:	72.46	(strict) 
Instance-level recall:	77.85	(soft) 
Instance-level recall:	86.79	(Levenshtein) 
Instance-level recall:	82.72	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.44     | 78.36     | 78.9      | 63265   |
| date                        | 95.93     | 94.2      | 95.05     | 63662   |
| first_author                | 94.84     | 93.5      | 94.17     | 63265   |
| inTitle                     | 95.81     | 94.89     | 95.35     | 63213   |
| issue                       | 1.98      | 75        | 3.86      | 16      |
| page                        | 96.25     | 95.43     | 95.84     | 53375   |
| title                       | 90.28     | 90.91     | 90.6      | 62044   |
| volume                      | 97.91     | 98.4      | 98.15     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.71** | **92.14** | **92.42** | 429889  |
| all fields (macro avg.)     | 81.56     | 90.09     | 81.49     | 429889  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.58     | 78.49     | 79.03     | 63265   |
| date                        | 95.93     | 94.2      | 95.05     | 63662   |
| first_author                | 94.92     | 93.58     | 94.25     | 63265   |
| inTitle                     | 96.3      | 95.37     | 95.83     | 63213   |
| issue                       | 1.98      | 75        | 3.86      | 16      |
| page                        | 96.25     | 95.43     | 95.84     | 53375   |
| title                       | 95.95     | 96.62     | 96.28     | 62044   |
| volume                      | 97.91     | 98.4      | 98.15     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.64** | **93.07** | **93.35** | 429889  |
| all fields (macro avg.)     | 82.35     | 90.89     | 82.29     | 429889  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 93.32     | 92.05     | 92.68     | 63265   |
| date                        | 95.93     | 94.2      | 95.05     | 63662   |
| first_author                | 95.37     | 94.03     | 94.69     | 63265   |
| inTitle                     | 96.62     | 95.7      | 96.16     | 63213   |
| issue                       | 1.98      | 75        | 3.86      | 16      |
| page                        | 96.25     | 95.43     | 95.84     | 53375   |
| title                       | 97.66     | 98.34     | 98        | 62044   |
| volume                      | 97.91     | 98.4      | 98.15     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.01** | **95.42** | **95.72** | 429889  |
| all fields (macro avg.)     | 84.38     | 92.89     | 84.3      | 429889  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.75     | 85.57     | 86.16     | 63265   |
| date                        | 95.93     | 94.2      | 95.05     | 63662   |
| first_author                | 94.85     | 93.52     | 94.18     | 63265   |
| inTitle                     | 96.3      | 95.38     | 95.84     | 63213   |
| issue                       | 1.98      | 75        | 3.86      | 16      |
| page                        | 96.25     | 95.43     | 95.84     | 53375   |
| title                       | 97.51     | 98.19     | 97.85     | 62044   |
| volume                      | 97.91     | 98.4      | 98.15     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.91** | **94.33** | **94.62** | 429889  |
| all fields (macro avg.)     | 83.44     | 91.96     | 83.37     | 429889  |

#### Instance-level results

```
Total expected instances: 		63664
Total extracted instances: 		66390
Total correct instances: 		42407 (strict) 
Total correct instances: 		45251 (soft) 
Total correct instances: 		52911 (Levenshtein) 
Total correct instances: 		49510 (RatcliffObershelp) 

Instance-level precision:	63.88 (strict) 
Instance-level precision:	68.16 (soft) 
Instance-level precision:	79.7 (Levenshtein) 
Instance-level precision:	74.57 (RatcliffObershelp) 

Instance-level recall:	66.61	(strict) 
Instance-level recall:	71.08	(soft) 
Instance-level recall:	83.11	(Levenshtein) 
Instance-level recall:	77.77	(RatcliffObershelp) 

Instance-level f-score:	65.21 (strict) 
Instance-level f-score:	69.59 (soft) 
Instance-level f-score:	81.37 (Levenshtein) 
Instance-level f-score:	76.14 (RatcliffObershelp) 

Matching 1 :	58739

Matching 2 :	1008

Matching 3 :	1244

Matching 4 :	366

Total matches :	61357
```

#### Citation context resolution

```

Total expected references: 	 63664 - 64.7 references per article
Total predicted references: 	 66390 - 67.47 references per article

Total expected citation contexts: 	 109022 - 110.79 citation contexts per article
Total predicted citation contexts: 	 0 - 0 citation contexts per article

Total correct predicted citation contexts: 	 0 - 0 citation contexts per article
Total wrong predicted citation contexts: 	 0 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 NaN
Recall citation contexts: 	 0
fscore citation contexts: 	 NaN
```

Evaluation metrics produced in 1541.928 seconds
