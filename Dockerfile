## Docker GROBID image
FROM jetty:9.3-jre8

MAINTAINER Luca Foppiano <luca.foppiano@inria.fr>, Patrice Lopez <patrice.lopez@science-miner.org>
LABEL Description="This image is used to generate a GROBID image" Version="0.4.1-SNAPSHOT"

ADD ./grobid-home/target/grobid-home-0.4.1-SNAPSHOT.zip /opt
RUN unzip /opt/grobid-home-0.4.1-SNAPSHOT.zip -d /opt && rm /opt/grobid-home-0.4.1-SNAPSHOT.zip

RUN apt-get update && apt-get -y --no-install-recommends install libxml2

COPY ./grobid-service/target/grobid-service-0.4.1-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war