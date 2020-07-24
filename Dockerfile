FROM openjdk:11
ADD target/test-service-0.1.0-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]