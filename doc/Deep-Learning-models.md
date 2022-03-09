# Using Deep Learning models instead of default CRF

## Integration with DeLFT

Since version `0.5.4` (2018), it is possible to use in GROBID recent Deep Learning sequence labelling models trained with [DeLFT](https://github.com/kermitt2/delft). The available neural models include in particular BidLSTM-CRF with Glove embeddings, with additional feature channel (for layout features), with ELMo, and BERT fine-tuned architectures with CRF activation layer (e.g. SciBERT-CRF), which can be used as alternative to the default Wapiti CRF.

These architectures have been tested on Linux 64bit and macOS.   

Integration is realized via Java Embedded Python [JEP](https://github.com/ninia/jep), which uses a JNI of CPython. This integration is two times faster than the Tensorflow Java API and significantly faster than RPC serving (see [here](https://www.slideshare.net/FlinkForward/flink-forward-berlin-2017-dongwon-kim-predictive-maintenance-with-apache-flink), and it does not require to modify DeLFT as it would be the case with Py4J gateway (socket-based).

There are no neural model for the segmentation and the fulltext models, because the input sequences for these models are too large for the current supported Deep Learning architectures. The problem would need to be formulated differently for these tasks or to use alternative DL architectures (with sliding window, etc.).

Low level models not using layout features (author name, dates, affiliations...) perform better than CRF. When layout features are involved, neural models with an additional feature channel should be preferred (e.g. `BidLSTM_CRF_FEATURES` in DeLFT), or they will perform significantly worse than Wapiti CRF.

See some evaluations under `grobid-trainer/docs`.

Current neural models can be up to 100 time slower than CRF, depending on the architecture. However when sequences can be processed in batch (e.g. for the citation model), overall runtime remains good with clear accuracy gain. This is where the possibility to mix CRF and Deep Learning models for different structuring tasks is very useful, as it permits to adjust the balance between possible accuracy and scalability in a fine-grained manner. 

### Getting started with Deep Learning

Using Deep Learning model in GROBID with a normal installation/build is not straightforward at the present time, due to the required availability of various native libraries and to the Python dynamic linking and packaging mess, which leads to force some strict version and system dependencies. Interfacing natively to a particular Python virtual environment (which is "sesssion-based") is challenging. We are exploring different approach to facilitate this and get a "out-of-the-out" working system. 

The most simple solution is to use the ["full" GROBID docker image](Grobid-docker.md), which allows to use Deep Learning models without further installation and which provides automatic GPU support. 

However if you need a "local" library installation and build, here are the step-by-step instructions to get a working Deep Learning GROBID.

#### Classic python and Virtualenv

<span>0.</span> Install GROBID as indicated [here](https://grobid.readthedocs.io/en/latest/Install-Grobid/).

You __must__ use a Java version under or equals to Java 11. At the present time, JVM 1.12 to 1.17 will fail to load the native JEP library (due to additional security constraints).

<span>1.</span> install [DeLFT](https://github.com/kermitt2/delft), see instructions [here](https://github.com/kermitt2/delft#install).
DeLFT version `0.3.0` has been tested successfully with Python 3.7 and 3.8. For GPU support, CUDA >-11.0 must be installed. 

<span>2.</span> Test your DeLFT installation for GROBID models: 

```shell
cd deflt/
python3 grobidTagger.py citation tag 
```

If it works (you see some annotations in JSON format), you are sure to have a working DeLFT environment for **all** GROBID models. The next steps address the native bridge between DeLFT and the JVM running GROBID. 

<span>3.</span> Configure your GROBID config file. 

Indicate the path to the DeLFT install in the GROBID config file `grobid.yaml` (`grobid-home/config/grobid.yaml`), 

```yaml
  delft:
    install: "../delft"
```

Indicate the GROBID model that should use a Deep Learning implementation in the same config file, for instance if you wish to use a Deep Learning model for the citation model (it provides 1-2 additional points to the f-score for bibliographical reference recognition) use:

```yaml
  models:
    - name: "citation"
      engine: "delft"
      architecture: "BidLSTM_CRF_FEATURES"
```

The default Deep Learning architecture is `BidLSTM_CRF`, which is the best sequence labelling RNN architecture (basically a slightly revised version of [(Lample et al., 2016)](https://arxiv.org/abs/1603.01360) with Glove embeddings). However for GROBID, an architecture also exploiting features (in particular layout features, which are not captured at all by the pretrained language models) gives usually better results and the prefered choise is `BidLSTM_CRF_FEATURES`. If you wish to use another architecture, you need to specify it in the same config file. 

For instance to use a model integrating a fine-tuned transformer, you can select a `BERT_CRF` fine-tuned model (basically the transformer layers with CRF as final activation layer) and indicate in the field `transformer` the name of the transformer model in the [Hugging Face transformers Hub](https://huggingface.co/models) to be use to instanciate the transformer layer, typically [allenai/scibert_scivocab_cased](https://huggingface.co/allenai/scibert_scivocab_cased) for `SciBERT` in the case of scientific articles:

```yaml
  models:
    - name: "citation"
      engine: "delft"
      architecture: "BERT_CRF"
      transformer: "allenai/scibert_scivocab_cased"
```

However, it will work only if the model is available under `grobid-home/models/`, currently only the `BidLSTM_CRF` and `BidLSTM_CRF_FEATURES` models are shipped with GROBID, given the size of BERT transformer models (400MB). To use a different architecture, you will thus need to train the new architecture model first with DeLFT and copy all the model files under the specific model subdirectory `grobid-home/models/**model**/`. 

If you are using a Python environment for the DeLFT installation, you can set the environment path in the config file as well:

```yaml
  delft:
    python_virtualEnv: /where/my/damned/python/virtualenv/is/
```

Normally by setting the Python environment path in the config file, you will not need to launch GROBID in the same activated environment. 

<span>4.</span> Install [JEP](https://github.com/ninia/jep) manually and preferably globally (outside a virtual env. and not under `~/.local/lib/python3.*/site-packages/`):

```shell
git clone https://github.com/ninia/jep
cd jep
sudo -E python3 setup.py build install
```

the `sudo -E` should ensure that JEP is installed globally and that the right JVM version is used (`-E` indicates to preserve the environment variables, in particular the `JAVA_HOME`). Installing JEP globally is the only safe way we found to be sure that JEP will work correctly in the JVM.

(here we are unfortunately touching the limit of the messy Python package management system, an install of JEP in a virtualenv should isolate the library depending on the pip/python version, but the JVM might not be able to found and linked these local/isolated libraries, even when the JVM is launched in the virtual env. A global install of JEP should however always work. pip in a virtual env still uses global python libraries when installed, but when pip uses user-level local libraries (e.g. libraries installed under `~/.local/lib/python3.8/site-packages/`) in the virtual env, we did not find a reliable way to make JEP working in the JVM.)

Copy the built JEP library to the `grobid-home/lib/` area - for instance on a Linux 64 machine with Python 3.8:

```shell
lopez@work:~/jep$ cp build/lib.linux-x86_64-3.8/jep/jep.cpython-38-x86_64-linux-gnu.so ~/grobid/grobid-home/lib/lin-64/libjep.so
```

This will ensure that the GROBID native JEP library matches the JEP version of the system/environment (i.e. same OS and the same python version) and the JEP jar version (GROBID contains a default `libjep.so` for python3.8 and JEP `4.0.2`, it will not work with other version of python and other version of JEP).

Also note that the installed version of JEP must be the same as given in `grobid/build.gradle`. The python version used in the system or environment must match the native JEP library. At the time this documentation is written, we are using JEP version `4.0.2`:

```
implementation 'black.ninia:jep:4.0.2'
```

So if another version of JEP is installed globally on the system, you should update the GROBID `build.gradle` to match this version.

<span>5.</span> Run GROBID, this is the "*but on my machine it works*" moment: 

To run the grobid service, under `grobid/`:

```shell
./gradlew run
```

In case you have installed DeLFT with `virtualenv` and you have not indicated the virtual environment in the GROBID config file, you should start GROBID after having activated the `virtualenv`. 

When running the GROBID service on a first input, you should see in the logs that the expected Deep Learning models are loaded and the JEP threads are activated:

```
INFO  [2020-10-30 23:04:07,756] org.grobid.core.jni.DeLFTModel: Loading DeLFT model for citation with architecture BidLSTM_CRF...
INFO  [2020-10-30 23:04:07,758] org.grobid.core.jni.JEPThreadPool: Creating JEP instance for thread 44
```

It is then possible to [benchmark end-to-end](https://grobid.readthedocs.io/en/latest/End-to-end-evaluation/) the selected Deep Learning models as any usual GROBID benchmarking exercise. In practice the CRF models should be mixed with Deep Learning models to keep the process reasonably fast and memory-hungry. In addition, note that, currently, due to the limited amount of training data, Deep Learning models perform significantly better than CRF only for two models (`citation` and `affiliation-address`), so there is likely no practical interest to use Deep Learning for the other models. This will of course certainly change in the future! 


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

Follow the steps described above, having the conda environment activated. Do not indicate a virtualEnv path in the GROBID config file and launch the GROBID service with the conda environment activated. For conda too, it is preferable to install JEP globally as indicated above. 

## Configuration

GROBID allows running models in mixed mode, i.e. to use DeLFT Deep Learning architectures for certain models, and Wapiti CRF for others. It is advised to select the preferred engine for each individual models in the GROBID config file. 

By default, all the models are set to use Wapiti CRF as sequence labeling implementation. Each model has its own configuration block, where it is possible to select a particular model implementation, with its own parameters. 

The following models (citation) will run with DeLFT `BidLSTM-BidLSTM_CRF_FEATURES` by adding: 

```yaml
  models:
    - name: "citation"
      engine: "delft"
      architecture: "BidLSTM_CRF_FEATURES"
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
