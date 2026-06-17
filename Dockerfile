FROM eclipse-temurin:25.0.2_10-jre-alpine-3.22

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app.jar
RUN chown appuser:appgroup /app.jar

ENV TZ="Europe/Madrid"
USER appuser
EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
