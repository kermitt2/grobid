## Header metadata

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 98.87     | 98.97     | 98.92     | 969     |
| first_author                | 99.18     | 99.28     | 99.23     | 969     |
| title                       | 95.63     | 94.2      | 94.91     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **97.88** | **97.45** | **97.66** | 2938    |
| all fields (macro avg.)     | 97.89     | 97.48     | 97.69     | 2938    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 98.87     | 98.97     | 98.92     | 969     |
| first_author                | 99.18     | 99.28     | 99.23     | 969     |
| title                       | 99.19     | 97.7      | 98.44     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.08** | **98.64** | **98.86** | 2938    |
| all fields (macro avg.)     | 99.08     | 98.65     | 98.86     | 2938    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.28     | 99.38     | 99.33     | 969     |
| first_author                | 99.28     | 99.38     | 99.33     | 969     |
| title                       | 99.59     | 98.1      | 98.84     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.38** | **98.94** | **99.16** | 2938    |
| all fields (macro avg.)     | 99.38     | 98.95     | 99.17     | 2938    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.18     | 99.28     | 99.23     | 969     |
| first_author                | 99.18     | 99.28     | 99.23     | 969     |
| title                       | 99.39     | 97.9      | 98.64     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.25** | **98.81** | **99.03** | 2938    |
| all fields (macro avg.)     | 99.25     | 98.82     | 99.03     | 2938    |

#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	937 (strict) 
Total correct instances: 	971 (soft) 
Total correct instances: 	975 (Levenshtein) 
Total correct instances: 	974 (ObservedRatcliffObershelp) 

Instance-level recall:	93.7	(strict) 
Instance-level recall:	97.1	(soft) 
Instance-level recall:	97.5	(Levenshtein) 
Instance-level recall:	97.4	(RatcliffObershelp) 
```

Evaluation metrics produced in 5.184 seconds
