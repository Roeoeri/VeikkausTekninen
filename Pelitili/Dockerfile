FROM maven:3.8.1-openjdk-11 AS build  
COPY src /usr/src/app/src  
COPY pom.xml /usr/src/app  
RUN mvn -f /usr/src/app/pom.xml clean package

FROM openjdk:11  
COPY --from=build /usr/src/app/target/consoleApp-1.0-SNAPSHOT-jar-with-dependencies.jar  /usr/app/app.jar  
EXPOSE 3000  
ENTRYPOINT ["java","-jar","/usr/app/app.jar"]  
