# Use OpenJDK base image
FROM openjdk:21-jdk-slim

# Set app directory
WORKDIR /app

# Copy the jar file into the container
COPY target/Auction-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Spring Boot default)
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
