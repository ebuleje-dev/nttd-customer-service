# Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/target/*.jar customer-service.jar
ENTRYPOINT ["java","-jar","/customer-service.jar"]
EXPOSE 8082
