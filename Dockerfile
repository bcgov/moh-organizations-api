# Build with Maven + JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /usr/src/app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime with Corretto 17
FROM amazoncorretto:17-alpine-jdk AS runtime
WORKDIR /app
COPY --from=build /usr/src/app/target/*.jar app.jar
EXPOSE 80
ENTRYPOINT ["java","-jar","app.jar"]
