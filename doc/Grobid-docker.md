<h1>GROBID and containers (Docker)</h1>

**NOTE**: the support to Docker is still experimental.  

Docker is an open-source project that automates the deployment of applications inside software containers. 
The documentation on how to install it and start using it can be found [here](https://docs.docker.com/engine/understanding-docker/). 

GROBID can be instantiated and run using Docker. The image information can be found [here](https://hub.docker.com/r/lfoppiano/grobid/).

The process for fetching and running the image is (assuming docker is installed and working):

- Pull the image from docker HUB
```bash
> docker pull lfoppiano/grobid:0.4.2
```
 
- Run the container:

```bash
> docker run -t --rm -p 8080:8080 lfoppiano/grobid:0.4.2
```

(alternatively you can also get the image ID)  
```bash
> docker images | grep lfoppiano/grobid | grep 0.4.2
> docker run -t --rm -p 8080:8080 $image_id_from_previous_command
```

- Access the service: 
  - get the ip address of the container 

```bash
> docker-machine ip default
```
  - open the browser at the address `http://{machine_id}:8080`


<h4>Troubleshooting</h4>

<h5>Out of memory while processing</h5>

This might be due to insufficient memory on the docker machine. Make sure your machine has enough: 

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

<h4>Build caveat</h4>
**NOTE**: The following part is only for development purposes. We recommend you to use the official 
docker images.
 
Reminder: Before building the docker GROBID image, remember to build grobid 
using the profile docker, in order to correctly set up the grobid-home in the web.xml.

```bash
> mvn clean install -P docker
```

make sure the Dockerfile points to the right jars (TODO: add placeholders based on the version), launch the build: 

```bash
> docker build -t lfoppiano/grobid:0.4.2 --build-arg GROBID_VERSION=0.4.2-SNAPSHOT .
```

In order to run the container of the newly created image: 
```bash
> docker run -t --rm -p 8080:8080 lfoppiano/grobid:0.4.2 
```

For testing or debugging purposes, you can connect to the container with a bash shell:
```bash
> docker exec -i -t {container_name} /bin/bash
```
