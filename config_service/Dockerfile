# Build from the repository root:  docker build -f config_service/Dockerfile -t sagar/webgenai-config-service .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY common-lib common-lib
RUN mvn -q -B -f common-lib/pom.xml install -DskipTests
COPY config_service config_service
RUN mvn -q -B -f config_service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/config_service/target/config-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
