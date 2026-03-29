# Use Java 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy everything
COPY . .

# Give permission to gradlew
RUN chmod +x gradlew

# Build the app
RUN ./gradlew build -x test

# Expose port
EXPOSE 8080

# Run the jar
CMD ["java", "-jar", "build/libs/havenly-stays-0.0.1-SNAPSHOT.jar"]
