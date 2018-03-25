FROM openjdk:8
RUN apt-get update && apt-get install -y netcat
ADD target/reservation-service.jar /reservation-service.jar
ADD start.sh /start.sh
RUN chmod u+x /start.sh
EXPOSE 8000
CMD ["/start.sh"]
