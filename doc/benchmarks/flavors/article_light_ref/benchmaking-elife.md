
## Header metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| authors                     | 80.42     | 78.13    | 79.26     | 983     |
| first_author                | 91.62     | 89.1     | 90.35     | 982     |
| title                       | 89.24     | 85.98    | 87.58     | 984     |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **87.09** | **84.4** | **85.72** | 2949    |
| all fields (macro avg.)     | 87.09     | 84.4     | 85.73     | 2949    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 80.73     | 78.43     | 79.57     | 983     |
| first_author                | 91.62     | 89.1      | 90.35     | 982     |
| title                       | 96.1      | 92.58     | 94.31     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.47** | **86.71** | **88.07** | 2949    |
| all fields (macro avg.)     | 89.48     | 86.71     | 88.07     | 2949    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 92.77     | 90.13     | 91.43     | 983     |
| first_author                | 91.94     | 89.41     | 90.66     | 982     |
| title                       | 97.57     | 94        | 95.76     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.09** | **91.18** | **92.61** | 2949    |
| all fields (macro avg.)     | 94.1      | 91.18     | 92.62     | 2949    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 86.07     | 83.62     | 84.83    | 983     |
| first_author                | 91.62     | 89.1      | 90.35    | 982     |
| title                       | 97.57     | 94        | 95.76    | 984     |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **91.74** | **88.91** | **90.3** | 2949    |
| all fields (macro avg.)     | 91.76     | 88.91     | 90.31    | 2949    |

#### Instance-level results

```
Total expected instances: 	984
Total correct instances: 	701 (strict) 
Total correct instances: 	753 (soft) 
Total correct instances: 	840 (Levenshtein) 
Total correct instances: 	799 (ObservedRatcliffObershelp) 

Instance-level recall:	71.24	(strict) 
Instance-level recall:	76.52	(soft) 
Instance-level recall:	85.37	(Levenshtein) 
Instance-level recall:	81.2	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.41     | 78.03     | 78.71     | 63265   |
| date                        | 95.87     | 93.79     | 94.82     | 63662   |
| first_author                | 94.8      | 93.12     | 93.95     | 63265   |
| inTitle                     | 95.79     | 94.52     | 95.15     | 63213   |
| issue                       | 1.98      | 75        | 3.85      | 16      |
| page                        | 96.25     | 95.04     | 95.65     | 53375   |
| title                       | 90.25     | 90.53     | 90.39     | 62044   |
| volume                      | 97.89     | 97.99     | 97.94     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.68** | **91.76** | **92.22** | 429889  |
| all fields (macro avg.)     | 81.53     | 89.75     | 81.31     | 429889  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.54     | 78.17     | 78.85     | 63265   |
| date                        | 95.87     | 93.79     | 94.82     | 63662   |
| first_author                | 94.88     | 93.2      | 94.03     | 63265   |
| inTitle                     | 96.27     | 94.99     | 95.63     | 63213   |
| issue                       | 1.98      | 75        | 3.85      | 16      |
| page                        | 96.25     | 95.04     | 95.65     | 53375   |
| title                       | 95.9      | 96.2      | 96.05     | 62044   |
| volume                      | 97.89     | 97.99     | 97.94     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.61** | **92.68** | **93.14** | 429889  |
| all fields (macro avg.)     | 82.32     | 90.55     | 82.1      | 429889  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 93.29     | 91.68     | 92.48    | 63265   |
| date                        | 95.87     | 93.79     | 94.82    | 63662   |
| first_author                | 95.33     | 93.64     | 94.48    | 63265   |
| inTitle                     | 96.59     | 95.31     | 95.95    | 63213   |
| issue                       | 1.98      | 75        | 3.85     | 16      |
| page                        | 96.25     | 95.04     | 95.65    | 53375   |
| title                       | 97.63     | 97.94     | 97.78    | 62044   |
| volume                      | 97.89     | 97.99     | 97.94    | 61049   |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **95.98** | **95.03** | **95.5** | 429889  |
| all fields (macro avg.)     | 84.36     | 92.55     | 84.12    | 429889  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.72     | 85.22     | 85.96     | 63265   |
| date                        | 95.87     | 93.79     | 94.82     | 63662   |
| first_author                | 94.82     | 93.13     | 93.97     | 63265   |
| inTitle                     | 96.27     | 95        | 95.63     | 63213   |
| issue                       | 1.98      | 75        | 3.85      | 16      |
| page                        | 96.25     | 95.04     | 95.65     | 53375   |
| title                       | 97.49     | 97.79     | 97.64     | 62044   |
| volume                      | 97.89     | 97.99     | 97.94     | 61049   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.88** | **93.94** | **94.41** | 429889  |
| all fields (macro avg.)     | 83.41     | 91.62     | 83.18     | 429889  |

#### Instance-level results

```
Total expected instances: 		63664
Total extracted instances: 		66093
Total correct instances: 		42236 (strict) 
Total correct instances: 		45064 (soft) 
Total correct instances: 		52700 (Levenshtein) 
Total correct instances: 		49316 (RatcliffObershelp) 

Instance-level precision:	63.9 (strict) 
Instance-level precision:	68.18 (soft) 
Instance-level precision:	79.74 (Levenshtein) 
Instance-level precision:	74.62 (RatcliffObershelp) 

Instance-level recall:	66.34	(strict) 
Instance-level recall:	70.78	(soft) 
Instance-level recall:	82.78	(Levenshtein) 
Instance-level recall:	77.46	(RatcliffObershelp) 

Instance-level f-score:	65.1 (strict) 
Instance-level f-score:	69.46 (soft) 
Instance-level f-score:	81.23 (Levenshtein) 
Instance-level f-score:	76.01 (RatcliffObershelp) 

Matching 1 :	58481

Matching 2 :	1007

Matching 3 :	1248

Matching 4 :	365

Total matches :	61101
```

#### Citation context resolution

```

Total expected references: 	 63664 - 64.7 references per article
Total predicted references: 	 66093 - 67.17 references per article

Total expected citation contexts: 	 109022 - 110.79 citation contexts per article
Total predicted citation contexts: 	 0 - 0 citation contexts per article

Total correct predicted citation contexts: 	 0 - 0 citation contexts per article
Total wrong predicted citation contexts: 	 0 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 NaN
Recall citation contexts: 	 0
fscore citation contexts: 	 NaN
```

Evaluation metrics produced in 1500.398 seconds
