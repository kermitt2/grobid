# Using Deep Learning models instead of default CRF

## Integration with DeLFT

Since version `0.5.4` (2018), it is possible to use in GROBID recent Deep Learning sequence labelling models trained with [DeLFT](https://github.com/kermitt2/delft). The available neural models include in particular BidLSTM-CRF with Glove embeddings, with ELMo, and BERT fine-tuned architectures with CRF activation layer (e.g. SciBERT-CRF), which can be used as alternative to the default Wapiti CRF.

These architectures have been tested on Linux 64bit and macOS.   

Integration is realized via Java Embedded Python [JEP](https://github.com/ninia/jep), which uses a JNI of CPython. This integration is two times faster than the Tensorflow Java API and significantly faster than RPC serving (see [here](https://www.slideshare.net/FlinkForward/flink-forward-berlin-2017-dongwon-kim-predictive-maintenance-with-apache-flink), and it does not require to modify DeLFT as it would be the case with Py4J gateway (socket-based).

There are no neural model for the segmentation and the fulltext models, because the input sequences for these models are far too large for the current supported Deep Learning architectures. The problem would need to be formulated differently for these tasks or to use alternative DL architectures (with sliding window, etc.).

Low level models not using layout features (author name, dates, affiliations...) perform better than CRF. When layout features are involved, neural models with an additional feature channel should be preferred (e.g. `BidLSTM_CRF_FEATURES` in DeLFT), or they will perform significantly worse than Wapiti CRF.

See some evaluation under [model benchmarking](Benchmarking-models.md) and `grobid-trainer/docs`.

Current neural models are at least 3 to 100 time slower than CRF, depending on the architecture: we do not use NN batch processing for the moment. It is not clear how to use batch processing with a model cascading approach, but we are working on it.

### Getting started with Deep Learning

Using Deep Learning model in GROBID is not straightforward at the present time, due to the required availability of various native libraries and to the Python dynamic linking and packaging mess, which leads to some strict version and system dependencies. Interfacing natively to a particular Python virtual environment (which is "sesssion-based") is challenging. We are exploring different approach to facilitate this and get a "out-of-the-out" working system in a near future - which will be likely a docker image. In the meantime, here are the step-by-step instructions to get a working Deep Learning GROBID.

#### Classic python and Virtualenv

<span>0.</span> Install GROBID as indicated [here](https://grobid.readthedocs.io/en/latest/Install-Grobid/).

You __must__ use a Java version under or equals to Java 11. At the present time, JVM 1.12 to 1.17 will fail to load the native JEP library (due to additional security constraints).

<span>1.</span> install [DeLFT](https://github.com/kermitt2/delft), see instructions [here](https://github.com/kermitt2/delft#install).

In case you have no GPU and you use CPU, you should set in the DeLFT's `requirements.txt` file:

```
tensorflow==1.12.0
```

instead of:

```
tensorflow_gpu==1.12.0
```

(although in principle `tensorflow_gpu` should fall back to CPU when no GPU is available, it creates some problems in practice, in particular looking for  installed CUDA libraries even if there's no GPU on the system).

<span>2.</span> Test your DeLFT installation for GROBID models: 

```shell
cd deflt/
python3 grobidTagger.py citation tag 
```

If it works (you see some annotations in JSON format), you are sure to have a working DeLFT environment for all GROBID models. The next steps address the native bridge between DeLFT and the JVM running GROBID. 

<span>3.</span> Configure your GROBID properties file. 

Indicate the path to the DeLFT install in the GROBID properties file `grobid.properties` (`grobid-home/config/grobid.properties`), 

```properties
grobid.delft.install=../delft
```

Indicate the GROBID model that should use a Deep Learning implementation in the same properties file, for instance if you wish to use a Deep Learning model for the citation model (it provides 1-2 additional points to the f-score for bibliographical reference recognition) use:

```properties
grobid.crf.engine.citation=delft
```

The default Deep Learning architecture will be `BidLSTM_CRF`, which is the best sequence labelling RNN architecture (basically a slightly revised version of [(Lample et al., 2016)](https://arxiv.org/abs/1603.01360) with Gloves embeddings). If you wish to use another architecture, you need to specify it in the same properties file, for instance:

```properties
grobid.delft.architecture=scibert
```

However, it will work only if the model is available under `grobid-home/models/`, currently only the `BidLSTM_CRF` models is shipped with GROBID, given the huge size of transformer models (1.3GB). To use a different architecture, you will thus need to train the new architecture model first with DeLFT and copy all the model files under the specific model subdirectory `grobid-home/models/**model**/`. 


If you are using a Python environment for the DeLFT installation, you can set the environment path in the properties file as well:

```properties
grobid.delft.python.virtualEnv=/where/my/damned/python/virtualenv/is/
```

Normally by setting the Python environment path in the properties file, you will not need to launch GROBID in the same activated environment. 


<span>4.</span> Install [JEP](https://github.com/ninia/jep) manually and preferably globally (outside a virtual env. and not under `~/.local/lib/python3.*/site-packages/`):

```shell
git clone https://github.com/ninia/jep
cd jep
sudo -E python3 setup.py build install
```

the `sudo -E` should ensure that JEP is installed globally and that the right JVM version is used (`-E` indicates to preserve the environment variables, in particular the `JAVA_HOME`). Installing JEP gloablly is the only safe way we found to be sure that JEP will work correctly in the JVM.

(here we are unfortunately touching the limit of the messy Python package management system, an install of JEP in a virtualenv should isolate the library depending on the pip/python version, but the JVM might not be able to found and linked these local/isolated libraries, even when the JVM is launched in the virtual env. A global install of JEP should however always work. pip in a virtual env still uses global python libraries when installed, but when pip uses user-level local libraries (e.g. libraries installed under `~/.local/lib/python3.5/site-packages/`) in the virtual env, we did not find a reliable way to make JEP working in the JVM.)

Copy the built JEP library to the `grobid-home/lib/` area - for instance on a Linux 64 machine with Python 3.5:

```shell
lopez@work:~/jep$ cp build/lib.linux-x86_64-3.5/jep/jep.cpython-35m-x86_64-linux-gnu.so ~/grobid/grobid-home/lib/lin-64/libjep.so
```

This will ensure that the GROBID native JEP library matches the JEP version of the system/environment (i.e. same OS and the same python version) and the JEP jar version (GROBID contains a default `libjep.so` for python3.6 and JEP `3.9.1`, it will not work with other version of python and other version of JEP).

Also note that the installed version of JEP must be the same as given in `grobid/build.gradle`. The python version used in the system or environment must match the native JEP library. At the time this documentation is written, we are using JEP version `3.9.1`:

```
implementation 'black.ninia:jep:3.9.1'
```

So if another version of JEP is installed globally on the system, you should update the GROBID `build.gradle` to match this version.

<span>5.</span> Run GROBID, this is the "*but on my machine it works*" moment: 

To run the grobid service, under `grobid/`:

```shell
./gradlew run
```

In case you have installed DeLFT with virtualenv and you have not indicated the virtual environment in the GROBID properties file, you should start GROBID after having activated the virtualenv. 

When running the GROBID service on a first input, you should see in the logs that the expected Deep Learning models are loaded and the JEP threads are activated:

```
INFO  [2020-10-30 23:04:07,756] org.grobid.core.jni.DeLFTModel: Loading DeLFT model for citation with architecture BidLSTM_CRF...
INFO  [2020-10-30 23:04:07,758] org.grobid.core.jni.JEPThreadPool: Creating JEP instance for thread 44
```

It is then possible to [benchmark end-to-end](https://grobid.readthedocs.io/en/latest/End-to-end-evaluation/) the selected Deep Learning models as any usual GROBID benchmarking exercise. In practice the CRF models should be mixed with Deep Learning models to keep the process reasonably fast and memory-hungry. In addition, note that, currently, due to the limited amount of training data, Deep Learning models perform significantly better than CRF only for two models (`citation` and `affiliation-address`), so there is likely no practical interest to use Deep Learning for the other models - see the [model-level evaluations](https://grobid.readthedocs.io/en/latest/Benchmarking-models/). This will of course certainly change in the future! 


#### Anaconda 

In case you use an anaconda environment instead of a Virtualenv:

- create the anaconda environment

```shell
conda create --name grobidDelft pip 
```

- activate the environment: 

```shell
conda activate grobidDelft
```

**NOTE**: make sure you are using the pip inside the environment (sometimes conda uses the root pip and mess up with your system): `which pip` should return a path within your environment. The conda creation command should already ensure that.  

Follow the steps described above, having the conda environment activated. Do not indicate a virtualEnv path in the GROBID properties file and launch the GROBID service with the conda environment activated. For conda too, it is preferable to install JEP globally as indicated. 

## Configuration

GROBID allows running models in mixed mode, i.e. to use DeLFT Deep Learning architectures for certain models, and Wapiti CRF for others. It is advised to select the preferred engine for each individual models in the GROBID properties file. 

For example, selecting Wapiti CRF as main engine: 

```properties
grobid.crf.engine=crf
```

By default, all the models will then use Wapiti CRF as sequence labeling implementation. The following models (citation) will run with DeLFT `BidLSTM-CRF` by adding: 

```properties
grobid.crf.engine.citation=delft
grobid.delft.architecture=BidLSTM_CRF
```

In contrast, selecting now DeLFT as main default engine: 

```properties
grobid.crf.engine=delft
```

The following models (segmentation, fulltext and reference-segmenter) will run with Wapiti CRF by adding: 

```properties
grobid.crf.engine.segmentation=wapiti
grobid.crf.engine.fulltext=wapiti
grobid.crf.engine.reference_segmenter=wapiti
```  

**NOTE**: use the underscore for models whose name contains hyphens.


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
