FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY ./build .

# Copy the shadow jar from the builder stage
COPY --from=builder ./build/libs/*-all.jar app.jar

# Expose port
EXPOSE 25565

# Run the jar
CMD ["java", "-jar", "app.jar"]
