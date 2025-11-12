# Use an official Eclipse Temurin (OpenJDK 17) base image
FROM eclipse-temurin:17-jdk-jammy

# Set working directory inside the container
WORKDIR /app

# Copy Maven files and source code
COPY pom.xml .
COPY src ./src

# Install Maven and build your Spring Boot app
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Expose the app port
EXPOSE 8090

# Run your Spring Boot JAR file
CMD ["java", "-jar", "target/DocAITutor-0.0.1-SNAPSHOT.jar"]
