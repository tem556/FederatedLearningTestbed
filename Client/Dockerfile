FROM gradle:7.4.2-jdk8 AS build
COPY  . /home/gradle/src
WORKDIR /home/gradle/src
ARG settings=./settings.xml
RUN mkdir ~/.m2
COPY $settings ~/.m2/settings.xml
RUN gradle build 

FROM openjdk:8-jre-slim
ARG host=localhost
ARG port=4602
ARG rounds=3
ARG port=4602
ARG workdir=.
ARG datasetratio=1.0
ARG minClients=1
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/Client-1.0-SNAPSHOT.jar

ENTRYPOINT java -jar Client-1.0-SNAPSHOT.jar --native --fl --workdir $workdir  --nclients 1 --host $host --port $port