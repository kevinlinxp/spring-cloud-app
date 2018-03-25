#!/bin/bash
while ! nc -z cloudapp-eureka-server 8761; do sleep 2; done
java -jar -Dspring.profiles.active=docker-dev hystrix-dashboard.jar
