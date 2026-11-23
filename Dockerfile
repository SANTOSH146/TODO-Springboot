# ---- Build stage: compile and package the jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Download dependencies first so this layer is cached when only source code changes
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Tests run separately in the CI pipeline, so skip them here
RUN mvn -B -q package -DskipTests

# ---- Runtime stage: small JRE image, non-root user ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/data && chown -R app:app /app

COPY --from=build --chown=app:app /build/target/todo-app-*.jar app.jar
USER app

EXPOSE 8080
# Let the JVM size its heap from the container memory limit
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]