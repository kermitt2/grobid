
## Header metadata 

Evaluation on 1000 random PDF files out of 998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 98.97     | 99.07     | 99.02    | 969     |
| first_author                | 99.28     | 99.38     | 99.33    | 969     |
| title                       | 95.77     | 95.1      | 95.43    | 1000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **97.99** | **97.82** | **97.9** | 2938    |
| all fields (macro avg.)     | 98.01     | 97.85     | 97.93    | 2938    |



#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 98.97     | 99.07     | 99.02    | 969     |
| first_author                | 99.28     | 99.38     | 99.33    | 969     |
| title                       | 99.3      | 98.6      | 98.95    | 1000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **99.18** | **99.01** | **99.1** | 2938    |
| all fields (macro avg.)     | 99.18     | 99.02     | 99.1     | 2938    |



#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1         | support |
|-----------------------------|-----------|-----------|------------|---------|
| authors                     | 99.28     | 99.38     | 99.33      | 969     |
| first_author                | 99.38     | 99.48     | 99.43      | 969     |
| title                       | 99.7      | 99        | 99.35      | 1000    |
|                             |           |           |            |         |
| **all fields (micro avg.)** | **99.45** | **99.29** | **99.37**  | 2938    |
| all fields (macro avg.)     | 99.45     | 99.29     | 99.37      | 2938    |



#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 99.18     | 99.28     | 99.23     | 969     |
| first_author                | 99.28     | 99.38     | 99.33     | 969     |
| title                       | 99.5      | 98.8      | 99.15     | 1000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **99.32** | **99.15** | **99.23** | 2938    |
| all fields (macro avg.)     | 99.32     | 99.15     | 99.23     | 2938    |


#### Instance-level results

```
Total expected instances: 	1000
Total correct instances: 	946 (strict) 
Total correct instances: 	981 (soft) 
Total correct instances: 	985 (Levenshtein) 
Total correct instances: 	984 (ObservedRatcliffObershelp) 

Instance-level recall:	94.6	(strict) 
Instance-level recall:	98.1	(soft) 
Instance-level recall:	98.5	(Levenshtein) 
Instance-level recall:	98.4	(RatcliffObershelp) 
```

Evaluation metrics produced in 12.472 seconds
