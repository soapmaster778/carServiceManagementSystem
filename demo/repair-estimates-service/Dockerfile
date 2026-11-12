FROM eclipse-temurin:17-jre

WORKDIR /app
COPY target/*.jar app.jar

RUN useradd --system --no-create-home spring
USER spring

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
