FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the project
COPY Sistema-Scouts-Jose-Hernandez/ .

# Make maven wrapper executable
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# List target directory for debugging
RUN ls -la target/

# Expose port
EXPOSE 8080

# Run the application - find the actual jar name
CMD ["java", "-jar", "target/Sistema-Scouts-Jose-Hernandez-0.0.1-SNAPSHOT.jar"]