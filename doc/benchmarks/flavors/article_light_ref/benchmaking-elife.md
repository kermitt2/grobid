## Header metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 78.51     | 76.2      | 77.34     | 983     |
| first_author                | 93.19     | 90.53     | 91.84     | 982     |
| title                       | 87.86     | 84.55     | 86.17     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.51** | **83.76** | **85.11** | 2949    |
| all fields (macro avg.)     | 86.52     | 83.76     | 85.12     | 2949    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1       | support |
|-----------------------------|-----------|----------|----------|---------|
| authors                     | 78.93     | 76.6     | 77.75    | 983     |
| first_author                | 93.19     | 90.53    | 91.84    | 982     |
| title                       | 95.99     | 92.38    | 94.15    | 984     |
|                             |           |          |          |         |
| **all fields (micro avg.)** | **89.35** | **86.5** | **87.9** | 2949    |
| all fields (macro avg.)     | 89.37     | 86.5     | 87.91    | 2949    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 92.35     | 89.62     | 90.97    | 983     |
| first_author                | 93.5      | 90.84     | 92.15    | 982     |
| title                       | 97.47     | 93.8      | 95.6     | 984     |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **94.43** | **91.42** | **92.9** | 2949    |
| all fields (macro avg.)     | 94.44     | 91.42     | 92.9     | 2949    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 84.8      | 82.3      | 83.53     | 983     |
| first_author                | 93.19     | 90.53     | 91.84     | 982     |
| title                       | 97.47     | 93.8      | 95.6      | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.8**  | **88.88** | **90.32** | 2949    |
| all fields (macro avg.)     | 91.82     | 88.88     | 90.32     | 2949    |

#### Instance-level results

```
Total expected instances: 	984
Total correct instances: 	674 (strict) 
Total correct instances: 	735 (soft) 
Total correct instances: 	847 (Levenshtein) 
Total correct instances: 	793 (ObservedRatcliffObershelp) 

Instance-level recall:	68.5	(strict) 
Instance-level recall:	74.7	(soft) 
Instance-level recall:	86.08	(Levenshtein) 
Instance-level recall:	80.59	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.4      | 78.05     | 78.72     | 63265   |
| date                        | 95.87     | 93.83     | 94.84     | 63662   |
| first_author                | 94.79     | 93.14     | 93.96     | 63265   |
| inTitle                     | 95.79     | 94.55     | 95.17     | 63213   |
| issue                       | 1.98      | 75        | 3.85      | 16      |
| page                        | 96.26     | 95.09     | 95.67     | 53375   |
| title                       | 90.23     | 90.55     | 90.39     | 62044   |
| volume                      | 97.9      | 98.04     | 97.97     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.68** | **91.79** | **92.23** | 429889  |
| all fields (macro avg.)     | 81.53     | 89.78     | 81.32     | 429889  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.53     | 78.18     | 78.85     | 63265   |
| date                        | 95.87     | 93.83     | 94.84     | 63662   |
| first_author                | 94.87     | 93.22     | 94.04     | 63265   |
| inTitle                     | 96.27     | 95.03     | 95.64     | 63213   |
| issue                       | 1.98      | 75        | 3.85      | 16      |
| page                        | 96.26     | 95.09     | 95.67     | 53375   |
| title                       | 95.88     | 96.22     | 96.05     | 62044   |
| volume                      | 97.9      | 98.04     | 97.97     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.6**  | **92.71** | **93.16** | 429889  |
| all fields (macro avg.)     | 82.32     | 90.58     | 82.11     | 429889  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 93.29     | 91.71     | 92.49     | 63265   |
| date                        | 95.87     | 93.83     | 94.84     | 63662   |
| first_author                | 95.32     | 93.66     | 94.49     | 63265   |
| inTitle                     | 96.6      | 95.35     | 95.97     | 63213   |
| issue                       | 1.98      | 75        | 3.85      | 16      |
| page                        | 96.26     | 95.09     | 95.67     | 53375   |
| title                       | 97.64     | 97.98     | 97.81     | 62044   |
| volume                      | 97.9      | 98.04     | 97.97     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **95.98** | **95.07** | **95.52** | 429889  |
| all fields (macro avg.)     | 84.36     | 92.58     | 84.14     | 429889  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.7      | 85.23     | 85.96     | 63265   |
| date                        | 95.87     | 93.83     | 94.84     | 63662   |
| first_author                | 94.81     | 93.16     | 93.98     | 63265   |
| inTitle                     | 96.28     | 95.03     | 95.65     | 63213   |
| issue                       | 1.98      | 75        | 3.85      | 16      |
| page                        | 96.26     | 95.09     | 95.67     | 53375   |
| title                       | 97.49     | 97.83     | 97.66     | 62044   |
| volume                      | 97.9      | 98.04     | 97.97     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.88** | **93.97** | **94.42** | 429889  |
| all fields (macro avg.)     | 83.41     | 91.65     | 83.2      | 429889  |

#### Instance-level results

```
Total expected instances: 		63664
Total extracted instances: 		66123
Total correct instances: 		42246 (strict) 
Total correct instances: 		45078 (soft) 
Total correct instances: 		52724 (Levenshtein) 
Total correct instances: 		49325 (RatcliffObershelp) 

Instance-level precision:	63.89 (strict) 
Instance-level precision:	68.17 (soft) 
Instance-level precision:	79.74 (Levenshtein) 
Instance-level precision:	74.6 (RatcliffObershelp) 

Instance-level recall:	66.36	(strict) 
Instance-level recall:	70.81	(soft) 
Instance-level recall:	82.82	(Levenshtein) 
Instance-level recall:	77.48	(RatcliffObershelp) 

Instance-level f-score:	65.1 (strict) 
Instance-level f-score:	69.46 (soft) 
Instance-level f-score:	81.25 (Levenshtein) 
Instance-level f-score:	76.01 (RatcliffObershelp) 

Matching 1 :	58495

Matching 2 :	1012

Matching 3 :	1253

Matching 4 :	364

Total matches :	61124
```

#### Citation context resolution

```

Total expected references: 	 63664 - 64.7 references per article
Total predicted references: 	 66123 - 67.2 references per article

Total expected citation contexts: 	 109022 - 110.79 citation contexts per article
Total predicted citation contexts: 	 93259 - 94.78 citation contexts per article

Total correct predicted citation contexts: 	 90162 - 91.63 citation contexts per article
Total wrong predicted citation contexts: 	 3097 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 96.68
Recall citation contexts: 	 82.7
fscore citation contexts: 	 89.15
```

Evaluation metrics produced in 652.777 seconds
