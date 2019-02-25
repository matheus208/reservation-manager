#Volcano Reservation Manager

#Running
Before running, build application with `mvn package` and docker with `docker-compose build`

Run with `docker-compose up`, the service will be available at `localhost:8080`

#Sample requests

curl -XPOST localhost:8080/reservations -H'Content-Type: application/json' -d \
'{
  "name": "Matheus",
  "email": "matheus@email.com",
  "from": "2019-02-10",
  "to": "2019-02-12"
}'

curl -XGET localhost:8080/reservations/0983e4d9-faaf-47c0-99b9-2f5628b81e72

curl -XGET localhost:8080/reservations?from=2019-02-01&to=2019-12-31

curl -XDELETE localhost:8080/reservations/0983e4d9-faaf-47c0-99b9-2f5628b81e72

curl -XPUT localhost:8080/reservations -H'Content-Type: application/json' -d \
'{
  "id": "0983e4d9-faaf-47c0-99b9-2f5628b81e72",
  "name": "Matheus",
  "email": "matheus@email.com",
  "from": "2019-02-12",
  "to": "2019-02-13"
}'

#Decisions
 - Stateless: easy to scale for high performance
 - Spring + Java: What I'm most comfortable and familiar with.
 - Caching: Using redis because it enables distributed caching
 - Locking: Used transactions to limit one booking per range of time.
 - Converting DTO to Model: I like to isolate the data transfer objects from the integration objects, even if it
 costs a little bit more verbosity/code.
 - "Functional": I appreciate functional programming and its concepts, as I believe it leads to cleaner and safer code,
 even thought it might not be familiar to some people.

#Wishlist
 - Simulate multiple machines/scaling using k8s
 - Run load tests to measure performance
 - Improve caching for list (but to be honest, I'd consider moving data to an index like Elasticsearch, maybe?)
 - Write the project in Kotlin, it'd have reduced boilerplate code (specially in the POJOs and utilities), but the
 specs file was titled "Java challenge", so I didn't want to go away from that.
 - Add authentication service (although specs mention passing name+email on the endpoints specifically)
 - Improve logging with slf4j, healthcheck endpoints using actuators, and monitoring tools. These would be mandatory
 for a real service
 - Using status field to control what reservations are valid and which ones have been cancelled, for traceability/
 history/auditing.
 - Set up SonarQube for static code analysis
