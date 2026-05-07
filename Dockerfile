# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy project files
COPY . .

# Build jar
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]