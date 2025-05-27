<h1>Run GROBID</h1>>

The standard way to run Grobid is to use [Docker](https://docs.docker.com/engine/understanding-docker/) for starting a Grobid server. 

For installing Docker on your system, please visit the official Docker documentation [here](https://docs.docker.com/get-docker/).

For convenience, we provide two docker images:

- the **full** image provides the best accuracy, because it includes all the required python and TensorFlow libraries, GPU support and all Deep Learning model resources. However it requires more resources, ideally a GPU (it will be automatically detected on Linux). If you have a limited amount of PDF, a good machine, and prioritize accuracy, use this Grobid flavor. To run this version of Grobid, the command is: 

```console
docker run --rm --gpus all --init --ulimit core=0 -p 8070:8070 grobid/grobid:0.8.2
```

- the **lightweight** image offers best runtime performance, memory usage and Docker image size. However, it does not use some of the best performing models in term of accuracy. If you have a lot of PDF to process, a low resource system, and accuracy is not so important, use this flavor:

```console
docker run --rm --init --ulimit core=0 -p 8070:8070 lfoppiano/grobid:0.8.2
```

More documentation on the Docker images can be found [here](Grobid-docker.md).

From there, you can check on your browser if the service works fine by accessing the welcome page of the service console, available at the URL <http://localhost:8070>. The GROBID server can be used via the [web service](Grobid-service.md). 

