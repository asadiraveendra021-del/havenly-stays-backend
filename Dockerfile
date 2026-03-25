# STEP 1: Build Stage (Uses the JDK to compile your code)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
# Make the gradlew file executable
RUN chmod +x ./gradlew
# Build the jar file, skipping tests to save time
RUN ./gradlew clean bootJar -x test

# STEP 2: Run Stage (Uses a smaller JRE to actually run the app)
FROM eclipse-temurin:17-jre-alpine
# CRITICAL: Install certificates so Java can connect to Supabase SSL
RUN apk add --no-cache ca-certificates
WORKDIR /app
# Copy the built jar from the 'build' stage above
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
