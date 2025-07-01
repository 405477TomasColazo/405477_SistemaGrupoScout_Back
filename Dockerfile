FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the project
COPY Sistema-Scouts-Jose-Hernandez/ .

# Make maven wrapper executable
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/*.jar"]