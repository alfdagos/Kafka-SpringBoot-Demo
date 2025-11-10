package it.alf.config;

import it.alf.dto.Order;
import it.alf.dto.User;
import it.alf.dto.Notification;
import it.alf.dto.GenericEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
/**
 * Kafka configuration centralizzata per l'applicazione.
 *
 * Contiene:
 * - ProducerFactory e KafkaTemplate per inviare messaggi JSON
 * - ConsumerFactory tipizzate per ogni DTO (User, Order, Notification, GenericEvent)
 * - ConcurrentKafkaListenerContainerFactory per creare listener container configurati
 * - DefaultErrorHandler con DeadLetterPublishingRecoverer per inoltrare i messaggi falliti sulla DLQ
 *
 * Note:
 * - I JSON vengono serializzati/deserializzati tramite i serializer di Spring Kafka
 * - TRUSTED_PACKAGES è impostato su "it.alf,*" per consentire la deserializzazione dei DTO locali
 */
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.users}")
    private String usersTopic;

    @Value("${app.kafka.topics.orders}")
    private String ordersTopic;

    @Value("${app.kafka.topics.notifications}")
    private String notificationsTopic;

    @Value("${app.kafka.topics.events}")
    private String eventsTopic;

    @Value("${app.kafka.topics.deadletter}")
    private String dlqTopic;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Configurazione di base per il producer: bootstrap servers e serializer JSON.
     * Usato per creare il {@link KafkaTemplate} iniettato nei producer/controller.
     */

    // Generic consumer factory (Object)
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        var props = new HashMap<String, Object>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "it.alf,*");
        return new DefaultKafkaConsumerFactory<>(props, new org.apache.kafka.common.serialization.StringDeserializer(), new JsonDeserializer<>(Object.class, false));
    }

    // Specific consumer factories for typed DTOs
    @Bean
    public ConsumerFactory<String, User> userConsumerFactory() {
        var props = new HashMap<String, Object>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "it.alf,*");
        return new DefaultKafkaConsumerFactory<>(props, new org.apache.kafka.common.serialization.StringDeserializer(), new JsonDeserializer<>(User.class, false));
    }

    @Bean
    public ConsumerFactory<String, Order> orderConsumerFactory() {
        var props = new HashMap<String, Object>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "it.alf,*");
        return new DefaultKafkaConsumerFactory<>(props, new org.apache.kafka.common.serialization.StringDeserializer(), new JsonDeserializer<>(Order.class, false));
    }

    @Bean
    public ConsumerFactory<String, Notification> notificationConsumerFactory() {
        var props = new HashMap<String, Object>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "it.alf,*");
        return new DefaultKafkaConsumerFactory<>(props, new org.apache.kafka.common.serialization.StringDeserializer(), new JsonDeserializer<>(Notification.class, false));
    }

    @Bean
    public ConsumerFactory<String, GenericEvent> eventConsumerFactory() {
        var props = new HashMap<String, Object>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "it.alf,*");
        return new DefaultKafkaConsumerFactory<>(props, new org.apache.kafka.common.serialization.StringDeserializer(), new JsonDeserializer<>(GenericEvent.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, User> userListenerContainerFactory(KafkaTemplate<String, Object> template) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, User>();
        factory.setConsumerFactory(userConsumerFactory());
        factory.setCommonErrorHandler(defaultErrorHandler(template));
        return factory;
    }

    /**
     * Crea un ConcurrentKafkaListenerContainerFactory tipizzato per {@link it.alf.dto.User}.
     * Il factory imposta anche l'error handler comune che pubblica su DLQ in caso di errori non recuperabili.
     */

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Order> orderListenerContainerFactory(KafkaTemplate<String, Object> template) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Order>();
        factory.setConsumerFactory(orderConsumerFactory());
        factory.setCommonErrorHandler(defaultErrorHandler(template));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Notification> notificationListenerContainerFactory(KafkaTemplate<String, Object> template) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Notification>();
        factory.setConsumerFactory(notificationConsumerFactory());
        factory.setCommonErrorHandler(defaultErrorHandler(template));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GenericEvent> eventListenerContainerFactory(KafkaTemplate<String, Object> template) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, GenericEvent>();
        factory.setConsumerFactory(eventConsumerFactory());
        factory.setCommonErrorHandler(defaultErrorHandler(template));
        return factory;
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> template) {
        var recoverer = new DeadLetterPublishingRecoverer(template, (r, e) -> new org.apache.kafka.common.TopicPartition(dlqTopic, r.partition()));
        // retry 3 times with 1s backoff
        var backOff = new FixedBackOff(1000L, 3L);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    /**
     * Error handler comune che invia i record falliti sulla DLQ dopo un numero predefinito di retry.
     * Cambiare la strategia di backoff o il recoverer per adattarsi a policy di produzione diverse.
     */

    @Bean
    public NewTopic usersTopic() {
        return new NewTopic(usersTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic ordersTopic() {
        return new NewTopic(ordersTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic notificationsTopic() {
        return new NewTopic(notificationsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic eventsTopic() {
        return new NewTopic(eventsTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic dlqTopic() {
        return new NewTopic(dlqTopic, 1, (short) 1);
    }
}
