# Build stage
FROM maven:3.8.8-openjdk-17 AS build
WORKDIR /workspace

# Copy maven config and source, then build
COPY pom.xml ./
COPY src ./src

# Use a reproducible, non-interactive build and skip tests by default to avoid requiring Kafka during image build
RUN mvn -B -DskipTests package

# Runtime stage
FROM openjdk:17-slim

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /workspace/target/*.jar app.jar

# Expose default web port
EXPOSE 8080

# Default environment variables
ENV SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
ENV JAVA_OPTS=""

# Entrypoint: allow overriding Kafka bootstrap servers via env var SPRING_KAFKA_BOOTSTRAP_SERVERS
# Spring Boot will map SPRING_KAFKA_BOOTSTRAP_SERVERS -> spring.kafka.bootstrap-servers
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS} -jar /app/app.jar"]

