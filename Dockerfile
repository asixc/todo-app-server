FROM cgr.dev/chainguard/jdk:latest

VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY --chown=java:java ${JAR_FILE} /app.jar

ENV TZ="Europe/Madrid"
USER java
EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
