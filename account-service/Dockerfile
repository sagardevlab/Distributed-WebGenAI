# Build from the repository root:  docker build -f account-service/Dockerfile -t sagar/webgenai-account-service .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY common-lib common-lib
RUN mvn -q -B -f common-lib/pom.xml install -DskipTests
COPY account-service account-service
RUN mvn -q -B -f account-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/account-service/target/account-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
