FROM openjdk:8
RUN apt-get update && apt-get install -y netcat
ADD target/reservation-client.jar /reservation-client.jar
ADD start.sh /start.sh
RUN chmod u+x /start.sh
EXPOSE 9999
CMD ["/start.sh"]
