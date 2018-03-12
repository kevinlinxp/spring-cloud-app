# spring-cloud-app
Having some fun with Spring Cloud

## Enable messaging

The microservices in this app use rabbitmq for messaging. To install and start the message broker, use:
```sh
brew install rabbitmq

rabbitmq-server
```

## Use Hystrix Dashboard
1. http://localhost:8010/hystrix
2. Input `http://localhost:9999/actuator/hystrix.stream` and click `Monitor Stream`