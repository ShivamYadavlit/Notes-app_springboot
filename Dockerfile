FROM openjdk:17-jdk-slim

WORKDIR /app

COPY backend/pom.xml .
COPY backend/src ./src

RUN apt-get update && apt-get install -y maven
RUN mvn clean package

EXPOSE 8080

CMD ["java", "-jar", "target/notesapp-0.0.1-SNAPSHOT.jar"]