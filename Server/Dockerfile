# syntax = docker/dockerfile:1.2

FROM maven:3.8.5-openjdk-8-slim AS build
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=mvn_settings mvn -s /run/secrets/mvn_settings clean package

FROM openjdk:8-jre-slim
WORKDIR /app
ARG WDIR .
COPY --from=build /app/target/Server-1.2-SNAPSHOT.jar /app/Server-1.2-SNAPSHOT.jar
COPY $WDIR/newmodel.zip .
COPY $WDIR/cifar-10 ./cifar-10
EXPOSE 4602:4602
ENTRYPOINT ["/usr/local/openjdk-8/bin/java", "-jar", "Server-1.2-SNAPSHOT.jar"]