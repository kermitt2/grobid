## Docker GROBID image

# > docker build -t lfoppiano/grobid:GROBID_VERSION --build-arg GROBID_VERSION=GROBID_VERSION .
# Example: > docker build -t lfoppiano/grobid:1.0.0 --build-arg GROBID_VERSION=1.0.0 .

# > docker run -t --rm -p 8080:8070 -p 8081:8071 {image_name}

# To connect to the container with a bash shell
# > docker exec -i -t {container_name} /bin/bash

FROM openjdk:8-jdk as builder

MAINTAINER Luca Foppiano <luca.foppiano@inria.fr>, Patrice Lopez <patrice.lopez@science-miner.org>

ARG GROBID_VERSION

USER root
RUN apt-get update && \
    apt-get -y --no-install-recommends install libxml2 git

RUN cd /opt && \
    git clone https://github.com/kermitt2/grobid.git grobid-source && \
    cd /opt/grobid-source && \
    git checkout ${GROBID_VERSION} && \
    ./gradlew clean assemble    

FROM openjdk:8-jre-slim

ARG GROBID_VERSION

MAINTAINER Luca Foppiano <luca.foppiano@inria.fr>, Patrice Lopez <patrice.lopez@science-miner.org>

LABEL \
    org.label-schema.name="Grobid" \
    org.label-schema.description="Image with GROBID service" \
    org.label-schema.url="https://github.com/kermitt2/grobid" \
    org.label-schema.version=${GROBID_VERSION}

ENV JAVA_OPTS=-Xmx4g

COPY --from=builder /opt/grobid-source/grobid-service/build/distributions/grobid-service-${GROBID_VERSION}.zip /opt
COPY --from=builder /opt/grobid-source/grobid-home/build/distributions/grobid-home-${GROBID_VERSION}.zip /opt


RUN unzip -o /opt/grobid-service-${GROBID_VERSION}.zip -d /opt/grobid && \
    mv /opt/grobid/grobid-service-${GROBID_VERSION} /opt/grobid/grobid-service

RUN unzip /opt/grobid-home-${GROBID_VERSION}.zip -d /opt/grobid && \
    mkdir -p /opt/grobid/grobid-home/tmp

# Workaround for version 0.5.1
#RUN mkdir /opt/grobid/grobid-service/config
#ADD ./grobid-service/config /opt/grobid/grobid-service/config

RUN apt-get update && \
    apt-get -y --no-install-recommends install \
    libxml2

VOLUME ["/opt/grobid/grobid-home/tmp"]

WORKDIR /opt/grobid

CMD ["./grobid-service/bin/grobid-service", "server", "grobid-service/config/config.yaml"]

## Docker tricks:

# - remove all stopped containers
# > docker rm $(docker ps -a -q)

# - remove all unused images
# > docker rmi $(docker images --filter "dangling=true" -q --no-trunc)

# - remove all untagged images
# > docker rmi $(docker images | grep "^<none>" | awk "{print $3}")

# - "Cannot connect to the Docker daemon. Is the docker daemon running on this host?"
# > docker-machine restart