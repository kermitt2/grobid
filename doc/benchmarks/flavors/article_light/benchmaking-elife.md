## Header metadata

Evaluation on 957 random PDF files out of 982 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 78.74     | 78.16     | 78.45     | 957     |
| first_author                | 92        | 91.42     | 91.71     | 956     |
| title                       | 89.92     | 87.67     | 88.78     | 957     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.87** | **85.75** | **86.31** | 2870    |
| all fields (macro avg.)     | 86.89     | 85.75     | 86.31     | 2870    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 79.05     | 78.47     | 78.76     | 957     |
| first_author                | 92        | 91.42     | 91.71     | 956     |
| title                       | 97        | 94.57     | 95.77     | 957     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.3**  | **88.15** | **88.73** | 2870    |
| all fields (macro avg.)     | 89.35     | 88.15     | 88.75     | 2870    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 90.53     | 89.86     | 90.19     | 957     |
| first_author                | 92.32     | 91.74     | 92.03     | 956     |
| title                       | 98.5      | 96.03     | 97.25     | 957     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.75** | **92.54** | **93.14** | 2870    |
| all fields (macro avg.)     | 93.78     | 92.54     | 93.16     | 2870    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 84.32     | 83.7      | 84.01     | 957     |
| first_author                | 92        | 91.42     | 91.71     | 956     |
| title                       | 98.5      | 96.03     | 97.25     | 957     |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.56** | **90.38** | **90.97** | 2870    |
| all fields (macro avg.)     | 91.61     | 90.38     | 90.99     | 2870    |

#### Instance-level results

```
Total expected instances: 	957
Total correct instances: 	678 (strict) 
Total correct instances: 	729 (soft) 
Total correct instances: 	811 (Levenshtein) 
Total correct instances: 	773 (ObservedRatcliffObershelp) 

Instance-level recall:	70.85	(strict) 
Instance-level recall:	76.18	(soft) 
Instance-level recall:	84.74	(Levenshtein) 
Instance-level recall:	80.77	(RatcliffObershelp) 
```

Evaluation metrics produced in 13.732 seconds
