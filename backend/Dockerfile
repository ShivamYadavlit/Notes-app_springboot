FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src
COPY .env .env

# Install Maven and build the application
RUN apt-get update && apt-get install -y maven
RUN mvn clean package

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/notesapp-0.0.1-SNAPSHOT.jar"]