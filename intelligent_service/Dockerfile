# Build from the repository root:  docker build -f intelligent_service/Dockerfile -t sagar/webgenai-intelligence-service .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY common-lib common-lib
RUN mvn -q -B -f common-lib/pom.xml install -DskipTests
COPY intelligent_service intelligent_service
RUN mvn -q -B -f intelligent_service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/intelligent_service/target/intelligence-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
