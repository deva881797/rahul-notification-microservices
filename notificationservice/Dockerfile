FROM eclipse-temurin:17-jdk-jammy

LABEL authors="rahul"
WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]