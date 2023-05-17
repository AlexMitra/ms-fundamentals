FROM amazoncorretto:17-alpine-jdk
COPY build/libs/*.jar /app/authorization-server.jar
ENTRYPOINT ["java", "-jar", "/app/authorization-server.jar"]
EXPOSE 8080