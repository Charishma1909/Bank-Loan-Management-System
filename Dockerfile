# Step 1: Build the Java executable with Maven
FROM maven:3.9.6-eclipse-temurin-26 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application into a JAR file (skipping tests for faster build)
RUN mvn clean package -DskipTests

# Step 2: Run the compiled app using a lightweight JRE image
FROM eclipse-temurin:26-jre
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (adjust 8080 if your app uses a different port like 8000)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]