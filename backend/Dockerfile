# Build the Spring Boot application with the JDK/Maven toolchain.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

# Run only the packaged application in the runtime image.
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home shopit

COPY --from=build --chown=shopit:shopit /workspace/target/shopit-api-1.0.0.jar /app/shopit-api.jar

USER shopit
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/shopit-api.jar"]
