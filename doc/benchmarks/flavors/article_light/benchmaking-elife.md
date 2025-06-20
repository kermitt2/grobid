## Header metadata

Evaluation on 984 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.75     | 77.72     | 78.72     | 983     |
| first_author                | 93.84     | 91.55     | 92.68     | 982     |
| title                       | 88.36     | 84.86     | 86.57     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.31** | **84.71** | **85.99** | 2949    |
| all fields (macro avg.)     | 87.32     | 84.71     | 85.99     | 2949    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 80.17     | 78.13     | 79.13     | 983     |
| first_author                | 93.84     | 91.55     | 92.68     | 982     |
| title                       | 96.4      | 92.58     | 94.45     | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.11** | **87.42** | **88.74** | 2949    |
| all fields (macro avg.)     | 90.14     | 87.42     | 88.76     | 2949    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall   | f1       | support |
|-----------------------------|-----------|----------|----------|---------|
| authors                     | 93.63     | 91.25    | 92.43    | 983     |
| first_author                | 94.15     | 91.85    | 92.99    | 982     |
| title                       | 97.99     | 94.11    | 96.01    | 984     |
|                             |           |          |          |         |
| **all fields (micro avg.)** | **95.25** | **92.4** | **93.8** | 2949    |
| all fields (macro avg.)     | 95.26     | 92.4     | 93.81    | 2949    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 86.22     | 84.03     | 85.11     | 983     |
| first_author                | 93.84     | 91.55     | 92.68     | 982     |
| title                       | 97.88     | 94        | 95.9      | 984     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.62** | **89.86** | **91.22** | 2949    |
| all fields (macro avg.)     | 92.65     | 89.86     | 91.23     | 2949    |

#### Instance-level results

```
Total expected instances: 	984
Total correct instances: 	681 (strict) 
Total correct instances: 	742 (soft) 
Total correct instances: 	853 (Levenshtein) 
Total correct instances: 	801 (ObservedRatcliffObershelp) 

Instance-level recall:	69.21	(strict) 
Instance-level recall:	75.41	(soft) 
Instance-level recall:	86.69	(Levenshtein) 
Instance-level recall:	81.4	(RatcliffObershelp) 
```

Evaluation metrics produced in 6.061 seconds
