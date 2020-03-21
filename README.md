# Flux Reservations

[SpringBoot 2](https://spring.io/projects/spring-boot) sample app in [Java 8](https://docs.oracle.com/javase/8/docs/api/), showcasing:

* [REST API](https://en.wikipedia.org/wiki/Representational_state_transfer) built using [WebFlux](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html)
* [Reactive programming](https://en.wikipedia.org/wiki/Reactive_programming) approach
* [Gradle](https://gradle.org) managed build
* [MongoDB](https://www.mongodb.com) as storage, using [reactive lib](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive.repositories)
* [JUnit 5](https://junit.org/junit5/) / [Mockito](https://github.com/mockito/mockito) for unit tests
* [Embedded Mongo](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo) for [integration tests](https://martinfowler.com/bliki/IntegrationTest.html)
* [Gatling](https://gatling.io) for [performance tests](https://en.wikipedia.org/wiki/Software_performance_testing) written in [Scala](https://www.scala-lang.org)
* [Swagger 2](https://swagger.io) for API doc generation
* [Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html) for [monitoring/instrumentation](https://en.wikipedia.org/wiki/Instrumentation_(computer_programming)) 

## Local env setup

### Install:

1. Java 8
2. Scala
3. MongoDB
4. Gradle

## Run unit and integration tests
These tests will use EmbeddedMongo by default.
```
$ gradle tests
$ open build/reports/tests/test/index.html
```

##  Run performance tests
These will use EmbeddedMongo as set via command line arg.
You need to open 2 Terminal consoles:
* In Terminal 1 start the REST server with EmbeddedMongo:
```
$ gradle clean bootRun -PembeddedMongo --args='--reservation.max-days-ahead=-1' 
```
* In Terminal 2 run the actual performance test:
```
$ gradle gatlingRun -DuserCount=100 
```

## Run the server
* Start MongoDB
```
$ brew services start mongodb-community
```
* Start the server
```
$ gradle runBoot
```
### Some REST calls using CURL
* Check availability
```
$ curl http://localhost:8080/reservations
{"inThisDateRange":{"from":"2020-03-19","to":"2020-04-19"},"datesAvailable":[{"from":"2020-03-19","to":"2020-04-19"}]}
$ curl http://localhost:8080/reservations?from=2020-09-01
{"inThisDateRange":{"from":"2020-09-01","to":"2020-10-01"},"datesAvailable":[{"from":"2020-09-01","to":"2020-10-01"}]}
```
* Make a reservation

Note how the `Location` response header shows the actual location (id) for the created resource
```
$ curl -v -d '{"email":"john.doe@email.com", "fullName":"John Doe", "dateRange":{"from": "2020-04-01", "to": "2020-04-03"}}' -H "Content-Type: application/json" -X POST http://localhost:8080/reservations
Note: Unnecessary use of -X or --request, POST is already inferred.
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> POST /reservations HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.64.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 111
> 
* upload completely sent off: 111 out of 111 bytes
< HTTP/1.1 201 Created
< Location: /reservations/5e73baec9c3d0c2d7ba63af7
< content-length: 0
< 
* Connection #0 to host localhost left intact
* Closing connection 0
```
* Modify a reservation
```
$ curl -d '{"from": "2020-04-02", "to": "2020-04-04"}' -H "Content-Type: application/json" -X PUT http://localhost:8080/reservations/5e73bc349c3d0c2d7ba63af8
{"id":"5e73bc349c3d0c2d7ba63af8","email":"john.doe@email.com","fullName":"John Doe","dateRange":{"from":"2020-04-02","to":"2020-04-04"}}
```
* Cancel a reservation
```
$ curl -X DELETE http://localhost:8080/reservations/5e73bc349c3d0c2d7ba63af8
```
### Open REST API Doc
```
$ open http://localhost:8080/swagger-ui.html
```
### Monitoring
```
$ open http://localhost:8080/manage/health
```
Some other built in endpoints available (via [Actuator](https://docs.spring.io/spring-boot/docs/2.0.x/actuator-api/html/)):
* `http://localhost:8080/manage/env`
* `http://localhost:8080/manage/health`
* `http://localhost:8080/manage/beans`
* `http://localhost:8080/manage/heapdump`
* `http://localhost:8080/manage/mappings`
* `http://localhost:8080/manage/metrics`
* `http://localhost:8080/manage/threaddump`



