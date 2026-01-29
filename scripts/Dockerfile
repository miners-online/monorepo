FROM eclipse-temurin:25-jre-alpine

# This argument will tell the Dockerfile which project to build
ARG WORK_PATH

WORKDIR /app

# Copy the build output of the target project into the image
# The build context will be the repo root, so this path is valid
COPY ${WORK_PATH}/build/libs/*-all.jar app.jar

# Expose port
EXPOSE 25565

# Run the jar
CMD ["java", "-jar", "app.jar"]
