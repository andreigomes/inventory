# Multi-stage Docker build for API Gateway
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY shared/pom.xml shared/
COPY api-gateway/pom.xml api-gateway/

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY shared/src shared/src
COPY api-gateway/src api-gateway/src

# Build application
RUN ./mvnw clean package -DskipTests -B

# Production image
FROM openjdk:17-jre-slim

# Create non-root user for security
RUN groupadd -r gatewayuser && useradd -r -g gatewayuser gatewayuser

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built JAR
COPY --from=builder /app/api-gateway/target/*.jar api-gateway.jar

# Change ownership to non-root user
RUN chown gatewayuser:gatewayuser /app/api-gateway.jar

# Switch to non-root user
USER gatewayuser

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8000/actuator/health || exit 1

EXPOSE 8000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar api-gateway.jar"]
