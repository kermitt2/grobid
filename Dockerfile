FROM openjdk:8-jdk as builder

ARG GROBID_VERSION=0.5.0

RUN cd /opt && \
    wget https://github.com/kermitt2/grobid/archive/${GROBID_VERSION}.zip && \
    unzip ${GROBID_VERSION}.zip && \
    mv /opt/grobid-${GROBID_VERSION} /opt/grobid && \
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

COPY --from=builder /opt/grobid /opt/grobid

RUN mkdir /opt/grobid/grobid-home/tmp && \
    apt-get update && \
    apt-get -y --no-install-recommends install \
        libxml2

VOLUME ["/opt/grobid/grobid-home/tmp", "/root/.gradle"]

WORKDIR /opt/grobid

CMD ["./gradlew", "run"]
