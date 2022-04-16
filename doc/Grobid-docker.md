# GROBID and Docker containers

Docker is an open-source project that automates the deployment of applications inside software containers. The documentation on how to install it and start using it can be found [here](https://docs.docker.com/engine/understanding-docker/).

GROBID can be instantiated and run using Docker. For convenience, we provide two docker images:

- a **lightweight image** with only CRF models: this image offers best performance in term of runtime and memory usage, as well as limiting the size of the image. The image information can be found [here](https://hub.docker.com/r/lfoppiano/grobid/).

- a **full image** able to run both CRF and Deep Learning models: this image includes all the required python and TensorFlow libraries, GPU support and all DL model resources. It can provide slighly more accurate results, but at the cost of much slower runtime and higher memory usage. The image is also considerably larger (python and tensorflow libraries taking more than 2GB and pre-loaded embeddings around 5GB).

We assume in the following that docker is installed and working on your system. Note that the default memory available for your container might need to be increased for using all the available GROBID services, in particular on `macos`, see the Troubleshooting section below.

## CRF-only image

The process for retrieving and running the image is as follow:

- Pull the image from docker HUB (Check the [latest version number](https://hub.docker.com/r/lfoppiano/grobid/tags)):

```bash
> docker pull lfoppiano/grobid:${latest_grobid_version}
```

- Run the container:

```bash
> docker run -t --rm --init -p 8070:8070 lfoppiano/grobid:${latest_grobid_version}
```

Latest verion:

```bash
> docker run -t --rm --init -p 8070:8070 lfoppiano/grobid:0.7.1
```

Note the default version is running on port `8070`, however it can be mapped on the more traditional port `8080` of your host with the following command:

```bash
> docker run -t --rm --init -p 8080:8070 -p 8081:8071 lfoppiano/grobid:${latest_grobid_version}
```

Access the service:
  - open the browser at the address `http://localhost:8080`
  - the health check will be accessible at the address `http://localhost:8081`

Grobid web services are then available as described in the [service documentation](https://grobid.readthedocs.io/en/latest/Grobid-service/).

## CRF and Deep Learning image

The process for retrieving and running the image is as follow:

- Pull the image from docker HUB (Check the [latest version number](https://hub.docker.com/r/grobid/grobid/tags)):

```bash
> docker pull grobid/grobid:${latest_grobid_version}
```

Current latest version:

```bash
> docker pull grobid/grobid:0.7.2-SNAPSHOT
```

- Run the container:

```bash
> docker run --rm --gpus all --init -p 8070:8070 grobid/grobid:${latest_grobid_version}
```

The image will automatically uses the GPU and CUDA version available on your host machine, but only on Linux. GPU usage via a container on Windows and MacOS machine is currently not supported by Docker. If no GPU are available, CPU will be used.  

To specify to use only certain GPUs (see the [nvidia container toolkit user guide](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/user-guide.html#gpu-enumeration) for more details):

```bash
> docker run --rm --gpus '"device=1,2"' --init -p 8070:8070 -p 8081:8071 grobid/grobid:${latest_grobid_version}
```

You can run the image on CPU by omitting the `-gpus` parameters. 

Note the default version is running on port `8070`, however it can be mapped on the more traditional port `8080` of your host with the following command:

```bash
> docker run -t --rm --gpus all --init -p 8080:8070 grobid/grobid:${latest_grobid_version}
```

Access the service:
  - open the browser at the address `http://localhost:8080`
  - the health check will be accessible at the address `http://localhost:8081`

Grobid web services are then available as described in the [service documentation](https://grobid.readthedocs.io/en/latest/Grobid-service/).

## Configure using the normal yaml config file

The simplest way to pass a modified configuration to the docker image is to mount the yaml GROBID config file `grobid.yaml` when running the image. Modify the config file `grobid/grobid-home/config/grobid.yaml` according to your requirements on the host machine and mount it when running the image as follow: 

```bash
docker run --rm --gpus all --init -p 8080:8070 -p 8081:8071 -v /home/lopez/grobid/grobid-home/config/grobid.yaml:/opt/grobid/grobid-home/config/grobid.yaml:ro  grobid/grobid:0.7.2-SNAPSHOT
```

You need to use an absolute path to specify your modified `grobid.yaml` file.

## Configuration using Environment Variables

This usage is currently not supported anymore, due to the number and the complexity of configuration parameters. Use the yaml configuration file to set production parameter to a docker image. 

## Troubleshooting

### Out of memory or container being killed while processing

This is usually be due to insufficient memory allocated to the docker machine. Depending on the intended usage, we recommend to allocate 4GB of RAM to structure entirely all the PDF content (`/api/processFulltextDocument`), otherwise 2GB are sufficient to extract only header information, and 3GB for citations. In case of more intensive usage and batch parallel processing, allocating 6 or 8GB is recommended.

On `macos`, see for instance [here](https://stackoverflow.com/questions/32834082/how-to-increase-docker-machine-memory-mac/39720010#39720010) on how to increase the RAM from the Docker UI.

The memory can be verified directly using the docker desktop application or via CLI:  

```bash
> docker-machine inspect
```

You should see something like:

```json
{
    "ConfigVersion": 3,
    "Driver": {
        "IPAddress": "192.168.99.100",
        "MachineName": "default",
        "SSHUser": "docker",
        "SSHPort": 55933,
        "SSHKeyPath": "/Users/lfoppiano/.docker/machine/machines/default/id_rsa",
        "StorePath": "/Users/lfoppiano/.docker/machine",
        "SwarmMaster": false,
        "SwarmHost": "tcp://0.0.0.0:3376",
        "SwarmDiscovery": "",
        "VBoxManager": {},
        "HostInterfaces": {},
        "CPU": 1,
        "Memory": 2048,     #<---- Memory: 2GB
        "DiskSize": 204800,
        "NatNicType": "82540EM",
        "Boot2DockerURL": "",
        "Boot2DockerImportVM": "",
        "HostDNSResolver": false,
        "HostOnlyCIDR": "192.168.99.1/24",
        "HostOnlyNicType": "82540EM",
        "HostOnlyPromiscMode": "deny",
        "NoShare": false,
        "DNSProxy": true,
        "NoVTXCheck": false
    },
    "DriverName": "virtualbox",
    "HostOptions": {
      [...]
        },
        "SwarmOptions": {
         [...]
        },
        "AuthOptions": {
           [...]
        }
    },
    "Name": "default"
}
```

See for instance [here](https://stackoverflow.com/a/36982696) for allocating to the Docker machine more than the default RAM on `macos` with command lines.

### pdfalto zombie processes

When running docker without an init process, the pdfalto processes will be hang as zombie eventually filling up the machine. The docker solution is to use `--init` as parameter when running the image. The solution shipped with the current Dockerfile, using [tini](https://github.com/krallin/tini) provides the correct init process to cleanup killed processes, but do not forget the `-init` parameter :)

## Building an image

The following part is normally for development purposes. You can use the official stable docker images from the docker HUB as described above.
However if you are interested in using the master version of Grobid in container, or a customized branch/fork, building a new image is the way to go.

### Building the CRF-only image

For building a CRF-only image, the dockerfile to be used is `./Dockerfile.crf`. The only important information then is the version which will be checked out from the tags.

```bash
> docker build -t grobid/grobid:0.7.1 --build-arg GROBID_VERSION=0.7.1 --file Dockerfile.crf .
```

Similarly, if you want to create a docker image from the current master, development version:

```bash
> docker build -t grobid/grobid:0.7.2-SNAPSHOT --build-arg GROBID_VERSION=0.7.2-SNAPSHOT --file Dockerfile.crf .
```

In order to run the container of the newly created image, for example for version `0.7.1`:

```bash
> docker run -t --rm --init -p 8080:8070 -p 8081:8071 grobid/grobid:0.7.1
```

For testing or debugging purposes, you can connect to the container with a bash shell (logs are under `/opt/grobid/logs/`):

```bash
> docker exec -i -t {container_name} /bin/bash
```

The container name is given by the command:

```bash
> docker container ls
```

### Building the CRF and Deep Learning image

In order to build an image supporting GPU, you need:

- to have the nvidia driver and CUDA properly installed on the machine you are using to build the image - this can be check by the usual `nvidia-smi` command

Without theis requirement, the image will default to CPU, even if GPU are available on the host machine running the image. 

For building a CRF-only image, the dockerfile to be used is `./Dockerfile.crf` (see previous section). For being able to use both CRF and Deep Learningmodels, use the dockerfile `./Dockerfile.delft`. The only important information then is the version which will be checked out from the tags.

```bash
> docker build -t grobid/grobid:0.7.1 --build-arg GROBID_VERSION=0.7.1 --file Dockerfile.delft .
```

Similarly, if you want to create a docker image from the current master, development version:

```bash
docker build -t grobid/grobid:0.7.2-SNAPSHOT --build-arg GROBID_VERSION=0.7.2-SNAPSHOT --file Dockerfile.delft .
```

In order to run the container of the newly created image, for example for the development version `0.7.2-SNAPSHOT`, using all GPU available:

```bash
> docker run --rm --gpus all --init -p 8080:8070 -p 8081:8071 grobid/grobid:0.7.2-SNAPSHOT
```

In practice, you need to indicate which models should use a Deep Learning model implementation and which ones can remain with a faster CRF model implementation, which is done currently in the `grobid.yaml` file. Modify the config file `grobid/grobid-home/config/grobid.yaml` accordingly on the host machine and mount it when running the image as follow: 

```bash
docker run --rm --gpus all --init -p 8080:8070 -p 8081:8071 -v /home/lopez/grobid/grobid-home/config/grobid.yaml:/opt/grobid/grobid-home/config/grobid.yaml:ro  grobid/grobid:0.7.2-SNAPSHOT
```

You need to use an absolute path to specify your modified `grobid.yaml` file.

For testing or debugging purposes, you can connect to the container with a bash shell (logs are under `/opt/grobid/logs/`):

```bash
> docker exec -i -t {container_name} /bin/bash
```

The container name is given by the command:

```bash
> docker container ls
```
