# Using Deep Learning models instead of default CRF

## Integration with DeLFT

Since version `0.5.4` (2018), it is possible to use in GROBID recent Deep Learning sequence labelling models trained with [DeLFT](https://github.com/kermitt2/delft).  The available neural models include in particular BidLSTM-CRF with Glove embeddings, with ELMo, and BERT fine-tuned architectures with CRF activation layer (e.g. SciBERT-CRF), which can be used as alternative to the default Wapiti CRF.

These architectures have been tested on Linux 64bit and macOS.   

Integration is realized via Java Embedded Python [JEP](https://github.com/ninia/jep), which uses a JNI of CPython. This integration is two times faster than the Tensorflow Java API and significantly faster than RPC serving (see [here](https://www.slideshare.net/FlinkForward/flink-forward-berlin-2017-dongwon-kim-predictive-maintenance-with-apache-flink), and it does not require to modify DeLFT as it would be the case with Py4J gateway (socket-based).

There are no neural model for the segmentation and the fulltext models, because the input sequence is far too big. The problem would need to be formulated differently for these tasks or to use alternative DL architectures.

Low level models not using layout features (author name, dates, affliliations...) perform better than CRF, but CRF is better when layout features are involved (in particular for the header model). However, the neural models will also use additional feature channels in future versions to integrate the layout features.

See some evaluation under [model benchmarking](Benchmarking-models.md) and `grobid-trainer/docs`.

Current neural models are at least 3-4 time slower than CRF: we do not use batch processing for the moment. It is not clear how to use batch processing with a cascading approach.


### Getting started with DL

#### Classic python 

- install [DeLFT](https://github.com/kermitt2/delft) 

- indicate the path of the DeLFT install in `grobid.properties` (`grobid-home/config/grobid.properties`)

- change the engine from `wapiti` to `delft` in the `grobid-properties` file

- run grobid 

```shell
./gradlew run
```

#### Virtualenv

- create the virtualenv environment (the DeLFT instruction are based on that scenario)

- activate the environment

- run Grobid

#### Anaconda 

- create the anaconda environment

CPU only: 

```shell
conda create --name grobidDelft --file requirements.conda.delft.cpu.txt
```

GPU: 

```shell
conda create --name grobidDelft --file requirements.conda.delft.gpu.txt
```

- activate the environment: 

```shell
conda activate grobidDelft
```

- install [DeLFT](https://github.com/kermitt2/delft), ignore the pip command for the installation in the delft documentation

- indicate the path of the DeLFT install in `grobid.properties` (`grobid-home/config/grobid.properties`)

- change the engine from `wapiti` to `delft` in the `grobid-properties` file

- run grobid

```shell
./gradlew run
```

## Troubleshooting

1. If there is a dependency problem when JEP starts usually the virtual machine is crashing. 
We are still discovering this part, please feel free to submit issues should you incur in these problems. 
See discussion [here](https://github.com/kermitt2/grobid/pull/454)

2. If the GLIBC causes an error,  

```
! ImportError: /lib64/libstdc++.so.6: version `GLIBCXX_3.4.20' not found (required by /home/Luca/.conda/envs/tensorflow/lib/python3.6/site-packages/tensorflow/python/_pywrap_tensorflow_internal.so)
```

here a quick solution (libgcc should be already installed, if so, just skip that pass): 

```shell
conda install libgcc

export export LD_PRELOAD=$anaconda_path/lib/libstdc++.so.6.0.25
    
./gradlew run
```
