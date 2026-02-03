FROM maven:3.9.9-eclipse-temurin-17 AS build

#Set working directory inside container
WORKDIR /app 

#copy pom.xml into container
COPY pom.xml .

#Download all dependencies
RUN mvn -q -B dependency:go-offline

#Copy source code
COPY src ./src

#build springboot jar. We will also skip test for now
RUN mvn -q -B package -DskipTests

#lightweight java runtime for production
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]

