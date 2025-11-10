package it.alf.producer;

import it.alf.dto.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {
    private final Logger logger = LoggerFactory.getLogger(NotificationProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.notifications}")
    private String topic;

    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Notification notification) {
        logger.info("Sending notification to topic {}: {}", topic, notification.getId());
        kafkaTemplate.send(topic, notification.getId(), notification);
    }
}
