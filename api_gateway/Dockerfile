# Build from the repository root:  docker build -f api_gateway/Dockerfile -t sagar/webgenai-api-gateway .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY common-lib common-lib
RUN mvn -q -B -f common-lib/pom.xml install -DskipTests
COPY api_gateway api_gateway
RUN mvn -q -B -f api_gateway/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/api_gateway/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
