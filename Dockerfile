# Use OpenJDK 21
FROM openjdk:21
WORKDIR /app
COPY target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]