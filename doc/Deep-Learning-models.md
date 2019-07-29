# Using Deep Learning models instead of default CRF

## Integration with DeLFT

Since version 0.5.4, it is possible to use in GROBID recent Deep Learning sequence labelling models trained with [DeLFT](https://github.com/kermitt2/delft).  The available neural models are currently BidLSTM-CRF with Glove embeddings, which can be used as alternative to the default Wapiti CRF.

This architecture has been tested on Linux 64bit and macOS.   

Integration is realized via Java Embedded Python [JEP](https://github.com/ninia/jep), which uses a JNI of CPython. This integration is two times faster than the Tensorflow Java API and significantly faster than RPC serving (see https://www.slideshare.net/FlinkForward/flink-forward-berlin-2017-dongwon-kim-predictive-maintenance-with-apache-flink), and it does not require to modify DeLFT as it would be the case with Py4J gateway (socket-based).

There are no neural model for the segmentation and the fulltext models, because the input sequence is far too big. The problem would need to be formulated differently for these tasks.

Low level models not using layout features (author name, dates, affliliations...) perform similarly as CRF, but CRF is much better when layout features are involved (in particular for the header model). However, the neural models do not use additional features for the moment.

See some evaluation under `grobid-trainer/docs`.

Current neural models are 3-4 time slower than CRF: we do not use batch processing for the moment. It is not clear how to use batch processing with a cascading approach.


### Getting started with DL

#### Classic python 

- install [DeLFT](https://github.com/kermitt2/delft) 

- indicate the path of the DeLFT install in `grobid.properties` (`grobid-home/config/grobid.properties`)

- change the engine from `wapiti` to `delft` in the `grobid-properties` file

- run grobid 

> ./gradlew run

#### Virtualenv

- create the virtualenv environment (the Delft instruction are based on that)

- activate the environment

- run Grobid

#### Anaconda 

- create the anaconda environment

CPU only: 

> conda create --name grobidDelft --file requirements.conda.delft.cpu.txt

GPU (has not been tested): 
> conda create --name grobidDelft --file requirements.conda.delft.gpu.txt

- activate the environment: 

> conda activate grobidDelft

- install [DeLFT](https://github.com/kermitt2/delft), ignore the pip command for the installation in the delft documentation

- indicate the path of the DeLFT install in `grobid.properties` (`grobid-home/config/grobid.properties`)

- change the engine from `wapiti` to `delft` in the `grobid-properties` file

- run grobid

> ./gradlew run


## Future improvements

ELMo embeddings have not been experimented for the GROBID models yet, but they could make some models better than their CRF counterpart, although probably too slow for concrete usage (it will make these models 100 times slower than the current CRF in our estimate). ELMo embeddings are already integrated in DeLFT.

However, we have also recently experimented with BERT fine-tuning for sequence labelling and more particularly with [SciBERT](https://github.com/allenai/scibert) (a BERT base model trained on Wikipedia and some semantic-scholar full texts). 
We got excellent results with a runtime close to RNN with Gloves embeddings (20 times faster than with ELMo embeddings). This is the target architectures for future GROBID Deep Learning models. 


## Troubleshooting

1. If there is a dependency problem when JEP starts usually the virtual machine is crashing. 
We are still discovering this part, please feel free to submit issues should you incur in these problems. 
See discussion [here](https://github.com/kermitt2/grobid/pull/454)

2. If the GLIBC causes an error,  
```
! ImportError: /lib64/libstdc++.so.6: version `GLIBCXX_3.4.20' not found (required by /home/Luca/.conda/envs/tensorflow/lib/python3.6/site-packages/tensorflow/python/_pywrap_tensorflow_internal.so)
```

here a quick solution (libgcc should be already installed, if so, just skip that pass): 


> conda install libgcc

> export export LD_PRELOAD=$anaconda_path/lib/libstdc++.so.6.0.25
    
> ./gradlew run
