FROM openjdk:8
ADD target/config-server.jar config-server.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker-dev", "config-server.jar"]