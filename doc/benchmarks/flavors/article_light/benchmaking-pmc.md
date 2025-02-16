
## Header metadata 

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 92.2      | 91.91     | 92.05     | 1941    |
| first_author                | 96.28     | 95.98     | 96.13     | 1941    |
| title                       | 84.33     | 83.38     | 83.85     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.95** | **90.42** | **90.69** | 5825    |
| all fields (macro avg.)     | 90.94     | 90.42     | 90.68     | 5825    |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.11     | 93.82     | 93.96     | 1941    |
| first_author                | 96.64     | 96.34     | 96.49     | 1941    |
| title                       | 92.04     | 90.99     | 91.51     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.27** | **93.72** | **93.99** | 5825    |
| all fields (macro avg.)     | 94.26     | 93.72     | 93.99     | 5825    |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 96.28     | 95.98     | 96.13     | 1941    |
| first_author                | 96.95     | 96.65     | 96.8      | 1941    |
| title                       | 98.18     | 97.07     | 97.62     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **97.13** | **96.57** | **96.85** | 5825    |
| all fields (macro avg.)     | 97.14     | 96.57     | 96.85     | 5825    |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 95.3      | 95        | 95.15     | 1941    |
| first_author                | 96.28     | 95.98     | 96.13     | 1941    |
| title                       | 96.2      | 95.11     | 95.65     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **95.92** | **95.36** | **95.64** | 5825    |
| all fields (macro avg.)     | 95.93     | 95.36     | 95.64     | 5825    |


#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	1506 (strict) 
Total correct instances: 	1669 (soft) 
Total correct instances: 	1816 (Levenshtein) 
Total correct instances: 	1760 (ObservedRatcliffObershelp) 

Instance-level recall:	77.51	(strict) 
Instance-level recall:	85.9	(soft) 
Instance-level recall:	93.46	(Levenshtein) 
Instance-level recall:	90.58	(RatcliffObershelp) 
```

Evaluation metrics produced in 15.301 seconds
