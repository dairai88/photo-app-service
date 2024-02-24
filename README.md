# photo-app-service

Photo App service is a learning project building around microservices. This project consists of following services.


microservice|description
--|--
config-server|Use spring cloud config to provide center managed configuration
discoveryservice|Use spring cloud eureka server to provide registration and service discovery
api-gateway|Use spring cloud gateway to provide gateway service
users-ws|Use spring cloud eureka client to register itself to discovery service
albums-ws|Use spring cloud eureka client to register itself to discovery service

## Run project

Start each service in below order to run the project:
- config-server
- discoveryservice
- api-gateway
- users-ws
- albums-ws

## Start config-server

Config server provides configuration notification using `spring-cloud-starter-bus-amqp` which uses rabbitmq as backend storage for configs.

Start a rabbitmq service via docker container and start config server as follows.

```zsh
docker container run -d --name rabbitmq-server -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```
```zsh
mvn spring-boot:run -Dspring-boot.run.profiles=git
```

## Start discoveryservice