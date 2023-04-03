FROM amazoncorretto:17-alpine-jdk
COPY build/libs/*.jar /app/service-discovery.jar
ENTRYPOINT ["java", "-jar", "/app/service-discovery.jar"]
EXPOSE 8080