<h1>GROBID and containers (Docker)</h1>

**NOTE**: the support to Docker is still experimental. 

Docker is an open-source project that automates the deployment of applications inside software containers. 
The documentation on how to install it and start using it can be found [here](https://docs.docker.com/engine/understanding-docker/). 

GROBID can be instantiated and run using Docker. The image information can be found [here](https://hub.docker.com/r/lfoppiano/grobid/).

The process for fetching and running the image is (assuming docker is installed and working):
 
```bash
> docker pull lfoppiano/grobid:0.1
```
 
then

```bash
> docker images
```

after that, take the iamge id and run 

```bash
> docker run -t --rm -p 8080:8080 $image_id
```


To access the service, first get the ip address of the container, 

```bash
> docker-machine ip default
```

and then you can use the browser at the address `http://{machine_id}:8080`


