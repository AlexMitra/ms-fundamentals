FROM amazoncorretto:17-alpine-jdk
COPY build/libs/*.jar /app/resource-service.jar
ENTRYPOINT ["java", "-jar", "/app/resource-service.jar"]
EXPOSE 8080
