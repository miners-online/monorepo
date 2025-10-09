# -----------------------------
# Stage 1: Build the Shadow Jar
# -----------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy the entire project
COPY . .

RUN ls -la /app/

# Make Gradle wrapper executable
RUN cd /app/games/lobby && chmod +x ./gradlew

# Build the shadow jar
RUN cd /app/games/lobby && ./gradlew --no-daemon shadowJar

# -----------------------------
# Stage 2: Runtime image
# -----------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the shadow jar from the builder stage
COPY --from=builder /app/games/lobby/build/libs/*-all.jar app.jar

# Expose port
EXPOSE 25565

# Run the jar
CMD ["java", "-jar", "app.jar"]
