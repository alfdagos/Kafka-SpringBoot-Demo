
# Kafka + Spring Boot (it.alf)

Progetto di esempio (template) che mostra come integrare Apache Kafka con Spring Boot in un'applicazione Java moderna.

Questa repository contiene:
- produttori (REST controllers) che pubblicano messaggi su topic Kafka
- consumer basati su `@KafkaListener` che deserializzano i DTO e li persistono su H2 tramite Spring Data JPA
- configurazione completa di `ProducerFactory` / `ConsumerFactory` / `ConcurrentKafkaListenerContainerFactory`
- gestione degli errori con `DefaultErrorHandler` e Dead Letter Queue (DLQ)
- test d'integrazione con `spring-kafka-test` (Embedded Kafka)

Principali tecnologie
- Java 21
- Spring Boot 3.5.7
- Spring Kafka
- Spring Data JPA + H2 (in-memory per i test)
- Lombok (per riduzione boilerplate code)
- Spring DevTools (per sviluppo con hot reload)
- Spring Actuator (per monitoraggio e metriche)
- JUnit 5

Indice
- Panoramica
- Argomenti (topics)
- Come eseguire (quickstart)
- API REST disponibili
- Testing e sincronizzazione dei test EmbeddedKafka
- Architettura e note di design
- Debug & troubleshooting
- Contributi

## Panoramica

Il progetto è pensato come base per esperimenti e demo. I componenti principali sono sotto il package `it.alf`.

I consumer ricevono oggetti JSON che vengono deserializzati in DTO (classe `it.alf.dto.*`) e salvati in tabelle H2 tramite le entity JPA sotto `it.alf.entity`.

## Topics
I topic usati nell'applicazione (configurati in `application.yml`):
- `users-topic` — messaggi di tipo `User`
- `orders-topic` — messaggi di tipo `Order`
- `notifications-topic` — messaggi di tipo `Notification`
- `events-topic` — messaggi di tipo `GenericEvent`
- `dlq-topic` — dead-letter queue quando i messaggi non vengono processati correttamente

I bean `NewTopic` sono creati dal `KafkaConfig` all'avvio per facilitare l'esperienza di sviluppo.

## Quickstart

Prerequisiti
- Docker (per eseguire Kafka in locale) o un broker Kafka disponibile
- Maven 3.8+ e JDK 21

1) Avviare Kafka in locale (facoltativo):

```powershell
docker-compose up -d
```

2) Avviare l'app in modalità sviluppo:

```powershell
mvn spring-boot:run
```

3) Usare le API REST per pubblicare messaggi su Kafka (esempi PowerShell/curl)

- Creare un `User`:

```powershell
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"id":"u1","name":"Alice","email":"alice@example.com"}'
```

- Creare un `Order`:

```powershell
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d '{"id":"o1","userId":"u1","product":"Book","amount":12.5}'
```

- Invio `Notification`:

```powershell
curl -X POST http://localhost:8080/api/notifications -H "Content-Type: application/json" -d '{"id":"n1","message":"Order received","level":"INFO"}'
```

- Invio `GenericEvent`:

```powershell
curl -X POST http://localhost:8080/api/events -H "Content-Type: application/json" -d '{"id":"e1","type":"user.signup","payload":{"userId":"u1"}}'
```

### Controllare i record persistiti

I consumer persistono i messaggi in tabelle H2. L'app espone endpoint GET per leggere le entità salvate (es. `/api/users/{id}`), oppure è possibile aprire la console H2 se abilitata (vedere `application.yml`).

### Monitoraggio con Spring Actuator

L'applicazione include Spring Actuator per il monitoraggio. Una volta avviata, sono disponibili i seguenti endpoint:

- **Health Check**: `http://localhost:8080/actuator/health`
- **Informazioni App**: `http://localhost:8080/actuator/info`
- **Metriche**: `http://localhost:8080/actuator/metrics`
- **Environment**: `http://localhost:8080/actuator/env`
- **Configurazioni**: `http://localhost:8080/actuator/configprops`
- **Loggers**: `http://localhost:8080/actuator/loggers`
- **Thread Dump**: `http://localhost:8080/actuator/threaddump`
- **Heap Dump**: `http://localhost:8080/actuator/heapdump`

### Spring DevTools per lo sviluppo

Il progetto include Spring DevTools che offre:
- **Riavvio automatico** quando si modificano i file Java
- **LiveReload** integrato per aggiornare automaticamente il browser
- **Configurazioni ottimizzate** per lo sviluppo

Per sfruttare il riavvio automatico, basta modificare un file Java e salvarlo. L'applicazione si riavvierà automaticamente.

## Testing — Embedded Kafka e stabilità dei test

I test d'integrazione si appoggiano a `spring-kafka-test` e `@EmbeddedKafka` per eseguire un broker Kafka in-process.

Problemi comuni
- Race conditions: il producer può inviare il messaggio prima che il container del listener sia assegnato alle partizioni. Questo provoca test intermittenti con repository vuote.
- Contesto condiviso: i test Spring condividono il context per default e possono interferire tra loro.

Soluzioni usate in questo progetto
- Nei test di integrazione si aspetta esplicitamente l'assegnazione del container listener prima di inviare il messaggio, usando il `KafkaListenerEndpointRegistry` e `ContainerTestUtils.waitForAssignment(...)`.
- Si usa `@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)` per isolare i test quando necessario.
- I test usano un polling semplice con timeout (esteso a 10s) per aspettare che l'entità venga scritta in DB. Se preferite, potete sostituire il polling con Awaitility per codice più espressivo.

Esempio sintetico (test):

1) attendere che il container sia assegnato
2) inviare il messaggio con `KafkaTemplate.send(...).get()`
3) pollare il repository fino a che il record non compare

## Architettura e note di design

- `KafkaConfig` configura producer/consumer factory e fornisce `ConcurrentKafkaListenerContainerFactory` tipizzate per ciascun DTO.
- `DefaultErrorHandler` è impostato con un `DeadLetterPublishingRecoverer` che invia i record falliti su `dlq-topic` dopo un numero definito di retry.
- I consumer sono semplici `@Service` con `@KafkaListener(...)` che trasformano i DTO in entity JPA e li salvano.

Design choices e motivazioni
- **JsonSerializer/JsonDeserializer**: usiamo il serializer JSON nativo di Spring Kafka per semplicità e leggibilità.
- **Tipizzazione dei consumer** (ConsumerFactory<User>, ecc.) aiuta a ottenere la deserializzazione diretta nelle classi DTO senza conversioni manuali.
- **Lombok**: tutte le entity e i DTO utilizzano Lombok per eliminare il boilerplate code (getter/setter/costruttori). Le annotazioni principali utilizzate sono:
  - `@Data` - genera getter, setter, toString, equals, hashCode
  - `@NoArgsConstructor` - costruttore vuoto (richiesto da JPA e Jackson)
  - `@AllArgsConstructor` - costruttore con tutti i parametri
- **Spring DevTools**: configurato per il riavvio automatico durante lo sviluppo e LiveReload per il browser.
- **Spring Actuator**: configurato per esporre endpoint di monitoraggio completi con dettagli su salute, metriche e configurazioni.

## Debug & troubleshooting

- Test falliscono con repository vuote: assicuratevi che i listener siano assegnati. Controllare i log del test per "partitions assigned".
- Messaggi in DLQ: aprire `dlq-topic` e leggere i record per capire l'errore di deserializzazione o business exception.

## Contribuire

Pull request benvenute. Per cambiamenti strutturali ai test, preferire refactoring incrementali e verificare `mvn test`.

---
Documentazione generata automaticamente e commentata inline nel codice per facilitare la lettura.
