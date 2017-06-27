# Fun.CQRS Kafka Consumer

Demo a Fun.CQRS projection that is attached to a Kafka topic. 

It uses a `CommitableOffset` and saves it on the topic itself.

Start Kafka in Docker if you don't have it in your system

```
docker-compose up -d
```
  
Start project using `sbt run`

On another prompt, start a kafka producer on command line:
```
kafka-console-producer --broker-list localhost:9092 --topic events
```

Type some messages on the kafka producer prompt and watch it being consumed by Fun.CQRS Projection.
