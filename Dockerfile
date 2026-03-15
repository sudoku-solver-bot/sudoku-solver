# Build stage
FROM gradle:8.11-jdk21-alpine AS build
WORKDIR /app

# Copy gradle files first for better caching
COPY settings.gradle.kts build.gradle.kts* gradle.properties* ./
COPY kotlin/build.gradle.kts ./kotlin/
COPY web/build.gradle.kts ./web/
COPY gradle ./gradle
COPY gradlew ./

# Copy source code
COPY kotlin/src ./kotlin/src
COPY web/src ./web/src

# Build the application
RUN ./gradlew :web:installDist --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built application
COPY --from=build /app/web/build/install/web ./app
RUN chown -R appuser:appgroup /app

USER appuser

# Render sets PORT environment variable
ENV PORT=10000

EXPOSE $PORT

ENTRYPOINT ["./app/bin/web"]
