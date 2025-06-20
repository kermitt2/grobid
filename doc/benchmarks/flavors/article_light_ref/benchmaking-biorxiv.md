## Header metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 84.93     | 83.74     | 84.33     | 1999    |
| first_author                | 96.91     | 95.64     | 96.27     | 1997    |
| title                       | 77.48     | 76.2      | 76.83     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.44** | **85.19** | **85.81** | 5996    |
| all fields (macro avg.)     | 86.44     | 85.2      | 85.81     | 5996    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 85.34     | 84.14     | 84.74     | 1999    |
| first_author                | 97.11     | 95.84     | 96.47     | 1997    |
| title                       | 79.51     | 78.2      | 78.85     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **87.32** | **86.06** | **86.69** | 5996    |
| all fields (macro avg.)     | 87.32     | 86.06     | 86.69     | 5996    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 92.59     | 91.3      | 91.94     | 1999    |
| first_author                | 97.31     | 96.04     | 96.67     | 1997    |
| title                       | 92.02     | 90.5      | 91.25     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.98** | **92.61** | **93.29** | 5996    |
| all fields (macro avg.)     | 93.97     | 92.61     | 93.29     | 5996    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.64     | 87.39     | 88.01     | 1999    |
| first_author                | 96.91     | 95.64     | 96.27     | 1997    |
| title                       | 87.7      | 86.25     | 86.97     | 2000    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.08** | **89.76** | **90.42** | 5996    |
| all fields (macro avg.)     | 91.08     | 89.76     | 90.42     | 5996    |

#### Instance-level results

```
Total expected instances: 	2000
Total correct instances: 	1351 (strict) 
Total correct instances: 	1382 (soft) 
Total correct instances: 	1706 (Levenshtein) 
Total correct instances: 	1571 (ObservedRatcliffObershelp) 

Instance-level recall:	67.55	(strict) 
Instance-level recall:	69.1	(soft) 
Instance-level recall:	85.3	(Levenshtein) 
Instance-level recall:	78.55	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 2000 random PDF files out of 1998 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 88.08     | 82.46     | 85.18     | 97183   |
| date                        | 91.66     | 85.54     | 88.49     | 97630   |
| doi                         | 70.88     | 82.15     | 76.1      | 16894   |
| first_author                | 94.98     | 88.85     | 91.81     | 97183   |
| inTitle                     | 82.75     | 78.66     | 80.65     | 96430   |
| issue                       | 94.33     | 91.01     | 92.64     | 30312   |
| page                        | 94.93     | 77.65     | 85.43     | 88597   |
| pmcid                       | 65.91     | 82.65     | 73.34     | 807     |
| pmid                        | 69.06     | 81.46     | 74.75     | 2093    |
| title                       | 84.81     | 82.77     | 83.78     | 92463   |
| volume                      | 96.19     | 94.38     | 95.28     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **89.79** | **84.52** | **87.08** | 707301  |
| all fields (macro avg.)     | 84.87     | 84.33     | 84.31     | 707301  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.23     | 83.54     | 86.29     | 97183   |
| date                        | 91.66     | 85.54     | 88.49     | 97630   |
| doi                         | 75.36     | 87.34     | 80.91     | 16894   |
| first_author                | 95.41     | 89.25     | 92.23     | 97183   |
| inTitle                     | 92.28     | 87.71     | 89.94     | 96430   |
| issue                       | 94.33     | 91.01     | 92.64     | 30312   |
| page                        | 94.93     | 77.65     | 85.43     | 88597   |
| pmcid                       | 75.3      | 94.42     | 83.78     | 807     |
| pmid                        | 73.59     | 86.81     | 79.66     | 2093    |
| title                       | 93.16     | 90.92     | 92.03     | 92463   |
| volume                      | 96.19     | 94.38     | 95.28     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **92.62** | **87.17** | **89.81** | 707301  |
| all fields (macro avg.)     | 88.31     | 88.05     | 87.88     | 707301  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.48     | 88.46     | 91.37     | 97183   |
| date                        | 91.66     | 85.54     | 88.49     | 97630   |
| doi                         | 77.55     | 89.88     | 83.26     | 16894   |
| first_author                | 95.56     | 89.39     | 92.37     | 97183   |
| inTitle                     | 93.23     | 88.62     | 90.87     | 96430   |
| issue                       | 94.33     | 91.01     | 92.64     | 30312   |
| page                        | 94.93     | 77.65     | 85.43     | 88597   |
| pmcid                       | 75.3      | 94.42     | 83.78     | 807     |
| pmid                        | 73.59     | 86.81     | 79.66     | 2093    |
| title                       | 95.97     | 93.67     | 94.8      | 92463   |
| volume                      | 96.19     | 94.38     | 95.28     | 87709   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **93.93** | **88.41** | **91.09** | 707301  |
| all fields (macro avg.)     | 89.34     | 89.08     | 88.9      | 707301  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall   | f1        | support |
|-----------------------------|-----------|----------|-----------|---------|
| authors                     | 91.44     | 85.61    | 88.43     | 97183   |
| date                        | 91.66     | 85.54    | 88.49     | 97630   |
| doi                         | 76.05     | 88.14    | 81.65     | 16894   |
| first_author                | 95.03     | 88.89    | 91.86     | 97183   |
| inTitle                     | 90.96     | 86.46    | 88.66     | 96430   |
| issue                       | 94.33     | 91.01    | 92.64     | 30312   |
| page                        | 94.93     | 77.65    | 85.43     | 88597   |
| pmcid                       | 65.91     | 82.65    | 73.34     | 807     |
| pmid                        | 69.06     | 81.46    | 74.75     | 2093    |
| title                       | 95.24     | 92.95    | 94.08     | 92463   |
| volume                      | 96.19     | 94.38    | 95.28     | 87709   |
|                             |           |          |           |         |
| **all fields (micro avg.)** | **92.96** | **87.5** | **90.14** | 707301  |
| all fields (macro avg.)     | 87.34     | 86.8     | 86.78     | 707301  |

#### Instance-level results

```
Total expected instances: 		98799
Total extracted instances: 		97431
Total correct instances: 		43382 (strict) 
Total correct instances: 		54257 (soft) 
Total correct instances: 		58394 (Levenshtein) 
Total correct instances: 		55154 (RatcliffObershelp) 

Instance-level precision:	44.53 (strict) 
Instance-level precision:	55.69 (soft) 
Instance-level precision:	59.93 (Levenshtein) 
Instance-level precision:	56.61 (RatcliffObershelp) 

Instance-level recall:	43.91	(strict) 
Instance-level recall:	54.92	(soft) 
Instance-level recall:	59.1	(Levenshtein) 
Instance-level recall:	55.82	(RatcliffObershelp) 

Instance-level f-score:	44.22 (strict) 
Instance-level f-score:	55.3 (soft) 
Instance-level f-score:	59.52 (Levenshtein) 
Instance-level f-score:	56.21 (RatcliffObershelp) 

Matching 1 :	78549

Matching 2 :	4417

Matching 3 :	4352

Matching 4 :	2085

Total matches :	89403
```

#### Citation context resolution

```

Total expected references: 	 98797 - 49.4 references per article
Total predicted references: 	 97431 - 48.72 references per article

Total expected citation contexts: 	 142862 - 71.43 citation contexts per article
Total predicted citation contexts: 	 131241 - 65.62 citation contexts per article

Total correct predicted citation contexts: 	 111876 - 55.94 citation contexts per article
Total wrong predicted citation contexts: 	 19365 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 85.24
Recall citation contexts: 	 78.31
fscore citation contexts: 	 81.63
```

Evaluation metrics produced in 791.053 seconds
