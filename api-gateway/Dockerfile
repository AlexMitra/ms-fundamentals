FROM amazoncorretto:17-alpine-jdk
COPY build/libs/*.jar /app/api-gateway.jar
ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]
EXPOSE 8080