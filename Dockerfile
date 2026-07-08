# Use a base image with JDK 21
FROM amazoncorretto:21-alpine

WORKDIR /deployments/app/

COPY target/Madoc-exec.jar /deployments/app

EXPOSE 8100

ENTRYPOINT ["java", "-jar", "Madoc-exec.jar"]