#!/bin/bash
# Deprecated - Use docker-compose in the parent project
APP_NAME=cloudapp-eureka-server
PORT=8761
LINK=cloudapp-config-server

docker stop ${APP_NAME}
docker rm ${APP_NAME}
mvn clean package
docker build -t ${APP_NAME} .
docker rmi $(docker images -qa -f "dangling=true")
docker run --name ${APP_NAME} -p${PORT}:${PORT} --link ${LINK} -d ${APP_NAME}
