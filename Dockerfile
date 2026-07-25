# Step 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build Fat JAR containing all dependencies
RUN mvn clean package -DskipTests

# Step 2: Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy specifically the JAR bundled with dependencies
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar

# Expose port (adjust if using a web framework)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]