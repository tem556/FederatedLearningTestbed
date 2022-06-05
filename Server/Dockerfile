# syntax = docker/dockerfile:1.2

FROM maven:3.8.5-openjdk-8-slim AS build
WORKDIR /app
COPY . .
RUN --mount=type=secret,id=mvn_settings mvn -s /run/secrets/mvn_settings clean package

FROM openjdk:8-jre-slim
ARG rounds=3
ARG port=4602
ARG workdir
ARG datasetratio=1.0
ARG minClients=1
WORKDIR /app
COPY --from=build /app/target/Server-1.0-SNAPSHOT.jar .
ENTRYPOINT java -jar Server-1.0-SNAPSHOT.jar --fl --datasetratio $datasetratio --minClients $minClients --workdir $workdir --rounds $rounds
