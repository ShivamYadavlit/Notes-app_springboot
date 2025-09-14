FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven and build the application
RUN apt-get update && apt-get install -y maven
RUN mvn clean package

# Expose port
EXPOSE 8080

# Environment variables with default values
ENV SPRING_DATA_MONGODB_URI=mongodb+srv://shivamyadav2072000_db_user:59wr5wd3V0HHAa0Y@cluster0.jvk8bpk.mongodb.net/notesapp?retryWrites=true&w=majority
ENV JWT_SECRET=NotesAppSecretKey12345678901234567890123456789012345678901234567890
ENV JWT_EXPIRATION=86400000

# Run the application
CMD ["java", "-jar", "target/notesapp-0.0.1-SNAPSHOT.jar"]