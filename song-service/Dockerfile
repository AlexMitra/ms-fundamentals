FROM amazoncorretto:17-alpine-jdk
COPY build/libs/*.jar /app/song-service.jar
ENTRYPOINT ["java", "-jar", "/app/song-service.jar"]
EXPOSE 8080