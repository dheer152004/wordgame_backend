# Use Java 21
FROM eclipse-temurin:21

# Copy jar file
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run app
ENTRYPOINT ["java","-jar","/app.jar"]