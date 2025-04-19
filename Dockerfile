# Use Amazon Corretto 21 as the base image
FROM amazoncorretto:21-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the target folder into the container
COPY target/*.jar /app/

# Expose the port the application will run on
EXPOSE 8081

# Run the JAR file when the container starts
CMD ["java", "-jar", "app.jar"]
