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

FROM openjdk:8-jdk as builder

ARG GROBID_VERSION=0.5.0

USER root
RUN apt-get update && \
    apt-get -y --no-install-recommends install libxml2 git

RUN cd /opt && \
    git clone https://github.com/kermitt2/grobid.git grobid-source && \
#    git checkout ${GROBID_VERSION} && \
    cd /opt/grobid && \
    ./gradlew clean install

FROM openjdk:8-jre-slim

ARG GROBID_VERSION=0.5.0

LABEL \
    org.label-schema.name="Grobid" \
    org.label-schema.description="Image with GROBID service" \
    org.label-schema.url="https://github.com/kermitt2/grobid/blob/master/README.md" \
    org.label-schema.version=${GROBID_VERSION}

ENV JAVA_OPTS=-Xmx4g

COPY --from=builder /opt/grobid-source/grobid-service/build/distributions/grobid-service-${GROBID_VERSION}.zip /opt
COPY --from=builder /opt/grobid-source/grobid-home/build/distributions/grobid-home-${GROBID_VERSION}.zip /opt

RUN unzip /opt/grobid-service-${GROBID_VERSION}.zip -d /opt/grobid && \
    mv /opt/grobid/grobid-service-${GROBID_VERSION} /opt/grobid/grobid-service

RUN unzip /opt/grobid-home-${GROBID_VERSION}.zip -d /opt/grobid &&
    mkdir /opt/grobid/grobid-home/tmp

RUN apt-get update && \
    apt-get -y --no-install-recommends install \
    libxml2

VOLUME ["/opt/grobid/grobid-home/tmp"]

WORKDIR /opt/grobid

CMD ["./grobid-service/bin/grobid-service", "server", "grobid-service/config/config.yaml"]
