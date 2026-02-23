# Use OpenJDK 21 as base image to match the project's Java version
FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Copy the built JAR file from the target directory
# Assuming the JAR is named hub-backend-0.0.1-SNAPSHOT.jar based on pom.xml
COPY target/*.jar app.jar

# Expose port 8080
EXPOSE 8081

# Set entrypoint to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]