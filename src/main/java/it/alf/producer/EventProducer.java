package it.alf.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import it.alf.dto.GenericEvent;

@Service
public class EventProducer {
    private final Logger logger = LoggerFactory.getLogger(EventProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.events}")
    private String topic;

    public EventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(GenericEvent event) {
        logger.info("Sending event to topic {}: {}", topic, event.getId());
        kafkaTemplate.send(topic, event.getId(), event);
    }
}
