#!/bin/bash
while ! nc -z cloudapp-reservation-service 8000; do sleep 2; done
java -jar -Dspring.profiles.active=docker-dev reservation-client.jar
