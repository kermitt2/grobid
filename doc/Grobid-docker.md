<h1>GROBID and containers (Docker)</h1>

**NOTE**: the support to Docker is still experimental.  

Docker is an open-source project that automates the deployment of applications inside software containers. 
The documentation on how to install it and start using it can be found [here](https://docs.docker.com/engine/understanding-docker/). 

GROBID can be instantiated and run using Docker. The image information can be found [here](https://hub.docker.com/r/lfoppiano/grobid/).

The process for fetching and running the image is (assuming docker is installed and working):

- Pull the image from docker HUB
```bash
> docker pull lfoppiano/grobid:${latest_grobid_version}
```
 
- Run the container (note the new version running on 8070, however it will be mapped on the 8080 of your host):

```bash
> docker run -t --rm --init -p 8080:8070 -p 8081:8071 lfoppiano/grobid:${latest_grobid_version}
```

(alternatively you can also get the image ID)  
```bash
> docker images | grep lfoppiano/grobid | grep ${latest_grobid_version}
> docker run -t --rm --init -p 8080:8070 -p 8081:8071 $image_id_from_previous_command
```

- Access the service: 
  - open the browser at the address `http://localhost:8080`
  - the health check will be accessible at the address `http://localhost:8081`


<h4>Troubleshooting</h4>

<h5>Out of memory or container being killed while processing</h5>

This might be due to insufficient memory on the docker machine. 
Depending on the intended usage, we recommend to allocate 4Gb extract all the PDF structures. Else 2Gb are sufficient to extract only header information, and 3Gb for citations.    

The memory can be verified directly using the docker desktop application or via CLI:  

```
> docker-machine inspect
```

You should see something like: 

```
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
        "Memory": 2048,     #<---- Memory: 2Gb                   
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

For more information see the [GROBID main page](https://github.com/kermitt2/grobid/blob/master/Readme.md).

<h5>pdfalto zombie processes</h5>
~~When running docker without an init process, the pdfalto processes will be hang as zombie eventually filling 
up the machine. The docker solution is to use `--init` as parameter when running the image, however we are discussing 
some more long-term solution compatible with Kubernetes for example.~~
The solution shipped with the current Dockerfile, using tini (https://github.com/krallin/tini) should provide the correct init process to cleanup 
killed processes. 
 

<h4>Build caveat</h4>
**NOTE**: The following part is only for development purposes. We recommend you to use the official 
docker images from the docker HUB.

The docker build from a particular version (here for example the latest stable version `0.5.6`) will clone the repository using git, so no need to custom builds. 
Only important information is the version which will be checked out from the tags.
 
```bash
> docker build -t grobid/grobid:0.5.6 --build-arg GROBID_VERSION=0.5.6 .
```

Similarly, if you want to create a docker image from the current master, development version:

```bash
> docker build -t grobid/grobid:0.6.0-SNAPSHOT --build-arg GROBID_VERSION=0.6.0-SNAPSHOT .
```

In order to run the container of the newly created image for version `0.5.6`: 
```bash
> docker run -t --rm --init -p 8080:8070 -p 8081:8071 grobid/grobid:0.5.6
```

For testing or debugging purposes, you can connect to the container with a bash shell:
```bash
> docker exec -i -t {container_name} /bin/bash
```
