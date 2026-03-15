# Build stage
FROM docker.io/gradle:8.11-jdk21 AS build
WORKDIR /app

# Copy gradle files first for dependency caching
COPY settings.gradle.kts build.gradle.kts* gradle.properties* ./
COPY kotlin/build.gradle.kts ./kotlin/
COPY web/build.gradle.kts ./web/

# Download dependencies (cached if gradle files don't change)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY kotlin/src ./kotlin/src
COPY web/src ./web/src

# Build the application
RUN gradle :web:installDist --no-daemon

# Runtime stage  
FROM docker.io/eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built application
COPY --from=build /app/web/build/install/web ./app

# Render sets PORT environment variable
ENV PORT=10000

EXPOSE $PORT

CMD ["./app/bin/web"]
