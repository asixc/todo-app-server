FROM eclipse-temurin:25.0.2_10-jre-alpine-3.22
# FROM eclipse-temurin:23.0.1_11-jre-alpine
# FROM eclipse-temurin:21.0.5_11-jre-alpine-3.21
VOLUME /tmp
COPY ./target/todo-app-0.2.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
ENV TZ="Europe/Madrid"