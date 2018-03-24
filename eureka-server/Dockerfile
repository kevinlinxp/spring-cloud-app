FROM openjdk:8
RUN apt-get update && apt-get install -y netcat
ADD target/eureka-server.jar eureka-server.jar
ADD start.sh /start.sh
RUN chmod u+x /start.sh
EXPOSE 8761
CMD ["/start.sh"]
