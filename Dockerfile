FROM jetty:9.3-jre8

ADD ./grobid-home/target/grobid-home-0.4.1-SNAPSHOT.zip /opt
RUN unzip /opt/grobid-home-0.4.1-SNAPSHOT.zip -d /opt && rm /opt/grobid-home-0.4.1-SNAPSHOT.zip

RUN apt-get update && apt-get -y --no-install-recommends install libxml2

COPY ./grobid-service/target/grobid-service-0.4.1-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war
