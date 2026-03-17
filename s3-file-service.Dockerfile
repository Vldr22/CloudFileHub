FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY common-kafka/pom.xml common-kafka/
COPY s3-file-service/pom.xml s3-file-service/
COPY antivirus-service/pom.xml antivirus-service/

RUN mvn dependency:go-offline -pl s3-file-service -am -q

COPY common-kafka/src common-kafka/src

COPY s3-file-service/src s3-file-service/src

RUN mvn clean package -pl s3-file-service -am -DskipTests -q

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/s3-file-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
