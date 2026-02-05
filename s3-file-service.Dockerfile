FROM eclipse-temurin:21-jre

WORKDIR /app

COPY s3-file-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
