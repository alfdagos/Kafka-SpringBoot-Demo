package it.alf.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import it.alf.dto.Order;

@Service
public class OrderProducer {
    private final Logger logger = LoggerFactory.getLogger(OrderProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.orders}")
    private String topic;

    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Order order) {
        logger.info("Sending order to topic {}: {}", topic, order.getId());
        kafkaTemplate.send(topic, order.getId(), order);
    }
}
