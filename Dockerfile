FROM eclipse-temurin:22.0.2_9-jre
VOLUME /tmp
COPY ./target/todoApp-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
ENV TZ="Europe/Madrid"