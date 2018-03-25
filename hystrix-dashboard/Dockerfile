FROM openjdk:8
RUN apt-get update && apt-get install -y netcat
ADD target/hystrix-dashboard.jar /hystrix-dashboard.jar
ADD start.sh /start.sh
RUN chmod u+x /start.sh
EXPOSE 7979
CMD ["/start.sh"]
