## Header metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 92.5      | 92.17     | 92.34     | 1941    |
| first_author                | 96.28     | 95.93     | 96.1      | 1941    |
| title                       | 84.28     | 83.32     | 83.8      | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.03** | **90.47** | **90.75** | 5825    |
| all fields (macro avg.)     | 91.02     | 90.47     | 90.75     | 5825    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.42     | 94.08     | 94.25     | 1941    |
| first_author                | 96.64     | 96.29     | 96.46     | 1941    |
| title                       | 91.98     | 90.94     | 91.46     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.35** | **93.77** | **94.06** | 5825    |
| all fields (macro avg.)     | 94.35     | 93.77     | 94.06     | 5825    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall   | f1       | support |
|-----------------------------|-----------|----------|----------|---------|
| authors                     | 96.54     | 96.19    | 96.36    | 1941    |
| first_author                | 96.95     | 96.6     | 96.77    | 1941    |
| title                       | 98.13     | 97.01    | 97.57    | 1943    |
|                             |           |          |          |         |
| **all fields (micro avg.)** | **97.2**  | **96.6** | **96.9** | 5825    |
| all fields (macro avg.)     | 97.2      | 96.6     | 96.9     | 5825    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 95.6      | 95.26     | 95.43     | 1941    |
| first_author                | 96.28     | 95.93     | 96.1      | 1941    |
| title                       | 96.15     | 95.06     | 95.6      | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.01** | **95.42** | **95.71** | 5825    |
| all fields (macro avg.)     | 96.01     | 95.42     | 95.71     | 5825    |

#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	1511 (strict) 
Total correct instances: 	1675 (soft) 
Total correct instances: 	1820 (Levenshtein) 
Total correct instances: 	1766 (ObservedRatcliffObershelp) 

Instance-level recall:	77.77	(strict) 
Instance-level recall:	86.21	(soft) 
Instance-level recall:	93.67	(Levenshtein) 
Instance-level recall:	90.89	(RatcliffObershelp) 
```

Evaluation metrics produced in 14.6 seconds
