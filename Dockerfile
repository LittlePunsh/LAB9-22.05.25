
FROM maven:3.9.8 AS builder

WORKDIR /app

COPY pom.xml .

COPY src /app/src

RUN ["mvn", "clean", "package", "-DskipTests=true"]

FROM openjdk:19-jdk-slim

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
