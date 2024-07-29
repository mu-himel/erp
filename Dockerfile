FROM maven:3.6.3-openjdk-17-slim AS builder

WORKDIR /app

COPY . .

COPY ./src/main/resources/application.properties ./src/main/resources/application.properties

RUN mvn clean install -DskipTests


FROM openjdk:17-oracle

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN useradd -ms /bin/bash appuser

USER appuser

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "./app.jar"]