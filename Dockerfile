FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B verify

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S gimnasio && adduser -S gimnasio -G gimnasio
WORKDIR /app
COPY --from=build --chown=gimnasio:gimnasio /workspace/target/Gimnasio_sara-0.0.1-SNAPSHOT.jar app.jar
USER gimnasio
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 CMD wget -q -T 8 -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
