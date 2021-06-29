## Docker GROBID image

## Docker GROBID image using CRF models only

## See https://grobid.readthedocs.io/en/latest/Grobid-docker/

## docker build -t grobid/grobid:GROBID_VERSION --build-arg GROBID_VERSION=GROBID_VERSION .
## docker run -t --rm -p 8080:8070 -p 8081:8071 {image_name}

# To connect to the container with a bash shell
# > docker exec -i -t {container_name} /bin/bash

# -------------------
# build builder image
# -------------------
FROM openjdk:8u212-jdk as builder

USER root

RUN apt-get update && \
    apt-get -y --no-install-recommends install unzip

WORKDIR /opt/grobid-source

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

# cleaning unused native libraries before packaging
RUN rm -rf grobid-home/pdf2xml
RUN rm -rf grobid-home/pdfalto/lin-32
RUN rm -rf grobid-home/pdfalto/mac-64
RUN rm -rf grobid-home/pdfalto/win-*
RUN rm -rf grobid-home/lib/lin-32
RUN rm -rf grobid-home/lib/win-*
RUN rm -rf grobid-home/lib/mac-64

# cleaning Delft models
RUN rm -rf grobid-home/models/*-BidLSTM_CRF*

RUN ./gradlew clean assemble --no-daemon  --info --stacktrace

WORKDIR /opt/grobid
RUN unzip -o /opt/grobid-source/grobid-service/build/distributions/grobid-service-*.zip && \
    mv grobid-service* grobid-service
RUN unzip -o /opt/grobid-source/grobid-home/build/distributions/grobid-home-*.zip && \
    chmod -R 755 /opt/grobid/grobid-home/pdfalto
RUN rm -rf grobid-source

# -------------------
# build runtime image
# -------------------
FROM openjdk:11-jre-slim

RUN apt-get update && \
    apt-get -y --no-install-recommends install libxml2 libfontconfig && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /opt/grobid

COPY --from=builder /opt/grobid .

# Add Tini
ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "-s", "--"]
CMD ["./grobid-service/bin/grobid-service"]

ARG GROBID_VERSION

LABEL \
    authors="The contributors" \
    org.label-schema.name="GROBID" \
    org.label-schema.description="Image with GROBID service" \
    org.label-schema.url="https://github.com/kermitt2/grobid" \
    org.label-schema.version=${GROBID_VERSION}
