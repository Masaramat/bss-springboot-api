# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the application's JAR file to the container
COPY target/loan-processor-0.0.1-SNAPSHOT.jar /app/loan-processor.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/loan-processor.jar"]
