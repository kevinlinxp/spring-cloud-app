#!/bin/bash
while ! nc -z cloudapp-config-server 8888; do sleep 2; done
java -jar -Dspring.profiles.active=docker-dev eureka-server.jar
