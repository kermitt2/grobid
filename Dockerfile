## Docker GROBID image
# Reminder: Before building the docker GROBID image, remember to build grobid using the profile docker, in
# order to correctly set up the grobid-home in the web.xml.

# > mvn clean install -P docker

# > docker build -t lfoppiano/grobid:GROBID_VERSION --build-arg GROBID_VERSION=GROBID_VERSION .
# Example: > docker build -t lfoppiano/grobid:1.0.0 --build-arg GROBID_VERSION=1.0.0 .

# > docker run -t --rm -p 8080:8080 {image_name}

# To find out the docker machine ip
# > docker-machine ip

# To connect to the container with a bash shell
# > docker exec -i -t {container_name} /bin/bash

FROM jetty:9.3-jre8

MAINTAINER Luca Foppiano <luca.foppiano@inria.fr>, Patrice Lopez <patrice.lopez@science-miner.org>

ARG GROBID_VERSION

LABEL Description="This image is used to generate a GROBID image" Version="${GROBID_VERSION}"

ENV JAVA_OPTS=-Xmx4g

RUN apt-get update && apt-get -y --no-install-recommends install libxml2

ADD ./grobid-home/target/grobid-home-${GROBID_VERSION}.zip /opt
RUN unzip /opt/grobid-home-${GROBID_VERSION}.zip -d /opt && rm /opt/grobid-home-${GROBID_VERSION}.zip

COPY ./grobid-service/target/grobid-service-${GROBID_VERSION}.war /var/lib/jetty/webapps/ROOT.war

# Workaround otherwise the tmp directory is not writeable (owner is root)
RUN mkdir /opt/grobid-home/tmp
RUN chown -R jetty:jetty /opt/grobid-home/
VOLUME /opt/grobid-home/tmp

## Docker tricks:

# - remove all stopped containers
# > docker rm $(docker ps -a -q)

# - remove all untagged images
# > docker rmi $(docker images | grep "^<none>" | awk "{print $3}")

# - "Cannot connect to the Docker daemon. Is the docker daemon running on this host?"
# > docker-machine restart