Rabbitmq
```zsh
docker container run -d --name rabbitmq-server -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```
zipkin
```zsh
docker container run --name zipkin-server -d -p 9411:9411 openzipkin/zipkin
```