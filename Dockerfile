# Build stage
FROM docker.io/gradle:8.11-jdk21 AS build
WORKDIR /app

# Copy everything
COPY . .

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
