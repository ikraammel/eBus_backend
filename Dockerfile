FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml ./
COPY src/main ./src/main
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
RUN mkdir -p /app/uploads && chown -R 10001:10001 /app
COPY --from=build --chown=10001:10001 /app/target/gestion-bus-0.0.1-SNAPSHOT.jar app.jar
USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
