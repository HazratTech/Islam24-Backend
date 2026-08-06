FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 9393
ENTRYPOINT ["java", "-jar", "app.jar"]
