# Multi-stage Docker build for enterprise-grade performance
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY shared/pom.xml shared/
COPY inventory-service/pom.xml inventory-service/

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY shared/src shared/src
COPY inventory-service/src inventory-service/src

# Build application
RUN ./mvnw clean package -DskipTests -B

# Production image
FROM openjdk:17-jre-slim

# Create non-root user for security
RUN groupadd -r inventoryuser && useradd -r -g inventoryuser inventoryuser

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built JAR
COPY --from=builder /app/inventory-service/target/*.jar inventory-service.jar

# Change ownership to non-root user
RUN chown inventoryuser:inventoryuser /app/inventory-service.jar

# Switch to non-root user
USER inventoryuser

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar inventory-service.jar"]
