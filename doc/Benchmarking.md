# Benchmarking

## End-to-end evaluation

This is the end-to-end benchmarking result for GROBID version 0.6.0 against the `PMC_sample_1943` dataset, see the [End-to-end evaluation](End-to-end-evaluation.md) page for explanations and for reproducing this evaluation. Key points to consider are the following:

- This dataset is independent from the training data used to train the different models involved in the full document processing, in particular several models do not use data from PMC articles at all. As a stable holdout set, it should thus provide a more reliable evaluation than cross-validation metrics.

- The evaluation covers the whole process, including PDF extraction, PDF noisiness and error cascading. It should thus provide a more realistic evaluation for the end user than the model-specific metrics with "clean" data that are usually reported in the literature. 

- As the evaluation data come from XML PMC and the scientific publishers, it contains some encoding errors (publisher data are far from perfect) and are not always complete (for instance some bibliographical references are provided as raw string and not structured). The results are therefore more an indication of error rates than trusful absolute accuracy performances.

- We think that these metrics are very good to compare improvements over time and to catch possible regressions, because relative improvements can be reliable with slighty imperfect evaluation data. 

More recent versions of these benchmarks might be available [here](https://github.com/kermitt2/grobid/tree/master/grobid-trainer/doc).

### General

The following end-to-end results are using CRF Wapiti as sequence labelling engine. Header extractions are consolidated by default with [biblio-glutton](https://github.com/kermitt2/biblio-glutton) service (the results with CrossRef REST API as consolidation service are similar but much slower). See [below](https://grobid.readthedocs.io/en/latest/Benchmarking/#deep-learning-models) for some evaluations with Deep-Learning architectures. 

Evaluation on 1943 random PDF files out of 1943 PDF from 1943 different journals (0 PDF parsing failure).

### Header metadata

#### Strict Matching (exact matches)

**Field-level results**

| label            |  precision |   recall  |     f1     | support |
|---		       |---	        |---	    |---	     |---      |
| abstract         |   14.71    |     13.87 |     14.28  | 1911    |
| authors          |   91.24    |     90.73 |     90.98  | 1941    | 
| first_author     |   96.36    |     95.47 |     95.91  | 1941    |
| keywords         |   65.63    |     53.41 |     58.89  | 1380    |
| title            |   83.92    |     83.02 |     83.47  | 1943    |
|				   |		    |			|	         |	       |
| **all fields (micro average)**|**71.61**|**68.33**|**69.93**|9116|
| all fields (macro average)| 70.37 | 67.3  |     68.71  | 9116    |


#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

|label         |    precision  |  recall   |    f1       |   support |
|---		   |---	           |---		   |---	         |---	     |
|abstract      |       51.58   |     48.61 |       50.05 |      1911 |
|authors       |       91.55   |     91.04 |       91.29 |      1941 | 
|first_author  |       96.46   |     95.57 |       96.01 |      1941 |  
|keywords      |       78.01   |     63.48 |       70    |      1380 |
|title         |       91.31   |     90.32 |       90.82 |      1943 |
|			   |			   |		   |			 |	         |
|**all fields (micro average)**|**82.56**|**78.78**|**80.63**|  9116 |
|all fields (macro average)| 81.78 | 77.8  |       79.63 |      9116 |


#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

|label       |      precision  |  recall   |    f1     | support  |
|---		 |---	           |---		   |---		   |---	      |
|abstract    |         87.51   |     82.47 |    84.91  |   1911   |
|authors     |         96.11   |     95.57 |    95.84  |   1941   |
|first_author|         96.78   |     95.88 |    96.33  |   1941   |
|keywords    |         88.87   |     72.32 |    79.74  |   1380   |
|title       |         94.38   |     93.36 |    93.87  |   1943   |
|			 |		           |		   |		   |	      |
|**all fields (micro average)**|**93.16**|**88.9**|**90.98**|9116 |
|all fields (macro average)| 92.73 | 87.92 |   90.14   |   9116   |


####Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

|label       |        precision |  recall   |    f1     |   support  |
|---		 |---	            |---		|---	    |---	     |
|abstract    |          80.79   |    76.14  |   78.39   |     1911   |
|authors     |          93.63   |    93.1   |   93.36   |     1941   |
|first_author|         96.36    |    95.47  |   95.91   |     1941   |
|keywords    |          84.15   |    68.48  |   75.51   |     1380   |
|title       |          93.86   |    92.85  |   93.35   |     1943   |
|			 |			        |		    |	        |			 |		
|**all fields (micro average)**|**90.4**|**86.27**|**88.29**| 9116   |
|all fields (macro average)| 89.76 | 85.21  |   87.31   |     9116   |


#### Instance-level results

```
Total expected instances:       1943
Total correct instances:        172 (strict) 
Total correct instances:        672 (soft) 
Total correct instances:        1230 (Levenshtein) 
Total correct instances:        1100 (ObservedRatcliffObershelp) 

Instance-level recall:  8.85    (strict) 
Instance-level recall:  34.59   (soft) 
Instance-level recall:  63.3    (Levenshtein) 
Instance-level recall:  56.61   (RatcliffObershelp)
```

### Citation metadata

Evaluation on 1942 random PDF files out of 1943 PDF (1 PDF parsing failure).

#### Strict Matching (exact matches)

**Field-level results**

|label      |        precision  |  recall   |     f1  |   support  |
|---		|---	            |---		|---	  |---	       |
|authors    |          83.19    |    74.55  |   78.64 |    85778   |
|date       |          92.74    |    81.82  |   86.94 |    87067   |  
|first_author|         89.92    |    80.56  |   84.99 |    85778   | 
|inTitle    |          71.33    |    69.8   |   70.56 |    81007   | 
|issue      |          88.74    |    82.65  |   85.59 |    16635   | 
|page       |          92.84    |    82.42  |   87.32 |    80501   |
|title      |          78.39    |    72.4   |   75.27 |    80736   | 
|volume     |          94.9     |    87.54  |   91.07 |    80067   |
|			|			        |			|		  |		       |
|**all fields (micro average)**|**86.11**|**78.56**|**82.17**|597569|
|all fields (macro average)| 86.51 | 78.97  |   82.55 |    597569  |


#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

|label      |        precision  | recall    |   f1     |  support |
|---		|---	            |---		|---	   |---	      |
|authors    |          83.51    |    74.84  |   78.93  |   85778  | 
|date       |          92.74    |    81.82  |   86.94  |   87067  |  
|first_author|         90.11    |    80.73  |   85.16  |   85778  |  
|inTitle    |          82.72    |    80.94  |   81.82  |   81007  |
|issue      |          88.74    |    82.65  |   85.59  |   16635  |
|page       |          92.84    |    82.42  |   87.32  |   80501  |
|title      |          89.48    |    82.64  |   85.93  |   80736  |
|volume     |          94.9     |    87.54  |   91.07  |   80067  | 
|			|			        |			|		   |		  |
|**all fields (micro average)**|**89.36**|**81.52**|**85.26**|597569|
|all fields (macro average)| 89.38 | 81.7   |   85.34  |   597569 |


#### Levenshtein Matching (Minimum Levenshtein distance at 0.8)

**Field-level results**

|label      |         precision |  recall   |   f1     | support  |
|---		|---	            |---		|---	   |---       |
|authors    |          88.73    |    79.52  |   83.87  |   85778  |
|date       |          92.74    |    81.82  |   86.94  |   87067  |  
|first_author|         90.22    |    80.83  |   85.27  |   85778  | 
|inTitle    |          83.65    |    81.86  |   82.75  |   81007  |  
|issue      |          88.74    |    82.65  |   85.59  |   16635  |
|page       |          92.84    |    82.42  |   87.32  |   80501  |  
|title      |          92.54    |    85.46  |   88.86  |   80736  |  
|volume     |          94.9     |    87.54  |   91.07  |   80067  |
|			|			        |	 		|		   |		  |
|**all fields (micro average)**|**90.66**|**82.72**|**86.51**|597569|
|all fields (macro average)| 90.55 | 82.76  |   86.46  |   597569 |


#### Ratcliff/Obershelp Matching (Minimum Ratcliff/Obershelp similarity at 0.95)

**Field-level results**

|label      |         precision |  recall   |   f1     | support  |
|---		|---	            |---		|---	   |---	      |
|authors    |          85.6     |    76.71  |   80.91  |   85778  | 
|date       |          92.74    |    81.82  |   86.94  |   87067  |  
|first_author|         89.94    |    80.58  |   85     |   85778  | 
|inTitle    |          81.31    |    79.56  |   80.42  |   81007  |  
|issue      |          88.74    |    82.65  |   85.59  |   16635  | 
|page       |          92.84    |    82.42  |   87.32  |   80501  | 
|title      |          91.61    |    84.6   |   87.97  |   80736  |  
|volume     |          94.9     |    87.54  |   91.07  |   80067  | 
|			|			        |			|		   |		  |
|**all fields (micro average)**|**89.71**|**81.85**|**85.6**|597569|
|all fields (macro average)| 89.71 | 81.98  |   85.65  |   597569 |


#### Instance-level results 

```
Total expected instances:               90125
Total extracted instances:              90917
Total correct instances:                37853 (strict) 
Total correct instances:                48998 (soft) 
Total correct instances:                53382 (Levenshtein) 
Total correct instances:                50187 (RatcliffObershelp) 

Instance-level precision:       41.63 (strict) 
Instance-level precision:       53.89 (soft) 
Instance-level precision:       58.72 (Levenshtein) 
Instance-level precision:       55.2 (RatcliffObershelp) 

Instance-level recall:  42      (strict) 
Instance-level recall:  54.37   (soft) 
Instance-level recall:  59.23   (Levenshtein) 
Instance-level recall:  55.69   (RatcliffObershelp) 

Instance-level f-score: 41.82 (strict) 
Instance-level f-score: 54.13 (soft) 
Instance-level f-score: 58.97 (Levenshtein) 
Instance-level f-score: 55.44 (RatcliffObershelp) 

Matching 1 :    65184

Matching 2 :    4675

Matching 3 :    2749

Matching 4 :    690

Total matches : 73298

```

#### Citation context resolution

```
======= Citation context resolution ======= 

Total expected references:       90125 - 46.38 references per article
Total predicted references:      90917 - 46.79 references per article

Total expected citation contexts:        139835 - 71.97 citation contexts per article
Total predicted citation contexts:       119472 - 61.49 citation contexts per article

Total correct predicted citation contexts:       96712 - 49.77 citation contexts per article
Total wrong predicted citation contexts:         22760 (wrong callout matching, callout missing in NLM, or matching with a bib. ref. not aligned with a bib.ref. in NLM)

Precision citation contexts:     80.95
Recall citation contexts:        69.16
fscore citation contexts:        74.59
```

### Fulltext structures  

Fulltext structure contents are complicated to capture from JATS NLM files. They are often normalized and different from the actual PDF content and are can be inconsistent from one document to another. The scores of the following metrics are thus not very meaningful in absolute term, in particular for the strict matching (textual content of the srtructure can be very long). As relative values for comparing different models, they seem however useful.  

#### Strict Matching (exact matches)

**Field-level results**

|label      |         precision |  recall   |   f1     | support |
|---        |---                |---        |---       |---      |
|figure_title|        32.24     |   23.04   |  26.87   |  7058   |
|reference_citation|  57.17     |   58.08   |  57.62   |  134196 |
|reference_figure|    60.68     |   61.51   |  61.09   |  19330  |
|reference_table |    81.03     |   83.29   |  82.15   |  7327   |
|section_title   |    73.7      |   67.12   |  70.25   |  27619  |
|table_title     |    55.4      |   49.74   |  52.42   |  3784   |
|				 |			    |			|		   |	   	 |
|**all fields (micro average)**|**59.87**|**59.19**|**59.53**|199314 |
|all fields (macro average)| 60.04 | 57.13  |  58.4    |  199314 |


#### Soft Matching (ignoring punctuation, case and space characters mismatches)

**Field-level results**

|label      |         precision |  recall   |   f1     | support  |
|---        |---                |---        |---       |---       |
|figure_title  |       73.06    |    52.21  |   60.9   |   7058   |
|reference_citation |  61.31    |    62.29  |   61.8   |   134196 |
|reference_figure   |  61.75    |    62.6   |   62.17  |   19330  |
|reference_table    |  81.61    |    83.9   |   82.74  |   7327   |
|section_title      |   78.44   |    71.44  |   74.78  |   27619  |
|table_title        |  80.34    |    72.12  |   76.01  |   3784   |
|					|			|			|		   |		  |
|**all fields (micro average)**|**64.95**|**64.21**|**64.58**|199314|
|all fields (macro average)| 72.75 | 67.43  |   69.73  |   199314 |


## Deep Learning models


### Accuracy 

For information, we report here some evaluations made with deep learning architectures for sequence labelling in comparison to CRF Wapiti. All results have been obtained with 10-fold cross-evaluations (average based on 10 sucessive partition of the training data). 

The Deep Learning architectures have been extended to use a feature channel for exploiting layout features (position of tokens, font, size, etc.), see [DeLFT](https://github.com/kermitt2/delft) and [here](https://github.com/kermitt2/delft/pull/82) for more details. Although we are working since more than 2 years on this approach, the usage of Deep Learning for Grobid tasks is still experimental and very far from something production ready with similar performances and scalability as the current GROBID stable version with CRF Wapiti.

However note that for some simpler NER-style tasks or especially for text classification, we found that Deep Learning approaches are significantly superior and usable, and, in contrast, we already developed and deployed several systems in production based on DeLFT. 

**Summary** 

Architectures: 

- [Architecture 1](https://github.com/kermitt2/delft/pull/82#issuecomment-587280497): using normal dropout after the BidLSTM of the feature channel

- [Architecture 2](https://github.com/kermitt2/delft/pull/82#issuecomment-588570868): using normal dropouts between embeddings and BidLSTM of the feature channel

- [Architecture 3](https://github.com/kermitt2/delft/pull/82#issuecomment-589442381): using recurrent dropouts on the BidLSTM of the feature channel

- Ignored features: using the standard BidLSTM-CRF without the use of any layout feature information

`Trainable=true` indicate that the features embeddings are trainable. 

All metrics has been calculated by running n-fold cross-validation with n = 10.

|Model |  CRF Wapiti | [Architecture 1](https://github.com/kermitt2/delft/pull/82#issuecomment-589447087) | [Architecture 1](https://github.com/kermitt2/delft/pull/82#issuecomment-593787846) (Trainable = true) | [Architecture 2](https://github.com/kermitt2/delft/pull/82#issuecomment-589439496) | [Architecture 2](https://github.com/kermitt2/delft/pull/82#issuecomment-593788260) (Trainable = true) | [Architecture 3](https://github.com/kermitt2/delft/pull/82#issuecomment-589523067) | [Architecture 3](https://github.com/kermitt2/delft/pull/82#issuecomment-594249488) (Trainable = true) | [Ignore features](https://github.com/kermitt2/delft/pull/82#issuecomment-586652333) |
|-- | -- | -- | -- | -- | -- | -- | -- | -- | 
|Affiliation-address | 0.8587 | 0.8709 | 0.8714 | 0.8721 | 0.872 | **0.873** | 0.8677 | 0.8668 | 
|Citation | 0.9448 | 0.9516 | **0.9522** | 0.9501 | 0.9503 | 0.9518 | 0.951 | 0.95 | 
|Date | **0.9833** | 0.9628 | 0.96 | 0.9606 | 0.9616 | 0.9631 | 0.961 | 0.9663 | 
|Figure | **0.9839** | 0.5594 | 0.5397 | 0.5907 | 0.4714 | 0.5515 | 0.6219 | 0.2949 | 
|Header | **0.7425** |0.7107 | 0.7102 | 0.7139 | 0.7156 | 0.7215 | 0.713 | 0.6764 | 
|Software | 0.7764 | 0.8112 | **0.8128** | 0.807 | 0.8039 | 0.8038 | 0.8084 | 0.7915 | 
|Superconductors [85 papers] | 0.6528 | 0.7774 | 0.772 | 0.7767 | **0.7814** | 0.7766 | 0.7791 | 0.7663 | 
|Quantities | 0.8014 | 0.8809 | 0.8752 | **0.883** | 0.8701 | 0.8724 | 0.8727 | 0.8733 | 
|Unit |  **0.9886** | 0.9838 | 0.9834 | 0.9829 | 0.9826 | 0.9816 | 0.9846 | 0.9801 |
|Values | 0.8457 | 0.979 | **0.9874** | 0.9854 | 0.9852 | 0.9851 | 0.9853 | 0.9827 | 
|  |   |   |   |   |   |   |   |  |
|**Average** | **0.85781** | 0.84877 | 0.84643 | 0.85224 | 0.83941 | 0.84804 | 0.85447 | 0.81483 | 


### Runtime

To appreciate the runtime impact of Deep Learning models over CRF Wapiti, we report here some relevant comparisons. The following runtimes were obtained based on a Ubuntu 16.04 server Intel i7-4790 (4 CPU), 4.00 GHz with 16 GB memory. The runtimes for the Deep Learning architectures are based on the same machine with a nvidia GPU GeForce 1080Ti (11 GB). We run here a [software mention recognizer](https://github.com/ourresearch/software-mentions) model with Grobid as reference model, but any Grobid model would exhibit similar relative differences. 

|CRF Wapiti ||
|--- | --- |
|threads | tokens/s | 
|1 | 23,685 | 
|2 | 43,281|
|3 | 59,867 | 
|4 | 73,339|
|6 | 92,385 | 
|7 | 97,659|
|8 | 100,879 | 

| BiLSTM-CRF || 
| --- |--- | 
| batch size | tokens/s | 
| 50 | 24,774 | 
| 100 | 28,707| 
| 150 | 30,247|
| 200 | 30,520|

| BiLSTM-CRF+ELMo||
| ---| --- |
| batch size | tokens/s|
| 5 | 271|
| 7 | 365|

| SciBERT+CRF||
| ---| --- |
| batch size | tokens/s|
| 5 | 4,729|
| 6 | 5,060|

Additional remarks:

- Batch size is a parameter constrained by the capacity of the available GPU. An improvement of the performance of the deep learning architecture requires increasing the number of GPU and the amount of memory of these GPU, similarly as improving CRF Wapiti capacity requires increasing the number of available threads and CPU. Running a Deep Learning architectures on CPU is around 50 times slower than on GPU (although it depends on the amount of RAM available with the CPU, which can allow to increase the batch size significantly). 

- The BERT-CRF architecture in DeLFT is a modified and heavily optimized version of the Google Research [reference distribution of BERT](https://github.com/google-research/bert) (which does not support sequence labelling as such), with a final CRF activation layer instead of a softmax (a CRF activation layer improves f-score in average by +0.30 for sequence labelling task). Above we run SciBERT, a BERT base model trained on scientific literature. Also note that given their limit of the size of the input sequence (512 tokens), BERT models are challenging to apply to several Grobid tasks which are working at document or paragraph levels. 

- Finally we present here the runtime for a single model. When using a cascade of models as in the Grobid core PDF structuring task, involving 9 different sequence labelling models, the possibility to use efficiently the batch size with the DL architecture is very challenging. In practice, as the batches will be often filled by 1 or a few input sequences, the runtime for a single document will be significantly longer (up to 100 times slower), and adapting the processing of multiple PDF in parallel with DL batches will require an important development effort. 
