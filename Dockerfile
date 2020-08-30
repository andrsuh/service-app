FROM adoptopenjdk/openjdk11:ubi
ADD target/demo-service-0.1.1-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=docker,prod","/app.jar"]