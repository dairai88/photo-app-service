# photo-app-service

Photo App service is a learning project building around microservices. This project consists of following services.


microservice|description
--|--
config-server|Use spring cloud config to provide center managed configuration
discoveryservice|Use spring cloud eureka server to provide registration and service discovery
api-gateway|Use spring cloud gateway to provide gateway service
users-ws|Use spring cloud eureka client to register itself to discovery service
albums-ws|Use spring cloud eureka client to register itself to discovery service
