FROM openjdk:8
RUN apt-get update && \
    apt-get install git
ADD config-repo /config-repo
ADD target/config-server.jar /config-server.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker-dev", "config-server.jar"]