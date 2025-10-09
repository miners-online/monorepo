# -----------------------------
# Stage 1: Build the Shadow Jar
# -----------------------------
FROM eclipse-temurin:21-jdk-focal AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and configuration for caching
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Download dependencies (cached)
RUN ./gradlew --no-daemon dependencies || true

# Copy the rest of the project
COPY . .

# Build the shadow jar
RUN ./gradlew --no-daemon shadowJar

# -----------------------------
# Stage 2: Runtime image
# -----------------------------
FROM eclipse-temurin:21-jre-focal

WORKDIR /app

# Copy the shadow jar from the builder stage
COPY --from=builder /app/build/libs/*-all.jar app.jar

# Expose port
EXPOSE 25565

# Run the jar
CMD ["java", "-jar", "app.jar"]
