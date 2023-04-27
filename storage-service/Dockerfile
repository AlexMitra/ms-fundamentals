FROM amazoncorretto:17-alpine-jdk
COPY build/libs/*.jar /app/storage-service.jar
ENTRYPOINT ["java", "-jar", "/app/storage-service.jar"]
EXPOSE 8080