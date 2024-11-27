## Header metadata

Evaluation on 1996 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 82.92     | 81.5      | 82.2      | 1995    |
| first_author                | 96.33     | 94.78     | 95.55     | 1993    |
| title                       | 78.16     | 73.7      | 75.86     | 1996    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **85.91** | **83.32** | **84.59** | 5984    |
| all fields (macro avg.)     | 85.8      | 83.33     | 84.54     | 5984    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 83.53     | 82.11     | 82.81    | 1995    |
| first_author                | 96.63     | 95.08     | 95.85    | 1993    |
| title                       | 80.66     | 76.05     | 78.29    | 1996    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **87.03** | **84.41** | **85.7** | 5984    |
| all fields (macro avg.)     | 86.94     | 84.41     | 85.65    | 5984    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 91.59     | 90.03     | 90.8      | 1995    |
| first_author                | 96.84     | 95.28     | 96.05     | 1993    |
| title                       | 92.03     | 86.77     | 89.32     | 1996    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.5**  | **90.69** | **92.08** | 5984    |
| all fields (macro avg.)     | 93.48     | 90.69     | 92.06     | 5984    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 87.51     | 86.02     | 86.75    | 1995    |
| first_author                | 96.33     | 94.78     | 95.55    | 1993    |
| title                       | 88.42     | 83.37     | 85.82    | 1996    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **90.78** | **88.05** | **89.4** | 5984    |
| all fields (macro avg.)     | 90.75     | 88.05     | 89.37    | 5984    |

#### Instance-level results

```
Total expected instances: 	1996
Total correct instances: 	1278 (strict) 
Total correct instances: 	1312 (soft) 
Total correct instances: 	1613 (Levenshtein) 
Total correct instances: 	1496 (ObservedRatcliffObershelp) 

Instance-level recall:	64.03	(strict) 
Instance-level recall:	65.73	(soft) 
Instance-level recall:	80.81	(Levenshtein) 
Instance-level recall:	74.95	(RatcliffObershelp) 
```

Evaluation metrics produced in 15.364 seconds
