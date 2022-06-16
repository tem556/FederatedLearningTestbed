FROM gradle:7.4.2-jdk8 AS build
COPY  . /home/gradle/src
WORKDIR /home/gradle/src
ENV GITHUB_USERNAME=[GITHUB_USERNAME]
ENV GITHUB_TOKEN=[GITHUB_TOKEN]
RUN gradle build
RUN gradle uberJar


FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/Client-1.1-SNAPSHOT.jar
ENV PORT 4602
EXPOSE $PORT:$PORT
ENTRYPOINT ["/usr/local/openjdk-8/bin/java", "-jar", "Client-1.1-SNAPSHOT.jar"]
