<h1>GROBID and containers (Docker)</h1>

**NOTE**: the support to Docker is still experimental. 

Docker is an open-source project that automates the deployment of applications inside software containers. 
The documentation on how to install it and start using it can be found [here](https://docs.docker.com/engine/understanding-docker/). 

GROBID can be instantiated and run using Docker. The image information can be found [here](https://hub.docker.com/r/lfoppiano/grobid/).

The process for fetching and running the image is (assuming docker is installed and working):
 
```bash
> docker pull lfoppiano/grobid:0.4.1-SNAPSHOT
```
 
then

```bash
> docker run -t --rm -p 8080:8080 lfoppiano/grobid:0.4.1-SNAPSHOT
```

or
```bash
> docker images | grep lfoppiano/grobid | grep 0.4.1-SNAPSHOT
> docker run -t --rm -p 8080:8080 $image_id_from_previous_command
```

To access the service, first get the ip address of the container, 

```bash
> docker-machine ip default
```

and then you can use the browser at the address `http://{machine_id}:8080`

<h4>Build caveat</h4>
**NOTE**: The following part is only for development purposes. We recommend you to use the official 
docker images.
 
Reminder: Before building the docker GROBID image, remember to build grobid 
using the profile docker, in order to correctly set up the grobid-home in the web.xml.

```bash
> mvn clean install -P docker
```
```bash
> docker build -t lfoppiano/grobid:0.4.1-SNAPSHOT .
```

In order to run the container of the newly created image: 
```bash
> docker run -t --rm -p 8080:8080 lfoppiano/grobid:0.4.1-SNAPSHOT 
```

For testing or debugging purposes, you can connect to the container with a bash shell:
```bash
> docker exec -i -t {container_name} /bin/bash
```
