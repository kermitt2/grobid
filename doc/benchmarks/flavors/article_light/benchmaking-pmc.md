## Header metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 92.72     | 92.53     | 92.63    | 1941    |
| first_author                | 96.49     | 96.29     | 96.39    | 1941    |
| title                       | 84.45     | 84.15     | 84.3     | 1943    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **91.22** | **90.99** | **91.1** | 5825    |
| all fields (macro avg.)     | 91.22     | 90.99     | 91.11    | 5825    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| authors                     | 94.68     | 94.49    | 94.58     | 1941    |
| first_author                | 96.9      | 96.7     | 96.8      | 1941    |
| title                       | 92.05     | 91.71    | 91.88     | 1943    |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **94.54** | **94.3** | **94.42** | 5825    |
| all fields (macro avg.)     | 94.54     | 94.3     | 94.42     | 5825    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 96.59     | 96.39     | 96.49     | 1941    |
| first_author                | 97.16     | 96.96     | 97.06     | 1941    |
| title                       | 98.24     | 97.89     | 98.07     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **97.33** | **97.08** | **97.21** | 5825    |
| all fields (macro avg.)     | 97.33     | 97.08     | 97.21     | 5825    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 95.61     | 95.41     | 95.51     | 1941    |
| first_author                | 96.49     | 96.29     | 96.39     | 1941    |
| title                       | 96.23     | 95.88     | 96.06     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.11** | **95.86** | **95.99** | 5825    |
| all fields (macro avg.)     | 96.11     | 95.86     | 95.99     | 5825    |

#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	1527 (strict) 
Total correct instances: 	1693 (soft) 
Total correct instances: 	1836 (Levenshtein) 
Total correct instances: 	1781 (ObservedRatcliffObershelp) 

Instance-level recall:	78.59	(strict) 
Instance-level recall:	87.13	(soft) 
Instance-level recall:	94.49	(Levenshtein) 
Instance-level recall:	91.66	(RatcliffObershelp) 
```

Evaluation metrics produced in 7.942 seconds
