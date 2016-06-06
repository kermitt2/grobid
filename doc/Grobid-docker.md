<h1>GROBID and containers (Docker)</h1>

**NOTE**: the support to Docker is still experimental. 

Docker is an open-source project that automates the deployment of applications inside software containers. 
GROBID can be instantiated and run using Docker. 
The image can be obtained from the repository: `https://hub.docker.com/r/lfoppiano/grobid/`

The process for fetching and running the image is:
 
`docker pull lfoppiano-grobid`
 
then

`docker images` 

after that, take the iamge id and run 

`docker run -t --rm -p 8080:8080 $image_id`


To access the service, first get the ip address of the container, 

`docker-machine ip default`

and then you can use the browser at the address http://{machine_id}:8080


