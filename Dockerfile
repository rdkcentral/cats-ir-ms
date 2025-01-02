FROM --platform=$BUILDPLATFORM amazoncorretto:17-alpine3.20
ENV ARTIFACTID ir-ms

ADD target/$ARTIFACTID.jar /data/$ARTIFACTID.jar

RUN mkdir /irms
VOLUME /irms

ADD /irms/ir-ms.yml /irms/$ARTIFACTID.yml

CMD java -jar /data/$ARTIFACTID.jar server /irms/$ARTIFACTID.yml

EXPOSE 9090 9091