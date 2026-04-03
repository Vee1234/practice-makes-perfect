FROM eclipse-temurin:25-jre

WORKDIR /app

COPY new-project.jar app.jar

CMD ["java", "-jar", "app.jar"]
