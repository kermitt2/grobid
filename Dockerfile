## Docker GROBID image
# Reminder: Before building the docker GROBID image, remember to build grobid using the profile docker, in
# order to correctly set up the grobid-home in the web.xml.

# > mvn clean install -P docker

# > docker build -t lfoppiano/grobid:0.4.1-SNAPSHOT .
# > docker run -t --rm -p 8080:8080 {image_name}

# To connect to the container with a bash shell
# > docker exec -i -t {container_name} /bin/bash

FROM jetty:9.3-jre8

MAINTAINER Luca Foppiano <luca.foppiano@inria.fr>, Patrice Lopez <patrice.lopez@science-miner.org>
LABEL Description="This image is used to generate a GROBID image" Version="0.4.1-SNAPSHOT"

ADD ./grobid-home/target/grobid-home-0.4.1-SNAPSHOT.zip /opt
RUN unzip /opt/grobid-home-0.4.1-SNAPSHOT.zip -d /opt && rm /opt/grobid-home-0.4.1-SNAPSHOT.zip

RUN apt-get update && apt-get -y --no-install-recommends install libxml2

COPY ./grobid-service/target/grobid-service-0.4.1-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war


## Docker tricks:

# - remove all stopped containers
# > docker rm $(docker ps -a -q)

# - remove all untagged images
# > docker rmi $(docker images | grep "^<none>" | awk "{print $3}")