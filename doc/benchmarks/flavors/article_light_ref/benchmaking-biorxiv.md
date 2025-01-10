
## Header metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1       | support |
|-----------------------------|-----------|-----------|----------|---------|
| authors                     | 83.38     | 81.79     | 82.58    | 1999    |
| first_author                | 96.58     | 94.84     | 95.7     | 1997    |
| title                       | 78.19     | 73.85     | 75.96    | 2000    |
|                             |           |           |          |         |
| **all fields (micro avg.)** | **86.15** | **83.49** | **84.8** | 5996    |
| all fields (macro avg.)     | 86.05     | 83.49     | 84.75    | 5996    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.94     | 82.34     | 83.13     | 1999    |
| first_author                | 96.89     | 95.14     | 96.01     | 1997    |
| title                       | 80.57     | 76.1      | 78.27     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.21** | **84.52** | **85.85** | 5996    |
| all fields (macro avg.)     | 87.13     | 84.53     | 85.8      | 5996    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 92.04     | 90.3      | 91.16     | 1999    |
| first_author                | 97.04     | 95.29     | 96.16     | 1997    |
| title                       | 92.11     | 87        | 89.48     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.75** | **90.86** | **92.28** | 5996    |
| all fields (macro avg.)     | 93.73     | 90.86     | 92.27     | 5996    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.02     | 86.34     | 87.17     | 1999    |
| first_author                | 96.58     | 94.84     | 95.7      | 1997    |
| title                       | 88.35     | 83.45     | 85.83     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.02** | **88.21** | **89.59** | 5996    |
| all fields (macro avg.)     | 90.98     | 88.21     | 89.57     | 5996    |

#### Instance-level results

```
Total expected instances: 	2000
Total correct instances: 	1286 (strict) 
Total correct instances: 	1320 (soft) 
Total correct instances: 	1627 (Levenshtein) 
Total correct instances: 	1507 (ObservedRatcliffObershelp) 

Instance-level recall:	64.3	(strict) 
Instance-level recall:	66	(soft) 
Instance-level recall:	81.35	(Levenshtein) 
Instance-level recall:	75.35	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.07     | 82.47     | 85.18     | 97183   |
| date                        | 91.66     | 85.55     | 88.5      | 97630   |
| doi                         | 70.88     | 82.18     | 76.11     | 16894   |
| first_author                | 94.98     | 88.85     | 91.82     | 97183   |
| inTitle                     | 82.76     | 78.67     | 80.66     | 96430   |
| issue                       | 94.32     | 91.01     | 92.64     | 30312   |
| page                        | 94.93     | 77.66     | 85.43     | 88597   |
| pmcid                       | 65.91     | 82.65     | 73.34     | 807     |
| pmid                        | 69.03     | 81.46     | 74.73     | 2093    |
| title                       | 84.81     | 82.78     | 83.78     | 92463   |
| volume                      | 96.19     | 94.39     | 95.28     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.79** | **84.52** | **87.08** | 707301  |
| all fields (macro avg.)     | 84.87     | 84.33     | 84.31     | 707301  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.23     | 83.55     | 86.29     | 97183   |
| date                        | 91.66     | 85.55     | 88.5      | 97630   |
| doi                         | 75.35     | 87.37     | 80.92     | 16894   |
| first_author                | 95.41     | 89.26     | 92.23     | 97183   |
| inTitle                     | 92.29     | 87.73     | 89.95     | 96430   |
| issue                       | 94.32     | 91.01     | 92.64     | 30312   |
| page                        | 94.93     | 77.66     | 85.43     | 88597   |
| pmcid                       | 75.3      | 94.42     | 83.78     | 807     |
| pmid                        | 73.56     | 86.81     | 79.64     | 2093    |
| title                       | 93.15     | 90.92     | 92.02     | 92463   |
| volume                      | 96.19     | 94.39     | 95.28     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.62** | **87.18** | **89.82** | 707301  |
| all fields (macro avg.)     | 88.31     | 88.06     | 87.88     | 707301  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.48     | 88.47     | 91.38     | 97183   |
| date                        | 91.66     | 85.55     | 88.5      | 97630   |
| doi                         | 77.54     | 89.91     | 83.27     | 16894   |
| first_author                | 95.56     | 89.39     | 92.37     | 97183   |
| inTitle                     | 93.24     | 88.64     | 90.88     | 96430   |
| issue                       | 94.32     | 91.01     | 92.64     | 30312   |
| page                        | 94.93     | 77.66     | 85.43     | 88597   |
| pmcid                       | 75.3      | 94.42     | 83.78     | 807     |
| pmid                        | 73.56     | 86.81     | 79.64     | 2093    |
| title                       | 95.97     | 93.67     | 94.81     | 92463   |
| volume                      | 96.19     | 94.39     | 95.28     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.93** | **88.42** | **91.09** | 707301  |
| all fields (macro avg.)     | 89.34     | 89.08     | 88.91     | 707301  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| authors                     | 91.44     | 85.62    | 88.43     | 97183   |
| date                        | 91.66     | 85.55    | 88.5      | 97630   |
| doi                         | 76.04     | 88.17    | 81.66     | 16894   |
| first_author                | 95.03     | 88.9     | 91.86     | 97183   |
| inTitle                     | 90.97     | 86.48    | 88.67     | 96430   |
| issue                       | 94.32     | 91.01    | 92.64     | 30312   |
| page                        | 94.93     | 77.66    | 85.43     | 88597   |
| pmcid                       | 65.91     | 82.65    | 73.34     | 807     |
| pmid                        | 69.03     | 81.46    | 74.73     | 2093    |
| title                       | 95.24     | 92.96    | 94.09     | 92463   |
| volume                      | 96.19     | 94.39    | 95.28     | 87709   |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **92.96** | **87.5** | **90.15** | 707301  |
| all fields (macro avg.)     | 87.34     | 86.8     | 86.78     | 707301  |

#### Instance-level results

```
Total expected instances: 		98799
Total extracted instances: 		97436
Total correct instances: 		43388 (strict) 
Total correct instances: 		54260 (soft) 
Total correct instances: 		58398 (Levenshtein) 
Total correct instances: 		55164 (RatcliffObershelp) 

Instance-level precision:	44.53 (strict) 
Instance-level precision:	55.69 (soft) 
Instance-level precision:	59.93 (Levenshtein) 
Instance-level precision:	56.62 (RatcliffObershelp) 

Instance-level recall:	43.92	(strict) 
Instance-level recall:	54.92	(soft) 
Instance-level recall:	59.11	(Levenshtein) 
Instance-level recall:	55.83	(RatcliffObershelp) 

Instance-level f-score:	44.22 (strict) 
Instance-level f-score:	55.3 (soft) 
Instance-level f-score:	59.52 (Levenshtein) 
Instance-level f-score:	56.22 (RatcliffObershelp) 

Matching 1 :	78553

Matching 2 :	4422

Matching 3 :	4351

Matching 4 :	2084

Total matches :	89410
```

#### Citation context resolution

```

Total expected references: 	 98797 - 49.4 references per article
Total predicted references: 	 97436 - 48.72 references per article

Total expected citation contexts: 	 142862 - 71.43 citation contexts per article
Total predicted citation contexts: 	 0 - 0 citation contexts per article

Total correct predicted citation contexts: 	 0 - 0 citation contexts per article
Total wrong predicted citation contexts: 	 0 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 NaN
Recall citation contexts: 	 0
fscore citation contexts: 	 NaN
```

Evaluation metrics produced in 1779.407 seconds
