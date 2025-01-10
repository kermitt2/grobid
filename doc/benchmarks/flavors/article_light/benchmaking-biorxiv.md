
## Header metadata 

Evaluation on 1996 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 82.99     | 81.45     | 82.22     | 1995    |
| first_author                | 96.32     | 94.63     | 95.47     | 1993    |
| title                       | 78.19     | 73.65     | 75.85     | 1996    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **85.94** | **83.24** | **84.57** | 5984    |
| all fields (macro avg.)     | 85.84     | 83.24     | 84.51     | 5984    |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.55     | 82.01     | 82.77     | 1995    |
| first_author                | 96.63     | 94.93     | 95.77     | 1993    |
| title                       | 80.64     | 75.95     | 78.22     | 1996    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.03** | **84.29** | **85.64** | 5984    |
| all fields (macro avg.)     | 86.94     | 84.3      | 85.59     | 5984    |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 91.57     | 89.87     | 90.72     | 1995    |
| first_author                | 96.78     | 95.08     | 95.93     | 1993    |
| title                       | 92.13     | 86.77     | 89.37     | 1996    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.51** | **90.57** | **92.02** | 5984    |
| all fields (macro avg.)     | 93.49     | 90.58     | 92        | 5984    |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 87.59     | 85.96     | 86.77     | 1995    |
| first_author                | 96.32     | 94.63     | 95.47     | 1993    |
| title                       | 88.35     | 83.22     | 85.71     | 1996    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.79** | **87.93** | **89.34** | 5984    |
| all fields (macro avg.)     | 90.75     | 87.94     | 89.32     | 5984    |


#### Instance-level results

```
Total expected instances: 	1996
Total correct instances: 	1280 (strict) 
Total correct instances: 	1313 (soft) 
Total correct instances: 	1615 (Levenshtein) 
Total correct instances: 	1497 (ObservedRatcliffObershelp) 

Instance-level recall:	64.13	(strict) 
Instance-level recall:	65.78	(soft) 
Instance-level recall:	80.91	(Levenshtein) 
Instance-level recall:	75	(RatcliffObershelp) 
```

Evaluation metrics produced in 13.666 seconds
