## Docker GROBID image

# > docker build -t lfoppiano/grobid:GROBID_VERSION --build-arg GROBID_VERSION=GROBID_VERSION .
# Example: > docker build -t lfoppiano/grobid:1.0.0 --build-arg GROBID_VERSION=1.0.0 .

# > docker run -t --rm -p 8080:8070 -p 8081:8071 {image_name}

# To connect to the container with a bash shell
# > docker exec -i -t {container_name} /bin/bash

# -------------------
# build builder image
# -------------------
FROM openjdk:8u212-jdk as builder

USER root

RUN apt-get update && \
    apt-get -y --no-install-recommends install libxml2

WORKDIR /opt/grobid-source

RUN mkdir -p .gradle
VOLUME /opt/grobid-source/.gradle

# gradle
COPY gradle/ ./gradle/
COPY gradlew ./
COPY gradle.properties ./
COPY build.gradle ./
COPY settings.gradle ./

# source
COPY grobid-home/ ./grobid-home/
COPY grobid-core/ ./grobid-core/
COPY grobid-service/ ./grobid-service/
COPY grobid-trainer/ ./grobid-trainer/

RUN ./gradlew clean assemble --no-daemon  --info --stacktrace


# -------------------
# build runtime image
# -------------------
FROM openjdk:8u212-jre-slim

RUN apt-get update && \
    apt-get -y --no-install-recommends install libxml2 unzip

WORKDIR /opt

COPY --from=builder /opt/grobid-source/grobid-core/build/libs/grobid-core-*-onejar.jar ./grobid/grobid-core-onejar.jar
COPY --from=builder /opt/grobid-source/grobid-service/build/distributions/grobid-service-*.zip ./grobid-service.zip
COPY --from=builder /opt/grobid-source/grobid-home/build/distributions/grobid-home-*.zip ./grobid-home.zip

RUN unzip -o ./grobid-service.zip -d ./grobid && \
    mv ./grobid/grobid-service-* ./grobid/grobid-service

RUN unzip ./grobid-home.zip -d ./grobid && \
    mkdir -p /opt/grobid/grobid-home/tmp

RUN rm *.zip

# below to allow logs to be written in the container
# RUN mkdir -p logs

VOLUME ["/opt/grobid/grobid-home/tmp"]

WORKDIR /opt/grobid

ENV JAVA_OPTS=-Xmx4g

# Add Tini
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "-s", "--"]

CMD ["./grobid-service/bin/grobid-service", "server", "grobid-service/config/config.yaml"]

ARG GROBID_VERSION

LABEL \
    authors="Luca Foppiano <luca.foppiano@inria.fr>, Patrice Lopez <patrice.lopez@science-miner.org>" \
    org.label-schema.name="Grobid" \
    org.label-schema.description="Image with GROBID service" \
    org.label-schema.url="https://github.com/kermitt2/grobid" \
    org.label-schema.version=${GROBID_VERSION}

## Docker tricks:

# - remove all stopped containers
# > docker rm $(docker ps -a -q)

# - remove all unused images
# > docker rmi $(docker images --filter "dangling=true" -q --no-trunc)

# - remove all untagged images
# > docker rmi $(docker images | grep "^<none>" | awk "{print $3}")

# - "Cannot connect to the Docker daemon. Is the docker daemon running on this host?"
# > docker-machine restart

RUN chmod -R 755 /opt/grobid/grobid-home/pdf2xml 
RUN chmod 777 /opt/grobid/grobid-home/tmp
