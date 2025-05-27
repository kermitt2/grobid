## Header metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 92.77     | 92.53     | 92.65     | 1941    |
| first_author                | 96.64     | 96.39     | 96.52     | 1941    |
| title                       | 84.41     | 84.15     | 84.28     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.27** | **91.02** | **91.15** | 5825    |
| all fields (macro avg.)     | 91.27     | 91.02     | 91.15     | 5825    |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 94.73     | 94.49     | 94.61     | 1941    |
| first_author                | 97.06     | 96.81     | 96.93     | 1941    |
| title                       | 92.05     | 91.77     | 91.91     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **94.61** | **94.35** | **94.48** | 5825    |
| all fields (macro avg.)     | 94.61     | 94.35     | 94.48     | 5825    |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 96.59     | 96.34     | 96.47     | 1941    |
| first_author                | 97.31     | 97.06     | 97.19     | 1941    |
| title                       | 98.24     | 97.94     | 98.09     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **97.38** | **97.12** | **97.25** | 5825    |
| all fields (macro avg.)     | 97.38     | 97.12     | 97.25     | 5825    |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 95.66     | 95.41     | 95.54     | 1941    |
| first_author                | 96.64     | 96.39     | 96.52     | 1941    |
| title                       | 96.23     | 95.93     | 96.08     | 1943    |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **96.18** | **95.91** | **96.05** | 5825    |
| all fields (macro avg.)     | 96.18     | 95.91     | 96.05     | 5825    |

#### Instance-level results

```
Total expected instances: 	1943
Total correct instances: 	1527 (strict) 
Total correct instances: 	1694 (soft) 
Total correct instances: 	1836 (Levenshtein) 
Total correct instances: 	1782 (ObservedRatcliffObershelp) 

Instance-level recall:	78.59	(strict) 
Instance-level recall:	87.18	(soft) 
Instance-level recall:	94.49	(Levenshtein) 
Instance-level recall:	91.71	(RatcliffObershelp) 
```

## Citation metadata

Evaluation on 1943 random PDF files out of 1941 PDF (ratio 1.0).

#### Strict Matching (exact matches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 82.93     | 75.64     | 79.12     | 85778   |
| date                        | 94.34     | 83.46     | 88.56     | 87067   |
| first_author                | 89.65     | 81.74     | 85.51     | 85778   |
| inTitle                     | 72.88     | 71.07     | 71.96     | 81007   |
| issue                       | 89.96     | 87.45     | 88.68     | 16635   |
| page                        | 94.07     | 82.81     | 88.08     | 80501   |
| title                       | 79.46     | 74.55     | 76.93     | 80736   |
| volume                      | 95.72     | 88.96     | 92.22     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **86.94** | **79.97** | **83.31** | 597569  |
| all fields (macro avg.)     | 87.38     | 80.71     | 83.88     | 597569  |

#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 83.4      | 76.07     | 79.57     | 85778   |
| date                        | 94.34     | 83.46     | 88.56     | 87067   |
| first_author                | 89.82     | 81.89     | 85.68     | 85778   |
| inTitle                     | 84.62     | 82.51     | 83.55     | 81007   |
| issue                       | 89.96     | 87.45     | 88.68     | 16635   |
| page                        | 94.07     | 82.81     | 88.08     | 80501   |
| title                       | 91.23     | 85.59     | 88.32     | 80736   |
| volume                      | 95.72     | 88.96     | 92.22     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.34** | **83.09** | **86.56** | 597569  |
| all fields (macro avg.)     | 90.39     | 83.59     | 86.83     | 597569  |

#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 89.06     | 81.22     | 84.96     | 85778   |
| date                        | 94.34     | 83.46     | 88.56     | 87067   |
| first_author                | 90.02     | 82.07     | 85.86     | 85778   |
| inTitle                     | 85.87     | 83.73     | 84.79     | 81007   |
| issue                       | 89.96     | 87.45     | 88.68     | 16635   |
| page                        | 94.07     | 82.81     | 88.08     | 80501   |
| title                       | 93.56     | 87.78     | 90.58     | 80736   |
| volume                      | 95.72     | 88.96     | 92.22     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **91.67** | **84.32** | **87.84** | 597569  |
| all fields (macro avg.)     | 91.57     | 84.69     | 87.97     | 597569  |

#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

| label                       | precision | recall    | f1        | support |
|-----------------------------|-----------|-----------|-----------|---------|
| authors                     | 85.87     | 78.31     | 81.92     | 85778   |
| date                        | 94.34     | 83.46     | 88.56     | 87067   |
| first_author                | 89.67     | 81.75     | 85.53     | 85778   |
| inTitle                     | 83.2      | 81.13     | 82.15     | 81007   |
| issue                       | 89.96     | 87.45     | 88.68     | 16635   |
| page                        | 94.07     | 82.81     | 88.08     | 80501   |
| title                       | 93.16     | 87.4      | 90.19     | 80736   |
| volume                      | 95.72     | 88.96     | 92.22     | 80067   |
|                             |           |           |           |         |
| **all fields (micro avg.)** | **90.73** | **83.45** | **86.94** | 597569  |
| all fields (macro avg.)     | 90.75     | 83.91     | 87.17     | 597569  |

#### Instance-level results

```
Total expected instances: 		90125
Total extracted instances: 		85383
Total correct instances: 		38338 (strict) 
Total correct instances: 		50385 (soft) 
Total correct instances: 		55189 (Levenshtein) 
Total correct instances: 		51791 (RatcliffObershelp) 

Instance-level precision:	44.9 (strict) 
Instance-level precision:	59.01 (soft) 
Instance-level precision:	64.64 (Levenshtein) 
Instance-level precision:	60.66 (RatcliffObershelp) 

Instance-level recall:	42.54	(strict) 
Instance-level recall:	55.91	(soft) 
Instance-level recall:	61.24	(Levenshtein) 
Instance-level recall:	57.47	(RatcliffObershelp) 

Instance-level f-score:	43.69 (strict) 
Instance-level f-score:	57.42 (soft) 
Instance-level f-score:	62.89 (Levenshtein) 
Instance-level f-score:	59.02 (RatcliffObershelp) 

Matching 1 :	67653

Matching 2 :	4141

Matching 3 :	1853

Matching 4 :	669

Total matches :	74316
```

#### Citation context resolution

```

Total expected references: 	 90125 - 46.38 references per article
Total predicted references: 	 85383 - 43.94 references per article

Total expected citation contexts: 	 139835 - 71.97 citation contexts per article
Total predicted citation contexts: 	 110811 - 57.03 citation contexts per article

Total correct predicted citation contexts: 	 93760 - 48.26 citation contexts per article
Total wrong predicted citation contexts: 	 17051 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts: 	 84.61
Recall citation contexts: 	 67.05
fscore citation contexts: 	 74.81
```

Evaluation metrics produced in 675.814 seconds
