## Header metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 98.97     | 99.28     | 99.12     | 969     |
| first_author                | 99.28     | 99.59     | 99.43     | 969     |
| title                       | 95.79     | 95.5      | 95.64     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **97.99** | **98.09** | **98.04** | 2938    |
| all fields (macro avg.)     | 98.01     | 98.12     | 98.07     | 2938    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 98.97     | 99.28     | 99.12     | 969     |
| first_author                | 99.28     | 99.59     | 99.43     | 969     |
| title                       | 99.3      | 99        | 99.15     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.18** | **99.29** | **99.23** | 2938    |
| all fields (macro avg.)     | 99.18     | 99.29     | 99.24     | 2938    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.28     | 99.59     | 99.43     | 969     |
| first_author                | 99.38     | 99.69     | 99.54     | 969     |
| title                       | 99.7      | 99.4      | 99.55     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.46** | **99.56** | **99.51** | 2938    |
| all fields (macro avg.)     | 99.45     | 99.56     | 99.51     | 2938    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.18     | 99.48     | 99.33     | 969     |
| first_author                | 99.28     | 99.59     | 99.43     | 969     |
| title                       | 99.5      | 99.2      | 99.35     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.32** | **99.42** | **99.37** | 2938    |
| all fields (macro avg.)     | 99.32     | 99.42     | 99.37     | 2938    |

#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	950 (strict) 
Total correct instances: 	985 (soft) 
Total correct instances: 	989 (Levenshtein) 
Total correct instances: 	988 (ObservedRatcliffObershelp) 

Instance-level recall:	95	(strict) 
Instance-level recall:	98.5	(soft) 
Instance-level recall:	98.9	(Levenshtein) 
Instance-level recall:	98.8	(RatcliffObershelp) 
```

Evaluation metrics produced in 12.571 seconds
