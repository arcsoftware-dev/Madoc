# Use a base image with OpenJDK 21
FROM openjdk:21-jdk-slim

WORKDIR /deployments/app/

COPY target/Madoc-exec.jar /deployments/app

EXPOSE 8100

ENTRYPOINT ["java", "-jar", "Madoc-exec.jar"]