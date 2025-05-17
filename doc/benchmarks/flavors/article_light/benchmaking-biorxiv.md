## Header metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 84.63     | 83.44     | 84.03     | 1999    |
| first_author                | 96.7      | 95.44     | 96.07     | 1997    |
| title                       | 77.32     | 75.85     | 76.58     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.23** | **84.91** | **85.56** | 5996    |
| all fields (macro avg.)     | 86.22     | 84.91     | 85.56     | 5996    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 85.03     | 83.84     | 84.43     | 1999    |
| first_author                | 96.91     | 95.64     | 96.27     | 1997    |
| title                       | 79.41     | 77.9      | 78.65     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.13** | **85.79** | **86.45** | 5996    |
| all fields (macro avg.)     | 87.12     | 85.8      | 86.45     | 5996    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 92.24     | 90.95     | 91.59     | 1999    |
| first_author                | 97.11     | 95.84     | 96.47     | 1997    |
| title                       | 91.95     | 90.2      | 91.07     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.77** | **92.33** | **93.04** | 5996    |
| all fields (macro avg.)     | 93.76     | 92.33     | 93.04     | 5996    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.28     | 87.04     | 87.66     | 1999    |
| first_author                | 96.7      | 95.44     | 96.07     | 1997    |
| title                       | 87.56     | 85.9      | 86.72     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.85** | **89.46** | **90.15** | 5996    |
| all fields (macro avg.)     | 90.85     | 89.46     | 90.15     | 5996    |

#### Instance-level results

```
Total expected instances: 	2000
Total correct instances: 	1344 (strict) 
Total correct instances: 	1375 (soft) 
Total correct instances: 	1697 (Levenshtein) 
Total correct instances: 	1562 (ObservedRatcliffObershelp) 

Instance-level recall:	67.2	(strict) 
Instance-level recall:	68.75	(soft) 
Instance-level recall:	84.85	(Levenshtein) 
Instance-level recall:	78.1	(RatcliffObershelp) 
```

Evaluation metrics produced in 7.914 seconds
