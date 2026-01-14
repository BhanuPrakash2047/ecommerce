# ============================================
# MULTI-STAGE DOCKER BUILD FOR SPRING BOOT
# ============================================

# ============================================
# STAGE 1: BUILD STAGE
# ============================================
# Purpose: Compile the application and create the JAR file
# We use a full JDK image here because we need Maven to build

FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
# Docker caches layers - if pom.xml doesn't change, dependencies won't be re-downloaded
COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (this layer is cached if pom.xml doesn't change)
# -DskipTests: Skip tests during dependency resolution
# -B: Run in batch mode (non-interactive)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
# -DskipTests: Skip tests for faster build (run tests in CI/CD separately)
# package: Create the JAR file
RUN ./mvnw clean package -DskipTests -B

# ============================================
# STAGE 2: RUNTIME STAGE
# ============================================
# Purpose: Run the application with minimal image size
# We use JRE (not JDK) because we only need to RUN, not compile

FROM eclipse-temurin:17-jre-alpine AS runtime

# Add labels for better container management
LABEL maintainer="your-email@example.com"
LABEL application="snack-ecommerce"
LABEL version="0.0.1-SNAPSHOT"

# Create a non-root user for security
# Running as root inside containers is a security risk
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy ONLY the JAR file from the builder stage
# This is the key benefit of multi-stage: final image doesn't have source code, Maven, etc.
COPY --from=builder /app/target/snack-ecommerce-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose the application port
EXPOSE 8080


# JVM tuning for containers
# -XX:+UseContainerSupport: Use container memory limits
# -XX:MaxRAMPercentage=75.0: Use 75% of container memory for heap
# -Djava.security.egd: Faster random number generation
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
