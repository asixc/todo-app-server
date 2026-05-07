FROM eclipse-temurin:25.0.2_10-jre-alpine-3.22

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

VOLUME /tmp
COPY ./target/todo-app-0.2.6.jar app.jar
RUN chown appuser:appgroup /app.jar

ENV TZ="Europe/Madrid"
USER appuser

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
