FROM maven:3.6.0-jdk-11-slim AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM amazoncorretto:11-alpine-jdk as server
COPY --from=build /usr/src/app/target/*.jar app.jar
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} app.jar

FROM server as test
RUN echo "Much test"

FROM server as runtime
EXPOSE 80
ENTRYPOINT ["java","-jar","/app.jar"]
