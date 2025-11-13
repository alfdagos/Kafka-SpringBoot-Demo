# Kafka + Spring Boot (it.alf)

Example (template) project demonstrating how to integrate Apache Kafka with Spring Boot in a modern Java application.

This repository contains:
- producers (REST controllers) that publish messages to Kafka topics
- consumers using `@KafkaListener` that deserialize DTOs and persist them to H2 using Spring Data JPA
- full configuration of `ProducerFactory` / `ConsumerFactory` / `ConcurrentKafkaListenerContainerFactory`
- error handling with `DefaultErrorHandler` and a Dead Letter Queue (DLQ)
- integration tests using `spring-kafka-test` (Embedded Kafka)

Main technologies
- Java 21
- Spring Boot 3.5.7
- Spring Kafka
- Spring Data JPA + H2 (in-memory for tests)
- Lombok (to reduce boilerplate)
- Spring DevTools (development hot reload)
- Spring Actuator (monitoring and metrics)
- JUnit 5

Contents
- Overview
- Topics
- How to run (quickstart)
- Available REST APIs
- Testing and EmbeddedKafka test synchronization
- Architecture and design notes
- Debug & troubleshooting
- Contributing

## Overview

This project is intended as a starting point for experiments and demos. The main components are under the `it.alf` package.

Consumers receive JSON messages that are deserialized into DTOs (classes under `it.alf.dto.*`) and persisted into H2 tables using the JPA entities under `it.alf.entity`.

## Topics
Topics used by the application (configured in `application.yml`):
- `users-topic` — messages of type `User`
- `orders-topic` — messages of type `Order`
- `notifications-topic` — messages of type `Notification`
- `events-topic` — messages of type `GenericEvent`
- `dlq-topic` — dead-letter queue used when messages cannot be processed correctly

`NewTopic` beans are created by `KafkaConfig` at startup to simplify the developer experience.

## Quickstart

Prerequisites
- Docker (to run Kafka locally) or a running Kafka broker
- Maven 3.8+ and JDK 21

1) Start Kafka locally (optional):

```powershell
docker-compose up -d
```

2) Run the application in development mode:

```powershell
mvn spring-boot:run
```

3) Use the REST APIs to publish messages to Kafka (PowerShell/curl examples)

- Create a `User`:

```powershell
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"id":"u1","name":"Alice","email":"alice@example.com"}'
```

- Create an `Order`:

```powershell
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d '{"id":"o1","userId":"u1","product":"Book","amount":12.5}'
```

- Send a `Notification`:

```powershell
curl -X POST http://localhost:8080/api/notifications -H "Content-Type: application/json" -d '{"id":"n1","message":"Order received","level":"INFO"}'
```

- Send a `GenericEvent`:

```powershell
curl -X POST http://localhost:8080/api/events -H "Content-Type: application/json" -d '{"id":"e1","type":"user.signup","payload":{"userId":"u1"}}'
```

### Check persisted records

Consumers persist messages into H2 tables. The app exposes GET endpoints to read saved entities (e.g. `/api/users/{id}`) or you can open the H2 console if enabled (see `application.yml`).

### Monitoring with Spring Actuator

The application includes Spring Actuator for monitoring. Once the app is running, the following endpoints are available:

- **Health Check**: `http://localhost:8080/actuator/health`
- **App Info**: `http://localhost:8080/actuator/info`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Environment**: `http://localhost:8080/actuator/env`
- **Config Props**: `http://localhost:8080/actuator/configprops`
- **Loggers**: `http://localhost:8080/actuator/loggers`
- **Thread Dump**: `http://localhost:8080/actuator/threaddump`
- **Heap Dump**: `http://localhost:8080/actuator/heapdump`

### Spring DevTools for development

The project includes Spring DevTools, which provides:
- **Automatic restart** when Java files change
- **LiveReload** integration for the browser
- **Development-optimized defaults**

To use automatic restart, modify and save a Java file — the application will restart automatically.

## Testing — Embedded Kafka and test stability

Integration tests rely on `spring-kafka-test` and `@EmbeddedKafka` to run an in-process Kafka broker.

Common issues
- Race conditions: a producer might send a message before the listener container is assigned to partitions. This can cause flaky tests with empty repositories.
- Shared context: Spring tests share the application context by default and can interfere with each other.

Solutions used in this project
- Integration tests explicitly wait for the listener container assignment before sending messages, using `KafkaListenerEndpointRegistry` and `ContainerTestUtils.waitForAssignment(...)`.
- `@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)` is used to isolate tests when necessary.
- Tests use a simple polling approach with a timeout (extended to 10s) to wait for the entity to be written to the DB. You can replace this with Awaitility for more expressive assertions.

Typical test flow:

1) wait for the listener container assignment
2) send the message with `KafkaTemplate.send(...).get()`
3) poll the repository until the record appears

## Architecture and design notes

- `KafkaConfig` configures producer/consumer factories and provides typed `ConcurrentKafkaListenerContainerFactory` instances for each DTO.
- `DefaultErrorHandler` is configured with a `DeadLetterPublishingRecoverer` that publishes failed records to `dlq-topic` after a configured number of retries.
- Consumers are simple `@Service` components with `@KafkaListener(...)` that map DTOs to JPA entities and persist them.

Design choices and rationale
- **JsonSerializer/JsonDeserializer**: we use Spring Kafka's JSON serializer for simplicity and readability.
- **Typed consumers** (ConsumerFactory<User>, etc.) enable direct deserialization into DTO classes without manual conversion.
- **Lombok**: entities and DTOs use Lombok to remove boilerplate (getters/setters/constructors). Main annotations used:
  - `@Data` — generates getters, setters, toString, equals, and hashCode
  - `@NoArgsConstructor` — no-args constructor (required by JPA and Jackson)
  - `@AllArgsConstructor` — all-args constructor
- **Spring DevTools**: configured for automatic restart and LiveReload during development.
- **Spring Actuator**: configured to expose detailed monitoring endpoints for health, metrics, and configuration.

## Debug & troubleshooting

- Tests fail with empty repositories: ensure listeners are assigned; check test logs for "partitions assigned".
- Messages in DLQ: inspect the `dlq-topic` records to diagnose deserialization or business exceptions.

## Contributing

Pull requests are welcome. For structural changes to tests, prefer incremental refactoring and verify changes with `mvn test`.

---
Automatically generated documentation and inline code comments are provided to help readability.

## Demo: using Strimzi (Kafka on Kubernetes)

This section explains how to quickly try Kafka in a Kubernetes cluster using the Strimzi operator.
The simplest approach for local development is to deploy Strimzi on a local cluster (minikube/kind) and then use port-forwarding to connect your local application.

Prerequisites
- `kubectl` configured for your cluster (minikube, kind, or OpenShift)
- `helm` (optional)
- access to the machine running the cluster (minikube/kind)

1) Create the namespace and install the Strimzi operator

```powershell
kubectl create namespace kafka
# install Strimzi (operator + CRDs)
kubectl apply -f "https://strimzi.io/install/latest?namespace=kafka" -n kafka
```

2) Deploy a minimal Kafka cluster (1 replica) — save as `kafka-cluster.yaml` and apply

```yaml
apiVersion: kafka.strimzi.io/v1beta3
kind: Kafka
metadata:
  name: my-cluster
  namespace: kafka
spec:
  kafka:
    version: 3.4.0
    replicas: 1
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: external
        port: 9094
        type: nodeport
        tls: false
    storage:
      type: ephemeral
  zookeeper:
    replicas: 1
    storage:
      type: ephemeral
  entityOperator: {}
```

```powershell
kubectl apply -f kafka-cluster.yaml -n kafka
kubectl -n kafka wait kafka/my-cluster --for=condition=Ready --timeout=300s
kubectl -n kafka get pods
```

3) Create a topic (example `users-topic` used by the app)

```yaml
apiVersion: kafka.strimzi.io/v1beta3
kind: KafkaTopic
metadata:
  name: users-topic
  labels:
    strimzi.io/cluster: my-cluster
  namespace: kafka
spec:
  partitions: 1
  replicas: 1
```

```powershell
kubectl apply -f users-topic.yaml -n kafka
kubectl -n kafka get kafkatopic
```

4) Quick access from your local machine (port-forward)

This method is convenient for developing and testing the local application without deploying the app into Kubernetes.

```powershell
# forward the bootstrap service to localhost:9092
kubectl -n kafka port-forward svc/my-cluster-kafka-bootstrap 9092:9092
```

Then configure `application.yml` (or your `spring.kafka.bootstrap-servers`) with:

```
spring.kafka.bootstrap-servers: localhost:9092
```

Note: if you configured the `external` listener as `nodeport`, you can use the node IP and NodePort exposed by the `my-cluster-kafka-external-bootstrap` service.

5) Test with Strimzi clients (console producer/consumer)

Quick example using a temporary pod that contains Kafka tools:

```powershell
kubectl run --rm -i --tty kafka-client --image=strimzi/kafka:latest -- bash
# inside the pod shell
/opt/kafka/bin/kafka-console-producer.sh --broker-list my-cluster-kafka-bootstrap:9092 --topic users-topic
/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic users-topic --from-beginning
```

6) Run the app locally and send messages

With the port-forward active, you can start the app with `mvn spring-boot:run` (or run the JAR) and use the existing REST endpoints to publish messages to the Strimzi topics (for example `/api/users`).

Tips and notes
- For more realistic CI/CD tests you can deploy the application into the same Kubernetes cluster.
- For production use, prefer `external` listeners with TLS and authentication (SCRAM or TLS) — Strimzi manages `KafkaUser` and TLS secrets for you.
- CR API versions can change across Strimzi releases; consult the official docs (https://strimzi.io/docs) for the version you install.

If you want, I can add the full manifests as files, provide a kubectl demo script, or create a GitHub Action that brings up a `kind` cluster, installs Strimzi, and runs a smoke test.
