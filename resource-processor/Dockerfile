FROM amazoncorretto:17-alpine-jdk
COPY build/libs/*.jar /app/resource-processor.jar
ENTRYPOINT ["java", "-jar", "/app/resource-processor.jar"]
EXPOSE 8080