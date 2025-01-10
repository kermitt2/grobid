
## Header metadata 

Evaluation on 957 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.68     | 77.43     | 78.54     | 957     |
| first_author                | 91.83     | 89.33     | 90.56     | 956     |
| title                       | 89.25     | 85.89     | 87.54     | 957     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.91** | **84.22** | **85.54** | 2870    |
| all fields (macro avg.)     | 86.92     | 84.22     | 85.55     | 2870    |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 80        | 77.74     | 78.86     | 957     |
| first_author                | 91.83     | 89.33     | 90.56     | 956     |
| title                       | 96.42     | 92.79     | 94.57     | 957     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.39** | **86.62** | **87.98** | 2870    |
| all fields (macro avg.)     | 89.41     | 86.62     | 88        | 2870    |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 92.8      | 90.18     | 91.47     | 957     |
| first_author                | 92.26     | 89.75     | 90.99     | 956     |
| title                       | 98.05     | 94.36     | 96.17     | 957     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.35** | **91.43** | **92.87** | 2870    |
| all fields (macro avg.)     | 94.37     | 91.43     | 92.87     | 2870    |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 85.59     | 83.18     | 84.37     | 957     |
| first_author                | 91.83     | 89.33     | 90.56     | 956     |
| title                       | 97.94     | 94.25     | 96.06     | 957     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.77** | **88.92** | **90.32** | 2870    |
| all fields (macro avg.)     | 91.79     | 88.92     | 90.33     | 2870    |


#### Instance-level results

```
Total expected instances: 	957
Total correct instances: 	672 (strict) 
Total correct instances: 	723 (soft) 
Total correct instances: 	812 (Levenshtein) 
Total correct instances: 	768 (ObservedRatcliffObershelp) 

Instance-level recall:	70.22	(strict) 
Instance-level recall:	75.55	(soft) 
Instance-level recall:	84.85	(Levenshtein) 
Instance-level recall:	80.25	(RatcliffObershelp) 
```

Evaluation metrics produced in 12.061 seconds
