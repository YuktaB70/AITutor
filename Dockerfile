# Use an official OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven files and source code
COPY pom.xml .
COPY src ./src

# Install Maven and build the app
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Expose the Spring Boot port
EXPOSE 8090

# Run the app
CMD ["java", "-jar", "target/*.jar"]
